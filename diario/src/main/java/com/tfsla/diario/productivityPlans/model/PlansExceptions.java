package com.tfsla.diario.productivityPlans.model;


public class PlansExceptions {
	
	
	private String siteName;
	private int publication;
	private int id;
	private String planId;
	private boolean enabled;
	private int reasonId; //'general'|'rol'|'group'
	private String usercreation;
	private String user;
	private long from;
	private long to;
	private String comments;
	private int news;
	
	public String getSiteName() {
		return siteName;
	}
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	public int getPublication() {
		return publication;
	}
	public void setPublication(int publication) {
		this.publication = publication;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPlanId() {
		return planId;
	}
	public void setPlanId(String planId) {
		this.planId = planId;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public int getReasonId() {
		return reasonId;
	}
	public void setReasonId(int reasonId) {
		this.reasonId = reasonId;
	}
	public String getUsercreation() {
		return usercreation;
	}
	public void setUsercreation(String usercreation) {
		this.usercreation = usercreation;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public long getFrom() {
		return from;
	}
	public void setFrom(long from) {
		this.from = from;
	}
	public long getTo() {
		return to;
	}
	public void setTo(long to) {
		this.to = to;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public int getNews() {
		return news;
	}
	public void setNews(int news) {
		this.news = news;
	}
	
		
}