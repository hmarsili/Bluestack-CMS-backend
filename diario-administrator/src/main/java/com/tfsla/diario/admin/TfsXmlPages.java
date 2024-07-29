package com.tfsla.diario.admin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.flex.CmsFlexController;
import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.report.CmsLogReport;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.page.CmsXmlPageFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.collections.CollectionFactory;
import com.tfsla.diario.admin.jsp.TfsMessages;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.opencms.exceptions.ProgramException;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;

public class TfsXmlPages {

	private TfsMessages Tfsmessage;
	private CmsFlexController m_controller;
    private HttpSession m_session;
    private String siteName;
    private TipoEdicion currentPublication;
    private String publication="";
    private String content;
    private String path;
    private Hashtable<String, String> properties = new Hashtable<String,String>();
    private I_CmsResourceType resourceType;
    private CPMConfig config;
    private static CmsObject cms;
    
    private static final String TFS_ADMIN_FAVS  = "TFS_ADMIN_FAVS";
	private static final String ID_FAV = "ID_FAV";
	private static final String LASTMODIFIED = "LASTMODIFIED";
	private static final String USER_ID = "USER_ID";
	private static final String SITE = "SITE";
	private static final String PUBLICATION = "PUBLICATION";
	private static final String PATH = "PATH";
	private static final String ICON = "ICON";
	private static final String DESCRIPTION = "DESCRIPTION";
    
	public TfsXmlPages(PageContext context, HttpServletRequest req, HttpServletResponse res) throws Exception {
		config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		m_controller = CmsFlexController.getController(req);
        m_session = req.getSession();
    	siteName = OpenCms.getSiteManager().getCurrentSite(m_controller.getCmsObject()).getSiteRoot();
    	currentPublication = (TipoEdicion)m_session.getAttribute("currentPublication");
    	cms = m_controller.getCmsObject();

    	if (currentPublication == null) {
        	TipoEdicionService tService = new TipoEdicionService();
    		currentPublication = tService.obtenerEdicionOnlineRoot(siteName);
    		m_session.setAttribute("currentPublication", currentPublication);
    	}
    	
    	publication = Integer.toString(currentPublication.getId());
		Tfsmessage = new TfsMessages(context,req,res);
    	resourceType = OpenCms.getResourceManager().getResourceType("xmlpage");
	}
	
	public String getTemplateDefault() {
		String template = config.getParam(siteName, publication, "freeStylePages", "templateDefault");
		return template;
	}
    
	public String getFolderDefault() {
		String folder = config.getParam(siteName, publication, "freeStylePages", "folderDefault");
		return folder;
	}
	
	public void setProperties(String key, String value){
		this.properties.put(key, value);
	}
	
	public void setContent( String contents) {
		this.content = contents;
	}
	
	public void setPath( String pathPage) {
		this.path = pathPage;
	}
	
	public String savePage() {
		String status = "Success";
		CmsObject cms = m_controller.getCmsObject();
		Locale locale = java.util.Locale.ENGLISH;
		
		try {
		    CmsFile file = cms.readFile(this.path);
		    this.getFileLock(cms, file, path);
			CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, file);
			xmlContent.setAutoCorrectionEnabled(true); 
			xmlContent.correctXmlStructure(cms);
			setXmlContentValue("element", this.content, locale, cms, xmlContent);
			file.setContents(xmlContent.marshal());
			cms.writeFile(file);
			CmsProperty prop = new CmsProperty();

			Iterator<String> keySetIterator = this.properties.keySet().iterator();
			while(keySetIterator.hasNext()) {
				  String key = keySetIterator.next();
				  prop.setName(key);
				  prop.setValue(properties.get(key), CmsProperty.TYPE_INDIVIDUAL);
				  cms.writePropertyObject(this.path,prop);
			}
		   
			cms.unlockResource(path);
		} catch (Exception e) {
			status = Tfsmessage.keyDefault("GUI_XMLPAGE_ERROR_0", "Error al guardar la pagina");
			CmsLog.getLog(this).error("Error al guardar la pagina ["+ e.getMessage() +"]", e);
			e.printStackTrace();
		}
	                      
