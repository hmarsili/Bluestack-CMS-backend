package com.tfsla.diario.productivityPlans.model;

public class PlansUsers {
	
	
	private String siteName;
	private int publication;
	private String id;
	private String dataValue;
	private String dataKey;
	private Long startDay;
	
	public Long getStartDay() {
		return startDay;
	}
	public void setStartDay(Long startDay) {
		this.startDay = startDay;
	}
	public String getDataValue() {
		return dataValue;
	}
	public void setDataValue(String dataValue) {
		this.dataValue = dataValue;
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
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDataKey() {
		return dataKey;
	}
	public void setDataKey(String dataKey) {
		this.dataKey = dataKey;
	}

		
}
