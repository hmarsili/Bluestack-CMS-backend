package com.tfsla.diario.webservices.helpers;

import java.util.List;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;

import com.tfsla.diario.webservices.common.strings.ExceptionMessages;
import com.tfsla.diario.webservices.common.strings.StringConstants;

/**
 * Provides methdos to check user permissions within a CmsObject
 */
public class AuditPermissionsHelper {
	
	/**
	 * Checks if the current session-related CmsObject has the permissions required (or superior).
	 * If not, will throw an exception
	 * @param cms current session CmsObject instance
	 * @param permissionRequired required level to be checked
	 * @param module module to check permissions for
	 * @return actual permission level
	 * @throws Exception if the permission level is less than required
	 */
	@SuppressWarnings("unchecked")
	public synchronized static int checkUserPermission(CmsObject cms, int permissionRequired, String module) throws Exception {
		String permissionPreffix = String.format(
				StringConstants.PERMISSION_FORMAT,
				PublicationHelper.getCurrentPublication(cms),
				module
		);
		List<CmsGroup> groups = cms.getGroupsOfUser(cms.getRequestContext().currentUser().getName(), false);
		int permissionLevel = 0;
		for(CmsGroup group : groups) {
			if(group.getName().startsWith(permissionPreffix)) {
				String val = group.getName().split("_")[4];
				int currlevel = Integer.valueOf(val);
				if(currlevel > permissionLevel) {
					permissionLevel = currlevel;
				}
			}
		}
		if(permissionLevel < permissionRequired) {
			throw new Exception(ExceptionMessages.INSUFFICIENT_PRIVILEGES);
		}
		
		return permissionLevel;
	}
}
