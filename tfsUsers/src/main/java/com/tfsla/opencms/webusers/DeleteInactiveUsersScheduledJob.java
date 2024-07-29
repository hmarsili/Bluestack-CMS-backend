package com.tfsla.opencms.webusers;

import java.util.Date;
import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.scheduler.I_CmsScheduledJob;

public class DeleteInactiveUsersScheduledJob implements I_CmsScheduledJob {

	public String launch(CmsObject cms, Map parameters) throws Exception {
		
		String ou = RegistrationModule.getInstance(cms).getOu();
		
		RegistrationModule.getInstance(cms).deletePendingUsers(cms,"/" + ou);

		return "Delete inactive users process executed at " + new Date();
	}
}