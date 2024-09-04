package com.tfsla.diario.admin.jobs;

import java.util.Date;
import java.util.Map;

import com.tfsla.diario.analytics.data.NewsAnalyticsDataDAO;
import com.tfsla.diario.analytics.model.AnalyticsDateAutomatic;
import com.tfsla.diario.analytics.model.NewsAnalyticsData;
import com.tfsla.diario.analytics.services.AnalyticsDateAutomaticServices;
import com.tfsla.diario.analytics.services.AnalyticsServices;
import com.tfsla.diario.ediciones.services.SearchConsoleService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.scheduler.I_CmsScheduledJob;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import  java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.io.IOUtils; 
import com.tfsla.diario.analytics.data.*;

/**
 * Clase que se usa para obtene las noticias de la consola de Analytics y guardarlas en la base de datos. 
 * 
 * recibe como parámetro
 * - site : sitio de las noticias a limpiar
 * - publication: publicacion de las noticias a limpiar
 * 
 * Desde analytics siempre recibimos el siguiente formato:
 * {
  "rows": [
    {
      "keys": [
        "https://tork.news/autos/La-lista-negra-de-Ferrari-Karol-G-figuraria-en-ella-por-esta-razon-20230710-0032.html",
      ],
      "clicks": 19,
      "impressions": 286,
      "ctr": 0.066433566433566432,
      "position": 2.2727272727272725
    },
	.....
	....
	 ],
	  "responseAggregationType": "byPage"
	}

 */

public class MigrarDatosDeAnalytics implements I_CmsScheduledJob {

	private String PUBLICATION = "";
	private String RESULTLOG = "";
	private String SITE = "";
	
	private int countNewsNew = 0;
	private int countNewsUpdated = 0;
	protected static final Log LOG = CmsLog.getLog(MigrarDatosDeAnalytics.class);

	CPMConfig configxsml = CmsMedios.getInstance().getCmsParaMediosConfiguration();

