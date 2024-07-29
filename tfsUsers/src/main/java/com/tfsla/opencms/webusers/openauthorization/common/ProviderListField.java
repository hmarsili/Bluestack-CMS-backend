package com.tfsla.opencms.webusers.openauthorization.common;

import java.io.IOException;

import org.opencms.util.CmsDataTypeUtil;

public class ProviderListField {
	private String id;
	private Object value;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public void setValue(byte[] value) throws IOException, ClassNotFoundException {
		this.value = CmsDataTypeUtil.dataDeserialize(value, String.class.getName());
	}
}
