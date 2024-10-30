package com.tfsla.diario.newsletters.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.diario.newsletters.common.INewsletterEventsService;
import com.tfsla.diario.newsletters.common.NewsletterEvent;
import com.tfsla.diario.newsletters.common.NewsletterEventStatistics;
import com.tfsla.diario.newsletters.common.NewsletterStatistics;
import com.tfsla.diario.newsletters.common.NewsletterSubscriptionStatistics;
import com.tfsla.diario.newsletters.data.NewsletterEventDAO;

public class NewsletterEventsService implements INewsletterEventsService {

	@Override
	public void addEventSummary(NewsletterEvent newsletterEvent) {
		NewsletterEventDAO dao = new NewsletterEventDAO();
		try {
			dao.openConnection();
			dao.addEventSummary(newsletterEvent);
		} catch(Exception e) {
			LOG.error("Error while saving SES/SNS event", e);
		} finally {
			try {
				dao.closeConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void addEvent(NewsletterEvent newsletterEvent) {
		NewsletterEventDAO dao = new NewsletterEventDAO();
		try {
			dao.openConnection();
			dao.addEvent(newsletterEvent);
		} catch(Exception e) {
			LOG.error("Error while saving SES/SNS event", e);
		} finally {
			try {
				dao.closeConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void purgeStatistics(Date dateFrom, int newsletterID ) {
		NewsletterEventDAO dao = new NewsletterEventDAO();
		try {
			dao.openConnection();
			dao.purgeStatistics(dateFrom, newsletterID );
		} catch(Exception e) {
			LOG.error("Error while saving SES/SNS event", e);
		} finally {
			try {
				dao.closeConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	@Override
	public List<NewsletterEventStatistics> getEventsStatistics(Date dateFrom, int newsletterID) throws Exception {
		NewsletterEventDAO dao = new NewsletterEventDAO();
		try {
			dao.openConnection();
			return dao.getEventSummaryStatistics(dateFrom, newsletterID);
		} catch(Exception e) {
			LOG.error("Error while getting event statistics", e);
			throw e;
		} finally {
			try {
				dao.closeConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public List<NewsletterSubscriptionStatistics> getSubscriptionStatistics() throws Exception {
		return this.getSubscriptionStatistics(this.getDefaultDate(), 0);
	}
	
	@Override
	public List<NewsletterSubscriptionStatistics> getSubscriptionStatistics(Date dateFrom, int newsletterID) throws Exception {
		NewsletterEventDAO dao = new NewsletterEventDAO();
		try {
			int monthsDifference = dao.getMonthsDifference(dateFrom);
			dao.openConnection();
			List<NewsletterSubscriptionStatistics> subscriptions = dao.getSubscriptionStatistics(dateFrom, newsletterID);
			List<NewsletterSubscriptionStatistics> losts = dao.getLostStatistics(dateFrom, newsletterID);
			List<NewsletterSubscriptionStatistics> ret = new ArrayList<NewsletterSubscriptionStatistics>();
			NewsletterSubscriptionStatistics prevRecord = null;
			Calendar c = Calendar.getInstance();
			Calendar now = Calendar.getInstance();
			now.setTime(new Date());
			c.setTime(dateFrom);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			c.add(Calendar.DATE, -1);
			
			//get users count up to the start date, then reset the date for filtering new events
			long currentCount = dao.getSubscriptionsUpTo(c.getTime(), newsletterID);
			c.add(Calendar.DATE, 1);
			
			int dateField = Calendar.DATE;
			if (monthsDifference > 10) {
				dateField = Calendar.MONTH;
			}
			
			//this huge processing is for filling intermediate days between the date filter
			//(those would be empty if no new subscribers / drops)
			for (Date date = c.getTime(); c.before(now); c.add(dateField, 1), date = c.getTime()) {
				NewsletterSubscriptionStatistics s = new NewsletterSubscriptionStatistics();
				NewsletterSubscriptionStatistics fromDB = null;
				NewsletterSubscriptionStatistics lostDB = null;
				s.setDate(date);

				//check new subscribers for this day
				Calendar temp = Calendar.getInstance();
				for (NewsletterSubscriptionStatistics subscription : subscriptions) {
					temp.setTime(subscription.getDate());
					if (DateUtils.isSameDay(c, temp)) {
						fromDB = subscription;
					}
				}
				
				//check if there are any drops for this day
				for (NewsletterSubscriptionStatistics lost : losts) {
					temp.setTime(lost.getDate());
					if (DateUtils.isSameDay(c, temp)) {
						lostDB = lost;
					}
				}
				
				//add new subscribers to the count
				s.setUsersCount(currentCount);
				if (fromDB != null) {
					s.setDifference(fromDB.getDifference());
					currentCount += fromDB.getDifference();
				}
				
				//remove drops from the count
				if (lostDB != null) {
					s.setDifference(Math.abs(s.getDifference() - lostDB.getDifference()));
					currentCount -= lostDB.getDifference();
					s.setUsersCount(currentCount);
				}
				
				if (currentCount > 0 || ret.size() > 0) {
					if (prevRecord != null) {
						long prevCount = prevRecord.getUsersCount() + prevRecord.getDifference();
						if (currentCount > prevCount) {
							s.setUsersCount(prevCount);
						}
					}
					prevRecord = s;
					ret.add(s);
				}
			}
			return ret;
		} catch(Exception e) {
			LOG.error("Error while getting subscription statistics", e);
			throw e;
		} finally {
			try {
				dao.closeConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public List<NewsletterStatistics> getStatistics() throws Exception {
		return this.getStatistics(getDefaultDate(), 0);
	}
	
	@Override
	public List<NewsletterStatistics> getStatistics(Date dateFrom, int newsletterID) throws Exception {
		List<NewsletterEventStatistics> stats = this.getEventsStatistics(dateFrom, newsletterID);
		List<NewsletterStatistics> ret = new ArrayList<NewsletterStatistics>();
		if(stats.size() == 0) return ret;
		
		NewsletterStatistics stat = new NewsletterStatistics();
		Date currentDate = stats.get(0).getDate();
		stat.setTimestamp(currentDate);
		for (NewsletterEventStatistics evt : stats) {
			if (!evt.getDate().equals(currentDate)) {
				ret.add(stat);
				currentDate = evt.getDate();
				stat = new NewsletterStatistics();
				stat.setTimestamp(currentDate);
			}
			switch (evt.getEventType()) {
				case BOUNCE: stat.setBounces(evt.getEventsCount()); break;
				case COMPLAINT: stat.setComplaints(evt.getEventsCount()); break;
				case SEND: stat.setDeliveryAttempts(evt.getEventsCount()); break;
				case REJECT: stat.setRejects(evt.getEventsCount()); break;
				case OPEN: stat.setOpen(evt.getEventsCount()); break;
				default: break;
			}
		}
		
		ret.add(stat);
		
		return ret;
	}
	
	protected Date getDefaultDate() {
		//Default: last 30 days
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, -30);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		return c.getTime();
	}
	
	protected Log LOG = CmsLog.getLog(this.getClass());
}