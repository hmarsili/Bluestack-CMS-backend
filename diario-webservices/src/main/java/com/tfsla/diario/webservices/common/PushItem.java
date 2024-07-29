package com.tfsla.diario.webservices.common;

import java.util.Date;

import org.opencms.util.CmsUUID;

public class PushItem {
	
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getStructureIdAsString() {
		return this.structureId;
	}
	public CmsUUID getStructureId() {
		if (this.structureId==null)
			return null;
		return new CmsUUID(this.structureId);
	}
	public void setStructureId(String structureId) {
		this.structureId = structureId;
	}
	public PushStatus getStatus() {
		return status;
	}
	public void setStatus(PushStatus status) {
		this.status = status;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
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
	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	public Date getDatePushed() {
		return datePushed;
	}
	public void setDatePushed(Date datePushed) {
		this.datePushed = datePushed;
	}
	public String getPushType() {
		return pushType;
	}
	public void setPushType(String pushType) {
		this.pushType = pushType;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public Date getDateScheduled() {
		return dateScheduled;
	}
	public void setDateScheduled(Date dateScheduled) {
		this.dateScheduled = dateScheduled;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSubTitle() {
		return subTitle;
	}
	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}
	private int id;
	private int priority;
	private int publication;
	private String topic;
	private String title;
	private String subTitle;
	private String url;
	private String site;
	private String structureId;
	private String pushType;
	private String info;
	private String userName;
	private String jobName;
	private String image;
	private PushStatus status;
	private Date dateCreated;
	private Date datePushed;
	private Date dateScheduled;
}
