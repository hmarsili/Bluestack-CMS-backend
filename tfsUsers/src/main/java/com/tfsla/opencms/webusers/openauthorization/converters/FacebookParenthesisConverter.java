package com.tfsla.opencms.webusers.openauthorization.converters;

import com.tfsla.opencms.webusers.openauthorization.UserProfileData;
import com.tfsla.opencms.webusers.openauthorization.common.IValueConverter;
import com.tfsla.opencms.webusers.openauthorization.common.exceptions.InvalidConversionException;

public class FacebookParenthesisConverter implements IValueConverter {

	@Override
	public Object convert(Object item, Object parameter, UserProfileData data) throws InvalidConversionException {
		if(item == null) return item;
		String itemString = item.toString();
		if(itemString.trim().endsWith("()")) {
			itemString = itemString.replace("()", "");
		}
		return itemString;
	}

}
