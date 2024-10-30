package com.tfsla.diario.newsletters.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.diario.newsletters.common.ComplaintType;
import com.tfsla.diario.newsletters.common.GetSubscribersFilters;
import com.tfsla.diario.newsletters.common.INewsletterEventsService;
import com.tfsla.diario.newsletters.common.INewslettersService;
import com.tfsla.diario.newsletters.common.Newsletter;
import com.tfsla.diario.newsletters.common.NewsletterEvent;
import com.tfsla.diario.newsletters.common.NewsletterEventType;
import com.tfsla.diario.newsletters.common.NewsletterSubscription;
import com.tfsla.diario.newsletters.common.NewsletterSubscriptionStatus;
import com.tfsla.diario.newsletters.common.NewsletterUnsubscribeRequest;
import com.tfsla.diario.newsletters.data.NewsletterDAO;

public class NewslettersService implements INewslettersService {

	@Override
	public List<Newsletter> getNewsletters() throws Exception {
		NewsletterDAO dao = new NewsletterDAO();
		try {
			dao.openConnection();
			return dao.getNewsletters();
		} catch(Exception e) {
			LOG.error(e);
			throw e;
		} finally {
			dao.closeConnection();
		}
	}
	
	@Override
	public Newsletter getNewsletter(int newsletterID) throws Exception {
		NewsletterDAO dao = new NewsletterDAO();
		try {
			dao.openConnection();
			return dao.getNewsletterByID(newsletterID);
		} catch(Exception e) {
			LOG.error(e);
			throw e;
		} finally {
			dao.closeConnection();
		}
	}
	
	@Override
	public List<NewsletterSubscription> getSubscribers(GetSubscribersFilters filters) throws Exception {
		NewsletterDAO dao = new NewsletterDAO();
		try {
			dao.openConnection();
			return dao.getSubscribers(filters);
		} catch(Exception e) {
			LOG.error(e);
			throw e;
		} finally {
			dao.closeConnection();
		}
	}

	@Override
	public List<NewsletterSubscription> getNewsletterSubscriptions(int newsletterID) throws Exception {
		NewsletterDAO dao = new NewsletterDAO();
		try {
			dao.openConnection();
			return dao.getSubscribedToNewsletter(newsletterID);
		} catch(Exception e) {
			LOG.error(e);
			throw e;
		} finally {
			dao.closeConnection();
		}
	}

	@Override
	public List<String> getNewsletterSubscriptionsEmails(int newsletterID) throws Exception {
		NewsletterDAO dao = new NewsletterDAO();
		try {
			dao.openConnection();
			return dao.getEmailsSubscribedToNewsletter(newsletterID);
		} catch(Exception e) {
			LOG.error(e);
			throw e;
		} finally {
			dao.closeConnection();
		}
	}
	
	@Override
	public List<String> getNewsletterSubscriptionsEmails(int newsletterID, int startFrom, int batchSize) throws Exception {
		NewsletterDAO dao = new NewsletterDAO();
		try {
			dao.openConnection();
			return dao.getEmailsSubscribedToNewsletter(newsletterID, startFrom, batchSize);
		} catch(Exception e) {
			LOG.error(e);
			throw e;
		} finally {
			dao.closeConnection();
		}
	}

	@Override
	public NewsletterSubscription getNewsletterSubscription(String email, int newsletterID) throws Exception {
		NewsletterDAO dao = new NewsletterDAO();
		try {
			dao.openConnection();
			return dao.getNewsletterSubscription(email, newsletterID);
		} catch(Exception e) {
			LOG.error(e);
			throw e;
		} finally {
			dao.closeConnection();
		}
	}
	
	@Override
	public int subscribeToNewsletter(String email, int newsletterID) throws Exception {
		NewsletterDAO dao = new NewsletterDAO();
		try {
			
			dao.openConnection();
			NewsletterSubscription subscription = dao.getNewsletterSubscription(email, newsletterID);
			if(subscription == null) {
				dao.subscribe(email, newsletterID);
				return 1;
			} else {
				if (!subscription.getStatus().equals(NewsletterSubscriptionStatus.ACTIVE)) {
					subscription.setStatus(NewsletterSubscriptionStatus.ACTIVE);
					dao.update(subscription);
					return 2;
				}else 
					return 3;
			}
		} catch(Exception e) {
			if (e.getClass().toString().contains("MySQLIntegrityConstraintViolationException"))
				return 4;
			else {
				LOG.error(e);
				throw e;
			}
		} finally {
			dao.closeConnection();
		}
	}

	@Override
	public void unsubscribeEmail(String email) throws Exception {
		NewsletterDAO dao = new NewsletterDAO();
		try {
			dao.openConnection();
			dao.unsubscribeEmail(email);
		} catch(Exception e) {
			LOG.error(e);
			throw e;
		} finally {
			dao.closeConnection();
		}
	}
	
