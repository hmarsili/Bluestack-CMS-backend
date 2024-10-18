package com.tfsla.diario.webservices.common;

import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;

import org.opencms.file.CmsObject;
import org.opencms.jsp.CmsJspLoginBean;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionBaseService;

public class WebSession {
	public PageContext getContext() {
		return context;
	}
	public void setContext(PageContext context) {
		this.context = context;
	}
	public HttpServletRequest getRequest() {
		return request;
	}
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}
	public HttpServletResponse getResponse() {
		return response;
	}
	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}
	public CmsObject getCmsObject() {
		return cmsObject;
	}
	public void setCmsObject(CmsObject cmsObject) {
		this.cmsObject = cmsObject;
		
		if(this.site != null && this.publication != null
		 && this.site.equals("") && this.publication.equals("")) {
			
			this.publicationName = "0";
			
			TipoEdicionBaseService tService = new TipoEdicionBaseService();
		    	try {
					TipoEdicion tEdicion = tService.obtenerTipoEdicion(cmsObject, cmsObject.getRequestContext().getUri());			
					if (tEdicion!=null)
						this.publicationName = "" + tEdicion.getId();
		    	} catch (Exception e) {
					e.printStackTrace();
				}
			this.siteName = OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot();
		} else {
			this.publicationName = this.publication;
			this.siteName = this.site;
		}
	}
	public CmsJspLoginBean getLoginBean() {
		return loginBean;
	}
	public void setLoginBean(CmsJspLoginBean loginBean) {
		this.loginBean = loginBean;
	}
	public Date getExpirationDate() {
		return expirationDate;
	}
	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}
	public String getSite() {
		if(site == null || site.equals("")) return site;
		return site;
	}
	public void setSite(String site) {
		this.site = site;
	}
	public String getPublication() {
		if(publication == null || publication.equals("")) return publication;
		return publication;
	}
	public void setPublication(String publication) {
		this.publication = publication;
	}
	
	private String site;
	private String publication;
	private String siteName;
	private String publicationName;
	private Date expirationDate;
	private CmsObject cmsObject;
	private PageContext context;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private CmsJspLoginBean loginBean;
}
