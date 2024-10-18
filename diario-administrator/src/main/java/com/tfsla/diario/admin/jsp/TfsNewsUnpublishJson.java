package com.tfsla.diario.admin.jsp;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsResourceState;

import java.util.*;

import jakarta.servlet.jsp.PageContext;

import org.opencms.file.*;

import org.opencms.lock.CmsLock;
import org.opencms.main.*;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.report.CmsLogReport;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.ediciones.services.FreshnessService;
import com.tfsla.diario.model.TfsNoticia;
import com.tfsla.utils.CmsResourceUtils;

public class TfsNewsUnpublishJson {

	protected static final Log LOG = CmsLog.getLog(TfsNewsUnpublishJson.class);

	private JSONObject jsonRequest;
	private CmsObject cms;

	/** The current OpenCms users workplace settings. */
	private CmsWorkplaceSettings m_settings;

	private List<CmsResource> resources = new ArrayList<CmsResource>();

	private JSONObject content;
	public JSONArray jsonRelations = new JSONArray();
	CPMConfig configura;
	private PageContext pageContext;
	public CmsObject getCmsObject() {
		return this.cms;
	}

	public TfsNewsUnpublishJson(JSONObject jsonreq, CmsObject cmsObj, PageContext pgcontent) throws Exception {

		jsonRequest = jsonreq;

		cms = cmsObj;

		m_settings = CmsWorkplace.initWorkplaceSettings(getCmsObject(), m_settings, true);
		
		pageContext = pgcontent;

	}
	
	/**
	 * Proceso para validar los contenidos relacionadas al despublicar una noticia.
	 * @param newPath
	 * @param cmsObject
	 * @param pageContext
	 * @return
	 * @throws Exception 
	 */
	public JSONObject checkNew(String newPath, CmsObject cmsObject) throws Exception {
						
		JSONObject jsonItem = new JSONObject();
		
		@SuppressWarnings("unchecked")
		List<CmsRelation> relations = cmsObject.getRelationsForResource(newPath, CmsRelationFilter.ALL);

	
		for (CmsRelation rel : relations) {

			try {
				String relation = "";
		      	
		      	String rel1 = cmsObject.getRequestContext().removeSiteRoot(rel.getTargetPath());
				String rel2 = cmsObject.getRequestContext().removeSiteRoot(rel.getSourcePath());

				if (rel1.equals(newPath))
					relation = rel2;
				else
					relation = rel1;
				
				CmsResource  resourceRelation = cmsObject.readResource(relation);
				
				CmsResourceState estado = resourceRelation.getState();
		
				boolean canSelected = (estado.equals(CmsResourceState.STATE_NEW)) ? true : false;
		
				String typeRelation = OpenCms.getResourceManager().getResourceType(resourceRelation).getTypeName();
								
				if (typeRelation.indexOf("image") > -1) {
					
					JSONObject jsonRelation = checkContent(relation,"image", canSelected);
					jsonRelations.add(jsonRelation);
					
				} else if (typeRelation.indexOf("video") > -1) {
					
					JSONObject jsonRelation = checkContent(relation,"video",canSelected);
					jsonRelations.add(jsonRelation);
					
				} else if (typeRelation.indexOf("audio") > -1) {
					
					JSONObject jsonRelation = checkContent(relation,"audio",canSelected);
					jsonRelations.add(jsonRelation);
					
				}
				
				
			} catch (CmsException e) {
				e.printStackTrace();
			}
		}
		jsonItem.put("news",detailNew(newPath));
		jsonItem.put("relatios",jsonRelations);
		
		return jsonItem;
	}

	private boolean canSelected(String pathResource) throws CmsException {
		CmsObject cmsObject = this.getCmsObject();
		
		CmsResource resourceRelation = cmsObject.readResource(cmsObject.getRequestContext().removeSiteRoot(pathResource), CmsResourceFilter.IGNORE_EXPIRATION);
		CmsResourceState relState = resourceRelation.getState();
	
		return (relState.getState() == 2);
	}

