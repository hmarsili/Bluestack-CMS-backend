package com.tfsla.diario.twitter;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;


class TwitterConsumerReader {
	
	 private static final Log LOG = CmsLog.getLog(TwitterConsumerReader.class);
	 
	String fileName = "/system/modules/com.tfsla.opencmsdev/elements/twitterConsumer.html";
	
	CmsObject cms = null;
	
	TwitterConsumerReader(CmsObject cms)
	{
		this.cms = cms;
	}
	
	public TwitterAccountConsumer getConsumerData()
	{
		TwitterAccountConsumer account = new TwitterAccountConsumer();
		
		try {
			if (cms.getLock(fileName).isUnlocked())
            	cms.lockResource(fileName);
            else
            {
            	try {
            		cms.unlockResource(fileName);
            		cms.lockResource(fileName);
            	}
            	catch (Exception e)
            	{
	            	cms.changeLock(fileName);	            		
            	}
            }

			CmsFile contentFile = cms.readFile(fileName);
			CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, contentFile);
	
			content.setAutoCorrectionEnabled(true); 
			content.correctXmlStructure(cms);
	
			String secret = content.getStringValue(cms, "secret", Locale.ENGLISH);
			String key = content.getStringValue(cms, "clave", Locale.ENGLISH);
			
			account.setSecret(secret);
			account.setKey(key);
		
			cms.unlockResource(fileName);
		
		} catch (CmsException e) {
			LOG.error(e);
			e.printStackTrace();
		}

		return account;
	}
	
	public void setConsumerData(TwitterAccountConsumer account)
	{
		try {

			if (cms.getLock(fileName).isUnlocked())
            	cms.lockResource(fileName);
            else
            {
            	try {
            		cms.unlockResource(fileName);
            		cms.lockResource(fileName);
            	}
            	catch (Exception e)
            	{
	            	cms.changeLock(fileName);	            		
            	}
            }

			CmsFile contentFile = cms.readFile(fileName);
			CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, contentFile);
	
			content.setAutoCorrectionEnabled(true); 
			content.correctXmlStructure(cms);
	
			I_CmsXmlContentValue value = content.getValue("secret", Locale.ENGLISH);
			value.setStringValue(cms, account.getSecret());
			
			value = content.getValue("clave", Locale.ENGLISH.ENGLISH);
			value.setStringValue(cms, account.getKey());
					
			contentFile.setContents(content.marshal());
			cms.writeFile(contentFile);

			if (!cms.getLock(fileName).isUnlocked())
				cms.unlockResource(fileName);
		
		} catch (CmsException e) {
			LOG.error(e);
			e.printStackTrace();
		}

		
	}
}
