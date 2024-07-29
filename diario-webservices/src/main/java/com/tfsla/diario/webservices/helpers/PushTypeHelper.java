package com.tfsla.diario.webservices.helpers;

import java.util.Locale;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

public class PushTypeHelper {
	
	public static synchronized String getPushTypeFromResource(CmsResource resource, CmsObject cms) throws CmsXmlException, CmsException {
		Locale locale = cms.getRequestContext().getLocale();
		CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, cms.readFile(resource));
		return xmlContent.getStringValue(cms, "push", locale);
	}
	
	public static synchronized String getPushTypeFromResource(CmsResource resource, CmsObject cms, CmsXmlContent xmlContent) throws CmsXmlException, CmsException {
		Locale locale = cms.getRequestContext().getLocale();
		return xmlContent.getStringValue(cms, "push", locale);
	}
}
