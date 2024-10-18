package com.tfsla.diario.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.TfsJspTagLink;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.loader.I_CmsResourceStringDumpLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.imageVariants.ImageFinder;
import com.tfsla.utils.UrlLinkHelper;


public class TfsBodyFormatterHelper {

	protected static final Log LOG = CmsLog.getLog(TfsBodyFormatterHelper.class);
	private static CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();

	public static String formatAsFacebookInstantArticles(String content, CmsResource resource, CmsObject cms, ServletRequest request, PageContext pageContext) {

		Document doc = Jsoup.parse(content);
		doc.outputSettings().charset("UTF-8");		
		//Tomo todos los includes de redes sociales para formatarlos
		Elements elements = doc.select("div.ckeditor-fb");
		elements.addAll(doc.select("div.ck-flickr"));
		elements.addAll(doc.select("div.ck-instagram"));
		elements.addAll(doc.select("div.ck-pinterest"));
		elements.addAll(doc.select("div.ck-storify"));
		elements.addAll(doc.select("div.ck-twitter"));
		elements.addAll(doc.select("div.ck-tiktok"));
	
		TipoEdicion tEdicion  = null;
		TipoEdicionService tEService = new TipoEdicionService();
		try {
			tEdicion = tEService.obtenerTipoEdicion(cms,cms.getSitePath(resource));
		} catch (Exception e1) {
			LOG.error("No encuentra la publicacion ", e1);
		}
		String showIframe = config.getParam(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot(), String.valueOf(tEdicion.getId()), "videos", "showIframeFIA", "");
		String embedCode =  config.getParam(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot(), String.valueOf(tEdicion.getId()), "videos", "embedCode", "");
		
		elements.tagName("iframe");
		for (Element element : elements) {
			Attributes attributes = element.attributes();
			for (Attribute attribute : attributes)
				element.removeAttr(attribute.getKey());
		}

		for (Element element : elements) {
			Element figure = doc.createElement("figure");
			figure.addClass("op-interactive");

			figure.appendChild(element.clone());

			element.replaceWith(figure);
		}	
		// Considero el nuevo formato con el que puede venir el insert de facebook
		elements = doc.select("div.ckeditor-ifb");
		for (Element element : elements) {
			Element figure = doc.createElement("figure");
			figure.addClass("op-interactive");
			Element iframe = element.select("iframe").first();
			
			figure.appendChild(iframe);
			
			element.replaceWith(figure);
		}
		
		elements = doc.select("div.ck-jwplayer");
		for (Element element : elements) {
			Element figure = doc.createElement("figure");
			figure.addClass("op-interactive");
			figure.appendChild(element.clone());
			element.replaceWith(figure);
		}
		
		// Considero el nuevo formato con el que puede venir el insert de Vine
		elements = doc.select("div.ck-vine");
		for (Element element : elements) {
			Element figure = doc.createElement("figure");
			figure.addClass("op-interactive");
			Element iframe = element.select("iframe").first();
			
			figure.appendChild(iframe);
			
			element.replaceWith(figure);
		}
		
		//quito las span que se agregaron para estilado.
		doc.select("figure span.item").remove();

		String domain = UrlLinkHelper.getPublicationDomain(resource,cms);
		
		//Se agrega el atributo data-feedback a todas las imagenes
		elements = doc.select("figure.image");
		for (Element element : elements) {		
				String dataFeedback = "fb:likes, fb:comments";
				element.attr("data-feedback", dataFeedback);			
		}
		
		elements = doc.select("figure.image").select("img");
		for (Element element : elements) {
			String src = element.attr("src");
			if (src.startsWith("/")) {
				
				String imgPath = src;
				
				src = imgPath;
				
				if(imgPath.indexOf("/export")>-1){
					String ext = "";
					try {
						 ext = imgPath.substring(imgPath.lastIndexOf(".")+1);
					} catch (Exception ex) {
						LOG.error("FIA - Error en substring imagePath  imgPath.substring(imgPath.lastIndexOf('.')+1);" + imgPath, ex);
						
					}
					String[] p = imgPath.split("//."+ext);
				   
					if(p.length >1){
						String imgPath2 = "";
						try {
							imgPath2 = imgPath.substring(0,imgPath.lastIndexOf("."+ext+"_"))+"."+ext;
						} catch (Exception ex) {
							imgPath2 = p[0] + "." +ext;
							LOG.error("FIA - Error en substring  imgPath.lastIndexOf('.+ext+_') :" + imgPath + " Se realiza reemplazo por imgPath2", ex);
							//asumo que va la primera parte hasta el .ext
						
						}
						String[] parts = imgPath2.split("/export/sites/");
						
						try {
							src = parts[0] + "/" + parts[1].substring(parts[1].indexOf("/")+1);
						} catch (Exception ex) {
							LOG.error("FIA - Error en substring   parts[1].substring(parts[1].indexOf('/')+1) :" + parts[1], ex);
						}
					}
				} else if(imgPath.indexOf("?__scale")>-1){

					   String[] parts = imgPath.split("\\?__scale");
					              src = parts[0];
				}
				
				String size = element.attr("data-size");
				
				if(size==null || size.equals("")){
					
					try{
					    CmsProperty prop = cms.readPropertyObject(src, "image.size", false);      
					                size = (prop != null) ? prop.getValue() : null;
					} catch (CmsException e) {
						LOG.error("Error al buscar archivo: " + src, e);
					}
				}
					
				if(size!=null && !size.equals(""))
				{
					String[] ImgSize = size.split(",");
					String width = null;
					String height = null;
				    
				    if(ImgSize[0] != null) {
				       String[] w = ImgSize[0].split(":");
				            width = w[1];
				    }
				    
				    if(ImgSize[1] != null) {
				       String[] h = ImgSize[1].split(":");
				           height = h[1];
				    }
				    
				    if(width!=null && height !=null){
				    	
						src = TfsJspTagLink.linkTagAction(src + "?__scale=c:transparent,w:" + width +",h:" + height + ",t:3",request);
				    }
				}
				
				src = domain + src;
				
				element.attr("src", src);
			}
		}
		
		//Formateo las fotogalerias
		elements = doc.select("div.ck-image-gallery");
		for (Element element : elements) {
			//Elements elementImageGallery = element.select("figure[itemprop=\"associatedMedia\"]");
			Elements elementImageGallery = element.select("span[data-type=\"img\"]");
			Element slideShow = doc.createElement("figure");
			slideShow.addClass("op-slideshow");
			for (Element elementImage : elementImageGallery) {				
				Element figureImage = doc.createElement("figure");
				String dataFeedback = "fb:likes, fb:comments";
				figureImage.attr("data-feedback",dataFeedback);
				
				Element imgElement = doc.createElement("img");		
				//Element aHref = elementImage.select("a.fancybox").first();				
				imgElement.attr("src", domain + elementImage.attr("data-src"));			
				
				figureImage.appendChild(imgElement);
				
				/*Element img = elementImage.select("img").first();
				if(img != null){
					String imgDescription = img.attr("ck-description");
					if(imgDescription != null && imgDescription != ""){
						Element figCaption = doc.createElement("figcaption");				
						figCaption.appendText(imgDescription);
						
						figureImage.appendChild(figCaption);
					}
				}*/
				
				String imgDescription = elementImage.attr("description");
				if(imgDescription != null && imgDescription != ""){
					Element figCaption = doc.createElement("figcaption");				
					figCaption.appendText(imgDescription);
					
					figureImage.appendChild(figCaption);
				}
				
				slideShow.appendChild(figureImage);
			}
			element.replaceWith(slideShow);			
		}
		
		//Formateo el comparador de imagenes
		elements = doc.select("div.ckeditor-comparationimg");
		for (Element element : elements) {
					
			Element slideShow = doc.createElement("figure");
			slideShow.addClass("op-slideshow");
					
					Elements elementImageComparation = element.select("span[data-type=\"img\"]");
					
					for (Element elementImage : elementImageComparation) {				
						
						Element figureImage = doc.createElement("figure");
						String dataFeedback = "fb:likes, fb:comments";
						figureImage.attr("data-feedback",dataFeedback);
						
						Element imgElement = doc.createElement("img");		
						String src = elementImage.attr("data-src");
						
						if(src != null){
							imgElement.attr("src", domain + src);
							
						    figureImage.appendChild(imgElement);
						
							String imgDescription = elementImage.attr("description");
							
							if(imgDescription != null && imgDescription != ""){
								Element figCaption = doc.createElement("figcaption");				
								figCaption.appendText(imgDescription);
								
								figureImage.appendChild(figCaption);
							}
							
						    slideShow.appendChild(figureImage);
						
						}
					}
					element.replaceWith(slideShow);			
		}
		
		//Formateo los html Embed		
		elements = doc.select("div.ckeditor-em");
		for (Element element : elements) {
			Element figure = doc.createElement("figure");
			figure.addClass("op-interactive");
						
			Elements iframes = element.select("iframe");
			if(element.children().size()==1 && element.children().first().tagName().equals("iframe") ){
				figure.appendChild(iframes.first().clone());
			}else{
				Elements childElements = element.children();
				Element  iframe = doc.createElement("iframe");
				for(Element childElement : childElements){
					iframe.appendChild(childElement.clone());
				}			
				figure.appendChild(iframe);
			}
			element.replaceWith(figure);
		}
		
		//Formateo los video Brid	
		elements = doc.select("div.ckeditor-brid");
		for (Element element : elements) {
			Element figure = doc.createElement("figure");
				    figure.addClass("op-interactive");
								
			Elements iframes = element.select("iframe");
			if(element.children().size()==1 && element.children().first().tagName().equals("iframe") ){
					figure.appendChild(iframes.first().clone());
			}else{
					Elements childElements = element.children();
					Element  iframe = doc.createElement("iframe");
					for(Element childElement : childElements){
						iframe.appendChild(childElement.clone());
					}			
					figure.appendChild(iframe);
			}
			element.replaceWith(figure);
		}

		//Formateo los videos
		elements = doc.select("div.ck-video-player");
		for (Element element : elements) {
			
			//Check Video
			Elements videos = element.select("span[data-type=\"video\"]");			
			if (videos.size()==1 && videos != null) {
				Element videoCk = videos.first();
				Element figure = doc.createElement("figure").addClass("op-interactive");;
				Element video = doc.createElement("video");
				String dataType = videoCk.attr("video-type");
				if (dataType != null && dataType != "" && !dataType.equals("link")) {
					if (dataType.equals("youtube")) {
						Element iframe = doc.createElement("iframe");
						iframe.attr("width","560");
						iframe.attr("height","315");
						iframe.attr("allowfullscreen","");
						iframe.attr("frameborder","0");
						iframe.attr("src", "https://www.youtube.com/embed/" + videoCk.attr("youtubeid"));
						figure.appendChild(iframe);
					} else if(dataType.equals("embedded")) {
						boolean isJWPlayer = (videoCk.attr("code-embedded").indexOf("cdn.jwplayer.com/players") > -1 ) ? true : false;
						if (isJWPlayer) {
							String codeEmbedded = getRelative(videoCk.attr("code-embedded")).replaceAll("//cdn.jwplayer.com/players/","").replaceAll(".js","");
							String codeEmbeddedSpl[] = codeEmbedded.split("-");
							Element div = doc.createElement("div").addClass("ck-jwplayer");
							Element script = doc.createElement("script");
							div.attr("media-id",codeEmbeddedSpl[0]);
							div.attr("player-id",codeEmbeddedSpl[0]);
							div.attr("style","display: inline;");
							script.attr("src",videoCk.attr("code-embedded"));
							div.appendChild(script);
							figure.addClass("op-interactive");
							figure.appendChild(div);
						}else {
							Element iframe = doc.createElement("iframe");
							iframe.attr("allowfullscreen","");
							iframe.attr("frameborder","0");
							iframe.attr("src", videoCk.attr("code-embedded"));
							if (videoCk.attr("width") != null && !videoCk.attr("width").equals(""))
								iframe.attr("width",videoCk.attr("width"));
							else 
								iframe.attr("width","560");
							if (videoCk.attr("height") != null && !videoCk.attr("height").equals("") )
									iframe.attr("height",videoCk.attr("height"));
							else
								iframe.attr("height","315");
							//masterIframe.appendChild(iframe);
							figure.addClass("op-interactive");
							figure.appendChild(iframe);
						}
					}				
				}else{
					if (!showIframe.equals("true") || embedCode.equals("")) {
						Element source = doc.createElement("source");
						source.attr("type","video/mp4");
						source.attr("src",domain + videoCk.attr("data-src"));
						video.appendChild(source);				
						figure.attr("data-feedback", "fb:likes, fb:comments");
						figure.appendChild(video);	
							
						String description = videoCk.attr("descripcion");
						if(description !=null && description != ""){
							Element descriptionVideo = doc.createElement("figcaption");
							descriptionVideo.appendText(description);
							figure.appendChild(descriptionVideo);
						}
					} else {
						figure.addClass("op-interactive");
						String code = TfsVideoHelper.getVideoEmbedCode(videoCk.attr("data-src"), cms, String.valueOf(tEdicion.getId()));
						if (!code.equals("")){
							figure.append(code);
						}
						String description = videoCk.attr("descripcion");
						if(description !=null && description != ""){
							Element descriptionVideo = doc.createElement("figcaption");
							descriptionVideo.appendText(description);
							figure.appendChild(descriptionVideo);
						}
					}
				}
				element.replaceWith(figure);
			} else {
				for (Element videoACambiar : videos) {
					Element figure = doc.createElement("figure");
					if (!showIframe.equals("true") || embedCode.equals("")) {
							
						Element video = doc.createElement("video");
					
						Element source = doc.createElement("source");
						source.attr("type","video/mp4");
						source.attr("src",domain + videoACambiar.attr("data-src"));
						
						video.appendChild(source);				
						figure.attr("data-feedback", "fb:likes, fb:comments");
						figure.appendChild(video);	
							
						String description = videoACambiar.attr("descripcion");
						if(description !=null && description != ""){
							Element descriptionVideo = doc.createElement("figcaption");
							descriptionVideo.appendText(description);
							figure.appendChild(descriptionVideo);
						}
					} else {
						figure.addClass("op-interactive");
						String code = TfsVideoHelper.getVideoEmbedCode(videoACambiar.attr("data-src"), cms, String.valueOf(tEdicion.getId()));
						if (!code.equals("")){
							figure.append(code);
						}
						String description = videoACambiar.attr("descripcion");
						if(description !=null && description != ""){
							Element descriptionVideo = doc.createElement("figcaption");
							descriptionVideo.appendText(description);
							figure.appendChild(descriptionVideo);
						}
					}
					element.before(figure);
					
					//ver como agregar los elementos en conjuntopn    
				}
				element.remove();
			}
			
			
		}
		

		//Galeria de videos
		Elements videoGallery = doc.select("div.ck-video-gallery");
		for (Element element : videoGallery) {			
			Elements videoCk = element.select("span[data-type=\"video\"]");
			Element figureGallery = doc.createElement("span");
			figureGallery.addClass("item");
			for (Element el : videoCk) {
				Element figure = doc.createElement("figure");
				Element video = doc.createElement("video");				
				
				String dataType = el.attr("video-type");
				if(dataType != null && dataType != ""){
					if(dataType.equals("youtube")){
						Element iframe = doc.createElement("iframe");							
						iframe.attr("allowfullscreen","");
						iframe.attr("frameborder","0");
						iframe.attr("src", "https://www.youtube.com/embed/" + el.attr("youtubeid"));
						
						figure.addClass("op-interactive");
						figure.appendChild(iframe);
					}else if(dataType.equals("embedded")){
						Element masterIframe = doc.createElement("iframe");
						
						Element iframe = doc.createElement("iframe");
						iframe.attr("allowfullscreen","");
						iframe.attr("frameborder","0");
						iframe.attr("src", el.attr("code-embedded"));
						
						masterIframe.appendChild(iframe);
						
						figure.addClass("op-interactive");
						figure.appendChild(masterIframe);
					}else if(dataType.equals("link")){
						if (!showIframe.equals("true") || embedCode.equals("")) {
								
							Element source = doc.createElement("source");
							source.attr("type","video/mp4");
							source.attr("src",domain + el.attr("data-src"));
							
							video.appendChild(source);	
							figure.attr("data-feedback", "fb:likes, fb:comments");
							figure.appendChild(video);	
							
							String description = videoCk.attr("descripcion");
							if(description !=null && description != ""){
								Element descriptionVideo = doc.createElement("figcaption");
								descriptionVideo.appendText(description);
								figure.appendChild(descriptionVideo);
							}
						} else {
							figure.addClass("op-interactive");
							String code = TfsVideoHelper.getVideoEmbedCode(videoCk.attr("data-src"), cms, String.valueOf(tEdicion.getId()));
							if (!code.equals("")){
								figure.append(code);
							}
							String description = videoCk.attr("descripcion");
							if(description !=null && description != ""){
								Element descriptionVideo = doc.createElement("figcaption");
								descriptionVideo.appendText(description);
								figure.appendChild(descriptionVideo);
							}
						}	
					}
					
				}else{			
					if (!showIframe.equals("true") || embedCode.equals("")) {
							
						Element source = doc.createElement("source");
						source.attr("type","video/mp4");
						source.attr("src",domain + el.attr("data-src"));
						
						video.appendChild(source);	
						figure.attr("data-feedback", "fb:likes, fb:comments");
						figure.appendChild(video);	
						
						String description = videoCk.attr("descripcion");
						if(description !=null && description != ""){
							Element descriptionVideo = doc.createElement("figcaption");
							descriptionVideo.appendText(description);
							figure.appendChild(descriptionVideo);
						}
					} else {
						figure.addClass("op-interactive");
						String code = TfsVideoHelper.getVideoEmbedCode(videoCk.attr("data-src"), cms, String.valueOf(tEdicion.getId()));
						if (!code.equals("")){
							figure.append(code);
						}
						String description = videoCk.attr("descripcion");
						if(description !=null && description != ""){
							Element descriptionVideo = doc.createElement("figcaption");
							descriptionVideo.appendText(description);
							figure.appendChild(descriptionVideo);
						}
					}
						
				}				
				if(figureGallery == null){
					figureGallery = figure;
				}else{
					figureGallery.appendChild(figure);					
				}				
			}	
			element.replaceWith(figureGallery);
			element.select("span.item").remove();
		}
		
		// Hasta solucionar problema con imagen previa para audios, se sacan los audios del cuerpo
		doc.select("div.ck-audio-player").remove();
		doc.select("div.ck-audio-list").remove();
		doc.select("div.ck-audio-gallery").remove();
		
		//Formateo los audios
		/*elements = doc.select("div.ck-audio-player");
		for (Element element : elements) {
					Element figure = doc.createElement("figure");
					
					Element imagenAudioCk = element.select("span[data-type=\"img\"]").first();
					
					if(imagenAudioCk != null ){
						if (!imagenAudioCk.attr("data-src").equals("")){
							Element imgAudio = doc.createElement("img");
							imgAudio.attr("src",domain + imagenAudioCk.attr("data-src"));
				
							figure.appendChild(imgAudio);
						}
					}
					
					Element audio = doc.createElement("audio");
					figure.appendChild(audio);

					Element audioCk = element.select("span[data-type=\"audio\"]").first();
					
					if(audioCk != null){
						Element source = doc.createElement("source");
						source.attr("src",domain + audioCk.attr("data-src"));
						
						audio.appendChild(source);
						
						audio.attr("title",audioCk.attr("data-title"));
					}	
					element.replaceWith(figure);
		}*/
		
		//Formateo listas de audios
		/*elements = doc.select("div.ck-audio-list");
		for (Element element : elements) {
				Element figure = doc.createElement("figure");
				figure.attr("class","op-slideshow");
					
				int itemsList = element.select("span[data-type=\"audio\"]").size();
				
				for(int i=0; i< itemsList; i++){
					Element figureItem = doc.createElement("figure");
					figure.appendChild(figureItem);
					
					Element imagenAudioCk = null;
					
					if(element.select("span[data-type=\"img\"]").size()>0)
					  imagenAudioCk = element.select("span[data-type=\"img\"]").get(i);
					
					if(imagenAudioCk != null ){
						
						if (!imagenAudioCk.attr("data-src").equals("")){
							Element imgAudio = doc.createElement("img");
							imgAudio.attr("src",domain + imagenAudioCk.attr("data-src"));
				
							figureItem.appendChild(imgAudio);
						}
					}
					
					Element audio = doc.createElement("audio");
					figureItem.appendChild(audio);
					
					Element audioCk = element.select("span[data-type=\"audio\"]").get(i);
					
					if(audioCk != null){
						Element source = doc.createElement("source");
						source.attr("src",domain + audioCk.attr("data-src"));
						
						audio.appendChild(source);
					}	
				}
					
				element.replaceWith(figure);
		}*/
		
		//Formateo galerias de audios que se comportan igual a las listas
		/*elements = doc.select("div.ck-audio-gallery");
		for (Element element : elements) {
				Element figure = doc.createElement("figure");
				figure.attr("class","op-slideshow");
							
				int itemsList = element.select("span[data-type=\"audio\"]").size();
						
				for(int i=0; i< itemsList; i++){
						Element figureItem = doc.createElement("figure");
						figure.appendChild(figureItem);
							
						Element imagenAudioCk = element.select("span[data-type=\"img\"]").get(i);
							
						if(imagenAudioCk != null ){
								
							if (!imagenAudioCk.attr("data-src").equals("")){
									Element imgAudio = doc.createElement("img");
									imgAudio.attr("src",domain + imagenAudioCk.attr("data-src"));
						
									figureItem.appendChild(imgAudio);
							}
					    }
							
					Element audio = doc.createElement("audio");
					figureItem.appendChild(audio);
							
					Element audioCk = element.select("span[data-type=\"audio\"]").get(i);
							
					if(audioCk != null){
							Element source = doc.createElement("source");
							source.attr("src",domain + audioCk.attr("data-src"));
								
							audio.appendChild(source);
					}	
				}
							
				element.replaceWith(figure);

		}*/
		
		//No se agrega el elemento Iframe a los videos youtube
		elements = doc.select("div.ck-youtube");
		
		for (Element element : elements) {
			Element figure = doc.createElement("figure");
			figure.addClass("op-interactive");
			
			Elements childElements = element.select("iframe");
			
			for(Element childElement : childElements){
				figure.appendChild(childElement.clone());
			}			
			
			element.replaceWith(figure);
		}
		//quito las span que se agregaron para estilado.
		doc.select("figure span.item").remove();
				
		//Quito las encuestas:
		doc.select("div.ckeditor-poll").remove();
		
		
		Elements iframes = doc.select("iframe");
		for (Element element : iframes) {
			
			boolean isRaw = false;
			
			Element padre = element.parent();
			if(padre.attr("class")!= null && padre.attr("class").equals("ck-raw"))
				isRaw = true;
			
			if(!isRaw){
				boolean figureParent = padre.tagName().equals("figure") && (padre.hasClass("op-interactive") || padre.hasClass("op-interactive"));
				while (!(figureParent) && !padre.tagName().equals("body") ) {
					padre = padre.parent();
					figureParent = padre.tagName().equals("figure") && 
									(padre.hasClass("op-interactive") || padre.hasClass("op-interactive"));
				}	
				
				if ( !figureParent) {	
					Element figure = doc.createElement("figure");
					figure.addClass("op-interactive");
					figure.appendChild(element.clone());
					element.replaceWith(figure);
				}
			}
		}
		
		Elements eventos = doc.select("div.ck-events");
		getElementStyle (eventos,cms, pageContext,"events", "eventViewFIA.jsp");
		
		Elements recetas = doc.select("div.ck-recipe");
		getElementStyle (recetas,cms, pageContext,"recipes", "recipeViewFIA.jsp");
		
		Elements vods = doc.select("div.ck-vods");
		getElementStyle (vods,cms, pageContext,"vods", "vodViewFIA.jsp");
		
		Elements playlists = doc.select("div.ck-playlist");
		getElementStyle (playlists,cms, pageContext,"playlist", "playlistViewFIA.jsp");
		
		Elements trivias = doc.select("div.ck-trivias");
		getElementStyle (trivias,cms, pageContext,"trivias", "triviasViewFIA.jsp");
		
		
		/*for (Element element : eventos) {
			String eventPath = "";
			if (element.hasAttr("data-path")){
				eventPath = element.attr("data-path");
			}
			try {
				CmsFile eventoFile = cms.readFile(eventPath);
				CmsXmlContent contenido = CmsXmlContentFactory.unmarshal(cms, eventoFile);
				String estilo = contenido.getStringValue(cms, "estilo[1]", cms.getRequestContext().getLocale());
				//saco el path
				element.removeAttr("data-path");
				if (estilo == null || estilo.equals("")){
					element.remove();
				} else {
					String pathToGet = "/system/modules/com.tfsla.diario.newsTags/elements/events/"+estilo+ "/eventViewFIA.jsp";
					try {
						String contentToReplace = includeNoCache(pathToGet,"", pageContext, eventPath);
						element.html(contentToReplace);
					} catch (JspException e) {
						LOG.error("FIA - Error al buscar la jsp del evento: " + pathToGet + ". Se saca", e);
						element.remove();
					}
				}
			} catch (CmsException e) {
				LOG.error("FIA - Error al buscar el evento: " + eventPath + ". Se saca", e);
				element.remove();
			}
			
		}
		*/
		Elements relatedNews = doc.select("div.ck-related-news");
		String template =  config.getParam(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot(), String.valueOf(tEdicion.getId()), "ckeditor", "related-news-template-FIA", "");
		
		for (Element element : relatedNews) {
			
			if(template == null || template.equals("")){
				element.remove();
			}else{
				String relatedNewsPaths = "";
				
				int itemsList = element.select("span[data-type=\"news\"]").size();
				
				for(int i=0; i< itemsList; i++){
					Element relatedNewsItem = element.select("span[data-type=\"news\"]").get(i);
					if(relatedNewsItem != null ){
						if (!relatedNewsItem.attr("data-src").equals("")){
							relatedNewsPaths = relatedNewsPaths+","+relatedNewsItem.attr("data-src");
						}
					}
				}
				
				if(!relatedNewsPaths.equals(""))
					relatedNewsPaths = relatedNewsPaths.substring(1);
				
				try {
					String contentToReplace = includeNoCache(template,"", pageContext, relatedNewsPaths);
					element.html(contentToReplace);
				} catch (JspException e) {
					LOG.error("FIA - Error al insertar las noticias relacionadas. Se sacan", e);
					element.remove();
				}
			}
			
		}

		content = doc.body().html();

		//Quito los parrafos vacios
		content = content.replaceAll("<p\\s*></p>", "");

		return content;
	}
	
