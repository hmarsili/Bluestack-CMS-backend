package com.tfsla.utils;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.main.TfsLogger;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;

public class ScriptsJSFilter {
	
	private CPMConfig config;
    private String protectedClasses = null;
    private boolean removeScriptsBody = false;
    private boolean removeJsCompressBody = false;
    private boolean removeIframesBody = false;
    private static String user = null;
    private static String pathFile = null;
    private CmsObject cmsObj = null;
    
    protected static final Log LOG = CmsLog.getLog(ScriptsJSFilter.class);
    
    //String logFile = "bodyScriptsFilter.log";
	
    //protected static final TfsLogger TfsLOG = new TfsLogger();
    
    
	public ScriptsJSFilter(CmsObject cms, String path){
		
		config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		
		TipoEdicion tEdicion  = null;
		TipoEdicionService tEService = new TipoEdicionService();
		
		CmsFile file;
		try {
			file = cms.readFile(path,CmsResourceFilter.ALL);
			tEdicion = tEService.obtenerTipoEdicion(cms,file.getRootPath());
			
		} catch (CmsException e) {
			LOG.error("No pudo leer el archivo ", e);
		} catch (Exception e1) {
			LOG.error("No pudo obtener la publicacion ", e1);
		}
		
		protectedClasses = config.getParam(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot(), String.valueOf(tEdicion.getId()), "adminNewsConfiguration", "protectedClassesBody", null);
		removeScriptsBody = config.getBooleanParam(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot(), String.valueOf(tEdicion.getId()), "adminNewsConfiguration", "removeScriptsBody",false);
		removeJsCompressBody = config.getBooleanParam(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot(), String.valueOf(tEdicion.getId()), "adminNewsConfiguration", "removeJsCompressBody",false);
		removeIframesBody = config.getBooleanParam(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot(), String.valueOf(tEdicion.getId()), "adminNewsConfiguration", "removeIframesBody",false);
		
		user = cms.getRequestContext().currentUser().getName();
		pathFile = path;
		cmsObj = cms;
	}
	
