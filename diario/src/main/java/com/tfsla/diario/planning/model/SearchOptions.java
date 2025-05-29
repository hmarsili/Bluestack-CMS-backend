package com.tfsla.diario.planning.model;

public class SearchOptions {
	
	private String orderBy;
	private String siteName;
	private int publication; 
	private int id; 
	private String text;
	private String userName;
	private Long from;
	private Long to;
	private int count;
	private String history;
	
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
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getOrderBy() {
		return orderBy == null || orderBy.equals("") ? " NAME asc " : orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
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
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
	public String getHistory() {
		return history;
	}
	public void setHistory(String history) {
		this.history = history;
	}
	
	public boolean isNotNullFrom() {
		return this.from != null;
	}
	public boolean isNotNullTo() {
		return this.to != null;
	}
	public boolean isNotNullHistory() {
		return String.valueOf(this.history) != null;
	}
	public boolean isNotNullId() {
		return String.valueOf(this.id) != null;
	}
	
}