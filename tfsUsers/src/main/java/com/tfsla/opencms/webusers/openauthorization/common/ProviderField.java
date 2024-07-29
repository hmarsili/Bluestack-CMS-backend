package com.tfsla.opencms.webusers.openauthorization.common;

import com.tfsla.opencms.webusers.rankings.DimensionTypes;
import com.tfsla.opencms.webusers.rankings.UserDimension;

public class ProviderField extends UserDimension {
	
	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getConverter() {
		return converter;
	}

	public void setConverter(String converter) {
		this.converter = converter;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setProperty(String property) {
		this.property = property;
	}
	
	public String getProperty() {
		return property;
	}
	
	public boolean getForceWrite() {
		return forceWrite;
	}

	public void setForceWrite(boolean forceWrite) {
		this.forceWrite = forceWrite;
	}

	public Object getConverterParameter() {
		return converterParameter;
	}

	public void setConverterParameter(Object converterParameter) {
		this.converterParameter = converterParameter;
	}

	public String getListIdField() {
		return listIdField;
	}

	public void setListIdField(String listIdField) {
		this.listIdField = listIdField;
	}

	public String getListValueField() {
		return listValueField;
	}

	public void setListValueField(String listValueField) {
		this.listValueField = listValueField;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof ProviderField)) return false;
		ProviderField field = (ProviderField)obj;
		
		return field.getName().equals(this.getName());
	}
	
	@Override
	public void setType(String type) {
		if(type != null && type.equals(DimensionTypes.LIST)) {
			this.setTable("TFS_PROVIDER_COLLECTIONS");
		}
		super.setType(type);
	}
	
	private String property;
	private String path;
	private String converter;
	private String listIdField;
	private String listValueField;
	private Object converterParameter;
	private Object value;
	private boolean forceWrite;
}
