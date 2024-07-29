package com.tfsla.diario.facebook;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;


class FacebookPublisherReader {
	
	 private static final Log LOG = CmsLog.getLog(FacebookPublisherReader.class);
	 
	String fileName = "/system/modules/com.tfsla.opencmsdev/elements/facebookAccounts.html";
	
	CmsObject cms = null;
	
	FacebookPublisherReader(CmsObject cms)
	{
		this.cms = cms;
	}
	
	public List<FacebookAccountPublisher> getPublishersData()
	{
		List<FacebookAccountPublisher> accounts = new ArrayList<FacebookAccountPublisher>();
		
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


			int nroPub=1;
			
			String xmlName ="account[" + nroPub + "]/name[1]";
			String name = content.getStringValue(cms, xmlName, Locale.ENGLISH);
			while (name!=null)
			{

				FacebookAccountPublisher account = new FacebookAccountPublisher();

				xmlName ="account[" + nroPub + "]/key[1]";
				String key = content.getStringValue(cms, xmlName, Locale.ENGLISH);
	
				xmlName ="account[" + nroPub + "]/secret[1]";
				String secret = content.getStringValue(cms, xmlName, Locale.ENGLISH);

				account.setName(name);
				account.setSecret(secret);
				account.setKey(key);
			
				accounts.add(account);
				
				nroPub++;
				
				xmlName ="account[" + nroPub + "]/name[1]";
				name = content.getStringValue(cms, xmlName, Locale.ENGLISH);
			
			}
			
			cms.unlockResource(fileName);
		
		} catch (CmsException e) {
			LOG.error(e);
			e.printStackTrace();
		}

		return accounts;
	}
	
	public void addPublishersData(FacebookAccountPublisher account)
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
	
			content.addValue(cms, "account", Locale.ENGLISH, 0);
			
			I_CmsXmlContentValue value = content.getValue("account[1]/secret[1]", Locale.ENGLISH);
			value.setStringValue(cms, account.getSecret());
			
			value = content.getValue("account[1]/key[1]", Locale.ENGLISH);
			value.setStringValue(cms, account.getKey());

			value = content.getValue("account[1]/name[1]", Locale.ENGLISH);
			value.setStringValue(cms, account.getName());

			contentFile.setContents(content.marshal());
			cms.writeFile(contentFile);

			if (!cms.getLock(fileName).isUnlocked())
				cms.unlockResource(fileName);
		
		} catch (CmsException e) {
			LOG.error(e);
			e.printStackTrace();
		}

		
	}
	
	public void removePublishersData(FacebookAccountPublisher account)
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

			
			int nroPub=1;
			int idx=0;
			String xmlName ="account[" + nroPub + "]/name[1]";
			String name = content.getStringValue(cms, xmlName, Locale.ENGLISH);
			while (name!=null)
			{

				if (account.getName().equals(name))
				{
					name = null;
					idx = nroPub;
				}
				else {	
					nroPub++;
				
					xmlName ="account[" + nroPub + "]/name[1]";
					name = content.getStringValue(cms, xmlName, Locale.ENGLISH);
				}
			}

			if (idx!=0)
				content.removeValue("account", Locale.ENGLISH, idx-1);

			contentFile.setContents(content.marshal());
			cms.writeFile(contentFile);

			if (!cms.getLock(fileName).isUnlocked())
				cms.unlockResource(fileName);
		
		} catch (CmsException e) {
			LOG.error(e);
			e.printStackTrace();
		}

		
	}

	public void updatePublishersData(FacebookAccountPublisher account) {
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

			
			int nroPub=1;
			int idx=0;
			String xmlName ="account[" + nroPub + "]/name[1]";
			String name = content.getStringValue(cms, xmlName, Locale.ENGLISH);
			while (name!=null)
			{

				if (account.getName().equals(name))
				{
					name = null;
					idx = nroPub;
				}
				else {	
					nroPub++;
				
					xmlName ="account[" + nroPub + "]/name[1]";
					name = content.getStringValue(cms, xmlName, Locale.ENGLISH);
				}
			}

			if (idx!=0)
			{
				I_CmsXmlContentValue value = content.getValue("account[" + nroPub + "]/secret[1]", Locale.ENGLISH);
				value.setStringValue(cms, account.getSecret());

				value = content.getValue("account[" + nroPub + "]/key[1]", Locale.ENGLISH);
				value.setStringValue(cms, account.getKey());

			}
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
