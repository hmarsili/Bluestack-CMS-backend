package com.tfsla.diario.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.PublicationService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;

public class TfsVideoHelper {
	
	protected static final Log LOG = CmsLog.getLog(TfsVideoHelper.class);

	
	private static String replaceVariables(String link,  CmsResource resource,  CmsObject cmsObject) {
		List<String> presentVariables = new ArrayList<String>();
		int i=0;
		while (link.indexOf("{prop.", i)>=0){
				presentVariables.add(link.substring(link.indexOf("{prop.", i), link.indexOf("}", i)+1));
				i = link.indexOf("}", i)+1;
		}
		for (String var : presentVariables)	{
			String propName = var.replace("{prop.", "").replace("}", "");
			String value=null;
			if (propName.equals("extension"))
				value = resource.getName().substring(resource.getName().lastIndexOf(".")+1); //va sin el punto
			else {
				try {
					value = formatURLName(cmsObject.readPropertyObject(resource, propName, false).getValue());
				} catch (CmsException e) {
					
				}
			}
			if (value==null)
				value = "";
			
			link = link.replaceAll(Pattern.quote(var), value.replace(" ", "-"));
		}
		
		return link;
	}

	
	public static String getVideoUrlHelper ( String path, CmsObject cmsObject, boolean publicUrl,String currentPublcation){
	
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		String siteName = OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot();
		
		String link = "";
		CmsResource videoResource = null;
		
		try {
			videoResource = cmsObject.readResource(path,CmsResourceFilter.ALL);
		} catch (CmsException e) {
			LOG.error ("Error al obtener el video", e);
			return "";
		}
		
		String idPublication = currentPublcation;
		TipoEdicion publication = null; 
		if (currentPublcation.equals("")) {
			try {
				publication = PublicationService.getCurrentPublicationWithoutSettings(cmsObject);
				idPublication = String.valueOf(publication.getId());
				LOG.debug("publicacion encontrada:" + publication.getNombre() );
			} catch (Exception e1) {
				LOG.error("error al buscar la publicacion");
				return "";
			}
		} else {
			TipoEdicionService tEService = new TipoEdicionService();
			publication = tEService.obtenerTipoEdicion( Integer.parseInt(idPublication));
		}

		int typeid  = videoResource.getTypeId();
		String type = "";
		try {
			 type = OpenCms.getResourceManager().getResourceType(typeid).getTypeName();
		} catch (CmsLoaderException e) {
			LOG.error ("Error al obtener el tipo de video", e);
			return "";
		}
	
		String urlFriendlyFormat = "";
		String regExp = "";
		if (type.equals("video-link")){
			urlFriendlyFormat =  config.getParam(siteName, idPublication, "videos", "urlFriendlyFormatFlash", "");
			regExp = config.getParam(siteName, idPublication, "videos", "urlFriendlyFormatRegexFlash", "");
		} else if(type.equals("video-youtube")) {
			urlFriendlyFormat = config.getParam(siteName, idPublication, "videos", "urlFriendlyFormatYoutube", "");
			regExp = config.getParam(siteName, idPublication, "videos", "urlFriendlyFormatRegexYoutube", "");
		} else if(type.equals("video-embedded")) {
			urlFriendlyFormat =  config.getParam(siteName, idPublication, "videos", "urlFriendlyFormatEmbedded", "");
			regExp = config.getParam(siteName, idPublication, "videos", "urlFriendlyFormatRegexEmbedded", "");
		}
		
		if (urlFriendlyFormat.equals("") || regExp.equals("") ) { 
			LOG.info("NEWSTAG - VIDEO-URL Faltan configurar los parametros UrlFriendlyFormat y/o UrlFriendlyFormatRegex. " + 
					"Por favor revise que esten los pares correspondientes a cada tipo de video (embed, yotube y nativo");
			return "";
		} 
		
		try {
			link = path.replaceAll(regExp, urlFriendlyFormat);
		} catch (IndexOutOfBoundsException ex) {
			LOG.error("Error en la definicion de la regex para validar el formato", ex);
			return "";
		}
		
		link = replaceVariables(link, videoResource, cmsObject);
		
		String domain = getDomain (cmsObject, publication, videoResource, config, publicUrl);
			
		return domain + "/" + link;
	}

