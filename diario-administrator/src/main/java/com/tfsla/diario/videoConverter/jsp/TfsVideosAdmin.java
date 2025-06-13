package com.tfsla.diario.videoConverter.jsp;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;  
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.flex.CmsFlexController;
import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.URI;

import com.tfsla.diario.auditActions.data.TfsAuditActionDAO;
import com.tfsla.diario.auditActions.model.TfsAuditAction;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.file.types.TfsResourceTypeVideoLink;
import com.tfsla.diario.file.types.TfsResourceTypeVideoVodLink;
import com.tfsla.diario.videoConverter.ConverterLogger;
import com.tfsla.diario.videoConverter.Encoder;
import com.tfsla.diario.videoConverter.EncoderException;
import com.tfsla.diario.videoConverter.EncoderProgressListener;
import com.tfsla.diario.videoConverter.InputFormatException;
import com.tfsla.diario.videoConverter.MultimediaInfo;
import com.tfsla.diario.videoConverter.VideoInfo;
import com.tfsla.diario.videoConverter.VideoSize;
import com.tfsla.diario.videoConverter.jsp.amazon.AudioCodecHelper;
import com.tfsla.diario.videoConverter.jsp.amazon.JobStatusNotification;
import com.tfsla.diario.videoConverter.jsp.amazon.JobStatusNotification.JobState;
import com.tfsla.diario.videoConverter.jsp.amazon.JobStatusNotificationHandler;
import com.tfsla.diario.videoConverter.jsp.amazon.SqsQueueNotificationWorker;
import com.tfsla.utils.CmsResourceUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.services.elastictranscoder.ElasticTranscoderClient;
import software.amazon.awssdk.services.elastictranscoder.model.CreateJobOutput;
import software.amazon.awssdk.services.elastictranscoder.model.CreateJobPlaylist;
import software.amazon.awssdk.services.elastictranscoder.model.CreateJobRequest;
import software.amazon.awssdk.services.elastictranscoder.model.JobInput;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest.Builder;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;  
import software.amazon.awssdk.services.sqs.SqsClient;

import org.opencms.report.CmsLogReport;
import org.opencms.util.CmsFileUtil;
import org.opencms.workplace.CmsWorkplaceAction;

public class TfsVideosAdmin implements EncoderProgressListener {
	
	private CPMConfig config;
	private String siteName;
    private String publication;
    private TipoEdicion currentPublication;
    private String moduleConfigName;
    private CmsFlexController m_controller;
    private HttpSession m_session;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private PageContext m_context;
    private CmsObject m_cms;
    private String moduleVideoConfig = "videoUpload";
    private ConverterLogger LOG = new ConverterLogger();
    private String videoType = "";
    protected static final Log logger = CmsLog.getLog(TfsVideosAdmin.class);


    public TfsVideosAdmin(CmsObject cms, String site, String publicationId) throws Exception {
    	m_cms = cms;
    	siteName = site;
    	publication = publicationId;
    	moduleConfigName = "videoConvert";
    	config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
    }
    
	public TfsVideosAdmin(PageContext context, HttpServletRequest req, HttpServletResponse res) throws Exception {
		m_controller = CmsFlexController.getController(req);
		m_session = req.getSession();
		request = req;
		response = res;
		m_context = context;
		
		siteName = OpenCms.getSiteManager().getCurrentSite(getCmsObject()).getSiteRoot();
		currentPublication = (TipoEdicion) m_session.getAttribute("currentPublication");

    	if (currentPublication==null) {
        	TipoEdicionService tService = new TipoEdicionService();

    		currentPublication = tService.obtenerEdicionOnlineRoot(siteName);
    		m_session.setAttribute("currentPublication",currentPublication);
    	}
    	
    	publication = "" + currentPublication.getId();
    	moduleConfigName = "videoConvert";
    	if (request.getParameter("moduleConfig")!= null && !request.getParameter("moduleConfig").equals("") 
    			&& request.getParameter("moduleConvertConfig") != null && !request.getParameter("moduleConvertConfig").equals("")) {
    		moduleVideoConfig = request.getParameter("moduleConfig");
    		moduleConfigName =request.getParameter("moduleConvertConfig");
    	}
    	if (request.getParameter ("videoType")!= null && !request.getParameter("videoType").equals("")){
    		videoType = request.getParameter ("videoType");
    	}
    	config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
    }
	
	

	@SuppressWarnings("rawtypes")
	public List<String> getConfiguredFormats() {
		LinkedHashMap<String,String> values = config.getGroupParam(siteName, publication, moduleConfigName, "formats");
		
		Set formats = values.keySet();
		Iterator it = formats.iterator();
		
		List<String> formatsList = new ArrayList<String>();
		
		while( it.hasNext()) {
			formatsList.add((String)it.next());
		}
		
		return formatsList;
    }
	
	public Boolean getForceConversion() {
		String paramValue = config.getParam(siteName, publication, moduleConfigName, "forceConversion");
		if(paramValue == null || paramValue.equals("")) return false;
		
		return Boolean.parseBoolean(paramValue);
	}
	
	public Boolean isHLSConversionEnabled() {
		Set<String> formats = config.getGroupParam(siteName, publication, moduleConfigName, "formats").keySet();
		for(String format : formats) {
			if(format.toLowerCase().contains("hls")) return true;
		}
		return false;
	}
	
	public CmsObject getCmsObject() {
		if(m_cms!=null)
			return m_cms;
		else
	        return m_controller.getCmsObject();
	}
	
