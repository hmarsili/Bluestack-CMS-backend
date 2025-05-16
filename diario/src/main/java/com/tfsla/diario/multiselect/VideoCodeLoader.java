package com.tfsla.diario.multiselect;

import org.opencms.file.SwitcherHelper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.opencms.db.CmsDefaultUsers;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.search.CmsSearch;
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
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.VideoEmbeddedService;
import com.tfsla.diario.ediciones.services.VideosService;
import com.tfsla.diario.file.types.TfsResourceTypeVideoEmbedded;
import com.tfsla.diario.file.types.TfsResourceTypeVideoYoutubeLink;
import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.utils.TfsAdminUserProvider;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;

public class VideoCodeLoader extends A_DescriptionLoader implements I_CmsEventListener {

	private static final Log LOG = CmsLog.getLog(VideoCodeLoader.class);

	public void cmsEvent(CmsEvent event) {		
		int noticiaType = getNoticiaType();
		
		if (event.getType()==I_CmsEventListener.EVENT_BEFORE_RESOURCE_DELETED) {

			CmsObject cmsObject;
			VideoEmbeddedService videoEmbeddedService = new VideoEmbeddedService();
			try {
				for (CmsResource resource : (Iterable<CmsResource>) event.getData().get("resources")) {
					if (resource.getTypeId()==noticiaType) {
					
						cmsObject = SwitcherHelper.getUserCmsObject(resource.getUserLastModified());
	
						Locale locale = cmsObject.getRequestContext().getLocale();
						VideosService vService = VideosService.getInstance(cmsObject);
						String url = CmsResourceUtils.getLink(resource);
						
						if(url.indexOf("~")>-1) {
						     url = "/"+url.replace("~", "");

						     boolean discardChanges = CmsResourceUtils.mustDiscardTempResourceChanges(cmsObject, resource);
						     CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
								
						     if (!discardChanges && site!=null) {
								try {
									
									LOG.debug("Creación de videos desde form de noticia, extracción de datos de videos asociados.");
									
									cmsObject.getRequestContext().setSiteRoot(site.getSiteRoot());

									CmsXmlContent content = getXMLContent(url, cmsObject);
									Boolean isAlreadyLocked = false;
									
									if(!cmsObject.getLock(resource).isUnlocked())
										isAlreadyLocked = true;
									else
									    CmsResourceUtils.forceLockResource(cmsObject, url);
	
									CmsRelationFilter filter = CmsRelationFilter.TARGETS.filterType(CmsRelationType.valueOf("VideoEmbedded"));
									cmsObject.deleteRelationsFromResource(url, filter);
	
									String videoEmbeddedFolder = null;
									String videoYoutubeFolder = null;
	
									String videoIndex = "VIDEOS_OFFLINE";
	
									TipoEdicionService tEService = new TipoEdicionService();
									try {
										TipoEdicion tEdicion = tEService.obtenerTipoEdicion(cmsObject, url);
										
										if (tEdicion!=null) {
											videoEmbeddedFolder = tEdicion.getVideoEmbeddedDefaultVFSPath();
											videoYoutubeFolder = tEdicion.getVideoYoutubeDefaultVFSPath();
											videoIndex = tEdicion.getVideosIndexOffline();
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
									
									int videoscount;
									
									String basePath = "";
									fillVideoEmbedded(cmsObject, basePath,
											videoEmbeddedService, locale,
											vService, url, content,
											videoEmbeddedFolder, videoIndex);

									videoscount = content.getIndexCount("noticiaLista", locale);
									for (int j=1;j<=videoscount;j++) {
										 basePath = "noticiaLista[" + j + "]/";

										 fillVideoEmbedded(cmsObject, basePath,
													videoEmbeddedService, locale,
													vService, url, content,
													videoEmbeddedFolder, videoIndex);

									}

									filter = CmsRelationFilter.TARGETS.filterType(CmsRelationType.valueOf("VideoYouTube"));
									cmsObject.deleteRelationsFromResource(url, filter);
	
									basePath = "";
									fillVideoYoutube(cmsObject, basePath, locale,
											vService, url, content,
											videoYoutubeFolder, videoIndex);
									
									videoscount = content.getIndexCount("noticiaLista", locale);
									for (int j=1;j<=videoscount;j++) {
										 basePath = "noticiaLista[" + j + "]/";

											fillVideoYoutube(cmsObject, basePath, locale,
													vService, url, content,
													videoYoutubeFolder, videoIndex);

									}
			
									if(!isAlreadyLocked)
										   CmsResourceUtils.unlockResource(cmsObject, url, false);
									
								}
								catch (CmsException e) {
									LOG.error("Error al intentar verificar si la noticia tiene video emmbedded nuevos",e);
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
			String videoYoutubeFolder, String videoIndex) throws CmsException {
		
		LOG.debug("Noticia de la que se van a extraer los videos: "+url);
		
		int videoscount;
		videoscount = content.getIndexCount(basePath + "videoYouTube", locale);
		
		LOG.debug("La noticia tiene "+videoscount+" videos Youtube asociados.");
		
		for (int j=1;j<=videoscount;j++) {
			String youtubeId = content.getStringValue(cmsObject, basePath + "videoYouTube[" + j + "]/youtubeid", locale);
			String title = content.getStringValue(cmsObject, basePath + "videoYouTube[" + j + "]/titulo", locale);
			title = title != null ? title.replaceAll("<[^>]*>", ""): title;
			
			String fuente = content.getStringValue(cmsObject, basePath + "videoYouTube[" + j + "]/fuente", locale);
			fuente = fuente != null ? fuente.replaceAll("<[^>]*>", ""): fuente;
			String tags = content.getStringValue(cmsObject, basePath + "videoYouTube[" + j + "]/keywords", locale);
			String imagen = content.getStringValue(cmsObject, basePath + "videoYouTube[" + j + "]/imagen", locale);
			
			String  descripcion = content.getStringValue(cmsObject, basePath + "videoYouTube[" + j + "]/descripcion", locale);
			descripcion = descripcion != null ? descripcion.replaceAll("<[^>]*>", ""): descripcion;
			
			String        autor = content.getStringValue(cmsObject, basePath + "videoYouTube[" + j + "]/autor", locale);
			autor = autor != null ? autor.replaceAll("<[^>]*>", ""): autor;
			
			String calificacion = content.getStringValue(cmsObject, basePath + "videoYouTube[" + j + "]/calificacion", locale);
			//String    categoria = content.getStringValue(cmsObject, "videoYouTube[" + j + "]/categoria", locale);
			String    categoria = getElementValue(cmsObject,content,basePath + "videoYouTube",j,"categoria");
			
			String autoplay = content.getStringValue(cmsObject, basePath + "videoYouTube[" + j + "]/autoplay", locale);
			String mute = content.getStringValue(cmsObject, basePath + "videoYouTube[" + j + "]/mute", locale);
			
			if (youtubeId.length()>0) {
				if (youtubeId.indexOf("&")>0)
					youtubeId = youtubeId.split("&")[0];
				if (youtubeId.indexOf("v=")>0){
					youtubeId = youtubeId.split("v=")[1];
				} else if ( youtubeId.indexOf("/") >-1) {
					   String[] temp = youtubeId.split("/");
					   youtubeId = temp[temp.length-1];
				} 
				
				String youtubeVideoPath = videoExist(cmsObject,videoIndex, youtubeId, TfsResourceTypeVideoYoutubeLink.getStaticTypeName());
				
				if (youtubeVideoPath.length()==0){
					
					LOG.debug("Con Lucene no se encontro el id de youtube ("+youtubeId+"), se busca la property en la bd.");
					
					String videoInBD = videoExistInBD(cmsObject,youtubeId);
					
					if(videoInBD.length()>0)
						youtubeVideoPath = videoInBD;
				}
				
				if (youtubeVideoPath.length()==0){
					LOG.debug("No se encontró el id de youtube, se creara el video ");
					youtubeVideoPath = vService.createYouTubeVideo(videoYoutubeFolder,youtubeId,title,fuente,tags,imagen,descripcion,autor,calificacion,categoria,autoplay,mute);
				}
				
				if(youtubeVideoPath.length()>0){
					    vService.addNewsCountToVideo(cmsObject, youtubeVideoPath,"VideoYouTube");
					    cmsObject.addRelationToResource(url,youtubeVideoPath, CmsRelationType.valueOf("VideoYouTube").getName());	
					    
					    LOG.debug("Se agregó el video como relación a la noticia");
					    
					    
					    if(imagen==null || imagen.trim().equals("")){
					    	LOG.debug("Se va a agregar una imagen de Youtube por defecto. Url: "+url+" YoutubeId: "+youtubeId+" youtubeVideoPath: "+youtubeVideoPath);
					    	uploadImageYoutubeNews(url,basePath + "videoYouTube[" + j + "]/imagen" ,youtubeId, youtubeVideoPath, cmsObject);
					    }
			    }
			}
		}
	}

	private void fillVideoEmbedded(CmsObject cmsObject, String basePath, 
			VideoEmbeddedService videoEmbeddedService, Locale locale,
			VideosService vService, String url, CmsXmlContent content,
			String videoEmbeddedFolder, String videoIndex) throws CmsException {
		int videoscount = content.getIndexCount(basePath + "videoEmbedded", locale);
		for (int j=1;j<=videoscount;j++) {
			String fullVideoCode = content.getStringValue(cmsObject, basePath + "videoEmbedded[" + j + "]/codigo", locale);
			//String videoCode = videoEmbeddedService.extractVideoCode(fullVideoCode);
			
			String     title = content.getStringValue(cmsObject, basePath + "videoEmbedded[" + j + "]/titulo", locale);
			title = title != null ? title.replaceAll("<[^>]*>", ""): title;
			
			String    fuente = content.getStringValue(cmsObject, basePath + "videoEmbedded[" + j + "]/fuente", locale);
			fuente = fuente != null ? fuente.replaceAll("<[^>]*>", ""): fuente;
			
			String      tags = content.getStringValue(cmsObject, basePath + "videoEmbedded[" + j + "]/keywords", locale);
			String    imagen = content.getStringValue(cmsObject, basePath + "videoEmbedded[" + j + "]/imagen", locale);
			
			String  descripcion = content.getStringValue(cmsObject, basePath + "videoEmbedded[" + j + "]/descripcion", locale);
			descripcion = descripcion != null ? descripcion.replaceAll("<[^>]*>", ""): descripcion;
			
			String        autor = content.getStringValue(cmsObject, basePath + "videoEmbedded[" + j + "]/autor", locale);
			autor = autor != null ? autor.replaceAll("<[^>]*>", ""): autor;
			
			String calificacion = content.getStringValue(cmsObject, basePath + "videoEmbedded[" + j + "]/calificacion", locale);
		    //String    categoria = content.getStringValue(cmsObject, "videoEmbedded[" + j + "]/categoria", locale);
		    String    categoria = getElementValue(cmsObject,content,basePath + "videoEmbedded",j,"categoria");
			
			if (fullVideoCode.trim().length()>0) {
				String emmbededVideoPath = videoExist(cmsObject,videoIndex, fullVideoCode, TfsResourceTypeVideoEmbedded.getStaticTypeName());
				
				if (emmbededVideoPath.length()==0){
					String videoInBD = videoExistInBD(cmsObject,fullVideoCode);
					
					if(videoInBD.length()>0)
						emmbededVideoPath = videoInBD;
				}
				
				if (emmbededVideoPath.length()==0)
					emmbededVideoPath = vService.createEmbbedeVideo(videoEmbeddedFolder,fullVideoCode,title,fuente,tags,imagen,descripcion,autor,calificacion,categoria);
			
				if(emmbededVideoPath.length()>0){
			     	  vService.addNewsCountToVideo(cmsObject, emmbededVideoPath,"VideoEmbedded");									   	
			     	  cmsObject.addRelationToResource(url,emmbededVideoPath, CmsRelationType.valueOf("VideoEmbedded").getName());
				}
			}
		}
	}
	
	public static String videoExist(CmsObject cmsObject, String videoIndex, String videoCode, String type)
	{
		String code = videoCode;
		
		if(type.equals("video-embedded")){
			
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
	
	public static String videoExistInBD(CmsObject cmsObject, String videoCode) {
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
	
	private int getNoticiaType() {
		int noticiaType =-1;

		try {
			noticiaType = OpenCms.getResourceManager().getResourceType("noticia").getTypeId();
		} catch (CmsLoaderException e) {
			LOG.error("Error al intentar obtener el identificador de la noticia",e);
		}
		
		return noticiaType;
	}


	protected CmsObject getIndependentCmsObject() throws CmsException {
		CmsObject cmsObject =CmsWorkplaceAction.getInstance().getCmsAdminObject();
		
		CmsObject m_cloneCms = OpenCms.initCmsObject(cmsObject);
		m_cloneCms.getRequestContext().setRequestTime(CmsResource.DATE_RELEASED_EXPIRED_IGNORE);
		m_cloneCms.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));

		return m_cloneCms;
		
	}
	
	private void uploadImageYoutubeNews(String newsPath, String xpath, String youtubeID, String videoVfs, CmsObject cmsObject)
	{
		String prevImg = null;
		String imageVFS = null;
		
		ArrayList imgList = new ArrayList();
          imgList = VideosService.getInstance(cmsObject).getThumbnails(youtubeID);
          
		if( imgList !=null && imgList.size()>0){
			//Toma la mas grande
			HashMap<String, String> thumbnail = (HashMap<String, String>)imgList.get(imgList.size()-1);
			prevImg = thumbnail.get("url");
			LOG.debug("Obteniendo img para video Youtube: "+videoVfs+" thumbnail original: "+prevImg);
		}
		
		if(prevImg!=null && !prevImg.equals("")){
			
			try{
				
				if(ImagenService.getInstance(cmsObject).getDefaultUploadDestination().equals("vfs"))
				{
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