	public static String formatAsAMP(String content, CmsResource resource, CmsObject cms, ServletRequest request, PageContext pageContext)  {
		
		TipoEdicion tEdicion  = null;
		TipoEdicionService tEService = new TipoEdicionService();
		try {
			tEdicion = tEService.obtenerTipoEdicion(cms,cms.getSitePath(resource));
		} catch (Exception e1) {
			LOG.error("No encuentra la publicacion ", e1);
		}
		
		String  ampImageWidth = "";
		if (tEdicion != null )
			ampImageWidth = config.getParam(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot(), String.valueOf(tEdicion.getId()), "body-formats", "ampImageWidth");
			
		String showIframe = config.getParam(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot(), String.valueOf(tEdicion.getId()), "videos", "showIframeAMP", "");
		String embedCode =  config.getParam(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot(), String.valueOf(tEdicion.getId()), "videos", "embedCode", "");
		String showPolls = config.getParam(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot(), String.valueOf(tEdicion.getId()), "polls", "showInAmp", "");
		String ampImageGalleryVersion = config.getParam(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot(), String.valueOf(tEdicion.getId()), "body-formats", "ampImageGalleryVersion","v1");
		
		
		Document doc = Jsoup.parse(content);
		//Busco todos los tags con atributo style para eliminarlo
		Elements styleInline = doc.select("[^style]");
		styleInline.removeAttr("style");
		
		//Busco todos los tags con atributo onclick para eliminarlo
		Elements onclickInline = doc.select("[^onclick]");
		onclickInline.removeAttr("onclick");
		//busco los elementos font y los elimino
		doc.select("font").remove();
		
		
		//Busco todos los tags a y si no tienen target _blank lo agrego
		Elements links = doc.select("a");
		for (Element element : links) {
			
			if (!element.hasAttr("target")){
				element.attr ("target", "_blank");
			} else if (element.hasAttr("target") && (element.attr("target").equals("") || element.attr("target").equals("_top") || element.attr("target").equals("_parent"))) {
				element.attr("target", "_blank");
			}
			
			if (element.hasAttr("rel") && element.attr("rel").equals("")){
				element.removeAttr("rel");
			}
			
			/*if (!element.hasAttr("href") || element.attr("href").equals("")  || element.attr("href").equals("https://")  || element.attr("href").equals("http://") )
				element.remove();
			else if (!element.attr("href").equals("") ){
				String href = element.attr("href");
				//String pattern="^(http(s)?://)?(www\\.)?([\\w$-\\_+!*'(),%]+(\\.)?)+[‌​\\w]{2,63}/?$";
				String pattern = "^(http(s)?://)?(/(([\\w@:%+~#=])+)|(([\\w@:%+~#=])+([\\./])))+([-\\w@:%+\\.~#?&/=])*$";
				if (!href.matches(pattern))
					element.remove();
			} */
		}
		//elimino los elementos que no se tienen en cuenta
		if ( !showPolls.equals("true") && doc.select("[class^=ckeditor-poll]").size() > 0)
			doc.select("[class^=ckeditor-poll]").remove();
		if (doc.select("div.ck-storify").size() > 0)
			doc.select("div.ck-storify").remove();
		if (doc.select("div.ck-flickr").size() > 0)
			doc.select("div.ck-flickr").remove();
		
		// Elimino los elementos agregados por spellcheck
		if (doc.select("lt-highlighter").size() > 0)
			doc.select("lt-highlighter").remove();
		
		//elimino los span vacios
		/*Elements spans = doc.select("span");
		for (Element span : spans) {
			if (span.children().size() == 0 && span.attributes().size() == 0)
				span.remove(); 
		}*/
	
		//elimino los div vacios
		Elements divs = doc.select("div");
		for (Element div : divs) {
			if (div.children().size() == 0 && !div.hasText() )
				div.remove();
		}
		
		//embed
		Elements embeds  = doc.select("div.ckeditor-em");
		for (Element embed : embeds) {
			if (embed.select("iframe").size()>0){
				try {
					Elements iframes = embed.select("iframe");
					
					Element oldIframe =  null;
					
					for (Element iframe: iframes){
						
						Element newIframe =  new Element(Tag.valueOf("amp-iframe"), "");
						newIframe.attr("layout", "responsive");
						
						if ( iframe.attr("width").equals("") || iframe.attr("width").contains("%"))
							newIframe.attr("width", "640");
						else
							newIframe.attr("width", iframe.attr("width"));	
						if ( iframe.attr("height").equals("") || iframe.attr("height").contains("%"))
							newIframe.attr("height","360");
						else
							newIframe.attr("height",iframe.attr("height"));
						
						newIframe.attr("src", getHttps(iframe.attr("src")));
						newIframe.attr("sandbox", "allow-scripts allow-same-origin");
						newIframe.attr("frameborder", "0");
						
						if(oldIframe != null)
						{
							oldIframe.after(newIframe);
						}else{
							embed.replaceWith(newIframe);
						}
						
						oldIframe = newIframe;
					}
					
				} catch (Exception ex) {
					LOG.error ("Error al transformar embeddeds en formato amp para la nota: " + resource.getRootPath(), ex);
					//embed.remove();
				}
			} else if( embed.select("script").size()>0 && embed.select("script").contains(".brid") || embed.select("div.brid").size()>0){
				
				// Videos brid embedded con plugin html
				
				try {
					String bridPartner =  config.getParam(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot(), String.valueOf(tEdicion.getId()), "ckeditor", "brid-partner", "");
					
					Elements scripts = embed.select("script");
					
					boolean isScript=false;
					
					for (Element script: scripts) {
						String contentScript = script.html();
						
						String dataPartner = bridPartner;
			        	String dataPlayer = "";
			        	String dataVideo="";
			        	String bridWidth="480";
			        	String bridHeight="270";
			        	
			        	Pattern REGEX_ID = Pattern.compile("\"id\":\"(.*?)\"");
						Matcher matcherID = REGEX_ID.matcher(contentScript);
						
						if(matcherID.find()){
							dataPlayer = matcherID.group(1);
						}
						
						Pattern REGEX_VIDEO = Pattern.compile("\"video\":\"(.*?)\"");
						Matcher matcherVideo = REGEX_VIDEO.matcher(contentScript);
						
						if(matcherVideo.find()){
							dataVideo = matcherVideo.group(1);
						}
						
						Pattern REGEX_H = Pattern.compile("\"height\":\"(.*?)\"");
						Matcher matcherH = REGEX_H.matcher(contentScript);
						
						if(matcherH.find() && matcherH.group(1)!= null && !matcherH.group(1).equals("")){
							bridHeight = matcherH.group(1);
						}
						
						Pattern REGEX_W = Pattern.compile("\"width\":\"(.*?)\"");
						Matcher matcherW = REGEX_W.matcher(contentScript);
						
						if(matcherW.find() && matcherW.group(1)!= null && !matcherW.group(1).equals("")){
							bridWidth = matcherW.group(1);
						}
						
						if(!dataPlayer.equals("") && !dataVideo.equals("")){
							Element newBrid =  new Element(Tag.valueOf("amp-brid-player"), "");
					        	newBrid.attr("layout", "responsive");
					        	newBrid.attr("data-partner", dataPartner);
					        	newBrid.attr("data-player", dataPlayer);
					        	newBrid.attr("data-video", dataVideo);
					        	newBrid.attr("width", bridWidth);
					        	newBrid.attr("height", bridHeight);
					        	
					        	embed.replaceWith(newBrid);
					        	
					        	isScript = true;
						}
					}
					
					if(!isScript) 
						embed.remove();
					
				} catch (Exception ex) {
					LOG.error ("Error al transformar videos brid embeddeds en formato amp para la nota: " + resource.getRootPath(), ex);
					embed.remove();
				}
				
			}else{
				embed.remove();
			}
			
		}
		
		//Videos brid embeds con plugin BRID
		Elements bridEmbeds  = doc.select("div.ckeditor-brid");
		for (Element bridEmbed : bridEmbeds) {
			
			if( bridEmbed.select("script").size()>0 && bridEmbed.select("script").contains(".brid") || bridEmbed.select("div.brid").size()>0){
						
				try {
					String bridPartner =  config.getParam(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot(), String.valueOf(tEdicion.getId()), "ckeditor", "brid-partner", "");
					
					Elements scripts = bridEmbed.select("script");
					
					boolean isScript=false;
								
						for (Element script: scripts){
							String contentScript = script.html();
							
							String dataPartner = bridPartner;
				        	String dataPlayer = "";
				        	String dataVideo="";
				        	String bridWidth="480";
				        	String bridHeight="270";
				        	
				        	Pattern REGEX_ID = Pattern.compile("\"id\":\"(.*?)\"");
							Matcher matcherID = REGEX_ID.matcher(contentScript);
							
							if(matcherID.find()){
								dataPlayer = matcherID.group(1);
							}
							
							Pattern REGEX_VIDEO = Pattern.compile("\"video\":\"(.*?)\"");
							Matcher matcherVideo = REGEX_VIDEO.matcher(contentScript);
							
							if(matcherVideo.find()){
								dataVideo = matcherVideo.group(1);
							}
							
							Pattern REGEX_H = Pattern.compile("\"height\":\"(.*?)\"");
							Matcher matcherH = REGEX_H.matcher(contentScript);
							
							if(matcherH.find() && matcherH.group(1)!= null && !matcherH.group(1).equals("")){
								bridHeight = matcherH.group(1);
							}
							
							Pattern REGEX_W = Pattern.compile("\"width\":\"(.*?)\"");
							Matcher matcherW = REGEX_W.matcher(contentScript);
							
							if(matcherW.find() && matcherW.group(1)!= null && !matcherW.group(1).equals("")){
								bridWidth = matcherW.group(1);
							}
							    
							if(!dataPlayer.equals("") && !dataVideo.equals("")){
								Element newBrid =  new Element(Tag.valueOf("amp-brid-player"), "");
							        newBrid.attr("layout", "responsive");
							        newBrid.attr("data-partner", dataPartner);
							        newBrid.attr("data-player", dataPlayer);
							        newBrid.attr("data-video", dataVideo);
							        newBrid.attr("width", bridWidth);
							        newBrid.attr("height", bridHeight);
							        	
							        bridEmbed.replaceWith(newBrid);
							        
							        isScript = true;
							}
						}
						
						if(!isScript) 
							bridEmbed.remove();
							
				} catch (Exception ex) {
					LOG.error ("Error al transformar videos brid en formato amp de la nota: "  + resource.getRootPath(), ex);
					bridEmbed.remove();
				}
						
			}else{
					bridEmbed.remove();
			}
					
		}
		
		//Galeria de imagenes - ampImageGalleryVersion
		Elements imageGallerys = doc.select ("[class^=ck-image-gallery]");
		
		if(ampImageGalleryVersion.trim().equals("v2")){
			for (Element element : imageGallerys) {
				Element carousel = new Element(Tag.valueOf("amp-carousel"), "");
				carousel.attr("id","carouselWithPreview");
				carousel.attr("type", "slides");
				carousel.attr("on", "slideChange:carouselWithPreviewSelector.toggle(index=event.index, value=true)");
				carousel.attr("layout", "responsive");
				
				try {
					Elements images = element.children().select("span[data-type=\"img\"]");
					getImageGallery_v2(images, resource, cms, ampImageWidth, pageContext, carousel);
					if (carousel.children().size()>0) {
						
						Element selector = new Element(Tag.valueOf("amp-selector"), "");
						selector.attr("id","carouselWithPreviewSelector");
						selector.attr("class", "carousel-preview");
						selector.attr("on", "select:carouselWithPreview.goToSlide(index=event.targetOption)");
						selector.attr("layout", "container");
						
						getImageGallerySelector(images, ampImageWidth, pageContext, selector);
						
						Element divCarousel = new Element(Tag.valueOf("div"), "");
						
						divCarousel.appendChild(carousel);
						divCarousel.appendChild(selector);
						
						replaceDiv(element, divCarousel);
						
					}else {
						element.remove();
					}
				} catch (Exception ex) {
					LOG.error ("Error al transformar galeria de imagenes en formato amp de la nota: "  + resource.getRootPath(), ex);
					element.remove();
				}
			}
		}else {
			for (Element element : imageGallerys) {
				Element carousel = new Element(Tag.valueOf("amp-carousel"), "");
				carousel.attr("type", "slides");
				carousel.attr("layout", "responsive");
				try {
					Elements images = element.children().select("span[data-type=\"img\"]");
					getImageGallery(images, resource, cms,ampImageWidth, pageContext, carousel);
					
					if (carousel.children().size()>0)
						replaceDiv(element, carousel);
					else
						element.remove();
				} catch (Exception ex) {
					LOG.error ("Error al transformar galeria de imagenes en formato amp de la nota: "  + resource.getRootPath(), ex);
					element.remove();
				}
			}
		}
		
		//Comparador de imagenes - 
		Elements imageComparators = doc.select ("div.ckeditor-comparationimg");
		for (Element element : imageComparators) {
			Element slider = new Element(Tag.valueOf("amp-image-slider"), "");
			//carousel.attr("type", "slides");
			slider.attr("layout", "responsive");
			slider.attr("width",ampImageWidth);
			slider.attr("height","360");
			try {
				Elements imagesComparator = element.children().select("span[data-type=\"img\"]");
				for (Element image: imagesComparator) {
					Element newImage = new Element(Tag.valueOf("amp-img"), "");
					getImageForImageComparation (cms, resource, image,newImage, ampImageWidth,pageContext);
					if (!newImage.attr("src").equals(""))
						slider.appendChild(newImage);
				}
				
				if (slider.children().size()>0) 
					replaceDiv(element, slider);
				else
					element.remove();
			} catch (Exception ex) {
				LOG.error ("Error al transformar comparador de imagenes en formato amp de la nota: " + resource.getRootPath(), ex);
				element.remove();
			}
		}
		
		//Imagenes
		Elements images = doc.select("img");
		getAmpImages(resource, images, cms, ampImageWidth, pageContext);
	
		
		//Galeria de videos
		Elements videoGallery = doc.select("div.ck-video-gallery");
		for (Element element : videoGallery) {
			try {
				Elements spansVideo = element.select("span[data-type=\"video\"]");
				Element carousel = new Element(Tag.valueOf("amp-carousel"), "");
				carousel.attr("type", "slides");
				carousel.attr("layout", "responsive");
				for (Element span : spansVideo ) {
					Element video = null;
					if (span.attr("video-type").equals("youtube")){
						video= new Element(Tag.valueOf("amp-youtube"), "");
						getYoutubeVideo(span, video);
					} else if (span.attr("video-type").equals("link")){
						if (!showIframe.equals("true") || embedCode.equals("")) {
							video = new Element(Tag.valueOf("amp-video"), "");
							getJWPlayerData(span, video, cms, request);
						} else {
							video = new Element(Tag.valueOf("amp-iframe"), "");
							getVideoIframe(span, video, cms, String.valueOf(tEdicion.getId()));
						}
					} else if (span.attr("video-type").equals("embedded")){
						video = new Element(Tag.valueOf("amp-iframe"), "");
						getVideoEmbedded(span,video); 
					}	
					if (video != null){
						carousel.appendChild(video);
						if (carousel.attr("width").equals(""))
							carousel.attr("width",video.attr("width"));
				        if (carousel.attr("height").equals(""))
				        	carousel.attr("height",video.attr("height"));
				
					}
				}
				if (carousel.children().size()>0)
					replaceDiv(element, carousel);
				else
					element.remove();
			} catch (Exception ex) {
				LOG.error ("Error al transformar galeria de videos en formato amp de la nota:"  + resource.getRootPath(), ex);
				element.remove();
			}
		}

		// videos
		Elements videoPlayer = doc.select ("[class^=ck-video-player]");
		for (Element element : videoPlayer) {
			try {
				Elements spanVideos = element.select("span[data-type=\"video\"]");
				//no son lista
				if (spanVideos.size()==1){
					Element videoACambiar = spanVideos.first();
					Element video = null;
					Element videoDiv = doc.createElement("div").addClass("ck-video-player");
						
					if (videoACambiar.attr("video-type").equals("youtube")){
						video= new Element(Tag.valueOf("amp-youtube"), "");
						getYoutubeVideo(videoACambiar, video);
						getMediaDiv(element,video,videoDiv,"video__title");	
						replaceDiv(element, videoDiv);
					} else if (videoACambiar.attr("video-type").equals("embedded")) {
						boolean isJWPlayer = (videoACambiar.attr("code-embedded").indexOf("cdn.jwplayer.com/players") > -1 ) ? true : false;
						if (isJWPlayer) {
							String codeEmbedded = getRelative(videoACambiar.attr("code-embedded")).replaceAll("//cdn.jwplayer.com/players/","").replaceAll(".js","");
							String codeEmbeddedSpl[] = codeEmbedded.split("-");
							video = new Element (Tag.valueOf("amp-jwplayer"),"");
							video.attr("layout", "responsive");
							video.attr("width","16");
							video.attr("height","9");
							video.attr("data-media-id",codeEmbeddedSpl[0]);
							video.attr("data-player-id",codeEmbeddedSpl[1]);
							videoDiv.appendChild(video);
							replaceDiv(element, videoDiv);
						}else {
							video= new Element(Tag.valueOf("amp-iframe"), "");
							getVideoEmbedded(videoACambiar, video);
							getMediaDiv(element,video,videoDiv,"video__title");
							replaceDiv(element, videoDiv);
						}
					} else if ( !(element.attr("id").contains("youtube") || element.attr("id").contains("embedded"))) {
						if (!showIframe.equals("true") || embedCode.equals("")) {
							video = new Element(Tag.valueOf("amp-video"), "");
							getJWPlayerData(videoACambiar, video, cms, request);
							getMediaDiv(element,video,videoDiv,"video__title");
							replaceDiv(element, videoDiv);
						} else {
							video = new Element(Tag.valueOf("amp-iframe"), "");
							getVideoIframe(videoACambiar, video, cms, String.valueOf(tEdicion.getId()));
							getMediaDiv(element,video,videoDiv,"video__title");
							replaceDiv(element, videoDiv);
						}
					}
					if (video==null)
						element.remove();
				} else {
					//es una lista	
					Element carousel = new Element(Tag.valueOf("amp-carousel"), "");
					carousel.attr("type", "slides");
					carousel.attr("layout", "responsive");
					for (Element videoACambiar : spanVideos) {
						Element video = null;
						Element videoDiv = doc.createElement("div").addClass("ck-video-player");
						
						if (!showIframe.equals("true") || embedCode.equals("")) {
							video = new Element(Tag.valueOf("amp-video"), "");
							getJWPlayerData(videoACambiar, video, cms, request);
							getMediaDiv(element,video,videoDiv,"video__description");
						} else	{
							video = new Element(Tag.valueOf("amp-iframe"), "");
							getVideoIframe(videoACambiar, video, cms, String.valueOf(tEdicion.getId()));
							getMediaDiv(element,video,videoDiv,"video__description");
						}
						if (carousel.attr("width").equals(""))
				        	carousel.attr("width",video.attr("width"));
				        if (carousel.attr("height").equals(""))
				        	carousel.attr("height", video.attr("height"));
					    if (!video.attr("src").equals(""))
					    	carousel.appendChild(videoDiv);  
					}
					if (carousel.children().size()>0)
						replaceDiv(element, carousel);
					else
						element.remove();
				}
			} catch (Exception ex) {
				LOG.error ("Error al transformar videos en formato amp de la nota: "  + resource.getRootPath(), ex);
				element.remove();
			}
		}
		
		//facebook
		Elements fb = doc.select("div.ckeditor-fb");
		for (Element element : fb) {
			Element ampFb = new Element(Tag.valueOf("amp-facebook"), "");
			Element fbElement = null;
			try {			
				if (element.children().select("div.fb-post").size() > 0){
					fbElement =  element.children().select("div.fb-post").first();
					ampFb.attr("width", /*fbElement.attr("data-width")*/"550");
					ampFb.attr("height", "450");
				} else if (element.children().select("div.fb-video").size() >0 ) {
					//si se trata de un video lleva un atributo extra
					fbElement =  element.children().select("div.fb-video").first();
					ampFb.attr("data-embed-as","video");
					ampFb.attr("width", /*fbElement.attr("data-width")*/"550");
					ampFb.attr("height", "350");
				} 
					
				
				if (fbElement != null) {
					ampFb.attr("data-href",fbElement.attr("data-href"));
					ampFb.attr("layout", "responsive");
					replaceDiv(element, ampFb);
				} else {
					//lo saco por las dudas
					element.remove();
				}
			} catch (Exception ex) {
				LOG.error ("Error al transformar facebook en formato amp de la nota: " + resource.getRootPath(), ex);
				element.remove();
			}
		}
		
		// Considero el nuevo formato con el que puede venir el insert de facebook
		Elements fbi = doc.select("div.ckeditor-ifb");
		for (Element element : fbi) {
			Element ampiFb = new Element(Tag.valueOf("amp-facebook"), "");
			Element iframe = null;
			try {
				iframe = element.select("iframe").first();
				
				if (iframe != null) {
					
					String src = iframe.attr("src").substring(iframe.attr("src").lastIndexOf("/")+1);
					String[] srcA = src.split("&");
					
					String dataHref = null;
					
			        for (int i=0; i<srcA.length; i++)
			        {
			        	
			        	if( srcA[i]!=null && srcA[i].indexOf("href=")>-1)
			        	  dataHref = srcA[i].substring(srcA[i].indexOf("=")+1);
			        }
					
					try {
						ampiFb.attr("data-href", URLDecoder.decode( dataHref, "UTF-8" ));
					} catch (UnsupportedEncodingException e) {
						ampiFb.attr("data-href", dataHref);
					}
					
					ampiFb.attr("width", iframe.attr("width"));
					ampiFb.attr("height", iframe.attr("height"));
					ampiFb.attr("layout", "responsive");
					
					if( iframe.attr("src")!=null && iframe.attr("src").indexOf("video.php")>-1){
						ampiFb.attr("data-embed-as","video");
					}
					
					replaceDiv(element, ampiFb);
				} else {
					element.remove();
				}
			} catch (Exception ex) {
				LOG.error ("Error al transformar facebook formato nuevo en formato amp de la nota: "  + resource.getRootPath() , ex);
				element.remove();
			}
		}
		
		//twitter ck-twitter
		Elements tw = doc.select("div.ck-twitter");
		for (Element element : tw) {
			Element ampTw = new Element(Tag.valueOf("amp-twitter"), "");
			for (Element videosTw :element.select("[class^=twitter-video]") ) {
				videosTw.attr("class","twitter-tweet" );
			}
			try {
				Element blockquoteTw = element.select("[class^=twitter-tweet]").first();
				if (blockquoteTw != null  && blockquoteTw.select("a").size() >0) {
					Element a = blockquoteTw.select("a").last();
					String tweetID = a.attr("href").substring(a.attr("href").lastIndexOf("/")+1);
					if (tweetID.indexOf("?")>-1)
						tweetID = tweetID.substring(0, tweetID.indexOf("?"));
					ampTw.attr("data-tweetid",tweetID);
					ampTw.attr("width", "550");
					ampTw.attr("height", "450");
					ampTw.attr("layout", "responsive");
					//blockquoteTw.attr("placeholder", "");
					//ampTw.appendChild(blockquoteTw);
					replaceDiv(element, ampTw);
				} else if (blockquoteTw == null) {
					 blockquoteTw = element.select("[class^=twitter-moment]").first();
					 if (blockquoteTw != null  && blockquoteTw.select("a").size() >0) {
							Element a = blockquoteTw.select("a").last();
							String momentID = a.attr("href").substring(a.attr("href").lastIndexOf("/")+1);
							momentID = momentID.substring(0, momentID.indexOf("?"));
							ampTw.attr("data-momentid",momentID);
							ampTw.attr("width", "550");
							ampTw.attr("height", "450");
							ampTw.attr("layout", "responsive");
							replaceDiv(element, ampTw);
					 } else {
						 element.remove();
					 }
				} else {	
					element.remove();
				}
			} catch (Exception ex) {
				LOG.error ("Error al transformar twitter en formato amp de la nota: "  + resource.getRootPath(), ex);
				element.remove();
			}
		}
		
		//pinterest
		Elements pinterest = doc.select("div.ck-pinterest");
		for (Element element : pinterest) {
			Element ampPinterest = new Element(Tag.valueOf("amp-pinterest"), "");
			try {
				Element a = element.select("a").first();
				if (a!= null) {
					ampPinterest.attr("data-do","embedPin");
					ampPinterest.attr("data-url", a.attr("href"));
					ampPinterest.attr("width", "600");
					ampPinterest.attr("height", "600");
					ampPinterest.attr("layout", "responsive");
					replaceDiv(element, ampPinterest);
				} else {
					element.remove();
				}
			} catch (Exception ex) {
				LOG.error ("Error al transformar pinterest en formato amp de la nota: " + resource.getRootPath(), ex);
				element.remove();
			}
		}
		
		//Instagram
		Elements instagram = doc.select("div.ck-instagram");
		for (Element element : instagram) {
			Element ampInstragram= new Element(Tag.valueOf("amp-instagram"), "");
			try {
				Element iframe = element.select("a").first();
				if (iframe!= null){
					String shortCode = "";
					if (iframe.attr("href").contains("/p/"))
						shortCode = iframe.attr("href").substring(iframe.attr("href").indexOf("p/")+2, iframe.attr("href").indexOf("/",iframe.attr("href").indexOf("p/")+3));
					else if (iframe.attr("href").contains("/reel/"))
						shortCode = iframe.attr("href").substring(iframe.attr("href").indexOf("reel/")+5, iframe.attr("href").indexOf("/",iframe.attr("href").indexOf("reel/")+6));
					else 
						shortCode = iframe.attr("href").substring(iframe.attr("href").indexOf("tv/")+3, iframe.attr("href").indexOf("/",iframe.attr("href").indexOf("tv/")+4));
					ampInstragram.attr("data-shortcode", shortCode);
					ampInstragram.attr("width", "600");
					ampInstragram.attr("height", "600");
					ampInstragram.attr("layout", "responsive");
					replaceDiv(element, ampInstragram);
				} else {
					element.remove();
				}
			} catch (Exception ex) {
				LOG.error ("Error al transformar instagram en formato amp de la nota:"  + resource.getRootPath(), ex);
				element.remove();
			}
		}

		//youtube
		Elements youtube = doc.select("div.ck-youtube");
		for (Element element : youtube) {
			Element ampYoutube= new Element(Tag.valueOf("amp-youtube"), "");
			try {
				Element iframe = element.select("iframe").first();
				if (iframe != null) {
					String youtubeId = "";
					if (iframe.attr("src").indexOf("/") != -1){
						if (iframe.attr("src").indexOf("?") != -1)
							youtubeId=iframe.attr("src").substring(iframe.attr("src").lastIndexOf("/")+1, iframe.attr("src").indexOf("?"));
						else
							youtubeId=iframe.attr("src").substring(iframe.attr("src").lastIndexOf("/")+1);
					}
					ampYoutube.attr("data-videoid", youtubeId );
					ampYoutube.attr("width", "560");
					ampYoutube.attr("height", "315");
					ampYoutube.attr("layout", "responsive");
					replaceDiv(element, ampYoutube);
				} else {
					element.remove();
				}
			} catch (Exception ex) {
				LOG.error ("Error al transformar youtube en formato amp de la nota:"  + resource.getRootPath(), ex);
				element.remove();
			}
		}
		
		//vine
		Elements vine = doc.select("div.ck-vine");
		for (Element element : vine) {
			Element ampVine= new Element(Tag.valueOf("amp-vine"), "");
			try {
				Element iframe = element.select("iframe").first();
				ampVine.attr("data-vineid", iframe.attr("src").substring(iframe.attr("src").indexOf("v/")+2, iframe.attr("src").indexOf("/",iframe.attr("src").indexOf("v/")+3)));
				ampVine.attr("width", iframe.attr("height"));
				ampVine.attr("height", iframe.attr("width"));
				ampVine.attr("layout", "responsive");
				replaceDiv(element, ampVine);
			} catch (Exception ex) {
				LOG.error ("Error al transformar vine en formato amp de la nota: " + resource.getRootPath(), ex);
				element.remove();
			}
		}
		
		//audios
		Elements audios = doc.select("div.ck-audio-player");
		for (Element element : audios) {
			Element newAudio= new Element(Tag.valueOf("amp-audio"), "");
			Element audioDiv = doc.createElement("div").addClass("ck-audio-player");
			try {
				Elements audiosData = element.select("span");
				//newAudio.attr("layout", "responsive");
				for (Element data : audiosData ) {
					if (data.attr("data-type").equals("audio")){
						String file = data.attr("data-src");
						try {
							CmsResource videoResource = cms.readResource(file);
							String link = UrlLinkHelper.getUrlFriendlyLink(videoResource, cms, request, false);
							newAudio.attr("src", getRelative(link));
						} catch (CmsException e) {
							newAudio.attr("src", "");
							//no encuentra al video 
							LOG.error("Error al buscar archivo: " + file, e);
						}
					}	
					if (data.attr("class").equals("audio__description")){
						getMediaDiv(element,newAudio,audioDiv,"audio__description");
					}
					//if (!data.attr("data-height").equals(""))
					//	newAudio.attr("height", "50");
					//if (!data.attr("data-width").equals(""))
					//	newAudio.attr("width", "auto");
				}
				
				newAudio.attr("height", "50");
				newAudio.attr("width", "auto");
				
				replaceDiv(element, newAudio);
				
			} catch (Exception ex) {
				LOG.error ("Error al transformar audios en formato amp", ex);
				element.remove();
			}	
		}
		
		//audioGallery
		Elements audiosGallery = doc.select("div.ck-audio-gallery");
		getAudiosCarousel(audiosGallery, cms, request);
		
		//audiosList
		Elements audiosLists = doc.select("div.ck-audio-list");
		getAudiosCarousel(audiosLists, cms, request);
		
		//elimino posibles tags script
		doc.select("script").remove();
		
		//jwplayer
		Elements jwplayers = doc.select("div.ck-jwplayer");
		for (Element jwplayer: jwplayers) {
			Element newDiv = new Element (Tag.valueOf("amp-jwplayer"),"");
			newDiv.attr("layout", "responsive");
			newDiv.attr("width","16");
			newDiv.attr("height","9");
			if (!jwplayer.hasAttr("data-media-id") || !jwplayer.hasAttr("data-player-id")
					|| jwplayer.attr("data-media-id").equals("") || jwplayer.attr("data-player-id").equals(""))
				jwplayer.remove();
			else {
				newDiv.attr("data-media-id",jwplayer.attr("data-media-id"));
				newDiv.attr("data-player-id",jwplayer.attr("data-player-id"));
				jwplayer.replaceWith(newDiv);
			}
		}
		
		//Iframes sueltos
		Elements iframes  = doc.select("iframe");
		for (Element iframe : iframes) {
			
			Elements parents = iframe.parents();
			boolean isRaw = false;
			
			for(Element parent : parents){
				if(parent.attr("class")!= null && parent.attr("class").equals("ck-raw"))
					isRaw = true;
			}
			
			if(!isRaw){
				Element newIframe =  new Element(Tag.valueOf("amp-iframe"), "");
				newIframe.attr("layout", "responsive");
				try {
					if ( iframe.attr("width").equals("") || iframe.attr("width").contains("%"))
						newIframe.attr("width", "640");
					else
						newIframe.attr("width", iframe.attr("width"));	
					
					if ( iframe.attr("height").equals("") || iframe.attr("height").contains("%"))
						newIframe.attr("height","360");
					else
						newIframe.attr("height",iframe.attr("height"));
					
					newIframe.attr("src", getHttps(iframe.attr("src")));
					newIframe.attr("sandbox", "allow-scripts allow-same-origin");
					newIframe.attr("frameborder", "0");
					iframe.replaceWith(newIframe);
				} catch (Exception ex) {
					LOG.error ("Error al transformar iframes en formato amp de la nota: "  + resource.getRootPath(), ex);
					iframe.remove();
				}
			}
		}
		
		Elements relatedNews = doc.select("div.ck-related-news");
		String template =  config.getParam(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot(), String.valueOf(tEdicion.getId()), "ckeditor", "related-news-template-AMP", "");
		
		for (Element element : relatedNews) {
			
			if(template == null || template.equals("")){
				element.remove();
			}else{
				String relatedNewsPaths = "";
				
				int itemsList = element.select("span[data-type=\"news\"]").size();
				
				for(int i=0; i< itemsList; i++){
					Element relatedNewsItem = element.select("span[data-type=\"news\"]").get(i);
					if(relatedNewsItem != null ){
						if (!relatedNewsItem.attr("data-src").equals("")){
							relatedNewsPaths = relatedNewsPaths+","+relatedNewsItem.attr("data-src");
						}
					}
				}
				
				if(!relatedNewsPaths.equals(""))
					relatedNewsPaths = relatedNewsPaths.substring(1);
				
				try {
					String contentToReplace = includeNoCache(template,"", pageContext, relatedNewsPaths);
					element.html(contentToReplace);
				} catch (JspException e) {
					LOG.error("AMP - Error al insertar las noticias relacionadas. Se sacan. Nota: " + resource.getRootPath(), e);
					element.remove();
				}
			}
			
		}
		
		//tiktok ck-tiktok
		Elements tt = doc.select("div.ck-tiktok");
		for (Element element : tt) {
			Element ampTt = new Element(Tag.valueOf("amp-tiktok"), "");
					
			try {
				Element blockquoteTt = element.select("[class^=tiktok-embed]").first();
						
				if (blockquoteTt != null  && blockquoteTt.attr("data-video-id") != null) {
							
					String tiktokID = blockquoteTt.attr("data-video-id");
							
					if (tiktokID.indexOf("?")>-1)
						tiktokID = tiktokID.substring(0, tiktokID.indexOf("?"));
							
					ampTt.attr("data-src",tiktokID);
					ampTt.attr("width", "325");
					ampTt.attr("height", "575");
					ampTt.attr("layout","responsive");

					replaceDiv(element, ampTt);
				} else {	
					element.remove();
				}
			} catch (Exception ex) {
				LOG.error ("Error al transformar tiktok en formato amp de la nota: "  + resource.getRootPath(), ex);
				element.remove();
			}
		}
		
		Elements eventos = doc.select("div.ck-events");
		getElementStyle (eventos,cms, pageContext,"events", "eventViewAMP.jsp");
		
		
		Elements polls = doc.select("[class^=ckeditor-poll]");
		getElementStyle (polls,cms, pageContext,"polls", "pollAmp.jsp");
		
		Elements recetas = doc.select("div.ck-recipe");
		getElementStyle (recetas,cms, pageContext,"recipes", "recipeViewAMP.jsp");
		
		Elements vods = doc.select("div.ck-vods");
		getElementStyle (vods,cms, pageContext,"vods", "vodViewAMP.jsp");
		
		Elements playlists = doc.select("div.ck-playlist");
		getElementStyle (playlists,cms, pageContext,"playlist", "playlistViewAMP.jsp");
			
		Elements trivias = doc.select("div.ck-trivias");
		getElementStyle (trivias,cms, pageContext,"trivias", "triviasViewAMP.jsp");
	
		//tags a restantes
		Elements linksPendientes = doc.select("a");
		for (Element element : linksPendientes) {
			if (!element.hasAttr("href") || element.attr("href").equals("")  || element.attr("href").equals("https://")  || element.attr("href").equals("http://") )
				element.replaceWith(new TextNode(element.html(), ""));
			else if (!element.attr("href").equals("") ){
				String href = element.attr("href");
				String pattern = "^(http(s)?://)?(/(([\\w@:%+~#=])+)|(([\\w@:%+~#=])+([\\./])))+([-\\w@:%+\\.~#?,&/=!;$])*$";
				if (!href.matches(pattern))
					element.replaceWith(new TextNode(element.html(), ""));
			} 
		}
		
		content = doc.body().html();

		return content;
	}
	
	

