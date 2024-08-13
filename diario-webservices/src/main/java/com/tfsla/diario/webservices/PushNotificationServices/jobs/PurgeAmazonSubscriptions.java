package com.tfsla.diario.webservices.PushNotificationServices.jobs;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.scheduler.I_CmsScheduledJob;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.DeleteEndpointRequest;
import software.amazon.awssdk.services.sns.model.GetEndpointAttributesRequest;
import software.amazon.awssdk.services.sns.model.GetEndpointAttributesResponse;
import software.amazon.awssdk.services.sns.model.ListSubscriptionsByTopicRequest;
import software.amazon.awssdk.services.sns.model.ListSubscriptionsByTopicResponse;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;
import software.amazon.awssdk.services.sns.model.Subscription;
import software.amazon.awssdk.services.sns.model.UnsubscribeRequest;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

public class PurgeAmazonSubscriptions implements I_CmsScheduledJob {
	
	@SuppressWarnings("rawtypes")
	@Override
	public String launch(CmsObject cms, Map parameters) throws Exception {
		String amzPushTopicArn = parameters.get("topicArn").toString();
		String amzAccessID = parameters.get("accessID").toString();
		String amzAccessKey = parameters.get("accessKey").toString();
		String amzRegion = parameters.get("region").toString();
		
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(amzAccessID, amzAccessKey);

		SnsClient snsClient = SnsClient.builder()
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.region(Region.of(amzRegion))
				.build();
		
		LOG.info("Starting amazon push subscriptions purge");
		
		DeleteEndpointRequest deleteReq = null;
		int count = 0;
		int disabled = 0;
		int orphan = 0;
		
		// Loop through all the subscriptions and purge invalids
		ListSubscriptionsByTopicRequest listSubscriptionsByTopicRequest = ListSubscriptionsByTopicRequest.builder()
				.topicArn(amzPushTopicArn)
				.build();
		
		ListSubscriptionsByTopicResponse subscriptionsResult = null;
		do {
			// Get subscriptions chunk (max. 100 by amazon constraint)
			subscriptionsResult = snsClient.listSubscriptionsByTopic(listSubscriptionsByTopicRequest);
			for (Subscription subscription : subscriptionsResult.subscriptions()) {
				count++;
				String endpointArn = subscription.endpoint();
				try {
					// Check if the endpoint is valid
					GetEndpointAttributesRequest request = GetEndpointAttributesRequest.builder()
							.endpointArn(endpointArn)
							.build();
					GetEndpointAttributesResponse attrs = snsClient.getEndpointAttributes(request);
					if (attrs.attributes().get("Enabled") != null && attrs.attributes().get("Enabled").toLowerCase().equals("false")) {
						// The endpoint is disabled, remove it with the subscription
						disabled++;
						
						// Get endpoint subscription
						SubscribeRequest subscribeRequest = SubscribeRequest.builder()
							.topicArn(amzPushTopicArn)
							.endpoint(endpointArn)
							.protocol("Application")
							.build();
							
						SubscribeResponse subscribeResult = snsClient.subscribe(subscribeRequest);
						
						if (subscribeResult != null && subscribeResult.subscriptionArn() != null) {
							// If there is a subscription, remove it
							UnsubscribeRequest unsubscribeReq = UnsubscribeRequest.builder()
								.subscriptionArn(subscribeResult.subscriptionArn())
								.build();
							
							snsClient.unsubscribe(unsubscribeReq);
						}
						
						// Delete endpoint
						deleteReq = DeleteEndpointRequest.builder()
								.endpointArn(endpointArn)
								.build();
						
						snsClient.deleteEndpoint(deleteReq);
					}
				} catch (Exception e) {
					// Invalid endpoint, delete subscription
					orphan++;
					UnsubscribeRequest unsubscribeReq = UnsubscribeRequest.builder()
							.subscriptionArn(subscription.subscriptionArn())
							.build();
					
					snsClient.unsubscribe(unsubscribeReq);
				}
			}
			// Set the request to get the next subscriptions chunk
			listSubscriptionsByTopicRequest = ListSubscriptionsByTopicRequest.builder()
					.topicArn(amzPushTopicArn)
					.nextToken(subscriptionsResult.nextToken())
					.build();
			
			LOG.debug("Processing next subscriptions chunk, now starting from " + (count+1) + " - removed so far: " + disabled);
		} while (subscriptionsResult.nextToken() != null);
		
		LOG.info("Amazon push subscriptions purge finished, " + count + " subscriptions processed, " + disabled + " removed, " + orphan + " orphan subscriptions");
		return "Amazon push subscriptions purge finished, " + count + " subscriptions processed, " + disabled + " removed, " + orphan + " orphan subscriptions";
	}
	
	private Log LOG = CmsLog.getLog(this);
}
