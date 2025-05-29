package com.tfsla.diario.productivityPlans.model;


public class SearchOptionsExceptions {
	
	
	private String siteName;
	private int publication;
	private int id;
	private String planId;
	private boolean enabled;
	private String user;
	private Long from;
	private Long to;
	private String orderBy;
	private int count;
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
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public Long getFrom() {
		return from;
	}
	public void setFrom(Long from) {
		this.from = from;
	}
	public Long getTo() {
		return to;
	}
	public void setTo(Long to) {
		this.to = to;
	}
	public String getOrderBy() {
		return orderBy == null || orderBy.equals("") ? " ID asc " : orderBy;
	}
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
		
}