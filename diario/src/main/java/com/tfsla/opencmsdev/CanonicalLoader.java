package com.tfsla.opencmsdev;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.opencms.db.CmsDefaultUsers;
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
import org.opencms.site.CmsSite;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.utils.TfsAdminUserProvider;

public class CanonicalLoader implements I_CmsEventListener {

	private static final Log LOG = CmsLog.getLog(CanonicalLoader.class);
	
	public void cmsEvent(CmsEvent event) {
		
		int noticiaType;
		try {
			noticiaType = OpenCms.getResourceManager().getResourceType("noticia").getTypeId();
			if (event.getType()==I_CmsEventListener.EVENT_BEFORE_PUBLISH_PROJECT) {
				//CmsObject cmsObject = TfsContext.getInstance().getCmsObject();
				
				
				CmsPublishList pubList = (CmsPublishList) event.getData().get(I_CmsEventListener.KEY_PUBLISHLIST);
		
				for (Iterator it = pubList.getFileList().iterator();it.hasNext();)
				{
					CmsResource resource = (CmsResource)it.next();
					if (resource.getTypeId()==noticiaType)
					{
						try {
							
							CmsObject _cmsObject =CmsWorkplaceAction.getInstance().getCmsAdminObject();
							CmsObject cmsObject = OpenCms.initCmsObject(_cmsObject);

							CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
							if (site==null)
								continue;
							
							cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());
							cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
							
							String url = CmsResourceUtils.getLink(resource);
							
							CmsXmlContent content = getXmlContent(cmsObject, url);
							
							Locale locale = getContentLocale(cmsObject, url,
									content);

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
										
										LOG.debug("canonical en lower case " + titulo.toLowerCase());
									}
								}
							
								LOG.debug("canonical en noticia " + url + ": no encontrada. usando valor de titulo " + canonical);
								
								if (validValue)
								{
									content.getValue("canonical[1]", locale).setStringValue(cmsObject, canonical);
									CmsResourceUtils.forceLockResource(cmsObject, url);

									CmsFile file = cmsObject.readFile(url);
									file.setContents(content.marshal());
									cmsObject.writeFile(file);
									
									CmsResourceUtils.unlockResource(cmsObject, url, false);

								}

							}
						} catch (CmsException e) {
							CmsLog
							.getLog(this)
							.error(
									"Hubo problemas publicando la nota en twitter",
									e);
						}
						
					}
					
				}
				
			}
		} catch (CmsLoaderException e) {
			CmsLog
			.getLog(this)
			.error(
					"Hubo problemas publicando la nota en twitter",
					e);
		}


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
