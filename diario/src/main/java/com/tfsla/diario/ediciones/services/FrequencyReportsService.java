package com.tfsla.diario.ediciones.services;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.http.HttpStatus;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.productivityPlans.model.PlansUsers;
import com.tfsla.diario.productivityPlans.model.ProductivitiyPlans;

public class FrequencyReportsService {
	
	private String publication;
	private String siteName;
	private String serverId;
	private CPMConfig config;
	private String endpointFrequencyReport;
	private CmsObject cmsObject;
	private String publicationDescription;
	private String publicationImage;
	private String publicationName;
	
	private String moduleName = "productivityPlans";

	public FrequencyReportsService(CmsObject cmsObj) throws Exception {

		siteName = OpenCms.getSiteManager().getCurrentSite(cmsObj).getSiteRoot();
		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion currentPublication = tService.obtenerEdicionOnlineRoot(siteName.replaceAll("/sites/", ""));
		publication = ""+currentPublication.getId();
		
		publicationDescription = currentPublication.getDescripcion();
		publicationImage = currentPublication.getImagePath();
		publicationName = currentPublication.getNombre();
		
		config = CmsMedios.getInstance().getCmsParaMediosConfiguration();

		serverId = config.getParam(siteName, publication, moduleName, "serverId", "");
		endpointFrequencyReport = config.getParam(siteName, publication, moduleName, "endpointFrequencyReport", "");
		
		cmsObject = cmsObj;
		
	}
	
	public FrequencyReportsService(CmsObject cmsObj,String publicationID, String site) throws Exception {

		siteName = site;
		publication = publicationID;
		config = CmsMedios.getInstance().getCmsParaMediosConfiguration();

		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion currentPublication = tService.obtenerTipoEdicion(Integer.parseInt(publication));
		publicationDescription = currentPublication.getDescripcion();
		publicationImage = currentPublication.getImagePath();
		publicationName = currentPublication.getNombre();

		serverId = config.getParam(siteName, publication, moduleName, "serverId", "");
		endpointFrequencyReport = config.getParam(siteName, publication, moduleName, "endpointFrequencyReport", "");
		
		cmsObject = cmsObj;
	}
	
