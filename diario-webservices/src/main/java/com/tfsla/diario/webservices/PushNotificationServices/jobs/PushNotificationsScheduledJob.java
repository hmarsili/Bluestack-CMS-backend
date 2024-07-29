package com.tfsla.diario.webservices.PushNotificationServices.jobs;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.I_CmsScheduledJob;

import com.tfsla.diario.webservices.WebPushService;
import com.tfsla.diario.webservices.PushNotificationServices.FirebasePushNotificationService;
import com.tfsla.diario.webservices.PushNotificationServices.PushNotificationsJobScheduler;
import com.tfsla.diario.webservices.PushNotificationServices.PushServiceConfiguration;
import com.tfsla.diario.webservices.common.PushItem;
import com.tfsla.diario.webservices.common.PushStatus;
import com.tfsla.diario.webservices.common.TopicConfiguration;
import com.tfsla.diario.webservices.common.strings.LogMessages;
import com.tfsla.diario.webservices.data.PushNewsDAO;

import net.sf.json.JSONObject;

/**
 * Calls the provider services to push the notifications through their Push Services 
 */
public class PushNotificationsScheduledJob implements I_CmsScheduledJob {

	@SuppressWarnings("rawtypes")
	@Override
	public String launch(CmsObject cms, Map parameters) throws Exception {
		try {
			String publication = parameters.get("publication").toString();
			String site = cms.getRequestContext().getSiteRoot();
			
			String topic = parameters.get("topic").toString();
			String pushId =  parameters.get("pushId").toString();
			
			if(!PushServiceConfiguration.isEnabled(site, publication)) {
				return String.format(LogMessages.PUSH_NOT_ENABLED, site);
			}
			
			TopicConfiguration config = PushServiceConfiguration.getConfiguration(topic);
			
			
			PushItem item = this.getNewsToPush(cms, pushId);
			
			
			if(item == null) {
				LOG.debug(LogMessages.PUSH_JOB_FINISHED_NO_NEWS);
				return LogMessages.PUSH_JOB_FINISHED_NO_NEWS;
			}
			
			CmsResource resource = null;
			if (item.getStructureId()!=null)
				resource = cms.readResource(item.getStructureId());

			JSONObject jsonObject = WebPushService.getJson(cms, site, publication, item.getTitle(), item.getSubTitle(), config.getPushUrlParams(), item.getUrl(), resource, item.getImage());

			
			//Legacy: remove it at some point by using payload
			//WebPushService.updateJson(cms, jsonObject, site, publication);
			LOG.debug(String.format(LogMessages.PUSH_JOB_NEWS, 1));
			
			// sleep for a minute, wait CDN to clean the JSON cache -> should be also removed by using payload 
			//Thread.sleep(60000);
			
			boolean result = true;
			PushNewsDAO dao = new PushNewsDAO();
			try {
				dao.openConnection();
				
				try {
					FirebasePushNotificationService service = new FirebasePushNotificationService(site, publication);
					service.setTopic("" + topic);
					service.pushMessage(jsonObject);
					dao.setPushed(item.getId(), PushStatus.PUSHED, "");
				} catch (Exception e) {
					result = false;
					LOG.error(LogMessages.PUSH_ITEM_ERROR, e);
					dao.setPushed(item.getId(), PushStatus.ERROR, LogMessages.PUSH_ITEM_AMZ_ERROR);				}
			
			} catch (Exception e) {
				LOG.error(String.format(LogMessages.ERROR_REGISTERING_PUSH, ""), e);
			} finally {
				dao.closeConnection();
			}
			
			String jobName = parameters.get("jobName").toString();
			LOG.debug("Unscheduling job " + jobName);
			PushNotificationsJobScheduler scheduler = new PushNotificationsJobScheduler();
			scheduler.unscheduleJob(jobName, cms);
			LOG.debug("Finished unscheduling job " + jobName);
		
			//LOG.debug(String.format(LogMessages.PUSH_JOB_FINISHED, toPush.size(), ok, failed));
			
			// valido si la noticia tiene en la property pushQueueId el id del push. De ser así lo elimino.
			com.tfsla.utils.CmsResourceUtils.forceLockResource(cms,resource.getRootPath());
			
			String newsPath =  cms.getRequestContext().removeSiteRoot(resource.getRootPath());
			
			CmsProperty propPushId = cms.readPropertyObject(newsPath, "pushId", false);      
			String propPushIdStr = (propPushId != null) ? propPushId.getValue() : null;
			
			if (propPushIdStr != null) {
				String pushsIdsToNew = "";
			
				if (propPushIdStr.contains(pushId)) {
					if (propPushIdStr.contains(",")) {
					String propPushIdSpl[] = propPushIdStr.split(",");
					for (int i =0; i < propPushIdSpl.length; i ++ ) {
						if (!propPushIdSpl[i].equals(pushId)) {
							pushsIdsToNew += (i > 0) ? ","+pushsIdsToNew : pushsIdsToNew ;
						}
					}
					}
				}
				
				CmsProperty prop =  new CmsProperty("pushQueueId", null,pushsIdsToNew);
				cms.writePropertyObject(newsPath,prop);
				
				OpenCms.getPublishManager().publishResource(cms, newsPath);
			}
			
			com.tfsla.utils.CmsResourceUtils.unlockResource(cms,resource.getRootPath(), false);
			
			LOG.info(String.format(LogMessages.PUSH_QUEUE_JOB_SCHEDULE_FINISHED, resource.getRootPath(), topic, pushId ));
			
			return String.format(LogMessages.PUSH_JOB_FINISHED, 1, (result ? 1: 0), (result ? 0: 1));
		} catch(Throwable ex) {
			LOG.error(LogMessages.PUSH_JOB_ERROR, ex);
			return LogMessages.PUSH_JOB_ERROR;
		}
	}
	
	
	private PushItem getNewsToPush(CmsObject cms, String pushId) throws Throwable {
		PushNewsDAO dao = new PushNewsDAO();
		
		try {
			
			dao.openConnection();
			
			PushItem item = dao.getPushItem(pushId);
			
			return item;
		
			
		} catch(Throwable ex) {
			LOG.error(LogMessages.PUSH_JOB_ERROR_ON_START, ex);
			
		} finally {
			try {
				dao.closeConnection();
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			
			
		}
		
		return null;
	}
	
	private Log LOG = CmsLog.getLog(this);
}