	public String automaticEncodeAMZ(String sourceVFSPath, String formatName) throws CmsException, IOException {
		String result = null;
		try {
			CmsObject cmsObj = getCmsObject();
			
			String amzDirectory = config.getParam(siteName, publication, moduleVideoConfig, "amzDirectory","");
			String amzBucket = config.getParam(siteName, publication, moduleVideoConfig, "amzBucket","");
			String amzAccessID  = config.getParam(siteName, publication, moduleVideoConfig, "amzAccessID","");
			String amzAccessKey = config.getParam(siteName, publication, moduleVideoConfig, "amzAccessKey","");
			String amzRegion = config.getParam(siteName, publication, moduleVideoConfig, "amzRegion","");
			String amzPipelineID = config.getParam(siteName, publication, moduleVideoConfig, "amzPipelineID","");
			String amzQueueURL = config.getParam(siteName, publication, moduleVideoConfig, "amzQueueURL","");
			String timeOutSt =  config.getParam(siteName, publication, "videoConvert", "timeoutMin","5");
			int timeOut = Integer.valueOf(timeOutSt);
			
			
			Boolean isHLS = formatName.toLowerCase().contains("hls");
			CmsResource resource = cmsObj.readResource(sourceVFSPath,CmsResourceFilter.ALL);
			CmsFile file = cmsObj.readFile(resource);
			String sourceUrl = new String(file.getContents());
			String outputFormat = isHLS ? "" : getOutputFormat(formatName);
			String prefix = isHLS ? "" : getPrefix(formatName);
			String targetName = getOutputFileName(sourceVFSPath, outputFormat, prefix);
			String subFolderVFS = sourceVFSPath.substring(0, sourceVFSPath.lastIndexOf("/"));
			String defaultVfsFolder = config.getParam(siteName, publication, moduleVideoConfig, "defaultVideoFlashPath","");
			String linkFolderName = isHLS ? getOptionParamGroup(formatName, "folder") : getFolderDestination(formatName, "AMZ");
			if(sourceVFSPath.indexOf(defaultVfsFolder) > -1) {
				subFolderVFS = subFolderVFS.replace("/" + defaultVfsFolder, "");
			}
			
			String targetAMZPath = amzDirectory + "/" + linkFolderName + subFolderVFS + "/" + targetName;
			
			try {
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
				
				
				if(!sourceUrl.contains("amazonaws.com")) {
					sourceUrl = uploadS3(sourceUrl, file, amzBucket, amzRegion, awsCreds, cmsObj);
				}
				
				int ix = 0;
				if(sourceUrl.contains(amzBucket + "/")) {
					ix = sourceUrl.indexOf(amzBucket) + amzBucket.length() + 1;
				} else {
					String amzDomain = "amazonaws.com";
					ix = sourceUrl.indexOf(amzDomain) + amzDomain.length() + 1;
				}
				
				String key = sourceUrl.substring(ix);
				String targetAmzUrl = sourceUrl.substring(0, ix) + targetAMZPath;
				
				LOG.log("Region: "+amzRegion);
				LOG.log("Format Name: "+formatName);
				LOG.log("Key: " + key);
				
				SqsClient amazonSqs = SqsClient.builder()
						.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
						.build();
				
				SqsQueueNotificationWorker sqsQueueNotificationWorker = new SqsQueueNotificationWorker(amazonSqs, amzQueueURL, LOG);
				Thread notificationThread = new Thread(sqsQueueNotificationWorker);
		        notificationThread.start();
				
				JobInput input = JobInput.builder().key(key).build();
				List<CreateJobOutput> outputs = new ArrayList<CreateJobOutput>();
				CreateJobPlaylist playlist = null;
				if(isHLS) {
					LOG.log("HLS transcoding");
					Boolean hasAudio = AudioCodecHelper.videoHasAudio(sourceUrl);
					LOG.log("Has Audio: " + hasAudio);
					String segmentDuration = getOptionParamGroup(formatName, "segmentDuration");
					String format = getOptionParamGroup(formatName, "format");
					String presets = getOptionParamGroup(formatName, "presets");
					Collection<String> outputKeys = new ArrayList<String>();
					for(String preset : presets.split(";")) {
						if(!hasAudio && preset.contains("audio")) continue;
						String presetName = preset.substring(0, preset.indexOf("="));
						String presetID = preset.substring(preset.indexOf("=")+1);
						String presetTarget = amzDirectory + "/" + linkFolderName + subFolderVFS + "/" + targetName + "_" + presetName;
						
						CreateJobOutput output = CreateJobOutput.builder()
				            .key(presetTarget)
				            .segmentDuration(segmentDuration)
				            .presetId(presetID)
				            .build();
						
						outputs.add(output);
						outputKeys.add(output.key());
						LOG.log("Adding Preset ID: " + presetID + ", name: " + presetName);
						LOG.log("Target: " + presetTarget);
					}
					
					LOG.log("Done loading presets, playlist target: " + targetAMZPath);
					
					playlist = CreateJobPlaylist.builder()
			            .name(targetAMZPath)
			            .format(format)
			            .outputKeys(outputKeys)
			            .build();
				} else {
					String amzPresetID = getParamFormat(formatName, "presetAMZ");
					LOG.log("Target Amz Path: " + targetAMZPath);
					LOG.log("Preset ID: " + amzPresetID);
					
			        CreateJobOutput output = CreateJobOutput.builder()
			            .key(targetAMZPath)
			            .presetId(amzPresetID)
			            .build();
			        outputs.add(output);
				}
				
		        LOG.log("Pipeline ID: " + amzPipelineID);
		        LOG.log("Queue URL: " + amzQueueURL);
		        CreateJobRequest createJobRequest = null;
		        
		        if(isHLS) {
		        	createJobRequest = CreateJobRequest.builder()
			            .pipelineId(amzPipelineID)
			            .input(input)
			            .outputs(outputs)
			            .playlists(playlist)
			            .build();
		        }
		        else {
		        	createJobRequest = CreateJobRequest.builder()
			            .pipelineId(amzPipelineID)
			            .input(input)
			            .outputs(outputs)
			            .build();
			     }
		        
		        ElasticTranscoderClient amazonElasticTranscoder = null;
		        if(amzRegion == null || amzRegion.equals("")) {
		        	amazonElasticTranscoder = ElasticTranscoderClient.builder()
			        		.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
			        		.region(Region.of(amzRegion))
			        		.build();
		        	
		        }
		        else {
		        	amazonElasticTranscoder = ElasticTranscoderClient.builder()
			        		.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
			        		.region(Region.of(amzRegion))
			        		.build();
		        }
		        
		        final String jobID = amazonElasticTranscoder.createJob(createJobRequest).job().id();
		        LOG.log("Created transcoding job with id " + jobID);
		        JobStatusNotificationHandler handler = new JobStatusNotificationHandler() {
		            
		        	public Boolean hasError = false;
		        	private String message = "";
		        	
		            @Override
		            public void handle(JobStatusNotification jobStatusNotification) {
		            	LOG.log(String.format("Received notification, jobID: %s, notif. jobID: %s, notif detail: %s",
		            			jobID, jobStatusNotification.getJobId(), jobStatusNotification.toString()));
		                //if (jobStatusNotification.getJobId().equals(jobID)) {
		                    LOG.log("Handler notification: " + jobStatusNotification);
		                    if(jobStatusNotification.getState().equals(JobState.ERROR)) {
		                    	hasError = true;
		                    	message = jobStatusNotification.toString();
		                    	if (jobStatusNotification.getOutputs()!= null && jobStatusNotification.getOutputs().size()>0 && jobStatusNotification.getOutputs().get(0).getErrorCode() == 3002){
		                    		//el archivo existe
		                    		hasError = false;
		                    	}
		                    	LOG.log("ERROR while transcoding file: " + message);
		                    }
		                    if (jobStatusNotification.getState().isTerminalState()) {
		                        synchronized(this) {
		                            this.notifyAll();
		                            LOG.log("Handler notification: TERMINAL STATE");
		                        }
		                    }
		                //}
		            }
		            
		            @Override
		            public Boolean hasError() {
		            	return this.hasError;
		            }
		            
		            @Override
		            public String getMessage() {
		            	return this.message;
		            }
		        };
		        sqsQueueNotificationWorker.addHandler(handler);
		        
		        LOG.log("Waiting for job to complete...");
		        synchronized(handler) {
		            try {
						handler.wait(timeOut * 60 * 1000 * 3);
						LOG.log("Finished Encoding");
					} catch (InterruptedException e) {
						LOG.log("ERROR","Finished Encoding - " + e.getMessage());
					}
		        }
		        
		        LOG.log("Shutting down queue worker...");
		       
		        // When job completes, shutdown the sqs notification worker.
		        sqsQueueNotificationWorker.shutdown();
		        LOG.log("Done queue worker shut down!");
		        
		        if(handler.hasError()) {
		        	throw new Exception("Error transcoding video: " + handler.getMessage());
		        }
		        
				// Subo el link al vfs	
				String linkName = "/" + linkFolderName + subFolderVFS + "/" + targetName;
				checkFoldersExistVFS(linkFolderName + subFolderVFS);  
				
				try {
					List<CmsProperty> properties = new ArrayList<CmsProperty>();
					if(!isHLS) {
						LOG.log("Retrieving video properties from " + targetAmzUrl);
						String tmpFolder = config.getParam(siteName, publication, moduleConfigName, "tempFolder","");
						String targetTempPath = tmpFolder + "/" + targetName;
						File localFile = new File(targetTempPath);
						
						ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(GetObjectRequest.builder()
								.bucket(amzBucket.replace("/", ""))
								.key(targetAMZPath)
								.build());
						
			            byte[] data = objectBytes.asByteArray();

			            
			            OutputStream os = new FileOutputStream(localFile);
			            os.write(data);
			            os.close();
			            
						properties = getPropertiesConvertedVideo(targetTempPath, formatName);
					} else {
						linkName += ".m3u8";
						targetAmzUrl += ".m3u8";
						
						CmsProperty prop = new CmsProperty();
						prop.setName("video-format");
						prop.setAutoCreatePropertyDefinition(true);
						prop.setStructureValue(formatName);
						properties.add(prop);
					}
					
					LOG.log("AmzUrl: " + targetAmzUrl);
					LOG.log("Creating resource in VFS " + linkName);
					
					if (!cmsObj.existsResource(linkName)) {
						int type = OpenCms.getResourceManager().getResourceType("video-link").getTypeId();
						if (!videoType.equals("") || resource.getTypeId() == TfsResourceTypeVideoVodLink.getStaticTypeId()) {
							type = TfsResourceTypeVideoVodLink.getStaticTypeId();
						}
						cmsObj.createResource(linkName, 
							type,
							targetAmzUrl.getBytes(),
							properties);
					}
					result = linkName;
				} catch (CmsIllegalArgumentException e) {
					LOG.log("Video Convert - Error al generar el link en el vfs: " + e.getMessage());
				} catch (CmsLoaderException e) {
					LOG.log("Video Convert - Error al generar el link en el vfs: " + e.getMessage());
				} catch (CmsException e) {
					LOG.log("Video Convert - Error al generar el link en el vfs: " + e.getMessage());
				}
			} catch (Exception e) {
				CmsLog.getLog(this).error("Video Convert - Error", e);
				LOG.log("ERROR: ", e.getMessage());
			}
		} catch(Exception e) {
			CmsLog.getLog(this).error("Video Convert - General Error", e);
			LOG.log("GENERAL ERROR: ", e.getMessage());
		}
		return result;
	}
	
