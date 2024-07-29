package com.tfsla.webusersposts.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.event.I_TfsEventListener;
import com.tfsla.webusersnewspublisher.model.ModerationReason;
import com.tfsla.webusersnewspublisher.model.ModerationResult;
import com.tfsla.webusersnewspublisher.model.News;
import com.tfsla.webusersnewspublisher.service.NewsPublisherModerationManager;
import com.tfsla.webusersposts.common.*;
import com.tfsla.webusersposts.dataaccess.PostsDAO;
import com.tfsla.webusersposts.helper.LogMessages;

/**
 * A job to process the users posts pending to be published and moderate and/or publish them 
 */
public class UserPostsPublisherJob implements I_CmsScheduledJob {

	@SuppressWarnings("rawtypes")
	@Override
	public String launch(CmsObject cms, Map parameters) throws Exception {
		PostsDAO dao = new PostsDAO();
		PostsService postsService = new PostsService();
		
		//1- Process job parameters and read configuration
		String publication = (String)parameters.get("publication");
		String site = (String)parameters.get("site");
		int batchSize = 100;
		if (parameters.containsKey("batchSize")) {
			batchSize = (Integer)parameters.get("batchSize");
		}
		cms.getRequestContext().setSiteRoot(site);
		
		LOG.debug(String.format(LogMessages.JOB_STARTED, site, publication));
		News news = new News();
		
		try {
			//2- Get posts to be processed for the current site
			dao.openConnection();
			ArrayList<UserPost> posts = dao.getPendingPostsBySite(publication, site, batchSize);
			NewsPublisherModerationManager moderationManager = NewsPublisherModerationManager.getInstance(cms);
			news.setMode(false);
			news.setPublication(publication);
			news.setCmsObject(cms);
			String folder = news.getFolderName();
			Locale locale = cms.getRequestContext().getLocale();
			List<CmsResource> publishList = new ArrayList<CmsResource>();
			List<UserPost> approved = new ArrayList<UserPost>();
			List<UserPost> toModerate = new ArrayList<UserPost>();
			LOG.debug(String.format(LogMessages.JOB_INFO, posts.size(), folder));
			
			if(posts.size() == 0) {
				return String.format(
					LogMessages.JOB_EXECUTED,
					new Date()
				);
			}

			String siteRoot = cms.getRequestContext().getSiteRoot();
			cms.getRequestContext().setSiteRoot("/");
			
			//3- Loop for each post to process it
			for(UserPost post : posts) {
				
				try {
					//4- Retrieve post XML to be processed
					XmlUserPost userPost = postsService.getXmlPost(post, cms);
					CmsXmlContent xmlContent = userPost.getXml();
					
					//5- Get the post VFS content (create it if does not exists)
					CmsUser cmsUser = post.getCmsUser(cms);
					String path = "";
					CmsFile file = null;
					
					//Check if is a new post or a new edition
					if(post.getUrl() == null || post.getUrl().trim().equals("")) {
						//Set the user as author (for anonymous posts)
						I_CmsXmlContentValue value = xmlContent.getValue("autor/internalUser", locale);
						if(value != null) {
							value.setStringValue(cms, cmsUser.getName());
						} else {
							xmlContent.addValue(cms, "autor/internalUser", locale, 0).setStringValue(cms, cmsUser.getName());
						}
						
						path = news.getNewsName(folder);
						file = postsService.createVfsResource(path, cms);
						post.setUrl(path);
					} else {
						path = post.getUrl();
						file = cms.readFile(post.getUrl());
						this.unlockFile(cms, file, path);
					}
					setUrlFriendly(xmlContent, cms, news, locale, userPost);
					file.setContents(xmlContent.marshal());
					cms.writeFile(file);
					this.cleanReportHistory(cms, file, site, publication);
					post.setCmsResource(file);
					publishList.add(file);
					
					//6- Moderate the post (if pre moderation is enabled)
					if(moderationManager.isEnablePreModeration()) {
						List<ModerationResult> moderationResults = moderationManager.premoderation(cms, cms.readFile(path), cmsUser, userPost.getTitle() + " " + userPost.getBody() + " " + userPost.getKeywords(), false);
						post.setModerationResults(moderationResults);
						if(moderationResults.size() == 0) {
							approved.add(post);
							LOG.debug(String.format(LogMessages.POST_TO_PUBLISH, post.getId(), path));
						} else {
							//Post rejected, track the reason which it must be moderated for
							String moderationMessage = "";
							for(ModerationResult result : moderationResults) {
								moderationMessage += result.getDescription() + "\n";
							}
							post.setModerationMessage(moderationMessage);
							toModerate.add(post);
							LOG.debug(String.format(LogMessages.POST_REJECTED, post.getId()));
							moderationManager.setRevisionPost(cms, post.getCmsResource(), false);
						}
					} else {
						//If pre moderation is not enabled, publish all posts
						approved.add(post);
						LOG.debug(String.format(LogMessages.POST_TO_PUBLISH, post.getId(), path));
					}
					
				} catch(Exception e) {
					//If there is an error processing the post, register it into the DB and continue
					String errorMessage = String.format(LogMessages.POST_ERROR, post.getId()) + e.getMessage();
					LOG.error(errorMessage, e);
					String message = e.getMessage();
					int max = message.length() > 999 ? 999 : message.length();
					post.setModerationMessage(message.substring(0, max));
					toModerate.add(post);
					post.setStatus(PostStatus.ERROR);
					dao.updateStatus(post, PostStatus.ERROR);
					moderationManager.setRevisionPost(cms, post.getCmsResource(), false);
				}
				
			}
			
			//7- Publish ALL the posts
			if(publishList.size() > 0) {
				for(CmsResource item : publishList) {
					OpenCms.getPublishManager().publishResource(cms, item.getRootPath());
				}
				/*
				CmsPublishList pList = OpenCms.getPublishManager().getPublishList(cms, publishList, false);
				CmsLogReport report = new CmsLogReport(locale, this.getClass());
				OpenCms.getPublishManager().publishProject(cms, report, pList);
				OpenCms.getPublishManager().waitWhileRunning();
				*/
			}
			
			cms.getRequestContext().setSiteRoot(siteRoot);
			
			//8- Update posts status and url into the database (approved posts)
			for(UserPost post : approved) {
				dao.updateStatus(post, PostStatus.PUBLISHED);
				CmsResource publishedItem = publishList.get(approved.indexOf(post));
				PostsMailingService.sendNotification(post, publishedItem, cms, site, publication, PostsNotificationType.APPROVED);
				
		        this.sendNotifications(post, cms, I_TfsEventListener.EVENT_POST_ACEPTED);
			}
			
			//9- Update posts status into the database (posts to be moderated)
			for(UserPost post : toModerate) {
				PostsNotificationType notificationType = PostsNotificationType.REJECTED;
				
				if(post.getStatus() != PostStatus.ERROR) {
					//Check the moderation reason. If it was rejected because the post
					//must be moderated (category or section), then the post is IMPORTED
					//(PENDING to be moderated)
					for(ModerationResult result : post.getModerationResults()) {
						if(result.getModerationReason() == ModerationReason.CATEGORY_MODERATED
								|| result.getModerationReason() == ModerationReason.SECTION_MODERATED) {
							notificationType = PostsNotificationType.PENDING;
						}
					}
				}
				
				if(notificationType == PostsNotificationType.REJECTED) {
					dao.updateStatus(post, PostStatus.MODERATED);
				} else {
					post.setModerationMessage("");
					dao.updateStatus(post, PostStatus.IMPORTED);
				}
				
				PostsMailingService.sendNotification(post, cms, site, publication, notificationType);
			}
			
			LOG.debug(String.format(LogMessages.JOB_SUMMARY, approved.size(), toModerate.size()));
			
			return String.format(
				LogMessages.JOB_FINISHED,
				new Date(),
				posts.size(),
				approved.size(),
				toModerate.size()
			);
		} catch(Exception e) {
			LOG.error(LogMessages.JOB_ERROR + e.getMessage(), e);
			return String.format(LogMessages.JOB_EXECUTION_ERROR, new Date(), e.getMessage());
		} finally {
			dao.closeConnection();
		}
	}
	
