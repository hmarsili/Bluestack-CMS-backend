package com.tfsla.diario.newsletters.service;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;

import com.tfsla.diario.newsletters.common.NewsletterConfiguration;

public class NewsletterConfigurationService {
	
	public static synchronized NewsletterConfiguration getConfig(String site, String publication) {
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		String amzAccessID = config.getParam(site, publication, MODULE_NAME, "amzAccessID");
		String amzAccessKey = config.getParam(site, publication, MODULE_NAME, "amzAccessKey");
		String amzRegion = config.getParam(site, publication, MODULE_NAME, "amzRegion");
		String amzSESConfigSet = config.getParam(site, publication, MODULE_NAME, "amzSESConfigSet");
		int batchSize = config.getIntegerParam(site, publication, MODULE_NAME, "batchSize", 1000);
		int numOfMessagesShuttles = config.getIntegerParam(site, publication, MODULE_NAME, "numOfMessagesShuttles", 1);
		
		NewsletterConfiguration ret = new NewsletterConfiguration();
		ret.setAmzAccessID(amzAccessID);
		ret.setAmzAccessKey(amzAccessKey);
		ret.setAmzRegion(amzRegion);
		ret.setBatchSize(batchSize);
		ret.setNumOfMessagesShuttles(numOfMessagesShuttles);
		ret.setAmzSESConfigSet(amzSESConfigSet);
		return ret;
	}
	
	protected static final String MODULE_NAME = "newsletter";
}