	private static void getElementStyle (Elements elementos, CmsObject cms, PageContext pageContext,String type, String nameFile){
		for (Element element : elementos) {
			String elementPath = "";
			if (element.hasAttr("data-path")){
				elementPath = element.attr("data-path");
			}
			try {
				CmsFile elementFile = cms.readFile(elementPath);
				CmsXmlContent contenido = CmsXmlContentFactory.unmarshal(cms, elementFile);
				String estilo = contenido.getStringValue(cms, "estilo[1]", cms.getRequestContext().getLocale());
				//saco el path
				element.removeAttr("data-path");
				if (estilo == null || estilo.equals("")){
					element.remove();
				} else {
					//especial para vods
					if (type.equals("vods")) {
						if (elementPath.contains("serie"))
							type += "/serie";
						else if (elementPath.contains("pelicula"))
							type += "/pelicula";
						else if (elementPath.contains("temporada"))
							type += "/temporada";
						else if (elementPath.contains("episodio"))
							type += "/episodio";
					}
					//especial para vods
					String pathToGet = "/system/modules/com.tfsla.diario.newsTags/elements/"+ type+"/"+estilo+ "/"+ nameFile;
					try {
						String contentToReplace = includeNoCache(pathToGet,"", pageContext, elementPath);
						element.html(contentToReplace);
					} catch (JspException e) {
						LOG.debug("AMP - Error al buscar la jsp del " + type +" :" + pathToGet + ". Se saca", e);
						element.remove();
					}
				}
			} catch (CmsException e) {
				LOG.error("AMP - Error al buscar el elemento: " + elementPath + ". Se saca", e);
				element.remove();
			}
		}
	}
	
