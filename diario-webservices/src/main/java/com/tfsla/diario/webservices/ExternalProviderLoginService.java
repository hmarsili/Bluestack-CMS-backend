package com.tfsla.diario.webservices;

import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.diario.webservices.common.Token;
import com.tfsla.diario.webservices.common.WebSession;
import com.tfsla.diario.webservices.common.interfaces.IExternalProviderLoginService;
import com.tfsla.diario.webservices.core.SessionManager;
import com.tfsla.diario.webservices.core.TokenGenerator;
import com.tfsla.opencms.webusers.externalLoginProvider.FacebookLoginProvider;
import com.tfsla.opencms.webusers.externalLoginProvider.GoogleLoginProvider;

import net.sf.json.JSONObject;

public class ExternalProviderLoginService implements IExternalProviderLoginService {
	public ExternalProviderLoginService(PageContext context, HttpServletRequest request, HttpServletResponse response) {
		this(context, request, response, null, null);
	}
	
	public ExternalProviderLoginService(PageContext context, HttpServletRequest request, HttpServletResponse response, String site, String publication) {
		this.request = request;
		this.response = response;
		this.context = context;
		this.siteName = site;
		this.publication = publication;
		//this.logger = CmsLog.getLog(this);
	}
	
	public Token loginAndRegister() throws Throwable {
		
		Token token=null;
		FacebookLoginProvider fProvider = new FacebookLoginProvider(context, request, response);
		if (publication!=null && siteName!=null)
			fProvider.setPublication(siteName, Integer.parseInt(publication));
		else {
			fProvider.setPublicationFromRequest();
		}
		if (fProvider.hasFacebookLoginInformation()) {
			fProvider.login();
			if (fProvider.isLoginSuccess()) {
				token = setWebSession(fProvider.getCmsObject(),fProvider.getExpirationDate());
			}
			else {
				throw fProvider.getLoginException();
			}
		}
		else {
			GoogleLoginProvider gProvider = new GoogleLoginProvider(context, request, response);
			if (publication!=null && siteName!=null)
				gProvider.setPublication(siteName, Integer.parseInt(publication));
			else {
				gProvider.setPublicationFromRequest();
			}
			gProvider.setJsonRequest(fProvider.getJsonRequest());
			if (gProvider.hasGoogleLoginInformation()) {
				
				gProvider.login();
				if (gProvider.isLoginSuccess()) {
					token=setWebSession(gProvider.getCmsObject(),gProvider.getExpirationDate());
				}
				else {
					throw gProvider.getLoginException();
				}
			}
		}
		return token;
	}
	
	protected Token setWebSession(CmsObject cms, Date expires) throws CmsException {
		
		final CmsObject cmsObject = OpenCms.initCmsObject(cms);
		final Date expirationDate = expires;
		long duration = (expires.getTime() / 1000) - (int)(new Date().getTime()) / 1000;
		Token token = TokenGenerator.getToken(duration);
		
		WebSession webSession = new WebSession() {{
			setCmsObject(cmsObject);
			setContext(context);
			setLoginBean(null);
			setRequest(request);
			setResponse(response);
			setSite(siteName);
			setPublication(publication);
			setExpirationDate(expirationDate);
		}};
		SessionManager.saveSession(token, webSession);
		return token;
	}
	
	private String siteName;
	private String publication;
	private PageContext context;
	private HttpServletRequest request;
	private HttpServletResponse response;
	
	
}
