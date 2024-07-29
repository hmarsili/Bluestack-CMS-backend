package com.tfsla.genericImport.model;

public class DBColumn {
	private String field;
	private String type;
	private String allowNull;
	private String isKey;
	private String defaultValue;
	private String extra;
	private String table;


	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getAllowNull() {
		return allowNull;
	}
	public void setAllowNull(String allowNull) {
		this.allowNull = allowNull;
	}
	public String getIsKey() {
		return isKey;
	}
	public void setIsKey(String isKey) {
		this.isKey = isKey;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public String getExtra() {
		return extra;
	}
	public void setExtra(String extra) {
		this.extra = extra;
	}

}
