package com.tfsla.diario.admin.json;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentErrorHandler;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.auditActions.resourceMonitor.I_ResourceMonitor;
import com.tfsla.diario.auditActions.resourceMonitor.ResourceMonitorManager;
import com.tfsla.diario.ediciones.services.FreshnessService;
import com.tfsla.diario.freshness.model.Freshness;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;



public class TfsXmlContentSetter  extends A_TfsXmlContentProc {

	protected static final Log LOG = CmsLog.getLog(TfsXmlContentSetter.class);
	
	CmsXmlContentErrorHandler  m_validationHandler;
	CPMConfig configura;
	String siteName = "";
	String publication = "";
	JSONObject content;
	
	public TfsXmlContentSetter(CmsObject cms, String resource) throws CmsException {
		
		setResourceName(resource);
		setTempFileName(CmsWorkplace.getTemporaryFileName(m_resourceName));
		
		m_file = cms.readFile(resource, CmsResourceFilter.ALL);
        m_content = CmsXmlContentFactory.unmarshal(cms, m_file);
        this.cms = cms;
        
        configura = CmsMedios.getInstance().getCmsParaMediosConfiguration();
        siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();;
        
        setFileEncoding(getFileEncoding(cms, resource));
        
        setLocale(cms.getRequestContext().getLocale());
        
        retriveMessages();
	}
	
	private void fillContent(CmsXmlContent newContent, JSONObject content, String parentPath) {
		Iterator<String> keys = (Iterator<String>)content.keys();
		while(keys.hasNext()) {
		    String key = keys.next();
		    
		    String fullName = parentPath + key;
		    
		    if (content.get(key) instanceof JSONObject) {
		          // do something with jsonObject here      
		    }
		    else if (content.get(key) instanceof JSONArray) {
		    	JSONArray arrayItem = (JSONArray) content.get(key);
		    	
		    	int prevIdx = newContent.getIndexCount(fullName, getLocale());
		    	//LOG.debug("el path " + fullName + " esta " + prevIdx + " veces ");
		    	Iterator<JSONObject> it = arrayItem.iterator();
		    	int currIdx = 0;
		        while (it.hasNext()) {
		        	
		        	Object object = it.next();
		        	
		        	if (prevIdx<=currIdx) {
		        		newContent.addValue(cms, fullName, getLocale(), currIdx); //index:0
				    	//LOG.debug("agrego " + fullName + " 1 vez ");

		        	}
		        	
		        	String newPath = parentPath;
	        		
        			newPath += key + "[" + (currIdx+1) + "]/";
        			
		        	if (object instanceof JSONObject) {
		        		JSONObject jsonObj = (JSONObject) object;
		        		
		        		
		        		//LOG.debug("fillContent - jsonObj:" + jsonObj.toString());
		        		fillContent(newContent, jsonObj, newPath);
		        	
		        	}
		        	else if (object instanceof String) {
		        		String value = (String) object;
		 
		        		//LOG.debug("agregando el valor " + value  + " a " + newPath);
		        		I_CmsXmlContentValue contentValue = newContent.getValue(newPath, getLocale());
		        		contentValue.setStringValue(cms,value);
		        	} 
		        	
		        	currIdx++;
		        }
		        
		    }
		    else {
		    	Object keyvalue = content.get(key);
		    }
		}
	
	}
	
	public boolean invalidContent(CmsXmlContent newContent) throws CmsException
    {
		  
		if (cms.existsResource(getTempFileName(), CmsResourceFilter.ALL)) {
            if (!cms.getLock(getTempFileName()).isUnlocked()) {
            	cms.changeLock(getTempFileName());
            } else {
            	cms.lockResource(getTempFileName());
            }
        }
        else
        	createTempFile();
		
		CmsFile tmpFile = cms.readFile(getTempFileName(), CmsResourceFilter.ALL);
		tmpFile.setContents(newContent.marshal());

		CmsXmlContent tmpContent =  CmsXmlContentFactory.unmarshal(cms, tmpFile);

		m_validationHandler = tmpContent.validate(cms);
		//m_validationHandler = newContent.validate(cms);
    	
    	return m_validationHandler.hasErrors();
    }

