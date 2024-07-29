package com.tfsla.diario.terminos.model;
public class Terms {
	public Terms() {
		this.isFullTag = true;
	}
	
	protected long id_term;
	protected String name;
	protected String name_search;
	protected String lastmodified;
	protected String description;
	protected String image;
	protected String url;
	protected long type;
	protected int approved;
	protected String template;
	protected String synonymous="";
	protected Boolean isFullTag;
	protected long PrevType;
	
	public long getPrevType() {
		return PrevType;
	}
	public void setPrevType(long prevType) {
		PrevType = prevType;
	}
	public String getTemplate() {
		return template;
	}
	public void setTemplate(String template) {
		this.template = template;
	}

	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
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
	public long getId_term() {
		return id_term;
	}
	public void setId_term(long id_term) {
		this.id_term = id_term;
	}
	public String getLastmodified() {
		return lastmodified;
	}
	public void setLastmodified(String lastmodified) {
		this.lastmodified = lastmodified;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public long getType() {
		return type;
	}
	public void setType(long type) {
		this.type = type;
	}
	public int getApproved() {
		return approved;
	}
	public void setAproved(int approved) {
		this.approved = approved;
	}
	public Boolean getIsFullTag() {
		return isFullTag;
	}
	public void setIsFullTag(Boolean isFullTag) {
		this.isFullTag = isFullTag;
	}

	public String getSynonymous() {
		return synonymous;
	}

	public void setSynonymous(String synonymous) {
		if (synonymous== null || synonymous.trim().equals("null"))
			synonymous="";
		this.synonymous = synonymous;
	}
	public String getName_search() {
		return name_search;
	}
	public void setName_search(String name_search) {
		this.name_search = name_search;
	}

}
