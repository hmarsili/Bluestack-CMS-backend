package com.tfsla.diario.freshness.model;

public class SearchOptions {
	
	private String orderBy;
	private String siteName;
	private int publication; 
	private String url; 
	private Long from;
	private Long to;
	private String section;
	private String userName;
	private String history;

	public String getOrderBy() {
		return orderBy == null || orderBy.equals("") ? " NAME asc " : orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getHistory() {
		return history;
	}

	public void setHistory(String history) {
		this.history = history;
	}

	public boolean isNullUrl() {
		return this.url != null;
	}
	
	public boolean isNullSection() {
		return this.section != null;
	}
	
	public boolean isNullUserName() {
		return this.userName != null;
	}
	public boolean isNullFrom() {
		return this.from != null;
	}
	public boolean isNullTo() {
		return this.to != null;
	}
	
	public boolean isNotNullHistory() {
		return this.history != null;
	}

}