	public JSONArray getErrors() {
		JSONArray errorsJon = new JSONArray();
		
		Map<String,String> errors = m_validationHandler.getErrors(getLocale());
 	 	Iterator<Map.Entry<String, String>> elErrorsIter = errors.entrySet().iterator();
        	while (elErrorsIter.hasNext()) {
            		Map.Entry<String, String> elEntry = (Map.Entry<String, String>)elErrorsIter.next();
            		JSONObject error = new JSONObject();
            		error.put("element", elEntry.getKey());
            		error.put("message", m_messages.key(elEntry.getValue()));
            		errorsJon.add(error);
		}
        	
        return errorsJon;
	}
 	
	public boolean setResourceContent(JSONObject jsonContent) throws CmsException {
		CmsXmlContentDefinition contentDefinition = m_content.getContentDefinition();
		
		CmsXmlContent newContent = CmsXmlContentFactory.createDocument(
				cms, 
				getLocale(),
                OpenCms.getSystemInfo().getDefaultEncoding(),
                contentDefinition);
		
		String parentPath = "";
		
		fillContent(newContent, jsonContent, parentPath);
		
		if (invalidContent(newContent))
			return false;
		
		writeContent(newContent);
		
		auditChanges();
		
		content = jsonContent;
		
		return true;	
	}
	
	private void writeContent(CmsXmlContent newContent) throws CmsException {

        String decodedContent = newContent.toString();
         //decodedContent = decodedContent.replaceAll("\\p{C}", "");
        decodedContent = decodedContent.replaceAll("(?!\\n)[\\p{C}]", "");
        
        try {
            m_file.setContents(decodedContent.getBytes(getFileEncoding()));
        } catch (UnsupportedEncodingException e) {
            throw new CmsException(org.opencms.workplace.editors.Messages.get().container(org.opencms.workplace.editors.Messages.ERR_INVALID_CONTENT_ENC_1, getResourceName()), e);
        }
        // the file content might have been modified during the write operation    
        m_file =  cms.writeFile(m_file);
        m_content = CmsXmlContentFactory.unmarshal(cms, m_file);
    }

