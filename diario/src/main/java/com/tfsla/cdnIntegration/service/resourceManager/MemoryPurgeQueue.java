package com.tfsla.cdnIntegration.service.resourceManager;

import java.util.Iterator;
import java.util.LinkedHashSet;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

import com.tfsla.cdnIntegration.model.PurgePackage;

public class MemoryPurgeQueue implements I_PugeCacheFileQueue {

	private LinkedHashSet<CmsResource> filesToInvalidate = new LinkedHashSet<CmsResource>();
	
	public void addResource(CmsObject cms, CmsResource resource) {
		filesToInvalidate.add(resource);
	}

	public PurgePackage getNextResources(CmsObject cms, int count) {
		PurgePackage pac = new PurgePackage();
		
		int totalResources = filesToInvalidate.size();
		
		int size = (totalResources < count ? totalResources : count);
		
		Iterator<CmsResource> i = filesToInvalidate.iterator();
		for (int j=0;j<size;j++){
			CmsResource resource  = i.next();
			i.remove();
			pac.getResources().add(resource);
		}
		
		return pac;
	}

	public String getName() {
		return "inMemory";
	}

	public I_PugeCacheFileQueue create() {
		return new MemoryPurgeQueue();
	}

	public I_PugeCacheFileQueue configure(String siteName, String publication) {
		return this;
	}

	public void updatePackageStatus(PurgePackage pac) {
		//Nothing to do here...
		
	}

	public void removeStatusDone(PurgePackage pac) {
		//Nothing to do here...
		
	}

	public void removeOldPackages(int retries) {
		///Nothing to do here...
		
	}

	/*@Override
	public boolean checkResource(CmsObject cms, CmsResource resource, String site, String publication)
			throws Exception {
		return false;
	}*/

}
