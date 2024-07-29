package com.tfsla.opencmsdev.module;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;

import com.tfsla.opencms.util.PropertiesProvider;

public class UnlockResourceEventListener extends  SaveInEditorEventListener {

	private static String projectName;
	private static PropertiesProvider properties;

	private static void loadProperties() {
		properties = new PropertiesProvider(UnlockResourceEventListener.class, "unlockProject.properties");
		
		projectName = properties.get("afterUnlockProject");
		if (projectName == null) 
			projectName = "Offline";
	}
	
	public static String getProjectName()
	{
		if (properties==null)
			loadProperties();
		
		return projectName;
	}
	
	public UnlockResourceEventListener() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void doExecute(String url, CmsObject cmsObject) {
		
		try {
			if(!cmsObject.getLock(url).isUnlocked()) {
				
				try {
					cmsObject.unlockResource(url);
				}
	        	catch (Exception e)
	        	{
	        		
	                cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject(getProjectName()));

	        		cmsObject.changeLock(url);
	        		
	        		int flags = cmsObject.readResource(url, CmsResourceFilter.ALL).getFlags();
	                if ((flags & CmsResource.FLAG_TEMPFILE) == CmsResource.FLAG_TEMPFILE) {
	                    flags ^= CmsResource.FLAG_TEMPFILE;
	                    cmsObject.chflags(url, flags);
	                }
					cmsObject.unlockResource(url);
	        	}
			}
		}
		catch (CmsException e) {
			
		}

	}
}
