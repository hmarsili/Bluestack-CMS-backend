package com.tfsla.diario.ediciones.services;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
	
	public String urltranslateEndpoint;
	
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
	
	public String translateNews(String newsPath, TipoEdicion pubDestino) {
		
		TipoEdicionBaseService tService = new TipoEdicionBaseService();
		TipoEdicion tEdicion = null;
		try {
			tEdicion = tService.obtenerTipoEdicion(cmsObject, newsPath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("Error al intentar obtener la publicacion de la noticia " + newsPath,e);
			return null;
		}
		
		if (tEdicion.getLanguage().equals(pubDestino.getLanguage())) {
			LOG.error("No se puede traducir una noticia en su mismo idioma: " + newsPath);
			return null;
		}
		
		JSONObject content = new JSONObject();
		
		CmsFile file;
		I_CmsXmlDocument fileContent;
		try {
			file = getCmsFile(newsPath);
			fileContent = CmsXmlContentFactory.unmarshal(this.cmsObject, file);
		} catch (CmsException e) {
			LOG.error("Error al obtener la noticia " + newsPath,e);
			return null;
		}
		
		//TODO: Falta cambiar el alt y title dentro del cuerpo de la nota (y otros lados)
		//TODO: Falta incluir lo de nota lista
		
    	try {
			extractContent(content, fileContent,"titulo");
			extractContent(content, fileContent,"cuerpo");
			extractContent(content, fileContent,"copete");
			extractContent(content, fileContent,"volanta");
			extractContent(content, fileContent,"claves");
			extractContent(content, fileContent,"clavesOcultas");
			extractContent(content, fileContent,"noticiaLista[x]/titulo");
			extractContent(content, fileContent,"noticiaLista[x]/cuerpo");
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

		} catch (CmsXmlException e) {
			LOG.error("Error al obtener el contenido de la noticia " + newsPath,e);
			return null;
		}
		
		/*
		
		{
		    "process" : {
		        "origLang" : "es-MX",
		        "destLang" : "he"
		    }
		    "content": {
		        "hello":"hello",
		        "bye":"adios"
		    }
		}
		 
		 */
		
    	
		JSONObject jsonbody = new JSONObject();
		JSONObject config = new JSONObject();
		config.put("origLang",tEdicion.getLanguage());
		config.put("destLang",pubDestino.getLanguage());
		
		jsonbody.put("process", config);
		
		
		
		jsonbody.put("content", content);
		
		String response=null;
		try {
			response = callAwsTranslate(jsonbody);
			LOG.error(response);
		} catch (Exception e) {
			LOG.error("Error al intentar traducir la noticia " + newsPath,e);
			e.printStackTrace();
		}
		
		
		JSONObject jsonResponse = JSONObject.fromObject(response);
		LOG.error(jsonResponse);
		
		//Creo la noticia.
		String newsType;
		try {
			newsType = this.cmsObject.readPropertyObject(file, "newsType", true).getValue();
		} catch (CmsException e) {
			LOG.error("Error al obtener el tipo de noticia de " + newsPath,e);
			return null;
		}
		
		if (newsType==null) {
			LOG.error("Error al obtener el tipo de noticia de " + newsPath);
			return null;
		}
		
		NoticiasService nService = new NoticiasService();
		String fileNameDest;
		try {
			fileNameDest = nService.crearNoticia(this.cmsObject,pubDestino.getId(), newsType, new HashMap<String,String>());
		
			CmsProperty prop = new CmsProperty("adminVersion", null, "v8", true);
			cmsObject.writePropertyObject(fileNameDest, prop);

			prop = new CmsProperty("newsProcessCreation", null, "Translate", true);
			cmsObject.writePropertyObject(fileNameDest, prop);
			
		} catch (Exception e) {
			LOG.error("Error al crear la noticia en la publicacion " + pubDestino.getDescripcion() ,new Exception("Stack trace") );
			return null;
		}
		
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

		
		
		
		
		
		
		
		
		
		
		
		String decodedContent = contentDest.toString();
		decodedContent = decodedContent.replaceAll("(?!\\n)[\\p{C}]", "");
		        
		fileDest.setContents(decodedContent.getBytes("UTF-8"));
		LOG.error(decodedContent);
		cmsObject.writeFile(fileDest);
		cmsObject.unlockResource(fileNameDest);
		}
		catch (CmsException e) {
			LOG.error("Error al completar el contenido de la nueva noticia " + fileNameDest ,e );
			return null;
		} catch (UnsupportedEncodingException e) {
			LOG.error("Error al completar el contenido de la nueva noticia " + fileNameDest ,e );
			return null;
		}
		
		
		return null;
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
				"application/json",
				"UTF-8");

		PostMethod postMethod = new PostMethod(getTranslateEndpoint());
		postMethod.setRequestEntity(requestEntity);

		HttpClient httpClient = new HttpClient();
		int statusCode = httpClient.executeMethod(postMethod);
		LOG.debug("volvemos da AWS " + statusCode);
		if (statusCode == HttpStatus.SC_OK) {
			LOG.debug(postMethod.getResponseBodyAsString());
			return postMethod.getResponseBodyAsString();
		}
		else {
			LOG.error("Error al intentar traducir. Error code" + statusCode);
			LOG.error("Error response: " + postMethod.getResponseBodyAsString());
		}
		return null;
	}
	
	private CmsFile getCmsFile(String resourceName) throws CmsException {
    	
    	String filePath = CmsWorkplace.getTemporaryFileName(resourceName);
    	
    	if(!cmsObject.existsResource(filePath, CmsResourceFilter.ALL))
    		filePath = resourceName;
    	
    	CmsFile file = cmsObject.readFile(filePath);
    	
    	return file;
    	
    }
	
	public void extractContent(JSONObject ContentExtracted, I_CmsXmlDocument fileContent, String path) throws CmsXmlException {

		LOG.error("Extrayendo contenido del campo " + path);

		//I_CmsXmlDocument fileContent = CmsXmlContentFactory.unmarshal(this.cmsObject, file);
		Locale locale = this.cmsObject.getRequestContext().getLocale();

		
		if (path.contains("[x]")) {
			String value = null;
			int i=1;
			do {
				String pathNum = path;

				pathNum = pathNum.replace("[x]", "[" + i + "]");
				value = fileContent.getStringValue(cmsObject, pathNum, locale);
				LOG.error(pathNum + ": " + value);
				if (value!=null) {
					ContentExtracted.put(pathNum,value);
					
					//Obtener tambien los alt y los title:
					Document document = Jsoup.parse(value);
					int nro=1;
					nro = extractAttributes(ContentExtracted, pathNum, "title", document, nro);
					nro=1;
					nro = extractAttributes(ContentExtracted, pathNum, "alt", document, nro);
					
				}
				i++;
			}
			while (value!=null);

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
