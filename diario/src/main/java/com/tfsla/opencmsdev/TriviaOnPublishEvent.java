package com.tfsla.opencmsdev;

import java.util.ArrayList;
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
import com.tfsla.diario.ediciones.services.TipoEdicionBaseService;
import com.tfsla.trivias.data.TfsTriviasDAO;
import com.tfsla.utils.CmsObjectUtils;
import com.tfsla.utils.CmsResourceUtils;

public class TriviaOnPublishEvent  implements I_CmsEventListener {

	private static final Log LOG = CmsLog.getLog(TriviaOnPublishEvent.class);

	public void cmsEvent(CmsEvent event) {
		int triviaType;
		try {
			triviaType = OpenCms.getResourceManager().getResourceType("trivia").getTypeId();
			
			if (event.getType()==I_CmsEventListener.EVENT_BEFORE_PUBLISH_PROJECT) {

				long ahora = java.lang.System.currentTimeMillis(); 
				LOG.debug(ahora + "> - Comenzando evento trivianPublish.");

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

						if (resource.getTypeId()==triviaType && resource.getState()!=CmsResource.STATE_DELETED) {
							CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
							if (site==null)
								continue;
							cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());
							
							String url = CmsResourceUtils.getLink(resource);

							int publication = 0;
							TipoEdicionBaseService tService = new TipoEdicionBaseService();
					    	try {
								TipoEdicion tEdicion = tService.obtenerTipoEdicion(cmsObject, url);			
								if (tEdicion!=null)
									publication = tEdicion.getId();
							} catch (Exception e) {
								e.printStackTrace();
							};
							
							
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
								
								String store = content.getStringValue(cmsObject, "storeResults", locale.ENGLISH);
								if (store.equals("true")) { 
									String fechaCierre = content.getStringValue(cmsObject, "closeDate", locale.ENGLISH);
									if (!fechaCierre.equals("0")) {
										TfsTriviasDAO tDAO = new TfsTriviasDAO();
										int triviaId = 0;
										try {
											triviaId = tDAO.getIDTrivia(url, site.getSiteRoot().replace("/sites/",""), publication);
											if (triviaId != 0 ) 
												tDAO.updateCloseDate(triviaId, fechaCierre);
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}	
								}
								//Busco la fecha de cierre
							} catch (CmsXmlException e) {
								CmsLog.getLog(this).error("Error al intentar modificar la trivia en la publicacion " + url,e);
							} catch (CmsException e) {
								CmsLog.getLog(this).error("Error al intentar modificar el contenido de la trivia en la publicacion " + url,e);
							}
						} 
					}
				}

				long despues1 = java.lang.System.currentTimeMillis(); 
				org.opencms.main.CmsLog.getLog(NewsOnPublishEvents.class).debug(ahora + "> - termine evento triviaOnPublishEvent. milisengundos: " + (despues1 - ahora));
			}
		} catch (CmsLoaderException e) {
			CmsLog.getLog(this).error("Hubo problemas publicando la trivia",e);
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

		LOG.debug("buscando canonical en la trivia " + url + ": " + canonical);

		if (canonical==null || canonical.trim().equals("")) {

			boolean validValue = false;
			List<I_CmsXmlContentValue> titulos = content.getValues(
					"title", locale);
			for (I_CmsXmlContentValue tituloContent : titulos) {
				String titulo = tituloContent.getStringValue(cmsObject);
				if (validValue == false && !titulo.trim().equals("")) {
					validValue = true;
					canonical = titulo;
				}
			}

			LOG.debug("canonical en trivia " + url + ": no encontrado. usando valor de titulo " + canonical);

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
