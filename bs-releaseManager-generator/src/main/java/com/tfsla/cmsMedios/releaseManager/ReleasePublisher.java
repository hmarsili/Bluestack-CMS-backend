package com.tfsla.cmsMedios.releaseManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import com.tfsla.cmsMedios.releaseManager.common.AmazonConfiguration;
import com.tfsla.cmsMedios.releaseManager.common.GenerateReleaseRequest;
import com.tfsla.cmsMedios.releaseManager.github.common.InvalidManifestException;
import com.tfsla.cmsMedios.releaseManager.github.common.ReleaseCMS;
import com.tfsla.cmsMedios.releaseManager.github.service.ReleaseGenerator;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest.Builder;

public class ReleasePublisher {
	
	public static void publishRelease(GenerateReleaseRequest releaseRequest, AmazonConfiguration amazonConfiguration) throws IOException, InvalidManifestException {
		ReleaseCMS release = ReleaseGenerator.generate(releaseRequest);
		
		System.out.println("Authenticating on Amazon...");
		
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(amazonConfiguration.getAccessID(), amazonConfiguration.getAccessKey());

		S3Client s3 = null;
		
		if(amazonConfiguration.getRegion() != null && !amazonConfiguration.getRegion().equals("")) {
			
			s3 = S3Client.builder()
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.region(Region.of(amazonConfiguration.getRegion()))
				.build();
		}else {
			
			s3 = S3Client.builder()
					.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
					.build();
		}
		
		
		String releasePath = String.format("%s/%s", releaseRequest.getReleasesDirectory(), release.getReleaseName());
		System.out.println("Uploading release package to S3 at " + releasePath + ", please wait...");
		
		Builder putObjectRequestBuilder = PutObjectRequest.builder();
		putObjectRequestBuilder.bucket(amazonConfiguration.getBucket());
		
		File releaseZip = new File(release.getReleasePath());

		putObjectRequestBuilder.contentType("application/zip");
		putObjectRequestBuilder.contentLength(releaseZip.length());
		putObjectRequestBuilder.acl(ObjectCannedACL.PRIVATE);
		putObjectRequestBuilder.key(releasePath + "/" + release.getReleaseName() + ".zip");
		s3.putObject( putObjectRequestBuilder.build(), RequestBody.fromFile(releaseZip));
		
		System.out.println("Uploading readme.txt file...");
		String rmBaseDir = ReleaseGenerator.getRMBaseDir();
		
		putObjectRequestBuilder = PutObjectRequest.builder();
		putObjectRequestBuilder.bucket(amazonConfiguration.getBucket());
			
		File readmeText = new File(rmBaseDir+"/config/readme.txt");

		putObjectRequestBuilder.contentType("application/zip");
		putObjectRequestBuilder.contentLength(readmeText.length());
		putObjectRequestBuilder.acl(ObjectCannedACL.PUBLIC_READ);
		putObjectRequestBuilder.key(releasePath + "/readme.txt");
		s3.putObject( putObjectRequestBuilder.build(), RequestBody.fromFile(readmeText));
	
			
		System.out.println("Uploading attached files...");
		List<String> attachedFiles = ReleaseGenerator.getAttachedFiles();
		for (String attachedFile : attachedFiles) {
			
			putObjectRequestBuilder = PutObjectRequest.builder();
			putObjectRequestBuilder.bucket(amazonConfiguration.getBucket());
			
			File attached = new File(rmBaseDir+"/attachedFiles/"+attachedFile);

			//putObjectRequestBuilder.contentType("application/json");
			putObjectRequestBuilder.contentLength(attached.length());
			putObjectRequestBuilder.acl(ObjectCannedACL.PUBLIC_READ);
			putObjectRequestBuilder.key(releasePath + "/"+attachedFile);
			s3.putObject( putObjectRequestBuilder.build(), RequestBody.fromFile(attached));
			
		}

		System.out.println("Writing manifest...");
		
		putObjectRequestBuilder = PutObjectRequest.builder();
		putObjectRequestBuilder.bucket(amazonConfiguration.getBucket());
		
		File manifestFile = new File(release.getManifestPath());

		putObjectRequestBuilder.contentType("application/json");
		putObjectRequestBuilder.contentLength(manifestFile.length());
		//putObjectRequestBuilder.acl(ObjectCannedACL.PRIVATE);
		putObjectRequestBuilder.key(releasePath + "/manifest.json");
		s3.putObject( putObjectRequestBuilder.build(), RequestBody.fromFile(manifestFile));
		
		System.out.println("Process complete!");
	}
}
