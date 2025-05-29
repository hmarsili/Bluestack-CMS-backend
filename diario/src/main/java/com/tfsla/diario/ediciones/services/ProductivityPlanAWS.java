package com.tfsla.diario.ediciones.services;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.http.HttpStatus;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;

import com.tfsla.diario.productivityPlans.model.PlansUsers;
import com.tfsla.diario.productivityPlans.model.ProductivitiyPlans;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Clase que realiza la administracion de los planes de productividad.
 * @author Veronica Tarletta.
 *
 */
public class ProductivityPlanAWS extends baseService {

	private static final Log LOG = CmsLog.getLog(ProductivityPlanAWS.class);

	private String publication;
	private String siteName;
	private JSONObject jsonRequest;
	private String serverId;
	private String prodId;
	private String endpointInsertPlan;
	private String endpointInsertEvent;
	private String endpointReportUserPerDay;
	private String endpointReportUserBetweenDay;
	private String endpointReportPeriod;
	private String endpointDeletePlan;
	private CPMConfig config;
	private PlansUsersServices puService;
	private ProductivityPlanServices ppService;	
	private CmsObject cmsObj;
	
	public boolean isMark = false;

	/** 
	 * NO SIRVE PARA PUBLICACIONES SOLO PARA SITIOS
	 * 
	 * public ProductivityPlanAWS(CmsObject cmsObj) throws Exception {
	 *

		siteName = OpenCms.getSiteManager().getCurrentSite(cmsObj).getSiteRoot();
		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion currentPublication = tService.obtenerEdicionOnlineRoot(siteName.replaceAll("/sites/", ""));
		publication = ""+currentPublication.getId();

		config = CmsMedios.getInstance().getCmsParaMediosConfiguration();

		prodId = config.getParam(siteName, publication, "productivityPlans", "prodId", "");
		serverId = config.getParam(siteName, publication, "productivityPlans", "serverId", "");
		endpointInsertEvent = config.getParam(siteName, publication, "productivityPlans", "endpointInsertEvent", "");
		endpointInsertPlan = config.getParam(siteName, publication, "productivityPlans", "endpointInsertPlan", "");
		endpointReportUserBetweenDay = config.getParam(siteName, publication, "productivityPlans", "endpointReportUserBetweenDay", "");
		endpointReportUserPerDay = config.getParam(siteName, publication, "productivityPlans", "endpointReportUserPerDay", "");
		endpointReportPeriod = config.getParam(siteName, publication, "productivityPlans", "endpointReportPeriod", "");
		endpointDeletePlan = config.getParam(siteName, publication, "productivityPlans", "endpointDeletePlan", "");

	}*/

	public ProductivityPlanAWS(JSONObject jsonreq) throws Exception {

		jsonRequest = jsonreq;

		siteName = jsonRequest.getJSONObject("authentication").getString("siteName");
		publication = jsonRequest.getJSONObject("authentication").getString("publication");

		config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		prodId = config.getParam(siteName, publication, "productivityPlans", "prodId");
		serverId = config.getParam(siteName, publication, "productivityPlans", "serverId");
		endpointInsertEvent = config.getParam(siteName, publication, "productivityPlans", "endpointInsertEvent");
		endpointInsertPlan = config.getParam(siteName, publication, "productivityPlans", "endpointInsertPlan");
		endpointReportUserBetweenDay = config.getParam(siteName, publication, "productivityPlans", "endpointReportUserBetweenDay");
		endpointReportUserPerDay = config.getParam(siteName, publication, "productivityPlans", "endpointReportUserPerDay", "");
		endpointReportPeriod = config.getParam(siteName, publication, "productivityPlans", "endpointReportPeriod", "");
		endpointDeletePlan = config.getParam(siteName, publication, "productivityPlans", "endpointDeletePlan", "");

	}

	public Long getIniDayGMT() {
		String gmtRedaction = config.getParam(siteName, publication, "admin-settings", "gmtRedaction", "");
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.setTimeZone(TimeZone.getTimeZone("GMT"+gmtRedaction+":00"));	    
	    return c.getTimeInMillis() /1000;
		
	}
	
