package com.tfsla.diario.webservices;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.tfsla.diario.ediciones.services.VideoEmbeddedService;
import com.tfsla.diario.webservices.common.ServiceType;
import com.tfsla.diario.webservices.common.interfaces.IPostsListService;
import com.tfsla.diario.webservices.core.services.TfsListWebService;
import com.tfsla.webusersposts.common.UserPost;
import com.tfsla.webusersposts.common.XmlUserPost;
import com.tfsla.webusersposts.service.PostsService;
import com.tfsla.webusersposts.service.UserPostsService;

public class PostsListService extends TfsListWebService implements IPostsListService {

	public PostsListService(PageContext context, HttpServletRequest request, HttpServletResponse response) throws Throwable {
		super(context, request, response);
		
	}

	@Override
	protected JSON doExecute() throws Throwable {
		Map<String,Object> filters = this.getFilters();
		JSONArray jsonResponse = new JSONArray();
		UserPostsService userPostsService = new UserPostsService();
		PostsService postsService = new PostsService();
		String userId = (String)filters.get("userid");
		String site = (String)filters.get("site");
		String publication = (String)filters.get("publication");
		String status = (String)filters.get("status");
		int size = (Integer)filters.get("size");
		int from = filters.containsKey("from") && filters.get("from") != null && !filters.get("from").toString().equals("") ? Integer.parseInt(filters.get("from").toString()) : 0;
		
		if(userId == null || userId.equals("")) {
			userId = this.cms.getRequestContext().currentUser().getId().toString();
		}
		if(status == null) {
			status = "";
		}
		
		for(UserPost post : userPostsService.getUserPosts(userId, site, publication, size, from, status)) {
			JSONObject jsonPost = new JSONObject();
			jsonPost.put("id", post.getId());
			jsonPost.put("title", post.getTitle());
			jsonPost.put("url", post.getUrl());
			jsonPost.put("status", post.getStatus().toString().toLowerCase());
			jsonPost.put("moderation-message", post.getModerationMessage());
			jsonPost.put("date-created", post.getCreationDate().getTime());
			jsonPost.put("site", post.getSite());
			jsonPost.put("publication", post.getPublication());
			
			//Get post detail
			XmlUserPost xmlPost = postsService.getXmlPost(post, cms);
			jsonPost.put("tags", xmlPost.getKeywords());
			jsonPost.put("content", xmlPost.getBody());
			
			this.setPostListItems(xmlPost.getImages(), jsonPost, "images", false);
			this.setPostListItems(xmlPost.getVideos(), jsonPost, "videos", true);
			this.setPostListItems(xmlPost.getSources(), jsonPost, "sources", false);
			
			jsonResponse.add(jsonPost);
		}
		
		return jsonResponse;
	}
	
	@Override
	protected ServiceType getServiceType() {
		return ServiceType.POSTS_LIST;
	}
	
	private void setPostListItems(List<String> items, JSONObject jsonPost, String jsonName, Boolean extractCode) {
		if(items == null || items.size() == 0) return;
		
		JSONArray jsonItems = new JSONArray();
		for(String item : items) {
			if(item != null && !item.trim().equals("")) {
				if(!extractCode) {
					jsonItems.add(item);
				} else {
					if(item.contains("src=")) {
						VideoEmbeddedService videoService = new VideoEmbeddedService();
						jsonItems.add(videoService.extractVideoCode(item));
					} else {
						jsonItems.add(item);
					}
				}
			}
		}
		if(jsonItems.size() > 0) {
			jsonPost.put(jsonName, jsonItems);
		}
	}
}
