package com.tfsla.diario.admin.jsp;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.opencms.db.CmsPublishList;
import java.util.*;
import javax.servlet.jsp.PageContext;

import org.opencms.file.*;

import org.opencms.lock.CmsLock;
import org.opencms.main.*;
import org.opencms.report.CmsLogReport;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.utils.CmsResourceUtils;

public class TfsPollsAdminJson {

	protected static final Log LOG = CmsLog.getLog(TfsPollsAdminJson.class);

	private JSONObject jsonRequest;
	private CmsObject cms;

	/** The current OpenCms users workplace settings. */
	private CmsWorkplaceSettings m_settings;

	private List<CmsResource> resources = new ArrayList<CmsResource>();

	private String siteName;
	private TipoEdicion currentPublication;
	private String publication;


	public boolean isMark = false;	

	public String getSiteName() {
		return this.siteName;
	}

	public String getPublication() {
		return this.publication;
	}

	public CmsObject getCmsObject() {
		return this.cms;
	}

	public TfsPollsAdminJson(JSONObject jsonreq, CmsObject cmsObj) throws Exception {

		jsonRequest = jsonreq;

		cms = cmsObj;

		siteName = OpenCms.getSiteManager().getCurrentSite(getCmsObject()).getSiteRoot();

		m_settings = CmsWorkplace.initWorkplaceSettings(getCmsObject(), m_settings, true);

		if (jsonRequest.getJSONObject("authentication").has("publication")) {
			publication = jsonRequest.getJSONObject("authentication").getString("publication");
		} else {
			TipoEdicionService tService = new TipoEdicionService();

			currentPublication = tService.obtenerEdicionOnlineRoot(siteName);

			publication = "" + currentPublication.getId();
		}
	

	}
	
	public JSONObject retrievePolls() throws CmsException {

		JSONArray images = jsonRequest.getJSONArray("polls");
		
		JSONObject jsonResult = new JSONObject();
		JSONArray jsonErrors = new JSONArray();
		
		for (int idx = 0; idx < images.size(); idx++) {

			String path = images.getString(idx);
			try {
				resources.add(getCmsObject().readResource(path, CmsResourceFilter.ALL));
			}catch(CmsVfsResourceNotFoundException ex) {
				JSONObject jsonError = new JSONObject();				
				jsonError.put("path",path);
				jsonError.put("code","999.027");
				jsonError.put("data", ex.getMessage());
				jsonErrors.add(jsonError);
				isMark = true;
			}

		}
		if (isMark) {
			jsonResult.put("status","fail");
			jsonResult.put("errors",jsonErrors);
		}else {
			jsonResult.put("status","ok");
		}
		return jsonResult;
	}
	
	/**
	 * Proceso para validar las encuestas al publicar. Se valida los siguientes errores
	 * 
	 * - hasDraft
	 * - isLock 
 	 * - isSchedule
	 *
	 * @param pollPath
	 * @param cmsObject
	 * @param pageContext
	 * @return
	 * @throws Exception 
	 */