	public JSONObject getFrequencyReportsWithTotals(String user, String from, String to) throws Exception 
	{
		JSONArray jsonArray =  getFrequencyReports(user, from, to);
		JSONArray jsonDetailsArray = new JSONArray();
		
		int totalCount = 0;
        int newsCount = 0;
        int republishedCount = 0;
        int duplicatesCount = 0;
		int automaticCount = 0;
		int manualCount = 0;
		
		if (jsonArray != null) {
            for (int i = 0; i < jsonArray.size(); i++) {
                
            	JSONObject object = jsonArray.getJSONObject(i);
            	JSONObject jsonDetail = new JSONObject();
            	jsonDetail.put("pubHour",object.getString("pubHour"));

            	int total = object.getInt("totalNews");
                int news = object.getInt("firstPublishCount");
                int duplicates = object.getInt("copyCount");
                int republished = total - news - duplicates;
				int automatic = object.getInt("automaticCount");
				int manual = total - automatic;
            	
				jsonDetail.put("total",total);
				jsonDetail.put("new",news);
				jsonDetail.put("republished",republished);
				jsonDetail.put("duplicates",duplicates);
				jsonDetail.put("automatic",automatic);
				jsonDetail.put("manual",manual);
            	
            	jsonDetailsArray.add(jsonDetail);
            	
            	totalCount = totalCount + total;
                newsCount = newsCount + news;
                republishedCount = republishedCount + republished;
                duplicatesCount = duplicatesCount + duplicates;
        		automaticCount = automaticCount + automatic;
        		manualCount = manualCount +  manual;
            }
		 }
		
		JSONObject jsonTotals = new JSONObject();
		jsonTotals.put("total",totalCount);
		jsonTotals.put("new",newsCount);
		jsonTotals.put("republished",republishedCount);
		jsonTotals.put("duplicates",duplicatesCount);
		jsonTotals.put("automatic",automaticCount);
		jsonTotals.put("manual",manualCount);
		
		JSONObject frequencyReports =  new JSONObject();
		
		if(user.equals("_ALL_")) {
			
			JSONObject jsonPublication = new JSONObject();
			
			jsonPublication.put("name",publicationName);
			jsonPublication.put("description",publicationDescription);
			jsonPublication.put("image",publicationImage);
			
			frequencyReports.put("publication",jsonPublication);
			
		}else {
			JSONObject jsonUser = new JSONObject();
			
			String userFullName = "";
			String userImage = "";
			
			try {
				CmsUser cmsUser = cmsObject.readUser(user);
				userFullName = cmsUser.getFullName();
				userImage = (String) cmsUser.getAdditionalInfo("USER_PICTURE");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			jsonUser.put("userName",user);
			jsonUser.put("fullName", userFullName);
			jsonUser.put("image",userImage);
			
			frequencyReports.put("user",jsonUser);
		}
		
		frequencyReports.put("totals",jsonTotals);
		frequencyReports.put("details",jsonDetailsArray);
		
		return frequencyReports;
		
	}
	
	public JSONArray getFrequencyReports(String user, String from, String to) throws Exception 
	{
		String  planId = "";
		
		if(user.trim().equals("_ALL_")){
			planId = "_ALL_";
		}else{
			planId = getPlanId(user);
		}
		
		if (planId == null || planId.equals("")) {
			//el usuario no esta en ningun plan hay que enviar el plan general.
			planId = getGeneralPlanId();
		}
		
		JSONObject jsonbody = new JSONObject();
		
		jsonbody.put("planId",planId);
		jsonbody.put("serverId",serverId);
		jsonbody.put("publication",publication);
		jsonbody.put("user", user); 
		jsonbody.put("from",from);
		jsonbody.put("to",to);
		
		String sBody = jsonbody.toString();
		String result = callAws(endpointFrequencyReport, sBody);
		
		JSONArray jsonArray = JSONArray.fromObject(result);

		return jsonArray;
		
	}
	
	public String callAws(String endpoint, String sBody) throws Exception 
	{
		StringRequestEntity requestEntity = new StringRequestEntity(
				sBody,
			    "application/json",
			    "UTF-8");		
		
		PostMethod postMethod = new PostMethod(endpoint);
		postMethod.setRequestEntity(requestEntity);
		
		HttpClient httpClient = new HttpClient(); 
		int statusCode = httpClient.executeMethod(postMethod);

		if (statusCode == HttpStatus.SC_OK) {
			 return postMethod.getResponseBodyAsString();
        }
		
		return "";
	}
	
	public String getPlanId(String userName) {
		
		String planId = "";
		
		PlansUsersServices puService;
		try {
			JSONObject jsonAuth =  new JSONObject();
			jsonAuth.put("siteName",siteName);
			jsonAuth.put("publication",publication);
			
			JSONObject jsonreq =  new JSONObject();
			jsonreq.put("authentication",jsonAuth);
			
			puService = new PlansUsersServices(cmsObject,jsonreq);
			PlansUsers planForUser = puService.existPlanForUser(userName);
			planId = planForUser.getId();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return planId;
		
	}
	
	public String getGeneralPlanId() {
		
		String planId = "";
		
		ProductivityPlanServices ppService;
		try {
			JSONObject jsonAuth =  new JSONObject();
			jsonAuth.put("siteName",siteName);
			jsonAuth.put("publication",publication);
			
			JSONObject jsonreq =  new JSONObject();
			jsonreq.put("authentication",jsonAuth);
			
			ppService = new ProductivityPlanServices(jsonreq);
			ProductivitiyPlans planGeneral = ppService.getGeneralProductivitiyPlans();
			planId = planGeneral.getId();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return planId;
		
	}
	
}
