package com.tfsla.diario.admin.jobs;

import java.util.Date;
import java.util.Map;

import com.tfsla.diario.analytics.data.NewsAnalyticsDataDAO;
import com.tfsla.diario.analytics.model.AnalyticsDateAutomatic;
import com.tfsla.diario.analytics.model.NewsAnalyticsData;
import com.tfsla.diario.analytics.services.AnalyticsDateAutomaticServices;
import com.tfsla.diario.analytics.services.AnalyticsServices;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.scheduler.I_CmsScheduledJob;

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
	protected static final Log LOG = CmsLog.getLog(BorrarVODVaciosJob.class);

	CPMConfig configxsml = CmsMedios.getInstance().getCmsParaMediosConfiguration();

	public String launch(CmsObject cms, Map parameters) throws Exception {

		PUBLICATION = (String) parameters.get("publication");
		SITE = (String) parameters.get("siteName");
		
		AnalyticsServices analyticsServices = new AnalyticsServices(SITE, PUBLICATION);
		NewsAnalyticsDataDAO analyticsDAO = new NewsAnalyticsDataDAO();
		
		if (analyticsServices.checkDataAnalytics()) { //se agrega esta validacion por recomendación de la documentación

			Date nowDate = new Date();
			
			String dataJson = "{"
					+ "  \"startDate\": \""+analyticsServices.formatDateGoogle.format(analyticsServices.getDateFrom()).toString()+"\","
					+ "  \"endDate\": \""+analyticsServices.formatDateGoogle.format(nowDate).toString()+"\","
					+ "  \"dimensions\": ["
					+ "    \"PAGE\""
					+ "  ],"
					+ "\"startRow\": 25000,"
					+ "  \"rowLimit\": 25000,"
					+ "  \"dataState\": \"ALL\""
					+ "}";
				
			//Buscamos las noticias en google Analytivs
			JSONObject jsonDataAnalytics = analyticsServices.callAnalyticApis(dataJson);
			
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

		return RESULTLOG;
		
	}

}	