	private String uploadS3(String sourceUrl, CmsFile file, String amzBucket, String amzRegion, AwsBasicCredentials awsCreds, CmsObject cms) throws Exception {
		LOG.log(String.format("Moving %s to Amazon S3", sourceUrl));
		S3Client s3;
		
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
		
		LOG.log(String.format("Amazon URL: %s", amzUrl));
		LOG.log(String.format("Site path: %s", sitePath));
		
		long contentLength = 0;
		HttpURLConnection conn = null;
		try {
	        conn = (HttpURLConnection) new URL(sourceUrl).openConnection();
	        conn.setRequestMethod("HEAD");
	        conn.getInputStream();
	        contentLength = conn.getContentLength();
	    } catch (IOException e) {
	    	LOG.log(String.format("Error getting content length from: %s", sourceUrl));
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

		
		LOG.log(String.format("Locking %s", sitePath));
		String path = cms.getSitePath(file);
		CmsLock lock = cms.getLock(path);
		if(!lock.isUnlocked()) {
			cms.changeLock(path);
			cms.unlockResource(path);
		}
		cms.lockResource(path);
		
		file.setContents(amzUrl.getBytes());
		cms.writeFile(file);
		
		LOG.log(String.format("Updated %s", sitePath));
		OpenCms.getPublishManager().publishResource(cms, path);
		
		LOG.log(String.format("Published %s", path));
		return amzUrl;
	}
	
	public String automaticEncodeRFS(String sourceVFSPath, String formatName) throws CmsException{
		String result = null;
		
		CmsObject cmsObj = getCmsObject();
		
		CmsResource resource = cmsObj.readResource(sourceVFSPath);
		CmsFile 	    file = cmsObj.readFile(resource);
		String    sourcePath = new String(file.getContents());
		
		String outputFormat  = getOutputFormat(formatName);
		String       prefix  = getPrefix(formatName);
		String ffmpegOptions = getFormatOptions(formatName);
		
		String targetPath = null;
		
		String folderTarget  = getFolderDestination(formatName,"RFS");

		String  rfsDirectory = config.getParam(siteName, publication, moduleVideoConfig, "rfsDirectory","");
		String rfsVirtualUrl = config.getParam(siteName, publication, moduleVideoConfig, "rfsVirtualUrl","");
			
		sourcePath = sourcePath.replace(rfsVirtualUrl, rfsDirectory+"/" );
			
		String subFolder = getOutputSubFolder(sourcePath, "RFS", formatName);
			
		String targetName = getOutputFileName(sourcePath, outputFormat, prefix);
			   targetPath = rfsDirectory+"/"+ folderTarget + subFolder + "/" + targetName;
			   
		String targetUrl = 	rfsVirtualUrl + folderTarget + subFolder + "/" + targetName;  
			         
		checkFileExistRFS(targetPath);   
		
		LOG.log("Source Path: "+sourcePath);
		LOG.log("Target Path: "+targetPath);
			 
		File source = new File(sourcePath);
		File target = new File(targetPath);      
		
		Encoder encoder = new Encoder();
		
		try {
		    long duration = getVideoDuration(sourcePath);
		    
		    LOG.log("Preparing for start enconding ...");
		    LOG.log("Duration of sourcePath: "+duration);
		    
			encoder.automaticEncode(source, target, ffmpegOptions,this, duration, LOG);
			
			String defaultVfsFolder = config.getParam(siteName, publication, moduleVideoConfig, "defaultVideoFlashPath","");
			
			String linkFolderName = getFolderDestination(formatName,"VFS");
			String subFolderVFS = sourceVFSPath.substring(0, sourceVFSPath.lastIndexOf("/") );
			
			if(sourceVFSPath.indexOf(defaultVfsFolder)>-1)
				subFolderVFS = subFolderVFS.replace("/"+defaultVfsFolder,"");
			
			String linkName = "/"+linkFolderName + subFolderVFS +"/"+ targetName;
			
			checkFoldersExistVFS(linkFolderName + subFolderVFS);  
			
			List<CmsProperty> properties = getPropertiesConvertedVideo(targetPath, formatName);
			
			try {
				LOG.log("Creating resource in VFS "+linkName);
				
				int type = OpenCms.getResourceManager().getResourceType("video-link").getTypeId();
				if (!videoType.equals("") || resource.getTypeId() == TfsResourceTypeVideoVodLink.getStaticTypeId()) {
					type = TfsResourceTypeVideoVodLink.getStaticTypeId();
				}
				cmsObj.createResource(linkName, 
						type,
						targetUrl.getBytes(),
						properties);
				
				result = linkName;
			} catch (CmsIllegalArgumentException e) {
				CmsLog.getLog(this).error("Video Convert - Error al generar el link en el vfs: "+e.getMessage());
			} catch (CmsLoaderException e) {
				CmsLog.getLog(this).error("Video Convert - Error al generar el link en el vfs: "+e.getMessage());
			} catch (CmsException e) {
				CmsLog.getLog(this).error("Video Convert - Error al generar el link en el vfs: "+e.getMessage());
			}
		} catch (IllegalArgumentException e) {
				CmsLog.getLog(this).error("Video Convert - Error: "+e.getMessage());
				LOG.log("ERROR",e.getMessage());
		} catch (InputFormatException e) {
				CmsLog.getLog(this).error("Video Convert - Error: "+e.getMessage());
				LOG.log("ERROR", e.getMessage());
		} catch (EncoderException e) {
				CmsLog.getLog(this).error("Video Convert - Error: "+e.getMessage());
				LOG.log("ERROR", e.getMessage());
		} 
		
		return result;
	}
	
	public String automaticEncodeFTP(String sourceVFSPath, String formatName) throws CmsException, IOException{
		String result = null;
		
		CmsObject cmsObj = getCmsObject();
		
		String ftpServer = config.getParam(siteName, publication, moduleVideoConfig, "ftpServer","");
		String ftpUser = config.getParam(siteName, publication, moduleVideoConfig, "ftpUser","");
		String ftpPassword  = config.getParam(siteName, publication, moduleVideoConfig, "ftpPassword","");
		String ftpDirectory = config.getParam(siteName, publication, moduleVideoConfig, "ftpDirectory","");
		
		String tmpFolder = config.getParam(siteName, publication, moduleConfigName, "tempFolder","");
		
		CmsResource resource = cmsObj.readResource(sourceVFSPath);
		CmsFile 	    file = cmsObj.readFile(resource);
		String    sourcePath = new String(file.getContents());
		
		String sourceFileName = sourcePath.substring(sourcePath.lastIndexOf("/")+1); 

		String outputFormat  = getOutputFormat(formatName);
		String       prefix  = getPrefix(formatName);
		String ffmpegOptions = getFormatOptions(formatName);
		
		String      targetName = getOutputFileName(sourceVFSPath, outputFormat, prefix);
		String    targetFolder = getFolderDestination(formatName,"RFS");
		String targetSubFolder = getOutputSubFolder(sourceVFSPath, "RFS", formatName);
		
		String targetFTPPath = "/"+targetFolder +  targetSubFolder + "/" + targetName;
		
		String targetTempPath = tmpFolder + "/" + targetName;
		
		String  sourceTmpName = sourceVFSPath.substring(sourceVFSPath.lastIndexOf("/")+1);
		String sourceTempPath = tmpFolder + "/" + sourceTmpName;
		
		FTPClient client = new FTPClient();
		client.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

		try {
			// Paso el archivo del ftp a un tmp del server
			client.connect(ftpServer);
			client.login(ftpUser, ftpPassword);
			client.changeWorkingDirectory(ftpDirectory);
			
			client.enterLocalPassiveMode();
			
			InputStream is = client.retrieveFileStream(sourceFileName);
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(sourceTempPath));
			int c;
			while ((c = is.read()) != -1) {
				bw.write(c);
			}
			is.close();
			bw.flush();
			bw.close();
			
			client.logout();
			client.disconnect();
			
			// Con el archivo en el servidor empieza el encoder
			checkFileExistRFS(targetTempPath); 
			
			LOG.log("Source Path: "+sourceTempPath);
			LOG.log("Target Path: "+targetTempPath);
			
			File source = new File(sourceTempPath);
			File target = new File(targetTempPath);
			
			Encoder encoder = new Encoder();
			
			long duration = getVideoDuration(sourceTempPath);
			
			LOG.log("Preparing for start enconding ...");
			LOG.log("Duration of sourcePath: "+duration);
			    
			encoder.automaticEncode(source, target, ffmpegOptions,this, duration,LOG);
			
			// Subo el temporal generado al ftp
			client.connect(ftpServer);
			client.login(ftpUser, ftpPassword);
			client.changeWorkingDirectory(ftpDirectory);
			
			client.enterLocalPassiveMode();
			client.setFileType(FTPClient.BINARY_FILE_TYPE);

			client.connect(ftpServer);
			client.login(ftpUser, ftpPassword);
			client.changeWorkingDirectory(ftpDirectory);

			
			client.enterLocalPassiveMode();
			client.setFileType(FTPClient.BINARY_FILE_TYPE);

			BufferedInputStream buffIn = null;
	        buffIn = new BufferedInputStream(new FileInputStream(targetTempPath));
	        client.storeFile(targetFTPPath, buffIn);
	            
	        buffIn.close();  
	        client.logout();  
	        client.disconnect(); 
			
			// Subo el link al vfs	
	        String defaultVfsFolder = config.getParam(siteName, publication, moduleVideoConfig, "defaultVideoFlashPath","");
			
			String linkFolderName = getFolderDestination(formatName,"VFS");
			String subFolderVFS = sourceVFSPath.substring(0, sourceVFSPath.lastIndexOf("/") );
			
			if(sourceVFSPath.indexOf(defaultVfsFolder)>-1)
				subFolderVFS = subFolderVFS.replace("/"+defaultVfsFolder,"");
			
			String linkName = "/"+linkFolderName + subFolderVFS +"/"+ targetName;
			
			checkFoldersExistVFS(linkFolderName + subFolderVFS);  
			
			List<CmsProperty> properties = getPropertiesConvertedVideo(targetTempPath, formatName);
			
			try {
				LOG.log("Creating resource in VFS "+linkName);
				int type = OpenCms.getResourceManager().getResourceType("video-link").getTypeId();
				if (!videoType.equals("") || resource.getTypeId() == TfsResourceTypeVideoVodLink.getStaticTypeId()) {
					type = TfsResourceTypeVideoVodLink.getStaticTypeId();
				}
				cmsObj.createResource(linkName, 
						type,targetFTPPath.getBytes(),
						properties);
				
				result = linkName;
			} catch (CmsIllegalArgumentException e) {
				CmsLog.getLog(this).error("Video Convert - Error al generar el link en el vfs: "+e.getMessage());
			} catch (CmsLoaderException e) {
				CmsLog.getLog(this).error("Video Convert - Error al generar el link en el vfs: "+e.getMessage());
			} catch (CmsException e) {
				CmsLog.getLog(this).error("Video Convert - Error al generar el link en el vfs: "+e.getMessage());
			}
		} catch (SocketException e1) {
			CmsLog.getLog(this).error("Video converter - Error al intentar bajar el video de " + ftpServer,e1);
			LOG.log("ERROR", e1.getMessage());
			throw e1;
		} catch (IOException e1) {
			CmsLog.getLog(this).error("Video converter - Error al intentar bajar el video de " + ftpServer,e1);
			LOG.log("ERROR", e1.getMessage());
			throw e1;
		} catch (IllegalArgumentException e) {
			CmsLog.getLog(this).error("Video Convert - Error: "+e.getMessage());
			LOG.log("ERROR", e.getMessage());
		} catch (InputFormatException e) {
			CmsLog.getLog(this).error("Video Convert - Error: "+e.getMessage());
			LOG.log("ERROR", e.getMessage());
		} catch (EncoderException e) {
			CmsLog.getLog(this).error("Video Convert - Error: "+e.getMessage());
			LOG.log("ERROR", e.getMessage());
		} 

		return result;
	}
	
