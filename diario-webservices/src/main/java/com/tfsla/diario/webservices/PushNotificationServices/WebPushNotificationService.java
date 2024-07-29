package com.tfsla.diario.webservices.PushNotificationServices;

import com.tfsla.diario.webservices.common.strings.StringConstants;

public class WebPushNotificationService extends
		AndroidPushNotificationService {

	@Override
	protected String getPlatform() {
		return StringConstants.PLATFORM_WEB;
	}
}
