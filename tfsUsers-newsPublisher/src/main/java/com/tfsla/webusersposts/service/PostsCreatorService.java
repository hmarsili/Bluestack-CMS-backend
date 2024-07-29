package com.tfsla.webusersposts.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.jsp.CmsJspLoginBean;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContent;
import org.apache.commons.fileupload.FileItem;

import com.tfsla.diario.ediciones.services.PublicationService;
import com.tfsla.planilla.herramientas.PlanillaFormConstants;
import com.tfsla.webusersnewspublisher.service.UploadImageManager;
import com.tfsla.webusersnewspublisher.model.News;
import com.tfsla.webusersnewspublisher.service.NewsPublisherManager;
import com.tfsla.webusersposts.common.UserPost;
import com.tfsla.webusersposts.helper.XmlContentHelper;
import com.tfsla.webusersposts.strings.ExceptionMessages;

/**
 * This class provides services to add new posts (drafts)
 * by abstracting this logic from the presentation model
 */
public class PostsCreatorService {

	public Map<String, Object> getParametersMap() {
		return this.map;
	}
	
	public PostsCreatorService(String site, int publication) {
		this.site = site;
		this.publication = publication;
	}
	
	public PostsCreatorService(CmsObject cms) throws Exception {
		this.site = cms.getRequestContext().getSiteRoot();
		this.publication = PublicationService.getPublicationId(cms);
	}

	/**
	 * Retrieves a boolean indicating if the anonymous posts are allowed
	 * or not for the current site / publication
	 * @param site the site to check for the configuration
	 * @param publication the publication to check for the configuration
	 * @return true if anonymous posts are enabled, false otherwise
	 */
	public static boolean anonymousPostsAllowed(String site, int publication) {
		return CONFIG.getParam(site, String.valueOf(publication), "newsPublisher", "allowAnonymousPosts").equals(Boolean.toString(true));
	}
	
	/**
	 * Retrieves a boolean indicating if the anonymous posts are allowed
	 * or not for the current site / publication
	 * @param cms session CmsObject with an initialized context
	 * @return true if anonymous posts are enabled, false otherwise
	 * @throws Exception 
	 */
	public static boolean anonymousPostsAllowed(CmsObject cms) throws Exception {
		String site = cms.getRequestContext().getSiteRoot();
		int publication = PublicationService.getPublicationId(cms);
		return CONFIG.getParam(site, String.valueOf(publication), "newsPublisher", "allowAnonymousPosts").equals(Boolean.toString(true));
	}
	
	/**
	 * Creates a user post by processing the parameters in the current request
	 * @param pageContext current PageContext instance
	 * @param request Http request
	 * @param response Http response
	 * @return UserPost instance
	 * @throws Exception when trying to create an anoymous posts and the configuration
	 * does not allows it or if some parameters are missing
	 */
	@SuppressWarnings("unchecked")
	public UserPost createUserPost(PageContext pageContext, HttpServletRequest request, HttpServletResponse response) throws Exception {
		//obtengo el usuario logueado
		CmsJspLoginBean loginBean = new CmsJspLoginBean(pageContext, request, response);
		CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);

		//valido si hay usuario logueado
		boolean userIsLoggedIn = loginBean.isLoggedIn();
		boolean allowAnonymous = CONFIG.getParam(site, String.valueOf(publication), "newsPublisher", "allowAnonymousPosts").equals(Boolean.toString(true));

		if (!userIsLoggedIn && !allowAnonymous) {
			throw new Exception(String.format(ExceptionMessages.ERROR_ANONYMOUS_POSTS_NOT_ENABLED, site, publication));
		}

		String internalUser = loginBean.getUserName();
		if((internalUser.equals("") || internalUser.equals("Guest")) && !allowAnonymous){
			throw new Exception(String.format(ExceptionMessages.ERROR_ANONYMOUS_POSTS_NOT_ENABLED, site, publication));
		}

		//obtengo en un map todos los campos del formulario	
		NewsPublisherManager publishNewsManager = new NewsPublisherManager(request);
		publishNewsManager.setSite(site);
		this.map = publishNewsManager.getParamsForm();
		String email = null;

