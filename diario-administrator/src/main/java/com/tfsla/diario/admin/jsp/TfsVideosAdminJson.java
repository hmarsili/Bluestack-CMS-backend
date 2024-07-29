package com.tfsla.diario.admin.jsp;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
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
import com.tfsla.diario.terminos.data.*;
import com.tfsla.diario.terminos.model.*;
import com.tfsla.utils.CmsResourceUtils;

public class TfsVideosAdminJson {

	protected static final Log LOG = CmsLog.getLog(TfsVideosAdminJson.class);

	private JSONObject jsonRequest;
	private CmsObject cms;

	/** The current OpenCms users workplace settings. */
	private CmsWorkplaceSettings m_settings;

	private List<CmsResource> resources = new ArrayList<CmsResource>();

	private String siteName;
	private TipoEdicion currentPublication;
	private String publication;
	private CPMConfig config;
	
	private String categoryWarning;
	private String categoryCritial;

	public boolean isMark = false;	

	CPMConfig configura;

	public String getSiteName() {
		return this.siteName;
	}

	public String getPublication() {
		return this.publication;
	}

	public CmsObject getCmsObject() {
		return this.cms;
	}

	public TfsVideosAdminJson(JSONObject jsonreq, CmsObject cmsObj) throws Exception {

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

		configura = CmsMedios.getInstance().getCmsParaMediosConfiguration();		
//		categoryWarning = configura.getParam(siteName, publication, "amzContentModeration", "categoryWarning", "");
//		categoryCritial = configura.getParam(siteName, publication, "amzContentModeration", "categoryCritial", "");


	}

