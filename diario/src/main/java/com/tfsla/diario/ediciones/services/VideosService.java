package com.tfsla.diario.ediciones.services;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.configuration.CmsSchedulerConfiguration;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.scheduler.jobs.CmsPublishScheduledJob;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsFileUtil;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.workplace.commons.Messages;

import com.google.gdata.client.youtube.YouTubeQuery;
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.util.ServiceException;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.file.types.TfsResourceTypeVideoEmbedded;
import com.tfsla.diario.file.types.TfsResourceTypeVideoLink;
import com.tfsla.diario.file.types.TfsResourceTypeVideoLinkProcessing;
import com.tfsla.diario.file.types.TfsResourceTypeVideoVodEmbedded;
import com.tfsla.diario.file.types.TfsResourceTypeVideoVodLink;
import com.tfsla.diario.file.types.TfsResourceTypeVideoVodYoutube;
import com.tfsla.diario.file.types.TfsResourceTypeVideoYoutubeLink;
import com.tfsla.diario.videoConverter.Encoder;
import com.tfsla.diario.videoConverter.EncoderException;
import com.tfsla.diario.videoConverter.InputFormatException;
import com.tfsla.diario.videoConverter.MultimediaInfo;
import com.tfsla.diario.videoConverter.VideoInfo;
import com.tfsla.diario.videoConverter.VideoInfoExtractor;
import com.tfsla.diario.videoConverter.VideoSize;
import com.tfsla.opencms.dev.collector.DateFolder;
import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.workflow.QueryBuilder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;

public class VideosService extends UploadService {

	private static final Log LOG = CmsLog.getLog(VideosService.class);
	private static Map<String, VideosService> instances = new HashMap<String, VideosService>();

	static final public int VIDEO_TYPE_FLASH = 1;
	static final public int VIDEO_TYPE_DOWNLOAD = 2;
	static final public int VIDEO_TYPE_YOUTUBE = 3;
	static final public int VIDEO_TYPE_EMBEDDED = 4;
	static final public int VIDE_TYPE_FLASH_VOD = 5;
	static final public int VIDEO_TYPE_PROCESSING = 6;
 	
	private String youTubeClient = "";
	private String youTubeKey = "";
	private String defaultVideoDownloadPath = "";
	private String defaultVideoFlashPath = "";
	private String defaultVideoYouTubePath = "";
	private String defaultVideoEmbeddedPath = "";
	private String youTubeDataAPIKey = "";
	private static  String moduleName = "videoUpload";
	
	static private boolean createDateFolderDefault = false;
	