	@Override
	public void unsubscribeFromNewsletter(String email, int newsletterID) throws Exception {
		NewsletterDAO dao = new NewsletterDAO();
		try {
			dao.openConnection();
			dao.unsubscribe(email, newsletterID);
		} catch(Exception e) {
			LOG.error(e);
			throw e;
		} finally {
			dao.closeConnection();
		}
	}
	
	@Override
	public void unsubscribeFromNewsletter(String token) throws Exception {
		NewsletterUnsubscribeTokenManager tokenManager = new NewsletterUnsubscribeTokenManager();
		NewsletterUnsubscribeRequest request = tokenManager.decodeToken(token);
		this.unsubscribeFromNewsletter(request.getEmail(), request.getNewsletterID());
	}

	@Override
	public int getNewsletterSubscriptors(int newsletterID) throws Exception {
		NewsletterDAO dao = new NewsletterDAO();
		try {
			dao.openConnection();
			return dao.getNewsletterSubscriptors(newsletterID);
		} catch(Exception e) {
			LOG.error(e);
			throw e;
		} finally {
			dao.closeConnection();
		}
	}
	
	@Override
	public int getNewsletterDispatchesCount(int newsletterID) throws Exception {
		NewsletterDAO dao = new NewsletterDAO();
		try {
			dao.openConnection();
			return dao.getNewsletterDispatchesCount(newsletterID);
		} catch(Exception e) {
			LOG.error(e);
			throw e;
		} finally {
			dao.closeConnection();
		}
	}
	
	protected Log LOG = CmsLog.getLog(this);

	@Override
	public List<ComplaintType> getComplaintTypes() throws Exception {
		return this.getComplaintTypes(false);
	}
	
	@Override
	public List<ComplaintType> getComplaintTypes(Boolean onlyComplaints) throws Exception {
		if (_complaint_types == null) {
			_complaint_types = getStaticComplaintTypes();
		}
		if (onlyComplaints) {
			List<ComplaintType> filtered = new ArrayList<ComplaintType>();
			for (ComplaintType t : _complaint_types) {
				if (t.isComplaint()) {
					filtered.add(t);
				}
			}
			return filtered;
		}
		return _complaint_types;
	}
	
	@Override
	public ComplaintType getComplaintType(int complaintTypeID) throws Exception {
		if (_complaint_types == null) {
			_complaint_types = getStaticComplaintTypes();
		}
		for (ComplaintType t : _complaint_types) {
			if (t.getID() == complaintTypeID) {
				return t;
			}
		}
		return null;
	}

	@Override
	public void updateSubject(int newsletterID, String subject) throws Exception {
		NewsletterDAO dao = new NewsletterDAO();
		try {
			dao.openConnection();
			dao.updateSubject(newsletterID, subject);
		} catch(Exception e) {
			throw e;
		} finally {
			dao.closeConnection();
		}
	}

	@Override
	public void addComplaint(String token, int complaintTypeID, String message) throws Exception {
		NewsletterUnsubscribeTokenManager tokenManager = new NewsletterUnsubscribeTokenManager();
		NewsletterUnsubscribeRequest request = tokenManager.decodeToken(token);
		NewsletterDAO dao = new NewsletterDAO();
		try {
			dao.openConnection();
			Newsletter newsletter = dao.getNewsletterByID(request.getNewsletterID());
			ComplaintType complaintType = dao.getComplaintType(complaintTypeID);
			NewsletterSubscription subscription = dao.getNewsletterSubscription(request.getEmail(), request.getNewsletterID());
			dao.setSubscriptionComplaintType(subscription, complaintType);
			if (complaintType.isComplaint()) {
				INewsletterEventsService svc = NewsletterServiceContainer.getInstance(INewsletterEventsService.class);
				NewsletterEvent newsletterEvent = new NewsletterEvent();
				newsletterEvent.setFromEmail(newsletter.getEmailFrom());
				newsletterEvent.setEventType(NewsletterEventType.COMPLAINT);
				newsletterEvent.setToEmail(request.getEmail());
				newsletterEvent.setNewsletterID(request.getNewsletterID());
				newsletterEvent.setElement(String.valueOf(complaintTypeID));
				newsletterEvent.setEventData(message);
				svc.addEvent(newsletterEvent);
				
				svc.addEventSummary(newsletterEvent);
			}
		} catch(Exception e) {
			LOG.error(e);
			throw e;
		} finally {
			dao.closeConnection();
		}
	}
	
	protected static synchronized List<ComplaintType> getStaticComplaintTypes() throws Exception {
		NewsletterDAO dao = new NewsletterDAO();
		try {
			dao.openConnection();
			return dao.getComplaintTypes();
		} catch(Exception e) {
			throw e;
		} finally {
			dao.closeConnection();
		}
	}
	
	private static List<ComplaintType> _complaint_types = null;
}
