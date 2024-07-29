package com.tfsla.opencmsdev;


import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;

import com.tfsla.utils.CmsObjectUtils;
import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.vod.data.VodMyListDAO;
import com.tfsla.vod.model.TfsVodNews;




public class VodPublishExpirationChangeEvent  implements I_CmsEventListener {

	private static final Log LOG = CmsLog.getLog(VodPublishExpirationChangeEvent.class);

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
				LOG.debug(ahora + "> - Comenzando evento VodPublishExpirationChangeEvent.");

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
						VodMyListDAO vodDAO = new VodMyListDAO();
						
						if ((resource.getTypeId()==peliculaType || resource.getTypeId()==serieType || resource.getTypeId()==episodioType || 
								resource.getTypeId()==temporadaType)  && resource.getState()!=CmsResource.STATE_DELETED) {
							long dateExpired = resource.getDateExpired();
							
							try {
								long dateRegistered = vodDAO.getSourceModificationDate(CmsResourceUtils.getLink(resource));
								
								//No hay valor seteado
								if (!(dateExpired == CmsResource.DATE_EXPIRED_DEFAULT && (dateRegistered == 0 || dateExpired == dateRegistered))) {
									//actualizar se saco la fecha
									vodDAO.updateSourceModification(CmsResourceUtils.getLink(resource), dateExpired);
								}
							} catch (Exception ex) {
								LOG.error("Hubo problemas actualizando la fecha en el vod: " +CmsResourceUtils.getLink(resource) ,ex);
							}
						}
											
					}
				}

				long despues1 = java.lang.System.currentTimeMillis(); 
				LOG.debug(ahora + "> - termine evento VodOnPublishEvent. milisengundos: " + (despues1 - ahora));
			}
		} catch (CmsLoaderException e) {
			LOG.error("Hubo problemas publicando el vod",e);
		}
	}

	
	

	



}

