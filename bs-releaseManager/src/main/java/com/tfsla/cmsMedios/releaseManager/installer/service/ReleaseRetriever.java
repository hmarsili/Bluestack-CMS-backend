package com.tfsla.cmsMedios.releaseManager.installer.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;

import net.sf.json.JSONObject;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Exception;

import com.tfsla.cmsMedios.releaseManager.installer.common.AmazonConfiguration;
import com.tfsla.cmsMedios.releaseManager.installer.common.ClusterNode;
import com.tfsla.cmsMedios.releaseManager.installer.common.ExceptionMessages;
import com.tfsla.cmsMedios.releaseManager.installer.common.ProxyConfiguration;
import com.tfsla.cmsMedios.releaseManager.installer.common.exceptions.ReleaseNameNotFoundException;
import com.tfsla.cmsMedios.releaseManager.installer.data.NodeReleasesDAO;

public class ReleaseRetriever {
	
	public static final String MANIFEST_FILE = "manifest.json";
	
	/**
	 * Provides a list of new releases available to be installed for current server (node)
	 * @param request Current HttpServletRequest
	 * @param amazonConfiguration Configuration for accessing Amazon bucket
	 * @return A list of String with available releases
	 * @throws SQLException
	 * @throws ReleaseNameNotFoundException
	 */
	public static synchronized List<String> checkForUpdates(HttpServletRequest request, AmazonConfiguration amazonConfiguration) throws SQLException, ReleaseNameNotFoundException {
		NodeReleasesDAO dao = new NodeReleasesDAO();
		try {
			dao.openConnection();
			ClusterNode currentRM = dao.getNodeByIP(org.opencms.configuration.uuid.IPSeeker.getIPAddress());
			return checkForUpdates(currentRM.getRM(), amazonConfiguration);
		} catch(Exception e) {
			throw e;
		} finally {
			dao.closeConnection();
		}
	}
	
	/**
	 * Provides a list of new releases available to be installed
	 * @param currentRM base RM to check releases available for
	 * @param amazonConfiguration Configuration for accessing Amazon bucket
	 * @return A list of String with available releases
	 * @throws ReleaseNameNotFoundException
	 */
	public static synchronized List<String> checkForUpdates(String currentRM, AmazonConfiguration amazonConfiguration) throws ReleaseNameNotFoundException {
		List<String> ret = new ArrayList<String>();
		List<String> releases = getReleaseNames(amazonConfiguration);
		int ix = releases.indexOf(currentRM);
		if(ix == -1) {
			throw new ReleaseNameNotFoundException(String.format(ExceptionMessages.ERROR_RELEASE_NAME_NOT_FOUND, currentRM));
		}
		
		for(int i=ix+1; i<releases.size(); i++) {
			ret.add(releases.get(i));
		}
		
		return ret;
	}
	
	/**
	 * Retrieves a list of release names by looking into a directory on a S3 bucket
	 * @param amazonConfiguration Configuration for accessing Amazon bucket
	 * @return A list of Strings with release names
	 */
	public static synchronized List<String> getReleaseNames(AmazonConfiguration amazonConfiguration) {
		S3Client s3Reader = getS3client(amazonConfiguration);
		
		String releasesDirectory = amazonConfiguration.getReleasesDirectory();
		SetupProgressService.reportProgress("Listing objects...");
		ListObjectsV2Request listObjectRequest = 
				ListObjectsV2Request.builder()
					.bucket(amazonConfiguration.getBucket())
					.prefix(releasesDirectory)
					.build();
		
		
		 ListObjectsV2Response resp = s3Reader.listObjectsV2(listObjectRequest);
		 
		List<String> releases = resp.contents().stream().map(x->x.key())
				.filter(x->x.contains(MANIFEST_FILE))
				.map(x->x.replace(MANIFEST_FILE,  "").replace(releasesDirectory, "").replace("/", ""))
				.collect(Collectors.toList());
		
		SetupProgressService.reportProgress(String.format("Done, returning %s releases", releases.size()));
		Collections.sort(releases, new RMNamesComparer());
		return releases;
	}
	
	/**
	 * Returns a JSON representing release manifest file (manifest.json)
	 * @param amazonConfiguration Configuration for accessing Amazon bucket
	 * @param releaseName Release Name
	 * @return A JSONObject representing release manifest file
	 * @throws IOException 
	 * @throws ReleaseNameNotFoundException 
	 */
	public static synchronized JSONObject getReleaseManifest(AmazonConfiguration amazonConfiguration, String releaseName) throws IOException, ReleaseNameNotFoundException {
		String releasesDirectory = amazonConfiguration.getReleasesDirectory();
		if(!releasesDirectory.endsWith("/")) {
			releasesDirectory += "/";
		}
		
		String fileKey = releasesDirectory + releaseName + "/" + MANIFEST_FILE;
		SetupProgressService.reportProgress(String.format("Retrieving %s from S3...", fileKey));
		
		try {
			S3Client s3Reader = getS3client(amazonConfiguration);
			
			ResponseInputStream<GetObjectResponse> object = s3Reader.getObject(
					GetObjectRequest.builder()
						.bucket(amazonConfiguration.getBucket())
						.key(fileKey)
						.build()
					);
			
			String jsonString = IOUtils.toString(object);
			JSONObject jsonObject = JSONObject.fromObject(jsonString);
			IOUtils.closeQuietly(object);
			
			SetupProgressService.reportProgress("Manifest downloaded for release " + releaseName);
			return jsonObject;
		} catch(S3Exception e) {
			SetupProgressService.error(e);
			throw new ReleaseNameNotFoundException(String.format(ExceptionMessages.ERROR_RELEASE_NAME_NOT_FOUND, releaseName));
		}
	}
	
