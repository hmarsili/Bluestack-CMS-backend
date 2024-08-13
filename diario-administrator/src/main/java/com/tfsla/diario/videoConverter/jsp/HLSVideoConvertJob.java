package com.tfsla.diario.videoConverter.jsp;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.scheduler.I_CmsScheduledJob;

import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest.Builder;

import com.tfsla.diario.videoCollector.LuceneVideoCollector;

public class HLSVideoConvertJob implements I_CmsScheduledJob {

	@SuppressWarnings({ "rawtypes" })
	@Override
	public String launch(CmsObject cms, Map parameters) throws Exception {
		String publication = parameters.get("publication").toString();
		publishList = new ArrayList<CmsResource>();
		site = cms.getRequestContext().getSiteRoot();
		this.cms = cms;
		cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
		TfsVideosAdmin videosAdmin = new TfsVideosAdmin(cms, site, publication);
		if(!videosAdmin.isHLSConversionEnabled()) {
			return String.format("HLS format not available for site %s, publication %s", site, publication);
		}
		
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		amzAccessID = config.getParam(site, publication, "videoUpload", "amzAccessID", ""); 
		amzAccessKey = config.getParam(site, publication, "videoUpload", "amzAccessKey","");
		amzBucket = config.getParam(site, publication, "videoUpload", "amzBucket","");
		amzDirectory = config.getParam(site, publication, "videoUpload", "amzDirectory","");
		amzRegion = config.getParam(site, publication, "videoUpload", "amzRegion","");
		
		String hlsFormatName = "";
		HashMap<String, String> formats = config.getGroupParam(site, publication, "videoConvert", "formats");
		for(String key : formats.keySet()) {
			if(key.toLowerCase().contains("hls")) {
				hlsFormatName = key;
				break;
			}
		}
		
		if(hlsFormatName.equals("")) {
			throw new Exception(String.format("Cannot find a HLS format configured on 'videoConvert' module for site %s, publication %s", site, publication));
		}
		
		int daysBefore = Integer.parseInt(parameters.get("daysBefore").toString());
		LOG.info(String.format("Starting HLS video convert job, days before: %s", daysBefore));
		
		Map<String,Object> collectorParameters = new HashMap<String,Object>();
		long DAY_IN_MS = 1000 * 60 * 60 * 24;
		Date date = new Date(System.currentTimeMillis() - (DAY_IN_MS * daysBefore));
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		collectorParameters.put("from", fmt.format(date));
		collectorParameters.put("order", "modification-date desc");
		collectorParameters.put("publication", "1");
		collectorParameters.put("page", 1);
		collectorParameters.put("params-count", 5);
		collectorParameters.put("size", Integer.MAX_VALUE-1);
		collectorParameters.put("type","video-link");
		LuceneVideoCollector collector = new LuceneVideoCollector();
		
		List<CmsResource> videos = collector.collectVideos(collectorParameters, cms);
		LOG.info(String.format("Videos found: %s", videos.size()));
		int converted = 0;
		int errors = 0;
		int skipped = 0;
		
		for(CmsResource video : videos) {
			String videoPath = video.getRootPath().replace(site, "");
			int typeid = video.getTypeId();
			String typeName = OpenCms.getResourceManager().getResourceType(typeid).getTypeName();
			if(!typeName.equals("video") && !typeName.equals("video-link")) {
				LOG.info(String.format("Skipping video %s, type %s", videoPath, typeName));
				skipped++;
				continue;
			}
			
			CmsProperty prop = cms.readPropertyObject(video, "video-formats", false);
			if (prop != null) {
				String formatsProp = prop.getValue();
				if(formatsProp != null && formatsProp.toLowerCase().contains("hls")) {
					LOG.info(String.format("Video %s has HLS format", videoPath));
					skipped++;
					continue;
				}
				
				try {
					LOG.info(String.format("Adding HLS for video %s", videoPath));
					CmsFile file = cms.readFile(video);
					String sourceUrl = new String(file.getContents());
					if(!sourceUrl.contains("amazonaws.com")) {
						LOG.info(String.format("Moving %s to Amazon S3", videoPath));
						uploadS3(sourceUrl, file);
					}
					String hlsURI = videosAdmin.automaticEncodeAMZ(videoPath, hlsFormatName);
					converted++;
					
					formatsProp += (formatsProp == null || formatsProp.equals("") || formatsProp.equals("null") ? "" :  ", ") + hlsFormatName;
					prop = new CmsProperty("video-formats", formatsProp, formatsProp);
					stealLock(videoPath);
					cms.writePropertyObject(videoPath, prop);
					LOG.info(String.format("HLS generated for video %s at %s", videoPath, hlsURI));
					
					if(hlsURI != null) {
						cms.addRelationToResource(videoPath, hlsURI, "videoFormats");
					}
					
					publishList.add(video);
				} catch(Exception e) {
					LOG.error(String.format("Error generating HLS for video %s", videoPath), e);
					errors++;
				}
			}
		}
		
		if(publishList.size() > 0) {
			LOG.info(String.format("Publishing %s resources", publishList.size()));
			OpenCms.getPublishManager().publishProject(
				cms,
				new CmsLogReport(Locale.getDefault(), this.getClass()),
				OpenCms.getPublishManager().getPublishList(cms, publishList, true)
			);
		}
		
		LOG.info(String.format("HLS video convert job finished, videos converted: %s, skipped: %s, errors: %s", converted, skipped, errors));
		
		return null;
	}