	public JSONObject delete() {
		
		JSONObject result = new JSONObject();
		JSONArray resourcesStatus = new JSONArray();
		
		CmsObject cms = getCmsObject();

		JSONArray resources = jsonRequest.getJSONArray("resources");
		String status = "ok";
		for (int idx = 0; idx < resources.size(); idx++) {

			String path = resources.getString(idx);
			
			try {
				if(canSelected(path)) {
					String tempPath = CmsWorkplace.getTemporaryFileName(path);

					if (getCmsObject().existsResource(tempPath, CmsResourceFilter.ALL)) {
						CmsResourceUtils.forceLockResource(cms, tempPath);
						cms.deleteResource(tempPath, CmsResource.DELETE_PRESERVE_SIBLINGS);
					}
					
					
					CmsResourceUtils.forceLockResource(cms, path);
	
					getCmsObject().deleteResource(path, CmsResource.DELETE_PRESERVE_SIBLINGS);
	
					result.put("message","");
					result.put("path",path);
					result.put("status","ok");
				

					FreshnessService freshService = new FreshnessService();
					
					if (freshService.hasSetFreshness(jsonRequest.getJSONObject("authentication").getInt("publication"),jsonRequest.getJSONObject("authentication").getString("siteName"),path)){
						freshService.deleteFreshness(jsonRequest.getJSONObject("authentication").getInt("publication"),jsonRequest.getJSONObject("authentication").getString("siteName"),path);
					}
					
					
				}else {
					result.put("errorCode","999.028");
					result.put("path",path);
					result.put("status","fail");
				}
				resourcesStatus.add(result);

			} catch (Exception e) {

				LOG.error("Error trying delete " + path + " in site " + cms.getRequestContext().getSiteRoot()
						+ " (" + cms.getRequestContext().currentProject().getName() + ")", e);

				status = "error";
				
				result.put("message",e.getMessage());
				result.put("path", path);
				result.put("status", "error");
				
				try {
					CmsLock lockE = cms.getLock(path);

					if (!lockE.isUnlocked()) {
						CmsUUID userId = lockE.getUserId();
						CmsUser lockUser = cms.readUser(userId);

						result.put("lockby", lockUser.getFullName());
					}
				} catch (CmsException e2) {
				}

				resourcesStatus.add(result);
			}
		}
		
		result.put("resources", resourcesStatus);
		result.put("status", status);

		return result;
	}
	

	public JSONObject expire() {
		JSONObject result = new JSONObject();
		JSONArray resourcesStatus = new JSONArray();
		
		CmsObject cms = getCmsObject();

		JSONArray resources = jsonRequest.getJSONArray("resources");
		String status = "ok";
		
		for (int idx = 0; idx < resources.size(); idx++) {

			String path = resources.getString(idx);

			try {
				String tempPath = CmsWorkplace.getTemporaryFileName(path);

				if (getCmsObject().existsResource(tempPath, CmsResourceFilter.ALL)) {
					CmsResourceUtils.forceLockResource(cms, tempPath);
					cms.deleteResource(tempPath, CmsResource.DELETE_PRESERVE_SIBLINGS);
				}

				CmsResourceUtils.forceLockResource(cms, path);

				CmsFile file = cms.readFile(path, CmsResourceFilter.ALL);

				long expireDate = System.currentTimeMillis() - (60 * 60 * 1000);

				file.setDateExpired(expireDate);

				cms.lockResource(path);
				cms.writeFile(file);
				cms.unlockResource(path);

				resources.add(getCmsObject().readResource(path, CmsResourceFilter.ALL));
				
				result.put("message","");
				result.put("path",path);
				result.put("status","ok");
				
				resourcesStatus.add(result);

			} catch (Exception e) {

				LOG.error("Error trying delete " + path + " in site " + cms.getRequestContext().getSiteRoot()
						+ " (" + cms.getRequestContext().currentProject().getName() + ")", e);

				status = "error";
				
				result.put("message",e.getMessage());
				result.put("path", path);
				result.put("status", "error");
				
				try {
					CmsLock lockE = cms.getLock(path);

					if (!lockE.isUnlocked()) {
						CmsUUID userId = lockE.getUserId();
						CmsUser lockUser = cms.readUser(userId);

						result.put("lockby", lockUser.getFullName());
					}
				} catch (CmsException e2) {
				}

				resourcesStatus.add(result);
			}
			
		}

		result.put("resources", resourcesStatus);
		result.put("status", status);

		return result;
	}
	
	public synchronized void publish() throws CmsException {
		CmsObject cms = getCmsObject();

		CmsPublishList pList = OpenCms.getPublishManager().getPublishList(cms, resources, false);

		CmsLogReport report = new CmsLogReport(Locale.ENGLISH, this.getClass());
		OpenCms.getPublishManager().publishProject(cms, report, pList);

		if (pList.size() < 30)
			OpenCms.getPublishManager().waitWhileRunning();

	}
	