	public JSONObject retrieveVideos() throws CmsException {

		JSONArray videos = jsonRequest.getJSONArray("videos");
		
		JSONObject jsonResult = new JSONObject();
		JSONArray jsonErrors = new JSONArray();
		
		for (int idx = 0; idx < videos.size(); idx++) {

			String path = videos.getString(idx);
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
	 * Proceso para validar los videos al publicar. Se valida los siguientes errores
	 * 
	 * - hasDraft
	 * - isLock 
 	 * - isSchedule
	 *
	 * @param videosPath
	 * @param cmsObject
	 * @param pageContext
	 * @return
	 * @throws Exception 
	 */

	public JSONObject checkVideoToPublish(String videosPath, PageContext pageContext) throws Exception {

		JSONObject jsonItem = new JSONObject();
		JSONObject jsonError = new JSONObject();
		JSONArray jsonErrors = new JSONArray();

		boolean isMark = false;
		String videoTitle  = "";
		
		try {
			
		//Sin titulo
		
		CmsProperty prop =  cms.readPropertyObject(videosPath, "Title", false);
		videoTitle = (prop != null) ? prop.getValue() : "";
	
	
		if (videoTitle == null || videoTitle.equals("") || videoTitle.equals("||!!undefined!!||")) {
			jsonError.put("code","999.024");
			jsonError.put("data",videosPath);
			jsonErrors.add(jsonError);
			isMark = true;
			videoTitle = "";
		}
		
		//Sin Descripcion
		String videoDes  = "";
		prop =  cms.readPropertyObject(videosPath, "Description", false);
		videoDes = (prop != null) ? prop.getValue() : "";

	
		if (videoDes == null || videoDes.equals("") || videoDes.equals("||!!undefined!!||")) {
			jsonError.put("code","999.025");
			jsonError.put("data",videosPath);
			jsonErrors.add(jsonError);
			isMark = true;
			videoDes = "";
		}
	
		//Sin imagen preview
		String videoImgPrev  = "";
		prop =  cms.readPropertyObject(videosPath, "prevImage", false);
		videoImgPrev = (prop != null) ? prop.getValue() : "";

	
		if (videoImgPrev == null || videoImgPrev.equals("") || videoImgPrev.equals("||!!undefined!!||")) {
			jsonError.put("code","999.026");
			jsonError.put("data",videosPath);
			jsonErrors.add(jsonError);
			isMark = true;
			videoImgPrev = "";
		}
		
		// Tags Desaprobados
		String videoTags  = "";
		prop =  cms.readPropertyObject(videosPath, "Keywords", false);
		videoTags = (prop != null) ? prop.getValue() : "";

		if (videoTags != null && !videoTags.equals("")) {
			String termsTypeName = configura.getParam(siteName, publication, "terms", "termsType", "tags");
			
			TermsTypesDAO ttDAO = new TermsTypesDAO ();
		 	
			TermsTypes oTermTypes = null;
			Long type = new Long(1);

			try {
				oTermTypes = ttDAO.getTermType(termsTypeName);
				type = oTermTypes.getId_termType();
			} catch (Exception e) {
				//Por defecto se usa el tags
				//CmsLog.getLog(this).error("No puede obtener el tipo de tag",e);
			}
			boolean isValidTags = isValidTags(videoTags, type);
			if (!isValidTags) {
					JSONObject jsonRelatedOther = new JSONObject();
					jsonRelatedOther.put("title", "");
					jsonRelatedOther.put("type","otros");
					jsonRelatedOther.put("isMark",true);
					
					jsonErrors.add(jsonRelatedOther);
					isMark = true;
			}
		}
			
		try{
			
			boolean hasDraft =  (cms.existsResource(cms.getRequestContext().removeSiteRoot(org.opencms.workplace.CmsWorkplace.getTemporaryFileName(videosPath)), CmsResourceFilter.IGNORE_EXPIRATION));
			Long hasDraftDate = 0L;
			if (hasDraft){
				CmsResource resourceDraf = cms.readResource(cms.getRequestContext().removeSiteRoot(org.opencms.workplace.CmsWorkplace.getTemporaryFileName(videosPath)), CmsResourceFilter.IGNORE_EXPIRATION);
				hasDraftDate = resourceDraf.getDateLastModified();
				
				jsonError.put("code","999.011");
				jsonError.put("data",hasDraftDate);
				jsonErrors.add(jsonError);
				isMark = true;
				
			}

			// Bloqueo. 
			CmsLock lockFile = cms.getLock(videosPath);
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
						jsonErrors.add(jsonError);
						isMark =  true;
						
	        		}         
	        		 if (!cms.getRequestContext().currentUser().getName().equals(cms.readUser(lockFile.getUserId()).getName())){
						isLockData = "BY " + cms.readUser(lockFile.getUserId()).getFullName();
						
						jsonError.put("code","999.010");
						jsonError.put("data",isLockData);
						jsonErrors.add(jsonError);
						isMark = true;
					}
				
			}
			
			
			// expiracion
			CmsResource resVideo = cms.readFile(videosPath);
			Long dateExpired = 	resVideo.getDateExpired();
			Long dateReleased = resVideo.getDateReleased();

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
				jsonErrors.add(jsonError);
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
				jsonErrors.add(jsonError);
				isMark = true;

			}
			
			/** Seguridad de marca.
			prop =  cms.readPropertyObject(videoPath, "UnsafeLabels", false);
			String UnsafeLabels = (prop != null) ? prop.getValue() : null; 
			
			if (UnsafeLabels != null){
				
				LOG.debug("UnsafeLabels" + UnsafeLabels);				
				if (UnsafeLabels.indexOf(",") > 0){
					String[] UnsafeLabelsSpl = UnsafeLabels.split(", ");
		
					for (String UnsafeLabel : UnsafeLabelsSpl) {
						LOG.debug("categoryWarning" + categoryWarning);
						LOG.debug("categoryCritial" + categoryCritial);
						if(categoryWarning.contains(UnsafeLabel) || categoryCritial.contains(UnsafeLabel) ){
							jsonError.put("code","009.008");
							jsonError.put("data","");
							jsonErrors.add(jsonError);
							isMark = true;
						}
					}
				} else {
					LOG.debug("categoryWarning" + categoryWarning);
					LOG.debug("categoryCritial" + categoryCritial);
					if(categoryWarning.contains(UnsafeLabels) || categoryCritial.contains(UnsafeLabels) ){
						jsonError.put("code","009.008");
						jsonError.put("data","");
						jsonErrors.add(jsonError);
						isMark = true;
					}
				}
				
			}
			*/
			
		} catch (CmsException e) {
			jsonError.put("code","999.002");
			jsonError.put("data", e.getMessage());
			jsonErrors.add(jsonError);
			isMark = true;
		}
		
		}catch(CmsVfsResourceNotFoundException ex) {
			jsonError.put("code","999.027");
			jsonError.put("data", ex.getMessage());
			jsonErrors.add(jsonError);
			isMark = true;
		}
		
