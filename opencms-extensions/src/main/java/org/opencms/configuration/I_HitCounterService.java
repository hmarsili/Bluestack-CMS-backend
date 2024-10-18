package org.opencms.configuration;

import jakarta.servlet.http.HttpSession;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

public interface I_HitCounterService {
	void countHitView(CmsResource res,CmsObject cms, HttpSession session);
}