    private void auditChanges() {
    	I_ResourceMonitor monitor = ResourceMonitorManager.getInstance().getResourceMonitor(m_file.getTypeId());
    	if (monitor!=null) {
			monitor.auditChanges(cms, m_content, m_resourceName, getLocale());
    	}
    }

 
    /**
	 * Se agrega la seguridad de marca en todas las imagenes segun la cateogrización
	 * @param path
	 * @return
	 * @throws CmsException
	 */
	public JSONObject setAmzContentModeration(JSONObject jsonaAthentication) throws CmsException {
		
		JSONObject result = new JSONObject();
		JSONArray errorsJS = new JSONArray();

		String UnsafeLabelsInNews = "";
		JSONArray jsonUnsafeLabels = new JSONArray();
		
		try {
			List<CmsRelation> relations = cms.getRelationsForResource(m_resourceName, CmsRelationFilter.TARGETS);

			String categoryWarning = formatCategAmz(configura.getParam(siteName, jsonaAthentication.getString("publication"), "amzContentModeration", "categoryWarning", ""));
			String categoryCritial = formatCategAmz(configura.getParam(siteName, jsonaAthentication.getString("publication"), "amzContentModeration", "categoryCritial", ""));
			String[] categoryWarningSpl = categoryWarning.split(", ");
			String[] categoryCritialSpl = categoryCritial.split(", ");
			
			for (CmsRelation relation : relations) {

				String relationPath = cms.getRequestContext().removeSiteRoot(relation.getTargetPath());

				if (relationPath.contains("/img/")) {
					CmsResource resource = cms.readResource(relationPath);

					if (OpenCms.getResourceManager().getResourceType("external-image").getTypeId() == resource
							.getTypeId()) {

						CmsProperty prop =  cms.readPropertyObject(resource, "UnsafeLabels", false);
						String UnsafeLabels = (prop != null) ? prop.getValue() : null; 
						
						// primero vamos a agregar todas las categorias de las imagenes si no estan previamente agregadas 
						if (UnsafeLabels != null) {

							JSONObject jsonUnsafeLabel = new JSONObject();
							
							if (UnsafeLabels.indexOf(",") > 0) {
								String[] UnsafeLabelsSpl = UnsafeLabels.split(", ");

								for (String UnsafeLabel : UnsafeLabelsSpl) {
										if (!UnsafeLabelsInNews.contains(UnsafeLabel)) 
											UnsafeLabelsInNews += (!UnsafeLabelsInNews.equals("")) ? ", " + UnsafeLabel : UnsafeLabel;
								}
							}else {
								if (!UnsafeLabelsInNews.contains(UnsafeLabels))
									UnsafeLabelsInNews += (!UnsafeLabelsInNews.equals("")) ? ", " + UnsafeLabels : UnsafeLabels;

							}
						}
					}
				}
			}
			
			// ahora por todas las categorias que tenemos para esa noticia, vamos a validar la criticidad de cada una. 
			jsonUnsafeLabels = getUnsafeLabels(UnsafeLabelsInNews, categoryCritial, categoryCritialSpl, categoryWarningSpl);
			
LOG.debug("jsonUnsafeLabels --->" + jsonUnsafeLabels);
		} catch (Exception e) {

			LOG.error("Error saving to get properties " + m_resourceName + " in site " + cms.getRequestContext().getSiteRoot()
					+ " (" + cms.getRequestContext().currentProject().getName() + ")", e);

			result.put("status", "error");

			JSONObject error = new JSONObject();
			error.put("path", m_resourceName);
			error.put("message", e.getMessage());

			try {
				CmsLock lockE = cms.getLock(m_resourceName);

				if (!lockE.isUnlocked()) {
					CmsUUID userId = lockE.getUserId();
					CmsUser lockUser = cms.readUser(userId);

					error.put("lockby", lockUser.getFullName());
				}
			} catch (CmsException e2) {
			}

			errorsJS.add(error);
		}

		try {

			cms.lockResource(m_resourceName);
			cms.writePropertyObject(m_resourceName, new CmsProperty("UnsafeLabels", UnsafeLabelsInNews, null));
			cms.unlockResource(m_resourceName);

			result.put("status", "ok");

		} catch (CmsException e) {

			LOG.error("Error saving to set properties " + m_resourceName + " in site " + cms.getRequestContext().getSiteRoot()
					+ " (" + cms.getRequestContext().currentProject().getName() + ")", e);

			result.put("status", "error");

			JSONObject error = new JSONObject();
			error.put("path", m_resourceName);
			error.put("message", e.getMessage());

			try {
				CmsLock lockE = cms.getLock(m_resourceName);

				if (!lockE.isUnlocked()) {
					CmsUUID userId = lockE.getUserId();
					CmsUser lockUser = cms.readUser(userId);

					error.put("lockby", lockUser.getFullName());
				}
			} catch (CmsException e2) {
			}

			errorsJS.add(error);
		}

		result.put("errors", errorsJS);
		result.put("UnsafeLabels", jsonUnsafeLabels);

		return result;

	}
	

	/** 
	Validamos la seguridad de marca de una imagen ó noticia, 
	RECIBE: la lista de categorias
	DEVUELVE: solo las categorias segun su criticada definida en el cmsmedios.xl 
	*/
	
	public JSONArray getUnsafeLabels(String UnsafeLabels, String categoryCritial, String[] categoryCritialSpl, String[] categoryWarningSpl ){
		
		JSONArray jsonUnsafeLabels = new JSONArray();
		
		if (UnsafeLabels.indexOf(",") > 0){
			String[] UnsafeLabelsSpl = UnsafeLabels.split(", ");
		
			for (String UnsafeLabelString : UnsafeLabelsSpl) {
			
				JSONObject jsonUnsafeLabel = validarCripticidad(UnsafeLabelString, categoryCritial, categoryCritialSpl, categoryWarningSpl );
				
				if (jsonUnsafeLabel.has("label")) {
					jsonUnsafeLabels.add(jsonUnsafeLabel);
					LOG.debug("TENEMOS LABEL " + jsonUnsafeLabel);
					
				}
					
			}
			
			
		} else {
			
			JSONObject jsonUnsafeLabel = validarCripticidad(UnsafeLabels, categoryCritial, categoryCritialSpl, categoryWarningSpl );
			
			if (jsonUnsafeLabel.has("label"))
				jsonUnsafeLabels.add(jsonUnsafeLabel);
			
		}
		
		LOG.debug("jsonUnsafeLabels -- > " +  jsonUnsafeLabels);
		return jsonUnsafeLabels;
	};
	
