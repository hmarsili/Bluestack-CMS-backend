package com.tfsla.diario.ediciones.utils;

import java.util.Map;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;

import software.amazon.awssdk.services.rekognition.model.Label;
import software.amazon.awssdk.services.rekognition.model.ModerationLabel;
import com.tfsla.diario.ediciones.services.GoogleCloudTranslatorService;

public class ImageLabelsTranslator {
	
	private static final Log LOG = CmsLog.getLog(ImageLabelsTranslator.class);
	public static ImageLabelsTranslator getInstance() {
		return instance;
	}
	
	private static ImageLabelsTranslator instance = new ImageLabelsTranslator();
	
	private Map<String, Map<String,String>> dictionaries=null;
	
	private ImageLabelsTranslator(){
		dictionaries = new HashMap<String,Map<String,String>>();
	}
	

	
	public String getStoredLabelTranslation(String label, String language) {
		Map<String,String> table = dictionaries.get(language);
		
		if (table==null)
			return null;
		
		return table.get(label);
		
	}
	
	public void addLabelTranslation(String label, String language, String meaning){
		Map<String,String> table = dictionaries.get(language);
		
		if (table==null)
		{
			table = new HashMap<String,String>();
			dictionaries.put(language, table);
		}
		
		table.put(label, meaning);
		
	}

	public String getUnsafeLabel(CmsObject cmsObject, List<ModerationLabel> labels) throws IOException {
		GoogleCloudTranslatorService translateService = GoogleCloudTranslatorService.getInstance(cmsObject);
		boolean mustTranslate = translateService.isTranslatorEnabled() && !translateService.getLanguage().equals("en");

		String result = "";

		if (!mustTranslate) {
			for (ModerationLabel label: labels) {  
					if (result.length() >0) result+= ", ";
					result+=label.name();
			}
			return result;
		}
		
		//Me fijo si tengo que traducir algo
		String translatedText = "";
		String toTranslateText = "";
		String targetLang = translateService.getLanguage();
		for (ModerationLabel label: labels) {  				
			String meaning = getStoredLabelTranslation(label.name(), targetLang);
			LOG.debug("Etiqueta detectada: " + label.name() + ", buscando en diccionario (" + targetLang +  ") : " + meaning);
			if (meaning==null) {
				if (toTranslateText.length() >0) toTranslateText+= ". ";
				toTranslateText+=label.name();
			}
		                  
		}
				
		//Si tengo que traducir traduzco
		if (toTranslateText.length() >0)
			translatedText = GoogleCloudTranslatorService.getInstance(cmsObject).defaultTranslate(toTranslateText,"en");
			
		LOG.debug("A traducir: " + toTranslateText);
		LOG.debug("traducido: " + translatedText);
		
		String[] newTerms = translatedText.toLowerCase().split("\\. ");
		int newTermIdx = 0;
		for (ModerationLabel label: labels) {  
			if (label.confidence()>70F) {
				if (result.length() >0) result+= ", ";
				
				String meaning = getStoredLabelTranslation(label.name(), targetLang);
				
				LOG.debug( label.name() + " - " + meaning);
				if (meaning==null) { 
					LOG.debug( "Agregando significado " + newTerms[newTermIdx]);
					addLabelTranslation(label.name(), targetLang, newTerms[newTermIdx]);	
					result+=newTerms[newTermIdx];
					newTermIdx++;
				}
				else {
					result+=meaning;
				}
			}                  
		}
		
		
		return result;
		

	}
	
	public String getLabels(CmsObject cmsObject, List <Label> labels) throws IOException {
		
		GoogleCloudTranslatorService translateService = GoogleCloudTranslatorService.getInstance(cmsObject);
		boolean mustTranslate = translateService.isTranslatorEnabled() && !translateService.getLanguage().equals("en");

		String result = "";

		if (!mustTranslate) {
			for (Label label: labels) {  
				if (label.confidence()>70F) {
					if (result.length() >0) result+= ", ";
					result+=label.name();
				}                  
			}
			return result;
		}

		//Me fijo si tengo que traducir algo
		String translatedText = "";
		String toTranslateText = "";
		String targetLang = translateService.getLanguage();
		for (Label label: labels) {  
			if (label.confidence()>70F) {
				
				String meaning = getStoredLabelTranslation(label.name(), targetLang);
				LOG.debug("Etiqueta detectada: " + label.name() + ", buscando en diccionario (" + targetLang +  ") : " + meaning);
				if (meaning==null) {
					if (toTranslateText.length() >0) toTranslateText+= ". ";
					toTranslateText+=label.name();
				}
			}                  
		}
		
		//Si tengo que traducir traduzco
		if (toTranslateText.length() >0)
			translatedText = GoogleCloudTranslatorService.getInstance(cmsObject).defaultTranslate(toTranslateText,"en");
		
		LOG.debug("A traducir: " + toTranslateText);
		LOG.debug("traducido: " + translatedText);
		
		String[] newTerms = translatedText.toLowerCase().split("\\. ");
		int newTermIdx = 0;
		for (Label label: labels) {  
			if (label.confidence()>70F) {
				if (result.length() >0) result+= ", ";
				
				String meaning = getStoredLabelTranslation(label.name(), targetLang);
				
				LOG.debug( label.name() + " - " + meaning);
				if (meaning==null) { 
					LOG.debug( "Agregando significado " + newTerms[newTermIdx]);
					addLabelTranslation(label.name(), targetLang, newTerms[newTermIdx]);	
					result+=newTerms[newTermIdx];
					newTermIdx++;
				}
				else {
					result+=meaning;
				}
			}                  
		}
		
		
		return result;
	}
	
}
