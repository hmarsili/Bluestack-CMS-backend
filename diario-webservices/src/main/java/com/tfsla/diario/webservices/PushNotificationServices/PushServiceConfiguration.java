package com.tfsla.diario.webservices.PushNotificationServices;

import java.util.List;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.CmsJobScheduler;
import org.opencms.scheduler.CmsScheduledJobInfo;

import com.tfsla.diario.webservices.WebPushService;
import com.tfsla.diario.webservices.PushNotificationServices.jobs.PushNotificationsJob;
import com.tfsla.diario.webservices.common.PushConfiguration;
import com.tfsla.diario.webservices.common.TopicConfiguration;
import com.tfsla.diario.webservices.common.strings.LogMessages;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.common.strings.TimeUnits;
import com.tfsla.diario.webservices.data.PushTopicDAO;
import com.tfsla.diario.webservices.helpers.PublicationHelper;

public class PushServiceConfiguration {
	

	private static Log LOG = CmsLog.getLog(PushServiceConfiguration.class);
	
	public static synchronized void updateConfiguration(PushConfiguration config, CmsObject cms) throws Exception {
		String site = cms.getRequestContext().getSiteRoot();
		String publication = PublicationHelper.getCurrentPublication(cms);
		_config.setParam(site, publication, StringConstants.WEBSERVICES_MODULE_NAME, "pushFromHour", String.valueOf(config.getFromHour()));
		_config.setParam(site, publication, StringConstants.WEBSERVICES_MODULE_NAME, "pushFromMinutes", String.valueOf(config.getFromMinutes()));
		_config.setParam(site, publication, StringConstants.WEBSERVICES_MODULE_NAME, "pushToHour", String.valueOf(config.getToHour()));
		_config.setParam(site, publication, StringConstants.WEBSERVICES_MODULE_NAME, "pushToMinutes", String.valueOf(config.getToMinutes()));
		_config.setParam(site, publication, StringConstants.WEBSERVICES_MODULE_NAME, "pushInterval", String.valueOf(config.getInterval()));
		OpenCms.writeConfiguration(CmsMedios.getInstance().getClass());
		CmsScheduledJobInfo job = getPushJob(site, publication);
		CmsJobScheduler.scheduleJob(job, String.format("0 0/%s * * * ?", config.getInterval()), cms);
		//job.setCronExpression(String.format("0 0/%s * * * ?", config.getInterval()));
		//OpenCms.getScheduleManager().scheduleJob(cms, job);
	}
	
	public static synchronized TopicConfiguration getConfiguration(String topicName) {
		PushTopicDAO tDAO = new PushTopicDAO();
			tDAO.openConnection();
		try {
			return tDAO.getTopicConfiguration(topicName);
		} catch (Exception e) {
			LOG.error("Error obtaining the topic configuration", e);
		} finally {
			tDAO.closeConnection();
		}
		return null;
	}
	
	public static synchronized PushConfiguration getConfiguration(CmsObject cms) {
		PushConfiguration config = new PushConfiguration();
		String site = cms.getRequestContext().getSiteRoot();
		String publication = PublicationHelper.getCurrentPublication(cms);
		config.setFromHour(Integer.parseInt(_config.getParam(site, publication, StringConstants.WEBSERVICES_MODULE_NAME, "pushFromHour")));
		config.setFromMinutes(Integer.parseInt(_config.getParam(site, publication, StringConstants.WEBSERVICES_MODULE_NAME, "pushFromMinutes")));
		config.setToHour(Integer.parseInt(_config.getParam(site, publication, StringConstants.WEBSERVICES_MODULE_NAME, "pushToHour")));
		config.setToMinutes(Integer.parseInt(_config.getParam(site, publication, StringConstants.WEBSERVICES_MODULE_NAME, "pushToMinutes")));
		config.setInterval(Integer.parseInt(_config.getParam(site, publication, StringConstants.WEBSERVICES_MODULE_NAME, "pushInterval")));
		config.setIntervalUnit(TimeUnits.MINUTES);
		config.setIsJobScheduled(true);
		try {
			CmsScheduledJobInfo job = getPushJob(site, publication);
			if(job == null || !job.isActive()) config.setIsJobScheduled(false);
		} catch(Exception e) {
			config.setIsJobScheduled(false);
		}
		return config;
	}
	
	@SuppressWarnings("rawtypes")
	public static synchronized CmsScheduledJobInfo getPushJob(String site, String publication) {
		List jobs = OpenCms.getScheduleManager().getJobs();
		for(Object job : jobs) {
			CmsScheduledJobInfo jobInfo = (CmsScheduledJobInfo)job;
			if(jobInfo.getClassName().equals(PushNotificationsJob.class.getName())) {
				if(jobInfo.getCronExpression().endsWith(" * * ?")) {
					if(jobInfo.getContextInfo().getSiteRoot().startsWith(site)) {
						if(jobInfo.getParameters().containsKey(StringConstants.PUBLICATION) && jobInfo.getParameters().get(StringConstants.PUBLICATION).equals(publication)) {
							return jobInfo;
						}
					}
				}
			}
		}
		return null;
	}
	
	public static synchronized Boolean isEnabled(String site, String publication) {
		return WebPushService.isEnabled(site, publication);
	}
	
	public static String getGCMSenderId(CmsObject cms) {
		if(GCM_ID == null) {
			String site = cms.getRequestContext().getSiteRoot();
			String publication = PublicationHelper.getCurrentPublication(cms);
			GCM_ID = _config.getParam(site, publication, StringConstants.WEBSERVICES_MODULE_NAME, "gcmSenderId");
		}
		return GCM_ID;
	}
	
	public static String getModuleName() {
		return StringConstants.WEBSERVICES_MODULE_NAME;
	}
	
	private static String GCM_ID = null;
	private static CPMConfig _config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
}
