package com.tfsla.diario.ediciones.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentialsUtils;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.api.HarmCategory;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.api.SafetyRating;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.google.cloud.vertexai.api.SafetySetting;
import com.google.cloud.vertexai.generativeai.ResponseStream;
import com.google.gson.JsonObject;

public class VertexIaNewsService2 {

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
	
	protected String modelName = null;
	protected String project = null;
	protected String location = null;
	
	protected String prompt = null;
	protected String basePrompt = null;
	protected String replacePrompt = null;
	protected String custom1Prompt = null;
	protected String custom2Prompt = null;
	protected String custom3Prompt = null;
	protected String custom4Prompt = null;
	
	private static final Log LOG = CmsLog.getLog(VertexIaNewsService2.class);

	public VertexIaNewsService2(String siteName, String publication) {
		this.siteName = siteName;
		this.publication = publication;
	}
	
	public VertexIaNewsService2() {}
	
	public JsonObject generateNews(String prompt) throws IOException {
        GoogleCredentials credentials = null;
		
	    try {
			credentials = getCredentials();
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}

		
    try (VertexAI vertexAi = new VertexAI(getProject(), getLocation(),null,credentials); ) {
    	
    
      GenerationConfig generationConfig =
          GenerationConfig.newBuilder()
              .setMaxOutputTokens(Integer.parseInt(getMaxOutputTokens()))
              .setTemperature(Float.parseFloat(getTemperature()))
              .setTopP(Float.parseFloat(getTopP()))
              .build();
      GenerativeModel model = new GenerativeModel(getModel(), generationConfig, vertexAi);
      
      List<SafetySetting> safetySettings = setSafetySettings();
 
      //String prompt = "Escribe un noticia en español latinoamericano según el contenido de la siguiente noticia: https://www.tv7israelnews.com/us-israel-tiff-over-judicial-reform/. La noticia debe tener el formato {\"titulo\":\"\", \"bajada\":\"\" y \"cuerpo\":\"\"} en formato json válido con solo esos campos";
     String fullPrompt = prompt + " \n" + getBasePrompt();
     
     if (getCustom1Prompt() != null)
    	 fullPrompt += " "+getCustom1Prompt() + " \n" ;
     
     if (getCustom2Prompt() != null)
    	 fullPrompt += " "+getCustom2Prompt() + " \n" ;
     
     if (getCustom3Prompt() != null)
    	 fullPrompt += " "+getCustom3Prompt() + " \n" ;
     
     if (getCustom4Prompt() != null)
    	 fullPrompt += " "+getCustom4Prompt() + "." ;
     
     LOG.debug(fullPrompt);
     
      List<Content> contents = new ArrayList<>();
      contents.add(Content.newBuilder().setRole("user").addParts(Part.newBuilder().setText(fullPrompt)).build());

      ResponseStream<GenerateContentResponse> responseStream = model.generateContentStream(contents, safetySettings);
      // Do something with the response
      
      JsonObject jsonObject = processGeminiResponse(responseStream);

      //System.out.print(responseText);
      //JsonObject jsonObject = JsonParser.parseString(responseText).getAsJsonObject();
      //System.out.print(jsonObject);
      
      return jsonObject;
    }  
     
	}
	
