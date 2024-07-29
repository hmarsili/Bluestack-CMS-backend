package com.tfsla.diario.admin;

import com.tfsla.opencms.webusers.openauthorization.common.ProviderField;

public class AdminSearchField {
	private Boolean isProviderField;
	private String fieldName;
	private ProviderField providerField;
	
	public Boolean getIsProviderField() {
		return isProviderField;
	}
	
	public void setIsProviderField(Boolean isProviderField) {
		this.isProviderField = isProviderField;
	}
	
	public String getFieldName() {
		return fieldName;
	}
	
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public ProviderField getProviderField() {
		return providerField;
	}

	public void setProviderField(ProviderField providerField) {
		this.providerField = providerField;
	}
}
