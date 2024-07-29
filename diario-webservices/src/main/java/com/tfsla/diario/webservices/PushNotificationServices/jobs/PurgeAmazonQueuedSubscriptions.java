package com.tfsla.diario.webservices.PushNotificationServices.jobs;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.scheduler.I_CmsScheduledJob;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.DeleteEndpointRequest;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sns.model.UnsubscribeRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

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
		
		AWSCredentials awsCreds = new BasicAWSCredentials(amzAccessID, amzAccessKey);
		AmazonSNS snsClient = AmazonSNSClient.builder()
				.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
				.withRegion(amzRegion)
				.build();
		
		AmazonSQS sqsClient = AmazonSQSClient.builder()
				.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
				.withRegion(amzRegion)
				.build();
		
		int count = 0;
		int skipped = 0;
		int errors = 0;
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueURL);
		receiveMessageRequest.setMaxNumberOfMessages(10);
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
					String body = message.getBody();
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
					SubscribeRequest subscribeRequest = new SubscribeRequest();
					subscribeRequest.setTopicArn(topicArn);
					subscribeRequest.setEndpoint(endpointArn);
					subscribeRequest.setProtocol("Application");
					SubscribeResult subscribeResult = snsClient.subscribe(subscribeRequest);
					
					if (subscribeResult != null && subscribeResult.getSubscriptionArn() != null) {
						// If there is a subscription, remove it
						UnsubscribeRequest unsubscribeReq = new UnsubscribeRequest();
						unsubscribeReq.setSubscriptionArn(subscribeResult.getSubscriptionArn());
						snsClient.unsubscribe(unsubscribeReq);
					}
				} catch (Exception e) {
					LOG.error("Error removing endpoint ARN" + endpointArn, e);
					errors++;
				} finally {
					if (!endpointArn.equals("")) {
						try {
							// Delete endpoint
							deleteReq = new DeleteEndpointRequest();
							deleteReq.setEndpointArn(endpointArn);
							snsClient.deleteEndpoint(deleteReq);
						} catch(Exception e) {
							LOG.error("Error on fallback removing endpoint ARN" + endpointArn, e);
						}
					}
				}
				
				String messageReceiptHandle = message.getReceiptHandle();
				sqsClient.deleteMessage(new DeleteMessageRequest().withQueueUrl(queueURL).withReceiptHandle(messageReceiptHandle));
			}
		} while (messages != null && messages.size() > 0);
		
		return "Processed " + count + " messages - " + skipped + " skipped - " + errors +  " errors, from invalid tokens queue";
	}

	private List<Message> getMessages(AmazonSQS sqsClient, ReceiveMessageRequest receiveMessageRequest) throws Exception {
		List<Message> messages = null;
		int attempts = 0;
		do {
			try {
				messages = sqsClient.receiveMessage(receiveMessageRequest).getMessages();
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
