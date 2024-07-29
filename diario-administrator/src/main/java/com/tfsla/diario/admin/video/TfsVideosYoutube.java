package com.tfsla.diario.admin.video;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchResult;
import org.opencms.search.TfsAdvancedSearch;
import org.opencms.util.CmsUUID;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.ImagenService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.VideosService;
import com.tfsla.diario.file.types.TfsResourceTypeVideoYoutubeLink;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class TfsVideosYoutube {
	
	private CPMConfig config;
	private String siteName;
    private String publication;
    private TipoEdicion currentPublication;
	
    private HttpSession m_session;
    private CmsFlexController m_controller;
    private CmsObject m_cms;
    
  //private String  youtubeAPI = "http://gdata.youtube.com/feeds/api/videos/";
    private String youtubeAPI = "https://www.googleapis.com/youtube/v3/videos";
    private String youtubeID;
    private String moduleConfig;
    
    private String videoIndex = "VIDEOS_OFFLINE";
    
    public TfsVideosYoutube(CmsObject cms) throws Exception{
    	m_cms = cms;
    	
    	siteName = OpenCms.getSiteManager().getCurrentSite(m_cms).getSiteRoot();

    	if (currentPublication==null) {
    		String project = siteName.substring(siteName.lastIndexOf("/")+1);
    				
        	TipoEdicionService tService = new TipoEdicionService();
    		
    		if (tService!=null) {
    			currentPublication = tService.obtenerEdicionOnlineRoot(project);
				videoIndex = currentPublication.getVideosIndexOffline();
				publication = "" + currentPublication.getId();
			}
    	}
    	
    	config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
    }
    
    public TfsVideosYoutube(CmsObject cms, String module) throws Exception{
    	m_cms = cms;
    	
    	siteName = OpenCms.getSiteManager().getCurrentSite(m_cms).getSiteRoot();

    	if (currentPublication==null) {
    		String project = siteName.substring(siteName.lastIndexOf("/")+1);
    				
        	TipoEdicionService tService = new TipoEdicionService();
    		
    		if (tService!=null) {
    			currentPublication = tService.obtenerEdicionOnlineRoot(project);
				videoIndex = currentPublication.getVideosIndexOffline();
				publication = "" + currentPublication.getId();
			}
    	}
    	
    	if (module != null)
    		moduleConfig = module;
    	config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
    }
    
    public TfsVideosYoutube(CmsObject cms, String siteNAME, String publicationID) throws Exception{
    	
    	m_cms = cms;
    	siteName = siteNAME;
    	publication = publicationID;
    	
    	TipoEdicionService tService = new TipoEdicionService();
		currentPublication = tService.obtenerTipoEdicion(Integer.parseInt(publication));
		
		videoIndex = currentPublication.getVideosIndexOffline();
		
    	config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
    }
	
    public TfsVideosYoutube(CmsObject cms, String siteNAME, String publicationID,String module) throws Exception{
    	
	    	m_cms = cms;
	    	siteName = siteNAME;
	    	publication = publicationID;
	    	
	    	TipoEdicionService tService = new TipoEdicionService();
	    	currentPublication = tService.obtenerTipoEdicion(Integer.parseInt(publication));
			
		videoIndex = currentPublication.getVideosIndexOffline();
			
	    	config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
	    	if (module != null)
	    		moduleConfig = module;
	   
    }
 
	public TfsVideosYoutube(PageContext context, HttpServletRequest req, HttpServletResponse res) throws Exception{
		
		m_session = req.getSession();
		m_controller = CmsFlexController.getController(req);
		m_cms = m_controller.getCmsObject();
		
		siteName = OpenCms.getSiteManager().getCurrentSite(m_cms).getSiteRoot();
		
		currentPublication = (TipoEdicion) m_session.getAttribute("currentPublication");

    	if (currentPublication==null) {
        	TipoEdicionService tService = new TipoEdicionService();

    		currentPublication = tService.obtenerEdicionOnlineRoot(siteName);
    		m_session.setAttribute("currentPublication",currentPublication);
    		
    		videoIndex = currentPublication.getVideosIndexOffline();
    	}
    	
    	publication = "" + currentPublication.getId();
    	
    	config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
	    	
	} 
	
	public Object getVideoData(){
		
		String youTubeDataAPIKey = config.getParam(siteName, publication, "videoUpload", "youTubeDataAPIKey");
		
		String urlYoutube = youtubeAPI+"?id="+youtubeID+"&key="+youTubeDataAPIKey+"&part=snippet,contentDetails&fields=items(id,snippet(title,description,thumbnails),contentDetails)";
		
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
	
	public String getDuration(String youtubeid){
		String duration = null;
		
		youtubeID = youtubeid;
		
		JSONObject  json = (JSONObject) this.getVideoData();
		
		if(json==null)
			return "";
		
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
		
			if(durationStr.indexOf("M")==-1)
				durationTmp = "00:"+durationTmp;
			
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
		
		youtubeID = youtubeid;
		
		ArrayList<HashMap<String, String>> thumbnails = new ArrayList<HashMap<String, String>>();
		
		JSONObject json = (JSONObject) this.getVideoData();
		
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
	
	public String uploadYoutubeImage(String youtubeId, String youtubeUrl, String type) throws Exception{
		
		String status = null;
		
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put("section","videos");
		
		String path = "/" + ImagenService.getInstance(m_cms,siteName,publication).getDefaultVFSUploadFolder(parameters);
		
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
	    	 
		     m_cms.createResource(fullPath, resourceType, buffer, null);
			 m_cms.unlockResource(fullPath);
			 
			 status = fullPath;
	    	 
	     }else if(type.equals("RFS")){
  
	    	 	String lcFileName = fileName.toLowerCase();
	    	 	String imageVFS = ImagenService.getInstance(m_cms,siteName, publication).uploadRFSFile(path,lcFileName,parameters,in);
	
	    	 	status = imageVFS;
	    	 
	     }else if(type.equals("FTP")){
	    	 
	    	   String lcFileName = fileName.toLowerCase();
	 		   String imageFTP = ImagenService.getInstance(m_cms,siteName, publication).uploadFTPFile(path,lcFileName,parameters,in);
	 		
	 		   status = imageFTP;
	     } else if(type.equals("AMZ")){
	    	 
	    	   String lcFileName = fileName.toLowerCase();
	 		   String imageAMZ = ImagenService.getInstance(m_cms,siteName, publication).uploadAmzFile(path,lcFileName,parameters,in);
	 		
	 		   status = imageAMZ;
	     }
	     
	      in.close();
	     
	     return status;
		
	}
	
	public String uploadImageToCms(String vfsPath, String youtubeUrl, String type){
		
		String status = null;
		String imgPath = null;
		
		if(type.equals("VFS")){
			try {
				imgPath = uploadYoutubeImageVFS(vfsPath, youtubeUrl);
			} catch (Exception e) {
				status = "An error occurred uploading the preview image to VFS: "+e.getMessage();
			}
		}
		
		if(type.equals("RFS")){
			try {
				imgPath = uploadYoutubeImageRFS(vfsPath,youtubeUrl);
			} catch (Exception e) {
				status = "An error occurred uploading the preview image to RFS: "+e.getMessage();
			}
		}
		
		if(type.equals("FTP")){
			  try {
				imgPath = uploadYoutubeImageFTP(vfsPath, youtubeUrl);
			} catch (Exception e) {
				status = "An error occurred uploading the preview image to FTP: "+e.getMessage();
			}
		}
		
		if(type.equals("AMZ")){
			  try {
				imgPath = uploadYoutubeImageAMZ(vfsPath, youtubeUrl);
			} catch (Exception e) {
				status = "An error occurred uploading the preview image to FTP: "+e.getMessage();
			}
		}
		
		if(imgPath!=null){
			try{
				
				if (!m_cms.getLock(vfsPath).isUnlocked()){
				     if(!m_cms.getLock(vfsPath).isOwnedBy(m_cms.getRequestContext().currentUser())){
					      m_cms.changeLock(vfsPath);
				    }
				}else{
				     m_cms.lockResource(vfsPath);
				}
				
				if (!m_cms.getLock(imgPath).isUnlocked()){
				     if(!m_cms.getLock(imgPath).isOwnedBy(m_cms.getRequestContext().currentUser())){
					      m_cms.changeLock(imgPath);
				    }
				}else{
				     m_cms.lockResource(imgPath);
				}
				
				CmsProperty prop = new CmsProperty();
                prop.setName("prevImage");
                prop.setValue(imgPath, CmsProperty.TYPE_INDIVIDUAL);
                m_cms.writePropertyObject(vfsPath,prop);
                
				m_cms.addRelationToResource( vfsPath, imgPath, "videoImage");
				
				m_cms.unlockResource(vfsPath);
				m_cms.unlockResource(imgPath);
				
				status = imgPath;
				
			}catch (CmsException e) {
				CmsLog.getLog(this).error("Upload preview image for video - Error: "+e.getMessage());
				status = "Upload preview image for video - Error: "+e.getMessage();
			}
		}
		
		return status;
	}
	
	private String uploadYoutubeImageAMZ(String vfsPath, String youtubeUrl) throws Exception {
		String status = null;
		
		CmsResource resource = m_cms.readResource(vfsPath);
		CmsUUID     idSource = resource.getResourceId();
		
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put("section","videos");
		
		String path = "/" + ImagenService.getInstance(m_cms,siteName, publication).getDefaultVFSUploadFolder(parameters);
		
		String fileName = idSource.getStringValue() +"_"+ youtubeUrl.substring(youtubeUrl.lastIndexOf("/")+1);
		
		URL url;
		
		url = new URL(youtubeUrl);
		InputStream in = new BufferedInputStream(url.openStream());
	    
	    String lcFileName = fileName.toLowerCase();
		String imageFTP = ImagenService.getInstance(m_cms,siteName, publication).uploadAmzFile(path,lcFileName,parameters,in);
		
		in.close();
		 
		status = imageFTP;
		 
		return status;	
	}

	private String uploadYoutubeImageVFS(String vfsPath,String youtubeUrl) throws Exception{
		
		String status = null;
		
		CmsResource resource = m_cms.readResource(vfsPath);
		CmsUUID     idSource = resource.getResourceId();
		
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put("section","videos");
		
		String path = "/" + ImagenService.getInstance(m_cms,siteName, publication).getDefaultVFSUploadFolder(parameters);
		
		String fileName = idSource.getStringValue() +"_"+ youtubeUrl.substring(youtubeUrl.lastIndexOf("/")+1);
		
		String fullPath = path + fileName;
		
		URL url;
		
							  url = new URL(youtubeUrl);
		InputStream            in = new BufferedInputStream(url.openStream());
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    byte[]                buf = new byte[1024];
	    int                     n = 0;
	        
	     while (-1 != (n = in.read(buf))) {
	            out.write(buf, 0, n);
	     }
	     
	     out.close();
	      in.close();
	      
	     byte[] buffer = out.toByteArray();
	     
	     int type = OpenCms.getResourceManager().getResourceType("image").getTypeId();
		
	     m_cms.createResource(fullPath, type, buffer, null);
		 m_cms.unlockResource(fullPath);
		 
		 status = fullPath;
		 
		return status;	
	}
	
	private String uploadYoutubeImageRFS(String vfsPath,String youtubeUrl) throws Exception{
		
		String status = null;
		
		CmsResource resource = m_cms.readResource(vfsPath);
		CmsUUID     idSource = resource.getResourceId();
		
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put("section","videos");
		
		String path = "/" + ImagenService.getInstance(m_cms,siteName, publication).getDefaultVFSUploadFolder(parameters);
		
		String fileName = idSource.getStringValue() +"_"+ youtubeUrl.substring(youtubeUrl.lastIndexOf("/")+1);
		
		URL url;
		
				   url = new URL(youtubeUrl);
		InputStream in = new BufferedInputStream(url.openStream());
	    
	    String lcFileName = fileName.toLowerCase();
		String imageVFS = ImagenService.getInstance(m_cms,siteName, publication).uploadRFSFile(path,lcFileName,parameters,in);
		
		in.close();
		 
		status = imageVFS;
		 
		return status;	
	}
	
	private String uploadYoutubeImageFTP(String vfsPath,String youtubeUrl) throws Exception{
		
		String status = null;
		
		CmsResource resource = m_cms.readResource(vfsPath);
		CmsUUID     idSource = resource.getResourceId();
		
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put("section","videos");
		
		String path = "/" + ImagenService.getInstance(m_cms,siteName, publication).getDefaultVFSUploadFolder(parameters);
		
		String fileName = idSource.getStringValue() +"_"+ youtubeUrl.substring(youtubeUrl.lastIndexOf("/")+1);
		
		URL url;
		
				   url = new URL(youtubeUrl);
		InputStream in = new BufferedInputStream(url.openStream());
	    
	    String lcFileName = fileName.toLowerCase();
		String imageFTP = ImagenService.getInstance(m_cms,siteName, publication).uploadFTPFile(path,lcFileName,parameters,in);
		
		in.close();
		 
		status = imageFTP;
		 
		return status;	
	}
	
	public String createYoutubeVideo(String path, String youtubeId, String prevImg, String title, String description, String rated, String author, String agency, String category, String tags, String section) throws Exception{
		return createYoutubeVideo(path, youtubeId, prevImg,title, description, rated, author, agency, category, tags, section, null, null, null);
	}
	
	public String createYoutubeVideo(String path, String youtubeId, String prevImg, String title, String description, String rated, String author, String agency, String category, String tags, String section, String autoplay) throws Exception{
		return createYoutubeVideo(path, youtubeId, prevImg,title, description, rated, author, agency, category, tags, section, autoplay, null, null);
	}
	
	public String createYoutubeVideo(String path, String youtubeId, String prevImg, String title, String description, String rated, String author, String agency, String category, String tags, String section, String autoplay, String mute, String type) throws Exception{
		
		//String defaultVideoYouTubePath = config.getParam(siteName, publication, "videoUpload", "defaultVideoYouTubePath","");
		
		Map<String, String> parameters = new HashMap<String, String>(); 
		parameters.put("section", section);
		 
		VideosService videosService = null;
		if (moduleConfig == null)
			videosService = VideosService.getInstance(m_cms, siteName, publication);
		else 
			videosService = VideosService.getInstance(m_cms, moduleConfig,siteName, publication);
		
		//VideoType youtube == 3
		path = videosService.getDefaultUploadFolder(parameters, 3);
		checkFoldersExistVFS(path);
		/*if(section!=null && section != ""){
			String folder = defaultVideoYouTubePath;
			
			if(folder.lastIndexOf("/")!= folder.length()-1)
				folder = folder+"/";
			
			path = folder + section + "/";
			
			checkFoldersExistVFS(path);
		}*/
		
		String emmbededVideoPath = videoExist(m_cms,videoIndex, youtubeId, TfsResourceTypeVideoYoutubeLink.getStaticTypeName());
		
		if (emmbededVideoPath.length()==0){
			 String idYoutube = youtubeId;
		 	   
		 	   if( youtubeId.indexOf("v=") >-1) {
				 String[] temp = youtubeId.split("v=");
				     idYoutube = temp[1];
			   }else if( youtubeId.indexOf("/") >-1) {
				 String[] temp = youtubeId.split("/");
				     idYoutube = temp[temp.length-1];
			   }
			
			String duration = getDuration(idYoutube);
			
			CmsProperty prop = new CmsProperty();
			
			List<CmsProperty> properties = new ArrayList<CmsProperty>(8);
			
			prop = new CmsProperty();
			prop.setName("video-duration");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(duration);
			properties.add(prop);
			
			prop = new CmsProperty();
			prop.setName(CmsPropertyDefinition.PROPERTY_TITLE);
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(title);
			properties.add(prop);
			
			prop = new CmsProperty();
			prop.setName("Agency");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(agency);
			properties.add(prop);
			 
			prop = new CmsProperty();
			prop.setName("Keywords");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(tags);
			properties.add(prop);
			
			prop = new CmsProperty();
			prop.setName(CmsPropertyDefinition.PROPERTY_DESCRIPTION);
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(description);
			properties.add(prop);
			
			prop = new CmsProperty();
			prop.setName("video-rated");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(rated);
			properties.add(prop);
			
			prop = new CmsProperty();
			prop.setName("Author");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(author);
			properties.add(prop);
			
			prop = new CmsProperty();
			prop.setName("category");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(category);
			properties.add(prop);
			
			if(autoplay!=null)
			{
				prop = new CmsProperty();
				prop.setName("video-autoplay");
				prop.setAutoCreatePropertyDefinition(true);
				prop.setStructureValue(autoplay);
				properties.add(prop);
			}	
			
			if(mute!=null)
			{
				prop = new CmsProperty();
				prop.setName("video-mute");
				prop.setAutoCreatePropertyDefinition(true);
				prop.setStructureValue(mute);
				properties.add(prop);
			}	
			
			
			String vfsPath = videosService.createYouTubeVideo(path,idYoutube, properties, type);
			
			if(prevImg ==null || prevImg.equals("")){
				ArrayList imgList = new ArrayList();
				          imgList = getThumbnails(idYoutube);
				          
				if( imgList !=null && imgList.size()>0){
					HashMap<String, String> thumbnail = (HashMap<String, String>)imgList.get(imgList.size()-1);
					prevImg = thumbnail.get("url");
				}
			}
			
			if(prevImg !=null && !prevImg.equals("")){
				
				String imageVFS = "";
				
				if(ImagenService.getInstance(m_cms,siteName, publication).getDefaultUploadDestination().equals("vfs"))
				{
					imageVFS = uploadYoutubeImage(idYoutube, prevImg, "VFS");
				}
				
				if(ImagenService.getInstance(m_cms,siteName, publication).getDefaultUploadDestination().equals("server"))
				{
					imageVFS = uploadYoutubeImage(idYoutube, prevImg, "RFS");
				}
				
				if(ImagenService.getInstance(m_cms,siteName, publication).getDefaultUploadDestination().equals("ftp"))
				{
					imageVFS = uploadYoutubeImage(idYoutube, prevImg, "FTP");
				}
				if(ImagenService.getInstance(m_cms,siteName, publication).getDefaultUploadDestination().equals("amz"))
				{
					imageVFS = uploadYoutubeImage(idYoutube, prevImg, "AMZ");
				}
				
				if(imageVFS!=null && imageVFS!=""){
					if (!m_cms.getLock(vfsPath).isUnlocked()){
					     if(!m_cms.getLock(vfsPath).isOwnedBy(m_cms.getRequestContext().currentUser())){
						      m_cms.changeLock(vfsPath);
					    }
					}else{
					     m_cms.lockResource(vfsPath);
					}
				
					if (!m_cms.getLock(imageVFS).isUnlocked()){
					     if(!m_cms.getLock(imageVFS).isOwnedBy(m_cms.getRequestContext().currentUser())){
						      m_cms.changeLock(imageVFS);
					    }
					}else{
					     m_cms.lockResource(imageVFS);
					}
				
		            prop.setName("prevImage");
		            prop.setValue(imageVFS, CmsProperty.TYPE_INDIVIDUAL);
		            m_cms.writePropertyObject(vfsPath,prop);
		       
				    m_cms.addRelationToResource( vfsPath, imageVFS, "videoImage");
				
				    m_cms.unlockResource(vfsPath);
				    m_cms.unlockResource(imageVFS);
				}
			}
			
			return vfsPath;
		}else{
			return "Error, the video already exists";
		}
	}
	
	public void checkFoldersExistVFS(String foldersVFS){
		
		String[] parts = foldersVFS.split("/");
		String subfolderName = "/";
		String firstFolder = null;
		
		for (String part : parts)
		{
			if(!part.equals("")){
		     	subfolderName = subfolderName.trim() + part + "/";
		     	
		     	if (!m_cms.existsResource(subfolderName)) {
					try {
						m_cms.createResource(subfolderName, CmsResourceTypeFolder.getStaticTypeId());
						
						if(firstFolder==null) firstFolder = subfolderName;
						
					} catch (CmsIllegalArgumentException e) {
						CmsLog.getLog(this).error("Video converter - Error al crear carpeta en el VFS"+e.getMessage());
					} catch (CmsException e) {
						CmsLog.getLog(this).error("Video converter - Error al crear carpeta en el VFS"+e.getMessage());
					} catch (Exception e) {
						CmsLog.getLog(this).error("Video converter - Error al crear carpeta en el VFS"+e.getMessage());
					}
					
		     	}
		     	
			}
		}
		
		if(firstFolder!=null)
			try {
				OpenCms.getPublishManager().publishResource(m_cms, firstFolder);
			} catch (Exception e) {
				CmsLog.getLog(this).error("Video converter - Error al publicar carpeta en el VFS"+e.getMessage());
			}
	}
	
	private String videoExist(CmsObject cmsObject, String videoIndex, String videoCode, String type)
	{
		
		TfsAdvancedSearch adSearch = new TfsAdvancedSearch();
		adSearch.init(cmsObject);
		adSearch.setQuery("+Uid:\"" + videoCode + "\" +Type:\"video-youtube\"");
		adSearch.setMaxResults(5);
		adSearch.setIndex(videoIndex);
		adSearch.setLanguageAnalyzer(new WhitespaceAnalyzer());
		
		List<CmsSearchResult> resultados = adSearch.getSearchResult();
		
		if (resultados!=null && resultados.size()>0) {
			return cmsObject.getRequestContext().removeSiteRoot(resultados.get(0).getPath());
		}
		return "";		
	}

	
}
