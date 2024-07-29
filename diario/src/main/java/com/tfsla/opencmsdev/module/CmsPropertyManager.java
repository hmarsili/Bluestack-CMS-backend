package com.tfsla.opencmsdev.module;

import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;

public interface CmsPropertyManager {

	public void writeProperty(CmsResource resource, String propertyName, String value);
	public String readProperty(CmsResource resource, String propertyName);

	public CmsProperty readPropertyObject(CmsResource resource, String propertyName);
	public void writePropertyObject(CmsResource resource, CmsProperty property);

	
}
