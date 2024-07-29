package com.tfsla.statistics.service;


import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessages;

import com.tfsla.statistics.model.TfsHitPage;
import com.tfsla.statistics.model.TfsKeyValue;

public interface I_statisticsDataCollector {
    String A_CLASS = "class";

	public TfsKeyValue[] collect(CmsObject cms, CmsResource res, CmsUser user, String sessionId, TfsHitPage page) throws Exception;
	
	public String getContentType();
	
	public String getContentName();
	
	public String getValue(CmsObject cms, String uid, String key) throws Exception;

	public String getDateValue(CmsMessages msg, CmsObject cms, String uid, String key) throws Exception;

}
