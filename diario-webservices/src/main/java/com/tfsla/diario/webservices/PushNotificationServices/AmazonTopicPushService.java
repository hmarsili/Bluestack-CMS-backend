package com.tfsla.diario.webservices.PushNotificationServices;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.main.CmsLog;


import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

public class AmazonTopicPushService {
	
	public AmazonTopicPushService(String site, String publication) {
		this.config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		this.site = site;
		this.publication = publication;
		this.module = PushServiceConfiguration.getModuleName();
		this.LOG = CmsLog.getLog(this);
	}
	
	public void pushMessage() throws Exception {
		String amzAccessID = config.getParam(site, publication, module, "amzAccessID", ""); 
		String amzAccessKey = config.getParam(site, publication, module, "amzAccessKey", "");
		String amzRegion = config.getParam(site, publication, module, "amzRegion", "");
		String amzPushTopicArn = config.getParam(site, publication, module, "amzPushTopicArn", "");
		String pushTTL = config.getParam(site, publication, module, "pushTTL", "");
		String pushCollapseKey = config.getParam(site, publication, module, "pushCollapseKey", "");
		if (amzRegion == null || amzRegion.equals("")) {
			amzRegion = Region.US_EAST_1.toString();
		}
		
		if (amzAccessKey == null || amzAccessID == null || amzAccessKey.equals("") || amzAccessID.equals("")) {
			throw new Exception(String.format("amzAccessID and amzAccessKey cannot be empty: module %s, site %s - publication %s", module, site, publication));
		}
		
		if (amzPushTopicArn == null || amzPushTopicArn.equals("")) {
			throw new Exception(String.format("amzPushTopicArn parameter not specified for module %s, site %s - publication %s", module, site, publication));
		}
		
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(amzAccessID, amzAccessKey);

		SnsClient snsClient = SnsClient.builder()
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.region(Region.of(amzRegion))
				.build();
		
		LOG.debug(String.format("Amz accessID: %s, secret: %s, region: %s", amzAccessID, amzAccessKey, amzRegion));
		LOG.debug(String.format("Using %s as TTL and %s as collapse key for topic ARN %s", pushTTL, pushCollapseKey, amzPushTopicArn));
		
		Map<String, MessageAttributeValue> messageAttributes = new HashMap<String, MessageAttributeValue>();
		if (pushTTL != null && !pushTTL.equals("")) {
			messageAttributes.put("AWS.SNS.MOBILE.BAIDU.TTL", MessageAttributeValue.builder().dataType("String").stringValue(pushTTL).build());
			messageAttributes.put("time_to_live", MessageAttributeValue.builder().dataType("String").stringValue(pushTTL).build());
		}
		if (pushCollapseKey != null && !pushCollapseKey.equals("")) {
			messageAttributes.put("collapse_key", MessageAttributeValue.builder().dataType("String").stringValue(pushCollapseKey).build());
		}
		
		PublishRequest publishRequest =
				PublishRequest.builder()
				.messageAttributes(messageAttributes)
				.message("-")
				.targetArn(amzPushTopicArn)
				.build();
		
		try {
			PublishResponse publishResult = snsClient.publish(publishRequest);
			LOG.info(String.format("Published message id %s on ARN %s", publishResult.messageId(), amzPushTopicArn));
		} catch (Exception e) {
			LOG.error(e);
			throw e;
		}
	}
	
	protected CPMConfig config;
	protected String site;
	protected String publication;
	protected String module;
	protected Log LOG;
}
