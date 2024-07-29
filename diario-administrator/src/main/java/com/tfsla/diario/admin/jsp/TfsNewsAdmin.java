package com.tfsla.diario.admin.jsp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsResourceState;
import org.opencms.db.CmsSecurityManager;
import org.opencms.flex.CmsFlexController;

import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opencms.file.*;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLock;
import org.opencms.main.*;
import org.opencms.relations.*;
import org.opencms.report.CmsLogReport;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.*;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.admin.widgets.TfsCalendarWidget;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.NoticiasService;
import com.tfsla.diario.ediciones.services.OpenCmsBaseService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;
import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.utils.ScriptsJSFilter;

public class TfsNewsAdmin {

    protected static final Log LOG = CmsLog.getLog(TfsNewsAdmin.class);

    private CmsFlexController m_controller;
    private HttpSession m_session;
    private HttpServletRequest request;
    
    /**  The currently used message bundle. */
    private CmsMultiMessages m_messages;

    /** The current OpenCms users workplace settings. */
    private CmsWorkplaceSettings m_settings;

    
    private List<CmsResource> resources = new ArrayList<CmsResource>();
    
    private String siteName;
    private TipoEdicion currentPublication;
    private String publication;
    private String moduleConfigName;
    private CPMConfig config;

    
    private Boolean publish;
    private Boolean publishRelatedContent;
    private Boolean publishRelatedNews;
    private Boolean forcePublish;

    public String getSiteName() {
    	return this.siteName;
    }
    
    public String getPublication() {
    	return this.publication;
    }
    
    public CmsObject getCmsObject() {
        return m_controller.getCmsObject();
    }

	public TfsNewsAdmin(PageContext context, HttpServletRequest req, HttpServletResponse res) throws Exception
    {
		m_controller = CmsFlexController.getController(req);
        request = req;
        m_session = req.getSession();
        
    	siteName = OpenCms.getSiteManager().getCurrentSite(getCmsObject()).getSiteRoot();
    	

    	currentPublication = (TipoEdicion) m_session.getAttribute("currentPublication");

    	if (currentPublication==null) {
        	TipoEdicionService tService = new TipoEdicionService();

    		currentPublication = tService.obtenerEdicionOnlineRoot(siteName);
    		m_session.setAttribute("currentPublication",currentPublication);
    	}
    	
        m_settings = (CmsWorkplaceSettings)m_session.getAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);

        if (m_settings==null)  {
        	
        	if (LOG.isDebugEnabled()) {
        		LOG.debug("Editing the content " + req.getParameter("url")  + " in resource list. Session settings not found. Starting context-based configuration.");
        	}        	
        	m_settings = CmsWorkplace.initWorkplaceSettings( m_controller.getCmsObject(), m_settings, true);
        	m_session.setAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS, m_settings);
        	
