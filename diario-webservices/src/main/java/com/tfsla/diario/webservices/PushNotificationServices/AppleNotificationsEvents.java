package com.tfsla.diario.webservices.PushNotificationServices;

import org.apache.commons.logging.Log;

import com.notnoop.apns.ApnsDelegate;
import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.DeliveryError;
import com.notnoop.apns.internal.Utilities;

public class AppleNotificationsEvents implements ApnsDelegate {

	public AppleNotificationsEvents(Log LOG) {
		this.LOG = LOG;
	}
	
	@Override
	public void connectionClosed(DeliveryError error, int arg1) {
		if(error == DeliveryError.NONE || error == DeliveryError.NO_ERROR) return;
		LOG.error(
			String.format(
				"An error has occurred when trying to close the connection, name: '%s', code: %s",
				error.name(),
				error.code()
			)
		);
	}

	@Override
	public void messageSendFailed(ApnsNotification notification, Throwable exception) {
		LOG.error(
			String.format("Error when trying to push a message to Apple device '%s'", new String(notification.getDeviceToken())),
			exception
		);
	}

	@Override
	public void messageSent(ApnsNotification notification) {
		String token = Utilities.encodeHex(notification.getDeviceToken());
		LOG.debug(String.format("Sending message to Apple device '%s', Identifier: %s",
			token,
			notification.getIdentifier())
		);
	}

	protected Log LOG;
}
