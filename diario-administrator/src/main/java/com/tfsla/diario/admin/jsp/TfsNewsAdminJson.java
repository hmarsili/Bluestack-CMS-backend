package com.tfsla.diario.admin.jsp;

import org.apache.commons.logging.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.configuration.CmsSchedulerConfiguration;
import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsResourceState;

import java.util.stream.Collectors;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.jsp.PageContext;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


import org.opencms.file.*;

import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLock;
import org.opencms.main.*;
import org.opencms.relations.*;
import org.opencms.report.CmsLogReport;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.scheduler.jobs.CmsPublishScheduledJob;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.commons.Messages;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.admin.json.*;
import com.tfsla.diario.analysis.service.*;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.model.Seccion;
import com.tfsla.diario.ediciones.services.NoticiasService;
import com.tfsla.diario.ediciones.services.OpenCmsBaseService;
import com.tfsla.diario.ediciones.services.ProductivityPlanAWS;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.AmzComprehendService.DocEntity;
import com.tfsla.diario.history.*;
import com.tfsla.diario.ediciones.services.SeccionesService;
import com.tfsla.diario.model.*;
import com.tfsla.diario.model.TfsNoticia;
import com.tfsla.diario.security.services.SecurityService;
import com.tfsla.diario.terminos.data.*;
import com.tfsla.diario.terminos.model.*;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;
import com.tfsla.opencmsdev.encuestas.*;
import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.utils.UrlLinkHelper;

public class TfsNewsAdminJson  {

	protected static final Log LOG = CmsLog.getLog(TfsNewsAdminJson.class);

	private Boolean forcePublish;
	private Boolean publish;
	private Boolean publishRelatedContent;
	private Boolean publishRelatedNews;
	
	private CPMConfig configura;
	private CmsObject cms;
	private CmsWorkplaceSettings m_settings;
	
	private JSONObject jsonRequest;
	
	private int sizeMin;
	private int dimensionTotal;
	
	private List<CmsResource> resources = new ArrayList<CmsResource>();
	
	private String categoryWarning;
	private String categoryCritical;
	private String dateFormat;
	private String enableEntitiesDetection;
	private String moduleConfigName;
	private String publication;
	private String publicUrl;
	private String siteName;
	private String timeFormat;

	private String scoreDisplayEntitiesConfig = "0.85";
	private String scoreDisplayEntitiesHiddenConfig = "0.85";
	private String countSelectedEntitiesConfig = "0";
	private String countSelectedEntitiesHiddenConfig = "0";
	
	private TipoEdicion currentPublication;
	
	public boolean isMark = false;	
	public JSONArray jsonRelationsError = new JSONArray();
	public JSONArray jsonRelationsOk = new JSONArray();
	
	public String getSiteName() {
		return this.siteName;
	}

	public String getPublication() {
		return this.publication;
	}

	public CmsObject getCmsObject() {
		return this.cms;
	}
	public TfsNewsAdminJson(JSONObject jsonreq, CmsObject cmsObj ) throws Exception {

		jsonRequest = jsonreq;

		cms = cmsObj;

		configura = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		
		siteName = OpenCms.getSiteManager().getCurrentSite(getCmsObject()).getSiteRoot();

		m_settings = CmsWorkplace.initWorkplaceSettings(getCmsObject(), m_settings, true);

		if (jsonRequest.getJSONObject("authentication").has("publication")) {
			publication = jsonRequest.getJSONObject("authentication").getString("publication");
		} else {
			TipoEdicionService tService = new TipoEdicionService();

			currentPublication = tService.obtenerEdicionOnlineRoot(siteName);

			publication = "" + currentPublication.getId();
		}

		moduleConfigName = "adminNewsConfiguration";

		if (jsonRequest.getJSONObject("news").has("publish"))
			publish = Boolean.parseBoolean(jsonRequest.getJSONObject("news").getString("publish"));
		else
			publish = isDefaultForcePublishEnabled();

		if (jsonRequest.getJSONObject("news").has("publishRelatedContent"))
			publishRelatedContent = Boolean
					.parseBoolean(jsonRequest.getJSONObject("news").getString("publishRelatedContent"));
		else
			publishRelatedContent = isDefaultPublishRelatedContentEnabled();

		if (jsonRequest.getJSONObject("news").has("publishRelatedNews"))
			publishRelatedNews = Boolean
					.parseBoolean(jsonRequest.getJSONObject("news").getString("publishRelatedNews"));
		else
			publishRelatedNews = isDefaultPublishRelatedNewsEnabled();

		if (jsonRequest.getJSONObject("news").has("forcePublish"))
			forcePublish = Boolean.parseBoolean(jsonRequest.getJSONObject("news").getString("forcePublish"));
		else
			forcePublish = isDefaultForcePublishEnabled();
		
		dateFormat = configura.getParam(siteName, publication, "admin-settings", "dateFormat", "");
		timeFormat = configura.getParam(siteName, publication, "admin-settings", "timeFormat", "");
		
		sizeMin = configura.getIntegerParam(siteName, publication, "imageUpload", "dimensionMin",99999);
		dimensionTotal = configura.getIntegerParam(siteName, publication, "imageUpload", "dimensionTotal",99999);
						
		categoryWarning = configura.getParam(siteName, publication, "amzContentModeration", "categoryWarning", "");
		categoryCritical = configura.getParam(siteName, publication, "amzContentModeration", "categoryCritial", "");

		enableEntitiesDetection = configura.getParam(siteName, publication, "adminNewsConfiguration", "enableEntitiesDetection", "true");
		
		scoreDisplayEntitiesConfig = configura.getParam(siteName, publication, "adminNewsConfiguration", "scoreDisplayEntities", "0.85");
		scoreDisplayEntitiesHiddenConfig = configura.getParam(siteName, publication, "adminNewsConfiguration", "scoreDisplayEntitiesHidden", "0.85");
		countSelectedEntitiesConfig = configura.getParam(siteName, publication, "adminNewsConfiguration", "countSelectedEntities", "3");
		countSelectedEntitiesHiddenConfig = configura.getParam(siteName, publication, "adminNewsConfiguration", "countSelectedEntitiesHidden", "3");
		
		publicUrl = configura.getParam(siteName, publication, "newsTags", "publicUrl", "");

	}
	
	public void retrieveNews() throws CmsException {

		JSONArray news = jsonRequest.getJSONObject("news").getJSONArray("new");

		for (int idx = 0; idx < news.size(); idx++) {

			JSONObject noticia = news.getJSONObject(idx);
			String path = noticia.getString("path");
			resources.add(getCmsObject().readResource(path, CmsResourceFilter.ALL));

		}
	}

	public List<CmsResource> getRelatedResources(CmsResource resource) throws CmsException {

		List<CmsResource> relatedResources = new ArrayList<CmsResource>();

		List<CmsResource> currentRelResources = addRelatedResourcesToPublish(resource, false, true,
				new ArrayList<CmsResource>());
		relatedResources.addAll(currentRelResources);

		return relatedResources;
	}

	public List<CmsResource> getResources() throws CmsException {
		List<CmsResource> allresources = new ArrayList<CmsResource>();

		JSONArray news = jsonRequest.getJSONObject("news").getJSONArray("new");

		for (int idx = 0; idx < news.size(); idx++) {

			JSONObject noticia = news.getJSONObject(idx);
			String path = noticia.getString("path");
			allresources.add(cms.readFile(path, CmsResourceFilter.ALL));
		}

		return allresources;
	}

	// borrar este metodo.
	public List<CmsResource> getRelatedResources() throws CmsException {
		List<CmsResource> allresources = new ArrayList<CmsResource>();
		CmsObject cms = getCmsObject();

		JSONArray news = jsonRequest.getJSONObject("news").getJSONArray("new");

		for (int idx = 0; idx < news.size(); idx++) {

			JSONObject noticia = news.getJSONObject(idx);
			String path = noticia.getString("path");
			allresources.add(cms.readFile(path, CmsResourceFilter.ALL));
		}

		List<CmsResource> relatedResources = new ArrayList<CmsResource>();
		for (CmsResource resource : allresources) {
			List<CmsResource> currentRelResources = addRelatedResourcesToPublish(resource, false, true,
					new ArrayList<CmsResource>());
			currentRelResources.removeAll(relatedResources);
			currentRelResources.removeAll(allresources);
			relatedResources.addAll(currentRelResources);
		}

		return relatedResources;
	}

	public synchronized void publish(boolean unlock, boolean recursivePublish, PageContext pageContext ) throws Exception {
		CmsObject cms = getCmsObject();

		List<CmsResource> relatedResources = new ArrayList<CmsResource>();
		for (CmsResource resource : resources) {
		
			updateUrlCdn(resource);
			
			List<CmsResource> currentRelResources = addRelatedResourcesToPublish(resource, unlock, recursivePublish,
					new ArrayList<CmsResource>());
			currentRelResources.removeAll(relatedResources);
			currentRelResources.removeAll(resources);
			relatedResources.addAll(currentRelResources);
		}

		resources.addAll(relatedResources);

		CmsPublishList pList = OpenCms.getPublishManager().getPublishList(cms, resources, false);

		CmsLogReport report = new CmsLogReport(Locale.ENGLISH, this.getClass());
		OpenCms.getPublishManager().publishProject(cms, report, pList);

		if (pList.size() < 30)
			OpenCms.getPublishManager().waitWhileRunning();
	}

	public synchronized void publish() throws Exception {
		publish(true);
	}
	
	public synchronized void publish(boolean publishRelated) throws Exception {
		CmsObject cms = getCmsObject();

		List<CmsResource> relatedResources = new ArrayList<CmsResource>();
		for (CmsResource resource : resources) {
			
			updateUrlCdn(resource);

			if (publishRelated) {
				List<CmsResource> currentRelResources = addRelatedResourcesToPublish(resource, false, true,
						new ArrayList<CmsResource>());
				currentRelResources.removeAll(relatedResources);
				currentRelResources.removeAll(resources);
				relatedResources.addAll(currentRelResources);
			}
		}

		if (publishRelated)
			resources.addAll(relatedResources);

		//System.out.println("Se publicaran los siguientes recursos: ");
		//System.out.println(resources);
		
		CmsPublishList pList = OpenCms.getPublishManager().getPublishList(cms, resources, false);

		//System.out.println("Armo el publish list con " + pList.getAllResources().size()+ " recursos");
		//System.out.println(pList.getAllResources());
		
		CmsLogReport report = new CmsLogReport(Locale.ENGLISH, this.getClass());
		OpenCms.getPublishManager().publishProject(cms, report, pList);

		if (pList.size() < 30)
			OpenCms.getPublishManager().waitWhileRunning();

	}
	
