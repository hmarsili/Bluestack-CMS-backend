package com.tfsla.webusersposts.service;

import java.util.ArrayList;
import java.util.List;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;

import com.tfsla.webusersposts.common.UserPost;
import com.tfsla.webusersposts.common.XmlUserPost;
import com.tfsla.webusersposts.helper.XmlContentHelper;

public class PostsService {
	
	public XmlUserPost getXmlOnlinePost(String url, CmsObject cms) throws Exception {
		CmsProject currentProject = cms.getRequestContext().currentProject();
		try {
			UserPost post = new UserPost();
			post.setUrl(url);
			XmlContentHelper contentHelper = new XmlContentHelper(cms);
			
			cms.getRequestContext().setCurrentProject(cms.readProject("Online"));
			CmsFile file = cms.readFile(post.getUrl());
			CmsXmlContent xmlContent = contentHelper.getXmlContent(file);
			XmlUserPost userPost = new XmlUserPost(xmlContent, cms, post);
			
			return userPost;
		} catch(Exception e) {
			throw e;
		} finally {
			cms.getRequestContext().setCurrentProject(currentProject);
		}
	}
	
	public XmlUserPost getXmlPost(String id, CmsObject cms) throws Exception {
		UserPostsService service = new UserPostsService();
		UserPost post = service.getPostById(id);
		
		return this.getXmlPost(post, cms);
	}
	
	public XmlUserPost getXmlPost(UserPost post, CmsObject cms) throws Exception {
		XmlContentHelper contentHelper = new XmlContentHelper(cms);
		CmsXmlContent xmlContent = contentHelper.getXmlContent(post.getXmlContent());
		try {
			xmlContent.validateXmlStructure(new CmsXmlEntityResolver(cms));
		} catch (CmsXmlException eXml) {
			xmlContent.setAutoCorrectionEnabled(true);
			xmlContent.correctXmlStructure(cms);
        }
		XmlUserPost userPost = new XmlUserPost(xmlContent, cms, post);

		return userPost;
	}
	
	
	public List<XmlUserPost> getXmlUserPosts(CmsObject cms, String userId, String site, String publicationId, int countPosts, int fromPost, String status) throws Exception {
		List<XmlUserPost> ret = new ArrayList<XmlUserPost>();
		UserPostsService service = new UserPostsService();
		
		List<UserPost> posts = service.getUserPosts(userId, site, publicationId, countPosts, fromPost, status);
		
		for(UserPost post : posts) {
			ret.add(this.getXmlPost(post, cms));
		}
		
		return ret;
	}
	
	public CmsFile createVfsResource(String resourceName, CmsObject cms) throws Exception {
		int typeResource = OpenCms.getResourceManager().getResourceType("noticia").getTypeId();
		cms.createResource(resourceName, typeResource);
		cms.writePropertyObject(resourceName, new CmsProperty("newsType", "post", "post", true));
		return cms.readFile(resourceName);
	}
}
