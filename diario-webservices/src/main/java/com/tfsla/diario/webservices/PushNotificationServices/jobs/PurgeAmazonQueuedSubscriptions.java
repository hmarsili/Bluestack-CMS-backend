package com.tfsla.diario.webservices.PushNotificationServices.jobs;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.scheduler.I_CmsScheduledJob;


import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.DeleteEndpointRequest;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;
import software.amazon.awssdk.services.sns.model.UnsubscribeRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import net.sf.json.JSONObject;

public class PurgeAmazonQueuedSubscriptions implements I_CmsScheduledJob {

	@SuppressWarnings("rawtypes")
	@Override
	public String launch(CmsObject cms, Map parameters) throws Exception {
		String amzPushTopicArn = parameters.get("topicArn").toString();
		String amzAccessID = parameters.get("accessID").toString();
		String amzAccessKey = parameters.get("accessKey").toString();
		String amzRegion = parameters.get("region").toString();
		String queueURL = parameters.get("queueUrl").toString();
		
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(amzAccessID, amzAccessKey);

		SnsClient snsClient = SnsClient.builder()
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.region(Region.of(amzRegion))
				.build();
		
		SqsClient sqsClient = SqsClient.builder()
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.region(Region.of(amzRegion))
				.build();
		
		int count = 0;
		int skipped = 0;
		int errors = 0;
		ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
				.queueUrl(queueURL)
				.maxNumberOfMessages(10)
				.build();
		List<Message> messages = null;
		DeleteEndpointRequest deleteReq = null;
		do {
			// Get messages, wait and retry a few times if it fails
			messages = this.getMessages(sqsClient, receiveMessageRequest);
			String endpointArn = "";
			for (Message message : messages) {
				endpointArn = "";
				try {
					count++;
					String body = message.body();
					JSONObject jsonRequest = JSONObject.fromObject(body);
					String topicArn = amzPushTopicArn;
					if (jsonRequest.containsKey("TopicArn")) {
						topicArn = jsonRequest.getString("TopicArn");
					}
					if (jsonRequest.containsKey("Message")) {
						jsonRequest = JSONObject.fromObject(jsonRequest.getString("Message"));
					}
					String eventTypeString = jsonRequest.containsKey("EventType") ? jsonRequest.getString("EventType") : jsonRequest.getString("notificationType");
					if (eventTypeString == null || !eventTypeString.equals("DeliveryFailure")) {
						LOG.info("Invalid event type: " + eventTypeString);
						skipped++;
						continue;
					}
					
					endpointArn = jsonRequest.getString("EndpointArn");
				
					// Get endpoint subscription
					SubscribeRequest subscribeRequest = SubscribeRequest.builder()
						.topicArn(topicArn)
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
				} catch (Exception e) {
					LOG.error("Error removing endpoint ARN" + endpointArn, e);
					errors++;
				} finally {
					if (!endpointArn.equals("")) {
						try {
							// Delete endpoint
							deleteReq = DeleteEndpointRequest.builder()
									.endpointArn(endpointArn)
									.build();
							
							snsClient.deleteEndpoint(deleteReq);
						} catch(Exception e) {
							LOG.error("Error on fallback removing endpoint ARN" + endpointArn, e);
						}
					}
				}
				
				String messageReceiptHandle = message.receiptHandle();
				sqsClient.deleteMessage(
						DeleteMessageRequest.builder()
							.queueUrl(queueURL)
							.receiptHandle(messageReceiptHandle)
							.build());
			}
		} while (messages != null && messages.size() > 0);
		
		return "Processed " + count + " messages - " + skipped + " skipped - " + errors +  " errors, from invalid tokens queue";
	}

	private List<Message> getMessages(SqsClient sqsClient, ReceiveMessageRequest receiveMessageRequest) throws Exception {
		List<Message> messages = null;
		int attempts = 0;
		do {
			try {
				messages = sqsClient.receiveMessage(receiveMessageRequest).messages();
				return messages;
			} catch(SdkClientException e) {
				attempts++;
				LOG.error("Error getting SQS messages, attempt " + attempts, e);
				Thread.sleep(5000);
			}
		} while (messages == null && attempts < 5);
		String message = String.format("Cannot get messages from SQS after %s attempts, will exit the job now", attempts);
		LOG.error(message);
		throw new Exception(message);
	}
	
	protected Log LOG = CmsLog.getLog(this.getClass());
}
