package com.tfsla.diario.analytics.services;

import org.apache.commons.logging.Log;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.main.CmsLog;

import com.tfsla.diario.analytics.data.NewsAnalyticsDataDAO;
import com.tfsla.diario.analytics.model.NewsAnalyticsData;
import com.tfsla.diario.ediciones.services.SearchConsoleService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class AnalyticsServices {
	
	private static final Log LOG = CmsLog.getLog(AnalyticsServices.class);

	private CPMConfig configura;
	// private CmsWorkplaceSettings m_settings;
	
	private String moduleConfiguration;
	private String publication;
	private String siteName;
	private String siteUrl;
	
	private boolean updatedDateManual = false;
	private int	updatedDateManualTime = 0;
	private int newsFromLastHoursPublish = 0;
	
	private SimpleDateFormat formatDate  = new SimpleDateFormat("yyyyMMdd");
	public SimpleDateFormat formatDateGoogle  = new SimpleDateFormat("yyyy-MM-dd");
	
	private SearchConsoleService gservice;

	public AnalyticsServices( String _siteName, String _publication ) throws Exception {
		
		LOG.debug("Se inicializa la clase AnalyticsServices ");
		configura = CmsMedios.getInstance().getCmsParaMediosConfiguration(); 
		
		siteName = _siteName;
		publication = _publication;

		moduleConfiguration = "analyticsView";

		updatedDateManual = configura.getBooleanParam(siteName, publication, moduleConfiguration, "updatedDateManual",false);
		updatedDateManualTime = configura.getIntegerParam(siteName, publication, moduleConfiguration, "updatedDateManualTime",30);
		newsFromLastHoursPublish = configura.getIntegerParam(siteName, publication, moduleConfiguration, "newsFromLastHoursPublish", 72);
		siteUrl = configura.getParam(siteName, publication, moduleConfiguration, "siteUrl", "");
		
		gservice = new SearchConsoleService();
		LOG.debug("Se va a iniciar  SearchConsoleService con " + siteName +" - " +publication);
		
		gservice.initializeContext(siteName,publication);


	}
	
	public AnalyticsServices( String _siteName, String _publication, SearchConsoleService _gservice) throws Exception {

		configura = CmsMedios.getInstance().getCmsParaMediosConfiguration(); 
		
		siteName = _siteName;
		publication = _publication;

		moduleConfiguration = "analyticsView";

		updatedDateManual = configura.getBooleanParam(siteName, publication, moduleConfiguration, "updatedDateManual",false);
		updatedDateManualTime = configura.getIntegerParam(siteName, publication, moduleConfiguration, "updatedDateManualTime",30);
		newsFromLastHoursPublish = configura.getIntegerParam(siteName, publication, moduleConfiguration, "newsFromLastHoursPublish", 72);
		siteUrl = configura.getParam(siteName, publication, moduleConfiguration, "siteUrl", "");
		
		gservice = _gservice;

	}
	
	/**
	 * Metodo que obtiene todas las noticias de una publicacion desde la tabla de CMS.
	 * @return
	 */
	
	public Map<String, NewsAnalyticsData> getPubNewsDataToMap() {

		NewsAnalyticsDataDAO analyticsDAO = new NewsAnalyticsDataDAO();
		Map<String, NewsAnalyticsData> listData = null;
		try {
			listData = analyticsDAO.getPubNewsDataToMap(siteName, Integer.parseInt(publication));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return listData;
	}
	
	/**
	 * Metodo que obtiene una noticia desde la tabla de CMS.
	 * @return
	 */
	
	public NewsAnalyticsData getNewsData(String canonical) {

		NewsAnalyticsDataDAO analyticsDAO = new NewsAnalyticsDataDAO();
		NewsAnalyticsData newsData = null;
		try {
			newsData = analyticsDAO.getNewsData(siteName, Integer.parseInt(publication),canonical);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return newsData;
	}
	
	/**
	 * Metodo que actualiza la noticia en la ta~-bla del CMS. 
	 * @return
	 */

	public void updateAnalyticsData(NewsAnalyticsData newsAnalyticsData) throws Exception {
		
		LOG.debug(" -------- INI updateAnalyticsData -------- " );
		LOG.debug(" newsAnalyticsData " + newsAnalyticsData );
		
		NewsAnalyticsDataDAO analyticsDAO = new NewsAnalyticsDataDAO();
		try {
			analyticsDAO.updateDataResources(newsAnalyticsData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		LOG.debug(" -------- FIN updateAnalyticsData -------- " );
		
	}
	
	/**
	 * Metodo que agrega una noticia a la tabla del CMS. 
	 * @return
	 */

	public void insertAnalyticsData(NewsAnalyticsData newsAnalyticsData) throws Exception {
		
		NewsAnalyticsDataDAO analyticsDAO = new NewsAnalyticsDataDAO();
		try {
			analyticsDAO.insertNewsData(newsAnalyticsData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Método que valida si existen recursos para la fecha solicitada
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean checkDataAnalytics() throws Exception {

		String datajson = "{\"startDate\":\""+formatDateGoogle.format(getDateFrom()).toString()+"\", \"endDate\":\""+formatDateGoogle.format(new Date()).toString()+"\", \"dimensions\":[\"DATE\"]}";
		
		JSONObject jsondata = callAnalyticApis(datajson);
		if (jsondata.has("rows"))
			return true;
		else
			return false;
	}
	
	/**
	 * Método valida si existen datos para su actualizacion.
	 * 
	 * @return
	 * @throws Exception
	 
	public boolean updateNewsData() throws Exception {

		String datajson = "{\"startDate\":\""+formatDateGoogle.format(getDateFrom()).toString()+"\","
							+ "\"endDate\":\""+formatDateGoogle.format(new Date()).toString()+"\","
							+ "\"dimensions\":[\"DATE\"]}"
							+ "";
		
		JSONObject jsondata = callAnalyticApis(datajson);
		
		if (jsondata.has("rows")){
			return true;
		}else {
			return false;

		}
	}

	*/
	/**
	 * Metodo utilizado para llamar a la api de Google Search Console - Analytcis, para obtener datos. 
	 * 
	 * @param datajson: json de la consulta de datos que queremos traer. 
	 * @return json con los datos de los usuarios. 
	 * @throws Exception
	 */
	public JSONObject callAnalyticApis(String datajson) throws Exception {

		JSONObject jsono = new JSONObject();
		
		if (gservice.isAuthorized()) {
		String YOUR_API_KEY = gservice.getApiKey();
		
		siteUrl = siteUrl.replaceAll("https://","https%3A%2F%2F");
		siteUrl = siteUrl.replaceAll("http://","http%3A%2F%2F");
		
		LOG.debug(" -------- INI callAnalyticApis -------- " );
		LOG.debug("YOUR_API_KEY " + YOUR_API_KEY + " siteUrl: " + siteUrl );
		LOG.debug("datajson " + datajson); 
		
		String queryUrl = "https://searchconsole.googleapis.com/webmasters/v3/sites/"+siteUrl+"/searchAnalytics/query?key="+YOUR_API_KEY;
		URL urlObject = new URL(queryUrl);
		HttpURLConnection con = (HttpURLConnection)urlObject.openConnection();
		con.setRequestMethod("POST");
		
		LOG.debug("gservice.getCredential " + gservice.getCredential()  );
		LOG.debug("gservice.getCredential().getAccessToken() " + gservice.getCredential().getAccessToken() );

		con.setRequestProperty("Authorization", "Bearer "+gservice.getCredential().getAccessToken());
		con.setRequestProperty("Content-Type", "application/json");
		
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(datajson);
		wr.flush();
		wr.close();
		if (con.getResponseCode() != 200) {
			StringWriter writer = new StringWriter();
			IOUtils.copy(con.getErrorStream(), writer, "UTF-8");
			jsono = JSONObject.fromObject(writer.toString());  
			jsono.put("status","error");
			jsono.put("errorCode","008.019"); 
			
		}else{
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer responseBF = new StringBuffer(); 
			while ((inputLine = in.readLine()) != null){
				responseBF.append(inputLine);
			}
			in.close();
			String respuesta = responseBF.toString();
			
			jsono = JSONObject.fromObject(respuesta);  
			if (jsono.has("error"))
				jsono.put("status","error");
			else
				jsono.put("status","ok");
			
		}

		LOG.debug("return jsono " + jsono); 
		LOG.debug(" -------- FIN callAnalyticApis -------- " );
		} else {
			jsono.put("status","error");
			jsono.put("error","no esta autorizado.");
		}
		
		return jsono;
		
		
	}
	
	/**
	 * Método que actuliza manualmente una noticica en el CMS con datos de la api de Google.
	 * 
	 * @param canonical
	 * @return JSONObject
	 * @throws Exception 
	 */
	
	public JSONObject updateManual(String canonical) throws Exception {
		JSONObject jsonResult = new JSONObject();
		
		if (updatedDateManualEnabeld()){
			
			//busco los datos en la base del CMS.
			NewsAnalyticsData newsToUpdated = getNewsData(canonical);
			if (newsToUpdated != null ){
				
				Calendar calToNextUpdated = Calendar.getInstance();
				calToNextUpdated.setTimeInMillis(newsToUpdated.getUpdatedDate());
				calToNextUpdated.set(Calendar.MILLISECOND, 0);
				calToNextUpdated.set(Calendar.SECOND, 0);
				calToNextUpdated.set(Calendar.MINUTE, +updatedDateManualTime);
				
				Date dateToNextUpdated = calToNextUpdated.getTime();
				Long dateNow = new Date().getTime();
		
				//valido si la fecha actual es mayor a la fecha de proxima ejecucion.
				if (dateNow > dateToNextUpdated.getTime()) {
					
					String jsonAnalytics = "{\"startDate\":\""+formatDateGoogle.format(getDateFrom()).toString()+"\",\"endDate\":\""+formatDateGoogle.format(dateNow).toString()+"\",\"dimensions\":[\"DATE\"],\"dimensionFilterGroups\":[{\"filters\":[{\"dimension\":\"PAGE\",\"expression\":\""+canonical+"\",\"operator\":\"EQUALS\"}]}],\"aggregationType\":\"BY_PAGE\",\"rowLimit\":500,\"startRow\":0}";
					
					//pedimos los datos a la api
					JSONObject jsonDataAnalytics = callAnalyticApis(jsonAnalytics);
					
					if (jsonDataAnalytics.has("rows") && jsonDataAnalytics.getJSONArray("rows").size()>0) {
						
						JSONArray dataRows= jsonDataAnalytics.getJSONArray("rows");					
						// Se cambia porque mostramos del ultimo dia no un acumulado. 
						// for (int i = 0 ;  i < dataRows.size() ; i++) {
							
							JSONObject dataItem = dataRows.getJSONObject(dataRows.size()-1);

							// Actualizamos los datos
							newsToUpdated.setClicks(dataItem.getString("clicks"));
							newsToUpdated.setCtr(dataItem.getString("ctr"));
							newsToUpdated.setPosition(dataItem.getString("position"));
							newsToUpdated.setPrints(dataItem.getString("impressions"));
							newsToUpdated.setUpdatedDate(dateNow);
							
							updateAnalyticsData(newsToUpdated);
							jsonResult.put("status","ok");
							jsonResult.put("dataUpdated",newsToUpdated);
	
					//	}
					} else {
						if (jsonDataAnalytics.has("error")) {
							//Error al pedir los datos en Analytics .
							jsonResult.put("status","fail");
							jsonResult.put("error",jsonDataAnalytics);
							jsonResult.put("errorCode","018.004");
						} else {
							//No hay datos en analytics para esta noticia.
							jsonResult.put("status","fail");
							jsonResult.put("errorCode","018.003");
						}
					}		
					
				}else {
					//No paso el tiempo configurado permitido para volver a solicitar la noticia (updatedDateManualTime).
					jsonResult.put("status","fail");
					jsonResult.put("error",updatedDateManualTime);
					jsonResult.put("errorCode","018.001"); 
				}					
				
			}else {
				//No tenemos datos en nuestra tabla para esa noticia. 
				jsonResult.put("status","fail");
				jsonResult.put("error",canonical);
				jsonResult.put("errorCode","018.002"); 

			}
			
		}else{
			//no esta habilitada la opcion para actulizacion manual.
			jsonResult.put("status","fail");
			jsonResult.put("errorCode","018.000"); 
	
		}
		return jsonResult;
	}
	
	/**
	 * Metodo que busca al fecha de inicio (de las noticias publicadas) a partir de la cantidad de horas configuradas
	 * @return
	 */
	public Date getDateFrom() {
		Calendar calendarFrom = Calendar.getInstance();
		calendarFrom.setTimeZone(TimeZone.getTimeZone("GMT-0"));
		calendarFrom.add(Calendar.HOUR, -newsFromLastHoursPublish);
		calendarFrom.set(Calendar.MILLISECOND, 0);
		calendarFrom.set(Calendar.SECOND, 0);
		calendarFrom.set(Calendar.MINUTE, 0);
		
		Date dateFrom = calendarFrom.getTime();
		
		return dateFrom;
	}
	
	/**
	 * Metodo que obtiene la fecha actual;
	 * @return
	 */
	public String getDateTo() {
		return  formatDate.format(new Date()).toString();	
	}
	
	/**
	 * Metodo que devuelve si la actulizacion manual esta habilitada
	 */
	public boolean updatedDateManualEnabeld() {
		return updatedDateManual;
	}
	
	/**
	 * Metodo que devuelve si la noticica puede ser actualizada de forma manual.
	 */
	public boolean updatedDateManual() {
		//buscamos la noticia. 
		
		return updatedDateManual;
	}

	public int updatedDateManualTime() {
		return updatedDateManualTime;
	}
	
}