	private JsonObject processGeminiResponse(Iterable<GenerateContentResponse> responseStream) {
		
	    JsonObject jsonObject = new JsonObject();
	    StringBuilder responseTextBuilder = new StringBuilder(); // Usar StringBuilder para eficiencia

	    // *** PASO CLAVE: Verificar la seguridad ANTES de procesar el texto ***
	    boolean isBlockedForSafety = false;
	    String safetyReason = "";
	    
	    // Iteramos sobre las partes de la respuesta para verificar el feedback de seguridad
	    for (GenerateContentResponse responsePart : responseStream) {
	        if (responsePart.hasPromptFeedback() && !responsePart.getPromptFeedback().getSafetyRatingsList().isEmpty()) {
	            List<SafetyRating> safetyRatings = responsePart.getPromptFeedback().getSafetyRatingsList();
	            for (SafetyRating rating : safetyRatings) {
	                if (rating.getProbability() == SafetyRating.HarmProbability.HARM_PROBABILITY_UNSPECIFIED ||
	                    rating.getProbability() == SafetyRating.HarmProbability.HIGH ||
						rating.getProbability() == SafetyRating.HarmProbability.UNRECOGNIZED ||
					//	rating.getProbability() == SafetyRating.HarmProbability.LOW ||
	                    rating.getProbability() == SafetyRating.HarmProbability.MEDIUM) { // O ajusta según tu umbral
	                    
	                    isBlockedForSafety = true;
	                    LOG.debug("IA bloqueo la peticion por seguridad." + rating.getProbability().toString());
	                    safetyReason = "Blocked due to safety reason for category: " + rating.getProbability().name();
	                    break; // Salir una vez que se detecta el bloqueo
	                }
	            }
	        }
	        
	        if (!responsePart.getCandidatesList().isEmpty()) {
	            Content candidateContent = responsePart.getCandidates(0).getContent();
	            if (candidateContent != null && !candidateContent.getPartsList().isEmpty()) {
	                if (candidateContent.getParts(0).hasText()) {
	                    responseTextBuilder.append(candidateContent.getParts(0).getText());
	                }
	            } else {
	            	isBlockedForSafety = true;
	    	        jsonObject.addProperty("error", safetyReason); // Puedes usar el safetyReason detectado
	    	        jsonObject.addProperty("errorCode", "008.021");
	    	        LOG.debug("Gemini response blocked for safety: " + safetyReason ); // Log para depuración

	            }
	        }

	    }

	    if (isBlockedForSafety) {
	        jsonObject.addProperty("error", safetyReason); // Puedes usar el safetyReason detectado
	        jsonObject.addProperty("errorCode", "008.021");
	        LOG.debug("Gemini response blocked for safety: " + safetyReason); // Log para depuración
	        LOG.debug("jsonObject"); // Log para depuración
	        return jsonObject;
	    }

	    // Si no está bloqueado, entonces procesamos el texto como lo hacías antes
	    String responseText = responseTextBuilder.toString()
	                                            .replaceAll("```json", "")
	                                            .replaceAll("```", "");

	    String[] parte = responseText.split("\\n");
	    
	    int campo = 0;
	    String cuerpo = "";
	    for (String p : parte) {
	        LOG.debug(p); // Mantener tu log
	        
	        // Aquí tu lógica de parsing normal, ya que sabemos que la respuesta no está bloqueada
	        p = p.replaceFirst("\\**[tT][ií]tulo:\\**\\s", "")
	             .replaceFirst("\\**[Bb]ajada:\\**\\s", "")
	             .replaceFirst("\\**[Cc]uerpo:\\**[\\s\\n]", "");
	        
	        if (campo < 2 && p.replaceAll("\\n", "").trim().isEmpty()) {
	            continue;
	        }
	        
	        if (campo == 0) {
	            jsonObject.addProperty("titulo", p.replaceAll("\\*\\*", "").trim());
	        } else if (campo == 1) {
	            jsonObject.addProperty("bajada", p.replaceAll("\\*\\*", "").trim());
	        } else {
	            p = p.replaceAll("<br>", "").replaceAll("</br>", "").replaceAll("<br/>", "");
	            if (p.trim().length() > 0) {
	                cuerpo += "<p>" + p + "</p>\n";
	            }
	        }
	        campo++;
	    }
	    
	    jsonObject.addProperty("cuerpo", cuerpo.replaceAll("\\*\\*", "").replaceFirst("<p></p>\\n", "").trim());
	    return jsonObject;
	}
	
	private JsonObject extractedNews(ResponseStream<GenerateContentResponse> responseStream) {
		String responseText = "";
		  for (GenerateContentResponse responsePart: responseStream) {
			  responseText += ResponseHandler.getText(responsePart).replaceAll("```json", "").replaceAll("```", "");
			}
		
		  String[] parte = responseText.split("\\n");
		  
		  JsonObject jsonObject = new JsonObject();
		  
		  int campo = 0;
		  String cuerpo="";
		  for (String p : parte) {
			  LOG.debug(p);
			  if (p.equals("The response is blocked due to safety reason")){
				  jsonObject.addProperty("error","The response is blocked due to safety reason");
			  		jsonObject.addProperty("errorCode","000.999");
			  }else {
				  p = p.replaceFirst("\\**[tT][ií]tulo:\\**\\s", "").replaceFirst("\\**[Bb]ajada:\\**\\s", "").replaceFirst("\\**[Cc]uerpo:\\**[\\s\\n]", "");
				  
				  if (campo<2 && p.replaceAll("\\n", "").trim().length()==0)
					  continue;
				  
				  if (campo==0)
					  jsonObject.addProperty("titulo",  p.replaceAll("\\*\\*", "").trim());
				  else if (campo==1)
					  jsonObject.addProperty("bajada",  p.replaceAll("\\*\\*", "").trim());
				  else {
					  p = p.replaceAll("<br>", "").replaceAll("</br>", "").replaceAll("<br/>", "");
					  if (p.trim().length()>0)
						  cuerpo += "<p>" + p + "</p>\n";
				  }
				  campo++;
			  }
		  }
		  
		  jsonObject.addProperty("cuerpo",  cuerpo.replaceAll("\\*\\*", "").replaceFirst("<p></p>\\n", "").trim());
		return jsonObject;
	}
	
