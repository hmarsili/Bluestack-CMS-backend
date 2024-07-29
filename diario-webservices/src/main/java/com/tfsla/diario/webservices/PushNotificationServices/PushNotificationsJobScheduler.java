package com.tfsla.diario.webservices.PushNotificationServices;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsSchedulerConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.workplace.CmsWorkplaceAction;

import com.tfsla.diario.webservices.PushNotificationServices.jobs.PushNotificationsJob;
import com.tfsla.diario.webservices.PushNotificationServices.jobs.PushNotificationsScheduledJob;
import com.tfsla.diario.webservices.common.PushItem;
import com.tfsla.diario.webservices.common.PushRequest;
import com.tfsla.diario.webservices.common.strings.ExceptionMessages;
import com.tfsla.diario.webservices.common.strings.LogMessages;
import com.tfsla.diario.webservices.common.strings.PushNotificationTypes;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.data.PushNewsDAO;
import com.tfsla.diario.webservices.helpers.PublicationHelper;
import com.tfsla.diario.webservices.helpers.PushTypeHelper;

public class PushNotificationsJobScheduler {
	
	public static synchronized void scheduleScheduledPush(CmsObject cms, HttpSession session, String topic, String title, String subtitle, String url, CmsResource resource, Date scheduledDate, String image) throws Exception
	{
		
		String publication = PublicationHelper.getFromSession(cms, session);
		
		schedulePush(cms, publication, topic, title, subtitle, url, resource, scheduledDate, image);
	}
	
	public static synchronized void schedulePush(CmsObject cms, String publication, String topic, String title, String subtitle, String url, CmsResource resource, Date scheduledDate, String image) throws Exception
	{
		Locale locale = cms.getRequestContext().getLocale();
		
		String site = cms.getRequestContext().getSiteRoot();
				
		//If push is not enabled return.
		if(!PushServiceConfiguration.isEnabled(site, publication)) 
				return;
		
		String pushType = PushNotificationTypes.PROGRAMADO;
		if (scheduledDate==null) {
			pushType = PushNotificationTypes.INMEDIATO;
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.MINUTE, 2);
			scheduledDate = cal.getTime();
		}

		String userName = cms.getRequestContext().currentUser().getName();
		String jobName = String.format("Push '%s' in topic %s on %s", title, topic, scheduledDate );
		
		if (resource!=null)
			LOG.debug(String.format(LogMessages.PUSH_DATED_JOB_SCHEDULE_STARTED, resource.getRootPath()));
		
		PushNewsDAO dao = new PushNewsDAO();
		
		try {
			
			if (cms.readResource(image).getState().isNew()) {
				CmsResource parentFolder = cms.readResource(CmsResource.getParentFolder(image)); 
				publishParents(cms,parentFolder);
				OpenCms.getPublishManager().publishResource(cms, image);
			}
			
			dao.openConnection();
													
			String pushId = dao.addPushedNew(
					(resource!=null ? resource.getStructureId().toString() : null), 
					pushType, 
					userName, 
					title, 
					subtitle, 
					url, 
					scheduledDate, 
					topic, 
					site, 
					publication, 
					jobName,
					image);

			CmsScheduledJobInfo job = new CmsScheduledJobInfo();
			

			job.setJobName(jobName);
			job.setClassName(PushNotificationsScheduledJob.class.getName());
			job.setCronExpression(getCronExpression(pushType, scheduledDate, cms, locale));
			job.setActive(true);
			
			SortedMap<String, String> params = new TreeMap<String, String>();
			params.put("publication", publication);
			params.put("topic", topic);
			params.put("pushType", pushType);
			params.put("pushId", pushId);
			params.put("jobName", jobName);
			job.setParameters(params);
			
			CmsContextInfo contextInfo = new CmsContextInfo(cms.getRequestContext());
			CmsWorkplaceAction action = CmsWorkplaceAction.getInstance();
			contextInfo.setUserName(
				action.getCmsAdminObject().getRequestContext().currentUser().getName()
			);
			job.setContextInfo(contextInfo);
			
			OpenCms.getScheduleManager().scheduleJob(cms, job);
	        OpenCms.writeConfiguration(CmsSchedulerConfiguration.class);

	        if (resource!=null) {
	        	// guardo en la propery pushId el id del push, para poder reconocerlo desde el frontend. Solo los programados. 
	        
	        	if (pushType.equals(PushNotificationTypes.PROGRAMADO)){
	        		String newsPath =  cms.getRequestContext().removeSiteRoot(resource.getRootPath());
	        		
	        		LOG.info("status" + resource.getState().toString());
	        		
	        		boolean isPublish = (resource.getState().toString().equals("0")) ?  true : false;
	        				
					com.tfsla.utils.CmsResourceUtils.forceLockResource(cms,newsPath);
					
					CmsProperty propPushId = cms.readPropertyObject(newsPath, "pushId", false);      
					String propPushIdStr = (propPushId != null) ? propPushId.getValue() : null;
					if (propPushIdStr == null) 
						propPushIdStr = pushId; 
					else 
						propPushIdStr = propPushIdStr+","+pushId;
					
					CmsProperty prop =  new CmsProperty("pushId", null,propPushIdStr);
					cms.writePropertyObject(newsPath,prop);
		
					LOG.info("isPublish" + isPublish);
					
					if (isPublish) {
						OpenCms.getPublishManager().publishResource(cms, newsPath);
					}
					
					
					
					com.tfsla.utils.CmsResourceUtils.unlockResource(cms,newsPath, false);
	        	}
	        	
	        	LOG.info(String.format(LogMessages.PUSH_DATED_JOB_SCHEDULE_FINISHED, resource.getRootPath(), topic, pushId ));
	        }
	        
		} catch(Exception e) {
			LOG.error(ExceptionMessages.ERROR_PUSH_JOB, e);
		} finally {
			try {
				dao.closeConnection();
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}

	}
	
