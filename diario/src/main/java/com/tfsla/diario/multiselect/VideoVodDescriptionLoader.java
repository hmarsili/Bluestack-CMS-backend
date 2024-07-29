package com.tfsla.diario.multiselect;

import org.apache.commons.logging.Log;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelationType;
import org.opencms.search.CmsSearchResult;
import org.opencms.search.TfsAdvancedSearch;
import org.opencms.security.CmsSecurityException;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import org.opencms.xml.*;

import com.tfsla.diario.ediciones.services.PublicationService;
import com.tfsla.diario.ediciones.services.VideoEmbeddedService;
import com.tfsla.diario.ediciones.services.VideosService;
import com.tfsla.diario.file.types.TfsResourceTypeVideoEmbedded;
import com.tfsla.diario.file.types.TfsResourceTypeVideoVodEmbedded;
import com.tfsla.diario.file.types.TfsResourceTypeVideoVodYoutube;
import com.tfsla.diario.file.types.TfsResourceTypeVideoYoutubeLink;
import com.tfsla.utils.CmsObjectUtils;
import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;

/**
 * Evento que verifica en las noticias si los videos cargados cuentan con una descripcion para agregarlas al Title del video
 * @author Victor Podberezski
 *
 */
public class VideoVodDescriptionLoader extends A_DescriptionLoader  implements I_CmsEventListener {

	private static final Log LOG = CmsLog.getLog(VideoVodDescriptionLoader.class);

