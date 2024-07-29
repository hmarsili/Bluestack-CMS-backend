package com.tfsla.cdnIntegration.service.resourceManager;

import java.util.List;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;

import com.tfsla.cdnIntegration.data.PurgeQueueDAO;
import com.tfsla.cdnIntegration.model.PurgePackage;

public class DataBasePurgeQueue implements I_PugeCacheFileQueue {
 
	private static final Log LOG = CmsLog.getLog(DataBasePurgeQueue.class);

	private String siteName;
	private String publication;
	
	public synchronized void addResource(CmsObject cms,CmsResource resource) throws Exception {
		PurgeQueueDAO purgeDao = new PurgeQueueDAO(siteName,publication);
		purgeDao.insertResource(cms.getSitePath(resource));
	}
	
	public synchronized boolean checkResource(CmsObject cms,CmsResource resource,String site, String publication) throws Exception {
		PurgeQueueDAO purgeDao = new PurgeQueueDAO(siteName,this.publication);
		return purgeDao.existResource(cms.getSitePath(resource), site, publication);
	}

	public synchronized PurgePackage getNextResources(CmsObject cms, int count) {
		LOG.debug("Buscando paquete para purgar");
		PurgeQueueDAO purgeDao = new PurgeQueueDAO(siteName,publication);
		PurgePackage pac = null;
		try {
			pac = purgeDao.getNextPendingPackage();
			if (pac==null) {
				String processId = new CmsUUID().getStringValue();
				LOG.debug ("CDN - Cantidad de elementos:" + count + " , processID: " + processId );
				
				int size = purgeDao.createPackageList(processId, count);
				LOG.debug ("CDN - Cantidad de elementos modificados:" + size );
				if (size>0) {
					pac = purgeDao.createPackageHead(processId);
				}
			}
			
			if (pac!=null) {
				List<String> resNames = purgeDao.getResourcesFromPackage(pac.getProcessId());
				for (String name : resNames) {
					pac.getResources().add(cms.readResource(name, CmsResourceFilter.ALL));
				}
			}
		} catch (Exception e) {
			LOG.error("CDN - Buscando paquetes para purgar", e);
		}
		return pac;
	}

	public String getName() {
		return "database";
	}

	public I_PugeCacheFileQueue create() {
		return new DataBasePurgeQueue();
	}

	public synchronized I_PugeCacheFileQueue configure(String siteName, String publication) {
		
		this.siteName = siteName;
		this.publication = publication;
		return this;
	}

	public synchronized void updatePackageStatus(PurgePackage pac) {
		try {
			PurgeQueueDAO purgeDao = new PurgeQueueDAO(siteName,publication);
			purgeDao.updatePackageStatus(pac.getProcessId(), pac.getStatus(), pac.getRetries());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public synchronized void removeStatusDone(PurgePackage pac) {
		try {
			PurgeQueueDAO purgeDao = new PurgeQueueDAO(siteName,publication);
			purgeDao.removePackage(pac.getProcessId());
		} catch (Exception e) {
			CmsLog.getLog(this).error("CDN - Error al eliminar los registros exitosos de la tabla", e);
		}
		
	}

	public synchronized void removeOldPackages(int retries) {
		try {
			PurgeQueueDAO purgeDao = new PurgeQueueDAO(siteName,publication);
			purgeDao.removeOldPackages(retries);
		} catch (Exception e) {
			CmsLog.getLog(this).error("CDN - Error al eliminar los registros fallidos de la tabla", e);
		} 
		
	}

}