	static private void publishParents(CmsObject cms, CmsResource folder) {
		
		try {
			if (folder.getState().isNew()) {
				CmsResource parentFolder;
				
					parentFolder = cms.readResource(CmsResource.getParentFolder(cms.getSitePath(folder))); 
				
				if (parentFolder.getState().isNew()) {
					publishParents(cms,parentFolder);
				}
				OpenCms.getPublishManager().publishResource(cms, cms.getSitePath(folder));
			}
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static synchronized void scheduleQueuePush(CmsObject cms, HttpSession session, String topic, String title, String subtitle, String url, CmsResource resource, Date scheduledDate, String image) throws Exception
	{
		
		String publication = PublicationHelper.getFromSession(cms, session);
		
		scheduleQueue(cms, publication, topic, title, subtitle, url, resource, image);
	}
	
	static public synchronized void scheduleQueue(CmsObject cms, String publication, String topic, String title, String subtitle, String url, CmsResource resource, String image) throws Exception
	{
		String site = cms.getRequestContext().getSiteRoot();
		
		//If push is not enabled return.
		if(!PushServiceConfiguration.isEnabled(site, publication)) 
				return;
		
		if (resource!=null)
			LOG.debug(String.format(LogMessages.PUSH_QUEUE_JOB_SCHEDULE_STARTED, resource.getRootPath()));
		
		String pushType = PushNotificationTypes.EN_COLA;
		String userName = cms.getRequestContext().currentUser().getName();
		
		PushNewsDAO dao = new PushNewsDAO();
		
		try {
			
			if (cms.readResource(image).getState().isNew()) {
				CmsResource parentFolder = cms.readResource(CmsResource.getParentFolder(image)); 
				publishParents(cms,parentFolder);
				OpenCms.getPublishManager().publishResource(cms, image);
			}
			
			dao.openConnection();
									
			String pushId = dao.addPushedNewWithTopic(
					(resource!=null ? resource.getStructureId().toString() : null) , 
					pushType, 
					title, 
					subtitle,
					url,
					userName,
					topic, 
					site, 
					publication,
					image
					);
			
			if (resource!=null) 
				LOG.info(String.format(LogMessages.PUSH_QUEUE_JOB_SCHEDULE_FINISHED, resource.getRootPath(), topic, pushId ));
				
		} catch(Exception e) {
			e.printStackTrace();
			LOG.error(ExceptionMessages.ERROR_PUSH_JOB, e);
		} finally {
			try {
				dao.closeConnection();
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Generates and schedules a job to push the resources, by using the site and publication into the CmsObject param
	 * @param cms the CmsObject of the calling process
	 * @param resources a List of the resources to be pushed
	 * @throws Exception 
	 */
	@Deprecated
	public static synchronized void schedulePushJob(CmsObject cms, List<CmsResource> resources) throws Exception {
		String site = cms.getRequestContext().getSiteRoot();
		schedulePushJob(site, PublicationHelper.getCurrentPublication(site), cms, resources);
	}
	
	/**
	 * Generates and schedules a job to push the resources
	 * @param site the site of the resources
	 * @param publication the site of the resources
	 * @param cms the CmsObject of the calling process
	 * @param resources a List of the resources to be pushed
	 * @throws Exception 
	 */
	@Deprecated
	public static synchronized void schedulePushJob(String site, String publication, CmsObject cms, List<CmsResource> resources) throws Exception {
		if(!PushServiceConfiguration.isEnabled(site, publication)) return;
		
		List<PushRequest> requests = new ArrayList<PushRequest>();
		for(CmsResource resource : resources) {
			PushRequest request = PushRequest.getRequestFromResource(cms, resource, site, publication);
			requests.add(request);
		}
		
		schedulePushRequests(cms, requests);
	}

	/**
	 * Schedules the requests to be pushed to mobile platforms
	 * @param cms session CmsObject instance
	 * @param requests List of requests to be scheduled
	 * @throws CmsException
	 */
	@Deprecated
	public static synchronized void schedulePushRequests(CmsObject cms, List<PushRequest> requests) throws CmsException {
		schedulePushRequests(cms, requests, false);
	}
	
	/**
	 * Schedules the requests to be pushed to mobile platforms
	 * @param cms session CmsObject instance
	 * @param requests List of requests to be scheduled
	 * @param skipPushedCheck if true, skips the check to validate if the item was pushed already
	 * @throws CmsException
	 */
	@Deprecated
	public static synchronized JSONArray schedulePushRequests(CmsObject cms, List<PushRequest> requests, Boolean skipPushedCheck) throws CmsException {
		Locale locale = cms.getRequestContext().getLocale();
		LOG.debug(String.format(LogMessages.PUSH_JOB_SCHEDULE_STARTED, requests.size()));
		int scheduledItems = 0;
		int errors = 0;
		PushNewsDAO dao = new PushNewsDAO();
		JSONArray ret = new JSONArray();
		JSONObject item = new JSONObject();
		
		try {
			dao.openConnection();
			
			for(PushRequest request : requests) {
				if(!PushServiceConfiguration.isEnabled(request.getSite(), request.getPublication())) return null;
				
				item = new JSONObject();
				item.put("path", request.getPath());
				
				try {
					String pushType = request.getPushType();
					String userName = cms.getRequestContext().currentUser().getName();
					if(pushType == null || pushType.trim().equals("") || pushType.equals(PushNotificationTypes.NINGUNO)) {
						item.put(StringConstants.RESULT, StringConstants.SKIPPED);
						item.put(StringConstants.MESSAGE, String.format(LogMessages.PUSH_DISABLED, pushType));
						continue;
					}
					
					if(!skipPushedCheck && dao.isPushed(request.getStructureId().toString())) {
						item.put(StringConstants.RESULT, StringConstants.SKIPPED);
						item.put(StringConstants.MESSAGE, String.format(LogMessages.ITEM_ALREADY_PUSHED, request.getStructureId().toString(), skipPushedCheck));
						continue;
					}
					
					if(pushType.equals(PushNotificationTypes.EN_COLA)) {
						String pushId = dao.addPushedNew(request.getStructureId().toString(), request.getSite(), request.getPublication(), pushType, request.getTitle(), userName);
						scheduledItems++;
						item.put(StringConstants.ID, pushId);
						item.put(StringConstants.RESULT, StringConstants.OK);
						continue;
					}
					
					String jobName = String.format("Push '%s'", request.getTitle());
					Date date = request.getPushDate();
					String pushId = dao.addPushedNew(request.getStructureId(), request.getSite(), request.getPublication(), pushType, userName, request.getTitle(), date, jobName);
					
					CmsScheduledJobInfo job = new CmsScheduledJobInfo();
					job.setJobName(jobName);
					job.setClassName(PushNotificationsJob.class.getName());
					job.setCronExpression(getCronExpression(pushType, date, cms, locale));
					job.setActive(true);
					
					SortedMap<String, String> params = new TreeMap<String, String>();
					params.put("publication", request.getPublication());
					params.put("pushType", pushType);
					params.put("resourceName", request.getPath());
					params.put("jobName", jobName);
					job.setParameters(params);
					
					CmsContextInfo contextInfo = new CmsContextInfo(cms.getRequestContext());
					CmsWorkplaceAction action = CmsWorkplaceAction.getInstance();
					contextInfo.setUserName(
						action.getCmsAdminObject().getRequestContext().currentUser().getName()
					);
					job.setContextInfo(contextInfo);
					
					OpenCms.getScheduleManager().scheduleJob(cms, job);
			        OpenCms.writeConfiguration(CmsSchedulerConfiguration.class);
			        LOG.debug(String.format(LogMessages.PUSH_JOB_SCHEDULED, jobName, request.getTitle()));
			        scheduledItems++;
			        item.put(StringConstants.ID, pushId);
			        item.put(StringConstants.RESULT, StringConstants.OK);
				} catch(CmsException e) {
					item.put(StringConstants.RESULT, StringConstants.ERROR);
					e.printStackTrace();
					throw e;
				} catch(Exception e) {
					item.put(StringConstants.RESULT, StringConstants.ERROR);
					e.printStackTrace();
					LOG.error(String.format(ExceptionMessages.ERROR_SCHEDULING_PUSH_JOB, request.getTitle()), e);
					errors++;
				} finally {
					ret.add(item);
				}
			}
			LOG.debug(String.format(LogMessages.PUSH_JOB_SCHEDULE_FINISHED, scheduledItems, errors));
		} catch(Exception e) {
			e.printStackTrace();
			LOG.error(ExceptionMessages.ERROR_PUSH_JOB, e);
		} finally {
			try {
				dao.closeConnection();
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		
		return ret;
	}
	
	/**
	 * Removes a scheduled push
	 * @param pushItem the PushItem instance representing the push
	 * @param cms session CmsObject instance
	 * @throws CmsRoleViolationException
	 */
	public void unscheduleJob(PushItem pushItem, CmsObject cms) throws CmsRoleViolationException {
		this.unscheduleJob(pushItem.getJobName(), cms);
	}
	
	/**
	 * Removes a scheduled push
	 * @param jobName the jobName to be unscheduled
	 * @param cms session CmsObject instance
	 * @throws CmsRoleViolationException
	 */
	public void unscheduleJob(String jobName, CmsObject cms) throws CmsRoleViolationException {
		CmsScheduledJobInfo job = this.getJobByName(jobName, cms);
		if(job == null) return;
		OpenCms.getScheduleManager().unscheduleJob(cms, job.getId());
		OpenCms.writeConfiguration(CmsSchedulerConfiguration.class);
	}
	
	/**
	 * Retrieves a job by providing the job name
	 * @param jobName the job name
	 * @param cms session CmsObject instance
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public CmsScheduledJobInfo getJobByName(String jobName, CmsObject cms) {
		List jobs = OpenCms.getScheduleManager().getJobs();
		for(Object job : jobs) {
			CmsScheduledJobInfo jobInfo = (CmsScheduledJobInfo)job;
			if(jobInfo.getJobName().equals(jobName)) return jobInfo;
		}
		return null;
	}
	
	/**
	 * Generates a cron expression based on the date for the push and the push type
	 * @param pushType the type of push
	 * @param xmlContent the CmsResource Xml Content
	 * @param cms process CmsObject
	 * @param locale a Locale to be used to parse the date
	 * @return a String containing a Cron expression to schedule the push
	 * @throws Exception 
	 */
	private synchronized static String getCronExpression(String pushType, Date pushDate, CmsObject cms, Locale locale) throws Exception {
		Calendar calendar = Calendar.getInstance();
		if(pushDate.getTime() <= new Date().getTime()) {
			throw new Exception(ExceptionMessages.ERROR_SCHEDULED_DATE);
		}
		calendar.setTime(pushDate);
		
		return ""
            + calendar.get(Calendar.SECOND)
            + " "
            + calendar.get(Calendar.MINUTE)
            + " "
            + calendar.get(Calendar.HOUR_OF_DAY)
            + " "
            + calendar.get(Calendar.DAY_OF_MONTH)
            + " "
            + (calendar.get(Calendar.MONTH) + 1)
            + " "
            + "?"
            + " "
            + calendar.get(Calendar.YEAR);
	}
	
	private static Log LOG = CmsLog.getLog(PushNotificationsJobScheduler.class);
}