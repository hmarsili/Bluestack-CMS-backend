package com.tfsla.diario.newsletters.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.tfsla.diario.newsletters.common.NewsletterEvent;
import com.tfsla.diario.newsletters.common.NewsletterEventStatistics;
import com.tfsla.diario.newsletters.common.NewsletterSubscriptionStatistics;
import com.tfsla.diario.newsletters.common.strings.SqlQueries;
import com.tfsla.webusersposts.core.BaseDAO;

public class NewsletterEventDAO extends BaseDAO {
	
	public void addEventSummary(NewsletterEvent newsletterEvent) {
		PreparedStatement stmt = null;
		try {
			Date today = new Date();
			Calendar cal = new GregorianCalendar();
			cal.setTime(today);
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			
			stmt = conn.prepareStatement(SqlQueries.ADD_NEWSLETTER_EVENT_SUMMARY);
			stmt.setInt(1, newsletterEvent.getNewsletterID());
			stmt.setInt(2, newsletterEvent.getEventType().getValue());
			stmt.setTimestamp(3, new Timestamp(cal.getTime().getTime()));
			
			stmt.executeUpdate();
		} catch(Exception e) {
			LOG.error("Error trying to add newsletter event", e);
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public void purgeStatistics(Date dateFrom, int newsletterID) {
		PreparedStatement stmt = null;
		try {
			
			String sqlQuery = SqlQueries.PURGE_NEWSLETTER_EVENT;
			String newsletterFilter = "";
			if (newsletterID > 0) {
				newsletterFilter = SqlQueries.FILTER_AND + SqlQueries.NEWSLETTER_FILTER;
			}
			sqlQuery = String.format(sqlQuery, newsletterFilter);
			
			stmt = conn.prepareStatement(sqlQuery);
			
			stmt.setTimestamp(1, new Timestamp(dateFrom.getTime()));
			if (newsletterID > 0) {
				stmt.setInt(2, newsletterID);
			}
			
			stmt.executeUpdate();
		} catch(Exception e) {
			LOG.error("Error trying to purge newsletter events", e);
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void addEvent(NewsletterEvent newsletterEvent) {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.ADD_NEWSLETTER_EVENT);
			stmt.setString(1, newsletterEvent.getFromEmail());
			stmt.setString(2, newsletterEvent.getToEmail());
			stmt.setInt(3, newsletterEvent.getEventType().getValue());
			stmt.setString(4, newsletterEvent.getEventData());
			stmt.setString(5, newsletterEvent.getElement());
			stmt.setInt(6, newsletterEvent.getNewsletterID());
			stmt.executeUpdate();
		} catch(Exception e) {
			LOG.error("Error trying to add newsletter event", e);
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public NewsletterSubscriptionStatistics getAudienceStatistics(Date dateFrom, int newsletterID) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = SqlQueries.GET_AUDIENCE_FROM_DATE;
		try {
			String newsletterFilter = "";
			if (newsletterID > 0) {
				newsletterFilter += SqlQueries.FILTER_AND + SqlQueries.NEWSLETTER_FILTER;
			}
			
			sqlQuery = String.format(sqlQuery, newsletterFilter);
			
			java.sql.Timestamp date = new java.sql.Timestamp(dateFrom.getTime());
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setTimestamp(1, date);
			if (newsletterID > 0) {
				stmt.setInt(2, newsletterID);
			}
			rs = stmt.executeQuery();
			NewsletterSubscriptionStatistics ret = new NewsletterSubscriptionStatistics();
			while (rs.next()) {
				if (rs.getInt("STATUS") == 1) {
					ret.setUsersCount(rs.getInt("USERS_COUNT"));
				}
				if (rs.getInt("STATUS") == 0) {
					ret.setDifference(rs.getInt("USERS_COUNT"));
				}
			}
			return ret;
		} catch(Exception ex) {
			LOG.error("Error getting audience from date, query: " + sqlQuery, ex);
			throw ex;
		} finally {
			try {
				stmt.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
			try {
				rs.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public List<NewsletterEventStatistics> getEventStatistics(Date dateFrom, int newsletterID) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<NewsletterEventStatistics> ret = new ArrayList<NewsletterEventStatistics>();
		String sqlQuery = SqlQueries.GET_NEWSLETTER_EVENTS_BY_DATE;
		try {
			int diffMonth = getMonthsDifference(dateFrom);
			if (diffMonth > 10) {
				sqlQuery = SqlQueries.GET_NEWSLETTER_EVENTS_BY_MONTH;
			}
			String newsletterFilter = "";
			if (newsletterID > 0) {
				newsletterFilter = SqlQueries.FILTER_AND + SqlQueries.NEWSLETTER_FILTER;
			}
			sqlQuery = String.format(sqlQuery, newsletterFilter);
			
			java.sql.Timestamp date = new java.sql.Timestamp(dateFrom.getTime());
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setTimestamp(1, date);
			if (newsletterID > 0) {
				stmt.setInt(2, newsletterID);
			}
			rs = stmt.executeQuery();
			while (rs.next()) {
				NewsletterEventStatistics e = DataProcessor.getNewsletterEventStatisticsFromRecord(rs);
				ret.add(e);
			}
		} catch(Exception ex) {
			LOG.error("Error getting event statistics from date, query: " + sqlQuery, ex);
			throw ex;
		} finally {
			try {
				stmt.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
			try {
				rs.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	public List<NewsletterEventStatistics> getEventSummaryStatistics(Date dateFrom, int newsletterID) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<NewsletterEventStatistics> ret = new ArrayList<NewsletterEventStatistics>();
		String sqlQuery = SqlQueries.GET_NEWSLETTER_EVENTS_SUMMARY_BY_DATE;
		try {
			int diffMonth = getMonthsDifference(dateFrom);
			if (diffMonth > 10) {
				sqlQuery = SqlQueries.GET_NEWSLETTER_EVENTS_SUMMARY_BY_MONTH;
			}
			String newsletterFilter = "";
			if (newsletterID > 0) {
				newsletterFilter = SqlQueries.FILTER_AND + SqlQueries.NEWSLETTER_FILTER;
			}
			sqlQuery = String.format(sqlQuery, newsletterFilter);
			
			java.sql.Timestamp date = new java.sql.Timestamp(dateFrom.getTime());
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setTimestamp(1, date);
			if (newsletterID > 0) {
				stmt.setInt(2, newsletterID);
			}
			rs = stmt.executeQuery();
			while (rs.next()) {
				NewsletterEventStatistics e = DataProcessor.getNewsletterEventStatisticsFromRecord(rs);
				ret.add(e);
			}
		} catch(Exception ex) {
			LOG.error("Error getting event summary statistics from date, query: " + sqlQuery, ex);
			throw ex;
		} finally {
			try {
				stmt.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
			try {
				rs.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	
	public List<NewsletterSubscriptionStatistics> getLostStatistics(Date dateFrom, int newsletterID) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<NewsletterSubscriptionStatistics> ret = new ArrayList<NewsletterSubscriptionStatistics>();
		String sqlQuery = SqlQueries.GET_NEWSLETTER_LOSTS_BY_DATE;
		try {
			int diffMonth = getMonthsDifference(dateFrom);
			if (diffMonth > 10){
				sqlQuery = SqlQueries.GET_NEWSLETTER_LOSTS_BY_MONTH;
			}
			String newsletterFilter = "";
			if (newsletterID > 0) {
				newsletterFilter = SqlQueries.FILTER_AND + SqlQueries.NEWSLETTER_FILTER;
			}
			sqlQuery = String.format(sqlQuery, newsletterFilter);
			
			java.sql.Timestamp date = new java.sql.Timestamp(dateFrom.getTime());
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setTimestamp(1, date);
			if (newsletterID > 0) {
				stmt.setInt(2, newsletterID);
			}
			rs = stmt.executeQuery();
			while (rs.next()) {
				NewsletterSubscriptionStatistics e = DataProcessor.getNewsletterSubscriptionStatisticsFromRecord(rs);
				ret.add(e);
			}
		} catch(Exception ex) {
			LOG.error("Error getting losts statistics from date, query: " + sqlQuery, ex);
			throw ex;
		} finally {
			try {
				stmt.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
			try {
				rs.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	public long getSubscriptionsUpTo(Date dateFrom, int newsletterID) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = SqlQueries.GET_NEWSLETTER_SUBSCRIBERS_UP_TO_DATE;
		try {
			if (newsletterID > 0) {
				sqlQuery += SqlQueries.FILTER_AND + SqlQueries.NEWSLETTER_FILTER;
			}
			java.sql.Timestamp date = new java.sql.Timestamp(dateFrom.getTime());
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setTimestamp(1, date);
			if (newsletterID > 0) {
				stmt.setInt(2, newsletterID);
			}
			rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getLong("USERS_COUNT");
			}
			throw new Exception("Error getting subscribers up to date, query: " + sqlQuery);
		} catch(Exception ex) {
			LOG.error("Error getting subscribers up to date, query: " + sqlQuery, ex);
			return 0;
		} finally {
			try {
				stmt.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
			try {
				rs.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public List<NewsletterSubscriptionStatistics> getSubscriptionStatistics(Date dateFrom, int newsletterID) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<NewsletterSubscriptionStatistics> ret = new ArrayList<NewsletterSubscriptionStatistics>();
		String sqlQuery = SqlQueries.GET_NEWSLETTER_SUBSCRIPTIONS_BY_DATE;
		try {
			int diffMonth = getMonthsDifference(dateFrom);
			if (diffMonth > 10){
				sqlQuery = SqlQueries.GET_NEWSLETTER_SUBSCRIPTIONS_BY_MONTH;
			}
			String newsletterFilter = "";
			if (newsletterID > 0) {
				newsletterFilter = SqlQueries.FILTER_AND + SqlQueries.NEWSLETTER_FILTER;
			}
			sqlQuery = String.format(sqlQuery, newsletterFilter);
			
			java.sql.Timestamp date = new java.sql.Timestamp(dateFrom.getTime());
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setTimestamp(1, date);
			if (newsletterID > 0) {
				stmt.setInt(2, newsletterID);
			}
			rs = stmt.executeQuery();
			while (rs.next()) {
				NewsletterSubscriptionStatistics e = DataProcessor.getNewsletterSubscriptionStatisticsFromRecord(rs);
				ret.add(e);
			}
		} catch(Exception ex) {
			LOG.error("Error getting subscriptions statistics from date, query: " + sqlQuery, ex);
			throw ex;
		} finally {
			try {
				stmt.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
			try {
				rs.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	public long getNewsletterPerformance(Date dateFrom) throws Exception {
		return this.getNewsletterPerformance(dateFrom, 0);
	}
	
	public long getNewsletterPerformance(Date dateFrom, int newsletterID) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sqlQuery = newsletterID == 0 ? SqlQueries.GET_NEWSLETTER_PERFORMANCE : SqlQueries.GET_NEWSLETTER_PERFORMANCE_FILTERED;
		try {
			java.sql.Timestamp date = new java.sql.Timestamp(dateFrom.getTime());
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setTimestamp(1, date);
			if (newsletterID > 0) {
				stmt.setInt(2, newsletterID);
			}
			rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getLong("SENT");
			}
			throw new Exception("Error getting newsletter performance from date, query: " + sqlQuery + ", newsletterID " + newsletterID);
		} catch(Exception ex) {
			LOG.error("Error getting newsletter performance from date, query: " + sqlQuery + ", newsletterID " + newsletterID, ex);
			throw ex;
		} finally {
			try {
				stmt.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
			try {
				rs.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public int getMonthsDifference(Date dateFrom) {
		Calendar endCalendar = Calendar.getInstance();
		Calendar startCalendar = Calendar.getInstance();
		startCalendar.setTime(dateFrom);
		int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
		return diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
	}

}
