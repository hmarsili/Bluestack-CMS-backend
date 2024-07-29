package com.tfsla.opencms.webusers.openauthorization.common;

import java.util.Hashtable;

public class ProviderConfiguration implements Comparable<ProviderConfiguration> {
	
	public ProviderConfiguration() {
		this.fields = new Hashtable<String, ProviderField>();
		this.inviteContacts = false;
	}
	
	private Hashtable<String, ProviderField> fields;
	private String format;
	private String providerName;
	private String description;
	private String logo;
	private String locale;
	private String fieldsAsString;
	private String inviteSubject;
	private String inviteMessage;
	private Boolean inviteContacts;
	private int priority;
	
	public String getFormat() {
		return format;
	}
	
	public void setFormat(String format) {
		this.format = format;
	}
	
	public void addField(String fieldName, ProviderField field) {
		fields.put(fieldName, field);
	}
	
	public ProviderField getFieldByProperty(String propertyName) {
		for(String key : fields.keySet()) {
			if(fields.get(key).getProperty().equals(propertyName)) {
				return fields.get(key);
			}
		}
		return null;
	}
	
	public ProviderField getFieldByEntryname(String entryName) {
		for(String key : fields.keySet()) {
			if(fields.get(key).getEntryName().equals(entryName)) {
				return fields.get(key);
			}
		}
		return null;
	}
	
	public ProviderField getField(String fieldName) {
		return fields.get(fieldName);
	}
	
	public Hashtable<String, ProviderField> getFields() {
		return fields;
	}

	public int getPriority() {
		return priority;
	}

	public String getProviderName() {
		return providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	@Override
	public int compareTo(ProviderConfiguration arg0) {
		return this.getPriority() - arg0.getPriority();
	}

	public String getFieldsAsString() {
		return fieldsAsString;
	}

	public void setFieldsAsString(String fieldsAsString) {
		this.fieldsAsString = fieldsAsString;
	}

	public Boolean getInviteContacts() {
		return inviteContacts;
	}

	public void setInviteContacts(Boolean inviteContacts) {
		this.inviteContacts = inviteContacts;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getInviteMessage() {
		return inviteMessage;
	}

	public void setInviteMessage(String inviteMessage) {
		this.inviteMessage = inviteMessage;
	}

	public String getInviteSubject() {
		return inviteSubject;
	}

	public void setInviteSubject(String inviteSubject) {
		this.inviteSubject = inviteSubject;
	}
}
