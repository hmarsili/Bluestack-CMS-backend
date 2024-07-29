package com.tfsla.webusersposts.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;

import com.tfsla.opencms.webusers.RegistrationModule;
import com.tfsla.opencms.webusers.webusersposts.IUserPostsService;
import com.tfsla.webusersposts.common.INickNameHelper;
import com.tfsla.webusersposts.common.PostStatus;
import com.tfsla.webusersposts.common.UserPost;
import com.tfsla.webusersposts.dataaccess.PostsDAO;
import com.tfsla.webusersposts.helper.AdminCmsObjectHelper;
import com.tfsla.webusersposts.strings.ExceptionMessages;
import com.tfsla.webusersposts.strings.SqlQueries;

public class UserPostsService implements IUserPostsService {
	
	public UserPost savePost(String title, String xmlContent, String userId, int publication, String site) {
		return this.savePost(title, xmlContent, userId, publication, site, "");
	}
	
	public UserPost savePost(String title, String xmlContent, String userId, int publication, String site, String socialNetworks) {
		UserPost userPost = new UserPost();
		PostsDAO dao = new PostsDAO();
		try {
			//this.setPostStatus(userId, userPost);
			userPost.setCreationDate(new Date());
			userPost.setUpdateDate(new Date());
			userPost.setPublication(publication);
			userPost.setSite(site);
			userPost.setTitle(title);
			userPost.setUserId(userId);
			userPost.setXmlContent(xmlContent);
			userPost.setSocialNetworks(socialNetworks);
			userPost.setId(new CmsUUID().toString());
			dao.openConnection();
			userPost = dao.addPost(userPost);
		} catch(Exception ex) {
			LOG.error("Error while adding post", ex);
		} finally {
			dao.closeConnection();
		}
		
		return userPost;
	}
	
	public UserPost savePost(String title, String xmlContent, String userId, int publication, String site, String socialNetworks, String postId) {
		UserPost userPost = new UserPost();
		PostsDAO dao = new PostsDAO();
		try {
			userPost.setUpdateDate(new Date());
			userPost.setPublication(publication);
			userPost.setSite(site);
			userPost.setTitle(title);
			userPost.setUserId(userId);
			userPost.setXmlContent(xmlContent);
			userPost.setSocialNetworks(socialNetworks);
			userPost.setId(postId);
			dao.openConnection();
			userPost = dao.updatePost(userPost);
		} catch(Exception ex) {
			LOG.error("Error while saving post", ex);
		} finally {
			dao.closeConnection();
		}
		
		return userPost;
	}
	