	public void cmsEvent(CmsEvent event) {
		
		int resourceTypePelicula =-1;
		int resourceTypeEpisodio =-1;
		int resourceTypeTemporada = -1;
		int resourceTypeSerie = -1;
		int resourceTypePlaylist = -1;
		
		try {
			resourceTypePelicula = OpenCms.getResourceManager().getResourceType("pelicula").getTypeId();
			resourceTypeEpisodio =OpenCms.getResourceManager().getResourceType("episodio").getTypeId();
			resourceTypeTemporada =OpenCms.getResourceManager().getResourceType("temporada").getTypeId();
			resourceTypeSerie =OpenCms.getResourceManager().getResourceType("serie").getTypeId();
			resourceTypePlaylist =OpenCms.getResourceManager().getResourceType("playlist").getTypeId();
			
		} catch (CmsLoaderException e) {
				LOG.error("Error al intentar obtener el identificador del contenido vod",e);
		}

		if (event.getType()==I_CmsEventListener.EVENT_BEFORE_RESOURCE_DELETED) {

			CmsObject cmsObject=null;
			
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
					if (resource.getTypeId()==resourceTypeEpisodio || resource.getTypeId()==resourceTypePelicula 
							|| resource.getTypeId()==resourceTypeTemporada
							|| resource.getTypeId()==resourceTypeSerie || resource.getTypeId()==resourceTypePlaylist) {
						String url = CmsResourceUtils.getLink(resource);
						if (url.contains("~")) {
							url = url.replace("~", "");

							try {
								
								boolean discardChanges = CmsResourceUtils.mustDiscardTempResourceChanges(cmsObject, resource);
								
								CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
								CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
								PublicationService publicationService = new PublicationService();
								
								Boolean isAlreadyLocked = false;
								
								if(!cmsObject.getLock(resource).isUnlocked())
									isAlreadyLocked = true;
								else
								    CmsResourceUtils.forceLockResource(cmsObject, url);

								String videosIndex="";
								String videosIndexNoVod = "";
								try {
									videosIndex = config.getParam(site.getSiteRoot(), String.valueOf(publicationService.getCurrentPublicationId(cmsObject)), "vod", "index");
									videosIndexNoVod = config.getParam(site.getSiteRoot(), String.valueOf(publicationService.getCurrentPublicationId(cmsObject)), "vod", "indexNoVod");
									
								} catch (Exception e1) {
									LOG.error("No puede obtener el indice de videos");
								}
								
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
										if (resource.getTypeId()==resourceTypeEpisodio || resource.getTypeId()==resourceTypePelicula || resource.getTypeId()==resourceTypePlaylist ) {
											fillVideoData(cmsObject,content,"vodFlash",videosIndex,url);
											fillVideoData(cmsObject,content,"vodYouTube",videosIndex, url);
											fillVideoData(cmsObject,content,"vodEmbedded",videosIndex,url);
										}
										addRelation ("videoEmbedded",url,content,cmsObject,videosIndexNoVod);
										addRelation ("videoYouTube",url,content,cmsObject,videosIndexNoVod);
										
									} catch (CmsSecurityException ex) {
										CmsLog.getLog(this).info("El usuario no tiene suficientes permisos. Usuario: " + 
												cmsObject.getRequestContext().currentUser().getName());
										cmsObject = CmsObjectUtils.loginAsAdmin();
										if (cmsObject != null) {
											cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
											cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());
											if (resource.getTypeId()==resourceTypeEpisodio || resource.getTypeId()==resourceTypePelicula
													|| resource.getTypeId()==resourceTypePlaylist ) {
												fillVideoData(cmsObject,content,"vodFlash",videosIndex,url);
												fillVideoData(cmsObject,content,"vodYouTube",videosIndex,url);
												fillVideoData(cmsObject,content,"vodEmbedded",videosIndex,url);
											}
											addRelation ("videoEmbedded",url,content,cmsObject,videosIndexNoVod);
											addRelation ("videoYouTube",url,content,cmsObject,videosIndexNoVod);
									
										}
									}
									if(!isAlreadyLocked)
										   CmsResourceUtils.unlockResource(cmsObject, url, false);
									
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

	private void addRelation(String video, String url, CmsXmlContent content,CmsObject cmsObject,String videosIndex ) {
		String pathVideo = "" ;
		int nro = 1;
		String xmlName = video + "[" + nro + "]";
		I_CmsXmlContentValue value = content.getValue(xmlName, Locale.ENGLISH);
		boolean videoExists =false;
	
		if (video.equals("vodYouTube")) {
			xmlName = video + "[" + nro + "]/youtubeid";
			value = content.getValue(xmlName, Locale.ENGLISH);
			if (value!=null) {
				 String youtubeId = value.getStringValue(cmsObject);
				 pathVideo = videoExist(cmsObject,videosIndex, youtubeId, TfsResourceTypeVideoVodYoutube.getStaticTypeName());
				 if (pathVideo.length()==0){
					LOG.debug("Con Lucene no se encontro el id de youtube ("+youtubeId+"), se busca la property en la bd.");
						
					String videoInBD = videoExistInBD(cmsObject,youtubeId);
					if(videoInBD.length()>0)
						pathVideo = videoInBD;
				}
			}
		} else if (video.equals("vodEmbedded")) {
			xmlName = video + "[" + nro + "]/codigo";
			value = content.getValue(xmlName, Locale.ENGLISH);
			if (value!=null) {
				String embeddId = value.getStringValue(cmsObject);
				pathVideo = videoExist(cmsObject,videosIndex, embeddId, TfsResourceTypeVideoVodEmbedded.getStaticTypeName());
				if (pathVideo.length()==0){
					LOG.debug("Con Lucene no se encontro el id de embedded ("+embeddId+"), se busca la property en la bd.");
					
					String videoInBD = videoExistInBD(cmsObject,embeddId);
					
					if(videoInBD.length()>0)
						pathVideo = videoInBD;
				}
			}
		}
		if (pathVideo!=null && pathVideo.length()>0) 
				videoExists = cmsObject.existsResource(pathVideo);

		if (videoExists) {
			try {
				cmsObject.addRelationToResource(url,pathVideo, CmsRelationType.valueOf(video).getName());
			} catch (CmsIllegalArgumentException | CmsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	protected void fillVideoData(CmsObject cmsObject, CmsXmlContent content, String video, String videosIndex, String url) throws CmsException {
		VideosService vService = VideosService.getInstance(cmsObject);

		int nro = 1;
		String xmlName = video + "[" + nro + "]";
		I_CmsXmlContentValue value = content.getValue(xmlName, Locale.ENGLISH);
		boolean videoExists =false;
		String pathVideo = "" ;
			if (video.equals("vodFlash")) {
				xmlName = video + "[" + nro + "]/video[1]";
				value = content.getValue(xmlName, Locale.ENGLISH);
				if (value!=null) 
					 pathVideo = value.getStringValue(cmsObject);

			} else if (video.equals("vodYouTube")) {
				xmlName = video + "[" + nro + "]/youtubeid";
				value = content.getValue(xmlName, Locale.ENGLISH);
				if (value!=null) {
					 String youtubeId = value.getStringValue(cmsObject);
					 pathVideo = videoExist(cmsObject,videosIndex, youtubeId, TfsResourceTypeVideoYoutubeLink.getStaticTypeName());
					 if (pathVideo.length()==0){
							
							LOG.debug("Con Lucene no se encontro el id de youtube ("+youtubeId+"), se busca la property en la bd.");
							
							String videoInBD = videoExistInBD(cmsObject,youtubeId);
							
							if(videoInBD.length()>0)
								pathVideo = videoInBD;
						}
					
				}
			} else if (video.equals("vodEmbedded")) {
				xmlName = video + "[" + nro + "]/codigo";
				value = content.getValue(xmlName, Locale.ENGLISH);
				if (value!=null) {
					String embeddId = value.getStringValue(cmsObject);
					pathVideo = videoExist(cmsObject,videosIndex, embeddId, TfsResourceTypeVideoEmbedded.getStaticTypeName());
					 if (pathVideo.length()==0){
							
							LOG.debug("Con Lucene no se encontro el id de embedded ("+embeddId+"), se busca la property en la bd.");
							
							String videoInBD = videoExistInBD(cmsObject,embeddId);
							
							if(videoInBD.length()>0)
								pathVideo = videoInBD;
						}
				}
			}
			if (pathVideo!=null && pathVideo.length()>0) 
					videoExists = cmsObject.existsResource(pathVideo);
	
			if (videoExists) {
				String descripcion = content.getStringValue(cmsObject, "cuerpo",  Locale.ENGLISH);
				String categorias = content.getStringValue(cmsObject, "categorias",  Locale.ENGLISH);
				String keywords = content.getStringValue(cmsObject, "claves",  Locale.ENGLISH);
				String imagen = content.getStringValue(cmsObject, "imagenPrevisualizacion[1]/imagen",  Locale.ENGLISH);
				String calificacion = content.getStringValue(cmsObject, "calificacion",  Locale.ENGLISH);
				String titulo = content.getStringValue(cmsObject, "titulo",  Locale.ENGLISH);
				
				
				CmsResourceUtils.forceLockResource(cmsObject, pathVideo);

				cmsObject.writePropertyObject(pathVideo, new CmsProperty("category",categorias, null));
				
				cmsObject.writePropertyObject(pathVideo, new CmsProperty("Keywords",keywords, null));
				
				cmsObject.writePropertyObject(pathVideo, new CmsProperty("prevImage",imagen, null));

				cmsObject.writePropertyObject(pathVideo, new CmsProperty("Title",titulo, null));

				cmsObject.writePropertyObject(pathVideo, new CmsProperty("video-rated",calificacion, null));
			
				CmsFile file = cmsObject.readFile(pathVideo);
			 	file.setContents(file.getContents()); 
			 	cmsObject.writeFile(file);
			 	if (video.equals("vodEmbedded"))
			 			cmsObject.addRelationToResource(url,pathVideo, CmsRelationType.valueOf("videoEmbedded").getName());
				if (video.equals("vodYouTube"))
		 			cmsObject.addRelationToResource(url,pathVideo, CmsRelationType.valueOf("videoYouTube").getName());
		
			 	cmsObject.unlockResource(pathVideo);
				
					//vService.addNewsCountToVideo(cmsObject, pathVideo);
				
			}									
	}
	
	public static String videoExist(CmsObject cmsObject, String videoIndex, String videoCode, String type)
	{
		VideoEmbeddedService videoEmbeddedService = new VideoEmbeddedService();
		String embeddedCode = "" ;
		if ( (!type.equals(TfsResourceTypeVideoYoutubeLink.getStaticTypeName()) && !type.equals(TfsResourceTypeVideoVodYoutube.getStaticTypeName())))
			embeddedCode= videoEmbeddedService.extractVideoCode(videoCode);
		else {
			if( videoCode.indexOf("v=") >-1) {
				 String[] temp = videoCode.split("v=");
				 videoCode = temp[1];
			   }
		
			embeddedCode = videoCode;
		}
			TfsAdvancedSearch adSearch = new TfsAdvancedSearch();
		adSearch.init(cmsObject);
		
		adSearch.setQuery("+Uid:\"" + embeddedCode + "\" +Type:\"" + type + "\"");
		adSearch.setMaxResults(5);
		adSearch.setIndex(videoIndex);
		adSearch.setLanguageAnalyzer(new WhitespaceAnalyzer());
		
		List<CmsSearchResult> resultados = adSearch.getSearchResult();
		
		if (resultados!=null && resultados.size()>0) {
			return cmsObject.getRequestContext().removeSiteRoot(resultados.get(0).getPath());
		}
		return "";		
		
	}
	
	private String videoExistInBD(CmsObject cmsObject, String videoCode) {
		String videoBDPath = "";
		String resourceID = "";
		
		QueryBuilder<String> queryBuilder = new QueryBuilder<String>(cmsObject);
        queryBuilder.setSQLQuery("select PROPERTY_MAPPING_ID from CMS_OFFLINE_PROPERTIES, CMS_OFFLINE_PROPERTYDEF where CMS_OFFLINE_PROPERTIES.PROPERTYDEF_ID = CMS_OFFLINE_PROPERTYDEF.PROPERTYDEF_ID AND PROPERTYDEF_NAME = 'video-code' AND PROPERTY_VALUE =? ");
		queryBuilder.addParameter(videoCode);
		
		ResultSetProcessor<String> proc = new ResultSetProcessor<String>() {
			private String resourceID = "";
			public void processTuple(ResultSet rs) {
				try {
					this.resourceID = rs.getString(1);
				}
				catch (SQLException e) {
					CmsLog.getLog(this).error("Error al intentar buscar si el video existe en la base de datos "+e.getMessage());
				}
			}
			
			public String getResult() {
				return this.resourceID;
			}
			
		};
		
		resourceID = queryBuilder.execute(proc);
		
		if(resourceID!=null && resourceID.length()>0){
			CmsUUID structureUID = new CmsUUID(resourceID);
					
			try {
				CmsResource resource = cmsObject.readResource(structureUID);
				videoBDPath = cmsObject.getRequestContext().removeSiteRoot(resource.getRootPath());
			} catch (CmsException e) {
				e.printStackTrace();
			}
		}
		
		return videoBDPath;
	}

}