	public String automaticEncodeVFS(String sourceVFSPath, String formatName) throws CmsException, IOException{
		
		String result= null;
		
		CmsObject cmsObj = getCmsObject();
		
		String tmpFolder = config.getParam(siteName, publication, moduleConfigName, "tempFolder","");
		
		String outputFormat  = getOutputFormat(formatName);
		String       prefix  = getPrefix(formatName);
		String ffmpegOptions = getFormatOptions(formatName);
		
		String      targetName = getOutputFileName(sourceVFSPath, outputFormat, prefix);
		String    targetFolder = getFolderDestination(formatName,"VFS");
		String targetSubFolder = getOutputSubFolder(sourceVFSPath, "VFS", formatName);
		
		String targetVFSPath = "/"+targetFolder +  targetSubFolder + "/" + targetName;
		
		String targetTempPath = tmpFolder + "/" + targetName;
		
		String  sourceTmpName = sourceVFSPath.substring(sourceVFSPath.lastIndexOf("/")+1);
		String sourceTempPath = tmpFolder + "/" + sourceTmpName;
		
		CmsFile cmsFile;
		cmsFile = cmsObj.readFile(sourceVFSPath);
		
		OutputStream out = new FileOutputStream(sourceTempPath);
		out.write(cmsFile.getContents(),0, cmsFile.getLength());
		out.close();
		
		checkFileExistRFS(targetTempPath); 
		
		LOG.log("Source Path: "+sourceTempPath);
		LOG.log("Target Path: "+targetTempPath);
		
		File source = new File(sourceTempPath);
		File target = new File(targetTempPath);
		
		Encoder encoder = new Encoder();
		
		try {
		    long duration = getVideoDuration(sourceTempPath);
		    
		    LOG.log("Preparing for start enconding ...");
		    LOG.log("Duration of sourcePath: "+duration);
		    
			encoder.automaticEncode(source, target, ffmpegOptions,this, duration,LOG);
			
			String linkName = targetVFSPath;
			
			List<CmsProperty> properties = getPropertiesConvertedVideo(targetTempPath, formatName);
			
			FileInputStream			  in = new FileInputStream(targetTempPath);
			ByteArrayOutputStream outVfs = new ByteArrayOutputStream();
			byte[] 					 buf = new byte[1024];
			int                        n = 0;
			        
			while (-1 != (n = in.read(buf))) {
				outVfs.write(buf, 0, n);
			}
			     
			outVfs.close();
			in.close();
			byte[] buffer = outVfs.toByteArray();
			
			String defaultVfsFolder = config.getParam(siteName, publication, moduleVideoConfig, "defaultVideoFlashPath","");
			
			String linkFolderName = getFolderDestination(formatName,"VFS");
			String subFolderVFS = sourceVFSPath.substring(0, sourceVFSPath.lastIndexOf("/") );
			
			if(sourceVFSPath.indexOf(defaultVfsFolder)>-1)
				subFolderVFS = subFolderVFS.replace("/"+defaultVfsFolder,"");
			
			checkFoldersExistVFS(linkFolderName + subFolderVFS);  
			
			try {
				LOG.log("Creating resource in VFS "+linkName);
				int type = OpenCms.getResourceManager().getResourceType("video-link").getTypeId();
				if (!videoType.equals("") ) {
					type = TfsResourceTypeVideoVodLink.getStaticTypeId();
				}
				
				cmsObj.createResource(linkName, 
						type,buffer,
						properties);
				
				result = linkName;
				
			} catch (CmsIllegalArgumentException e) {
				CmsLog.getLog(this).error("Video Convert - Error al generar el link en el vfs: "+e.getMessage());
				LOG.log("ERROR", e.getMessage());
			} catch (CmsLoaderException e) {
				CmsLog.getLog(this).error("Video Convert - Error al generar el link en el vfs: "+e.getMessage());
				LOG.log("ERROR", e.getMessage());
			} catch (CmsException e) {
				CmsLog.getLog(this).error("Video Convert - Error al generar el link en el vfs: "+e.getMessage());
				LOG.log("ERROR", e.getMessage());
			}
		} catch (IllegalArgumentException e) {
			CmsLog.getLog(this).error("Video Convert - Error: "+e.getMessage());
			LOG.log("ERROR", e.getMessage());
		} catch (InputFormatException e) {
			CmsLog.getLog(this).error("Video Convert - Error: "+e.getMessage());
			LOG.log("ERROR", e.getMessage());
		} catch (EncoderException e) {
			CmsLog.getLog(this).error("Video Convert - Error: "+e.getMessage());
			LOG.log("ERROR", e.getMessage());
		} 
		
		return result;
	}
	
	public String getOutputFileName(String sourcePath, String outputFormat, String prefix){
		String fileName = sourcePath.substring(sourcePath.lastIndexOf("/")+1, sourcePath.length()-1); 
		fileName = fileName.substring(0,fileName.lastIndexOf(".")) + 
				prefix + 
				(outputFormat.equals("") ? "" : ".") + 
				outputFormat; 
		
		return fileName;
	}
	
	public String getOutputSubFolder(String sourcePath, String type, String formatName){
		String subFolder = null;
		
		if(type.equals("RFS")) {
			String  rfsDirectory = config.getParam(siteName, publication, moduleVideoConfig, "rfsDirectory","");
			
			subFolder = sourcePath.substring(rfsDirectory.length());
			subFolder = subFolder.substring(0, subFolder.lastIndexOf("/"));
		}
		if(type.equals("FTP")) {
			String  rfsDirectory = config.getParam(siteName, publication, moduleVideoConfig, "ftpDirectory","");
			
			subFolder = sourcePath.substring(rfsDirectory.length());
			subFolder = subFolder.substring(0, subFolder.lastIndexOf("/"));
		}
		if(type.equals("VFS")) {
			String defaultVfsFolder = config.getParam(siteName, publication, moduleVideoConfig, "defaultVideoFlashPath","");
			              subFolder = sourcePath.substring(0, sourcePath.lastIndexOf("/") );
			
			if(sourcePath.indexOf(defaultVfsFolder)>-1)
				subFolder = subFolder.replace("/"+defaultVfsFolder,"");
		}
		if(type.equals("AMZ")) {
			String  amzDirectory = config.getParam(siteName, publication, moduleVideoConfig, "amzDirectory","");
			
			subFolder = sourcePath.substring(amzDirectory.length());
			subFolder = subFolder.substring(0, subFolder.lastIndexOf("/"));
		}
		
		return subFolder;
	}
	
	public String getOptionParamGroup(String paramName, String type) {
		String param = config.getItemGroupParam(siteName, publication, moduleConfigName, "formats", paramName);
		String[] paramParts = param.split(",");
		for(String part : paramParts) {
			int ind = part.indexOf(":");
			String partName = part.substring(0, ind);
			if(partName.equals(type)) {
				return part.substring(ind+1);
			}
		}
		LOG.log("WARNING", String.format("Cannot find param %s for format %s, site %s - publication %s",
				type, paramName, siteName, publication));
		return null;
	}
	
