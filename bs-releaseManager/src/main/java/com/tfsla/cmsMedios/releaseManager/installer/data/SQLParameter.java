package com.tfsla.cmsMedios.releaseManager.installer.data;

public class SQLParameter {
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public SQLParameterType getParameterType() {
		return parameterType;
	}
	public void setParameterType(SQLParameterType parameterType) {
		this.parameterType = parameterType;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	
	int index;
	Object value;
	SQLParameterType parameterType;
	String format;
}