	private void uploadS3(String sourceUrl, CmsFile file) throws Exception {
		
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(amzAccessID, amzAccessKey);

		S3Client s3 = null;
		
		if(amzRegion != null && !amzRegion.equals("")) {
			
			s3 = S3Client.builder()
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.region(Region.of(amzRegion))
				.build();
		}else {
			
			s3 = S3Client.builder()
					.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
					.build();
		}
		
		String sitePath = cms.getSitePath(file);
		if(sitePath.startsWith("/")) {
			sitePath = sitePath.substring(1);
		}
		String urlRegion = amzRegion.toLowerCase().replace("_", "-");
		String amzUrl = String.format("https://%s.s3.dualstack.%s.amazonaws.com/%s", amzBucket, urlRegion, sitePath);
		
		LOG.info(String.format("Amazon URL: %s", amzUrl));
		LOG.info(String.format("Site path: %s", sitePath));
		
		long contentLength = 0;
		HttpURLConnection conn = null;
		try {
	        conn = (HttpURLConnection) new URL(sourceUrl).openConnection();
	        conn.setRequestMethod("HEAD");
	        conn.getInputStream();
	        contentLength = conn.getContentLength();
	    } catch (IOException e) {
	    	LOG.error(String.format("Error getting content length from: %s", sourceUrl), e);
	    } finally {
	        conn.disconnect();
	    }
		
		InputStream content = new URL(sourceUrl).openStream();
		
		Builder putObjectRequestBuilder = PutObjectRequest.builder();
		putObjectRequestBuilder.bucket(amzBucket.replace("/", ""));
		putObjectRequestBuilder.contentLength(contentLength);
		putObjectRequestBuilder.acl(ObjectCannedACL.PUBLIC_READ);
		putObjectRequestBuilder.key(sitePath);
		s3.putObject( putObjectRequestBuilder.build(), RequestBody.fromInputStream(content, contentLength));

				
		LOG.info(String.format("Locking %s", sitePath));
		stealLock(cms.getSitePath(file));
		
		file.setContents(amzUrl.getBytes());
		cms.writeFile(file);
		
		LOG.info(String.format("Updated %s", sitePath));
		publishList.add(file);
	}
	
	protected void stealLock(String path) throws Exception {
		CmsLock lock = cms.getLock(path);
		if(!lock.isUnlocked()) {
			cms.changeLock(path);
			cms.unlockResource(path);
		}
		cms.lockResource(path);
	}

	protected CmsObject cms;
	protected Log LOG = CmsLog.getLog(this);
	protected String site = "";
	protected String amzDirectory = "";
	protected String amzBucket = "";
	protected String amzAccessID = "";
	protected String amzAccessKey = "";
	protected String amzRegion = "";
	protected List<CmsResource> publishList;
}