        	if (LOG.isDebugEnabled()) {
        		LOG.debug("Current proyect " + m_settings.getProject() + " - Current site" + m_settings.getSite());
        	}
        }

        // initialize messages            
        CmsMessages messages = OpenCms.getWorkplaceManager().getMessages(getLocale());
        // generate a new multi messages object and add the messages from the workplace
        m_messages = new CmsMultiMessages(getLocale());
        m_messages.addMessages(messages);

    	publication = "" + currentPublication.getId();
    	moduleConfigName = "adminNewsConfiguration";
 		config = CmsMedios.getInstance().getCmsParaMediosConfiguration();

        
    	publish = Boolean.parseBoolean(request.getParameter("publish"));
    	
    	publishRelatedContent = Boolean.parseBoolean(request.getParameter("publishRelatedContent"));
    	publishRelatedNews = Boolean.parseBoolean(request.getParameter("publishRelatedNews"));
    	forcePublish = Boolean.parseBoolean(request.getParameter("forcePublish"));
    }

	public List<CmsResource> getResources() throws CmsException {
		List<CmsResource> allresources = new ArrayList<CmsResource>();
		CmsObject cms = getCmsObject();
		
		int resourcesCount = request.getParameterValues("path[]").length;
	
		for (int idx=0;idx<resourcesCount;idx++) {
			String path = String.valueOf(request.getParameterValues("path[]")[idx]);
			allresources.add(cms.readFile(path,CmsResourceFilter.ALL));
		}

		return allresources;
	}

	public List<CmsResource> getRelatedResources() throws CmsException {
		List<CmsResource> allresources = new ArrayList<CmsResource>();
		CmsObject cms = getCmsObject();
		
		int resourcesCount = request.getParameterValues("path[]").length;
	
		for (int idx=0;idx<resourcesCount;idx++) {
			String path = String.valueOf(request.getParameterValues("path[]")[idx]);
			allresources.add(cms.readFile(path,CmsResourceFilter.ALL));
		}
		List<CmsResource> relatedResources = new ArrayList<CmsResource>();
		for (CmsResource resource : allresources ) {
			List<CmsResource> currentRelResources = addRelatedResourcesToPublish(resource,false,true, new ArrayList<CmsResource>());
			currentRelResources.removeAll(relatedResources);
			currentRelResources.removeAll(allresources);
			relatedResources.addAll(currentRelResources);
		}
		
		return relatedResources;
	}
	
	public void changeState() throws CmsException
	{
		CmsObject cms = getCmsObject();
		
		String state = request.getParameter("state");
		
		int resourcesCount = request.getParameterValues("path[]").length;
		
		for (int idx=0;idx<resourcesCount;idx++) {
			String path = String.valueOf(request.getParameterValues("path[]")[idx]);
			
			if (cms.getLock(path).isOwnedBy(cms.getRequestContext().currentUser()) )
				cms.unlockResource(path);
	
				CmsLock lock = cms.getLock(path);
		
				if (!lock.isUnlocked()) {
					if (!lock.getUserId().equals(cms.getRequestContext().currentUser().getId())) {
						if (forcePublish) {
							cms.changeLock(path);
						}
						else {
							continue;
						}
					}
				}
				else
					cms.lockResource(path);

			CmsFile file = cms.readFile(path,CmsResourceFilter.ALL);
			
			CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
		
			content.setAutoCorrectionEnabled(true);
			content.correctXmlStructure(cms);
		
		
			content.getValue("estado", Locale.ENGLISH).setStringValue(cms, state);

		   	file.setContents(content.marshal());
		   	cms.writeFile(file);		
		
			cms.unlockResource(path);

	   		resources.add(getCmsObject().readResource(path));
			
		}
		
	}
	
	public void retrieveNews() throws CmsException {
		
		int resourcesCount = request.getParameterValues("path[]").length;
		
		for (int idx=0;idx<resourcesCount;idx++) {
			String path = String.valueOf(request.getParameterValues("path[]")[idx]);
	   		resources.add(getCmsObject().readResource(path, CmsResourceFilter.ALL));
		}
	}
	
	public JSONObject copyResource(String publicationName, String urlResource){
		return copyResource(publicationName, urlResource, false);
	}
	
	public JSONObject copyResource(String publicationName, String urlResource, boolean returnUrl){
		JSONObject result = new JSONObject();
		JSONArray errorsJS = new JSONArray();
		CmsObject cms = getCmsObject();
		
		String newResource = "";
		String publicationTarget = publicationName;
		String resourceToCopy = urlResource;		
		
		String proyecto = OpenCmsBaseService.getCurrentSite(cms);
		TipoEdicionService tipoEdicionService = new TipoEdicionService();
		TipoEdicion tipoEdicionTarget = tipoEdicionService.obtenerTipoEdicion(publicationTarget,proyecto);	
		NoticiasService	nService = new NoticiasService();
		
		String resourceType = "news";
		
		CmsResource res;
		try {
			res = cms.readResource(urlResource, CmsResourceFilter.ALL);
			CmsProperty prop = cms.readPropertyObject(res, "newsType",false);
	        String targetResourceType = prop.getValue("");
	        
	        // Nos fijamos si en la publicacion de destino existe el tipo
	        List<String> tiposNoticia = nService.obtenerTiposDeNoticia(cms, tipoEdicionTarget.getId());
	        
			if (tiposNoticia!=null && tiposNoticia.size() > 0 && !tiposNoticia.get(0).equals("")) {
				for (String tipoNoticia: tiposNoticia) {
					if(tipoNoticia.equals(targetResourceType))
						resourceType = targetResourceType;
				}
			}
	        
		} catch (CmsException e2) {
			
			e2.printStackTrace();
		}       
		
		//
		//Creating new resource
		//
		try {
			newResource = nService.crearNoticia(getCmsObject(), tipoEdicionTarget.getId(), resourceType, new HashMap<String,String>());
		} catch (CmsLoaderException e1) {
			result.put("status", "error");
			
			JSONObject error = new JSONObject(); 
        	error.put("path", resourceToCopy);
        	error.put("message",e1.getMessage());		
			errorsJS.add(error);
		} catch (Exception e){
			result.put("status", "error");
			
			JSONObject error = new JSONObject(); 
        	error.put("path", resourceToCopy);
        	error.put("message",e.getMessage());		
			errorsJS.add(error);
		}
		
		//
		//Reading and Copying News Information
		//
		
		try {
			//Lock copy resource
			CmsResourceUtils.forceLockResource(cms,resourceToCopy);
			CmsFile file = cms.readFile(resourceToCopy,CmsResourceFilter.ALL);		
			CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
			content.setAutoCorrectionEnabled(true);
			content.correctXmlStructure(cms);
			
			//Lock new resource
			CmsResourceUtils.forceLockResource(cms,newResource);
			CmsFile newFile = cms.readFile(newResource,CmsResourceFilter.ALL);			
			CmsXmlContent newContent = CmsXmlContentFactory.unmarshal(cms, newFile);
			newContent.setAutoCorrectionEnabled(true);
			newContent.correctXmlStructure(cms);
			
			copyItemsList(content, newContent,cms);
			
			newContent.getValue("titulo", Locale.ENGLISH).setStringValue(cms, content.getValue("titulo", Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("volanta", Locale.ENGLISH).setStringValue(cms, content.getValue("volanta", Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("copete", Locale.ENGLISH).setStringValue(cms, content.getValue("copete", Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("cuerpo", Locale.ENGLISH).setStringValue(cms, content.getValue("cuerpo", Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("claves", Locale.ENGLISH).setStringValue(cms, content.getValue("claves", Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("clavesOcultas", Locale.ENGLISH).setStringValue(cms, content.getValue("clavesOcultas", Locale.ENGLISH).getStringValue(cms));
						
			newContent.getValue("imagenPrevisualizacion/imagen", Locale.ENGLISH).setStringValue(cms,
								content.getValue("imagenPrevisualizacion/imagen",Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("imagenPrevisualizacion/descripcion", Locale.ENGLISH).setStringValue(cms,
								content.getValue("imagenPrevisualizacion/descripcion",Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("imagenPrevisualizacion/fuente", Locale.ENGLISH).setStringValue(cms,
								content.getValue("imagenPrevisualizacion/fuente",Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("imagenPrevisualizacion/keywords", Locale.ENGLISH).setStringValue(cms,
								content.getValue("imagenPrevisualizacion/keywords",Locale.ENGLISH).getStringValue(cms));
			
			//
			//Galery Images
			//			
			int galleryImagesCount = nService.cantidadDeImagenesEnFotogaleria(cms, cms.readResource(resourceToCopy));
			
			for (int j=1;j<=galleryImagesCount;j++)
			{
				//Imagen Fotogaleria
				String galleryImage = getElementValue("imagenesFotogaleria[" + j + "]/imagen[1]", 
										content, Locale.ENGLISH);
				if(!galleryImage.isEmpty()){
					String xmlName ="imagenesFotogaleria[" + j + "]/imagen[1]";
					I_CmsXmlContentValue value = newContent.getValue(xmlName, Locale.ENGLISH);
					
					if(value==null){
						newContent.addValue(cms, "imagenesFotogaleria", Locale.ENGLISH,j-1);
					}
					
					newContent.getValue("imagenesFotogaleria[" + j + "]/imagen[1]",
										Locale.ENGLISH).setStringValue(cms, galleryImage);
				}	
				
				//Description de Fotogaleria
				String galleryDescription = getElementValue("imagenesFotogaleria[" + j + "]/descripcion", 
										content, Locale.ENGLISH);
				if(!galleryDescription.isEmpty()){				
					newContent.getValue("imagenesFotogaleria[" + j + "]/descripcion",
										Locale.ENGLISH).setStringValue(cms, galleryDescription);
				}	
				
				//Source de Fotogaleria
				String gallerySource = getElementValue("imagenesFotogaleria[" + j + "]/fuente", 
										content, Locale.ENGLISH);
				if(!gallerySource.isEmpty()){				
					newContent.getValue("imagenesFotogaleria[" + j + "]/fuente",
										Locale.ENGLISH).setStringValue(cms, gallerySource);
				}	
				
				//Tags de Fotogaleria
				String galleryTags = getElementValue("imagenesFotogaleria[" + j + "]/keywords", 
										content, Locale.ENGLISH);
				if(!galleryTags.isEmpty()){				
					newContent.getValue("imagenesFotogaleria[" + j + "]/keywords",
										Locale.ENGLISH).setStringValue(cms, galleryTags);
				}	
			}
						
			//
			//Videos
			//
			int flashVideoCount = getElementCountWithValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash"),
													   		TfsXmlContentNameProvider.getInstance().getTagName("news.video.video"),
													   		content);			
			for (int j=1;j<=flashVideoCount;j++)
			{
				//Video de videoFlash
				String videoFlashVideo = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+j+"]/video[1]", 
										content, Locale.ENGLISH);
				if(!videoFlashVideo.isEmpty()){
					String xmlName = TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+j+"]/video[1]";
					I_CmsXmlContentValue value = newContent.getValue(xmlName, Locale.ENGLISH);					
					if(value==null){
						newContent.addValue(cms, TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash"), Locale.ENGLISH,j-1);
					}					
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+j+"]/video[1]",
										Locale.ENGLISH).setStringValue(cms, videoFlashVideo);
				}	
				
				//Imagen de videoFlash
				String videoFlashImage = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+j+"]/imagen[1]", 
										content, Locale.ENGLISH);
				if(!videoFlashImage.isEmpty()){
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+j+"]/imagen[1]",
										Locale.ENGLISH).setStringValue(cms, videoFlashImage);
				}	
				
				//Titulo de videoFlash
				String videoFlashTitle = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+j+"]/titulo", 
										content, Locale.ENGLISH);
				if(!videoFlashTitle.isEmpty()){
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+j+"]/titulo",
										Locale.ENGLISH).setStringValue(cms, videoFlashTitle);
				}	
				
				//Imagen de videoFlash
				String videoFlashSource = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+j+"]/fuente", 
										content, Locale.ENGLISH);
				if(!videoFlashSource.isEmpty()){
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+j+"]/fuente",
										Locale.ENGLISH).setStringValue(cms, videoFlashSource);
				}	
				
				//Imagen de videoFlash
				String videoFlashDescription = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+j+"]/descripcion", 
										content, Locale.ENGLISH);
				if(!videoFlashDescription.isEmpty()){
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+j+"]/descripcion",
										Locale.ENGLISH).setStringValue(cms, videoFlashDescription);
				}	
				
				//Imagen de videoFlash
				String videoFlashTags = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+j+"]/keywords", 
										content, Locale.ENGLISH);
				if(!videoFlashTags.isEmpty()){
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+j+"]/keywords",
										Locale.ENGLISH).setStringValue(cms, videoFlashTags);
				}	
			}			
						
			int youtubeImagesCount = getElementCountWithValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube"),
															TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube.id"),
					   										content);
			for (int j=1;j<=youtubeImagesCount;j++)
			{
				//Youtube id
				String videoYoutubeid = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+j+"]/youtubeid", 
										content, Locale.ENGLISH);
				if(!videoYoutubeid.isEmpty()){
					String xmlName = TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+j+"]/youtubeid";
					I_CmsXmlContentValue value = newContent.getValue(xmlName, Locale.ENGLISH);					
					if(value==null){
						newContent.addValue(cms, TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube"), Locale.ENGLISH,j-1);
					}	
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+j+"]/youtubeid",
										Locale.ENGLISH).setStringValue(cms, videoYoutubeid);
				}	
				
				//Youtube image
				String videoYoutubeImage = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+j+"]/imagen", 
										content, Locale.ENGLISH);
				if(!videoYoutubeImage.isEmpty()){
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+j+"]/imagen",
										Locale.ENGLISH).setStringValue(cms, videoYoutubeImage);
				}	
				
				//Youtube Title
				String videoYoutubeTitle = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+j+"]/titulo", 
										content, Locale.ENGLISH);
				if(!videoYoutubeTitle.isEmpty()){
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+j+"]/titulo",
										Locale.ENGLISH).setStringValue(cms, videoYoutubeTitle);
				}	
				
				//Youtube Description
				String videoYoutubeDescription = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+j+"]/descripcion", 
										content, Locale.ENGLISH);
				if(!videoYoutubeDescription.isEmpty()){
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+j+"]/descripcion",
										Locale.ENGLISH).setStringValue(cms, videoYoutubeDescription);
				}	
				
				//Youtube Source
				String videoYoutubeSource = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+j+"]/fuente", 
										content, Locale.ENGLISH);
				if(!videoYoutubeSource.isEmpty()){
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+j+"]/fuente",
										Locale.ENGLISH).setStringValue(cms, videoYoutubeSource);
				}	
				
				//Youtube Tags
				String videoYoutubeTags = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+j+"]/keywords", 
										content, Locale.ENGLISH);
				if(!videoYoutubeTags.isEmpty()){
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+j+"]/keywords",
										Locale.ENGLISH).setStringValue(cms, videoYoutubeTags);
				}	
			}
			
			int embeddedImagesCount = getElementCountWithValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded"),
															TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode"),
					   										content);			
			for (int j=1;j<=embeddedImagesCount;j++)
			{
				//Embedded codigo
				String videoEmbeddedCode = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+j+"]/codigo", 
										content, Locale.ENGLISH);
				if(!videoEmbeddedCode.isEmpty()){
					String xmlName = TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+j+"]/codigo";
					I_CmsXmlContentValue value = newContent.getValue(xmlName, Locale.ENGLISH);					
					if(value==null){
						newContent.addValue(cms, TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded"), Locale.ENGLISH,j-1);
					}	
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+j+"]/codigo",
										Locale.ENGLISH).setStringValue(cms, videoEmbeddedCode);
				}	
				
				//Embedded Image
				String videoEmbeddedImage = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+j+"]/imagen", 
										content, Locale.ENGLISH);
				if(!videoEmbeddedImage.isEmpty()){					
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+j+"]/imagen",
										Locale.ENGLISH).setStringValue(cms, videoEmbeddedImage);
				}	
				
				//Embedded Title
				String videoEmbeddedTitle = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+j+"]/titulo", 
										content, Locale.ENGLISH);
				if(!videoEmbeddedTitle.isEmpty()){					
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+j+"]/titulo",
										Locale.ENGLISH).setStringValue(cms, videoEmbeddedTitle);
				}
				
				//Embedded Description
				String videoEmbeddedDescription = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+j+"]/descripcion", 
										content, Locale.ENGLISH);
				if(!videoEmbeddedDescription.isEmpty()){					
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+j+"]/descripcion",
										Locale.ENGLISH).setStringValue(cms, videoEmbeddedDescription);
				}
				
				//Embedded Source
				String videoEmbeddedSource = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+j+"]/fuente", 
										content, Locale.ENGLISH);
				if(!videoEmbeddedSource.isEmpty()){					
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+j+"]/fuente",
										Locale.ENGLISH).setStringValue(cms, videoEmbeddedSource);
				}
				
				//Embedded Tags
				String videoEmbeddedTags = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+j+"]/keywords", 
										content, Locale.ENGLISH);
				if(!videoEmbeddedTags.isEmpty()){					
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+j+"]/keywords",
										Locale.ENGLISH).setStringValue(cms, videoEmbeddedTags);
				}
			}
			
			newFile.setContents(newContent.marshal());
			cms.writeFile(newFile);	
			CmsResourceUtils.unlockResource(cms,newResource,false);
			CmsResourceUtils.unlockResource(cms,resourceToCopy,false);
			
			if(returnUrl){
				result.put("url", newResource);
			}else{
				result.put("status", "ok");
			}
			
		} catch (CmsException ex) {
			result.put("status", "error");
			
			JSONObject error = new JSONObject(); 
        	error.put("path", resourceToCopy);
        	error.put("message",ex.getMessage());		
			errorsJS.add(error);
		}
		
		result.put("errors", errorsJS );
		return result;
	}
	
	protected void copyItemsList(CmsXmlContent content,CmsXmlContent newContent,CmsObject cms){
		
	newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemListIntegrated"), Locale.ENGLISH).setStringValue(cms, content.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemListIntegrated"), Locale.ENGLISH).getStringValue(cms));
		
		int noticiaListaCount = getElementCountWithValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList"),
				 TfsXmlContentNameProvider.getInstance().getTagName("news.itemList.title"),
					 content);

		for (int j=1;j<=noticiaListaCount;j++)
		{
			String xmlName ="noticiaLista[" + j + "]/titulo";
			I_CmsXmlContentValue value = newContent.getValue(xmlName, Locale.ENGLISH);
					
			if(value==null)
				newContent.addValue(cms, "noticiaLista", Locale.ENGLISH,j-1);
			
			newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/titulo", Locale.ENGLISH).setStringValue(cms, content.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/titulo", Locale.ENGLISH).getStringValue(cms));
			newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/cuerpo", Locale.ENGLISH).setStringValue(cms, content.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/cuerpo", Locale.ENGLISH).getStringValue(cms));
		
			// Categories
			int categoriesCount = getElementCountWithValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.categories"),"",content);	

			for (int m=1;m<=categoriesCount;m++)
			{
				String CategoryValue = newContent.getStringValue(cms,TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/Categorias["+m+"]", Locale.ENGLISH);
				
				if(CategoryValue==null)
					newContent.addValue(cms, TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/Categorias", Locale.ENGLISH,m-1);
				
				newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/Categorias["+ m +"]", Locale.ENGLISH).setStringValue(cms, content.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/Categorias["+ m +"]", Locale.ENGLISH).getStringValue(cms));
			}	
			
			// Imagen
			String imageValue = content.getStringValue(cms,TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/imagenlista/imagen", Locale.ENGLISH);
			
			if(imageValue!=null && !imageValue.equals("")){
				String newImageValue = newContent.getStringValue(cms,TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/imagenlista/imagen", Locale.ENGLISH);
	
				if(newImageValue==null)
							newContent.addValue(cms, TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/imagenlista", Locale.ENGLISH,0);
				
				newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/imagenlista/imagen", Locale.ENGLISH).setStringValue(cms, content.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/imagenlista/imagen", Locale.ENGLISH).getStringValue(cms));
				
				String fotografoValue = content.getStringValue(cms,TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/imagenlista/fotografo", Locale.ENGLISH);
				
				if(fotografoValue!=null){
				
					if(newContent.getStringValue(cms,TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/imagenlista[1]/fotografo", Locale.ENGLISH)==null)
						newContent.addValue(cms, TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/imagenlista[1]/fotografo", Locale.ENGLISH,0);	
					
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/imagenlista/fotografo", Locale.ENGLISH).setStringValue(cms, content.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/imagenlista/fotografo", Locale.ENGLISH).getStringValue(cms));
					
				}
				
				newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/imagenlista/descripcion", Locale.ENGLISH).setStringValue(cms, content.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/imagenlista/descripcion", Locale.ENGLISH).getStringValue(cms));
				newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/imagenlista/fuente", Locale.ENGLISH).setStringValue(cms, content.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/imagenlista/fuente", Locale.ENGLISH).getStringValue(cms));
				newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/imagenlista/keywords", Locale.ENGLISH).setStringValue(cms, content.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/imagenlista/keywords", Locale.ENGLISH).getStringValue(cms));
				
			}
			
			//videoYouTube
			int youtubeVideosCount = getElementCountWithValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/videoYouTube", TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube.id"), content);
			
			for (int y=1;y<=youtubeVideosCount;y++)
			{
				String videoYoutubeid = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/videoYouTube"+"["+y+"]/youtubeid",content, Locale.ENGLISH);
				
				if(!videoYoutubeid.isEmpty()){
					String xmlNameYt = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+y+"]/youtubeid";
					I_CmsXmlContentValue valueYt = newContent.getValue(xmlNameYt, Locale.ENGLISH);					
					if(valueYt==null){
						newContent.addValue(cms, TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube"), Locale.ENGLISH,y-1);
					}	
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+y+"]/youtubeid", Locale.ENGLISH).setStringValue(cms, videoYoutubeid);
					
					//Youtube image
					String videoYoutubeImage = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+y+"]/imagen",content, Locale.ENGLISH);
					if(!videoYoutubeImage.isEmpty()){
						newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+y+"]/imagen",Locale.ENGLISH).setStringValue(cms, videoYoutubeImage);
					}	
					
					//Youtube Title
					String videoYoutubeTitle = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+y+"]/titulo",content, Locale.ENGLISH);
					if(!videoYoutubeTitle.isEmpty()){
						newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+y+"]/titulo",Locale.ENGLISH).setStringValue(cms, videoYoutubeTitle);
					}	
					
					//Youtube Description
					String videoYoutubeDescription = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+y+"]/descripcion", content, Locale.ENGLISH);
					
					if(!videoYoutubeDescription.isEmpty()){
						newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+y+"]/descripcion",Locale.ENGLISH).setStringValue(cms, videoYoutubeDescription);
					}	
					
					//Youtube Source
					String videoYoutubeSource = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+y+"]/fuente", content, Locale.ENGLISH);
					
					if(!videoYoutubeSource.isEmpty()){
						newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+y+"]/fuente",Locale.ENGLISH).setStringValue(cms, videoYoutubeSource);
					}
					
					String tagNameAutor = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+y+"]/autor";
					newContent.getValue(tagNameAutor,Locale.ENGLISH).setStringValue(cms,getElementValue(tagNameAutor, content, Locale.ENGLISH));
					
					String tagNameCalificacion = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+y+"]/calificacion";
					newContent.getValue(tagNameCalificacion,Locale.ENGLISH).setStringValue(cms,getElementValue(tagNameCalificacion, content, Locale.ENGLISH));
					
			        String tagNameMostrarEnHome = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+y+"]/mostrarEnHome";
					newContent.getValue(tagNameMostrarEnHome,Locale.ENGLISH).setStringValue(cms,getElementValue(tagNameMostrarEnHome, content, Locale.ENGLISH));
					
			        String tagNameKeywords = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+y+"]/keywords";
					newContent.getValue(tagNameKeywords,Locale.ENGLISH).setStringValue(cms,getElementValue(tagNameKeywords, content, Locale.ENGLISH));
					
					// Categories
					int categoriesCountYt = getElementCountWithValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube"),
					   		TfsXmlContentNameProvider.getInstance().getTagName("news.video.categories"),
					   		content);	

					for (int my=1;my<=categoriesCountYt;my++)
					{
						String CategoryValue = newContent.getStringValue(cms,TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"[" + y + "]/categoria", Locale.ENGLISH);
						
						if(CategoryValue==null)
							newContent.addValue(cms, TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"[" + y + "]/categoria["+ my +"]", Locale.ENGLISH,my-1);
						
						newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"[" + y + "]/categoria["+ my +"]", Locale.ENGLISH).setStringValue(cms, content.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"[" + y + "]/categoria["+ my +"]", Locale.ENGLISH).getStringValue(cms));
					}	
					
					
			        String tagNameAutoplay = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+y+"]/autoplay";
					newContent.getValue(tagNameAutoplay,Locale.ENGLISH).setStringValue(cms,getElementValue(tagNameAutoplay, content, Locale.ENGLISH));
					
			        String tagNameMute = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")+"["+y+"]/mute";
					newContent.getValue(tagNameMute,Locale.ENGLISH).setStringValue(cms,getElementValue(tagNameMute, content, Locale.ENGLISH));
				}	
				
			}
			
			//videoEmbedded
