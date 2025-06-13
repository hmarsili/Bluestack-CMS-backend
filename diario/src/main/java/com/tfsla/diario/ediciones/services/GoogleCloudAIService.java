package com.tfsla.diario.ediciones.services;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentialsUtils;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.api.HarmCategory;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.api.SafetySetting;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.google.cloud.vertexai.generativeai.ResponseStream;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.jsoup.Jsoup;

public class GoogleCloudAIService {
	
	private static final Log LOG = CmsLog.getLog(GoogleCloudAIService.class);
	private static Map<String, GoogleCloudAIService> instances = new HashMap<String, GoogleCloudAIService>();
	
	protected CmsObject cmsObject = null;
	protected String siteName;
	protected String publication;

	protected String client_id = null;
	protected String client_email = null;
	protected String private_key = null;
	protected String private_key_id = null;
	
	protected String temperature = null;
	protected String maxOutputTokens = null;
	protected String topP = null;
	protected String topK = null;
	
	protected String project = null;
	protected String location = null;
	protected String publisher = null;
	protected String model = null;
	
	protected String promptTitles = null;
	protected String promptTitlesSocial = null;
	protected String promptSugestions = null;
	protected String promptResumen = null;
	
	protected String promptsContexto = null;
	protected List<String> promptsAsistencia = null;
	
	public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
	public static final Charset UTF_8 = Charset.forName("UTF-8");

	public static GoogleCloudAIService getInstance(CmsObject cms) {
		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
		String publication = "0";
		try {
			publication = String.valueOf(PublicationService.getPublicationId(cms));
		} catch (Exception e) {
			LOG.error(e);
		}

		String id = siteName + "||" + publication;

		GoogleCloudAIService instance = instances.get(id);

		if (instance == null) {
			instance = new GoogleCloudAIService(cms,siteName, publication);

			instances.put(id, instance);
		}

		instance.cmsObject = cms;

		return instance;
	}
	
	public GoogleCloudAIService() {}

	
	public GoogleCloudAIService(CmsObject cmsObject, String siteName, String publication) {
		this.siteName = siteName;
		this.publication = publication;
		this.cmsObject = cmsObject;
	}
	
