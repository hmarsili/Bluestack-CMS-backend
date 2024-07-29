package com.tfsla.opencmsdev;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMultiMessages;
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

import com.tfsla.utils.CmsObjectUtils;
import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.vod.data.VodMyListDAO;
import com.tfsla.vod.model.TfsVodNews;

public class VodOnPublishEvent  implements I_CmsEventListener {

	private static final Log LOG = CmsLog.getLog(VodOnPublishEvent.class);

	public void cmsEvent(CmsEvent event) {
		int peliculaType;
		int serieType;
		int temporadaType;
		int episodioType;
		try {
			peliculaType = OpenCms.getResourceManager().getResourceType("pelicula").getTypeId();
			serieType = OpenCms.getResourceManager().getResourceType("serie").getTypeId();
			temporadaType = OpenCms.getResourceManager().getResourceType("temporada").getTypeId();
			episodioType = OpenCms.getResourceManager().getResourceType("episodio").getTypeId();

			if (event.getType()==I_CmsEventListener.EVENT_BEFORE_PUBLISH_PROJECT) {

				long ahora = java.lang.System.currentTimeMillis(); 
				LOG.debug(ahora + "> - Comenzando evento VodOnPublishEvent.");

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

						if ((resource.getTypeId()==peliculaType || resource.getTypeId()==serieType || resource.getTypeId()==episodioType || 
								resource.getTypeId()==temporadaType)  && resource.getState()!=CmsResource.STATE_DELETED) {
							CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
							if (site==null)
								continue;
							cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());

							String url = CmsResourceUtils.getLink(resource);

							CmsXmlContent content;
							try {
								content = getXmlContent(cmsObject, url);

								Locale locale = getContentLocale(cmsObject, url, content);

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

								//Establezco el cannonical
								boolean contentChanged = setCannonical(url, content, locale, cmsObject);

								//Cambio el estado
								/*if (!url.contains("~") &&  !content.getValue("estado", locale).getStringValue(cmsObject).equals(PlanillaFormConstants.PUBLICADA_VALUE)) {
									content.getValue("estado", locale).setStringValue(cmsObject, PlanillaFormConstants.PUBLICADA_VALUE);
									contentChanged = true;
								}*/

								try {
									ejecuteAction (contentChanged, url, content, properties, cmsObject);
								}  catch (CmsSecurityException ex) {
									LOG.info("El usuario no tiene suficientes permisos. Usuario: " + 
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
						if ( (resource.getTypeId()==episodioType || resource.getTypeId()==temporadaType)  && resource.getState()!=CmsResource.STATE_DELETED) {
							// Si es nuevo entonces se asume novedad
							if(resource.getState().equals(CmsResource.STATE_NEW)){
								TfsVodNews vodNews = new TfsVodNews();
								if (resource.getDateReleased()  == CmsResource.DATE_RELEASED_DEFAULT) {
									vodNews.setDisponibility(null);
								} else
									vodNews.setDisponibility(new Timestamp(resource.getDateReleased()));
								
								vodNews.setFecha(new Timestamp((new Date()).getTime()));
								vodNews.setFechaPublicacion(new Timestamp((new Date()).getTime()));

								CmsMultiMessages messages = new CmsMultiMessages(CmsLocaleManager.getDefaultLocale());
								 	//fecha publicacion
								if (resource.getTypeId()==episodioType) {
									vodNews.setDescripcion("GUI_NEW_EPISODE");
								} else {
									vodNews.setDescripcion("GUI_NEW_SEASON");
								}
								try {
									vodNews.setSourceParent(cmsObject.readPropertyObject(CmsResourceUtils.getLink(resource), "serie-path",false).getValue());
								} catch (CmsException e) {
									LOG.error("Error al buscar la serie a la que pertenece el elemento: ", e);
									
								}
								vodNews.setSource(CmsResourceUtils.getLink(resource));
								if (vodNews.getSourceParent()!= null && !vodNews.getSourceParent().equals("")) {
									VodMyListDAO vodDAO = new VodMyListDAO();
									
									try {
										vodDAO.insertNews(vodNews);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										LOG.error("Error al insertar la novedad: ", e);
										
									}
								}
							}	
								
							
						}

					}
				}

				long despues1 = java.lang.System.currentTimeMillis(); 
				org.opencms.main.CmsLog.getLog(NewsOnPublishEvents.class).debug(ahora + "> - termine evento VodOnPublishEvent. milisengundos: " + (despues1 - ahora));
			}
		} catch (CmsLoaderException e) {
			CmsLog.getLog(this).error("Hubo problemas publicando el vod",e);
		}
	}

	private void ejecuteAction (boolean contentChanged,String url, CmsXmlContent content, List<CmsProperty> properties,CmsObject cmsObject) throws CmsException{
		if (contentChanged)
			writeContent(url,content,properties, cmsObject);
		else if (properties.size()>0)
			writeProperties(url,properties,cmsObject);
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
					canonical = titulo;
				}
			}

			LOG.debug("canonical en noticia " + url + ": no encontrada. usando valor de titulo " + canonical);

			if (validValue){
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


}