	/**
	 * Returns a Stream to the zip file for the release
	 * @param amazonConfiguration Configuration for accessing Amazon bucket
	 * @param releaseName Release Name
	 * @return An InputStream with the zip file
	 * @throws IOException 
	 * @throws ReleaseNameNotFoundException 
	 */
	public static synchronized InputStream getReleaseFile(AmazonConfiguration amazonConfiguration, String releaseName) throws IOException, ReleaseNameNotFoundException {
		String releasesDirectory = amazonConfiguration.getReleasesDirectory();
		if(!releasesDirectory.endsWith("/")) {
			releasesDirectory += "/";
		}
		
		String fileKey = releasesDirectory + releaseName + "/" + releaseName + ".zip";
		SetupProgressService.reportProgress(String.format("Retrieving %s from S3...", fileKey));
		
		try {
			S3Client s3Reader = getS3client(amazonConfiguration);
			
			ResponseInputStream<GetObjectResponse> object = s3Reader.getObject(
					GetObjectRequest.builder()
						.bucket(amazonConfiguration.getBucket())
						.key(fileKey)
						.build()
					);
			//object.response()
			//InputStream objectData = object.getObjectContent();
			SetupProgressService.reportProgress("Download complete");
			return object;
		} catch(S3Exception e) {
			SetupProgressService.error(e);
			throw new ReleaseNameNotFoundException(String.format(ExceptionMessages.ERROR_RELEASE_NAME_NOT_FOUND, releaseName));
		}
	}
	
	public static synchronized String getPublicURLFile(AmazonConfiguration amazonConfiguration, String releaseName,String fileName) throws IOException, ReleaseNameNotFoundException {
		
		String releasesDirectory = amazonConfiguration.getReleasesDirectory();
		if(!releasesDirectory.endsWith("/")) {
			releasesDirectory += "/";
		}
		
		String bucket = amazonConfiguration.getBucket();
		
		String fileKey = releasesDirectory + releaseName + "/"+fileName;
		SetupProgressService.reportProgress(String.format("Retrieving %s from S3...", fileKey));
		
		try {
			S3Client s3Reader = getS3client(amazonConfiguration);
			String filePath = s3Reader.utilities().getUrl(GetUrlRequest.builder().bucket(bucket).key(fileKey).build()).toString();
			
			SetupProgressService.reportProgress("Download complete");
			
			return filePath;
		} catch(S3Exception e) {
			SetupProgressService.error(e);
			throw new ReleaseNameNotFoundException(String.format(ExceptionMessages.ERROR_RELEASE_NAME_NOT_FOUND, releaseName));
		}
	}
	
	protected static S3Client getS3client(AmazonConfiguration amazonConfiguration) {
		SetupProgressService.reportProgress("Logging into Amazon...");
		
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(amazonConfiguration.getAccessID(), amazonConfiguration.getAccessKey());
		S3ClientBuilder s3builder = S3Client.builder()
			.credentialsProvider(StaticCredentialsProvider.create(awsCreds));
			
		
		//AmazonS3ClientBuilder s3builder = AmazonS3Client.builder(); 
		//s3builder.withCredentials(new AWSStaticCredentialsProvider(awsCreds));
		if (amazonConfiguration.useProxy()) {
			
			//ClientConfiguration config = new ClientConfiguration();
			
			software.amazon.awssdk.http.apache.ProxyConfiguration.Builder awsProxyConfig = software.amazon.awssdk.http.apache.ProxyConfiguration.builder();
			
			ProxyConfiguration proxyConfig = amazonConfiguration.getProxyConfiguration();
			if (proxyConfig.getPreemptiveBasicAuth() !=null && proxyConfig.getPreemptiveBasicAuth()) {
				awsProxyConfig.preemptiveBasicAuthenticationEnabled(true);
			}
			if (proxyConfig.getDomain() != null && !proxyConfig.getDomain().equals("")) {
				awsProxyConfig.ntlmDomain(proxyConfig.getDomain());
			}
			if (proxyConfig.getNonProxyHosts() != null && !proxyConfig.getNonProxyHosts().equals("")) {
				awsProxyConfig.addNonProxyHost(proxyConfig.getNonProxyHosts());
			}
			if (proxyConfig.getPassword() != null && !proxyConfig.getPassword().equals("")) {
				awsProxyConfig.password(proxyConfig.getPassword());
			}
			if (proxyConfig.getUsername() != null && !proxyConfig.getUsername().equals("")) {
				awsProxyConfig.username(proxyConfig.getUsername());
			}
			if (proxyConfig.getWorkstation() != null && !proxyConfig.getWorkstation().equals("")) {
				awsProxyConfig.ntlmWorkstation(proxyConfig.getWorkstation());
			}
			if (proxyConfig.getHost() != null && !proxyConfig.getHost().equals("")) {
			
				String host = proxyConfig.getHost();
				if (proxyConfig.getPort() > 0)
					host +=":"+ proxyConfig.getPort();
				
				awsProxyConfig.endpoint(URI.create(host));
				
			}
			ApacheHttpClient.Builder httpClientBuilder =
			        ApacheHttpClient.builder()
			                        .proxyConfiguration(awsProxyConfig.build());

			ClientOverrideConfiguration.Builder overrideConfig =
			        ClientOverrideConfiguration.builder();
			
			s3builder.httpClientBuilder(httpClientBuilder);
		}
		if (amazonConfiguration.getRegion() != null && !amazonConfiguration.getRegion().equals("")) {
			s3builder.region(Region.of(amazonConfiguration.getRegion()));
		}
		return s3builder.build();
	}
}