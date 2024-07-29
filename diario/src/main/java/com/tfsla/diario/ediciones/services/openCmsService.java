package com.tfsla.diario.ediciones.services;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.data.ProjectDAO;
import com.tfsla.diario.ediciones.model.TipoEdicion;

public class openCmsService {

	public static String getSiteName(String siteRoot)
	{
		String siteName = siteRoot;
		
		if (siteName.equals(""))
			return "";
		
		if(siteName.substring(siteRoot.length() - 1, siteName.length()).equals("/"))
			siteName = siteRoot.substring(0, siteRoot.length() - 1);
		
		siteName = siteName.replaceAll("/sites/", "");

		return siteName;
	}	

	public static String getCurrentSite(CmsObject obj)
	{
		String siteName = OpenCms.getSiteManager().getCurrentSite(obj).getSiteRoot();
		
		if(siteName.substring(siteName.length() - 1, siteName.length()).equals("/"))
			siteName = siteName.substring(0, siteName.length() - 1);
		
		siteName = siteName.replaceAll("/sites/", "");

		return siteName;
	}
	
	public static int getCurrentSiteId(CmsObject obj)
	{
		String siteName = getCurrentSite(obj);
		ProjectDAO pDAO = new ProjectDAO();
		try {
			return pDAO.getProjectByName(siteName).getIdProject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public static String getPublicationName(CmsObject cms, String path){
		try {
			TipoEdicionService tService = new TipoEdicionService();
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, path);
			return tEdicion.getNombre();
		}
		catch(Exception ex)
		{
			return "online";
		}	
	}
	
	public static String getPublicationNameFromContext(CmsObject cms){
		return getPublicationName(cms, cms.getRequestContext().getUri());
	}
}
