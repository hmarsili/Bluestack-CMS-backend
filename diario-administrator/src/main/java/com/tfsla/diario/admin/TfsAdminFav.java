package com.tfsla.diario.admin;

import java.sql.Timestamp;

import org.opencms.file.CmsUser;

public class TfsAdminFav {

	private String id;
	private Timestamp lastmodified;
	private String user_id;
	private CmsUser user;
	private String site;
	private String publication;
	private String path;
	private String icon;
	private String description;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public Timestamp getLastmodified(){
		return lastmodified;
	}
	
	public void setLastmodified(Timestamp lastmodified){
		this.lastmodified = lastmodified;
	}
	
	public String getUserId(){
		return user_id;
	}
	
	public void setUserId(String user_id){
		this.user_id = user_id;
	}
	
	public CmsUser getUser(){
		return user;
	}
	
	public void setUser(CmsUser user){
		this.user = user;
	}
	
	public String getSite(){
		return site;
	}
	
	public void setSite(String site){
		this.site = site;
	}
	
	public String getPublication(){
		return publication;
	}
	
	public void setPublication(String publication){
		this.publication = publication;
	}
	
	public String getPath(){
		return path;
	}
	
	public void setPath(String path){
		this.path = path;
	}
	
	public String getIcon(){
		return icon;
	}
	
	public void setIcon(String icon){
		this.icon = icon;
	}
	
	public String getDescription(){
		return description;
	}
	
	public void setDescription(String description){
		this.description = description;
	}

}