	public static VideosService getInstance(CmsObject cms) {
    	
    	String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	String publication = "0";
     	TipoEdicionBaseService tService = new TipoEdicionBaseService();
    	try {
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());			
			if (tEdicion!=null) {
				publication = "" + tEdicion.getId();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

    	
        return getInstance(cms, siteName, publication);
    }
	
	public static VideosService getInstance(CmsObject cms, String siteName, String publication) {
    	
    	String id = siteName + "||" + publication;
    	VideosService instance = instances.get(id);
    	
    	if (instance == null) {
    		moduleName = "videoUpload";
	    	instance = new VideosService(cms,siteName, publication);
	    	instances.put(id, instance);
    	}
    	
    	instance.cmsObject = cms;
    	
        return instance;
    }

	public static VideosService getInstance(CmsObject cms, String module, String siteName, String publication) {
    	
    	String id = siteName + "||" + publication + "||" + module  ;
    	VideosService instance = instances.get(id);
    	
    	if (instance == null) {
    		setModuleName(module); 
	    	instance = new VideosService(cms,siteName, publication);
	    	instances.put(id, instance);
    	}
    	
    	instance.cmsObject = cms;
    	 return instance;
    }
	
	public static VideosService getInstance(CmsObject cms, String module) {
    	
    	String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	String publication = "0";
     	TipoEdicionBaseService tService = new TipoEdicionBaseService();
    	try {
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());			
			if (tEdicion!=null) {
				publication = "" + tEdicion.getId();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

    	
    	 return getInstance(cms, module, siteName, publication);
    }
	
	
	public VideosService(CmsObject cmsObject, String siteName, String publication) {
		this.cmsObject = cmsObject;
		this.loadProperties(siteName,publication);
	}

	public void loadProperties(String siteName, String publication) {
    	String module = getModuleName();
 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		
		youTubeKey = config.getParam(siteName, publication, module, "youTubeKey","");
		youTubeClient = config.getParam(siteName, publication, module, "defaultVideoEmbeddedPath","");
		
		defaultVideoDownloadPath = config.getParam(siteName, publication, module, "defaultVideoDownloadPath","");
		defaultVideoFlashPath = config.getParam(siteName, publication, module, "defaultVideoFlashPath","");
		
		defaultVideoEmbeddedPath = config.getParam(siteName, publication, module, "defaultVideoEmbeddedPath","");
		defaultVideoYouTubePath = config.getParam(siteName, publication, module, "defaultVideoYouTubePath","");
		
		createDateFolderDefault = config.getBooleanParam(siteName, publication, module, "createDateFolderDefault",false);
		
		youTubeDataAPIKey = config.getParam(siteName, publication, module, "youTubeDataAPIKey");
		
		rfsSubFolderFormat = config.getParam(siteName, publication, module, "rfsSubFolderFormat",""); 
		
		loadBaseProperties(siteName, publication);
	}

	public List<VideoEntry> searchYouTubeVideo(String query, YouTubeQuery.OrderBy orderBy) throws IOException, ServiceException {
		YouTubeService service = new YouTubeService(youTubeClient, youTubeKey);

		//YouTubeQuery ytquery = new YouTubeQuery(new URL("http://gdata.youtube.com/feeds/api/videos"));
		YouTubeQuery ytquery = new YouTubeQuery(new URL("http://gdata.youtube.com/feeds/api/users/" + youTubeClient + "/uploads"));

		if (orderBy!=null)
			ytquery.setOrderBy(orderBy);
		else
			ytquery.setOrderBy(YouTubeQuery.OrderBy.PUBLISHED);
		
		ytquery.setFullTextQuery(query);
		
		ytquery.setMaxResults(50);
		
		VideoFeed videoFeed = service.query(ytquery, VideoFeed.class);

		return videoFeed.getEntries();

	}
	
	@Override 
	public CmsResource uploadVFSFile(String path, String fileName, InputStream content) throws CmsException, IOException {
		List properties = new ArrayList(0);
		return uploadVFSFile(path,fileName,content,properties);
	}
	
	@Override 
	public CmsResource uploadVFSFile(String path, String fileName, InputStream content, List properties) throws CmsException, IOException {
		byte[] buffer = CmsFileUtil.readFully(content, false);
		
		fileName = getValidFileName(fileName);
		
		try {
			    String tmpFile = tmpFileFFmepg(fileName, buffer);
				if(tmpFile != null) {
				   try {
					File uploadedFile = new File(tmpFile);
					Encoder encoder = new Encoder();
				    MultimediaInfo infoVideo = new MultimediaInfo();
								   infoVideo = encoder.getInfo(uploadedFile);
							       
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
					
					CmsProperty prop = new CmsProperty();
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
					prop.setName("video-bitrate");
					prop.setAutoCreatePropertyDefinition(true);
					prop.setStructureValue(bitrate);
					properties.add(prop);
					
				  } catch (InputFormatException e) {
						e.printStackTrace();
				  } catch (EncoderException e) {
						e.printStackTrace();
				  }
				}

			int type = getVFSResourceType(fileName);
			
			CmsResource res = cmsObject.createResource(path + fileName, type, buffer, properties);
			cmsObject.unlockResource(path + fileName);
			
			return res;
		} catch (CmsSecurityException e) {
			// in case of not enough permissions, try to create a plain text file	
			CmsResource res = cmsObject.createResource(path + fileName, CmsResourceTypePlain.getStaticTypeId(), buffer, properties);
			cmsObject.unlockResource(path + fileName);
			return res;
		} catch (CmsDbSqlException sqlExc) {
			// SQL error, probably the file is too large for the database settings, delete file
			cmsObject.lockResource(path + fileName);
			cmsObject.deleteResource(path + fileName, CmsResource.DELETE_PRESERVE_SIBLINGS);
		    throw  sqlExc;   		        
		}
	}
	
	@Override 
	public String uploadRFSFile(String path, String fileName, Map<String,String> parameters, InputStream content) throws Exception {
		List properties = new ArrayList(0);
		return uploadRFSFile(path, fileName, parameters, content,properties);
	}
	
	@Override 
	public String uploadRFSFile(String path, String fileName, Map<String,String> parameters, InputStream content, List properties) throws Exception {
		
		fileName = getValidFileName(fileName);

		LOG.debug("Nombre corregido del archivo a subir al rfs: " + fileName);
		String subFolderRFSPath = getRFSSubFolderPath(rfsSubFolderFormat, parameters);
		
		LOG.debug("subcarpeta: " + subFolderRFSPath);

		File dir = new File(rfsDirectory + "/" + subFolderRFSPath);
		if (!dir.exists() && !dir.mkdirs()) {
			LOG.error("Error al intentar crear el directorio " + dir.getAbsolutePath());
	    }

		String fullPath = dir.getAbsolutePath() + "/" + fileName;
		String url = rfsVirtualUrl + subFolderRFSPath + fileName;
		
		LOG.debug("url : " + url);

		File uploadedFile = new File(fullPath);
		try {
			FileOutputStream fOut = new FileOutputStream(uploadedFile);
			fOut.write(CmsFileUtil.readFully(content, true));
			fOut.close();
		} catch (Exception e1) {
			LOG.error("Error al intentar subir el archivo " + fullPath,e1);
			throw e1;
		}
		
		Encoder encoder = new Encoder();
		MultimediaInfo infoVideo = new MultimediaInfo();
		try {
			infoVideo = encoder.getInfo(uploadedFile);
		} catch (Exception ex){
			LOG.error("Error al realizar el encoding del archivo " + fullPath, ex);
			throw ex;
		} 
		
		long durationMills = infoVideo.getDuration();
		int seconds = (int)((durationMills / 1000)%60);  
		int minutes = (int)((durationMills / 1000)/60);  
		int hours = (int)((durationMills / (1000*60*60))%24);  
									   
		DecimalFormat formateador = new DecimalFormat("00");
		String duration = formateador.format(hours)+":"+formateador.format(minutes)+":"+formateador.format(seconds);
						
		VideoInfo  infoVideoMedia = new VideoInfo();
		infoVideoMedia = infoVideo.getVideo();
		VideoSize	videoSize =  infoVideoMedia.getSize(); 
		String bitrate = infoVideo.getBitrate();
		
		CmsProperty prop = new CmsProperty();
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
		prop.setName("video-bitrate");
		prop.setAutoCreatePropertyDefinition(true);
		prop.setStructureValue(bitrate);
		properties.add(prop);
		
		try {
			String linkName = path + fileName;
			LOG.debug("creando link en vfs: " + linkName);
			
			int type = getPointerType();
			if (!this.videoType.equals("")) {
				type = TfsResourceTypeVideoVodLink.getStaticTypeId();
			}
			cmsObject.createResource(linkName, 
					type,
					url.getBytes(),
					properties);

			cmsObject.unlockResource(linkName);
			return linkName;
		} catch (CmsIllegalArgumentException e) {
			LOG.error("Error al intentar subir el archivo " + fullPath,e);
			throw e;
		} catch (CmsLoaderException e) {
			LOG.error("Error al intentar subir el archivo " + fullPath,e);
			throw e;
		} catch (CmsException e) {
			LOG.error("Error al intentar subir el archivo " + fullPath,e);
			throw e;
		}
	}
	
	@Override 
	public String uploadAmzFile(String path, String fileName, Map<String,String> parameters, InputStream content) throws Exception {
		List properties = new ArrayList(0);
		return uploadAmzFile(path, fileName, parameters, content,properties);
	}
	
	@Override 
	public String uploadAmzFile(String path, String fileName, Map<String,String> parameters, InputStream content, List properties) throws Exception {
			
		String vfsResource = super.uploadAmzFile(path, fileName, parameters, content, properties);
		createSpecificProperties(vfsResource);
		return vfsResource;

	}
	
	public String createVideoProcessing(String path, String fileName, List properties) throws Exception {
		
		fileName = getValidFileName(fileName);

		LOG.debug("Nombre corregido para el archivo del tipo video-processing: " + fileName);
		String subFolderVFSPath = getDefaultVFSUploadFolder(new HashMap<>());
		LOG.debug("subcarpeta: " + subFolderVFSPath);

		String dir =  "/" + subFolderVFSPath;
		if(!dir.endsWith("/")) {
			dir += "/";
		}
		String fullPath = dir + fileName;
		
		try {
			String linkName = path + fileName;
			int type = TfsResourceTypeVideoLinkProcessing.getStaticTypeId();
			
			cmsObject.createResource(linkName, 
					type,
					null,
					properties);

			cmsObject.unlockResource(linkName);
			return linkName;
		} catch (CmsException e) {
			LOG.error("Error al intentar crear el recurso " + fullPath,e);
			throw e;
		}
		
	}
	
	@Override 
	public String uploadAmzFileTM(String fullPath, Map<String,String> parameters, InputStream content) throws Exception {
	
		String amzUrlUploaded = null;
		String uploadStatus = null;
		
		String subFolderRFSPath = getRFSSubFolderPath(rfsSubFolderFormat, parameters);
		String dir = amzDirectory + "/" + subFolderRFSPath;
		
        String fileName = fullPath.substring(fullPath.lastIndexOf('/') + 1);  
		
		String amzPath = dir + fileName;
		
		amzUrlUploaded = super.uploadAmzFileTM(amzPath, parameters, content);
		
		if(amzUrlUploaded!=null && !amzUrlUploaded.equals("Error")) {
			
			byte[] amzUrl = amzUrlUploaded.getBytes();
			
			cmsObject.lockResource(fullPath);
			
			CmsResource resource = cmsObject.readResource(fullPath);
			resource.setType(TfsResourceTypeVideoLink.getStaticTypeId());

			CmsFile file = cmsObject.readFile(resource);
            file.setContents(amzUrl);
            cmsObject.writeFile(file);
			
            cmsObject.unlockResource(fullPath);
            
            try {
            	createSpecificProperties(fullPath);
            } catch (CmsException e) {
    			LOG.error("Error al intentar obtener la info del video: " + fullPath,e);
    		}
         			
		}else {
			uploadStatus = amzUrlUploaded;
		}
		
		return uploadStatus;
	}
	
	public String getAmzSubFolderPath(String vfsPath){
		
		String amzSubFolderPath = "";
		
		Map<String,String> parameters = new HashMap<String,String>();
		
		try {
			String subFolderRFSPath = getRFSSubFolderPath(rfsSubFolderFormat, parameters);
			String dir = "/" +amzDirectory + "/" + subFolderRFSPath;
			
	        String fileName = vfsPath.substring(vfsPath.lastIndexOf('/') + 1);  
			
	        amzSubFolderPath = dir + fileName;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return amzSubFolderPath;
	}
	
	public static String getUploadStatus(String fullPath) {
		
		String status = com.tfsla.diario.ediciones.services.UploadService.getUploadStatus(fullPath);
		
		return status;
	}
	
	public static void setUploadStatus(String path,String status){
		
		com.tfsla.diario.ediciones.services.UploadService.setUploadStatus(path,status);
		
		return;
	}
	
	public String uploadCancel(String path) {
		
		String status = "ok";
		try {
			
			cmsObject.lockResource(path);
			
			CmsResource resource = cmsObject.readResource(path);
			int resourceType = resource.getTypeId();
		
			cmsObject.deleteResource(path, CmsResource.DELETE_PRESERVE_SIBLINGS);
		
			if(OpenCms.getResourceManager().getResourceType("video-processing").getTypeId()==resourceType){
				com.tfsla.diario.ediciones.services.UploadService.uploadCancel(true);
			}else{
				 deleteVideoFromQueueByPath(path);
				 com.tfsla.diario.ediciones.services.UploadService.setUploadStatus(path,"Canceled");
			}
			
			CmsResourceState  estado = resource.getState();
			String estadoStr = estado.toString();
		
			if( !estadoStr.equals("2") )
				OpenCms.getPublishManager().publishResource(cmsObject,path);
		
		} catch (CmsException e) {
			status = "Error."+e.getMessage();
		} catch (Exception e) {
			status = "Error."+e.getMessage();
		}
		
		return status;
	}	
	
	protected void deleteVideoFromQueueByPath(String source) {
		QueryBuilder queryBuilder = new QueryBuilder(cmsObject);
		queryBuilder.setSQLQuery("DELETE FROM TFS_ENCODER_QUEUE WHERE SOURCE=? ");
		queryBuilder.addParameter(source);
		
		queryBuilder.execute();
	}

	protected void createSpecificProperties(String vfsResource) throws Exception {
		
		VideoInfoExtractor infoExtractor = new VideoInfoExtractor();
		MultimediaInfo infoVideo = new MultimediaInfo();
		
		String url = new String(cmsObject.readFile(vfsResource).getContents());
		
		try {
			infoVideo = infoExtractor.getInfo(url);
		} catch (Exception ex){
			LOG.error("Error al buscar informacion adicional de " + vfsResource, ex);
			throw ex;
		}
		
		
		long durationMills = infoVideo.getDuration();
		int seconds = (int)((durationMills / 1000)%60);  
		int minutes = (int)((durationMills / 1000)/60);  
		int hours = (int)((durationMills / (1000*60*60))%24);  
									   
		DecimalFormat formateador = new DecimalFormat("00");
		String duration = formateador.format(hours)+":"+formateador.format(minutes)+":"+formateador.format(seconds);
						
		VideoInfo  infoVideoMedia = new VideoInfo();
		infoVideoMedia = infoVideo.getVideo();
		VideoSize	videoSize =  infoVideoMedia.getSize(); 
		String bitrate = infoVideo.getBitrate();
		
		List properties = new ArrayList(0);
		
		CmsProperty prop = new CmsProperty();
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
		prop.setName("video-bitrate");
		prop.setAutoCreatePropertyDefinition(true);
		prop.setStructureValue(bitrate);
		properties.add(prop);
		
		prop = new CmsProperty();
		prop.setName("video-aspectratio");
		prop.setAutoCreatePropertyDefinition(true);
		prop.setStructureValue(infoVideo.getVideo().getAspectRatio());
		properties.add(prop);
		
		cmsObject.lockResource(vfsResource);
		cmsObject.writePropertyObjects(vfsResource, properties);
		cmsObject.unlockResource(vfsResource);
	}
		
	@Override 
	public String uploadFTPFile(String path, String fileName, Map<String,String> parameters, InputStream content) throws Exception {
		List properties = new ArrayList(0);
		return uploadFTPFile(path, fileName, parameters, content,properties);
	}
	
	@Override 
	public String uploadFTPFile(String path, String fileName, Map<String,String> parameters, InputStream content, List properties) throws Exception {
		String ftpSubfolder = "";
		fileName = getValidFileName(fileName);

		FTPClient client = new FTPClient();
		client.addProtocolCommandListener(new PrintCommandListener(new
				PrintWriter(System.out)));

		byte[] buffer = CmsFileUtil.readFully(content, false);
		
		try {
			client.connect(ftpServer);
			client.login(ftpUser, ftpPassword);
			client.changeWorkingDirectory(ftpDirectory);

			ftpSubfolder = getFTPSubFolderPath(client,parameters);
			
			client.enterLocalPassiveMode();
			client.setFileType(FTPClient.BINARY_FILE_TYPE);

			// Upload a file
			OutputStream os = client.storeFileStream( fileName);

			os.write(buffer);
			os.close();

			client.completePendingCommand();

			//System.out.println(client.printWorkingDirectory());

			client.logout();
			client.disconnect();
		} catch (SocketException e1) {
			LOG.error("Error al intentar subir el archivo a " + ftpServer,e1);
			throw e1;
		} catch (IOException e1) {
			LOG.error("Error al intentar subir el archivo a " + ftpServer,e1);
			throw e1;
		}

		String audioMultimediaPath = ftpVirtualUrl + ftpSubfolder + fileName;
		
		try {
				
			String tmpFile = tmpFileFFmepg(fileName, buffer);
				
			if(tmpFile!=null){
				   try {
					File uploadedFile = new File(tmpFile);
					Encoder encoder = new Encoder();
				    MultimediaInfo infoVideo = new MultimediaInfo();
								   infoVideo = encoder.getInfo(uploadedFile);
							       
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
					
					CmsProperty prop = new CmsProperty();
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
					prop.setName("video-bitrate");
					prop.setAutoCreatePropertyDefinition(true);
					prop.setStructureValue(bitrate);
					properties.add(prop);
				  } catch (InputFormatException e) {
						e.printStackTrace();
				  } catch (EncoderException e) {
						e.printStackTrace();
				  }
			}
			

			String linkName = path + fileName;
			cmsObject.createResource(linkName, 
					getPointerType(),
					audioMultimediaPath.getBytes(),
					properties);

			cmsObject.unlockResource(linkName);
			
			return linkName;
		} catch (CmsIllegalArgumentException e) {
			LOG.error("Error al intentar subir el archivo a " + ftpServer,e);
			throw e;
		} catch (CmsLoaderException e) {
			LOG.error("Error al intentar subir el archivo a " + ftpServer,e);
			throw e;
		} catch (CmsException e) {
			LOG.error("Error al intentar subir el archivo a " + ftpServer,e);
			throw e;
		}
	}
	
	@Deprecated
	public String uploadRFSVideo(String path, FileItem item ) throws Exception
	{
		return uploadRFSVideo(path, item, false);
	}
	
	@Deprecated
	public String uploadRFSVideo(String path, FileItem item, boolean createDateFolder ) throws Exception
	{
		String videoMultimediaPath = rfsDirectory + item.getName();
		String videoMultimediaUrl = rfsVirtualUrl + item.getName();		
		File uploadedFile = new File(videoMultimediaPath);
		try {
			item.write(uploadedFile);
		} catch (Exception e1) {
			LOG.error("Error al intentar subir el video " + videoMultimediaPath,e1);
			throw e1;
		}

		try {
			
			if (createDateFolder)
				path = new DateFolder(path, true).getTodayFolder(cmsObject);
			
			String linkName = getNextVideoName(cmsObject,path,"videoRFS");

			List properties = new ArrayList(0);

			cmsObject.createResource(linkName, 
					OpenCms.getResourceManager().getResourceType("video-link").getTypeId(),
					videoMultimediaUrl.getBytes(),
					properties);

			return linkName;
		} catch (CmsIllegalArgumentException e) {
			LOG.error("Error al intentar subir el video " + videoMultimediaPath,e);
			throw e;
		} catch (CmsLoaderException e) {
			LOG.error("Error al intentar subir el video " + videoMultimediaPath,e);
			throw e;
		} catch (CmsException e) {
			LOG.error("Error al intentar subir el video " + videoMultimediaPath,e);
			throw e;
		}

	}

	@Deprecated
	public String uploadFTPVideo(String path, FileItem item) throws Exception
	{
		return uploadFTPVideo(path, item, false);
	}
	
	@Deprecated
	public String uploadFTPVideo(String path, FileItem item,  boolean createDateFolder ) throws Exception
	{

		FTPClient client = new FTPClient();
		client.addProtocolCommandListener(new PrintCommandListener(new
				PrintWriter(System.out)));

		try {
			client.connect(ftpServer);
			client.login(ftpUser, ftpPassword);
			client.changeWorkingDirectory(ftpDirectory);

			
			client.enterLocalPassiveMode();
			client.setFileType(FTPClient.BINARY_FILE_TYPE);

			// Upload a file
			OutputStream os = client.storeFileStream( item.getName());

			byte[] buffer = null;
			if (item.getSize() == -1) {
				buffer = CmsFileUtil.readFully(item.getInputStream(), false);
			} else {
				buffer = CmsFileUtil.readFully(item.getInputStream(), (int)item.getSize(), false);
			}
			os.write(buffer, 0, (int)item.getSize());
			os.close();

			client.completePendingCommand();

			//System.out.println(client.printWorkingDirectory());

			client.logout();
			client.disconnect();

		} catch (SocketException e1) {
			LOG.error("Error al intentar subir el video a " + ftpServer,e1);
			throw e1;
		} catch (IOException e1) {
			LOG.error("Error al intentar subir el video a " + ftpServer,e1);
			throw e1;
		}

		String videoMultimediaPath = ftpVirtualUrl + item.getName();


		try {
			
			if (createDateFolder)
				path = new DateFolder(path, true).getTodayFolder(cmsObject);

			String linkName = getNextVideoName(cmsObject,path,"videoRFS");

			List properties = new ArrayList(0);

			cmsObject.createResource(linkName, 
					OpenCms.getResourceManager().getResourceType("video-link").getTypeId(),
					videoMultimediaPath.getBytes(),
					properties);

			return linkName;
		} catch (CmsIllegalArgumentException e) {
			LOG.error("Error al intentar subir el video a " + ftpServer,e);
			throw e;
		} catch (CmsLoaderException e) {
			LOG.error("Error al intentar subir el video a " + ftpServer,e);
			throw e;
		} catch (CmsException e) {
			LOG.error("Error al intentar subir el video a " + ftpServer,e);
			throw e;
		}

	}
	
	public String createYouTubeVideo(String path, String videoId, List<CmsProperty> properties ) {
		return createYouTubeVideo( path,  videoId,  properties, null );
	}

	
	public String createYouTubeVideo(String path, String videoId, List<CmsProperty> properties, String type )
	{
		if (path==null){ 
			path = defaultVideoYouTubePath;
				
			if(path.lastIndexOf("/")!= path.length()-1)
				path = path+"/";
			
			if (isCreateDateFolderDefault())
				path = new DateFolder(path, true).getTodayFolder(cmsObject);
			
		}
		
		try {
			String linkName = getNextVideoName(cmsObject,path,"videoYouTube");
			
			CmsProperty prop = new CmsProperty();
					    prop.setName("video-code");
					    prop.setAutoCreatePropertyDefinition(true);
					    prop.setStructureValue(videoId);
			properties.add(prop);
			
			int typeID =TfsResourceTypeVideoYoutubeLink.getStaticTypeId();
			if (type!= null && type.equals("videoVod-youtube")) {
				typeID = TfsResourceTypeVideoVodYoutube.getStaticTypeId();
			}
			CmsResource res = cmsObject.createResource(linkName, 
					typeID,
					videoId.getBytes(),
					properties);
			
			cmsObject.unlockResource(cmsObject.getSitePath(res));
			
			return cmsObject.getSitePath(res);
			
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
	}
	
	public String createYouTubeVideo(String path, String videoId, String title, String fuente, String tags, String imagen,String descripcion,String autor,String calificacion,String categoria,String autoplay,String mute){
		return createYouTubeVideo(path, videoId,  title,  fuente,  tags,  imagen, descripcion, autor, calificacion, categoria, autoplay, mute,	TfsResourceTypeVideoYoutubeLink.getStaticTypeId());
	}
	
	public String createYouTubeVideo(String path, String videoId, String title, String fuente, String tags, String imagen,String descripcion,String autor,String calificacion,String categoria,String autoplay,String mute,int videoType)
	{
		//TODO: Armar que la creacion de videso youtube y embedded tome el path de la publicacion.
		if (path==null) 
			path = defaultVideoYouTubePath;
		
		if (isCreateDateFolderDefault()) {
			if (path.lastIndexOf("/") < path.length()-1) 
				path +="/";
			path = new DateFolder(path, true).getTodayFolder(cmsObject);
		}
		String linkName = "";
		
		try {
			linkName = getNextVideoName(cmsObject,path,"videoYouTube");
		
			
			List<CmsProperty> properties = new ArrayList<CmsProperty>(3);
			
			CmsProperty prop = new CmsProperty();
			prop.setName(CmsPropertyDefinition.PROPERTY_TITLE);
			prop.setStructureValue(title);
			properties.add(prop);
			
			prop = new CmsProperty();
			prop.setName("Agency");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(fuente);
			properties.add(prop);
			 
			prop = new CmsProperty();
			prop.setName("Keywords");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(tags);
			properties.add(prop);
			 
			prop = new CmsProperty();
			prop.setName("prevImage");
			prop.setStructureValue(imagen);
			prop.setAutoCreatePropertyDefinition(true);
			properties.add(prop);
				
			prop = new CmsProperty();
			prop.setName(CmsPropertyDefinition.PROPERTY_DESCRIPTION);
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(descripcion);
			properties.add(prop);
				
			prop = new CmsProperty();
			prop.setName("video-rated");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(calificacion);
			properties.add(prop);
				
			prop = new CmsProperty();
			prop.setName("Author");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(autor);
			properties.add(prop);
				
			prop = new CmsProperty();
			prop.setName("category");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(categoria);
			properties.add(prop);
			
			String duration = getYoutubeDuration(videoId);
			
			prop = new CmsProperty();
			prop.setName("video-duration");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(duration);
			properties.add(prop);
			
			prop = new CmsProperty();
			prop.setName("video-code");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(videoId);
			properties.add(prop);
			
			prop = new CmsProperty();
			prop.setName("video-autoplay");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(autoplay);
			properties.add(prop);
			
			prop = new CmsProperty();
			prop.setName("video-mute");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(mute);
			properties.add(prop);
			
			LOG.debug("Se creara el video Youtube con el path "+linkName+" .Uid: "+videoId);
			
			CmsResource res = cmsObject.createResource(linkName, 
					videoType,
					videoId.getBytes(),
					properties);
			
			cmsObject.unlockResource(cmsObject.getSitePath(res));
			
			return cmsObject.getSitePath(res);
			
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error("Error en la creación del video youtube en el vfs. Uid: "+videoId+". Link en  el vfs: "+linkName+". Error:"+e.getMessage());
		}

		return "";
		
	}
	
	
	public String createEmbbedeVideo(String path, String videoCode, List<CmsProperty> properties) {
		return createEmbbedeVideo( path,  videoCode, properties, null);
	}
	
	public String createEmbbedeVideo(String path, String videoCode, List<CmsProperty> properties,String type)
	{
		
		if (path==null){ 
			path = defaultVideoEmbeddedPath;
				
			if(path.lastIndexOf("/")!= path.length()-1)
				path = path+"/";
			
			if (isCreateDateFolderDefault()){
				path = new DateFolder(path, true).getTodayFolder(cmsObject);
			}
		}
		
		
		try {
			String linkName = getNextVideoName(cmsObject,path,"videoEmbedded");
			
			CmsProperty prop = new CmsProperty();
					    prop.setName("video-code");
					    prop.setAutoCreatePropertyDefinition(true);
					    prop.setStructureValue(videoCode);
			properties.add(prop);
		
			int typeId = TfsResourceTypeVideoEmbedded.getStaticTypeId();
			if (type!= null && type.equals(TfsResourceTypeVideoVodEmbedded.getStaticTypeName())) {
				 typeId = TfsResourceTypeVideoVodEmbedded.getStaticTypeId();
			}
			CmsResource res = cmsObject.createResource(linkName, 
					typeId,
					videoCode.getBytes(),
					properties);
			
			cmsObject.unlockResource(cmsObject.getSitePath(res));
			
			return cmsObject.getSitePath(res);
			
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
		
	}
	public String createEmbbedeVideo(String path, String videoCode, String title, String fuente, String tags, String imagen,String descripcion, String autor,String calificacion, String categoria){
		return createEmbbedeVideo(path, videoCode, title, fuente, tags, imagen, descripcion, autor, calificacion, categoria,TfsResourceTypeVideoEmbedded.getStaticTypeId());
	}
	
	public String createEmbbedeVideo(String path, String videoCode, String title, String fuente, String tags, String imagen,String descripcion, String autor,String calificacion, String categoria, int videoType) {
		
		if (path==null) 
			path = defaultVideoEmbeddedPath;
		
		if (isCreateDateFolderDefault()){
			if (path.lastIndexOf("/") < path.length()-1) 
				path +="/";
			path = new DateFolder(path, true).getTodayFolder(cmsObject);
		}
		
		try {
			String linkName = getNextVideoName(cmsObject,path,"videoEmbedded");
		
			
			List<CmsProperty> properties = new ArrayList<CmsProperty>(3);
			
			CmsProperty prop = new CmsProperty();
			prop.setName(CmsPropertyDefinition.PROPERTY_TITLE);
			prop.setStructureValue(title);
			properties.add(prop);
			
			prop = new CmsProperty();
			prop.setName("Agency");
			prop.setStructureValue(fuente);
			prop.setAutoCreatePropertyDefinition(true);
			properties.add(prop);
			 
			prop = new CmsProperty();
			prop.setName("Keywords");
			prop.setStructureValue(tags);
			prop.setAutoCreatePropertyDefinition(true);
			properties.add(prop);
			
			prop = new CmsProperty();
			prop.setName("prevImage");
			prop.setStructureValue(imagen);
			prop.setAutoCreatePropertyDefinition(true);
			properties.add(prop);
			
			prop = new CmsProperty();
			prop.setName(CmsPropertyDefinition.PROPERTY_DESCRIPTION);
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(descripcion);
			properties.add(prop);
			
			prop = new CmsProperty();
			prop.setName("video-rated");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(calificacion);
			properties.add(prop);
			
			prop = new CmsProperty();
			prop.setName("Author");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(autor);
			properties.add(prop);
			
			prop = new CmsProperty();
			prop.setName("category");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(categoria);
			properties.add(prop);
			
			prop = new CmsProperty();
			prop.setName("video-code");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(videoCode);
			properties.add(prop);
			 
			CmsResource res = cmsObject.createResource(linkName, 
					videoType,
					videoCode.getBytes(),
					properties);
			
			cmsObject.unlockResource(cmsObject.getSitePath(res));
			
			return cmsObject.getSitePath(res);
			
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
		
	}
	
public String uploadYoutubeImage(String youtubeId, String youtubeUrl, String type) throws Exception{
		
		String status = null;
		
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put("section","videos");
		
		String path = "/" + ImagenService.getInstance(cmsObject).getDefaultVFSUploadFolder(parameters);
		
		String fileName = youtubeId+"_"+youtubeUrl.substring(youtubeUrl.lastIndexOf("/")+1);
		
		URL url;
		
							  url = new URL(youtubeUrl);
		InputStream            in = new BufferedInputStream(url.openStream());
	    
	     
	     if(type.equals("VFS")){
	    	 
	    	 ByteArrayOutputStream out = new ByteArrayOutputStream();
	 	     byte[]                buf = new byte[1024];
	 	     int                     n = 0;
	 	        
	 	     while (-1 != (n = in.read(buf))) {
	 	            out.write(buf, 0, n);
	 	     }
	 	     
	 	     byte[] buffer = out.toByteArray();
	 	     
	 	     out.close();
	    	 
	    	 int resourceType = OpenCms.getResourceManager().getResourceType("image").getTypeId();
	 		
	    	 String fullPath = path + fileName;
	    	 
	    	 cmsObject.createResource(fullPath, resourceType, buffer, null);
	    	 cmsObject.unlockResource(fullPath);
			 
			 status = fullPath;
	    	 
	     }else if(type.equals("RFS")){
  
	    	 	String lcFileName = fileName.toLowerCase();
	    	 	String imageVFS = ImagenService.getInstance(cmsObject).uploadRFSFile(path,lcFileName,parameters,in);
	
	    	 	status = imageVFS;
	    	 
	     }else if(type.equals("FTP")){
	    	 
	    	   String lcFileName = fileName.toLowerCase();
	 		   String imageFTP = ImagenService.getInstance(cmsObject).uploadFTPFile(path,lcFileName,parameters,in);
	 		
	 		   status = imageFTP;
	     }
	     
	      in.close();
	     
	     return status;
		
	}

	@Override
	protected int getVFSResourceType(String fileName) throws CmsException {
		return  OpenCms.getResourceManager().getResourceType("video").getTypeId();
	}

	public void addNewsCountToVideo(CmsObject cmsObject, String videoPath)
	{
		try {
			CmsResourceUtils.forceLockResource(cmsObject, videoPath);
		
			CmsRelationFilter filter = CmsRelationFilter.SOURCES;
			List<CmsRelation> rels = cmsObject.getRelationsForResource(videoPath, filter);
			int cantNoticias = 0;
	
			for (CmsRelation rel : rels)
			{
				int type = rel.getSource(cmsObject, CmsResourceFilter.ALL).getTypeId();
				if (type == OpenCms.getResourceManager().getResourceType("noticia").getTypeId())
					cantNoticias++;
			}
			if (cantNoticias>0)
				cmsObject.writePropertyObject(videoPath, new CmsProperty("newsCount","" + cantNoticias,"" + cantNoticias));
			else
				cmsObject.writePropertyObject(videoPath, new CmsProperty("newsCount",CmsProperty.DELETE_VALUE,CmsProperty.DELETE_VALUE));


			cmsObject.unlockResource(videoPath);

		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addNewsCountToVideo(CmsObject cmsObject, String videoPath, String videoType)
	{
		try {
			CmsResourceUtils.forceLockResource(cmsObject, videoPath);
		
			CmsRelationFilter filter = CmsRelationFilter.SOURCES.filterType(CmsRelationType.valueOf(videoType));
			List rel = cmsObject.getRelationsForResource(videoPath, filter);
			int cantNoticias = rel.size();
	
			if (cantNoticias>0)
				cmsObject.writePropertyObject(videoPath, new CmsProperty("newsCount","" + cantNoticias,"" + cantNoticias));
			else
				cmsObject.writePropertyObject(videoPath, new CmsProperty("newsCount",CmsProperty.DELETE_VALUE,CmsProperty.DELETE_VALUE));

	
			cmsObject.unlockResource(videoPath);

		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	private String getNextVideoName(CmsObject obj,String location, String prefixVideoName) throws CmsException
	{
		String fileName="";

		//videoRFS
		int maxVideoValue  = 0;
		List cmsFiles = obj.getResourcesInFolder(location, CmsResourceFilter.ALL);
		for (Iterator it = cmsFiles.iterator(); it.hasNext();) {
			CmsResource resource = (CmsResource) it.next();
			fileName = resource.getName();
			if (fileName.matches(".*" + prefixVideoName + "_[0-9]{4}.lnk")) {
				String auxFileName =fileName.substring(fileName.indexOf(prefixVideoName + "_"));
				int newsValue = Integer.parseInt(auxFileName.replace(prefixVideoName + "_","").replace(".lnk",""));
				if (maxVideoValue<newsValue)
					maxVideoValue=newsValue;
			}
		}

		DecimalFormat df = new DecimalFormat("0000");
		fileName = location + prefixVideoName + "_" + df.format(maxVideoValue+1) + ".lnk"; 

		return fileName;
	}

	public static boolean isCreateDateFolderDefault() {
		return createDateFolderDefault;
	}
	
	public String getDefaultUploadFolder(Map<String,String> parameters, int videoType) throws Exception
	{
		String videoPath = "";
		
		switch (videoType) {
			case (VIDEO_TYPE_FLASH):
				videoPath = defaultVideoFlashPath;
				this.videoType = "";
			
				break;
			case VIDEO_TYPE_DOWNLOAD:
				videoPath = defaultVideoDownloadPath;
				break;
			case VIDEO_TYPE_YOUTUBE:
				videoPath = defaultVideoYouTubePath;
				break;
			case VIDEO_TYPE_EMBEDDED: 
				videoPath = defaultVideoEmbeddedPath;
				break;
			case VIDE_TYPE_FLASH_VOD: 
				videoPath = defaultVideoFlashPath;
				this.videoType = "videoVod-link";
				break;
			case VIDEO_TYPE_PROCESSING:
				videoPath = defaultVideoFlashPath;
				this.videoType = "video-processing";
			break;
		}		
		return videoPath + "/" + getVFSSubFolderPath(videoPath, getVfsFolderType(), vfsSubFolderFormat, parameters);
	}
	
	@Override
	protected int getVfsFolderType() {	
		return CmsResourceTypeFolder.getStaticTypeId();
	}

	@Override
	protected String getModuleName() {
		return moduleName;
	}
	
	protected static void setModuleName(String module) {
		 moduleName = module;
	}
	
	@Override
	protected int getPointerType()  throws CmsLoaderException
	{
		return OpenCms.getResourceManager().getResourceType("video-link").getTypeId();
	}
	
public Object getYoutubeAPIData(String youtubeId){
		
		String urlYoutube = "https://www.googleapis.com/youtube/v3/videos?id="+youtubeId+"&key="+youTubeDataAPIKey+"&part=snippet,contentDetails&fields=items(id,snippet(title,description,thumbnails),contentDetails)";
		
		HttpURLConnection requestYoutube = null;
		JSONObject jsonObject = null;
		
		try {
			URL url = new URL(urlYoutube); 
			
			requestYoutube = (HttpURLConnection)url.openConnection();
			requestYoutube.connect();
			
			int responseCode = requestYoutube.getResponseCode();
			
			if(responseCode!=200)
				return null;
			
			BufferedReader in = new BufferedReader(new InputStreamReader(requestYoutube.getInputStream()));
			
	    	String resultString;
	    	String result = "";
	    	
	    	while ((resultString = in.readLine()) != null) {
 	    		result += resultString;
	    	}
	    	   
	    	in.close();
	    	    		
	    	jsonObject = (JSONObject) JSONSerializer.toJSON( result );
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
		return jsonObject;
	}
	
	public String getYoutubeDuration(String youtubeID){
		
		String duration = "";
		
		JSONObject json = null;
		           json = (JSONObject) getYoutubeAPIData(youtubeID);
		
		if(json==null || json.isNullObject()){
			LOG.debug("No se pudo obtener la duracion para el video de youtube "+youtubeID);
		   	return "";
		}
		   		
		 JSONArray    items = json.getJSONArray("items");
		 String durationStr = null;
		   		
		 for (int i = 0; i < items.size(); i++)
		 {
		   		   JSONObject contentDetails = (JSONObject) items.getJSONObject(i).getJSONObject("contentDetails");
		   		   
		   		   if(!contentDetails.isNullObject()){
		   			   durationStr = contentDetails.getString("duration");
		   		   }
		  }
		   		
		 if(durationStr!=null){
		   			String durationTmp = durationStr.replaceAll("PT","");
		   			durationTmp = durationTmp.replaceAll("H",":");
		   			durationTmp = durationTmp.replaceAll("M",":");
		   			durationTmp = durationTmp.replaceAll("S","");
		   			
		   			if(durationStr.indexOf("H")==-1)
		   				durationTmp = "00:"+durationTmp;
		   			
		   			if(durationStr.indexOf("S")==-1)
		   				durationTmp = durationTmp+"00";
		   			
		   			String pt[] = durationTmp.split(":");
		   			
		   			String h = pt[0];
		   			
		   			if(pt[0].length()<2)
		   				h = 0+pt[0];
		   			
		   			String m = pt[1];
		   			
		   			if(pt[1].length()<2)
		   				m = 0+pt[1];
		   			
		   			String s = pt[2];
		   			
		   			if(pt[2].length()<2)
		   				s = 0+pt[2];
		   			
		   			duration = h + ":" + m + ":" + s;
		 }
			
		return duration;
	}
	
	public ArrayList<HashMap<String, String>> getThumbnails(String youtubeid){
		
		ArrayList<HashMap<String, String>> thumbnails = new ArrayList<HashMap<String, String>>();
		
		String[] urlValues = youtubeid.split("v=");
		if (urlValues.length > 1){
			urlValues = urlValues[1].split("&");
			youtubeid = urlValues[0];
			
		}
		
		JSONObject json = (JSONObject) getYoutubeAPIData(youtubeid);
		
		if(json!=null && !json.isNullObject()){
			JSONArray    items = json.getJSONArray("items");
			HashMap<String, String> thumbnail = new HashMap<String, String>();
			
			for (int i = 0; i < items.size(); i++)
			{
			   JSONObject snippet = (JSONObject) items.getJSONObject(i).getJSONObject("snippet");
			   JSONObject images = (JSONObject) snippet.getJSONObject("thumbnails");
			   
			   if(!images.getJSONObject("default").isNullObject()){
				   thumbnail.put("name", "default");
				   thumbnail.put("url", images.getJSONObject("default").getString("url"));
				   thumbnail.put("height",images.getJSONObject("default").getString("height"));
				   thumbnail.put("width", images.getJSONObject("default").getString("width"));
				   
				   thumbnails.add(thumbnail);
			   }
			   
			   if(!images.getJSONObject("medium").isNullObject()){
				   
				   thumbnail = new HashMap<String, String>();
				   
				   thumbnail.put("name", "medium");
				   thumbnail.put("url", images.getJSONObject("medium").getString("url"));
				   thumbnail.put("height",images.getJSONObject("medium").getString("height"));
				   thumbnail.put("width", images.getJSONObject("medium").getString("width"));
				   
				   thumbnails.add(thumbnail);
			   }
			   
			   if(!images.getJSONObject("high").isNullObject()){
				   
				   thumbnail = new HashMap<String, String>();
				   
				   thumbnail.put("name", "high");
				   thumbnail.put("url", images.getJSONObject("high").getString("url"));
				   thumbnail.put("height",images.getJSONObject("high").getString("height"));
				   thumbnail.put("width", images.getJSONObject("high").getString("width"));
				   
				   thumbnails.add(thumbnail);
			   }
			   
			   if(!images.getJSONObject("standard").isNullObject()){
				   
				   thumbnail = new HashMap<String, String>();
				   
				   thumbnail.put("name", "standard");
				   thumbnail.put("url", images.getJSONObject("standard").getString("url"));
				   thumbnail.put("height",images.getJSONObject("standard").getString("height"));
				   thumbnail.put("width", images.getJSONObject("standard").getString("width"));
				   
				   thumbnails.add(thumbnail);
			   }
			   
			   if(!images.getJSONObject("maxres").isNullObject()){
				   
				   thumbnail = new HashMap<String, String>();
				   
				   thumbnail.put("name", "maxres");
				   thumbnail.put("url", images.getJSONObject("maxres").getString("url"));
				   thumbnail.put("height",images.getJSONObject("maxres").getString("height"));
				   thumbnail.put("width", images.getJSONObject("maxres").getString("width"));
				   
				   thumbnails.add(thumbnail);
			   }
			    
			}
		}

		return thumbnails;
		
	}
	
	public void scheduledPublishContent(String publishScheduledDate, String[] resources) throws CmsException, ParseException {
	        // get the request parameters for resource and publish scheduled date
	        String userName = cmsObject.getRequestContext().currentUser().getName();
	
	        // get the java date format
	        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.ENGLISH);
	        Date date = dateFormat.parse(publishScheduledDate);
	
	        // check if the selected date is in the future
	        if (date.getTime() < new Date().getTime()) {
	            // the selected date in in the past, this is not possible
	            throw new CmsException(Messages.get().container(Messages.ERR_PUBLISH_SCHEDULED_DATE_IN_PAST_1, date));
	        }
	
	        // make copies from the admin cmsobject and the user cmsobject
	        // get the admin cms object
	        CmsWorkplaceAction action = CmsWorkplaceAction.getInstance();
	        CmsObject cmsAdmin = action.getCmsAdminObject();
	        // get the user cms object
	        CmsObject cms = OpenCms.initCmsObject(cmsObject);
	
	        // set the current user site to the admin cms object
	        cmsAdmin.getRequestContext().setSiteRoot(cms.getRequestContext().getSiteRoot());
	
	        // create the temporary project, which is deleted after publishing
	        // the publish scheduled date in project name
	        String dateTime = CmsDateUtil.getDateTime(date, DateFormat.SHORT, Locale.ENGLISH);
	        
	        // the resource name to publish scheduled
        	String resName = CmsResource.getName(resources[0]);
        
	       String projectName = "publish videos: " + resName + " / " + dateTime.toString();
	        // the HTML encoding for slashes is necessary because of the slashes in english date time format
	        // in project names slahes are not allowed, because these are separators for organizaional units
	        projectName = projectName.replace("/", "&#47;");
	        // create the project
	        CmsProject tmpProject = cmsAdmin.createProject(
	            projectName,
	            "",
	            CmsRole.WORKPLACE_USER.getGroupName(),
	            CmsRole.PROJECT_MANAGER.getGroupName(),
	            CmsProject.PROJECT_TYPE_TEMPORARY);
	        // make the project invisible for all users
	        tmpProject.setHidden(true);
	        // write the project to the database
	        cmsAdmin.writeProject(tmpProject);
	        // set project as current project
	        cmsAdmin.getRequestContext().setCurrentProject(tmpProject);
	        cms.getRequestContext().setCurrentProject(tmpProject);
			
	        for (int i = 0; i < resources.length; i++) {
				
	        	
		        // copy the resource to the project
		        cmsAdmin.copyResourceToProject(resources[i]);
	
		        // lock the resource in the current project
		        CmsLock lock = cms.getLock(resources[i]);
		        // prove is current lock from current but not in current project
		        if ((lock != null)
		            && lock.isOwnedBy(cms.getRequestContext().currentUser())
		            && !lock.isOwnedInProjectBy(cms.getRequestContext().currentUser(), cms.getRequestContext().currentProject())) {
		            // file is locked by current user but not in current project
		            // change the lock from this file
		            cms.changeLock(resources[i]);
		        }
		        // lock resource from current user in current project
		        cms.lockResource(resources[i]);
		        // get current lock
		        lock = cms.getLock(resources[i]);
	        }
	        
	        // create a new scheduled job
	        CmsScheduledJobInfo job = new CmsScheduledJobInfo();
	        // the job name
	        String jobName = projectName;
	        // set the job parameters
	        job.setJobName(jobName);
	        job.setClassName("org.opencms.scheduler.jobs.CmsPublishScheduledJob");
	        // create the cron expression
	        Calendar calendar = Calendar.getInstance();
	        calendar.setTime(date);
	        String cronExpr = ""
	            + calendar.get(Calendar.SECOND)
	            + " "
	            + calendar.get(Calendar.MINUTE)
	            + " "
	            + calendar.get(Calendar.HOUR_OF_DAY)
	            + " "
	            + calendar.get(Calendar.DAY_OF_MONTH)
	            + " "
	            + (calendar.get(Calendar.MONTH) + 1)
	            + " "
	            + "?"
	            + " "
	            + calendar.get(Calendar.YEAR);
	        // set the cron expression
	        job.setCronExpression(cronExpr);
	        // set the job active
	        job.setActive(true);
	        // create the context info
	        CmsContextInfo contextInfo = new CmsContextInfo();
	        contextInfo.setProjectName(projectName);
	        contextInfo.setUserName(cms.getRequestContext().currentUser().getName());
	        //contextInfo.setUserName(cmsAdmin.getRequestContext().currentUser().getName());
	        // create the job schedule parameter
	        SortedMap<String, String> params = new TreeMap<String, String>();
	        // the user to send mail to
	        params.put(CmsPublishScheduledJob.PARAM_USER, userName);
	        // the job name
	        params.put(CmsPublishScheduledJob.PARAM_JOBNAME, jobName);
	        // the link check
	        params.put(CmsPublishScheduledJob.PARAM_LINKCHECK, "true");
	        // add the job schedule parameter
	        job.setParameters(params);
	        // add the context info to the scheduled job
	        job.setContextInfo(contextInfo);
	        // add the job to the scheduled job list
	        OpenCms.getScheduleManager().scheduleJob(cmsAdmin, job);
	        OpenCms.writeConfiguration(CmsSchedulerConfiguration.class);
	        
    }

	@Override
	public JSONObject callbackUpload(JSONObject data) {
		// TODO Falta implementar
		throw new RuntimeException("Metodo no implementado!");
	}

}
