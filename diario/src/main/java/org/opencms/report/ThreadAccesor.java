package org.opencms.report;

import org.opencms.file.CmsObject;

public class ThreadAccesor {

	public static CmsObject getCmsObject(A_CmsReportThread thread) {
		return thread.getCms();
	}
	
}