	public UserPost createAnonymousDraft(String title, String xmlContent, String email, int publication, String site, String url) throws Exception {
		
		if(!CONFIG.getParam(site, String.valueOf(publication), "newsPublisher", "allowAnonymousPosts").equals(Boolean.toString(true)))
	 		throw new Exception(String.format(ExceptionMessages.ERROR_ANONYMOUS_POSTS_NOT_ENABLED, site, publication));
		
		CmsObject cmsObject = AdminCmsObjectHelper.getAdminCmsObject();
		cmsObject.getRequestContext().setSiteRoot(site);
		RegistrationModule regModule = RegistrationModule.getInstance(cmsObject);
		Boolean userRegistered = true;
		CmsUser user = null;
		String username = regModule.UserNameByMail(cmsObject, email);
		if(username != null && !username.equals("")) {
			user = cmsObject.readUser("webUser/" + username);
		} else {
			String password = RegistrationModule.getRandomPassword();
			String name = email.split("@")[0];
			name = name.replaceAll(NICKNAME_REGEX, "");
			userRegistered = false;
			
			user = regModule.addWebUser(cmsObject,
					email,
					password,
					password,
					name,
					name,
					email,
					email,
					"", "", "", "", "", "", "", "", "", "",
					new ArrayList<String>(),
					new ArrayList<String>(),
					new ArrayList<String>()
			);

			try {
				@SuppressWarnings("unchecked")
				Class<INickNameHelper> c = (Class<INickNameHelper>)Class.forName("com.tfsla.diario.webservices.helpers.NicknameHelper");
				INickNameHelper helper = c.newInstance(); 
				user.setAdditionalInfo("APODO", helper.getUniqeNickname(name, name));
				cmsObject.writeUser(user);
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			if(!regModule.getSendMailConfirmation()){
				regModule.forgotPassword(cmsObject, email, email);
			}
		}
		
		UserPost ret = this.createDraft(title, xmlContent, user.getId().toString(), publication, site, url);
		ret.setUserRegistered(userRegistered);
		return ret;
	}
	
	public Boolean processUserActivePosts(CmsUser user) {
		PostsDAO dao = new PostsDAO();
		try {
			dao.openConnection();
			int updated = dao.updateUserPendingPosts(user.getId().toString());
			return updated > 0;
		} catch(Exception e) {
			LOG.error("Error updating user posts, user: " + user.getName(), e);
			e.printStackTrace();
			return false;
		} finally {
			dao.closeConnection();
		}
	}
	
	public UserPost createDraft(String title, String xmlContent, String userId, int publication, String site, String url) throws Exception {
		UserPost userPost = new UserPost();
		this.setPostStatus(userId, userPost);
		
		PostsDAO dao = new PostsDAO();
		try {
			userPost.setCreationDate(new Date());
			userPost.setUpdateDate(new Date());
			userPost.setPublication(publication);
			userPost.setSite(site);
			userPost.setTitle(title);
			userPost.setUserId(userId);
			userPost.setXmlContent(xmlContent);
			userPost.setUrl(url);
			userPost.setId(new CmsUUID().toString());
			dao.openConnection();
			userPost = dao.addPost(userPost);
		} catch(Exception ex) {
			ex.printStackTrace();
			LOG.error("Error while adding post", ex);
		} finally {
			dao.closeConnection();
		}
		
		return userPost;
	}
	
	public void deleteDraft(String draftId) {
		PostsDAO dao = new PostsDAO();
		try {
			dao.openConnection();
			dao.deleteDraft(draftId);
		} catch(Exception ex) {
			LOG.error("Error while deleting draft", ex);
		} finally {
			dao.closeConnection();
		}
	}
	
	public UserPost changeStatus(UserPost userPost, PostStatus status) {
		PostsDAO dao = new PostsDAO();
		try {
			userPost.setStatus(status);
			userPost.setUpdateDate(new Date());
			dao.openConnection();
			userPost = dao.updateStatus(userPost, status);
		} catch(Exception ex) {
			LOG.error("Error while changing post status", ex);
		} finally {
			dao.closeConnection();
		}
		return userPost;
	}
	
	public UserPost getPostById(String id) {
		UserPost post = null;
		PostsDAO dao = new PostsDAO();
		try {
			dao.openConnection();
			post = dao.getPostById(id);
		} catch(Exception ex) {
			LOG.error("Error retrieving post by ID", ex);
		} finally {
			dao.closeConnection();
		}
		return post;
	}
	
	public List<UserPost> getUserPosts(String userId) {
		return this.getUserPosts(userId, null, null, 0, 0, "");
	}
	
	public List<UserPost> getUserPosts(String userId, String site, String publicationId, int countPosts, int fromPost, String statusFilter) {
		List<UserPost> posts = null;
		PostsDAO dao = new PostsDAO();
		int publication = 0;
		try {
			publication = Integer.parseInt(publicationId);
		} catch(Exception e) {
			publication = 0;
		}
		if(site != null) {
			if(site.endsWith("/")) site = site.substring(0, site.length()-1);
			site = "%" + site;
		}
		List<Integer> statuses = this.getStatuses(statusFilter);
		String filter = String.format(" %s STATUS %s (%s) ",
			SqlQueries.AND_FILTER,
			SqlQueries.IN_OP,
			StringUtils.join(statuses.iterator(), ", ")
		);
		try {
			dao.openConnection();
			posts = dao.getUserPosts(userId, site, publication, countPosts, fromPost, filter);
		} catch(Exception ex) {
			LOG.error("Error retrieving user posts", ex);
		} finally {
			dao.closeConnection();
		}
		return posts;
	}
	
	private List<Integer> getStatuses(String status) {
		List<Integer> ret = new ArrayList<Integer>();
		if(status != null && !status.trim().equals("")) {
			for(String s : status.split(",")) {
				try {
					ret.add(PostStatus.valueOf(s.toUpperCase()).getValue());
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			ret.add(PostStatus.DRAFT.getValue());
			ret.add(PostStatus.PENDING.getValue());
		}
		
		return ret;
	}
	
	private void setPostStatus(String userId, UserPost userPost) throws Exception {
		CmsObject cmsObject = AdminCmsObjectHelper.getAdminCmsObject();
		CmsUser user = cmsObject.readUser(new CmsUUID(userId));
		if(!user.isEnabled()) {
			//throw new Exception(String.format(ExceptionMessages.ERROR_USER_DISABLED, user.getFullName()));
			LOG.debug(String.format(ExceptionMessages.ERROR_USER_DISABLED, user.getFullName()));
			userPost.setUserPending(true);
		}
		
		Object userPending = user.getAdditionalInfo(RegistrationModule.USER_PENDING);
		if(userPending == null || userPending.toString().toLowerCase().trim().equals(Boolean.toString(true))) {
			userPost.setStatus(PostStatus.PENDING_USER);
			userPost.setUserPending(true);
		}
	}
	
	private Log LOG = CmsLog.getLog(this);
	private static CPMConfig CONFIG = CmsMedios.getInstance().getCmsParaMediosConfiguration();
	private static String NICKNAME_REGEX = "['\"!#$%&()*+./:;<=>?@^`~-]";
}
