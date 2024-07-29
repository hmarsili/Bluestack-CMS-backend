package com.tfsla.event;

import org.opencms.main.I_CmsEventListener;

public interface I_TfsEventListener extends I_CmsEventListener {

	int EVENT_COMMENT_NEW = 1001;
	
	int EVENT_COMMENT_REJECTED = 1002;
	
	int EVENT_COMMENT_ACEPTED = 1003;
	
	int EVENT_COMMENT_REVISION = 1004;
	
	int EVENT_COMMENT_REPORTED = 1005;

	int EVENT_POST_REJECTED = 1006;
	
	int EVENT_POST_ACEPTED = 1007;
	
	int EVENT_POST_REVISION = 1008;
	
	int EVENT_POST_REPORTED = 1009;

	int EVENT_CDN_PURGE_ERROR = 1010;
	
	String KEY_COMMENT = "comment";

	String KEY_ABUSETYPE = "abuseType";
	
	String KEY_USERMESSAGE = "userMessage";

	String KEY_PURGE_ID = "purgeCdnId";
	
	String KEY_ERROR_LIST = "errorList";
	
	String KEY_ERROR_MESSAGE = "errorMsg";
	
}
