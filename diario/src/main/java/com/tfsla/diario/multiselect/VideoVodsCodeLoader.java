package com.tfsla.diario.multiselect;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.db.CmsDefaultUsers;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.SwitcherHelper;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.search.CmsSearchResult;
import org.opencms.search.TfsAdvancedSearch;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.ImagenService;
import com.tfsla.diario.ediciones.services.PublicationService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.VideoEmbeddedService;
import com.tfsla.diario.ediciones.services.VideosService;
import com.tfsla.diario.file.types.TfsResourceTypeVideoEmbedded;
import com.tfsla.diario.file.types.TfsResourceTypeVideoVodEmbedded;
import com.tfsla.diario.file.types.TfsResourceTypeVideoVodYoutube;
import com.tfsla.diario.file.types.TfsResourceTypeVideoYoutubeLink;
import com.tfsla.utils.CmsObjectUtils;
import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.utils.TfsAdminUserProvider;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;

public class VideoVodsCodeLoader extends A_DescriptionLoader implements I_CmsEventListener {

	private static final Log LOG = CmsLog.getLog(VideoVodsCodeLoader.class);

	public void cmsEvent(CmsEvent event) {		
		int resourceTypePelicula =-1;
		int resourceTypeEpisodio =-1;
		int resourceTypeTemporada = -1;
		int resourceTypeSerie = -1;
		int resourceTypePlaylist  =-1;

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
			
			CmsObject cmsObject = null;
			VideoEmbeddedService videoEmbeddedService = new VideoEmbeddedService();
			try {
				for (CmsResource resource : (Iterable<CmsResource>) event.getData().get("resources")) {
					if (resource.getTypeId()==resourceTypePelicula ||
						resource.getTypeId()==resourceTypeSerie ||
						resource.getTypeId()==resourceTypeEpisodio ||
						resource.getTypeId()==resourceTypeTemporada || 
						resource.getTypeId()==resourceTypePlaylist ) {
					
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
						
					
						Locale locale = cmsObject.getRequestContext().getLocale();
						VideosService vService = VideosService.getInstance(cmsObject);
						String url = CmsResourceUtils.getLink(resource);
						
						if(url.indexOf("~")>-1) {
						     url = "/"+url.replace("~", "");

						     boolean discardChanges = CmsResourceUtils.mustDiscardTempResourceChanges(cmsObject, resource);
						     CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
								
						     if (!discardChanges && site!=null) {
								try {
									
									LOG.debug("Creación de videos desde forms de Vods, extracción de datos de videos asociados.");
									
									cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());

									CmsXmlContent content = getXMLContent(url, cmsObject);
									Boolean isAlreadyLocked = false;
									
									if(!cmsObject.getLock(resource).isUnlocked())
										isAlreadyLocked = true;
									else
									    CmsResourceUtils.forceLockResource(cmsObject, url);
	
									String videoIndex = "VIDEOS_OFFLINE";
									String videosIndexVod = "VOD_OFFLINE";	
									String videoEmbeddedFolder = null;
									String videoYoutubeFolder = null;
	
									String videoVodEmbeddedFolder = null;
									String videoVodYoutubeFolder = null;
	
									CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
									PublicationService publicationService = new PublicationService();
								
									
									TipoEdicionService tEService = new TipoEdicionService();
									try {
										TipoEdicion tEdicion = tEService.obtenerTipoEdicion(cmsObject, url);
										
										if (tEdicion!=null) {
											videoEmbeddedFolder = tEdicion.getVideoEmbeddedDefaultVFSPath();
											videoYoutubeFolder = tEdicion.getVideoYoutubeDefaultVFSPath();
											videoIndex = tEdicion.getVideosIndexOffline();
											videosIndexVod = tEdicion.getVodIndexOffline();
											videoVodEmbeddedFolder = config.getParam(site.getSiteRoot(), String.valueOf(tEdicion.getId()), "vod", "defaultVideoEmbeddedPath") ;
											videoVodYoutubeFolder = config.getParam(site.getSiteRoot(),String.valueOf(tEdicion.getId()), "vod", "defaultVideoYouTubePath");
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
									CmsRelationFilter filter = CmsRelationFilter.TARGETS.filterType(CmsRelationType.valueOf("VideoEmbedded"));
									cmsObject.deleteRelationsFromResource(url, filter);
									
									String basePath = "";
									fillVideoEmbedded(cmsObject, basePath,videoEmbeddedService, locale,
											vService, url, content,
											videoEmbeddedFolder, videoIndex, "videoEmbedded");
									
									filter = CmsRelationFilter.TARGETS.filterType(CmsRelationType.valueOf("VideoYouTube"));
									cmsObject.deleteRelationsFromResource(url, filter);
	
									basePath = "";
									fillVideoYoutube(cmsObject, basePath, locale,
											vService, url, content,
											videoYoutubeFolder, videoIndex,"videoYouTube");
									if (resource.getTypeId()==resourceTypeEpisodio || resource.getTypeId()==resourceTypePelicula || resource.getTypeId()==resourceTypePlaylist ) {
										filter = CmsRelationFilter.TARGETS.filterType(CmsRelationType.valueOf("vodEmbedded"));
										cmsObject.deleteRelationsFromResource(url, filter);
										
										basePath = "";
										fillVideoEmbedded(cmsObject, basePath,videoEmbeddedService, locale,
												vService, url, content,
												videoVodEmbeddedFolder, videosIndexVod, "vodEmbedded");
										
										filter = CmsRelationFilter.TARGETS.filterType(CmsRelationType.valueOf("vodYouTube"));
										cmsObject.deleteRelationsFromResource(url, filter);
		
										basePath = "";
										fillVideoYoutube(cmsObject, basePath, locale,
												vService, url, content,
												videoVodYoutubeFolder, videosIndexVod,"vodYouTube");
		
									}	
									
									if(!isAlreadyLocked)
										   CmsResourceUtils.unlockResource(cmsObject, url, false);
									
								} catch (CmsException e) {
									LOG.error("Error al intentar verificar si el vod tiene videos emmbedded nuevos",e);
								}
						     }
						}
					}
				}
			} catch (CmsException e1) {
				LOG.error("Error al intentar obtener el CmsObject",e1);
			}
		}
	}

