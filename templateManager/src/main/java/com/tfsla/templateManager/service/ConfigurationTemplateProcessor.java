package com.tfsla.templateManager.service;

import java.util.*;

import jakarta.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.flex.CmsFlexController;
import org.opencms.staticexport.CmsLinkManager;

public class ConfigurationTemplateProcessor extends A_ItemProcessor {
	
	private String name;
	private List<ConfigurationTemplateItemProcessor> configTemplateItems = new ArrayList<ConfigurationTemplateItemProcessor>();
	
	public ConfigurationTemplateProcessor()
	{
		itemType = "configurationTemplate";
	}	
	
	@Override
	public A_ItemProcessor clone() {
		return new ConfigurationTemplateProcessor();
	}	
	
	@Override
	public void printDOJOGlobalConf() {
		if (project.equals("Offline")) {
	        CmsFlexController controller = CmsFlexController.getController(req);
	
	        String target =  templateProcessor.baseURL + templateProcessor.jsPath + "/" + itemType + ".js";
	        target = CmsLinkManager.getAbsoluteUri(target, controller.getCurrentRequest().getElementUri());
	
			try {
				includeNoCache(controller, target, "");
			} catch (JspException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}	
	
	public void preProcess(CmsObject cms, CmsXmlContent content, int nroCont) {
		String xmlName ="configurationTemplate[" + nroCont + "]/name[1]";
		I_CmsXmlContentValue value = content.getValue(xmlName, Locale.ENGLISH);
		name = value.getStringValue(cms);
		
		int nro=1;
		xmlName ="configurationTemplate[" + nroCont + "]/configurationTemplateContainers[" + nro + "]";
		value = content.getValue(xmlName, Locale.ENGLISH);
		while (value!=null)
		{
			ConfigurationTemplateItemProcessor configTemplateItem = new ConfigurationTemplateItemProcessor();			
			
			xmlName ="configurationTemplate[" + nroCont + "]/configurationTemplateContainers[" + nro + "]/contenedorName[1]";
			value = content.getValue(xmlName, Locale.ENGLISH);
			configTemplateItem.setContainerName(value.getStringValue(cms));	
			
			xmlName ="configurationTemplate[" + nroCont + "]/configurationTemplateContainers[" + nro + "]/hide[1]";
			value = content.getValue(xmlName, Locale.ENGLISH);
			configTemplateItem.setHide(Boolean.valueOf(value.getStringValue(cms)));		
			
			xmlName ="configurationTemplate[" + nroCont + "]/configurationTemplateContainers[" + nro + "]/configuration[1]";
			value = content.getValue(xmlName, Locale.ENGLISH);
			configTemplateItem.setConfiguration(value.getStringValue(cms));
			
			configTemplateItems.add(configTemplateItem);
			
			nro++;
			xmlName ="configurationTemplate[" + nroCont + "]/configurationTemplateContainers[" + nro + "]";
			value = content.getValue(xmlName, Locale.ENGLISH);			
		}
	}
	
	public void printDOJOHTML() {
		if (project.equals("Offline")) {
	        CmsFlexController controller = CmsFlexController.getController(req);
	
	        String target =  templateProcessor.baseURL + templateProcessor.builderPath + "/setConfigurationTemplateProperties.jsp";
	        target = CmsLinkManager.getAbsoluteUri(target, controller.getCurrentRequest().getElementUri());
	
			try {
				includeNoCache(controller, target, "");
			} catch (JspException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}	
	
	public void getXmlDataItems(){
		
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}	
	
	public List<ConfigurationTemplateItemProcessor> getConfigurationTemplateItems() {
		return configTemplateItems;
	}	
}
