package com.tfsla.opencmsdev;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsMedios;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;
import org.opencms.site.CmsSite;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.twitter.BitlyService;
import com.tfsla.planilla.herramientas.PlanillaFormConstants;
import com.tfsla.utils.CmsObjectUtils;
import com.tfsla.utils.CmsResourceUtils;

public class NewsOnPublishEvents  implements I_CmsEventListener {

	private static final Log LOG = CmsLog.getLog(NewsOnPublishEvents.class);
	
	public void cmsEvent(CmsEvent event) {
		int noticiaType;
		try {
			noticiaType = OpenCms.getResourceManager().getResourceType("noticia").getTypeId();
			if (event.getType()==I_CmsEventListener.EVENT_BEFORE_PUBLISH_PROJECT) {
				
				long ahora = java.lang.System.currentTimeMillis(); 
				org.opencms.main.CmsLog.getLog(NewsOnPublishEvents.class).debug(ahora + "> - Comenzando evento NewsOnPublishEvents.");
				
				CmsObject cmsObject=null;
				try {
					CmsObject cmsObjectToClone = (CmsObject)event.getData().get(I_CmsEventListener.KEY_CMS_OBJECT);
					cmsObject = CmsObjectUtils.getClone(cmsObjectToClone);
					if (cmsObject != null)
						cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
				} catch (Exception ex){
					CmsLog.getLog(this).error("Error al intentar obtener el cmsObject del evento",ex);
				}
				if (cmsObject == null) {
					cmsObject = CmsObjectUtils.loginAsAdmin();		
				}
				if (cmsObject!=null) {
					CmsPublishList pubList = (CmsPublishList) event.getData().get(I_CmsEventListener.KEY_PUBLISHLIST);
					for (Iterator it = pubList.getFileList().iterator();it.hasNext();) {
						CmsResource resource = (CmsResource)it.next();
						List<CmsProperty> properties = new ArrayList<CmsProperty>();
						
						if (resource.getTypeId()==noticiaType && resource.getState()!=CmsResource.STATE_DELETED) {
							CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
							if (site==null)
								continue;
							cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());
							
							String url = CmsResourceUtils.getLink(resource);
							
							CmsXmlContent content;
							try {
								content = getXmlContent(cmsObject, url);
								
								Locale locale = getContentLocale(cmsObject, url, content);
								
								/*** INI NAA-2704
								
								//seteo que la noticia no esta mas programada en caso de estarlo
								CmsProperty isShedule = new CmsProperty();
								isShedule.setName("isScheduled");
								isShedule.setAutoCreatePropertyDefinition(false);
								isShedule.setResourceValue("false");
								isShedule.setStructureValue(CmsProperty.DELETE_VALUE);
								isShedule.setResourceValue(null);
								properties.add(isShedule);
								*/
								
								/** INI NAA-3080
								cmsObject.writePropertyObject(url, new CmsProperty("isScheduled","false",null));
								*/
								CmsProperty isScheduled = new CmsProperty();
								isScheduled.setName("isScheduled");
								isScheduled.setAutoCreatePropertyDefinition(true);
								isScheduled.setStructureValue("false");
								
								properties.add(isScheduled);
								
								LOG.debug("Se actuliza property NAA-2704");
								/** FIN NAA-2704 y NAA-3080*/
								
								//Guardo la fecha de la primera publicacion
								if(resource.getState().equals(CmsResource.STATE_NEW)){
									long currentDate = new java.util.Date().getTime();
									String currentDateStr = Long.toString(currentDate);
									
									CmsProperty publishDate = new CmsProperty();
									publishDate.setName("firstPublishDate");
									publishDate.setAutoCreatePropertyDefinition(true);
									publishDate.setStructureValue(currentDateStr);
									
									properties.add(publishDate);
								}
								
								//Guardo la fecha de la primera publicacion en GMT0 para poder mandarla a AWS 
								if(resource.getState().equals(CmsResource.STATE_NEW)){
								
									SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
									dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
									
									Calendar c = Calendar.getInstance();
								    c.setTimeZone(TimeZone.getTimeZone("GMT"));
								    Date now = c.getTime();
									long pubDate;
									try {
										pubDate = dateFormatGmt.parse( dateFormatGmt.format(now)).getTime()/1000;
										String pubDateStr = Long.toString(pubDate);
										
										CmsProperty publishDate = new CmsProperty();
										publishDate.setName("firstPublishDateGMT");
										publishDate.setAutoCreatePropertyDefinition(true);
										publishDate.setStructureValue(pubDateStr);
										
										properties.add(publishDate);
									} catch (ParseException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} 
									
								}else {
									
									String firstPublishDateGMTProp = cmsObject.readPropertyObject(url, "firstPublishDateGMT", false).getValue("");
						            
						            if (firstPublishDateGMTProp==null || firstPublishDateGMTProp.equals("")) {
						            	/** Se comenta porque no tenemos acceso a esa clase. 
						            	 *
						                List<tfsresourceversion> tfsHistory = tfsResourceHistory.getInstance().tfsHistory(cms, newsPath);
						                
						                tfsResourceVersion tfsLastVersion  = tfsHistory.get(1);
						                String firstPublishString = tfsLastVersion.getDatePublished();
						                
						               
						                SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy HH:mm");
						                Date firstPublishDate = formato.parse(firstPublishString);
						            	 
						            	Se toma el valor de firstPublishDate
				            	 		*/
						            	String firstPublishString = cmsObject.readPropertyObject(url, "firstPublishDate", false).getValue("");
										
						            	Calendar firstPublishCal = Calendar.getInstance();
						            	firstPublishCal.setTimeInMillis(Long.parseLong(firstPublishString));
						            	firstPublishCal.setTimeZone(TimeZone.getTimeZone("GMT"));
						            	
						                firstPublishDateGMTProp = String.valueOf(firstPublishCal.getTimeInMillis()/1000); 
										
						                CmsProperty publishDate = new CmsProperty();
										publishDate.setName("firstPublishDateGMT");
										publishDate.setAutoCreatePropertyDefinition(true);
										publishDate.setStructureValue(firstPublishDateGMTProp);
										
										properties.add(publishDate);
					            
						                LOG.debug("se actaliza la primera fecha de publicacion en GMT "+ firstPublishDateGMTProp +"  " );
						            }
								}

								//Establezco el cannonical
								boolean contentChanged = setCannonical(url, content, locale, cmsObject);
								
								boolean urlflriendlyChanged = setUrlFriendly(url, resource, cmsObject, site, content, locale);
							
								if(urlflriendlyChanged)
									contentChanged = urlflriendlyChanged;
								
								//Establezco la url bittly.
								CmsProperty bitlyUrl = setBitlyUrl(url, resource, cmsObject, site);
								if (bitlyUrl!=null)
									properties.add(bitlyUrl);
								
								//Cambio el estado
								if (!url.contains("~") && !isPost(cmsObject, resource) && !content.getValue("estado", locale).getStringValue(cmsObject).equals(PlanillaFormConstants.PUBLICADA_VALUE)) {
									content.getValue("estado", locale).setStringValue(cmsObject, PlanillaFormConstants.PUBLICADA_VALUE);
									contentChanged = true;
								}
								
								try {
									ejecuteAction (contentChanged, url, content, properties, cmsObject);
								}  catch (CmsSecurityException ex) {
									CmsLog.getLog(this).info("El usuario no tiene suficientes permisos. Usuario: " + 
											cmsObject.getRequestContext().currentUser().getName());
									//ejecuto la accion con el cmsObject tfs-Admin si falla por un tema de permisos
									cmsObject = CmsObjectUtils.loginAsAdmin();	
									if (cmsObject!=null) {
										cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
										cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());
										ejecuteAction (contentChanged, url, content, properties, cmsObject);
									}
								}
							} catch (CmsXmlException e) {
								CmsLog.getLog(this).error("Error al intentar modificar la noticia en la publicacion " + url,e);
							} catch (CmsException e) {
								CmsLog.getLog(this).error("Error al intentar modificar el contenido de la noticia en la publicacion " + url,e);
							}
						}
					}
				}
				
				long despues1 = java.lang.System.currentTimeMillis(); 
				org.opencms.main.CmsLog.getLog(NewsOnPublishEvents.class).debug(ahora + "> - termine evento NewsOnPublishEvents. milisengundos: " + (despues1 - ahora));
			}
		} catch (CmsLoaderException e) {
			CmsLog.getLog(this).error("Hubo problemas publicando la notacia",e);
		}
	}
	
	private void ejecuteAction (boolean contentChanged,String url, CmsXmlContent content, List<CmsProperty> properties,CmsObject cmsObject) throws CmsException{
		if (contentChanged)
			writeContent(url,content,properties, cmsObject);
		else if (properties.size()>0)
			writeProperties(url,properties,cmsObject);
	}
	
	private CmsProperty setBitlyUrl(String url, CmsResource resource, CmsObject cmsObject, CmsSite site) {
		TipoEdicionService tEdicionService = new TipoEdicionService();
		try {
			TipoEdicion tEdicion = tEdicionService.obtenerTipoEdicion(cmsObject, url);

			LOG.debug("Analizando codigo bitly de noticia " + url);
		
			if (BitlyService.getInstance(site.getSiteRoot(),"" + tEdicion.getId()).isEnabled()) {
				CmsProperty bitlyUrl = cmsObject.readPropertyObject(resource, "bitlyUrl", false);
				if (bitlyUrl==null || bitlyUrl.getValue()==null || bitlyUrl.getValue().equals(""))
				{
					LOG.debug("Codigo bitly inexistente. agregando codigo a la noticia " + url);
					String shortUrl = BitlyService.getInstance(site.getSiteRoot(),"" + tEdicion.getId()).getShortenUrl(cmsObject, resource);
					LOG.debug("Codigo bitly a agregar " + shortUrl + " a la noticia " + url);
					return new CmsProperty("bitlyUrl",shortUrl,shortUrl);
				}
			}

		} catch (Exception e) {
			CmsLog
			.getLog(this)
			.error(
					"Hubo problemas creando la url corta",
					e);
		}

		return null;

	}
	
	private boolean setUrlFriendly(String url, CmsResource resource, CmsObject cmsObject, CmsSite site, CmsXmlContent content, Locale locale) {
		
		String urlFriendly = content.getValue("urlFriendly[1]", locale).getStringValue(cmsObject);
		
		if (urlFriendly!=null && !urlFriendly.trim().equals(""))
			return false;
		
		TipoEdicionService tEdicionService = new TipoEdicionService();
		try {
			TipoEdicion tEdicion = tEdicionService.obtenerTipoEdicion(cmsObject, url);
			String publication = "" + tEdicion.getId();
			
			String sitio = site.getSiteRoot();

			boolean setUrlFriendlyOnPublish = CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(sitio, publication, "adminNewsConfiguration", "setUrlFriendlyOnPublish", false);
			
			if(setUrlFriendlyOnPublish){
				urlFriendly = content.getValue("urlFriendly[1]", locale).getStringValue(cmsObject);
				
				if (urlFriendly==null || (urlFriendly!=null && urlFriendly.equals("")))
				{
					
					boolean validValue = false;
					List<I_CmsXmlContentValue> titulos = content.getValues("titulo", locale);
					
					for (I_CmsXmlContentValue tituloContent : titulos) {
						String titulo = tituloContent.getStringValue(cmsObject);
						if (validValue == false && !titulo.trim().equals("")) {
							validValue = true;
							urlFriendly = titulo.toLowerCase();
						}
					}
				
					if (validValue)
					{
						content.getValue("urlFriendly[1]", locale).setStringValue(cmsObject, urlFriendly);
						return true;
					}
					
				}
				
			}else{
				return false;
			}
			
		} catch (Exception e) {
			CmsLog.getLog(this).error("Hubo problemas generando la urlfriendly",e);
		}
		
		return false;
	}
	
	private boolean setCannonical(String url, CmsXmlContent content, Locale locale, CmsObject cmsObject) {
		
		String canonical = content.getValue("canonical[1]", locale).getStringValue(cmsObject);
		
		LOG.debug("buscando canonical en noticia " + url + ": " + canonical);
		
		if (canonical==null || canonical.trim().equals("")) {
			
			boolean validValue = false;
			List<I_CmsXmlContentValue> titulos = content.getValues(
					"titulo", locale);
			for (I_CmsXmlContentValue tituloContent : titulos) {
				String titulo = tituloContent.getStringValue(cmsObject);
				if (validValue == false && !titulo.trim().equals("")) {
					validValue = true;
					canonical = titulo.toLowerCase();
				}
			}
		
			LOG.debug("canonical en noticia " + url + ": no encontrada. usando valor de titulo " + canonical);
			
			if (validValue)
			{
				content.getValue("canonical[1]", locale).setStringValue(cmsObject, canonical);
				return true;
			}
		}
		return false;
	}

	private void writeProperties(String url, List<CmsProperty> properties, CmsObject cmsObject) throws CmsException {
			
			CmsResourceUtils.forceLockResource(cmsObject, url);
				
			for (CmsProperty prop: properties) {
				cmsObject.writePropertyObject(url,prop);
			} 
			
			CmsResourceUtils.unlockResource(cmsObject, url, false);
		
	}
	
	private void writeContent(String url, CmsXmlContent content, List<CmsProperty> properties, CmsObject cmsObject) throws CmsException {
		CmsFile file;
			CmsResourceUtils.forceLockResource(cmsObject, url);
			
			file = cmsObject.readFile(url);
		
			file.setContents(content.marshal());
			cmsObject.writeFile(file);
			
			for (CmsProperty prop: properties) {
				cmsObject.writePropertyObject(url,prop);
			} 
			
			CmsResourceUtils.unlockResource(cmsObject, url, false);
		
	}
	
	private Locale getContentLocale(CmsObject cmsObject, String url,
			CmsXmlContent content) {
		List<Locale> locales = content.getLocales();

		if (locales.size() == 0) {
			locales = OpenCms.getLocaleManager().getDefaultLocales(cmsObject,url);
		}

		Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(CmsLocaleManager.getLocale(""),OpenCms.getLocaleManager().getDefaultLocales(cmsObject,url), locales);
		return locale;
	}

	protected CmsXmlContent getXmlContent(CmsObject cmsObject, String url)
			throws CmsXmlException, CmsException {

		CmsFile contentFile = cmsObject.readFile(url);

		CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmsObject, contentFile);

		try {	
			CmsXmlUtils.validateXmlStructure(contentFile.getContents(), new CmsXmlEntityResolver(cmsObject));
		} catch (CmsException e) {
			//	CmsResourceUtils.forceLockResource(cmsObject, cmsObject.getSitePath(contentFile));
			content.setAutoCorrectionEnabled(true);
			content.correctXmlStructure(cmsObject);
			//  CmsResourceUtils.unlockResource(cmsObject, cmsObject.getSitePath(contentFile), false);
		}
		return content;
	}
	
	public static boolean isPost(CmsObject cms, CmsResource resource) {
		boolean isPost = false;
		try {
			CmsProperty prop = cms.readPropertyObject(resource, "newsType", false);
			isPost = "post".equals(prop.getValue(""));
		} catch (CmsException e) {
			e.printStackTrace();
		}

		return isPost;
	}

}