		//email obligatorio para posts anónimos
		if(allowAnonymous && !userIsLoggedIn) {
			if(!map.containsKey("email")) throw new Exception(ExceptionMessages.ERROR_EMAIL_ANONYMOUS_POSTS);
			email = ((FileItem)map.get("email")).getString();
			if(email == null || email.equals(""))  throw new Exception(ExceptionMessages.ERROR_EMAIL_ANONYMOUS_POSTS);
		}

		News news = new News();
		List<String> videosEmbedded = new ArrayList<String>();
		List<String> imagesGalery = new ArrayList<String>();
		List<String> imagesNames = new ArrayList<String>();
		List<String> images64Paths = new ArrayList<String>();
		String imagesPreview = "";
		Boolean isPreview = false;

		String path = ((FileItem)map.get("path")).getString();
		if(path != null && !path.equals("") && !path.trim().equals("null")) {
			path = site + path;
		} else {
			path = null;
		}

		//guardo los datos del formulario
		String postId = this.getStringValueFromMap("postId");
		String title = this.getStringValueFromMap("titulo");	
		String content = this.getStringValueFromMap("contenido");
		String keywords = this.getStringValueFromMap("temas");
		String section = this.getStringValueFromMap("seccion");
		String imagespaths = this.getStringValueFromMap("imagesPreload");
		List<FileItem> images = (ArrayList<FileItem>)map.get("images");
		List<String> images64_data = (ArrayList<String>)map.get("images64.data");
		List<String> images64_names = (ArrayList<String>)map.get("images64.names");
		List<String> videos = (ArrayList<String>)map.get("videos");
		List<String> fuentes = (ArrayList<String>)map.get("fuentes");
		List<String> categories = (ArrayList<String>)map.get("categories");
		
		content = this.getEncodedString(content);
		keywords = this.getEncodedString(keywords);
		title = this.getEncodedString(title);
		title = title.replace("<","").replace(">","").replace("!","").replace("¡","").replace("--","").replace("script","").replace("SCRIPT","");

		CmsProject currentProject = cms.getCmsObject().getRequestContext().currentProject();

		//images
		UploadImageManager uploadManager = new UploadImageManager();
		uploadManager.setSite(site);
		uploadManager.setInternalUser(internalUser);
		uploadManager.setMode(isPreview);
		uploadManager.setAutoPublishImages(false);

		//cuento la cantidad de imgs que voy a subir
		int cantImagesUpload = 0;
		for (FileItem item : images) {
			imagesNames.add(uploadManager.upload(item));
			cantImagesUpload++;
		}
		for(String item : images64_names) {
			String imageData = images64_data.get(images64_names.indexOf(item));
			images64Paths.add(uploadManager.upload(imageData, item));
			cantImagesUpload++;
		}
		
		//publica las impagenes y el arbol de carpetas, en caso de que lo haya creado
		uploadManager.publish();

		//esto es porque el UploadImageManager cambia el currentProject, lo restablezco acá
		cms.getCmsObject().getRequestContext().setCurrentProject(currentProject);

		//armo el iframe de los videos
		for (String item:videos) {
			videosEmbedded.add(
				String.format("<iframe width=\"%s\" height=\"%s\" frameborder=\"0\" src=\"%s\"></iframe>",
					CONFIG.getParam(site, String.valueOf(publication), "newsPublisher", "videosWidth"),
					CONFIG.getParam(site, String.valueOf(publication), "newsPublisher", "videosHeight"),
					item
				)
			);
		}
		
		if(imagespaths != null && !imagespaths.equals("") && !imagespaths.trim().equals("null")) {
			//armo la lista de imagenes ya cargadas
			String[] imagenes = imagespaths.split(",");
			for (String item:imagenes) {
				imagesGalery.add(site+item);
			}
			//selecciono la primer img cargada como preview
			imagesPreview = imagenes[0];
		}

		news.setMode(isPreview);