		return status;
	}
	
	public String newPage(String path, String FileName) throws CmsLoaderException {
		String status = "Success";
		CmsObject cms = m_controller.getCmsObject();
		String resourcename = path+FileName;
		
		if (cms.existsResource(resourcename)) {
			status = Tfsmessage.keyDefault("GUI_XMLPAGE_ERROR_1", "Error - El archivo ya existe");
			CmsLog.getLog(this).error("Error - El archivo ya existe");
		} else {
			try {
				String content = this.getXmlPageContent(cms);
			    cms.createResource(resourcename, resourceType.getTypeId(), content.getBytes(), null);
			} catch (Exception e) {
				status = Tfsmessage.keyDefault("GUI_XMLPAGE_ERROR_2", "Error - No se pudo crear el archivo");
				CmsLog.getLog(this).error("Error - No se pudo crear el archivo ["+e.getMessage()+"]", e);
				e.printStackTrace();
			}
		}
	
		return status;
	}
	
	public String newSavePage(String pathFile, String FileName, boolean publish) throws CmsLoaderException {
		String status = "Success";
		String resourcename = "";
		CmsObject cms = m_controller.getCmsObject();
		if(pathFile != null) {
		    resourcename = pathFile + FileName;
		} else {
			resourcename = path;
		}
		if (cms.existsResource(resourcename)) {
			Locale locale = java.util.Locale.ENGLISH;
			
			try {
			    CmsFile file = cms.readFile(resourcename);
			    this.getFileLock(cms, file, resourcename);
			    CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, file);
				xmlContent.setAutoCorrectionEnabled(true); 
				xmlContent.correctXmlStructure(cms);
				setXmlContentValue("element", this.content, locale, cms, xmlContent);
				file.setContents(xmlContent.marshal());
				cms.writeFile(file);
				
				CmsProperty prop = new CmsProperty();
				Iterator<String> keySetIterator = this.properties.keySet().iterator();
				
				while(keySetIterator.hasNext()) {
					  String key = keySetIterator.next();
					  prop.setName(key);
					  prop.setValue(properties.get(key), CmsProperty.TYPE_INDIVIDUAL);
					  cms.writePropertyObject(resourcename, prop);
				}
			   
			   cms.unlockResource(resourcename);	
			   
			   if(publish) {
				   publishPage(resourcename);
			   }
			} catch (Exception e) {
				status = Tfsmessage.keyDefault("GUI_XMLPAGE_ERROR_0", "Error al guardar la pagina");
				CmsLog.getLog(this).error("Error al guardar la pagina ["+ e.getMessage() +"]", e);
				e.printStackTrace();
			}
		} else {
			try {
				String content = this.getXmlPageContent(cms);
			    cms.createResource(resourcename, this.resourceType.getTypeId(), content.getBytes(), null);
			    Locale locale = java.util.Locale.ENGLISH;
			    try {
				    CmsFile file = cms.readFile(resourcename);
				    this.getFileLock(cms, file, resourcename);
				    CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, file);
					xmlContent.setAutoCorrectionEnabled(true); 
					xmlContent.correctXmlStructure(cms);
					setXmlContentValue("element", this.content, locale, cms, xmlContent);
					file.setContents(xmlContent.marshal());
					cms.writeFile(file);
					CmsProperty prop = new CmsProperty();
				   
					Iterator<String> keySetIterator = this.properties.keySet().iterator();
					while(keySetIterator.hasNext()) {
						  String key = keySetIterator.next();
						  prop.setName(key);
						  prop.setValue(properties.get(key), CmsProperty.TYPE_INDIVIDUAL);
						  cms.writePropertyObject(resourcename,prop);
					}
				   
					cms.unlockResource(resourcename);
				} catch (CmsException e) {
					status = Tfsmessage.keyDefault("GUI_XMLPAGE_ERROR_0", "Error al guardar la pagina");
					CmsLog.getLog(this).error("Error al guardar la pagina ["+ e.getMessage() +"]", e);
					e.printStackTrace();
				}
			    
			    if(publish) {
			    	publishPage(resourcename);
			    }
			} catch (Exception e) {
				status = Tfsmessage.keyDefault("GUI_XMLPAGE_ERROR_2", "Error - No se pudo crear el archivo");
				CmsLog.getLog(this).error("Error - No se pudo crear el archivo ["+e.getMessage()+"]", e);
				e.printStackTrace();
			}
		}
		return status;
	}
	
	public void publishPage(String resourcename) throws CmsLoaderException{			
		CmsObject cms = m_controller.getCmsObject();
		try {
			List<CmsResource> relatedResources = getRelatedResources(resourcename);
			CmsPublishList pList = OpenCms.getPublishManager().getPublishList(cms, relatedResources, false);
			
			CmsLogReport report = new CmsLogReport(Locale.ENGLISH,this.getClass());
	        OpenCms.getPublishManager().publishProject(cms, report, pList);
	        if(pList.size()<30)
	        		OpenCms.getPublishManager().waitWhileRunning();

			//OpenCms.getPublishManager().publishResource(cms, resourcename);
		} catch (Exception e) {
			CmsLog.getLog(this).error("Error - No se pudo publicar ["+e.getMessage()+"]", e);
			e.printStackTrace();
		}
	}
	
	public List<CmsResource> getRelatedResources(String resourceName) throws CmsException {
		List<CmsResource> allresources = new ArrayList<CmsResource>();
		CmsObject cms = m_controller.getCmsObject();
		
		String path = String.valueOf(resourceName);
		allresources.add(cms.readFile(path,CmsResourceFilter.ALL));
		
		List<CmsResource> relatedResources = new ArrayList<CmsResource>();
		for (CmsResource resource : allresources ) {
			List<CmsResource> currentRelResources = addRelatedResourcesToPublish(resource,false,true, new ArrayList<CmsResource>());
			currentRelResources.removeAll(relatedResources);
			//currentRelResources.removeAll(allresources);
			relatedResources.addAll(currentRelResources);
		}
		relatedResources.addAll(allresources);
		return relatedResources;
	}
	
	public List<CmsResource> addRelatedResourcesToPublish(CmsResource resource, boolean unlock,
			boolean recursivePublish, List<CmsResource> resourcesList) {
		CmsObject cms = m_controller.getCmsObject();
		try {

			@SuppressWarnings("unchecked")
			List<CmsRelation> relations = cms.getRelationsForResource(cms.getSitePath(resource), CmsRelationFilter.ALL);
			for (CmsRelation relation : relations) {
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
					if ( estado.equals(CmsResourceState.STATE_UNCHANGED))
						continue;

					CmsLock lock = cms.getLock(res);

					if (!lock.isUnlocked() && unlock) {
						if (lock.getUserId().equals(cms.getRequestContext().currentUser().getId())) {
							cms.unlockResource(cms.getRequestContext().removeSiteRoot(rel));
						} else {
							continue;
						}
					}
					
					if (!resourcesList.contains(res))
						resourcesList.add(res);	
				} catch (CmsException e) {
					e.printStackTrace();
				}
			}
		} catch (CmsException e) {
			e.printStackTrace();
		}
		return resourcesList;
	}

	
	private String getXmlPageContent(CmsObject cms) throws CmsLoaderException, CmsXmlException {
		String xmlContent = CmsXmlPageFactory.createDocument(
				cms.getRequestContext().getLocale(),
				cms.getRequestContext().getEncoding()
		);
		return xmlContent;
	}
	
	private void getFileLock(CmsObject cms, CmsResource file, String resourcename) throws CmsException {
		CmsLock lock = cms.getLock(file);
		if(!lock.isUnlocked()) {
			cms.changeLock(resourcename);
			cms.unlockResource(resourcename);
		}
		cms.lockResource(resourcename);
	}
	
	private void setXmlContentValue(String keyString, String valueString, Locale locale, CmsObject cms, CmsXmlContent xmlContent) {
		if(valueString == null) return;
		
		I_CmsXmlContentValue value = xmlContent.getValue(keyString, locale);
		if(value != null) {
			value.setStringValue(cms, valueString);
		} else {
			xmlContent.addValue(cms, keyString, locale, 0).setStringValue(cms, valueString);
		}
	}
	
	public String saveFavLink(String path, String icon, String description) throws CmsException
	{
		String user = cms.getRequestContext().currentUser().getId().getStringValue();
		
		String status= "";
		
		try {
		
			QueryBuilder<Object> queryBuilder = new QueryBuilder<Object>(cms);
			queryBuilder.setSQLQuery("INSERT INTO "+TFS_ADMIN_FAVS+" ("+USER_ID+","+SITE+","+PUBLICATION+","+PATH+", "+ICON+","+DESCRIPTION+") " +
					"VALUES (?,?,?,?,?,?);");
	       
			queryBuilder.addParameter(user);
			queryBuilder.addParameter(siteName);
			queryBuilder.addParameter(publication);
			queryBuilder.addParameter(path);
			queryBuilder.addParameter(icon);
			queryBuilder.addParameter(description);
			
			queryBuilder.execute();
			
			status= "ok";
		
		}catch (Exception e) {
			status= "No se pudo guardar el link. Error: "+e.getMessage();
		}
		
		return status;
	}
	
	public String deleteFavLink(String idFav) throws CmsException
	{
		String status= "";
		
		try {
		
			QueryBuilder<Object> queryBuilder = new QueryBuilder<Object>(cms);
			queryBuilder.setSQLQuery("DELETE FROM "+TFS_ADMIN_FAVS+" WHERE "+ID_FAV+"=? ");
	       
			queryBuilder.addParameter(idFav);
			queryBuilder.execute();
			
			status= "ok";
		
		}catch (Exception e) {
			status= "No se pudo borrar el link. Error: "+e.getMessage();
		}
		
		return status;
	}
	
	public List<String> getFavPaths(){
		
		String user = cms.getRequestContext().currentUser().getId().getStringValue();
		
		QueryBuilder<List<String>> queryBuilder = new QueryBuilder<List<String>>(cms);
		
		queryBuilder.setSQLQuery("SELECT "+PATH+" FROM "+TFS_ADMIN_FAVS+" WHERE "+USER_ID+"=? and "+SITE+"=? and "+PUBLICATION+"=? ORDER BY "+LASTMODIFIED+" DESC");
		queryBuilder.addParameter(user);
		queryBuilder.addParameter(siteName);
		queryBuilder.addParameter(publication);
		
		ResultSetProcessor<List<String>> proc = getFavsListPathsProcessor();

		return queryBuilder.execute(proc);
	}
	
	private static ResultSetProcessor<List<String>> getFavsListPathsProcessor() {
		ResultSetProcessor<List<String>> proc = new ResultSetProcessor<List<String>>() {

			private List<String> results = CollectionFactory.createList();

			public void processTuple(ResultSet rs) {
				
				try {
					this.results.add(rs.getString(PATH));
				}
				catch (SQLException e) {
					throw ProgramException.wrap("error al intentar recuperar los links favoritos de la base", e);
				}
			}
			
			public List<String> getResult() {
				return this.results;
			}
		};
		return proc;
	}
	
	public List<TfsAdminFav> getFavs(){
		
		String user = cms.getRequestContext().currentUser().getId().getStringValue();
		
		QueryBuilder<List<TfsAdminFav>> queryBuilder = new QueryBuilder<List<TfsAdminFav>>(cms);
		
		queryBuilder.setSQLQuery("SELECT * FROM "+TFS_ADMIN_FAVS+" WHERE "+USER_ID+"=? and "+SITE+"=? and "+PUBLICATION+"=? ORDER BY "+LASTMODIFIED+" DESC");
		queryBuilder.addParameter(user);
		queryBuilder.addParameter(siteName);
		queryBuilder.addParameter(publication);
		
		ResultSetProcessor<List<TfsAdminFav>> proc = getFavsListProcessor();

		return queryBuilder.execute(proc);
	}
	
	private static ResultSetProcessor<List<TfsAdminFav>> getFavsListProcessor() {
		ResultSetProcessor<List<TfsAdminFav>> proc = new ResultSetProcessor<List<TfsAdminFav>>() {

			private List<TfsAdminFav> results = CollectionFactory.createList();

			public void processTuple(ResultSet rs) {
				
				try {
					TfsAdminFav adminFav = new TfsAdminFav();
					adminFav.setDescription(rs.getString(DESCRIPTION));
					adminFav.setPath(rs.getString(PATH));
					adminFav.setIcon(rs.getString(ICON));
					adminFav.setId(rs.getString(ID_FAV));
					adminFav.setLastmodified(rs.getTimestamp(LASTMODIFIED));
					
					String userId = rs.getString(USER_ID);
					
					CmsUUID userUUID = new CmsUUID(userId);
					
					try {
						cms.readUser(userUUID);
					} catch (CmsException e) {
						CmsLog.getLog(this).error("Error - No se pudo obtener el usuario para favoritos ["+e.getMessage()+"]", e);
					}
					
					adminFav.setUserId(rs.getString(USER_ID));
					
					this.results.add(adminFav);
					
				}
				catch (SQLException e) {
					throw ProgramException.wrap("error al intentar recuperar los links favoritos de la base", e);
				}
			}
			
			public List<TfsAdminFav> getResult() {
				return this.results;
			}
		};
		return proc;
	}
	
	
}