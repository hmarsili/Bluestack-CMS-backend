package com.tfsla.webusersnewspublisher.service;

import java.util.ArrayList;
import java.util.List;

import org.opencms.db.CmsDefaultUsers;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.xml.CmsXmlException;

import com.tfsla.utils.TfsAdminUserProvider;
import com.tfsla.webusersposts.helper.XmlContentHelper;

/**
 * Provides an interface to edit news by setting its XML fields or custom properties 
 */
public class NewsEditorManager {

	private CmsObject adminCmsObject;
	private CmsFile cmsFile;
	private String resourceName;
	private XmlContentHelper contentHelper;
	private List<CmsProperty> properties;
	
	/**
	 * Creates a new instance of the Editor Manager, will be used to edit properties
	 * and xml values for a specific resource into a site
	 * @param resourceName the name of the resource to be edited
	 * @param siteName the site name the resource belongs to
	 * @throws Exception
	 */
	public NewsEditorManager(String resourceName, String siteName) throws Exception {
		this.adminCmsObject = getAdminCmsObject();
		this.adminCmsObject.getRequestContext().setSiteRoot(siteName);
		this.resourceName = resourceName;
		this.cmsFile = adminCmsObject.readFile(resourceName);
		this.contentHelper = new XmlContentHelper(adminCmsObject, cmsFile);
		this.properties = new ArrayList<CmsProperty>();
		this.stealLockFile();
	}
	
	/**
	 * Sets a custom property to the current resource
	 * @param propertyName the name for the custom property 
	 * @param value property value
	 * @throws CmsException
	 */
	public void setProperty(String propertyName, String value) throws CmsException {
		CmsProperty prop = new CmsProperty(propertyName, value, value);
		this.properties.add(prop);
	}
	
	/**
	 * Removes a xml value from the current resource
	 * @param xmlField name of the xml field to be removed
	 */
	public void removeXmlValue(String xmlField) {
		contentHelper.removeXmlContentValue(xmlField);
	}
	
	/**
	 * Updates or adds a xml value from the current resource
	 * @param xmlField name of the xml field to be setted up
	 * @param value actual value for the xml field
	 * @throws Exception
	 */
	public void setXmlValue(String xmlField, String value) throws Exception {
		contentHelper.setXmlContentValue(xmlField, value);
	}
	
	/**
	 * Saves the changes made to the current resource
	 * @throws CmsXmlException
	 * @throws Exception
	 */
	public void save() throws CmsXmlException, Exception {
		this.cmsFile.setContents(this.contentHelper.getXmlContent().marshal());
		this.adminCmsObject.writeFile(this.cmsFile);
		for(CmsProperty prop : this.properties) {
			this.adminCmsObject.writePropertyObject(resourceName, prop);
		}
	}
	
	/**
	 * Publishes the current resource
	 * @throws Exception
	 */
	public void publish() throws Exception {
		OpenCms.getPublishManager().publishResource(
			this.adminCmsObject,
			this.resourceName
		);
	}
	
	/**
	 * Removes the lock from the file
	 * @throws Exception
	 */
	public void unlockFile() throws Exception {
		CmsLock lock = this.adminCmsObject.getLock(this.cmsFile);
		if(!lock.isUnlocked()) {
			this.adminCmsObject.changeLock(this.resourceName);
			this.adminCmsObject.unlockResource(this.resourceName);
		}
	}
	
	/**
	 * Forces the lock of the file
	 * @throws Exception
	 */
	public void stealLockFile() throws Exception {
		this.unlockFile();
		this.adminCmsObject.lockResource(this.resourceName);
	}
	
	private CmsObject getAdminCmsObject() throws CmsException {
		if(this.adminCmsObject == null) {
			CmsObject _cmsObject =CmsWorkplaceAction.getInstance().getCmsAdminObject();
			this.adminCmsObject = OpenCms.initCmsObject(_cmsObject);
			this.adminCmsObject.getRequestContext().setCurrentProject(this.adminCmsObject.readProject("Offline"));
		}
		return this.adminCmsObject;
	}
}