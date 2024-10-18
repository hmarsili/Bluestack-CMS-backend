package com.tfsla.diario.webservices.core.services;

import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.db.CmsDefaultUsers;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;

import com.tfsla.diario.ediciones.services.PublicationService;
import com.tfsla.diario.webservices.common.ServiceHelper;
import com.tfsla.diario.webservices.common.WebServicesConfiguration;
import com.tfsla.diario.webservices.common.WebSession;
import com.tfsla.diario.webservices.common.exceptions.InvalidTokenException;
import com.tfsla.diario.webservices.common.interfaces.ITfsWebService;
import com.tfsla.diario.webservices.common.strings.ExceptionMessages;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.core.*;
import com.tfsla.diario.webservices.helpers.ParametersHelper;
import com.tfsla.diario.webservices.helpers.PublicationHelper;

public abstract class TfsWebService implements ITfsWebService {
	
	public TfsWebService(HttpServletRequest request) throws Throwable {
		this(null, request, null);
	}
	
	public TfsWebService(PageContext context, HttpServletRequest request, HttpServletResponse response) throws Throwable {
		this.request = request;
		this.token = request.getParameter(StringConstants.TOKEN);
		this.session = SessionManager.getSession(token);
		this.cms = session.getCmsObject();
		this.locale = cms.getRequestContext().getLocale();
		this.config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		this.paramsHelper = new ParametersHelper();
		this.encoding = request.getParameter(StringConstants.ENCODING) == null? StringConstants.ENCODING_ISO:request.getParameter(StringConstants.ENCODING);
		String publication = "";
		
		if(request.getAttribute(StringConstants.ALLOW_GUEST) == null || !request.getAttribute(StringConstants.ALLOW_GUEST).equals(Boolean.TRUE.toString().toLowerCase())) {
			if(this.cms.getRequestContext().currentUser().getName().equals(new CmsDefaultUsers().getUserGuest())) {
				throw new InvalidTokenException(ExceptionMessages.ERROR_ACCESS_TOKEN);
			}
		} else {
			request.removeAttribute(StringConstants.ALLOW_GUEST);
		}
		
		if(request.getAttribute(StringConstants.SITE) != null && !request.getAttribute(StringConstants.SITE).toString().equals("")) {
			this.cms.getRequestContext().setSiteRoot(request.getAttribute(StringConstants.SITE).toString());
		}
		
		try {
			publication = String.valueOf(PublicationService.getCurrentPublication(cms).getId());
		}catch(Exception ex){
			if(request.getAttribute(StringConstants.PUBLICATION) != null && !request.getAttribute(StringConstants.SITE).toString().equals("")) {
				publication = request.getAttribute(StringConstants.PUBLICATION).toString();
			}
		}
		 		
 		this.configuration = ServiceHelper.getWSConfiguration(cms.getRequestContext().getSiteRoot(), publication);
	}
	
	public Boolean isGuest() {
		if(this.token == null) return true;
		return this.token.equals(this.configuration.getGuestToken());
	}
	
	public JSON execute() throws Throwable {
		SessionManager.getSession(token);
		JSON ret = null;
		Throwable exception = null;
		try {
			ret = this.doExecute();
		} catch(Throwable ex) {
			exception = ex;
		}
		if(exception != null) {
			LOG.error(ExceptionMessages.ERROR_SERVICE_CALL, exception);
			throw exception;
		}
		return ret;
	}
	
	protected String getCurrentPublication() {
		return PublicationHelper.getCurrentPublication(cms);
	}
	
	protected String assertRequestParameter(String parameterName) throws Exception {
		return this.paramsHelper.assertRequestParameter(parameterName, request);
	}
	
	protected String assertJSONParameter(String parameterName, JSONObject jsonObject) throws Exception {
		return this.paramsHelper.assertJSONParameter(parameterName, jsonObject);
	}
	
	protected abstract JSON doExecute() throws Throwable;
	
	protected CmsObject cms;
	protected Locale locale;
	protected HttpServletRequest request;
	protected String token;
	protected WebSession session;
	protected WebServicesConfiguration configuration;
	protected CPMConfig config;
	protected Log LOG = CmsLog.getLog(this);
	protected ParametersHelper paramsHelper;
	protected String encoding;
}
