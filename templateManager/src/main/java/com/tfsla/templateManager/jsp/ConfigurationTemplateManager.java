package com.tfsla.templateManager.jsp;

import java.util.Locale;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;

import org.opencms.main.CmsException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.tfsla.templateManager.service.ConfigurationTemplateProcessor;

public class ConfigurationTemplateManager {

	private CmsObject cmsObject;
	
	public ConfigurationTemplateManager(CmsObject cmsObject) {		
		this.cmsObject = cmsObject;
	}
	
	public ConfigurationTemplateProcessor getConfigurationTemplate(Integer indexConfigurationTemplate, String resourceName) throws Exception {
		
        CmsFile templateFile = cmsObject.readFile(resourceName);	
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmsObject, templateFile);
		
		ConfigurationTemplateProcessor configurationTemplateProcessor = new ConfigurationTemplateProcessor();
		configurationTemplateProcessor.preProcess(cmsObject, content, indexConfigurationTemplate);
		
		return configurationTemplateProcessor;
	}
	
	public void saveConfigurationTemplate(String xml, String resourceName, String configurationTemplateName, Boolean isNewTemplate) throws Exception {
		
		try {
			
			lockTheFile(resourceName);
			
			CmsFile contentFile = cmsObject.readFile(resourceName);
			
			CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmsObject, contentFile);
			
			content.setAutoCorrectionEnabled(true);
			content.correctXmlStructure(cmsObject);
						
			InputStream is = new ByteArrayInputStream(xml.getBytes("ISO-8859-1"));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			DocumentBuilder db = dbf.newDocumentBuilder();
	        Document dom = db.parse(is);

	        int NroContainer = 0;
	        	        
	        int countConfigurationTemplate = content.getIndexCount("configurationTemplate", Locale.ENGLISH);
	        int indexConfigurationTemplate = 0;
	        
        	if(isNewTemplate){
        		if(countConfigurationTemplate == 0)
        			indexConfigurationTemplate = 1;
        		else
        			indexConfigurationTemplate = countConfigurationTemplate + 1;
        	}
        	else{
        		for (int i = 1; i <= countConfigurationTemplate; i++) {
	        		I_CmsXmlContentValue valueConfigurationTemplate = content.getValue("configurationTemplate[" + i + "]/name", Locale.ENGLISH);
	        		
	        		if(valueConfigurationTemplate.getStringValue(cmsObject).equals(configurationTemplateName)){
	        			indexConfigurationTemplate = i;
	        			break;
	        		}	        			
        		}        		
        	}
        	
	        NodeList containers = dom.getElementsByTagName("contenedor");        	
	        
	        if (containers != null && indexConfigurationTemplate > 0) {
	        	
				if(isNewTemplate){
					content.addValue(cmsObject, "configurationTemplate", Locale.ENGLISH, indexConfigurationTemplate - 1);
				}
				else{
    				while (content.getValue("configurationTemplate[" + indexConfigurationTemplate + "]/configurationTemplateContainers[1]", Locale.ENGLISH)!=null)
    					content.removeValue("configurationTemplate[" + indexConfigurationTemplate + "]/configurationTemplateContainers", Locale.ENGLISH, 0);    					
				}
				        			
        		for (int i = 0; i < containers.getLength(); i++) {
        			NroContainer = i+1;
        			Element container = (Element)containers.item(i);
        			
        	        content.addValue(cmsObject, "configurationTemplate[" + indexConfigurationTemplate + "]/configurationTemplateContainers", Locale.ENGLISH, i);
        	        
        	        //NAME
        	        I_CmsXmlContentValue valueName = content.getValue("configurationTemplate[" + indexConfigurationTemplate + "]/name", Locale.ENGLISH);
        	        valueName.setStringValue(cmsObject, configurationTemplateName);           	        
        	        
        	        //NAME CONTAINER
        	        NodeList node = container.getElementsByTagName("name");
        	        if (node != null && node.getLength() == 1) {
        	        	I_CmsXmlContentValue value = content.getValue("configurationTemplate[" + indexConfigurationTemplate + "]/configurationTemplateContainers[" + NroContainer + "]/contenedorName", Locale.ENGLISH);
        	        	value.setStringValue(cmsObject, node.item(0).getFirstChild().getNodeValue());
        	        }
        	        
        	        //HIDE
        	        node = container.getElementsByTagName("hide");
        	        if (node != null && node.getLength() == 1) {
        	        	I_CmsXmlContentValue value = content.getValue("configurationTemplate[" + indexConfigurationTemplate + "]/configurationTemplateContainers[" + NroContainer + "]/hide", Locale.ENGLISH);
        	        	value.setStringValue(cmsObject, node.item(0).getFirstChild().getNodeValue());
        	        }	        
        	        
        	        //CONFIGURATION
        	        node = container.getElementsByTagName("configuration");
        	        if (node != null && node.getLength() == 1) {
        	        	I_CmsXmlContentValue value = content.getValue("configurationTemplate[" + indexConfigurationTemplate + "]/configurationTemplateContainers[" + NroContainer + "]/configuration", Locale.ENGLISH);
        	        	value.setStringValue(cmsObject, node.item(0).getFirstChild().getNodeValue());
        	        }        	        
        		}	  
	        }
	        
			contentFile.setContents(content.marshal());
			cmsObject.writeFile(contentFile);
						
			cmsObject.unlockResource(resourceName);
		}
		catch (Exception e){	
			throw e;
		}
	}
	
	private void lockTheFile(String file) throws CmsException
	{
		if (cmsObject.getLock(file).isUnlocked())
			cmsObject.lockResource(file);
        else
        {
        	try {
        		cmsObject.unlockResource(file);
        		cmsObject.lockResource(file);
        	}
        	catch (Exception e)
        	{
        		cmsObject.changeLock(file);	            		
        	}
        }
	}
}
