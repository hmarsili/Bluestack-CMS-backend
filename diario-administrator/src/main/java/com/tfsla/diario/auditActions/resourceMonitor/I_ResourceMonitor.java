package com.tfsla.diario.auditActions.resourceMonitor;

import java.util.Locale;

import org.opencms.file.CmsObject;
import org.opencms.xml.content.CmsXmlContent;

public interface I_ResourceMonitor {
	public void auditChanges(CmsObject cms, CmsXmlContent newContent,String resourceName, Locale locale);
	public int getResourceType();
}