	private static String getDomain ( CmsObject cmsObject, TipoEdicion publication, CmsResource resource, CPMConfig config, boolean publicUrl) {
		
		TipoEdicionService tEService = new TipoEdicionService();
		
		String customDomain = "/";
       
		if (cmsObject.getRequestContext().currentProject().isOnlineProject()) {
	    	customDomain = publication.getCustomDomain();
        	LOG.debug("UrlVideoHelper / "  + customDomain);
        	customDomain = (customDomain == null || customDomain.trim().equals("") ? "/" : customDomain + "/");
		} 
		
		customDomain = customDomain.trim();
		
		if (customDomain.equals("/")) {
		  	LOG.debug("UrlVideoHelper / no tiene custom domain"  + customDomain);
			
			TipoEdicion publicacionRaiz;
			try {
				publicacionRaiz = tEService.obtenerTipoEdicion(cmsObject, OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot());
				LOG.debug("publicacion Raiz: " + publicacionRaiz.getNombre());
				if (!publication.getNombre().equals(publicacionRaiz.getNombre())) {
					customDomain +=  publication.getNombre();
				}
			} catch (Exception e) {
				LOG.error("Error al buscar publicacion raiz");
			}
			LOG.debug("UrlVideokHelper / dominio relativo " + customDomain);
		} else {
			customDomain = "/";
		  	LOG.debug("UrlVideoHelper / tiene custom domain"  + customDomain);
		}
	  	LOG.debug("UrlVideoHelper / entrega el siguiente custom"  + customDomain);
		
		return customDomain.equals("/")?"":customDomain;
		
	}

	public static String getVideoEmbedCode(String videoPath, CmsObject cmsObject) {
		return getVideoEmbedCode( videoPath, cmsObject,"" );
	}	
	
	public static String getVideoEmbedCode(String videoPath, CmsObject cmsObject,String currentPublcation ) {
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();

		String siteName = OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot();
		String idPublication = currentPublcation;
		TipoEdicion tEdicion = null; 
		if (currentPublcation.equals("")) {
			try {
				tEdicion = PublicationService.getCurrentPublicationWithoutSettings(cmsObject);
				idPublication = String.valueOf(tEdicion.getId());
			} catch (Exception e1) {
				LOG.error("error al buscar la publicacion");
				return "";
			}
		}
		
		CmsResource videoResource = null;
		
		try {
			videoResource = cmsObject.readResource(videoPath,CmsResourceFilter.ALL);
		   
		} catch (CmsException e) {
			LOG.error ("Error al obtener el video", e);
			return "";
		}

		int typeid  = videoResource.getTypeId();
		String type = "";
		try {
			 type = OpenCms.getResourceManager().getResourceType(typeid).getTypeName();
		} catch (CmsLoaderException e) {
			LOG.error ("Error al obtener el tipo de video", e);
			return "";
		}
		
		if (type.equals("video-link")){ 
			String embedCode =  config.getParam(siteName, idPublication, "videos", "embedCode", "");
			
			String urlPathEmbedCodeFormat =  config.getParam(siteName, idPublication, "videos", "urlPathEmbedCodeFormat", "");
			String urlPathEmbedCodeRegExp = config.getParam(siteName, idPublication, "videos", "urlPathEmbedCodeRegExp", "");
	
			if (embedCode.equals("") || urlPathEmbedCodeFormat.equals("") ||  urlPathEmbedCodeRegExp.equals("")){
				LOG.info("No están configurados los parametros embedCode, urlPathEmbedCodeFormat y/o urlPathEmbedCodeRegExp dentro de videos");
				return "";
			}
			
			String link = videoPath.replaceAll(urlPathEmbedCodeRegExp, urlPathEmbedCodeFormat);
					
			return embedCode.replace("[EMBEDCODE]",  link);
			
		} else
			return "";
		
		
	}
	
	public static String formatURLName(String urlName)
	{
		if (urlName != null)
			return urlName
				.replaceAll(" ", "-")
				.replaceAll("á", "a")
				.replaceAll("é", "e")
				.replaceAll("í", "i")
				.replaceAll("ó", "o")
				.replaceAll("ú", "u")
				.replaceAll("Á", "A")
				.replaceAll("É", "E")
				.replaceAll("Í", "I")
				.replaceAll("Ó", "O")
				.replaceAll("ü", "u")
				.replaceAll("Ü", "U")
				.replaceAll("ñ", "n")
				.replaceAll("Ñ", "N")
				.replaceAll("[^a-zA-Z0-9\\.\\-]","");
		return null;
	}
}
