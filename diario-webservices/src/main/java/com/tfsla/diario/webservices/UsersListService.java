package com.tfsla.diario.webservices;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.opencms.file.CmsUser;
import org.opencms.util.CmsUUID;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.tfsla.diario.usersCollector.DataUsersCollector;
import com.tfsla.diario.webservices.common.ServiceType;
import com.tfsla.diario.webservices.common.interfaces.IUsersListService;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.core.services.*;
import com.tfsla.diario.webservices.helpers.UsersListProcessor;

public class UsersListService extends TfsListWebService implements IUsersListService {
	
	public UsersListService(PageContext context, HttpServletRequest request, HttpServletResponse response) throws Throwable {
		super(context, request, response);
	}
	
	@Override
	protected ServiceType getServiceType() {
		return ServiceType.USERS_LIST;
	}

	@Override
	protected JSON doExecute() {
		Map<String,Object> filters = getFilters();
		List<CmsUser> users = null;
		DataUsersCollector collector = new DataUsersCollector();
		
		if((filters.containsKey(StringConstants.ID) && filters.get(StringConstants.ID) != null) || (filters.containsKey(StringConstants.USERNAME) && filters.get(StringConstants.USERNAME)!=null )) {
			users = new ArrayList<CmsUser>();
			try {
				if(filters.get(StringConstants.ID) != null && !filters.get(StringConstants.ID).toString().equals("")) {
					users.add(cms.readUser(new CmsUUID(filters.get(StringConstants.ID).toString())));
				} else {
					users.add(cms.readUser(filters.get(StringConstants.USERNAME).toString()));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			users = collector.collectUsers(filters, cms, true);
		}
		
		//PROCESS USERS TO RETRIEVE
		JSONArray jsonResult = UsersListProcessor.processUsersList(users, this.cms, this.session.getSite(), this.session.getPublication());
		
		JSONObject jsonObject = null;
		if(this.getRequestedFields() != null) {
			for(int i=0; i < jsonResult.size(); i++) {
				jsonObject = (JSONObject)jsonResult.get(i);
				this.removeNonRequestedEntries(jsonObject);
			}
		}
		
		return jsonResult;
	}
}
