package com.tfsla.opencms.webusers.openauthorization.converters;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.tfsla.opencms.webusers.openauthorization.UserProfileData;
import com.tfsla.opencms.webusers.openauthorization.common.IValueConverter;
import com.tfsla.opencms.webusers.openauthorization.common.exceptions.InvalidConversionException;

public class RegexTableValueConverter implements IValueConverter {

	@Override
	public Object convert(Object item, Object parameter, UserProfileData data) throws InvalidConversionException {
		if(item == null) return "";
		
		BufferedReader br = null;
		String line = null;
		try {
			br = new BufferedReader(new FileReader(parameter.toString()));
			
			//Buscar match en las regular expressions de la tabla
			while ((line = br.readLine()) != null) {
				if(line == null || line.trim().equals("") || line.split("=").length != 2) continue;
				
				//path[0] = regular expression
				//path[1] = valor resultado del converter
				String[] paths = line.split("=");
				String regEx = paths[0];
				
				if(item.toString().matches(regEx)) {
					return paths[1];
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return "";
	}

}
