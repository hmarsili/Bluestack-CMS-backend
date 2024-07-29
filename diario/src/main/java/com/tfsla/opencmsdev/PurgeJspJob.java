package com.tfsla.opencmsdev;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexCache;
import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.I_CmsScheduledJob;

public class PurgeJspJob implements I_CmsScheduledJob {

	public String launch(CmsObject arg0, Map arg1) throws Exception {
		OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_FLEX_PURGE_JSP_REPOSITORY, new HashMap(0)));
		OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR, Collections.singletonMap("action",
			new Integer(CmsFlexCache.CLEAR_ENTRIES))));
		return this.getClass().getName() + " end ";
	}

}
