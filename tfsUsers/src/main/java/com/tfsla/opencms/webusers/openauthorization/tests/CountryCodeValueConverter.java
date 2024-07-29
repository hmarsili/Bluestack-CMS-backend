package com.tfsla.opencms.webusers.openauthorization.tests;

import com.tfsla.opencms.webusers.openauthorization.UserProfileData;
import com.tfsla.opencms.webusers.openauthorization.common.IValueConverter;

public class CountryCodeValueConverter implements IValueConverter {

	@Override
	public Object convert(Object item, Object parameter, UserProfileData data) {
		return item.toString().toLowerCase();
	}

}
