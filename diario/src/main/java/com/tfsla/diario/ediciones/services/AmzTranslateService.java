package com.tfsla.diario.ediciones.services;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.google.gson.JsonObject;
import com.tfsla.diario.ediciones.model.TipoEdicion;

import net.sf.json.JSONObject;

public class AmzTranslateService {

	protected CmsObject cmsObject = null;
	protected String siteName;
	protected String publication;

	private static final Log LOG = CmsLog.getLog(AmzTranslateService.class);

	private static Map<String, AmzTranslateService> instances = new HashMap<String, AmzTranslateService>();
	
	protected String getModuleName() {
		return "translate";
	}

	public boolean isAmzTranslateEnabled() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(siteName, publication, getModuleName(), "AmzTranslateEnabled", false);
	}
	
	public String getTranslateEndpoint() {
		return (urltranslateEndpoint!=null ? urltranslateEndpoint : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "translateEndpoint", ""));
	}
	
	public String getRewriteContext() {
		return (rewriteContext!=null ? rewriteContext : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "rewriteContext", ""));
	}
	
	public String urltranslateEndpoint;
	public String rewriteContext;
	
	public static AmzTranslateService getInstance(CmsObject cms) {
    	String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	String publication = "0";
    	try {
			publication = String.valueOf(PublicationService.getCurrentPublicationWithoutSettings(cms).getId());
		} catch (Exception e) {
			LOG.error(e);
		}
    	    	
    	String id = siteName + "||" + publication;
    	
    	AmzTranslateService instance = instances.get(id);
    	
    	if (instance == null) {
	    	instance = new AmzTranslateService(cms,siteName, publication);

	    	instances.put(id, instance);
    	}
    	
    	instance.cmsObject = cms;
    	
    	
        return instance;
    }

	public AmzTranslateService() {}
	
	public AmzTranslateService(CmsObject cmsObject, String siteName, String publication) {
		this.siteName = siteName;
		this.publication = publication;
		this.cmsObject = cmsObject;
	}
	
	public String rewriteNews(String newsPath, TipoEdicion pubDestino, String prompt) throws Exception {
		TipoEdicionBaseService tService = new TipoEdicionBaseService();
		TipoEdicion tEdicion = null;
		try {
			tEdicion = tService.obtenerTipoEdicion(cmsObject, newsPath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("Error al intentar obtener la publicacion de la noticia " + newsPath,e);
			throw new Exception("Error al intentar obtener la publicacion de la noticia " + newsPath,e); 
			
		}
		
		if (!tEdicion.getLanguage().equals(pubDestino.getLanguage())) {
			LOG.error("No se puede reescribir una noticia en un idioma diferente: " + newsPath);
			throw new Exception("No se puede reescribir una noticia en un idioma diferente: " + newsPath); 
		}
		
		
		CmsFile file;
		I_CmsXmlDocument fileContent;
		try {
			file = getCmsFile(newsPath);
			fileContent = CmsXmlContentFactory.unmarshal(this.cmsObject, file);
		} catch (CmsException e) {
			LOG.error("Error al obtener la noticia " + newsPath,e);
			throw new Exception("Error al obtener la noticia " + newsPath,e); 
			
		}
		
		JSONObject content = new JSONObject();
    	try {
			extractFieldsToTranslateFromNews(newsPath, content, fileContent);
		} catch (CmsXmlException e) {
			LOG.error("Error al obtener el contenido de la noticia " + newsPath,e);
			throw new Exception("Error al obtener el contenido de la noticia " + newsPath,e); 
		}
    	
    	//LOG.error("imprimiendo contenido extraido de la noticia");
    	//LOG.error(content);
    	
    	String context = getRewriteContext();
    	String newsTitle = content.getString("titulo");
		String newsSubTitleFixed = content.getString("volanta");
		String cuerpo = content.getString("cuerpo");
	
		//LOG.error("cuerpo:" + cuerpo);
		String newsContentFixed = "";
		HashMap<Integer,String> extraContent = new HashMap<>();
		Document body = Jsoup.parse(cuerpo);
		int nroParagraph = 0;
		
		for(Element bodyPart : body.select("body").get(0).children()) {
			//LOG.error("nroParagraph: " + nroParagraph);
			//LOG.error(bodyPart.outerHtml());
			//LOG.error("bodyPart.tagName() --> " + bodyPart.tagName());
			if (bodyPart.tagName().equals("p")) {
				newsContentFixed += bodyPart.text() + "\n ";
			}
			else {
				extraContent.put(nroParagraph, bodyPart.outerHtml());
			}
			nroParagraph++;
			
			//LOG.error("----------------------------------------");
		}
		
		String fullPrompt = context + "\n" + prompt;
		fullPrompt += "\n Título: "+ newsTitle
				   + "\n Subtítulo: "+ newsSubTitleFixed
				   + "\n Cuerpo: "+ newsContentFixed;
		
		//LOG.error(fullPrompt);
		
		VertexIaNewsService aiService = new VertexIaNewsService(cmsObject.getRequestContext().getSiteRoot(), "" + tEdicion.getId());
    	try {
    		JsonObject responseGenerated = aiService.generateNews(fullPrompt);
    		
    		//LOG.error(responseGenerated);
    		
    		content.put("titulo",responseGenerated.get("titulo").getAsString());
    		content.put("volanta",responseGenerated.get("bajada").getAsString());
    		
    		
    		String newsContentFixedRewrited = "";
    		body = Jsoup.parse(responseGenerated.get("cuerpo").getAsString());
    		
    		//LOG.error(body);
    		
    		int oldNroParagraph = nroParagraph;
    		Element bodyContent = body.select("body").get(0);
    		int newNroParagraph = bodyContent.children().size();
    		
    	
    		int i=0;
    		int j=0;
    		
    		while (i<=oldNroParagraph || j<newNroParagraph) {
    			String extra = extraContent.get(i);
    			//LOG.error("Extra content:");
    			//LOG.error(extraContent.get(i));
    			if (extra!=null) {
    				newsContentFixedRewrited += extra  + "\n ";
    			}
    			else if (j<newNroParagraph) {
    				newsContentFixedRewrited += bodyContent.children().get(j).outerHtml()  + "\n ";
    				j++;
    			}	
    			i++;
    		}
    		
    		//LOG.error(newsContentFixedRewrited);
    		content.put("cuerpo",newsContentFixedRewrited);
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("Error al intentar la reescritura de la noticia " + newsPath,e);
			throw new Exception("Error al intentar la reescritura de la noticia " + newsPath,e); 
		}
    	
    	//JSONObject jsonResponse = translateContent(newsPath, pubDestino, tEdicion, content);
		
		String path="cuerpo";
		String attrName = "alt";
		replaceAttributes(content, path, attrName);
		attrName = "title";
		replaceAttributes(content, path, attrName);
    	
    	LOG.debug(content);
		
		String newsType;
		try {
			newsType = this.cmsObject.readPropertyObject(file, "newsType", true).getValue();
		} catch (CmsException e) {
			LOG.error("Error al obtener el tipo de noticia de " + newsPath,e);
			throw new Exception("Error al obtener el tipo de noticia de " + newsPath,e);
		}
		
		if (newsType==null) {
			LOG.error("Error al obtener el tipo de noticia de " + newsPath);
			throw new Exception("Error al obtener el tipo de noticia de " + newsPath);
		}
		
		String fileNameDest = fillDestNews(file, fileContent, content, pubDestino, newsType, "rewrite");
		
		
		return fileNameDest;

	}
	
	public String rewriteNews(String newsPath, String prompt) throws Exception {
		
		TipoEdicionBaseService tService = new TipoEdicionBaseService();
		TipoEdicion tEdicion = null;
		try {
			tEdicion = tService.obtenerTipoEdicion(cmsObject, newsPath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("Error al intentar obtener la publicacion de la noticia " + newsPath,e);
			throw new Exception("Error al intentar obtener la publicacion de la noticia " + newsPath, e);
			
		}
		
		return rewriteNews(newsPath, tEdicion, prompt);

	}
	
	public String rewriteAndTranslateNews(String newsPath, TipoEdicion pubDestino, String prompt) throws Exception {
		TipoEdicionBaseService tService = new TipoEdicionBaseService();
		TipoEdicion tEdicion = null;
		try {
			tEdicion = tService.obtenerTipoEdicion(cmsObject, newsPath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("Error al intentar obtener la publicacion de la noticia " + newsPath,e);
			throw new Exception("Error al intentar obtener la publicacion de la noticia " + newsPath,e);
		}
		
		if (tEdicion.getLanguage().equals(pubDestino.getLanguage())) {
			LOG.error("No se puede traducir una noticia en su mismo idioma: " + newsPath);
			throw new Exception("No se puede traducir una noticia en su mismo idioma: " + newsPath);
		}
		
		return rewriteAndTranslateNews(newsPath, pubDestino, pubDestino.getLanguage(), prompt);
    	
	}
	
	public String rewriteAndTranslateNews(String newsPath, TipoEdicion pubDestino, String lang, String prompt) throws Exception {
		TipoEdicionBaseService tService = new TipoEdicionBaseService();
		TipoEdicion tEdicion = null;
		try {
			tEdicion = tService.obtenerTipoEdicion(cmsObject, newsPath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("Error al intentar obtener la publicacion de la noticia " + newsPath,e);
			throw new Exception("Error al intentar obtener la publicacion de la noticia " + newsPath,e);
		}
		
		if (tEdicion.getLanguage().equals(lang)) {
			LOG.error("No se puede traducir una noticia en su mismo idioma: " + newsPath);
			throw new Exception("No se puede traducir una noticia en su mismo idioma: " + newsPath);
		}
		
		CmsFile file;
		I_CmsXmlDocument fileContent;
		try {
			file = getCmsFile(newsPath);
			fileContent = CmsXmlContentFactory.unmarshal(this.cmsObject, file);
		} catch (CmsException e) {
			LOG.error("Error al obtener la noticia " + newsPath,e);
			throw new Exception("Error al obtener la noticia " + newsPath,e);
		}
		
		JSONObject content = new JSONObject();
    	try {
			extractFieldsToTranslateFromNews(newsPath, content, fileContent);
		} catch (CmsXmlException e) {
			LOG.error("Error al obtener el contenido de la noticia " + newsPath,e);
			throw new Exception("Error al obtener el contenido de la noticia " + newsPath,e);
		}
    	
    	String context = getRewriteContext();
    	String newsTitle = content.getString("titulo");
		String newsSubTitleFixed = content.getString("copete");
		String cuerpo = content.getString("cuerpo");
	
		String newsContentFixed = "";
		HashMap<Integer,String> extraContent = new HashMap<>();
		Document body = Jsoup.parse(cuerpo);
		
		int nroParagraph = 0;
		for(Element bodyPart : body.select("body").get(0).children()) {
			if (bodyPart.tagName().equals("p")) {
				newsContentFixed += bodyPart.text() + "\n ";
			}
			else {
				extraContent.put(nroParagraph, bodyPart.outerHtml());
			}
			nroParagraph++;
		}
		
		String fullPrompt = context + "\n" + prompt;
		fullPrompt += "\n Título: "+ newsTitle
				   + "\n Subtítulo: "+ newsSubTitleFixed
				   + "\n Cuerpo: "+ newsContentFixed;
		
		VertexIaNewsService aiService = new VertexIaNewsService(cmsObject.getRequestContext().getSiteRoot(), "" + tEdicion.getId());
    	try {
    		JsonObject responseGenerated = aiService.generateNews(fullPrompt);
    		
    		
    		content.put("titulo",responseGenerated.get("titulo").getAsString());
    		content.put("copete",responseGenerated.get("bajada").getAsString());
    		
    		
    		String newsContentFixedRewrited = "";
    		body = Jsoup.parse(responseGenerated.get("cuerpo").getAsString());
    		
    		int oldNroParagraph = nroParagraph;
    		Element bodyContent = body.select("body").get(0);
    		int newNroParagraph = bodyContent.children().size();
    	
    		int i=0;
    		int j=0;
    		
    		while (i<=oldNroParagraph || j<newNroParagraph) {
    			String extra = extraContent.get(i);
    			if (extra!=null) {
    				newsContentFixedRewrited += extra + "\n ";
    			}
    			else if (j<newNroParagraph) {
    				newsContentFixedRewrited += bodyContent.children().get(j).outerHtml()  + "\n ";
    				j++;
    			}	
    			i++;
    		}
    		
    		content.put("cuerpo",newsContentFixedRewrited);
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("Error al intentar la reescritura de la noticia " + newsPath,e);
			throw new Exception("Error al intentar la reescritura de la noticia " + newsPath,e);
		}
    	
    	JSONObject jsonResponse = translateContent(newsPath, lang, tEdicion, content);
		
		String path="cuerpo";
		String attrName = "alt";
		replaceAttributes(jsonResponse, path, attrName);
		attrName = "title";
		replaceAttributes(jsonResponse, path, attrName);
    	
    	//LOG.error(jsonResponse);
		
		String newsType;
		try {
			newsType = this.cmsObject.readPropertyObject(file, "newsType", true).getValue();
		} catch (CmsException e) {
			LOG.error("Error al obtener el tipo de noticia de " + newsPath,e);
			throw new Exception("Error al obtener el tipo de noticia de " + newsPath,e);
		}
		
		if (newsType==null) {
			LOG.error("Error al obtener el tipo de noticia de " + newsPath);
			throw new Exception("Error al obtener el tipo de noticia de " + newsPath);
		}
		
		
		String fileNameDest = fillDestNews(file, fileContent, jsonResponse, pubDestino, newsType, "rewrite&translate");
		
		
		return fileNameDest;
    	
	}
	
	
	public String translateNews(String newsPath, TipoEdicion pubDestino, String lang) throws Exception {
		
		TipoEdicionBaseService tService = new TipoEdicionBaseService();
		TipoEdicion tEdicion = null;
		try {
			tEdicion = tService.obtenerTipoEdicion(cmsObject, newsPath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("Error al intentar obtener la publicacion de la noticia " + newsPath,e);
			throw new Exception("Error al intentar obtener la publicacion de la noticia " + newsPath,e);
		}
		
		CmsFile file;
		I_CmsXmlDocument fileContent;
		try {
			file = getCmsFile(newsPath);
			fileContent = CmsXmlContentFactory.unmarshal(this.cmsObject, file);
		} catch (CmsException e) {
			LOG.error("Error al obtener la noticia " + newsPath,e);
			throw new Exception("Error al obtener la noticia " + newsPath,e);
		}
		
		JSONObject content = new JSONObject();
    	try {
			extractFieldsToTranslateFromNews(newsPath, content, fileContent);
		} catch (CmsXmlException e) {
			LOG.error("Error al obtener el contenido de la noticia " + newsPath,e);
			throw new Exception("Error al obtener el contenido de la noticia " + newsPath,e);
		}
    	
		JSONObject jsonResponse = translateContent(newsPath, lang, tEdicion, content);
		
		String path="cuerpo";
		String attrName = "alt";
		replaceAttributes(jsonResponse, path, attrName);
		attrName = "title";
		replaceAttributes(jsonResponse, path, attrName);
		
		//LOG.error(jsonResponse);
		
		String newsType;
		try {
			newsType = this.cmsObject.readPropertyObject(file, "newsType", true).getValue();
		} catch (CmsException e) {
			LOG.error("Error al obtener el tipo de noticia de " + newsPath,e);
			throw new Exception("Error al obtener el tipo de noticia de " + newsPath,e);
		}
		
		if (newsType==null) {
			LOG.error("Error al obtener el tipo de noticia de " + newsPath);
			throw new Exception("Error al obtener el tipo de noticia de " + newsPath);
		}
		
		
		String fileNameDest = fillDestNews(file, fileContent, jsonResponse, pubDestino, newsType, "translate");
		
		
		return fileNameDest;
	}

	public String translateNews(String newsPath, TipoEdicion pubDestino) throws Exception {
		
		TipoEdicionBaseService tService = new TipoEdicionBaseService();
		TipoEdicion tEdicion = null;
		try {
			tEdicion = tService.obtenerTipoEdicion(cmsObject, newsPath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("Error al intentar obtener la publicacion de la noticia " + newsPath,e);
			throw new Exception("Error al intentar obtener la publicacion de la noticia " + newsPath,e);
		}
		
		if (tEdicion.getLanguage().equals(pubDestino.getLanguage())) {
			LOG.error("No se puede traducir una noticia en su mismo idioma: " + newsPath);
			throw new Exception("No se puede traducir una noticia en su mismo idioma: " + newsPath);
		}
		
		return translateNews(newsPath, pubDestino, pubDestino.getLanguage());
	}

	private String fillDestNews(CmsFile file, I_CmsXmlDocument fileContent, JSONObject jsonResponse,TipoEdicion pubDestino, String newsType, String proccesCreation) throws Exception {
		
		String fileNameDest = createDestNews(pubDestino, newsType, proccesCreation, cmsObject.getSitePath(file));
		if (fileNameDest==null)
			return null;
		
		NoticiasService nService = new NoticiasService();
		try {
			cmsObject.changeLock(fileNameDest);
			CmsFile fileDest = cmsObject.readFile(fileNameDest, CmsResourceFilter.ALL);        
					
			CmsXmlContent contentDest = CmsXmlContentFactory.unmarshal(cmsObject, fileDest);
	
			contentDest.getValue("titulo", Locale.ENGLISH).setStringValue(cmsObject, jsonResponse.getString("titulo"));
			contentDest.getValue("cuerpo", Locale.ENGLISH).setStringValue(cmsObject, jsonResponse.getString("cuerpo"));
			contentDest.getValue("copete", Locale.ENGLISH).setStringValue(cmsObject, jsonResponse.getString("copete"));
			contentDest.getValue("volanta", Locale.ENGLISH).setStringValue(cmsObject, jsonResponse.getString("volanta"));
			contentDest.getValue("claves", Locale.ENGLISH).setStringValue(cmsObject, jsonResponse.getString("claves"));
			contentDest.getValue("clavesOcultas", Locale.ENGLISH).setStringValue(cmsObject, jsonResponse.getString("clavesOcultas"));
			
			contentDest.getValue("imagenPrevisualizacion/imagen", Locale.ENGLISH).setStringValue(cmsObject,
					fileContent.getValue("imagenPrevisualizacion/imagen",Locale.ENGLISH).getStringValue(cmsObject));
			contentDest.getValue("imagenPrevisualizacion/descripcion", Locale.ENGLISH).setStringValue(cmsObject,
					jsonResponse.getString("imagenPrevisualizacion/descripcion"));
			contentDest.getValue("imagenPrevisualizacion/fuente", Locale.ENGLISH).setStringValue(cmsObject,
					jsonResponse.getString("imagenPrevisualizacion/fuente"));
			contentDest.getValue("imagenPrevisualizacion/keywords", Locale.ENGLISH).setStringValue(cmsObject,
					jsonResponse.getString("imagenPrevisualizacion/keywords"));
			
			int galleryImagesCount = nService.cantidadDeImagenesEnFotogaleria(cmsObject, file);
			
			for (int j=1;j<=galleryImagesCount;j++)
			{
				//Imagen Fotogaleria
				String galleryImage = getElementValue(cmsObject,"imagenesFotogaleria[" + j + "]/imagen[1]", 
						fileContent, Locale.ENGLISH);
				if(!galleryImage.isEmpty()){
					String xmlName ="imagenesFotogaleria[" + j + "]/imagen[1]";
					I_CmsXmlContentValue value = contentDest.getValue(xmlName, Locale.ENGLISH);
					
					if(value==null){
						contentDest.addValue(cmsObject, "imagenesFotogaleria", Locale.ENGLISH,j-1);
					}
					
					contentDest.getValue("imagenesFotogaleria[" + j + "]/imagen[1]",
										Locale.ENGLISH).setStringValue(cmsObject, galleryImage);
				}	
				
				
				
				
				//Description de Fotogaleria
				String galleryDescription = jsonResponse.getString("imagenesFotogaleria[" + j + "]/descripcion");
				if(!galleryDescription.isEmpty()){				
					contentDest.getValue("imagenesFotogaleria[" + j + "]/descripcion",
										Locale.ENGLISH).setStringValue(cmsObject, galleryDescription);
				}	
				
				//Source de Fotogaleria
				String gallerySource = jsonResponse.getString("imagenesFotogaleria[" + j + "]/fuente");
				if(!gallerySource.isEmpty()){				
					contentDest.getValue("imagenesFotogaleria[" + j + "]/fuente",
										Locale.ENGLISH).setStringValue(cmsObject, gallerySource);
				}	
				
				//Tags de Fotogaleria
				String galleryTags = jsonResponse.getString("imagenesFotogaleria[" + j + "]/keywords");
				if(!galleryTags.isEmpty()){				
					contentDest.getValue("imagenesFotogaleria[" + j + "]/keywords",
										Locale.ENGLISH).setStringValue(cmsObject, galleryTags);
				}	
			}
			
			int flashVideoCount = getElementCountWithValue(cmsObject,"videoFlash",
			   		"video",
			   		fileContent);
			
			for (int j=1;j<=flashVideoCount;j++)
			{
				//Video de videoFlash
				String videoFlashVideo = getElementValue(cmsObject,"videoFlash"+"["+j+"]/video[1]", 
										fileContent, Locale.ENGLISH);
				if(!videoFlashVideo.isEmpty()){
					String xmlName = "videoFlash"+"["+j+"]/video[1]";
					I_CmsXmlContentValue value = contentDest.getValue(xmlName, Locale.ENGLISH);					
					if(value==null){
						contentDest.addValue(cmsObject, "videoFlash", Locale.ENGLISH,j-1);
					}					
					contentDest.getValue("videoFlash"+"["+j+"]/video[1]",
										Locale.ENGLISH).setStringValue(cmsObject, videoFlashVideo);
				}	
				
				//Imagen de videoFlash
				String videoFlashImage = getElementValue(cmsObject,"videoFlash"+"["+j+"]/imagen[1]", 
						fileContent, Locale.ENGLISH);
				if(!videoFlashImage.isEmpty()){
					contentDest.getValue("videoFlash"+"["+j+"]/imagen[1]",
										Locale.ENGLISH).setStringValue(cmsObject, videoFlashImage);
				}	
				
				//Titulo de videoFlash
				String videoFlashTitle = jsonResponse.getString("videoFlash[" + j + "]/titulo");
				
				if(!videoFlashTitle.isEmpty()){
					contentDest.getValue("videoFlash"+"["+j+"]/titulo",
										Locale.ENGLISH).setStringValue(cmsObject, videoFlashTitle);
				}	
				
				//Imagen de videoFlash
				String videoFlashSource = jsonResponse.getString("videoFlash[" + j + "]/fuente"); 
				
				if(!videoFlashSource.isEmpty()){
					contentDest.getValue("videoFlash"+"["+j+"]/fuente",
										Locale.ENGLISH).setStringValue(cmsObject, videoFlashSource);
				}	
				
				//Imagen de videoFlash
				String videoFlashDescription = jsonResponse.getString("videoFlash[" + j + "]/descripcion");
				
				if(!videoFlashDescription.isEmpty()){
					contentDest.getValue("videoFlash"+"["+j+"]/descripcion",
										Locale.ENGLISH).setStringValue(cmsObject, videoFlashDescription);
				}	
				
				//Imagen de videoFlash
				String videoFlashTags = jsonResponse.getString("videoFlash[" + j + "]/keywords");
				
				if(!videoFlashTags.isEmpty()){
					contentDest.getValue("videoFlash"+"["+j+"]/keywords",
										Locale.ENGLISH).setStringValue(cmsObject, videoFlashTags);
				}	
			}			
						
			int youtubeImagesCount = getElementCountWithValue(cmsObject,"videoYouTube",
															"youtubeid",
					   										fileContent);
			for (int j=1;j<=youtubeImagesCount;j++)
			{
				//Youtube id
				String videoYoutubeid = getElementValue(cmsObject, "videoYouTube"+"["+j+"]/youtubeid", 
										fileContent, Locale.ENGLISH);
				if(!videoYoutubeid.isEmpty()){
					String xmlName = "videoYouTube"+"["+j+"]/youtubeid";
					I_CmsXmlContentValue value = contentDest.getValue(xmlName, Locale.ENGLISH);					
					if(value==null){
						contentDest.addValue(cmsObject, "videoYouTube", Locale.ENGLISH,j-1);
					}	
					contentDest.getValue("videoYouTube"+"["+j+"]/youtubeid",
										Locale.ENGLISH).setStringValue(cmsObject, videoYoutubeid);
				}	
				
				//Youtube image
				String videoYoutubeImage = getElementValue(cmsObject, "videoYouTube"+"["+j+"]/imagen", 
						fileContent, Locale.ENGLISH);
				if(!videoYoutubeImage.isEmpty()){
					contentDest.getValue("videoYouTube"+"["+j+"]/imagen",
										Locale.ENGLISH).setStringValue(cmsObject, videoYoutubeImage);
				}	
				
				
				
				//Youtube Title
				String videoYoutubeTitle = jsonResponse.getString("videoYouTube[" + j + "]/titulo");
				if(!videoYoutubeTitle.isEmpty()){
					contentDest.getValue("videoYouTube"+"["+j+"]/titulo",
										Locale.ENGLISH).setStringValue(cmsObject, videoYoutubeTitle);
				}	
				
				//Youtube Description
				String videoYoutubeDescription = jsonResponse.getString("videoYouTube[" + j + "]/descripcion");
				if(!videoYoutubeDescription.isEmpty()){
					contentDest.getValue("videoYouTube"+"["+j+"]/descripcion",
										Locale.ENGLISH).setStringValue(cmsObject, videoYoutubeDescription);
				}	
				
				//Youtube Source
				String videoYoutubeSource = jsonResponse.getString("videoYouTube[" + j + "]/fuente");
				if(!videoYoutubeSource.isEmpty()){
					contentDest.getValue("videoYouTube"+"["+j+"]/fuente",
										Locale.ENGLISH).setStringValue(cmsObject, videoYoutubeSource);
				}	
				
				//Youtube Tags
				String videoYoutubeTags = jsonResponse.getString("videoYouTube[" + j + "]/keywords");
				if(!videoYoutubeTags.isEmpty()){
					contentDest.getValue("videoYouTube"+"["+j+"]/keywords",
										Locale.ENGLISH).setStringValue(cmsObject, videoYoutubeTags);
				}	
			}
			
			int embeddedImagesCount = getElementCountWithValue(cmsObject,"videoEmbedded",
															"codigo",
					   										fileContent);			
			for (int j=1;j<=embeddedImagesCount;j++)
			{
				//Embedded codigo
				String videoEmbeddedCode = getElementValue(cmsObject,"videoEmbedded"+"["+j+"]/codigo", 
										fileContent, Locale.ENGLISH);
				if(!videoEmbeddedCode.isEmpty()){
					String xmlName = "videoEmbedded"+"["+j+"]/codigo";
					I_CmsXmlContentValue value = contentDest.getValue(xmlName, Locale.ENGLISH);					
					if(value==null){
						contentDest.addValue(cmsObject, "videoEmbedded", Locale.ENGLISH,j-1);
					}	
					contentDest.getValue("videoEmbedded"+"["+j+"]/codigo",
										Locale.ENGLISH).setStringValue(cmsObject, videoEmbeddedCode);
				}	
				
				//Embedded Image
				String videoEmbeddedImage = getElementValue(cmsObject,"videoEmbedded"+"["+j+"]/imagen", 
						fileContent, Locale.ENGLISH);
				if(!videoEmbeddedImage.isEmpty()){					
					contentDest.getValue("videoEmbedded"+"["+j+"]/imagen",
										Locale.ENGLISH).setStringValue(cmsObject, videoEmbeddedImage);
				}	
				
				//Embedded Title
				String videoEmbeddedTitle = jsonResponse.getString("videoEmbedded[" + j + "]/titulo");
				if(!videoEmbeddedTitle.isEmpty()){					
					contentDest.getValue("videoEmbedded"+"["+j+"]/titulo",
										Locale.ENGLISH).setStringValue(cmsObject, videoEmbeddedTitle);
				}
				
				//Embedded Description
				String videoEmbeddedDescription = jsonResponse.getString("videoEmbedded[" + j + "]/descripcion");
				if(!videoEmbeddedDescription.isEmpty()){					
					contentDest.getValue("videoEmbedded"+"["+j+"]/descripcion",
										Locale.ENGLISH).setStringValue(cmsObject, videoEmbeddedDescription);
				}
				
				//Embedded Source
				String videoEmbeddedSource = jsonResponse.getString("videoEmbedded[" + j + "]/fuente");
				if(!videoEmbeddedSource.isEmpty()){					
					contentDest.getValue("videoEmbedded"+"["+j+"]/fuente",
										Locale.ENGLISH).setStringValue(cmsObject, videoEmbeddedSource);
				}
				
				//Embedded Tags
				String videoEmbeddedTags = jsonResponse.getString("videoEmbedded[" + j + "]/keywords");
				if(!videoEmbeddedTags.isEmpty()){					
					contentDest.getValue("videoEmbedded"+"["+j+"]/keywords",
										Locale.ENGLISH).setStringValue(cmsObject, videoEmbeddedTags);
				}
			}
			
			copyItemsList(fileContent,contentDest, jsonResponse, cmsObject);
	
			String decodedContent = contentDest.toString();
			decodedContent = decodedContent.replaceAll("(?!\\n)[\\p{C}]", "");
			        
			fileDest.setContents(decodedContent.getBytes("UTF-8"));
			//LOG.error(decodedContent);
			cmsObject.writeFile(fileDest);
			cmsObject.unlockResource(fileNameDest);
		}
		catch (CmsException e) {
			LOG.error("Error al completar el contenido de la nueva noticia " + fileNameDest ,e );
			throw new Exception("Error al completar el contenido de la nueva noticia " + fileNameDest ,e );
		} catch (UnsupportedEncodingException e) {
			LOG.error("Error al completar el contenido de la nueva noticia " + fileNameDest ,e );
			throw new Exception("Error al completar el contenido de la nueva noticia " + fileNameDest ,e );
		}
		
		return fileNameDest;
	}

	private String createDestNews(TipoEdicion pubDestino, String newsType, String processCreation, String originalNewsPath) throws Exception {
		String fileNameDest;
		
		NoticiasService nService = new NoticiasService();
		try {
			fileNameDest = nService.crearNoticia(this.cmsObject,pubDestino.getId(), newsType, new HashMap<String,String>());
		
			CmsProperty prop = new CmsProperty("adminVersion", null, "v8", true);
			cmsObject.writePropertyObject(fileNameDest, prop);

			prop = new CmsProperty("newsProcessCreation", null, processCreation, true);
			cmsObject.writePropertyObject(fileNameDest, prop);
			
			prop = new CmsProperty("originalNewsPath", null, originalNewsPath, true);
			cmsObject.writePropertyObject(fileNameDest, prop);
			
		} catch (Exception e) {
			LOG.error("Error al crear la noticia en la publicacion " + pubDestino.getDescripcion() ,e );
			throw new Exception("Error al crear la noticia en la publicacion " + pubDestino.getDescripcion() ,e );
		}
		return fileNameDest;
	}

	private JSONObject translateContent(String newsPath, String lang, TipoEdicion tEdicion,
			JSONObject content) throws Exception {
		JSONObject jsonbody = new JSONObject();
		JSONObject config = new JSONObject();
		config.put("origLang",tEdicion.getLanguage());
		config.put("destLang",lang);
		
		jsonbody.put("process", config);
		
		
		
		jsonbody.put("content", content);
		
		String response=null;
		try {
			response = callAwsTranslate(jsonbody);
			LOG.debug(response);
		} catch (Exception e) {
			LOG.error("Error al intentar traducir la noticia " + newsPath,e);
			throw new Exception("Error al intentar traducir la noticia " + newsPath,e);
		}
		
		
		JSONObject jsonResponse = JSONObject.fromObject(response);
		return jsonResponse;
	}

	private void extractFieldsToTranslateFromNews(String newsPath, JSONObject content, I_CmsXmlDocument fileContent) throws CmsXmlException {
			extractContent(content, fileContent,"titulo");
			extractContent(content, fileContent,"cuerpo");
			extractContent(content, fileContent,"copete");
			extractContent(content, fileContent,"volanta");
			extractContent(content, fileContent,"claves");
			extractContent(content, fileContent,"clavesOcultas");
			extractContent(content, fileContent,"noticiaLista[x]/titulo");
			extractContent(content, fileContent,"noticiaLista[x]/cuerpo");
			
			extractContent(content, fileContent,"noticiaLista[x]/imagenlista/descripcion");
			extractContent(content, fileContent,"noticiaLista[x]/imagenlista/fuente");
			extractContent(content, fileContent,"noticiaLista[x]/imagenlista/keywords");
			
			extractContent(content, fileContent,"noticiaLista[x]/videoYouTube[x]/titulo");
			extractContent(content, fileContent,"noticiaLista[x]/videoYouTube[x]/descripcion");
			extractContent(content, fileContent,"noticiaLista[x]/videoYouTube[x]/fuente");
			extractContent(content, fileContent,"noticiaLista[x]/videoYouTube[x]/keywords");

			extractContent(content, fileContent,"noticiaLista[x]/videoEmbedded[x]/titulo");
			extractContent(content, fileContent,"noticiaLista[x]/videoEmbedded[x]/description");
			extractContent(content, fileContent,"noticiaLista[x]/videoEmbedded[x]/fuente");
			extractContent(content, fileContent,"noticiaLista[x]/videoEmbedded[x]/keywords");

			extractContent(content, fileContent,"noticiaLista[x]/videoFlash[x]/description");
			extractContent(content, fileContent,"noticiaLista[x]/videoFlash[x]/titulo");
			extractContent(content, fileContent,"noticiaLista[x]/videoFlash[x]/fuente");
			extractContent(content, fileContent,"noticiaLista[x]/videoFlash[x]/keywords");

			extractContent(content, fileContent,"imagenPrevisualizacion/descripcion");
			extractContent(content, fileContent,"imagenPrevisualizacion/keywords");
			extractContent(content, fileContent,"imagenPrevisualizacion/fuente");
			extractContent(content, fileContent,"imagenesFotogaleria[x]/descripcion");
			extractContent(content, fileContent,"imagenesFotogaleria[x]/keywords");
			extractContent(content, fileContent,"imagenesFotogaleria[x]/fuente");
			
			extractContent(content, fileContent,"videoFlash[x]/titulo");
			extractContent(content, fileContent,"videoFlash[x]/descripcion");
			extractContent(content, fileContent,"videoFlash[x]/fuente");
			extractContent(content, fileContent,"videoFlash[x]/keywords");
			
			extractContent(content, fileContent,"videoYouTube[x]/titulo");
			extractContent(content, fileContent,"videoYouTube[x]/descripcion");
			extractContent(content, fileContent,"videoYouTube[x]/fuente");
			extractContent(content, fileContent,"videoYouTube[x]/keywords");
			
			extractContent(content, fileContent,"videoEmbedded[x]/titulo");
			extractContent(content, fileContent,"videoEmbedded[x]/descripcion");
			extractContent(content, fileContent,"videoEmbedded[x]/fuente");
			extractContent(content, fileContent,"videoEmbedded[x]/keywords");

		
	}

	private void replaceAttributes(JSONObject jsonResponse, String path, String attrName) {
		int idx=1;
		//LOG.error("reemplazando ->" + path + "_" + attrName + "[" + idx + "]");
		//LOG.error("existe? ->" + jsonResponse.has(path + "_" + attrName + "[" + idx + "]"));
		int startPos = 0;
		while (jsonResponse.has(path + "_" + attrName + "[" + idx + "]")) {
			String value = jsonResponse.getString(path);
			startPos = value.indexOf(attrName+"=\"", startPos);
			if (startPos>-1) {
				startPos += attrName.length() + 2;
				int endPos = value.indexOf("\"", startPos);
				
				value = value.substring(0, startPos) + jsonResponse.getString(path + "_" + attrName + "[" + idx + "]") + value.substring(endPos);
			}
			
			//LOG.error("value ->" + value);
			jsonResponse.put(path, value);
			idx++;
		}
	}
	
	protected void copyItemsList(I_CmsXmlDocument content,CmsXmlContent newContent, JSONObject jsonResponse, CmsObject cms) throws CmsXmlException{
				
		
		newContent.getValue("noticiaListaIntegrada", Locale.ENGLISH).setStringValue(cms, content.getValue("noticiaListaIntegrada", Locale.ENGLISH).getStringValue(cms));
		
		int noticiaListaCount = content.getIndexCount("noticiaLista", Locale.ENGLISH);
		
		//int noticiaListaCount = getElementCountWithValue(cms, "noticiaLista",
		//		 "titulo",
		//			 content);

		for (int j=1;j<=noticiaListaCount;j++)
		{
			String xmlName ="noticiaLista[" + j + "]/titulo";
			I_CmsXmlContentValue value = newContent.getValue(xmlName, Locale.ENGLISH);
					
			if(value==null)
				newContent.addValue(cms, "noticiaLista", Locale.ENGLISH,j-1);
			
			if (jsonResponse.has("noticiaLista[" + j +  "]/titulo"))
				newContent.getValue("noticiaLista" + "[" + j + "]/titulo", Locale.ENGLISH).setStringValue(cms, jsonResponse.getString("noticiaLista[" + j +  "]/titulo"));
			
			String path="noticiaLista"+"[" + j + "]/cuerpo";
			
			if (jsonResponse.has(path)) {
				String attrName = "alt";
				replaceAttributes(jsonResponse, path, attrName);
				attrName = "title";
				replaceAttributes(jsonResponse, path, attrName);
				
				newContent.getValue("noticiaLista"+"[" + j + "]/cuerpo", Locale.ENGLISH).setStringValue(cms, jsonResponse.getString("noticiaLista[" + j +  "]/cuerpo"));
			}
			// Categories
			int categoriesCount = getElementCountWithValue(cms,"noticiaLista"+"[" + j + "]/"+"Categorias","",content);	

			for (int m=1;m<=categoriesCount;m++)
			{
				String CategoryValue = newContent.getStringValue(cms,"noticiaLista"+"[" + j + "]/Categorias["+m+"]", Locale.ENGLISH);
				
				if(CategoryValue==null)
					newContent.addValue(cms, "noticiaLista"+"[" + j + "]/Categorias", Locale.ENGLISH,m-1);
				
				newContent.getValue("noticiaLista"+"[" + j + "]/Categorias["+ m +"]", Locale.ENGLISH).setStringValue(cms, content.getValue("noticiaLista"+"[" + j + "]/Categorias["+ m +"]", Locale.ENGLISH).getStringValue(cms));
			}	
			
			// Imagen
			String imageValue = content.getStringValue(cms,"noticiaLista"+"[" + j + "]/imagenlista/imagen", Locale.ENGLISH);
			
			if(imageValue!=null && !imageValue.equals("")){
				String newImageValue = newContent.getStringValue(cms,"noticiaLista"+"[" + j + "]/imagenlista/imagen", Locale.ENGLISH);
	
				if(newImageValue==null)
							newContent.addValue(cms, "noticiaLista"+"[" + j + "]/imagenlista", Locale.ENGLISH,0);
				
				newContent.getValue("noticiaLista"+"[" + j + "]/imagenlista/imagen", Locale.ENGLISH).setStringValue(cms, content.getValue("noticiaLista"+"[" + j + "]/imagenlista/imagen", Locale.ENGLISH).getStringValue(cms));
				
				String fotografoValue = content.getStringValue(cms,"noticiaLista"+"[" + j + "]/imagenlista/fotografo", Locale.ENGLISH);
				
				if(fotografoValue!=null){
				
					if(newContent.getStringValue(cms,"noticiaLista"+"[" + j + "]/imagenlista[1]/fotografo", Locale.ENGLISH)==null)
						newContent.addValue(cms, "noticiaLista"+"[" + j + "]/imagenlista[1]/fotografo", Locale.ENGLISH,0);	
					
					newContent.getValue("noticiaLista"+"[" + j + "]/imagenlista/fotografo", Locale.ENGLISH).setStringValue(cms, content.getValue("noticiaLista"+"[" + j + "]/imagenlista/fotografo", Locale.ENGLISH).getStringValue(cms));
					
				}
				
				if (jsonResponse.has("noticiaLista[" + j + "]/imagenlista/descripcion"))
					newContent.getValue("noticiaLista"+"[" + j + "]/imagenlista/descripcion", Locale.ENGLISH).setStringValue(cms, jsonResponse.getString("noticiaLista[" + j + "]/imagenlista/descripcion"));
				if (jsonResponse.has("noticiaLista[" + j + "]/imagenlista/fuente"))
					newContent.getValue("noticiaLista"+"[" + j + "]/imagenlista/fuente", Locale.ENGLISH).setStringValue(cms, jsonResponse.getString("noticiaLista[" + j + "]/imagenlista/fuente"));
				if (jsonResponse.has("noticiaLista[" + j + "]/imagenlista/keywords"))
					newContent.getValue("noticiaLista"+"[" + j + "]/imagenlista/keywords", Locale.ENGLISH).setStringValue(cms, jsonResponse.getString("noticiaLista[" + j + "]/imagenlista/keywords"));
				
			}
			
			//videoYouTube
			int youtubeVideosCount = getElementCountWithValue(cms, "noticiaLista"+"[" + j + "]/videoYouTube", "youtubeid", content);
			
			for (int y=1;y<=youtubeVideosCount;y++)
			{
				String videoYoutubeid = getElementValue(cms,"noticiaLista"+"[" + j + "]/videoYouTube"+"["+y+"]/youtubeid",content, Locale.ENGLISH);
				
				if(!videoYoutubeid.isEmpty()){
					String xmlNameYt = "noticiaLista"+"[" + j + "]/"+"videoYouTube"+"["+y+"]/youtubeid";
					I_CmsXmlContentValue valueYt = newContent.getValue(xmlNameYt, Locale.ENGLISH);					
					if(valueYt==null){
						newContent.addValue(cms, "noticiaLista"+"[" + j + "]/"+"videoYouTube", Locale.ENGLISH,y-1);
					}	
					newContent.getValue("noticiaLista"+"[" + j + "]/"+"videoYouTube"+"["+y+"]/youtubeid", Locale.ENGLISH).setStringValue(cms, videoYoutubeid);
					
					//Youtube image
					String videoYoutubeImage = getElementValue(cms, "noticiaLista"+"[" + j + "]/"+"videoYouTube"+"["+y+"]/imagen",content, Locale.ENGLISH);
					if(!videoYoutubeImage.isEmpty()){
						newContent.getValue("noticiaLista"+"[" + j + "]/"+"videoYouTube"+"["+y+"]/imagen",Locale.ENGLISH).setStringValue(cms, videoYoutubeImage);
					}	

					//Youtube Title
					String videoYoutubeTitle = jsonResponse.getString("noticiaLista[" + j + "]/videoYouTube["+y+"]/titulo");
					if(!videoYoutubeTitle.isEmpty()){
						newContent.getValue("noticiaLista"+"[" + j + "]/"+"videoYouTube"+"["+y+"]/titulo",Locale.ENGLISH).setStringValue(cms, videoYoutubeTitle);
					}	
					
					//Youtube Description
					String videoYoutubeDescription = jsonResponse.getString("noticiaLista[" + j + "]/videoYouTube["+y+"]/descripcion");
					
					if(!videoYoutubeDescription.isEmpty()){
						newContent.getValue("noticiaLista"+"[" + j + "]/"+"videoYouTube"+"["+y+"]/descripcion",Locale.ENGLISH).setStringValue(cms, videoYoutubeDescription);
					}	
					
					//Youtube Source
					String videoYoutubeSource = jsonResponse.getString("noticiaLista[" + j + "]/videoYouTube["+y+"]/fuente");
					
					if(!videoYoutubeSource.isEmpty()){
						newContent.getValue("noticiaLista"+"[" + j + "]/"+"videoYouTube"+"["+y+"]/fuente",Locale.ENGLISH).setStringValue(cms, videoYoutubeSource);
					}
					
					String tagNameAutor = "noticiaLista"+"[" + j + "]/"+"videoYouTube"+"["+y+"]/autor";
					newContent.getValue(tagNameAutor,Locale.ENGLISH).setStringValue(cms,getElementValue(cms, tagNameAutor, content, Locale.ENGLISH));
					
					String tagNameCalificacion = "noticiaLista"+"[" + j + "]/"+"videoYouTube"+"["+y+"]/calificacion";
					newContent.getValue(tagNameCalificacion,Locale.ENGLISH).setStringValue(cms,getElementValue(cms, tagNameCalificacion, content, Locale.ENGLISH));
					
			        String tagNameMostrarEnHome = "noticiaLista"+"[" + j + "]/"+"videoYouTube"+"["+y+"]/mostrarEnHome";
					newContent.getValue(tagNameMostrarEnHome,Locale.ENGLISH).setStringValue(cms,getElementValue(cms, tagNameMostrarEnHome, content, Locale.ENGLISH));
					
			        String tagNameKeywords = "noticiaLista"+"[" + j + "]/"+"videoYouTube"+"["+y+"]/keywords";
			        if (jsonResponse.has(tagNameKeywords))
			        	newContent.getValue(tagNameKeywords,Locale.ENGLISH).setStringValue(cms,jsonResponse.getString("noticiaLista[" + j + "]/videoYouTube["+y+"]/keywords"));
					
					// Categories
					int categoriesCountYt = getElementCountWithValue(cms, "noticiaLista"+"[" + j + "]/"+"videoYouTube",
					   		"categoria",
					   		content);	

					for (int my=1;my<=categoriesCountYt;my++)
					{
						String CategoryValue = newContent.getStringValue(cms,"noticiaLista"+"[" + j + "]/"+"videoYouTube"+"[" + y + "]/categoria", Locale.ENGLISH);
						
						if(CategoryValue==null)
							newContent.addValue(cms, "noticiaLista"+"[" + j + "]/"+"videoYouTube"+"[" + y + "]/categoria["+ my +"]", Locale.ENGLISH,my-1);
						
						newContent.getValue("noticiaLista"+"[" + j + "]/"+"videoYouTube"+"[" + y + "]/categoria["+ my +"]", Locale.ENGLISH).setStringValue(cms, content.getValue("noticiaLista"+"[" + j + "]/"+"videoYouTube"+"[" + y + "]/categoria["+ my +"]", Locale.ENGLISH).getStringValue(cms));
					}	
					
					
			        String tagNameAutoplay = "noticiaLista"+"[" + j + "]/"+"videoYouTube"+"["+y+"]/autoplay";
					newContent.getValue(tagNameAutoplay,Locale.ENGLISH).setStringValue(cms,getElementValue(cms,tagNameAutoplay, content, Locale.ENGLISH));
					
			        String tagNameMute = "noticiaLista"+"[" + j + "]/"+"videoYouTube"+"["+y+"]/mute";
					newContent.getValue(tagNameMute,Locale.ENGLISH).setStringValue(cms,getElementValue(cms, tagNameMute, content, Locale.ENGLISH));
				}	
				
			}

			//videoEmbedded
			int embededVideosCount = getElementCountWithValue(cms, "noticiaLista"+"[" + j + "]/"+"videoEmbedded", "codigo", content);
			
			for (int e=1;e<=embededVideosCount;e++)
			{
				String videoCode = getElementValue(cms, "noticiaLista"+"[" + j + "]/"+"videoEmbedded"+"["+e+"]/"+"codigo",content, Locale.ENGLISH);
				
				if(!videoCode.isEmpty()){
					String xmlNameE = "noticiaLista"+"[" + j + "]/"+"videoEmbedded"+"["+e+"]/"+"codigo";
					I_CmsXmlContentValue valueE = newContent.getValue(xmlNameE, Locale.ENGLISH);					
					if(valueE==null){
						newContent.addValue(cms, "noticiaLista"+"[" + j + "]/"+"videoEmbedded", Locale.ENGLISH,e-1);
					}	
					newContent.getValue("noticiaLista"+"[" + j + "]/"+"videoEmbedded"+"["+e+"]/"+"codigo", Locale.ENGLISH).setStringValue(cms, videoCode);
					
					//Embeded image
					String videoEmbededImage = getElementValue(cms, "noticiaLista"+"[" + j + "]/"+"videoEmbedded"+"["+e+"]/"+"imagen",content, Locale.ENGLISH);
					if(!videoEmbededImage.isEmpty()){
						newContent.getValue("noticiaLista"+"[" + j + "]/"+"videoEmbedded"+"["+e+"]/"+"imagen",Locale.ENGLISH).setStringValue(cms, videoEmbededImage);
					}	
					
					//Embedded Title
					String videoEmbeddedTitle = jsonResponse.getString("noticiaLista[" + j + "]/videoEmbedded["+e+"]/titulo");
					if(!videoEmbeddedTitle.isEmpty()){
						newContent.getValue("noticiaLista"+"[" + j + "]/"+"videoEmbedded"+"["+e+"]/"+"titulo",Locale.ENGLISH).setStringValue(cms, videoEmbeddedTitle);
					}	
					
					//Embedded Description
					String videoEmbeddedDescription = jsonResponse.getString("noticiaLista[" + j + "]/videoEmbedded["+e+"]/description");
					
					if(!videoEmbeddedDescription.isEmpty()){
						newContent.getValue("noticiaLista"+"[" + j + "]/"+"videoEmbedded"+"["+e+"]/"+"description",Locale.ENGLISH).setStringValue(cms, videoEmbeddedDescription);
					}	
					
					//Embedded Source
					
					if (jsonResponse.has("noticiaLista[" + j + "]/videoEmbedded["+e+"]/fuente")) {
						String videoEmbeddedSource = jsonResponse.getString("noticiaLista[" + j + "]/videoEmbedded["+e+"]/fuente");
						
						if(!videoEmbeddedSource.isEmpty()){
							newContent.getValue("noticiaLista"+"[" + j + "]/"+"videoEmbedded"+"["+e+"]/"+"fuente",Locale.ENGLISH).setStringValue(cms, videoEmbeddedSource);
						}
					}
					String tagNameAutor = "noticiaLista"+"[" + j + "]/"+"videoEmbedded"+"["+e+"]/autor";
					newContent.getValue(tagNameAutor,Locale.ENGLISH).setStringValue(cms,getElementValue(cms,tagNameAutor, content, Locale.ENGLISH));
					
					String tagNameCalificacion = "noticiaLista"+"[" + j + "]/"+"videoEmbedded"+"["+e+"]/calificacion";
					newContent.getValue(tagNameCalificacion,Locale.ENGLISH).setStringValue(cms,getElementValue(cms,tagNameCalificacion, content, Locale.ENGLISH));
					
			        String tagNameKeywords = "noticiaLista"+"[" + j + "]/"+"videoEmbedded"+"["+e+"]/"+"keywords";
			        if (jsonResponse.has(tagNameKeywords))
			        		newContent.getValue(tagNameKeywords,Locale.ENGLISH).setStringValue(cms,jsonResponse.getString("noticiaLista[" + j + "]/videoEmbedded["+e+"]/keywords"));
					
					// Categories
					int categoriesCountE = getElementCountWithValue(cms,"noticiaLista"+"[" + j + "]/"+"videoEmbedded","categoria",content);	

					for (int me=1;me<=categoriesCountE;me++)
					{
						String CategoryValue = newContent.getStringValue(cms,"noticiaLista"+"[" + j + "]/"+"videoEmbedded"+"[" + e + "]/"+"categoria", Locale.ENGLISH);
						
						if(CategoryValue==null)
							newContent.addValue(cms, "noticiaLista"+"[" + j + "]/"+"videoEmbedded"+"[" + e + "]/"+"categoria"+"["+ me +"]", Locale.ENGLISH,me-1);
						
						newContent.getValue("noticiaLista"+"[" + j + "]/"+"videoEmbedded"+"[" + e + "]/"+"categoria"+"["+ me +"]", Locale.ENGLISH).setStringValue(cms, content.getValue("noticiaLista"+"[" + j + "]/"+"videoEmbedded"+"[" + e + "]/"+"categoria"+"["+ me +"]", Locale.ENGLISH).getStringValue(cms));
					}	
					
				}	
			}
			
			//videoFlash
			int flashVideosCount = getElementCountWithValue(cms,"noticiaLista"+"[" + j + "]/"+"videoFlash", "video", content);
			
			for (int f=1;f<=flashVideosCount;f++)
			{
				String videoFlash = getElementValue(cms, "noticiaLista"+"[" + j + "]/"+"videoFlash"+"["+f+"]/"+"video",content, Locale.ENGLISH);
				
				if(!videoFlash.isEmpty()){
					String xmlNameF = "noticiaLista"+"[" + j + "]/"+"videoFlash"+"["+f+"]/"+"video";
					I_CmsXmlContentValue valueF = newContent.getValue(xmlNameF, Locale.ENGLISH);					
					if(valueF==null){
						newContent.addValue(cms, "noticiaLista"+"[" + j + "]/"+"videoFlash", Locale.ENGLISH,f-1);
					}	
					newContent.getValue("noticiaLista"+"[" + j + "]/"+"videoFlash"+"["+f+"]/"+"video", Locale.ENGLISH).setStringValue(cms, videoFlash);
					
					//Video nativo image
					String videoFlashImage = getElementValue(cms, "noticiaLista"+"[" + j + "]/"+"videoFlash"+"["+f+"]/imagen"+"imagen",content, Locale.ENGLISH);
					if(!videoFlashImage.isEmpty()){
						newContent.getValue("noticiaLista"+"[" + j + "]/"+"videoFlash"+"["+f+"]/"+"imagen",Locale.ENGLISH).setStringValue(cms, videoFlashImage);
					}	
					
					 if (jsonResponse.has("noticiaLista[" + j + "]/videoFlash["+f+"]/titulo")) {
						//Flash Title
						String videoFlaseTitle = jsonResponse.getString("noticiaLista[" + j + "]/videoFlash["+f+"]/titulo");
						if(!videoFlaseTitle.isEmpty()){
							newContent.getValue("noticiaLista"+"[" + j + "]/"+"videoFlash"+"["+f+"]/"+"titulo",Locale.ENGLISH).setStringValue(cms, videoFlaseTitle);
						}	
					 }
					
					//Flash Description
					 if (jsonResponse.has("noticiaLista[" + j + "]/videoFlash["+f+"]/description")) {
						String videoFlashDescription = jsonResponse.getString("noticiaLista[" + j + "]/videoFlash["+f+"]/description");
						
						if(!videoFlashDescription.isEmpty()){
							newContent.getValue("noticiaLista"+"[" + j + "]/"+"videoFlash"+"["+f+"]/"+"description",Locale.ENGLISH).setStringValue(cms, videoFlashDescription);
						}	
					 }
					
					//flash Source
					 if (jsonResponse.has("noticiaLista[" + j + "]/videoFlash["+f+"]/fuente")) {
						String videoFlashSource = jsonResponse.getString("noticiaLista[" + j + "]/videoFlash["+f+"]/fuente");
						
						if(!videoFlashSource.isEmpty()){
							newContent.getValue("noticiaLista"+"[" + j + "]/"+"videoFlash"+"["+f+"]/"+"fuente",Locale.ENGLISH).setStringValue(cms, videoFlashSource);
						}
					 }
					
					String tagNameAutor = "noticiaLista"+"[" + j + "]/"+"videoFlash"+"["+f+"]/autor";
					newContent.getValue(tagNameAutor,Locale.ENGLISH).setStringValue(cms,getElementValue(cms, tagNameAutor, content, Locale.ENGLISH));
					
					String tagNameCalificacion = "noticiaLista"+"[" + j + "]/"+"videoFlash"+"["+f+"]/calificacion";
					newContent.getValue(tagNameCalificacion,Locale.ENGLISH).setStringValue(cms,getElementValue(cms, tagNameCalificacion, content, Locale.ENGLISH));
					
					String tagNameHideComments = "noticiaLista"+"[" + j + "]/"+"videoFlash"+"["+f+"]/ocultarComentarios";
					newContent.getValue(tagNameHideComments,Locale.ENGLISH).setStringValue(cms,getElementValue(cms, tagNameHideComments, content, Locale.ENGLISH));
					
			        String tagNameMostrarEnHome = "noticiaLista"+"[" + j + "]/"+"videoFlash"+"["+f+"]/mostrarEnHome";
					newContent.getValue(tagNameMostrarEnHome,Locale.ENGLISH).setStringValue(cms,getElementValue(cms, tagNameMostrarEnHome, content, Locale.ENGLISH));
					
			        String tagNameKeywords = "noticiaLista"+"[" + j + "]/"+"videoFlash"+"["+f+"]/keywords";
			        if (jsonResponse.has(tagNameKeywords))
			        	newContent.getValue(tagNameKeywords,Locale.ENGLISH).setStringValue(cms,jsonResponse.getString("noticiaLista[" + j + "]/videoFlash["+f+"]/keywords"));
					
					// Categories
					int categoriesCountF = getElementCountWithValue(cms,"noticiaLista"+"[" + j + "]/"+"videoFlash",
					   		"categoria",
					   		content);	

					for (int mf=1;mf<=categoriesCountF;mf++)
					{
						String CategoryValue = newContent.getStringValue(cms,"noticiaLista"+"[" + j + "]/"+"videoFlash"+"[" + f + "]/categoria", Locale.ENGLISH);
						
						if(CategoryValue==null)
							newContent.addValue(cms, "noticiaLista"+"[" + j + "]/"+"videoFlash"+"[" + f + "]/categoria["+ mf +"]", Locale.ENGLISH,mf-1);
						
						newContent.getValue("noticiaLista"+"[" + j + "]/"+"videoFlash"+"[" + f + "]/categoria["+ mf +"]", Locale.ENGLISH).setStringValue(cms, content.getValue("noticiaLista"+"[" + j + "]/"+"videoFlash"+"[" + f + "]/categoria["+ mf +"]", Locale.ENGLISH).getStringValue(cms));
					}	
					
					
			        String tagNameAutoplay = "noticiaLista"+"[" + j + "]/"+"videoFlash"+"["+f+"]/autoplay";
					newContent.getValue(tagNameAutoplay,Locale.ENGLISH).setStringValue(cms,getElementValue(cms,tagNameAutoplay, content, Locale.ENGLISH));
					
			        String tagNameMute = "noticiaLista"+"[" + j + "]/"+"videoFlash"+"["+f+"]/mute";
					newContent.getValue(tagNameMute,Locale.ENGLISH).setStringValue(cms,getElementValue(cms,tagNameMute, content, Locale.ENGLISH));
				}	
				
			}
			
			//noticia
			 String tagNameNoticia = "noticiaLista"+"[" + j + "]/"+"noticia";
			 
			 if(content.getStringValue(cms,tagNameNoticia, Locale.ENGLISH)!=null){
				 
				 String noticiaValue = newContent.getStringValue(cms,"noticiaLista"+"[" + j + "]/"+"noticia"+"[1]", Locale.ENGLISH);
				
				 if(noticiaValue==null)
					 newContent.addValue(cms, "noticiaLista"+"[" + j + "]/"+"noticia", Locale.ENGLISH,0);
			 
			 
			newContent.getValue(tagNameNoticia,Locale.ENGLISH).setStringValue(cms,getElementValue(cms,tagNameNoticia, content, Locale.ENGLISH));
			}
				
			//fecha
			 String tagNameFecha = "noticiaLista"+"[" + j + "]/"+"fecha";
					newContent.getValue(tagNameFecha,Locale.ENGLISH).setStringValue(cms,getElementValue(cms,tagNameFecha , content, Locale.ENGLISH));
		}
	}

    protected int getElementCountWithValue(CmsObject cms, String key, String controlKey, I_CmsXmlDocument content)
	{
		Locale locale = cms.getRequestContext().getLocale();
		int total = content.getIndexCount(key, locale);
		
		int blank = 0;
		for (int j=1;j<=total;j++)
		{
			String controlValue;
			try {
				controlValue = content.getStringValue(cms, key + "[" + j + "]/" + controlKey, locale);
			
				if (controlValue==null || controlValue.trim().equals(""))
					blank ++;
			} catch (CmsXmlException e) {
				LOG.debug("Error reading content value " + key + "[" + j + "]/" + controlKey + " on content " + content.getFile().getRootPath(),e);

			}
		}
		
		
		return total - blank;
	}

	protected String getElementValue(CmsObject cms, String elementName, I_CmsXmlDocument content, Locale locale) {    
		try {
	    	String value = content.getStringValue(cms, elementName, locale);
	    	if (value==null)
	    	{
	    		value = "";
	    		LOG.debug("Content value " + elementName + "not found on content" + content.getFile().getRootPath());
	    	}
			return value;
		} catch (CmsXmlException e) {
			LOG.error("Error reading content value " + elementName + " on content " + content.getFile().getRootPath(),e);
		}
	
		return "";
	}
		
	protected String callAwsTranslate(JSONObject jsonbody) throws Exception
	{

		String sBody = jsonbody.toString(2);
		// LOG.debug("informamos a AWS " + sBody + " endpoint "+ endpoint);
		StringRequestEntity requestEntity = new StringRequestEntity(
				sBody,
				"application/json; charset=utf-8",
				"utf-8");

		PostMethod postMethod = new PostMethod(getTranslateEndpoint());
		postMethod.setRequestEntity(requestEntity);
		postMethod.addRequestHeader(new Header("Accept", "application/json"));
		postMethod.addRequestHeader(new Header("Accept-Charset", "utf-8"));
		
		HttpClient httpClient = new HttpClient();
		int statusCode = httpClient.executeMethod(postMethod);
		LOG.debug("volvemos da AWS " + statusCode);
		if (statusCode == HttpStatus.SC_OK) {
			LOG.debug(postMethod.getResponseBodyAsString());
			
			ByteBuffer buffer = StandardCharsets.ISO_8859_1.encode(postMethod.getResponseBodyAsString()); 

			String utf8EncodedString = StandardCharsets.UTF_8.decode(buffer).toString();
			LOG.debug("utf8:");
			LOG.debug(utf8EncodedString);
			
			return utf8EncodedString;
			//buffer = StandardCharsets.UTF_8.encode(postMethod.getResponseBodyAsString());
			//LOG.debug("ISO_8859_1:");
			//LOG.debug( StandardCharsets.ISO_8859_1.decode(buffer).toString());
			
			//return postMethod.getResponseBodyAsString();
		}
		else {
			LOG.error("Error al intentar traducir. Error code" + statusCode);
			LOG.error("Error response: " + postMethod.getResponseBodyAsString());
			throw new Exception("Error al intentar traducir. Error code" + statusCode);
		}
		
	}
	
	private CmsFile getCmsFile(String resourceName) throws CmsException {
    	
    	String filePath = CmsWorkplace.getTemporaryFileName(resourceName);
    	
    	if(!cmsObject.existsResource(filePath, CmsResourceFilter.ALL))
    		filePath = resourceName;
    	
    	CmsFile file = cmsObject.readFile(filePath);
    	
    	return file;
    	
    }

	public void extractContent(JSONObject ContentExtracted, I_CmsXmlDocument fileContent, String path) throws CmsXmlException {

		//LOG.debug("Extrayendo contenido del campo " + path);

		//I_CmsXmlDocument fileContent = CmsXmlContentFactory.unmarshal(this.cmsObject, file);
		Locale locale = this.cmsObject.getRequestContext().getLocale();

		String pathNum = "";
		int lastPos = 0;
		if (path.contains("[x]")) {
			int i=1;
			do {
				
				pathNum = path.replaceFirst("\\[x\\]", "[" + i + "]");
				
				int lenIdx = ("" + i).length(); 
				lastPos = (path.indexOf("x]/")>-1 ? path.indexOf("x]/")+lenIdx+1 : pathNum.length());
				
				extractContent(ContentExtracted, fileContent, pathNum);
				i++;
			}
			while (fileContent.getValue(pathNum.substring(0,lastPos), locale)!=null);
		}
		else {
			String value = fileContent.getStringValue(this.cmsObject, path, locale);
			if (value!=null) {
				ContentExtracted.put(path,value);
				
				//Obtener tambien los alt y los title:
				Document document = Jsoup.parse(value);
				int nro=1;
				nro = extractAttributes(ContentExtracted, path, "title", document, nro);
				nro=1;
				nro = extractAttributes(ContentExtracted, path, "alt", document, nro);
			}

		}



    }

	private int extractAttributes(JSONObject ContentExtracted, String path, String attr, Document document, int nro) {
		for (Element element : document.select("["+ attr +"]")) {
			String attrValue = element.attr(attr);
			if (attrValue.length()>0) {
				ContentExtracted.put(path + "_" + attr + "[" + nro + "]",attrValue);
			}
			nro++;
		}
		return nro;
	}
}
