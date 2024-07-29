package com.tfsla.opencms.webusers.rankings;

import java.util.ArrayList;

public class RankingsServiceRequest {
	
	private String dimensionX;
	private String dimensionY;
	private ArrayList<RankingReportFilter> filters;
	
	public RankingsServiceRequest() {
		this.filters = new ArrayList<RankingReportFilter>();
	}
	
	public String getDimensionX() {
		return dimensionX;
	}
	public void setDimensionX(String dimensionX) {
		this.dimensionX = dimensionX;
	}
	public String getDimensionY() {
		return dimensionY;
	}
	public void setDimensionY(String dimensionY) {
		this.dimensionY = dimensionY;
	}
	public ArrayList<RankingReportFilter> getFilters() {
		return filters;
	}
	public void setFilters(ArrayList<RankingReportFilter> filters) {
		this.filters = filters;
	}
}
