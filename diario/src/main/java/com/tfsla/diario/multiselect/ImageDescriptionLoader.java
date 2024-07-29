package com.tfsla.diario.multiselect;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;
import org.opencms.site.CmsSite;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Locale;
import com.tfsla.diario.ediciones.services.VideosService;
import com.tfsla.utils.CmsObjectUtils;
import com.tfsla.utils.CmsResourceUtils;

/**
 * Evento que verifica en las noticias si las imagenes cargadas cuentan con una descripcion para agregarlas al Title de la imagen
 * @author Victor Podberezski
 *
 */
public class ImageDescriptionLoader extends A_DescriptionLoader implements I_CmsEventListener {

	private static final Log LOG = CmsLog.getLog(ImageDescriptionLoader.class);

	@Override
	public void cmsEvent(CmsEvent event) {
		int noticiaType =-1;

		try {
			noticiaType = OpenCms.getResourceManager().getResourceType("noticia").getTypeId();
		} catch (CmsLoaderException e) {
			LOG.error("Error al intentar obtener el identificador de la noticia",e);
		}

		if (event.getType()==I_CmsEventListener.EVENT_BEFORE_RESOURCE_DELETED) {

			
			CmsObject cmsObject = null;
			try {
				CmsUser user = (CmsUser)event.getData().get(I_CmsEventListener.KEY_USER);
				cmsObject = CmsObjectUtils.loginUser(user); 
				if (cmsObject != null)
					cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
			} catch (Exception ex){
				CmsLog.getLog(this).error("Error al intentar obtener el cmsObject del evento",ex);
			}
			if (cmsObject == null) {
				cmsObject = CmsObjectUtils.loginAsAdmin();		
			}
			
				
			for (CmsResource resource : (Iterable<CmsResource>) event.getData().get("resources")) {
				if (resource.getTypeId()==noticiaType) {
					String url = CmsResourceUtils.getLink(resource);
					if (url.contains("~")) {
						url = url.replace("~", "");
						
						try {
							
							//si la imagen esta programada no se debe cambiar su metadata CMSM-
							boolean scheduledNews = false;

							CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
							
							if (site!=null) {
								cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());

							
							CmsLock lockFile = cmsObject.getLock(url);
							boolean isLock = !lockFile.isUnlocked() ;
							
							
							if (isLock){
							
								//usuario que bloquea la noticia
							     
							       CmsProject lockInProject = lockFile.getProject();
							       
							       if(CmsProject.PROJECT_TYPE_TEMPORARY == lockInProject.getType())
							        		scheduledNews = true;
							}
							       
							if (!scheduledNews) {

								//boolean discardChanges = CmsResourceUtils.mustDiscardTempResourceChanges(cmsObject, resource);
								
	
									CmsXmlContent content = getXmlContent(cmsObject, url);
									try {
										fillImageData(cmsObject,content,"imagenesFotogaleria");
									} catch (CmsSecurityException ex){
										CmsLog.getLog(this).info("El usuario no tiene suficientes permisos. Usuario: " + 
												cmsObject.getRequestContext().currentUser().getName());
									
										cmsObject = CmsObjectUtils.loginAsAdmin();
										if (cmsObject != null) {
											cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
											cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());
											fillImageData(cmsObject,content,"imagenesFotogaleria");
										}
									}
									try {
										fillImageData(cmsObject,content,"imagenPrevisualizacion");
									} catch (CmsSecurityException ex){
										CmsLog.getLog(this).info("El usuario no tiene suficientes permisos. Usuario: " + 
												cmsObject.getRequestContext().currentUser().getName());
									
										cmsObject = CmsObjectUtils.loginAsAdmin();
										if (cmsObject != null) {
											cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
											cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());
											fillImageData(cmsObject,content,"imagenPrevisualizacion");
										}
									}
									try {
										fillImageData(cmsObject,content,"imagenPersonalizada");
									} catch (CmsSecurityException ex) {
										CmsLog.getLog(this).info("El usuario no tiene suficientes permisos. Usuario: " + 
												cmsObject.getRequestContext().currentUser().getName());
									
										cmsObject = CmsObjectUtils.loginAsAdmin();
										if (cmsObject != null) {
											cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
											cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());
											fillImageData(cmsObject,content,"imagenPersonalizada");
										}
									}
								}
							}
						}
						catch (CmsException e) {
							LOG.error("Error al intentar verificar si la noticia tiene imagenes con descripcion para cargar",e);
						}

					}
				}
			}
		}
	}

	protected void fillImageData(CmsObject cmsObject, CmsXmlContent content, String image) throws CmsException
	{
		VideosService vService = VideosService.getInstance(cmsObject);

		int nro = 1;
		String xmlName = image + "[" + nro + "]";
		I_CmsXmlContentValue value = content.getValue(xmlName, Locale.ENGLISH);

		while (value!=null)
		{
			xmlName = image + "[" + nro + "]/imagen[1]";
			value = content.getValue(xmlName, Locale.ENGLISH);
			if (value!=null)
			{
				String pathVideo = value.getStringValue(cmsObject);

				if (pathVideo!=null && pathVideo.length()>0)
				{
					if (cmsObject.existsResource(pathVideo)) {

						String descripcion = getElementValue(cmsObject,content,image,nro,"descripcion");
						
						if (descripcion==null)
							descripcion = getElementValue(cmsObject,content,image,nro,"epigrafe");
						
						String categorias = getElementValue(cmsObject,content,image,nro,"categoria");
						String keywords = getElementValue(cmsObject,content,image,nro,"keywords");
						String fotografo = getElementValue(cmsObject,content,image,nro,"fotografo");
						String fuente = getElementValue(cmsObject,content,image,nro,"fuente");
						String puntoFocal = getElementValue(cmsObject,content,image,nro,"focalPoint");
						
						
						boolean mustAddDescription = mustAddProperty(cmsObject,descripcion, "Title",pathVideo);
						boolean mustAddCategory = mustAddProperty(cmsObject,categorias, "category",pathVideo);
						boolean mustAddKeywords = mustAddProperty(cmsObject,keywords, "Keywords",pathVideo);
						boolean mustAddFotografo = mustAddProperty(cmsObject,fotografo, "Author",pathVideo);
						boolean mustAddFuente = mustAddProperty(cmsObject,fuente, "Agency",pathVideo);
						boolean mustAddFocalPoint = mustAddProperty(cmsObject,puntoFocal, "image.focalPoint",pathVideo);
						
						
						if (mustAddDescription ||	mustAddCategory || mustAddFocalPoint ||
								mustAddKeywords ||	mustAddFotografo ||	mustAddFuente) {
							
							CmsResourceUtils.forceLockResource(cmsObject, pathVideo);

							if (mustAddDescription)
								cmsObject.writePropertyObject(pathVideo, new CmsProperty("Title",descripcion, null));

							if (mustAddKeywords)
								cmsObject.writePropertyObject(pathVideo, new CmsProperty("Keywords",keywords, null));

							if (mustAddCategory)
								cmsObject.writePropertyObject(pathVideo, new CmsProperty("category",categorias, null));

							if (mustAddFotografo)
								cmsObject.writePropertyObject(pathVideo, new CmsProperty("Author",fotografo, null));

							if (mustAddFuente)
								cmsObject.writePropertyObject(pathVideo, new CmsProperty("Agency",fuente, null));
						
							if (mustAddFocalPoint)
								cmsObject.writePropertyObject(pathVideo, new CmsProperty("image.focalPoint",puntoFocal, null));
						
							CmsFile file = cmsObject.readFile(pathVideo);
							 file.setContents(file.getContents()); 
							 cmsObject.writeFile(file);
							
							 cmsObject.unlockResource(pathVideo);

							vService.addNewsCountToVideo(cmsObject, pathVideo);

						}
					}
				}
			}									

			nro++;
		}
	}

}