	/** 
	 * validamos la criticidad de una categria en particular
	 * */
	private JSONObject validarCripticidad(String unsafeLabel, String categoryCritial, String[] categoryCritialSpl, String[] categoryWarningSpl ){
		
		String type = "";
		JSONObject jsonUnsafeLabel = new JSONObject();
		
		String unsafeLabelAux = formatCategAmz(unsafeLabel);
		
		//nivel rojo CRITICO
		if (categoryCritial.contains(unsafeLabelAux)){		
			for (String categoryCritialS : categoryCritialSpl) { 
				if(categoryCritialS.equals(unsafeLabelAux)){
					type = "CRITIAL";
				}
			}
		}
		//nivel naranja MODERADO
		if (!type.equals("")){		
			for (String categoryWarningS : categoryWarningSpl) { 
				if(categoryWarningS.equals(unsafeLabelAux)){
					 type = "MODERATE";
				}
			}
		}
		
		LOG.debug("type " +  type );
		LOG.debug("jsonUnsafeLabel " +  jsonUnsafeLabel );
		
		if (type.equals("CRITIAL")){
			jsonUnsafeLabel.put("name",unsafeLabel);						
			jsonUnsafeLabel.put("label","critical");
		} 
		
		if (type.equals("MODERATE")){
			jsonUnsafeLabel.put("name",unsafeLabel);						
			jsonUnsafeLabel.put("label","moderate");
	
		}
		
		LOG.debug("jsonUnsafeLabel " +  jsonUnsafeLabel );
			
		return jsonUnsafeLabel;
	}
	
