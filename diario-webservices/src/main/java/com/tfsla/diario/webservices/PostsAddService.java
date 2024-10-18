package com.tfsla.diario.webservices;

import java.util.ArrayList;
import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;

import org.opencms.xml.content.CmsXmlContent;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.tfsla.diario.ediciones.services.PublicationService;
import com.tfsla.diario.webservices.common.ServiceHelper;
import com.tfsla.diario.webservices.common.interfaces.IPostsAddService;
import com.tfsla.diario.webservices.common.strings.ExceptionMessages;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.core.GuestSessionManager;
import com.tfsla.diario.webservices.core.services.TfsWebService;
import com.tfsla.planilla.herramientas.PlanillaFormConstants;
import com.tfsla.webusersposts.common.PostStatus;
import com.tfsla.webusersposts.common.UserPost;
import com.tfsla.webusersposts.helper.XmlContentHelper;
import com.tfsla.webusersposts.service.UserPostsService;

public class PostsAddService extends TfsWebService implements IPostsAddService {

	public PostsAddService(HttpServletRequest request) throws Throwable {
		super(GuestSessionManager.checkForGuestSession(request, null, null));
		
		String stringRequest = ServiceHelper.getRequestAsString(request);
		JSONObject jsonRequest = JSONObject.fromObject(stringRequest);
		this.requestItems = jsonRequest.getJSONArray(StringConstants.DATA);
		this.site = jsonRequest.getString(StringConstants.SITE);
		if(this.site != null && !this.site.equals("")) {
			this.cms.getRequestContext().setSiteRoot(this.site);
		}
	}

	@Override
	protected JSON doExecute() throws Throwable {
		JSONArray jsonResponse = new JSONArray();
		UserPostsService postsService = new UserPostsService();
		String publication = String.valueOf(PublicationService.getPublicationId(this.cms));
		if(this.isGuest()) {
			String allowAnonymousConfig = this.config.getParam(site, publication, "newsPublisher", "allowAnonymousPosts");
			if(allowAnonymousConfig == null || !allowAnonymousConfig.equals(Boolean.toString(true))) {
				throw new Exception(String.format(ExceptionMessages.ERROR_ANONYMOUS_POSTS_NOT_ALLOWED, this.site, publication));
			}
		}
		
		for(int i=0; i<requestItems.size(); i++) {
			JSONObject item = new JSONObject();
			
			try {
				JSONObject post = requestItems.getJSONObject(i);
				this.assertParameters(post);
				
				String xmlContent = this.getXmlContentAsString(post);
				UserPost userPost = null;
				if(this.isGuest()) {
					userPost = postsService.createAnonymousDraft(
							post.getString("title"),
							xmlContent, 
							post.getString("email"),
							PublicationService.getPublicationId(cms), 
							site, 
							""
						);
					if(!userPost.getUserPending()) {
						postsService.changeStatus(userPost, PostStatus.PENDING);
					}
				} else {
					userPost = postsService.savePost(
						post.getString("title"),
						xmlContent,
						this.cms.getRequestContext().currentUser().getId().toString(),
						PublicationService.getPublicationId(cms),
						site
					);
					postsService.changeStatus(userPost, PostStatus.PENDING);
				}
				
				item.put("post-status", userPost.getStatus().getValue());
				item.put(StringConstants.STATUS, StringConstants.OK);
				item.put(StringConstants.INDEX, i);
				item.put(StringConstants.ID, userPost.getId());
				if(this.isGuest()) {
					item.put("registered", userPost.getUserRegistered());
					item.put("pending", userPost.getUserPending());
				}
			} catch(Exception e) {
				item.put(StringConstants.STATUS, StringConstants.ERROR);
				item.put(StringConstants.INDEX, i);
				item.put(StringConstants.MESSAGE, e.getMessage());
			}
			jsonResponse.add(item);
		}

		return jsonResponse;
	}

	protected void assertParameters(JSONObject item) throws Exception {
		if(!item.containsKey("title") || item.getString("title") == null || item.getString("title").equals("")) {
			throw new Exception(String.format(ExceptionMessages.MISSING_OR_EMPTY_PARAMETER, "title"));
		}
		if(!item.containsKey("body") || item.getString("body") == null || item.getString("body").equals("")) {
			throw new Exception(String.format(ExceptionMessages.MISSING_OR_EMPTY_PARAMETER, "body"));
		}
		if(this.isGuest() && !item.containsKey("email")) {
			throw new Exception(String.format(ExceptionMessages.MISSING_OR_EMPTY_PARAMETER, "email"));
		}
	}
	
	protected String getXmlContentAsString(JSONObject item) throws Exception {
		CmsXmlContent xmlContent = this.getXmlContent(item);
		byte[] plainContent = xmlContent.marshal();
		String contentAsString = new String(plainContent);
		return contentAsString;
	}
	
	protected CmsXmlContent getXmlContent(JSONObject item) throws Exception {
		XmlContentHelper contentHelper = new XmlContentHelper(cms);
		contentHelper.setXmlValue("estado", PlanillaFormConstants.PUBLICADA_VALUE);
		contentHelper.setXmlValue("autor/internalUser", this.cms.getRequestContext().currentUser().getName());
		contentHelper.setXmlValue("ultimaModificacion", "" + (new Date()).getTime());
		contentHelper.setXmlValue("cuerpo", item.getString("body"));
		contentHelper.setXmlValue("titulo", item.getString("title"));
		contentHelper.setXmlValue("urlFriendly", item.getString("title"));
		
		if(item.containsKey("section"))
			contentHelper.setXmlValue("seccion", item.getString("section"));
		if(item.containsKey("keywords"))
			contentHelper.setXmlValue("claves", item.getString("keywords"));
		if(item.containsKey("image-preview"))
			contentHelper.setXmlValue("imagenPrevisualizacion/imagen", item.getString("image-preview"));
		if(item.containsKey("categories"))
			contentHelper.setXmlListValue("Categorias[%s]", getListFromJSON(item.getJSONArray("categories")));
		if(item.containsKey("sources"))
			contentHelper.setXmlListValue("fuente[%s]/nombre", getListFromJSON(item.getJSONArray("sources")), "fuente");
		if(item.containsKey("videos"))
			contentHelper.setXmlListValue("videoFlash[%s]/video", getListFromJSON(item.getJSONArray("videos")), "videoFlash");
		if(item.containsKey("videos-embedded"))
			contentHelper.setXmlListValue("videoEmbedded[%s]/codigo", getListFromJSON(item.getJSONArray("videos-embedded")), "videoEmbedded");
		if(item.containsKey("images"))
			contentHelper.setXmlListValue("imagenesFotogaleria[%s]/imagen", getListFromJSON(item.getJSONArray("images")), "imagenesFotogaleria");
		
		return contentHelper.getXmlContent();
	}
	
	protected ArrayList<String> getListFromJSON(JSONArray array) {
		ArrayList<String> list = new ArrayList<String>();
		for(int i=0; i<array.size(); i++) {
			list.add(array.get(i).toString());
		}
		return list;
	}
	
	protected JSONArray requestItems;
	protected String site;
}
