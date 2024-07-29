package org.opencms.main;

import javax.servlet.http.HttpServletRequest;

import org.opencms.file.CmsRequestContext;

public class OpenCmsAccess {
	public static void addSessionInfo(CmsSessionInfo sessionInfo) {
		OpenCms.getSessionManager().addSessionInfo(sessionInfo);
	}
	
	public static void updateSessionInfo(HttpServletRequest req, CmsRequestContext ctx) {
		CmsSessionInfo info = OpenCms.getSessionManager().getSessionInfo(req);
		if (info != null) info.update(ctx);
	}
}