	public JSONObject checkPollToPublish(String pollPath, PageContext pageContext) throws Exception {

		JSONObject jsonItem = new JSONObject();
		JSONObject jsonError = new JSONObject();
		JSONArray jsonPollsError = new JSONArray();

		boolean isMark = false;
		
		try{
			
			boolean hasDraft =  (cms.existsResource(cms.getRequestContext().removeSiteRoot(org.opencms.workplace.CmsWorkplace.getTemporaryFileName(pollPath)), CmsResourceFilter.IGNORE_EXPIRATION));
			Long hasDraftDate = 0L;
			if (hasDraft){
				CmsResource resourceDraf = cms.readResource(cms.getRequestContext().removeSiteRoot(org.opencms.workplace.CmsWorkplace.getTemporaryFileName(pollPath)), CmsResourceFilter.IGNORE_EXPIRATION);
				hasDraftDate = resourceDraf.getDateLastModified();
				
				jsonError.put("code","999.011");
				jsonError.put("data",hasDraftDate);
				jsonPollsError.add(jsonError);
				isMark = true;
				
			}

			// Bloqueo de imagen. 
			CmsLock lockFile = cms.getLock(pollPath);
			boolean isLock = !lockFile.isUnlocked() ; // true si esta desbloqueado, false si esta bloqueado
			String isLockData = "";
			
			if (isLock){
				// si el usuario que lockea no es el usuario logueado
		       
			        CmsProject lockInProject = lockFile.getProject();
			        
			        if(CmsProject.PROJECT_TYPE_TEMPORARY == lockInProject.getType()){   
	        			
	        			String lockInProjectName = lockInProject.getName();
	        			
	        			String altText = "";
	     				   
	        			if( lockInProjectName.indexOf(" at ") > -1 ){	    	     	 	
	    	     	 		String scheduleDataspl[] = lockInProjectName.split(" at ");
	    	     	 		String scheduleDataFormat = scheduleDataspl[1].replaceAll("&","").replaceAll("#47;","/").replaceAll("/22 ","/2022 ").replaceAll("on ",""); //11/30/2022 on 0:571 in /sites/generic1
	    	     	 		String scheduleDataFormatSplt[] = scheduleDataFormat.split("in "); //11/30/2022 0:571 
	    	     	 		String valid12hSplt[] = scheduleDataFormatSplt[0].split(":");  // 571 
	    	     	 		if (valid12hSplt[1].length() > 2){ //tenemos el 3er digito que indica que es am ó pm.
	    	     	 			altText = scheduleDataFormatSplt[0].replaceAll("0 "," AM").replaceAll("1 "," PM");
		    	     	 	}else{
		    	     	 		altText = scheduleDataFormatSplt[0];
		    	     	 	}
		
						} else {
							altText = lockInProjectName;
						}
    	     	        
    	     	 		jsonError.put("code","999.014");
    	     	 		jsonError.put("data",altText);
						jsonPollsError.add(jsonError);
						isMark =  true;
						
	        		}         
	        		 if (!cms.getRequestContext().currentUser().getName().equals(cms.readUser(lockFile.getUserId()).getName())){
						isLockData = "BY " + cms.readUser(lockFile.getUserId()).getFullName();
						
						jsonError.put("code","999.010");
						jsonError.put("data",isLockData);
						jsonPollsError.add(jsonError);
						isMark = true;
					}
				
			}
			
			
			
			// expiracion de encuestas
			CmsResource resPoll = cms.readFile(pollPath);
			Long dateExpired = 	resPoll.getDateExpired();
			Long dateReleased = resPoll.getDateReleased();

			Date date = new Date();
			boolean isExpired = (dateExpired < date.getTime() || dateReleased > date.getTime()) ? true : false;
			Boolean hasExpireReleaseDates = (dateReleased != CmsResource.DATE_RELEASED_DEFAULT || dateExpired != CmsResource.DATE_EXPIRED_DEFAULT);
			
			if (isExpired){
				Long longDateExpired = (dateExpired == 9223372036854775807L) ? 0 : dateExpired ;
				JSONObject jsonExpireItem = new JSONObject();
				
				jsonExpireItem.put("availableDate",dateReleased);
				jsonExpireItem.put("expireDate",longDateExpired);
				
				
				jsonError.put("code","999.012");// contenido despublicado. (expiró el xxxx ó estuvo disponible desde xxxx y expiró xxxx)
				
				if (dateReleased > 0 && longDateExpired == 0) {
					
					jsonError.put("code","999.018"); // contenido despublicado (disponible a partir del xxxx)
	 
				}
				
				jsonError.put("data",jsonExpireItem);
				jsonPollsError.add(jsonError);
				isMark = true;
				
			}else if (hasExpireReleaseDates) {
				
				Long longDateExpired = (dateExpired == 9223372036854775807L) ? 0 : dateExpired ;
				JSONObject jsonExpireItem = new JSONObject();
				
				jsonExpireItem.put("availableDate",dateReleased);
				jsonExpireItem.put("expireDate",longDateExpired);
				
				if ( longDateExpired > 0) {
					
					jsonError.put("code","999.019"); //contenido que expirará ( disponible desde xxxx y expirará xxxx ó con fecha de expiración xxxx)
				
				}
				
				jsonError.put("data",jsonExpireItem);
				jsonPollsError.add(jsonError);
				isMark = true;

			}
		
			
		} catch (CmsException e) {
			jsonError.put("code","999.002");
			jsonError.put("data", e.getMessage());
			jsonPollsError.add(jsonError);
			isMark = true;
		}
		
		
		jsonItem.put("errores","");
		if (isMark)
		   	jsonItem.put("errores",jsonPollsError);
		
		jsonItem.put("isMark",isMark);
		jsonItem.put("path", pollPath);
		
	  
		
		
		return jsonItem;
	}
	
	

	public JSONObject expire() {
		JSONObject result = new JSONObject();
		JSONArray errorsJS = new JSONArray();
		CmsObject cms = getCmsObject();

		String pollPath = jsonRequest.getString("path");

		try {
			String tempPath = CmsWorkplace.getTemporaryFileName(pollPath);

			if (getCmsObject().existsResource(tempPath, CmsResourceFilter.ALL)) {
				CmsResourceUtils.forceLockResource(cms, tempPath);
				cms.deleteResource(tempPath, CmsResource.DELETE_PRESERVE_SIBLINGS);
			}

			CmsResourceUtils.forceLockResource(cms, pollPath);

			CmsFile file = cms.readFile(pollPath, CmsResourceFilter.ALL);

			long expireDate = System.currentTimeMillis() - (60 * 60 * 1000);

			file.setDateExpired(expireDate);

			cms.lockResource(pollPath);
			cms.writeFile(file);
			cms.unlockResource(pollPath);

			resources.add(getCmsObject().readResource(pollPath, CmsResourceFilter.ALL));

			result.put("status", "ok");

		} catch (Exception e) {

			LOG.error("Error trying to save resource " + pollPath + " in site " + cms.getRequestContext().getSiteRoot()
					+ " (" + cms.getRequestContext().currentProject().getName() + ")", e);

			result.put("status", "error");

			JSONObject error = new JSONObject();
			
			if (e.getMessage().indexOf("Error reading resource from path") > -1)
				error.put("errorCode","999.027");
			
			error.put("path", pollPath);
			error.put("message", e.getMessage());

			try {
				CmsLock lockE = cms.getLock(pollPath);

				if (!lockE.isUnlocked()) {
					CmsUUID userId = lockE.getUserId();
					CmsUser lockUser = cms.readUser(userId);

					error.put("lockby", lockUser.getFullName());
					error.put("errorCode","999.010");
				}
			} catch (CmsException e2) {
			}

			errorsJS.add(error);
		}

		result.put("errors", errorsJS);

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
	
	public Locale getLocale() {

		return m_settings.getUserSettings().getLocale();
	}

	protected String getFileEncoding(CmsObject cms, String filename) {

		try {
			return cms.readPropertyObject(filename, CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, true)
					.getValue(OpenCms.getSystemInfo().getDefaultEncoding());
		} catch (CmsException e) {
			return OpenCms.getSystemInfo().getDefaultEncoding();
		}
	}
	
}
