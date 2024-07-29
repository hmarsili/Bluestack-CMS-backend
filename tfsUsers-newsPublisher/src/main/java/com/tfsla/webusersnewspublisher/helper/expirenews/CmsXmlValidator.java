package com.tfsla.webusersnewspublisher.helper.expirenews;

import java.util.Locale;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

/**
 * Provides generic methods to Xml validators, usefull to reutilize this services among the code
 */
public abstract class CmsXmlValidator {
	
	/**
	 * Constructs an instance of the CmsXmlValidator
	 * @param cmsObject the CmsObject of the current session
	 * @param resourcePath the path of the resource to be validated
	 * @throws Exception
	 */
	public CmsXmlValidator(CmsObject cmsObject, String resourcePath) throws Exception {
		this.cmsObject = cmsObject;
		this.resourcePath = resourcePath;
		this.locale = cmsObject.getRequestContext().getLocale();
		CmsFile file = cmsObject.readFile(resourcePath);
		this.xmlContent = CmsXmlContentFactory.unmarshal(cmsObject, file);
	}
	
	/**
	 * Gets a string value for a path into the XML document
	 * @param path where to look for the String value
	 * @return the String value for the path provided (never returns null)
	 */
	public String getStringValue(String path) {
		if(xmlContent.hasValue(path, locale)) {
			String val = xmlContent.getStringValue(cmsObject, path, locale);
			if(val != null) return val;
		}
		
		return "";
	}
	
	protected CmsObject cmsObject;
	protected String resourcePath;
	protected Locale locale;
	protected CmsXmlContent xmlContent;
}
