package com.tfsla.diario.webservices;

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.opencms.jsp.CmsJspLoginBean;

import com.tfsla.diario.webservices.common.*;
import com.tfsla.diario.webservices.common.exceptions.*;
import com.tfsla.diario.webservices.common.interfaces.IAuthorizationService;
import com.tfsla.diario.webservices.core.SessionManager;
import com.tfsla.diario.webservices.core.TokenGenerator;
import com.tfsla.diario.webservices.helpers.PublicationHelper;

public class AuthorizationService implements IAuthorizationService {
	
	public AuthorizationService(PageContext context, HttpServletRequest request, HttpServletResponse response) {
		this(context, request, response, null, null);
	}
	
	public AuthorizationService(PageContext context, HttpServletRequest request, HttpServletResponse response, String site, String publication) {
		this.request = request;
		this.response = response;
		this.context = context;
		this.site = site;
		this.publication = publication;
	}
	
	public Token requestToken(String username, String password) throws InvalidLoginException, DisabledUserException {
		
		final CmsJspLoginBean loginBean = new CmsJspLoginBean(context, request, response);
		loginBean.login(username, password, SessionManager.PROJECT_ONLINE);
		if(!loginBean.isLoginSuccess()) {
			
            if (org.opencms.security.Messages.ERR_LOGIN_FAILED_DISABLED_2 == loginBean.getLoginException().getMessageContainer().getKey()) {
            	//User disabled.
            	
            	throw new DisabledUserException();
            }
            else
            	throw new InvalidLoginException();
		}

		if (site != null && !site.equals("/") && !site.equals("null")) {
			loginBean.getCmsObject().getRequestContext().setSiteRoot(site);			
		}
		
		//Si no vienen los parametros del sitio y publicacion 
		if (site == null || site.equals("/") || site.equals("null")) {
			site = (String)loginBean.getCmsObject().getRequestContext().currentUser().getAdditionalInfo("USERPREFERENCES_workplace-startupsettingssite");
		
			if (publication == null || publication.equals("") || publication.equals("null")) {
				publication = (String)loginBean.getCmsObject().getRequestContext().currentUser().getAdditionalInfo("USERPREFERENCES_workplace-startupsettingspublication");
			}
			
		}
		
		//Si no tiene definido el usuario en forma predetermina los datos de sitio y publicacion
		if (site == null || site.equals("/") || site.equals("null")) {
			site = loginBean.getRequestContext().getSiteRoot();
		}
		
		if (publication == null || publication.equals("") || publication.equals("null")) {
			String proyecto = site.split("/")[2];
			publication = PublicationHelper.getCurrentPublication(proyecto);
		}
			
		WebServicesConfiguration config = ServiceHelper.getWSConfiguration(site, publication);
		TokenGenerator.setTokenDuration(config.getTokensDuration());
		Token token = TokenGenerator.getToken();
		final Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, token.getIntDuration());
		
		final String siteWebSession = site;
		final String publicationWebSession = publication;
		
		request.setAttribute("site", siteWebSession);
		request.setAttribute("publication", publicationWebSession);
		
		WebSession webSession = new WebSession() {{
			setLoginBean(loginBean);
			setContext(context);
			setRequest(request);
			setResponse(response);
			setSite(siteWebSession);
			setPublication(publicationWebSession);
			setCmsObject(loginBean.getCmsObject());
			setExpirationDate(calendar.getTime());
		}};
		SessionManager.saveSession(token, webSession);
		return token;
	}
	
	public WebSession checkToken(String token) throws TokenExpiredException, InvalidTokenException {
		return SessionManager.getSession(token);
	}
	
	private String site;
	private String publication;
	private PageContext context;
	private HttpServletRequest request;
	private HttpServletResponse response;
}
