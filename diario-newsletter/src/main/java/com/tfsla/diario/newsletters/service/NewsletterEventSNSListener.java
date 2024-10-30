package com.tfsla.diario.newsletters.service;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.diario.newsletters.common.INewsletterEventsService;
import com.tfsla.diario.newsletters.common.INewslettersService;
import com.tfsla.diario.newsletters.common.NewsletterEvent;
import com.tfsla.diario.newsletters.common.NewsletterEventType;
import com.tfsla.diario.webservices.common.ServiceHelper;

import net.sf.json.JSONObject;

public class NewsletterEventSNSListener {
	
	public NewsletterEventSNSListener(int newsletterID, PageContext context, HttpServletRequest request, HttpServletResponse response) {
		this.context = context;
		this.request = request;
		this.response = response;
		this.newsletterID = newsletterID;
	}
	
	public void processEvent() throws IOException {
		request.setAttribute("Accept", "*");
		response.setHeader("Accept", "*");
		
		//Will receive JSON messages from Amazon for either confirm the subscription
		//or with SES email events (send, open, bounce, complaint, etc.)
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

		//This header MUST be specified for a SNS message, if not this is not Amazon calling us...
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
		
		LOG.debug("SNS Listener / Request received from newsletter " + newsletterID + " - " + jsonRequest.toString());
		try {
			//Will save the event on the database, firstly process the Message
			jsonRequest = JSONObject.fromObject(jsonRequest.getString("Message"));
			
			//Actual Event Type (send, bounce, click, etc.)
			String eventTypeString = jsonRequest.containsKey("eventType") ? jsonRequest.getString("eventType") : jsonRequest.getString("notificationType");
			LOG.info("Event type: " + eventTypeString);
			NewsletterEventType eventType = NewsletterEventType.getFromString(eventTypeString);
			
			String customData = this.getCustomData(jsonRequest, eventType);
			String from = jsonRequest.getJSONObject("mail").getString("source");
			String destination = jsonRequest.getJSONObject("mail").getJSONArray("destination").get(0).toString();
			NewsletterEvent newsletterEvent = new NewsletterEvent();
			newsletterEvent.setElement(customData);
			newsletterEvent.setEventData(stringRequest);
			newsletterEvent.setEventType(eventType);
			newsletterEvent.setFromEmail(from);
			newsletterEvent.setToEmail(destination);
			newsletterEvent.setNewsletterID(newsletterID);
			
			INewsletterEventsService service = NewsletterServiceContainer.getInstance(INewsletterEventsService.class);
			service.addEvent(newsletterEvent);
			
			service.addEventSummary(newsletterEvent);
			LOG.debug("SNS Event stored successfully");
			
			if (eventType == NewsletterEventType.BOUNCE) {
				INewslettersService svc = NewsletterServiceContainer.getInstance(INewslettersService.class);
				svc.unsubscribeEmail(destination);
				LOG.info("BOUNCE received - Unsubscribed email " + destination);
			}
		} catch(Exception e) {
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
	
	private String getCustomData(JSONObject jsonRequest, NewsletterEventType eventType) {
		if(eventType == NewsletterEventType.COMPLAINT) {
			return jsonRequest.getJSONObject("complaint").getString("complaintFeedbackType");
		}
		if(eventType == NewsletterEventType.REJECT) {
			return jsonRequest.getJSONObject("reject").getString("reason");
		}
		if(eventType == NewsletterEventType.OPEN) {
			return jsonRequest.getJSONObject("open").getString("userAgent");
		}
		if(eventType == NewsletterEventType.CLICK) {
			return jsonRequest.getJSONObject("click").getString("link");
		}
		return "";
	}
	
	protected int newsletterID;
	protected PageContext context;
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected Log LOG = CmsLog.getLog(this.getClass());
}