	private void fillVideoYoutube(CmsObject cmsObject, String basePath, Locale locale,
			VideosService vService, String url, CmsXmlContent content,
			String videoYoutubeFolder, String videoIndex,String videoType) throws CmsException {
		
		LOG.debug("Vod de la que se van a extraer los videos: "+url);
		
		int videoscount;
		videoscount = content.getIndexCount(basePath + videoType, locale);
		
		LOG.debug("El vod tiene "+videoscount+" videos Youtube asociados.");
		
		for (int j=1;j<=videoscount;j++) {
			String youtubeId = content.getStringValue(cmsObject, basePath + videoType+"[" + j + "]/youtubeid", locale);
			String title = content.getStringValue(cmsObject, basePath + videoType+"[" + j + "]/titulo", locale);
			title = title != null ? title.replaceAll("<[^>]*>", ""): title;
			
			String fuente = content.getStringValue(cmsObject, basePath + videoType+"[" + j + "]/fuente", locale);
			fuente = fuente != null ? fuente.replaceAll("<[^>]*>", ""): fuente;
			String tags = content.getStringValue(cmsObject, basePath + videoType+"[" + j + "]/keywords", locale);
			String imagen = content.getStringValue(cmsObject, basePath + videoType+"[" + j + "]/imagen", locale);
			
			String  descripcion = content.getStringValue(cmsObject, basePath + videoType+"[" + j + "]/descripcion", locale);
			descripcion = descripcion != null ? descripcion.replaceAll("<[^>]*>", ""): descripcion;
			
			String        autor = content.getStringValue(cmsObject, basePath + videoType+"[" + j + "]/autor", locale);
			autor = autor != null ? autor.replaceAll("<[^>]*>", ""): autor;
			
			String calificacion = content.getStringValue(cmsObject, basePath + videoType+"[" + j + "]/calificacion", locale);
			//String    categoria = content.getStringValue(cmsObject, "videoYouTube[" + j + "]/categoria", locale);
			String    categoria = getElementValue(cmsObject,content,basePath + videoType,j,"categoria");
			
			String autoplay = content.getStringValue(cmsObject, basePath + videoType+"[" + j + "]/autoplay", locale);
			String mute = content.getStringValue(cmsObject, basePath + videoType+"[" + j + "]/mute", locale);
			
			if (youtubeId.length()>0) {
				if (youtubeId.indexOf("&")>0)
					youtubeId = youtubeId.split("&")[0];
				if (youtubeId.indexOf("v=")>0){
					youtubeId = youtubeId.split("v=")[1];
				} else if ( youtubeId.indexOf("/") >-1) {
					   String[] temp = youtubeId.split("/");
					   youtubeId = temp[temp.length-1];
				} 
				
				String youtubeVideoPath ="";
				if (videoType.equals("videoYouTube"))
					youtubeVideoPath = videoExist(cmsObject,videoIndex, youtubeId, TfsResourceTypeVideoYoutubeLink.getStaticTypeName());
				else 
					youtubeVideoPath = videoExist(cmsObject,videoIndex, youtubeId, TfsResourceTypeVideoVodYoutube.getStaticTypeName());
				
				if (youtubeVideoPath.length()==0){
					
					LOG.debug("Con Lucene no se encontro el id de youtube ("+youtubeId+"), se busca la property en la bd.");
					
					String videoInBD = videoExistInBD(cmsObject,youtubeId,videoYoutubeFolder);
					
					if(videoInBD.length()>0)
						youtubeVideoPath = videoInBD;
				}
				
				if (youtubeVideoPath.length()==0 || (!videoType.equals("videoYouTube") && !youtubeVideoPath.contains(videoYoutubeFolder))){
					LOG.debug("No se encontró el id de youtube, se creara el video ");
					if (videoType.equals("videoYouTube")) {
						youtubeVideoPath = vService.createYouTubeVideo(videoYoutubeFolder,youtubeId,title,fuente,tags,imagen,descripcion,autor,calificacion,categoria,autoplay,mute);
					} else {
						youtubeVideoPath = vService.createYouTubeVideo(videoYoutubeFolder,youtubeId,title,fuente,tags,imagen,descripcion,autor,calificacion,categoria,autoplay,mute,TfsResourceTypeVideoVodYoutube.getStaticTypeId());
					}
				}
				
				if(youtubeVideoPath.length()>0){
					    vService.addNewsCountToVideo(cmsObject, youtubeVideoPath,videoType);
					    cmsObject.addRelationToResource(url,youtubeVideoPath, CmsRelationType.valueOf(videoType).getName());	
					    
					    LOG.debug("Se agregó el video como relación al vod");
					    
					    if(imagen==null || imagen.trim().equals("")){
					    	LOG.debug("Se va a agregar una imagen de Youtube por defecto. Url: "+url+" YoutubeId: "+youtubeId+" youtubeVideoPath: "+youtubeVideoPath);
					    	uploadImageYoutubeNews(url,basePath + videoType+"[" + j + "]/imagen" ,youtubeId, youtubeVideoPath, cmsObject);
					    }
			    }
			}
		}
	}

