package com.tfsla.diario.webservices.helpers;

import org.opencms.file.CmsObject;
import org.opencms.lock.CmsLock;

public class VFSUnlockerHelper {

	/**
	 * Performs the 'steal lock' action for a VFS resource by forcing the acquire
	 * of the resource lock
	 * @param cms session CmsObject
	 * @param path the path for the VFS resource
	 * @throws Exception
	 */
	public static synchronized void stealLock(CmsObject cms, String path) throws Exception {
		CmsLock lock = cms.getLock(path);
		if(!lock.isUnlocked()) {
			cms.changeLock(path);
			cms.unlockResource(path);
		}
		cms.lockResource(path);
	}
}