	public static String removeJavascriptCode(String message) {
		
		String scriptRegex = "(<script[\\d\\D]*?>[\\d\\D]*?<\\/script.*>)";
	      
	      Pattern pattern2 = Pattern.compile(scriptRegex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	      if(message != null) {
	            Matcher matcher2 = pattern2.matcher(message);
	            StringBuffer str = new StringBuffer(message.length());
	            
	            while(matcher2.find()) {
	              LOG.error("Código borrado del copete: "+matcher2.group(1) + " - Usuario: "+user + " - path: "+pathFile);
	              matcher2.appendReplacement(str, Matcher.quoteReplacement(" "));
	            }
	            matcher2.appendTail(str);
	            message = str.toString();
	      }
	     return message;
	}
	
	public static String removeIframesCode(String message) {
		
		String scriptRegex = "(<iframe[\\d\\D]*?>[\\d\\D]*?<\\/iframe.*>)";
	      
	      Pattern pattern2 = Pattern.compile(scriptRegex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	      if(message != null) {
	            Matcher matcher2 = pattern2.matcher(message);
	            StringBuffer str = new StringBuffer(message.length());
	            while(matcher2.find()) {
		          LOG.error("Se removió el siguiente iframe: "+matcher2.group(1)+ " - Usuario: "+user + " - path: "+pathFile);
	              matcher2.appendReplacement(str, Matcher.quoteReplacement(" "));
	            }
	            matcher2.appendTail(str);
	            message = str.toString();
	      }
	     return message;
		
	}
	
	public static String removeJavascriptCompressedCode(String message) {
		
		String textStr[] = message.split("\\r\\n|\\n|\\r");
		
		String cleanMessage = "";
		String deletedString = "";
		
		for (int i = 0; i < textStr.length; i++) {

   	      	if(textStr[i] != null && !textStr[i].equals("")) {
   	      		
   	      		if(!isJavascriptCode(textStr[i])){
   	      			cleanMessage += textStr[i]+"\n";
   	      		}else{
   	      		    deletedString += textStr[i]+"\n";
   	      		}
   	      	}
        }
		
		if(!deletedString.trim().equals(""))
			 LOG.error("Código borrado (Javascript obfuscado o comprimido): "+deletedString + " - Usuario: "+user + " - path: "+pathFile);
		
		return cleanMessage;
	}
	
	private static boolean isJavascriptCode(String input){
		
		boolean isJsCode = false;
		
		String scriptRegex = "\\\\x\\d|function\\s?\\(";
		
		int RUN_LENGTH_THRESHOLD = 10;
		
		Pattern pattern2 = Pattern.compile(scriptRegex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		Matcher countCodeMatcher = pattern2.matcher(input);
		
		int count = 0;
		
		while (countCodeMatcher.find()){
		    count++;
		}
		
		if(count>= RUN_LENGTH_THRESHOLD )
			isJsCode = true;
		
		return isJsCode;
	}
	
	public String removeAllJavascriptCode(String input, boolean removeIframes) {
		
		String message = "";
		
		message = removeJavascriptCode(input);
		message = removeJavascriptCompressedCode(message);
		
		if(removeIframes)
			message = removeIframesCode(message);
		
		//TfsLOG.close();
		
		return message.replaceAll("\\s*$","");
	}
	
	
	public  String removeJSFromBody(String content) {
		
		String newsBody = content;
		
		List<String> classesProtectedList =new ArrayList<String>();
		
		if(protectedClasses!=null){
			classesProtectedList = Arrays.asList(protectedClasses.split("\\s*,\\s*"));
		}
		
		Document doc = Jsoup.parse(newsBody);
		doc.outputSettings().charset("UTF-8");	
		
		Elements elements;
		
		if (doc.select("lt-highlighter").size() > 0){
			elements = doc.select("lt-highlighter");
			for (Element element : elements) {
				element.remove();
			}
		}
		
		if(removeScriptsBody ){
			elements = doc.select("script");
			
			for (Element element : elements) {
				
				Element parent = element.parent();
				
				if(protectedClasses==null){
					LOG.error("Se remueve el script (protectedClasses==null): "+element.toString() + " - Usuario: "+user + " - path: "+pathFile);
					element.remove();
				}else{
				
					String parentClass = parent.attr("class");
					
					if(parentClass==null || (parentClass!=null && !classesProtectedList.contains(parentClass))){
						LOG.error("Se remueve el script (parentClass= "+parentClass+"): "+element.toString() + " - Usuario: "+user + " - path: "+pathFile);
						element.remove();
					}
				}
			}
		}
		
		if(removeIframesBody){
			elements = doc.select("iframe");
			
			for (Element element : elements) {
				
				Element parent = element.parent();
				
				if(protectedClasses==null){
					LOG.error("Se remueve el iframe (protectedClasses==null): "+element.toString() + " - Usuario: "+user + " - path: "+pathFile);
					element.remove();
				}else{
				
					String parentClass = parent.attr("class");
					
					if(parentClass==null || (parentClass!=null && !classesProtectedList.contains(parentClass))){
						LOG.error("Se remueve el iframe (parentClass= "+parentClass+"): "+element.toString() + " - Usuario: "+user + " - path: "+pathFile);
						element.remove();
					}
				}
			}
		}
		
		newsBody = doc.body().html();
		
		if(removeJsCompressBody)
			newsBody = removeJavascriptCompressedCode(newsBody);
		
		if(protectedClasses!=null){
			
			if(protectedClasses.indexOf("ck-instagram")>-1){
				newsBody = validateIframeProtectedClass(newsBody,"ck-instagram");
				newsBody = validateInstagram(newsBody);
			}
			
			if(protectedClasses.indexOf("ckeditor-fb")>-1){
				newsBody = validateIframeProtectedClass(newsBody,"ckeditor-fb");
				newsBody = validateFacebook(newsBody);
			}
			
			if(protectedClasses.indexOf("ckeditor-ifb")>-1){
				newsBody = validateScriptProtectedClass(newsBody,"ckeditor-ifb");
				newsBody = validateIframeFacebook(newsBody);
			}
			
			if(protectedClasses.indexOf("ck-twitter")>-1)
				newsBody = validateTwitter(newsBody);
			
			if(protectedClasses.indexOf("ck-tiktok")>-1)
				newsBody = validateTiktok(newsBody);
			
			if(protectedClasses.indexOf("ckeditor-poll")>-1){
				newsBody = validateScriptProtectedClass(newsBody,"ckeditor-poll");
				newsBody = validateIframeProtectedClass(newsBody,"ckeditor-poll");
			}
			
			if(protectedClasses.indexOf("ck-image-gallery")>-1){
				newsBody = validateScriptProtectedClass(newsBody,"ck-image-gallery");
				newsBody = validateIframeProtectedClass(newsBody,"ck-image-gallery");
			}
			
			if(protectedClasses.indexOf("ckeditor-comparationimg")>-1){
				newsBody = validateScriptProtectedClass(newsBody,"ckeditor-comparationimg");
				newsBody = validateIframeProtectedClass(newsBody,"ckeditor-comparationimg");
			}
			
			if(protectedClasses.indexOf("ck-audio-player")>-1){
				newsBody = validateScriptProtectedClass(newsBody,"ck-audio-player");
				newsBody = validateIframeProtectedClass(newsBody,"ck-audio-player");
			}
			
			if(protectedClasses.indexOf("ck-audio-gallery")>-1){
				newsBody = validateScriptProtectedClass(newsBody,"ck-audio-gallery");
				newsBody = validateIframeProtectedClass(newsBody,"ck-audio-gallery");
			}
			
			if(protectedClasses.indexOf("ck-video-player")>-1){
				newsBody = validateScriptProtectedClass(newsBody,"ck-video-player");
				newsBody = validateIframeProtectedClass(newsBody,"ck-video-player");
			}
			
			if(protectedClasses.indexOf("ck-video-gallery")>-1){
				newsBody = validateScriptProtectedClass(newsBody,"ck-video-gallery");
				newsBody = validateIframeProtectedClass(newsBody,"ck-video-gallery");
			}
			
			if(protectedClasses.indexOf("ck-youtube")>-1)
				newsBody = validateYoutube(newsBody);
			
			if(protectedClasses.indexOf("ck-pinterest")>-1)
				newsBody = validatePinterest(newsBody);
			
			if(protectedClasses.indexOf("ck-flickr")>-1)
				newsBody = validateFlickr(newsBody);
			
			//ck-storify - Cerro, no deberia usarse mas esta clase, por las dudas la limpiamos
			if(protectedClasses.indexOf("ck-storify")>-1){
				newsBody = validateScriptProtectedClass(newsBody,"ck-storify");
				newsBody = validateIframeProtectedClass(newsBody,"ck-storify");
			}
			
			if(protectedClasses.indexOf("ck-related-news")>-1){
				newsBody = validateScriptProtectedClass(newsBody,"ck-related-news");
				newsBody = validateIframeProtectedClass(newsBody,"ck-related-news");
			}
			
			if(protectedClasses.indexOf("ck-events")>-1){
				newsBody = validateScriptProtectedClass(newsBody,"ck-events");
				newsBody = validateIframeProtectedClass(newsBody,"ck-events");
			}
			
			if(protectedClasses.indexOf("ck-vods")>-1){
				newsBody = validateScriptProtectedClass(newsBody,"ck-vods");
				newsBody = validateIframeProtectedClass(newsBody,"ck-vods");
			}
			
			if(protectedClasses.indexOf("ck-recipe")>-1){
				newsBody = validateScriptProtectedClass(newsBody,"ck-recipe");
				newsBody = validateIframeProtectedClass(newsBody,"ck-recipe");
			}
			
			if(protectedClasses.indexOf("ck-trivias")>-1){
				newsBody = validateScriptProtectedClass(newsBody,"ck-trivias");
				newsBody = validateIframeProtectedClass(newsBody,"ck-trivias");
			}
			
			if(protectedClasses.indexOf("ck-playlist")>-1){
				newsBody = validateScriptProtectedClass(newsBody,"ck-playlist");
				newsBody = validateIframeProtectedClass(newsBody,"ck-playlist");
			}
		}
		
		//TfsLOG.close();
		
		return newsBody;
	}
	
	public static String validateFlickr(String message) {
		
		String cleanMessage = "";
		
		Document doc = Jsoup.parse(message);
		doc.outputSettings().charset("UTF-8");	
		
		Elements flickrElements = doc.select("div.ck-flickr");
		
		for (Element element : flickrElements) {
			
			String htmlCode = element.html();
			
			Pattern pattern = Pattern.compile("(<script.*?>.*?<\\/script>)");
			Matcher matcher = pattern.matcher(htmlCode);
			
			while(matcher.find())
			{
				String replaceCode = matcher.group(1);
				htmlCode = htmlCode.replace(replaceCode,"");
				LOG.info("Flick - Se reemplazó el siguiente código (seteamos el código de acuerdo a documentación): "+replaceCode);
			}
			
			element.html(htmlCode);
			
			pattern = Pattern.compile("(<iframe.*?>.*?<\\/iframe>)");
			matcher = pattern.matcher(htmlCode);
			
			while(matcher.find())
			{
				String replaceCode = matcher.group(1);
				htmlCode = htmlCode.replace(replaceCode,"");
				LOG.error("Flickr - Se removió el siguiente código: "+replaceCode + " - Usuario: "+user + " - path: "+pathFile);
			}
			
			htmlCode = htmlCode + "<script async src=\"//widgets.flickr.com/embedr/embedr.js\" charset=\"utf-8\"></script>";
			
			element.html(htmlCode);
			
		}
		
		cleanMessage = doc.body().html();
		
		return cleanMessage;
	}
	
	public static String validatePinterest(String message) {
		
		String cleanMessage = "";
		
		Document doc = Jsoup.parse(message);
		doc.outputSettings().charset("UTF-8");	
		
		Elements pinterestElements = doc.select("div.ck-pinterest");
		
		for (Element element : pinterestElements) {
			
			String htmlCode = element.html();
			
			Pattern pattern = Pattern.compile("(<script.*?>.*?<\\/script>)");
			Matcher matcher = pattern.matcher(htmlCode);
			
			while(matcher.find())
			{
				String replaceCode = matcher.group(1);
				htmlCode = htmlCode.replace(replaceCode,"");
				LOG.info("Pinterest - Se reemplazó el siguiente código (seteamos el código de acuerdo a documentación): "+replaceCode);
			}
			
			pattern = Pattern.compile("(<iframe.*?>.*?<\\/iframe>)");
			matcher = pattern.matcher(htmlCode);
			
			while(matcher.find())
			{
				String replaceCode = matcher.group(1);
				htmlCode = htmlCode.replace(replaceCode,"");
				LOG.error("Pinterest - Se removió el siguiente código: "+replaceCode + " - Usuario: "+user + " - path: "+pathFile);
			}
			
			htmlCode = htmlCode + "<script type=\"text/javascript\" async src=\"//assets.pinterest.com/js/pinit.js\" ></script>";
			
			element.html(htmlCode);
			
		}
		
		cleanMessage = doc.body().html();
		
		return cleanMessage;
	}
	
	public static String validateInstagram(String message) {
		
		String cleanMessage = "";
		
		Document doc = Jsoup.parse(message);
		doc.outputSettings().charset("UTF-8");	
		
		Elements instagram = doc.select("div.ck-instagram");
		
		for (Element element : instagram) {
			boolean replaceCode = false;
			String htmlCode = element.html();
			
			Pattern pattern = Pattern.compile("href=\"(.*?)\"");
			Matcher matcher = pattern.matcher(htmlCode);
			
			if (matcher.find())
			{
				String instagramHTML = matcher.group(1);
				String regex = "^(https?:\\/\\/)?((w{3}\\.)?)instagram\\.com\\/(([a-z\\d.]{5,})?)";
				Pattern patternHTML = Pattern.compile(regex);
				
				Matcher matcherHTML = patternHTML.matcher(instagramHTML);
				
				while(matcherHTML.find()){
					if(htmlCode.indexOf("script")>-1){
						String newScript = "<script async defer src=\"//platform.instagram.com/en_US/embeds.js\"></script>";
						
						Pattern patternScript = Pattern.compile("(<script.*?>.*?<\\/script>)");
						Matcher matcherScript = patternScript.matcher(htmlCode);
						
						while(matcherScript.find())
						{
						  LOG.info("Instagram - Se reemplaza el siguiente script (seteamos el código de acuerdo a documentación):"+matcherScript.group(1));
						}
							
						String newInstagramHTML = htmlCode.replaceAll("<script(.*)<\\/script>", newScript);
						element.html(newInstagramHTML);
					}
				}
				
			}else{
				replaceCode = true;
			}
			
			if(replaceCode){
				LOG.error("Instagram - Se remueve el siguiente código (código mal formado): "+element.toString() + " - Usuario: "+user + " - path: "+pathFile);
				element.remove();
			}
		}
		
		cleanMessage = doc.body().html();
		
		return cleanMessage;
	}
	
	public static String validateFacebook(String message) {
		
		String cleanMessage = "";
		
		Document doc = Jsoup.parse(message);
		doc.outputSettings().charset("UTF-8");	
		
		Elements facebook = doc.select("div.ckeditor-fb");
		
		for (Element element : facebook) {
			boolean replaceCode = false;
			String htmlCode = element.html();
			
			Pattern pattern = Pattern.compile("data-href=\"(.*?)\"");
			Matcher matcher = pattern.matcher(htmlCode);
			
			if (matcher.find())
			{
				String facebookHREF = matcher.group(1);
				String regex = "^(https?:\\/\\/)?((w{3}\\.)?)facebook\\.com\\/(([a-z\\d.]{5,})?)";
				Pattern patternHTML = Pattern.compile(regex);
				
				Matcher matcherHTML = patternHTML.matcher(facebookHREF);
				
				if (matcherHTML!= null && matcherHTML.find()){
					if(htmlCode.indexOf("script")>-1){
						String newFacebookHTML = htmlCode;
						
						String regexReplace = "<script.*src=\"(.*?)\".*";
						Pattern patternReplace = Pattern.compile(regexReplace);
						Matcher matcherReplace = patternReplace.matcher(newFacebookHTML);
						
						while(matcherReplace.find()){
							String codeToReplace =  matcherReplace.group(1);
						
							LOG.info("Facebook - Se reemplazó el siguiente código de (seteamos el código de acuerdo a documentación): "+codeToReplace);
							
							newFacebookHTML = newFacebookHTML.replaceAll(codeToReplace,"https://connect.facebook.net/es_ES/sdk.js#xfbml=1");
							
						}
						
						element.html(newFacebookHTML);
					}
					
				}else{
					replaceCode = true;
				}
				
			}else{
				replaceCode = true;
			}
			
			if(replaceCode){
				LOG.error("Facebook - Se remueve el siguiente código (código mal formado): "+element.toString()+ " - Usuario: "+user + " - path: "+pathFile);
				element.remove();
			}
		}
		
		cleanMessage = doc.body().html();
		
		return cleanMessage;
	}
	
	
	public static String validateIframeFacebook(String message) {
		
		String cleanMessage = "";
		
		Document doc = Jsoup.parse(message);
		doc.outputSettings().charset("UTF-8");	
		
		Elements facebook = doc.select("div.ckeditor-ifb");
		
		for (Element element : facebook) {
			boolean replaceCode = false;
			String htmlCode = element.html();
			
			Pattern pattern = Pattern.compile("src=\"(.*?)\"");
			Matcher matcher = pattern.matcher(htmlCode);
			
			int cont = 0;
			
			while(matcher.find())
			{
				cont++;
				
				String facebookHREF = matcher.group(1);
				String regex = "^(https?:\\/\\/)?((w{3}\\.))facebook\\.com\\/.*?";
				Pattern patternHTML = Pattern.compile(regex);
					
				Matcher matcherHTML = patternHTML.matcher(facebookHREF);
					
				if (matcherHTML== null || (matcherHTML!= null && !matcherHTML.find())){
					replaceCode = true;
				}
			}
			
			if(replaceCode || cont<1 ){
				LOG.error("Facebook Iframes - src mal formado - Se saca el elemento completo: "+element.toString()+ " - Usuario: "+user + " - path: "+pathFile);
				element.remove();
			}
		}
		
		cleanMessage = doc.body().html();
		
		return cleanMessage;
	}
	
	public static String validateYoutube(String message) {
		
		String cleanMessage = "";
		
		Document doc = Jsoup.parse(message);
		doc.outputSettings().charset("UTF-8");	
		
		Elements youtubeElements = doc.select("div.ck-youtube");
		
		for (Element element : youtubeElements) {
			
			boolean replaceCode = false;
			String htmlCode = element.html();
			
			Pattern pattern = Pattern.compile("(<script.*?>.*?<\\/script>)");
			Matcher matcher = pattern.matcher(htmlCode);
			
			while(matcher.find())
			{
				String stringReplace = matcher.group(1);
				LOG.error("Youtube - Se remueven los scripts: "+ stringReplace + " - Usuario: "+user + " - path: "+pathFile);
				htmlCode = htmlCode.replace(stringReplace,"");
			}
			
			element.html(htmlCode);
			
			pattern = Pattern.compile("<iframe.*?src=\\\"(.*?)\\\"");
			matcher = pattern.matcher(htmlCode);
			
			int cont = 0;
			
			while(matcher.find())
			{
				cont++;
				
				String youtubeSrc = matcher.group(1);
				String regex = "^(https?:\\/\\/)?((w{3}\\.)?)youtube\\.com\\/.*?";
				Pattern patternHTML = Pattern.compile(regex);
					
				Matcher matcherHTML = patternHTML.matcher(youtubeSrc);
					
				if (matcherHTML== null || (matcherHTML!= null && !matcherHTML.find())){
					replaceCode = true;
				}
			}
			
			
			if(replaceCode || cont<1){
				LOG.error("Youtube - src mal formado - Se saca el elemento completo: "+element.toString() + " - Usuario: "+user + " - path: "+pathFile);
				element.remove();
			}
		}
		
		cleanMessage = doc.body().html();
		
		return cleanMessage;
	}
	
	public static String validateTwitter(String message) {
		
		String cleanMessage = "";
		
		Document doc = Jsoup.parse(message);
		doc.outputSettings().charset("UTF-8");	
		
		Elements twitterElements = doc.select("div.ck-twitter");
		
		for (Element element : twitterElements) {
			
			String htmlCode = element.html();
			
			Pattern pattern = Pattern.compile("(<script.*?>.*?<\\/script>)");
			Matcher matcher = pattern.matcher(htmlCode);
			
			while(matcher.find())
			{
				String stringReplace = matcher.group(1);
				LOG.info("Twitter - Se reemplaza el siguiente script (seteamos el código de acuerdo a documentación): "+ stringReplace);
				htmlCode = htmlCode.replace(stringReplace,"");
			}
			
			pattern = Pattern.compile("(<iframe.*?>.*?<\\/iframe>)");
			matcher = pattern.matcher(htmlCode);
			
			while(matcher.find())
			{
				String stringReplace = matcher.group(1);
				LOG.error("Twitter - Se remueven los iframes: "+ stringReplace + " - Usuario: "+user + " - path: "+pathFile);
				htmlCode = htmlCode.replace(stringReplace,"");
			}
			
			htmlCode = htmlCode + "<script async src=\"https://platform.twitter.com/widgets.js\" charset=\"utf-8\"></script>";
			
			element.html(htmlCode);
			
		}
		
		cleanMessage = doc.body().html();
		
		return cleanMessage;
	}
	
	public static String validateTiktok(String message) {
		
		String cleanMessage = "";
		
		Document doc = Jsoup.parse(message);
		doc.outputSettings().charset("UTF-8");	
		
		Elements tiktokElements = doc.select("div.ck-tiktok");
		
		for (Element element : tiktokElements) {
			
			String htmlCode = element.html();
			
			Pattern pattern = Pattern.compile("(<script.*?>.*?<\\/script>)");
			Matcher matcher = pattern.matcher(htmlCode);
			
			while(matcher.find())
			{
				String stringReplace = matcher.group(1);
				LOG.info("Tiktok - Se reemplaza el siguiente script (seteamos el código de acuerdo a documentación): "+ stringReplace);
				htmlCode = htmlCode.replace(stringReplace,"");
			}
			
			pattern = Pattern.compile("(<iframe.*?>.*?<\\/iframe>)");
			matcher = pattern.matcher(htmlCode);
			
			while(matcher.find())
			{
				String stringReplace = matcher.group(1);
				LOG.error("Tiktok - Se remueven los iframes: "+ stringReplace + " - Usuario: "+user + " - path: "+pathFile);
				htmlCode = htmlCode.replace(stringReplace,"");
			}
			
			htmlCode = htmlCode + "<script async src=\"https://www.tiktok.com/embed.js\" ></script>";
			
			element.html(htmlCode);
			
		}
		
		cleanMessage = doc.body().html();
		
		return cleanMessage;
	}
	
	public static String validateScriptProtectedClass(String message, String className) {
		
		String cleanMessage = "";
		
		Document doc = Jsoup.parse(message);
		doc.outputSettings().charset("UTF-8");	
		
		Elements pluginElements = doc.select("div."+className);
		
		for (Element element : pluginElements) {
			
			String htmlCode = element.html();
			
			Pattern pattern = Pattern.compile("(<script.*?>.*?<\\/script>)");
			Matcher matcher = pattern.matcher(htmlCode);
			
			while(matcher.find())
			{
				String stringReplace = matcher.group(1);
				LOG.error("Validacion de scripts en clases protegidas - Clase: "+className+" - Se remueve el script: "+ stringReplace + " - Usuario: "+user + " - path: "+pathFile);
				htmlCode = htmlCode.replace(stringReplace,"");
			}
			
			element.html(htmlCode);
		}
		
		cleanMessage = doc.body().html();
		
		return cleanMessage;
	}
	
	public static String validateIframeProtectedClass(String message, String className) {
		
		String cleanMessage = "";
		
		Document doc = Jsoup.parse(message);
		doc.outputSettings().charset("UTF-8");	
		
		Elements pluginElements = doc.select("div."+className);
		
		for (Element element : pluginElements) {
			
			String htmlCode = element.html();
			
			Pattern  pattern = Pattern.compile("(<iframe.*?>.*?<\\/iframe>)");
			Matcher  matcher = pattern.matcher(htmlCode);
			
			while(matcher.find())
			{
				String stringReplace = matcher.group(1);
				LOG.error("Validacion de iframes en clases protegidas - Clase: "+className+" - Se remueve el iframe: "+ stringReplace + " - Usuario: "+user + " - path: "+pathFile);
				htmlCode = htmlCode.replace(stringReplace,"");
			}
			
			element.html(htmlCode);
		}
		
		cleanMessage = doc.body().html();
		
		return cleanMessage;
	}
	
	public void cleanResource( String resourceType ){
		
		try {
			CmsFile file = cmsObj.readFile(pathFile,CmsResourceFilter.ALL);
			
			CmsResourceUtils.forceLockResource(cmsObj,pathFile);
			
			CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmsObj, file);
			content.setAutoCorrectionEnabled(true);
			content.correctXmlStructure(cmsObj);
			
			CmsXmlContent cleanContent = cleanNewsContent(content);
			
			String fileEncoding = getFileEncoding(cmsObj, file.getRootPath());
			String decodedContent = cleanContent.toString();
			
	        try {
				file.setContents(decodedContent.getBytes(fileEncoding));
				cmsObj.writeFile(file);	
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
		} catch (CmsException e) {
			
			e.printStackTrace();
		}
		
		return;
	}
	
	protected String getFileEncoding(CmsObject cms, String filename) {

        try {
            return cms.readPropertyObject(filename, CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, true).getValue(
                OpenCms.getSystemInfo().getDefaultEncoding());
        } catch (CmsException e) {
            return OpenCms.getSystemInfo().getDefaultEncoding();
        }
    }
	
	protected CmsXmlContent cleanNewsContent(CmsXmlContent content){

	    
	    String bodyOrig = content.getValue("cuerpo", Locale.ENGLISH).getStringValue(cmsObj);
		String body =  removeJSFromBody(bodyOrig);
			
		content.getValue("cuerpo", Locale.ENGLISH).setStringValue(cmsObj, body);
	    
		String[] fieldsToRemoveJS = {"titulo","autor","imagenPrevisualizacion","imagenesFotogaleria","videoYouTube","videoEmbedded","videoFlash","audio","copete","volanta","resumen","custom1","custom2","custom3","custom4","custom5"};
		
	    
	    for (int i=0; i<fieldsToRemoveJS.length;i++){
			
			String fieldName = fieldsToRemoveJS[i];
			
			int indexCount = -1;
			String fieldToClean = "";
			String field = "";
				
			if(fieldName.equals("titulo")){   
				indexCount = content.getIndexCount("titulo[1]", Locale.ENGLISH);
				
				if(indexCount>=1){
					for (int j=1; j<=indexCount;j++){
						fieldToClean = content.getValue(fieldName+"["+j+"]", Locale.ENGLISH).getStringValue(cmsObj);
						field = removeAllJavascriptCode(fieldToClean,true);
						content.getValue(fieldName+"["+j+"]", Locale.ENGLISH).setStringValue(cmsObj, field);
					}
				}
			}else if(fieldName.equals("autor")){
				indexCount = content.getIndexCount("autor", Locale.ENGLISH);
				
				if(indexCount>=1){
					for (int j=1; j<=indexCount;j++){
						fieldToClean = content.getValue(fieldName+"["+j+"]/nombre", Locale.ENGLISH).getStringValue(cmsObj);
						field = removeAllJavascriptCode(fieldToClean,true);
						content.getValue(fieldName+"["+j+"]/nombre", Locale.ENGLISH).setStringValue(cmsObj, field);
						
						fieldToClean = content.getValue(fieldName+"["+j+"]/descripcion_autor", Locale.ENGLISH).getStringValue(cmsObj);
						field = removeAllJavascriptCode(fieldToClean,true);
						content.getValue(fieldName+"["+j+"]/descripcion_autor", Locale.ENGLISH).setStringValue(cmsObj, field);
						
						fieldToClean = content.getValue(fieldName+"["+j+"]/lugar", Locale.ENGLISH).getStringValue(cmsObj);
						field = removeAllJavascriptCode(fieldToClean,true);
						content.getValue(fieldName+"["+j+"]/lugar", Locale.ENGLISH).setStringValue(cmsObj, field);
					}
				}
				
			}else if(fieldName.equals("imagenPrevisualizacion") || fieldName.equals("imagenesFotogaleria")){
				
				indexCount = content.getIndexCount(fieldName, Locale.ENGLISH);
				
				if(indexCount>=1){
					for (int j=1; j<=indexCount;j++){
						
						if(content.getIndexCount(fieldName+"["+j+"]/fotografo", Locale.ENGLISH)>=1){
							fieldToClean = content.getValue(fieldName+"["+j+"]/fotografo", Locale.ENGLISH).getStringValue(cmsObj);
							field = removeAllJavascriptCode(fieldToClean,true);
							content.getValue(fieldName+"["+j+"]/fotografo", Locale.ENGLISH).setStringValue(cmsObj, field);
						}
						
						fieldToClean = content.getValue(fieldName+"["+j+"]/descripcion", Locale.ENGLISH).getStringValue(cmsObj);
						field = removeAllJavascriptCode(fieldToClean,true);
						content.getValue(fieldName+"["+j+"]/descripcion", Locale.ENGLISH).setStringValue(cmsObj, field);
						
						fieldToClean = content.getValue(fieldName+"["+j+"]/fuente", Locale.ENGLISH).getStringValue(cmsObj);
						field = removeAllJavascriptCode(fieldToClean,true);
						content.getValue(fieldName+"["+j+"]/fuente", Locale.ENGLISH).setStringValue(cmsObj, field);
					}
				}
				
			}else if(fieldName.equals("videoYouTube") || fieldName.equals("videoEmbedded") || fieldName.equals("videoFlash") || fieldName.equals("audio")){
				
				indexCount = content.getIndexCount(fieldName, Locale.ENGLISH);
				
				if(indexCount>=1){
					for (int j=1; j<=indexCount;j++){
						fieldToClean = content.getValue(fieldName+"["+j+"]/titulo", Locale.ENGLISH).getStringValue(cmsObj);
						field = removeAllJavascriptCode(fieldToClean,true);
						content.getValue(fieldName+"["+j+"]/titulo", Locale.ENGLISH).setStringValue(cmsObj, field);
						
						fieldToClean = content.getValue(fieldName+"["+j+"]/descripcion", Locale.ENGLISH).getStringValue(cmsObj);
						field = removeAllJavascriptCode(fieldToClean,true);
						content.getValue(fieldName+"["+j+"]/descripcion", Locale.ENGLISH).setStringValue(cmsObj, field);
						
						fieldToClean = content.getValue(fieldName+"["+j+"]/fuente", Locale.ENGLISH).getStringValue(cmsObj);
						field = removeAllJavascriptCode(fieldToClean,true);
						content.getValue(fieldName+"["+j+"]/fuente", Locale.ENGLISH).setStringValue(cmsObj, field);
						
						fieldToClean = content.getValue(fieldName+"["+j+"]/autor", Locale.ENGLISH).getStringValue(cmsObj);
						field = removeAllJavascriptCode(fieldToClean,true);
						content.getValue(fieldName+"["+j+"]/autor", Locale.ENGLISH).setStringValue(cmsObj, field);
					}
				}
			}else if(fieldName.equals("noticiaLista")){
				
				indexCount = content.getIndexCount(fieldName, Locale.ENGLISH);
				
				if(indexCount>=1){
					for (int j=1; j<=indexCount;j++){
						
						bodyOrig = content.getValue(fieldName+"["+j+"]/cuerpo", Locale.ENGLISH).getStringValue(cmsObj);
						body =  removeJSFromBody(bodyOrig);
							
						content.getValue(fieldName+"["+j+"]/cuerpo", Locale.ENGLISH).setStringValue(cmsObj, body);
					    
						String[] fieldsToRemoveJSList = {"titulo","imagenlista","videoYouTube","videoEmbedded","videoFlash"};
						
						for (int s=0; s<fieldsToRemoveJSList.length;s++){
							
							String fieldNameList = fieldsToRemoveJSList[s];
							
							if(fieldNameList.equals("titulo")){   
								indexCount = content.getIndexCount("titulo", Locale.ENGLISH);
								
								if(indexCount>=1){
										fieldToClean = content.getValue(fieldNameList, Locale.ENGLISH).getStringValue(cmsObj);
										field = removeAllJavascriptCode(fieldToClean,true);
										content.getValue(fieldNameList, Locale.ENGLISH).setStringValue(cmsObj, field);
								}
							}else if(fieldNameList.equals("imagenlista")){  
								
								indexCount = content.getIndexCount(fieldNameList, Locale.ENGLISH);
								
								if(indexCount>=1){
									for (int n=1; n<=indexCount;n++){
										
										if(content.getIndexCount(fieldName+"["+j+"]/"+fieldNameList+"["+n+"]/fotografo", Locale.ENGLISH)>=1){
											fieldToClean = content.getValue(fieldName+"["+j+"]/"+fieldNameList+"["+n+"]/fotografo", Locale.ENGLISH).getStringValue(cmsObj);
											field = removeAllJavascriptCode(fieldToClean,true);
											content.getValue(fieldName+"["+j+"]/"+fieldNameList+"["+n+"]/fotografo", Locale.ENGLISH).setStringValue(cmsObj, field);
										}
										
										fieldToClean = content.getValue(fieldName+"["+j+"]/"+fieldNameList+"["+n+"]/descripcion", Locale.ENGLISH).getStringValue(cmsObj);
										field = removeAllJavascriptCode(fieldToClean,true);
										content.getValue(fieldName+"["+j+"]/"+fieldNameList+"["+n+"]/descripcion", Locale.ENGLISH).setStringValue(cmsObj, field);
										
										fieldToClean = content.getValue(fieldName+"["+n+"]/fuente", Locale.ENGLISH).getStringValue(cmsObj);
										field = removeAllJavascriptCode(fieldToClean,true);
										content.getValue(fieldName+"["+j+"]/"+fieldNameList+"["+n+"]/fuente", Locale.ENGLISH).setStringValue(cmsObj, field);
									}
								}
								
							}else if(fieldNameList.equals("videoYouTube") || fieldNameList.equals("videoEmbedded") || fieldNameList.equals("videoFlash")){
								
								int indexCountList = content.getIndexCount(fieldNameList, Locale.ENGLISH);
								
								if(indexCountList>=1){
									for (int n=1; n<=indexCountList;n++){
										fieldToClean = content.getValue(fieldName+"["+j+"]/"+fieldNameList+"["+n+"]/titulo", Locale.ENGLISH).getStringValue(cmsObj);
										field = removeAllJavascriptCode(fieldToClean,true);
										content.getValue(fieldName+"["+j+"]/"+fieldNameList+"["+n+"]/titulo", Locale.ENGLISH).setStringValue(cmsObj, field);
										
										fieldToClean = content.getValue(fieldName+"["+j+"]/"+fieldNameList+"["+n+"]/descripcion", Locale.ENGLISH).getStringValue(cmsObj);
										field = removeAllJavascriptCode(fieldToClean,true);
										content.getValue(fieldName+"["+j+"]/"+fieldNameList+"["+n+"]/descripcion", Locale.ENGLISH).setStringValue(cmsObj, field);
										
										fieldToClean = content.getValue(fieldName+"["+j+"]/"+fieldNameList+"["+n+"]/fuente", Locale.ENGLISH).getStringValue(cmsObj);
										field = removeAllJavascriptCode(fieldToClean,true);
										content.getValue(fieldName+"["+j+"]/"+fieldNameList+"["+n+"]/fuente", Locale.ENGLISH).setStringValue(cmsObj, field);
										
										fieldToClean = content.getValue(fieldName+"["+j+"]/"+fieldNameList+"["+n+"]/autor", Locale.ENGLISH).getStringValue(cmsObj);
										field = removeAllJavascriptCode(fieldToClean,true);
										content.getValue(fieldName+"["+j+"]/"+fieldNameList+"["+n+"]/autor", Locale.ENGLISH).setStringValue(cmsObj, field);
									}
								}
							}
							
						}
					
					}
				}
				
			}else{
				indexCount = content.getIndexCount(fieldName, Locale.ENGLISH);
				
				if(indexCount>=1){
					for (int j=1; j<=indexCount;j++){
						fieldToClean = content.getValue(fieldName+"["+j+"]", Locale.ENGLISH).getStringValue(cmsObj);
						field = removeAllJavascriptCode(fieldToClean,true);
						content.getValue(fieldName+"["+j+"]", Locale.ENGLISH).setStringValue(cmsObj, field);
					}
				}
			}
			
			
	    }
		
		return content;
		
	} 
	
}
