package com.tfsla.diario.webservices;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Locale;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.TfsJspTagLink;
import org.opencms.loader.CmsExternalImageScaler;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUriSplitter;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.NoticiasService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.utils.UrlLinkHelper;


public class WebPushService {
	
	 
	public static synchronized void updateJson(CmsObject cms, JSONObject jsonObject, String site, String publication) throws Exception {
		String jsonOnRFS = _config.getParam(site, publication, "webservices", "pushJsonRFSPath");
		File jsonFile = new File(jsonOnRFS);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(jsonFile, false));
			writer.write(jsonObject.toString());
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	
	private static CmsFile getResource(CmsObject cms, String site, CmsResource resource) throws CmsException {
		if (resource==null)
			return null;
		
		String currentProject = cms.getRequestContext().currentProject().getName();
		CmsFile cmsFile = null;
		try {
			cms.getRequestContext().setSiteRoot("/");
			if(currentProject.equals("Online")) {
				cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
			}
			cmsFile = cms.readFile(resource);
		} catch(Exception e) {
			throw e;
		} finally {
			if(!currentProject.equals("Offline")) {
				try {
					cms.getRequestContext().setCurrentProject(cms.readProject(currentProject));
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			cms.getRequestContext().setSiteRoot(site);
		}
		return cmsFile;
	}
	
	public static JSONObject getJson(CmsObject cms, String site, String publication, String title, String subTitle, String pushUrlParams, String manualUrl, CmsResource latestNew, String image) throws CmsException {
		//Locale locale = cms.getRequestContext().getLocale();
		CmsFile cmsFile = getResource(cms,site,latestNew);
		
		JSONObject jsonObject = new JSONObject();
		//String utm = _config.getParam(site, publication, "webservices", "pushUrlParams", "");
		
		try {
			cms.getRequestContext().setSiteRoot(site);
			
			jsonObject.put("title", title);
			jsonObject.put("subtitle", subTitle);
			if (cmsFile!=null) {
				CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, cmsFile);

				TipoEdicion edicionNota = getTipoEdicionFromResource(cmsFile, cms);
				
				jsonObject.put("url", cleanUrl(cmsFile.getRootPath(), pushUrlParams));
				jsonObject.put("urlfriendly", cleanUrl(UrlLinkHelper.getUrlFriendlyLink(cmsFile, cms, null, false, true), pushUrlParams, edicionNota));
				jsonObject.put("urlredirect", UrlLinkHelper.getExternalLink(cmsFile, cms));
				jsonObject.put("canonical", cleanUrl(UrlLinkHelper.getCanonicalLink(cmsFile, cms, null), pushUrlParams, edicionNota));
				jsonObject.put("image", getExportImage(content, cms, false));
				jsonObject.put("icon", getExportImage(content, cms, true));
			}
			else {
				jsonObject.put("url", manualUrl!=null ? cleanUrl(manualUrl,pushUrlParams) : "");
				jsonObject.put("urlfriendly", "");
				jsonObject.put("urlredirect", "");
				jsonObject.put("canonical", "");
				jsonObject.put("image", getExportImage(image,cms, false));
				jsonObject.put("icon", getExportImage(image,cms, true));
				
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			cms.getRequestContext().setSiteRoot("/");
		}
		
		return jsonObject;
	}

	
	@Deprecated
	public static JSONObject getJson(CmsObject cms, CmsResource latestNew, String site, String publication) throws CmsException {
		Locale locale = cms.getRequestContext().getLocale();
		String currentProject = cms.getRequestContext().currentProject().getName();
		CmsFile cmsFile = null;
		try {
			cms.getRequestContext().setSiteRoot("/");
			if(currentProject.equals("Online")) {
				cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
			}
			cmsFile = cms.readFile(latestNew);
		} catch(Exception e) {
			throw e;
		} finally {
			if(!currentProject.equals("Offline")) {
				try {
					cms.getRequestContext().setCurrentProject(cms.readProject(currentProject));
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			cms.getRequestContext().setSiteRoot(site);
		}
		
		CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, cmsFile);
		JSONObject jsonObject = new JSONObject();
		String utm = _config.getParam(site, publication, "webservices", "pushUrlParams", "");
		
		try {
			cms.getRequestContext().setSiteRoot(site);
			String title = content.getStringValue(cms, "tituloPush", locale);
			String subtitle = content.getStringValue(cms, "subtituloPush", locale);
			if (title == null || title.equals("")) {
				title = content.getStringValue(cms, "titulo", locale);
			}
			if (subtitle == null || subtitle.equals("")) {
				subtitle = filterHtml(content.getStringValue(cms, "copete", locale));
			}
			TipoEdicion edicionNota = getTipoEdicionFromResource(cmsFile, cms);
			jsonObject.put("title", title);
			jsonObject.put("subtitle", subtitle);
			jsonObject.put("url", cleanUrl(cmsFile.getRootPath(), utm));
			jsonObject.put("urlfriendly", cleanUrl(UrlLinkHelper.getUrlFriendlyLink(cmsFile, cms, null, false, true), utm, edicionNota));
			jsonObject.put("urlredirect", UrlLinkHelper.getExternalLink(cmsFile, cms));
			jsonObject.put("canonical", cleanUrl(UrlLinkHelper.getCanonicalLink(cmsFile, cms, null), utm, edicionNota));
			jsonObject.put("image", getExportImage(content, cms, true));
			jsonObject.put("icon", getExportImage(content, cms, false));
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			cms.getRequestContext().setSiteRoot("/");
		}
		
		return jsonObject;
	}
	
	public static synchronized String filterHtml(String html) {
		//html = html.replaceAll("\\s*<a.*</a>\\s*", ""); //remove links
		String nohtml = html.replaceAll("\\<.*?>",""); //remove html tags
		nohtml = StringEscapeUtils.unescapeHtml(nohtml); //decode html characters
		
		return nohtml;
	}
	
	public static synchronized Boolean isEnabled(String site, String publication) {
		String param = _config.getParam(site, publication, "webservices", "pushEnabled");
		if(param == null || param.trim().equals("")) return false;
		
		try {
			return Boolean.parseBoolean(param);
		} catch(Exception e) {
			return false;
		}
	}
	
	/*
	private static synchronized String getExportImage(String path, CmsObject cms, boolean isImage) {
		LOG.debug("getExportImage FOR PATH"+ path); 
		String scaleCode = (isImage) ? SCALE_IMAGE_CODE : SCALE_ICON_CODE; 
		
		if (path==null)
			return "";
		
		try {
			CmsFile readFile;
			readFile = cms.readFile(path.replace(cms.getRequestContext().getSiteRoot(), ""));
			long date = readFile.getDateLastModified();
			if (path.startsWith(cms.getRequestContext().getSiteRoot())) {
				return "/__export/" + date + path + scaleCode ;
			}
		return "/__export/" + date + cms.getRequestContext().getSiteRoot() + path + scaleCode;
		} catch (Exception e) {
			return cleanUrl(path, "");
		}
	}
	
	private static synchronized String getExportImage(CmsXmlContent content, CmsObject cms, boolean isImage) {
		LOG.debug("getExportImage FOR CONTENT"); 
		String path = "";
		String scaleCode = (isImage) ? SCALE_IMAGE_CODE : SCALE_ICON_CODE; 
		
		Locale locale = cms.getRequestContext().getLocale();
		try {
			path = content.getStringValue(cms, "imagenPrevisualizacion/imagen", locale);
			LOG.debug("imagenPrevisualizacion/imagen"+ path);
			if (path == null || path.equals("")) {
				path = content.getStringValue(cms, "imagenesFotogaleria[1]/imagen", locale);
				LOG.debug("imagenesFotogaleria[1]/imagen"+ path);
			}
			CmsFile readFile = cms.readFile(path.replace(cms.getRequestContext().getSiteRoot(), ""));
			long date = readFile.getDateLastModified();
			if (path.startsWith(cms.getRequestContext().getSiteRoot())) {
				return "/__export/" + date + path + scaleCode;
			}
			return  "/__export/" + date + cms.getRequestContext().getSiteRoot() + path + scaleCode;
		} catch (Exception e) {
			return cleanUrl(path, "");
		}
	}
	*/
	
	private static synchronized String cleanUrl(String url, String utm) {
		return cleanUrl(url, utm, null);
	}
	
	private static synchronized String cleanUrl(String url, String utm, TipoEdicion edicion) {
		LOG.debug("se va por el cleanURL");

		
		if (url == null || url.equals("")) {
			return "";
		}
		
		String ret = url;
		try {
			if (url.startsWith("/sites")) {
				ret = url.substring(StringUtils.ordinalIndexOf(url, "/", 3));
			} else {
				if (edicion == null) {
					return ret + utm;
				}
				// Para custom domain eliminar el path base de la publicación
				String customDomain = edicion.getCustomDomain();
				if (customDomain != null && !customDomain.equals("")) {
					String urlEdicion = edicion.getBaseURL(); // Por ejemplo: /sites/debate/mujeres/
					String publicacion = urlEdicion.substring(StringUtils.ordinalIndexOf(urlEdicion, "/", 3)+1); // publicación sería mujeres/
					
					// Eliminar el mujeres/ de la URL siendo que se usa solamente el custom domain
					ret = ret.replace(publicacion, "");
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return ret + utm;
	}
	
	public static TipoEdicion getTipoEdicionFromResource(CmsResource resource, CmsObject cmsObject) throws Exception {
		TipoEdicion tEdicion  = null;
		TipoEdicionService tEService = new TipoEdicionService();
		try {
			tEdicion = tEService.obtenerTipoEdicion(cmsObject,cmsObject.getSitePath(resource));
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return tEdicion;
	}
	

	private static synchronized String getScale(String path, CmsObject cms, boolean isImage) {
		String scaleCode = (isImage) ? SCALE_IMAGE_CODE : SCALE_ICON_CODE; 
		
		if (path==null)
			return "";
		
		try {
			CmsFile readFile = cms.readFile(path.replace(cms.getRequestContext().getSiteRoot(), ""));
			if (path.startsWith(cms.getRequestContext().getSiteRoot())) {
				return path + scaleCode ;
			}
			
		return cms.getRequestContext().getSiteRoot() + path + scaleCode;
		} catch (Exception e) {
			return cleanUrl(path, "");
		}
	}
	
	
	private static synchronized CmsObject getCloneCms(CmsObject cms) throws CmsException {

        CmsObject m_cloneCms = OpenCms.initCmsObject(cms);
        m_cloneCms.getRequestContext().setRequestTime(CmsResource.DATE_RELEASED_EXPIRED_IGNORE);
        
        return m_cloneCms;
    }
	
	private static synchronized String getExportImage(String path, CmsObject cms, boolean asIcon) {
		
		try {
			
			CmsFile readFile = cms.readFile(path.replace(cms.getRequestContext().getSiteRoot(), ""));
			
			return getScaledImage(readFile, asIcon, cms);
			
		} catch (Exception e) {
			return cleanUrl(path, "");
		}
	}
	
	private static synchronized String getExportImage(CmsXmlContent content, CmsObject cms, boolean asIcon) {
		String path = "";
		Locale locale = cms.getRequestContext().getLocale();
		try {
			path = content.getStringValue(cms, "imagenPrevisualizacion/imagen", locale);
			if (path == null || path.equals("")) {
				path = content.getStringValue(cms, "imagenesFotogaleria[1]/imagen", locale);
			}
			
			CmsFile readFile = cms.readFile(path.replace(cms.getRequestContext().getSiteRoot(), ""));
			
			return getScaledImage(readFile, asIcon, cms);
			
		} catch (Exception e) {
			return cleanUrl(path, "");
		}
	}
	private static synchronized String getScaledImage(CmsResource imageRes, boolean asIcon, CmsObject cms) {
		
		CmsObject onlineCmsObject = null;
		try {
			onlineCmsObject = getCloneCms(cms);
			onlineCmsObject.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		CmsExternalImageScaler m_scaler = new CmsExternalImageScaler();
		
		CmsExternalImageScaler reScaler = null;
		String scaleParams = (asIcon ? SCALE_ICON_CODE : SCALE_IMAGE_CODE);
		
		if (asIcon) {
			//"?__scale=w:512,h:512,t:2,p:6";
			m_scaler.setWidth(512);
			m_scaler.setHeight(512);
			m_scaler.setType(2);
			m_scaler.setPosition(6);
		}
		else {
			//"?__scale=w:1200,h:900,t:0,c:000";
			m_scaler.setWidth(1200);
			m_scaler.setHeight(900);
			m_scaler.setType(0);
			m_scaler.setColor("000");
		}
		
		String[] scaleStr = (String[])CmsRequestUtil.createParameterMap(scaleParams).get(
                CmsExternalImageScaler.PARAM_SCALE);
		
		reScaler = new CmsExternalImageScaler(scaleStr[0]);
		m_scaler = reScaler.getCropScaler(m_scaler);
		
		  // calculate target scale dimensions (if required)  
        if ((m_scaler.getHeight() <= 0) || (m_scaler.getWidth() <= 0)) {
            // read the image properties for the selected resource
            CmsExternalImageScaler original = new CmsExternalImageScaler(onlineCmsObject, imageRes);
            if (original.isValid()) {
            	m_scaler = original.getReScaler(m_scaler);
            }
        }
        
        String imageLink = onlineCmsObject.getSitePath(imageRes);
        if (m_scaler.isValid()) {
            // now append the scaler parameters
            imageLink += m_scaler.toRequestParam();
        }
        

        LOG.debug("Por pushear noticia con imagen: " + imageLink + " (" + onlineCmsObject.getRequestContext().getSiteRoot() + ")" );
        
        String absUri = CmsLinkManager.getAbsoluteUri(imageLink, onlineCmsObject.getRequestContext().getUri());

        LOG.debug("absUri: " + absUri );

        String newlink = OpenCms.getLinkManager().substituteLinkForUnknownTarget(
        		onlineCmsObject,
            absUri);

        LOG.debug("newlink: " + newlink );

        if (newlink.startsWith("/export/")) {
			long dateLastModified = imageRes.getDateLastModified();
	        LOG.debug("DateLastModified: " + dateLastModified + " > " + newlink + " - " + absUri );
			if (dateLastModified > 0) {
				newlink = "/__export/" + dateLastModified
						+ newlink.substring("/export".length());
			}
		} 
        
        //LOG.fatal("Push de la noticia - url de la imagen: " + newlink);
        return newlink;
        
       
		
	}
	
	/*
	private static synchronized String getExportImage(CmsXmlContent content, CmsObject cms) {
		String path = "";
		Locale locale = cms.getRequestContext().getLocale();
		try {
			path = content.getStringValue(cms, "imagenPrevisualizacion/imagen", locale);
			if (path == null || path.equals("")) {
				path = content.getStringValue(cms, "imagenesFotogaleria[1]/imagen", locale);
			}
			
			return path;
			
		} catch (Exception e) {
			return cleanUrl(path, "");
		}
	}
	*/
	private static synchronized String getScale(CmsXmlContent content, CmsObject cms, boolean isImage) {
		String path = "";
		String scaleCode = (isImage) ? SCALE_IMAGE_CODE : SCALE_ICON_CODE; 
		
		Locale locale = cms.getRequestContext().getLocale();
		try {
			path = content.getStringValue(cms, "imagenPrevisualizacion/imagen", locale);
			if (path == null || path.equals("")) {
				path = content.getStringValue(cms, "imagenesFotogaleria[1]/imagen", locale);
			}
			CmsFile readFile = cms.readFile(path.replace(cms.getRequestContext().getSiteRoot(), ""));
			if (path.startsWith(cms.getRequestContext().getSiteRoot())) {
				return path + scaleCode;
			}
			return cms.getRequestContext().getSiteRoot() + path + scaleCode;
		} catch (Exception e) {
			return cleanUrl(path, "");
		}
	}
	
	
	private static CPMConfig _config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
	private static final Log LOG = CmsLog.getLog(WebPushService.class);
	private static final String SCALE_IMAGE_CODE =  "?__scale=w:1200,h:900,t:0,c:000";
	private static final String SCALE_ICON_CODE =  "?__scale=w:512,h:512,t:2,p:6";
}
