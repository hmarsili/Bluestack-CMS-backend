package com.tfsla.opencmsdev.module;

import org.opencms.db.CmsDbContext;
import org.opencms.db.I_CmsVfsDriver;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

import com.tfsla.exceptions.ApplicationException;
import com.tfsla.utils.CmsResourceUtils;

public class VfsDriverPropertyManager extends AbstractCmsPropertyManager {

	private I_CmsVfsDriver driver;
	private CmsProject project;
	private CmsDbContext dbc;
	
	public VfsDriverPropertyManager(I_CmsVfsDriver driver, CmsProject project, CmsDbContext dbc) {
		super();
		this.driver = driver;
		this.project = project;
		this.dbc = dbc;
	}

	public CmsProperty readPropertyObject(CmsResource resource, String propertyName) {
			
		try {
			return this.driver.readPropertyObject(this.dbc, propertyName, this.project, resource);
		}
		catch (CmsDataAccessException e) {
				throw new ApplicationException("No se pudo leer la property " + propertyName + " para el resource" + ((resource != null) ? CmsResourceUtils.getLink(resource) : "null"), e);
		}
	}

	public void writePropertyObject(CmsResource resource, CmsProperty property) {
		try {
			this.driver.writePropertyObject(this.dbc, this.project, resource, property);
		}
		catch (CmsException e) {
			String propertyName = property != null ? property.getName() : "null";
			String value = property != null ? property.getValue() : "null";
			String resourceName = resource != null ? CmsResourceUtils.getLink(resource) : "null";
			
			throw new ApplicationException("No se pudo escribir la property " + propertyName+ " = " + value +" para el resource" + resourceName, e);
		}
	}

	

}
