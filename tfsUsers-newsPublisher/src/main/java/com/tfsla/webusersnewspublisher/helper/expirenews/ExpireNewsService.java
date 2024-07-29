package com.tfsla.webusersnewspublisher.helper.expirenews;

import java.util.ArrayList;
import java.util.Date;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

public class ExpireNewsService {
	
	public void expire(CmsObject cmsObject, String resourcePath, String userName) throws Exception {
		long expiredDate = new Date().getTime();
		this.expire(cmsObject, resourcePath, userName, expiredDate);
	}
	
	public void expire(CmsObject cmsObject, String resourcePath, String userName, long expiredDate) throws Exception {
		this.unlockResource(resourcePath, cmsObject);
		ArrayList<CmsProperty> properties = this.setExpired(cmsObject, resourcePath, expiredDate, userName);
		cmsObject.writePropertyObjects(resourcePath, properties);
		OpenCms.getPublishManager().publishResource(cmsObject, resourcePath);
	}
	
	public void release(CmsObject cmsObject, String resourcePath, String userName, long fromDate) throws Exception {
		this.unlockResource(resourcePath, cmsObject);
		ArrayList<CmsProperty> properties = this.setReleased(cmsObject, resourcePath, fromDate, userName);
		cmsObject.writePropertyObjects(resourcePath, properties);
		OpenCms.getPublishManager().publishResource(cmsObject, resourcePath);
	}
	
	public void releaseAndExpire(CmsObject cmsObject, String resourcePath, String userName, long fromDate, long expiredDate) throws Exception {
		this.unlockResource(resourcePath, cmsObject);
		ArrayList<CmsProperty> releasedProperties = this.setReleased(cmsObject, resourcePath, fromDate, userName);
		ArrayList<CmsProperty> expiredProperties = this.setExpired(cmsObject, resourcePath, expiredDate, userName);
		releasedProperties.addAll(expiredProperties);
		cmsObject.writePropertyObjects(resourcePath, releasedProperties);
		OpenCms.getPublishManager().publishResource(cmsObject, resourcePath);
	}
	
	private ArrayList<CmsProperty> setReleased(CmsObject cmsObject, String resourcePath, long fromDate, String userName) throws CmsException {
		ArrayList<CmsProperty> properties = new ArrayList<CmsProperty>();
		if(fromDate == 0) return properties;
		if(fromDate == Long.MIN_VALUE) fromDate = 0;
		
		cmsObject.setDateReleased(resourcePath, fromDate, false);
		CmsProperty property = new CmsProperty();
		
		property.setName(Strings.RELEASED_DATE_PROP);
		property.setAutoCreatePropertyDefinition(true);
		property.setStructureValue(String.valueOf(fromDate));
		properties.add(property);
		
		property = new CmsProperty();
		property.setName(Strings.RELEASED_USER_PROP);
		property.setAutoCreatePropertyDefinition(true);
		property.setStructureValue(userName);
		properties.add(property);
		
		return properties;
	}
	
	private ArrayList<CmsProperty> setExpired(CmsObject cmsObject, String resourcePath, long expiredDate, String userName) throws CmsException {
		ArrayList<CmsProperty> properties = new ArrayList<CmsProperty>();
		if(expiredDate == 0) return properties;
		if(expiredDate == Long.MIN_VALUE) expiredDate = Long.MAX_VALUE;
		
		cmsObject.setDateExpired(resourcePath, expiredDate, false);
		CmsProperty property = new CmsProperty();
		
		property.setName(Strings.EXPIRED_DATE_PROP);
		property.setAutoCreatePropertyDefinition(true);
		property.setStructureValue(String.valueOf(expiredDate));
		properties.add(property);
		
		property = new CmsProperty();
		property.setName(Strings.EXPIRED_USER_PROP);
		property.setAutoCreatePropertyDefinition(true);
		property.setStructureValue(userName);
		properties.add(property);
		
		return properties;
	}
	
	private void unlockResource(String resourcePath, CmsObject cmsObject) throws CmsException {
		CmsLock lock = cmsObject.getLock(resourcePath);
		if(!lock.isUnlocked()) {
			cmsObject.changeLock(resourcePath);
			cmsObject.unlockResource(resourcePath);
		}
		cmsObject.lockResource(resourcePath);
	}
}