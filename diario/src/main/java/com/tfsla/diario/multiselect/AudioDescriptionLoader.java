package com.tfsla.diario.multiselect;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;
import org.opencms.site.CmsSite;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.ediciones.services.VideosService;
import com.tfsla.utils.CmsObjectUtils;
import com.tfsla.utils.CmsResourceUtils;

/**
 * Evento que verifica en las noticias si los audios cargados cuentan con una descripcion para agregarlas al Title del audio
 * @author Victor Podberezski
 *
 */
public class AudioDescriptionLoader extends A_DescriptionLoader  implements I_CmsEventListener {

	private static final Log LOG = CmsLog.getLog(ImageDescriptionLoader.class);

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
			
			if (cmsObject != null) {
				for (CmsResource resource : (Iterable<CmsResource>) event.getData().get("resources")) {
					if (resource.getTypeId()==noticiaType) {
						String url = CmsResourceUtils.getLink(resource);
						if (url.contains("~")) {
							url = url.replace("~", "");
	
							try {
								boolean discardChanges = CmsResourceUtils.mustDiscardTempResourceChanges(cmsObject, resource);
								
								CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
								if (site!=null && !discardChanges) {
									cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());
	
									CmsFile contentFile = cmsObject.readFile(url);
	
									CmsXmlContent content = CmsXmlContentFactory.unmarshal(getCloneCms(cmsObject), contentFile);
	
									try {	
										CmsXmlUtils.validateXmlStructure(contentFile.getContents(), new CmsXmlEntityResolver(cmsObject));
									} catch (CmsException e) {
											CmsResourceUtils.forceLockResource(cmsObject, cmsObject.getSitePath(contentFile));
											content.setAutoCorrectionEnabled(true);
									        content.correctXmlStructure(cmsObject);
									        CmsResourceUtils.unlockResource(cmsObject, cmsObject.getSitePath(contentFile), false);
									}
									
									try {
										fillAudioData(cmsObject,content,"audio");
									} catch (CmsSecurityException ex) {
										CmsLog.getLog(this).info("El usuario no tiene suficientes permisos. Usuario: " + 
												cmsObject.getRequestContext().currentUser().getName());
									
										cmsObject = CmsObjectUtils.loginAsAdmin();
										if (cmsObject != null) {
											cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
											cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());
											fillAudioData(cmsObject,content,"audio");
										} 
									}
								}
	
							} catch (CmsException e) {
								LOG.error("Error al intentar verificar si la noticia tiene audios con descripcion para cargar",e);
							}
	
						}
					}
				}
			}
		}
	}

	protected void fillAudioData(CmsObject cmsObject, CmsXmlContent content, String audio) throws CmsException
	{
		VideosService vService = VideosService.getInstance(cmsObject);

		int nro = 1;
		String xmlName = audio + "[" + nro + "]";
		I_CmsXmlContentValue value = content.getValue(xmlName, Locale.ENGLISH);

		while (value!=null)
		{
			xmlName = audio + "[" + nro + "]/audio[1]";
			value = content.getValue(xmlName, Locale.ENGLISH);
			if (value!=null)
			{
				String pathVideo = value.getStringValue(cmsObject);

				if (pathVideo!=null && pathVideo.length()>0)
				{
					if (cmsObject.existsResource(pathVideo)) {

						String descripcion = getElementValue(cmsObject,content,audio,nro,"descripcion");
						String categorias = getElementValue(cmsObject,content,audio,nro,"categoria");
						String keywords = getElementValue(cmsObject,content,audio,nro,"keywords");
						String imagen = getElementValue(cmsObject,content,audio,nro,"imagen");
						String fuente = getElementValue(cmsObject,content,audio,nro,"fuente");
						String autor = getElementValue(cmsObject,content,audio,nro,"autor");
						String calificacion = getElementValue(cmsObject,content,audio,nro,"calificacion");
						String titulo = getElementValue(cmsObject,content,audio,nro,"titulo");
						String ocultarComentario = getElementValue(cmsObject,content,audio,nro,"hideComments");
						String autoplay = getElementValue(cmsObject,content,audio,nro,"autoplay");
						
						boolean mustAddTitle = mustAddProperty(cmsObject,titulo, "Title",pathVideo);
						boolean mustAddAgency = mustAddProperty(cmsObject,fuente, "Agency",pathVideo);
						boolean mustAddAuthor = mustAddProperty(cmsObject,autor, "Author",pathVideo);
						boolean mustAddCalification = mustAddProperty(cmsObject,calificacion, "audio-rated",pathVideo);
						boolean mustAddHideComments = mustAddProperty(cmsObject,ocultarComentario, "hideComments",pathVideo);
						boolean mustAddAutoplay = mustAddProperty(cmsObject,autoplay,"audio-autoplay",pathVideo);
						
						boolean mustAddDescription = mustAddProperty(cmsObject,titulo, "Description",pathVideo);
						boolean mustAddKeywords = mustAddProperty(cmsObject,keywords, "Keywords",pathVideo);
						boolean mustAddCategory = mustAddProperty(cmsObject,categorias, "category",pathVideo);
						boolean mustAddImage = mustAddProperty(cmsObject,imagen, "prevImage",pathVideo);

						
						if (
								mustAddTitle
								|| mustAddAgency
								|| mustAddAuthor
								|| mustAddCalification
								|| mustAddHideComments
								|| mustAddDescription
								|| mustAddKeywords
								|| mustAddCategory
								|| mustAddImage
								|| mustAddAutoplay
							)
						{
								CmsResourceUtils.forceLockResource(cmsObject, pathVideo);


								if (mustAddDescription)
									cmsObject.writePropertyObject(pathVideo, new CmsProperty("Description",descripcion, null));
	
								if (mustAddKeywords)
									cmsObject.writePropertyObject(pathVideo, new CmsProperty("Keywords",keywords, null));

								if (mustAddCategory)
									cmsObject.writePropertyObject(pathVideo, new CmsProperty("category",categorias, null));

								if (mustAddImage)
									cmsObject.writePropertyObject(pathVideo, new CmsProperty("prevImage",imagen, null));

								if (mustAddTitle)
									cmsObject.writePropertyObject(pathVideo, new CmsProperty("Title",titulo, null));

								if (mustAddAgency)
									cmsObject.writePropertyObject(pathVideo, new CmsProperty("Agency",fuente, null));
								
								if (mustAddAuthor)
									cmsObject.writePropertyObject(pathVideo, new CmsProperty("Author",autor, null));
								
								if (mustAddCalification)
									cmsObject.writePropertyObject(pathVideo, new CmsProperty("audio-rated",calificacion, null));
								
								if (mustAddHideComments)
									cmsObject.writePropertyObject(pathVideo, new CmsProperty("hideComments",ocultarComentario, null));
								
								if(mustAddAutoplay)
									cmsObject.writePropertyObject(pathVideo, new CmsProperty("audio-autoplay",autoplay, null));
								
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
