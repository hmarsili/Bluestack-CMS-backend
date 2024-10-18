package com.tfsla.diario.webservices.PushNotificationServices;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.webservices.common.PushConfiguration;
import com.tfsla.diario.webservices.common.PushItem;
import com.tfsla.diario.webservices.common.PushStatus;
import com.tfsla.diario.webservices.common.TopicConfiguration;
import com.tfsla.diario.webservices.common.model.PushItemModel;
import com.tfsla.diario.webservices.common.strings.ExceptionMessages;
import com.tfsla.diario.webservices.common.strings.PushNotificationTypes;
import com.tfsla.diario.webservices.core.services.TransactionableService;
import com.tfsla.diario.webservices.data.PushClientDAO;
import com.tfsla.diario.webservices.data.PushNewsDAO;
import com.tfsla.diario.webservices.data.PushTopicDAO;
import com.tfsla.diario.webservices.helpers.PublicationHelper;
import com.tfsla.diario.webservices.helpers.PushConfigurationHelper;
import com.tfsla.webusersposts.core.BaseDAO;

public class PushItemsService extends TransactionableService {
	
	
	
	public static synchronized int getPushClientsCountInPeriod(String site, String publication, Date from, Date to) throws Exception {
		PushClientDAO dao = new PushClientDAO();
		try {
			Boolean isSiteScope = PushConfigurationHelper.isSiteManaged(site, publication);
			dao.openConnection();
			return dao.getClientsCount(site, publication, !isSiteScope, from, to);
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				dao.closeConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static synchronized int getPushClientsCountInPeriod(String topic, String site, String publication, Date from, Date to) throws Exception {
		PushClientDAO dao = new PushClientDAO();
		try {
			Boolean isSiteScope = PushConfigurationHelper.isSiteManaged(site, publication);
			dao.openConnection();
			return dao.getClientsCount(topic, site, publication, !isSiteScope, from, to);
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				dao.closeConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static synchronized int getPushClientsCount(String site, String publication) throws Exception {
		PushClientDAO dao = new PushClientDAO();
		try {
			Boolean isSiteScope = PushConfigurationHelper.isSiteManaged(site, publication);
			dao.openConnection();
			return dao.getClientsCount(site, publication, !isSiteScope);
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				dao.closeConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Retrieves the CmsXmlContent instance related to a PushItem
	 * @param cms CmsObject instance for the session
	 * @param item the PushItem representing the push
	 * @return the CmsXmlContent related to the PushItem
	 * @throws CmsException
	 */
	public static synchronized CmsXmlContent getXmlContent(CmsObject cms, PushItem item) throws CmsException {
		if (item.getStructureId()==null)
			return null;
		CmsResource resource = cms.readResource(item.getStructureId());
		CmsFile file = cms.readFile(resource);
		return CmsXmlContentFactory.unmarshal(cms, file);
	}
	
	/**
	 * Updates the priority for a push item. Must call commit() after updating.
	 * @param pushId the REGISTER_ID for the push item
	 * @param priority the priority to be updated
	 * @throws Exception 
	 */
	public void updatePriority(int pushId, int priority) throws Exception {
		try {
			PushNewsDAO dao = (PushNewsDAO)this.transactionableDAO;
			dao.updatePriority(pushId, priority);
		} catch(Exception e) {
			e.printStackTrace();
			LOG.error(String.format(ExceptionMessages.ERROR_UPDATING_PUSH_PRIORITY, pushId), e);
			throw e;
		}
	}
	
	/**
	 * Removes a push from the database and their job from the scheduler
	 * @param pushId the ID of the push
	 * @param cms the CmsObject instance for the current session 
	 * @throws Exception
	 */
	public void unschedulePush(String pushId, CmsObject cms) throws Exception {
		PushNewsDAO dao = new PushNewsDAO();
		try {
			dao.openConnection();
			PushItem pushItem = dao.getPushItem(pushId);
			if(pushItem.getPushType().equals(PushNotificationTypes.INMEDIATO) || pushItem.getPushType().equals(PushNotificationTypes.PROGRAMADO)) {
				PushNotificationsJobScheduler scheduler = new PushNotificationsJobScheduler();
				scheduler.unscheduleJob(pushItem, cms);
			}
			dao.removePush(pushId);
		} catch(Exception e) {
			e.printStackTrace();
			LOG.error(String.format(ExceptionMessages.ERROR_UNSCHEDULING_PUSH_JOB, pushId), e);
			throw e;
		} finally {
			dao.closeConnection();
		}
	}
	
	public String enqueueItem(CmsObject cms, String pushId) throws Exception {
		PushNewsDAO dao = new PushNewsDAO();
		try {
			dao.openConnection();
			return dao.copyPushItem(pushId, cms.getRequestContext().currentUser().getName());
		} catch(Exception e) {
			e.printStackTrace();
			LOG.error(String.format(ExceptionMessages.ERROR_UNSCHEDULING_PUSH_JOB, pushId), e);
			throw e;
		} finally {
			dao.closeConnection();
		}
	}
	
	public PushItemModel getPushItem(String pushId, CmsObject cms) throws Exception {
		PushNewsDAO dao = new PushNewsDAO();
		try {
			dao.openConnection();
			return new PushItemModel(dao.getPushItem(pushId), cms);
		} catch(Exception e) {
			e.printStackTrace();
			LOG.error(String.format(ExceptionMessages.ERROR_RETRIEVING_PUSH_ITEM, pushId), e);
			throw e;
		} finally {
			dao.closeConnection();
		}
	}
	
		
	public List<PushItemModel> getPushSchedule(CmsObject cms, String topic, HttpSession session) {
		PushNewsDAO dao = new PushNewsDAO();
		PushTopicDAO pDao = new PushTopicDAO();
		
		List<PushItem> items = null;
		List<PushItemModel> ret = new ArrayList<PushItemModel>();
		try {
			String site = cms.getRequestContext().getSiteRoot();
			String publication = PublicationHelper.getFromSession(cms, session);
			
			dao.openConnection();
			
			
			
			items = dao.getAllPushItems(site, publication, topic, PushStatus.PENDING);
			
			pDao.openConnection();
			TopicConfiguration topicConfig = pDao.getTopicConfiguration(topic);
			
			Date date = new Date();
			Boolean skipReSchedule = false;
			for(PushItem item : items) {
				ret.add(new PushItemModel(item, cms));
				if(item.getPushType().equals(PushNotificationTypes.EN_COLA)) {
					Date nextExecution = topicConfig.getNextExecutionForDate(date, skipReSchedule);
					if(this.dayChanged(nextExecution, date)) skipReSchedule = true;
					
					item.setDateScheduled(nextExecution);
					date = nextExecution;
				}
			}
			sort(ret);
		} catch(Exception e) {
			e.printStackTrace();
			LOG.error(ExceptionMessages.ERROR_RETRIEVING_PUSH_SCHEDULE, e);
		} finally {
			pDao.closeConnection();
			dao.closeConnection();
		}
		return ret;
	}
	
	/**
	 * Retrieves the current push schedule (sorted by scheduled date)
	 * @param cms the CmsObject instance for the current session
	 * @param session current HttpSession to get publication for
	 * @return a List of PushItem instances sorted by their scheduled date 
	 */
	public List<PushItemModel> getPushSchedule(CmsObject cms, HttpSession session) {
		PushNewsDAO dao = new PushNewsDAO();
		PushTopicDAO pDao = new PushTopicDAO();
		
		Map<String,TopicConfiguration> configs = new HashMap<String,TopicConfiguration>();
		Map<String,Date> nextExecutions = new HashMap<String,Date>();
		Map<String,Boolean> skipReSchedules = new HashMap<String,Boolean>();
		
		List<PushItem> items = null;
		List<PushItemModel> ret = new ArrayList<PushItemModel>();
		try {
			String site = cms.getRequestContext().getSiteRoot();
			String publication = PublicationHelper.getFromSession(cms, session);
			
			dao.openConnection();
			pDao.openConnection();
			Boolean isSiteScope = PushConfigurationHelper.isSiteManaged(site, publication);
			items = dao.getAllPushItems(site, publication, PushStatus.PENDING, !isSiteScope);
			PushConfiguration pushConfig = PushServiceConfiguration.getConfiguration(cms);
			Date date = new Date();
			Boolean skipReSchedule = false;
			for(PushItem item : items) {
				ret.add(new PushItemModel(item, cms));
				if(item.getPushType().equals(PushNotificationTypes.EN_COLA)) {
					Date nextExecution = null;
					if (item.getTopic()!=null) {
						TopicConfiguration config = configs.get(item.getTopic());
						Date nextDate = nextExecutions.get(item.getTopic());
						Boolean skip = skipReSchedules.get(item.getTopic());
						
						if (skip==null)
							skip = false;
						
						if (config==null) {
							config = pDao.getTopicConfiguration(item.getTopic());
							configs.put(item.getTopic(),config);
						}
						
						if (nextDate==null) {
							nextDate = new Date();
							skip=Boolean.FALSE;
						}
						
						nextExecution = config.getNextExecutionForDate(nextDate, skip);
						
						if(this.dayChanged(nextExecution, nextDate)) 
							skipReSchedules.put(item.getTopic(),Boolean.TRUE);
						
						nextExecutions.put(item.getTopic(),nextExecution);
					
						item.setDateScheduled(nextExecution);
					}
					else {
						nextExecution = pushConfig.getNextExecutionForDate(date, skipReSchedule);
						if(this.dayChanged(nextExecution, date)) skipReSchedule = true;
						
						item.setDateScheduled(nextExecution);
						date = nextExecution;
					}
					
				}
			}
			sort(ret);
		} catch(Exception e) {
			e.printStackTrace();
			LOG.error(ExceptionMessages.ERROR_RETRIEVING_PUSH_SCHEDULE, e);
		} finally {
			pDao.closeConnection();
			dao.closeConnection();
		}
		return ret;
	}
	
	

	/**
	 * Retrieves the history of the latest pushed items
	 * @param cms the CmsObject instance for the current session
	 * @param session current HttpSession to get publication from
	 * @return a List of PushItem instances sorted by their pushed date 
	 * @throws Exception 
	 */
	public List<PushItemModel> getAdvancedPushHistory(CmsObject cms, HttpSession session, String topic, String type, String user, Date from, Date to, String text, Integer count ) throws Exception {
		PushNewsDAO dao = new PushNewsDAO();
		List<PushItem> items = null;
		List<PushItemModel> ret = new ArrayList<PushItemModel>();
		String site = cms.getRequestContext().getSiteRoot();

		String publication = PublicationHelper.getFromSession(cms, session);

		Boolean isSiteScope = PushConfigurationHelper.isSiteManaged(site, publication);
				
		if (isSiteScope)
			publication = null;
		
		try {
			dao.openConnection();
			items = dao.getPushedItems(site, publication, topic, type, user, from, to, text, count);
			for(PushItem item : items) {
				try {
					ret.add(new PushItemModel(item, cms));
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			LOG.error(ExceptionMessages.ERROR_RETRIEVING_PUSH_SCHEDULE, e);
		} finally {
			dao.closeConnection();
		}
		return ret;
	}

	
	public int getPushedCount(CmsObject cms, HttpSession session, String topic, String type, Date from, Date to ) throws Exception {
		int count = 0;
		
		PushNewsDAO dao = new PushNewsDAO();
		
		String site = cms.getRequestContext().getSiteRoot();
		String publication = PublicationHelper.getFromSession(cms, session);
		Boolean isSiteScope = PushConfigurationHelper.isSiteManaged(site, publication);
		
		if (isSiteScope)
			publication = null;
		
		try {
			dao.openConnection();
			count = dao.getPushedItemsCount(topic, site, publication, type, from, to);
			
		} catch(Exception e) {
			e.printStackTrace();
			LOG.error(ExceptionMessages.ERROR_RETRIEVING_PUSH_SCHEDULE, e);
		} finally {
			dao.closeConnection();
		}
		return count;
		
	}
	
	/**
	 * Retrieves the history of the latest pushed items
	 * @param cms the CmsObject instance for the current session
	 * @param session current HttpSession to get publication from
	 * @return a List of PushItem instances sorted by their pushed date 
	 * @throws Exception 
	 */
	public List<PushItemModel> getPushHistory(CmsObject cms, HttpSession session) throws Exception {
		PushNewsDAO dao = new PushNewsDAO();
		List<PushItem> items = null;
		List<PushItemModel> ret = new ArrayList<PushItemModel>();
		String site = cms.getRequestContext().getSiteRoot();
		String publication = PublicationHelper.getFromSession(cms, session);
		Boolean isSiteScope = PushConfigurationHelper.isSiteManaged(site, publication);
		try {
			dao.openConnection();
			items = dao.getPushedItems(site, publication, !isSiteScope);
			for(PushItem item : items) {
				try {
					ret.add(new PushItemModel(item, cms));
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			LOG.error(ExceptionMessages.ERROR_RETRIEVING_PUSH_SCHEDULE, e);
		} finally {
			dao.closeConnection();
		}
		return ret;
	}
	
	/**
	 * Sorts a List of PushItem instances ascending by their scheduled date
	 * @param items
	 */
	public static synchronized void sort(List<PushItemModel> items) {
		Collections.sort(items, new PushItemsDateComparator());
	}
	
	public static class PushItemsDateComparator implements Comparator<PushItemModel> {
	    @Override
	    public int compare(PushItemModel o1, PushItemModel o2) {
	    	if(o1 == null || o1.getScheduledDate() == null) return -1;
	    	if(o2 == null || o2.getScheduledDate() == null) return 1;
	        return o1.getItem().getDateScheduled().compareTo(o2.getItem().getDateScheduled());
	    }
	}
	
	private Boolean dayChanged(Date date1, Date date2) {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.setTime(date1);
		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(date2);
		
		return calendar1.get(Calendar.DAY_OF_MONTH) != calendar2.get(Calendar.DAY_OF_MONTH);
	}
	
	private Log LOG = CmsLog.getLog(this);

	@Override
	protected BaseDAO getTransactionableDAO() {
		return new PushNewsDAO();
	}
}