	private String formatCategAmz(String unsafeLabel) {
		
		String unsafeLabelAux = unsafeLabel.toLowerCase();
		unsafeLabelAux = unsafeLabelAux.replaceAll("á","a");
		unsafeLabelAux = unsafeLabelAux.replaceAll("é","e");
		unsafeLabelAux = unsafeLabelAux.replaceAll("í","i");
		unsafeLabelAux = unsafeLabelAux.replaceAll("ó","o");
		unsafeLabelAux = unsafeLabelAux.replaceAll("ú","u");
		
		return unsafeLabelAux;
	}
	/**
	 * Se verifica que la calidad de imagen sea la correcta, segun la configuración del cmsmedios, en los campos:
	 * - imagenPreview 
	 * - ImagenPersonalizada
	 * - Imágenes de fotogalería
	 * - Imagen del item de nota lista
	 * @param path
	 * @return
	 * @throws CmsException
	 */
	public JSONObject setQualityError(JSONObject jsonRequest) throws CmsException {
		
		JSONObject result = new JSONObject();
		JSONArray errorsJS = new JSONArray();

		boolean qualityImageError = false;
		
		int sizeMin = configura.getIntegerParam(siteName, jsonRequest.getJSONObject("authentication").getString("publication"), "imageUpload", "dimensionMin",99999);
		int dimensionTotal = configura.getIntegerParam(siteName, jsonRequest.getJSONObject("authentication").getString("publication"), "imageUpload", "dimensionTotal",99999);
		
		JSONObject content = jsonRequest.getJSONObject("content");
		
		String firstFields = "imagenPrevisualizacion,imagenPersonalizada";
		String firstFieldsSplt[] = firstFields.split(",");
		int level = 1;
		for (int i=0; i < firstFieldsSplt.length; i++) { 
		if (!firstFieldsSplt[i].equals("")){
		
			String firstField = firstFieldsSplt[i];
		
			if (content.has(firstField) && content.getJSONArray(firstField).size() > 0){				 
		
				JSONArray firstToProcess = content.getJSONArray(firstField); //imagenPrevisualizacion[ ...  ]
		
				for (int idx=0; idx < firstToProcess.size(); idx++) {
				
				JSONObject firstProcess = firstToProcess.getJSONObject(idx); //imagenPrevisualizacion[idx]/ .....
				
				if (firstProcess.has("imagen") && firstProcess.getJSONArray("imagen").size() > 0 ){	
				
				JSONArray imagenPathArray = firstProcess.getJSONArray("imagen"); //imagenPrevisualizacion[idx]/description[j]

				if (!imagenPathArray.getString(0).isEmpty() && !imagenPathArray.getString(0).equals("") && !imagenPathArray.getString(0).equals("||!!undefined!!||")) {

					String imagenPath = imagenPathArray.getString(0);
					CmsProperty prop =  cms.readPropertyObject(imagenPath, "image.size", false);
					String imageSize = (prop != null) ? prop.getValue() : null;
					imageSize = imageSize.replaceAll("w:","").replaceAll(",h:","x");
					
					if(imageSize != null){
						imageSize = imageSize.replaceAll("w:","").replaceAll(",h:","x");
						String[] imageSizeSpl = imageSize.split("x");
						
						int imageSizeW = Integer.parseInt(imageSizeSpl[0]);
						int imageSizeH = Integer.parseInt(imageSizeSpl[1]);
													
						int imageSizeTotal = imageSizeW * imageSizeH;
						boolean imageSizeTotalOk = (imageSizeTotal >= dimensionTotal) ? true : false;
						boolean imageMinOk = (imageSizeW >= sizeMin)? true : false;
						boolean imageAproved = (imageMinOk && imageSizeTotalOk) ? true : false;
				
						if (!imageAproved && !qualityImageError) {
							qualityImageError =  true;
	
						}
					}
				}
				}
				}
			}
		}
		}
	
		if (content.has("noticiaLista") && content.getJSONArray("noticiaLista").size() > 0){
		
			JSONArray noticiaLista = content.getJSONArray("noticiaLista"); //noticiaLista[ ...  ]
	
			for (int idl=0; idl < noticiaLista.size(); idl++) {
			
			JSONObject newsList = noticiaLista.getJSONObject(idl); //noticiaLista[idx]/ .....
				
				if (newsList.has("imagenlista") && newsList.getJSONArray("imagenlista").size() > 0){	
				
					JSONArray imageList = newsList.getJSONArray("imagenlista"); //noticiaLista[idx]/titulo[j]
					
					String imagePath = imageList.getJSONObject(0).getJSONArray("imagen").getString(0); //imagenPrevisualizacion[idx]/description[j]
	
					if (!imagePath.equals("") && !imagePath.equals("||!!undefined!!||")) {
					CmsProperty prop =  cms.readPropertyObject(imagePath, "image.size", false);
					String imageSize = (prop != null) ? prop.getValue() : null;
					imageSize = imageSize.replaceAll("w:","").replaceAll(",h:","x");
								
					if(imageSize != null){
						imageSize = imageSize.replaceAll("w:","").replaceAll(",h:","x");
						String[] imageSizeSpl = imageSize.split("x");
						
						int imageSizeW = Integer.parseInt(imageSizeSpl[0]);
						int imageSizeH = Integer.parseInt(imageSizeSpl[1]);
													
						int imageSizeTotal = imageSizeW * imageSizeH;
						boolean imageSizeTotalOk = (imageSizeTotal >= dimensionTotal) ? true : false;
						boolean imageMinOk = (imageSizeW >= sizeMin)? true : false;
						boolean imageAproved = (imageMinOk && imageSizeTotalOk) ? true : false;
				
						if (!imageAproved && !qualityImageError) {
							qualityImageError =  true;
	
						}
					}
					}
				}
			
			}
		}
		try {

			cms.lockResource(m_resourceName);
        	cms.writePropertyObject(m_resourceName, new CmsProperty("image.qualityError",String.valueOf(qualityImageError),null));
			cms.unlockResource(m_resourceName);

			result.put("status", "ok");

		} catch (CmsException e) {

			LOG.error("Error saving to set properties " + m_resourceName + " in site " + cms.getRequestContext().getSiteRoot()
					+ " (" + cms.getRequestContext().currentProject().getName() + ")", e);

			result.put("status", "error");

			JSONObject error = new JSONObject();
			error.put("path", m_resourceName);
			error.put("message", e.getMessage());

			try {
				CmsLock lockE = cms.getLock(m_resourceName);

				if (!lockE.isUnlocked()) {
					CmsUUID userId = lockE.getUserId();
					CmsUser lockUser = cms.readUser(userId);

					error.put("lockby", lockUser.getFullName());
				}
			} catch (CmsException e2) {
			}

			errorsJS.add(error);
		}

		result.put("errors", errorsJS);

		return result;

	}
	
