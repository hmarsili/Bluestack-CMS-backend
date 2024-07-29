package com.tfsla.diario.admin.video;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.VideosService;
import com.tfsla.diario.file.types.TfsResourceTypeVideoEmbedded;
import com.tfsla.diario.multiselect.VideoCodeLoader;

public class TfsVideosEmbedded {

	private CmsObject m_cms;
	private CPMConfig config;
	private String siteName;
    private String publication;
    private TipoEdicion currentPublication;
    private String moduleConfig = null;
    
    private String videoIndex = "VIDEOS_OFFLINE";
	
	public TfsVideosEmbedded(CmsObject cms) throws Exception{
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
	
	
	public TfsVideosEmbedded(CmsObject cms, String module) throws Exception{
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
    	
    	if (module!= null)
    		moduleConfig = module;
	}
	
	public TfsVideosEmbedded(CmsObject cms, String siteNAME, String publicationID) throws Exception{
    	
    	m_cms = cms;
    	siteName = siteNAME;
    	publication = publicationID;
    	
    	TipoEdicionService tService = new TipoEdicionService();
		currentPublication = tService.obtenerTipoEdicion(Integer.parseInt(publication));
		
		videoIndex = currentPublication.getVideosIndexOffline();
		
    	config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
    }
	
	//Se agrega para contemplar subida segun especificacion de modulo
	public TfsVideosEmbedded(CmsObject cms, String siteNAME, String publicationID, String module) throws Exception{
	 	m_cms = cms;
	 	siteName = siteNAME;
    		publication = publicationID;
    	
    		TipoEdicionService tService = new TipoEdicionService();
		currentPublication = tService.obtenerTipoEdicion(Integer.parseInt(publication));
		
		videoIndex = currentPublication.getVideosIndexOffline();
		
    		config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
    		if (module!= null)
    			moduleConfig = module;
	}	   
	
	
	public String createEmbeddedVideo(String path, String code, String title, String description, String rated, String author, String agency, String category, String tags, String section) throws Exception {
		return createEmbeddedVideo( path,  code,  title,  description,  rated,  author,  agency,  category,  tags,  section, null);
	}
		
	
	public String createEmbeddedVideo(String path, String code, String title, String description, String rated, String author, String agency, String category, String tags, String section, String type) throws Exception{
				
		
		Map<String, String> parameters = new HashMap<String, String>(); 
		parameters.put("section", section);
		 
		VideosService videosService = null;
		if (moduleConfig == null)
			videosService = VideosService.getInstance(m_cms,siteName,publication);
		else 
			videosService = VideosService.getInstance(m_cms, moduleConfig,siteName,publication);
		
	
		
		//VideoType embedded == 4
		path = videosService.getDefaultUploadFolder(parameters, 4);
		checkFoldersExistVFS(path);
	
		/*String defaultVideoEmbeddedPath = config.getParam(siteName, publication, "videoUpload", "defaultVideoEmbeddedPath","");
		
		if(section!=null && section != ""){
			String folder = defaultVideoEmbeddedPath;
			
			if(folder.lastIndexOf("/")!= folder.length()-1)
				folder = folder+"/";
			
			path = folder + section + "/";
			
			checkFoldersExistVFS(path);
		}*/
		
		String emmbededVideoPath = VideoCodeLoader.videoExist(m_cms,videoIndex, code, TfsResourceTypeVideoEmbedded.getStaticTypeName());
		
		if (emmbededVideoPath.length()==0){
			CmsProperty prop = new CmsProperty();
			
			List<CmsProperty> properties = new ArrayList<CmsProperty>(8);
			
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
						
			String vfsPath = videosService.createEmbbedeVideo(path,code, properties, type);
			
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
	

}
