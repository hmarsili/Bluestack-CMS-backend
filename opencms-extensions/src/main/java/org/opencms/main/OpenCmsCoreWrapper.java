package org.opencms.main;

import javax.servlet.http.HttpServletRequest;
import org.opencms.file.CmsObject;

public class OpenCmsCoreWrapper {

	public static CmsObject getCmsObject(HttpServletRequest req)
	{
		CmsObject cms = null;
		
		try {
			cms = OpenCmsCore.getInstance().initCmsObjectFromSession(req);
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return cms;
	}
}
