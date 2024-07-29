package com.tfsla.diario.twitter;

import java.io.IOException;

import com.tfsla.bitly.BitlyClient;
import com.tfsla.bitly.builder.v3.ShortenRequest;
import com.tfsla.bitly.model.Response;
import com.tfsla.bitly.model.v3.ShortenResponse;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionBaseService;
import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.utils.UrlLinkHelper;

/**
 * Clase para la integracion con Bitly
 * @author Victor Podberezski (vpod@tfsla.com)
 *
 */
public class BitlyService {
    
	private static final Log LOG = CmsLog.getLog(BitlyService.class);
	
	private String bitlyAToken = "";
	
	
	protected String siteName="";
	protected String publication="";
	protected String module = "bitly";

	private BitlyService(CmsObject cms){
		
		setConfiguration(cms);
		
	}
	
	private BitlyService(String site, String publicacion){
		
		this.siteName = site;
		this.publication = publicacion;
		
	}
	
	/**
	 * En base a la url del recurso genera una url corta en bitly y la retorna.
	 * @param cmsObject 
	 * @param resource (String) - url en el vfs
	 * @return String (String) - url en bitly
	 * @throws Exception 
	 * @throws BitlyException
	 * @throws IOException
	 */
	public String getShortenUrl(CmsObject cmsObject, CmsResource resource) throws Exception 
	{

		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		
		this.bitlyAToken = config.getParam(siteName, publication, module, "bitlyAToken");
		
		String url = CmsResourceUtils.getLink(resource);
		
		String friendlyUrl = UrlLinkHelper.getRelativeUrlFriendlyLink(resource,cmsObject);

		String dominio = UrlLinkHelper.getPublicationDomain(resource, cmsObject);
		String fullUrl = dominio + (!url.startsWith("/") && !dominio.endsWith("/") ? "/" : "") + friendlyUrl;
		
		LOG.debug("Creando url short para noticia " + fullUrl + "( token " + bitlyAToken + ")");
		
		BitlyClient client = new BitlyClient(bitlyAToken);
		
		ShortenRequest req = client.shorten();		
		req.setLongUrl(fullUrl); 		
		Response<ShortenResponse> respShort = req.call();

		LOG.debug("resultado creacion de url short para noticia " + fullUrl + "(  " + respShort.status_code + " / "  + respShort.status_txt + ")");
		
		if (respShort.status_code!=200)
			throw new Exception("Error creando la url corta con bitly para " + resource + ": " + respShort.status_txt);
		
		return respShort.data.url;
	}

	public static BitlyService getInstance(CmsObject cms) {
		return new BitlyService(cms);
	}
	
	public static BitlyService getInstance(String site, String publicacion) {
		return new BitlyService(site, publicacion);
	}
	
	public void setConfiguration(CmsObject cms)
	{
    	siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	publication = "0";
     	TipoEdicionBaseService tService = new TipoEdicionBaseService();
    	try {
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());			
			if (tEdicion!=null)
				publication = "" + tEdicion.getId();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String getShortenUrl(String path) throws Exception {
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		
		
		this.bitlyAToken = config.getParam(siteName, publication, module, "bitlyAToken");
		
		
		
		LOG.debug("Creando url short para noticia " + path + "( token " + bitlyAToken + ")");
		
		BitlyClient client = new BitlyClient(bitlyAToken);
		
		ShortenRequest req = client.shorten();
		
		req.setLongUrl(path); 
		
		Response<ShortenResponse> respShort = req.call();

		LOG.debug("resultado creacion de url short para noticia " + path + "(  " + respShort.status_code + " / "  + respShort.status_txt + ")");
		
		if (respShort.status_code!=200)
			throw new Exception("Error creando la url corta con bitly para " + path + ": " + respShort.status_txt);
		
		return respShort.data.url;
	}
	
	public boolean isEnabled() {
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		return config.getBooleanParam(siteName, publication, module, "enabled",false);
	}

}
