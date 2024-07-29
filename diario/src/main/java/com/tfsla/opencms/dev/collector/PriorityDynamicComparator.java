package com.tfsla.opencms.dev.collector;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

import com.tfsla.opencmsdev.CmsResourceExtended;

public class PriorityDynamicComparator extends AscDescComparator<CmsResource>{

	private CmsObject cmsObject;
	private String propertyName;

	public PriorityDynamicComparator(CmsObject cmsObject, String propertyName, boolean asc) {
		super(asc);
		this.cmsObject = cmsObject;
		this.propertyName = propertyName;
	}

	@Override
	protected int naturalCompare(CmsResource obj1, CmsResource obj2) {
		return this.readPropertyAsInteger(obj1).compareTo(this.readPropertyAsInteger(obj2));
	}
	
	protected String readProperty(CmsResource resource) {
		String value;
		try {
			
			int index = ((CmsResourceExtended)resource).getIndexPublication(); 
			
			if(index == 0)
				value = this.cmsObject.readPropertyObject(resource, this.propertyName, false).getValue();
			else
				value = this.cmsObject.readPropertyObject(resource, this.propertyName + index, false).getValue();
			
		} catch (CmsException e) {
			throw new RuntimeException(e);
		}
		return value != null ? value : "";
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