int embededVideosCount = getElementCountWithValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded"), TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode"), content);
			
			for (int e=1;e<=embededVideosCount;e++)
			{
				String videoCode = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+e+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode"),content, Locale.ENGLISH);
				
				if(!videoCode.isEmpty()){
					String xmlNameE = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+e+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode");
					I_CmsXmlContentValue valueE = newContent.getValue(xmlNameE, Locale.ENGLISH);					
					if(valueE==null){
						newContent.addValue(cms, TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded"), Locale.ENGLISH,e-1);
					}	
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+e+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode"), Locale.ENGLISH).setStringValue(cms, videoCode);
					
					//Embeded image
					String videoEmbededImage = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+e+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.thumbnail"),content, Locale.ENGLISH);
					if(!videoEmbededImage.isEmpty()){
						newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+e+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.thumbnail"),Locale.ENGLISH).setStringValue(cms, videoEmbededImage);
					}	
					
					//Embedded Title
					String videoEmbeddedTitle = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+e+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.title"),content, Locale.ENGLISH);
					if(!videoEmbeddedTitle.isEmpty()){
						newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+e+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.title"),Locale.ENGLISH).setStringValue(cms, videoEmbeddedTitle);
					}	
					
					//Embedded Description
					String videoEmbeddedDescription = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+e+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.description"), content, Locale.ENGLISH);
					
					if(!videoEmbeddedDescription.isEmpty()){
						newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+e+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.description"),Locale.ENGLISH).setStringValue(cms, videoEmbeddedDescription);
					}	
					
					//Embedded Source
					String videoEmbeddedSource = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+e+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.source"), content, Locale.ENGLISH);
					
					if(!videoEmbeddedSource.isEmpty()){
						newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+e+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.source"),Locale.ENGLISH).setStringValue(cms, videoEmbeddedSource);
					}
					
					String tagNameAutor = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+e+"]/autor";
					newContent.getValue(tagNameAutor,Locale.ENGLISH).setStringValue(cms,getElementValue(tagNameAutor, content, Locale.ENGLISH));
					
					String tagNameCalificacion = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+e+"]/calificacion";
					newContent.getValue(tagNameCalificacion,Locale.ENGLISH).setStringValue(cms,getElementValue(tagNameCalificacion, content, Locale.ENGLISH));
					
			        String tagNameKeywords = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"["+e+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.keywords");
					newContent.getValue(tagNameKeywords,Locale.ENGLISH).setStringValue(cms,getElementValue(tagNameKeywords, content, Locale.ENGLISH));
					
					// Categories
					int categoriesCountE = getElementCountWithValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded"),TfsXmlContentNameProvider.getInstance().getTagName("news.video.categories"),content);	

					for (int me=1;me<=categoriesCountE;me++)
					{
						String CategoryValue = newContent.getStringValue(cms,TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"[" + e + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.categories"), Locale.ENGLISH);
						
						if(CategoryValue==null)
							newContent.addValue(cms, TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"[" + e + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.categories")+"["+ me +"]", Locale.ENGLISH,me-1);
						
						newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"[" + e + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.categories")+"["+ me +"]", Locale.ENGLISH).setStringValue(cms, content.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")+"[" + e + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.categories")+"["+ me +"]", Locale.ENGLISH).getStringValue(cms));
					}	
					
				}	
			}
			
			//videoFlash
			int flashVideosCount = getElementCountWithValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash"), TfsXmlContentNameProvider.getInstance().getTagName("news.video.video"), content);
			
			for (int f=1;f<=flashVideosCount;f++)
			{
				String videoFlash = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+f+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.video"),content, Locale.ENGLISH);
				
				if(!videoFlash.isEmpty()){
					String xmlNameF = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+f+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.video");
					I_CmsXmlContentValue valueF = newContent.getValue(xmlNameF, Locale.ENGLISH);					
					if(valueF==null){
						newContent.addValue(cms, TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash"), Locale.ENGLISH,f-1);
					}	
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+f+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.video"), Locale.ENGLISH).setStringValue(cms, videoFlash);
					
					//Video nativo image
					String videoFlashImage = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+f+"]/imagen"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.thumbnail"),content, Locale.ENGLISH);
					if(!videoFlashImage.isEmpty()){
						newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+f+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.thumbnail"),Locale.ENGLISH).setStringValue(cms, videoFlashImage);
					}	
					
					//Flash Title
					String videoFlaseTitle = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+f+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.title"),content, Locale.ENGLISH);
					if(!videoFlaseTitle.isEmpty()){
						newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+f+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.title"),Locale.ENGLISH).setStringValue(cms, videoFlaseTitle);
					}	
					
					//Flash Description
					String videoFlashDescription = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+f+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.description"), content, Locale.ENGLISH);
					
					if(!videoFlashDescription.isEmpty()){
						newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+f+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.description"),Locale.ENGLISH).setStringValue(cms, videoFlashDescription);
					}	
					
					//flash Source
					String videoFlashSource = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+f+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.source"), content, Locale.ENGLISH);
					
					if(!videoFlashSource.isEmpty()){
						newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+f+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.source"),Locale.ENGLISH).setStringValue(cms, videoFlashSource);
					}
					
					String tagNameAutor = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+f+"]/autor";
					newContent.getValue(tagNameAutor,Locale.ENGLISH).setStringValue(cms,getElementValue(tagNameAutor, content, Locale.ENGLISH));
					
					String tagNameCalificacion = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+f+"]/calificacion";
					newContent.getValue(tagNameCalificacion,Locale.ENGLISH).setStringValue(cms,getElementValue(tagNameCalificacion, content, Locale.ENGLISH));
					
					String tagNameHideComments = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+f+"]/ocultarComentarios";
					newContent.getValue(tagNameHideComments,Locale.ENGLISH).setStringValue(cms,getElementValue(tagNameHideComments, content, Locale.ENGLISH));
					
			        String tagNameMostrarEnHome = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+f+"]/mostrarEnHome";
					newContent.getValue(tagNameMostrarEnHome,Locale.ENGLISH).setStringValue(cms,getElementValue(tagNameMostrarEnHome, content, Locale.ENGLISH));
					
			        String tagNameKeywords = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+f+"]/keywords";
					newContent.getValue(tagNameKeywords,Locale.ENGLISH).setStringValue(cms,getElementValue(tagNameKeywords, content, Locale.ENGLISH));
					
					// Categories
					int categoriesCountF = getElementCountWithValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash"),
					   		TfsXmlContentNameProvider.getInstance().getTagName("news.video.categories"),
					   		content);	

					for (int mf=1;mf<=categoriesCountF;mf++)
					{
						String CategoryValue = newContent.getStringValue(cms,TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"[" + f + "]/categoria", Locale.ENGLISH);
						
						if(CategoryValue==null)
							newContent.addValue(cms, TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"[" + f + "]/categoria["+ mf +"]", Locale.ENGLISH,mf-1);
						
						newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"[" + f + "]/categoria["+ mf +"]", Locale.ENGLISH).setStringValue(cms, content.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"[" + f + "]/categoria["+ mf +"]", Locale.ENGLISH).getStringValue(cms));
					}	
					
					
			        String tagNameAutoplay = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+f+"]/autoplay";
					newContent.getValue(tagNameAutoplay,Locale.ENGLISH).setStringValue(cms,getElementValue(tagNameAutoplay, content, Locale.ENGLISH));
					
			        String tagNameMute = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")+"["+f+"]/mute";
					newContent.getValue(tagNameMute,Locale.ENGLISH).setStringValue(cms,getElementValue(tagNameMute, content, Locale.ENGLISH));
				}	
				
			}
			
			//noticia
			 String tagNameNoticia = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.itemList.relatedNews");
			 
			 if(content.getStringValue(cms,tagNameNoticia, Locale.ENGLISH)!=null){
				 
				 String noticiaValue = newContent.getStringValue(cms,TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.itemList.relatedNews")+"[1]", Locale.ENGLISH);
				
				 if(noticiaValue==null)
					 newContent.addValue(cms, TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.itemList.relatedNews"), Locale.ENGLISH,0);
			 
			 
			newContent.getValue(tagNameNoticia,Locale.ENGLISH).setStringValue(cms,getElementValue(tagNameNoticia, content, Locale.ENGLISH));
			}
				
			//fecha
			 String tagNameFecha = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")+"[" + j + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.itemList.date");
					newContent.getValue(tagNameFecha,Locale.ENGLISH).setStringValue(cms,getElementValue(tagNameFecha , content, Locale.ENGLISH));
		}
	}
	
	public JSONObject save() {
		JSONObject result = new JSONObject();
		JSONArray errorsJS = new JSONArray();
		CmsObject cms = getCmsObject();
		int resourcesCount = request.getParameterValues("path[]").length;
		
		for (int idx=0;idx<resourcesCount;idx++) {
			
			String path = String.valueOf(request.getParameterValues("path[]")[idx]);
			
			/* scriptsJSFilter Remove 
			//ScriptsJSFilter scriptsJSFilter = new com.tfsla.utils.ScriptsJSFilter(cms,path);
			
			//String titleOrig = String.valueOf(request.getParameterValues("title[]")[idx]);
			//String title = "";
			
			//if(!titleOrig.equals("") && !titleOrig.equals("||!!undefined!!||"))
			//	title = scriptsJSFilter.removeAllJavascriptCode(titleOrig,true);
			//else
			//	title = titleOrig;
			 * 
			 */
			String title = String.valueOf(request.getParameterValues("title[]")[idx]);
			
			String section = String.valueOf(request.getParameterValues("section[]")[idx]);
			String zoneHome = String.valueOf(request.getParameterValues("zoneHome[]")[idx]);
			String zoneHomePriority = String.valueOf(request.getParameterValues("zoneHomePriority[]")[idx]);
			String zoneSection = String.valueOf(request.getParameterValues("zoneSection[]")[idx]);
			String zoneSectionPriority = String.valueOf(request.getParameterValues("zoneSectionPriority[]")[idx]);
			String keywords = String.valueOf(request.getParameterValues("keywords[]")[idx]);	
			String hiddenkeywords = String.valueOf(request.getParameterValues("hiddenkeywords[]")[idx]);	
			
			/* scriptsJSFilter Remove 
			String copeteOrig = String.valueOf(request.getParameterValues("copete[]")[idx]);
			String copete = "";
			
			if(!copeteOrig.equals("") && !copeteOrig.equals("||!!undefined!!||"))
				copete = scriptsJSFilter.removeAllJavascriptCode(copeteOrig,true);
			else
				copete = copeteOrig;
			
			
			String bodyOrig = String.valueOf(request.getParameterValues("body[]")[idx]);
			
			String body = "";
			
			if(!bodyOrig.equals("") && !bodyOrig.equals("||!!undefined!!||") )
				body = scriptsJSFilter.removeJSFromBody(bodyOrig);
			else
				body = bodyOrig;
			*/
			
			String copete = String.valueOf(request.getParameterValues("copete[]")[idx]);
			
			String body = String.valueOf(request.getParameterValues("body[]")[idx]);
			
            String strIsSetRelease = String.valueOf(request.getParameterValues("isSetRelease[]")[idx]);
            String strIsSetExpire = String.valueOf(request.getParameterValues("isSetExpire[]")[idx]);
            String ocultarAnuncios = String.valueOf(request.getParameterValues("hideAds[]")[idx]);
            
            boolean isExpirationReleaseDefined = (!strIsSetRelease.equals("||!!undefined!!||"));

            boolean isSetRelease = Boolean.valueOf(strIsSetRelease);
            boolean isSetExpire = Boolean.valueOf(strIsSetExpire);

            //boolean isExpirationReleaseDefined = isSetRelease || isSetExpire;
            
            long releaseDate = CmsResource.DATE_RELEASED_DEFAULT;
            long expireDate = CmsResource.DATE_EXPIRED_DEFAULT;
			long ultimaModificacion = 0;
            
            try {
            	String strReleaseDate = String.valueOf(request.getParameterValues("releaseDate[]")[idx]);
            	if (strReleaseDate.indexOf("||!!undefined!!||")==-1)
            		releaseDate = TfsCalendarWidget.getCalendarDate(m_messages, strReleaseDate, true);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}

            try {	
            	String strExpireDate = String.valueOf(request.getParameterValues("expireDate[]")[idx]);
            	if (strExpireDate.indexOf("||!!undefined!!||")==-1)
            		expireDate = TfsCalendarWidget.getCalendarDate(m_messages, strExpireDate, true);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
            
            try {	
            	String strLastModifDate = String.valueOf(request.getParameterValues("lastModifDate[]")[idx]);
            	if (strLastModifDate!=null && !strLastModifDate.matches("-?\\d+(\\.\\d+)?") && strLastModifDate.indexOf("||!!undefined!!||")==-1 && !strLastModifDate.equals("0"))
            		ultimaModificacion = TfsCalendarWidget.getCalendarDate(m_messages, strLastModifDate, true);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
				
			try {		
				String tempPath = CmsWorkplace.getTemporaryFileName(path);
            	
            	if (getCmsObject().existsResource(tempPath, CmsResourceFilter.ALL)) {
            		CmsResourceUtils.forceLockResource(cms,tempPath);
            		cms.deleteResource(tempPath, CmsResource.DELETE_PRESERVE_SIBLINGS);
            	}
            	
            	CmsResourceUtils.forceLockResource(cms,path);
		
				CmsFile file = cms.readFile(path,CmsResourceFilter.ALL);
				
				CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
				content.setAutoCorrectionEnabled(true);
				content.correctXmlStructure(cms);
				
				
				if (request.getParameterValues("categorias[]")[idx]!=null && !request.getParameterValues("categorias[]")[idx].equals("||!!undefined!!||")) {
					removeAllElement(content,"Categorias");
					String categs = String.valueOf(request.getParameterValues("categorias[]")[idx]);
					if(categs != null && !categs.equals("")) {
						String[] categorias = request.getParameterValues("categorias[]")[idx].split(",");
						for (int catIdx=0;catIdx<categorias.length;catIdx++) {
							content.addValue(cms, "Categorias", Locale.ENGLISH, 0).setStringValue(cms, categorias[catIdx]);
						}
					}
				}
				
				content.getValue("titulo", Locale.ENGLISH).setStringValue(cms, title);
				content.getValue("seccion", Locale.ENGLISH).setStringValue(cms, section);
				if (!zoneHome.equals("||!!undefined!!||"))
					content.getValue("zonahome", Locale.ENGLISH).setStringValue(cms, zoneHome);
				if (!zoneHomePriority.equals("||!!undefined!!||"))
					content.getValue("prioridadhome", Locale.ENGLISH).setStringValue(cms, zoneHomePriority);
				if (!zoneSection.equals("||!!undefined!!||"))
					content.getValue("zonaseccion", Locale.ENGLISH).setStringValue(cms, zoneSection);
				if (!zoneSectionPriority.equals("||!!undefined!!||"))
					content.getValue("prioridadseccion", Locale.ENGLISH).setStringValue(cms, zoneSectionPriority);
				if (!keywords.equals("||!!undefined!!||"))
					content.getValue("claves", Locale.ENGLISH).setStringValue(cms, keywords);
				if (!hiddenkeywords.equals("||!!undefined!!||"))
					content.getValue("clavesOcultas", Locale.ENGLISH).setStringValue(cms, hiddenkeywords);
				if (!copete.equals("||!!undefined!!||"))
					content.getValue("copete", Locale.ENGLISH).setStringValue(cms, copete);
				if (!body.equals("||!!undefined!!||"))
					content.getValue("cuerpo", Locale.ENGLISH).setStringValue(cms, body);	
				if (!ocultarAnuncios.equals("||!!undefined!!||"))
					content.getValue("ocultarAnuncios", Locale.ENGLISH).setStringValue(cms, ocultarAnuncios);
				if (ultimaModificacion != 0 )
					content.getValue("ultimaModificacion", Locale.ENGLISH).setStringValue(cms, String.valueOf(ultimaModificacion));
				
				int pubNum = 0;
				while (request.getParameter("publications[" + idx + "][" + pubNum + "][section]")!=null) {
					content.getValue("publicaciones[" + (pubNum +1) + "]/seccion[1]" , Locale.ENGLISH).setStringValue(cms, request.getParameter("publications[" + idx + "][" + pubNum + "][section]"));
					content.getValue("publicaciones[" + (pubNum +1) + "]/zonahome[1]" , Locale.ENGLISH).setStringValue(cms, request.getParameter("publications[" + idx + "][" + pubNum + "][zoneHome]"));
					content.getValue("publicaciones[" + (pubNum +1) + "]/prioridadhome[1]" , Locale.ENGLISH).setStringValue(cms, request.getParameter("publications[" + idx + "][" + pubNum + "][zoneHomePriority]"));
					content.getValue("publicaciones[" + (pubNum +1) + "]/zonaseccion[1]" , Locale.ENGLISH).setStringValue(cms, request.getParameter("publications[" + idx + "][" + pubNum + "][zoneSection]"));
					content.getValue("publicaciones[" + (pubNum +1) + "]/prioridadseccion[1]" , Locale.ENGLISH).setStringValue(cms, request.getParameter("publications[" + idx + "][" + pubNum + "][zoneSectionPriority]"));
					pubNum ++;
				}
						
				String fileEncoding = getFileEncoding(cms, file.getRootPath());
				
				String decodedContent = content.toString();
		        decodedContent = decodedContent.replaceAll("(?!\\n)[\\p{C}]", "");
				
				//file.setContents(content.marshal());
		        file.setContents(decodedContent.getBytes(fileEncoding));
				cms.writeFile(file);	
								
				if (isExpirationReleaseDefined)
	            	modifyResourceAvailability(file, releaseDate, expireDate, isSetRelease, isSetExpire);


				cms.unlockResource(path);
				result.put("status", "ok");
						   	
				resources.add(getCmsObject().readResource(path,CmsResourceFilter.ALL));
			
			} catch (Exception e) {
				
				LOG.error("Error trying to save resource " + path + " in site " + cms.getRequestContext().getSiteRoot() + " (" + cms.getRequestContext().currentProject().getName() + ")", e);
				
				result.put("status", "error");
				
				JSONObject error = new JSONObject(); 
            	error.put("path", path);
            	error.put("message",e.getMessage());
            	
				try{
					CmsLock lockE = cms.getLock(path);
	           	
	            	if (!lockE.isUnlocked()) {
	           		CmsUUID userId = lockE.getUserId();
	            		CmsUser lockUser = cms.readUser(userId);
	            		
	            		error.put("lockby",lockUser.getFullName());
	            	}
				}catch (CmsException e2) {
				}
				
				errorsJS.add(error);
			}
		}
		
		result.put("errors", errorsJS );
		
		return result;
	}
	
	public JSONObject saveDirect() {
		JSONObject result = new JSONObject();
		JSONArray errorsJS = new JSONArray();
		CmsObject cms = getCmsObject();
		int resourcesCount = request.getParameterValues("path[]").length;
		String images = "||!!undefined!!||";
		String video = "||!!undefined!!||";
		String videoYT = "||!!undefined!!||";
		String videoEmb = "||!!undefined!!||";
		String relatedNews = "||!!undefined!!||";
		String author = "||!!undefined!!||";
		
		for (int idx=0;idx<resourcesCount;idx++) {
			String path = String.valueOf(request.getParameterValues("path[]")[idx]);
			String title = String.valueOf(request.getParameterValues("title[]")[idx]);
			String section = String.valueOf(request.getParameterValues("section[]")[idx]);
			String zoneHome = String.valueOf(request.getParameterValues("zoneHome[]")[idx]);
			String zoneHomePriority = String.valueOf(request.getParameterValues("zoneHomePriority[]")[idx]);
			String zoneSection = String.valueOf(request.getParameterValues("zoneSection[]")[idx]);
			String zoneSectionPriority = String.valueOf(request.getParameterValues("zoneSectionPriority[]")[idx]);
			String keywords = String.valueOf(request.getParameterValues("keywords[]")[idx]);	
			String hiddenkeywords = String.valueOf(request.getParameterValues("hiddenkeywords[]")[idx]);	
			String copete = String.valueOf(request.getParameterValues("copete[]")[idx]);
			String body = String.valueOf(request.getParameterValues("body[]")[idx]);
			
			try{				
				images = String.valueOf(request.getParameterValues("images[]")[idx]);
				video = String.valueOf(request.getParameterValues("video[]")[idx]);
				videoYT = String.valueOf(request.getParameterValues("videoYT[]")[idx]);
				videoEmb = String.valueOf(request.getParameterValues("videoEmbedded[]")[idx]);
				relatedNews = String.valueOf(request.getParameterValues("relatedNews[]")[idx]);
				author = String.valueOf(request.getParameterValues("author[]")[idx]);
			}catch(Exception e){
				//No se envian en todos lados estos parametros - just write
				e.printStackTrace();
			}
			
            String strIsSetRelease = String.valueOf(request.getParameterValues("isSetRelease[]")[idx]);
            String strIsSetExpire = String.valueOf(request.getParameterValues("isSetExpire[]")[idx]);

            boolean isExpirationReleaseDefined = (!strIsSetRelease.equals("||!!undefined!!||"));

            boolean isSetRelease = Boolean.valueOf(strIsSetRelease);
            boolean isSetExpire = Boolean.valueOf(strIsSetExpire);

            long releaseDate = CmsResource.DATE_RELEASED_DEFAULT;
            long expireDate = CmsResource.DATE_EXPIRED_DEFAULT;

			try {		
				String tempPath = CmsWorkplace.getTemporaryFileName(path);
            	
            	if (getCmsObject().existsResource(tempPath, CmsResourceFilter.ALL)) {
            		CmsResourceUtils.forceLockResource(cms,tempPath);
            		cms.deleteResource(tempPath, CmsResource.DELETE_PRESERVE_SIBLINGS);
            	}
            	
            	CmsResourceUtils.forceLockResource(cms,path);
		
				CmsFile file = cms.readFile(path,CmsResourceFilter.ALL);
				
				CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
				content.setAutoCorrectionEnabled(true);
				content.correctXmlStructure(cms);
				
				
				if (request.getParameterValues("categorias[]")[idx]!=null && !request.getParameterValues("categorias[]")[idx].equals("||!!undefined!!||")) {
					removeAllElement(content,"Categorias");
					String categs = String.valueOf(request.getParameterValues("categorias[]")[idx]);
					if(categs != null && !categs.equals("")) {
						String[] categorias = request.getParameterValues("categorias[]")[idx].split(",");
						for (int catIdx=0;catIdx<categorias.length;catIdx++) {
							content.addValue(cms, "Categorias", Locale.ENGLISH, 0).setStringValue(cms, categorias[catIdx]);
						}
					}
				}
				
				content.getValue("titulo", Locale.ENGLISH).setStringValue(cms, title);
				content.getValue("seccion", Locale.ENGLISH).setStringValue(cms, section);
				if (!zoneHome.equals("||!!undefined!!||"))
					content.getValue("zonahome", Locale.ENGLISH).setStringValue(cms, zoneHome);
				if (!zoneHomePriority.equals("||!!undefined!!||"))
					content.getValue("prioridadhome", Locale.ENGLISH).setStringValue(cms, zoneHomePriority);
				if (!zoneSection.equals("||!!undefined!!||"))
					content.getValue("zonaseccion", Locale.ENGLISH).setStringValue(cms, zoneSection);
				if (!zoneSectionPriority.equals("||!!undefined!!||"))
					content.getValue("prioridadseccion", Locale.ENGLISH).setStringValue(cms, zoneSectionPriority);
				if (!keywords.equals("||!!undefined!!||"))
					content.getValue("claves", Locale.ENGLISH).setStringValue(cms, keywords);
				if (!hiddenkeywords.equals("||!!undefined!!||"))
					content.getValue("clavesOcultas", Locale.ENGLISH).setStringValue(cms, hiddenkeywords);
				if (!copete.equals("||!!undefined!!||"))
					content.getValue("copete", Locale.ENGLISH).setStringValue(cms, copete);
				if (!body.equals("||!!undefined!!||"))
					content.getValue("cuerpo", Locale.ENGLISH).setStringValue(cms, body);	
				if (!author.equals("||!!undefined!!||")){
					content.getValue("autor/internalUser", Locale.ENGLISH).setStringValue(cms, author);	
				}
				
				if (!images.equals("||!!undefined!!||")){
					String[] imagesPhoto = images.split(",");					
					
					I_CmsXmlContentValue value = null;
					
					if (imagesPhoto.length>0)
					{					
						value = content.getValue("imagenPrevisualizacion/imagen",Locale.ENGLISH);
						value.setStringValue(cms,imagesPhoto[0]);
					}
					
					for (int j=1;j<imagesPhoto.length;j++)
					{
						String xmlName ="imagenesFotogaleria[" + j + "]";
						value = content.getValue(xmlName, Locale.ENGLISH);
						if (value==null)
							value = content.addValue(cms,"imagenesFotogaleria",Locale.ENGLISH,j-1);
							xmlName ="imagenesFotogaleria[" + j + "]/imagen[1]";
							value = content.getValue(xmlName,Locale.ENGLISH);
							value.setStringValue(cms,imagesPhoto[j-1]);														
					}
				}
				if (!video.equals("||!!undefined!!||")){
					content.getValue("videoFlash[1]/video[1]", Locale.ENGLISH).setStringValue(cms, video);	
				}
				if (!videoYT.equals("||!!undefined!!||")){
					String[] videosYoutube = videoYT.split(",");
					
					for (int Idx=0;Idx<videosYoutube.length;Idx++) {		
						int c = Idx + 1;
						try{
							if(!content.hasValue("videoYouTube["+c+"]", Locale.ENGLISH))
								content.addValue(cms, "videoYouTube["+c+"]", Locale.ENGLISH, 0);						
						}catch(Exception e){
							e.printStackTrace();
						}						
						content.getValue("videoYouTube[1]/youtubeid[1]", Locale.ENGLISH, 0).setStringValue(cms, videosYoutube[Idx]);								
					}	
				}
				if (!videoEmb.equals("||!!undefined!!||")){
					String[] videosEmbedded = videoEmb.split(",");
					
					for (int Idx=0;Idx<videosEmbedded.length;Idx++) {		
						int c = Idx + 1;
						try{
							if(!content.hasValue("videoEmbedded["+c+"]", Locale.ENGLISH))
								content.addValue(cms, "videoEmbedded["+c+"]", Locale.ENGLISH, 0);						
						}catch(Exception e){
							e.printStackTrace();
						}						
						content.getValue("videoEmbedded[1]/codigo[1]", Locale.ENGLISH, 0).setStringValue(cms, videosEmbedded[Idx]);								
					}					
				}
				if (!relatedNews.equals("||!!undefined!!||")){
					String[] rNews = relatedNews.split(",");
					
					for (int Idx=0;Idx<rNews.length;Idx++) {		
						int c = Idx + 1;
						try{
							if(!content.hasValue("noticiasRelacionadas["+c+"]", Locale.ENGLISH))
								content.addValue(cms, "noticiasRelacionadas["+c+"]", Locale.ENGLISH, 0);						
						}catch(Exception e){
							e.printStackTrace();
						}						
						content.getValue("noticiasRelacionadas[1]/noticia[1]", Locale.ENGLISH, 0).setStringValue(cms, rNews[Idx]);								
					}		
				}	
				
				
				int pubNum = 0;
				while (request.getParameter("publications[" + idx + "][" + pubNum + "][section]")!=null) {
					content.getValue("publicaciones[" + (pubNum +1) + "]/seccion[1]" , Locale.ENGLISH).setStringValue(cms, request.getParameter("publications[" + idx + "][" + pubNum + "][section]"));
					content.getValue("publicaciones[" + (pubNum +1) + "]/zonahome[1]" , Locale.ENGLISH).setStringValue(cms, request.getParameter("publications[" + idx + "][" + pubNum + "][zoneHome]"));
					content.getValue("publicaciones[" + (pubNum +1) + "]/prioridadhome[1]" , Locale.ENGLISH).setStringValue(cms, request.getParameter("publications[" + idx + "][" + pubNum + "][zoneHomePriority]"));
					content.getValue("publicaciones[" + (pubNum +1) + "]/zonaseccion[1]" , Locale.ENGLISH).setStringValue(cms, request.getParameter("publications[" + idx + "][" + pubNum + "][zoneSection]"));
					content.getValue("publicaciones[" + (pubNum +1) + "]/prioridadseccion[1]" , Locale.ENGLISH).setStringValue(cms, request.getParameter("publications[" + idx + "][" + pubNum + "][zoneSectionPriority]"));
					pubNum ++;
				}
									   			   	
				file.setContents(content.marshal());
				cms.writeFile(file);	
				
	            if (isExpirationReleaseDefined)
	            	modifyResourceAvailability(file, releaseDate, expireDate, isSetRelease, isSetExpire);
				            
				cms.unlockResource(path);
				result.put("status", "ok");
						   	
				resources.add(getCmsObject().readResource(path,CmsResourceFilter.ALL));
			
			} catch (Exception e) {
				
				result.put("status", "error");
				
				JSONObject error = new JSONObject(); 
            	error.put("path", path);
            	error.put("message",e.getMessage());
            	
				try{
					CmsLock lockE = cms.getLock(path);
	           	
	            	if (!lockE.isUnlocked()) {
	           		CmsUUID userId = lockE.getUserId();
	            		CmsUser lockUser = cms.readUser(userId);
	            		
	            		error.put("lockby",lockUser.getFullName());
	            	}
				}catch (CmsException e2) {
				}
				
				errorsJS.add(error);
			}
		}
		
		result.put("errors", errorsJS );
		
		return result;
	}
	
    protected void modifyResourceAvailability(
            CmsFile resource,
            long releaseDate,
            long expireDate,
            boolean isSetRelease,
            boolean isSetExpire) throws CmsException {

    		CmsObject cms = getCmsObject();

            String resourcePath = cms.getSitePath(resource);
            
            // modify release and expire date of the resource if needed
            if (!isSetRelease) 
            	releaseDate = CmsResource.DATE_RELEASED_DEFAULT;
            
            cms.setDateReleased(resourcePath, releaseDate, false);
            
            if (!isSetExpire)
            	expireDate = CmsResource.DATE_EXPIRED_DEFAULT;
            
            cms.setDateExpired(resourcePath, expireDate, false);
    }
	
	public void removeAllElement(CmsXmlContent content, String pathElement)
    {
    	int count = content.getIndexCount(pathElement, Locale.ENGLISH);
    	for (int j=0;j<count;j++)
    		content.removeValue(pathElement, Locale.ENGLISH, 0);
    	
        
    }
	
	public synchronized void publish(boolean unlock, boolean recursivePublish) throws CmsException
	{
		CmsObject cms = getCmsObject();
		
		List<CmsResource> relatedResources = new ArrayList<CmsResource>();
		for (CmsResource resource : resources ) {
			
			if(resource.getTypeId() == OpenCms.getResourceManager().getResourceType("noticia").getTypeId() ){
				ScriptsJSFilter scriptsJSFilter = new com.tfsla.utils.ScriptsJSFilter(cms,cms.getRequestContext().removeSiteRoot(resource.getRootPath()));
				scriptsJSFilter.cleanResource("noticia");
			}
			
			List<CmsResource> currentRelResources = addRelatedResourcesToPublish(resource,unlock,recursivePublish, new ArrayList<CmsResource>());
			currentRelResources.removeAll(relatedResources);
			currentRelResources.removeAll(resources);
			relatedResources.addAll(currentRelResources);
		}
		
		resources.addAll(relatedResources);
		
		
		CmsPublishList pList = OpenCms.getPublishManager().getPublishList(cms, resources, false);
		
		
		CmsLogReport report = new CmsLogReport(Locale.ENGLISH,this.getClass());
         	OpenCms.getPublishManager().publishProject(cms, report, pList);
         	
         	if(pList.size()<30)
         	OpenCms.getPublishManager().waitWhileRunning();
	}
	
	public synchronized void publish() throws CmsException
	{
		CmsObject cms = getCmsObject();
		
		List<CmsResource> relatedResources = new ArrayList<CmsResource>();
		for (CmsResource resource : resources ) {
			List<CmsResource> currentRelResources = addRelatedResourcesToPublish(resource,false,true, new ArrayList<CmsResource>());
			currentRelResources.removeAll(relatedResources);
			currentRelResources.removeAll(resources);
			relatedResources.addAll(currentRelResources);
		}
		
		resources.addAll(relatedResources);
		
		
		CmsPublishList pList = OpenCms.getPublishManager().getPublishList(cms, resources, false);
		
		
		CmsLogReport report = new CmsLogReport(Locale.ENGLISH,this.getClass());
         	OpenCms.getPublishManager().publishProject(cms, report, pList);

         	if(pList.size()<30)
         	OpenCms.getPublishManager().waitWhileRunning();

	}
	
	public synchronized void publishNews() throws CmsException
	{
		CmsObject cms = getCmsObject();
		if(resources.isEmpty()){
			retrieveNews();
		}
		List<CmsResource> relatedResources = new ArrayList<CmsResource>();
		for (CmsResource resource : resources ) {
			List<CmsResource> currentRelResources = addRelatedResourcesToPublish(resource,false,true, new ArrayList<CmsResource>());
			currentRelResources.removeAll(relatedResources);
			currentRelResources.removeAll(resources);
			relatedResources.addAll(currentRelResources);
		}
		
		resources.addAll(relatedResources);
		
		
		CmsPublishList pList = OpenCms.getPublishManager().getPublishList(cms, resources, false);
		
		
		CmsLogReport report = new CmsLogReport(Locale.ENGLISH,this.getClass());
         	OpenCms.getPublishManager().publishProject(cms, report, pList);
         	if(pList.size()<30)
         	OpenCms.getPublishManager().waitWhileRunning();

	}
	
	public List<CmsResource> addRelatedResourcesToPublish(CmsResource resource, boolean unlock, boolean recursivePublish ,List<CmsResource> resourcesList) {
		CmsObject cms = getCmsObject();
		

		try {

   			int tipoNoticia = OpenCms.getResourceManager().getResourceType("noticia").getTypeId();
   			int tipoVideoLink = OpenCms.getResourceManager().getResourceType("video-link").getTypeId();
   			int tipoVideo = OpenCms.getResourceManager().getResourceType("video").getTypeId();

			int tipoOrigen = resource.getTypeId();
			boolean esVideoOrigen = (tipoOrigen == tipoVideo) || (tipoOrigen == tipoVideoLink);

			@SuppressWarnings("unchecked")
			List<CmsRelation> relations = cms.getRelationsForResource(cms.getSitePath(resource), CmsRelationFilter.ALL);
   		
			//LOG.error(resource.getRootPath() + " | esVideoOrigen " + esVideoOrigen + "" );
   		
       		for ( CmsRelation relation : relations) {
       			
       			try {
       				String rel1 = relation.getTargetPath();
       				String rel2 = relation.getTargetPath();

       				String rel = "";
       				if (rel1.equals(resource.getRootPath()))
       					rel = rel2;
       				else
       					rel = rel1;
       				
       				
       				CmsResource res = cms.readResource(cms.getRequestContext().removeSiteRoot(rel));
				
       				CmsResourceState estado = res.getState();

       				if (estado.equals(CmsResourceState.STATE_UNCHANGED))
       					continue;
       				
		    	    int tipo = res.getTypeId();
				
       				CmsLock lock = cms.getLock(res);
       				boolean esNoticia = (tipo == tipoNoticia);
       				boolean esVideo = (tipo == tipoVideo) || (tipo == tipoVideoLink);
			
       				//LOG.error("relacion con " + res.getRootPath() + " | esVideo " + esVideo  + " | esNoticia " + esNoticia );

       				if (!lock.isUnlocked() && unlock) {
       					if (lock.getUserId().equals(cms.getRequestContext().currentUser().getId())) {
       						cms.unlockResource(cms.getRequestContext().removeSiteRoot(rel));
       					}
       					else if (forcePublish) {
       						cms.changeLock(cms.getRequestContext().removeSiteRoot(rel));
       						cms.unlockResource(cms.getRequestContext().removeSiteRoot(rel));
       					}
       					else {
       						continue;
       					}
       				}

       				//LOG.error("esVideoOrigen " + esVideoOrigen + " | esVideo " + esVideo  + " | publishRelatedContent " + publishRelatedContent );

       				if (esVideoOrigen && esVideo) {
						if (publishRelatedContent)
							if (!resourcesList.contains(res))
								resourcesList.add(res);	
       					
       				}
       				else if (!esNoticia) {
						if (publishRelatedContent)
							if (!resourcesList.contains(res))
								resourcesList.add(res);	
						
						if (esVideo && publishRelatedContent)
							addRelatedResourcesToPublish(res, unlock, true, resourcesList);
					}
					else if (publishRelatedNews && recursivePublish) {
						if (!resourcesList.contains(res))
							resourcesList.add(res);
						addRelatedResourcesToPublish(res, unlock, false, resourcesList);

					}
				
			} catch (CmsException e) {
				 e.printStackTrace();
		      	}
			}
		}
		catch (CmsException e) {
			 e.printStackTrace();
		}
       	return resourcesList;

	}

	public Boolean mustPublish() {
		return publish;
	}

	public Boolean isForcePublish() {
		return forcePublish;
	}

	public Boolean getPublishRelatedContent() {
		return publishRelatedContent;
	}

	public Boolean getPublishRelatedNews() {
		return publishRelatedNews;
	}
	
    public boolean isDefaultAutoSaveEnabled()
    {
    	return config.getParam(siteName, publication, moduleConfigName, "autoSaveDefault","enabled").toLowerCase().equals("enabled");
    }

    public boolean isDefaultForcePublishEnabled()
    {
    	return config.getParam(siteName, publication, moduleConfigName, "forcePublishDefault","enabled").toLowerCase().equals("enabled");
    }

    public boolean isDefaultPublishRelatedContentEnabled()
    {
    	return config.getParam(siteName, publication, moduleConfigName, "publishRelatedContentDefault","enabled").toLowerCase().equals("enabled");
    }

    public boolean isDefaultPublishRelatedNewsEnabled()
    {
    	return config.getParam(siteName, publication, moduleConfigName, "publishRelatedNewsDefault","enabled").toLowerCase().equals("enabled");
    }
    
    public String getDefaultNewsOrder()
    {
    	return config.getParam(siteName, publication, moduleConfigName, "defaultNewsOrder","user-modification-date desc");
    }
    
    public int getDefaultNewsResultSize()
    {
    	return config.getIntegerParam(siteName, publication, moduleConfigName, "defaultNewsResultCount",100);
    }

    public List<String> getNewsResultSizeOptions()
    {
    	return config.getParamList(siteName, publication, moduleConfigName, "newsResultSizeOptions");
    }
    
    public String getDefaultPollsTemplate()
    {
    	return config.getParam(siteName, publication, "polls", "defaultNewsTemplate", null);
    }
    
    public List<String> getPollsTemplates()
    {
    	return config.getParamList(siteName, publication, "polls", "styles");
    }
    
    public void unlockRelated(String path){
    	
    	CmsObject cms = getCmsObject();
    	
		try {
			CmsResource resource = cms.readResource(path);
	        
	        @SuppressWarnings("unchecked")
			List<CmsRelation> relations = cms.getRelationsForResource(cms.getSitePath(resource), CmsRelationFilter.ALL);
	   		
	        for ( CmsRelation relation : relations) {
	        	String rel1 = relation.getTargetPath();
   				String rel2 = relation.getTargetPath();

   				String rel = "";
   				if (rel1.equals(resource.getRootPath()))
   					rel = rel2;
   				else
   					rel = rel1;
   				
   				CmsResource relatedResource = cms.readResource(cms.getRequestContext().removeSiteRoot(rel));
   				String     rel_resourceName = cms.getRequestContext().removeSiteRoot(relatedResource.getRootPath());
	        	
	        	com.tfsla.utils.CmsResourceUtils.unlockResource(cms, rel_resourceName , false);
	        }
	        
		} catch (CmsException e) {
			e.printStackTrace();
		}
		
		return;
    }
    
    public Locale getLocale() {

        return m_settings.getUserSettings().getLocale();
    }
    
    protected int getElementCountWithValue(String key, String controlKey, I_CmsXmlDocument content)
	{
		Locale locale = getCmsObject().getRequestContext().getLocale();
		int total = content.getIndexCount(key, locale);
		
		int blank = 0;
		for (int j=1;j<=total;j++)
		{
			String controlValue;
			try {
				controlValue = content.getStringValue(getCmsObject(), key + "[" + j + "]/" + controlKey, locale);
			
				if (controlValue==null || controlValue.trim().equals(""))
					blank ++;
			} catch (CmsXmlException e) {
				LOG.debug("Error reading content value " + key + "[" + j + "]/" + controlKey + " on content " + content.getFile().getRootPath(),e);

			}
		}
		
		
		return total - blank;
	}
	protected String getElementValue(String elementName, I_CmsXmlDocument content, Locale locale) {    
		try {
	    	String value = content.getStringValue(getCmsObject(), elementName, locale);
	    	if (value==null)
	    	{
	    		value = "";
	    		LOG.debug("Content value " + elementName + "not found on content" + content.getFile().getRootPath());
	    	}
			return value;
		} catch (CmsXmlException e) {
			LOG.error("Error reading content value " + elementName + " on content " + content.getFile().getRootPath(),e);
		}
	
		return "";
	}
	
	protected String getFileEncoding(CmsObject cms, String filename) {

        try {
            return cms.readPropertyObject(filename, CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, true).getValue(
                OpenCms.getSystemInfo().getDefaultEncoding());
        } catch (CmsException e) {
            return OpenCms.getSystemInfo().getDefaultEncoding();
        }
    }
	
}