		XmlContentHelper contentHelper = new XmlContentHelper(cms.getCmsObject(), isPreview);
		contentHelper.setXmlValue("urlFriendly", news.removeInvalidXmlCharacters(title));
		contentHelper.setXmlValue("estado", PlanillaFormConstants.PUBLICADA_VALUE);
		contentHelper.setXmlValue("autor/internalUser", internalUser);
		contentHelper.setXmlValue("ultimaModificacion", "" + news.getDateCreated().getTime());
		contentHelper.setXmlValue("titulo", title);
		contentHelper.setXmlValue("cuerpo", content);
		contentHelper.setXmlValue("claves", keywords);
		contentHelper.setXmlValue("seccion", section);
		contentHelper.setXmlListValue("Categorias[%s]", categories, "Categorias");
		contentHelper.setXmlListValue("fuente[%s]/nombre", fuentes, "fuente");
		contentHelper.setXmlListValue("videoEmbedded[%s]/codigo", videosEmbedded, "videoEmbedded");
		contentHelper.setXmlImageValues(images, imagesNames, images64_names, images64Paths);
		contentHelper.setXmlListValue("imagenesFotogaleria[%s]/imagen", imagesGalery, "imagenesFotogaleria", cantImagesUpload);
		contentHelper.setXmlValue("seccion", this.getEncodedString(this.getStringValueFromMap("seccion")));
		for(int i=1; i<=10; i++) {
			String customString = "custom" + String.valueOf(i);
			contentHelper.setXmlValue(customString, this.getEncodedString(this.getStringValueFromMap(customString)));
		}
		
		//seteo de campos extra no mapeados
		String extraFieldsSize = this.getStringValueFromMap("extraFieldsSize");
		if(extraFieldsSize != null && !extraFieldsSize.equals("")) {
			String fieldName = null;
			String fieldValue = null;
			for(int i = 0; i < Integer.parseInt(extraFieldsSize); i++) {
				fieldName = this.getStringValueFromMap(String.format("extraFieldName_%s", i));
				fieldValue = this.getStringValueFromMap(String.format("extraFieldValue_%s", i));
				if(fieldName.equals("")) continue;
				
				contentHelper.setXmlValue(fieldName, this.getEncodedString(fieldValue));
			}
		}
		
		//si no hay img nuevas, el preview es la primera precargada
		if (cantImagesUpload == 0) {contentHelper.setXmlValue("imagenPrevisualizacion/imagen", imagesPreview);}

		CmsXmlContent xmlContent = contentHelper.getXmlContent();
		byte[] plainContent = xmlContent.marshal();
		String contentAsString = new String(plainContent);
		UserPostsService service = new UserPostsService();
		UserPost userPost = null;
		try {
			if(!userIsLoggedIn) {
				userPost = service.createAnonymousDraft(
					title,
					contentAsString,
					email,
					publication,
					site,
					path
				);
			} else {
				if(path == null || path.equals("")) {
					if(postId != null && !postId.equals("")) {
						userPost = service.savePost(
							title,
							contentAsString,
							loginBean.getUser().getId().toString(),
							publication,
							site,
							"",
							postId
						);
					} else {
						userPost = service.savePost(
							title,
							contentAsString,
							loginBean.getUser().getId().toString(),
							publication,
							site,
							""
						);
					}
				} else {
					userPost = service.createDraft(
						title,
						contentAsString,
						loginBean.getUser().getId().toString(),
						publication,
						site,
						path
					);
				}
			}
		} catch(Exception e) {
			OpenCms.getLog(this).error("Error al procesar post", e);
			e.printStackTrace();
			throw e;
		}
		
		return userPost;
	}
	
	private String getEncodedString(String value) throws UnsupportedEncodingException {
		byte bytesValue[] = value.getBytes("ISO-8859-1"); 
		return new String(bytesValue, "UTF-8");
	}
	
	private String getStringValueFromMap(String key) {
		if(!map.containsKey(key)) return "";
		return ((FileItem)map.get(key)).getString();
	}

	private static CPMConfig CONFIG = CmsMedios.getInstance().getCmsParaMediosConfiguration();
	private int publication;
	private String site;
	private Map<String, Object> map;
}
