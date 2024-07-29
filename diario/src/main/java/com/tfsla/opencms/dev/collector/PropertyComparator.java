package com.tfsla.opencms.dev.collector;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

public class PropertyComparator extends AscDescComparator<CmsResource>{

	private CmsObject cmsObject;
	private String propertyName;

	public PropertyComparator(CmsObject cmsObject, String propertyName, boolean asc) {
		super(asc);
		this.cmsObject = cmsObject;
		this.propertyName = propertyName;
	}

	@Override
	protected int naturalCompare(CmsResource obj1, CmsResource obj2) {
		return this.readPropertyAsLowerCase(obj1).compareTo(this.readPropertyAsLowerCase(obj2));
	}
	
	protected String readProperty(CmsResource resource) {
		String value;
		try {
			value = this.cmsObject.readPropertyObject(resource, this.propertyName, false).getValue();
		} catch (CmsException e) {
			throw new RuntimeException(e);
		}
		return value != null ? value : "";
	}
	
	protected String readPropertyAsLowerCase(CmsResource resource) {
		return this.readProperty(resource).toLowerCase();
	}
	
	protected Integer readPropertyAsInteger(CmsResource resource) {
		try {
			return Integer.parseInt(this.readProperty(resource));
		}
		catch(NumberFormatException ex) {
			return Integer.MAX_VALUE;
		}
	}

}
