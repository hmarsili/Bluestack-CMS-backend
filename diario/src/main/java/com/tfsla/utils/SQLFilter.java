package com.tfsla.utils;

public interface SQLFilter {

	String toWhereSql();
	String toHavingSql();
	
	SQLFilter andEqual(String attribute, String value);
	SQLFilter orEqual(String attribute, String value);

	SQLFilter or(SQLFilter andFilter);
	SQLFilter and(SQLFilter andFilter);
	
	SQLFilter addHaving(String having);
}
