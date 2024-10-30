package com.tfsla.diario.newsletters.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.tfsla.diario.newsletters.common.ComplaintType;
import com.tfsla.diario.newsletters.common.GetSubscribersFilters;
import com.tfsla.diario.newsletters.common.Newsletter;
import com.tfsla.diario.newsletters.common.NewsletterSubscription;
import com.tfsla.diario.newsletters.common.strings.SqlQueries;
import com.tfsla.webusersposts.core.BaseDAO;

public class NewsletterDAO extends BaseDAO {

	protected static List<Newsletter> _newsletters = null;
	
	public List<Newsletter> getNewsletters() throws Exception {
		return getNewsletters(false);
	}
	
	public List<Newsletter> getNewsletters(Boolean fromCache) throws Exception {
		if(!fromCache || _newsletters == null) {
			PreparedStatement stmt = null;
			ResultSet rs = null;
			_newsletters = new ArrayList<Newsletter>();
			try {
				stmt = conn.prepareStatement(SqlQueries.GET_NEWSLETTERS);
				rs = stmt.executeQuery();
				while (rs.next()) {
					_newsletters.add(DataProcessor.getNewsletterFromRecord(rs));
				}
			} catch(Exception ex) {
				LOG.error("Error getting newsletters from database", ex);
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
		return _newsletters;
	}
	
	public List<String> getEmailsSubscribedToNewsletter(int newsletterID) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<String> ret = new ArrayList<String>();
		try {
			stmt = conn.prepareStatement(SqlQueries.GET_EMAILS_SUBSCRIBED_TO_NEWSLETTER);
			stmt.setInt(1, newsletterID);
			rs = stmt.executeQuery();
			while (rs.next()) {
				ret.add(rs.getString("EMAIL"));
			}
		} catch(Exception ex) {
			LOG.error("Error getting subscribers for newsletter ID " + newsletterID, ex);
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
	
	public List<String> getEmailsSubscribedToNewsletter(int newsletterID, int startFrom, int batchSize) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<String> ret = new ArrayList<String>();
		try {
			stmt = conn.prepareStatement(SqlQueries.GET_EMAILS_SUBSCRIBED_TO_NEWSLETTER_PAGED);
			stmt.setInt(1, newsletterID);
			stmt.setInt(2, startFrom);
			stmt.setInt(3, batchSize);
			rs = stmt.executeQuery();
			while (rs.next()) {
				ret.add(rs.getString("EMAIL"));
			}
		} catch(Exception ex) {
			LOG.error("Error getting subscribers for newsletter ID " + newsletterID, ex);
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
	
	public List<NewsletterSubscription> getSubscribers(GetSubscribersFilters filters) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<NewsletterSubscription> ret = new ArrayList<NewsletterSubscription>();
		String sqlQuery = SqlQueries.GET_SUBSCRIBERS;
		try {
			if (filters.getNewsletterID() > 0) {
				sqlQuery += SqlQueries.FILTER_AND + SqlQueries.NEWSLETTER_FILTER;
			}
			if (!filters.getSearchFilter().equals("")) {
				sqlQuery += SqlQueries.FILTER_AND + SqlQueries.EMAIL_FILTER;
			}
			if (filters.getStatus() >= 0) {
				sqlQuery += SqlQueries.FILTER_AND + SqlQueries.STATUS_FILTER;
			}
			if (filters.getPageSize() > 0) {
				sqlQuery += SqlQueries.LIMIT;
			}
			if (!filters.getOrderBy().equals("")) {
				sqlQuery += SqlQueries.ORDER_BY;
			}

			int filterIx = 1;
			stmt = conn.prepareStatement(sqlQuery);
			if (filters.getNewsletterID() > 0) {
				stmt.setInt(filterIx, filters.getNewsletterID());
				filterIx++;
			}
			if (!filters.getSearchFilter().equals("")) {
				String search = filters.getSearchFilter();
				if (!search.startsWith("%")) {
					search = "%" + search;
				}
				if (!search.endsWith("%")) {
					search += "%";
				}
				stmt.setString(filterIx, search);
				filterIx++;
			}
			if (filters.getStatus() >= 0) {
				stmt.setInt(filterIx, filters.getStatus());
				filterIx++;
			}
			if (filters.getPageSize() > 0) {
				stmt.setInt(filterIx, filters.getPageSize()*filters.getPageNumber());
				filterIx++;
				stmt.setInt(filterIx, filters.getPageSize());
				filterIx++;
			}
			if (!filters.getOrderBy().equals("")) {
				stmt.setString(filterIx, filters.getOrderBy());
				filterIx++;
			}
			
			rs = stmt.executeQuery();
			while (rs.next()) {
				NewsletterSubscription s = DataProcessor.getNewsletterSubscriptionFromRecord(rs);
				//s.setNewsletter(this.getNewsletterByID(rs.getInt("NEWSLETTER_ID")));
				ret.add(s);
			}
		} catch(Exception ex) {
			LOG.error("Error getting subscribers with filters, query: " + sqlQuery, ex);
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
	
	public List<NewsletterSubscription> getSubscribedToNewsletter(int newsletterID) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<NewsletterSubscription> ret = new ArrayList<NewsletterSubscription>();
		try {
			stmt = conn.prepareStatement(SqlQueries.GET_SUBSCRIBED_TO_NEWSLETTER);
			stmt.setInt(1, newsletterID);
			rs = stmt.executeQuery();
			while (rs.next()) {
				NewsletterSubscription s = DataProcessor.getNewsletterSubscriptionFromRecord(rs);
				s.setNewsletter(this.getNewsletterByID(newsletterID));
				ret.add(s);
			}
		} catch(Exception ex) {
			LOG.error("Error getting subscribers for newsletter ID " + newsletterID, ex);
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
	
	public int getNewsletterSubscriptors(int newsletterID) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.GET_NEWSLETTER_SUBSCRIPTORS);
			stmt.setInt(1, newsletterID);
			rs = stmt.executeQuery();
			while (rs.next()) {
				return rs.getInt("SUBSCRIPTORS");
			}
			
			return 0;
		} catch(Exception ex) {
			LOG.error("Error getting subscribers for newsletter ID " + newsletterID, ex);
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
	
	public int getNewsletterDispatchesCount(int newsletterID) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.GET_NEWSLETTER_DISPATCHES_COUNT);
			stmt.setInt(1, newsletterID);
			rs = stmt.executeQuery();
			while (rs.next()) {
				return rs.getInt("DISPATCHES");
			}
			
			return 0;
		} catch(Exception ex) {
			LOG.error("Error getting dispatches for newsletter ID " + newsletterID, ex);
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
	
	public Newsletter getNewsletterByID(int id) throws Exception {
		List<Newsletter> newsletters = this.getNewsletters();
		for(Newsletter newsletter : newsletters) {
			if(newsletter.getID() == id) return newsletter;
		}
		return null;
	}

	public void unsubscribeEmail(String email) {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.UNSUBSCRIBE_EMAIL);
			java.sql.Timestamp date = new java.sql.Timestamp(new Date().getTime());
			stmt.setTimestamp(1, date);
			stmt.setString(2, email);
			stmt.executeUpdate();
		} catch(Exception e) {
			LOG.error("Error trying to unsubscribe email " + email, e);
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
	
	public void unsubscribe(String email, int newsletterID) {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.UNSUBSCRIBE_FROM_NEWSLETTER);
			java.sql.Timestamp date = new java.sql.Timestamp(new Date().getTime());
			stmt.setTimestamp(1, date);
			stmt.setString(2, email);
			stmt.setInt(3, newsletterID);
			stmt.executeUpdate();
		} catch(Exception e) {
			LOG.error("Error trying to unsubscribe email " + email + " from newsletter " + newsletterID, e);
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
	
	public NewsletterSubscription getNewsletterSubscription(String email, int newsletterID) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		NewsletterSubscription ret = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.GET_NEWSLETTER_SUBSCRIPTOR);
			stmt.setInt(1, newsletterID);
			stmt.setString(2, email);
			rs = stmt.executeQuery();
			if (rs.next()) {
				ret = DataProcessor.getNewsletterSubscriptionFromRecord(rs);
				ret.setNewsletter(this.getNewsletterByID(newsletterID));
			}
		} catch(Exception ex) {
			LOG.error(String.format("Error getting subscriber for newsletter ID %s and email %s", newsletterID, email), ex);
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
	
	public void subscribe(String email, int newsletterID) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.SUBSCRIBE_TO_NEWSLETTER);
			stmt.setString(1, email);
			stmt.setInt(2, newsletterID);
			stmt.executeUpdate();
		} catch(Exception e) {
			LOG.error("Error trying to unsubscribe email " + email + " from newsletter " + newsletterID, e);
			throw e;
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
	
	public void update(NewsletterSubscription subscription) {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.UPDATE_NEWSLETTER_SUBSCRIPTION);
			java.sql.Timestamp date = new java.sql.Timestamp(new Date().getTime());
			stmt.setInt(1, subscription.getStatus().getValue());
			stmt.setTimestamp(2, date);
			stmt.setString(3, subscription.getEmail());
			stmt.setInt(4, subscription.getID());
			stmt.executeUpdate();
		} catch(Exception e) {
			LOG.error("Error trying to update subscription with email " + subscription.getEmail() + ", ID " + subscription.getID(), e);
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

	public List<ComplaintType> getComplaintTypes() throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<ComplaintType> ret = new ArrayList<ComplaintType>();
		try {
			stmt = conn.prepareStatement(SqlQueries.GET_COMPLAINT_TYPES + SqlQueries.GET_COMPLAINT_TYPES_ORDER);
			rs = stmt.executeQuery();
			while (rs.next()) {
				ComplaintType t = DataProcessor.getComplaintTypeFromRecord(rs);
				ret.add(t);
			}
		} catch(Exception ex) {
			LOG.error("Error getting complaint types ", ex);
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
	
	public ComplaintType getComplaintType(int complaintTypeID) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ComplaintType ret = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.GET_COMPLAINT_TYPE_BY_ID);
			stmt.setInt(1, complaintTypeID);
			rs = stmt.executeQuery();
			if (rs.next()) {
				ret = DataProcessor.getComplaintTypeFromRecord(rs);
			}
		} catch(Exception ex) {
			LOG.error("Error getting complaint type " + complaintTypeID, ex);
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
	
	public void setSubscriptionComplaintType(NewsletterSubscription subscription, ComplaintType complaintType) {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.UPDATE_NEWSLETTER_SUBSCRIPTION_COMPLAINT);
			stmt.setInt(1, complaintType.getID());
			stmt.setInt(2, subscription.getID());
			stmt.executeUpdate();
		} catch(Exception e) {
			LOG.error("Error trying to set subscription complaint with subscription ID " + subscription.getID() + " and complaint type ID " + complaintType.getID(), e);
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
	
	public void addComplaint(NewsletterSubscription subscription, ComplaintType complaintType, String message) {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.ADD_NEWSLETTER_COMPLAINT);
			stmt.setInt(1, subscription.getID());
			stmt.setInt(2, complaintType.getID());
			stmt.setString(3, message);
			stmt.executeUpdate();
		} catch(Exception e) {
			LOG.error("Error trying to insert complaint with subscription ID " + subscription.getID() + ", complaint type ID " + complaintType.getID() + " and message " + message, e);
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

	public void updateSubject(int newsletterID, String subject) {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.UPDATE_NEWSLETTER_SUBJECT);
			stmt.setString(1, subject);
			stmt.setInt(2, newsletterID);
			stmt.executeUpdate();
		} catch(Exception e) {
			LOG.error("Error trying to update subject, newsletterID: " + newsletterID + ", subject: " + subject, e);
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
}
