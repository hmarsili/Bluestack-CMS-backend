package com.tfsla.diario.order;

import java.util.ArrayList;
import java.util.List;

public abstract class A_OrderDirective {

	public static final String TYPE_DATE = "Date";
	public static final String TYPE_INTEGER = "Integer";
	public static final String TYPE_STRING = "String";
	public static final String TYPE_LONG = "Long";
	public static final String TYPE_FLOAT = "Float";
	
	protected static List<A_OrderDirective> availableOrders = new ArrayList<A_OrderDirective>();

	protected boolean ascending=false;
	protected String propertyName;
	protected String luceneName;
	protected String contentName;
	protected String type;
	protected String name;

	public static String getDataType(String value) {
		
		if (value==null)
			return TYPE_STRING;
		
		value = value.trim().toLowerCase();
		
		if (value.equals("alphanumeric") || value.equals("string") || value.equals("text"))
			return TYPE_STRING;
		
		if (value.equals("integer"))
			return TYPE_INTEGER;

		if (value.equals("decimal") || value.equals("real") || value.equals("numeric"))
			return TYPE_FLOAT;

		if (value.equals("date") || value.equals("datetime"))
			return TYPE_DATE;

		if (value.equals("bignumber") || value.equals("long"))
			return TYPE_LONG;

		return TYPE_STRING;
	}
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final A_OrderDirective other = (A_OrderDirective) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	public String getName() {
		return name;
	}
	public String getType() {
		return type;
	}
	public String getContentName() {
		return contentName;
	}
	public String getLuceneName() {
		return luceneName;
	}
	public String getPropertyName() {
		return propertyName;
	}

	public boolean isAscending() {
		return ascending;
	}
	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}
	
	public A_OrderDirective(String name, String propertyName, String luceneName, String contentName, String type, boolean ascending)
	{
		this.name = name;
		this.contentName = contentName;
		this.propertyName = propertyName;
		this.luceneName = luceneName;
		this.type = type;
		this.ascending = ascending;
	}

	public A_OrderDirective(String name) {
		this.name = name;
	}

}
