package com.tfsla.diario.ediciones.services;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
//import org.opencms.site.CmsSiteManager;

public class OpenCmsBaseService {
	
	public static String getSiteName(String siteRoot)
	{
		String siteName = siteRoot;
		
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
	
}
