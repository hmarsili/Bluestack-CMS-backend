package com.tfsla.diario.webservices;

import java.io.IOException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.services.ImagenService;
import com.tfsla.diario.ediciones.services.VideoPropertiesService;
import com.tfsla.diario.ediciones.services.VideosService;
import com.tfsla.diario.videoConverter.jsp.TfsEnconderQueue;
import com.tfsla.diario.videoConverter.jsp.TfsVideosAdmin;
import com.tfsla.diario.webservices.common.interfaces.IVideosAddWebService;
import com.tfsla.diario.webservices.common.strings.ExceptionMessages;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.core.services.*;
	
public class VideosAddWebService extends FilesAddWebService implements IVideosAddWebService {

	public VideosAddWebService(HttpServletRequest request) throws Throwable {
		super(request);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected JSON doExecute() throws Throwable {
		Object videoType = this.getPostRequestParam("video-type");
		if(videoType == null || videoType.toString().trim().equals("")) {
			throw new Exception(ExceptionMessages.MISSING_VIDEO_TYPE);
		}
		this.videoType = videoType.toString().toLowerCase();
		
		// Physical upload to RFS and links to VFS
		if(this.videoType.equals(VIDEO_TYPE_DOWNLOAD) || this.videoType.equals(VIDEO_TYPE_FLASH) || this.videoType.equals(VIDEO_TYPE_LINK)) {
			return super.doExecute();
		}
		
		JSONArray jsonResponse = new JSONArray();
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		
		try {
			this.switchToOfflineSession();
			this.processItemsRequest(params, encoding);
			int itemsCount = this.getItemsCount(params);
			
			VideosService service = new VideosService(cms, sitename, MODULE_NAME);
			//VideoEmbeddedService embeddedService = new VideoEmbeddedService();
			for(int i=0; i<itemsCount; i++) {
				JSONObject jsonItem = new JSONObject();
				try {
					String linkName = "";
					String fileIndex = String.format("file[%s]", i);
					List properties = new ArrayList(0);
					this.setCommonProperties(properties, params, fileIndex);
					
					if(this.videoType.equals(VIDEO_TYPE_YOUTUBE)) {
						String videoId = params.get(fileIndex).toString();
						
						String duration = service.getYoutubeDuration(videoId);
						CmsProperty prop = new CmsProperty();
						prop.setName("video-duration");
						prop.setAutoCreatePropertyDefinition(true);
						prop.setStructureValue(duration);
						properties.add(prop);
						
						linkName = service.createYouTubeVideo(null, videoId, properties);
					}
					if(this.videoType.equals(VIDEO_TYPE_EMBEDDED)) {
						//String code = embeddedService.extractVideoCode(params.get(fileIndex).toString());
						String code = params.get(fileIndex).toString();
						linkName = service.createEmbbedeVideo(null, code, properties);
					}
					
					jsonItem.put(StringConstants.STATUS, StringConstants.OK);
					jsonItem.put(StringConstants.NAME, linkName);
				} catch(Exception e) {
					jsonItem.put(StringConstants.STATUS, StringConstants.ERROR);
					jsonItem.put(StringConstants.ERROR, e.getMessage());
				} finally {
					jsonItem.put(StringConstants.INDEX, i);
					jsonResponse.add(jsonItem);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			this.restoreSession();
		}
		return jsonResponse;
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	protected void setProperties(List properties, String fullPath) {
		try {
			VideoPropertiesService service = new VideoPropertiesService();
			service.setVideoProperties(properties, fullPath);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected List<String> getFileExtensions() {
		return this.configuration.getVideoExtensions();
	}

	@Override
	protected String getRFSVirtualUrl() {
		return this.config.getParam(sitename, publication, MODULE_NAME, "rfsVirtualUrl","");
	}

	@Override
	protected String getRFSDirectory() {
		String subFolderRFSPath = this.getSubFolderPath();
		String rfsDirectory = config.getParam(sitename, publication, MODULE_NAME, "rfsDirectory","");
		String rfsPath = rfsDirectory + "/" + subFolderRFSPath;
		return rfsPath;
	}

	@Override
	protected String getVFSDirectory() {
		VideosService service = VideosService.getInstance(cms);
		String vfsSubFolderFormat = config.getParam(sitename, publication, MODULE_NAME, "vfsSubFolderFormat","");
		String vfsPath = this.getVideoPath();
		try {
			HashMap<String, String> parameters = new HashMap<String, String>();
			parameters.put(StringConstants.SECTION, this.section);
			vfsPath += "/" + service.getVFSSubFolderPath(vfsPath, CmsResourceTypeFolder.getStaticTypeId(), vfsSubFolderFormat, parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return vfsPath;
	}

	@Override
	protected String getSubFolderPath() {
		VideosService service = VideosService.getInstance(cms);
		String rfsDirectoryFormat = config.getParam(sitename, publication, MODULE_NAME, "rfsSubFolderFormat","");
		String subFolderRFSPath = "";
		try {
			HashMap<String, String> parameters = new HashMap<String, String>();
			parameters.put(StringConstants.SECTION, this.section);
			subFolderRFSPath = service.getRFSSubFolderPath(rfsDirectoryFormat, parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return subFolderRFSPath;
	}
	
	private String getVideoPath() {
		String videoPath = "";
		if(this.videoType.equals(VIDEO_TYPE_FLASH) || this.videoType.equals(VIDEO_TYPE_LINK)) {
			videoPath = config.getParam(sitename, publication, MODULE_NAME, "defaultVideoFlashPath","");
		}
		if(this.videoType.equals(VIDEO_TYPE_DOWNLOAD)) {
			videoPath = config.getParam(sitename, publication, MODULE_NAME, "defaultVideoDownloadPath","");
		}
		if(this.videoType.equals(VIDEO_TYPE_YOUTUBE)) {
			videoPath = config.getParam(sitename, publication, MODULE_NAME, "defaultVideoYouTubePath","");;
		}
		if(this.videoType.equals(VIDEO_TYPE_EMBEDDED)) { 
			videoPath = config.getParam(sitename, publication, MODULE_NAME, "defaultVideoEmbeddedPath","");
		}
		
		return videoPath;
	}
	
	public String getConfiguredFormats() {
		LinkedHashMap<String,String> values = config.getGroupParam(sitename, publication, MODULE_NAME_CONVERT, "formats");
		
		Set formats = values.keySet();
		Iterator it = formats.iterator();
		
		String formatsList = "";
		
		while( it.hasNext()) {
			formatsList = formatsList+","+(String)it.next();
		}
		
		if(values.size()>0)
			formatsList = formatsList.substring(1);
		else
			formatsList = null;
		
		return formatsList;
    }
	
	@Override
	protected int getPointerType() throws CmsLoaderException {
		if(this.videoType.equals(VIDEO_TYPE_YOUTUBE)) {
			return OpenCms.getResourceManager().getResourceType("video-youtube").getTypeId();
		}
		if(this.videoType.equals(VIDEO_TYPE_EMBEDDED)) { 
			return OpenCms.getResourceManager().getResourceType("video-embedded").getTypeId();
		}
		return OpenCms.getResourceManager().getResourceType("video-link").getTypeId();
	}
	
	protected int getItemsCount(Hashtable<String, Object> params) {
		int itemsCount = 0;
		
		for(String key : params.keySet()) {
			if(key.matches("file\\[[0-9]+\\].*")) {
				String no = key.substring(key.indexOf("[") + 1, key.indexOf("]"));
				int i = Integer.parseInt(no);
				if(i > itemsCount) itemsCount = i;
			}
		}
		return itemsCount + 1;
	}
	
	protected String videoType;
	
	static final public String VIDEO_TYPE_FLASH = "flash";
	static final public String VIDEO_TYPE_LINK = "link";
	static final public String VIDEO_TYPE_DOWNLOAD = "download";
	static final public String VIDEO_TYPE_YOUTUBE = "youtube";
	static final public String VIDEO_TYPE_EMBEDDED = "embedded";
	static final public String MODULE_NAME = "videoUpload";
	static final public String MODULE_NAME_CONVERT = "videoConvert";
	
	@Override
	protected String uploadFile(CmsObject cms, InputStream fileStream,
			String filename) {
		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("section", this.section);
		
		List properties = new ArrayList(1);
		CmsProperty titleProp = new CmsProperty();
		titleProp.setName("classification");
		if (OpenCms.getWorkplaceManager().isDefaultPropertiesOnStructure()) {
			titleProp.setStructureValue("videoflash");
		} else {
			titleProp.setResourceValue("videoflash");
		}
		properties.add(titleProp);
		
		String path = this.getVFSDirectory();
		String fileNameVFS = "";
		String type = "";
		
		try {
			if( VideosService.getInstance(cms).getDefaultUploadDestination().equals("vfs")) {
				VideosService.getInstance(cms).uploadVFSFile(path,cms.getRequestContext().getFileTranslator().translateResource(filename.toLowerCase()),fileStream,properties);
				type = "VFS";
			} else if( ImagenService.getInstance(cms).getDefaultUploadDestination().equals("ftp")){
				fileNameVFS = VideosService.getInstance(cms).uploadFTPFile(path,filename,parameters,fileStream,properties); 
				type = "FTP";
			}else if( VideosService.getInstance(cms).getDefaultUploadDestination().equals("server")){
				fileNameVFS = VideosService.getInstance(cms).uploadRFSFile(path,filename,parameters,fileStream,properties);
				type = "RFS";
			}else if( VideosService.getInstance(cms).getDefaultUploadDestination().equals("amz")){
				fileNameVFS = VideosService.getInstance(cms).uploadAmzFile(path,filename,parameters,fileStream,properties);
				type = "AMZ";
			}
			
			String formatsGenerated = "";
			
			String formats = getConfiguredFormats();
			
			TfsVideosAdmin videoAdmin = new TfsVideosAdmin(cms,sitename, publication);
			
			boolean existsFormatsInQueue = videoAdmin.existsFormatsInQueue( fileNameVFS,formats);
		  	
			if(!existsFormatsInQueue && formats!=null ) {  	
			   formatsGenerated = queueConvert( fileNameVFS, formats, type );
			} 
			
		} catch (CmsException e) {
			LOG.error("Error en crecion de imagen - webservice: " + e.getMessage());
		} catch (IOException e) {
			LOG.error("Error en crecion de imagen - webservice: " + e.getMessage());
		} catch (Exception e) {
			LOG.error("Error en crecion de imagen - webservice: " + e.getMessage());
		}
		return fileNameVFS;

	}
	
	protected String queueConvert(String sourceVFSPath, String formats, String type) {
		String status = "OnQueue";
		
		CmsObject cmsObj = cms;
		TfsEnconderQueue queue = new TfsEnconderQueue(cms);
		CmsUser currentUser = cmsObj.getRequestContext().currentUser();  
		
		int idInQueue = queue.insertQueueDB("/"+sourceVFSPath, formats, type, publication, sitename, currentUser.getName());
		
		int     idInProcess = queue.idVideoOnProcess();
		int idNextInProcess = queue.idNextVideoOnProcess();
			
		if(idInQueue == idInProcess || (idInProcess==0 && idInQueue == idNextInProcess))
			status = "OnProcess";
		
		//queue.checkEncoderQueue();
		
		return status;
	}
}