	public String launch(CmsObject cms, Map parameters) throws Exception {

		PUBLICATION = (String) parameters.get("publication");
		SITE = (String) parameters.get("siteName");
		String CAllAPI  = (String) parameters.get("CAllAPI");
		LOG.debug("entra al job con los datos SITE: " + SITE + " y PUB: " + PUBLICATION);
		
		AnalyticsServices analyticsServices = new AnalyticsServices(SITE, PUBLICATION);
		NewsAnalyticsDataDAO analyticsDAO = new NewsAnalyticsDataDAO();
		
		SearchConsoleService gservice = new SearchConsoleService();
		gservice.initializeContext(SITE, PUBLICATION);

		if (gservice.isAuthorized()){
		
			if (analyticsServices.checkDataAnalytics()) { //se agrega esta validacion por recomendación de la documentación
	
				Date nowDate = new Date();
				
				String dataJson = "{ \"startDate\": \""+analyticsServices.formatDateGoogle.format(analyticsServices.getDateFrom()).toString()+"\", \"endDate\": \""+analyticsServices.formatDateGoogle.format(nowDate).toString()+"\", \"dimensions\": [\"PAGE\"], \"startRow\": 25000, \"rowLimit\": 25000,  \"dataState\": \"ALL\" }";
				
				LOG.debug("dataJson " + dataJson);
				
				//Buscamos las noticias en google Analytivs
				JSONObject jsonDataAnalytics = new JSONObject();
				if (CAllAPI.equals("true")) {
					analyticsServices.callAnalyticApis(dataJson);
				}else {
					String siteUrl = configxsml.getParam(SITE, PUBLICATION, "analyticsView", "siteUrl", "");
					siteUrl = siteUrl.replaceAll("https://","https%3A%2F%2F");
					siteUrl = siteUrl.replaceAll("http://","http%3A%2F%2F");

					String YOUR_API_KEY = gservice.getApiKey();
					
					String queryUrl = "https://searchconsole.googleapis.com/webmasters/v3/sites/"+siteUrl+"/searchAnalytics/query?key="+YOUR_API_KEY;
					URL urlObject = new URL(queryUrl);
					HttpURLConnection con = (HttpURLConnection)urlObject.openConnection();
					con.setRequestMethod("POST");
					
					con.setRequestProperty("Authorization", "Bearer "+gservice.getCredential().getAccessToken());
					con.setRequestProperty("Content-Type", "application/json");
					
					con.setDoOutput(true);
					DataOutputStream wr = new DataOutputStream(con.getOutputStream());
					wr.writeBytes(dataJson);
					wr.flush();
					wr.close();

					if (con.getResponseCode() != 200) {
						LOG.debug("NO HAY DATOS");
						StringWriter writer = new StringWriter();
						IOUtils.copy(con.getErrorStream(), writer, "UTF-8");
						jsonDataAnalytics = JSONObject.fromObject(writer.toString());  
						jsonDataAnalytics.put("status","error");
						jsonDataAnalytics.put("errorCode","008.019"); 
						
					}else{
						LOG.debug("HAY DATOS");
						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
						String inputLine;
						StringBuffer responseBF = new StringBuffer(); 
						while ((inputLine = in.readLine()) != null){
							responseBF.append(inputLine);
						}
						in.close();
						String respuesta = responseBF.toString();
						
						jsonDataAnalytics = JSONObject.fromObject(respuesta);  
						if (jsonDataAnalytics.has("error"))
							jsonDataAnalytics.put("status","error");
						else
							jsonDataAnalytics.put("status","ok");
						
					}
				}
				
				//Buscamos todas las noticias en la tabla del CMS. 
				Map<String, NewsAnalyticsData> analyticDataCMS = analyticsServices.getPubNewsDataToMap();
				
				if (jsonDataAnalytics.has("error")) {
					
					RESULTLOG = "Error al pedir los datos en Analytics. Activar los DEBUG. Code:" + jsonDataAnalytics.getString("code") + ", message: " + jsonDataAnalytics.getString("message") ;
					
					LOG.debug("Error al pedir los datos a la api de Analytics. Datos de call:" + dataJson );
					LOG.debug("Respuesta: " + jsonDataAnalytics);
					
				} else {
					JSONArray dataRows= jsonDataAnalytics.getJSONArray("rows");
					
					for (int i = 0 ;  i < dataRows.size() ; i++) {
						
						JSONObject dataItem = dataRows.getJSONObject(i);
						
						String dataKeyCanonical = dataItem.getJSONArray("keys").getString(0);
		
						// por cada noticia de analytics, validamos si hay registro en la base de CMS.
						NewsAnalyticsData newsToProcess = analyticDataCMS.get(dataKeyCanonical);
							
						// POSIBLE MEJORA, VALIDAR si no conviene hacer una única consulta en la base de datos, que actulice todas o inserte varias a la vez.. !!!!! 
						if (analyticDataCMS.get(dataKeyCanonical) != null){
							
							// Actualizamos sus datos
							newsToProcess.setClicks(dataItem.getString("clicks"));
							newsToProcess.setCtr(dataItem.getString("ctr"));
							newsToProcess.setPosition(dataItem.getString("position"));
							newsToProcess.setPrints(dataItem.getString("impressions"));
							newsToProcess.setUpdatedDate(nowDate.getTime());
							
							analyticsDAO.updateDataResources(newsToProcess);
							countNewsUpdated++;
							
							LOG.debug("Se actuliza la noticia " +newsToProcess.getPage());
							
						}else {
													
							// Agregamos el registro en nuestra base. 
							newsToProcess.setSitename(SITE);
							newsToProcess.setPublication(Integer.parseInt(PUBLICATION));
							newsToProcess.setPage(dataKeyCanonical);
							newsToProcess.setClicks(dataItem.getString("clicks"));
							newsToProcess.setCtr(dataItem.getString("ctr"));
							newsToProcess.setPosition(dataItem.getString("position"));
							newsToProcess.setPrints(dataItem.getString("impressions"));
							newsToProcess.setUpdatedDate(nowDate.getTime());
							
							analyticsDAO.insertNewsData(newsToProcess);
							countNewsNew ++;
							
							LOG.debug("Se agrega la noticia " +newsToProcess.getPage());
		
						}
						
						if (countNewsUpdated > 0) {
							AnalyticsDateAutomaticServices autoDateServices = new AnalyticsDateAutomaticServices(SITE, Integer.parseInt(PUBLICATION));
							AnalyticsDateAutomatic automaticDate = new AnalyticsDateAutomatic();
							automaticDate.setSitename(SITE);
							automaticDate.setPublication(Integer.parseInt(PUBLICATION));
							automaticDate.setDateUpdated(nowDate.getTime());
							
							if (autoDateServices.getDate() != null)
								autoDateServices.insertDate(automaticDate);
							else
								autoDateServices.updateDate(automaticDate);
							
							
						}
						
					}
					RESULTLOG = "Se procesaron noticias desde Analytics. Nuevas" + countNewsNew + "Actualizadas. " + countNewsUpdated ;
				}
			} else {
				LOG.debug("Se valida con las fechas (" + analyticsServices.getDateFrom().toString() + "-" + analyticsServices.getDateTo()+")");
	
				RESULTLOG = "No hay datos para levantar de analytics ";
	
			}
		}else {
			LOG.debug("No hay datos de credenciales para autorizar");

		}
			

		return RESULTLOG;
		
	}

}	