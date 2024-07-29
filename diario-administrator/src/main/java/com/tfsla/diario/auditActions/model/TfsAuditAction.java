package com.tfsla.diario.auditActions.model;

import java.util.*;

public class TfsAuditAction {
	
	public static final int ACTION_USER_REPORT=1;
	public static final int ACTION_NEWS_PUBLISHED=2;
	public static final int ACTION_NEWS_STATUS_CHANGED=3;
	public static final int ACTION_NEWS_VALUE_CHANGED=4;
	public static final int ACTION_POLL_PUBLISHED=5;
	public static final int ACTION_POLL_STATUS_CHANGED=6;
	public static final int ACTION_USER_CREATED=7;
	public static final int ACTION_USER_LOGIN=8;
	public static final int ACTION_NEWS_DELETED = 9;
	public static final int ACTION_POLL_DELETED = 10;
	public static final int ACTION_NEWS_CREATED = 11;
	public static final int ACTION_POLL_CREATED = 12;
	public static final int ACTION_USER_STATUS=13;
	public static final int ACTION_COMMENT_REPORTED = 14;
	public static final int ACTION_COMMENT_REVISION = 15;
	public static final int ACTION_POST_PUBLISHED = 16;
	public static final int ACTION_POST_CREATED = 17;
	public static final int ACTION_POST_REPORTED = 18;
	public static final int ACTION_POST_REVISION = 19;
	public static final int ACTION_POST_ACEPTED = 20;
	public static final int ACTION_POST_REJECTED = 21;
	public static final int ACTION_VIDEO_ENCODER = 22;

	public static Map<String,Integer> actions=null;
	public static List<String> actionsNames = new ArrayList<String>() {
		{
			add("USER_REPORT");
			add("USER_REPORT");
			add("NEWS_PUBLISHED");
			add("NEWS_STATUS_CHANGED");
			add("NEWS_VALUE_CHANGED");
			add("POLL_PUBLISHED");
			add("POLL_STATUS_CHANGED");
			add("USER_CREATED");
			add("USER_LOGIN");
			add("NEWS_DELETED");
			add("POLL_DELETED");
			add("NEWS_CREATED");
			add("POLL_CREATED");
			add("USER_STATUS");
			add("COMMENT_REPORTED");
			add("COMMENT_REVISION");
			add("POST_PUBLISHED");
			add("POST_CREATED");
			add("POST_REPORTED");
			add("POST_REVISION");
			add("POST_ACEPTED");
			add("POST_REJECTED");
			add("VIDEO_ENCODER");
			
		}
		
	};


	public static int getActionId(String action) {
		if (actions==null) {
			actions = new HashMap<String,Integer>();
			actions.put("USER_REPORT", ACTION_USER_REPORT);
			actions.put("USER_REPORT", ACTION_USER_REPORT);
			actions.put("NEWS_PUBLISHED", ACTION_NEWS_PUBLISHED);
			actions.put("NEWS_STATUS_CHANGED", ACTION_NEWS_STATUS_CHANGED);
			actions.put("NEWS_VALUE_CHANGED", ACTION_NEWS_VALUE_CHANGED);
			actions.put("POLL_PUBLISHED", ACTION_POLL_PUBLISHED);
			actions.put("POLL_STATUS_CHANGED", ACTION_POLL_STATUS_CHANGED);
			actions.put("USER_CREATED", ACTION_USER_CREATED);
			actions.put("USER_LOGIN", ACTION_USER_LOGIN);
			actions.put("NEWS_DELETED", ACTION_NEWS_DELETED);
			actions.put("POLL_DELETED", ACTION_POLL_DELETED);
			actions.put("NEWS_CREATED", ACTION_NEWS_CREATED);
			actions.put("POLL_CREATED", ACTION_POLL_CREATED);
			actions.put("USER_STATUS", ACTION_USER_STATUS);
			actions.put("COMMENT_REPORTED", ACTION_COMMENT_REPORTED);
			actions.put("COMMENT_REVISION", ACTION_COMMENT_REVISION);
			actions.put("POST_PUBLISHED", ACTION_POST_PUBLISHED);
			actions.put("POST_CREATED", ACTION_POST_CREATED);
			actions.put("POST_REPORTED", ACTION_POST_REPORTED);
			actions.put("POST_REVISION", ACTION_POST_REVISION);
			actions.put("POST_ACEPTED", ACTION_POST_ACEPTED);
			actions.put("POST_REJECTED", ACTION_POST_REJECTED);
			actions.put("VIDEO_ENCODER", ACTION_VIDEO_ENCODER);
		}
		return actions.get(action);
	}
	
	public static List<String> getActionNames() {
		return actionsNames;
	}
	
	private Date timeStamp;
	private Date lastModified;
	private String userName;
	private int actionId;
	private String targetId;
	private String description;
	private String sitio;
	private String publicacion;
	private long eventId;
	private int comments;
	private int attachments;
	
	public Date getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public int getActionId() {
		return actionId;
	}
	public void setActionId(int actionId) {
		this.actionId = actionId;
	}
	public String getTargetId() {
		return targetId;
	}
	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getSitio() {
		return sitio;
	}
	public void setSitio(String sitio) {
		this.sitio = sitio;
	}
	public String getPublicacion() {
		return publicacion;
	}
	public void setPublicacion(String publicacion) {
		this.publicacion = publicacion;
	}
	public long getEventId() {
		return eventId;
	}
	public void setEventId(long eventId) {
		this.eventId = eventId;
	}
	public int getComments() {
		return comments;
	}
	public void setComments(int comments) {
		this.comments = comments;
	}
	public int getAttachments() {
		return attachments;
	}
	public void setAttachments(int attachments) {
		this.attachments = attachments;
	}
	public Date getLastModified() {
		return lastModified;
	}
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	
	
}
