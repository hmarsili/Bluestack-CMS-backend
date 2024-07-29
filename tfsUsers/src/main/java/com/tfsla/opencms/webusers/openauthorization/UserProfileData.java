package com.tfsla.opencms.webusers.openauthorization;

import java.util.ArrayList;
import java.util.HashMap;

import com.tfsla.opencms.webusers.openauthorization.common.ProviderListField;

public class UserProfileData {
	public UserProfileData() {
		additionalInfo = new HashMap<String, Object>();
		lists = new HashMap<String, ArrayList<ProviderListField>>();
	}
	
	private String key;
	private String firstName;
	private String lastName;	
	private String email;
	private String nickName;
	private String picture;
	private String userUrl;
	private String accessToken;
	private String accessSecret;
	private HashMap<String, Object> additionalInfo;
	private HashMap<String, ArrayList<ProviderListField>> lists;
	private Object providerResponse;
	
	public HashMap<String, Object> getAdditionalInfos() {
		return this.additionalInfo;
	}
	
	public Object getAdditionalInfo(String key) {
		return this.additionalInfo.get(key);
	}
	
	public void putAdditionalInfo(String key, Object value) {
		this.additionalInfo.put(key, value);
	}
	
	public String getKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}	
		
	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getNickName() {
		return this.nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
		
	public String getPicture() {
		return this.picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}
	
	public String getUserUrl() {
		return this.userUrl;
	}

	public void setUserUrl(String userUrl) {
		this.userUrl = userUrl;
	}	
	
	public String getAccessToken() {
		return this.accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
	public String getAccessSecret() {
		return this.accessSecret;
	}

	public void setAccessSecret(String accessSecret) {
		this.accessSecret = accessSecret;
	}

	public Object getProviderResponse() {
		return providerResponse;
	}

	public void setProviderResponse(Object providerResponse) {
		this.providerResponse = providerResponse;
	}

	public HashMap<String, ArrayList<ProviderListField>> getLists() {
		return lists;
	}

	public ArrayList<ProviderListField> getList(String listName) {
		return this.lists.get(listName);
	}
	
	public void putList(String listName, ArrayList<ProviderListField> listValues) {
		this.lists.put(listName, listValues);
	}
}