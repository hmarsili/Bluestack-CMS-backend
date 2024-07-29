package com.tfsla.diario.history;

import org.opencms.file.CmsUser;

public class tfsResourceVersion {
	
	private CmsUser CmsUserLastModified;
	private String UsernameLastModified;
	private String     UserLastModified;
	private String        DatePublished;
	private String     DateLastModified;
	private int                 version;
	private int					   size;
	private String           StrVersion;
	private String 	        urlVersion;
	private String           proyect;
    
    
    public void setUrlVersion(String urlVersion){
		this.urlVersion = urlVersion;
	}
	
	public String getUrlVersion(){
		return this.urlVersion;
	}
	
	public void setDatePublished(String datePublished){
		this.DatePublished = datePublished;
	}
	
	public String getDatePublished(){
		return this.DatePublished;
	}
	
	public void setDateLastModified(String dateLastModified){
		this.DateLastModified = dateLastModified;
	}
	
	public String getDateLastModified(){
		return this.DateLastModified;
	}
	
	public void setUsernameLastModified(String username){
		this.UsernameLastModified = username;
	}
	
	public String getUsernameLastModified(){
		return this.UsernameLastModified;
	}
	
	public void setUserLastModified(String username){
		this.UserLastModified = username;
	}
	
	public String getUserLastModified(){
		return this.UserLastModified;
	}
	
	public void setSize(int size){
		this.size = size;
	}
	
	public int getSize(){
		return this.size;
	}
	
	public void setCmsUserLastModified(CmsUser userLastModified ){
		this.CmsUserLastModified = userLastModified;
	}
	
	public CmsUser getCmsUserLastModified(){
		return this.CmsUserLastModified;
	}
	
	public void setVersion(int version){
		this.version = version;
	}
	
	public int getVersion(){
		return this.version;
	}
	
	public void setStrVersion(String strVersion){
		this.StrVersion = strVersion;
	}
	
	public String getStrVersion(){
		return StrVersion;
	}
	
	public String getProyect() {
		return proyect;
	}

	public void setProyect(String proyect) {
		this.proyect = proyect;
	}
}
