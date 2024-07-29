package com.tfsla.webusersnewspublisher.helper.expirenews;

import org.opencms.file.CmsObject;

/**
 * Executes all the validations to check if a resource can be expired or not 
 */
public class ExpireNewsValidator {
	
	/**
	 * Validates if a resource can be deleted or not
	 * @param deleteMode the mode desired to expire or delete the content
	 * @param cmsObject CmsObject of the current session
	 * @param resourcePath the path of the resource to be expired or deleted
	 * @param pageBuilder the path of the page builder to check if the resource is assigned there
	 * @return true if can be expired or not
	 * @throws Exception
	 */
	public Boolean validate(String deleteMode, CmsObject cmsObject, String resourcePath, String pageBuilder) throws Exception {
		if(deleteMode == null || deleteMode.equals("")) return false;
		
		//Expire directly, without requesting for approval
		if(deleteMode.equals(Strings.DIRECT)) return true;

		//Expire only if the resource is not displayed at any zone
		if(deleteMode.equals(Strings.ZONE)) {
			ZoneValidator zoneValidator = new ZoneValidator(cmsObject, resourcePath);
			if(!zoneValidator.validate()) return false;
			
			if(pageBuilder != null && !pageBuilder.equals("")) {
				PageBuilderValidator pageBuilderValidator = new PageBuilderValidator(cmsObject, pageBuilder);
				if(!pageBuilderValidator.validate(resourcePath)) return false;
			}
		}
		
		return true;
	}
}