	/**
	 * Agrego ó elimino la exipiración de notas segun lo que el usuario seteo. 
	 * @param newPath
	 * @param cmsObject
	 * @return
	 */
	public JSONObject setExpireReleaseDates(String newPath, JSONObject jsonDetail) {
		
		JSONObject status = new JSONObject();
		
		try{
			if (jsonDetail.size() > 0 && jsonDetail.has("hasExpireReleaseDates") && jsonDetail.getString("hasExpireReleaseDates").equals("true")){
		
			
			long releaseDate = jsonDetail.getJSONObject("isExpiredDate").getLong("availableDate");
			long expireDate = jsonDetail.getJSONObject("isExpiredDate").getLong("expireDate");
			
			
			cms.setDateReleased(newPath, releaseDate, false);
			cms.setDateExpired(newPath, expireDate, false);
		
			}
		
			if (jsonDetail.size() > 0 && jsonDetail.has("hasExpireReleaseDates") && jsonDetail.getString("hasExpireReleaseDates").equals("false")){
				
				cms.setDateReleased(newPath, CmsResource.DATE_RELEASED_DEFAULT, false);
				cms.setDateExpired(newPath, CmsResource.DATE_EXPIRED_DEFAULT, false);
	
			}
			status.put("status","ok");
		
		} catch (CmsException e) {
			status.put("status","error");
			status.put("error",e.getMessage());
		}
		
		return status;
		
	}
	
	/**
	 * Agrego ó elimino la exipiración de notas segun lo que el usuario seteo. 
	 * @param newPath
	 * @param cmsObject
	 * @return
	 */
	public JSONObject setComplianceData(JSONObject jsonDetail) {
		
		JSONObject result = new JSONObject();
		JSONArray errorsJS = new JSONArray();

		if (jsonDetail.size() > 0 && jsonDetail.has("compliance")){
			
			String complianceData = "C:"+jsonDetail.getJSONObject("compliance").getString("characters") + ";W:"+ jsonDetail.getJSONObject("compliance").getString("words");
			
			try {

				cms.lockResource(m_resourceName);
	        	cms.writePropertyObject(m_resourceName, new CmsProperty("complianceData",complianceData,null));
				cms.unlockResource(m_resourceName);

				result.put("status", "ok");

			} catch (CmsException e) {

				LOG.error("Error saving to set properties complianceData " + m_resourceName + " in site " + cms.getRequestContext().getSiteRoot()
						+ " (" + cms.getRequestContext().currentProject().getName() + ")", e);

				result.put("status", "error");

				JSONObject error = new JSONObject();
				error.put("path", m_resourceName);
				error.put("message", e.getMessage());

				try {
					CmsLock lockE = cms.getLock(m_resourceName);

					if (!lockE.isUnlocked()) {
						CmsUUID userId = lockE.getUserId();
						CmsUser lockUser = cms.readUser(userId);

						error.put("lockby", lockUser.getFullName());
					}
				} catch (CmsException e2) {
				}

				errorsJS.add(error);
			}

			result.put("errors", errorsJS);
		
		}
		return result;
	}

