package com.tfsla.diario.webservices.core;

import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CPMPublicationConfig;
import org.opencms.configuration.CPMSiteConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.db.CmsDefaultUsers;
import org.opencms.file.CmsObject;
import org.opencms.jsp.CmsJspLoginBean;
import org.opencms.main.OpenCms;

import com.tfsla.diario.webservices.common.ServiceHelper;
import com.tfsla.diario.webservices.common.Token;
import com.tfsla.diario.webservices.common.WebServicesConfiguration;
import com.tfsla.diario.webservices.common.WebSession;
import com.tfsla.diario.webservices.common.exceptions.InvalidTokenException;
import com.tfsla.diario.webservices.helpers.PublicationHelper;

public class GuestSessionManager {
	
	public static synchronized HttpServletRequest checkForGuestSession(final HttpServletRequest request) throws Throwable {
		return checkForGuestSession(request, null, null);
	}
	
	public static synchronized HttpServletRequest checkForGuestSession(final HttpServletRequest request, final HttpServletResponse response, final PageContext pageContext) throws Throwable {
		final String token = request.getParameter("token");
		WebServicesConfiguration config = null ;
		
		if (token == null) throw new InvalidTokenException();
		
		try {
			WebSession webSessionToken = SessionManager.getSession(token);
			
			if(webSessionToken != null) {
				config = ServiceHelper.getWSConfiguration(webSessionToken.getSite(),webSessionToken.getPublication());
				if (token.equals(config.getGuestToken())) {
					request.setAttribute("allow_guest", "true");
					request.setAttribute("site", webSessionToken.getSite());
					request.setAttribute("publication", webSessionToken.getPublication());
				}
				return request;
			}
		} catch(Throwable e) {
			//pregunto si ese token corresponde a la pub que viene por request.
			String site  		= (request.getParameter("site") != null) ?  request.getParameter("site")  : "";
			String publication 	= (request.getParameter("publication") != null) ?  request.getParameter("publication")  : "";
			Boolean crearToken 	= false;
					
			if (!site.equals("") && !publication.equals("")) {
				config = ServiceHelper.getWSConfiguration(site,publication);
				if(token.equals(config.getGuestToken())) {
					crearToken = true;
				}else {
					throw new InvalidTokenException("The token doesn't existe for the selected site and publication");
				}
			} else { 
				// busco ese token para la pub de contexto. 
				final CmsObject cmsObject = OpenCms.initCmsObject(new CmsDefaultUsers().getUserGuest());

				String currSiteRoot = cmsObject.getRequestContext().getSiteRoot();
				String currPublication = PublicationHelper.getCurrentPublication(currSiteRoot);
				
				if (!currSiteRoot.equals("") && !currPublication.equals("")) {
					config = ServiceHelper.getWSConfiguration(site,publication);
					if(token.equals(config.getGuestToken())) {
						crearToken = true;
						site =  currSiteRoot;
						publication = currPublication;
					}else {
						throw new InvalidTokenException("The token doesn't existe for the site and publication");
					}
				} else {
					// busco ese token en el cmsmedios.xml 
					CPMConfig cmsMedios = CmsMedios.getInstance().getCmsParaMediosConfiguration();

					CPMSiteConfig[] sites = cmsMedios.getSites();
					for (CPMSiteConfig st : sites){
						CPMPublicationConfig[] listadoPubs = st.getPublications();
						for (CPMPublicationConfig pub : listadoPubs){					
							if (token.equals(cmsMedios.getParam(st.getName(), pub.getName(),  "webservices","guestToken","false"))) {
								site =  st.getName();
								publication = pub.getName();
								crearToken = true;
								break;
							}
						}
						if (crearToken) break;
					}
				}
			}
			
			// si todavia no tenemos el sitio y publicación busco a nivel general.
			if (!crearToken && site.equals("") && publication.equals("")) {
				config = ServiceHelper.getWSConfiguration();
				if(token.equals(config.getGuestToken())) {
					site =  config.getDefaultSite();
					publication = config.getDefaultPublication();
					crearToken = true;
				}
			}
				
			if (crearToken) {
				final CmsObject cmsObject = OpenCms.initCmsObject(new CmsDefaultUsers().getUserGuest());
				final String siteWebSession = site;
				final String publicationWebSession = publication;
				
				request.setAttribute("allow_guest", "true");
				request.setAttribute("site", siteWebSession);
				request.setAttribute("publication", publicationWebSession);
				
				final CmsJspLoginBean loginBean = new CmsJspLoginBean(pageContext, request, response);
				
				Token guestToken = new Token() {{
					setValue(token);
					setDuration(Integer.MAX_VALUE);
				}};
				WebSession webSession = new WebSession() {{
					setLoginBean(loginBean);
					setContext(pageContext);
					setRequest(request);
					setResponse(response);
					setSite(siteWebSession);
					setPublication(publicationWebSession);
					setCmsObject(cmsObject);
					setExpirationDate(new Date(Integer.MAX_VALUE));
				}};
				
				SessionManager.saveSession(guestToken, webSession);
				
				return request;
			} else {
				throw new InvalidTokenException();
			}
		}
	
		throw new InvalidTokenException();
		
	}
}
