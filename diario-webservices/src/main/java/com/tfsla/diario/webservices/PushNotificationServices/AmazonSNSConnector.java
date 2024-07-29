package com.tfsla.diario.webservices.PushNotificationServices;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.main.CmsLog;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.DeleteEndpointRequest;
import com.amazonaws.services.sns.model.GetEndpointAttributesRequest;
import com.amazonaws.services.sns.model.GetEndpointAttributesResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sns.model.UnsubscribeRequest;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.data.PushClientDAO;

public class AmazonSNSConnector {
	
	private CPMConfig config;
	private AmazonSNS snsClient;
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
			amzRegion = Region.US_Standard.toString();
		}
		
		if (amzAccessKey == null || amzAccessID == null || amzAccessKey.equals("") || amzAccessID.equals("")) {
			throw new Exception(String.format("amzAccessID and amzAccessKey cannot be empty: module %s, site %s - publication %s", module, site, publication));
		}
		
		AWSCredentials awsCreds = new BasicAWSCredentials(amzAccessID, amzAccessKey);
		this.snsClient = AmazonSNSClient.builder()
				.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
				.withRegion(amzRegion)
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
		CreatePlatformEndpointRequest platformEndpointRequest = new CreatePlatformEndpointRequest();
		platformEndpointRequest.setPlatformApplicationArn(amzArn);
		platformEndpointRequest.setToken(token.replace("https://android.googleapis.com/gcm/send/", ""));
		CreatePlatformEndpointResult result = snsClient.createPlatformEndpoint(platformEndpointRequest);
		
		// Subscribe the endpoint ARN to the topic
		SubscribeRequest subscribeRequest = new SubscribeRequest();
		subscribeRequest.setTopicArn(amzPushTopicArn);
		subscribeRequest.setEndpoint(result.getEndpointArn());
		subscribeRequest.setProtocol("Application");
		SubscribeResult subscribeResult = snsClient.subscribe(subscribeRequest);
		
		LOG.debug("Added push subscriber - token: " + token + ", endpoint: " + result.getEndpointArn());
		
		if (saveInDB) {
			PushClientDAO dao = new PushClientDAO();
			try {
				dao.openConnection();
				
				if (!dao.userExists(token, platform, subscribeResult.getSubscriptionArn(), site, Integer.valueOf(publication))) {
					dao.registerEndpoint(token, platform, result.getEndpointArn(), subscribeResult.getSubscriptionArn(), site, publication);
				} else {
					dao.updateEndpoint(token, platform, result.getEndpointArn(), subscribeResult.getSubscriptionArn(), site, publication);
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
			GetEndpointAttributesRequest request = new GetEndpointAttributesRequest();
			request.setEndpointArn(endpointArn);
			GetEndpointAttributesResult result = snsClient.getEndpointAttributes(request);
			return result.getAttributes().get("Token");
		} catch (Exception e) {
			return "";
		}
	}
	
	public void removePushSubscriber(String endpointArn) throws Exception {
		String amzPushTopicArn = config.getParam(site, publication, module, "amzPushTopicArn", "");
		
		try {
			// Get topic subscription
			SubscribeRequest subscribeRequest = new SubscribeRequest();
			subscribeRequest.setTopicArn(amzPushTopicArn);
			subscribeRequest.setEndpoint(endpointArn);
			subscribeRequest.setProtocol("Application");
			SubscribeResult subscribeResult = snsClient.subscribe(subscribeRequest);
			
			if (subscribeResult.getSubscriptionArn() != null && !subscribeResult.getSubscriptionArn().equals("")) {
				// Unsubscribe from push topic
				UnsubscribeRequest unsubscribeReq = new UnsubscribeRequest();
				unsubscribeReq.setSubscriptionArn(subscribeResult.getSubscriptionArn());
				snsClient.unsubscribe(unsubscribeReq);
				LOG.debug("Removing subscription " + subscribeResult.getSubscriptionArn() + " for endpoint " + endpointArn);
			}
		} catch (Exception e) {
			LOG.error("Error while removing endpoint " + endpointArn, e);
		}
		
		// Delete endpoint from Application
		DeleteEndpointRequest deleteReq = new DeleteEndpointRequest();
		deleteReq.setEndpointArn(endpointArn);
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
