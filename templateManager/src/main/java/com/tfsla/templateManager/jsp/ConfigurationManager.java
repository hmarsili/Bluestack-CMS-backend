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

import org.opencms.main.OpenCms;

public class ConfigurationManager {

	private CmsObject cmsObject;
	
	public ConfigurationManager(CmsObject cmsObject) {		
		this.cmsObject = cmsObject;
	}
	
	public void saveConfiguration(String xml, Boolean publish, String resourceName) throws Exception {
		
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
	        
	        NodeList containers = dom.getElementsByTagName("contenedor");
	        
	        int p;
	        
	        if (containers != null) {
	        	
				while (content.getValue("contenedor[1]", Locale.ENGLISH)!=null)
					content.removeValue("contenedor", Locale.ENGLISH, 0);	        	
	        	
        		for (int i = 0; i < containers.getLength(); i++) {
        			NroContainer = i+1;
        			Element container = (Element)containers.item(i);
        			
        	        content.addValue(cmsObject, "contenedor", Locale.ENGLISH, i);
        	        
        	        //NAME
        	        NodeList node = container.getElementsByTagName("name");
        	        if (node != null && node.getLength() == 1) {
        	        	I_CmsXmlContentValue value = content.getValue("contenedor[" + NroContainer + "]/name", Locale.ENGLISH);
        	        	value.setStringValue(cmsObject, node.item(0).getFirstChild().getNodeValue());
        	        }
        	        
        	        //HIDE
        	        node = container.getElementsByTagName("hide");
        	        if (node != null && node.getLength() == 1) {
        	        	I_CmsXmlContentValue value = content.getValue("contenedor[" + NroContainer + "]/hide", Locale.ENGLISH);
        	        	value.setStringValue(cmsObject, node.item(0).getFirstChild().getNodeValue());
        	        }	        
        	        
        	        //CONFIGURATION
        	        node = container.getElementsByTagName("configuration");
        	        if (node != null && node.getLength() == 1) {
        	        	I_CmsXmlContentValue value = content.getValue("contenedor[" + NroContainer + "]/configuration", Locale.ENGLISH);
        	        	value.setStringValue(cmsObject, node.item(0).getFirstChild().getNodeValue());
        	        }
        	        
        	        //MAXITEMS
        	        node = container.getElementsByTagName("maxItems");
        	        if (node != null && node.getLength() == 1) {
        	        	I_CmsXmlContentValue value = content.getValue("contenedor[" + NroContainer + "]/maxItems", Locale.ENGLISH);
        	        	value.setStringValue(cmsObject, node.item(0).getFirstChild().getNodeValue());
        	        }

        	        //TIPO
        	        node = container.getElementsByTagName("allowedType");
        	        if (node != null) {
        	        	for (p = node.getLength()-1; p >= 0; p--) {
        	        		I_CmsXmlContentValue value = content.addValue(cmsObject, "contenedor[" + NroContainer + "]/tipo", Locale.ENGLISH, 0);
        	        		value.setStringValue(cmsObject, node.item(p).getFirstChild().getNodeValue());
        	        	}              		
        	        }
        	        
        	        //ALLOWED CONFIGURATION
        	        node = container.getElementsByTagName("allowedConfiguration");
        	        if (node != null) {
        	        	for (p = node.getLength()-1; p >= 0; p--) {
        	        		I_CmsXmlContentValue value = content.addValue(cmsObject, "contenedor[" + NroContainer + "]/allowedConfiguration", Locale.ENGLISH, 0);
        	        		value.setStringValue(cmsObject, node.item(p).getFirstChild().getNodeValue());
        	        	}              		
        	        }
        	        
        	        //ITEMS
        	        node = container.getElementsByTagName("items");
        	        if (node != null) {
        	        	for (p = node.getLength()-1; p >= 0; p--) {
        	        		content.addValue(cmsObject, "contenedor[" + NroContainer + "]/items", Locale.ENGLISH, 0);
        	        		I_CmsXmlContentValue value = content.getValue("contenedor[" + NroContainer + "]/items[1]/tipo", Locale.ENGLISH);
        	        		Element item = (Element)node.item(p);
        	        		value.setStringValue(cmsObject, item.getFirstChild().getFirstChild().getNodeValue());
        	        		
        	        		//PARAMETROS
        	        		NodeList nodeParams = item.getElementsByTagName("parametros");
        	        		if (nodeParams != null) {
        	        			for (int k = nodeParams.getLength()-1; k >= 0; k--) {
        	        				content.addValue(cmsObject, "contenedor[" + NroContainer + "]/items[1]/parametros", Locale.ENGLISH, 0);
        	        				
        	        				Element parametros = (Element)nodeParams.item(k);
        	        				
                	        		I_CmsXmlContentValue nameParam = content.getValue("contenedor[" + NroContainer + "]/items[1]/parametros[1]/nombre", Locale.ENGLISH);
                	        		nameParam.setStringValue(cmsObject, parametros.getElementsByTagName("nombre").item(0).getFirstChild().getNodeValue());

                	        		I_CmsXmlContentValue valueParam = content.getValue("contenedor[" + NroContainer + "]/items[1]/parametros[1]/valor", Locale.ENGLISH);
                	        		valueParam.setStringValue(cmsObject, parametros.getElementsByTagName("valor").item(0).getFirstChild().getNodeValue());
        	        			}
        	        		}
        	        	}    		
        	        }         	        
        		}
	        }
	        
			contentFile.setContents(content.marshal());
			cmsObject.writeFile(contentFile);
						
			cmsObject.unlockResource(resourceName);
			
		   	if(publish)
		   		OpenCms.getPublishManager().publishResource(cmsObject, resourceName);			
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
