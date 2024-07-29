package com.tfsla.opencms.dev.collector;

import java.util.Comparator;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

public class IntegerPropertyComparator extends PropertyComparator implements Comparator<CmsResource> {

	public IntegerPropertyComparator(CmsObject cmsObject, String propertyName, boolean asc) {
		super(cmsObject, propertyName, asc);
	}
	
	@Override
	protected int naturalCompare(CmsResource obj1, CmsResource obj2) {
		return this.readPropertyAsInteger(obj1).compareTo(this.readPropertyAsInteger(obj2));
	}

}
