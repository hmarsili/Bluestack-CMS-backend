package com.tfsla.diario.admin.jobs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tfsla.diario.analytics.data.NewsAnalyticsDataDAO;
import com.tfsla.diario.analytics.model.AnalyticsDateAutomatic;
import com.tfsla.diario.analytics.model.NewsAnalyticsData;
import com.tfsla.diario.analytics.services.AnalyticsDateAutomaticServices;
import com.tfsla.diario.analytics.services.AnalyticsServices;
import com.tfsla.diario.ediciones.services.SearchConsoleService;
import com.tfsla.diario.friendlyTags.TfsNoticiasListTag;
import com.tfsla.diario.newsCollector.A_NewsCollector;
import com.tfsla.utils.UrlLinkHelper;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.scheduler.I_CmsScheduledJob;
import java.text.SimpleDateFormat;

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
		
		AnalyticsServices analyticsServices = new AnalyticsServices(SITE, PUBLICATION);
		NewsAnalyticsDataDAO analyticsDAO = new NewsAnalyticsDataDAO();
		
		SearchConsoleService gservice = new SearchConsoleService();
		gservice.initializeContext(SITE, PUBLICATION);

			if (analyticsServices.checkDataAnalytics()) { //se agrega esta validacion por recomendación de la documentación
	
				Date nowDate = new Date();
				String today = analyticsServices.formatDateGoogle.format(nowDate).toString();
				String dataJson = "{ \"startDate\": \""+today+"\", \"endDate\": \""+today+"\", \"dimensions\": [\"PAGE\",\"DATE\"], \"startRow\": 1, \"rowLimit\": 25000,  \"dataState\": \"ALL\" }";
		
				//Buscamos las noticias en google Analytivs
				LOG.debug("vamos a buscar las notticias a API" + dataJson);
				JSONObject jsonDataAnalytics = 	analyticsServices.callAnalyticApis(dataJson);
				
				//Buscamos todas las noticias en la tabla del CMS. 
				Map<String, NewsAnalyticsData> analyticDataCMS = analyticsServices.getPubNewsDataToMap();
				
				//Buscamos las ultimas noticias publicadas.
				Map<String, String> newsLastPub = new HashMap<String, String>();
				Map<String,Object> paramNews = new HashMap<String,Object>();
				String order = "modification-date desc ";
				SimpleDateFormat formatDate  = new SimpleDateFormat("yyyyMMdd");
				
				paramNews.put("size",100);
				paramNews.put("page",1);
				paramNews.put("publication",PUBLICATION);
				paramNews.put("from",formatDate.format(analyticsServices.getDateFrom()).toString());
				paramNews.put("to",formatDate.format(nowDate)+ " 2359");
				paramNews.put("state","publicada");								
				paramNews.put("order","modification-date desc ");
				paramNews.put("params-count",(Integer)paramNews.size());
					
				List<CmsResource> noticias = new ArrayList<CmsResource>();
				TfsNoticiasListTag tfsNoticiasListTag = new TfsNoticiasListTag();
				A_NewsCollector bestMatchCollector = tfsNoticiasListTag.getNewCollector(paramNews,order);
		
				if (bestMatchCollector!=null){
					noticias = bestMatchCollector.collectNews(paramNews,cms);
					for ( CmsResource resource : noticias) {
						
						String canonical = UrlLinkHelper.getCanonicalLink(resource, cms, null);
						
						newsLastPub.put(canonical,"true");
					}
				}

				LOG.debug("Existen " + newsLastPub.size() + " noticias publicadas en las ultimas hs. Params" + paramNews.toString());

				
				if (jsonDataAnalytics.has("error")) {
					
					RESULTLOG = "Error al pedir los datos en Analytics. Activar los DEBUG. Code:" + jsonDataAnalytics.getString("code") + ", message: " + jsonDataAnalytics.getString("message") ;
					
					LOG.debug("Error al pedir los datos a la api de Analytics. Datos de call:" + dataJson );
					
					
				} else {
					
					
					JSONArray dataRows= jsonDataAnalytics.getJSONArray("rows");
					
					LOG.debug("Existen Datos en Analytics, se van a procesar " + dataRows.size() + " urls.");
					
					for (int i = 0 ;  i < dataRows.size() ; i++) {
						JSONObject dataItem = dataRows.getJSONObject(i);
						
						String dataKeyCanonical = dataItem.getJSONArray("keys").getString(0);
					
						if (analyticDataCMS.get(dataKeyCanonical) != null){
							
							NewsAnalyticsData newsToupdate = new NewsAnalyticsData();
							
							// Actualizamos sus datos
							newsToupdate.setClicks(dataItem.getString("clicks"));
							newsToupdate.setCtr(dataItem.getString("ctr"));
							newsToupdate.setPosition(dataItem.getString("position"));
							newsToupdate.setPrints(dataItem.getString("impressions"));
							newsToupdate.setSitename(SITE);
							newsToupdate.setPublication(Integer.parseInt(PUBLICATION));
							newsToupdate.setPage(dataKeyCanonical);

							analyticsDAO.updateDataResources(newsToupdate);
							
							countNewsUpdated ++;
						} else if (newsLastPub.get(dataKeyCanonical) != null){
				
							// Agregamos el registro en nuestra base. 
							NewsAnalyticsData newsToInsert = new NewsAnalyticsData();
							newsToInsert.setSitename(SITE);
							newsToInsert.setPublication(Integer.parseInt(PUBLICATION));
							newsToInsert.setPage(dataKeyCanonical);
							newsToInsert.setClicks(dataItem.getString("clicks"));
							newsToInsert.setCtr(dataItem.getString("ctr"));
							newsToInsert.setPosition(dataItem.getString("position"));
							newsToInsert.setPrints(dataItem.getString("impressions"));
							
							analyticsDAO.insertNewsData(newsToInsert);
							
							countNewsNew ++;
						}
						
					}
					
					LOG.debug("Se actualizaron " +countNewsUpdated + " noticias y se agregaron " + countNewsNew);
					
					if (countNewsNew > 0 || countNewsUpdated > 0) {
						AnalyticsDateAutomaticServices autoDateServices = new AnalyticsDateAutomaticServices(SITE, Integer.parseInt(PUBLICATION));
						AnalyticsDateAutomatic automaticDate = new AnalyticsDateAutomatic();
						automaticDate.setSitename(SITE);
						automaticDate.setPublication(Integer.parseInt(PUBLICATION));
						automaticDate.setDateUpdated(nowDate.getTime());
						
						if (autoDateServices.getDate() == null)
							autoDateServices.insertDate(automaticDate);
						else
							autoDateServices.updateDate(automaticDate);
					
						LOG.debug("Se actalizo la tabla de fecha " +automaticDate.getDateUpdated() + " - " + automaticDate.getSitename() + " - " + automaticDate.getPublication());
						
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