	public String getParamFormat( String paramName, String type ) {
		String param = config.getItemGroupParam(siteName, publication, moduleConfigName,"formats",paramName);
		
		String [] paramParts = param.split(",");
		int ind = 0;
		
		String value = null;
		
		if(type.equals("options")){
		    ind = paramParts[0].indexOf(":");
			value = paramParts[0].substring(ind+1);
		}
		
		if(type.equals("output")){
		    ind = paramParts[1].indexOf(":");
			value = paramParts[1].substring(ind+1);
		}
		
		if(type.equals("folderVFS")){
		    ind = paramParts[2].indexOf(":");
			value = paramParts[2].substring(ind+1);
		}
		
		if(type.equals("folderRFS")){
		    ind = paramParts[3].indexOf(":");
			value = paramParts[3].substring(ind+1);
		}
		
		if(type.equals("folderFTP")){
		    ind = paramParts[4].indexOf(":");
			value = paramParts[4].substring(ind+1);
		}
		
		if(type.equals("prefix")){
		    ind = paramParts[5].indexOf(":");
			value = paramParts[5].substring(ind+1);
		}
		
		if(type.equals("folderAMZ")){
		    ind = paramParts[6].indexOf(":");
			value = paramParts[6].substring(ind+1);
		}
		
		if(type.equals("presetAMZ")){
		    ind = paramParts[7].indexOf(":");
			value = paramParts[7].substring(ind+1);
		}
		
		return value;
    }
	
	public void checkFileExistRFS( String targetPath)
	{
		File      archivo = null;
        
		try {
			 archivo = new File (targetPath);
	         
			 if(archivo.exists())
				     archivo.delete();
	         
		} catch (Exception e) {
			LOG.log("ERROR", e.getMessage());
		}
		
	}
	
	public void checkFoldersExistVFS(String foldersVFS){
		
		CmsObject cmsObj = getCmsObject();
		
		String[] parts = foldersVFS.split("/");
		String subfolderName = "/";
		String firstFolder = null;
		
		for (String part : parts)
		{
			if(!part.equals("")){
		     	subfolderName = subfolderName.trim() + part + "/";
		     	
		     	if (!cmsObj.existsResource(subfolderName)) {
					try {
						cmsObj.createResource(subfolderName, CmsResourceTypeFolder.getStaticTypeId());
						
						if(firstFolder==null) firstFolder = subfolderName;
						
					} catch (CmsIllegalArgumentException e) {
						CmsLog.getLog(this).error("Video converter - Error al crear carpeta en el VFS"+e.getMessage());
						LOG.log("ERROR", e.getMessage());
					} catch (CmsException e) {
						CmsLog.getLog(this).error("Video converter - Error al crear carpeta en el VFS"+e.getMessage());
						LOG.log("ERROR", e.getMessage());
					} catch (Exception e) {
						CmsLog.getLog(this).error("Video converter - Error al crear carpeta en el VFS"+e.getMessage());
						LOG.log("ERROR", e.getMessage());
					}
					
		     	}
		     	
			}
		}
		
		if(firstFolder!=null)
			try {
				OpenCms.getPublishManager().publishResource(cmsObj, firstFolder);
			} catch (Exception e) {
				CmsLog.getLog(this).error("Video converter - Error al publicar carpeta en el VFS"+e.getMessage());
				LOG.log("ERROR", e.getMessage());
			}
		
	}
	
	public String getFormatOptions( String paramName )
    {
		return getParamFormat( paramName, "options" );
    }
	
	public String getFolderDestination( String paramName, String type )
    {
		String folder = null;
		
		if(type.equals("VFS")) 
			folder = getParamFormat( paramName, "folderVFS" );
		
		if(type.equals("RFS")) 
			folder = getParamFormat( paramName, "folderRFS" );
		
		if(type.equals("FTP")) 
			folder = getParamFormat( paramName, "folderFTP" );
		
		if(type.equals("AMZ")) 
			folder = getParamFormat( paramName, "folderAMZ" );
		
		return folder; 
    }
	
	public String getPrefix(String paramName)
	{
		return getParamFormat( paramName, "prefix" );
	}
	
	public String getOutputFormat(String paramName)
	{
		return getParamFormat( paramName, "output" );
	}
	
	public long getVideoDuration(String sourcePath){
		
		File target = new File(sourcePath);
		Encoder encoder = new Encoder();
		
		MultimediaInfo infoVideo = new MultimediaInfo();
		
		try {
				infoVideo = encoder.getInfo(target);
		} catch (InputFormatException e) {
				e.printStackTrace();
				LOG.log("ERROR", e.getMessage());
		} catch (EncoderException e) {
				e.printStackTrace();
				LOG.log("ERROR", e.getMessage());
		}
					   
		long duration = infoVideo.getDuration();
		
		return duration;
		
	}
	
	public List<CmsProperty> getPropertiesConvertedVideo(String targetPath, String formatName) {
		try {
			File target = new File(targetPath);
			Encoder encoder = new Encoder();
			
			MultimediaInfo infoVideo = new MultimediaInfo();
						   infoVideo = encoder.getInfo(target);
						   
			long durationMills = infoVideo.getDuration();
			int 	   seconds = (int)((durationMills / 1000)%60);  
			int 	   minutes = (int)((durationMills / 1000)/60);  
			int 	     hours = (int)((durationMills / (1000*60*60))%24);  
						   
			DecimalFormat formateador = new DecimalFormat("00");
			String duration = formateador.format(hours)+":"+formateador.format(minutes)+":"+formateador.format(seconds);
			
			VideoInfo  infoVideoMedia = new VideoInfo();
	        infoVideoMedia = infoVideo.getVideo();
	         
	        VideoSize	videoSize =  infoVideoMedia.getSize(); 
			  
			CmsProperty prop = new CmsProperty();
			
			List<CmsProperty> properties = new ArrayList<CmsProperty>(4);
			
			prop = new CmsProperty();
			prop.setName("video-duration");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(duration);
			properties.add(prop);
			 
			prop = new CmsProperty();
			prop.setName("video-size");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(videoSize.getWidth()+"x"+videoSize.getHeight());
			properties.add(prop);
			 
			prop = new CmsProperty();
			prop.setName("video-format");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(formatName);
			properties.add(prop);
			 
			prop = new CmsProperty();
			prop.setName("video-bitrate");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(infoVideo.getBitrate());
			properties.add(prop);
			 
			return properties;
		} catch (InputFormatException e) {
			CmsLog.getLog(this).error("Video convert - Error al obtener la info de "+targetPath);
			LOG.log("ERROR", e.getMessage());
		} catch (EncoderException e) {
			CmsLog.getLog(this).error("Video convert - Error al obtener la info de "+targetPath);
			LOG.log("ERROR", e.getMessage());
		} catch (Exception e) {
			LOG.log("ERROR", e.getMessage());
		}
  
		return null;
	}
	
	public void updateVideoSourceProperties(String sourceVFSPath,String formatName){
		
		CmsObject cmsObj = getCmsObject();
		CmsProperty   prop = null;
		String propFormats = null; 
		
		try{
			prop = cmsObj.readPropertyObject(sourceVFSPath, "video-formats", false);  
		}catch(Exception e){
			LOG.log("ERROR", e.getMessage());
		}
		
		if(prop !=null &&  prop.getValue()!=null){
			propFormats = prop.getValue()+", "+formatName;
		}else{
			propFormats = formatName; 
		}
		
	    try{
	    	CmsResourceUtils.forceLockResource(cmsObj, sourceVFSPath);
		
	    	 CmsFile file = cmsObj.readFile(sourceVFSPath);
	    	 
		    CmsProperty propV = new CmsProperty();
		                propV.setName("video-formats");
		                propV.setValue(propFormats, CmsProperty.TYPE_INDIVIDUAL);
		
		    cmsObj.writePropertyObject(sourceVFSPath,propV);
		    
		    file.setContents(file.getContents()); 
			cmsObj.writeFile(file);
		    cmsObj.unlockResource(sourceVFSPath);
        }
        catch(Exception e){
          CmsLog.getLog(this).error("Video convert - Error al actualizar la propiedad video-formats de "+sourceVFSPath+" ["+e.getMessage()+"]");
          LOG.log("ERROR", e.getMessage());
        }
		
	}

	@Override
	public void sourceInfo(MultimediaInfo info) {
		if(this.request!=null)	
			this.request.getSession().setAttribute("encoderMultimediaInfo", info);
	}

	@Override
	public void progress(int permil) {
		if(this.request!=null)	
			this.request.getSession().setAttribute("encoderProgress", permil);
	}

	@Override
	public void message(String message) {
		if(this.request!=null)	
		    this.request.getSession().setAttribute("encoderMessage", message);
	}
	