	/**
	 * Al guardar la noticia, se valida si tiene o no seteada una frecuencia. 
	 * Si la misma no esta en a base, se crea el registo para que el job la levante. 
	 * Si la misma esta en la base, se valida que sea igual. Si tuvo cambios se actualiza la misma. 
	 *
	 * @param jsonRequest
	 * @throws Exception
	 */
	public boolean processFreshness(JSONObject jsonRequest) throws Exception{
		
		
		String pathNew = jsonRequest.getString("path");

		FreshnessService freshService = new FreshnessService();
		
		JSONObject jsonFrescura = jsonRequest.getJSONObject("detail").getJSONObject("freshness");
		jsonFrescura.put("siteName", jsonRequest.getJSONObject("authentication").getString("siteName"));
		jsonFrescura.put("publication", jsonRequest.getJSONObject("authentication").getString("publication"));
		jsonFrescura.put("url", pathNew);
		jsonFrescura.put("section", jsonRequest.getJSONObject("content").getJSONArray("seccion").get(0).toString());

		int publicationID = jsonRequest.getJSONObject("authentication").getInt("publication");
		String siteName =  jsonRequest.getJSONObject("authentication").getString("siteName");
		
		if (jsonFrescura.getString("type").equals("")){
			freshService.deleteFreshness(publicationID, siteName, pathNew);
			return true;
		}
		
		if (freshService.hasSetFreshness(publicationID, siteName, pathNew) ){
			
		
			Freshness freshnessExiting = freshService.getFreshness(publicationID, siteName, pathNew);
			Long freshnessExitingDateAux = freshnessExiting.getDate();
			if (freshnessExiting.getType().equals("DATE_UPDATED")) 
				freshnessExiting.setDate(0);
			
			Freshness freshnessToUpdate = freshService.formatJsonToFreshness(jsonFrescura,pathNew);

			Long freshnessToUpdateAux = freshnessToUpdate.getDate();
			if (freshnessToUpdate.getType().equals("DATE_EXACT")) 
				freshnessToUpdate.setDate(0);
			
//			REVISAR PORQUE LA FECHA ES DIFERNTE
			
			if (!freshService.isFreshnessEquals(freshnessExiting, freshnessToUpdate)) {
				
				
				freshService.deleteFreshness(publicationID, siteName, pathNew);
				
				freshnessToUpdate.setDate(freshnessToUpdateAux);

				freshService.createFreshness(freshnessToUpdate);
				
				LOG.debug("se creo una frescura para la nota  " + pathNew + "  --> " + freshService.hasSetFreshness(publicationID, siteName, pathNew) + "  con los datos " + freshService.formatFreshnessToJSON(freshnessToUpdate) );
			} else {
//				verifico si la frescura es de tipo recurrencia y si la fecha en la base es mayor al dia de hoy. 
//				si la frescura es de tipo manual y la fecha es menor HUBO ERROR AL PROCESAR. 
//				si la frescura es de tipo recurrencia y es menor a hoy > hay que actualizarla. 
				 
				if (freshnessExiting.isTypeRecurrence()) {
				
				Date freshnessDate = new Date (freshnessExitingDateAux);
				Date today = new Date(); 
				
				if(freshnessDate.before(today)){
					
					// actualizar fecha, segun recurrencia hasta que sea mayor a hoy. 
					Date newDateToFreshness = freshnessDate; 
					newDateToFreshness = new Date (freshService.setDateFreshness(freshnessExiting.getDate(), freshnessExiting.getRecurrece()));
					while (newDateToFreshness.before(today)) {
						newDateToFreshness = new Date (freshService.setDateFreshness(newDateToFreshness.getTime(), freshnessExiting.getRecurrece()));
					}
					
					freshnessToUpdate.setDate(newDateToFreshness.getTime());
					
					freshService.updateFreshness(freshnessToUpdate);
					
				}
				}
			}
		} else {
			
		//	Freshness freshnessToUpdate = freshService.formatJsonToFreshness(jsonFrescura,pathNew);
			Freshness frestocreate = (Freshness)JSONObject.toBean(jsonFrescura,Freshness.class);
			
			LOG.debug("jsonFrescura " + jsonFrescura);

			
			if (frestocreate.equals("RECURRENCE")) {
				frestocreate.setDate(jsonFrescura.getLong("dateNew"));
				frestocreate.setStartDate(jsonFrescura.getLong("dateNew"));
			} else
				frestocreate.setDate(jsonFrescura.getLong("dateNew"));
			

			LOG.debug("freshnessToUpdate section " + frestocreate.getSection());

			
			freshService.createFreshness(frestocreate);
			
			LOG.debug("jsonFrescura " + jsonFrescura);
		}
		
		return true;
	}
	
	
	    	
}