	private void fillVideoEmbedded(CmsObject cmsObject, String basePath, 
			VideoEmbeddedService videoEmbeddedService, Locale locale,
			VideosService vService, String url, CmsXmlContent content,
			String videoEmbeddedFolder, String videoIndex,String videoType) throws CmsException {
		int videoscount = content.getIndexCount(basePath + videoType, locale);
		for (int j=1;j<=videoscount;j++) {
			String fullVideoCode = content.getStringValue(cmsObject, basePath + videoType+"[" + j + "]/codigo", locale);
			//String videoCode = videoEmbeddedService.extractVideoCode(fullVideoCode);
			
			String     title = content.getStringValue(cmsObject, basePath + videoType+"[" + j + "]/titulo", locale);
			title = title != null ? title.replaceAll("<[^>]*>", ""): title;
			
			String    fuente = content.getStringValue(cmsObject, basePath + videoType+"[" + j + "]/fuente", locale);
			fuente = fuente != null ? fuente.replaceAll("<[^>]*>", ""): fuente;
			
			String      tags = content.getStringValue(cmsObject, basePath + videoType+"[" + j + "]/keywords", locale);
			String    imagen = content.getStringValue(cmsObject, basePath + videoType+"[" + j + "]/imagen", locale);
			
			String  descripcion = content.getStringValue(cmsObject, basePath + videoType+"[" + j + "]/descripcion", locale);
			descripcion = descripcion != null ? descripcion.replaceAll("<[^>]*>", ""): descripcion;
			
			String        autor = content.getStringValue(cmsObject, basePath + videoType+"[" + j + "]/autor", locale);
			autor = autor != null ? autor.replaceAll("<[^>]*>", ""): autor;
			
			String calificacion = content.getStringValue(cmsObject, basePath + videoType+"[" + j + "]/calificacion", locale);
		    //String    categoria = content.getStringValue(cmsObject, "videoEmbedded[" + j + "]/categoria", locale);
		    String    categoria = getElementValue(cmsObject,content,basePath + videoType,j,"categoria");
			
			if (fullVideoCode.trim().length()>0) {
				
				String emmbededVideoPath ="";
				if (videoType.equals("videoEmbedded"))
					emmbededVideoPath = videoExist(cmsObject,videoIndex, fullVideoCode, TfsResourceTypeVideoVodEmbedded.getStaticTypeName());
				else
					emmbededVideoPath = videoExist(cmsObject,videoIndex, fullVideoCode, TfsResourceTypeVideoVodEmbedded.getStaticTypeName());
				
				if (emmbededVideoPath.length()==0){
					String videoInBD = videoExistInBD(cmsObject,fullVideoCode,videoEmbeddedFolder);
					
					if(videoInBD.length()>0)
						emmbededVideoPath = videoInBD;
				}
				
				if (emmbededVideoPath.length()==0 || (!videoType.equals("videoEmbedded") && !emmbededVideoPath.contains(videoEmbeddedFolder))) {
					if (videoType.equals("videoEmbedded"))
						emmbededVideoPath = vService.createEmbbedeVideo(videoEmbeddedFolder,fullVideoCode,title,fuente,tags,imagen,descripcion,autor,calificacion,categoria);
					else {
						emmbededVideoPath = vService.createEmbbedeVideo(videoEmbeddedFolder,fullVideoCode,title,fuente,tags,imagen,descripcion,autor,calificacion,categoria,TfsResourceTypeVideoVodEmbedded.getStaticTypeId());
					}
				} 	
					
				if(emmbededVideoPath.length()>0){
			     	  vService.addNewsCountToVideo(cmsObject, emmbededVideoPath,videoType);									   	
			     	  cmsObject.addRelationToResource(url,emmbededVideoPath, CmsRelationType.valueOf(videoType).getName());
				}
			}
		}
	}
	