	public JSONObject checkNewToPublish(String newPath, CmsObject cmsObject, PageContext pageContext, boolean entitiesDetection) throws Exception {
		
		JSONObject jsonItem = new JSONObject();
		
		CmsResource resourceNew;
	
		try {
			resourceNew = cmsObject.readResource(newPath, CmsResourceFilter.IGNORE_EXPIRATION);
			
			/** se valida nivel 0 de errores de Noticias
			*	- hasDraft
			*	- Islock
			*	- Estado pendiente de publicacion
			*	- permisoso insuficientes
			*	- categorias repetidas
			* 	- Si cumple o no con los criterios de compliance
			*/
			jsonItem = checkNew(resourceNew, cmsObject, pageContext, 0);
			
			List<CmsRelation> relations = cmsObject.getRelationsForResource(newPath, CmsRelationFilter.ALL);

		       	for ( CmsRelation relation : relations) {
	       		try {
	       			
	       			String rel = "";
	       			
	   				if (relation.getSourcePath().equals(siteName+newPath)){
	   					rel = relation.getTargetPath();
	   				   
		       		CmsResource resourceRelation = cmsObject.readResource(cmsObject.getRequestContext().removeSiteRoot(rel), CmsResourceFilter.IGNORE_EXPIRATION);
					String typeRelation = OpenCms.getResourceManager().getResourceType(resourceRelation).getTypeName();
					if( OpenCms.getResourceManager().getResourceType("noticia").getTypeId() == resourceRelation.getTypeId()){
						/** validar nivel 0 de errores Noticias. 
						* 	+ validar nivel 1 de errores de Noticias
						*	- hasExpired
						*	- Estado En Parrilla ó en redaccion.
						*/
						JSONObject jsonRelatedNews = checkNew(resourceRelation, cmsObject, pageContext, 1);
						if (Boolean.parseBoolean(jsonRelatedNews.getString("isMark"))) {
							jsonRelationsError.add(jsonRelatedNews);
							isMark = true;
						}else{
							jsonRelationsOk.add(jsonRelatedNews);
						}
					} else  if( typeRelation.indexOf("encuesta") > -1 ){
						/** validar nivel 1  de errores Imagenes. 
						*	- Si esta cerrada ó expidada.
						*/
						String urlResource = cmsObject.getRequestContext().removeSiteRoot(org.opencms.workplace.CmsWorkplace.getTemporaryFileName(resourceRelation.getRootPath())).replaceAll("~","");
						JSONObject jsonRelatedPoll = checkPoll(urlResource, cmsObject);
						if (Boolean.parseBoolean(jsonRelatedPoll.getString("isMark"))) {
							jsonRelationsError.add(jsonRelatedPoll);
							isMark = true;
						}else{
							jsonRelationsOk.add(jsonRelatedPoll);
						}
					} else if ( typeRelation.indexOf("trivia") > -1 ){
						/** validar nivel 1  de errores Imagenes
						*	- Estado pendiente de publicacion
						*	- Estado En Parrilla ó en redaccion.
						*/
						JSONObject jsonRelatedTrivia = checkTrivia(resourceRelation, cmsObject, pageContext);
						if (Boolean.parseBoolean(jsonRelatedTrivia.getString("isMark"))) {
							jsonRelationsError.add(jsonRelatedTrivia);
							isMark = true;
						}else{
							jsonRelationsOk.add(jsonRelatedTrivia);
						}
					} else if( typeRelation.indexOf("receta") > -1 ){
						/** validar nivel 1  de errores Imagenes. 
						*	- Si esta cerrada ó expidada.
						*/
						//jsonRelations.add ("receta");
						JSONObject jsonRelatedRecipe = checkRecipe(resourceRelation, cmsObject, pageContext);
						if (Boolean.parseBoolean(jsonRelatedRecipe.getString("isMark"))) {
							jsonRelationsError.add(jsonRelatedRecipe);
							isMark = true;
						}else{
							jsonRelationsOk.add(jsonRelatedRecipe);
						}
					} else {
						if( typeRelation.indexOf("imagen") == 0 ){
							String urlRelation = cmsObject.getRequestContext().removeSiteRoot(org.opencms.workplace.CmsWorkplace.getTemporaryFileName(resourceRelation.getRootPath())).replaceAll("~","");
							String title = cmsObject.readPropertyObject(urlRelation, "Title", false).getValue("");
							if (typeRelation.indexOf("video") > -1)
								typeRelation = "video"; 
							if (typeRelation.indexOf("audio") > -1)
								typeRelation = "audio"; 
							if (typeRelation.indexOf("pointer") > -1)
								title = resourceRelation.getName(); 
								
							JSONObject jsonRelated =new JSONObject();
							jsonRelated.put("title",title);
							jsonRelated.put("type",typeRelation);
							jsonRelated.put("isMark","false");
							jsonRelationsOk.add(jsonRelated);
						}
					}
				
	   				}
	       		} catch (CmsException e) {
	       			e.printStackTrace();
		      	}
		       	}
		} catch (CmsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}  	
	    		
		TfsXmlContentGetter getter = null;
		try {
			getter = new TfsXmlContentGetter(cmsObject ,newPath);
		
			JSONObject content = getter.getResourceContent();
			
			/** CUERPO DE LA NOTA. */
			if (content.has("cuerpo") && content.getJSONArray("cuerpo").getString(0).length() > 0){				 
				
				Document doc = Jsoup.parse(content.getJSONArray("cuerpo").getString(0));
				processCuerpo(doc, cmsObject);
				
			}
			
			
			/** IMAGENES: 
			* Nivel 0 de errores Imagenes. 
			*	- Si no esta publicada
			*	- Seguridad de Marca
			*	- isLock
			*	- Si no tiene descripcion.
			*	- Si esta eliminada. 
			*	
			* Nivel 1 de errores Imagenes
			*	- validar nivel 0 de errores Imagenes. 
			*	- Validar Dimension Recomendada.
			*
			*/
			String firstFields = "imagenPrevisualizacion,imagenPersonalizada,imagenesFotogaleria,imagenFacebook";
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
						
						String imageDescription = "" ;
						if (firstProcess.has("descripcion") && firstProcess.getJSONArray("descripcion").size() > 0){
							JSONArray descripcion = firstProcess.getJSONArray("descripcion"); //imagenPrevisualizacion[idx]/description[j]
							imageDescription = descripcion.getString(0);
						}
						boolean NANVALIDATE = true;
						if (firstFieldsSplt[i].equals("imagenFacebook")){
							NANVALIDATE = false;
							level = 0;
							if (imageDescription.equals("")) 
								imageDescription = imagenPathArray.getString(0);
						}
						JSONObject jsonRelatedImg = checkImage(imagenPathArray.getString(0), imageDescription,cmsObject,jsonRequest.getJSONObject("authentication").getString("publication"),level, NANVALIDATE);
						if (Boolean.parseBoolean(jsonRelatedImg.getString("isMark"))) {
							jsonRelationsError.add(jsonRelatedImg);
							isMark = true;
						}else{
							jsonRelationsOk.add(jsonRelatedImg);
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
					
							String imageDescription = "" ;
							if (imageList.getJSONObject(0).has("descripcion") && imageList.getJSONObject(0).getJSONArray("descripcion").size() > 0){
								JSONArray descripcion = imageList.getJSONObject(0).getJSONArray("descripcion"); //imagenPrevisualizacion[idx]/description[j]
								imageDescription = descripcion.getString(0);
							}
							
							JSONObject jsonRelatedImg = checkImage(imagePath, imageDescription,cmsObject,jsonRequest.getJSONObject("authentication").getString("publication"),0, true);
							if (Boolean.parseBoolean(jsonRelatedImg.getString("isMark"))) {
								jsonRelationsError.add(jsonRelatedImg);
								isMark = true;
							}else{
								jsonRelationsOk.add(jsonRelatedImg);
							}
						
						
						}
					}
					
					if (newsList.has("videoYouTube")) {
						JSONArray videoArray = newsList.getJSONArray("videoYouTube"); //videoFlash[ ...  ]
						processVideoYoutbe(videoArray);
					}
	
					if (newsList.has("videoFlash")) {
						
						JSONArray videoArray = newsList.getJSONArray("videoFlash"); //videoYouTube[ ...  ]
						processVideoFlash(videoArray);
					}
	
	
					if (newsList.has("videoEmbedded")) {
						JSONArray videoArray = newsList.getJSONArray("videoEmbedded"); //videoEmbedded[ ...  ]
						processVideoEmbedded(videoArray);
					}
					
					if (newsList.has("cuerpo") && newsList.getJSONArray("cuerpo").size() > 0){	
						Document doc = Jsoup.parse(newsList.getJSONArray("cuerpo").getString(0));
						processCuerpo(doc, cmsObject);	
					}
				
				}	
			}
			
			/** NOTICIAS RELACIOADAS MANUALES. 
			*	Si la nota tiene seteado que se muestran noticias relacioadas manuales y no hay ninguna seleccionada se le advierte al usuario
			*/	
			if (content.getJSONArray("noticiasRelacionadasAutomaticas").getString(0).equals("false") && content.has("noticiasRelacionadas")) {
				JSONArray noticiasRelacionadas = content.getJSONArray("noticiasRelacionadas");	
				JSONObject noticiaRelacionada = noticiasRelacionadas.getJSONObject(0); //noticiasRelacionadas[0]/ .....
			 	if (noticiaRelacionada.getJSONArray("noticia").getString(0).equals("")){
				 	JSONObject jsonError = new JSONObject();
					jsonError.put("code","999.017");
					jsonError.put("type","related");
					jsonError.put("data","");
					jsonRelationsError.add(jsonError);
					isMark = true;		
				}
			}
			if (content.getJSONArray("noticiasRelacionadasAutomaticas").getString(0).equals("false") && !content.has("noticiasRelacionadas")) {
				 	JSONObject jsonError = new JSONObject();
					jsonError.put("code","999.017");
					jsonError.put("type","related");
					jsonError.put("data","");
					jsonRelationsError.add(jsonError);
					isMark = true;		
			}
			
			/** IMAGENES PERSONALIZADAS .
			*	- Validamos si alguna de las imagenes personalizadas no esta setada. 
			*/
			if (content.has("imagenPersonalizada")) {
				
				boolean isMarkPreviewError = false;
				String isMarkPreviewData = "";
				
				for (int i = 0; i< content.getJSONArray("imagenPersonalizada").size(); i++) {
				
					JSONObject imagenPersonalizada = content.getJSONArray("imagenPersonalizada").getJSONObject(i);
					if (imagenPersonalizada.getJSONArray("configuracion").getString(0).equals("DESKTOP") &&
						imagenPersonalizada.getJSONArray("imagen").getString(0).equals("")) {
						isMarkPreviewError = true;
						isMarkPreviewData = "Desktop";
						
					} 
					if (imagenPersonalizada.getJSONArray("configuracion").getString(0).equals("MOBILE") &&
						imagenPersonalizada.getJSONArray("imagen").getString(0).equals("")) {
						isMarkPreviewError = true;
						isMarkPreviewData += (isMarkPreviewData.equals("")) ? "Mobile" : "/Mobile";
						
					} 
					if (imagenPersonalizada.getJSONArray("configuracion").getString(0).equals("MOBILE") &&
						imagenPersonalizada.getJSONArray("imagen").getString(0).equals("")) {
						isMarkPreviewError = true;
						isMarkPreviewData += (isMarkPreviewData.equals("")) ? "Tablet" : "/Tablet";							
					}
					
				}
				if (isMarkPreviewError) {
				 	JSONObject jsonError = new JSONObject();
					jsonError.put("code","999.032");
					jsonError.put("type","image");
					jsonError.put("data",isMarkPreviewData);
					jsonRelationsError.add(jsonError);
					isMark = true;		
				}
					
				
			}
			
			/** PERSONAS y TAGS .
			*	- Validamos si alguno de los tags, tagsOcultos y Personas NO Esta APROBADO. 
			*/
			
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
			
			boolean isValidTagsPersons = true;
			
			if (content.has("claves") && !content.getJSONArray("claves").getString(0).equals("") && !content.getJSONArray("claves").getString(0).isEmpty()) {
				isValidTagsPersons = isValidTags(content.getJSONArray("claves").getString(0), type);
				if (!isValidTagsPersons) {
	
						JSONObject jsonRelatedOther = new JSONObject();
						jsonRelatedOther.put("title", "");
						jsonRelatedOther.put("type","otros");
						jsonRelatedOther.put("isMark",true);
						
						jsonRelationsError.add(jsonRelatedOther);
						isMark = true;
				}
			}
			
			if (isValidTagsPersons && content.has("clavesOcultas") && !content.getJSONArray("clavesOcultas").getString(0).equals("") && !content.getJSONArray("clavesOcultas").getString(0).isEmpty()) {
				isValidTagsPersons = isValidTags(content.getJSONArray("clavesOcultas").getString(0), type);
				if (!isValidTagsPersons) {

						JSONObject jsonRelatedOther = new JSONObject();
						jsonRelatedOther.put("title", "");
						jsonRelatedOther.put("type","otros");
						jsonRelatedOther.put("isMark",true);
						
						jsonRelationsError.add(jsonRelatedOther);
						isMark = true;
				}
	
			}
			
			if (isValidTagsPersons && content.has("personas") && !content.getJSONArray("personas").getString(0).equals("") && !content.getJSONArray("personas").getString(0).isEmpty()) {
				isValidTagsPersons = isValidPersons(content.getJSONArray("personas").getString(0));
				if (!isValidTagsPersons) {
	
						JSONObject jsonRelatedOther = new JSONObject();
						jsonRelatedOther.put("title", "");
						jsonRelatedOther.put("type","otros");
						jsonRelatedOther.put("isMark",true);
						
						jsonRelationsError.add(jsonRelatedOther);
						isMark = true;
				}
			}
			

			/**
			 * Sugerencias de Tags para los campos claves y clavesOcultas.
			 * Solo se envian aquellos que superan el score configurado 
			 * y aquellos que no estan en la noticia, si el tag es uno de los que debe estar seleccionado quita un lugar. 
			 */
			jsonItem.put("hasEntities",entitiesDetection); //indica al front si se deben mostrar o no las entidades, solo se muestran si se esta publicando una única noticia.
			jsonItem.put("entities","");
			
			if (entitiesDetection && enableEntitiesDetection.equals("true")) {
				String claves = "";
				if (content.has("claves") && !content.getJSONArray("claves").getString(0).equals("") && !content.getJSONArray("claves").getString(0).isEmpty()) {
					claves = content.getJSONArray("claves").getString(0);
				}
				
				String clavesOcultas = "";
				if (content.has("clavesOcultas") && !content.getJSONArray("clavesOcultas").getString(0).equals("") && !content.getJSONArray("clavesOcultas").getString(0).isEmpty()) {
					clavesOcultas = content.getJSONArray("clavesOcultas").getString(0);
				}
				jsonItem.put("entities",suggestEntities(claves.toLowerCase(),clavesOcultas.toLowerCase(),cmsObject,newPath));

			}
			/** VIDEOS: 
			*
			* Nivel 0 de errores videos
			*	validar que no tenga descripcion.
			*
			*/
			
			if (content.has("videoYouTube")) {
				JSONArray videoArray = content.getJSONArray("videoYouTube"); //videoFlash[ ...  ]
				processVideoYoutbe(videoArray);
			}

			if (content.has("videoFlash")) {
				JSONArray videoArray = content.getJSONArray("videoFlash"); //videoYouTube[ ...  ]
				processVideoFlash(videoArray);
			}


			if (content.has("videoEmbedded")) {
				JSONArray videoArray = content.getJSONArray("videoEmbedded"); //videoEmbedded[ ...  ]
				processVideoEmbedded(videoArray);
			}		
			
			
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		if (isMark) {
			jsonItem.put("relationsIsMark",isMark);
			jsonItem.put("relations",jsonRelationsError);
		}else {
			jsonItem.put("relationsIsMark",isMark);
			jsonItem.put("relations",jsonRelationsOk);	
		}
		
		
		
		return jsonItem;
	}
	
	/*
	 * Validamos las entidades posibles para una noticia.
	 * Las obtenemos de AMZ.
	 * Las procesamos.
	 * Las chequeamos contra la noticia.
	 * Si no estan se envian como sugerencias. 
	 */
	@SuppressWarnings({ "null", "unlikely-arg-type" })
	public JSONObject suggestEntities(String claves, String clavesOcultas, CmsObject cmsObject, String newPath) throws Exception {
		
		JSONObject jsono = new JSONObject();
		JSONArray suggestClaves =  new JSONArray();
		JSONArray suggestClavesOcultas =  new JSONArray();
				
		NewsEntities wizard = new NewsEntities(cmsObject);
		List<DocEntity> analysis = wizard.analizeNews(newPath,publication);
		
		// elimino las entidades simples
		List<DocEntity> entitiesComp =  new ArrayList<>(analysis);
		entitiesComp.removeIf(e -> !e.getName().contains(" "));
		
		// elimino las entidades compuestas
		List<DocEntity> entitiesSimple =  new ArrayList<>(analysis);
		entitiesSimple.removeIf(e -> e.getName().contains(" "));

		List<DocEntity> entitiesFinals =  new ArrayList<>();
		
		// valido si una entidad simple pertenece a una entidad compuesta ej: 'Messi' pertenece a 'Lionel Messi'
		if (!entitiesComp.isEmpty() && !entitiesSimple.isEmpty()) {
			entitiesFinals =  new ArrayList<>(entitiesComp);
			
			for (DocEntity entityS: entitiesSimple) { 
				boolean addEntity = false;
				
				String entityNameS = new String(entityS.getName().getBytes( "UTF-8" ), "UTF8" ).toLowerCase();
				
				for (DocEntity entityC: entitiesComp) {
					
					String entityNameC = new String(entityC.getName().getBytes( "UTF-8" ), "UTF8" ).toLowerCase();
					
					if (entityNameC.indexOf(entityNameS) > -1) {
						 addEntity =  true;
						 //LOG.debug("pertenece a otra entidad:" + entityNameS);
						continue;
					}
						
				}
				
				if (!addEntity) {
					// agrego entidad a mi array, ya que no existe. 
					entitiesFinals.add(entityS);
					//LOG.debug("No existe la entidad, la agrego a entitiesComp " + entityNameS);
				}
				
			}			
		} else if (entitiesComp.isEmpty() && !entitiesSimple.isEmpty()) {
			
			entitiesFinals =  new ArrayList<>(entitiesSimple);
			
		} else if (!entitiesComp.isEmpty() && entitiesSimple.isEmpty()) {
			
			entitiesFinals =  new ArrayList<>(entitiesComp);
			
		}
		
		/** Validamos si las entidades tiene un Score >=  que el configurado.
		 * Existe una configuracion que indica la cantiad de entidades preseleccionadas que se deben ver
		 * Si una entidad que debe estar preselecciona esta en la noticia, la misma ocupa un lugar en al preseleccion
		 */
		
		int selectedEntitiesCountAux = 0;
		int countSelectedEntitiesFinal = Integer.parseInt(countSelectedEntitiesConfig) ;
		int countSelectedEntitiesHiddenFinal = Integer.parseInt(countSelectedEntitiesHiddenConfig) ;
		
		LOG.debug("path:" + newPath );
		LOG.debug("Score para mostrar las entidades en el campo claves:" + scoreDisplayEntitiesConfig );
		LOG.debug("Score para mostrar las entidades en el campo claves Ocultas:" + scoreDisplayEntitiesHiddenConfig );
		LOG.debug("entitiesFinals:" + entitiesFinals );

		
		for (DocEntity entity: entitiesFinals) {
						
			selectedEntitiesCountAux ++;
			
			if (entity.getScore() >= Double.parseDouble(scoreDisplayEntitiesConfig)) {
				
				JSONObject suggestInClaves = checkEntitiesInField(claves,entity);
				if (suggestInClaves.getBoolean("suggestTag")) {
					suggestClaves.add(suggestInClaves.getJSONObject("tag"));
				}else if (selectedEntitiesCountAux <= Integer.parseInt(countSelectedEntitiesConfig))
					countSelectedEntitiesFinal --;
			
			}
			if (entity.getScore() >= Double.parseDouble(scoreDisplayEntitiesHiddenConfig)) {
				JSONObject suggestInClavesOcultas = checkEntitiesInField(clavesOcultas,entity);
				if (suggestInClavesOcultas.getBoolean("suggestTag")) {
					suggestClavesOcultas.add(suggestInClavesOcultas.getJSONObject("tag"));
				}else if (selectedEntitiesCountAux <= Integer.parseInt(countSelectedEntitiesHiddenConfig))
					countSelectedEntitiesHiddenFinal --;
			
			
			}
			
			
		}
		
		jsono.put("claves",suggestClaves);
		jsono.put("clavesOcultas",suggestClavesOcultas);
		jsono.put("clavesSelectCount",countSelectedEntitiesFinal);
		jsono.put("clavesOcultasSelectCount",countSelectedEntitiesHiddenFinal);
			
		return jsono;
		
	}
	
	
	/**
	 * validamos si las entidades de amz ya estan agregadas en la noticia, para eso se usa el campo suggestTag
	 */
	private JSONObject checkEntitiesInField(String tagsInNotice, DocEntity entity) throws Exception {
		
		tagsInNotice = tagsInNotice.toLowerCase().replaceAll(" ", "");
		JSONObject jsono = new JSONObject();
		boolean exiteTag = false;
			
		String newSuggestTag = entity.getName().toLowerCase().replaceAll(" ", "");	
		
		if(!tagsInNotice.equals("") && tagsInNotice.contains(newSuggestTag)) { 
			String[] tagsInNewSp = tagsInNotice.split(",");
			
			for (int i = 0; i < tagsInNewSp.length; i ++) {
				if (tagsInNewSp[i].equals(newSuggestTag)) {
					exiteTag = true;
					break;
				}
			}
			
		}
		
		jsono.put("tag",entity);
		jsono.put("suggestTag",!exiteTag);

		return jsono; 
		
	}
	
	private void processCuerpo(Document doc, CmsObject cmsObject){

		/** Imágenes simples */ 
		Elements figures = doc.select("figure[class=\"image\"]");
				
		for (Element figure:figures){

			Elements img = figure.select("img");
			
			for (Element image:img){
				String src = image.attr("src"); 
				
				if (src.indexOf("?") != -1)
					src = src.substring(0, src.indexOf("?"));
				if (src.indexOf("/img") > 0) {
					if (src.matches(".*.[a-z]*_[0-9]*.[a-z]*"))
						src = src.substring(0, src.lastIndexOf("_"));
				}

				boolean isExternalImage = isExternalContent(src); 
				if (!isExternalImage){ 
					String description = "";
					
					if (figure.select("figcaption").html() != null && !figure.select("figcaption").html().equals("")) {
						description = figure.select("figcaption").html();
					}
					
					if (description.equals("") && image.attr("title") != null && !image.attr("title").equals("")) { 
						description = image.attr("title"); 
					}
					
					if (description.equals("") && image.attr("alt") != null && !image.attr("alt").equals("")) { 
						description = image.attr("alt"); 
					}
				
					JSONObject jsonRelatedImg = checkImage(src, description,cmsObject,jsonRequest.getJSONObject("authentication").getString("publication"),0, true);
					if (Boolean.parseBoolean(jsonRelatedImg.getString("isMark"))) {
						jsonRelationsError.add(jsonRelatedImg);
						isMark = true;
					}else{
						jsonRelationsOk.add(jsonRelatedImg);
					}
				}
			}
		}	
		
		/** Galeria de imagenes */ 
		Elements imagesGallerys = doc.select("div.ck-image-gallery");

		if (imagesGallerys != null) {
		
		
		for (Element imageGallery:imagesGallerys){
			
			Elements imagesInGallery = imageGallery.getElementsByAttributeValue("data-type", "img");
								
			for (Element image:imagesInGallery){		

				if (image != null) {
					String src = image.attr("data-src"); 
					if (src.indexOf("?") != -1)
						src = src.substring(0, src.indexOf("?"));
					if (src.indexOf("/img") > 0) {
						if (src.matches(".*.[a-z]*_[0-9]*.[a-z]*"))
							src = src.substring(0, src.lastIndexOf("_"));
					}
					boolean isExternalImage = isExternalContent(src); 
					if (!isExternalImage){
						String description = image.attr("description"); 
										
						JSONObject jsonRelatedImg = checkImage(src, description,cmsObject,jsonRequest.getJSONObject("authentication").getString("publication"),0, true);
						if (Boolean.parseBoolean(jsonRelatedImg.getString("isMark"))) {
							jsonRelationsError.add(jsonRelatedImg);
							isMark = true;
						}else{
							jsonRelationsOk.add(jsonRelatedImg);
						}
					}
				}
			}
			}
			
		}	
		
		/** Encuestas se comenta porque se obtiene desde los contenidos relacionados.  Linea 353
		
		Elements encuestas = doc.select("div.ckeditor-poll");
		if (encuestas!= null) {
			for (Element encuesta:encuestas){			
				String src = encuesta.attr("data-path"); 

				if (src.indexOf("/encuestas/") > 0) {
					JSONObject jsonRelatedPoll = checkPoll(src, cmsObject);
					if (Boolean.parseBoolean(jsonRelatedPoll.getString("isMark"))) {
						jsonRelationsError.add(jsonRelatedPoll);
						isMark = true;
					}else{
						jsonRelationsOk.add(jsonRelatedPoll);
					}
				}
			}
		}
		*/
		
		/** Videos */
		Elements videos = doc.select("div.ck-video-player");
		
		if (videos != null) {
			for (Element video:videos){		
				JSONObject checkVideoCKeditor = checkVideoCKeditor(video);
				if (Boolean.parseBoolean(checkVideoCKeditor.getString("isMark"))) {
					jsonRelationsError.add(checkVideoCKeditor);
					isMark = true;
				}else{
					jsonRelationsOk.add(checkVideoCKeditor);
				}
			}
		}
		
		/** Galerias de Videos */
		Elements galleryVideos = doc.select("div.ck-video-gallery");
		
		if (galleryVideos != null) {
			for (Element galleryVideo:galleryVideos){	
				int titleNro = 0;
				Elements titles = galleryVideo.getElementsByClass("video__title");
				
				for (Element title:titles){		
					if (title != null) {
						Element video = galleryVideo.getElementsByAttributeValue("data-type", "video").get(titleNro);
						
						JSONObject checkVideoCKeditor = checkVideoGalleryCKeditor(title,video);
						if (Boolean.parseBoolean(checkVideoCKeditor.getString("isMark"))) {
							jsonRelationsError.add(checkVideoCKeditor);
							isMark = true;
						}else{
							jsonRelationsOk.add(checkVideoCKeditor);
						}
					}
					titleNro ++;
				}
			}
		}
	}
	/**
	 * 
	 */
	public JSONObject checkVideoCKeditor(Element video) { 
		JSONObject jsonItem = new JSONObject();
		JSONObject jsonError = new JSONObject();
		JSONArray jsonErrors = new JSONArray();
		boolean isMark = false;
		
		String videoTitle = "";
		String videoName = "";

		Elements firstSpan = video.getElementsByAttribute("video-type"); 
		if (firstSpan != null && firstSpan.size() > 0) {
			String videoType = firstSpan.attr("video-type"); // en base al tipo obtengo el path.
			if (!videoType.isEmpty()) {
				if (videoType.equals("youtube")) {
					videoName = firstSpan.attr("youtubeid"); 
				}
				if (videoType.equals("embedded") || videoType.equals("link")) {
					videoName = firstSpan.attr("data-src"); 
				}
			}
			
		}
		
		Elements video__title = video.getElementsByClass("video__title");  // busco su título
		if (video__title != null && video__title.size() > 0) {
			  videoTitle = video__title.html();
			  
			  if (videoTitle.isEmpty() || videoTitle.equals("") || videoTitle.equals("&nbsp;")) {
		            jsonError.put("code","015.004");
		            jsonError.put("type","video");
		            jsonError.put("data",videoName);
		            isMark = true;
		            jsonErrors.add(jsonError);
		            videoTitle = "";
			  }
		}
		jsonItem.put("title",videoTitle.replaceAll("&nbsp;", ""));
		jsonItem.put("type","video");
		jsonItem.put("isMark", isMark);
		if (isMark) {		
			jsonItem.put("errors",jsonErrors);
		}
		
		return jsonItem;
	}
	
	public JSONObject checkVideoGalleryCKeditor(Element videoTitle, Element spanVideo) { 
		JSONObject jsonItem = new JSONObject();
		JSONObject jsonError = new JSONObject();
		JSONArray jsonErrors = new JSONArray();
		boolean isMark = false;
		
		String title = "";
		String videoName = "";

		title = videoTitle.html();
		
		if (title.isEmpty() || title.equals("") || title.equals("&nbsp;")) {
			  
			  	String videoType = spanVideo.attr("video-type"); // en base al tipo obtengo el path.
				if (!videoType.isEmpty()) {
					if (videoType.equals("youtube")) {
						videoName = spanVideo.attr("youtubeid"); 
					}
					if (videoType.equals("embedded") || videoType.equals("link")) {
						videoName = spanVideo.attr("data-src"); 
					}
				}
				
	            jsonError.put("code","015.004");
	            jsonError.put("type","video");
	            jsonError.put("data",videoName);
	            isMark = true;
	            jsonErrors.add(jsonError);
	            title = "";
		}
		
		jsonItem.put("title",title.replaceAll("&nbsp;", ""));
		jsonItem.put("type","video");
		jsonItem.put("isMark", isMark);
		if (isMark) {		
			jsonItem.put("errors",jsonErrors);
		}
		
		return jsonItem;
	}
	
	
	private void processVideoYoutbe(JSONArray videoArray){
		
		if (!videoArray.getJSONObject(0).getJSONArray("youtubeid").getString(0).equals("") && 
				!videoArray.getJSONObject(0).getJSONArray("youtubeid").getString(0).isEmpty()){
				
				for (int idx=0; idx < videoArray.size(); idx++) {
					
					JSONObject videoObject = videoArray.getJSONObject(idx); //videoYotube[idx]/ .....
					
					JSONObject checkVideo = checkVideo(videoObject,"youtubeid");
					
					if (Boolean.parseBoolean(checkVideo.getString("isMark"))) {
						jsonRelationsError.add(checkVideo);
						isMark = true;
					}else{
						if (checkVideo.has("type")) 
							jsonRelationsOk.add(checkVideo);
					}
		
				}
			}
	}
	

	private void processVideoFlash(JSONArray videoArray){
		
		if (!videoArray.getJSONObject(0).getJSONArray("video").getString(0).equals("") && 
				!videoArray.getJSONObject(0).getJSONArray("video").getString(0).isEmpty()){
				
				for (int idx=0; idx < videoArray.size(); idx++) {
					
					JSONObject videoObject = videoArray.getJSONObject(idx); //videoYotube[idx]/ .....
					
					JSONObject checkVideo = checkVideo(videoObject,"video");
					
					if (Boolean.parseBoolean(checkVideo.getString("isMark"))) {
						jsonRelationsError.add(checkVideo);
						isMark = true;
					}else{
						if (checkVideo.has("type")) 
							jsonRelationsOk.add(checkVideo);
					}
		
				}
			}
	}
	
	private void processVideoEmbedded(JSONArray videoArray){
		
		if (!videoArray.getJSONObject(0).getJSONArray("codigo").getString(0).equals("") && 
				!videoArray.getJSONObject(0).getJSONArray("codigo").getString(0).isEmpty()){
				
				for (int idx=0; idx < videoArray.size(); idx++) {
					
					JSONObject videoObject = videoArray.getJSONObject(idx); //videoYotube[idx]/ .....
					
					JSONObject checkVideo = checkVideo(videoObject,"codigo");
					
					if (Boolean.parseBoolean(checkVideo.getString("isMark"))) {
						jsonRelationsError.add(checkVideo);
						isMark = true;
					}else{
						if (checkVideo.has("type")) 
							jsonRelationsOk.add(checkVideo);
					}
		
				
				}
			}
	}
	/** Validamos las noticias relacionadas a publicar. */
	public JSONObject checkVideo(JSONObject videoObject, String fieldCode) {
		
		JSONObject jsonItem = new JSONObject();
		JSONObject jsonError = new JSONObject();
		JSONArray jsonErrors = new JSONArray();
		boolean isMark = false;
		String videoDescripion = "" ;
		
		if (videoObject.has(fieldCode) && videoObject.getJSONArray(fieldCode).size() > 0 ){	
			
			JSONArray videoCode = videoObject.getJSONArray(fieldCode); //videoYotube[idx]/youtubeid[0]
			
			if (!videoCode.getString(0).isEmpty() && !videoCode.getString(0).equals("") && !videoCode.getString(0).equals("||!!undefined!!||")) {

			    if (videoObject.has("titulo") && videoObject.getJSONArray("titulo").size() > 0){
			        JSONArray descripcion = videoObject.getJSONArray("titulo"); //videoFlash[idx]/description[0]
			        videoDescripion = descripcion.getString(0);
			        
			        if (videoDescripion.equals("") || videoDescripion.equals("||!!undefined!!||")){
			            jsonError.put("code","015.004");
			            jsonError.put("data",videoCode.getString(0));
			            jsonErrors.add(jsonError);
			            isMark = true;
			        }    
			    }else{
			        jsonError.put("code","015.004");
			        jsonError.put("data",videoCode.getString(0));
			        jsonErrors.add(jsonError);
			        isMark = true;
			    }
		
			}
		}
	
		
		jsonItem.put("title",videoDescripion);
		jsonItem.put("type","video");
		jsonItem.put("isMark", isMark);
		if (isMark) {		
			jsonItem.put("errors",jsonErrors);
		}
		
		return jsonItem;
	}
	
	/** Validamos las noticias relacionadas a publicar. */
	public JSONObject checkNew(CmsResource resource, CmsObject cmsObject, PageContext pageContext, int level){
		
		JSONObject jsonItem = new JSONObject();
		JSONObject jsonError = new JSONObject();
		JSONArray jsonErrors = new JSONArray();
		boolean isMark = false;
		
		try{
		
		String fulFileNameNoTemp = cmsObject.getRequestContext().removeSiteRoot(resource.getRootPath()).replaceAll("~","");
		
		jsonItem.put("title", cmsObject.readPropertyObject(fulFileNameNoTemp, "Title", false).getValue(""));
		jsonItem.put("type", cmsObject.readPropertyObject(fulFileNameNoTemp, "newsType", false).getValue(""));
		
		CmsLock lockFile = cmsObject.getLock(fulFileNameNoTemp);
		boolean isLock = !lockFile.isUnlocked() ; // true si esta desbloqueado, false si esta bloqueado
		String isLockData = "";
		
		if (isLock){
			// si el usuario que lockea no es el usuario logueado
	       
		        CmsProject lockInProject = lockFile.getProject();
		        
        		if(CmsProject.PROJECT_TYPE_TEMPORARY == lockInProject.getType()){   
   
        			 String lockInProjectName = lockInProject.getName();
	     	         String altText = "";
	     				   
	     	        if( lockInProjectName !=null) {
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
	     	        }
	     	        
	     	        jsonError.put("code","999.014");
					jsonError.put("data",altText);
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
			
		boolean hasDraft =  (cmsObject.existsResource(cmsObject.getRequestContext().removeSiteRoot(org.opencms.workplace.CmsWorkplace.getTemporaryFileName(fulFileNameNoTemp)), CmsResourceFilter.IGNORE_EXPIRATION));
		Long hasDraftDate = 0L;
		if (hasDraft){
			CmsResource resourceDraf = cmsObject.readResource(cmsObject.getRequestContext().removeSiteRoot(org.opencms.workplace.CmsWorkplace.getTemporaryFileName(fulFileNameNoTemp)), CmsResourceFilter.IGNORE_EXPIRATION);
			hasDraftDate = resourceDraf.getDateLastModified();
			
			jsonError.put("code","999.011");
			jsonError.put("data",hasDraftDate);
			jsonErrors.add(jsonError);
			isMark = true;
			
		}
		
		CmsFile file = cmsObject.readFile(resource);
		I_CmsXmlDocument m_content = CmsXmlContentFactory.unmarshal(cmsObject, file,pageContext.getRequest());
		TfsNoticia noticia = new TfsNoticia(cmsObject, m_content,cmsObject.getRequestContext().getLocale(), pageContext);
		
		jsonItem.put("urlFriendly",noticia.getLink());

		boolean isExpired = noticia.getIsExpired();
		Boolean hasExpireReleaseDates = (noticia.isDateReleasedSet() || noticia.isDateExpiredSet());
		
		if (isExpired){
			Long longDateExpired = (noticia.getLongDateExpired() == 9223372036854775807L) ? 0 : noticia.getLongDateExpired() ;
			JSONObject jsonExpireItem = new JSONObject();
			
			jsonExpireItem.put("availableDate",noticia.getLongDateReleased());
			jsonExpireItem.put("expireDate",longDateExpired);
			
			
			jsonError.put("code","999.012");// contenido despublicado. (expiró el xxxx ó estuvo disponible desde xxxx y expiró xxxx)
			
			if (noticia.getLongDateReleased() > 0 && longDateExpired == 0) {
				
				jsonError.put("code","999.018"); // contenido despublicado (disponible a partir del xxxx)
 
			}
			
			jsonError.put("data",jsonExpireItem);
			jsonErrors.add(jsonError);
			isMark = true;
			
		}else if (hasExpireReleaseDates) {
			
			Long longDateExpired = (noticia.getLongDateExpired() == 9223372036854775807L) ? 0 : noticia.getLongDateExpired() ;
			JSONObject jsonExpireItem = new JSONObject();
			
			jsonExpireItem.put("availableDate",noticia.getLongDateReleased());
			jsonExpireItem.put("expireDate",longDateExpired);
			
			if ( longDateExpired > 0) {
				
				jsonError.put("code","999.019"); //contenido que expirará ( disponible desde xxxx y expirará xxxx ó con fecha de expiración xxxx)
			
			}
			
			jsonError.put("data",jsonExpireItem);
			jsonErrors.add(jsonError);
			isMark = true;

		}
		
		if (level == 1){	
			if(noticia.getState().equals("redaccion") || noticia.getState().equals("parrilla")){
			
				jsonError.put("code","999.013");
				jsonError.put("data",noticia.getState());
				jsonErrors.add(jsonError);
				isMark = true;
			}
		}else{
			JSONArray jsonAuthors = new JSONArray();
			JSONObject jsonUser = new JSONObject();
			CmsUser userCms ;
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
					error3.put("errorCode","008.000"); 
					error3.put("error", e.getMessage());
					jsonAuthors.add(error3);
				}
				
				// Cumpleo no con los criterios de compliance 
				CmsProperty prop =  cmsObject.readPropertyObject(fulFileNameNoTemp, "compliance", false);
				String compliance = (prop != null) ? prop.getValue() : null; 

				if (compliance != null && compliance.indexOf("false") > -1) {
					if( compliance.indexOf("words") > -1) {
						jsonError.put("code","999.029");
					}else {
						jsonError.put("code","999.031");
					}
						jsonError.put("data","");
						jsonErrors.add(jsonError);
						isMark = true;

				}				
			}
			
			jsonItem.put("section", cmsObject.readPropertyObject(fulFileNameNoTemp, "seccion", false).getValue(""));
			jsonItem.put("signatureUserCount", noticia.getAuthorscount());
			jsonItem.put("signatureUsers", jsonAuthors);

			//categorias repetidas.
			if (noticia.getCategoriescount() > 1) {
				List<String> categorias = noticia.getCategories();
				List<String> duplicateCategDescrp = new ArrayList<String>();
				
			    List<String> duplicateCateg = 
			    		categorias.stream()
			                // agrupar por categorias.
			                .collect(Collectors.groupingBy(s -> s))
			                .entrySet()
			                .stream()
			                // filtrar por los duplicados
			                .filter(e -> e.getValue().size() > 1)
			                .map(e -> e.getKey())
			                .collect(Collectors.toList());
			    
			    for(String categName : duplicateCateg) {
			    	duplicateCategDescrp.add(cmsObject.readPropertyObject(categName, "Title", false).getValue(""));
			    }
			    
		        if(!duplicateCateg.isEmpty()) {
					jsonError.put("code","999.030");
					jsonError.put("type","categories");
					jsonError.put("data",duplicateCategDescrp);
					jsonErrors.add(jsonError);
					isMark = true;
		        }
	
			}
		
		}
	
		} catch (CmsException e) {
			if(e.getMessage().indexOf("Error reading resource from path") > -1) {
				jsonError.put("code","999.027");
				jsonError.put("data",resource.getRootPath().replaceAll("~",""));
			}else {
				jsonError.put("code","999.002");
				jsonError.put("data", e.getMessage());
			}
			jsonError.put("data", e.getMessage());
			jsonErrors.add(jsonError);
			isMark = true;
		}
		
		
		
		jsonItem.put("isMark", isMark);
		if (isMark) {		
			jsonItem.put("errors",jsonErrors);
		}
		
		return jsonItem;
		
		
	}
	
	/** Validamos las encuestas relacionadas a publicar. 
	 * - si esta cerrada o despublicada.
	 * - si fue eliminada. 
	 * */
	
	public JSONObject checkPoll(String urlResource, CmsObject cmsObject){
			
			JSONObject jsonItem = new JSONObject();
			JSONObject jsonError = new JSONObject();
			JSONArray jsonErrors = new JSONArray();
			boolean isMark = false;
			String title = "";
	
			try{
	
				Encuesta encuesta = Encuesta.getEncuestaFromURL(cmsObject, urlResource);
				title = encuesta.getPregunta();
				
				if (encuesta.getEstado().toLowerCase().equals("cerrada") || encuesta.getEstado().toLowerCase().equals("despublicada")){				
					jsonError.put("code","999.013");
					jsonError.put("data",encuesta.getEstado().toLowerCase());
					jsonErrors.add(jsonError);
					isMark = true;					
				}
				
			} catch (CmsException e) {
				if(e.getMessage().indexOf("Error reading resource from path") > -1) {
					jsonError.put("code","999.013");
				}else {
					jsonError.put("code","999.002");
					jsonError.put("data", e.getMessage());
				}
				jsonErrors.add(jsonError);
				isMark = true;
			
			} catch (Exception eX) {
				jsonError.put("code","999.002");
				jsonError.put("data", eX.getMessage());
				jsonErrors.add(jsonError);
				isMark = true;
			}
			
			jsonItem.put("title", title);
			jsonItem.put("type","encuesta");
			jsonItem.put("isMark", isMark);
	
			if (isMark) {
				jsonItem.put("errors",jsonErrors);
			}
			
			return jsonItem;
			
			
		}
	
	/** Validamos las recetas relacionadas a publicar. */
	public JSONObject checkRecipe(CmsResource resource, CmsObject cmsObject, PageContext pageContext){
		
		JSONObject jsonItem = new JSONObject();
		JSONObject jsonError = new JSONObject();
		JSONArray jsonErrors = new JSONArray();
		boolean isMark = false;
		String titulo = ""; 
		
		try{
		String fulFileNameNoTemp = cmsObject.getRequestContext().removeSiteRoot(resource.getRootPath()).replaceAll("~","");
		titulo = cmsObject.readPropertyObject(fulFileNameNoTemp, "Title", false).getValue("");
		
		CmsLock lockFile = cmsObject.getLock(fulFileNameNoTemp);
		boolean isLock = !lockFile.isUnlocked() ; // true si esta desbloqueado, false si esta bloqueado
		String isLockData = "";
		
		if (isLock){
			// si el usuario que lockea no es el usuario logueado
		        if (!cmsObject.getRequestContext().currentUser().getName().equals(cmsObject.readUser(lockFile.getUserId()).getName())){
			        CmsProject lockInProject = lockFile.getProject();
			        
				if(CmsProject.PROJECT_TYPE_TEMPORARY == lockInProject.getType()){   
	     	         String lockInProjectName = lockInProject.getName();
	     	         String altText = "";
	     				   
	     	        if( lockInProjectName !=null) {
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
	     	        }
	    	     	jsonError.put("code","999.014");
					jsonError.put("data",altText);
					jsonErrors.add(jsonError);
					isMark =  true;
	
				}         
				
				isLockData = "BY " + cmsObject.readUser(lockFile.getUserId()).getFullName();
				
				jsonError.put("code","999.010");
				jsonError.put("data",isLockData);
				jsonErrors.add(jsonError);
				isMark = true;
				
			}
			
		}
			
		boolean hasDraft =  (cmsObject.existsResource(cmsObject.getRequestContext().removeSiteRoot(org.opencms.workplace.CmsWorkplace.getTemporaryFileName(fulFileNameNoTemp)), CmsResourceFilter.IGNORE_EXPIRATION));
		Long hasDraftDate = 0L;
		if (hasDraft){
			CmsResource resourceDraf = cmsObject.readResource(cmsObject.getRequestContext().removeSiteRoot(org.opencms.workplace.CmsWorkplace.getTemporaryFileName(fulFileNameNoTemp)), CmsResourceFilter.IGNORE_EXPIRATION);
			hasDraftDate = resourceDraf.getDateLastModified();
			
			jsonError.put("code","999.011");
			jsonError.put("data",hasDraftDate);
			jsonErrors.add(jsonError);
			isMark = true;
			
		}
		
	
		CmsFile file = cmsObject.readFile(resource);
		I_CmsXmlDocument m_content = CmsXmlContentFactory.unmarshal(cmsObject, file,pageContext.getRequest());
		TfsReceta recipe = new TfsReceta(cmsObject, m_content,cmsObject.getRequestContext().getLocale(), pageContext);
		
		boolean isExpired = recipe.getIsExpired();
		
		if (isExpired){
			Long longDateExpired = (recipe.getLongDateExpired() == 9223372036854775807L) ? 0 : recipe.getLongDateExpired() ;
			JSONArray jsonExpire = new JSONArray();
			JSONObject jsonExpireItem = new JSONObject();
			jsonExpireItem.put("expireDate",longDateExpired);
			
			jsonError.put("code","999.012");
			jsonError.put("data",jsonExpireItem);
			jsonErrors.add(jsonError);
			isMark = true;
		}
		
		if(recipe.getState().equals("Despublicada") || recipe.getState().equals("parrilla")){
		
			jsonError.put("code","999.013");
			jsonError.put("data",recipe.getState());
			jsonErrors.add(jsonError);
			isMark = true;
		}
	
		} catch (CmsException e) {
			if(e.getMessage().indexOf("Error reading resource from path") > -1) {
				jsonError.put("code","999.027");
				jsonError.put("data",resource.getRootPath().replaceAll("~",""));
			}else {
				jsonError.put("code","999.002");
				jsonError.put("data", e.getMessage());
			}
			jsonErrors.add(jsonError);
			isMark = true;
		}
		
		jsonItem.put("title", titulo);
		jsonItem.put("type","receta");
		jsonItem.put("isMark", isMark);
		
		if (isMark) {
			jsonItem.put("errors",jsonErrors);
		}
		
		return jsonItem;
		
		
	}
	
	/** Validamos las trivias relacionadas a publicar. */
	public JSONObject checkTrivia(CmsResource resource, CmsObject cmsObject, PageContext pageContext){
		
		JSONObject jsonItem = new JSONObject();
		JSONObject jsonError = new JSONObject();
		JSONArray jsonErrors = new JSONArray();
		boolean isMark = false;
		String titulo = "";
		try{
		String fulFileNameNoTemp = cmsObject.getRequestContext().removeSiteRoot(resource.getRootPath()).replaceAll("~","");
		titulo = cmsObject.readPropertyObject(fulFileNameNoTemp, "Title", false).getValue("");
			
		CmsLock lockFile = cmsObject.getLock(fulFileNameNoTemp);
		boolean isLock = !lockFile.isUnlocked() ; // true si esta desbloqueado, false si esta bloqueado
		String isLockData = "";
		
		if (isLock){
			// si el usuario que lockea no es el usuario logueado
		        if (!cmsObject.getRequestContext().currentUser().getName().equals(cmsObject.readUser(lockFile.getUserId()).getName())){
			        CmsProject lockInProject = lockFile.getProject();
			        
				if(CmsProject.PROJECT_TYPE_TEMPORARY == lockInProject.getType()){   
	     	         String lockInProjectName = lockInProject.getName();
	     	         String altText = "";
	     				   
	     	        if( lockInProjectName !=null) {
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
	     	        }

	     	 		jsonError.put("code","999.014");
					jsonError.put("data",altText);
					jsonErrors.add(jsonError);
					isMark =  true;
	
				}         
				
				isLockData = "BY " + cmsObject.readUser(lockFile.getUserId()).getFullName();
				
				jsonError.put("code","999.010");
				jsonError.put("data",isLockData);
				jsonErrors.add(jsonError);
				isMark = true;
				
			}
			
		}
			
		boolean hasDraft =  (cmsObject.existsResource(cmsObject.getRequestContext().removeSiteRoot(org.opencms.workplace.CmsWorkplace.getTemporaryFileName(fulFileNameNoTemp)), CmsResourceFilter.IGNORE_EXPIRATION));
		Long hasDraftDate = 0L;
		if (hasDraft){
			CmsResource resourceDraf = cmsObject.readResource(cmsObject.getRequestContext().removeSiteRoot(org.opencms.workplace.CmsWorkplace.getTemporaryFileName(fulFileNameNoTemp)), CmsResourceFilter.IGNORE_EXPIRATION);
			hasDraftDate = resourceDraf.getDateLastModified();
			
			jsonError.put("code","999.011");
			jsonError.put("data",hasDraftDate);
			jsonErrors.add(jsonError);
			isMark = true;
			
		}
		
	
		CmsFile file = cmsObject.readFile(resource);
		I_CmsXmlDocument m_content = CmsXmlContentFactory.unmarshal(cmsObject, file,pageContext.getRequest());
		TfsTrivia trivia = new TfsTrivia(cmsObject, m_content,cmsObject.getRequestContext().getLocale(), pageContext);
		
		boolean isExpired = trivia.isDateExpiredSet();
		
		if (isExpired){
			Long longDateExpired = (trivia.getLongDateExpired() == 9223372036854775807L) ? 0 : trivia.getLongDateExpired() ;
			JSONArray jsonExpire = new JSONArray();
			JSONObject jsonExpireItem = new JSONObject();
			jsonExpireItem.put("expireDate",longDateExpired);
			
			jsonError.put("code","999.012");
			jsonError.put("data",jsonExpireItem);
			jsonErrors.add(jsonError);
			isMark = true;
		}
		
		if(trivia.getState().equals("inactiva") || trivia.getState().equals("cerrada")){
		
			jsonError.put("code","999.013");
			jsonError.put("data",trivia.getState());
			jsonErrors.add(jsonError);
			isMark = true;
		}
	
		} catch (CmsException e) {
			if(e.getMessage().indexOf("Error reading resource from path") > -1) {
				jsonError.put("code","999.027");
				jsonError.put("data",resource.getRootPath().replaceAll("~",""));
				
			}else {
				jsonError.put("code","999.002");
				jsonError.put("data", e.getMessage());
			}
			jsonErrors.add(jsonError);
			isMark = true;
		}
		
		jsonItem.put("title", titulo);
		jsonItem.put("type","trivia");
		jsonItem.put("isMark", isMark);
		
		if (isMark) {
			jsonItem.put("errors",jsonErrors);
		}
		
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

	/** Validamos si la persona existe */
	public boolean isValidPersons(String peoples) {
	
		boolean isMark = true;
		if (!peoples.equals("") && !peoples.isEmpty()) {
		 	PersonsDAO personsDAO = new PersonsDAO();
		 	String[] peopleSPl = peoples.split(",");
		 	for (int idx = 0 ; idx < peopleSPl.length; idx ++) {
				 	try{
						Long idPerson = personsDAO.existePersonaByName(peopleSPl[idx].trim());
						
						if (idPerson > 0){	
							Persons person = personsDAO.getPersonaById(idPerson);
							if (person.getApproved() != 1) {
								isMark = false;
								return isMark;
							}
						}else{
							isMark = false;
							return isMark;
						}
					} catch (Exception e) {
						isMark = false;
						return isMark ;
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
	
	/** Validamos las imagenes relacionadas a publicar. 
	 * - si no tiene descripción
	 * - Si esta bloqueada por otro usuario
	 * - si no cumple con la seguridad de marca
	 * - si tiene las dimenciones corretas (solo para algunos campos específicos) 
	 * - si fue eliminada. 
	 * */
	public JSONObject checkImage(String imagePath, String imageDescrip, CmsObject cmsObject,String publicationID, int level, boolean NANVALIDATE){
				
		JSONObject jsonItem = new JSONObject();
		JSONObject jsonError = new JSONObject();
		JSONArray jsonErrors = new JSONArray();
		boolean isMark = false;

		imageDescrip = imageDescrip.replaceAll("&nbsp;","");
		
		if (imageDescrip.equals("") || imageDescrip.equals("||!!undefined!!||") && !NANVALIDATE) {
			jsonError.put("code","009.009");
			jsonError.put("data",imagePath);
			jsonErrors.add(jsonError);
			isMark = true;
			imageDescrip = "";
		}
			
		try{
		
		CmsLock lockFile = cmsObject.getLock(imagePath);
		boolean isLock = !lockFile.isUnlocked() ; // true si esta desbloqueado, false si esta bloqueado
		String isLockData = "";
		
		if (isLock){
				// si el usuario que lockea no es el usuario logueado
		        if (!cmsObject.getRequestContext().currentUser().getName().equals(cmsObject.readUser(lockFile.getUserId()).getName())){
			       
					isLockData = "BY " + cmsObject.readUser(lockFile.getUserId()).getFullName();
					
					jsonError.put("code","999.010");
					jsonError.put("data",isLockData);
					jsonErrors.add(jsonError);
					isMark = true;
				
		        }
			
		}
		
		// Seguridad de marca: 
		// Se valida el nivel de cripticidad de la seguridad de marca y se envia el valor mas alto de la misma.
		
		CmsProperty prop =  cmsObject.readPropertyObject(imagePath, "UnsafeLabels", false);
		String UnsafeLabels = (prop != null) ? prop.getValue() : null; 
		
		if (UnsafeLabels != null){
			
			String categoriesCriticalsScape = categoryCritical.toLowerCase();
			categoriesCriticalsScape = categoriesCriticalsScape.replaceAll("á","a");
			categoriesCriticalsScape = categoriesCriticalsScape.replaceAll("é","e");
			categoriesCriticalsScape = categoriesCriticalsScape.replaceAll("í","i");
			categoriesCriticalsScape = categoriesCriticalsScape.replaceAll("ó","o");
			categoriesCriticalsScape = categoriesCriticalsScape.replaceAll("ú","u");
			
			String categoryWarningScape = categoryWarning.toLowerCase();
			categoryWarningScape = categoryWarningScape.replaceAll("á","a");
			categoryWarningScape = categoryWarningScape.replaceAll("é","e");
			categoryWarningScape = categoryWarningScape.replaceAll("í","i");
			categoryWarningScape = categoryWarningScape.replaceAll("ó","o");
			categoryWarningScape = categoryWarningScape.replaceAll("ú","u");
			
			String[] categoryCriticalSpl = categoriesCriticalsScape.split(", ");
			String[] categoryWarningSpl = categoryWarningScape.split(", ");
	
			String unsafeLabelsType = getUnsafeLabelsType(UnsafeLabels, categoriesCriticalsScape, categoryCriticalSpl, categoryWarningSpl);
			
			if (!unsafeLabelsType.equals("")){
					jsonError.put("code","009.008");
					jsonError.put("data",unsafeLabelsType);
					jsonErrors.add(jsonError);
					isMark = true;
			}
		}
		
	
		// calidad de imagen, dimensiones
		if (level == 1){
			prop = cmsObject.readPropertyObject(imagePath, "image.size", false);      
		
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
					jsonErrors.add(jsonError);
					isMark = true;
				}
				
			}
		}
			
		} catch (CmsException e) {
			if(e.getMessage().indexOf("Error reading resource from path") > -1) {
				jsonError.put("code","999.027");
				jsonError.put("data",imagePath);
			}else {
				jsonError.put("code","999.002");
				jsonError.put("data", e.getMessage());
			}

			jsonErrors.add(jsonError);
			isMark = true;
		}
		
		
		jsonItem.put("title", imageDescrip);
		jsonItem.put("type","image");
		jsonItem.put("isMark", isMark);
		
		if (isMark) {
			jsonItem.put("errors",jsonErrors);
		}
		
		return jsonItem;	
	}



	/** fin proceso de check */
	/**
	 * public synchronized void publishNews() throws CmsException { CmsObject cms =
	 * getCmsObject(); if(resources.isEmpty()){ retrieveNews(); } List<CmsResource>
	 * relatedResources = new ArrayList<CmsResource>(); for (CmsResource resource :
	 * resources ) { List<CmsResource> currentRelResources =
	 * addRelatedResourcesToPublish(resource,false,true, new
	 * ArrayList<CmsResource>()); currentRelResources.removeAll(relatedResources);
	 * currentRelResources.removeAll(resources);
	 * relatedResources.addAll(currentRelResources); }
	 * 
	 * resources.addAll(relatedResources);
	 * 
	 * CmsPublishList pList = OpenCms.getPublishManager().getPublishList(cms,
	 * resources, false);
	 * 
	 * 
	 * CmsLogReport report = new CmsLogReport(Locale.ENGLISH,this.getClass());
	 * OpenCms.getPublishManager().publishProject(cms, report, pList);
	 * if(pList.size()<30) OpenCms.getPublishManager().waitWhileRunning();
	 * 
	 * }
	 */

	public List<CmsResource> addRelatedResourcesToPublish(CmsResource resource, boolean unlock,
			boolean recursivePublish, List<CmsResource> resourcesList) {
		CmsObject cms = getCmsObject();

		try {

			int tipoNoticia = OpenCms.getResourceManager().getResourceType("noticia").getTypeId();
			int tipoVideoLink = OpenCms.getResourceManager().getResourceType("video-link").getTypeId();
			int tipoVideo = OpenCms.getResourceManager().getResourceType("video").getTypeId();
			int encuestaType = OpenCms.getResourceManager().getResourceType("encuesta").getTypeId();

			int tipoOrigen = resource.getTypeId();
			boolean esVideoOrigen = (tipoOrigen == tipoVideo) || (tipoOrigen == tipoVideoLink);

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

					if (estado.equals(CmsResourceState.STATE_UNCHANGED))
						continue;

					int tipo = res.getTypeId();

					CmsLock lock = cms.getLock(res);
					boolean esNoticia = (tipo == tipoNoticia);
					boolean esVideo = (tipo == tipoVideo) || (tipo == tipoVideoLink);

					if (!lock.isUnlocked() && unlock) {
						if (lock.getUserId().equals(cms.getRequestContext().currentUser().getId())) {
							cms.unlockResource(cms.getRequestContext().removeSiteRoot(rel));
						} else if (forcePublish) {
							cms.changeLock(cms.getRequestContext().removeSiteRoot(rel));
							cms.unlockResource(cms.getRequestContext().removeSiteRoot(rel));
						} else {
							continue;
						}
					}

					if (esVideoOrigen && esVideo) {
						if (publishRelatedContent)
							if (!resourcesList.contains(res))
								resourcesList.add(res);

					} else if (!esNoticia) {
						if (publishRelatedContent)
							if (!resourcesList.contains(res))
								resourcesList.add(res);

						if (esVideo && publishRelatedContent)
							addRelatedResourcesToPublish(res, unlock, true, resourcesList);
					} else if (publishRelatedNews && recursivePublish) {
						if (!resourcesList.contains(res))
							resourcesList.add(res);
						addRelatedResourcesToPublish(res, unlock, false, resourcesList);

					}

				} catch (CmsException e) {
					e.printStackTrace();
				}
			}
		} catch (CmsException e) {
			e.printStackTrace();
		}
		return resourcesList;

	}

	/**
	 * @param publicationName
	 * @param urlResource
	 * @param returnUrl
	 * @param publicationSource
	 * @return
	 * @throws Exception 
	 */
	public JSONObject copyResource(String publicationName, String urlResource, boolean returnUrl,
			int publicationSource) throws Exception {
		JSONObject result = new JSONObject();
		JSONArray errorsJS = new JSONArray();
		CmsObject cms = getCmsObject();

		String newResource = "";
		String publicationTarget = publicationName;
		String resourceToCopy = urlResource;

		String proyecto = OpenCmsBaseService.getCurrentSite(cms);
		TipoEdicionService tipoEdicionService = new TipoEdicionService();
		TipoEdicion tipoEdicionTarget = tipoEdicionService.obtenerTipoEdicion(publicationTarget, proyecto);
		NoticiasService nService = new NoticiasService();

		String resourceType = "news";

		CmsResource res;
		try {
			res = cms.readResource(urlResource, CmsResourceFilter.IGNORE_EXPIRATION);
			CmsProperty prop = cms.readPropertyObject(res, "newsType", false);
			String targetResourceType = prop.getValue("");

			// Nos fijamos si en la publicacion de destino existe el tipo
			List<String> tiposNoticia = nService.obtenerTiposDeNoticia(cms, tipoEdicionTarget.getId());

			if (tiposNoticia != null && tiposNoticia.size() > 0 && !tiposNoticia.get(0).equals("")) {
				for (String tipoNoticia : tiposNoticia) {
					if (tipoNoticia.equals(targetResourceType))
						resourceType = targetResourceType;
				}
			}

		} catch (CmsException e2) {

			e2.printStackTrace();
		}

		//
		// Creating new resource
		//
		try {
			newResource = nService.crearNoticia(getCmsObject(), tipoEdicionTarget.getId(), resourceType,
					new HashMap<String, String>());
		} catch (CmsLoaderException e1) {
			result.put("status", "error");

			JSONObject error = new JSONObject();
			error.put("path", resourceToCopy);
			error.put("message", e1.getMessage());
			errorsJS.add(error);
		} catch (Exception e) {
			result.put("status", "error");

			JSONObject error = new JSONObject();
			error.put("path", resourceToCopy);
			error.put("message", e.getMessage());
			errorsJS.add(error);
		}

		//
		// Reading and Copying News Information
		//

		try {
			// Lock copy resource
			//CmsResourceUtils.forceLockResource(cms, resourceToCopy);
			CmsFile file = cms.readFile(resourceToCopy, CmsResourceFilter.IGNORE_EXPIRATION);
			CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
			content.setAutoCorrectionEnabled(true);
			content.correctXmlStructure(cms);

			// Lock new resource
			CmsResourceUtils.forceLockResource(cms, newResource);
			CmsFile newFile = cms.readFile(newResource, CmsResourceFilter.IGNORE_EXPIRATION);
			CmsXmlContent newContent = CmsXmlContentFactory.unmarshal(cms, newFile);
			newContent.setAutoCorrectionEnabled(true);
			newContent.correctXmlStructure(cms);

			copyItemsList(content, newContent, cms);

			int tituloCount = getSimpleElementCountWithValue(
					TfsXmlContentNameProvider.getInstance().getTagName("news.title"), content);

			 for (int j = 1; j<=tituloCount; j++) {
				 String titulo = getElementValue("titulo[" + j + "]", content,
						Locale.ENGLISH);
				
				 if (!titulo.isEmpty()) {
					String xmlName = "titulo[" + j + "]";
					I_CmsXmlContentValue value = newContent.getValue(xmlName, Locale.ENGLISH);

					if (value == null && j > 1 ) {
						newContent.addValue(cms, "titulo", Locale.ENGLISH, j - 1);
					}

					newContent.getValue("titulo[" + j + "]", Locale.ENGLISH).setStringValue(cms,
							titulo);
				}
			 }
			 
			newContent.getValue("volanta", Locale.ENGLISH).setStringValue(cms,
					content.getValue("volanta", Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("copete", Locale.ENGLISH).setStringValue(cms,
					content.getValue("copete", Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("cuerpo", Locale.ENGLISH).setStringValue(cms,
					content.getValue("cuerpo", Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("claves", Locale.ENGLISH).setStringValue(cms,
					content.getValue("claves", Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("clavesOcultas", Locale.ENGLISH).setStringValue(cms,
					content.getValue("clavesOcultas", Locale.ENGLISH).getStringValue(cms));

			// Si no es la misma publicación
			// 1. SECCION: validamos que esa existe la seccion en la publicación.
			// 2. AUTOR: validamos que el usuario tenga acceso a la publicación. Caso que no: ponemos lo configurado en el cmsmedios.

			int autorCount = content.getIndexCount(TfsXmlContentNameProvider.getInstance().getTagName("news.authors"), cms.getRequestContext().getLocale());

			if (tipoEdicionTarget.getId() == publicationSource) {
				
				newContent.getValue("seccion", Locale.ENGLISH).setStringValue(cms,
						content.getValue("seccion", Locale.ENGLISH).getStringValue(cms));
								
				for (int j = 1; j <= autorCount; j++) {
					
					if (j > 1 ) {
						newContent.addValue(cms, "autor", Locale.ENGLISH,  j - 1);
					}
					
					String autorNombre = getElementValue("autor[" + j + "]/nombre", content, Locale.ENGLISH);
					if (!autorNombre.isEmpty()) {
						newContent.getValue("autor[" + j + "]/nombre", Locale.ENGLISH).setStringValue(cms, autorNombre);
					}
					 
					String autorinternalUser = getElementValue("autor[" + j + "]/internalUser", content, Locale.ENGLISH);
					if (!autorinternalUser.isEmpty()) {
						newContent.getValue("autor[" + j + "]/internalUser", Locale.ENGLISH).setStringValue(cms, autorinternalUser);
					}
					 
					String autorFoto = getElementValue("autor[" + j + "]/foto", content, Locale.ENGLISH);
					if (!autorFoto.isEmpty()) {
						newContent.getValue("autor[" + j + "]/foto", Locale.ENGLISH).setStringValue(cms, autorFoto);
					}
					
					String autorLugar = getElementValue("autor[" + j + "]/lugar", content, Locale.ENGLISH);
					if (!autorLugar.isEmpty()) {
						newContent.getValue("autor[" + j + "]/lugar", Locale.ENGLISH).setStringValue(cms, autorLugar);
					}

					
					String autorEmail = getElementValue("autor[" + j + "]/email", content, Locale.ENGLISH);
					if (!autorEmail.isEmpty()) {
						newContent.getValue("autor[" + j + "]/foto", Locale.ENGLISH).setStringValue(cms, autorEmail);
					}

					 
					String autor_descripcion = getElementValue("autor[" + j + "]/autor_descripcion", content, Locale.ENGLISH);
						if (!autor_descripcion.isEmpty()) {
							newContent.getValue("autor[" + j + "]/foto", Locale.ENGLISH).setStringValue(cms, autor_descripcion);
						}
					}
			} else {

				String seccionSource = content.getValue("seccion", Locale.ENGLISH).getStringValue(cms);
				SeccionesService sService = new SeccionesService();
				Seccion seccion = sService.obtenerSeccion(seccionSource, tipoEdicionTarget.getId());

				if (seccion != null) {
					if (seccion.getName().equals(seccionSource))
						newContent.getValue("seccion", Locale.ENGLISH).setStringValue(cms,
								content.getValue("seccion", Locale.ENGLISH).getStringValue(cms));
				}
				
				for (int j = 1; j <= autorCount; j++) {
					
					// Al copiar la noticia a otra publicacion se debe verificar si el autor TIENE ACCESO en ambas publicaciones. 
					// si no tiene acceso se debe cambiar la firma. Aca se debe poner la firma predeterminada de la publicacion.
					// Si es la firma del autor se pondrá la de la persona que copia.
					
					if (j > 1 ) {
						newContent.addValue(cms, "autor", Locale.ENGLISH,  j - 1);
					}
					
					String internalUser = content.getValue("autor[" + j + "]/internalUser", Locale.ENGLISH).getStringValue(cms);
					boolean hasGrantUSER = false;
										
					if (!internalUser.equals("")) {
						String internalUserID =  cms.readUser(internalUser).getId().toString();
						
						SecurityService secService = new SecurityService();
						hasGrantUSER = secService.hasGrantAccess("NEWS_CREATE", tipoEdicionTarget.getId(), internalUserID);
					}
										
					if (hasGrantUSER) {
						
						String autorinternalUser = getElementValue("autor[" + j + "]/internalUser", content, Locale.ENGLISH);
						if (!autorinternalUser.isEmpty()) {
							newContent.getValue("autor[" + j + "]/internalUser", Locale.ENGLISH).setStringValue(cms, autorinternalUser);
						}
	
					}else {
				 		
						if(!internalUser.equals("")) {
							
							String autorNombre = getElementValue("autor[" + j + "]/nombre", content, Locale.ENGLISH);
							if (!autorNombre.isEmpty()) {
								newContent.getValue("autor[" + j + "]/nombre", Locale.ENGLISH).setStringValue(cms, autorNombre);
							}
							 
							String autorFoto = getElementValue("autor[" + j + "]/foto", content, Locale.ENGLISH);
							if (!autorFoto.isEmpty()) {
								newContent.getValue("autor[" + j + "]/foto", Locale.ENGLISH).setStringValue(cms, autorFoto);
							}
							
							String autorLugar = getElementValue("autor[" + j + "]/lugar", content, Locale.ENGLISH);
							if (!autorLugar.isEmpty()) {
								newContent.getValue("autor[" + j + "]/lugar", Locale.ENGLISH).setStringValue(cms, autorLugar);
							}

							
							String autorEmail = getElementValue("autor[" + j + "]/email", content, Locale.ENGLISH);
							if (!autorEmail.isEmpty()) {
								newContent.getValue("autor[" + j + "]/foto", Locale.ENGLISH).setStringValue(cms, autorEmail);
							}

							 
							String autor_descripcion = getElementValue("autor[" + j + "]/autor_descripcion", content, Locale.ENGLISH);
								if (!autor_descripcion.isEmpty()) {
									newContent.getValue("autor[" + j + "]/foto", Locale.ENGLISH).setStringValue(cms, autor_descripcion);
								}
						
						}else{
							String authorMode =  configura.getParam(siteName, publicationName, "newsAuthor", "authorMode", "signedByUser");
	
							if (authorMode.equals("signedByAnonymous")) {
								newContent.getValue("autor[" + j + "]/internalUser", Locale.ENGLISH).setStringValue(cms,
										 configura.getParam(siteName, publicationName, "newsAuthor", "signedByAnonymous", ""));
							}else if (authorMode.equals("signedByFreeText")) {
								newContent.getValue("autor[" + j + "]/internalUser", Locale.ENGLISH).setStringValue(cms,
										 configura.getParam(siteName, publicationName, "newsAuthor", "signedByFreeText", ""));
							}else if (authorMode.equals("signedByUser")) {
								newContent.getValue("autor[" + j + "]/internalUser", Locale.ENGLISH).setStringValue(cms,
										cms.getRequestContext().currentUser().getName());
							}
						}
					}
				}
				
				
			}

			newContent.getValue("imagenPrevisualizacion/imagen", Locale.ENGLISH).setStringValue(cms,
					content.getValue("imagenPrevisualizacion/imagen", Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("imagenPrevisualizacion/descripcion", Locale.ENGLISH).setStringValue(cms,
					content.getValue("imagenPrevisualizacion/descripcion", Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("imagenPrevisualizacion/fuente", Locale.ENGLISH).setStringValue(cms,
					content.getValue("imagenPrevisualizacion/fuente", Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("imagenPrevisualizacion/keywords", Locale.ENGLISH).setStringValue(cms,
					content.getValue("imagenPrevisualizacion/keywords", Locale.ENGLISH).getStringValue(cms));

			//
			// Galery Images
			//
			int galleryImagesCount = nService.cantidadDeImagenesEnFotogaleria(cms, cms.readResource(resourceToCopy, CmsResourceFilter.IGNORE_EXPIRATION));

			for (int j = 1; j <= galleryImagesCount; j++) {
				// Imagen Fotogaleria
				String galleryImage = getElementValue("imagenesFotogaleria[" + j + "]/imagen[1]", content,
						Locale.ENGLISH);
				if (!galleryImage.isEmpty()) {
					String xmlName = "imagenesFotogaleria[" + j + "]/imagen[1]";
					I_CmsXmlContentValue value = newContent.getValue(xmlName, Locale.ENGLISH);

					if (value == null) {
						newContent.addValue(cms, "imagenesFotogaleria", Locale.ENGLISH, j - 1);
					}

					newContent.getValue("imagenesFotogaleria[" + j + "]/imagen[1]", Locale.ENGLISH).setStringValue(cms,
							galleryImage);
				}

				// Description de Fotogaleria
				String galleryDescription = getElementValue("imagenesFotogaleria[" + j + "]/descripcion", content,
						Locale.ENGLISH);
				if (!galleryDescription.isEmpty()) {
					newContent.getValue("imagenesFotogaleria[" + j + "]/descripcion", Locale.ENGLISH)
							.setStringValue(cms, galleryDescription);
				}

				// Source de Fotogaleria
				String gallerySource = getElementValue("imagenesFotogaleria[" + j + "]/fuente", content,
						Locale.ENGLISH);
				if (!gallerySource.isEmpty()) {
					newContent.getValue("imagenesFotogaleria[" + j + "]/fuente", Locale.ENGLISH).setStringValue(cms,
							gallerySource);
				}

				// Tags de Fotogaleria
				String galleryTags = getElementValue("imagenesFotogaleria[" + j + "]/keywords", content,
						Locale.ENGLISH);
				if (!galleryTags.isEmpty()) {
					newContent.getValue("imagenesFotogaleria[" + j + "]/keywords", Locale.ENGLISH).setStringValue(cms,
							galleryTags);
				}
			}

			//
			// Social
			//
			
			String tituloFacebook = getElementValue("tituloFacebook", content, Locale.ENGLISH);
			if (!tituloFacebook.isEmpty()) {
				String xmlName = "tituloFacebook";
				I_CmsXmlContentValue value = newContent.getValue(xmlName, Locale.ENGLISH);

				if (value == null) {
					newContent.addValue(cms, "tituloFacebook", Locale.ENGLISH, 0);
				}

				newContent.getValue("tituloFacebook", Locale.ENGLISH).setStringValue(cms,
						tituloFacebook);
			}
			
			String descripcionFacebook = getElementValue("descripcionFacebook", content, Locale.ENGLISH);
			if (!descripcionFacebook.isEmpty()) {
				String xmlName = "descripcionFacebook";
				I_CmsXmlContentValue value = newContent.getValue(xmlName, Locale.ENGLISH);

				if (value == null) {
					newContent.addValue(cms, "descripcionFacebook", Locale.ENGLISH, 0);
				}

				newContent.getValue("descripcionFacebook", Locale.ENGLISH).setStringValue(cms,
						descripcionFacebook);
			}
			
			String KeywordRedesSociales = getElementValue("KeywordRedesSociales", content, Locale.ENGLISH);
			if (!KeywordRedesSociales.isEmpty()) {
				String xmlName = "KeywordRedesSociales";
				I_CmsXmlContentValue value = newContent.getValue(xmlName, Locale.ENGLISH);

				if (value == null) {
					newContent.addValue(cms, "KeywordRedesSociales", Locale.ENGLISH, 0);
				}

				newContent.getValue("KeywordRedesSociales", Locale.ENGLISH).setStringValue(cms,
						KeywordRedesSociales);
			}
			
			String imagenFacebook = getElementValue("imagenFacebook/imagen", content, Locale.ENGLISH);
			if (!imagenFacebook.isEmpty()) {
				String xmlName = "imagenFacebook/imagen";
				I_CmsXmlContentValue value = newContent.getValue(xmlName, Locale.ENGLISH);

				if (value == null) {
					newContent.addValue(cms, "imagenFacebook", Locale.ENGLISH, 0);
				}

				newContent.getValue("imagenFacebook/imagen", Locale.ENGLISH).setStringValue(cms,
						imagenFacebook);
			
			
				newContent.getValue("imagenFacebook/descripcion", Locale.ENGLISH).setStringValue(cms,
						content.getValue("imagenFacebook/descripcion", Locale.ENGLISH).getStringValue(cms));
				
				newContent.getValue("imagenFacebook/fuente", Locale.ENGLISH).setStringValue(cms,
						content.getValue("imagenFacebook/fuente", Locale.ENGLISH).getStringValue(cms));
				
				newContent.getValue("imagenFacebook/keywords", Locale.ENGLISH).setStringValue(cms,
						content.getValue("imagenFacebook/keywords", Locale.ENGLISH).getStringValue(cms));
			}
			
			newContent.getValue("twitterPublish", Locale.ENGLISH).setStringValue(cms,
					content.getValue("twitterPublish", Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("facebookPublish", Locale.ENGLISH).setStringValue(cms,
					content.getValue("facebookPublish", Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("facebookPagePublish", Locale.ENGLISH).setStringValue(cms,
					content.getValue("facebookPagePublish", Locale.ENGLISH).getStringValue(cms));
			
			//
			// Custom
			//

			newContent.getValue("custom1", Locale.ENGLISH).setStringValue(cms,
					content.getValue("custom1", Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("custom2", Locale.ENGLISH).setStringValue(cms,
					content.getValue("custom2", Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("custom3", Locale.ENGLISH).setStringValue(cms,
					content.getValue("custom3", Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("custom4", Locale.ENGLISH).setStringValue(cms,
					content.getValue("custom4", Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("custom5", Locale.ENGLISH).setStringValue(cms,
					content.getValue("custom5", Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("custom6", Locale.ENGLISH).setStringValue(cms,
					content.getValue("custom6", Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("custom7", Locale.ENGLISH).setStringValue(cms,
					content.getValue("custom7", Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("custom8", Locale.ENGLISH).setStringValue(cms,
					content.getValue("custom8", Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("custom9", Locale.ENGLISH).setStringValue(cms,
					content.getValue("custom9", Locale.ENGLISH).getStringValue(cms));
			newContent.getValue("custom10", Locale.ENGLISH).setStringValue(cms,
					content.getValue("custom10", Locale.ENGLISH).getStringValue(cms));
			//
			// Videos
			//
			int flashVideoCount = getElementCountWithValue(
					TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash"),
					TfsXmlContentNameProvider.getInstance().getTagName("news.video.video"), content);
			for (int j = 1; j <= flashVideoCount; j++) {
				// Video de videoFlash
				String videoFlashVideo = getElementValue(
						TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "[" + j + "]/video[1]",
						content, Locale.ENGLISH);
				if (!videoFlashVideo.isEmpty()) {
					String xmlName = TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "[" + j
							+ "]/video[1]";
					I_CmsXmlContentValue value = newContent.getValue(xmlName, Locale.ENGLISH);
					if (value == null) {
						newContent.addValue(cms, TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash"),
								Locale.ENGLISH, j - 1);
					}
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "[" + j
							+ "]/video[1]", Locale.ENGLISH).setStringValue(cms, videoFlashVideo);
				}

				// Imagen de videoFlash
				String videoFlashImage = getElementValue(
						TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "[" + j + "]/imagen[1]",
						content, Locale.ENGLISH);
				if (!videoFlashImage.isEmpty()) {
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "[" + j
							+ "]/imagen[1]", Locale.ENGLISH).setStringValue(cms, videoFlashImage);
				}

				// Titulo de videoFlash
				String videoFlashTitle = getElementValue(
						TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "[" + j + "]/titulo",
						content, Locale.ENGLISH);
				if (!videoFlashTitle.isEmpty()) {
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "[" + j
							+ "]/titulo", Locale.ENGLISH).setStringValue(cms, videoFlashTitle);
				}

				// Imagen de videoFlash
				String videoFlashSource = getElementValue(
						TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "[" + j + "]/fuente",
						content, Locale.ENGLISH);
				if (!videoFlashSource.isEmpty()) {
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "[" + j
							+ "]/fuente", Locale.ENGLISH).setStringValue(cms, videoFlashSource);
				}

				// Imagen de videoFlash
				String videoFlashDescription = getElementValue(
						TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "[" + j
								+ "]/descripcion",
						content, Locale.ENGLISH);
				if (!videoFlashDescription.isEmpty()) {
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "[" + j
							+ "]/descripcion", Locale.ENGLISH).setStringValue(cms, videoFlashDescription);
				}

				// Imagen de videoFlash
				String videoFlashTags = getElementValue(
						TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "[" + j + "]/keywords",
						content, Locale.ENGLISH);
				if (!videoFlashTags.isEmpty()) {
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "[" + j
							+ "]/keywords", Locale.ENGLISH).setStringValue(cms, videoFlashTags);
				}
			}

			int youtubeImagesCount = getElementCountWithValue(
					TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube"),
					TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube.id"), content);
			for (int j = 1; j <= youtubeImagesCount; j++) {
				// Youtube id
				String videoYoutubeid = getElementValue(
						TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube") + "[" + j
								+ "]/youtubeid",
						content, Locale.ENGLISH);
				if (!videoYoutubeid.isEmpty()) {
					String xmlName = TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube") + "[" + j
							+ "]/youtubeid";
					I_CmsXmlContentValue value = newContent.getValue(xmlName, Locale.ENGLISH);
					if (value == null) {
						newContent.addValue(cms,
								TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube"), Locale.ENGLISH,
								j - 1);
					}
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube") + "["
							+ j + "]/youtubeid", Locale.ENGLISH).setStringValue(cms, videoYoutubeid);
				}

				// Youtube image
				String videoYoutubeImage = getElementValue(
						TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube") + "[" + j + "]/imagen",
						content, Locale.ENGLISH);
				if (!videoYoutubeImage.isEmpty()) {
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube") + "["
							+ j + "]/imagen", Locale.ENGLISH).setStringValue(cms, videoYoutubeImage);
				}

				// Youtube Title
				String videoYoutubeTitle = getElementValue(
						TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube") + "[" + j + "]/titulo",
						content, Locale.ENGLISH);
				if (!videoYoutubeTitle.isEmpty()) {
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube") + "["
							+ j + "]/titulo", Locale.ENGLISH).setStringValue(cms, videoYoutubeTitle);
				}

				// Youtube Description
				String videoYoutubeDescription = getElementValue(
						TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube") + "[" + j
								+ "]/descripcion",
						content, Locale.ENGLISH);
				if (!videoYoutubeDescription.isEmpty()) {
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube") + "["
							+ j + "]/descripcion", Locale.ENGLISH).setStringValue(cms, videoYoutubeDescription);
				}

				// Youtube Source
				String videoYoutubeSource = getElementValue(
						TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube") + "[" + j + "]/fuente",
						content, Locale.ENGLISH);
				if (!videoYoutubeSource.isEmpty()) {
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube") + "["
							+ j + "]/fuente", Locale.ENGLISH).setStringValue(cms, videoYoutubeSource);
				}

				// Youtube Tags
				String videoYoutubeTags = getElementValue(
						TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube") + "[" + j
								+ "]/keywords",
						content, Locale.ENGLISH);
				if (!videoYoutubeTags.isEmpty()) {
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube") + "["
							+ j + "]/keywords", Locale.ENGLISH).setStringValue(cms, videoYoutubeTags);
				}
			}

			int embeddedImagesCount = getElementCountWithValue(
					TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded"),
					TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode"), content);
			for (int j = 1; j <= embeddedImagesCount; j++) {
				// Embedded codigo
				String videoEmbeddedCode = getElementValue(
						TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "[" + j + "]/codigo",
						content, Locale.ENGLISH);
				if (!videoEmbeddedCode.isEmpty()) {
					String xmlName = TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "[" + j
							+ "]/codigo";
					I_CmsXmlContentValue value = newContent.getValue(xmlName, Locale.ENGLISH);
					if (value == null) {
						newContent.addValue(cms,
								TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded"),
								Locale.ENGLISH, j - 1);
					}
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "["
							+ j + "]/codigo", Locale.ENGLISH).setStringValue(cms, videoEmbeddedCode);
				}

				// Embedded Image
				String videoEmbeddedImage = getElementValue(
						TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "[" + j + "]/imagen",
						content, Locale.ENGLISH);
				if (!videoEmbeddedImage.isEmpty()) {
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "["
							+ j + "]/imagen", Locale.ENGLISH).setStringValue(cms, videoEmbeddedImage);
				}

				// Embedded Title
				String videoEmbeddedTitle = getElementValue(
						TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "[" + j + "]/titulo",
						content, Locale.ENGLISH);
				if (!videoEmbeddedTitle.isEmpty()) {
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "["
							+ j + "]/titulo", Locale.ENGLISH).setStringValue(cms, videoEmbeddedTitle);
				}

				// Embedded Description
				String videoEmbeddedDescription = getElementValue(
						TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "[" + j
								+ "]/descripcion",
						content, Locale.ENGLISH);
				if (!videoEmbeddedDescription.isEmpty()) {
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "["
							+ j + "]/descripcion", Locale.ENGLISH).setStringValue(cms, videoEmbeddedDescription);
				}

				// Embedded Source
				String videoEmbeddedSource = getElementValue(
						TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "[" + j + "]/fuente",
						content, Locale.ENGLISH);
				if (!videoEmbeddedSource.isEmpty()) {
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "["
							+ j + "]/fuente", Locale.ENGLISH).setStringValue(cms, videoEmbeddedSource);
				}

				// Embedded Tags
				String videoEmbeddedTags = getElementValue(
						TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "[" + j
								+ "]/keywords",
						content, Locale.ENGLISH);
				if (!videoEmbeddedTags.isEmpty()) {
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "["
							+ j + "]/keywords", Locale.ENGLISH).setStringValue(cms, videoEmbeddedTags);
				}
			}

			//
			// LIVEBLOG
			//
			if (resourceType.equals("liveblog")) {
				
				newContent.getValue("liveBlogDetail/titulo", Locale.ENGLISH).setStringValue(cms,
						content.getValue("liveBlogDetail/titulo", Locale.ENGLISH).getStringValue(cms));

				newContent.getValue("liveBlogDetail/descripcion", Locale.ENGLISH).setStringValue(cms,
						content.getValue("liveBlogDetail/descripcion", Locale.ENGLISH).getStringValue(cms));

				newContent.getValue("liveBlogDetail/ubicacionLugar", Locale.ENGLISH).setStringValue(cms,
						content.getValue("liveBlogDetail/ubicacionLugar", Locale.ENGLISH).getStringValue(cms));

				newContent.getValue("liveBlogDetail/ubicacionDireccion", Locale.ENGLISH).setStringValue(cms,
						content.getValue("liveBlogDetail/ubicacionDireccion", Locale.ENGLISH).getStringValue(cms));

				newContent.getValue("liveBlogDetail/ubicacionCodigoPostal", Locale.ENGLISH).setStringValue(cms,
						content.getValue("liveBlogDetail/ubicacionCodigoPostal", Locale.ENGLISH).getStringValue(cms));

				newContent.getValue("liveBlogDetail/ubicacionLocalidad", Locale.ENGLISH).setStringValue(cms,
						content.getValue("liveBlogDetail/ubicacionLocalidad", Locale.ENGLISH).getStringValue(cms));
				
				newContent.getValue("liveBlogDetail/ubicacionRegion", Locale.ENGLISH).setStringValue(cms,
						content.getValue("liveBlogDetail/ubicacionRegion", Locale.ENGLISH).getStringValue(cms));

				newContent.getValue("liveBlogDetail/ubicacionPais", Locale.ENGLISH).setStringValue(cms,
						content.getValue("liveBlogDetail/ubicacionPais", Locale.ENGLISH).getStringValue(cms));

				newContent.getValue("liveBlogDetail/ubicacionCoordenadaX", Locale.ENGLISH).setStringValue(cms,
						content.getValue("liveBlogDetail/ubicacionCoordenadaX", Locale.ENGLISH).getStringValue(cms));

				newContent.getValue("liveBlogDetail/ubicacionCoordenadaY", Locale.ENGLISH).setStringValue(cms,
						content.getValue("liveBlogDetail/ubicacionCoordenadaY", Locale.ENGLISH).getStringValue(cms));

				newContent.getValue("liveBlogConfig/tipo", Locale.ENGLISH).setStringValue(cms,
						content.getValue("liveBlogConfig/tipo", Locale.ENGLISH).getStringValue(cms));

			}
			
			newFile.setContents(newContent.marshal());
			cms.writeFile(newFile);

			copyProperties(urlResource, newResource, cms);

			CmsResourceUtils.unlockResource(cms, newResource, false);

			//CmsResourceUtils.unlockResource(cms, resourceToCopy, false);

			if (returnUrl) {
				result.put("url", newResource);
			} else {
				result.put("status", "ok");
			}

		} catch (CmsException ex) {
			result.put("status", "error");

			JSONObject error = new JSONObject();
			error.put("path", resourceToCopy);
			error.put("message", ex.getMessage());
			errorsJS.add(error);
		}

		result.put("errors", errorsJS);
		return result;
	}

	protected void copyProperties(String urlResource, String newResource, CmsObject cms) {

		String unsafeLabels = "";
		CmsResource res;
		String qualityError = "";
		try {
			res = cms.readResource(urlResource, CmsResourceFilter.IGNORE_EXPIRATION);

			CmsProperty propUnsafeLabels = cms.readPropertyObject(res, "UnsafeLabels", false);
			unsafeLabels = propUnsafeLabels.getValue("");
			if (unsafeLabels == null)
				unsafeLabels = "";

			CmsProperty propQualityError = cms.readPropertyObject(res, "image.qualityError", false);
			qualityError = propQualityError.getValue("");
			if (qualityError == null)
				qualityError = "";
			
			cms.writePropertyObject(newResource, new CmsProperty("adminVersion", "v8", null));			
			cms.writePropertyObject(newResource, new CmsProperty("image.qualityError", qualityError, null));
			cms.writePropertyObject(newResource, new CmsProperty("isACopy", "true", null));
			cms.writePropertyObject(newResource, new CmsProperty("originalNote", urlResource, null));
			cms.writePropertyObject(newResource, new CmsProperty("UnsafeLabels", unsafeLabels, null));


		} catch (CmsException e2) {

			e2.printStackTrace();
		}
	}

	protected void copyItemsList(CmsXmlContent content, CmsXmlContent newContent, CmsObject cms) {

		newContent
				.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemListIntegrated"), Locale.ENGLISH)
				.setStringValue(cms,
						content.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemListIntegrated"),
								Locale.ENGLISH).getStringValue(cms));

		int noticiaListaCount = getElementCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.itemList"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.itemList.title"), content);

		for (int j = 1; j <= noticiaListaCount; j++) {
			String xmlName = "noticiaLista[" + j + "]/titulo";
			I_CmsXmlContentValue value = newContent.getValue(xmlName, Locale.ENGLISH);

			if (value == null)
				newContent.addValue(cms, "noticiaLista", Locale.ENGLISH, j - 1);

			newContent
					.getValue(
							TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/titulo",
							Locale.ENGLISH)
					.setStringValue(cms, content.getValue(
							TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/titulo",
							Locale.ENGLISH).getStringValue(cms));
			newContent
					.getValue(
							TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/cuerpo",
							Locale.ENGLISH)
					.setStringValue(cms, content.getValue(
							TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/cuerpo",
							Locale.ENGLISH).getStringValue(cms));

			// Categories
			int categoriesCount = getElementCountWithValue(
					TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
							+ TfsXmlContentNameProvider.getInstance().getTagName("news.categories"),
					"", content);

			for (int m = 1; m <= categoriesCount; m++) {
				String CategoryValue = newContent.getStringValue(cms,
						TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/Categorias["
								+ m + "]",
						Locale.ENGLISH);

				if (CategoryValue == null)
					newContent.addValue(cms, TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "["
							+ j + "]/Categorias", Locale.ENGLISH, m - 1);

				newContent
						.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
								+ "]/Categorias[" + m + "]", Locale.ENGLISH)
						.setStringValue(
								cms, content
										.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")
												+ "[" + j + "]/Categorias[" + m + "]", Locale.ENGLISH)
										.getStringValue(cms));
			}

			// Imagen
			String imageValue = content.getStringValue(cms,
					TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
							+ "]/imagenlista/imagen",
					Locale.ENGLISH);

			if (imageValue != null && !imageValue.equals("")) {
				String newImageValue = newContent.getStringValue(cms,
						TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
								+ "]/imagenlista/imagen",
						Locale.ENGLISH);

				if (newImageValue == null)
					newContent.addValue(cms, TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "["
							+ j + "]/imagenlista", Locale.ENGLISH, 0);

				newContent
						.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
								+ "]/imagenlista/imagen", Locale.ENGLISH)
						.setStringValue(
								cms, content
										.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")
												+ "[" + j + "]/imagenlista/imagen", Locale.ENGLISH)
										.getStringValue(cms));

				String fotografoValue = content.getStringValue(cms,
						TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
								+ "]/imagenlista/fotografo",
						Locale.ENGLISH);

				if (fotografoValue != null) {

					if (newContent.getStringValue(cms,
							TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
									+ "]/imagenlista[1]/fotografo",
							Locale.ENGLISH) == null)
						newContent.addValue(cms, TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")
								+ "[" + j + "]/imagenlista[1]/fotografo", Locale.ENGLISH, 0);

					newContent
							.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
									+ "]/imagenlista/fotografo", Locale.ENGLISH)
							.setStringValue(
									cms, content
											.getValue(
													TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")
															+ "[" + j + "]/imagenlista/fotografo",
													Locale.ENGLISH)
											.getStringValue(cms));

				}

				newContent
						.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
								+ "]/imagenlista/descripcion", Locale.ENGLISH)
						.setStringValue(
								cms, content
										.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")
												+ "[" + j + "]/imagenlista/descripcion", Locale.ENGLISH)
										.getStringValue(cms));
				newContent
						.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
								+ "]/imagenlista/fuente", Locale.ENGLISH)
						.setStringValue(
								cms, content
										.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")
												+ "[" + j + "]/imagenlista/fuente", Locale.ENGLISH)
										.getStringValue(cms));
				newContent
						.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
								+ "]/imagenlista/keywords", Locale.ENGLISH)
						.setStringValue(
								cms, content
										.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")
												+ "[" + j + "]/imagenlista/keywords", Locale.ENGLISH)
										.getStringValue(cms));

			}

			// videoYouTube
			int youtubeVideosCount = getElementCountWithValue(
					TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/videoYouTube",
					TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube.id"), content);

			for (int y = 1; y <= youtubeVideosCount; y++) {
				String videoYoutubeid = getElementValue(
						TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/videoYouTube"
								+ "[" + y + "]/youtubeid",
						content, Locale.ENGLISH);

				if (!videoYoutubeid.isEmpty()) {
					String xmlNameYt = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
							+ "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube") + "[" + y
							+ "]/youtubeid";
					I_CmsXmlContentValue valueYt = newContent.getValue(xmlNameYt, Locale.ENGLISH);
					if (valueYt == null) {
						newContent.addValue(cms,
								TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
										+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube"),
								Locale.ENGLISH, y - 1);
					}
					newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
							+ "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube") + "[" + y
							+ "]/youtubeid", Locale.ENGLISH).setStringValue(cms, videoYoutubeid);

					// Youtube image
					String videoYoutubeImage = getElementValue(
							TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
									+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube") + "[" + y
									+ "]/imagen",
							content, Locale.ENGLISH);
					if (!videoYoutubeImage.isEmpty()) {
						newContent
								.getValue(
										TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
												+ "]/"
												+ TfsXmlContentNameProvider.getInstance()
														.getTagName("news.videoyoutube")
												+ "[" + y + "]/imagen",
										Locale.ENGLISH)
								.setStringValue(cms, videoYoutubeImage);
					}

					// Youtube Title
					String videoYoutubeTitle = getElementValue(
							TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
									+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube") + "[" + y
									+ "]/titulo",
							content, Locale.ENGLISH);
					if (!videoYoutubeTitle.isEmpty()) {
						newContent
								.getValue(
										TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
												+ "]/"
												+ TfsXmlContentNameProvider.getInstance()
														.getTagName("news.videoyoutube")
												+ "[" + y + "]/titulo",
										Locale.ENGLISH)
								.setStringValue(cms, videoYoutubeTitle);
					}

					// Youtube Description
					String videoYoutubeDescription = getElementValue(
							TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
									+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube") + "[" + y
									+ "]/descripcion",
							content, Locale.ENGLISH);

					if (!videoYoutubeDescription.isEmpty()) {
						newContent
								.getValue(
										TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
												+ "]/"
												+ TfsXmlContentNameProvider.getInstance()
														.getTagName("news.videoyoutube")
												+ "[" + y + "]/descripcion",
										Locale.ENGLISH)
								.setStringValue(cms, videoYoutubeDescription);
					}

					// Youtube Source
					String videoYoutubeSource = getElementValue(
							TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
									+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube") + "[" + y
									+ "]/fuente",
							content, Locale.ENGLISH);

					if (!videoYoutubeSource.isEmpty()) {
						newContent
								.getValue(
										TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
												+ "]/"
												+ TfsXmlContentNameProvider.getInstance()
														.getTagName("news.videoyoutube")
												+ "[" + y + "]/fuente",
										Locale.ENGLISH)
								.setStringValue(cms, videoYoutubeSource);
					}

					String tagNameAutor = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
							+ "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube") + "[" + y
							+ "]/autor";
					newContent.getValue(tagNameAutor, Locale.ENGLISH).setStringValue(cms,
							getElementValue(tagNameAutor, content, Locale.ENGLISH));

					String tagNameCalificacion = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")
							+ "[" + j + "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")
							+ "[" + y + "]/calificacion";
					newContent.getValue(tagNameCalificacion, Locale.ENGLISH).setStringValue(cms,
							getElementValue(tagNameCalificacion, content, Locale.ENGLISH));

					String tagNameMostrarEnHome = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")
							+ "[" + j + "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")
							+ "[" + y + "]/mostrarEnHome";
					newContent.getValue(tagNameMostrarEnHome, Locale.ENGLISH).setStringValue(cms,
							getElementValue(tagNameMostrarEnHome, content, Locale.ENGLISH));

					String tagNameKeywords = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "["
							+ j + "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube") + "["
							+ y + "]/keywords";
					newContent.getValue(tagNameKeywords, Locale.ENGLISH).setStringValue(cms,
							getElementValue(tagNameKeywords, content, Locale.ENGLISH));

					// Categories
					int categoriesCountYt = getElementCountWithValue(
							TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
									+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube"),
							TfsXmlContentNameProvider.getInstance().getTagName("news.video.categories"), content);

					for (int my = 1; my <= categoriesCountYt; my++) {
						String CategoryValue = newContent.getStringValue(cms,
								TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
										+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube") + "["
										+ y + "]/categoria",
								Locale.ENGLISH);

						if (CategoryValue == null)
							newContent.addValue(cms,
									TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
											+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube")
											+ "[" + y + "]/categoria[" + my + "]",
									Locale.ENGLISH, my - 1);

						newContent
								.getValue(
										TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
												+ "]/"
												+ TfsXmlContentNameProvider.getInstance().getTagName(
														"news.videoyoutube")
												+ "[" + y + "]/categoria[" + my + "]",
										Locale.ENGLISH)
								.setStringValue(cms, content.getValue(
										TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
												+ "]/"
												+ TfsXmlContentNameProvider.getInstance().getTagName(
														"news.videoyoutube")
												+ "[" + y + "]/categoria[" + my + "]",
										Locale.ENGLISH).getStringValue(cms));
					}

					String tagNameAutoplay = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "["
							+ j + "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube") + "["
							+ y + "]/autoplay";
					newContent.getValue(tagNameAutoplay, Locale.ENGLISH).setStringValue(cms,
							getElementValue(tagNameAutoplay, content, Locale.ENGLISH));

					String tagNameMute = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
							+ "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube") + "[" + y
							+ "]/mute";
					newContent.getValue(tagNameMute, Locale.ENGLISH).setStringValue(cms,
							getElementValue(tagNameMute, content, Locale.ENGLISH));
				}

			}

			// videoEmbedded
			int embededVideosCount = getElementCountWithValue(
					TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
							+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded"),
					TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode"), content);

			for (int e = 1; e <= embededVideosCount; e++) {
				String videoCode = getElementValue(
						TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
								+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "[" + e
								+ "]/"
								+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode"),
						content, Locale.ENGLISH);

				if (!videoCode.isEmpty()) {
					String xmlNameE = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
							+ "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "[" + e
							+ "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode");
					I_CmsXmlContentValue valueE = newContent.getValue(xmlNameE, Locale.ENGLISH);
					if (valueE == null) {
						newContent.addValue(cms,
								TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
										+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded"),
								Locale.ENGLISH, e - 1);
					}
					newContent
							.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
									+ "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")
									+ "[" + e + "]/"
									+ TfsXmlContentNameProvider.getInstance()
											.getTagName("news.videoembedded.videocode"),
									Locale.ENGLISH)
							.setStringValue(cms, videoCode);

					// Embeded image
					String videoEmbededImage = getElementValue(
							TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
									+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "[" + e
									+ "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.video.thumbnail"),
							content, Locale.ENGLISH);
					if (!videoEmbededImage.isEmpty()) {
						newContent.getValue(
								TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
										+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "["
										+ e + "]/"
										+ TfsXmlContentNameProvider.getInstance().getTagName("news.video.thumbnail"),
								Locale.ENGLISH).setStringValue(cms, videoEmbededImage);
					}

					// Embedded Title
					String videoEmbeddedTitle = getElementValue(
							TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
									+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "[" + e
									+ "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.video.title"),
							content, Locale.ENGLISH);
					if (!videoEmbeddedTitle.isEmpty()) {
						newContent.getValue(
								TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
										+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "["
										+ e + "]/"
										+ TfsXmlContentNameProvider.getInstance().getTagName("news.video.title"),
								Locale.ENGLISH).setStringValue(cms, videoEmbeddedTitle);
					}

					// Embedded Description
					String videoEmbeddedDescription = getElementValue(
							TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
									+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "[" + e
									+ "]/"
									+ TfsXmlContentNameProvider.getInstance().getTagName("news.video.description"),
							content, Locale.ENGLISH);

					if (!videoEmbeddedDescription.isEmpty()) {
						newContent.getValue(
								TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
										+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "["
										+ e + "]/"
										+ TfsXmlContentNameProvider.getInstance().getTagName("news.video.description"),
								Locale.ENGLISH).setStringValue(cms, videoEmbeddedDescription);
					}

					// Embedded Source
					String videoEmbeddedSource = getElementValue(
							TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
									+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "[" + e
									+ "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.video.source"),
							content, Locale.ENGLISH);

					if (!videoEmbeddedSource.isEmpty()) {
						newContent.getValue(
								TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
										+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "["
										+ e + "]/"
										+ TfsXmlContentNameProvider.getInstance().getTagName("news.video.source"),
								Locale.ENGLISH).setStringValue(cms, videoEmbeddedSource);
					}

					String tagNameAutor = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
							+ "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "[" + e
							+ "]/autor";
					newContent.getValue(tagNameAutor, Locale.ENGLISH).setStringValue(cms,
							getElementValue(tagNameAutor, content, Locale.ENGLISH));

					String tagNameCalificacion = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")
							+ "[" + j + "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")
							+ "[" + e + "]/calificacion";
					newContent.getValue(tagNameCalificacion, Locale.ENGLISH).setStringValue(cms,
							getElementValue(tagNameCalificacion, content, Locale.ENGLISH));

					String tagNameKeywords = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "["
							+ j + "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "["
							+ e + "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.video.keywords");
					newContent.getValue(tagNameKeywords, Locale.ENGLISH).setStringValue(cms,
							getElementValue(tagNameKeywords, content, Locale.ENGLISH));

					// Categories
					int categoriesCountE = getElementCountWithValue(
							TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
									+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded"),
							TfsXmlContentNameProvider.getInstance().getTagName("news.video.categories"), content);

					for (int me = 1; me <= categoriesCountE; me++) {
						String CategoryValue = newContent.getStringValue(cms,
								TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
										+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded") + "["
										+ e + "]/"
										+ TfsXmlContentNameProvider.getInstance().getTagName("news.video.categories"),
								Locale.ENGLISH);

						if (CategoryValue == null)
							newContent.addValue(cms,
									TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
											+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded")
											+ "[" + e + "]/"
											+ TfsXmlContentNameProvider.getInstance()
													.getTagName("news.video.categories")
											+ "[" + me + "]",
									Locale.ENGLISH, me - 1);

						newContent
								.getValue(
										TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
												+ "]/"
												+ TfsXmlContentNameProvider.getInstance()
														.getTagName("news.videoembedded")
												+ "[" + e + "]/"
												+ TfsXmlContentNameProvider.getInstance()
														.getTagName("news.video.categories")
												+ "[" + me + "]",
										Locale.ENGLISH)
								.setStringValue(cms,
										content.getValue(
												TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")
														+ "[" + j + "]/"
														+ TfsXmlContentNameProvider
																.getInstance().getTagName("news.videoembedded")
														+ "[" + e + "]/"
														+ TfsXmlContentNameProvider.getInstance()
																.getTagName("news.video.categories")
														+ "[" + me + "]",
												Locale.ENGLISH).getStringValue(cms));
					}

				}
			}

			// videoFlash
			int flashVideosCount = getElementCountWithValue(
					TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
							+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash"),
					TfsXmlContentNameProvider.getInstance().getTagName("news.video.video"), content);

			for (int f = 1; f <= flashVideosCount; f++) {
				String videoFlash = getElementValue(
						TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
								+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "[" + f + "]/"
								+ TfsXmlContentNameProvider.getInstance().getTagName("news.video.video"),
						content, Locale.ENGLISH);

				if (!videoFlash.isEmpty()) {
					String xmlNameF = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
							+ "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "[" + f
							+ "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.video.video");
					I_CmsXmlContentValue valueF = newContent.getValue(xmlNameF, Locale.ENGLISH);
					if (valueF == null) {
						newContent.addValue(cms,
								TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
										+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash"),
								Locale.ENGLISH, f - 1);
					}
					newContent.getValue(
							TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
									+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "[" + f
									+ "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.video.video"),
							Locale.ENGLISH).setStringValue(cms, videoFlash);

					// Video nativo image
					String videoFlashImage = getElementValue(
							TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
									+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "[" + f
									+ "]/imagen"
									+ TfsXmlContentNameProvider.getInstance().getTagName("news.video.thumbnail"),
							content, Locale.ENGLISH);
					if (!videoFlashImage.isEmpty()) {
						newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "["
								+ j + "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "["
								+ f + "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.video.thumbnail"),
								Locale.ENGLISH).setStringValue(cms, videoFlashImage);
					}

					// Flash Title
					String videoFlaseTitle = getElementValue(
							TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
									+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "[" + f
									+ "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.video.title"),
							content, Locale.ENGLISH);
					if (!videoFlaseTitle.isEmpty()) {
						newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "["
								+ j + "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "["
								+ f + "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.video.title"),
								Locale.ENGLISH).setStringValue(cms, videoFlaseTitle);
					}

					// Flash Description
					String videoFlashDescription = getElementValue(
							TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
									+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "[" + f
									+ "]/"
									+ TfsXmlContentNameProvider.getInstance().getTagName("news.video.description"),
							content, Locale.ENGLISH);

					if (!videoFlashDescription.isEmpty()) {
						newContent.getValue(
								TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
										+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "["
										+ f + "]/"
										+ TfsXmlContentNameProvider.getInstance().getTagName("news.video.description"),
								Locale.ENGLISH).setStringValue(cms, videoFlashDescription);
					}

					// flash Source
					String videoFlashSource = getElementValue(
							TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
									+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "[" + f
									+ "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.video.source"),
							content, Locale.ENGLISH);

					if (!videoFlashSource.isEmpty()) {
						newContent.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "["
								+ j + "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "["
								+ f + "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.video.source"),
								Locale.ENGLISH).setStringValue(cms, videoFlashSource);
					}

					String tagNameAutor = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
							+ "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "[" + f
							+ "]/autor";
					newContent.getValue(tagNameAutor, Locale.ENGLISH).setStringValue(cms,
							getElementValue(tagNameAutor, content, Locale.ENGLISH));

					String tagNameCalificacion = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")
							+ "[" + j + "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")
							+ "[" + f + "]/calificacion";
					newContent.getValue(tagNameCalificacion, Locale.ENGLISH).setStringValue(cms,
							getElementValue(tagNameCalificacion, content, Locale.ENGLISH));

					String tagNameHideComments = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")
							+ "[" + j + "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")
							+ "[" + f + "]/ocultarComentarios";
					newContent.getValue(tagNameHideComments, Locale.ENGLISH).setStringValue(cms,
							getElementValue(tagNameHideComments, content, Locale.ENGLISH));

					String tagNameMostrarEnHome = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")
							+ "[" + j + "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")
							+ "[" + f + "]/mostrarEnHome";
					newContent.getValue(tagNameMostrarEnHome, Locale.ENGLISH).setStringValue(cms,
							getElementValue(tagNameMostrarEnHome, content, Locale.ENGLISH));

					String tagNameKeywords = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "["
							+ j + "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "[" + f
							+ "]/keywords";
					newContent.getValue(tagNameKeywords, Locale.ENGLISH).setStringValue(cms,
							getElementValue(tagNameKeywords, content, Locale.ENGLISH));

					// Categories
					int categoriesCountF = getElementCountWithValue(
							TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
									+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash"),
							TfsXmlContentNameProvider.getInstance().getTagName("news.video.categories"), content);

					for (int mf = 1; mf <= categoriesCountF; mf++) {
						String CategoryValue = newContent.getStringValue(cms,
								TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
										+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "["
										+ f + "]/categoria",
								Locale.ENGLISH);

						if (CategoryValue == null)
							newContent.addValue(cms,
									TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
											+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")
											+ "[" + f + "]/categoria[" + mf + "]",
									Locale.ENGLISH, mf - 1);

						newContent
								.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
										+ "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")
										+ "[" + f + "]/categoria[" + mf + "]", Locale.ENGLISH)
								.setStringValue(cms, content
										.getValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList")
												+ "[" + j + "]/"
												+ TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash")
												+ "[" + f + "]/categoria[" + mf + "]", Locale.ENGLISH)
										.getStringValue(cms));
					}

					String tagNameAutoplay = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "["
							+ j + "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "[" + f
							+ "]/autoplay";
					newContent.getValue(tagNameAutoplay, Locale.ENGLISH).setStringValue(cms,
							getElementValue(tagNameAutoplay, content, Locale.ENGLISH));

					String tagNameMute = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j
							+ "]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash") + "[" + f
							+ "]/mute";
					newContent.getValue(tagNameMute, Locale.ENGLISH).setStringValue(cms,
							getElementValue(tagNameMute, content, Locale.ENGLISH));
				}

			}

			// noticia
			String tagNameNoticia = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
					+ TfsXmlContentNameProvider.getInstance().getTagName("news.itemList.relatedNews");

			if (content.getStringValue(cms, tagNameNoticia, Locale.ENGLISH) != null) {

				String noticiaValue = newContent.getStringValue(cms,
						TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
								+ TfsXmlContentNameProvider.getInstance().getTagName("news.itemList.relatedNews")
								+ "[1]",
						Locale.ENGLISH);

				if (noticiaValue == null)
					newContent.addValue(cms,
							TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
									+ TfsXmlContentNameProvider.getInstance().getTagName("news.itemList.relatedNews"),
							Locale.ENGLISH, 0);

				newContent.getValue(tagNameNoticia, Locale.ENGLISH).setStringValue(cms,
						getElementValue(tagNameNoticia, content, Locale.ENGLISH));
			}

			// fecha
			String tagNameFecha = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "[" + j + "]/"
					+ TfsXmlContentNameProvider.getInstance().getTagName("news.itemList.date");
			newContent.getValue(tagNameFecha, Locale.ENGLISH).setStringValue(cms,
					getElementValue(tagNameFecha, content, Locale.ENGLISH));
		}
	}

	public JSONObject save(String path, String title,String zoneHome, String zoneHomePriority, String zoneSection, String zoneSectionPriority, String hideAds, String strLastModifDate) {

		JSONObject result = new JSONObject();
		JSONArray errorsJS = new JSONArray();
		CmsObject cms = getCmsObject();

			String lastModification = "";

			if (!strLastModifDate.equals("") || !strLastModifDate.equals("0"))
				lastModification = strLastModifDate;

			try {
				String tempPath = CmsWorkplace.getTemporaryFileName(path);

				if (getCmsObject().existsResource(tempPath, CmsResourceFilter.ALL)) {
					CmsResourceUtils.forceLockResource(cms, tempPath);
					cms.deleteResource(tempPath, CmsResource.DELETE_PRESERVE_SIBLINGS);
				}

				CmsResourceUtils.forceLockResource(cms, path);

				CmsFile file = cms.readFile(path, CmsResourceFilter.ALL);

				CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
				content.setAutoCorrectionEnabled(true);
				content.correctXmlStructure(cms);
				
				if (!title.equals(""))
					content.getValue("titulo", Locale.ENGLISH).setStringValue(cms, title);
				if (!zoneHome.equals(""))
					content.getValue("zonahome", Locale.ENGLISH).setStringValue(cms, zoneHome);
				if (!zoneHomePriority.equals(""))
					content.getValue("prioridadhome", Locale.ENGLISH).setStringValue(cms, zoneHomePriority);
				if (!zoneSection.equals(""))
					content.getValue("zonaseccion", Locale.ENGLISH).setStringValue(cms, zoneSection);
				if (!zoneSectionPriority.equals(""))
					content.getValue("prioridadseccion", Locale.ENGLISH).setStringValue(cms, zoneSectionPriority);
				if (!hideAds.equals(""))
					content.getValue("ocultarAnuncios", Locale.ENGLISH).setStringValue(cms, hideAds);
				if (!lastModification.equals(""))
					content.getValue("ultimaModificacion", Locale.ENGLISH).setStringValue(cms,
							String.valueOf(lastModification));


				String fileEncoding = getFileEncoding(cms, file.getRootPath());

				String decodedContent = content.toString();
				decodedContent = decodedContent.replaceAll("(?!\\n)[\\p{C}]", "");

				file.setContents(decodedContent.getBytes(fileEncoding));
				cms.writeFile(file);

				cms.unlockResource(path);
				result.put("status", "ok");

			} catch (Exception e) {

				LOG.error("Error trying to save resource " + path + " in site " + cms.getRequestContext().getSiteRoot()
						+ " (" + cms.getRequestContext().currentProject().getName() + ")", e);

				result.put("status", "error");

				JSONObject error = new JSONObject();
				error.put("path", path);
				error.put("message", e.getMessage());

				try {
					CmsLock lockE = cms.getLock(path);

					if (!lockE.isUnlocked()) {
						CmsUUID userId = lockE.getUserId();
						CmsUser lockUser = cms.readUser(userId);

						error.put("lockby", lockUser.getFullName());
					}
				} catch (CmsException e2) {
				}

				errorsJS.add(error);
			}
//		}

		result.put("errors", errorsJS);

		return result;
	}
	    	
	
	
	
	public JSONObject updateEntities(String path, JSONArray tagJson, JSONArray tagsHiddenJson) {
		
		JSONObject result = new JSONObject();
		JSONArray errorsJS = new JSONArray();
		CmsObject cms = getCmsObject();

		String enableEntitiesDetection = configura.getParam(siteName, jsonRequest.getJSONObject("authentication").getString("publication"), "adminNewsConfiguration", "enableEntitiesDetection", "true");
		
		if (enableEntitiesDetection.equals("true")) {

			try {
				CmsResourceUtils.forceLockResource(cms, path);

				CmsFile file = cms.readFile(path, CmsResourceFilter.ALL);

				CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
				content.setAutoCorrectionEnabled(true);
				content.correctXmlStructure(cms);
				
				String tags = "";
				String COMMA = ""; 
				for (int i=0; i < tagJson.size(); i++) {
					tags = tags + COMMA + tagJson.getString(i);
					COMMA = ", "; 
				}

				if (!tags.equals("")) {
					String tagsExist = content.getValue("claves", Locale.ENGLISH).getStringValue(cms);
					if (!tagsExist.equals(""))
						tags = tagsExist+", "+tags;
					
					content.getValue("claves", Locale.ENGLISH).setStringValue(cms, tags);
				}
				
				String tagsHidden = "";
				COMMA = ""; 
				for (int j=0; j < tagsHiddenJson.size(); j++) {
					tagsHidden = tagsHidden + COMMA + tagsHiddenJson.getString(j);
					COMMA = ", "; 
				}
				if (!tagsHidden.equals("")) {
					String tagsHiddenExist = content.getValue("clavesOcultas", Locale.ENGLISH).getStringValue(cms);
					if (!tagsHiddenExist.equals(""))
						tagsHidden = tagsHiddenExist+", "+tagsHidden;
					
					content.getValue("clavesOcultas", Locale.ENGLISH).setStringValue(cms, tagsHidden);
				}

				String fileEncoding = getFileEncoding(cms, file.getRootPath());

				String decodedContent = content.toString();
				decodedContent = decodedContent.replaceAll("(?!\\n)[\\p{C}]", "");

				// file.setContents(content.marshal());
				file.setContents(decodedContent.getBytes(fileEncoding));
				cms.writeFile(file);

				cms.unlockResource(path);
				result.put("status", "ok");

			} catch (Exception e) {

				LOG.error("Error trying to save resource " + path + " in site " + cms.getRequestContext().getSiteRoot()
						+ " (" + cms.getRequestContext().currentProject().getName() + ")", e);

				result.put("status", "error");

				JSONObject error = new JSONObject();
				error.put("path", path);
				error.put("message", e.getMessage());

				try {
					CmsLock lockE = cms.getLock(path);

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
	 * Proceso para validar las noticias a modificar de forma massiva.
	 * @param newPath
	 * @param cmsObject
	 * @param pageContext
	 * @return
	 * @throws Exception 
	 */
		
	public JSONObject checkMassiveNewToSave(String newPath, CmsObject cmsObject, PageContext pageContext, boolean entitiesDetection) throws Exception {
			
			JSONObject jsonItem = new JSONObject();
			
			CmsResource resourceNew;
		
			try {
				resourceNew = cmsObject.readResource(newPath, CmsResourceFilter.IGNORE_EXPIRATION);
				
				/** se valida nivel 0 de errores de Noticias
				*	- hasDraft
				*	- Islock
				*	- Estado pendiente de publicacion
				*	- permisoso insuficientes
				*	- categorias repetidas
				* 	- Si cumple o no con los criterios de compliance
				*/
				jsonItem = checkNew(resourceNew, cmsObject, pageContext, 0);
	
			} catch (CmsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			jsonItem.put("relations",jsonRelationsOk);
			
			
			return jsonItem;
	}

	public JSONObject saveMassive() {
		JSONObject result = new JSONObject();
		JSONArray errorsJS = new JSONArray();
		CmsObject cms = getCmsObject();

		JSONArray news = jsonRequest.getJSONObject("news").getJSONArray("new");
		String zoneHome = (jsonRequest.getJSONObject("news").has("zoneHome"))
				? jsonRequest.getJSONObject("news").getString("zoneHome")
				: "";
		String zoneHomePriority = (jsonRequest.getJSONObject("news").has("zoneHomePriority"))
				? jsonRequest.getJSONObject("news").getString("zoneHomePriority")
				: "";
		String zoneSection = (jsonRequest.getJSONObject("news").has("zoneSection"))
				? jsonRequest.getJSONObject("news").getString("zoneSection")
				: "";
		String zoneSectionPriority = (jsonRequest.getJSONObject("news").has("zoneSectionPriority"))
				? jsonRequest.getJSONObject("news").getString("zoneSectionPriority")
				: "";
		String section = (jsonRequest.getJSONObject("news").has("section"))
				? jsonRequest.getJSONObject("news").getString("section")
				: "";
		String keywords = (jsonRequest.getJSONObject("news").has("keywords"))
				? jsonRequest.getJSONObject("news").getString("keywords")
				: "";

		for (int idx = 0; idx < news.size(); idx++) {

			JSONObject noticia = news.getJSONObject(idx);
			String path = noticia.getString("path");

			try {
				String tempPath = CmsWorkplace.getTemporaryFileName(path);

				if (getCmsObject().existsResource(tempPath, CmsResourceFilter.ALL)) {
					CmsResourceUtils.forceLockResource(cms, tempPath);
					cms.deleteResource(tempPath, CmsResource.DELETE_PRESERVE_SIBLINGS);
				}

				CmsResourceUtils.forceLockResource(cms, path);

				CmsFile file = cms.readFile(path, CmsResourceFilter.ALL);

				CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
				content.setAutoCorrectionEnabled(true);
				content.correctXmlStructure(cms);

				if (!section.equals(""))
					content.getValue("seccion", Locale.ENGLISH).setStringValue(cms, section);
				if (!zoneHome.equals(""))
					content.getValue("zonahome", Locale.ENGLISH).setStringValue(cms, zoneHome);
				if (!zoneHomePriority.equals(""))
					content.getValue("prioridadhome", Locale.ENGLISH).setStringValue(cms, zoneHomePriority);
				if (!zoneSection.equals(""))
					content.getValue("zonaseccion", Locale.ENGLISH).setStringValue(cms, zoneSection);
				if (!zoneSectionPriority.equals(""))
					content.getValue("prioridadseccion", Locale.ENGLISH).setStringValue(cms, zoneSectionPriority);
				if (!keywords.equals(""))
					content.getValue("claves", Locale.ENGLISH).setStringValue(cms, keywords);

				String fileEncoding = getFileEncoding(cms, file.getRootPath());

				String decodedContent = content.toString();
				decodedContent = decodedContent.replaceAll("(?!\\n)[\\p{C}]", "");

				// file.setContents(content.marshal());
				file.setContents(decodedContent.getBytes(fileEncoding));
				cms.writeFile(file);

				cms.unlockResource(path);
				result.put("status", "ok");

				resources.add(getCmsObject().readResource(path, CmsResourceFilter.ALL));

			} catch (Exception e) {

				LOG.error("Error trying to save resource " + path + " in site " + cms.getRequestContext().getSiteRoot()
						+ " (" + cms.getRequestContext().currentProject().getName() + ")", e);

				result.put("status", "error");

				JSONObject error = new JSONObject();
				error.put("path", path);
				error.put("message", e.getMessage());

				try {
					CmsLock lockE = cms.getLock(path);

					if (!lockE.isUnlocked()) {
						CmsUUID userId = lockE.getUserId();
						CmsUser lockUser = cms.readUser(userId);

						error.put("lockby", lockUser.getFullName());
					}
				} catch (CmsException e2) {
				}

				errorsJS.add(error);
			}
		}

		result.put("errors", errorsJS);

		return result;
	}

	public JSONObject expire() {
		JSONObject result = new JSONObject();
		JSONArray errorsJS = new JSONArray();
		CmsObject cms = getCmsObject();

		JSONArray news = jsonRequest.getJSONObject("news").getJSONArray("new");

		for (int idx = 0; idx < news.size(); idx++) {

			JSONObject noticia = news.getJSONObject(idx);
			String path = noticia.getString("path");

			try {
				
				CmsFile file = cms.readFile(path, CmsResourceFilter.ALL);
				CmsResource newsResource = getCmsObject().readResource(path, CmsResourceFilter.ALL);
				/*
				CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
				
				try {	
					CmsXmlUtils.validateXmlStructure(file.getContents(), new CmsXmlEntityResolver(cms));
				} catch (CmsException e) {
					CmsResourceUtils.forceLockResource(cms, cms.getSitePath(newsResource));
					content.setAutoCorrectionEnabled(true);
			        content.correctXmlStructure(cms);
			        CmsResourceUtils.unlockResource(cms, cms.getSitePath(newsResource), false);
				}
				
				file.setContents(content.marshal());
				*/
				String tempPath = CmsWorkplace.getTemporaryFileName(path);

				if (getCmsObject().existsResource(tempPath, CmsResourceFilter.ALL)) {
					CmsResourceUtils.forceLockResource(cms, tempPath);
					cms.deleteResource(tempPath, CmsResource.DELETE_PRESERVE_SIBLINGS);
				}

				CmsResourceUtils.forceLockResource(cms, path);


				long expireDate = System.currentTimeMillis() - (60 * 60 * 1000);

				cms.setDateExpired(path, expireDate, false);
				CmsResource resource = cms.readResource(path, CmsResourceFilter.IGNORE_EXPIRATION);
				
				newsResource.setState(CmsResourceState.STATE_CHANGED);
				//file.setDateExpired(expireDate);
								
				//cms.lockResource(path);
				//cms.writeFile(file);
				//cms.unlockResource(path);

				resources.add(resource);

				result.put("status", "ok");

			} catch (Exception e) {

				LOG.error("Error trying to save resource " + path + " in site " + cms.getRequestContext().getSiteRoot()
						+ " (" + cms.getRequestContext().currentProject().getName() + ")", e);

				result.put("status", "error");

				JSONObject error = new JSONObject();
				error.put("path", path);
				error.put("message", e.getMessage());

				try {
					CmsLock lockE = cms.getLock(path);

					if (!lockE.isUnlocked()) {
						CmsUUID userId = lockE.getUserId();
						CmsUser lockUser = cms.readUser(userId);

						error.put("lockby", lockUser.getFullName());
					}
				} catch (CmsException e2) {
				}

				errorsJS.add(error);
			}
		}

		result.put("errors", errorsJS);

		return result;
	}

	public void publishScheduled() throws CmsException, ParseException {
		// get the request parameters for resource and publish scheduled date
		JSONArray news = jsonRequest.getJSONObject("news").getJSONArray("new");

		for (int idx = 0; idx < news.size(); idx++) {

			JSONObject noticia = news.getJSONObject(idx);
			String path = noticia.getString("path");

			String userName = getCmsObject().getRequestContext().currentUser().getName();

			Date publishDate = jsonRequest.getJSONObject("news").has("publishDate")
					? new Date(Long.parseLong(jsonRequest.getJSONObject("news").getString("publishDate")))
					: null;
			String cronExpresion = jsonRequest.getJSONObject("news").has("cronExpresion")
					? jsonRequest.getJSONObject("news").getString("cronExpresion")
					: "";
			String cronDescription = jsonRequest.getJSONObject("news").has("cronDescription")
					? jsonRequest.getJSONObject("news").getString("cronDescription")
					: "";

			// get the java date format
			// DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT,
			// DateFormat.SHORT, getLocale());
			// Date date = dateFormat.parse(publishDateString);

			// check if the selected date is in the future
			if (publishDate != null && (publishDate.getTime() < new Date().getTime())) {
				// the selected date in in the past, this is not possible
				throw new CmsException(
						Messages.get().container(Messages.ERR_PUBLISH_SCHEDULED_DATE_IN_PAST_1, publishDate));
			}

			// make copies from the admin cmsobject and the user cmsobject
			// get the admin cms object
			CmsWorkplaceAction action = CmsWorkplaceAction.getInstance();
			CmsObject cmsAdmin = action.getCmsAdminObject();
			// get the user cms object
			CmsObject cms = OpenCms.initCmsObject(getCmsObject());

			// set the current user site to the admin cms object
			cmsAdmin.getRequestContext().setSiteRoot(cms.getRequestContext().getSiteRoot());

			// create the temporary project, which is deleted after publishing
			// the publish scheduled date in project name
			String jobDte = "";
		
			System.out.println("Se solicito la publicacion de " + path);
			
			if (publishDate != null) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(publishDate);
				jobDte = dateFormat.toLowerCase();
				
				LOG.debug("LOGPROGRAMAR publishDate del front: " + publishDate);
				LOG.debug("LOGPROGRAMAR dateFormat configurada: " + dateFormat);
				jobDte = jobDte.replaceAll("dd", String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
				jobDte = jobDte.replaceAll("mm", String.valueOf(calendar.get(Calendar.MONTH) + 1));
				jobDte = jobDte.replaceAll("yyyy", String.valueOf(calendar.get(Calendar.YEAR)));
				
				LOG.debug("LOGPROGRAMAR timeFormat configurado: " + timeFormat);
				if (timeFormat.equals("12h"))
					jobDte = jobDte + " on "+  String.valueOf(calendar.get(Calendar.HOUR)) +":"+ String.format("%02d",calendar.get(Calendar.MINUTE))  + String.valueOf(calendar.get(Calendar.AM_PM)); 
				else
					jobDte = jobDte + " on "+  String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) +":"+ String.format("%02d",calendar.get(Calendar.MINUTE)); 
				
			}else {
				jobDte = cronDescription;
			}
					
			//LOG.debug("LOGPROGRAMAR jobDescription Final" + jobDte);
			
			// the resource name to publish scheduled
			String resName = path;
			String projectName = "Publish " + resName + " at " + jobDte + " in " + siteName;
			// the HTML encoding for slashes is necessary because of the slashes in english
			// date time format
			// in project names slahes are not allowed, because these are separators for
			// organizaional units
			projectName = projectName.replace("/", "&#47;");
			// create the project
			CmsProject tmpProject = cmsAdmin.createProject(projectName, "", CmsRole.WORKPLACE_USER.getGroupName(),
					CmsRole.PROJECT_MANAGER.getGroupName(), CmsProject.PROJECT_TYPE_TEMPORARY);
			// make the project invisible for all users
			tmpProject.setHidden(true);
			// write the project to the database
			cmsAdmin.writeProject(tmpProject);
			// set project as current project
			cmsAdmin.getRequestContext().setCurrentProject(tmpProject);
			cms.getRequestContext().setCurrentProject(tmpProject);

			// copy the resource to the project
			cmsAdmin.copyResourceToProject(path);
	
			System.out.println("Se creo el proyecto para publicacion " + projectName);
			
			//SETEAMOS LA PROPERTY PARA INDICAR QUE LA NOTA ESTA PROGRAMA.
			CmsResourceUtils.forceLockResource(cms, path);
			CmsProperty prop = new CmsProperty("isScheduled", null, "true", true);
			cms.writePropertyObject(path, prop);
			
			//SETEAMOS el nobre del job por si hay que despublicarlo.
			prop = new CmsProperty("isScheduledData", null, projectName, true);
			cms.writePropertyObject(path, prop);

			// lock the resource in the current project
			CmsLock lock = cms.getLock(path);
			// prove is current lock from current but not in current project
			if ((lock != null) && lock.isOwnedBy(cms.getRequestContext().currentUser())
					&& !lock.isOwnedInProjectBy(cms.getRequestContext().currentUser(),
							cms.getRequestContext().currentProject())) {
				// file is locked by current user but not in current project
				// change the lock from this file
				cms.changeLock(path);
			}
			// lock resource from current user in current project
			cms.lockResource(path);
			// get current lock
			lock = cms.getLock(path);

			// Agreagamos tambien los recursos relacionados
			CmsResource resource = cms.readResource(cms.getRequestContext().removeSiteRoot(path),
					CmsResourceFilter.ALL);
			List<CmsResource> relatedResources = new ArrayList<CmsResource>();
			relatedResources = addRelatedResourcesToPublish(resource, false, true, new ArrayList<CmsResource>());

			for (CmsResource relatedResource : relatedResources) {
				String rel_resourceName = cms.getRequestContext().removeSiteRoot(relatedResource.getRootPath());
				
				
				//System.out.println("Verificando la inclusion del recurso relacionado " +rel_resourceName);
				CmsLock lockRel = cms.getLock(rel_resourceName);

				
				if (lockRel.isExclusive() && lockRel.getProject().getName().startsWith("Publish")) {
					
					//La noticia esta lockeada para publicacion.
					String progProjectName = lockRel.getProject().getName();

					//System.out.println("El recurso " + rel_resourceName + " se encuentra bloqueado para publicación en el proyecto " + progProjectName.replaceAll("&#47;", "/"));

					String timeRegex = ".* at ([0-9]{1,2}/[0-9]{1,2}/[0-9]{4}) on ([0-9]{1,2}:[0-9]{1,2})([0-1]{0,1}) in .*";
					Pattern pattern = Pattern.compile(timeRegex);
					Matcher matcher = pattern.matcher(progProjectName.replaceAll("&#47;", "/"));
					if (matcher.matches()) {
						
						
						String progAMPM = null;
					    String progDate = matcher.group(1);
					    String progTime = matcher.group(2);
					    if (matcher.groupCount()>=3)
					    	progAMPM = matcher.group(3);
			
					    String allDate = progDate+ " " + progTime;
					    
					    String progjobDte = dateFormat + " ";
					    if (timeFormat.equals("12h")) {
					    	if (progAMPM=="0")
					    		allDate += " AM";
					    	else 
					    		allDate += " PM";
					    	progjobDte += "KK:mm a";
					    }
					    else {
					    	progjobDte +=  "HH:mm";
						    
					    }
					    
					    //System.out.println("La fecha programada es " + allDate);
					    //System.out.println("LA programación corresponde a " + progjobDte);
					    SimpleDateFormat sdf = new SimpleDateFormat(progjobDte);
					    Date existProgramedDate = sdf.parse(allDate);
					    //System.out.println("existProgramedDate " + existProgramedDate);
					    
					    if (existProgramedDate.before(publishDate))
					    	continue;
					    
					    //System.out.println("El recurso se establecera a publicar con el proyecto actual");
					    
					}
				}
				
				//if ((lockRel != null) && lockRel.isOwnedBy(cms.getRequestContext().currentUser())
				//		&& !lockRel.isOwnedInProjectBy(cms.getRequestContext().currentUser(),
				//				cms.getRequestContext().currentProject())) {
					
				//}
				
				if (!lockRel.isUnlocked())
					cms.changeLock(rel_resourceName);
				else 
					cms.lockResource(rel_resourceName);
				
				cmsAdmin.copyResourceToProject(rel_resourceName);
				
				

				//cms.lockResource(rel_resourceName);
			}
			
			// create a new scheduled job
			CmsScheduledJobInfo job = new CmsScheduledJobInfo();
			// the job name
			String jobName = projectName;
			// set the job parameters
			job.setJobName(jobName);
			job.setClassName("org.opencms.scheduler.jobs.CmsPublishScheduledJob");
			// create and set the cron expression

			LOG.debug("LOGPROGRAMAR publishDate : " + publishDate);
			
			if (publishDate != null && cronExpresion.equals("")) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(publishDate);
				String cronExpr = "" + calendar.get(Calendar.SECOND) + " " + calendar.get(Calendar.MINUTE) + " "
						+ calendar.get(Calendar.HOUR_OF_DAY) + " " + calendar.get(Calendar.DAY_OF_MONTH) + " "
						+ (calendar.get(Calendar.MONTH) + 1) + " " + "?" + " " + calendar.get(Calendar.YEAR);
				job.setCronExpression(cronExpr);
				
				LOG.debug("LOGPROGRAMAR cronExpr: " + cronExpr);
				
			} else {
				// publicar recursivamente.
				job.setCronExpression(cronExpresion);
				
				LOG.debug("LOGPROGRAMAR cronExpr : " + cronExpresion);
			}
			// set the job active
			job.setActive(true);
			// create the context info
			CmsContextInfo contextInfo = new CmsContextInfo();
			contextInfo.setProjectName(projectName);

			contextInfo.setUserName(cms.getRequestContext().currentUser().getName());
			// contextInfo.setUserName(cmsAdmin.getRequestContext().currentUser().getName());

			// create the job schedule parameter
			SortedMap<String, String> params = new TreeMap<String, String>();
			// the user to send mail to
			params.put(CmsPublishScheduledJob.PARAM_USER, userName);
			// the job name
			params.put(CmsPublishScheduledJob.PARAM_JOBNAME, jobName);
			// the link check
			params.put(CmsPublishScheduledJob.PARAM_LINKCHECK, "true");
			// add the job schedule parameter
			job.setParameters(params);
			// add the context info to the scheduled job
			job.setContextInfo(contextInfo);
			// add the job to the scheduled job list
			OpenCms.getScheduleManager().scheduleJob(cmsAdmin, job);
			OpenCms.writeConfiguration(CmsSchedulerConfiguration.class);

		}
		
	}
	
	public void unscheduleJob(String jobName, CmsObject cms) throws CmsRoleViolationException {
		CmsScheduledJobInfo job = this.getJobByName(jobName, cms);
		if(job == null) return;
		OpenCms.getScheduleManager().unscheduleJob(cms, job.getId());
		OpenCms.writeConfiguration(CmsSchedulerConfiguration.class);
	}

	public CmsScheduledJobInfo getJobByName(String jobName, CmsObject cms) {
		List jobs = OpenCms.getScheduleManager().getJobs();
		for(Object job : jobs) {
			CmsScheduledJobInfo jobInfo = (CmsScheduledJobInfo)job;
			if(jobInfo.getJobName().equals(jobName)) return jobInfo;
		}
		return null;
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

	public boolean isDefaultAutoSaveEnabled() {
		return configura.getParam(siteName, publication, moduleConfigName, "autoSaveDefault", "enabled").toLowerCase()
				.equals("enabled");
	}

	public boolean isDefaultForcePublishEnabled() {
		return configura.getParam(siteName, publication, moduleConfigName, "forcePublishDefault", "enabled").toLowerCase()
				.equals("enabled");
	}

	public boolean isDefaultPublishRelatedContentEnabled() {
		return configura.getParam(siteName, publication, moduleConfigName, "publishRelatedContentDefault", "enabled")
				.toLowerCase().equals("enabled");
	}

	public boolean isDefaultPublishRelatedNewsEnabled() {
		return configura.getParam(siteName, publication, moduleConfigName, "publishRelatedNewsDefault", "enabled")
				.toLowerCase().equals("enabled");
	}

	public String getDefaultNewsOrder() {
		return configura.getParam(siteName, publication, moduleConfigName, "defaultNewsOrder",
				"user-modification-date desc");
	}

	public int getDefaultNewsResultSize() {
		return configura.getIntegerParam(siteName, publication, moduleConfigName, "defaultNewsResultCount", 100);
	}

	public List<String> getNewsResultSizeOptions() {
		return configura.getParamList(siteName, publication, moduleConfigName, "newsResultSizeOptions");
	}

	public boolean IsSiteAmp() {
		return Boolean.parseBoolean(configura.getParam(siteName, publication, "adminNewsConfiguration", "siteIsAmp", "false"));
	}
	
	
	
	public void updateUrlCdn(CmsResource resource) {
		boolean siteIsAmp = IsSiteAmp();

		try {
			
			String path = cms.getRequestContext().removeSiteRoot(resource.getRootPath());
			CmsResourceUtils.forceLockResource(cms, path);
			
			String urlFriendly = "";
			String urlFriendlyAmp = "";
			
			boolean isPublicUrlSet = (publicUrl.equals("")) ? false : true;
			
			if (isPublicUrlSet) {
				urlFriendly = publicUrl + UrlLinkHelper.getUrlFriendlyLink(resource, cms, true, true);
				if (!siteIsAmp){
					urlFriendlyAmp = publicUrl + "/amp" + UrlLinkHelper.getUrlFriendlyLink(resource, cms, true, false);
					
					TipoEdicionService tEService = new TipoEdicionService();
	   				TipoEdicion tEdicion = tEService.obtenerTipoEdicion(jsonRequest.getJSONObject("authentication").getInt("publication"));
					if (tEdicion.getTipoPublicacion().equals("2"))
						urlFriendlyAmp = urlFriendlyAmp.replaceAll("/amp"+tEdicion.getBaseURL().replaceAll(siteName,""), "/amp/");

				}
				LOG.debug("CASO 1, tiene publicURL" );
				LOG.debug("CASO 1, publicUrl: " + publicUrl );
				LOG.debug("CASO 1, siteIsAmp: " + siteIsAmp );
				LOG.debug("CASO 1, urlFriendly: " + urlFriendly );
				LOG.debug("CASO 1, urlFriendlyAmp: " + urlFriendlyAmp );
			} else {
			

				if (!UrlLinkHelper.getPublicationDomain(resource, cms).equals("")) {
					urlFriendly = UrlLinkHelper.getPublicationDomain(resource, cms) + UrlLinkHelper.getUrlFriendlyLink(resource, cms, true, true);
					if (!siteIsAmp){
						urlFriendlyAmp = UrlLinkHelper.getPublicationDomain(resource, cms) + "/amp" + UrlLinkHelper.getUrlFriendlyLink(resource, cms, true, true);
					}
					LOG.debug("CASO 2, tiene customURl" );
					LOG.debug("CASO 2, publicUrl: " + publicUrl );
					LOG.debug("CASO 2, siteIsAmp: " + siteIsAmp );
					LOG.debug("CASO 2, urlFriendly: " + urlFriendly );
					LOG.debug("CASO 2, urlFriendlyAmp: " + urlFriendlyAmp );
				} else {
					urlFriendly = UrlLinkHelper.getPublicationDomain(resource, cms) + UrlLinkHelper.getUrlFriendlyLink(resource, cms, true, false);
					if (!siteIsAmp){
						urlFriendlyAmp = UrlLinkHelper.getPublicationDomain(resource, cms) + "/amp" + UrlLinkHelper.getUrlFriendlyLink(resource, cms, true, false);
					}
					// valido si es 
					LOG.debug("CASO 2, no tiene nada." );
					LOG.debug("CASO 3, publicUrl: " + publicUrl );
					LOG.debug("CASO 3, siteIsAmp: " + siteIsAmp );
					LOG.debug("CASO 3, urlFriendly: " + urlFriendly );
					LOG.debug("CASO 3, urlFriendlyAmp: " + urlFriendlyAmp );
				}
					
			}

			
			CmsProperty property = cms.readPropertyObject(resource, "public.url.cdn1", false);
						
			if (property.getValue() == null || property.getValue().equals("")) {
				CmsProperty prop = new CmsProperty("public.url.cdn1", null,
						UrlLinkHelper.getUrlFriendlyLink(resource, cms, false, true), true);
				cms.writePropertyObject(path, prop);
				
				if (!siteIsAmp) {
					// si urlFriendlyAmp tiene doble // esta bien, porque en un servicio elimina el customdomain.
					prop = new CmsProperty("public.url.cdn2", null, urlFriendlyAmp, true);
					cms.writePropertyObject(path, prop);
				}
				
			} else {
				int index = 1;
				boolean updateProp = true;
				
				CmsProperty lastProperty = cms.readPropertyObject(resource, "public.url.cdn" + index, false);
				CmsProperty propertyAux = null;
				boolean exists = true;
				while (exists) {
					propertyAux = cms.readPropertyObject(resource, "public.url.cdn" + index, false);
					if (propertyAux.getValue() != null && !propertyAux.getValue().equals("")) {
						index++;
						lastProperty = propertyAux;
					} else {
						exists = false;
					}
				}

				if ((siteIsAmp && !lastProperty.getValue().equals(urlFriendly))
						|| (!siteIsAmp && !lastProperty.getValue().equals(urlFriendly)
								&& !lastProperty.getValue().equals(urlFriendlyAmp))) {

					CmsProperty prop = new CmsProperty("public.url.cdn" + index, null, urlFriendly, true);
					cms.writePropertyObject(path, prop);
					
					if (!siteIsAmp) {
						prop = new CmsProperty("public.url.cdn" + (index + 1), null, urlFriendlyAmp, true);
						cms.writePropertyObject(path, prop);
						updateProp =  true;
					}
					
				}
				//se agrega contingencia para saber en el republish hay que agregar la url amp, por error de rm072022 
				if (!resource.getState().equals(CmsResourceState.STATE_NEW) &&
					 !siteIsAmp && !lastProperty.getValue().equals(urlFriendlyAmp) && !updateProp) {
					 CmsProperty prop = new CmsProperty("public.url.cdn" + (index + 2), null, urlFriendlyAmp, true);
					 cms.writePropertyObject(path, prop);
				}
			}
			
			
			
			CmsResourceUtils.unlockResource(cms, path, false);
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void cancelScheduledPublication(String path) throws Exception {
		CmsObject cms = getCmsObject();

		CmsLock lockRel = cms.getLock(path);

		
		if (lockRel.isUnlocked())
			throw new Exception("The resource is not currently scheduled: " + path);
		
		if (lockRel.isExclusive() && lockRel.getProject().getName().startsWith("Publish")) {
			
			//La noticia esta lockeada para publicacion y obtengo el nombre del proyecto temporal en el que esta.
			String progProjectName = lockRel.getProject().getName();
			
			
			//obtengo los recursos de esa noticia que se encuentran en el mismo proyecto para publicar
			CmsProject currProj = cms.getRequestContext().currentProject();
			cms.getRequestContext().setCurrentProject(cms.readProject(progProjectName));
			CmsPublishList pList = OpenCms.getPublishManager().getPublishList(cms);
			List<CmsResource> resourcesInProject = pList.getAllResources();
			cms.getRequestContext().setCurrentProject(currProj);
			
			//quito los recursos relacionados de esa noticia del proyecto de publicacion
			for (CmsResource res : resourcesInProject) {
				com.tfsla.utils.CmsResourceUtils.unlockResource(cms, cms.getRequestContext().removeSiteRoot(res.getRootPath()), false);
			}
			
			//Quito la noticia del proyecto de publicacion.
			com.tfsla.utils.CmsResourceUtils.unlockResource(cms, path, false);

			cms.lockResource(path);
			//Le quito a las noticias las properties de publicacion
			CmsProperty prop = new CmsProperty("isScheduled", null, "false", true);
			cms.writePropertyObject(path, prop);	
			prop = new CmsProperty("isScheduledData", null, "", true);
			cms.writePropertyObject(path, prop);	
			cms.unlockResource(path);
			
			//Elimino el scheduledjob para que no se ejecute.
			unscheduleJob(progProjectName, cms);
		}
		else 
			throw new Exception("The resource is not currently scheduled: " + path);
	}

	public void unlockRelated(String path) {

		CmsObject cms = getCmsObject();

		try {
			CmsResource resource = cms.readResource(path);

			@SuppressWarnings("unchecked")
			List<CmsRelation> relations = cms.getRelationsForResource(cms.getSitePath(resource), CmsRelationFilter.ALL);

			for (CmsRelation relation : relations) {
				String rel1 = relation.getTargetPath();
				String rel2 = relation.getTargetPath();

				String rel = "";
				if (rel1.equals(resource.getRootPath()))
					rel = rel2;
				else
					rel = rel1;

				CmsResource relatedResource = cms.readResource(cms.getRequestContext().removeSiteRoot(rel));
				String rel_resourceName = cms.getRequestContext().removeSiteRoot(relatedResource.getRootPath());

				com.tfsla.utils.CmsResourceUtils.unlockResource(cms, rel_resourceName, false);
			}

		} catch (CmsException e) {
			e.printStackTrace();
		}

		return;
	}

	public Locale getLocale() {

		return m_settings.getUserSettings().getLocale();
	}

	protected int getElementCountWithValue(String key, String controlKey, I_CmsXmlDocument content) {
		Locale locale = getCmsObject().getRequestContext().getLocale();
		int total = content.getIndexCount(key, locale);

		int blank = 0;
		for (int j = 1; j <= total; j++) {
			String controlValue;
			try {
				controlValue = content.getStringValue(getCmsObject(), key + "[" + j + "]/" + controlKey, locale);

				if (controlValue == null || controlValue.trim().equals(""))
					blank++;
			} catch (CmsXmlException e) {
				// LOG.debug("Error reading content value " + key + "[" + j + "]/" + controlKey + " on content "
					//	+ content.getFile().getRootPath(), e);

			}
		}

		return total - blank;
	}

	protected int getSimpleElementCountWithValue(String key, I_CmsXmlDocument content) {
		Locale locale = getCmsObject().getRequestContext().getLocale();
		int total = content.getIndexCount(key, locale);

		int blank = 0;
		for (int j = 1; j <= total; j++) {
			String controlValue;
			try {
				controlValue = content.getStringValue(getCmsObject(), key + "[" + j + "]", locale);

				if (controlValue == null || controlValue.trim().equals(""))
					blank++;
			} catch (CmsXmlException e) {
				// LOG.debug("Error reading content value " + key + "[" + j + "]/" + " on content "
				//		+ content.getFile().getRootPath(), e);

			}
		}

		return total - blank;
	}

	protected String getElementValue(String elementName, I_CmsXmlDocument content, Locale locale) {
		try {
			String value = content.getStringValue(getCmsObject(), elementName, locale);
			if (value == null) {
				value = "";
				// LOG.debug("Content value " + elementName + "not found on content" + content.getFile().getRootPath());
			}
			return value;
		} catch (CmsXmlException e) {
			LOG.error("Error reading content value " + elementName + " on content " + content.getFile().getRootPath(),
					e);
		}

		return "";
	}

	protected String getFileEncoding(CmsObject cms, String filename) {

		try {
			return cms.readPropertyObject(filename, CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, true)
					.getValue(OpenCms.getSystemInfo().getDefaultEncoding());
		} catch (CmsException e) {
			return OpenCms.getSystemInfo().getDefaultEncoding();
		}
	}

	protected String getDescripcionFecha(String cronDate) {
		String[] cronDateSplt = cronDate.split(cronDate);
		String seconds = cronDateSplt[0];
		String minutes = cronDateSplt[1];
		String hours = cronDateSplt[2];
		String dayMonth = cronDateSplt[3];
		String month = cronDateSplt[4];
		String dayWeek = cronDateSplt[5];
		String years = cronDateSplt[6];

		String FechaDescripcion = "";

		String descrip1 = "Repetir mensual los días X del mes";

		String descrip2 = "Repetir anualmente en la fecha seleccionada";
		String Custom1 = "Repetir cada X dias";
		String Custom2 = "Repetir los dias XXXX cada X semanas";
		String Custom3 = "Repetir cada X meses";
		String Custom4 = "Repetir los dias X todos los meses";
		String Custom5 = "Repetir x año";

		String Repite1 = "nunca";
		String Repite2 = "hasta el dia xx";
		String Repite3 = "hasta xx ocurrencias";

		try {

			if (seconds.equals("*"))
				FechaDescripcion = "cada segundo";
			else if (seconds.indexOf("-") > -1) {
				String[] aux = seconds.split("-");
				FechaDescripcion = "de " + aux[0] + " a " + aux[1];
			}

			if (minutes.equals("*"))
				FechaDescripcion = "cada minuto";
			else if (minutes.indexOf("-") > -1) {
				String[] aux = minutes.split("-");
				FechaDescripcion = "de " + aux[0] + " a " + aux[1];
			} else {
				FechaDescripcion = " : " + minutes + " : ";
			}

			if (hours.equals("*"))
				FechaDescripcion = "cada hora";
			else if (hours.indexOf("-") > -1) {
				String[] aux = hours.split("-");
				FechaDescripcion = "de " + aux[0] + " a " + aux[1];
			} else {
				FechaDescripcion = " : " + hours + " : ";
			}

			if (dayMonth.equals("*"))
				FechaDescripcion = "todos los dia";
			else if (dayMonth.indexOf("-") > -1) {
				String[] aux = dayMonth.split("-");
				FechaDescripcion = "de " + aux[0] + " a " + aux[1];
			}
			if (dayMonth.indexOf(",") > -1) {
				String[] aux = dayMonth.split(",");
				for (int i = 0; i < aux.length; i++) {
					FechaDescripcion += "los " + aux[i] + ", ";
				}
			} else {
				FechaDescripcion = " el " + dayMonth;
			}

		} catch (Exception e) {
			System.out.println("error crone expresion.");
		}

		return FechaDescripcion;
	}
	

	public String getUnsafeLabelsType(String UnsafeLabels, String categoryCritical, String[] categoryCriticalSpl, String[] categoryWarningSpl ){
		
		String unsafeLabelTypeFinal = "";
		
		if (UnsafeLabels.indexOf(",") > 0){
			String[] UnsafeLabelsSpl = UnsafeLabels.split(", ");
		
			for (String UnsafeLabelString : UnsafeLabelsSpl) {
			
				String unsafeLabelType = validarCripticidad(UnsafeLabelString, categoryCritical, categoryCriticalSpl, categoryWarningSpl );

				if (unsafeLabelType.equals("CRITICAL")) 
					return unsafeLabelType;
		
				else if (unsafeLabelType.equals("MODERATE")) 
						unsafeLabelTypeFinal = unsafeLabelType;
				
			}
			
			
		} else {
			
			String unsafeLabelType = validarCripticidad(UnsafeLabels, categoryCritical, categoryCriticalSpl, categoryWarningSpl );
			
			if (unsafeLabelType.equals("CRITICAL")) 
				return unsafeLabelType;
	
			else if (unsafeLabelType.equals("MODERATE")) 
					unsafeLabelTypeFinal = unsafeLabelType;
			
		}
		
		return unsafeLabelTypeFinal;
	};
	
	private String validarCripticidad(String unsafeLabel, String categoryCritical, String[] categoryCriticalSpl, String[] categoryWarningSpl ){
		
		String type = "";
		String unsafeLabelAux = unsafeLabel.toLowerCase();
		unsafeLabelAux = unsafeLabelAux.replaceAll("á","a");
		unsafeLabelAux = unsafeLabelAux.replaceAll("é","e");
		unsafeLabelAux = unsafeLabelAux.replaceAll("í","i");
		unsafeLabelAux = unsafeLabelAux.replaceAll("ó","o");
		unsafeLabelAux = unsafeLabelAux.replaceAll("ú","u");
			
		//nivel rojo CRITICO
		if (categoryCritical.contains(unsafeLabelAux)){		
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
	
//**************************************************************************************************************************
// AGREGO LOS METODOS CON LA APERTURA Y GRABACIÓN DEL ARCHIVO SOLO UNA VEZ PARA EVITAR EL TIEMPO DE GRABAR VARIAS VECES ****
//**************************************************************************************************************************

	public CmsXmlContent getNewsContent(String path, boolean lockFile) throws CmsException {
		
		CmsObject cms = getCmsObject();
		if (lockFile) {
			CmsResourceUtils.forceLockResource(cms, path);
		}

		CmsFile file = cms.readFile(path, CmsResourceFilter.ALL);

		CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
		content.setAutoCorrectionEnabled(true);
		content.correctXmlStructure(cms);
		
		return content;
	}
	
	public void writeNewContent(String path, CmsXmlContent newContent, boolean unlockFile) throws UnsupportedEncodingException, CmsException {
		String fileEncoding = getFileEncoding(cms, path);

		String decodedContent = newContent.toString();
		decodedContent = decodedContent.replaceAll("(?!\\n)[\\p{C}]", "");
		
		// file.setContents(content.marshal());
		newContent.getFile().setContents(decodedContent.getBytes(fileEncoding));
		cms.writeFile(newContent.getFile());

		if (unlockFile)
			cms.unlockResource(path);

	} 
	
	public void setLastModified(CmsXmlContent content, String lastModification) {
		
		CmsObject cms = getCmsObject();
		content.getValue("ultimaModificacion", Locale.ENGLISH).setStringValue(cms, lastModification);
	}
	
	public JSONObject updateEntities(CmsXmlContent content, JSONArray tagJson, JSONArray tagsHiddenJson) {
		
		JSONObject result = new JSONObject();
		JSONArray errorsJS = new JSONArray();
		CmsObject cms = getCmsObject();

		String enableEntitiesDetection = configura.getParam(siteName, jsonRequest.getJSONObject("authentication").getString("publication"), "adminNewsConfiguration", "enableEntitiesDetection", "true");
		
		if (enableEntitiesDetection.equals("true")) {

			try {
					
				String tags = "";
				String COMMA = ""; 
				for (int i=0; i < tagJson.size(); i++) {
					tags = tags + COMMA + tagJson.getString(i);
					COMMA = ", "; 
				}

				if (!tags.equals("")) {
					String tagsExist = content.getValue("claves", Locale.ENGLISH).getStringValue(cms);
					if (!tagsExist.equals(""))
						tags = tagsExist+", "+tags;
					
					content.getValue("claves", Locale.ENGLISH).setStringValue(cms, tags);
				}
				
				String tagsHidden = "";
				COMMA = ""; 
				for (int j=0; j < tagsHiddenJson.size(); j++) {
					tagsHidden = tagsHidden + COMMA + tagsHiddenJson.getString(j);
					COMMA = ", "; 
				}
				if (!tagsHidden.equals("")) {
					String tagsHiddenExist = content.getValue("clavesOcultas", Locale.ENGLISH).getStringValue(cms);
					if (!tagsHiddenExist.equals(""))
						tagsHidden = tagsHiddenExist+", "+tagsHidden;
					
					content.getValue("clavesOcultas", Locale.ENGLISH).setStringValue(cms, tagsHidden);
				}

//				String fileEncoding = getFileEncoding(cms, cms.getSitePath(content.getFile()));

//				String decodedContent = content.toString();
//				decodedContent = decodedContent.replaceAll("(?!\\n)[\\p{C}]", "");

				result.put("status", "ok");

			} catch (Exception e) {

				LOG.error("Error trying to save resource " + cms.getSitePath(content.getFile()) + " in site " + cms.getRequestContext().getSiteRoot()
						+ " (" + cms.getRequestContext().currentProject().getName() + ")", e);

				result.put("status", "error");

				JSONObject error = new JSONObject();
				error.put("path", cms.getSitePath(content.getFile()));
				error.put("message", e.getMessage());

				try {
					CmsLock lockE = cms.getLock(cms.getSitePath(content.getFile()));

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

}