	private void cleanReportHistory(CmsObject cms, CmsFile file, String site, String publication) throws CmsException {
		cms.writePropertyObject(cms.getSitePath(file), new CmsProperty("abuseReportCount", "0", "0", true));
		PostsDAO dao = new PostsDAO();
		try {
			String path = file.getRootPath().replace(site, "");
			if(!site.endsWith("/")) site += "/";
			if(!path.startsWith("/")) path = "/" + path;
			dao.openConnection();
			dao.resetPostAbuseReportCount(path, site, publication);
			LOG.info(String.format(LogMessages.JOB_CLEAN_HISTORY, path, site, publication));
		} catch(Exception e) {
			LOG.error(LogMessages.JOB_ERROR_CLEAN_HISTORY, e);
			e.printStackTrace();
		} finally {
			try {
				dao.closeConnection();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void setUrlFriendly(CmsXmlContent xmlContent, CmsObject cms, News news, Locale locale, XmlUserPost userPost) {
		String urlFriendly = xmlContent.getStringValue(cms, "urlFriendly", locale);
		if(urlFriendly == null || urlFriendly.trim().equals("")) {
			xmlContent.getValue("urlFriendly", locale).setStringValue(
				cms, 
				news.removeInvalidXmlCharacters(
					userPost.getTitle()
				)
			);
		}
	}

	private void unlockFile(CmsObject cms, CmsResource file, String path) throws Exception {
		CmsLock lock = cms.getLock(file);
		if(!lock.isUnlocked()) {
			cms.changeLock(path);
			cms.unlockResource(path);
		}
		cms.lockResource(path);
	}
	
	private void sendNotifications(UserPost post, CmsObject cms, int eventListener) throws Exception {
		HashMap<String, Object> eventData = new HashMap<String, Object>();
		eventData.put(I_TfsEventListener.KEY_RESOURCE, post.getCmsResource());
		eventData.put(I_TfsEventListener.KEY_USER_NAME, post.getCmsUser(cms).getName());
		OpenCms.fireCmsEvent(new CmsEvent(eventListener, eventData));
	}
	
	private Log LOG = CmsLog.getLog(this);
}
