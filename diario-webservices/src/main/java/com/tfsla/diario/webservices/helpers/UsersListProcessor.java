package com.tfsla.diario.webservices.helpers;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;

import com.tfsla.diario.webservices.common.ServiceHelper;
import com.tfsla.opencms.webusers.openauthorization.common.ProviderConfigurationLoader;
import com.tfsla.opencms.webusers.openauthorization.common.ProviderField;
import com.tfsla.opencms.webusers.openauthorization.common.ProviderListField;
import com.tfsla.opencms.webusers.openauthorization.data.ProviderFieldDAO;

/**
 * Provides services to process users collections, by adding their social data and groups
 * and removing their hidden fields (parametrized by configuration) 
 */
public class UsersListProcessor {
	
	/**
	 * Processes a users collection and retrieves them into a JSONArray
	 * @param users array with users, usually retrieved by DataUsersCollector or SearchUsers instances
	 * @param cms CmsObject of the current session
	 * @param site current site to retrieve the configuration from
	 * @param publication current publication to retrieve the configuration from
	 * @return a JSONArray instance with the users processed
	 */
	public static synchronized JSONArray processUsersList(List<CmsUser> users, CmsObject cms, String site, String publication) {
		return processUsersList(users, cms, site, publication, false);
	}
	
	/**
	 * Processes a users collection and retrieves them into a JSONArray
	 * @param users array with users, usually retrieved by DataUsersCollector or SearchUsers instances
	 * @param cms CmsObject of the current session
	 * @param site current site to retrieve the configuration from
	 * @param publication current publication to retrieve the configuration from
	 * @param onlyGroupNames indicates if the groups object should have just group names or their info too
	 * @return a JSONArray instance with the users processed
	 */
	public static synchronized JSONArray processUsersList(List<CmsUser> users, CmsObject cms, String site, String publication, Boolean onlyGroupNames) {
		JSONArray jsonResult = JSONArray.fromObject(users);
		JSONObject jsonObject = null;
		for(int i=0; i < jsonResult.size(); i++) {
			jsonObject = (JSONObject)jsonResult.get(i);
			
			//ADD USER GROUPS TO THE JSON OBJECT
			try {
				JSONArray groups = JSONArray.fromObject(cms.getGroupsOfUser(jsonObject.getString("name"), false));
				if(!onlyGroupNames) {
					jsonObject.put("groups", groups);
				} else {
					List<String> groupNames = new ArrayList<String>();
					for(int j=0; j < groups.size(); j++) {
						JSONObject groupObject = (JSONObject)groups.get(j);
						groupNames.add(groupObject.getString("name"));
					}
					jsonObject.put("groups", StringUtils.join(groupNames.iterator(), ","));
				}
			} catch (CmsException e) {
				e.printStackTrace();
			}
			
			//SET SOCIAL DATA LISTS AS ADDITIONAL INFO
			setListsAsAdditionalInfo(jsonObject, site, publication);
			
			//REMOVE HIDDEN FIELDS
			removeHiddenFields(jsonObject, ServiceHelper.getWSConfiguration(site, publication).getUsersHiddenFields());
		}
		
		return jsonResult;
	}
	
	/**
	 * Removes the fields those should not be retrieved by configuration
	 * @param jsonObject the object the fields will be removed from
	 * @param hiddenFields a list with fields to remove from the JSON
	 */
	private static void removeHiddenFields(JSONObject jsonObject, List<String> hiddenFields) {
		for(String field : hiddenFields) {
			if(!field.contains(".")) {
				jsonObject.remove(field);
			} else {
				String[] paths = field.split("\\.");
				JSONObject objectPath = jsonObject;
				for(String path : paths) {
					if(!path.equals(paths[paths.length - 1])){
						objectPath = objectPath.getJSONObject(path);
					}
				}
			 	String jsonPath = paths[paths.length - 1];
				objectPath.remove(jsonPath);
			}
		}
	}
	
	/**
	 * Adds the user lists (from social providers) as additional info
	 * @param jsonObject the object to add the lists to
	 * @param site current site to retrieve the configuration from
	 * @param publication current publication to retrieve the configuration from
	 */
	private static void setListsAsAdditionalInfo(JSONObject jsonObject, String site, String publication) {
		ProviderFieldDAO dao = new ProviderFieldDAO();
		try {
			dao.openConnection();
			ProviderConfigurationLoader loader = new ProviderConfigurationLoader();
			JSONObject jsonId = jsonObject.getJSONObject("id");
			JSONObject additionalInfo = jsonObject.getJSONObject("additionalInfo");
			JSONObject jsonList = new JSONObject();
			String userId = jsonId.getString("stringValue");
			
			//ITERATE OVER EVERY LIST CONFIGURED
			for(ProviderField list : loader.getConfiguredLists(site, publication)) {
				ArrayList<ProviderListField> values = dao.getListValues(list.getEntryName(), userId);
				if(values.size() > 0) {
					jsonList.put(list.getName(), JSONArray.fromObject(values));
				}
			}
			
			additionalInfo.put("LISTS", jsonList);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				dao.closeConnection();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}