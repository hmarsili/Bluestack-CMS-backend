package com.tfsla.diario.newsCollector.comparators;

import java.util.Comparator;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

import com.tfsla.diario.newsCollector.order.OrderDirective;

public class PropertyComparator extends AscDescComparator<CmsResource>{

	private CmsObject cmsObject;
	private String propertyName;
	
	private Comparator<CmsResource> comparator = new compareString();

	private class compareLong implements Comparator<CmsResource>
	{
		public int compare(CmsResource obj1, CmsResource obj2) {
			return readPropertyAsLong(obj1).compareTo(readPropertyAsLong(obj2));
		}	
	}

	private class compareInteger implements Comparator<CmsResource>
	{
		public int compare(CmsResource obj1, CmsResource obj2) {
			return readPropertyAsInteger(obj1).compareTo(readPropertyAsInteger(obj2));
		}	
	}

	private class compareString implements Comparator<CmsResource>
	{
		public int compare(CmsResource obj1, CmsResource obj2) {
			return readPropertyAsLowerCase(obj1).compareTo(readPropertyAsLowerCase(obj2));
		}	
	}

	private class compareDate implements Comparator<CmsResource>
	{
		public int compare(CmsResource obj1, CmsResource obj2) {
			return readPropertyAsInteger(obj1).compareTo(readPropertyAsInteger(obj2));
		}	
	}

	public PropertyComparator(CmsObject cmsObject, String propertyName, boolean asc) {
		super(asc);
		this.cmsObject = cmsObject;
		this.propertyName = propertyName;
	}

	public PropertyComparator(CmsObject cmsObject, String propertyName, String propertyType, boolean asc) {
		super(asc);
		this.cmsObject = cmsObject;
		this.propertyName = propertyName;
		if (propertyType.equals(OrderDirective.TYPE_DATE))
			comparator = new compareDate();
		else if (propertyType.equals(OrderDirective.TYPE_INTEGER))
			comparator = new compareInteger();
		else if (propertyType.equals(OrderDirective.TYPE_STRING))
			comparator = new compareString();
		else if (propertyType.equals(OrderDirective.TYPE_LONG))
			comparator = new compareLong();
	}

	@Override
	protected int naturalCompare(CmsResource obj1, CmsResource obj2) {
		return comparator.compare(obj1,obj2);
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

	protected Long readPropertyAsLong(CmsResource resource) {
		try {
			return Long.parseLong(this.readProperty(resource));
		}
		catch(NumberFormatException ex) {
			return Long.MAX_VALUE;
		}
	}

}
