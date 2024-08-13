package com.tfsla.diario.webservices.PushNotificationServices;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.main.CmsLog;

import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreatePlatformEndpointRequest;
import software.amazon.awssdk.services.sns.model.CreatePlatformEndpointResponse;
import software.amazon.awssdk.services.sns.model.DeleteEndpointRequest;
import software.amazon.awssdk.services.sns.model.GetEndpointAttributesRequest;
import software.amazon.awssdk.services.sns.model.GetEndpointAttributesResponse;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;
import software.amazon.awssdk.services.sns.model.UnsubscribeRequest;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.data.PushClientDAO;

public class AmazonSNSConnector {
	
	private CPMConfig config;
	private SnsClient snsClient;
	private String module;
	private String site;
	private String publication;
	
	public AmazonSNSConnector(String site, String publication) throws Exception {
		this.config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		this.module = PushServiceConfiguration.getModuleName();
		this.site = site;
		this.publication = publication;
		
		
		
		String amzAccessID = config.getParam(site, publication, module, "amzAccessID", ""); 
		String amzAccessKey = config.getParam(site, publication, module, "amzAccessKey","");
		String amzRegion = config.getParam(site, publication, module, "amzRegion","");
		if(amzRegion == null || amzRegion.equals("")) {
			amzRegion = Region.US_EAST_1.toString();
		}
		
		if (amzAccessKey == null || amzAccessID == null || amzAccessKey.equals("") || amzAccessID.equals("")) {
			throw new Exception(String.format("amzAccessID and amzAccessKey cannot be empty: module %s, site %s - publication %s", module, site, publication));
		}
		
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(amzAccessID, amzAccessKey);

		this.snsClient = 
				SnsClient.builder()
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.region(Region.of(amzRegion))
				.build();
	}
	
	public void addPushSubscriber(String token, String platform) throws Exception {
		addPushSubscriber(token, platform, false);
	}
	
	public void addPushSubscriber(String token, String platform, Boolean saveInDB) throws Exception {
		String amzPushTopicArn = config.getParam(site, publication, module, "amzPushTopicArn", "");
		String amzArn = this.getPlatformArn(site, publication, platform);
		if (amzArn == null || amzArn.equals("")) {
			throw new Exception(String.format("ARN not specified for module %s and platform %s, site %s - publication %s", module, platform, site, publication));
		}
		
		// Get the ARN for the application (AKA: create platform endpoint)
		CreatePlatformEndpointRequest platformEndpointRequest = CreatePlatformEndpointRequest.builder()
				.platformApplicationArn(amzArn)
				.token(token.replace("https://android.googleapis.com/gcm/send/", ""))
				.build();
		
		CreatePlatformEndpointResponse result = snsClient.createPlatformEndpoint(platformEndpointRequest);
		
		// Subscribe the endpoint ARN to the topic
		SubscribeRequest subscribeRequest = SubscribeRequest.builder()
				.topicArn(amzPushTopicArn)
				.endpoint(result.endpointArn())
				.protocol("Application")
				.build();
				
		SubscribeResponse subscribeResult = snsClient.subscribe(subscribeRequest);
		
		LOG.debug("Added push subscriber - token: " + token + ", endpoint: " + result.endpointArn());
		
		if (saveInDB) {
			PushClientDAO dao = new PushClientDAO();
			try {
				dao.openConnection();
				
				if (!dao.userExists(token, platform, subscribeResult.subscriptionArn(), site, Integer.valueOf(publication))) {
					dao.registerEndpoint(token, platform, result.endpointArn(), subscribeResult.subscriptionArn(), site, publication);
				} else {
					dao.updateEndpoint(token, platform, result.endpointArn(), subscribeResult.subscriptionArn(), site, publication);
				}
				LOG.debug("Push subscriber registered with token " + token);
			} catch (Exception e) {
				LOG.error("Error adding push subscriber", e);
				e.printStackTrace();
			} finally {
				dao.closeConnection();
			}
		}
	}
	
	public String getEndpointToken(String endpointArn) throws Exception {
		try {
			GetEndpointAttributesRequest request = GetEndpointAttributesRequest.builder()
					.endpointArn(endpointArn)
					.build();
			
			GetEndpointAttributesResponse result = snsClient.getEndpointAttributes(request);
			return result.attributes().get("Token");
		} catch (Exception e) {
			return "";
		}
	}
	
	public void removePushSubscriber(String endpointArn) throws Exception {
		String amzPushTopicArn = config.getParam(site, publication, module, "amzPushTopicArn", "");
		
		try {
			// Get topic subscription
			SubscribeRequest subscribeRequest = 
					SubscribeRequest.builder()
						.topicArn(amzPushTopicArn)
						.endpoint(endpointArn)
						.protocol("Application")
						.build();
			
			SubscribeResponse subscribeResult = snsClient.subscribe(subscribeRequest);
			
			if (subscribeResult.subscriptionArn() != null && !subscribeResult.subscriptionArn().equals("")) {
				// Unsubscribe from push topic
				UnsubscribeRequest unsubscribeReq = UnsubscribeRequest.builder()
						.subscriptionArn(subscribeResult.subscriptionArn())
						.build();
				snsClient.unsubscribe(unsubscribeReq);
				LOG.debug("Removing subscription " + subscribeResult.subscriptionArn() + " for endpoint " + endpointArn);
			}
		} catch (Exception e) {
			LOG.error("Error while removing endpoint " + endpointArn, e);
		}
		
		// Delete endpoint from Application
		DeleteEndpointRequest deleteReq = DeleteEndpointRequest.builder()
				.endpointArn(endpointArn)
				.build();
		
		snsClient.deleteEndpoint(deleteReq);
	}
	
	protected String getPlatformArn(String site, String publication, String platform) throws Exception {
		if (platform == null) throw new Exception("Platform cannot be null");
		platform = platform.toLowerCase();
		String paramName = null;
		if (platform.equals(StringConstants.PLATFORM_APPLE)) {
			paramName = "amzAppleArn";
		}
		if (platform.equals(StringConstants.PLATFORM_ANDROID) || platform.equals(StringConstants.PLATFORM_WEB)) {
			paramName = "amzGCMArn";
		}
		if (paramName == null) throw new Exception("Unknown platform " + platform);
		return config.getParam(site, publication, module, paramName, "");
	}
	
	protected Log LOG = CmsLog.getLog(this);
}
