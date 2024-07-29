package com.tfsla.diario.analytics.model;

public class NewsAnalyticsData {

	private String sitename;
	private int publication;
	private String page;
	private String position;
	private String prints;
	private String ctr;
	private String clicks;
	private long updatedDate;
	
	public String getSitename() {
		return sitename;
	}
	public void setSitename(String sitename) {
		this.sitename = sitename;
	}
	public int getPublication() {
		return publication;
	}
	public void setPublication(int publication) {
		this.publication = publication;
	}
	public String getPage() {
		return page;
	}
	public void setPage(String page) {
		this.page = page;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public String getPrints() {
		return prints;
	}
	public void setPrints(String prints) {
		this.prints = prints;
	}
	public String getCtr() {
		return ctr;
	}
	public void setCtr(String ctr) {
		this.ctr = ctr;
	}
	public String getClicks() {
		return clicks;
	}
	public void setClicks(String clicks) {
		this.clicks = clicks;
	}
	public long getUpdatedDate() {
		return updatedDate;
	}
	public void setUpdatedDate(long updatedDate) {
		this.updatedDate = updatedDate;
	}
	
	
}