	public static String videoExist(CmsObject cmsObject, String videoIndex, String videoCode, String type) {
		String code = videoCode;
		
		if(type.equals("video-embedded") || type.equals("videoVod-embedded")){
			VideoEmbeddedService videoEmbeddedService = new VideoEmbeddedService();
			code = videoEmbeddedService.extractVideoCode(videoCode);
		}
		
		TfsAdvancedSearch adSearch = new TfsAdvancedSearch();
		adSearch.init(cmsObject);
		
		adSearch.setQuery("+Uid:\"" + code + "\" +Type:\"" + type + "\"");
		adSearch.setMaxResults(5);
		adSearch.setIndex(videoIndex);
		adSearch.setLanguageAnalyzer(new WhitespaceAnalyzer());
		
		List<CmsSearchResult> resultados = adSearch.getSearchResult();
		
		if (resultados!=null && resultados.size()>0) {
			return cmsObject.getRequestContext().removeSiteRoot(resultados.get(0).getPath());
		}
		return "";		
	}
	
	private String videoExistInBD(CmsObject cmsObject, String videoCode, String folder) {
		String videoBDPath = "";
		ArrayList<String> resourceIDList = new ArrayList<String>();
		
		QueryBuilder<ArrayList<String>> queryBuilder = new QueryBuilder<ArrayList<String>>(cmsObject);
        queryBuilder.setSQLQuery("select PROPERTY_MAPPING_ID from CMS_OFFLINE_PROPERTIES, CMS_OFFLINE_PROPERTYDEF where CMS_OFFLINE_PROPERTIES.PROPERTYDEF_ID = CMS_OFFLINE_PROPERTYDEF.PROPERTYDEF_ID AND PROPERTYDEF_NAME = 'video-code' AND PROPERTY_VALUE =? ");
		queryBuilder.addParameter(videoCode);
		
		ResultSetProcessor<ArrayList<String>> proc = new ResultSetProcessor<ArrayList<String>>() {
			private ArrayList<String> resourcesID = new ArrayList<String>();
			public void processTuple(ResultSet rs) {
				
				try {
					this.resourcesID.add ( rs.getString(1));
				} catch (SQLException e) {
					CmsLog.getLog(this).error("Error al intentar buscar si el video existe en la base de datos "+e.getMessage());
				}
			}
			
			public ArrayList<String> getResult() {
				return this.resourcesID;
			}
			
		};
		
		resourceIDList = queryBuilder.execute(proc);
		
		for (String resourceID: resourceIDList){
			CmsUUID structureUID = new CmsUUID(resourceID);
			try {
				CmsResource resource = cmsObject.readResource(structureUID);
				if (resource.getRootPath().contains(folder)){
					videoBDPath = cmsObject.getRequestContext().removeSiteRoot(resource.getRootPath());
					return videoBDPath;
				}
			} catch (CmsException e) {
				e.printStackTrace();
			}
		}
		
		return videoBDPath;
	}
	
