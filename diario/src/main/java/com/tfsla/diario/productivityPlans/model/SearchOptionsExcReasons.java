package com.tfsla.diario.productivityPlans.model;


public class SearchOptionsExcReasons {
	
	
	private String siteName;
	private int publication;
	private int id;
	private boolean enabled;
	private String userCreation;
	private String orderBy;
	private int count;
	private boolean isSpecificReasonsPub;
	
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

	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getUserCreation() {
		return userCreation;
	}
	public void setUserCreation(String userCreation) {
		this.userCreation = userCreation;
	}
	public String getOrderBy() {
		return orderBy == null || orderBy.equals("") ? " DESCRIPTION asc " : orderBy;
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
	public boolean isSpecificReasonsPub() {
		return isSpecificReasonsPub;
	}
	public void setSpecificReasonsPub(boolean isSpecificReasonsPub) {
		this.isSpecificReasonsPub = isSpecificReasonsPub;
	}

	
}