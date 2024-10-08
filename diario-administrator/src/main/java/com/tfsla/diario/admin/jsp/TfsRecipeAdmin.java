package com.tfsla.diario.admin.jsp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.report.CmsLogReport;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.admin.widgets.TfsCalendarWidget;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.utils.CmsResourceUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import java.text.ParseException;

public class TfsRecipeAdmin {

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

    public TfsRecipeAdmin (PageContext context, HttpServletRequest req, HttpServletResponse res) throws Exception {
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
		moduleConfigName = "events";
		config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
	
	    
		publish = Boolean.parseBoolean(request.getParameter("publish"));
		
		publishRelatedContent = Boolean.parseBoolean(request.getParameter("publishRelatedContent"));
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
		
	public Locale getLocale() {
		return m_settings.getUserSettings().getLocale();
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
	
	public JSONObject save() {
		JSONObject result = new JSONObject();
		JSONArray errorsJS = new JSONArray();
		CmsObject cms = getCmsObject();
		
		String path = String.valueOf(request.getParameter("path"));
		String title = String.valueOf(request.getParameter("title"));
		String keywords = String.valueOf(request.getParameter("keywords"));	
		//String personas = String.valueOf(request.getParameter("personas"));
        
	    String dificultad = String.valueOf(request.getParameter("difficulty"));
	    String tipoCoccion = String.valueOf(request.getParameter("cookingType"));
	    String tipoCocina = String.valueOf(request.getParameter("cuisinteType"));
	    String porciones = String.valueOf(request.getParameter("portions"));
	           
	    String strIsSetRelease = String.valueOf(request.getParameter("isSetRelease"));
        String strIsSetExpire = String.valueOf(request.getParameter("isSetExpire"));

        boolean isExpirationReleaseDefined = (!strIsSetExpire.equals("||!!undefined!!||")||!strIsSetRelease.equals("||!!undefined!!||"));

        boolean isSetRelease = Boolean.valueOf(strIsSetRelease);
        boolean isSetExpire = Boolean.valueOf(strIsSetExpire);

        long releaseDate = CmsResource.DATE_RELEASED_DEFAULT;
        long expireDate = CmsResource.DATE_EXPIRED_DEFAULT;
			
        try {
            	String strReleaseDate = String.valueOf(request.getParameter("releaseDate"));
            	if (isSetRelease && !strReleaseDate.equals("||!!undefined!!||"))
            		releaseDate = TfsCalendarWidget.getCalendarDate(m_messages, strReleaseDate, true);
        } catch (ParseException e1) {
				e1.printStackTrace();
		}

            try {	
            	String strExpireDate = String.valueOf(request.getParameter("expireDate"));
            	if (isSetExpire && !strExpireDate.equals("||!!undefined!!||"))
            		expireDate = TfsCalendarWidget.getCalendarDate(m_messages, strExpireDate, true);
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
				
				
				if (request.getParameterValues("categorias")!=null && !request.getParameterValues("categorias").equals("||!!undefined!!||")) {
					removeAllElement(content,"Categorias");
					String categs = String.valueOf(request.getParameterValues("categorias[]"));
					if(categs != null && !categs.equals("")) {
						String[] categorias = request.getParameter("categorias").split(",");
						for (int catIdx=0;catIdx<categorias.length;catIdx++) {
							content.addValue(cms, "Categorias", Locale.ENGLISH, 0).setStringValue(cms, categorias[catIdx]);
						}
					}
				}
				
				content.getValue("titulo", Locale.ENGLISH).setStringValue(cms, title);
				if (!keywords.equals("||!!undefined!!||"))
					content.getValue("claves", Locale.ENGLISH).setStringValue(cms, keywords);
				/*if (!personas.equals("||!!undefined!!||"))
					content.getValue("personas", Locale.ENGLISH).setStringValue(cms, personas);
				if (!body.equals("||!!undefined!!||"))
					content.getValue("cuerpo", Locale.ENGLISH).setStringValue(cms, body);	
				*/
				if (!porciones.equals("||!!undefined!!||"))
					content.getValue("porciones", Locale.ENGLISH).setStringValue(cms, porciones);	
				if (!tipoCoccion.equals("||!!undefined!!||"))
					content.getValue("tipoCoccion", Locale.ENGLISH).setStringValue(cms, tipoCoccion);	
				if (!tipoCocina.equals("||!!undefined!!||"))
					content.getValue("tipoCocina", Locale.ENGLISH).setStringValue(cms, tipoCocina);	
				if (!dificultad.equals("||!!undefined!!||"))
					content.getValue("dificultad", Locale.ENGLISH).setStringValue(cms, dificultad);	
				
				file.setContents(content.marshal());
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
		
		
		result.put("errors", errorsJS );
		
		return result;
	}
	
	public JSONObject saveAutomaticCategories() {
		JSONObject result = new JSONObject();
		JSONArray errorsJS = new JSONArray();
		CmsObject cms = getCmsObject();
		String path = String.valueOf(request.getParameter("path"));
		
		
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
			
			List<I_CmsXmlContentValue> categoriasAutomaticas = content.getValues("playlistAutomatica[1]/Categorias", Locale.ENGLISH);
			List<String> categorias = new ArrayList<String>();
			for (I_CmsXmlContentValue value : categoriasAutomaticas) {
				categorias.add(value.getStringValue(cms));
			}
			
			removeAllElement(content,"Categorias");
			for (String categoria : categorias) {
				content.addValue(cms, "Categorias", Locale.ENGLISH, 0).setStringValue(cms, categoria);
			}
					
			file.setContents(content.marshal());
			cms.writeFile(file);	
				            
			cms.unlockResource(path);
			result.put("status", "ok");
					   	
			resources.add(getCmsObject().readResource(path,CmsResourceFilter.ALL));
			
		} catch (Exception e) {
			
			LOG.error("Error trying to save resource " + path + " in site " + cms.getRequestContext().getSiteRoot() + " (" + cms.getRequestContext().currentProject().getName() + ")", e);
			
			result.put("status", "error");
			
			JSONObject error = new JSONObject(); 
        	error.put("path", path);
        	error.put("message",e.getMessage());
        	
        	errorsJS.add(error);
		}
		result.put("errors", errorsJS );

		return result;
	}
	
	public void removeAllElement(CmsXmlContent content, String pathElement)  {
    	int count = content.getIndexCount(pathElement, Locale.ENGLISH);
    	for (int j=0;j<count;j++)
    		content.removeValue(pathElement, Locale.ENGLISH, 0);
    }
	
	
	public Boolean mustPublish() {
		return publish;
	}
	
	public synchronized void publish(boolean unlock, boolean recursivePublish) throws CmsException {
		CmsObject cms = getCmsObject();
		
		List<CmsResource> relatedResources = new ArrayList<CmsResource>();
		for (CmsResource resource : resources ) {
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


}