	/** Validamos las imagenes relacionadas a publicar. 
	 * @throws CmsException */
	private JSONObject checkContent(String path, String type, boolean canSelected) throws CmsException{
		
		CmsObject cmsObject = this.getCmsObject();
		
		String titleProperty = (type.equals("image")) ? "Description": "Title";
		String title = cmsObject.readPropertyObject(path, titleProperty, false).getValue("");
		
		JSONObject jsonRelated = new JSONObject();
		
		
		try {
			
			jsonRelated.put("canSelected",canSelected);
			jsonRelated.put("isLock",isLock(path));
			jsonRelated.put("title",title);
			jsonRelated.put("type",type);
			jsonRelated.put("path",path);
			
			
		} catch (Exception e) {
			
		}
		
		return jsonRelated;		
	}

	
	private JSONObject detailNew(String path) throws CmsException {

	
		JSONObject jsonNew = new JSONObject();
		JSONObject jsonError = new JSONObject();
		JSONArray jsonErrors = new JSONArray();
		boolean isMark = false;
		CmsObject cmsObject = this.getCmsObject();
		
		CmsFile file = cmsObject.readFile(path);
		I_CmsXmlDocument m_content = CmsXmlContentFactory.unmarshal(cmsObject, file,pageContext.getRequest());
		TfsNoticia noticia = new TfsNoticia(cmsObject, m_content,cmsObject.getRequestContext().getLocale(), pageContext);

		
		CmsLock lockFile = this.getCmsObject().getLock(path);
		boolean isLock = !lockFile.isUnlocked() ; // true si esta desbloqueado, false si esta bloqueado
		String isLockData = "";
		
		if (isLock){
			// si el usuario que lockea no es el usuario logueado
	       
		        CmsProject lockInProject = lockFile.getProject();
		        
        		if(CmsProject.PROJECT_TYPE_TEMPORARY == lockInProject.getType()){   
    	     	 		jsonError.put("code","999.014");
						jsonError.put("data","");
						jsonErrors.add(jsonError);
						isMark =  true;
        		}         
        		 if (!cmsObject.getRequestContext().currentUser().getName().equals(cmsObject.readUser(lockFile.getUserId()).getName())){
					isLockData = "BY " + cmsObject.readUser(lockFile.getUserId()).getFullName();
					
					jsonError.put("code","999.010");
					jsonError.put("data",isLockData);
					jsonErrors.add(jsonError);
					isMark = true;
				}
			
		}
			
		boolean hasDraft =  (cmsObject.existsResource(cmsObject.getRequestContext().removeSiteRoot(org.opencms.workplace.CmsWorkplace.getTemporaryFileName(path)), CmsResourceFilter.IGNORE_EXPIRATION));
		Long hasDraftDate = 0L;
		if (hasDraft){
			CmsResource resourceDraf = cmsObject.readResource(cmsObject.getRequestContext().removeSiteRoot(org.opencms.workplace.CmsWorkplace.getTemporaryFileName(path)), CmsResourceFilter.IGNORE_EXPIRATION);
			hasDraftDate = resourceDraf.getDateLastModified();
			
			jsonError.put("code","999.011");
			jsonError.put("data",hasDraftDate);
			jsonErrors.add(jsonError);
			isMark = true;
			
		}
		
		
		JSONObject jsonUser = new JSONObject();
		CmsUser userCms ;
		JSONArray jsonAuthors = new JSONArray();
		for (int i=1; i<=noticia.getAuthorscount(); i++){
			String internalUser = noticia.getAuthor("autor",i,"internalUser");
			try{
				if (!internalUser.equals("")){
					userCms =  cmsObject.readUser(internalUser);
					jsonUser.put("isInternalUser",true);
					jsonUser.put("description",userCms.getName());
					jsonUser.put("description",userCms.getFirstname() + ", " +userCms.getLastname());
					jsonUser.put("photo",(userCms.getAdditionalInfo("USER_PICTURE") !=null ) ? userCms.getAdditionalInfo("USER_PICTURE").toString():"noPicture");
					} else{
					jsonUser.put("isInternalUser",false);
					jsonUser.put("description","");
					jsonUser.put("description",noticia.getAuthor("autor",i,"nombre"));
					jsonUser.put("photo",noticia.getAuthor("autor",i,"foto"));
				}
				jsonAuthors.add(jsonUser);
			} catch (CmsException e) {	
				JSONObject error3 = new JSONObject();
				error3.put("errorCode","008.00Z"); 
				error3.put("error", e.getMessage());
				jsonAuthors.add(error3);
			}
		}
				
		jsonNew.put("isMark", isMark);
		if (isMark) {		
			jsonNew.put("errors",jsonErrors);
		}

		jsonNew.put("signatureUsers", jsonAuthors);
		jsonNew.put("signatureUserCount", noticia.getAuthorscount());
		jsonNew.put("section", noticia.getSection());
		jsonNew.put("title",noticia.getTitle());
		jsonNew.put("type", cmsObject.readPropertyObject(path, "newsType", false).getValue(""));
		
		
		return jsonNew;
	}
	

	private JSONObject isLock(String pathResource) throws CmsException {
 
		JSONObject jsonlock = new JSONObject();
		String fulFileNameNoTemp = this.getCmsObject().getRequestContext().removeSiteRoot(pathResource).replaceAll("~","");

		CmsLock lockFile = this.getCmsObject().getLock(fulFileNameNoTemp);
		boolean isLock = !lockFile.isUnlocked() ;
		String isLockData = "";
		
		jsonlock.put("value",isLock);
		jsonlock.put("code","");
		jsonlock.put("data","");
		
		if (isLock){
			
		        CmsProject lockInProject = lockFile.getProject();
		        
        		if(CmsProject.PROJECT_TYPE_TEMPORARY == lockInProject.getType()){   

        				jsonlock.put("code","999.014");
        				jsonlock.put("data",isLockData);
        		}         
        		 if (!this.getCmsObject().getRequestContext().currentUser().getName().equals(this.getCmsObject().readUser(lockFile.getUserId()).getName())){
					isLockData = this.getCmsObject().readUser(lockFile.getUserId()).getFullName();
					
					jsonlock.put("code","999.010");
					jsonlock.put("data",isLockData);
				}
			
		}
		return (jsonlock);
	}	
	
}
