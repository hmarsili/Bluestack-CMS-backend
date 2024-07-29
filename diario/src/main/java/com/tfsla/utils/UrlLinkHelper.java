package com.tfsla.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspTagLink;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteMatcher;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.NoticiasService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;

/**
 * Permite obtener la url de la noticia en sus diferentes variantes
 * @author Victor Podberezski
 *
 */
public class UrlLinkHelper {
	
	public static final String URLFRIENDLY_PROPERTY = "urlFriendly";
	public static final String URLEXTERNAL_PROPERTY = "urlRedirect";
	public static final String TITLE_PROPERTY = "title";

	private static CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
	
	protected static final Log LOG = CmsLog.getLog(UrlLinkHelper.class);

	/**
	 * Obtiene la url externa de la noticia si la tiene definida.
	 * @param resource (CmsResource) - noticia
	 * @param cmsObject (CmsObject)
	 * @return url externo o vacio (String)
	 */
	public static String getExternalLink(CmsResource resource, CmsObject cmsObject) {
		String urlLink = "";
		try {
			CmsProperty prop = cmsObject.readPropertyObject(resource, URLEXTERNAL_PROPERTY, false);

			if (prop.getValue() != null && !prop.getValue().isEmpty()) {
				urlLink = prop.getValue();
				TipoEdicion pubActual = getPublicacionActual(cmsObject.getRequestContext().getUri(),cmsObject);
				
				String protocol = config.getParam(OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot(), String.valueOf( pubActual.getId()), "newsTags", "navigationProtocol");
				if (!urlLink.contains("http://") && !urlLink.contains("https://")){
					if (protocol != null) {
						urlLink =  protocol +  "://" + urlLink;
					} else
						urlLink = "http://" + urlLink;
				}
			}
		} catch (CmsException e) {
			e.printStackTrace();
		}
		
		return urlLink;
	}
	
	public static String getItemNewsUrlFriendlyLink(CmsResource resource, int itemNumber, CmsObject cmsObject, ServletRequest req) {
		return getUrlFriendlyLink(resource, cmsObject, req,true, false);
	}

	public static String getItemNewsUrlFriendlyLink(CmsResource resource, int itemNumber, CmsObject cmsObject, ServletRequest req, boolean relative) {
		String linkName = CmsResourceUtils.getLink(resource);
		try {
			String titleNews = getTitleURL(resource,cmsObject);
			String seccion = cmsObject.readPropertyObject(resource, "seccion", false).getValue();
			if (seccion==null)
				seccion="noticia";
			
			LinkedHashMap<String,String> variables = new LinkedHashMap<String, String>();
			variables.put("{seccion}", seccion);
			variables.put("{titleNews}", titleNews);
			variables.put("{itemNumber}", Integer.toString(itemNumber));
			
			String link = "";
						
			NoticiasService nService = new NoticiasService();
			TipoEdicion publication = getPublicacion(resource,cmsObject);
			int idPublication = publication.getId();
			TipoEdicion pubActual = getPublicacionActual(cmsObject.getRequestContext().getUri(),cmsObject);
			
			String customDomain = "/";
            boolean hasCustomDomain = false;

			if (cmsObject.getRequestContext().currentProject().isOnlineProject()) {
				LOG.debug("UrlLinkHelperItemNews / " + resource.getRootPath() + " en " + publication.getNombre() + " - mostrado en " + pubActual.getNombre());
            	customDomain = publication.getCustomDomain();
            	hasCustomDomain = customDomain != null && !customDomain.trim().equals("");
            	customDomain = (customDomain == null || customDomain.trim().equals("") ? "/" : customDomain + "/");
                if (publication.getId() == pubActual.getId()) {
                    customDomain = "/";
                }
			}
            				
			customDomain = customDomain.trim();
			
			if (!relative && customDomain.equals("/")) {
				customDomain = getPublicationDomain(resource, cmsObject) + "/";
				LOG.debug("UrlLinkHelperItemNews / dominio absoluto " + resource.getRootPath() + " dominio " + customDomain);
			}
			
			List<String> tiposNoticia = nService.obtenerTiposDeNoticia(cmsObject, idPublication);
			if (tiposNoticia!=null && tiposNoticia.size() > 0 && !tiposNoticia.get(0).equals("")) {
				for (String tipoNoticia: tiposNoticia) {
					String path = nService.obtenerPathTipoDeNoticia(cmsObject, idPublication, tipoNoticia);
					if (linkName.startsWith(path + "/")) {
						
						String urlFriendlyFormatListItem = nService.obtenerUrlFriendlyFormatTipoDeNoticiaListItem(cmsObject, idPublication, tipoNoticia);
						String urlFriendlyRegExpListItem = nService.obtenerUrlFriendlyRegExpTipoDeNoticiaListItem(cmsObject, idPublication, tipoNoticia);

						link = getTransformedLink(resource, cmsObject,
								linkName, variables, urlFriendlyFormatListItem,
								urlFriendlyRegExpListItem);
						
						LOG.debug("UrlLinkHelperItemNews / " + resource.getRootPath() + " > " + customDomain + link);
	                    String fullLink = joinLinkWithDomain(req, relative,
								link, customDomain, hasCustomDomain);
	                    return fullLink;
					}
				}
			}
			
			String urlFriendlyFormatListItem = nService.obtenerUrlFriendlyFormatNoticiaListItem(cmsObject, idPublication);
			String urlFriendlyRegExpListItem = nService.obtenerUrlFriendlyRegExpNoticiaListItem(cmsObject, idPublication);

			link = getTransformedLink(resource, cmsObject, linkName, variables,
					urlFriendlyFormatListItem, urlFriendlyRegExpListItem);
			
			LOG.debug("UrlLinkHelperItemNews / " + resource.getRootPath() + " > " + customDomain + link);
            String fullLink = joinLinkWithDomain(req, relative, link,
					customDomain, hasCustomDomain);
            return fullLink;
		} catch (CmsException e) {
			e.printStackTrace();
			return linkName;
		}
	}
	