	private CmsXmlContent getXMLContent(String url, CmsObject cmsObject) throws CmsException {
		CmsFile contentFile = cmsObject.readFile(url);
		CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmsObject, contentFile);

		try {	
			CmsXmlUtils.validateXmlStructure(contentFile.getContents(), new CmsXmlEntityResolver(cmsObject));
		} catch (CmsException e) {
			CmsResourceUtils.forceLockResource(cmsObject, cmsObject.getSitePath(contentFile));
			content.setAutoCorrectionEnabled(true);
	        content.correctXmlStructure(cmsObject);
	        CmsResourceUtils.unlockResource(cmsObject, cmsObject.getSitePath(contentFile), false);
		}
		
		return content;
	}
	
	protected CmsObject getIndependentCmsObject() throws CmsException {
		CmsObject cmsObject =CmsWorkplaceAction.getInstance().getCmsAdminObject();
		
		CmsObject m_cloneCms = OpenCms.initCmsObject(cmsObject);
		m_cloneCms.getRequestContext().setRequestTime(CmsResource.DATE_RELEASED_EXPIRED_IGNORE);
		m_cloneCms.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));

		return m_cloneCms;
		
	}
	
	private void uploadImageYoutubeNews(String newsPath, String xpath, String youtubeID, String videoVfs, CmsObject cmsObject) {
		String prevImg = null;
		String imageVFS = null;
		
		ArrayList imgList = new ArrayList();
          imgList = VideosService.getInstance(cmsObject).getThumbnails(youtubeID);
          
		if( imgList !=null && imgList.size()>0){
			HashMap<String, String> thumbnail = (HashMap<String, String>)imgList.get(imgList.size()-1);
			prevImg = thumbnail.get("url");
			LOG.debug("Obteniendo img para video Youtube: "+videoVfs+" thumbnail original: "+prevImg);
		}
		
		if(prevImg!=null && !prevImg.equals("")){
			try{
				
				if(ImagenService.getInstance(cmsObject).getDefaultUploadDestination().equals("vfs")) {
					imageVFS = VideosService.getInstance(cmsObject).uploadYoutubeImage(youtubeID, prevImg, "VFS");
				}
				
				if(ImagenService.getInstance(cmsObject).getDefaultUploadDestination().equals("server"))
				{
					imageVFS = VideosService.getInstance(cmsObject).uploadYoutubeImage(youtubeID, prevImg, "RFS");
				}
				
				if(ImagenService.getInstance(cmsObject).getDefaultUploadDestination().equals("ftp"))
				{
					imageVFS = VideosService.getInstance(cmsObject).uploadYoutubeImage(youtubeID, prevImg, "FTP");
				}
				
			} catch (Exception e) {
				LOG.error("Error al intentar subir imagen de video youtube ",e);
			}
			
		}
		
		if(imageVFS!=null && !imageVFS.equals("")){
		
			try{
				if (!cmsObject.getLock(videoVfs).isUnlocked()){
				     if(!cmsObject.getLock(videoVfs).isOwnedBy(cmsObject.getRequestContext().currentUser())){
				    	 cmsObject.changeLock(videoVfs);
				    }
				}else{
					cmsObject.lockResource(videoVfs);
				}
			
				if (!cmsObject.getLock(imageVFS).isUnlocked()){
				     if(!cmsObject.getLock(imageVFS).isOwnedBy(cmsObject.getRequestContext().currentUser())){
				    	 cmsObject.changeLock(imageVFS);
				    }
				}else{
					cmsObject.lockResource(imageVFS);
				}
			
				CmsProperty prop = new CmsProperty();
	            prop.setName("prevImage");
	            prop.setValue(imageVFS, CmsProperty.TYPE_INDIVIDUAL);
	            cmsObject.writePropertyObject(videoVfs,prop);
	      
	            cmsObject.addRelationToResource(videoVfs, imageVFS, "videoImage");
			
	            cmsObject.unlockResource(videoVfs);
	            cmsObject.unlockResource(imageVFS);
		    
				Locale locale = cmsObject.getRequestContext().getLocale();
			
				CmsFile             file = cmsObject.readFile(newsPath);
				CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cmsObject, file);
							  xmlContent.setAutoCorrectionEnabled(true); 
							  xmlContent.correctXmlStructure(cmsObject);
							  xmlContent.getValue(xpath, locale).setStringValue(cmsObject, imageVFS);
							  file.setContents(xmlContent.marshal());
							  cmsObject.writeFile(file);
							  
			} catch (CmsException e) {
				LOG.error("Error al agregar imagen Youtube a video en nota ",e);
			}
		}
	}
}
