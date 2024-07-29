package com.tfsla.diario.analytics.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.tfsla.data.baseDAO;
import com.tfsla.diario.analytics.model.AnalyticsDataUpdatedDate;

public class AnalyticsDataUpdatedDateDAO extends baseDAO {

	public AnalyticsDataUpdatedDate getData(String sitename, int publication) throws Exception {
		AnalyticsDataUpdatedDate AnalyticsDataUpdatedDate = new AnalyticsDataUpdatedDate();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select SITENAME, PUBLICATION, AUTOMATIC_UPDATED_DATE"
					+ "from TFS_NEWS_ANALYTICS_UPDATEDDATE "
					+ "where  SITENAME=? and PUBLICATION=? and RESOURCE_PATH=?");

			stmt.setString(1, sitename);
			stmt.setInt(2, publication);
				
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				AnalyticsDataUpdatedDate newsAnalytics = fillData(rs);
			}

			rs.close();
			
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return AnalyticsDataUpdatedDate;
	}
	
	public void insetData(String sitename, int publication, Long date) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("update TFS_NEWS_ANALYTICS_UPDATEDDATE set AUTOMATIC_UPDATED_DATE=? where SITENAME=? and PUBLICATION=?)",Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, date);
			stmt.setString(2, sitename);
			stmt.setInt(3,publication);
			stmt.executeUpdate();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}
	
	public void updateData(AnalyticsDataUpdatedDate AnalyticsDataUpdatedDate) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("insert into TFS_NEWS_ANALYTICS_UPDATEDDATE (SITENAME, PUBLICATION, AUTOMATIC_UPDATED_DATE) values (?,?,?)",Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, AnalyticsDataUpdatedDate.getSitename());
			stmt.setInt(2, AnalyticsDataUpdatedDate.getPublication());
			stmt.setLong(3, AnalyticsDataUpdatedDate.getAutomaticUpdatedDate());
			stmt.executeUpdate();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}
	
	private AnalyticsDataUpdatedDate fillData(ResultSet rs) throws SQLException {
		AnalyticsDataUpdatedDate AnalyticsDataUpdatedDate = new AnalyticsDataUpdatedDate();
		AnalyticsDataUpdatedDate.setSitename(rs.getString("SITENAME"));
		AnalyticsDataUpdatedDate.setPublication(rs.getInt("PUBLICATION"));
		AnalyticsDataUpdatedDate.setAutomaticUpdatedDate(rs.getLong("AUTOMATIC_UPDATED_DATE"));

		return AnalyticsDataUpdatedDate;

	}

}