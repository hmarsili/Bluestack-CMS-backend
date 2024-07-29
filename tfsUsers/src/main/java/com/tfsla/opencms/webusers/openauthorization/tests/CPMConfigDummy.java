package com.tfsla.opencms.webusers.openauthorization.tests;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.opencms.configuration.CPMConfig;

public class CPMConfigDummy extends CPMConfig {
	
	@Override
	public String getParam(String site, String publication, String module, String param) {
		if(module.equals("webusers-invalidConfiguration")) {
			return null;
		}
		if(param.equals("format")) {
			return "json";
		}
		if(module.equals("webusers-facebook")) {
			if(param.equals("priority")){
				return "1";
			}
			return "es_AR";
		}
		if(module.equals("webusers-twitter"))
			return "2";
		if(module.equals("webusers-googlePlus"))
			return "3";
		
		return "";
	}
	
	@Override
	public int getIntegerParam(String site, String publication, String module, String param) {
		if(module.equals("webusers-facebook"))
			return 1;
		if(module.equals("webusers-twitter"))
			return 2;
		if(module.equals("webusers-googlePlus"))
			return 3;
		return 0;
	}
	
	@Override
	public List<String> getParamList(String site, String publication, String module, String param) {
		List<String> retList = new ArrayList<String>();
		if(module.equals("webusers-facebook")) {
			retList.add("username");
			retList.add("email");
			retList.add("gender");
			retList.add("likes");
		}
		else if(module.equals("webusers-twitter")) {
			retList.add("name");
			retList.add("location");
		}
		else if(module.equals("webusers-googlePlus")) {
			retList.add("name");
			retList.add("gender");
		}
		else if(module.equals("webusers")) {
			retList.add("webusers-facebook");
			retList.add("webusers-twitter");
			retList.add("webusers-googlePlus");
		}
		return retList;
	}
	
	@Override
	public LinkedHashMap<String,String> getGroupParam(String site, String publication, String module, String group) {
		LinkedHashMap<String,String> retHashMap = new LinkedHashMap<String, String>();
		
		if(module.equals("webusers-facebook")) {
			if(group.equals("username")) {
				retHashMap.put("type", "string");
				retHashMap.put("entryname", "USER_NAME");
				retHashMap.put("path", "username");
				retHashMap.put("property", "setNickName");
				retHashMap.put("forceWrite", "true");
			}
			else if(group.equals("email")) {
				retHashMap.put("description", "Email");
				retHashMap.put("type", "string");
				retHashMap.put("entryname", "USER_EMAIL");
				retHashMap.put("path", "email");
				retHashMap.put("property", "setEmail");
			}
			else if(group.equals("gender")) {
				retHashMap.put("type", "string");
				retHashMap.put("entryname", "USER_GENDER");
				retHashMap.put("path", "gender");
				retHashMap.put("converter", "com.tfsla.diario.webusers.services.FacebookGenderConverter");
			}
			else if(group.equals("likes")) {
				retHashMap.put("type", "list");
				retHashMap.put("entryname", "USER_LIKES");
				retHashMap.put("path", "likes");
			}
		}
		else if(module.equals("webusers-twitter")) {
			if(group.equals("name")) {
				retHashMap.put("type", "string");
				retHashMap.put("entryname", "USER_NAME");
				retHashMap.put("path", "name");
			}
			else if(group.equals("location")) {
				retHashMap.put("type", "string");
				retHashMap.put("entryname", "USER_LOCATION");
				retHashMap.put("path", "location");
				retHashMap.put("converter", "com.tfsla.diario.webusers.services.TwitterLocationConverter");
			}
		}
		else if(module.equals("webusers-googlePlus")) {
			if(group.equals("name")) {
				retHashMap.put("type", "string");
				retHashMap.put("entryname", "USER_NAME");
				retHashMap.put("path", "name.formatted");
			}
			else if(group.equals("gender")) {
				retHashMap.put("type", "string");
				retHashMap.put("entryname", "USER_GENDER");
				retHashMap.put("path", "gender");
				retHashMap.put("converterParameter", "parameter");
				retHashMap.put("converter", "com.tfsla.diario.webusers.services.GooglePlusGenderConverter");
			}
		}
		
		return retHashMap;
	}
}
