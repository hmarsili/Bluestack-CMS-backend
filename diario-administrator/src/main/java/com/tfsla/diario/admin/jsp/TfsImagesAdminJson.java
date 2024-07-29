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

public class TfsImagesAdminJson {

	protected static final Log LOG = CmsLog.getLog(TfsImagesAdminJson.class);

	private JSONObject jsonRequest;
	private CmsObject cms;

	/** The current OpenCms users workplace settings. */
	private CmsWorkplaceSettings m_settings;

	private List<CmsResource> resources = new ArrayList<CmsResource>();

	private String siteName;
	private TipoEdicion currentPublication;
	private String publication;
	private String moduleConfigName;
	private CPMConfig config;
	private int sizeMin;
	private int dimensionTotal;
	
	private String categoryWarning;
	private String categoryCritical;
	private String categoriesCriticalsScape;
	private String categoryWarningScape;
	private String[] categoryCriticalSpl;
	private String[] categoryWarningSpl;

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

	public TfsImagesAdminJson(JSONObject jsonreq, CmsObject cmsObj) throws Exception {

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

		moduleConfigName = "imageUpload";

		configura = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		
		sizeMin = configura.getIntegerParam(siteName, publication, moduleConfigName, "dimensionMin", 99999);
		dimensionTotal = configura.getIntegerParam(siteName, publication, moduleConfigName, "dimensionTotal", 99999);
						
		categoryWarning = configura.getParam(siteName, publication, "amzContentModeration", "categoryWarning", "");
		categoryCritical = configura.getParam(siteName, publication, "amzContentModeration", "categoryCritial", "");
 
		categoriesCriticalsScape = categoryCritical.toLowerCase();
		categoriesCriticalsScape = categoriesCriticalsScape.replaceAll(", ",",");
		categoriesCriticalsScape = categoriesCriticalsScape.replaceAll("á","a");
		categoriesCriticalsScape = categoriesCriticalsScape.replaceAll("é","e");
		categoriesCriticalsScape = categoriesCriticalsScape.replaceAll("í","i");
		categoriesCriticalsScape = categoriesCriticalsScape.replaceAll("ó","o");
		categoriesCriticalsScape = categoriesCriticalsScape.replaceAll("ú","u");
		
		categoryWarningScape = categoryWarning.toLowerCase();
		categoryWarningScape = categoryWarningScape.replaceAll(", ",",");
		categoryWarningScape = categoryWarningScape.replaceAll("á","a");
		categoryWarningScape = categoryWarningScape.replaceAll("é","e");
		categoryWarningScape = categoryWarningScape.replaceAll("í","i");
		categoryWarningScape = categoryWarningScape.replaceAll("ó","o");
		categoryWarningScape = categoryWarningScape.replaceAll("ú","u");
		
		categoryCriticalSpl = categoriesCriticalsScape.split(",");
		categoryWarningSpl = categoryWarningScape.split(",");

	}
	
