package com.tfsla.opencms.webusers.openauthorization.common;

import java.util.ArrayList;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.tfsla.opencms.webusers.openauthorization.UserProfileData;
import com.tfsla.opencms.webusers.openauthorization.common.exceptions.InvalidPathException;

public class JSONUserProfileDataManager extends UserProfileDataManager {

	protected JSONUserProfileDataManager(ProviderConfiguration configuration, UserProfileData data) {
		super(configuration, data);
	}

	@Override
	protected Object getObjectValue(Object providerResponse, String jsonPath) throws InvalidPathException {
		JSONObject json = (JSONObject) providerResponse;
		Object objectValue = null;
		String [] paths = null;
		JSONObject jsonObject = json;
		
		if(jsonPath == null) {
			InvalidPathException exception = new InvalidPathException("");
			exception.setDocument(json);
			exception.printStackTrace();
		}
		
		try {
			paths = jsonPath.split("\\.");
			//Obtener el valor del JSon en caso de que el path sea combinado
			for(String path : paths) {
				if(!path.equals(paths[paths.length - 1])){
					jsonObject = jsonObject.getJSONObject(path);
				}
			}
			if(paths.length > 1) {
				jsonPath = paths[paths.length - 1];
			}
			objectValue = jsonObject.getString(jsonPath);
		}
		catch(Exception e) {
			InvalidPathException exception = new InvalidPathException(jsonPath);
			exception.setDocument(json);
			exception.setInnerException(e);
			exception.setPath(jsonPath);
			exception.printStackTrace();
			//throw exception;
		}
		return objectValue;
	}

	@Override
	protected ArrayList<ProviderListField> getListValue(Object objectList, String idField, String valueField) {
		ArrayList<ProviderListField> ret = new ArrayList<ProviderListField>();
		try {
			JSONArray jsonArray = JSONArray.fromObject(objectList);
			ProviderListField listField = null;
			JSONObject jsonItem = null;
			
			for(int i=0;i<jsonArray.size();i++) {
				listField = new ProviderListField();
				jsonItem = jsonArray.getJSONObject(i);
				if(jsonItem == null) continue;
				
				if(idField != null && !idField.equals("") && jsonItem.containsKey(idField)) {
					listField.setId(jsonItem.getString(idField));
				}
				if(valueField != null && !valueField.equals("") && jsonItem.containsKey(valueField)) {
					listField.setValue(jsonItem.getString(valueField));
				}
				if(valueField == null || valueField.equals("")) {
					listField.setValue(jsonItem.toString());
				}
				ret.add(listField);
			}
		} catch(Exception e) {
			e.printStackTrace();
			if(objectList != null) {
				try {
					if(objectList.toString().contains(",")) {
						for(String item : objectList.toString().split(",")) {
							ProviderListField listField = new ProviderListField();
							listField.setValue(item);
							ret.add(listField);
						}
					}
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		return ret;
	}
}
