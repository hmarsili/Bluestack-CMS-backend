package com.tfsla.diario.webservices.PushNotificationServices;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.diario.webservices.common.ServiceHelper;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.data.PushClientDAO;

import net.sf.json.JSONObject;

public class PushEventSNSListener {

	public PushEventSNSListener(PageContext context, HttpServletRequest request, HttpServletResponse response, String site, String publication) {
		this.context = context;
		this.request = request;
		this.response = response;
		this.site = site;
		this.publication = publication;
	}
	
	public void processEvent() throws IOException {
		request.setAttribute("Accept", "*");
		response.setHeader("Accept", "*");
		String stringRequest = ServiceHelper.getRequestAsString(request);
		if (stringRequest.equals("")) {
			LOG.info("Empty request");
			return;
		}
		JSONObject jsonRequest = null;
		try {
			jsonRequest = JSONObject.fromObject(stringRequest);
		} catch(Exception e) {
			LOG.error("Invalid JSON request", e);
			return;
		}
		
		String msgType = request.getHeader("x-amz-sns-message-type");
		if(msgType == null) return;
		
		if(msgType.equals("SubscriptionConfirmation")) {
			//The very first call is to confirm the SNS subscription for this endpoint
			//It will provide a URL with a confirmation URL, need to hit it.
			//In case of any error, look for the URL on the event log and hit it by browsing it
			try {
				this.handleSubscription(jsonRequest);
			} catch (Exception e) {
				LOG.error("Error while trying to confirm subscription to SNS topic", e);
				LOG.info("Request received: " + jsonRequest.toString());
			}
			return;
		}
		
		if(!msgType.equals("Notification")) {
			//If the message is not a SubscriptionConfirmation it MUST be a Notification
			LOG.warn("SNS Listener - Received an invalid message type: " + msgType);
			return;
		}
		
		LOG.debug("Push SNS Listener / Request received - " + jsonRequest.toString());
		
		try {
			jsonRequest = JSONObject.fromObject(jsonRequest.getString("Message"));
			String eventTypeString = jsonRequest.containsKey("EventType") ? jsonRequest.getString("EventType") : jsonRequest.getString("notificationType");
			LOG.info("Event type: " + eventTypeString);
			
			// Must be DeliveryFailure
			if (eventTypeString == null || !eventTypeString.equals("DeliveryFailure")) {
				LOG.warn("Not processing event type, exiting");
				return;
			}
			
			String endpointArn = jsonRequest.getString("EndpointArn");
			AmazonSNSConnector snsConnector = new AmazonSNSConnector(site, publication);
			snsConnector.removePushSubscriber(endpointArn);
			
			PushClientDAO dao = new PushClientDAO();
			try {
				String token = snsConnector.getEndpointToken(endpointArn);
				dao.openConnection();
				if (token == null) {
					dao.unregisterEndpoint(endpointArn);
				} else {
					dao.unregisterClient(token, StringConstants.PLATFORM_WEB);
				}
			} catch(Exception ex) {
				LOG.error("Error removing token", ex);
				ex.printStackTrace();
				throw ex;
			} finally {
				dao.closeConnection();
			}
		} catch (Throwable e) {
			LOG.error("Error processing SNS message", e);
			LOG.info(stringRequest);
		}
	}
	
	private void handleSubscription(JSONObject jsonRequest) throws HttpException, IOException {
		String url = jsonRequest.get("SubscribeURL").toString();
		if (url == null || url.equals("")) {
			LOG.warn("The URL provided for confirming SES/SNS subscription is empty, cannot confirm the subscription");
			LOG.info("Request received: " + jsonRequest.toString());
			return;
		}
		LOG.info("Trying to subscribe to SNS using " + url);
		GetMethod method = new GetMethod(url);
		method.setFollowRedirects(true);
		HttpClient client = new HttpClient();
		client.executeMethod(method);
		LOG.info("Successfully subscribed to SNS, response: " + method.getResponseBodyAsString());
		method.releaseConnection();
	}
	
	protected String site;
	protected String publication;
	protected PageContext context;
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected Log LOG = CmsLog.getLog(this.getClass());
}
