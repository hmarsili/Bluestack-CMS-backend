package com.tfsla.opencmsdev.module;

import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;

public abstract class AbstractCmsPropertyManager implements CmsPropertyManager {
	
	public final String readProperty(CmsResource resource, String propertyName) {
		return this.readPropertyObject(resource, propertyName).getValue();
	}
	
	public final void writeProperty(CmsResource resource, String propertyName, String value) {
//		CmsProperty property = this.readPropertyObject(resource, propertyName);
//		
//		if(property.isNullProperty()) {
//			property = new CmsProperty(propertyName, value, value);
//		}
//		else {
//			property.setStructureValue(value);
//			property.setResourceValue(value);
//		}
//		this.writePropertyObject(resource, property);
		
		this.writePropertyObject(resource, new CmsProperty(propertyName, value, value));

	}
	
	
}
