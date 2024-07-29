package com.tfsla.opencms.webusers.openauthorization.common;

import java.util.ArrayList;

import org.openid4java.message.ax.FetchResponse;

import com.tfsla.opencms.webusers.openauthorization.UserProfileData;
import com.tfsla.opencms.webusers.openauthorization.common.exceptions.InvalidPathException;

public class FetchResponseProfileDataManager extends UserProfileDataManager {

	protected FetchResponseProfileDataManager(ProviderConfiguration configuration, UserProfileData data) {
		super(configuration, data);
	}

	@Override
	protected Object getObjectValue(Object providerResponse, String path) throws InvalidPathException {
		FetchResponse response = (FetchResponse) providerResponse;
		String ret = null;
		try {
			ret = response.getAttributeValue(path);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	}

	@Override
	protected ArrayList<ProviderListField> getListValue(Object objectList,
			String idField, String valueField) {
		// TODO Auto-generated method stub
		return null;
	}

}