	private static void getImageForImageComparation (CmsObject cms, CmsResource newsFile, Element image, Element newImage, String ampImageWidth, PageContext pageContext){
		String width =  image.attr("width");
		String height = image.attr("height");
		String src = image.attr("data-src");
		String alt = image.attr("description");
		if (ampImageWidth != null && !ampImageWidth.equals("")){
			newImage.attr("layout", "responsive");
			Long heightInt = new Long(0);
			try {
				heightInt = Math.round(0.5625 * Integer.parseInt(ampImageWidth));
				height = heightInt.toString();
				width = ampImageWidth;
			} catch (NumberFormatException ex) {
				LOG.error("Error al calcular height en galeria de images: " + src , ex) ;
			}
		} else {
			newImage.attr("layout", "fill");
		}	
		newImage.attr("width", width);
		newImage.attr("height",height);
		newImage.attr("alt",alt);

		ImageFinder imgFinder = new ImageFinder();
		String path="";
		try {
			path = imgFinder.getImageVariantPathBody(cms, newsFile, src, Integer.parseInt(ampImageWidth));

			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		if (!path.equals(""))
			newImage.attr("src", path);
		else {
			if (src.indexOf("?") != -1)
				src = src.substring(0, src.indexOf("?"));
			if (src.indexOf("/img") > 0)
				src = src.substring(src.indexOf("/img"));
			newImage.attr("src",	TfsJspTagLink.linkTagAction(src + "?__scale=c:transparent,w:" + width +",h:" + height, pageContext.getRequest()) );
		}
	}
	
	private static void getImageGallery(Elements images, CmsResource newsFile,CmsObject cms, String ampImageWidth, PageContext pageContext, Element carousel) {
		for (Element image : images) {
			Element div = new Element(Tag.valueOf("div"), ""); 
			div.addClass("slide");
			Element newImage = new Element(Tag.valueOf("amp-img"), "");
			String width =  image.attr("width");
			String height = image.attr("height");
			String src = image.attr("data-src");
			if (ampImageWidth != null && !ampImageWidth.equals("")){
				newImage.attr("layout", "responsive");
				Long heightInt = new Long(0);
				try {
					heightInt = Math.round(0.5625 * Integer.parseInt(ampImageWidth));
					height = heightInt.toString();
					width = ampImageWidth;
				} catch (NumberFormatException ex) {
					LOG.error("Error al calcular height en galeria de images: " + src , ex) ;
				}
			} else {
				newImage.attr("layout", "fill");
			}	
			newImage.attr("width", width);
			newImage.attr("height",height);

			if (src.indexOf("?") != -1)
				src = src.substring(0, src.indexOf("?"));
			if (src.indexOf("/img") > 0)
				src = src.substring(src.indexOf("/img"));
			
			
			ImageFinder imgFinder = new ImageFinder();
			String path="";
			try {
				path = imgFinder.getImageVariantPathBody(cms, newsFile, src, Integer.parseInt(ampImageWidth));

				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			if (!path.equals(""))
				newImage.attr("src", path);
			else 
				newImage.attr("src",	TfsJspTagLink.linkTagAction(src + "?__scale=c:transparent,w:" + width +",h:" + height, pageContext.getRequest()) );
			
			Element divCaption = new Element(Tag.valueOf("div"), "");
			divCaption.attr("class","caption");
			divCaption.html(image.attr("description"));
			div.appendChild(newImage);
			div.appendChild(divCaption);
			if (carousel.attr("width").equals(""))
				carousel.attr("width",width);
	        if (carousel.attr("height").equals(""))
	        	carousel.attr("height",height);
			carousel.appendChild(div);
		}
	}
	
	private static void getImageGallery_v2(Elements images, CmsResource newsFile,CmsObject cms, String ampImageWidth, PageContext pageContext, Element carousel) {
		
		for (Element image : images) {
			
			Element newImage = new Element(Tag.valueOf("amp-img"), "");
			String width =  image.attr("width");
			String height = image.attr("height");
			String src = image.attr("data-src");
			if (ampImageWidth != null && !ampImageWidth.equals("")){
				newImage.attr("layout", "responsive");
				Long heightInt = new Long(0);
				try {
					heightInt = Math.round(0.5625 * Integer.parseInt(ampImageWidth));
					height = heightInt.toString();
					width = ampImageWidth;
				} catch (NumberFormatException ex) {
					LOG.error("Error al calcular height en galeria de images: " + src , ex) ;
				}
			} else {
				newImage.attr("layout", "fill");
			}	
			newImage.attr("width", width);
			newImage.attr("height",height);
			newImage.attr("alt",image.attr("description"));

			if (src.indexOf("?") != -1)
				src = src.substring(0, src.indexOf("?"));
			if (src.indexOf("/img") > 0)
				src = src.substring(src.indexOf("/img"));
			
			ImageFinder imgFinder = new ImageFinder();
			String path="";
			try {
				path = imgFinder.getImageVariantPathBody(cms, newsFile, src, Integer.parseInt(ampImageWidth));

				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (!path.equals(""))
				newImage.attr("src", path);
			else 
				newImage.attr("src",	TfsJspTagLink.linkTagAction(src + "?__scale=c:transparent,w:" + width +",h:" + height, pageContext.getRequest()) );
			
			
			if (carousel.attr("width").equals(""))
				carousel.attr("width",width);
	        if (carousel.attr("height").equals(""))
	        	carousel.attr("height",height);
	        
	        carousel.appendChild(newImage);
		}
	}
	
	private static void getImageGallerySelector(Elements images, String ampImageWidth, PageContext pageContext, Element selector) {
		
		int i = 0; 
		
		for (Element image : images) {
			
			Element newImage = new Element(Tag.valueOf("amp-img"), "");
			String width =  "60";
			String height = "40";
			String src = image.attr("data-src");
			
			newImage.attr("width", width);
			newImage.attr("height",height);
			
			newImage.attr("option",i+"");
			
			if(i==0)
				newImage.attr("selected",true);
			
			i = i + 1;

			if (src.indexOf("?") != -1)
				src = src.substring(0, src.indexOf("?"));
			if (src.indexOf("/img") > 0)
				src = src.substring(src.indexOf("/img"));
			newImage.attr("src",TfsJspTagLink.linkTagAction(src + "?__scale=c:transparent,w:" + width +",h:" + height, pageContext.getRequest()) );
		
			newImage.attr("alt",image.attr("description"));
			
	        selector.appendChild(newImage);
		}
	}
	
	private static void getAudiosCarousel (Elements audiosGallery,  CmsObject cms, ServletRequest request ) {
		for (Element element : audiosGallery) {
			Element carousel = new Element(Tag.valueOf("amp-carousel"), "");
			carousel.attr("type", "slides");
			carousel.attr("layout", "responsive");
			String height="50";
			String width="auto";
			try {
				Elements audiosData = element.select("span");
				/*for (Element data : audiosData ) {
					if (!data.attr("data-height").equals(""))
						height = data.attr("data-height");
					if (!data.attr("data-width").equals(""))
						width =  data.attr("data-width");
				}*/
				for (Element data : audiosData) {
					if (data.attr("data-type").equals("audio")){
						Element newAudio= new Element(Tag.valueOf("amp-audio"), "");
						//newAudio.attr("layout", "responsive");
						String file = data.attr("data-src");
						try {
							CmsResource videoResource = cms.readResource(file);
							String link = UrlLinkHelper.getUrlFriendlyLink(videoResource, cms, request, false);
							newAudio.attr("src", getRelative(link));
						} catch (CmsException e) {
							newAudio.attr("src", "");
							//no encuentra al video 
							LOG.error("Error al buscar archivo: " + file, e);
						}
						newAudio.attr("height", height);
						newAudio.attr("width", width);
						carousel.appendChild(newAudio);
					}
				}
				carousel.attr("height", "106");
				carousel.attr("width", "480");
				if (carousel.children().size()>0)
					replaceDiv(element, carousel);
				else
					element.remove();
			} catch (Exception ex) {
				LOG.error ("Error al transformar galleria o listado de audios en formato amp", ex);
				element.remove();
			}
		}
	}
	
	private static void getVideoEmbedded(Element element, Element video) {
			video.attr("width", element.attr("width").equals("") || !NumberUtils.isNumber(element.attr("width").trim()) ? "640" :element.attr("width"));	
			video.attr("height",element.attr("height").equals("") || !NumberUtils.isNumber(element.attr("width").trim())? "360":element.attr("height"));
			video.attr("src", getHttps(element.attr("code-embedded")));
			video.attr("sandbox", "allow-scripts allow-same-origin");
			video.attr("frameborder", "0");
			video.attr("layout", "responsive");
	}
	
	

	private static void getImage(CmsResource newsFile, Element element, Element newImage, CmsObject cms, String ampImageWidth,PageContext pageContext ){
		String src = element.attr("src"); 
		if (src.indexOf("?") != -1)
			src = src.substring(0, src.indexOf("?"));
		if (src.indexOf("/img") > 0) {
			if (src.matches(".*.[a-z]*_[0-9]*.[a-z]*"))
				src = src.substring(src.indexOf("/img"), src.lastIndexOf("_"));
			else 
				src = src.substring(src.indexOf("/img"));
		}
		boolean isExternalImage = isExternalContent(src); 
		String width = element.attr("data-width");
		String height = element.attr("data-height");
		if (!element.attr("alt").equals(""))
				newImage.attr("alt", element.attr("alt"));
		newImage.attr("layout", "responsive");
		if ((width.equals("") || width.equals("undefined") || width.equals("0")  
				|| height.equals("") ||  height.equals("undefined") ||  height.equals("0"))
				&& !isExternalImage) {
			try {
				//CmsResource imageResource = cms.readResource(src);
				CmsProperty       prop = cms.readPropertyObject(src, "image.size", false);      
				String       ImageSize = (prop != null) ? prop.getValue() : null;
				if(ImageSize != null){
				    String[] ImgSize = ImageSize.split(",");
				    
				    if(ImgSize[0] != null) {
				       String[] w = ImgSize[0].split(":");
				            width = w[1];
				    }
				    
				    if(ImgSize[1] != null) {
				       String[] h = ImgSize[1].split(":");
				           height = h[1];
				    }
				}
			} catch (CmsException e) {
					LOG.error("Error al buscar archivo: " + src, e);
			}
		}
		Long heightInt = new Long(0);
		if (ampImageWidth != null && !ampImageWidth.equals("")) {
			try {
				heightInt = Math.round((Double.parseDouble(height)/ Double.parseDouble(width)) * Double.parseDouble(ampImageWidth));
				width = ampImageWidth;
				height = String.valueOf(heightInt);
			} catch (NumberFormatException ex) {
				LOG.debug("Error en el calculo del aspect de la imagen: " + src, ex);
				heightInt = Math.round(0.5625 * Integer.parseInt(ampImageWidth));
				height = heightInt.toString();
				width = ampImageWidth;
			}
		}
		
		String path = "";
		ImageFinder imgFinder = new ImageFinder();
		try {
			path = imgFinder.getImageVariantPathBody(cms, newsFile, src, Integer.parseInt(ampImageWidth));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (!path.equals("")) {
			newImage.attr("src", path);
			newImage.attr("width", width);
			newImage.attr("height", height);
		}
		else 
			if (!height.equals("") && !height.equals("0")) {
				src = TfsJspTagLink.linkTagAction(src + "?__scale=c:transparent,w:" + width +",h:" + height + ",t:3" , pageContext.getRequest());
				newImage.attr("src",src );
				newImage.attr("width", width);
				newImage.attr("height", height);
			} else {
				newImage.attr("src", "");
			}
		
		
		String srcset = element.attr("srcset");
		String sizes = element.attr("sizes");
		
		if(srcset!=null && !srcset.equals(""))
			newImage.attr("srcset", srcset);
		
		if(sizes!=null && !sizes.equals(""))
			newImage.attr("sizes", sizes);
	}
	
	private static void getAmpImages(CmsResource newsFile, Elements images, CmsObject cms, String ampImageWidth, PageContext pageContext) {
		for (Element element:images){
			Element newImage = new Element(Tag.valueOf("amp-img"), "");
			try {
				getImage(newsFile, element, newImage, cms, ampImageWidth, pageContext);
				if (!newImage.attr("src").equals(""))
					replaceDiv(element, newImage);
				else 
					element.remove();
			} catch (Exception ex) {
				LOG.error ("Error al transformar imagenes en formato amp", ex);
				element.remove();
			}
		}
	}
	
	private static void getVideoIframe(Element element, Element video, CmsObject cms, String currentPublication) {
		video.attr("width", element.attr("width").equals("") || !NumberUtils.isNumber(element.attr("width").trim()) ? "640" :element.attr("width"));	
		video.attr("height",element.attr("height").equals("") || !NumberUtils.isNumber(element.attr("width").trim())? "360":element.attr("height"));
		video.attr("sandbox", "allow-scripts allow-same-origin");
		video.attr("frameborder", "0");
		video.attr("layout", "responsive");
		//video.attr("poster",video.attr("data-img"));
		//<amp-img layout="fill" src="[IMAGEN DEL VIDEO MP4]" placeholder></amp-img>
		String file = element.attr("data-src");
		String src = TfsVideoHelper.getVideoEmbedCode(file, cms,currentPublication );
		Element img =  new Element(Tag.valueOf("amp-img"), "");
		if ( element.attr("data-img")!= null && !element.attr("data-img").equals("")) {
			img.attr("src",element.attr("data-img"));
			img.attr("layout", "fill");
			img.attr("placeholder", "");
			video.appendChild(img);
		}
		try {
			src = src.substring( src.indexOf("src=")+5, src.indexOf('"', src.indexOf("src=")+5));
			video.attr("src", getHttps(src));
			
		} catch (Exception ex) {
		  element.remove();
		  video = null;
		}
	}
	
	private static void getJWPlayerData(Element videoPlayer, Element video, CmsObject cms, ServletRequest request){
		video.attr("width","560");
		video.attr("height","315");
		video.attr("poster",videoPlayer.attr("data-img"));
		video.attr("controls", "");
		video.attr("layout", "responsive");	
		if (videoPlayer.attr("autoplay").equals("true"))
			video.attr("autoplay", "");
		if (videoPlayer.attr("mute").equals("true"))
			video.attr("muted", "");
		String file = videoPlayer.attr("data-src");
		try {
			CmsResource videoResource = cms.readResource(file);
			String link = UrlLinkHelper.getUrlFriendlyLink(videoResource, cms, request, false);
			video.attr("src", getRelative(link));
		} catch (CmsException e) {
			video.attr("src", "");
			//no encuentra al video 
			LOG.error("Error al buscar archivo: " + file, e);
		}
    }
	
	

	private static void getYoutubeVideo(Element element, Element ampYoutube){
		ampYoutube.attr("width", "560");
		ampYoutube.attr("height","315");
		ampYoutube.attr("layout", "responsive");
		/*if (element.attr("autoplay").equals("true"))
			ampYoutube.attr("autoplay", element.attr("autoplay"));
		if (element.attr("mute").equals("true"))
			ampYoutube.attr("muted", element.attr("mute"));
		*/
		String youtubeId = element.attr("youtubeid");
		if (youtubeId.indexOf("/") != -1){
			if (youtubeId.indexOf("?") != -1)
				youtubeId=youtubeId.substring(youtubeId.lastIndexOf("/")+1, youtubeId.indexOf("?"));
			else
				youtubeId=youtubeId.substring(youtubeId.lastIndexOf("/")+1);
		}
		ampYoutube.attr("data-videoid", youtubeId);
	}
		
	private static void getMediaDiv(Element ckeditor, Element mediaAmp , Element nuevoDiv, String class__description){
		Elements spanDescripcion = ckeditor.select("span[class=\""+class__description+"\"]");
		
		nuevoDiv.appendChild(mediaAmp);
		
		if (spanDescripcion != null && !spanDescripcion.isEmpty()) { 
			Element figCaption = new Element(Tag.valueOf("figcaption"), "");
			String textDescription = spanDescripcion.text();
			
			if(textDescription != null && textDescription != ""){
				figCaption.appendText(textDescription);
				nuevoDiv.appendChild(figCaption);
			}
		}
		
	}  
	
	private static String getRelative(String src) {
		if (src.contains("https://"))
			return src.replace("https:","");
		else if (src.contains("http://"))
			return src.replace("http:","");
		return src;
	}  
	
	private static String getHttps(String src) {
		if (!(src.contains("https:") || src.contains("http:")) && src.contains("//"))
			return src.replace("//", "https://");
		return src.replace("http:","https:");
		
	}  
	
	private static boolean isExternalContent(String src) {
		LOG.info("isExternalContent: " + src);
		if (src.contains("https://"))
			return true;
		if (src.contains("http://"))
			return true;
		return false;
	}  
	
	private static void replaceDiv(Element toReplace, Element newElement){
		if (toReplace.parent()!=null && toReplace.parent().nodeName().equals("div"))
			toReplace.parent().replaceWith(newElement);
		else
			toReplace.replaceWith(newElement);
	}
	
	private static String includeNoCache(String target,  String element,  PageContext pageContext, String resourcePath) throws JspException {
  		HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
  		HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
  		CmsFlexController controller = CmsFlexController.getController(req);

  		try {
            // include is not cachable 
            CmsFile file = controller.getCmsObject().readFile(target);
            CmsObject cms = controller.getCmsObject();
            Locale locale = cms.getRequestContext().getLocale();

            Map<String,String[]> parameters = new HashMap<String,String[]>();
            String valores[] = new String[1];
			valores[0] = resourcePath;
            parameters.put ("path",valores );
            controller.getCurrentRequest().addParameterMap(parameters);
            // get the loader for the requested file 
            I_CmsResourceLoader loader = OpenCms.getResourceManager().getLoader(file);
            String content;
            if (loader instanceof I_CmsResourceStringDumpLoader) {
                // loader can provide content as a String
                I_CmsResourceStringDumpLoader strLoader = (I_CmsResourceStringDumpLoader)loader;
                content = strLoader.dumpAsString(cms, file, element, locale, req, response);
            } else {
                // get the bytes from the loader and convert them to a String
                byte[] result = loader.dump(
                    cms,
                    file,
                    element,
                    locale,
                    req,
                    response);
                // use the encoding from the property or the system default if not available
                String encoding = cms.readPropertyObject(file, CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, true).getValue(
                    OpenCms.getSystemInfo().getDefaultEncoding());
                // If the included target issued a redirect null will be returned from loader 
                if (result == null) {
                    result = new byte[0];
                }
                content = new String(result, encoding);
            }
            // write the content String to the JSP output writer
            return content;

        } catch (ServletException e) {
            // store original Exception in controller in order to display it later
            Throwable t = (e.getRootCause() != null) ? e.getRootCause() : e;
            t = controller.setThrowable(t, target);
            throw new JspException(t);
        } catch (IOException e) {
            // store original Exception in controller in order to display it later
            Throwable t = controller.setThrowable(e, target);
            throw new JspException(t);
        } catch (CmsException e) {
            // store original Exception in controller in order to display it later
            Throwable t = controller.setThrowable(e, target);
            throw new JspException(t);
        }
    }
	
	private static void getImageWidthControl(CmsResource newsFile, Element element, Element newImage, CmsObject cms, String maxImageWidth,boolean isLoadingLazy,PageContext pageContext ) throws NumberFormatException, Exception{
		
		if(maxImageWidth.equals("0"))
			return;
		
		String srcset = element.attr("srcset");
		
		if(srcset!=null && !srcset.equals(""))
			return;
		
		String src = element.attr("src"); 
		
		if (src.indexOf("?") != -1)
			src = src.substring(0, src.indexOf("?"));
		if (src.indexOf("/img") > 0) {
			if (src.matches(".*.[a-z]*_[0-9]*.[a-z]*"))
				src = src.substring(src.indexOf("/img"), src.lastIndexOf("_"));
			else 
				src = src.substring(src.indexOf("/img"));
		}
		
		boolean isExternalImage = isExternalContent(src); 
		
		String width = element.attr("data-width");
		String height = element.attr("data-height");
		
		if ((width.equals("") || width.equals("undefined") || width.equals("0") || height.equals("") ||  height.equals("undefined") ||  height.equals("0")) && !isExternalImage) {
			try {
				CmsProperty       prop = cms.readPropertyObject(src, "image.size", false);      
				String       ImageSize = (prop != null) ? prop.getValue() : null;
				if(ImageSize != null){
				    String[] ImgSize = ImageSize.split(",");
				    
				    if(ImgSize[0] != null) {
				       String[] w = ImgSize[0].split(":");
				            width = w[1];
				    }
				    
				    if(ImgSize[1] != null) {
				       String[] h = ImgSize[1].split(":");
				           height = h[1];
				    }
				}
			} catch (CmsException e) {
					LOG.error("Error al buscar archivo: " + src, e);
			}
		}
		
		if (!width.equals("") && !width.equals("0") && !height.equals("") && !height.equals("0") && !isExternalImage) {
		
			ImageFinder imgFinder = new ImageFinder();
			String path = imgFinder.getImageVariantPathBody(cms, newsFile, src, Integer.parseInt(maxImageWidth));
			
			
			if(Integer.parseInt(width)>Integer.parseInt(maxImageWidth)){
				
				Long heightInt = new Long(0);
	
				try {
						heightInt = Math.round((Double.parseDouble(height)/ Double.parseDouble(width)) * Double.parseDouble(maxImageWidth));
						width = maxImageWidth;
						height = String.valueOf(heightInt);
				} catch (NumberFormatException ex) {
						LOG.debug("Error en el calculo del aspect de la imagen: " + src, ex);
						heightInt = Math.round(0.5625 * Integer.parseInt(maxImageWidth));
						height = heightInt.toString();
						width = maxImageWidth;
				}
	
				if (!element.attr("alt").equals(""))
					newImage.attr("alt", element.attr("alt"));
				
				if (!element.attr("data-height").equals(""))
					newImage.attr("data-height", height);
				
				if (!element.attr("data-width").equals(""))
					newImage.attr("data-width",width);
				
				if (!element.attr("data-size").equals(""))
					newImage.attr("data-size", "w:"+width+",h:"+height);
				
				if (!element.attr("hspace").equals(""))
					newImage.attr("hspace", element.attr("hspace"));
				
				if (!element.attr("vspace").equals(""))
					newImage.attr("vspace", element.attr("vspace"));
				
				if (!element.attr("title").equals(""))
					newImage.attr("title", element.attr("title"));
				
				if (!path.equals(""))
					newImage.attr("src", path);
				else {
					src = TfsJspTagLink.linkTagAction(src + "?__scale=c:transparent,w:" + width +",h:" + height + ",t:3" , pageContext.getRequest());
					newImage.attr("src",src );
				}
					newImage.attr("width", width);
					newImage.attr("height", height);
				
			}else {
				newImage.attr("src", "");
			}
		
		}else {
			newImage.attr("src", "");
		}
		
		if(isLoadingLazy)
			newImage.attr("loading","lazy");
		else
			newImage.removeAttr("loading"); 
	}
	
	public static String formatWidthControl(String content, CmsResource resource, CmsObject cms, ServletRequest request, PageContext pageContext){
		
		TipoEdicion tEdicion  = null;
		TipoEdicionService tEService = new TipoEdicionService();
		try {
			tEdicion = tEService.obtenerTipoEdicion(cms,cms.getSitePath(resource));
		} catch (Exception e1) {
			LOG.error("No encuentra la publicacion ", e1);
		}
		
		String  maxImageWidth = "0";
		Boolean isLoadingLazy = false;
		
		if (tEdicion != null ){
			maxImageWidth = config.getParam(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot(), String.valueOf(tEdicion.getId()), "body-formats", "maxImageWidth","0");
			isLoadingLazy = config.getBooleanParam(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot(), String.valueOf(tEdicion.getId()), "imageUpload", "loadingLazy",false);
		}
		
		Document doc = Jsoup.parse(content);
		Elements images = doc.select("img");
			
		for (Element element:images){
				
			try {
				Element newImage = new Element(Tag.valueOf("img"), "");
				getImageWidthControl(resource, element, newImage, cms, maxImageWidth,isLoadingLazy, pageContext);
					
				if (!newImage.attr("src").equals("")) {
						element.replaceWith(newImage);
				}else{
					if(isLoadingLazy)
						element.attr("loading","lazy");
					else
						element.removeAttr("loading"); 
				}
					
			} catch (Exception ex) {
				LOG.error ("Error al controlar el ancho de la imagen", ex);
				element.remove();
			}
		}
			
		content = doc.body().html();
		
		
		return content;
	}

}
