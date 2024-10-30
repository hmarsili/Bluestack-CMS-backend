package com.tfsla.diario.newsletters.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.diario.newsletters.common.ComplaintType;
import com.tfsla.diario.newsletters.common.INewslettersService;
import com.tfsla.diario.newsletters.common.Newsletter;
import com.tfsla.diario.newsletters.common.NewsletterDispatch;
import com.tfsla.diario.newsletters.common.NewsletterEventStatistics;
import com.tfsla.diario.newsletters.common.NewsletterEventType;
import com.tfsla.diario.newsletters.common.NewsletterSubscription;
import com.tfsla.diario.newsletters.common.NewsletterSubscriptionStatistics;
import com.tfsla.diario.newsletters.common.NewsletterSubscriptionStatus;
import com.tfsla.diario.newsletters.service.NewsletterServiceContainer;

public class DataProcessor {
	
	public static synchronized Newsletter getNewsletterFromRecord(ResultSet rs) throws SQLException {
		Newsletter ret = new Newsletter();
		ret.setEmailFrom(rs.getString("EMAIL_FROM"));
		ret.setHtmlPath(rs.getString("HTML_PATH"));
		ret.setID(rs.getInt("REGISTER_ID"));
		ret.setJobName(rs.getString("JOB_NAME"));
		ret.setName(rs.getString("NAME"));
		ret.setPublication(rs.getString("PUBLICATION"));
		ret.setSubject(rs.getString("SUBJECT"));
		ret.setSite(rs.getString("SITE"));
		ret.setConfigSet(rs.getString("AMZ_CONFIG_SET"));
		return ret;
	}
	
	public static synchronized NewsletterSubscription getNewsletterSubscriptionFromRecord(ResultSet rs) throws SQLException {
		NewsletterSubscription ret = new NewsletterSubscription();
		ret.setEmail(rs.getString("EMAIL"));
		ret.setID(rs.getInt("REGISTER_ID"));
		ret.setStatus(NewsletterSubscriptionStatus.values()[rs.getInt("STATUS")]);
		ret.setSubscribed(rs.getTimestamp("SUBSCRIBED_DATE"));
		ret.setUpdated(rs.getTimestamp("UPDATED_DATE"));
		
		String complaintID = rs.getString("COMPLAINT_ID");
		if (complaintID != null && !complaintID.equals("")) {
			try {
				int complaintTypeID = Integer.valueOf(complaintID);
				INewslettersService svc = NewsletterServiceContainer.getInstance(INewslettersService.class);
				ret.setComplaintType(svc.getComplaintType(complaintTypeID));
			} catch(Exception e) {
				e.printStackTrace();
				LOG.error(e);
			}
		}
		return ret;
	}
	
	public static synchronized NewsletterEventStatistics getNewsletterEventStatisticsFromRecord(ResultSet rs) throws SQLException {
		NewsletterEventStatistics ret = new NewsletterEventStatistics();
		ret.setDate(rs.getDate("DATE_EVENT"));
		ret.setEventsCount(rs.getLong("EVENTS_COUNT"));
		ret.setEventType(NewsletterEventType.values()[rs.getInt("EVENT_TYPE")]);
		return ret;
	}
	
	public static synchronized NewsletterSubscriptionStatistics getNewsletterSubscriptionStatisticsFromRecord(ResultSet rs) throws SQLException {
		NewsletterSubscriptionStatistics ret = new NewsletterSubscriptionStatistics();
		ret.setDate(rs.getDate("DATE_EVENT"));
		ret.setDifference(rs.getLong("USERS_COUNT"));
		return ret;
	}

	public static NewsletterDispatch getDispatchFromRecord(ResultSet rs) throws SQLException {
		Newsletter n = new Newsletter();
		n.setID(rs.getInt("NEWSLETTER_ID"));
		NewsletterDispatch ret = new NewsletterDispatch();
		ret.setID(rs.getInt("REGISTER_ID"));
		ret.setNewsletter(n);
		ret.setOpened(rs.getInt("MAILS_OPENED"));
		ret.setSent(rs.getInt("MAILS_SENT"));
		ret.setRejected(rs.getInt("MAILS_REJECTED"));
		ret.setDate(rs.getDate("DISPATCH_DATE"));
		return ret;
	}

	public static ComplaintType getComplaintTypeFromRecord(ResultSet rs) throws SQLException {
		ComplaintType ret = new ComplaintType();
		ret.setID(rs.getInt("REGISTER_ID"));
		ret.setName(rs.getString("NAME"));
		ret.setIsComplaint(rs.getInt("IS_COMPLAINT") == 1);
		return ret;
	}
	
	protected static Log LOG = CmsLog.getLog(DataProcessor.class);
}