	private static String joinLinkWithDomain(ServletRequest request,
			boolean relative, String link, String customDomain,
			boolean hasCustomDomain) {
		String fullLink = customDomain + link;
		if (relative && !hasCustomDomain) {
		    fullLink = CmsJspTagLink.linkTagAction(fullLink, request);
		}
		return fullLink;
	}

	private static String joinLinkWithDomain(
			CmsObject cmsObject, boolean relative, String link, String customDomain,
			boolean hasCustomDomain) {
		String fullLink = customDomain + link;
		if (relative && !hasCustomDomain) {
		    fullLink = OpenCms.getLinkManager().substituteLinkForUnknownTarget(cmsObject,fullLink);    		
		}
		return fullLink;
	}

	private static String getTransformedLink(CmsResource resource,
			CmsObject cmsObject, String linkName,
			LinkedHashMap<String, String> variables, String urlFriendlyFormat,
			String urlFriendlyRegExp) {
		String link;
		link = linkName.replaceAll(urlFriendlyRegExp, urlFriendlyFormat);
		
		link = replaceVariables(link,resource,cmsObject);
		link = replaceVariables(link,variables);
		
		if (link.startsWith("/"))
			link = link.replaceFirst("/", "");
		return link;
	}
	
	/**
	 * Obtiene la url amigable de la noticia utilizando el campo urlFriendly o el titulo de la noticia (si el primero no esta disponible)
	 * @param resource (CmsResource) - noticia
	 * @param cmsObject (CmsObject)
	 * @param req (ServletRequest)
	 * @return urlfriendly (String)
	 */
	public static String getUrlFriendlyLink(CmsResource resource, CmsObject cmsObject, ServletRequest req) {
		return getUrlFriendlyLink(resource, cmsObject, req,true, false);
	}

	public static String getUrlFriendlyLink(CmsResource resource, CmsObject cmsObject, ServletRequest req, boolean relative) {
		return getUrlFriendlyLink(resource, cmsObject, req, relative, false);
	}
	
