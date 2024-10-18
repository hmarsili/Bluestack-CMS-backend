package com.tfsla.diario.webservices.helpers;

import jakarta.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import com.tfsla.diario.webservices.common.strings.ExceptionMessages;

public class ParametersHelper {

	public String assertJSONParameter(String parameterName, JSONObject jsonObject) throws Exception {
		if(!jsonObject.containsKey(parameterName)) {
			throw new Exception(String.format(ExceptionMessages.MISSING_OR_EMPTY_PARAMETER, parameterName));
		}
		String parameterValue = jsonObject.getString(parameterName);
		if(parameterValue == null || parameterValue.equals("")) {
			throw new Exception(String.format(ExceptionMessages.MISSING_OR_EMPTY_PARAMETER, parameterName));
		}
		return parameterValue;
	}
	
	public String assertRequestParameter(String parameterName, HttpServletRequest request) throws Exception {
		String parameterValue = request.getParameter(parameterName);
		if(parameterValue == null || parameterValue.equals("")) {
			throw new Exception(String.format(ExceptionMessages.MISSING_OR_EMPTY_PARAMETER, parameterName));
		}
		return parameterValue;
	}
	
	public String getJsonStringValue(String key, JSONObject jsonObject) {
		if(jsonObject.containsKey(key))
			return jsonObject.getString(key);
		return "";
	}
}