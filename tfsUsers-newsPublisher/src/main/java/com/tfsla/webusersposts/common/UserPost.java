package com.tfsla.webusersposts.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.util.CmsUUID;

import com.tfsla.webusersnewspublisher.model.ModerationResult;

public class UserPost {
	public UserPost() {
		this.status = PostStatus.DRAFT;
		this.userRegistered = true;
		this.userPending = false;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public PostStatus getStatus() {
		return status;
	}
	public void setStatus(PostStatus status) {
		this.status = status;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getXmlContent() {
		return xmlContent;
	}
	public void setXmlContent(String xmlContent) {
		this.xmlContent = xmlContent;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public int getPublication() {
		return publication;
	}
	public void setPublication(int publication) {
		this.publication = publication;
	}
	public String getSite() {
		return site;
	}
	public void setSite(String site) {
		this.site = site;
	}
	public String getSocialNetworks() {
		return socialNetworks;
	}
	public void setSocialNetworks(String socialNetworks) {
		this.socialNetworks = socialNetworks;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	public String getModerationMessage() {
		return moderationMessage;
	}
	public void setModerationMessage(String moderationMessage) {
		this.moderationMessage = moderationMessage;
	}
	public List<String> getSocialNetworksList() {
		List<String> ret = new ArrayList<String>();
		if(this.getSocialNetworks() != null && !this.getSocialNetworks().equals("")) {
			for(String item : this.getSocialNetworks().split(",")) {
				ret.add(item);
			}
		}
		return ret;
	}
	public CmsUser getCmsUser(CmsObject cms) throws Exception {
		if(this.cmsUser == null) {
			this.cmsUser = cms.readUser(new CmsUUID(this.getUserId()));
		}
		return this.cmsUser;
	}
	public CmsResource getCmsResource() {
		return cmsResource;
	}
	public void setCmsResource(CmsResource cmsResource) {
		this.cmsResource = cmsResource;
	}
	public List<ModerationResult> getModerationResults() {
		return moderationResults;
	}
	public void setModerationResults(List<ModerationResult> moderationResults) {
		this.moderationResults = moderationResults;
	}
	public Boolean getUserRegistered() {
		return userRegistered;
	}
	public void setUserRegistered(Boolean userRegistered) {
		this.userRegistered = userRegistered;
	}
	public void setUserPending(boolean userPending) {
		this.userPending = userPending;
	}
	public Boolean getUserPending() {
		return userPending;
	}
	private Boolean userRegistered;
	private Boolean userPending;
	private String url;
	private String id;
	private String userId;
	private String title;
	private String xmlContent;
	private String socialNetworks;
	private String site;
	private String moderationMessage;
	private Date creationDate;
	private Date updateDate;
	private PostStatus status;
	private CmsUser cmsUser;
	private CmsResource cmsResource;
	private int publication;
	private List<ModerationResult> moderationResults;
}
