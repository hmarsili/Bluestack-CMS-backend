package com.tfsla.diario.analytics.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.tfsla.data.baseDAO;
import com.tfsla.diario.analytics.model.AnalyticsDateAutomatic;

public class AnalyticsDateAutomaticDAO extends baseDAO {

	public void updatedDate(AnalyticsDateAutomatic dataUpdated) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("update TFS_NEWS_ANALYTICS_UPDATEDDATE set AUTOMATIC_UPDATED_DATE=? where SITENAME=? and PUBLICATION=?)",Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, dataUpdated.getDateUpdated());
			stmt.setString(2, dataUpdated.getSitename());
			stmt.setInt(3, dataUpdated.getPublication());
			stmt.executeUpdate();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}
	
	public void insertDate(AnalyticsDateAutomatic dataUpdated) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("insert into TFS_NEWS_ANALYTICS_UPDATEDDATE (SITENAME, PUBLICATION, AUTOMATIC_UPDATED_DATE) values (?,?,?)",Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, dataUpdated.getSitename());
			stmt.setInt(2, dataUpdated.getPublication());
			stmt.setLong(3, dataUpdated.getDateUpdated());
			stmt.executeUpdate();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}
	
	public AnalyticsDateAutomatic getDate(String siteName, int publication) throws Exception {
		
		AnalyticsDateAutomatic newsAnalytics = null;
		
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("select * from TFS_NEWS_ANALYTICS_UPDATEDDATE where SITENAME = ? AND PUBLICATION = ?; ",
					Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, siteName);
			stmt.setInt(2, publication);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				newsAnalytics = fillData(rs);
			}

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return newsAnalytics;
	}
	
	
	private AnalyticsDateAutomatic fillData(ResultSet rs) throws SQLException {
		AnalyticsDateAutomatic AnalyticsDataUpdatedDate = new AnalyticsDateAutomatic();
		AnalyticsDataUpdatedDate.setSitename(rs.getString("SITENAME"));
		AnalyticsDataUpdatedDate.setPublication(rs.getInt("PUBLICATION"));
		AnalyticsDataUpdatedDate.setDateUpdated(rs.getLong("AUTOMATIC_UPDATED_DATE"));

		return AnalyticsDataUpdatedDate;

	}

}