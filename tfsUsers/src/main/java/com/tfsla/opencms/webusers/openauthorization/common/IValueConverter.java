package com.tfsla.opencms.webusers.openauthorization.common;

import com.tfsla.opencms.webusers.openauthorization.UserProfileData;
import com.tfsla.opencms.webusers.openauthorization.common.exceptions.InvalidConversionException;

public interface IValueConverter {
	Object convert(Object item, Object parameter, UserProfileData data) throws InvalidConversionException;
}
