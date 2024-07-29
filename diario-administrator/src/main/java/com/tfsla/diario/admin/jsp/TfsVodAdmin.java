package com.tfsla.diario.admin.jsp;

import java.text.ParseException;
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
import org.opencms.file.CmsProperty;
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
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.admin.widgets.TfsCalendarWidget;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.utils.CmsResourceUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TfsVodAdmin {

	 protected static final Log LOG = CmsLog.getLog(TfsVodAdmin.class);
	 
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

    public TfsVodAdmin (PageContext context, HttpServletRequest req, HttpServletResponse res) throws Exception {
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
		moduleConfigName = "vod";
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
				int tipoTemporada = OpenCms.getResourceManager().getResourceType("temporada").getTypeId();
				int tipoEpisodio = OpenCms.getResourceManager().getResourceType("episodio").getTypeId();
	
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
	   				boolean esTemporada = (tipo == tipoTemporada);
	   				boolean esEpisodio = (tipo == tipoEpisodio);
	   				
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
	
	   				if (esVideoOrigen && esVideo) {
						if (publishRelatedContent)
							if (!resourcesList.contains(res))
								resourcesList.add(res);	
	   				}
	   				else if (!esNoticia) {
						if (publishRelatedContent)
							if (!resourcesList.contains(res)) {
								resourcesList.add(res);	
								if (esTemporada && publishRelatedContent)
									addRelatedResourcesToPublish(res, unlock, true, resourcesList);
								if (esEpisodio && publishRelatedContent)
									addRelatedResourcesToPublish(res, unlock, true, resourcesList);
							}
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
		int resourcesCount = request.getParameterValues("path[]").length;
		
		for (int idx=0;idx<resourcesCount;idx++) {
			String path = String.valueOf(request.getParameterValues("path[]")[idx]);
			String title = String.valueOf(request.getParameterValues("title[]")[idx]);
			String keywords = String.valueOf(request.getParameterValues("keywords[]")[idx]);	
			String actores = String.valueOf(request.getParameterValues("actores[]")[idx]);
			String creadores = String.valueOf(request.getParameterValues("creadores[]")[idx]);
			
            String directores = String.valueOf(request.getParameterValues("directores[]")[idx]);
           // String orden  = String.valueOf(request.getParameterValues("orden[]")[idx]);
            String calificacion = String.valueOf(request.getParameterValues("classification[]")[idx]);
            
            String strIsSetRelease = String.valueOf(request.getParameterValues("isSetRelease[]")[idx]);
            String strIsSetExpire = String.valueOf(request.getParameterValues("isSetExpire[]")[idx]);

            boolean isExpirationReleaseDefined = (!strIsSetRelease.equals("||!!undefined!!||"));

            boolean isSetRelease = Boolean.valueOf(strIsSetRelease);
            boolean isSetExpire = Boolean.valueOf(strIsSetExpire);

            //boolean isExpirationReleaseDefined = isSetRelease || isSetExpire;
            
            long releaseDate = CmsResource.DATE_RELEASED_DEFAULT;
            long expireDate = CmsResource.DATE_EXPIRED_DEFAULT;
			
            try {
            	String strReleaseDate = String.valueOf(request.getParameterValues("releaseDate[]")[idx]);
            	if (!strReleaseDate.equals("||!!undefined!!||"))
            		releaseDate = TfsCalendarWidget.getCalendarDate(m_messages, strReleaseDate, true);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}

            try {	
            	String strExpireDate = String.valueOf(request.getParameterValues("expireDate[]")[idx]);
            	if (!strExpireDate.equals("||!!undefined!!||"))
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
				if (!keywords.equals("||!!undefined!!||"))
					content.getValue("claves", Locale.ENGLISH).setStringValue(cms, keywords);
				if (!actores.equals("||!!undefined!!||"))
					content.getValue("personas", Locale.ENGLISH).setStringValue(cms, actores);
				if (!creadores.equals("||!!undefined!!||"))
					content.getValue("creadores", Locale.ENGLISH).setStringValue(cms, creadores);
				if (!directores.equals("||!!undefined!!||"))
					content.getValue("directores", Locale.ENGLISH).setStringValue(cms, directores);	
				//if (!orden.equals("||!!undefined!!||"))
				//	content.getValue("orden", Locale.ENGLISH).setStringValue(cms, orden);	
				if (!calificacion.equals("||!!undefined!!||"))
					content.getValue("calificacion", Locale.ENGLISH).setStringValue(cms, calificacion);	
				
				
				file.setContents(content.marshal());
				cms.writeFile(file);	
				

	            if (isExpirationReleaseDefined)
	            	modifyResourceAvailability(file, releaseDate, expireDate, isSetRelease, isSetExpire);
				
	            CmsResourceUtils.unlockResource(cms,path, false);
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
	
	public List<CmsResource> getRelatedResourcesForPublish() throws CmsException {
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
	
	public JSONArray getRelatedResource(String path) {
		CmsObject cms = getCmsObject();
		JSONObject result = new JSONObject();
		JSONArray list = new JSONArray();
		
		
		try {
			CmsResource resource = cms.readResource(cms.getRequestContext().removeSiteRoot(path));
			
			int tipoTemporada = OpenCms.getResourceManager().getResourceType("temporada").getTypeId();
			int tipoSerie = OpenCms.getResourceManager().getResourceType("serie").getTypeId();
			int tipoCapitulo = OpenCms.getResourceManager().getResourceType("episodio").getTypeId();
			
			int resourceType = resource.getTypeId();
			
			@SuppressWarnings("unchecked")
			List<CmsRelation> relations = cms.getRelationsForResource(cms.getSitePath(resource), CmsRelationFilter.ALL);
			
	   		for ( CmsRelation relation : relations) {
	   			
	   			try {
	   				String rel1 = relation.getSourcePath();
	   				String rel2 = relation.getTargetPath();
	
	   				String rel = "";
	   				if (rel1.equals(resource.getRootPath()))
	   					rel = rel2;
	   				else
	   					rel = rel1;
	   			
	   				CmsResource res = cms.readResource(cms.getRequestContext().removeSiteRoot(rel),CmsResourceFilter.ALL);
				
	   				int tipo = res.getTypeId();
				
	   				boolean esTemporada = (tipo == tipoTemporada);
	   				boolean esSerie = (tipo == tipoSerie);
			
	   				if ((resourceType == tipoTemporada && esSerie)  || (resourceType == tipoCapitulo && esTemporada)) {
	   					CmsProperty   prop = cms.readPropertyObject(res, "Title", false);  
	   					String title = (prop != null) ? prop.getValue() : null; 
	   					
	   					result.put("path",rel);
	   					result.put("title",title);
	   					
	   					list.add(result);
	   				} 
					
				
			} catch (CmsException e) {
				 e.printStackTrace();
		      	}
			}
		}
		catch (CmsException e) {
			 e.printStackTrace();
		}
	   	return list;
	}
	
	public JSONObject saveRelation() {
		JSONObject result = new JSONObject();
		JSONArray errorsJS = new JSONArray();
		CmsObject cms = getCmsObject();
		String resourceParent = request.getParameter("path");
		String element = request.getParameter("element");
		String parentTitle  = request.getParameter("parentTitle");
		String parentPath = request.getParameter("parentPath");
		String reset = request.getParameter("reset");
		String removeOldPath = request.getParameter ("removeOld");
		
		if (!resourceParent.equals("")) {
	        try {		
					String tempPath = CmsWorkplace.getTemporaryFileName(resourceParent);
	            	
	            	if (getCmsObject().existsResource(tempPath, CmsResourceFilter.ALL)) {
	            		CmsResourceUtils.forceLockResource(cms,tempPath);
	            		cms.deleteResource(tempPath, CmsResource.DELETE_PRESERVE_SIBLINGS);
	            	}
	            	
	            	int vodElemensCount = cantidadDeElementosVod(cms, cms.readResource(resourceParent, CmsResourceFilter.ALL), element);
					
	            	CmsResourceUtils.forceLockResource(cms,resourceParent);
			
					CmsFile file = cms.readFile(resourceParent,CmsResourceFilter.ALL);
					
					CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
					content.setAutoCorrectionEnabled(true);
					content.correctXmlStructure(cms);
					
					if (request.getParameter("vodPath")!=null  && request.getParameter("vodTitle")!=null && element !=null) {
						for (int j=1;j<=vodElemensCount;j++){
							String xmlName =element +"[" + j + "]";
							I_CmsXmlContentValue value = content.getValue(xmlName, Locale.ENGLISH);
							
							if(value==null){
								content.addValue(cms, element, Locale.ENGLISH,j-1);
								content.getValue(element +"[" + j + "]",
												Locale.ENGLISH).setStringValue(cms,  request.getParameter("vodPath"));
							
								
							} else {
								I_CmsXmlContentValue path = content.getValue(element + "[" + j + "]",  Locale.ENGLISH);
								if (path.getStringValue(cms).equals( request.getParameter("vodPath"))) {
									if (reset != null && reset.equals("true"))
										content.removeValue(element, Locale.ENGLISH, j-1);
									break;
								}
							}
						}
					
						file.setContents(content.marshal());
						cms.writeFile(file);	
						cms.unlockResource(resourceParent);
					}	
				} catch (Exception e) {
					
					LOG.error("Error trying to add relation to  resource " + resourceParent + " in site " + cms.getRequestContext().getSiteRoot() + " (" + cms.getRequestContext().currentProject().getName() + ")", e);
					
					result.put("status", "error");
					
					JSONObject error = new JSONObject(); 
	            	error.put("path", resourceParent);
	            	error.put("message",e.getMessage());
	            	
					try{
						CmsLock lockE = cms.getLock(resourceParent);
		           	
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
		if (removeOldPath != null && !removeOldPath.equals("")) {
			try {		
				String tempPath = CmsWorkplace.getTemporaryFileName(removeOldPath);
        	
	        	if (getCmsObject().existsResource(tempPath, CmsResourceFilter.ALL)) {
	        		CmsResourceUtils.forceLockResource(cms,tempPath);
	        		cms.deleteResource(tempPath, CmsResource.DELETE_PRESERVE_SIBLINGS);
	        	}
        	
	        	int vodElemensCount = cantidadDeElementosVod(cms, cms.readResource(removeOldPath, CmsResourceFilter.ALL), element);
			
	        	CmsResourceUtils.forceLockResource(cms,removeOldPath);
	
	        	CmsFile file = cms.readFile(removeOldPath,CmsResourceFilter.ALL);
			
				CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
				content.setAutoCorrectionEnabled(true);
				content.correctXmlStructure(cms);
				
				if (request.getParameter("vodPath")!=null   && element !=null) {
					for (int j=1;j<=vodElemensCount;j++){
						String xmlName =element +"[" + j + "]";
						I_CmsXmlContentValue value = content.getValue(xmlName, Locale.ENGLISH);
					
						if(value!=null){
							I_CmsXmlContentValue path = content.getValue(element + "[" + j + "]",  Locale.ENGLISH);
							if (path.getStringValue(cms).equals( request.getParameter("vodPath"))) {
								content.removeValue(element, Locale.ENGLISH, j-1);
								break;
							}
						}
					}
			
					file.setContents(content.marshal());
					cms.writeFile(file);	
			
					cms.unlockResource(removeOldPath);
				}	
		
			} catch (Exception e) {
			
				LOG.error("Error trying to add remove to  resource " + removeOldPath + " in site " + cms.getRequestContext().getSiteRoot() + " (" + cms.getRequestContext().currentProject().getName() + ")", e);
			
				result.put("status", "error");
				
				JSONObject error = new JSONObject(); 
	        	error.put("path", resourceParent);
	        	error.put("message",e.getMessage());
	        	
				
				errorsJS.add(error);
			}
		}	
		
		if (request.getParameter("vodPath")!=null  && request.getParameter("vodTitle")!=null) {
			Boolean hasValue = !parentPath.equals("");
			
			String subParentPath = "";
			if (parentPath.split("-").length >1) {
				subParentPath = parentPath.split("-")[1];
				parentPath = parentPath.split("-")[0];
			}
			
			String subParentTitle = "";
			if (parentTitle.split("-").length >1) {
				subParentTitle = parentTitle.split("-")[1];
				parentTitle = parentTitle.split("-")[0];
			}
			
			try {
		    	CmsResourceUtils.forceLockResource(cms,request.getParameter("vodPath"));
		        CmsProperty prop = new CmsProperty();
		        prop.setName("serie-path");
		        prop.setValue(parentPath, CmsProperty.TYPE_INDIVIDUAL);
		        
		        cms.writePropertyObject(request.getParameter("vodPath"),prop);
		  
		        CmsProperty prop1 = new CmsProperty();
		        prop1.setName("serie-title");
		        prop1.setValue(parentTitle, CmsProperty.TYPE_INDIVIDUAL);
		        
		        cms.writePropertyObject(request.getParameter("vodPath"),prop1);
		        
		        if (!subParentPath.equals("") && !subParentTitle.equals("")) {
		        	CmsProperty prop2 = new CmsProperty();
		            prop2.setName("temporada-path");
		            prop2.setValue(subParentPath, CmsProperty.TYPE_INDIVIDUAL);
		            
		            cms.writePropertyObject(request.getParameter("vodPath"),prop2);
		            
		            CmsProperty prop4 = new CmsProperty();
		            prop4.setName("temporada-title");
		            prop4.setValue(subParentTitle, CmsProperty.TYPE_INDIVIDUAL);
		            
		            cms.writePropertyObject(request.getParameter("vodPath"),prop4);
		        } 
		        
		        CmsProperty prop3 = new CmsProperty();
		        prop3.setName("hasVodRelated");
		        prop3.setValue(hasValue.toString(), CmsProperty.TYPE_INDIVIDUAL);
		        
		        cms.writePropertyObject(request.getParameter("vodPath"),prop3);
		        
		        CmsResourceUtils.unlockResource(cms,request.getParameter("vodPath"), false);
		        result.put("status", "ok");
			} catch (Exception e) {
				
				LOG.error("Error trying to add property to  resource " + request.getParameter("vodPath") + " in site " + cms.getRequestContext().getSiteRoot() + " (" + cms.getRequestContext().currentProject().getName() + ")", e);
				
				result.put("status", "error");
				
				JSONObject error = new JSONObject(); 
	        	error.put("path", request.getParameter("vodPath"));
	        	error.put("message",e.getMessage());
	        	
				errorsJS.add(error);
			}	
		}
	       result.put("status", errorsJS);
		return result;
	}
	
	
	public int cantidadDeElementosVod(CmsObject cmsObject, CmsResource recurso, String element) {
		
		int nroRef = 1;
		CmsFile contentFile;
		try {
			contentFile = cmsObject.readFile(recurso);
		
			CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmsObject, contentFile);

			if (!cmsObject.getRequestContext().currentProject().isOnlineProject()) {
				try {	
					CmsXmlUtils.validateXmlStructure(contentFile.getContents(), new CmsXmlEntityResolver(cmsObject));
				} catch (CmsException e) {
					CmsResourceUtils.forceLockResource(cmsObject, cmsObject.getSitePath(recurso));
					content.setAutoCorrectionEnabled(true);
					content.correctXmlStructure(cmsObject);
					CmsResourceUtils.unlockResource(cmsObject, cmsObject.getSitePath(recurso), false);
				}
			}
		
			
			String xmlName =element + "[" + nroRef + "]";
			I_CmsXmlContentValue value = content.getValue(xmlName, Locale.ENGLISH);

			while (value!=null) {

				xmlName =element+"[" + nroRef + "]";
				value = content.getValue(xmlName, Locale.ENGLISH);

				if (value!=null) {
				String pathVod = value.getStringValue(cmsObject);

				//if (pathVod!=null && pathVod.length()>0)
				//	nroVod++;
			}
			nroRef++;
			xmlName =element+"[" + nroRef + "]";
			value = content.getValue(xmlName, Locale.ENGLISH);

			}
		} catch (CmsException e) {
			LOG.error("Error al intentar determinar la cantidad de imagenes en la noticia " + recurso.getRootPath() ,e);
		}
	
		return nroRef;
	}
	
	
	public boolean setChaptersSeasonRelation(String path, String serieTitle, String seriePath) {
		CmsObject cms = getCmsObject();
		
		
		try {
			CmsResource resource = cms.readResource(cms.getRequestContext().removeSiteRoot(path), CmsResourceFilter.ALL);
			
			int tipoCapitulo = OpenCms.getResourceManager().getResourceType("episodio").getTypeId();
			
			
			@SuppressWarnings("unchecked")
			List<CmsRelation> relations = cms.getRelationsForResource(cms.getSitePath(resource), CmsRelationFilter.ALL);
			
	   		for ( CmsRelation relation : relations) {
	   			
	   			try {
	   				String rel1 = relation.getSourcePath();
	   				String rel2 = relation.getTargetPath();
	
	   				String rel = "";
	   				if (rel1.equals(resource.getRootPath()))
	   					rel = rel2;
	   				else
	   					rel = rel1;
	   			
	   				CmsResource res = cms.readResource(cms.getRequestContext().removeSiteRoot(rel),CmsResourceFilter.ALL);
				
	   				int tipo = res.getTypeId();
				
	   				if ( tipo == tipoCapitulo) {
	   					CmsResourceUtils.forceLockResource(cms,cms.getRequestContext().removeSiteRoot(rel));
	   		         
	   					CmsProperty prop = new CmsProperty();
	   					prop.setName("serie-title");
	   					prop.setValue(serieTitle, CmsProperty.TYPE_INDIVIDUAL);
	   					
	   				  	cms.writePropertyObject(cms.getRequestContext().removeSiteRoot(rel),prop);
	   				  	
	   				  	CmsProperty prop1 = new CmsProperty();
	   					prop1.setName("serie-path");
	   					prop1.setValue(seriePath, CmsProperty.TYPE_INDIVIDUAL);
	   					
	   				  	cms.writePropertyObject(cms.getRequestContext().removeSiteRoot(rel),prop1);
	   				  	
	   				  	if (serieTitle.equals("")) {
	   				  		CmsProperty prop3 = new CmsProperty();
	   				  		prop3.setName("hasVodRelated");
	   	                	prop3.setValue("false", CmsProperty.TYPE_INDIVIDUAL);
	   	                
	   	                	cms.writePropertyObject(cms.getRequestContext().removeSiteRoot(rel),prop3);
	   	                }
	   				  CmsResourceUtils.unlockResource(cms, cms.getRequestContext().removeSiteRoot(rel), false);
	   				  	
	   				} 
			} catch (CmsException e) {
				LOG.error("Error modificando properties de recursos relacionados a " + path + " in site " + cms.getRequestContext().getSiteRoot() + 
						" (" + cms.getRequestContext().currentProject().getName() + ")", e);
				return false;
		      	}
			}
		}
		catch (CmsException e) {
			LOG.error("Error modificando properties de  recursos relacionados a " + path + " in site " + cms.getRequestContext().getSiteRoot() + 
					" (" + cms.getRequestContext().currentProject().getName() + ")", e);
			return false;
		}
	   	return true;
	}
	
	public boolean isDefaultForcePublishEnabled()
    {
    	return config.getParam(siteName, publication, "adminNewsConfiguration", "forcePublishDefault","enabled").toLowerCase().equals("enabled");
    }

	public boolean isDefaultPublishRelatedContentEnabled()
    {
    	return config.getParam(siteName, publication, "adminNewsConfiguration", "publishRelatedContentDefault","enabled").toLowerCase().equals("enabled");
    }
	
	public Boolean isForcePublish() {
		return forcePublish;
	}
	
}

