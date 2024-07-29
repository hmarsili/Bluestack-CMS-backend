package com.tfsla.opencms.webusers.rankings;

public class RankingReportFilter {
	
	private UserDimension dimension;
	private String operator;
	private String value;
	
	public RankingReportFilter(String dimension, String operator, String value) {
		this.dimension = new RankingDimension();
		this.dimension.setName(dimension);
		this.operator = operator;
		this.value = value;
	}
	
	public UserDimension getDimension() {
		return dimension;
	}
	public void setDimension(UserDimension dimension) {
		this.dimension = dimension;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String getValue() {
		if(this.operator.equals("like")) return "%" + value + "%";
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
