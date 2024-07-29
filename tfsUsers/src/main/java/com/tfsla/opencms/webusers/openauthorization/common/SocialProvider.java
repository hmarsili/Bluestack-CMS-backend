package com.tfsla.opencms.webusers.openauthorization.common;

public class SocialProvider {
	private String name;
	private String description;
	private String logo;
	
	public String getShortname() {
		if(name!=null && name.contains("webusers")) {
			return name.split("-")[1].toUpperCase();
		}
		return name.toUpperCase();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}
}
