package com.tfsla.diario.webservices.PushNotificationServices.jobs;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.scheduler.I_CmsScheduledJob;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.DeleteEndpointRequest;
import com.amazonaws.services.sns.model.GetEndpointAttributesRequest;
import com.amazonaws.services.sns.model.GetEndpointAttributesResult;
import com.amazonaws.services.sns.model.ListSubscriptionsByTopicRequest;
import com.amazonaws.services.sns.model.ListSubscriptionsByTopicResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sns.model.Subscription;
import com.amazonaws.services.sns.model.UnsubscribeRequest;

public class PurgeAmazonSubscriptions implements I_CmsScheduledJob {
	
	@SuppressWarnings("rawtypes")
	@Override
	public String launch(CmsObject cms, Map parameters) throws Exception {
		String amzPushTopicArn = parameters.get("topicArn").toString();
		String amzAccessID = parameters.get("accessID").toString();
		String amzAccessKey = parameters.get("accessKey").toString();
		String amzRegion = parameters.get("region").toString();
		
		AWSCredentials awsCreds = new BasicAWSCredentials(amzAccessID, amzAccessKey);
		AmazonSNS snsClient = AmazonSNSClient.builder()
				.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
				.withRegion(amzRegion)
				.build();
		
		LOG.info("Starting amazon push subscriptions purge");
		
		DeleteEndpointRequest deleteReq = null;
		int count = 0;
		int disabled = 0;
		int orphan = 0;
		
		// Loop through all the subscriptions and purge invalids
		ListSubscriptionsByTopicRequest listSubscriptionsByTopicRequest = new ListSubscriptionsByTopicRequest();
		listSubscriptionsByTopicRequest.setTopicArn(amzPushTopicArn);
		ListSubscriptionsByTopicResult subscriptionsResult = null;
		do {
			// Get subscriptions chunk (max. 100 by amazon constraint)
			subscriptionsResult = snsClient.listSubscriptionsByTopic(listSubscriptionsByTopicRequest);
			for (Subscription subscription : subscriptionsResult.getSubscriptions()) {
				count++;
				String endpointArn = subscription.getEndpoint();
				try {
					// Check if the endpoint is valid
					GetEndpointAttributesRequest request = new GetEndpointAttributesRequest();
					request.setEndpointArn(endpointArn);
					GetEndpointAttributesResult attrs = snsClient.getEndpointAttributes(request);
					if (attrs.getAttributes().get("Enabled") != null && attrs.getAttributes().get("Enabled").toLowerCase().equals("false")) {
						// The endpoint is disabled, remove it with the subscription
						disabled++;
						
						// Get endpoint subscription
						SubscribeRequest subscribeRequest = new SubscribeRequest();
						subscribeRequest.setTopicArn(amzPushTopicArn);
						subscribeRequest.setEndpoint(endpointArn);
						subscribeRequest.setProtocol("Application");
						SubscribeResult subscribeResult = snsClient.subscribe(subscribeRequest);
						
						if (subscribeResult != null && subscribeResult.getSubscriptionArn() != null) {
							// If there is a subscription, remove it
							UnsubscribeRequest unsubscribeReq = new UnsubscribeRequest();
							unsubscribeReq.setSubscriptionArn(subscribeResult.getSubscriptionArn());
							snsClient.unsubscribe(unsubscribeReq);
						}
						
						// Delete endpoint
						deleteReq = new DeleteEndpointRequest();
						deleteReq.setEndpointArn(endpointArn);
						snsClient.deleteEndpoint(deleteReq);
					}
				} catch (Exception e) {
					// Invalid endpoint, delete subscription
					orphan++;
					UnsubscribeRequest unsubscribeReq = new UnsubscribeRequest();
					unsubscribeReq.setSubscriptionArn(subscription.getSubscriptionArn());
					snsClient.unsubscribe(unsubscribeReq);
				}
			}
			// Set the request to get the next subscriptions chunk
			listSubscriptionsByTopicRequest.setNextToken(subscriptionsResult.getNextToken());
			LOG.debug("Processing next subscriptions chunk, now starting from " + (count+1) + " - removed so far: " + disabled);
		} while (subscriptionsResult.getNextToken() != null);
		
		LOG.info("Amazon push subscriptions purge finished, " + count + " subscriptions processed, " + disabled + " removed, " + orphan + " orphan subscriptions");
		return "Amazon push subscriptions purge finished, " + count + " subscriptions processed, " + disabled + " removed, " + orphan + " orphan subscriptions";
	}
	
	private Log LOG = CmsLog.getLog(this);
}
