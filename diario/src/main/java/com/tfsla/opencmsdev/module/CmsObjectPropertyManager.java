package com.tfsla.opencmsdev.module;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

import com.tfsla.exceptions.ApplicationException;
import com.tfsla.utils.CmsResourceUtils;

public class CmsObjectPropertyManager extends AbstractCmsPropertyManager {

	private CmsObject cms;

	public CmsObjectPropertyManager(CmsObject cms) {
		super();
		this.cms = cms;
	}

	
	public CmsProperty readPropertyObject(CmsResource resource, String propertyName) {
		
		try {
			boolean lock = CmsResourceUtils.lockResource(cms, resource, false);
			CmsProperty property = this.cms.readPropertyObject(resource, propertyName, false);
			if(lock) {
				CmsResourceUtils.unlockResource(cms, CmsResourceUtils.getLink(resource), false);
			}
			return property;
		}
		catch (CmsException e) {
				throw new ApplicationException("No se pudo leer la property " + propertyName + " para el resource" + ((resource != null) ? resource.getRootPath() : "null"), e);
		}
	}

	public void writePropertyObject(CmsResource resource, CmsProperty property) {
		boolean lock = CmsResourceUtils.lockResource(cms, resource, false);		
		try {
			this.cms.writePropertyObject("/" + CmsResourceUtils.getLink(resource), property);
			if(lock) {
				CmsResourceUtils.unlockResource(cms, CmsResourceUtils.getLink(resource), false);
			}
		}
		catch (CmsException e) {
			//Si fallo por el corto, busco por el largo
			try {
				this.cms.writePropertyObject(resource.getRootPath(), property);
				if(lock) {
					CmsResourceUtils.unlockResource(cms, resource.getRootPath(), false);
				}
			}
			catch (CmsException e1) {
				String propertyName = property != null ? property.getName() : "null";
				String value = property != null ? property.getValue() : "null";
				String resourceName = resource != null ? resource.getRootPath() : "null";
				String proyecto = cms.getRequestContext().currentProject().getDescription();
				
				throw new ApplicationException("No se pudo escribir la property " + propertyName+ " = " + value +" para el resource" + resourceName + " en el projecto: " + proyecto, e);
			}
			
			
		}
		catch(Exception e) {
			String propertyName = property != null ? property.getName() : "null";
			String value = property != null ? property.getValue() : "null";
			String resourceName = resource != null ? CmsResourceUtils.getLink(resource) : "null";
			
			throw new ApplicationException("No se pudo escribir la property " + propertyName+ " = " + value +" para el resource" + resourceName + " en el projecto: " + cms.getRequestContext().currentProject().getDescription(), e);
		}
	}

}