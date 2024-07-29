package com.tfsla.opencms.webusers.openauthorization.common;

import java.util.ArrayList;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsLog;

import com.tfsla.opencms.webusers.RegistrationModule;
import com.tfsla.opencms.webusers.openauthorization.UserProfileData;
import com.tfsla.opencms.webusers.openauthorization.common.exceptions.InvalidConfigurationException;
import com.tfsla.opencms.webusers.openauthorization.data.ProviderFieldDAO;

public class ProviderDataSync {
	
	private CmsUser webUser;
	private String siteName;
	private String publication;
	private String providerName;
	private UserProfileData profileData;
	private ProviderConfiguration config;
	
	public ProviderDataSync(CmsUser webUser, String siteName, String publication, String providerName, UserProfileData profileData) {
		this.webUser = webUser;
		this.siteName = siteName;
		this.publication = publication;
		this.providerName = providerName;
		this.profileData = profileData;
	}
	
	public void setDataFromProvider() throws Exception {
		ProviderConfiguration config = this.getConfig();
		
		UserProfileDataManager dataManager = UserProfileDataManager.getInstance(config, profileData);
		dataManager.updateUserProfileData();
	}
	
	public void updateProviderData(CmsObject cms) throws Exception {
		ProviderConfiguration config = this.getConfig();
		Object additionalInfo = null;
		ProviderField field = null;
		
		field = config.getFieldByProperty("setFirstName");
		if(webUser.getFirstname() == null || webUser.getFirstname().trim().equals("") || (field != null && field.getForceWrite())) {
			if(profileData.getFirstName() != null && !profileData.getFirstName().trim().equals(""))
				webUser.setFirstname(profileData.getFirstName());
		}
		field = config.getFieldByProperty("setLastName");
		if(webUser.getLastname() == null || webUser.getLastname().trim().equals("") || (field != null && field.getForceWrite())) {
			if(profileData.getLastName() != null && !profileData.getLastName().trim().equals(""))
			webUser.setLastname(profileData.getLastName());
		}
		field = config.getFieldByProperty("setEmail");
		if(webUser.getEmail() == null || webUser.getEmail().trim().equals("") || (field != null && field.getForceWrite())) {
			if(profileData.getEmail() != null && !profileData.getEmail().trim().equals(""))
			webUser.setEmail(profileData.getEmail());
		}
		
		additionalInfo = webUser.getAdditionalInfo("APODO");
		field = config.getFieldByProperty("setNickName");
		String nickName = additionalInfo.toString();
		if(additionalInfo == null || additionalInfo.toString().equals("") || (field != null && field.getForceWrite())) {
			webUser.setAdditionalInfo("APODO", profileData.getNickName());
			nickName = profileData.getNickName();
		}
		additionalInfo = webUser.getAdditionalInfo("USER_PICTURE");
		field = config.getFieldByProperty("setPicture");
		if(additionalInfo == null || additionalInfo.toString().equals("") || (field != null && field.getForceWrite())) {
			webUser.setAdditionalInfo("USER_PICTURE", profileData.getPicture());
			
			String imagePath = RegistrationModule.getInstance(cms).uploadImageOpenId(profileData.getPicture(),nickName,webUser.getId().toString(),cms);  
			
			if(imagePath!=null) {
				webUser.setAdditionalInfo("USER_PICTURE", imagePath);
			}
		}
		
		for(String key : profileData.getAdditionalInfos().keySet()) {
			additionalInfo = webUser.getAdditionalInfo(key);
			if(additionalInfo == null || additionalInfo.toString().equals("") || config.getFieldByEntryname(key).getForceWrite()) {
				webUser.setAdditionalInfo(key, profileData.getAdditionalInfo(key));
			}
		}
		
		if(profileData.getLists().size() > 0) {
			ProviderFieldDAO dao = new ProviderFieldDAO();
			try {
				if(!dao.openConnection()) {
					throw new Exception("Cannot open a DB connection");
				}
				
				for(String listName : profileData.getLists().keySet()) {
					
					ArrayList<ProviderListField> list = profileData.getLists().get(listName);
					if(!dao.userHasList(webUser.getId().toString(), listName) || config.getFieldByEntryname(listName).getForceWrite()) {
						dao.deleteUserList(webUser.getId().toString(), listName);
						dao.saveList(webUser.getId().toString(), providerName, listName, list);
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
				CmsLog.getLog(this).error("Error inserting data into the DB: " + e.getMessage());
			} finally {
				dao.closeConnection();
			}
		}
	}
	
	private ProviderConfiguration getConfig() throws InvalidConfigurationException {
		if(this.config == null) {
			ProviderConfigurationLoader configLoader = new ProviderConfigurationLoader();
			this.config = configLoader.getConfiguration(providerName, siteName, publication);
		}
		return this.config;
	}
}
