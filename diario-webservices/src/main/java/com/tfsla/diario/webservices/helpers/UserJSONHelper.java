package com.tfsla.diario.webservices.helpers;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.services.PublicationService;
import com.tfsla.opencms.webusers.RegistrationModule;

public class UserJSONHelper {
	
	public static final String USER_TELEPHONE = "USER_TELEPHONE";
	public static final String USER_CELLPHONE = "USER_CELLPHONE";
	public static final String USER_STATE = "USER_STATE";
	public static final String USER_GENDER = "USER_GENDER";
	public static final String USER_BIRTHDATE = "USER_BIRTHDATE";
	public static final String USER_DNI = "USER_DNI";
	
	public UserJSONHelper() {
		this.paramsHelper = new ParametersHelper();
	}
	
	public CmsUser createCmsUserFromJSON(JSONObject jsonUser, CmsObject cms, RegistrationModule regModule) throws ParseException, Exception {
		return this.createCmsUserFromJSON(jsonUser, cms, regModule, true);
	}
	
	public CmsUser createCmsUserFromJSON(JSONObject jsonUser, CmsObject cms, RegistrationModule regModule, Boolean parseGroups) throws Exception {
		return createCmsUserFromJSON(jsonUser, cms, regModule, parseGroups, null);
	}
	
	public CmsUser createCmsUserFromJSON(JSONObject jsonUser, CmsObject cms, RegistrationModule regModule, Boolean parseGroups, String publicationID) throws Exception {
		List<String> infoAttributes = new ArrayList<String>();
		List<String> infoValues = new ArrayList<String>();
		List<String> additionalGroups = new ArrayList<String>();
		
		if(jsonUser.containsKey("additional-info") && ((JSON)jsonUser.get("additional-info")).isArray()) {
			JSONArray additionalInfo = jsonUser.getJSONArray("additional-info");
			for(int j=0; j<additionalInfo.size(); j++) {
				JSONObject infoItem = (JSONObject)additionalInfo.get(j);
				for(Object key : infoItem.keySet()) {
					if(key != null && !key.equals("")) {
						infoAttributes.add(key.toString());
						infoValues.add(infoItem.getString(key.toString()));
					}
				}
			}
		}
		
		if(parseGroups) {
			if(jsonUser.containsKey("groups") && ((JSON)jsonUser.get("groups")).isArray()) {
				JSONArray groups = jsonUser.getJSONArray("groups");
				for(int j=0; j<groups.size(); j++) {
					additionalGroups.add(groups.getString(j));
				}
			}
		}
		
		String userName = paramsHelper.assertJSONParameter("username", jsonUser);
		String password = paramsHelper.assertJSONParameter("password", jsonUser);
		String email = paramsHelper.assertJSONParameter("email", jsonUser);
		
		if(publicationID!=null)
			regModule.setPublication(publicationID);
		
		CmsUser user = regModule.addWebUser(
			cms,
			userName,
			password,
			password,
			paramsHelper.assertJSONParameter("first-name", jsonUser),
			paramsHelper.assertJSONParameter("last-name", jsonUser),
			email,
			email,
			paramsHelper.getJsonStringValue("document", jsonUser),
			paramsHelper.getJsonStringValue("birthday", jsonUser),
			paramsHelper.getJsonStringValue("gender", jsonUser),
			paramsHelper.getJsonStringValue("country", jsonUser),
			paramsHelper.getJsonStringValue("state", jsonUser),
			paramsHelper.getJsonStringValue("city", jsonUser),
			paramsHelper.getJsonStringValue("address", jsonUser),
			paramsHelper.getJsonStringValue("zipcode", jsonUser),
			paramsHelper.getJsonStringValue("phone", jsonUser),
			paramsHelper.getJsonStringValue("mobile", jsonUser),
			infoAttributes,
			infoValues,
			additionalGroups
		);
		
		String active = paramsHelper.getJsonStringValue("active", jsonUser);
		if(active.toLowerCase().equals(Boolean.TRUE.toString())) {
			user.setEnabled(true); 
		    user.setAdditionalInfo("USER_PENDING", "false");
		    cms.writeUser(user);
		}
		
		return user;
	}
	
	public CmsUser getCmsUserFromJSON(JSONObject jsonUser, CmsObject cms) throws Exception {
		String userName = paramsHelper.assertJSONParameter("username", jsonUser);
		if (userName == null)
			throw new Exception("Invalid user name");
		CmsUser user =  cms.readUser(userName);
		return user;
	}
	
	public boolean togleActivationCmsUserFromJSON(JSONObject jsonUser, CmsObject cms, CmsUser user) throws Exception {
		String active = paramsHelper.getJsonStringValue("active", jsonUser);
		boolean activeValue;
		if(active.toLowerCase().equals(Boolean.TRUE.toString())) {
			activeValue = true;
		    user.setAdditionalInfo("USER_PENDING", "false");
		} else if (active.toLowerCase().equals(Boolean.FALSE.toString())){
			activeValue = false;
		} else 
			throw new Exception("invalid parameter active");
		user.setEnabled(activeValue);
		cms.writeUser(user);
		return activeValue;
	}
	
	
	@SuppressWarnings("unchecked")
	public CmsUser updateCmsUserFromJSON(JSONObject jsonUser, CmsObject cms, RegistrationModule regModule) throws Exception {
		String userName = paramsHelper.assertJSONParameter("username", jsonUser);
		CmsUser user =  cms.readUser(userName);
		UserUpdateHelper helper = new UserUpdateHelper(cms, user);
		user = helper.assertAndUpdate(jsonUser, regModule);
		
		if (jsonUser.containsKey("active")) {
			String active = paramsHelper.getJsonStringValue("active", jsonUser);
			boolean activeValue;
			if(active.toLowerCase().equals(Boolean.TRUE.toString())) {
				activeValue = true;
			    user.setAdditionalInfo("USER_PENDING", "false");
			    user.setEnabled(activeValue);
			} else if (active.toLowerCase().equals(Boolean.FALSE.toString())){
				activeValue = false;
				user.setEnabled(activeValue);
			}
		}
		
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
		String ou =  config.getParam(siteName, String.valueOf(PublicationService.getCurrentPublicationId(cms)), "webusers", "usersOu","webUser/");
		
		if(jsonUser.containsKey("groups") && ((JSON)jsonUser.get("groups")).isArray()) {
			List<CmsGroup> gruposDelUsuario = cms.getGroupsOfUser(userName, true, false);
			for (CmsGroup group : gruposDelUsuario) {
				cms.removeUserFromGroup(userName, group.getName());
			}
			if (user.isWebuser()) {
				cms.addUserToGroup(user.getName(), "TFS-WEBUSERS");
			}
			
			JSONArray groups = jsonUser.getJSONArray("groups");
			for(int j=0; j<groups.size(); j++) {
				try {
					cms.addUserToGroup(user.getName(),groups.getString(j) );
				} catch (CmsException e) {
					if (user.isWebuser())	
						cms.addUserToGroup(user.getName(),ou + groups.getString(j) );
				}
			}
		}
			
		cms.writeUser(user);
		return user;
	}
	
	protected ParametersHelper paramsHelper;
}