	/**
	 * Obtiene la url amigable de la noticia utilizando el campo urlFriendly o el titulo de la noticia (si el primero no esta disponible)
	 * @param resource (CmsResource) - noticia
	 * @param cmsObject (CmsObject)
	 * @param req (ServletRequest)
	 * @param relative (boolean)
	 * @return urlfriendly (String)
	 * @return publicUrl (boolean)
	 */	
	public static String getUrlFriendlyLink(CmsResource resource, CmsObject cmsObject, ServletRequest req, boolean relative, boolean publicUrl) {
		String linkName = CmsResourceUtils.getLink(resource);
		try {
			String titleNews = getTitleURL(resource,cmsObject);
			String[] parts = linkName.split("/");
			
			String seccion = cmsObject.readPropertyObject(resource, "seccion", false).getValue();
			if (seccion==null)
				seccion="noticia";
			
			String link = "";
			
			NoticiasService nService = new NoticiasService();
			TipoEdicion publication = getPublicacion(resource,cmsObject);
			int idPublication = publication.getId();
			TipoEdicion pubActual = getPublicacionActual(cmsObject.getRequestContext().getUri(),cmsObject);
	
			String customDomain = "/";
            boolean hasCustomDomain = false;
            
			if (cmsObject.getRequestContext().currentProject().isOnlineProject()) {
				LOG.debug("UrlLinkHelper / " + resource.getRootPath() + " en " + publication.getNombre() + " - mostrado en " + pubActual.getNombre());
            	customDomain = publication.getCustomDomain();
            	hasCustomDomain = customDomain != null && !customDomain.trim().equals("");
            	customDomain = (customDomain == null || customDomain.trim().equals("") ? "/" : customDomain + "/");
               		
            	if (publication.getId() == pubActual.getId()) {
                    customDomain = "/";
                }
			}
				
			customDomain = customDomain.trim();

			if (!relative && customDomain.equals("/")) {
				customDomain = getPublicationDomain(resource, cmsObject, publicUrl) + "/";
				LOG.debug("UrlLinkHelper / dominio absoluto " + resource.getRootPath() + " dominio " + customDomain);
			}
			
				
			List<String> tiposNoticia = nService.obtenerTiposDeNoticia(cmsObject, idPublication);
			if (tiposNoticia!=null && tiposNoticia.size() > 0 && !tiposNoticia.get(0).equals("")) {
				for (String tipoNoticia: tiposNoticia) {
					String path = nService.obtenerPathTipoDeNoticia(cmsObject, idPublication, tipoNoticia);
					if (linkName.startsWith(path + "/")) {
						
						String urlFriendlyFormat = nService.obtenerUrlFriendlyFormatTipoDeNoticia(cmsObject, idPublication, tipoNoticia);
						String urlFriendlyRegExp = nService.obtenerUrlFriendlyRegExpTipoDeNoticia(cmsObject, idPublication, tipoNoticia);

						link = linkName.replaceAll(urlFriendlyRegExp, urlFriendlyFormat);
						link = replaceVariables(link,resource,cmsObject,titleNews);
						
						if (link.startsWith("/"))
							link = link.replaceFirst("/", "");
						
						String publication_part = parts[0] + "/";
						
						
						if(publication.getCustomDomain()==null || ( publication.getCustomDomain()!=null && publication.getCustomDomain().equals(""))){
							if(!publication_part.equals(tipoNoticia+"/"))
								link = publication_part +  link;
						}else{
							if(!publication_part.equals(tipoNoticia+"/") && (!cmsObject.getRequestContext().currentProject().isOnlineProject() && !publicUrl))
								link = publication_part +  link;
						}
							
						LOG.debug("UrlLinkHelper / " + resource.getRootPath() + " > " + customDomain + link);
	                   
						String fullLink = joinLinkWithDomain(req, relative,
								link, customDomain, hasCustomDomain);
	                    return fullLink;
					}
				}
			}
			
			if (linkName.matches("contenidos/[0-9]{4}/[0-9]{2}/[0-9]{2}/noticia_[0-9]{4,6}.html"))
				link = formatURLName(seccion) + "/" + titleNews + "-" + parts[1] + parts[2] + parts[3] + "-" + parts[4].replace("noticia_", "");
			else if (linkName.matches(".*./contenidos/[0-9]{4}/[0-9]{2}/[0-9]{2}/noticia_[0-9]{4,6}.html")) {
				link = formatURLName(seccion) + "/" + titleNews + "-" + parts[2] + parts[3] + parts[4] + "-" + parts[5].replace("noticia_", "");
				
				if(publication.getCustomDomain()==null || ( publication.getCustomDomain()!=null && publication.getCustomDomain().equals(""))){
					link = parts[0] + "/" + link;
				}else{
					if(!cmsObject.getRequestContext().currentProject().isOnlineProject() && !publicUrl)
						link = parts[0] + "/"  +  link;
				}
				
			} else if (linkName.matches(".*/[0-9]{4}/[0-9]{1,2}/edicion_[0-9]*/contenidos/noticia_[0-9]{4,6}.html")){
				link = formatURLName(seccion) + "/" + titleNews + "-" + parts[1] + parts[2] + "-" + parts[3].replace("edicion_", "") + "-" + parts[5].replace("noticia_", "");  
			
				if(publication.getCustomDomain()==null || ( publication.getCustomDomain()!=null && publication.getCustomDomain().equals(""))){
					link = parts[0] + "/" + link;
				}else{
					if(!cmsObject.getRequestContext().currentProject().isOnlineProject() && !publicUrl)
						link = parts[0] + "/"  +  link;
				}
			}else
				link = linkName;
			
			if (link.startsWith("/"))
				link = link.replaceFirst("/", "");
			
	
			LOG.debug("UrlLinkHelper / " + resource.getRootPath() + " > " + customDomain + link);
				
            String fullLink = joinLinkWithDomain(req, relative, link,
					customDomain, hasCustomDomain);
            return fullLink;

		} catch (CmsException e) {
			e.printStackTrace();
			return linkName;
		}
	}

	
	public static String getUrlFriendlyLink(CmsResource resource, CmsObject cmsObject, boolean relative, boolean publicUrl) {
		String linkName = CmsResourceUtils.getLink(resource);
		try {
			String titleNews = getTitleURL(resource,cmsObject);
			String[] parts = linkName.split("/");
			
			String seccion = cmsObject.readPropertyObject(resource, "seccion", false).getValue();
			if (seccion==null)
				seccion="noticia";
			
			String link = "";
						
			NoticiasService nService = new NoticiasService();
			TipoEdicion publication = getPublicacion(resource,cmsObject);
			int idPublication = publication.getId();
			TipoEdicion pubActual = getPublicacionActual(cmsObject.getRequestContext().getUri(),cmsObject);
			
			String customDomain = "/";
            boolean hasCustomDomain = false;

			if (cmsObject.getRequestContext().currentProject().isOnlineProject()) {
				LOG.debug("UrlLinkHelper / " + resource.getRootPath() + " en " + publication.getNombre() + " - mostrado en " + pubActual.getNombre());
            	customDomain = publication.getCustomDomain();
            	hasCustomDomain = customDomain != null && !customDomain.trim().equals("");
            	customDomain = (customDomain == null || customDomain.trim().equals("") ? "/" : customDomain + "/");
                if (publication.getId() == pubActual.getId()) {
                    customDomain = "/";
                }
			}
            				
			customDomain = customDomain.trim();
			
			if (!relative && customDomain.equals("/")) {
				customDomain = getPublicationDomain(resource, cmsObject, publicUrl) + "/";
				LOG.debug("UrlLinkHelper / dominio absoluto " + resource.getRootPath() + " dominio " + customDomain);
			}
			
			List<String> tiposNoticia = nService.obtenerTiposDeNoticia(cmsObject, idPublication);
			if (tiposNoticia!=null && tiposNoticia.size() > 0 && !tiposNoticia.get(0).equals("")) {
				for (String tipoNoticia: tiposNoticia) {
					String path = nService.obtenerPathTipoDeNoticia(cmsObject, idPublication, tipoNoticia);
					if (linkName.startsWith(path + "/")) {
						
						String urlFriendlyFormat = nService.obtenerUrlFriendlyFormatTipoDeNoticia(cmsObject, idPublication, tipoNoticia);
						String urlFriendlyRegExp = nService.obtenerUrlFriendlyRegExpTipoDeNoticia(cmsObject, idPublication, tipoNoticia);

						link = linkName.replaceAll(urlFriendlyRegExp, urlFriendlyFormat);
						link = replaceVariables(link,resource,cmsObject,titleNews);
						
						if (link.startsWith("/"))
							link = link.replaceFirst("/", "");
						
						LOG.debug("UrlLinkHelper / " + resource.getRootPath() + " > " + customDomain + link);
	                    String fullLink = joinLinkWithDomain(cmsObject,relative,
								link, customDomain, hasCustomDomain);
	                    return fullLink;
					}
				}
			}
			
			if (linkName.matches("contenidos/[0-9]{4}/[0-9]{2}/[0-9]{2}/noticia_[0-9]{4,6}.html"))
				link = formatURLName(seccion) + "/" + titleNews + "-" + parts[1] + parts[2] + parts[3] + "-" + parts[4].replace("noticia_", "");
			else if (linkName.matches(".*./contenidos/[0-9]{4}/[0-9]{2}/[0-9]{2}/noticia_[0-9]{4,6}.html")) {
				if (!publicUrl)
					link = (!hasCustomDomain  ? parts[0] + "/" : "" ) + formatURLName(seccion) + "/" + titleNews + "-" + parts[2] + parts[3] + parts[4] + "-" + parts[5].replace("noticia_", "");			
				else 
					link = ((config.getParam(OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot(), "", "newsTags", "publicUrl")+"/").equals(customDomain) ? parts[0] + "/" : "" ) + 
							formatURLName(seccion) + "/" + titleNews + "-" + parts[2] + parts[3] + parts[4] + "-" + parts[5].replace("noticia_", "");			
			} else if (linkName.matches(".*/[0-9]{4}/[0-9]{1,2}/edicion_[0-9]*/contenidos/noticia_[0-9]{4,6}.html")){
				if (!publicUrl)
					link = (!hasCustomDomain  ? parts[0] + "/" : "" ) + formatURLName(seccion) + "/" + titleNews + "-" + parts[1] + parts[2] + "-" + parts[3].replace("edicion_", "") + "-" + parts[5].replace("noticia_", "");  
				else
					link = ( (config.getParam(OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot(), "", "newsTags", "publicUrl")+"/").equals(customDomain) ? parts[0] + "/" : "" )
							+ formatURLName(seccion) + "/" + titleNews + "-" + parts[1] + parts[2] + "-" + parts[3].replace("edicion_", "") + "-" + parts[5].replace("noticia_", "");  
			}else
				link = linkName;
			
			if (link.startsWith("/"))
				link = link.replaceFirst("/", "");
			
			LOG.debug("UrlLinkHelper / " + resource.getRootPath() + " > " + customDomain + link);
            String fullLink = joinLinkWithDomain(cmsObject, relative, link,
					customDomain, hasCustomDomain);
            return fullLink;

		} catch (CmsException e) {
			e.printStackTrace();
			return linkName;
		}
	}