	protected String getModuleName() {
		return "googleCloudAI";
	}
	
	
	public boolean isAIEnabled() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(siteName, publication, getModuleName(), "AIEnabled", false);
	}

	protected String getClientId() {
		return (client_id!=null ? client_id : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "clientId", ""));
	}

	protected String getClientEmail() {
		return (client_email!=null ? client_email : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "clientEmail", ""));
	}

	protected String getPrivateKey() {
		return (private_key!=null ? private_key :
			org.apache.commons.lang.StringEscapeUtils.unescapeJava(
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "privateKey", ""))
			);
	}

	protected String getPrivateKeyId() {
		return (private_key_id!=null ? private_key_id : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "privateKeyId", "")
		);
	}
   
	protected String getTemperature() {
		return (temperature!=null ? temperature : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "temperature", "0.2"));
	}
    
	protected String getMaxOutputTokens() {
		return (maxOutputTokens!=null ? maxOutputTokens : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "maxOutputTokens", "256"));
	}
    
	protected String getTopP() {
		return (topP!=null ? topP : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "topP", "0.95"));
	}
    
	protected String getTopK() {
		return (topK!=null ? topK : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "topK", "40"));
	}
    
	protected String getProject() {
		return (project!=null ? project : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "project", ""));
	}
    
	protected String getLocation() {
		return (location!=null ? location : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "location", "us-central1"));
	}
	
	protected String getPublisher() {
		return (publisher!=null ? publisher : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "publisher", "google"));
	}
	
	protected String getModel() {
		return (model!=null ?  model : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "model", ""));
	}
	
	protected String getPromptTitles() {
		return (promptTitles!=null ? promptTitles : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "prompt_titulos", null));
	}
	
	protected String getPromptResumen() {
		return (promptResumen!=null ? promptResumen : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "prompt_resumen", null));
	}
	
	protected String getPromptTitlesSocial() {
		return (promptTitlesSocial!=null ? promptTitlesSocial : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "prompt_titulos_redes", null));
	}
	
	protected String getPromptSugestions() {
		return (promptSugestions!=null ? promptSugestions : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "prompt_sugerencias", null));
	}
	
	protected String getPromptGeneralContexts() {
		return (promptsContexto!=null ? promptsContexto : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "prompts_contexto", null));
	}
	
	protected List <String> getPromptsAssistance() {
		return (promptsAsistencia = CmsMedios.getInstance().getCmsParaMediosConfiguration().getParamList(siteName, publication, getModuleName(), "prompts_asistencia"));
	}
	    
    protected GoogleCredentials getCredentials(){
    
    	GoogleCredentials credentials = null;
    	
    	List<String> scopes = new ArrayList<String>();
	    scopes.add("https://www.googleapis.com/auth/cloud-platform");
	    scopes.add("https://www.googleapis.com/auth/cloud-platform.read-only");
    	 
    	try {
	    	credentials = new ServiceAccountCredentialsUtils()
	    			.setClientId(getClientId())
					.setClientEmail(getClientEmail())
					.setPrivateKeyId(getPrivateKeyId())
					.setPrivateKey(getPrivateKey())
	    			.getServiceAccountCredentials(scopes);
	    } catch (IOException e) {
	    			e.printStackTrace();
	    }
    
    	return credentials;
    }
    
    public String getPredictText(String questionToAI) {
    	
    	String reponse = null;
    	
    	try {
    		reponse = predictTextPrompt(questionToAI);
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
    	
    	return reponse;
    }
    
    //NAA-3314. Se modifica la clase agregando una validacion para buscar todos los componentes ó solo lo pedido desde el front.
    public JSONObject getSuggestionAI(String newsPath) throws Exception{
        return getSuggestionAI(newsPath, "ALL");
    }
    
    public JSONObject getSuggestionAI(String newsPath, String seachComponent) throws Exception{
    
    	JSONObject JsonResult =  new JSONObject();

    	CmsFile file = getCmsFile(newsPath);
    	I_CmsXmlDocument fileContent = CmsXmlContentFactory.unmarshal(this.cmsObject, file);
    	
    	String newsContent = extractConntent(fileContent,new String[]{"cuerpo"});
    	String newsTitle = extractConntent(fileContent,new String[]{"titulo"});
    	String newsSubTitle = extractConntent(fileContent,new String[]{"copete"});
    	
    	boolean searchAll = (seachComponent.equals("ALL"))? true : false; //NAA-3314
    	 
    	if (searchAll || seachComponent.equals("TITULO")) { //NAA-3314
	    	List<String> titulos = getPredictList(newsContent, "titles");
	    			  
	    	List<String> titulosRedes = getPredictList(newsContent, "titlesSocialNetworks");
	    	
	    	JsonResult.put("titulos",titulos);
	    	JsonResult.put("titulosRedes",titulosRedes);
    	}
    	
    	if (searchAll || !seachComponent.equals("TITULO")) { //NAA-3314 
	    	List <String> listPrompts = getPromptsAssistance();
	    	
	    	JSONArray groupsJsonArray = new JSONArray();
	   
	    	for (int i = 0; i < listPrompts.size(); i++) {
		    	String context = getPromptGeneralContexts(); //NAA-3395

	    	    String group = listPrompts.get(i);
	    	    
	    	    if (searchAll || seachComponent.equals(group.toUpperCase())) { //NAA-3314
		    	    String groupTitle =  CmsMedios.getInstance().getCmsParaMediosConfiguration().getItemGroupParam(siteName, publication, getModuleName(), group, "titulo", "");
		    	  
		    	    String contextGroup = CmsMedios.getInstance().getCmsParaMediosConfiguration().getItemGroupParam(siteName, publication, getModuleName(), group, "contexto", "");
		    		
		    	    if(contextGroup!=null && !contextGroup.trim().equals(""))
		    	    	context = contextGroup;
		    	    
		    	    String promptAssistance =  CmsMedios.getInstance().getCmsParaMediosConfiguration().getItemGroupParam(siteName, publication, getModuleName(), group, "consulta", "");
		    	  
		    	    JSONArray assistance = getPredictList(newsTitle,newsSubTitle,newsContent, context,promptAssistance);
		        	 
		    	    JSONObject groupInfoJson = new JSONObject();
		    	    groupInfoJson.put("titulo",groupTitle); 
		    	    groupInfoJson.put("items",assistance); 
		    	    
		    	    groupsJsonArray.add(groupInfoJson);
	    	    }
	    	}
	    	
	    	JSONObject groupJson = new JSONObject();
		    groupJson.put("grupo",groupsJsonArray); 
	    	
		    JsonResult.put("asistencia",groupJson);
    	}
    	return JsonResult;
    }
    
    private CmsFile getCmsFile(String resourceName) throws CmsException {
    	
    	String filePath = CmsWorkplace.getTemporaryFileName(resourceName);
    	
    	if(!cmsObject.existsResource(filePath, CmsResourceFilter.ALL))
    		filePath = resourceName;
    	
    	CmsFile file = cmsObject.readFile(filePath);
    	
    	return file;
    	
    }
    
    public JSONArray getPredictList(String newsTitle,String newsSubTitle,String newsContent,String context, String prompt) {
    	    	
    	String newsSubTitleFixed = newsSubTitle.replaceAll("\"", "'"); 
    	String newsContentFixed = newsContent.replaceAll("\"", "'"); 
    	
    	String contentResponse = "";
    	JSONArray jsonResponse = new JSONArray();
    
    	String dataArticle = "\n Título: "+ newsTitle
				   + "\n Subtítulo: "+ newsSubTitleFixed
				   + "\n Cuerpo: "+ newsContentFixed;
 	
    	String content = context.replaceAll("%1", dataArticle) + "\n" + prompt.replaceAll("%1", dataArticle);
 		
    	try {
			String response = predictTextPrompt(content);
			
			if (!response.contains( " \"sugerencias\": ")) {
		    	
				String textPrompt = prompt.replaceAll("%1",content);
		    	String textFixed = textPrompt.replaceAll("\"", "'"); 
		    	
		    	String resumen = getPredictText(textFixed);
		    	
		    	JSONObject jsonsub = new JSONObject();
		    	jsonsub.put("sugerencia", resumen);
		    	
				jsonResponse.add(jsonsub);
				
				LOG.debug("textCompleto");
			
			}else {
				
			JsonElement jsonObject = JsonParser.parseString(response);
			
			String respuesjson = "[ ";
			JsonArray sugerencias =  jsonObject.getAsJsonObject().get("sugerencias").getAsJsonArray();
			for (JsonElement element : sugerencias) {
		
				Iterator<String> it = element.getAsJsonObject().keySet().iterator();
				
				String jsonAttrName = it.next();
				String sugerencia = (element.getAsJsonObject().get(jsonAttrName).toString() !=null ? element.getAsJsonObject().get(jsonAttrName).toString() : "");
				
				String justificacion = "";
				if (it.hasNext()) {
					jsonAttrName = it.next();
					justificacion = (element.getAsJsonObject().get(jsonAttrName).toString() != null ? element.getAsJsonObject().get(jsonAttrName).toString() :"");
				}
				if (!sugerencia.startsWith("\""))
					sugerencia = "\"" + sugerencia + "\"";
				if (!justificacion.startsWith("\""))
					justificacion = "\"" + justificacion + "\"";
				
				respuesjson += "{ "
						+ "\"sugerencia\": " + sugerencia
						+   ","
						+ "\"justificacion\": " + justificacion
						+   "},";
			}
			
			if (sugerencias.size()>0)
				respuesjson = respuesjson.replaceAll(",$", "");
			
			respuesjson += "] ";
			
		
			JsonElement jsonElement = JsonParser.parseString(respuesjson);
			
			if(jsonElement.isJsonArray())
				jsonResponse = JSONArray.fromObject(respuesjson);		
			
			LOG.debug("SE VA POR ACA 2 ");
			}
			
    	}catch(JsonSyntaxException e){
			e.printStackTrace();
			String jsonString = "[{\"sugerencia\": \"Error\", \"justificacion\": \"No se pudo procesar correctamente la solicitud\"}]";
			jsonResponse = JSONArray.fromObject(jsonString);
			
    	}catch (IOException e) {
			e.printStackTrace();
		}
    	
    	return jsonResponse;
    }
    
    public List<String> getPredictList(String text, String type) {
   
    	String textFixed = "";
    			
    	if(text != null)
    	textFixed =	text.replaceAll("\"", "'"); 
    	 	
    	List <String> listResult = new ArrayList <String>();
    	
    	String textPrompt = "";
    	
    	if(type.equals("corrections")){
    		promptSugestions = getPromptSugestions(); //NAA-3395
    		if(promptSugestions!=null)
    			textPrompt = promptSugestions.replaceAll("%1",textFixed);
    		else
    			textPrompt = "\" Eres periodista y debes indicar que cambiarías en el artículo para que llamen la atención, generar mas interés y que sea óptimo para Discover de Google."
        				+ "\n"	
        				+ "Artículo: \n"
    					+ textFixed
    					+ "\n"	
    					+ "No hacer sugerencias sobre imágenes ni videos.\n"
    					+ "No indicar cambios en el título.\n"
    					+ "Se deben incluir correcciones ortografícas y gramáticales si las hay.\n"
    					+ "Las modificaciones que deberíamos hacer son 5.";
    	}
    	
    	if(type.equals("titles")) {
    		promptTitles = getPromptTitles(); //NAA-3395
    		if(promptTitles != null )
    			textPrompt = promptTitles.replaceAll("%1",textFixed);
    		else
    			textPrompt = "\"Sugerir 5 títulos alternativos con mayor impacto para el artículo"
				    	+ "\n"		
						+ "Artículo: \n"
						+ textFixed;
    	}
    			
    	if(type.equals("titlesSocialNetworks")) {
    		promptTitlesSocial = getPromptTitlesSocial();
    		if(promptTitlesSocial!=null)
    			textPrompt = promptTitlesSocial.replaceAll("%1",textFixed);
    		else
    			textPrompt = "\"Sugerir 5 títulos optimizados para búsqueda"
    			    	+ "\n"		
    					+ "Artículo: \n"
    					+ textFixed;
    	}
    	
    		
    	try {
    		
			String response = predictTextPrompt(textPrompt);
			
			String[] partes = response.split("\\n");
			
			for (String parte : partes) {
				parte = parte.replaceAll("\\*{2}", "");
				if (parte.matches("^\\d{1,3}\\.\\s.*$"))
				{
					String opcion = parte.replaceAll("^\\d{1,3}\\.\\s", "");
					listResult.add(opcion.trim());
				}
			}
						
		} catch (IOException e) {
			e.printStackTrace();
		} 
    	
    	return listResult;
    }
    
    public String predictTextPrompt( String prompt) throws IOException {
 	   
	    GoogleCredentials credentials = null;
		
	    try {
			credentials = getCredentials();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	    
	    
	    try (VertexAI vertexAi = new VertexAI(getProject(), getLocation(),null,credentials); ) {
	        
	    GenerationConfig generationConfig =
	            GenerationConfig.newBuilder()
	                .setMaxOutputTokens(Integer.parseInt(getMaxOutputTokens()))
	                .setTemperature(Float.parseFloat(getTemperature()))
	                .setTopP(Float.parseFloat(getTopP()))
	                .setTopK(Float.parseFloat(getTopK()))
	                .build();
	        GenerativeModel model = new GenerativeModel(getModel(), generationConfig, vertexAi);
	        List<SafetySetting> safetySettings = Arrays.asList(
	          SafetySetting.newBuilder()
	              .setCategory(HarmCategory.HARM_CATEGORY_HATE_SPEECH)
	              .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_ONLY_HIGH)
	              .build(),
	          SafetySetting.newBuilder()
	              .setCategory(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT)
	              .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_ONLY_HIGH)
	              .build(),
	          SafetySetting.newBuilder()
	              .setCategory(HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT)
	              .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_ONLY_HIGH)
	              .build(),
	          SafetySetting.newBuilder()
	              .setCategory(HarmCategory.HARM_CATEGORY_HARASSMENT)
	              .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_ONLY_HIGH)
	              .build()
	      );
	       
	        List<Content> contents = new ArrayList<>();
	        contents.add(Content.newBuilder().setRole("user").addParts(Part.newBuilder().setText(prompt)).build());

	        ResponseStream<GenerateContentResponse> responseStream = model.generateContentStream(contents, safetySettings);
	        // Do something with the response
	        
	        String responseText = "";
	        
	        try {
	        for (GenerateContentResponse responsePart: responseStream) {
	      	  responseText += ResponseHandler.getText(responsePart).replaceAll("```json", "").replaceAll("json", "").replaceAll("```", "").replaceAll("\\\\\"", "'");
	      	}
	        }
	        catch (java.lang.IllegalArgumentException ex) {
	        	LOG.error("Error obteniendo la respuesta de Vertex por el prompt " + prompt,ex);
	        }
	        
	        return responseText;

	    }
	  }
    
    public String extractConntent(I_CmsXmlDocument fileContent, String[] contentPaths) throws CmsXmlException {
		
    	String text = "";
    	
    	//I_CmsXmlDocument fileContent = CmsXmlContentFactory.unmarshal(this.cmsObject, file);
		Locale locale = this.cmsObject.getRequestContext().getLocale();
		
		StringBuilder sBuilder = new StringBuilder();
		for (String path : contentPaths) {
			
			if (path.contains("[x]")) {
				String value = null;
				int i=1;
				do {
					String pathNum = path;
					
					pathNum = pathNum.replace("[x]", "[" + i + "]");
					value = fileContent.getStringValue(cmsObject, pathNum, locale);
					LOG.error(pathNum + ": " + value);
					if (value!=null) {
						sBuilder.append(Jsoup.parse(value).text());
						sBuilder.append("\\n ");
					}
					i++;
				}
				while (value!=null);
				
			}
			else {
				String value = fileContent.getStringValue(this.cmsObject, path, locale);
				value = (value != null ? value : "");
				
				sBuilder.append(Jsoup.parse(value).text());
				sBuilder.append("\\n ");
			}
			
		}
		text = sBuilder.toString();
		
		return text;
	}
    
    
    public List<String> getPredict(String text, String prompt) {
    	

    	String textFixed = text.replaceAll("\"", "'"); 
    	
    	List <String> listResult = new ArrayList <String>();
    	
    	String textPrompt = prompt.replaceAll("%1",textFixed);
    	
    		
    	try {
			String response = predictTextPrompt(textPrompt);
			
			String[] partes = response.split("\\n");
			
			for (String parte : partes) {
				if (parte.matches("^\\d{1,3}\\.\\s\\*{2}.*\\*\\*$"))
				{
					String opcion = parte.replaceAll("^\\d{1,3}\\.\\s\\*{2}", "");
					opcion = opcion.replaceAll("\\*\\*$", "");
					listResult.add(opcion.trim());
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
    	
    	return listResult;
    }

    
    
    public JSONObject getSummarySuggestionAI(String newsPath) throws Exception{
    	
    	String resumen = "";
    	
    	try {
	     	String content = extractNewsContent(newsPath);
	    	String contentFixed = content.replace("$", "\\$");
	     	
	    	String prompt = getPromptResumen();
	
	    	String textPrompt = prompt.replaceAll("%1",contentFixed);
	    	String textFixed = textPrompt.replaceAll("\"", "'"); 
	    	
	    	resumen = getPredictText(textFixed);
	    	
    	}catch (java.lang.IndexOutOfBoundsException e) {
	    	resumen = "No se pudo procesar correctamente la solicitud";
	      
    	}
	    
	    JSONObject response = new JSONObject();
	    response.put("resumen",resumen); 
    	
    	return response;
    }

	public String extractNewsContent(String newsPath) throws CmsException, CmsXmlException {
		CmsFile file = getCmsFile(newsPath);
    	I_CmsXmlDocument fileContent = CmsXmlContentFactory.unmarshal(this.cmsObject, file);
    	
    	String newsContent = extractConntent(fileContent,new String[]{"cuerpo"});
    	String newsTitle = extractConntent(fileContent,new String[]{"titulo"});
    	String newsSubTitle = extractConntent(fileContent,new String[]{"copete"});
    	
    	String content = "\n Título: "+ newsTitle
				   + "\n Subtítulo: "+ newsSubTitle
				   + "\n Cuerpo: "+ newsContent;
		return content;
	}
    
	public JSONObject getSummarySuggestionAIWithText(String newsContent) throws Exception{
    	
    	String prompt = getPromptResumen();

    	String textPrompt = prompt.replaceAll("%1",Matcher.quoteReplacement(newsContent));
    	String textFixed = textPrompt.replaceAll("\"", "'"); 
    	
    	String resumen = getPredictText(textFixed); 
    			
    	JSONObject response = new JSONObject();
    	response.put("resumen",resumen); 
    	
    	return response;
    }
    
	public static void main(String [] args) throws Exception
	{
		GoogleCloudAIService AiService = new GoogleCloudAIService();
		
		AiService.client_id = "111822283461883893638";
	    AiService.client_email = "vertexia-serviceaccount@cmsmedios-1.iam.gserviceaccount.com";	
		AiService.private_key = 
			"-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCqHqSGACR/yP78\nuenkwp0W/yuuho2/tHqXu17x9vWI4zlUta2msK0mvcB8IBOoyL1d1Uukr"
			+ "3PzJcR1\nm0djxtR35nc2pWFPpQQ1xXnUOw9Xt7zp10jF2n66uzq7rm1+sGBkb6pB2jZA6ist\nO27yI28kAi+106FN1LUxs5wNvuETQDgOPHssHP2vmY77fiykzEEUEItj85H5j2BZ\nwUfrVuxEkg1qdcNEGDvhIldpPfEFwV0GmD7V+5hkkNWbPR3p"
			+ "TSftpAsHwfDsDh4N\nFPDBTqVOiUOaiDitjFCwKybQrdDx6GIskHyuXAIUiT6VqHJDWU0vvj+XfTZR1X9b\nuQab0kxxAgMBAAECggEAUpNgMek0zlA+sWb+6PssbGK9DdCjUpgk36zkN9gzs9fz\n1rH/Uge1bLYzjiy1zjpubPMhbhV6V16QP7P5Ua3"
			+ "MKU0VtdFN7G6BniY2bjWiy2XV\nDb7hxKQ72qESL9LJtHKhv27SeneU2SqxfZm5T1Cy3IwxI3XP+OsUbsUrrSJnt0Ms\nJnmNwFB4uksIc/RXzYZkG0CXXRAULeCYIeyc1fz6GOlii1mfSXGbDsRJY1r4mILy\nzUt7POcRBLl1opDbFsh8uOQmeFsnoC"
			+ "o2F7ZIhhxDFCRmjFLNCSdED13jw8MMRqH7\nCpHefoCmq/GFd841pjgA7G0NXRh23EcqsfYJeVS2VwKBgQDhBj1q95RA4qLnB5r8\nk7ZBkI9eVp+IH+8Z4gkrysBDPIXWtymeOyI//lkcvVhvam6vAPgxaajFU3JD6n6r\nyWi3BFACFrQ45TDHBsuJe"
			+ "xdupUUCDsv8GTY5DoTbBIvEItpk2gNDiwMEDeoCw/En\n3V8zW46MUWl0dV1JGEGii9yyOwKBgQDBiZYI6UsKr3CDDCHELibO3bC1APCUIxWo\nnRfncbQudlqG/+ANA1IM3OeXPl9aOAaTtHsfZNhU4JDJe5xrjYFjuQRGMcMCy/R0\nO7TWoIeGbH8v"
			+ "hqFkQe2u/nb45l1yl0apd9YD9jyaigCbpBsbYch0j1Qzc5xbuLsY\nGthg8vWFQwKBgAX+tpAs/GQmJZTmHPjxrn9crJUk1ac4wpP786aZBjpEWIR56L/w\nb18pFZKwcLS3Ly+2ZOZcHET1jivFCY7mFMYbdU448KA7autCgRWuk9c8y/PcFQ8o\nVMt"
			+ "8WYnEOz0EeLELJd8cyv3GqR9dvQUE3s5teATA/oA9yDh0TvtXoo+VAoGAVCJF\nsPYKA0klNjLt8uTFRiSljuorq5pc7gHsxFZvp8sXq5xMgBuzbZgSrXMV7Jhyc3+A\nDrO+P5eoGY68/xxy5TEnJIS5a9vSFz1lbfA3Oj6sQ7LZMkZe1zYT7jvm38vX"
			+ "khBg\nXzSKP/xCK2b5ybuGcvVj+0IE9PKnkuEIs4yrIxUCgYEAke4bRkH+Ae7vUwZtIT8Y\nlWrqQpVenfyn4K6Eauk8jXRdRscYXTlACJ6XtvYzRGqYttB6HTcdFIy+A7J2cD/Z\nkRDSD/SDSWbRLDPq1+nde0wVyloleNegmiv6gcFhLqWqAksh9QJ"
			+ "+9T+3a6TYUaS/\n10WmAF0gDqXZbYdGWj3tc7g=\n-----END PRIVATE KEY-----\n";
	
		AiService.private_key_id ="bb0cc56e7d5a73e37fc87475aa86e4f50901f3cb";
	    AiService.project ="cmsmedios-1";
	    AiService.location ="us-central1";
	    AiService.publisher ="google";
	    AiService.model ="gemini-1.0-pro-001";
	    AiService.maxOutputTokens="8192";
	    AiService.temperature="0.5";
	    AiService.topP ="0.4";
	    AiService.topK = "10";
	    
	    
	    //AiService.promptResumen = "Sugerir 5 títulos alternativos con mayor impacto para el artículo: %1";
	    //AiService.promptResumen = "Resumir el contenido del artículo en el que queden sus ideas principales, las personas y lugares involucrados y las citas textuales. El resultado esparado solo contiene las oraciones del resumen sin agregados adicionales ni enumeracion de los mismos. El artículo es el siguiente: %1.";
	
	    AiService.promptResumen = "Resumir el contenido del artículo en el que queden sus ideas principales. Inidcar las personas y lugares involucrados. Incluir las citas textuales. El resultado esparado solo contiene las oraciones del resumen sin agregados adicionales ni enumeracion de los mismos. El artículo es el siguiente: %1.";
	
	    /*
	    String newsTitle = "EE. UU. e Israel discrepan sobre la reforma judicial";
	    String newsSubTitle = "Washington expresa preocupación por las implicaciones para la democracia";
	    String newsContent = "El gobierno de Estados Unidos ha expresado su preocupación por la propuesta de reforma judicial del gobierno israelí, que ha generado protestas masivas en todo el país. En una llamada telefónica con el primer ministro israelí, Benjamin Netanyahu, el secretario de Estado estadounidense, Antony Blinken, enfatizó la importancia de mantener instituciones democráticas sólidas, incluido un poder judicial independiente. Blinken también expresó su preocupación por las implicaciones de la reforma para la protección de los derechos de las minorías y el Estado de derecho. El gobierno israelí ha defendido la reforma, argumentando que es necesaria para restaurar el equilibrio entre las ramas del gobierno y abordar la percepción de parcialidad judicial. Sin embargo, los críticos sostienen que la reforma debilitaría la independencia del poder judicial y permitiría al gobierno ejercer un mayor control sobre los tribunales. La discrepancia entre Estados Unidos e Israel sobre la reforma judicial ha generado tensiones en la relación bilateral. Estados Unidos ha sido un firme partidario de Israel durante décadas, pero la administración Biden ha expresado su preocupación por la erosión de las instituciones democráticas en el país. Se espera que la reforma judicial sea debatida en el Parlamento israelí en las próximas semanas. El resultado de la votación tendrá un impacto significativo en la relación entre Estados Unidos e Israel, así como en el futuro de la democracia israelí. masivas en Israel y ha suscitado preocupaciones entre los aliados internacionales del país.";
	    */
	    
	    String newsTitle = "El Gobierno estimó que el paro le costará más de USD 500 millones a la economía";
	    String newsSubTitle = "Según cálculos oficiales que coinciden con números privados, ese será el impacto de la huelga general de la CGT sobre la actividad. Representaría un 1,1% del PBI de mayo o un cuarto de lo que se produciría en un día normal";
	    String newsContent = "El paro nacional que realiza la CGT este jueves le costará a la actividad económica más de 500 millones de dólares, según estimaciones que hizo el Gobierno nacional y también desde el sector privado. Para eso se estimó cuál sería un nivel razonable de adhesión a la huelga general en los distintos sectores productivos de la economía. “El cálculo tomando el PIB mensualizado estimado a hoy y haciendo supuestos de adhesión que consideramos razonables daría unos USD 520 millones diarios de costo”, aseguraron desde el equipo económico. Un cálculo hecho por el economista Fausto Spotorno junto con la UADE puso ese número en torno de los USD 544 millones. “Según la estimación preliminar del Instituto de Economía de UADE, el costo económico que implica el paro general este 9 de mayo de 2024 sería de $489.272 millones o USD 544 millones. Esta cifra equivale al 1,1% del PIB de mayo o el 24,3% de lo que se hubiera producido en el día. Este cálculo asume que no todos los sectores y regiones perderán por igual durante el paro, y que incluso, el 20,1% de lo inicialmente perdido se recupera dentro del mes”, explicaron desde ese centro de estudios. “Para el cálculo de este impacto se estimó el efecto en cada uno de los sectores económicos basado en eventos similares en el pasado. Algunos sectores prácticamente no sufren ningún impacto y otros lo recuperan rápidamente. Sin embargo, hay otros sectores y empresas que sufren pérdidas irrecuperables y otros que, si bien pueden recuperar gran parte de lo perdido, lo harán en un plazo mucho más largo. Por eso, es que este cálculo se hizo de sector a sector”, mencionaron desde el Instituto de Economía de la UADE. “En esta estimación sólo se tomaron las pérdidas directas, netas de las recuperaciones que se darán dentro del mes. Así, por ejemplo, el comercio recuperará un 35% y restaurantes el 0% de lo que no se venda. En este sentido, los sectores que explican la mayor parte del impacto negativo, es el sector de industria manufacturera y el de la construcción. Ambos sectores, perderán producción que será difícil de recuperar o que se lo hará con mayores costos”, concluyeron. El mayor impacto se produciría sobre los sectores productores de servicios, que de los 544 millones de dólares que estimó la UADE, absorberían unos 277 millones de dólares. Enseñanza, servicios de salud y servicios sociales representarían unos 90 millones de dólares, unos $81.000 millones. Le siguen el comercio con 56 millones de dólares, intermediación financiera con 43 millones de dólares, entre otros. Dentro de los productores de bienes, el impacto total sería de 175 millones de dólares, concentrado mayormente en la industria manufacturera que se llevaría la peor parte, con unos 110 millones de dólares, casi 100.000 millones de pesos. La construcción resignaría, por su lado, unos 50 millones de dólares. Las principales cámaras empresarias salieron a rechazar la huelga general. La Cámara Argentina de Comercio y Servicios (CAC), dijo que “la medida anunciada resulta injustificada y por demás inoportuna”. “Resulta llamativo que a cinco meses de la asunción del actual Gobierno Nacional ya se lleve a cabo un segundo paro general, cuando durante los cuatro años del anterior período presidencial –mientras muchos de los males señalados se incubaban– no hubo siquiera una sola protesta de estas características. Es dable destacar que el panorama económico y social de entonces lejos estaba de ser idílico: el aumento de la pobreza y la cuadruplicación de la tasa de inflación entre 2019 y 2023 son tan solo dos muestras de ello”, indicaron. Por su parte, la Confederación Argentina de la Mediana Empresa (CAME), que representa a pequeñas y medianas empresas industriales y comerciales, aseguró que “la caída de la demanda, que repercute seriamente en el comercio y la producción industrial pyme, se vería aún más agravada en un escenario de fábricas y locales cerrados durante toda la jornada, lo que generaría cuantiosas pérdidas económicas adicionales para el sector”. “CAME es consciente de las dificultades que atraviesa el país. Por eso mismo sostiene que las pymes necesitan trabajar para hacer frente a sus obligaciones financieras y mantenerse como las principales generadoras de empleo registrado en Argentina”, concluyeron.";
	    
	    int longitudNoticia = newsTitle.length() + newsSubTitle.length() + newsContent.length();
	    
	    //newsContent += " Recuerde que puede suscribirse al diario por una cuota mensual simple. Erre con erre guitarra. erre con erre barril. que rapido ruedan las ruedas del ferrocarril. me gusta la comida picante. pero me da acidez. entonces... uso pastillas corrugadas. compre y siga comiendo como yo. ijfsdjkfvrngvnernkvnjefnbfd. df wnfijwf.";
	    String content = "\n Título: "+ newsTitle
				   + "\n Subtítulo: "+ newsSubTitle
				   + "\n Cuerpo: "+ newsContent; // + " " + newsContent;
		
	    //System.out.println(content);
	    
	    JSONObject json = AiService.getSummarySuggestionAIWithText(content);
	    
	    //System.out.println(json.get("resumen").toString());
	    
	    int longitudResumen = json.get("resumen").toString().length();
	    
	    //System.out.println("longitudResumen: " + longitudResumen);
	    
		System.out.println(json);
		
		System.out.println("-----------------");
		System.out.println("Estimado de contenido principal : " + ((float)longitudResumen*1.3/(float)longitudNoticia)*100 + "%");
		
		
	}

		public void setTemperature(String temperature) {
			this.temperature = temperature;
		}
		
		public void setMaxOutputTokens(String maxOutputTokens) {
			this.maxOutputTokens = maxOutputTokens;
		}
		
		public void setTopP(String topP) {
			this.topP = topP;
		}
		
		public void setTopK(String topK) {
			this.topK = topK;
		}
		
		public void setPromptResumen(String promptResumen) {
			this.promptResumen = promptResumen;
		}
    
}