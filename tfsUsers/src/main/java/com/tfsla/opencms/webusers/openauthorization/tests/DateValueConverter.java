package com.tfsla.opencms.webusers.openauthorization.tests;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.tfsla.opencms.webusers.openauthorization.UserProfileData;
import com.tfsla.opencms.webusers.openauthorization.common.IValueConverter;
import com.tfsla.opencms.webusers.openauthorization.common.exceptions.InvalidConversionException;

public class DateValueConverter implements IValueConverter {

	@Override
	public Object convert(Object item, Object parameter, UserProfileData data) throws InvalidConversionException {
		SimpleDateFormat formatter = new SimpleDateFormat(parameter.toString());
		try {
			return formatter.parse(item.toString());
		} catch (ParseException e) {
			InvalidConversionException exception = new InvalidConversionException();
			exception.setInnerException(e);
			exception.setValue(item);
			exception.setValueConverterType(DateValueConverter.class.getName());
			throw exception;
		}
	}
	
}
