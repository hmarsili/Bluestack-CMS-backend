package com.tfsla.diario.newsletters.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.tfsla.diario.newsletters.common.NewsletterDispatch;
import com.tfsla.diario.newsletters.common.strings.SqlQueries;
import com.tfsla.webusersposts.core.BaseDAO;

public class NewsletterDispatchDAO extends BaseDAO {
	
	public void addDispatch(NewsletterDispatch dispatch) {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.ADD_NEWSLETTER_DISPATCH);
			stmt.setInt(1, dispatch.getNewsletter().getID());
			stmt.setInt(2, dispatch.getSent());
			stmt.executeUpdate();
		} catch(Exception e) {
			LOG.error("Error trying to add newsletter dispatch ", e);
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

	public List<NewsletterDispatch> getDispatches(int newsletterID) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<NewsletterDispatch> dispatches = new ArrayList<NewsletterDispatch>();
		try {
			stmt = conn.prepareStatement(SqlQueries.GET_NEWSLETTER_DISPATCHES);
			rs = stmt.executeQuery();
			while (rs.next()) {
				dispatches.add(DataProcessor.getDispatchFromRecord(rs));
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
		return null;
	}
}