	/**
	 * Obtiene la url amigable de la noticia utilizando el campo urlFriendly o el titulo de la noticia (si el primero no esta disponible)
	 * @param resource (CmsResource) - noticia
	 * @param cmsObject (CmsObject)
	 * @param req (ServletRequest)
	 * @param relative (boolean)
	 * @return urlfriendly (String)
	 */	
	public static String getRelativeUrlFriendlyLink(CmsResource resource, CmsObject cmsObject) {
		String linkName = CmsResourceUtils.getLink(resource);
		try {
			String titleNews = getTitleURL(resource,cmsObject);
			String[] parts = linkName.split("/");
			
			String seccion = cmsObject.readPropertyObject(resource, "seccion", false).getValue();
			if (seccion==null)
				seccion="noticia";
			
			String link = "";
						
			NoticiasService nService = new NoticiasService();
			TipoEdicion publication = getPublicacion(resource,cmsObject);
			int idPublication = publication.getId();
		
			boolean hasCustomDomain = false;

			if (cmsObject.getRequestContext().currentProject().isOnlineProject()) {
            	String customDomain = publication.getCustomDomain();
            	hasCustomDomain = customDomain != null && !customDomain.trim().equals("");            	
			}
            							
			List<String> tiposNoticia = nService.obtenerTiposDeNoticia(cmsObject, idPublication);
			if (tiposNoticia!=null && tiposNoticia.size() > 0 && !tiposNoticia.get(0).equals("")) {
				for (String tipoNoticia: tiposNoticia) {
					String path = nService.obtenerPathTipoDeNoticia(cmsObject, idPublication, tipoNoticia);
					if (linkName.startsWith(path + "/")) {
						
						String urlFriendlyFormat = nService.obtenerUrlFriendlyFormatTipoDeNoticia(cmsObject, idPublication, tipoNoticia);
						String urlFriendlyRegExp = nService.obtenerUrlFriendlyRegExpTipoDeNoticia(cmsObject, idPublication, tipoNoticia);

						link = linkName.replaceAll(urlFriendlyRegExp, urlFriendlyFormat);
						
						link = replaceVariables(link,resource,cmsObject,titleNews);
						
						
						if (link.startsWith("/"))
							link = link.replaceFirst("/", "");
						
						LOG.debug("UrlLinkHelper / " + resource.getRootPath() + " > " + link);
	                   
	                    return link;
					}
				}
			}
			
			if (linkName.matches("contenidos/[0-9]{4}/[0-9]{2}/[0-9]{2}/noticia_[0-9]{4,6}.html"))
				link = formatURLName(seccion) + "/" + titleNews + "-" + parts[1] + parts[2] + parts[3] + "-" + parts[4].replace("noticia_", "");
			else if (linkName.matches(".*./contenidos/[0-9]{4}/[0-9]{2}/[0-9]{2}/noticia_[0-9]{4,6}.html"))
				link = (!hasCustomDomain  ? parts[0] + "/" : "" ) + formatURLName(seccion) + "/" + titleNews + "-" + parts[2] + parts[3] + parts[4] + "-" + parts[5].replace("noticia_", "");			
			else if (linkName.matches(".*/[0-9]{4}/[0-9]{1,2}/edicion_[0-9]*/contenidos/noticia_[0-9]{4,6}.html"))
				link = (!hasCustomDomain ? parts[0] + "/" : "" ) + formatURLName(seccion) + "/" + titleNews + "-" + parts[1] + parts[2] + "-" + parts[3].replace("edicion_", "") + "-" + parts[5].replace("noticia_", "");  
			else
				link = linkName;
			
			if (link.startsWith("/"))
				link = link.replaceFirst("/", "");
			
			LOG.debug("UrlLinkHelper / " + resource.getRootPath() + " > " + link);
            
            return link;
		} catch (CmsException e) {
			e.printStackTrace();
			return linkName;
		}
	}
	
