package com.tfsla.cdnIntegration.service.resourceManager;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import com.tfsla.cdnIntegration.model.PurgePackage;

public interface I_PugeCacheFileQueue {
	public void addResource(CmsObject cms, CmsResource resource) throws Exception;
	//public boolean checkResource(CmsObject cms,CmsResource resource,String site, String publication) throws Exception;
	public PurgePackage getNextResources(CmsObject cms, int count);
	public void updatePackageStatus(PurgePackage pac);
	
	public String getName();
	public I_PugeCacheFileQueue create();
	public I_PugeCacheFileQueue configure(String siteName, String publication);
	public void removeStatusDone(PurgePackage pac);
	public void removeOldPackages (int retries);
}
