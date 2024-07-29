package com.tfsla.diario.webservices.core.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import net.sf.json.JSONObject;

import com.tfsla.diario.friendlyTags.TfsUserListTag;
import com.tfsla.diario.webservices.common.ServiceHelper;
import com.tfsla.diario.webservices.common.ServiceType;
import com.tfsla.diario.webservices.core.GuestSessionManager;

/**
 * Represents a Web Service which lists data (such as news, users, etc.)
 */
public abstract class TfsListWebService extends TfsWebService {

	public TfsListWebService(PageContext context, HttpServletRequest request, HttpServletResponse response) throws Throwable {
		super(GuestSessionManager.checkForGuestSession(request, response, context));
	}
	
	public TfsListWebService(HttpServletRequest request) throws Throwable {
		super(GuestSessionManager.checkForGuestSession(request, null, null));
	}

	protected abstract ServiceType getServiceType();
	
	protected Map<String,Object> getFilters() {
		Map<String,Object> filters = new HashMap<String,Object>();
		int paramsCount = 2;
		for(String param : ServiceHelper.getFilterParameters(this.getServiceType())) {
			String value = request.getParameter(param);
			if(value != null && !value.equals("")) {
				filters.put(param, value);
				paramsCount++;
			} else {
				filters.put(param, null);
			}
		}
		try {
			filters.put(TfsUserListTag.param_size, Integer.parseInt(request.getParameter(TfsUserListTag.param_size)));
		} catch(Exception e) {
			filters.put(TfsUserListTag.param_size, 10);
		}
		try {
			filters.put(TfsUserListTag.param_page, Integer.parseInt(request.getParameter(TfsUserListTag.param_page)));
		} catch(Exception e) {
			filters.put(TfsUserListTag.param_page, 1);
		}
		filters.put(TfsUserListTag.param_numberOfParamters, paramsCount);
		return filters;
	}
	
	protected void removeNonRequestedEntries(JSONObject jsonObject) {
		String [] fieldsArray = this.getRequestedFields();
		if(fieldsArray == null) return;
		
		ArrayList<String> keysToRemove = new ArrayList<String>();
		Boolean remove = true;
		for(Object key : jsonObject.keySet()) {
			remove = true;
			for(String field : fieldsArray) {
				if(field.equals(key.toString())) remove = false;
			}
			if(remove) keysToRemove.add(key.toString());
		}
		for(String key : keysToRemove) {
			jsonObject.remove(key);
		}
	}
	
	protected String[] getRequestedFields() {
		if(requestedFields == null) {
			String fields = request.getParameter("fields");
			if(fields == null || fields.equals("")) return null;
			
			this.requestedFields = fields.split(",");
		}
		return requestedFields;
	}
	
	protected String[] requestedFields;
}