	public JSONObject retrieveImages() throws CmsException {

		JSONArray images = jsonRequest.getJSONArray("images");
		
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
	 * Proceso para validar las imagenes al publicar. Se valida los siguientes errores
	 * 
	 * - hasDraft
	 * - isLock 
 	 * - isSchedule
	 * - calidad
	 * - seguridad de marca
	 *
	 * @param imagePath
	 * @param cmsObject
	 * @param pageContext
	 * @return
	 * @throws Exception 
	 */

	public JSONObject checkImageToPublish(String imagePath, PageContext pageContext) throws Exception {

		JSONObject jsonItem = new JSONObject();
		JSONObject jsonError = new JSONObject();
		JSONArray jsonImagesError = new JSONArray();

		boolean isMark = false;
		String imageDescrip  = "";
		try {
			
		// Descripción  
		CmsProperty prop =  cms.readPropertyObject(imagePath, "Description", false);
		imageDescrip = (prop != null) ? prop.getValue() : "";

	
		if (imageDescrip == null || imageDescrip.equals("") || imageDescrip.equals("||!!undefined!!||")) {
			jsonError.put("code","999.025");
			jsonError.put("data",imagePath);
			jsonImagesError.add(jsonError);
			isMark = true;
			imageDescrip = "";
		}
			
		try{
			
			boolean hasDraft =  (cms.existsResource(cms.getRequestContext().removeSiteRoot(org.opencms.workplace.CmsWorkplace.getTemporaryFileName(imagePath)), CmsResourceFilter.IGNORE_EXPIRATION));
			Long hasDraftDate = 0L;
			if (hasDraft){
				CmsResource resourceDraf = cms.readResource(cms.getRequestContext().removeSiteRoot(org.opencms.workplace.CmsWorkplace.getTemporaryFileName(imagePath)), CmsResourceFilter.IGNORE_EXPIRATION);
				hasDraftDate = resourceDraf.getDateLastModified();
				
				jsonError.put("code","999.011");
				jsonError.put("data",hasDraftDate);
				jsonImagesError.add(jsonError);
				isMark = true;
				
			}

			// Bloqueo de imagen. 
			CmsLock lockFile = cms.getLock(imagePath);
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
						jsonImagesError.add(jsonError);
						isMark =  true;
						
	        		}         
	        		 if (!cms.getRequestContext().currentUser().getName().equals(cms.readUser(lockFile.getUserId()).getName())){
						isLockData = "BY " + cms.readUser(lockFile.getUserId()).getFullName();
						
						jsonError.put("code","999.010");
						jsonError.put("data",isLockData);
						jsonImagesError.add(jsonError);
						isMark = true;
					}
				
			}
			
			// Seguridad de marca: 
			// Se valida el nivel de cripticidad de la seguridad de marca y se envia el valor mas alto de la misma.
			
			prop =  cms.readPropertyObject(imagePath, "UnsafeLabels", false);
			String UnsafeLabels = (prop != null) ? prop.getValue() : null; 
			
			if (UnsafeLabels != null){
				
				String unsafeLabelsType = getUnsafeLabelsType(UnsafeLabels);
				
				if (!unsafeLabelsType.equals("")){
						jsonError.put("code","009.008");
						jsonError.put("data",unsafeLabelsType);
						jsonImagesError.add(jsonError);
						isMark = true;
				}
			}
			
			// Calidad de imagen.
			prop = cms.readPropertyObject(imagePath, "image.size", false);      
		
			String imageSize = (prop != null) ? prop.getValue() : null;
			
			if(imageSize != null){
				/**
					<param name="dimensionRecommended">1200</param>
					<param name="dimensionMin">1200</param>
					<param name="dimensionTotal">800000</param>
				*/
				
				imageSize = imageSize.replaceAll("w:","").replaceAll(",h:","x");
				String[] imageSizeSpl = imageSize.split("x");
				int imageSizeW = Integer.parseInt(imageSizeSpl[0]);
				int imageSizeH = Integer.parseInt(imageSizeSpl[1]);
				
				int imageSizeTotal = imageSizeW * imageSizeH;
				boolean imageSizeTotalOk = (imageSizeTotal >= dimensionTotal) ? true : false;
				boolean imageMinOk = (imageSizeW >= sizeMin)? true : false;
				boolean imageAproved = (imageMinOk && imageSizeTotalOk) ? true : false;
				
				if (!imageAproved){
					
					jsonError.put("code","009.007");
					jsonError.put("data","");
					jsonImagesError.add(jsonError);
					isMark = true;
				}
				
			}
			
			// expiracion de imagenes
			CmsResource resImg = cms.readFile(imagePath);
			Long dateExpired = 	resImg.getDateExpired();
			Long dateReleased = resImg.getDateReleased();

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
				jsonImagesError.add(jsonError);
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
				jsonImagesError.add(jsonError);
				isMark = true;

			}
		
			
		} catch (CmsException e) {
			jsonError.put("code","999.002");
			jsonError.put("data", e.getMessage());
			jsonImagesError.add(jsonError);
			isMark = true;
		}
		
		}catch(CmsVfsResourceNotFoundException ex) {
			jsonError.put("code","999.027");
			jsonError.put("data", ex.getMessage());
			jsonImagesError.add(jsonError);
			isMark = true;
		}
		
		jsonItem.put("descripcion", imageDescrip);
		jsonItem.put("errores","");
		if (isMark)
		   	jsonItem.put("errores",jsonImagesError);
		
		jsonItem.put("isMark",isMark);
		jsonItem.put("path", imagePath);
		
	  
		
		
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

		String imagePath = jsonRequest.getString("path");

		try {
			String tempPath = CmsWorkplace.getTemporaryFileName(imagePath);

			if (getCmsObject().existsResource(tempPath, CmsResourceFilter.ALL)) {
				CmsResourceUtils.forceLockResource(cms, tempPath);
				cms.deleteResource(tempPath, CmsResource.DELETE_PRESERVE_SIBLINGS);
			}

			CmsResourceUtils.forceLockResource(cms, imagePath);

			CmsFile file = cms.readFile(imagePath, CmsResourceFilter.ALL);

			long expireDate = System.currentTimeMillis() - (60 * 60 * 1000);

			file.setDateExpired(expireDate);

			cms.lockResource(imagePath);
			cms.writeFile(file);
			cms.unlockResource(imagePath);

			resources.add(getCmsObject().readResource(imagePath, CmsResourceFilter.ALL));

			result.put("status", "ok");

		} catch (Exception e) {

			LOG.error("Error trying to save resource " + imagePath + " in site " + cms.getRequestContext().getSiteRoot()
					+ " (" + cms.getRequestContext().currentProject().getName() + ")", e);

			result.put("status", "error");

			JSONObject error = new JSONObject();
			
			if (e.getMessage().indexOf("Error reading resource from path") > -1)
				error.put("errorCode","999.027");
			
			error.put("path", imagePath);
			error.put("message", e.getMessage());

			try {
				CmsLock lockE = cms.getLock(imagePath);

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
	


	public boolean isDefaultAutoSaveEnabled() {
		return config.getParam(siteName, publication, moduleConfigName, "autoSaveDefault", "enabled").toLowerCase()
				.equals("enabled");
	}

	public boolean isDefaultForcePublishEnabled() {
		return config.getParam(siteName, publication, moduleConfigName, "forcePublishDefault", "enabled").toLowerCase()
				.equals("enabled");
	}

	public boolean isDefaultPublishRelatedContentEnabled() {
		return config.getParam(siteName, publication, moduleConfigName, "publishRelatedContentDefault", "enabled")
				.toLowerCase().equals("enabled");
	}

	public boolean isDefaultPublishRelatedNewsEnabled() {
		return config.getParam(siteName, publication, moduleConfigName, "publishRelatedNewsDefault", "enabled")
				.toLowerCase().equals("enabled");
	}

	public String getDefaultNewsOrder() {
		return config.getParam(siteName, publication, moduleConfigName, "defaultNewsOrder",
				"user-modification-date desc");
	}

	public int getDefaultNewsResultSize() {
		return config.getIntegerParam(siteName, publication, moduleConfigName, "defaultNewsResultCount", 100);
	}

	public List<String> getNewsResultSizeOptions() {
		return config.getParamList(siteName, publication, moduleConfigName, "newsResultSizeOptions");
	}

	public boolean IsSiteAmp() {
		return config.getParam(siteName, publication, "adminNewsConfiguration", "siteIsAmp", "false").toLowerCase()
				.equals("enabled");
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
	
	public String getUnsafeLabelsType(String UnsafeLabels){
		
		String unsafeLabelTypeFinal = "";
		
		if (UnsafeLabels.indexOf(",") > 0){
			String[] UnsafeLabelsSpl = UnsafeLabels.split(", ");
		
			for (String UnsafeLabelString : UnsafeLabelsSpl) {
			
				String unsafeLabelType = validarCripticidad(UnsafeLabelString);

				if (unsafeLabelType.equals("CRITICAL")) 
					return unsafeLabelType;
		
				else if (unsafeLabelType.equals("MODERATE")) 
						unsafeLabelTypeFinal = unsafeLabelType;
				
			}
			
			
		} else {
			
			String unsafeLabelType = validarCripticidad(UnsafeLabels);
			
			if (unsafeLabelType.equals("CRITICAL")) 
				return unsafeLabelType;
	
			else if (unsafeLabelType.equals("MODERATE")) 
					unsafeLabelTypeFinal = unsafeLabelType;
			
		}
		
		return unsafeLabelTypeFinal;
	};
	
	private String validarCripticidad(String unsafeLabel){
	
	String type = "";
	String unsafeLabelAux = unsafeLabel.toLowerCase();
	unsafeLabelAux = unsafeLabelAux.replaceAll("á","a");
	unsafeLabelAux = unsafeLabelAux.replaceAll("é","e");
	unsafeLabelAux = unsafeLabelAux.replaceAll("í","i");
	unsafeLabelAux = unsafeLabelAux.replaceAll("ó","o");
	unsafeLabelAux = unsafeLabelAux.replaceAll("ú","u");
	
	//nivel rojo CRITICO
	if (categoriesCriticalsScape.contains(unsafeLabelAux)){		
		for (String categoryCriticalS : categoryCriticalSpl) {
			if(categoryCriticalS.equals(unsafeLabelAux)){
				type = "CRITICAL";	
			}
		}
	}
	
	//nivel naranja MODERADO
	if (type.equals("")){		
		for (String categoryWarningS : categoryWarningSpl) { 
			if(categoryWarningS.equals(unsafeLabelAux)){
				 type = "MODERATE";
			}
		}
	}	
	return type;
}
	
}
