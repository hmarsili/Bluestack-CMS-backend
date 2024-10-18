package com.tfsla.capcha;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionBaseService;


public class RecaptchaManager {

	private static final Log LOG = CmsLog.getLog(RecaptchaManager.class);
	private String url = "https://www.google.com/recaptcha/api/siteverify";
	private String secret;
	private String client_id;
	
	private boolean success;
	private String hostname = "";
	private String challenge_ts = null;
	private Double score = null;
	private String action = "";
	private String error_codes = null;
	private List<String> error_codes_list = null;
	
	private CmsObject cms;
	
	public RecaptchaManager(CmsObject cmsObject,int version){
		
		this.cms = cmsObject;
		
		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	String publication = "1";
    	
     	TipoEdicionBaseService tService = new TipoEdicionBaseService();
    	try {
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());			
			if (tEdicion!=null) {
				publication = "" + tEdicion.getId();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	String module = "captcha";
 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		
		this.url = config.getParam(siteName, publication, module, "recaptcha-url");
		this.secret = config.getParam(siteName, publication, module, "recaptcha-secret-v"+version,"");
		this.client_id = config.getParam(siteName, publication, module, "recaptcha-client-v"+version,"");

	}
	
	public boolean verify(String gRecaptchaResponse) throws IOException
    {
     	if (gRecaptchaResponse == null || "".equals(gRecaptchaResponse)) 
			return false;
	
     	try{
     		URL obj = new URL(this.url);
     		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
		
     		String postParams = "secret=" + this.secret + "&response="+ gRecaptchaResponse;
		
     		con.setRequestMethod("POST");
     		con.setDoOutput(true);
		
     		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
     		wr.writeBytes(postParams);
     		wr.flush();
     		wr.close();
		
     		int responseCode = con.getResponseCode();
		
     		LOG.debug("Sending 'POST' request to URL : " + url);
     		LOG.debug("Post parameters : " + postParams);
     		LOG.debug("Response Code : " + responseCode);

     		BufferedReader in = new BufferedReader(new InputStreamReader(
     		con.getInputStream()));
				
     		String inputLine;
     		StringBuffer response = new StringBuffer();

     		while ((inputLine = in.readLine()) != null){
     			response.append(inputLine);
     		}
     		in.close();

     		LOG.debug("Respuesta : " + response.toString());
		
     		JsonReader jsonReader = Json.createReader(new StringReader(response.toString()));
     		JsonObject jsonObject = jsonReader.readObject();
			jsonReader.close();
			
			this.success = jsonObject.getBoolean("success");
			
			if(jsonObject.containsKey("hostname"))
				this.hostname = jsonObject.getString("hostname");
			
			if(jsonObject.containsKey("challenge_ts"))
				this.challenge_ts = jsonObject.getString("challenge_ts");
		
			if(jsonObject.containsKey("score"))
				this.score = jsonObject.getJsonNumber("score").doubleValue();
			
			if(jsonObject.containsKey("action"))
				this.action = jsonObject.getString("action");
			
			if(jsonObject.containsKey("error-codes")){
				error_codes = "";
				error_codes_list = new ArrayList<String>();
				
				for(int i=0; i < jsonObject.getJsonArray("error-codes").size(); i++){
					JsonValue jv = jsonObject.getJsonArray("error-codes").get(i);
					String errorTXT = jv.toString();
					       errorTXT = errorTXT.replace("\"", "");
					
					error_codes += ","+errorTXT;
					
					error_codes_list.add(errorTXT);
				}
				
				error_codes = error_codes.substring(1);
			}
		
			return this.success;
		
     	}catch(Exception e){
     		e.printStackTrace();
     		LOG.equals(e);
     		
     		return false;
     	}	
    }	
	
	
	public String getClientId(){
		return this.client_id;
	}
	
	public boolean getSucess(){
		return this.success;
	}
	
	public String getHostname(){
		return this.hostname;
	}

	public String getChallenge_ts(){
		return this.challenge_ts;
	}

	public Double getScore(){
		return this.score;
	}

	public String getAction(){
		return this.action;
	}

	public String getErrorCode(){
		return this.error_codes;
	}

	public List<String> getListErrors(){
		return this.error_codes_list;
	}
}
