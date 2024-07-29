package org.opencms.scheduler;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRoleViolationException;

public class CmsJobScheduler {
	
	public static synchronized void scheduleJob(CmsScheduledJobInfo job, String cronExpression, CmsObject cms) throws CmsRoleViolationException, CmsSchedulerException {
		job.setFrozen(false);
		job.setCronExpression(cronExpression);
		OpenCms.getScheduleManager().scheduleJob(cms, job);
		job.setFrozen(true);
	}
}