	@SuppressWarnings("unchecked")
public void deleteVideo(String sourceVFSPath, CmsObject cms) {
		
		try {	
			if (!cms.getLock(sourceVFSPath).isUnlocked()) {
			     if(!cms.getLock(sourceVFSPath).isOwnedBy(cms.getRequestContext().currentUser())) {
				      cms.changeLock(sourceVFSPath);
			    }
			} else {
			     cms.lockResource(sourceVFSPath);
			}
			
			boolean fileDeleted = true;
			
			// borro el archivo fisico
			CmsResource  resource = cms.readResource(sourceVFSPath);
			
			if (resource.getTypeId() == TfsResourceTypeVideoLink.getStaticTypeId()) {
				try {
					deleteFile(resource,cms);
				} catch (Exception e) {
					fileDeleted = false;
					CmsLog.getLog(this).error("Error al borrar el archivo del video : "+sourceVFSPath+". Error: "+e.getMessage());
				}
			}
			
			if(fileDeleted) {
				CmsRelationFilter filter = CmsRelationFilter.ALL.filterType(CmsRelationType.valueOf("videoFormats"));
				List<CmsRelation> relations = cms.getRelationsForResource(sourceVFSPath, filter);
				
				for (CmsRelation rel : relations) {
					
					String relation = "";
			      	
			      	String rel1 = cms.getRequestContext().removeSiteRoot(rel.getTargetPath());
	   				String rel2 = cms.getRequestContext().removeSiteRoot(rel.getSourcePath());
	
	   				if (rel1.equals(sourceVFSPath))
	   					relation = rel2;
	   				else
	   					relation = rel1;
	   				
	   				if (!cms.getLock(relation).isUnlocked()){
					     if(!cms.getLock(relation).isOwnedBy(cms.getRequestContext().currentUser())){
						      cms.changeLock(relation);
					    }
					}else{
					     cms.lockResource(relation);
					}
	   				
	   				CmsResource    relResource = cms.readResource(relation);
					CmsResourceState estadoRel = relResource.getState();
			    	String        estadoRelStr = estadoRel.toString();
			    	
			    	try {
						deleteFile(relResource,cms);
					} catch (Exception e) {
						CmsLog.getLog(this).error("Error al borrar el archivo del video : "+relation+". Error: "+e.getMessage());
					}
			    	
					cms.deleteResource(relation, CmsResource.DELETE_PRESERVE_SIBLINGS);
					
			    	if( !estadoRelStr.equals("2") )
					    OpenCms.getPublishManager().publishResource(cms,relation);
				 }
				
				CmsResourceState  estado = resource.getState();
		    	String         estadoStr = estado.toString();
		    	
				cms.deleteResource(sourceVFSPath, CmsResource.DELETE_PRESERVE_SIBLINGS);
				
		    	if( !estadoStr.equals("2") )
				     OpenCms.getPublishManager().publishResource(cms,sourceVFSPath);
				
				TfsEnconderQueue queue;
								 queue = new TfsEnconderQueue(cms,m_context, request, response);
								 queue.deleteFromQueueByPath(sourceVFSPath);
			}
			
		} catch (CmsException e1) {
			CmsLog.getLog(this).error("Error al borrar videos: "+e1.getMessage());
		} catch (Exception e) {
			CmsLog.getLog(this).error("Error al borrar videos: "+e.getMessage());
		}
		
	}
	
private void deleteFile(CmsResource resource,CmsObject cms) throws CmsException {
		
		CmsFile file = cms.readFile(resource);
		String videoUrl = new String(file.getContents());
		
		String S3_URL_REGEX = "^https?://[a-z0-9.-]+\\.s3\\..*\\..*\\.amazonaws\\.com/[A-Za-z0-9._-]+[/[^/]+]*$"; 
		
		Pattern pattern = Pattern.compile(S3_URL_REGEX);  
        Matcher matcher = pattern.matcher(videoUrl);  
        
        if(matcher.matches())
        {
			String amzBucket = config.getParam(siteName, publication, moduleVideoConfig, "amzBucket","");
			String amzAccessID  = config.getParam(siteName, publication, moduleVideoConfig, "amzAccessID","");
			String amzAccessKey = config.getParam(siteName, publication, moduleVideoConfig, "amzAccessKey","");
			String amzRegion = config.getParam(siteName, publication, moduleVideoConfig, "amzRegion","");
			
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
			 
	        try {  
	            String key = URI.create(videoUrl).getPath().substring(1);  
	            
	            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()  
		                .bucket(amzBucket)  
		                .key(key)  
		                .build();  

	            s3.deleteObject(deleteRequest);  
	        } catch (Exception e) {  
	            CmsLog.getLog(this).error("Error al eliminar el archivo: " + e.getMessage());
	        }  
	        
        }else {
        	
        	String  rfsDirectory = config.getParam(siteName, publication, moduleVideoConfig, "rfsDirectory","");
			String rfsVirtualUrl = config.getParam(siteName, publication, moduleVideoConfig, "rfsVirtualUrl","");
			
			String rfsPath = videoUrl.replace(rfsVirtualUrl, rfsDirectory+"/" );
        	
        	File videoFile = new File("/"+rfsPath);
        	
        	if(videoFile.exists()) 
        		videoFile.delete();
        }
        

	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void publishVideos(List videos,  CmsObject cms){
		try {	
			List<CmsResource> publishList = new ArrayList<CmsResource>();
            Iterator it =  videos.iterator();

			while(it.hasNext()) {
				String video = (String)it.next();
				
				if (!cms.getLock(video).isUnlocked()) {
				     if(!cms.getLock(video).isOwnedBy(cms.getRequestContext().currentUser())){
					      cms.changeLock(video);
				    }
				} else {
				     cms.lockResource(video);
				}
				
				checkFFmpegProperties(video);
				
				CmsResource  resource = cms.readResource(video);
				publishList.add(resource);

				CmsRelationFilter filter = CmsRelationFilter.ALL.filterType(CmsRelationType.valueOf("videoFormats"));
				
				List<CmsRelation> relations = cms.getRelationsForResource(resource, filter);
				
				for (CmsRelation rel : relations) {
					String relation = "";
			      	
			      	String rel1 = cms.getRequestContext().removeSiteRoot(rel.getTargetPath());
	   				String rel2 = cms.getRequestContext().removeSiteRoot(rel.getSourcePath());

	   				if (rel1.equals(video))
	   					relation = rel2;
	   				else
	   					relation = rel1;
	   				
	   				if (!cms.getLock(relation).isUnlocked()) {
					     if(!cms.getLock(relation).isOwnedBy(cms.getRequestContext().currentUser())) {
						      cms.changeLock(relation);
					    }
					} else {
					     cms.lockResource(relation);
					}
					
					CmsResource  resourceRelation = cms.readResource(relation);
	   				
	   				publishList.add(resourceRelation);
			   }
				
               CmsRelationFilter filterImages = CmsRelationFilter.ALL.filterType(CmsRelationType.valueOf("videoImage"));
			   List<CmsRelation> relatedImages = cms.getRelationsForResource(resource, filterImages);
			
			   for (CmsRelation rel : relatedImages) {
					
					String relation = "";
			      	
			      	String rel1 = cms.getRequestContext().removeSiteRoot(rel.getTargetPath());
	   				String rel2 = cms.getRequestContext().removeSiteRoot(rel.getSourcePath());

	   				if (rel1.equals(video))
	   					relation = rel2;
	   				else
	   					relation = rel1;
	   				
	   				if (!cms.getLock(relation).isUnlocked()) {
	   					if(!cms.getLock(relation).isOwnedBy(cms.getRequestContext().currentUser())) {
						      cms.changeLock(relation);
					    }
					} else {
					     cms.lockResource(relation);
					}
					
					CmsResource  resourceRelation = cms.readResource(relation);
	   				
	   				publishList.add(resourceRelation);
				}
			}
			
			OpenCms.getPublishManager().publishProject(cms, new CmsLogReport(Locale.getDefault(), this.getClass()), OpenCms.getPublishManager().getPublishList(cms,publishList, false));
		} catch (CmsException e1) {
			CmsLog.getLog(this).error("Error al publicar videos: "+e1.getMessage());
		}
	}
	
	public String queueConvert(String sourceVFSPath, String formats, String type) {
		String status = "OnQueue";
		
		CmsObject cmsObj = getCmsObject();
		TfsEnconderQueue queue = new TfsEnconderQueue(cmsObj,m_context, request, response);
		CmsUser currentUser = cmsObj.getRequestContext().currentUser();  
		
		logger.debug("insertando registro en la base de datos: " + sourceVFSPath);
		
		int idInQueue = queue.insertQueueDB(sourceVFSPath, formats, type, publication, siteName, currentUser.getName());
		
		int     idInProcess = queue.idVideoOnProcess();
		int idNextInProcess = queue.idNextVideoOnProcess();
			
		if(idInQueue == idInProcess || (idInProcess==0 && idInQueue == idNextInProcess))
			status = "OnProcess";
		
		queue.checkEncoderQueue();
		
		return status;
	}

	public String convert( String sourceVFSPath, String formats, String type, String username ) {
		String fileLog = sourceVFSPath.substring(sourceVFSPath.lastIndexOf("/")+1);
		       fileLog = fileLog + ".log";
		
		LOG.setup(fileLog, this.getClass().getName());
    	
		String formatsGenerated = "";
		
		CmsObject cmsObj = getCmsObject();
		
		List<CmsResource> publishList = new ArrayList<CmsResource>();
		
		String [] formatList = formats.split(",");
		
		LinkedHashMap<String, String> formatsConvertedMap = new LinkedHashMap<String, String>();
	    
	    for(int i=0; i<= formatList.length-1; i++) {
	      
	        String formatName = formatList[i];
	               formatName = formatName.trim();
	               
	        String formatVFS = null;       
	            
	        try {      
	    		LOG.log("Init video convert of "+sourceVFSPath+" in format "+formatName);
	    		
	    		LOG.log("Formats upload to "+type);
	    	
		        if(type.equals("RFS"))
		        	formatVFS = automaticEncodeRFS(sourceVFSPath ,formatName);
		        
		        if(type.equals("VFS"))
		        	formatVFS =  automaticEncodeVFS(sourceVFSPath ,formatName);
		        	
		        if(type.equals("FTP"))
		        	formatVFS =  automaticEncodeFTP(sourceVFSPath ,formatName);
		        
		        if(type.equals("AMZ"))
		        	formatVFS = automaticEncodeAMZ(sourceVFSPath ,formatName);
	    
			} catch (CmsException e) {
				e.printStackTrace();
				LOG.log("ERROR", e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
				LOG.log("ERROR", e.getMessage());
			}
	        
	        if(formatVFS!=null) { 
	        	CmsResource resourceFormatVFS;
				try {
					resourceFormatVFS = cmsObj.readResource(formatVFS); 
					com.tfsla.utils.CmsResourceUtils.forceLockResource(cmsObj,formatVFS);
					publishList.add(resourceFormatVFS);
					
					formatsConvertedMap.put(formatName, formatVFS);
					
				} catch (CmsException e) {
					LOG.log("ERROR", e.getMessage());
				}
	        }
	        
	       formatsGenerated = formatsGenerated + " " + formatName;
       }
	      
	   TfsEnconderQueue queue = new TfsEnconderQueue(cmsObj,m_context, request, response);
       queue.deleteOnProcessFromQueueDB();
	                       
	   String propFormats = "";
	   TFSEncoderRelationQueue relationQueue = new TFSEncoderRelationQueue(cmsObj);
			
	   for (Map.Entry<String, String> entry : formatsConvertedMap.entrySet()) {
	          String formatConverted =  entry.getKey();
	          String pathVFSConverted = entry.getValue();
 
	          propFormats = propFormats+", "+ formatConverted;
	          
	          try{
	        	    if (!cmsObj.getLock(sourceVFSPath).isUnlocked()) {
					     if(!cmsObj.getLock(sourceVFSPath).isOwnedBy(cmsObj.getRequestContext().currentUser()))
						      cmsObj.changeLock(sourceVFSPath);
				    } else {
					     cmsObj.lockResource(sourceVFSPath);
				    }
	        	    
					if (!cmsObj.getLock(pathVFSConverted).isUnlocked()) {
					    if(!cmsObj.getLock(pathVFSConverted).isOwnedBy(cmsObj.getRequestContext().currentUser()))
						      cmsObj.changeLock(pathVFSConverted);
					} else {
					     cmsObj.lockResource(pathVFSConverted);
					}
					
			     	cmsObj.addRelationToResource( sourceVFSPath, pathVFSConverted, "videoFormats");
			     	
			     	LOG.log("Video Convert - Added relation to resource "+ sourceVFSPath);
			     	
				} catch (CmsException e) {
					CmsLog.getLog(this).error("Video Convert - Error al agregar relacion: "+e.getMessage());
					LOG.log("ERROR", e.getMessage());
				
					relationQueue.addRelationToList(sourceVFSPath, formatConverted,pathVFSConverted);
					
				}
	      }
	   		// Se verifica si se generaron errores para la asignacion de relaciones del archivo y se almacenan en la tabla de pendientes
	   	  if (relationQueue.getEncodingRelationList().size() > 0)
	   		  relationQueue.insertVideoToRelationQueue();
	      
	      propFormats = propFormats.substring(2);
	      
	      LOG.log("Update properties of source in VFS "+propFormats);
		  updateVideoSourceProperties(sourceVFSPath, propFormats);
	                       
	      try {
	    	    CmsResource sourceFormatVFS = cmsObj.readResource(sourceVFSPath); 
				com.tfsla.utils.CmsResourceUtils.forceLockResource(cmsObj,sourceVFSPath);
				publishList.add(sourceFormatVFS);
				
				//Verificamos si las carpetas padres estan publicadas y si no las agregamos a la lista de publicación
				List parentFolders = new ArrayList();
				for (Iterator<CmsResource> iterator = publishList.iterator(); iterator.hasNext();) {
					CmsResource resource = iterator.next();
					CmsResource parentFolder = cmsObj.readResource(CmsResource.getParentFolder(cmsObj.getSitePath(resource)));
					CmsLock lockRes = cmsObj.getLock(parentFolder);
					while (!lockRes.getSystemLock().isPublish() && !parentFolder.getState().isUnchanged() && lockRes.isLockableBy(cmsObj.getRequestContext().currentUser())) {
						parentFolders.add(parentFolder);
						
						parentFolder = cmsObj.readResource(CmsResource.getParentFolder(cmsObj.getSitePath(parentFolder)));
						lockRes = cmsObj.getLock(parentFolder);
					} 
				}
				
				publishList.addAll(parentFolders);
	    	  
			    OpenCms.getPublishManager().publishProject(cmsObj, new CmsLogReport(Locale.getDefault(), this.getClass()), OpenCms.getPublishManager().getPublishList(cmsObj,publishList, false));
			    LOG.log("Se publicó el video original y sus formatos");
	      } catch (CmsException e1) {
			  LOG.log("ERROR", e1.getMessage());
		  }
	      
	      CmsUser cmsUserToNotify = null;
	      String     userToNotify = username;
	      String      languageMsg = "EN";
	    		  
	      try {
	    	  cmsUserToNotify = cmsObj.readUser(username);
	    	     userToNotify = cmsUserToNotify.getFullName();
	      } catch(CmsException e) {
	    	  LOG.log("ERROR", e.getMessage());
	      }
	      
	      if(cmsUserToNotify!=null) {
	    	  String prefLanguage = (String)cmsUserToNotify.getAdditionalInfo("USERPREFERENCES_workplace-startupsettingslocale");
	    	  
	    	  if(prefLanguage!=null) {
	    		  if(prefLanguage.toUpperCase().equals("ES"))
	    			  languageMsg = "ES";
	    	  }
	    }
	      
	    TfsAuditAction      action = new TfsAuditAction();
	    TfsAuditActionDAO auditDAO = new TfsAuditActionDAO();            
	    String           actionMsg = userToNotify +" the process of encoder "+sourceVFSPath+" to the formats "+formatsGenerated+" is finish.";
	     
	    if(languageMsg.equals("ES")) {
	    	  actionMsg = userToNotify +" el proceso de conversión del video "+sourceVFSPath+" a los formatos "+formatsGenerated+"  finalizó.";
	    }
	      
        action.setActionId(TfsAuditAction.ACTION_VIDEO_ENCODER);
		action.setTimeStamp(new Date());
		action.setPublicacion(publication);
		action.setSitio(siteName+"/");
		
		
		try {
			
			action.setUserName(CmsWorkplaceAction.getInstance().getCmsAdminObject().getRequestContext().currentUser().getName());
			action.setDescription(actionMsg);

			
			auditDAO.insertUserAuditEvent(action);
			auditDAO.insertNotificationAuditEvent(action.getEventId(),action.getTimeStamp(),username);

		} catch (Exception e) {
			CmsLog.getLog(this).error("Error registrando el evento "+e.getMessage());
		}
			
		LOG.close();
  
		return formatsGenerated;
	}
	
	public boolean isInRFS(String sourceVFSPath) {
		CmsObject cmsObj = getCmsObject();
		boolean isInRFS = false;
		
		try { 
			CmsResource resource = cmsObj.readResource(sourceVFSPath);
			CmsFile 	    file = cmsObj.readFile(resource);
			String    sourcePath = new String(file.getContents());
	
			String rfsVirtualUrl = config.getParam(siteName, publication, moduleVideoConfig, "rfsVirtualUrl","");
			
			if(sourcePath.indexOf(rfsVirtualUrl)>-1)
				isInRFS = true;
			
		} catch (CmsException e) {
			CmsLog.getLog(this).equals("Error al verificar destino de video: "+e.getMessage());
		}
		
		return isInRFS;
	}
	
	public void checkFFmpegProperties( String sourceVFSPath ) {
		CmsObject cmsObj = getCmsObject();
		CmsProperty   prop  = null;
		String propDuration = null; 
		String 	   propSize = null;
		String 	propBitrate = null;
		
		try {
			prop = cmsObj.readPropertyObject(sourceVFSPath, "video-duration", false);  
			if(prop.getValue()!=null)
				propDuration = prop.getValue();
			
			prop = cmsObj.readPropertyObject(sourceVFSPath, "video-bitrate", false);  
			if(prop.getValue()!=null)
				propBitrate = prop.getValue();
			
			prop = cmsObj.readPropertyObject(sourceVFSPath, "video-size", false);  
			if(prop.getValue()!=null)
				propSize = prop.getValue();
			
			if(propDuration==null && propBitrate==null && propSize==null) {
				CmsResource resource = cmsObj.readResource(sourceVFSPath);
				CmsFile 	    file = cmsObj.readFile(resource);
				String          data = new String(file.getContents());
				int           typeid = resource.getTypeId();
				String  resourceType = OpenCms.getResourceManager().getResourceType(typeid).getTypeName();  
				String          type = "VFS";
	
				if(resourceType.equals("video-link")) {
					  boolean isInRFS = isInRFS(sourceVFSPath);
					    
					  if(isInRFS)
					         type = "RFS";
					    else
					         type = "FTP";
				}
				
				String filePath = "";
				
				if(type.equals("RFS")) {
					String  rfsDirectory = config.getParam(siteName, publication, moduleVideoConfig, "rfsDirectory","");
					String rfsVirtualUrl = config.getParam(siteName, publication, moduleVideoConfig, "rfsVirtualUrl","");
					
					filePath = data.replace(rfsVirtualUrl, rfsDirectory+"/" );
				}
				
				if(type.equals("VFS")) {
					CmsFile cmsFile;
					cmsFile = cmsObj.readFile(sourceVFSPath);
					
					String fileName = sourceVFSPath.substring(sourceVFSPath.lastIndexOf("/")); 
					
					String tmpFile = tmpFileFFmepg(fileName, cmsFile.getContents());
					
					if(tmpFile != null)
						filePath = tmpFile;
				}
				
				if(type.equals("FTP")) {
					String ftpServer = config.getParam(siteName, publication, moduleVideoConfig, "ftpServer","");
					String ftpUser = config.getParam(siteName, publication, moduleVideoConfig, "ftpUser","");
					String ftpPassword  = config.getParam(siteName, publication, moduleVideoConfig, "ftpPassword","");
					String ftpDirectory = config.getParam(siteName, publication, moduleVideoConfig, "ftpDirectory","");
					
					FTPClient client = new FTPClient();
					client.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

					client.connect(ftpServer);
					client.login(ftpUser, ftpPassword);
					client.changeWorkingDirectory(ftpDirectory);
						
					client.enterLocalPassiveMode();
						
					InputStream content = client.retrieveFileStream(data);
						
					byte[] buffer = CmsFileUtil.readFully(content, true);
						
					String fileName = sourceVFSPath.substring(sourceVFSPath.lastIndexOf("/")); 
						
					String tmpFile = tmpFileFFmepg(fileName, buffer);
						
					if(tmpFile != null)
						filePath = tmpFile;
					
				}
				
				// Con el filePath leo y guardo las propiedades
				if(filePath != null && !filePath.equals("")) {
					File uploadedFile = new File(filePath);
					Encoder encoder = new Encoder();
				    MultimediaInfo infoVideo =  encoder.getInfo(uploadedFile);
							       
					long durationMills = infoVideo.getDuration();
					int 	   seconds = (int)((durationMills / 1000)%60);  
					int 	   minutes = (int)((durationMills / 1000)/60);  
					int 	     hours = (int)((durationMills / (1000*60*60))%24);  
												   
					DecimalFormat formateador = new DecimalFormat("00");
					String duration = formateador.format(hours)+":"+formateador.format(minutes)+":"+formateador.format(seconds);
									
					VideoInfo  infoVideoMedia = new VideoInfo();
					infoVideoMedia = infoVideo.getVideo();
					VideoSize	videoSize =  infoVideoMedia.getSize(); 
					String bitrate = infoVideo.getBitrate();
					
					CmsProperty propV = new CmsProperty();
	                propV.setName("video-size");
	                propV.setValue(videoSize.getWidth()+"x"+videoSize.getHeight(), CmsProperty.TYPE_INDIVIDUAL);
	                cmsObj.writePropertyObject(sourceVFSPath,propV);
	                
	                propV = new CmsProperty();
	                propV.setName("video-bitrate");
	                propV.setValue(bitrate, CmsProperty.TYPE_INDIVIDUAL);
	                cmsObj.writePropertyObject(sourceVFSPath,propV);
	                
	                propV = new CmsProperty();
	                propV.setName("video-duration");
	                propV.setValue(duration, CmsProperty.TYPE_INDIVIDUAL);
	                cmsObj.writePropertyObject(sourceVFSPath,propV);
				}
			}
		
		} catch(Exception e) {
			  CmsLog.getLog(this).error("ERROR obteniento propiedades del video "+e.getMessage());
		}
	}
	
	public String tmpFileFFmepg(String filename, byte[] contentFFmpeg) {
		String tmpFilePath = null;
		
		File temp = new File(System.getProperty("java.io.tmpdir"), "cmsMediosVideoConverter");
		if (!temp.exists()) {
			temp.mkdirs();
			temp.deleteOnExit();
		}
		
		Runtime runtime = Runtime.getRuntime();
		try {
			runtime.exec(new String[] { "/bin/chmod", "775",temp.getAbsolutePath() });
		} catch (IOException e) {
			LOG.log("ERROR", e.getMessage());
			e.printStackTrace();
		}
		
		String folderConvertPath = temp.getAbsolutePath();
		String fileFFmpegPath = folderConvertPath + "/" + filename;
		File uploadedFile = new File(fileFFmpegPath);
		
		try {
			FileOutputStream fOut = new FileOutputStream(uploadedFile);
			fOut.write(contentFFmpeg);
			fOut.close();
			
			tmpFilePath = fileFFmpegPath;
		} catch (Exception e1) {
		   CmsLog.getLog(this).error("Error al intentar subir el archivo " + fileFFmpegPath +" :"+e1.getMessage());
		}
		
		return tmpFilePath;
	}
	
	@SuppressWarnings("rawtypes")
	public String getFormatsByPath(String sourceVFSPath) {
		CmsObject cmsObj = getCmsObject();
		String formats = "";
		String formatsProp = "";
		
		try { 
			CmsResource resource = cmsObj.readResource(sourceVFSPath);
			CmsProperty prop = cmsObj.readPropertyObject(resource, "video-formats", false);
			if (prop!=null)
				formatsProp = prop.getValue();
			
			List<String> formatsList = getConfiguredFormats();
			Iterator iter = formatsList.iterator();
		    while (iter.hasNext()) {
				String format = (String)iter.next(); 
				if(formatsProp ==null || formatsProp.indexOf(format)==-1){
					if(formats.equals(""))
						formats = format;
					else
						formats = formats+","+format;
				}
		    }
			
		} catch (CmsException e) {
			CmsLog.getLog(this).equals("Error al verificar formatos cargados del video: "+e.getMessage());
		}
		
		return formats;
	}
	
	@SuppressWarnings("rawtypes")
	public boolean existsFormatsInQueue(String sourceVFSPath, String formats ) {
		boolean existFormatInQueue = false;
		
		String [] formatList = formats.split(",");
		CmsObject cms = getCmsObject();
		TfsEnconderQueue queue = new TfsEnconderQueue(cms,m_context, request, response);
		List<String> formatsInQueue = queue.getEncoderInfoBySource(sourceVFSPath);
		Iterator itr = formatsInQueue.iterator();
		
		while(itr.hasNext()) {
			String formatQ = (String)itr.next();
			
		    for(int i=0; i<= formatList.length-1; i++) {
		        String formatName = formatList[i];
		               formatName = formatName.trim();
		         
		        if(formatQ.indexOf(formatName) > -1)
	            	  existFormatInQueue = true;
		    }
		}
		
		return existFormatInQueue;
	}
	
	public String getConversionStatus(String sourceVFSPath) {
		
		String status = null;
		int statusInQueue = -1;
		
		TfsEnconderQueue queue = new TfsEnconderQueue(getCmsObject(),m_context, request, response);
		statusInQueue = queue.getQueueStatusByPath(sourceVFSPath);
			
		if(statusInQueue==1)
			status = "OnProcess";
		else if(statusInQueue==0)
			status = "OnQueue";
		else{
			try {
				
				CmsResource resource = getCmsObject().readResource(sourceVFSPath);
				CmsProperty prop = getCmsObject().readPropertyObject(resource, "video-formats", false);
				String videoFormats = null;
				
				if (prop!=null)
				videoFormats = prop.getValue();
				
				if(videoFormats!= null && !videoFormats.equals(""))
				status = "Converted";
		
			} catch (CmsException e) {
				CmsLog.getLog(this).equals("Error al verificar formatos cargados del video: "+e.getMessage());
			}
		}
		
		if(status==null)
			status = "NoFormats";
		
		return status;
		
	}
}