		jsonItem.put("descripcion", videoTitle);
		jsonItem.put("errores","");
		if (isMark)
		   	jsonItem.put("errores",jsonErrors);
		
		jsonItem.put("isMark",isMark);
		jsonItem.put("path", videosPath);
		
	  
		
		
		return jsonItem;
	}
	
	/** Validamos otros contenidos relacioados a publicar. */
	public boolean isValidTags(String tags, Long type ){
		
		boolean isMark = true;
		
		if (!tags.equals("") && !tags.isEmpty()) {
			String[] tagsSpl = tags.split(", ");
			
			for (int ixd=0 ;ixd < tagsSpl.length; ixd ++) {
				
				TermsDAO termsDAO = new TermsDAO();
			 	try{
					Long existeValor = termsDAO.existeTerminoByType(type, tagsSpl[ixd].trim());
					if (existeValor > 0){	
						Terms tag = termsDAO.getTerminoById(existeValor, type);
						if(tag.getName().toLowerCase().equals(tag.getName().toLowerCase())){
							if(tag.getApproved() != 1) {
								isMark = false;
								return isMark;
							}
						}
					}else{
						isMark = false;
						return isMark;
					}
				} catch (Exception e) {
					isMark = false;
					return isMark;
				}
				
			}
		}
		return isMark;	
		
	}
	
	/** Validamos si el contenido es externo */	
	public boolean isExternalContent(String src) {
		if (src.contains("https://"))
			return true;
		if (src.contains("http://"))
			return true;
		return false;
	}  
	

	public JSONObject expire() {
		JSONObject result = new JSONObject();
		JSONArray errorsJS = new JSONArray();
		CmsObject cms = getCmsObject();

		String videoPath = jsonRequest.getString("path");

		try {
			String tempPath = CmsWorkplace.getTemporaryFileName(videoPath);

			if (getCmsObject().existsResource(tempPath, CmsResourceFilter.ALL)) {
				CmsResourceUtils.forceLockResource(cms, tempPath);
				cms.deleteResource(tempPath, CmsResource.DELETE_PRESERVE_SIBLINGS);
			}

			CmsResourceUtils.forceLockResource(cms, videoPath);

			CmsFile file = cms.readFile(videoPath, CmsResourceFilter.ALL);

			long expireDate = System.currentTimeMillis() - (60 * 60 * 1000);

			file.setDateExpired(expireDate);

			cms.lockResource(videoPath);
			cms.writeFile(file);
			cms.unlockResource(videoPath);

			resources.add(getCmsObject().readResource(videoPath, CmsResourceFilter.ALL));

			result.put("status", "ok");

		} catch (Exception e) {

			LOG.error("Error trying to save resource " + videoPath + " in site " + cms.getRequestContext().getSiteRoot()
					+ " (" + cms.getRequestContext().currentProject().getName() + ")", e);

			result.put("status", "error");

			JSONObject error = new JSONObject();
			
			if (e.getMessage().indexOf("Error reading resource from path") > -1)
				error.put("errorCode","999.027");
			
			error.put("path", videoPath);
			error.put("message", e.getMessage());

			try {
				CmsLock lockE = cms.getLock(videoPath);

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