	public static String getCanonicalLink(CmsResource resource, CmsObject cmsObject, ServletRequest req) {
		String linkName = CmsResourceUtils.getLink(resource);
		try {
			CmsFile file = cmsObject.readFile(resource);
			String titleNews = getCanonicalURL(file,cmsObject);
			String[] parts = linkName.split("/");
			
			String seccion = cmsObject.readPropertyObject(resource, "seccion", false).getValue();
			if (seccion==null)
				seccion="noticia";
			
			String link = "";
						
			NoticiasService nService = new NoticiasService();
			TipoEdicion publication = getPublicacion(resource,cmsObject);
			int idPublication = publication.getId();
			TipoEdicion pubActual = getPublicacionActual(cmsObject.getRequestContext().getUri(),cmsObject);
			LOG.debug("UrlLinkHelper / publicacion " + idPublication + " actual " + pubActual.getId());
			
			String customDomain = "/";
            boolean hasCustomDomain = false;

			//if (cmsObject.getRequestContext().currentProject().isOnlineProject()) {
			LOG.debug("UrlLinkHelper / " + resource.getRootPath() + " en " + publication.getNombre() + " - mostrado en " + pubActual.getNombre());
            
			customDomain = publication.getCustomDomain();
            hasCustomDomain = customDomain != null && !customDomain.trim().equals("");
            customDomain = (customDomain == null || customDomain.trim().equals("") ? "/" : customDomain + "/");
            if (publication.getId() == pubActual.getId()) {
                    customDomain = "/";
            }
			//}
            				
			customDomain = customDomain.trim();
			
			customDomain = getPublicationDomain(resource, cmsObject) + "/";
			LOG.debug("UrlLinkHelper / dominio absoluto " + resource.getRootPath() + " dominio " + customDomain);
			
			String protocol = config.getParam(OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot(), String.valueOf(pubActual.getId()), "newsTags", "navigationProtocol");
						
			List<String> tiposNoticia = nService.obtenerTiposDeNoticia(cmsObject, idPublication);
			if (tiposNoticia!=null && tiposNoticia.size() > 0 && !tiposNoticia.get(0).equals("")) {
				for (String tipoNoticia: tiposNoticia) {
					String path = nService.obtenerPathTipoDeNoticia(cmsObject, idPublication, tipoNoticia);
					if (linkName.startsWith(path + "/")) {
						
						String urlFriendlyFormat = nService.obtenerUrlFriendlyFormatTipoDeNoticia(cmsObject, idPublication, tipoNoticia);
						String urlFriendlyRegExp = nService.obtenerUrlFriendlyRegExpTipoDeNoticia(cmsObject, idPublication, tipoNoticia);

						link = linkName.replaceAll(urlFriendlyRegExp, urlFriendlyFormat);
						link = replaceVariables(link,resource,cmsObject,titleNews);
						
						if (link.startsWith("/"))
							link = link.replaceFirst("/", "");
						
						if(!hasCustomDomain && publication.getNombre().equals(parts[0]))
							link = parts[0]+"/"+link;
							
						LOG.debug("UrlLinkHelper / " + resource.getRootPath() + " > " + customDomain + link);
	                    String fullLink = joinLinkWithDomain(req, false,
								link, customDomain, hasCustomDomain);
	                    if (protocol != null)
	        				fullLink = protocol + (fullLink.contains("://") ? "" : "://") + fullLink.replace("https", "").replace("http", "");
	        			
	                    return fullLink;
					}
				}
			}
			
			if (linkName.matches("contenidos/[0-9]{4}/[0-9]{2}/[0-9]{2}/noticia_[0-9]{4,6}.html"))
				link = formatURLName(seccion) + "/" + titleNews + "-" + parts[1] + parts[2] + parts[3] + "-" + parts[4].replace("noticia_", "");
			else if (linkName.matches(".*./contenidos/[0-9]{4}/[0-9]{2}/[0-9]{2}/noticia_[0-9]{4,6}.html"))
				link = (!hasCustomDomain  ? parts[0] + "/" : "" ) + formatURLName(seccion) + "/" + titleNews + "-" + parts[2] + parts[3] + parts[4] + "-" + parts[5].replace("noticia_", "");			
			else if (linkName.matches(".*/[0-9]{4}/[0-9]{1,2}/edicion_[0-9]*/contenidos/noticia_[0-9]{4,6}.html"))
				link = (!hasCustomDomain ? parts[0] + "/" : "" ) + formatURLName(seccion) + "/" + titleNews + "-" + parts[1] + parts[2] + "-" + parts[3].replace("edicion_", "") + "-" + parts[5].replace("noticia_", "");  
			else
				link = linkName;
			
			if (link.startsWith("/"))
				link = link.replaceFirst("/", "");
			
			LOG.debug("UrlLinkHelper / " + resource.getRootPath() + " > " + customDomain + link);
            String fullLink = joinLinkWithDomain(req, false, link,
					customDomain, hasCustomDomain);
            if (protocol != null)
				fullLink = protocol + (fullLink.contains("://") ? "" : "://") + fullLink.replace("https", "").replace("http", "");
			return fullLink;

		} catch (CmsException e) {
			e.printStackTrace();
			return linkName;
		}
	}

	private static String replaceVariables(String link, LinkedHashMap<String,String> variables) {
		for (String var : variables.keySet())
			link = link.replaceAll(Pattern.quote(var), formatURLName(variables.get(var)));
		 
		return link;
	}

	private static String replaceVariables(String link, CmsResource resource, CmsObject cmsObject) {
		return replaceVariables(link, resource,cmsObject, null);
	}
	
	private static String replaceVariables(String link, CmsResource resource, CmsObject cmsObject, String title) {
		Pattern pattern = Pattern.compile("\\{prop\\.[^\\{]*\\}");
		Matcher matcher = pattern.matcher(link);
		List<String> presentVariables = new ArrayList<String>();
		while (matcher.find())
			if (!presentVariables.contains(matcher.group()))
				presentVariables.add(matcher.group());
		for (String var : presentVariables) {
			String propName = var.replace("{prop.", "").replace("}", "");
			String value=null;
			try {
				if (propName.equals("Title") && title != null ) {
					value = title;
				} else 
					value = cmsObject.readPropertyObject(resource, propName, false).getValue();
			} catch (CmsException e) {
				e.printStackTrace();
			}
			if (value==null)
				value = "";
			
			link = link.replaceAll(Pattern.quote(var), formatURLName(value));
		}
		
		return link;
	}

