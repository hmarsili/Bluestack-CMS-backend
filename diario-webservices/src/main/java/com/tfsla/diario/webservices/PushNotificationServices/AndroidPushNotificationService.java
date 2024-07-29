package com.tfsla.diario.webservices.PushNotificationServices;

import java.util.Hashtable;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.DeleteEndpointRequest;
import com.amazonaws.services.sns.model.EndpointDisabledException;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Builder;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.tfsla.diario.webservices.common.strings.ExceptionMessages;
import com.tfsla.diario.webservices.common.strings.LogMessages;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.core.services.PushNotificationService;
import com.tfsla.diario.webservices.data.PushClientDAO;

/**
 * Manages Push Notifications with Android Apps, see http://developer.android.com/google/gcm/server.html 
 */
public class AndroidPushNotificationService extends PushNotificationService {

	@Override
	protected String getPlatform() {
		return StringConstants.PLATFORM_ANDROID;
	}

	@Override
	public void startMessageService() {
		this.sender = new Sender(
			this.config.getParam(site, publication, module, "androidApiKey")
		);
	}

	@Override
	public void stopMessageService() {
		this.sender = null;
	}

	@Override
	protected void sendMessage(String client, Hashtable<String, String> message) throws Exception {
		Message androidMessage = this.getMessage(message);
		Result result = sender.send(androidMessage, client, 3);
		
		if(result.getMessageId() == null) { //We have an error
			
			//Check if the client should be removed
			this.assertUserActive(client, result);
			
			throw new Exception(
				String.format(
					ExceptionMessages.ERROR_PUSHING_TO_ANDROID,
					result.getErrorCodeName()
				)
			);
		}
	}
	
	@Override
	protected void sendMessage(List<String> rawclients, Hashtable<String, String> message) throws Exception {
		String amzAccessID = config.getParam(site, publication, module, "amzAccessID", ""); 
		String amzAccessKey = config.getParam(site, publication, module, "amzAccessKey","");
		String amzRegion = config.getParam(site, publication, module, "amzRegion","");
		String amzGCMArn = config.getParam(site, publication, module, "amzGCMArn", "");
		if(amzRegion == null || amzRegion.equals("")) {
			amzRegion = Region.US_Standard.toString();
		}
		
		if(amzAccessKey == null || amzAccessID == null || amzAccessKey.equals("") || amzAccessID.equals("")) {
			throw new Exception(String.format("amzAccessID and amzAccessKey cannot be empty: module %s, site %s - publication %s", module, site, publication));
		}
		
		if(amzGCMArn == null || amzGCMArn.equals("")) {
			throw new Exception(String.format("amzGCMArn parameter not specified for module %s, site %s - publication %s", module, site, publication));
		}
		
		AWSCredentials awsCreds = new BasicAWSCredentials(amzAccessID, amzAccessKey);
		AmazonSNS snsClient = AmazonSNSClient.builder()
				.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
				.withRegion(amzRegion)
				.build();
		LOG.info(String.format("Amz accessID: %s, secret: %s", amzAccessID, amzAccessKey));
		for(String client : rawclients) {
			CreatePlatformEndpointResult result = null;
			try {
				CreatePlatformEndpointRequest platformEndpointRequest = new CreatePlatformEndpointRequest();
				platformEndpointRequest.setPlatformApplicationArn(amzGCMArn);
				platformEndpointRequest.setToken(client.replace("https://android.googleapis.com/gcm/send/", ""));
				result = snsClient.createPlatformEndpoint(platformEndpointRequest);
				LOG.info(String.format("Obtained ARN %s for endpoint %s", result.getEndpointArn(), client));
			} catch(Exception e) {
				LOG.info("Error trying to retrieve ARN for endpoint " + client);
				if(result != null) {
					LOG.info("Result: " + result);
				}
				LOG.error(e);
			}
			
			if(result != null) {
				PublishRequest publishRequest = new PublishRequest();
				publishRequest.setTargetArn(amzGCMArn);
				publishRequest.setMessage("-");
				publishRequest.setTargetArn(result.getEndpointArn());
				
				try {
					PublishResult publishResult = snsClient.publish(publishRequest);
					LOG.info(String.format("Published message id %s on ARN %s", publishResult.getMessageId(), client));
				} catch(EndpointDisabledException e) {
					LOG.debug(String.format(LogMessages.REMOVING_ANDROID_CLIENT, client));
					PushClientDAO dao = new PushClientDAO();
					try {
						dao.openConnection();
						dao.unregisterClient(client, this.getPlatform());
					} catch(Exception ex) {
						ex.printStackTrace();
						LOG.info("Error removing endpoint " + client + " from platform " + this.getPlatform());
						LOG.error(ex);
					} finally {
						dao.closeConnection();
					}
					if(result != null) {
						try {
							LOG.info("Removing ARN " + result.getEndpointArn());
							DeleteEndpointRequest deleteRequest = new DeleteEndpointRequest();
							deleteRequest.setEndpointArn(result.getEndpointArn());
							snsClient.deleteEndpoint(deleteRequest);
						} catch(Exception ex) {
							LOG.info("Error removing ARN " + result.getEndpointArn() + " from platform " + this.getPlatform());
							LOG.error(ex);
						}
					}
				} catch(Exception e) {
					LOG.info("Cannot publish message to ARN " + client);
					LOG.error(e);
				}
			}
		}
	}
	
	protected Message getMessage(Hashtable<String, String> message) {
		Builder builder = new Message.Builder();
		for(String key : message.keySet()) {
			builder.addData(key, message.get(key));
		}
		builder.timeToLive(60);
		return builder.build();
	}
	
	protected void assertUserActive(String client, Result result) throws Exception {
		if(result.getErrorCodeName().equals(Constants.ERROR_NOT_REGISTERED)
			|| result.getErrorCodeName().equals(Constants.ERROR_INVALID_REGISTRATION)) {
			
			LOG.debug(String.format(LogMessages.REMOVING_ANDROID_CLIENT, client));
			PushClientDAO dao = new PushClientDAO();
			try {
				dao.openConnection();
				dao.unregisterClient(client, this.getPlatform());
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				dao.closeConnection();
			}
		}
		
		throw new Exception(String.format(LogMessages.REMOVING_ANDROID_CLIENT, client));
	}
	
	private Sender sender;
}
