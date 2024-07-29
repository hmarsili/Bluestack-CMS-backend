package com.tfsla.diario.webservices;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import com.tfsla.diario.webservices.common.interfaces.ISwitchSiteService;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.core.services.TfsWebService;
import com.tfsla.diario.webservices.helpers.PublicationHelper;

public class SwitchSiteService extends TfsWebService implements ISwitchSiteService {

	public SwitchSiteService(HttpServletRequest request) throws Throwable {
		super(request);
	}

	@Override
	protected JSON doExecute() throws Throwable {
		String site = this.assertRequestParameter(StringConstants.SITE);
		String proyecto = site.split("/")[2];
		String publication = PublicationHelper.getCurrentPublication(proyecto);

		this.session.setPublication(publication);
		this.session.setSite(site);
		
		this.cms.getRequestContext().setSiteRoot(site);
		
		JSONObject jsonResponse = new JSONObject();
		jsonResponse.put(StringConstants.STATUS, StringConstants.OK);
		jsonResponse.put(StringConstants.SITE, this.cms.getRequestContext().getSiteRoot());
		return jsonResponse;
	}
}