	public static String getPublicationDomain(CmsResource resource, CmsObject cmsObject){
		return getPublicationDomain( resource, cmsObject, false);
	}
	
	public static String getPublicationDomain(CmsResource resource, CmsObject cmsObject, boolean publicUrl) {
		TipoEdicion tEdicion = getPublicacion(resource,cmsObject);
		
		String customDomain = tEdicion.getCustomDomain(); 
		if(customDomain==null || customDomain.trim().equals(""))
			if (publicUrl){
				customDomain = config.getParam(OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot(), "", "newsTags", "publicUrl");
			} else
				customDomain = OpenCms.getSiteManager().getCurrentSite(cmsObject).getUrl();
		return customDomain;
	}
	
	private static TipoEdicion getPublicacion(CmsResource resource, CmsObject cmsObject) {
		TipoEdicion tEdicion  = null;
		TipoEdicionService tEService = new TipoEdicionService();
		try {
			tEdicion = tEService.obtenerTipoEdicion(cmsObject,cmsObject.getSitePath(resource));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return tEdicion;
	}

	private static TipoEdicion getPublicacionActual(String path, CmsObject cmsObject) {
		TipoEdicion tEdicion  = null;
		TipoEdicionService tEService = new TipoEdicionService();
		try {
			tEdicion = tEService.obtenerTipoEdicion(cmsObject, path);
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return tEdicion;
	}
	
	static String getTitleURL(CmsResource resource, CmsObject cmsObject) throws CmsException {
		String urlName = "";
		CmsProperty prop = cmsObject.readPropertyObject(resource, URLFRIENDLY_PROPERTY, false);
		if (prop.getValue() != null && !prop.getValue().isEmpty()) {
			urlName = formatURLName(prop.getValue());
		} else {
			prop = cmsObject.readPropertyObject(resource, TITLE_PROPERTY, false);
			if (prop.getValue() != null && !prop.getValue().isEmpty()) {
				urlName = formatURLName(prop.getValue());
			}
		}

		return urlName;
	}
	
	static String getCanonicalURL(CmsFile file, CmsObject cmsObject) throws CmsException {
		CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmsObject, file);

		String urlName = content.getStringValue(cmsObject, "canonical[1]", cmsObject.getRequestContext().getLocale());
		if (urlName==null || urlName.trim().length()==0) {
			CmsProperty prop = cmsObject.readPropertyObject(file, TITLE_PROPERTY, false);
			if (prop.getValue() != null && !prop.getValue().isEmpty()) {
				urlName = prop.getValue();
			}
		}

		return formatURLName(urlName);
	}	
	
	private static String formatURLName(String urlName) {
		return urlName
			.replaceAll(" ", "-")
			.replaceAll("á", "a")
			.replaceAll("à", "a")
			.replaceAll("â", "a")
			.replaceAll("ã", "a")
			.replaceAll("é", "e")
			.replaceAll("ê", "e")
			.replaceAll("í", "i")
			.replaceAll("ó", "o")
			.replaceAll("ô", "o")
			.replaceAll("õ", "o")
			.replaceAll("ú", "u")
			.replaceAll("Á", "A")
			.replaceAll("À", "A")
			.replaceAll("Â", "A")
			.replaceAll("Ã", "A")
			.replaceAll("É", "E")
			.replaceAll("Ê", "E")
			.replaceAll("Í", "I")
			.replaceAll("Ó", "O")
			.replaceAll("Ô", "O")
			.replaceAll("Õ", "O")
			.replaceAll("Ú", "U")
			.replaceAll("ü", "u")
			.replaceAll("Ü", "U")
			.replaceAll("ñ", "n")
			.replaceAll("Ñ", "N")
			.replaceAll("ç", "c")
			.replaceAll("Ç", "C")
			.replaceAll("[^a-zA-Z0-9\\.\\-]","");
	}
	
	public String getResourcePath(CmsObject cms, String url) {
		TipoEdicion resourcePublicacion = null;
		
		String urlAux = url;
		
		
		TipoEdicionService tEService = new TipoEdicionService();
		
		if (urlAux.contains("//"))
			urlAux = urlAux.substring(urlAux.indexOf("//"));
		
		//primero me fijo si la publicacion tiene un dominio propio.
		String domain = urlAux.substring(0,urlAux.indexOf("/"));
		List<TipoEdicion> publicaciones = tEService.obtenerTipoEdiciones();
		for (TipoEdicion publicacion : publicaciones) {
			String pubDomain = publicacion.getCustomDomain();
			if (pubDomain.equals(domain)) {
				resourcePublicacion = publicacion;
				break;
			}
				
		}
		
		
		//Si no es una publicacion con un dominio propio me fijo si es un sitio del opencms.
		if (resourcePublicacion==null) {
			CmsSiteMatcher matcher = new CmsSiteMatcher(domain);
			CmsSite site = OpenCms.getSiteManager().matchSite(matcher);
		}
		
		//Si no tiene publicacon con dominio propio o es la principal, me fijo si el primer path es el nombre de la publicacion.
		if (resourcePublicacion==null || resourcePublicacion.isOnline()) {
			
		}
		
		return null;
		
	}
	
	
	/****
	 * Canonical para Eventos
	 */
	public static String getCanonicalEventosLink(CmsResource resource, CmsObject cmsObject, ServletRequest req) {
		String linkName = CmsResourceUtils.getLink(resource);
		try {
			CmsFile file = cmsObject.readFile(resource);
			String titleNews = getCanonicalURL(file,cmsObject);
			String[] parts = linkName.split("/");
			
			String link = "";
						
			TipoEdicion publication = getPublicacion(resource,cmsObject);
			TipoEdicion pubActual = getPublicacionActual(cmsObject.getRequestContext().getUri(),cmsObject);
			LOG.debug("UrlLinkHelper / publicacion " + " actual " + pubActual.getId());
			
			String customDomain = "/";
            boolean hasCustomDomain = false;

			if (cmsObject.getRequestContext().currentProject().isOnlineProject()) {
				LOG.debug("UrlLinkHelper / " + resource.getRootPath() + " en " + publication.getNombre() + " - mostrado en " + pubActual.getNombre());
            	customDomain = publication.getCustomDomain();
            	hasCustomDomain = customDomain != null && !customDomain.trim().equals("");
            	customDomain = (customDomain == null || customDomain.trim().equals("") ? "/" : customDomain + "/");
                if (publication.getId() == pubActual.getId()) {
                    customDomain = "/";
                }
			}
            				
			customDomain = customDomain.trim();
			
			customDomain = getPublicationDomain(resource, cmsObject) + "/";
			LOG.debug("UrlLinkHelper / dominio absoluto " + resource.getRootPath() + " dominio " + customDomain);
			
			String protocol = config.getParam(OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot(), String.valueOf(pubActual.getId()), "newsTags", "navigationProtocol");
						
		
			
			if (linkName.matches("eventos/[0-9]{4}/[0-9]{2}/[0-9]{2}/evento_[0-9]{4,6}.html"))
				link =  titleNews + "-e" + parts[1] + parts[2] + parts[3] + "-" + parts[4].replace("evento_", "");
			else if (linkName.matches(".*./eventos/[0-9]{4}/[0-9]{2}/[0-9]{2}/evento_[0-9]{4,6}.html"))
				link = (!hasCustomDomain  ? parts[0] + "/" : "" ) + titleNews + "-e" + parts[2] + parts[3] + parts[4] + "-" + parts[5].replace("evento_", "");			
			else if (linkName.matches(".*/[0-9]{4}/[0-9]{1,2}/edicion_[0-9]*/eventos/evento_[0-9]{4,6}.html"))
				link = (!hasCustomDomain ? parts[0] + "/" : "" ) +  titleNews + "-e" + parts[1] + parts[2] + "-" + parts[3].replace("edicion_", "") + "-" + parts[5].replace("evento_", "");  
			else
				link = linkName;
			
			if (link.startsWith("/"))
				link = link.replaceFirst("/", "");
			
			LOG.debug("UrlLinkHelper / " + resource.getRootPath() + " > " + customDomain + link);
            String fullLink = joinLinkWithDomain(req, false, link,
					customDomain, hasCustomDomain);
            if (protocol != null)
				fullLink = protocol + (fullLink.contains("://") ? "" : "://") + fullLink.replace("https", "").replace("http", "");
			return fullLink;

		} catch (CmsException e) {
			e.printStackTrace();
			return linkName;
		}
	}
	
	
	
			
	/**
	 * Obtiene la url amigable del evento utilizando el campo urlFriendly o el titulo de la noticia (si el primero no esta disponible)
	 * @param resource (CmsResource) - noticia
	 * @param cmsObject (CmsObject)
	 * @param req (ServletRequest)
	 * @param relative (boolean)
	 * @return urlfriendly (String)
	 * @return publicUrl (boolean)
	 */	
	public static String getUrlFriendlyEventosLink(CmsResource resource, CmsObject cmsObject, ServletRequest req, boolean relative, boolean publicUrl) {
		String linkName = CmsResourceUtils.getLink(resource);
		try {
			String titleNews = getTitleURL(resource,cmsObject);
			String[] parts = linkName.split("/");
					
			String link = "";
						
			TipoEdicion publication = getPublicacion(resource,cmsObject);
			TipoEdicion pubActual = getPublicacionActual(cmsObject.getRequestContext().getUri(),cmsObject);
			
			String customDomain = "/";
            boolean hasCustomDomain = false;

			if (cmsObject.getRequestContext().currentProject().isOnlineProject()) {
				LOG.debug("UrlLinkHelper / " + resource.getRootPath() + " en " + publication.getNombre() + " - mostrado en " + pubActual.getNombre());
            	customDomain = publication.getCustomDomain();
            	hasCustomDomain = customDomain != null && !customDomain.trim().equals("");
            	customDomain = (customDomain == null || customDomain.trim().equals("") ? "/" : customDomain + "/");
                if (publication.getId() == pubActual.getId()) {
                    customDomain = "/";
                }
			}
            				
			customDomain = customDomain.trim();
			
			if (!relative && customDomain.equals("/")) {
				customDomain = getPublicationDomain(resource, cmsObject, publicUrl) + "/";
				LOG.debug("UrlLinkHelper / dominio absoluto " + resource.getRootPath() + " dominio " + customDomain);
			}
			
				
			if (linkName.matches("eventos/[0-9]{4}/[0-9]{2}/[0-9]{2}/evento_[0-9]{4,6}.html"))
				link =  "/" + titleNews + "-e" + parts[1] + parts[2] + parts[3] + "-" + parts[4].replace("evento_", "");
			else if (linkName.matches(".*./eventos/[0-9]{4}/[0-9]{2}/[0-9]{2}/evento_[0-9]{4,6}.html")) {
				if (!publicUrl)
					link = (!hasCustomDomain  ? parts[0] + "/" : "" )  + titleNews + "-e" + parts[2] + parts[3] + parts[4] + "-" + parts[5].replace("evento_", "");			
				else 
					link = ((config.getParam(OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot(), "", "newsTags", "publicUrl")+"/").equals(customDomain) ? parts[0] + "/" : "" ) + 
							  titleNews + "-e" + parts[2] + parts[3] + parts[4] + "-" + parts[5].replace("evento_", "");			
			} else if (linkName.matches(".*/[0-9]{4}/[0-9]{1,2}/edicion_[0-9]*/eventos/evento_[0-9]{4,6}.html")){
				if (!publicUrl)
					link = (!hasCustomDomain  ? parts[0] +"/" : "" ) + titleNews + "-e" + parts[1] + parts[2] + "-" + parts[3].replace("edicion_", "") + "-" + parts[5].replace("evento_", "");  
				else
					link = ( (config.getParam(OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot(), "", "newsTags", "publicUrl")+"/").equals(customDomain) ? parts[0] + "/" : "" )
							 + titleNews + "-e" + parts[1] + parts[2] + "-" + parts[3].replace("edicion_", "") + "-" + parts[5].replace("evento_", "");  
			}else
				link = linkName;
			
			if (link.startsWith("/"))
				link = link.replaceFirst("/", "");
			
			LOG.debug("UrlLinkHelper / " + resource.getRootPath() + " > " + customDomain + link);
            String fullLink = joinLinkWithDomain(req, relative, link,
					customDomain, hasCustomDomain);
            return fullLink;

		} catch (CmsException e) {
			e.printStackTrace();
			return linkName;
		}
	}
	
	
	public static String getCanonicalLinkNoNews(CmsResource resource, CmsObject cmsObject, ServletRequest req,String module, String urlFormat,String urlFriendlyRegex) {
		String linkName = CmsResourceUtils.getLink(resource);
		try {
			CmsFile file = cmsObject.readFile(resource);
			String titleNews = getCanonicalURL(file,cmsObject);
			String link = "";
						
			TipoEdicion publication = getPublicacion(resource,cmsObject);
			int idPublication = publication.getId();
			TipoEdicion pubActual = getPublicacionActual(cmsObject.getRequestContext().getUri(),cmsObject);
			LOG.debug("UrlLinkHelper / publicacion " + idPublication + " actual " + pubActual.getId());
			
			String customDomain = "/";
            boolean hasCustomDomain = false;

			if (cmsObject.getRequestContext().currentProject().isOnlineProject()) {
				LOG.debug("UrlLinkHelper / " + resource.getRootPath() + " en " + publication.getNombre() + " - mostrado en " + pubActual.getNombre());
            	customDomain = publication.getCustomDomain();
            	hasCustomDomain = customDomain != null && !customDomain.trim().equals("");
            	customDomain = (customDomain == null || customDomain.trim().equals("") ? "/" : customDomain + "/");
                if (publication.getId() == pubActual.getId()) {
                    customDomain = "/";
                }
			}
            				
			customDomain = customDomain.trim();
			
			customDomain = getPublicationDomain(resource, cmsObject) + "/";
			LOG.debug("UrlLinkHelper / dominio absoluto " + resource.getRootPath() + " dominio " + customDomain);
			
			String protocol = config.getParam(OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot(), String.valueOf(pubActual.getId()), "newsTags", "navigationProtocol");
				
			String siteName = OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot();

			CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
			String urlFriendlyFormat = config.getParam(siteName, "" + idPublication, module,urlFormat,"");
			String urlFriendlyRegExp =config.getParam(siteName, "" + idPublication, module,urlFriendlyRegex,"");
			
			link = linkName.replaceAll(urlFriendlyRegExp, urlFriendlyFormat);
			link = replaceVariables(link,resource,cmsObject,titleNews);
			
			if (link.startsWith("/"))
				link = link.replaceFirst("/", "");
			
			LOG.debug("UrlLinkHelper / " + resource.getRootPath() + " > " + customDomain + link);
            String fullLink = joinLinkWithDomain(req, false,
					link, customDomain, hasCustomDomain);
            if (protocol != null)
				fullLink = protocol + (fullLink.contains("://") ? "" : "://") + fullLink.replace("https", "").replace("http", "");
			
            return fullLink;
			
		} catch (CmsException e) {
			e.printStackTrace();
			return linkName;
		}
	}
	
	public static String getUrlFriendlyLinkRegex (CmsResource resource, CmsObject cmsObject, boolean relative, 
			boolean publicUrl,String module, String urlFormat,String urlFriendlyRegex) {
		String linkName = CmsResourceUtils.getLink(resource);
		try {
			String titleNews = getTitleURL(resource,cmsObject);
			String link = "";
						
			TipoEdicion publication = getPublicacion(resource,cmsObject);
			int idPublication = publication.getId();
			TipoEdicion pubActual = getPublicacionActual(cmsObject.getRequestContext().getUri(),cmsObject);
			
			String customDomain = "/";
            boolean hasCustomDomain = false;

			if (cmsObject.getRequestContext().currentProject().isOnlineProject()) {
				LOG.debug("UrlLinkHelper / " + resource.getRootPath() + " en " + publication.getNombre() + " - mostrado en " + pubActual.getNombre());
            	customDomain = publication.getCustomDomain();
            	hasCustomDomain = customDomain != null && !customDomain.trim().equals("");
            	customDomain = (customDomain == null || customDomain.trim().equals("") ? "/" : customDomain + "/");
                if (publication.getId() == pubActual.getId()) {
                    customDomain = "/";
                }
			}
            				
			customDomain = customDomain.trim();
			
			if (!relative && customDomain.equals("/")) {
				customDomain = getPublicationDomain(resource, cmsObject, publicUrl) + "/";
				LOG.debug("UrlLinkHelper / dominio absoluto " + resource.getRootPath() + " dominio " + customDomain);
			}
			
						
			String siteName = OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot();

			CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
			String urlFriendlyFormat = config.getParam(siteName, "" + idPublication, module,urlFormat,"");
			String urlFriendlyRegExp =config.getParam(siteName, "" + idPublication, module,urlFriendlyRegex,"");
					
			link = linkName.replaceAll(urlFriendlyRegExp, urlFriendlyFormat);
			link = replaceVariables(link,resource,cmsObject,titleNews);
						
			if (link.startsWith("/"))
				link = link.replaceFirst("/", "");
			
			LOG.debug("UrlLinkHelper / " + resource.getRootPath() + " > " + customDomain + link);
            String fullLink = joinLinkWithDomain(cmsObject,relative,
					link, customDomain, hasCustomDomain);
            return fullLink;
		} catch (CmsException e) {
			LOG.error("Error al obtener urlfriendly. retorna: " + linkName,e);
		      
			return linkName;
		}
	}
	
}