	public Long getIniNexDayGMT() {
		String gmtRedaction = config.getParam(siteName, publication, "admin-settings", "gmtRedaction", "");
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);		
	    c.setTimeZone(TimeZone.getTimeZone("GMT"+gmtRedaction+":00"));
	    c.add(Calendar.HOUR_OF_DAY, 24);
	    return c.getTimeInMillis() /1000;
		
	}
	public boolean isActiveModule() {
		return Boolean.parseBoolean(config.getParam(siteName, publication, "productivityPlans", "active","false"));

	}

	public String informarPlan(String path, JSONObject plan) throws Exception {

		JSONObject jsonbody = new JSONObject();
		jsonbody.put("serverId", serverId);
		jsonbody.put("pubId", publication);
		jsonbody.put("prodId", path);
		jsonbody.put("enabled", plan.getString("enabled"));
		jsonbody.put("type", plan.getString("type"));
		jsonbody.put("title", plan.getString("title"));
		jsonbody.put("description", plan.getString("description"));
		jsonbody.put("format", plan.getString("format"));
		jsonbody.put("newsCount", plan.getString("newsCount"));
		jsonbody.put("method", plan.getString("method"));
		jsonbody.put("minimum", plan.getString("minNum"));
		jsonbody.put("FrecMonday", plan.getString("frecMonday"));
		jsonbody.put("FrecTuesday", plan.getString("frecTuesday"));
		jsonbody.put("FrecWednesday", plan.getString("frecWednesday"));
		jsonbody.put("FrecThursday", plan.getString("frecThursday"));
		jsonbody.put("FrecFriday", plan.getString("frecFriday"));
		jsonbody.put("FrecSaturday", plan.getString("frecSaturday"));
		jsonbody.put("FrecSunday", plan.getString("frecSunday"));
		jsonbody.put("FrecFrom", plan.getString("frecFrom"));
		jsonbody.put("FrecTo", plan.getString("frecTo"));
		jsonbody.put("timeZone","GMT"+config.getParam(siteName, publication, "admin-settings", "gmtRedaction", ""));
		jsonbody.put("segmentSize",config.getParam(siteName, publication, "productivityPlans", "hoursRepublication", ""));
		
		return callAws(endpointInsertPlan, jsonbody);

	}
	
	// Reporte estadisticas entre dias devolviendo el detalle usuario
	public String getStaticsPeriod(String planId,String period) throws Exception
	{
		JSONObject jsonbody = new JSONObject();

		jsonbody.put("planId",planId); 
		jsonbody.put("serverId",serverId);
		jsonbody.put("publication",publication);
		jsonbody.put("period", period);
		
		return callAws(endpointReportPeriod, jsonbody);
	}
	
	// Reporte estadisticas entre dias por usuario
		public String deletePlan(String planId) throws Exception
		{
			JSONObject jsonbody = new JSONObject();

			jsonbody.put("prodId",planId);
			jsonbody.put("serverId",serverId);
			jsonbody.put("pubId",publication);
			
			return callAws(endpointDeletePlan, jsonbody);
		}

	// Reporte estadisticas entre dias por usuario
	public String getStaticsUser(JSONObject parameters) throws Exception
	{
		JSONObject jsonbody = new JSONObject();

		jsonbody.put("planId",parameters.getJSONObject("filters").getString("planId")); // primera vez que se publica la nota.
		jsonbody.put("serverId",serverId);
		jsonbody.put("publication",publication);
		jsonbody.put("user", parameters.getJSONObject("filters").getString("user"));
		jsonbody.put("from", parameters.getJSONObject("filters").getString("from"));
		jsonbody.put("to", parameters.getJSONObject("filters").getString("to"));

		return callAws(endpointReportUserBetweenDay, jsonbody);

	}
	
	// Reporte estadisticas de un dia por usuario
	public String getStaticsUserDay(JSONObject parameters) throws Exception
	{
		JSONObject jsonbody = new JSONObject();

		jsonbody.put("planId",parameters.getJSONObject("filters").getString("path")); // primera vez que se publica la nota.
		jsonbody.put("serverId",serverId);
		jsonbody.put("publication",publication);
		jsonbody.put("user", parameters.getJSONObject("filters").getString("userName"));
		jsonbody.put("pubDay", parameters.getJSONObject("filters").getString("pubDay"));

		return callAws(endpointReportUserPerDay, jsonbody);

	}

	/**Metodo que informa a AWS las publicaciones para un usuario.
	 * Obtiene los autores y/o creadores de la noticia
	 * Por cada uno de ellos en los que exista un plan de productividad
	 * Generar un json
	 * Enviar la informacion a un endpoint definido en la configuracion.
	 *
	 * @param resource CmsResource de la noticia
	 * @param isAutomaticPublish : indica si es automatica o no la publicacion(automatica = programada = true
	 * @param isFirstPublish: json con los datos de la primera publicacion.;
	 * @throws Exception
	 */
	public void processUsersPlans(String newsPath, JSONArray usersToNew, JSONObject jsonFristPub, boolean isAutomaticPublish, String complianceData, CmsObject cObject) throws Exception {

		LOG.debug("["+siteName + "-" + publication + "] procesando los usuarios de la nota " + newsPath + " " );
		
		cmsObj = cObject;
				
		JSONObject jsonAuth =  new JSONObject();
		jsonAuth.put("siteName",siteName);
		jsonAuth.put("publication",publication);
		
		JSONObject jsonreq =  new JSONObject();
		jsonreq.put("authentication",jsonAuth);
		
		puService = new PlansUsersServices(cObject,jsonreq);
		
		for (int i=0; i < usersToNew.size(); i ++) {
					
			String userName = (String) usersToNew.get(i);
			boolean isSigning =  (userName.indexOf("signing_") >-1) ? true : false;
			boolean isCreation =  !isSigning;
			userName = userName.replaceAll("signing_", "");

			LOG.debug("["+siteName + "-" + publication + "]  isSigning ? " +  isSigning + " -  isCreation? " + isCreation);

			
			boolean isSameUser =  (userName.indexOf("same_") >-1) ? true : false;
			userName = userName.replaceAll("same_", "");
			
			LOG.debug("["+siteName + "-" + publication + "]  isSameUser ? " +  isSameUser ) ;

			PlansUsers planForUser = puService.existPlanForUser(userName);
			ppService = new ProductivityPlanServices(jsonRequest);
			
			// 1- El usuario pertenece a un plan y de la forma correcta: contabilizo en ese plan 
			// 2- El usuario pertenece a un plan y es el mismo que firma y crea: contabilizo a ese plan sin validar el tipo del mismo porque esta de ambas maneras
			// 3- El usuario pertenece a un plan pero de forma equivocada: va al general validando que sea del mismo tipo. 
			// 4- El usuario no pertenece a un plan y es el mismo: contabilizo en el general validando el tipo del plan
			// 5- El usuario no pertenece a un plan y no es el mismo que firma y crea: contabilizo en el general valindando el tipo de plan.
			// 6- El usuario no pertenece a un plan y el tipo plan del plan general es distinto a como esta en la noticia: SE PIERDE 
			// 7- El usuario pertenece a un plan, pero de forma equivocada y el tipo de plan general es distinto a como esta en la noticia: SE PIERDE
			
			if (planForUser.getId() != null  && !planForUser.getId().equals("")){

				ProductivitiyPlans planDetail = ppService.getProductivitiyPlans(planForUser.getId());
				
				// 1- El usuario pertenece a un plan y de la forma correcta: contabilizo en ese plan 
				// 2- El usuario pertenece a un plan y es el mismo que firma y crea: contabilizo a ese plan sin validar el tipo del mismo porque esta de ambas maneras
				if ( (planDetail.getUsersType().equals("signing") && isSigning) || 
					 (planDetail.getUsersType().equals("creation") && isCreation) ||
						 isSameUser){ 
				
					if (isSameUser) 
						LOG.debug("["+siteName + "-" + publication + "] 2- El usuario "+userName+" pertenece a un plan y es el mismo que firma y crea: contabilizo a ese plan sin validar el tipo del mismo porque esta de ambas maneras");
					else
						LOG.debug("["+siteName + "-" + publication + "] 1- El usuario "+userName+" pertenece a un plan y de la forma correcta: contabilizo en ese plan");
					
					JSONObject jsonbody = new JSONObject();
				
					SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
					dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));

					String gmtRedaction = config.getParam(siteName, publication, "admin-settings", "gmtRedaction", "");
					SimpleDateFormat dateFormatRedactionHours = new SimpleDateFormat("HH");
					dateFormatRedactionHours.setTimeZone(TimeZone.getTimeZone("GMT"+gmtRedaction+":00"));

					SimpleDateFormat dateFormatRedaction = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
					dateFormatRedaction.setTimeZone(TimeZone.getTimeZone("GMT"+gmtRedaction+":00"));

					
				    Calendar c = Calendar.getInstance();
				    Date today = c.getTime();
				    int dayPub = c.get(Calendar.DAY_OF_WEEK);
				    
				    long pubDate_gmt =  dateFormatGmt.parse( dateFormatGmt.format(today)).getTime();
				    
					jsonbody.put("planId", planForUser.getId());
					jsonbody.put("serverId",serverId);
					jsonbody.put("user",userName);
					jsonbody.put("pubDate", "" + pubDate_gmt/1000); 
					jsonbody.put("url",newsPath);
					jsonbody.put("publication",publication);
					jsonbody.put("site",siteName);
					String[] complianceDataSpl = complianceData.split(";");  //C:917;W:150
					
					if (planDetail.getMethod().equals("words")) {
						String number = complianceDataSpl[1].replaceAll("W:","") ;
						if (number.length()==0)
							number = "0";
						jsonbody.put("wordsCount",number); // cantidad de palabras
						jsonbody.put("lettersCount","0"); 
						
					}else{
						String number = complianceDataSpl[0].replaceAll("C:","") ;
						if (number.length()==0)
							number = "0";
						jsonbody.put("lettersCount",number ); // cantidad de letras
						jsonbody.put("wordsCount","0"); 
						
					}

					String isAcopy = cObject.readPropertyObject(newsPath, "isACopy", false).getValue("");
					if (isAcopy == null || isAcopy.equals("")) isAcopy = "false";


					jsonbody.put("firstPudbDate","" + jsonFristPub.getString("firstPublishDate")); // primera vez que se publica la nota.
					// Nuevas: Son las que no fueron copiadas y se publican la primera vez o se republico en el rango configurado.
					// Copiadas: Son las copiadas y se publicaron por primera vez  o se republico en el rango configurado..
					// Republicadas: Son las noticias que no importa su procedencia y se volvieron a publicar fuera del rango establecido como primera publicacion..

					if (!Boolean.parseBoolean(isAcopy) && jsonFristPub.getBoolean("isFirstPublish")) {
						jsonbody.put("isFirstPublish","true");
						jsonbody.put("isACopy","false");
					}else if (Boolean.parseBoolean(isAcopy) && jsonFristPub.getBoolean("isFirstPublish")) {
						jsonbody.put("isFirstPublish","false");
						jsonbody.put("isACopy","true");
					}else{
						jsonbody.put("isFirstPublish","false");
						jsonbody.put("isACopy","false");
					}
					
					jsonbody.put("isAutomaticPublish",""+isAutomaticPublish);
					
					int hourPub = Integer.parseInt(dateFormatRedactionHours.format(new Date())); // GMT REDACCION
					boolean inTime = (planDetail.getFrecFrom() <= hourPub && planDetail.getFrecTo() >= hourPub ) ? true : false;  

					boolean inDay = true;
					switch (dayPub){
			            case 1: if (planDetail.isFrecSunday()) 
			            	inDay = true;
			                break;
			            case 2: if (planDetail.isFrecMonday()) 
			        		inDay = true;
			                break;
			            case 3: if (planDetail.isFrecThuesday()) 
			        		inDay = true;
			                break;
			            case 4: if (planDetail.isFrecWednesday())
			        		inDay = true;
			            	break;
			            case 5: if (planDetail.isFrecThursday())
			        		inDay = true;
			            	break;
			            case 6: if (planDetail.isFrecFriday())
			        		inDay = true;
			            	break;
			            case 7: if (planDetail.isFrecSaturday())
			        		inDay = true;
			            	break;
			        }
				    
					jsonbody.put("inDay",""+inDay);
					jsonbody.put("inTime",""+inTime);

					callAws(endpointInsertEvent, jsonbody);
										
				} else {
					// 3- El usuario pertenece a un plan pero de forma equivocada: va al general validando que sea del mismo tipo. 	
					// 7- El usuario pertenece a un plan, pero de forma equivocada y el tipo de plan general es distinto a como esta en la noticia: SE PIERDE

					ProductivitiyPlans planGeneral = ppService.getGeneralProductivitiyPlans();
					if (planGeneral == null) {
						LOG.error("Error procesando productividad para el usuario " + userName + " en noticia " + newsPath + ": No se encuentra el plan general para el sitio " + siteName + " y publicacion " + publication);
					}
					else {
						String generalTypePlan  = planGeneral.getType();
						
						if (isSigning && generalTypePlan.equals("signing") || isCreation && generalTypePlan.equals("creation")) {
							addToGeneralPlan(newsPath, userName, jsonFristPub, isAutomaticPublish, complianceData);
							LOG.debug("["+siteName + "-" + publication + "] 3- El usuario "+userName+" pertenece a un plan pero de forma equivocada: va al general validando que sea del mismo tipo.");
						}else {
							LOG.debug("["+siteName + "-" + publication + "] 7- El usuario "+userName+" pertenece a un plan, pero de forma equivocada y el tipo de plan general es distinto a como esta en la noticia: SE PIERDE");
						}
					}
					
				}

			} else {
				// 4- El usuario no pertenece a un plan y es el mismo: contabilizo en el general validando el tipo del plan
				// 5- El usuario no pertenece a un plan y no es el mismo que firma y crea: contabilizo en el general valindando el tipo de plan.
				// 6- El usuario no pertenece a un plan y el tipo plan del plan general es distinto a como esta en la noticia: SE PIERDE 
				
				ProductivitiyPlans planGeneral = ppService.getGeneralProductivitiyPlans();
				LOG.debug("["+siteName + "-" + publication + "] "+ planGeneral.getId()  );	

				if (planGeneral == null) {
					LOG.error("Error procesando productividad para el usuario " + userName + " en noticia " + newsPath + ": No se encuentra el plan general para el sitio " + siteName + " y publicacion " + publication);
				}
				else {
					String generalTypePlan  = planGeneral.getUsersType();
					LOG.debug("["+siteName + "-" + publication + "] planGeneral.getUsersType" + planGeneral.getUsersType() );	
					LOG.debug("["+siteName + "-" + publication + "] planGeneral.getType" + planGeneral.getType() );	

					
					if ( (generalTypePlan.equals("signing") && isSigning) || 
					     (generalTypePlan.equals("creation") && isCreation) ||
					     isSameUser) {
						
							addToGeneralPlan(newsPath, userName, jsonFristPub, isAutomaticPublish, complianceData);
							if (isSameUser)
								LOG.debug("["+siteName + "-" + publication + "] 4- El usuario "+userName+" no pertenece a un plan y es el mismo: contabilizo en el general validando el tipo del plan" );	
							else
								LOG.debug("["+siteName + "-" + publication + "] 5- El usuario "+userName+" no pertenece a un plan y no es el mismo que firma y crea: contabilizo en el general valindando el tipo de plan.");
				
					} else {
						LOG.debug("["+siteName + "-" + publication + "] 6- El usuario "+userName+" no pertenece a un plan y el tipo plan del plan general es distinto a como esta en la noticia: SE PIERDE");
	
					}
				}				
			}

		}
	}

	/**
	 * Conexion con AWS
	 * @param endpoint: al que vamos a llamar
	 * @param jsonbody: parametros.
	 * @return
	 * @throws Exception
	 */
	public String callAws(String endpoint, JSONObject jsonbody) throws Exception
	{

		String sBody = jsonbody.toString(2);
		// LOG.debug("["+siteName + "-" + publication + "] informamos a AWS " + sBody + " endpoint "+ endpoint);
		StringRequestEntity requestEntity = new StringRequestEntity(
				sBody,
				"application/json",
				"UTF-8");

		PostMethod postMethod = new PostMethod(endpoint);
		postMethod.setRequestEntity(requestEntity);

		HttpClient httpClient = new HttpClient();
		int statusCode = httpClient.executeMethod(postMethod);
		LOG.debug("["+siteName + "-" + publication + "] volvemos da AWS " + statusCode);
		if (statusCode == HttpStatus.SC_OK) {
			LOG.debug(postMethod.getResponseBodyAsString());
			return postMethod.getResponseBodyAsString();
		}
		else {
			LOG.debug(postMethod.getResponseBodyAsString());
		}
		return "";
	}
	
	public void addToGeneralPlan(String newsPath, String userName, JSONObject jsonFristPub, boolean isAutomaticPublish, String complianceData) throws Exception {

		ProductivitiyPlans generalPlan = ppService.getGeneralProductivitiyPlans();

		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));

		String gmtRedaction = config.getParam(siteName, publication, "admin-settings", "gmtRedaction", "");
		SimpleDateFormat dateFormatRedactionHours = new SimpleDateFormat("HH");
		dateFormatRedactionHours.setTimeZone(TimeZone.getTimeZone("GMT"+gmtRedaction+":00"));

		SimpleDateFormat dateFormatRedaction = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
		dateFormatRedaction.setTimeZone(TimeZone.getTimeZone("GMT"+gmtRedaction+":00"));

		
	    Calendar c = Calendar.getInstance();
	    Date today = c.getTime();
	    int dayPub = c.get(Calendar.DAY_OF_WEEK);
	    
	    long pubDate_gmt =  dateFormatGmt.parse( dateFormatGmt.format(today)).getTime();
	    
		JSONObject jsonbody = new JSONObject();
		jsonbody.put("planId", generalPlan.getId());
		jsonbody.put("serverId",serverId);
		jsonbody.put("user",userName);
		jsonbody.put("pubDate", "" + pubDate_gmt/1000); 
		jsonbody.put("url",newsPath);
		jsonbody.put("publication",publication);
		jsonbody.put("site",siteName);
		String[] complianceDataSpl = complianceData.split(";");  //C:917;W:150
		
		if (generalPlan.getMethod().equals("words")) {
			String number = complianceDataSpl[1].replaceAll("W:","") ;
			if (number.length()==0)
				number = "0";
			jsonbody.put("wordsCount",number); // cantidad de palabras
			jsonbody.put("lettersCount","0"); 
			
		}else{
			String number = complianceDataSpl[0].replaceAll("C:","") ;
			if (number.length()==0)
				number = "0";
			jsonbody.put("lettersCount",number ); // cantidad de letras
			jsonbody.put("wordsCount","0"); 
			
		}

		String isAcopy = cmsObj.readPropertyObject(newsPath, "isACopy", false).getValue("");
		if (isAcopy == null || isAcopy.equals("")) isAcopy = "false";


		jsonbody.put("firstPudbDate","" + jsonFristPub.getString("firstPublishDate")); // primera vez que se publica la nota.
		// Nuevas: Son las que no fueron copiadas y se publican la primera vez o se republico en el rango configurado.
		// Copiadas: Son las copiadas y se publicaron por primera vez  o se republico en el rango configurado..
		// Republicadas: Son las noticias que no importa su procedencia y se volvieron a publicar fuera del rango establecido como primera publicacion..

		if (!Boolean.parseBoolean(isAcopy) && jsonFristPub.getBoolean("isFirstPublish")) {
			jsonbody.put("isFirstPublish","true");
			jsonbody.put("isACopy","false");
		}else if (Boolean.parseBoolean(isAcopy) && jsonFristPub.getBoolean("isFirstPublish")) {
			jsonbody.put("isFirstPublish","false");
			jsonbody.put("isACopy","true");
		}else{
			jsonbody.put("isFirstPublish","false");
			jsonbody.put("isACopy","false");
		}
		
		jsonbody.put("isAutomaticPublish",""+isAutomaticPublish);
		
		int hourPub = Integer.parseInt(dateFormatRedactionHours.format(new Date())); // GMT REDACCION
		boolean inTime = (generalPlan.getFrecFrom() <= hourPub && generalPlan.getFrecTo() >= hourPub ) ? true : false;  

		boolean inDay = true;
		switch (dayPub){
            case 1: if (generalPlan.isFrecSunday()) 
            	inDay = true;
                break;
            case 2: if (generalPlan.isFrecMonday()) 
        		inDay = true;
                break;
            case 3: if (generalPlan.isFrecThuesday()) 
        		inDay = true;
                break;
            case 4: if (generalPlan.isFrecWednesday())
        		inDay = true;
            	break;
            case 5: if (generalPlan.isFrecThursday())
        		inDay = true;
            	break;
            case 6: if (generalPlan.isFrecFriday())
        		inDay = true;
            	break;
            case 7: if (generalPlan.isFrecSaturday())
        		inDay = true;
            	break;
        }
	    
		jsonbody.put("inDay",""+inDay);
		jsonbody.put("inTime",""+inTime);

		callAws(endpointInsertEvent, jsonbody);
	}

}