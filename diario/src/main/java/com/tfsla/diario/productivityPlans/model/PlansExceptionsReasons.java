package com.tfsla.diario.productivityPlans.model;


public class PlansExceptionsReasons {
	
	
	private String siteName; //0 si es para todas.
	private int publication; //0 si es para todas.
	private int id;
	private String description; 
	private boolean enabled;
	private String userCreation;
	private boolean specificReasonsPub;

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
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
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
	public boolean isSpecificReasonsPub() {
		return specificReasonsPub;
	}
	public void setSpecificReasonsPub(boolean specificReasonsPub) {
		this.specificReasonsPub = specificReasonsPub;
	}
		
	
}