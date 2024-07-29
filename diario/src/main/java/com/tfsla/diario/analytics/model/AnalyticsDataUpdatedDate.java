package com.tfsla.diario.analytics.model;

public class AnalyticsDataUpdatedDate {

	private String sitename;
	private int publication;
	private long automaticUpdatedDate;
	
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
	public long getAutomaticUpdatedDate() {
		return automaticUpdatedDate;
	}
	public void setAutomaticUpdatedDate(long automaticUpdatedDate) {
		this.automaticUpdatedDate = automaticUpdatedDate;
	}
	
}