	public JsonObject generateNews(String url, String aditionalPrompt) throws IOException {
        GoogleCredentials credentials = null;
		
	    try {
			credentials = getCredentials();
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}

		
    try (VertexAI vertexAi = new VertexAI(getProject(), getLocation(),null,credentials); ) {
    	
    
      GenerationConfig generationConfig =
          GenerationConfig.newBuilder()
              .setMaxOutputTokens(Integer.parseInt(getMaxOutputTokens()))
              .setTemperature(Float.parseFloat(getTemperature()))
              .setTopP(Float.parseFloat(getTopP()))
              .build();
      GenerativeModel model = new GenerativeModel(getModel(), generationConfig, vertexAi);
      
      List<SafetySetting> safetySettings = setSafetySettings();
 
      //String prompt = "Escribe un noticia en español latinoamericano según el contenido de la siguiente noticia: https://www.tv7israelnews.com/us-israel-tiff-over-judicial-reform/. La noticia debe tener el formato {\"titulo\":\"\", \"bajada\":\"\" y \"cuerpo\":\"\"} en formato json válido con solo esos campos";
     String fullPrompt = getPrompt().replaceAll("\\$URL\\$",url) + " \n" + aditionalPrompt;
      
     //System.out.print(fullPrompt);
     
      List<Content> contents = new ArrayList<>();
      contents.add(Content.newBuilder().setRole("user").addParts(Part.newBuilder().setText(fullPrompt)).build());

      ResponseStream<GenerateContentResponse> responseStream = model.generateContentStream(contents, safetySettings);
      // Do something with the response
      
      JsonObject jsonObject = processGeminiResponse(responseStream);

      //System.out.print(responseText);
      //JsonObject jsonObject = JsonParser.parseString(responseText).getAsJsonObject();
     // System.out.print(jsonObject);
      
      return jsonObject;
    }  
     
	}

	private List<SafetySetting> setSafetySettings() {
		List<SafetySetting> safetySettings = Arrays.asList(
		    SafetySetting.newBuilder()
		        .setCategory(HarmCategory.HARM_CATEGORY_HATE_SPEECH)
		        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE)
		        .build(),
		    SafetySetting.newBuilder()
		        .setCategory(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT)
		        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE)
		        .build(),
		    SafetySetting.newBuilder()
		        .setCategory(HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT)
		        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE)
		        .build(),
		    SafetySetting.newBuilder()
		        .setCategory(HarmCategory.HARM_CATEGORY_HARASSMENT)
		        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE)
		        .build()
		);
		return safetySettings;
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
    
    protected String getModuleName() {
		return "VertexIaNews";
	}
	
	
	public boolean isAIEnabled() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(siteName, publication, getModuleName(), "AIEnabled", false);
	}
	
	public String getReplacePromptParagraph() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "paragraphPromptReplace", "");
	} 


	public String getReplacePromptLanguage() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "languagePromptReplace", "");
	} 
	
	public String getReplacePromptTone() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "tonePromptReplace", "");
	} 

	public String getReplacePromptTranscribe() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "transcribePromptReplace", "");
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
	
	protected String getPrompt() {
		return (prompt!=null ? prompt : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "defaultPrompt", ""));
	}
	
	protected String getBasePrompt() {
		return (basePrompt!=null ? basePrompt : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "basePrompt", ""));
	} 
	

	protected String getCustom1Prompt() {
		return (custom1Prompt!=null ? custom1Prompt : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "custom1Prompt", ""));
	} 
	
	protected String getCustom2Prompt() {
		return (custom2Prompt!=null ? custom2Prompt : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "custom2Prompt", ""));
	} 
	
	protected String getCustom3Prompt() {
		return (custom3Prompt!=null ? custom3Prompt : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "custom3Prompt", ""));
	} 
	
	protected String getCustom4Prompt() {
		return (custom4Prompt!=null ? custom4Prompt : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "custom4Prompt", ""));
	} 
	
	protected String getModel() {
		return (modelName = 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "model", ""));
	}

}