package com.tfsla.diario.webservices.PushNotificationServices.jobs;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.webservices.WebPushService;
import com.tfsla.diario.webservices.PushNotificationServices.FirebasePushNotificationService;
import com.tfsla.diario.webservices.PushNotificationServices.PushNotificationsJobScheduler;
import com.tfsla.diario.webservices.PushNotificationServices.PushServiceConfiguration;
import com.tfsla.diario.webservices.common.PushConfiguration;
import com.tfsla.diario.webservices.common.PushItem;
import com.tfsla.diario.webservices.common.PushStatus;
import com.tfsla.diario.webservices.common.TopicConfiguration;
import com.tfsla.diario.webservices.common.strings.LogMessages;
import com.tfsla.diario.webservices.common.strings.PushNotificationTypes;
import com.tfsla.diario.webservices.data.PushNewsDAO;
import com.tfsla.diario.webservices.data.PushTopicDAO;
import com.tfsla.diario.webservices.helpers.PushConfigurationHelper;

import net.sf.json.JSONObject;

/**
 * Calls the provider services to push the notifications through their Push Services 
 */
public class PushNotificationsJob implements I_CmsScheduledJob {

	@SuppressWarnings("rawtypes")
	@Override
	public String launch(CmsObject cms, Map parameters) throws Exception {
		try {
			String publication = parameters.get("publication").toString();
			String site = cms.getRequestContext().getSiteRoot();
			
			String topic = parameters.get("topic").toString();
			
			if(!PushServiceConfiguration.isEnabled(site, publication)) {
				return String.format(LogMessages.PUSH_NOT_ENABLED, site);
			}
			
			TopicConfiguration config = PushServiceConfiguration.getConfiguration(topic);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			if(config.isInIdleTimeWindow(calendar)) {
				LOG.debug(LogMessages.PUSH_JOB_FINISHED_IDLE);
				return LogMessages.PUSH_JOB_FINISHED_IDLE;
			}
			
		
			PushItem item = this.getNewsToPush(cms, config);
		
			if(item == null) {
				LOG.debug(LogMessages.PUSH_JOB_FINISHED_NO_NEWS);
				return LogMessages.PUSH_JOB_FINISHED_NO_NEWS;
			}
			
			CmsResource resource = null;
			if (item.getStructureId()!=null)
				resource = cms.readResource(item.getStructureId());

			JSONObject jsonObject = WebPushService.getJson(cms, site, publication, item.getTitle(), item.getSubTitle(), config.getPushUrlParams() , item.getUrl() ,resource, item.getImage());
			
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
					result=false;
					LOG.error(LogMessages.PUSH_ITEM_ERROR, e);
					
					dao.setPushed(item.getId(), PushStatus.ERROR, LogMessages.PUSH_ITEM_AMZ_ERROR);
				}
			} catch (Exception e) {
				LOG.error(String.format(LogMessages.ERROR_REGISTERING_PUSH, ""), e);
			} finally {
				dao.closeConnection();
			}

			
			/*
			int failed = 0;
			int ok = 0;
			
			PushNewsDAO dao = new PushNewsDAO();
			try {
				dao.openConnection();
				for(String item : toPush.keySet()) {
					try {
						FirebasePushNotificationService service = new FirebasePushNotificationService(site, publication);
						service.setTopic("" + topic);
						service.pushMessage(jsonObject);
						ok++;
						dao.setPushed(item, cms.getRequestContext().getSiteRoot(), publication, pushType, PushStatus.PUSHED, "");
					} catch (Exception e) {
						LOG.error(LogMessages.PUSH_ITEM_ERROR, e);
						failed++;
						dao.setPushed(item, cms.getRequestContext().getSiteRoot(), publication, pushType, PushStatus.ERROR, LogMessages.PUSH_ITEM_AMZ_ERROR);
					}
				}
			} catch (Exception e) {
				LOG.error(String.format(LogMessages.ERROR_REGISTERING_PUSH, ""), e);
			} finally {
				dao.closeConnection();
			}
			
			
			LOG.debug(String.format(LogMessages.PUSH_JOB_FINISHED, toPush.size(), ok, failed));
			
			*/
			
			return String.format(LogMessages.PUSH_JOB_FINISHED, 1, (result ? 1: 0), (result ? 0: 1));
		} catch(Throwable ex) {
			LOG.error(LogMessages.PUSH_JOB_ERROR, ex);
			return LogMessages.PUSH_JOB_ERROR;
		}
	}
	
	/**
	 * Retrieves the news to be pushed from the DB (TFS_SOCIAL_PUSH table as a QUEUE)
	 * @param cms a CmsObject for the current context
	 * @param topic the publication where the news will be searched from
	 * @return a Hashtable with the structure ID and their title to be pushed
	 * @throws Throwable
	 */
	private PushItem getNewsToPush(CmsObject cms, TopicConfiguration config) throws Throwable {
		PushNewsDAO dao = new PushNewsDAO();
		Hashtable<String, Hashtable<String, String>> toPush = new Hashtable<String, Hashtable<String, String>>();
		
		
		
		try {
			
			dao.openConnection();
			
			List<PushItem> items = dao.getItemsToPush(
				config.getName(),
				PushStatus.PENDING,
				1
			);
			if (items.size()==1) {
				return items.get(0);
			}
			return null;
			
			/*
			Locale locale = cms.getRequestContext().getLocale();
			for(PushItem item : items) {
				CmsResource resource = cms.readResource(item.getStructureId());
				CmsFile file = cms.readFile(resource);
				CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, file);
				Hashtable<String, String> attributes = new Hashtable<String, String>();
				attributes.put("title", xmlContent.getStringValue(cms, "titulo", locale));
				attributes.put("url", file.getRootPath());
				toPush.put(item.getStructureIdAsString(), attributes);
			}
			
			*/
		} catch(Throwable ex) {
			LOG.error(LogMessages.PUSH_JOB_ERROR_ON_START, ex);
			throw ex;
		} finally {
			try {
				dao.closeConnection();
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			
			
		}
	}

	
	private Log LOG = CmsLog.getLog(this);
}
