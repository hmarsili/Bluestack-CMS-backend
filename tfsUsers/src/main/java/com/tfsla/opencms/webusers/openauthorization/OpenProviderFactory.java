package com.tfsla.opencms.webusers.openauthorization;

import jakarta.servlet.http.HttpServletRequest;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.services.openCmsService;

public class OpenProviderFactory {
	
	public static IOpenProvider GetInstance(HttpServletRequest request, String providerName) throws Exception {
		try {
			if("facebook".equals(providerName))
				return new FacebookProvider(request);
			else if("googlePlus".equals(providerName))
				return new GooglePlusProvider(request);
			else if("google".equals(providerName))
				return new GoogleProvider(request);
			else if("linkedin".equals(providerName))
				return new LinkedInProvider(request);
			else if("twitter".equals(providerName))
				return new TwitterProvider(request);
			else if("yahoo".equals(providerName))
				return new YahooProvider(request);
			else
				return null;
		} catch(Exception ex) {
			throw ex;
		}		
	}
	
	public static String getSiteName(HttpServletRequest request) {
        CmsFlexController controller = CmsFlexController.getController(request);
        CmsObject cms = controller.getCmsObject();
        
        String siteName = "";
        
        if (OpenCms.getSiteManager().getSites().size() > 1)
        	siteName = openCmsService.getCurrentSite(cms) + ".";
        
        return siteName;		
	}
}
