package com.tfsla.opencms.webusers.openauthorization.common;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.main.CmsLog;

import com.tfsla.opencms.webusers.openauthorization.common.exceptions.InvalidConfigurationException;

import java.util.Collections;

public class ProviderConfigurationLoader {
	private CPMConfig config = null;
	
	public ProviderConfigurationLoader() {
		
	}
	
	public List<ProviderField> getConfiguredLists(String siteName, String publication) throws InvalidConfigurationException {
		ArrayList<ProviderField> fields = new ArrayList<ProviderField>();
		
		for(ProviderField field : this.getAllFields(siteName, publication)) {
			if(field.getType() != null && field.getType().equals("list")) {
				fields.add(field);
			}
		}
		return fields;
	}
	
	public List<ProviderField> getConfiguredLists(String siteName, String publication, String providerName) throws InvalidConfigurationException {
		ArrayList<ProviderField> fields = new ArrayList<ProviderField>();
		
		ProviderConfiguration config = this.getConfiguration(providerName, siteName, publication);
		
		for(ProviderField field : config.getFields().values()) {
			if(field.getType().equals("list")) {
				fields.add(field);
			}
		}
		return fields;
	}
	
	public List<ProviderField> getConfiguredFields(String siteName, String publication, String providerName) throws InvalidConfigurationException {
		ArrayList<ProviderField> fields = new ArrayList<ProviderField>();
		
		ProviderConfiguration config = this.getConfiguration(providerName, siteName, publication);
		for(ProviderField field : config.getFields().values()) {
			if(!field.getType().equals("list")) {
				fields.add(field);
			}
		}
		
		return fields;
	}
	
	public List<ProviderField> getConfiguredFields(String siteName, String publication) throws InvalidConfigurationException {
		ArrayList<ProviderField> fields = new ArrayList<ProviderField>();
		
		for(ProviderField field : this.getAllFields(siteName, publication)) {
			if(field.getType() == null || !field.getType().equals("list")) {
				fields.add(field);
			}
		}
		return fields;
	}
	
	public List<SocialProvider> getProviders(String siteName, String publication) {
		List<SocialProvider> providers = new ArrayList<SocialProvider>();
		if(this.config == null) {
			this.setDefaultConfig();
		}
		
		for(String providerName : this.config.getParamList(siteName, publication, "webusers", "providersToSync")) {
			SocialProvider provider = new SocialProvider();
			provider.setName(providerName);
			provider.setDescription(this.config.getParam(siteName, publication, providerName, "description"));
			provider.setLogo(this.config.getParam(siteName, publication, providerName, "logo"));
			providers.add(provider);
		}
		return providers;
	}
	
	public ProviderConfiguration getConfiguration(String providerName, String siteName, String publication) throws InvalidConfigurationException {
		if(this.config == null) {
			this.setDefaultConfig();
		}
		
		if(this.config.getParam(siteName, publication, providerName, "format") == null) {
			InvalidConfigurationException exception = new InvalidConfigurationException();
			exception.setModuleName(providerName);
			throw exception;
		}
		
		ProviderConfiguration configuration = new ProviderConfiguration();
		configuration.setDescription(this.config.getParam(siteName, publication, providerName, "description"));
		configuration.setLogo(this.config.getParam(siteName, publication, providerName, "logo"));
		configuration.setFormat(this.config.getParam(siteName, publication, providerName, "format"));
		configuration.setLocale(this.config.getParam(siteName, publication, providerName, "locale"));
		configuration.setFieldsAsString(this.config.getParam(siteName, publication, providerName, "provider-fields"));
		configuration.setPriority(Integer.parseInt(this.config.getParam(siteName, publication, providerName, "priority")));
		configuration.setInviteSubject(this.config.getParam(siteName, publication, providerName, "invite-subject"));
		configuration.setInviteMessage(this.config.getParam(siteName, publication, providerName, "invite-message"));
		String inviteContacts = this.config.getParam(siteName, publication, providerName, "invite-contacts");
		if(inviteContacts != null && !inviteContacts.equals("")) {
			configuration.setInviteContacts(Boolean.parseBoolean(inviteContacts));
		}
		List<String> providerFields = this.config.getParamList(siteName, publication, providerName, "provider-fields");
		LinkedHashMap<String,String> providerFieldConfiguration = null;
		ProviderField providerField = null;
		String forceWrite = "";
		
		CmsLog.getLog(this).debug("Loaded configuration for provider " + providerName + ", " + providerFields.size() + " fields");
		
		for(String fieldName : providerFields) {
			providerFieldConfiguration = this.config.getGroupParam(siteName, publication, providerName, fieldName);
			if(providerFieldConfiguration == null) continue;
			
			providerField = new ProviderField();
			
			providerField.setName(fieldName);
			providerField.setDescription(providerFieldConfiguration.get("description"));
			providerField.setConverter(providerFieldConfiguration.get("converter"));
			providerField.setConverterParameter(providerFieldConfiguration.get("converterParameter"));
			providerField.setType(providerFieldConfiguration.get("type"));
			providerField.setEntryName(providerFieldConfiguration.get("entryname"));
			providerField.setPath(providerFieldConfiguration.get("path"));
			providerField.setProperty(providerFieldConfiguration.get("property"));
			providerField.setListIdField(providerFieldConfiguration.get("listIdField"));
			providerField.setListValueField(providerFieldConfiguration.get("listValueField"));
			forceWrite = providerFieldConfiguration.get("forceWrite");
			if(forceWrite != null && !forceWrite.equals("")) {
				providerField.setForceWrite(Boolean.parseBoolean(providerFieldConfiguration.get("forceWrite")));
			}
			configuration.addField(fieldName, providerField);
		}
		
		configuration.setProviderName(providerName.split("-")[1]);
		
		return configuration;
	}
	
	public List<ProviderField> getAllFields(String siteName, String publication) throws InvalidConfigurationException {
		ArrayList<ProviderField> fields = new ArrayList<ProviderField>();
		ArrayList<ProviderConfiguration> configurations = new ArrayList<ProviderConfiguration>();
		List<SocialProvider> providers = this.getProviders(siteName, publication);
		List<String> additionalInfoFields = this.config.getParamList(siteName, publication, "webusers", "additionalInfo");
		
		for(SocialProvider provider : providers) {
			configurations.add(this.getConfiguration(provider.getName(), siteName, publication));
		}
		
		Collections.sort(configurations);
		
		for(ProviderConfiguration providerConfiguration : configurations) {
			for(ProviderField field : providerConfiguration.getFields().values()) {
				if(!fields.contains(field) && !additionalInfoFields.contains(field.getName())) {
					fields.add(field);
				}
			}
		}
		CmsLog.getLog(this).error("Retrieving " + fields.size() + " fields to process");
		return fields;
	}
	
	public void setConfig(CPMConfig config){
		this.config = config;
	}
	
	private void setDefaultConfig() {
		config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
	}
}
