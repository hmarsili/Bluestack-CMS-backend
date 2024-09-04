package com.tfsla.diario.analytics.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tfsla.data.baseDAO;
import com.tfsla.diario.analytics.model.NewsAnalyticsData;
import com.tfsla.diario.analytics.model.NewsAnalyticsData;

public class NewsAnalyticsDataDAO extends baseDAO {

	public Map<String,NewsAnalyticsData> getPubNewsDataToMap(String sitename, int publication) throws Exception {

		Map<String,NewsAnalyticsData> dataMap = new HashMap<>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select SITENAME, PUBLICATION, PAGE, CTR, CLICKS, POSITION, PRINTS, UPDATED_DATE "
					+ "from TFS_NEWS_ANALYTICS "
					+ "where  SITENAME=? and PUBLICATION=?");

			stmt.setString(1, sitename);
			stmt.setInt(2, publication);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				//eliminar sitio y pub que no se usa por el front. 
				NewsAnalyticsData newsAnalytics = fillData(rs);
				dataMap.put(newsAnalytics.getPage(),newsAnalytics);
			}

			rs.close();
			
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return dataMap;
	}
	
	public List<NewsAnalyticsData> getPubNewsData(String sitename, int publication) throws Exception {
		List<NewsAnalyticsData> NewsAnalyticsData2 = new ArrayList<NewsAnalyticsData>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select SITENAME, PUBLICATION, PAGE, CTR, CLICKS, POSITION, PRINTS, UPDATED_DATE "
					+ "from TFS_NEWS_ANALYTICS "
					+ "where  SITENAME=? and PUBLICATION=?");

			stmt.setString(1, sitename);
			stmt.setInt(2, publication);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				NewsAnalyticsData newsAnalytics = fillData(rs);
				NewsAnalyticsData2.add(newsAnalytics);
			}

			rs.close();
			
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return NewsAnalyticsData2;
	}
	
	public NewsAnalyticsData getNewsData(String sitename, int publication, String resourcePath) throws Exception {
		NewsAnalyticsData NewsAnalyticsData2 = new NewsAnalyticsData();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select SITENAME, PUBLICATION, PAGE, CTR, CLICKS, POSITION, PRINTS, UPDATED_DATE "
					+ "from TFS_NEWS_ANALYTICS "
					+ "where  SITENAME=? and PUBLICATION=? and PAGE=?");

			stmt.setString(1, sitename);
			stmt.setInt(2, publication);
			stmt.setString(3, resourcePath);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				NewsAnalyticsData2 = fillData(rs);
			}

			rs.close();
			
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return NewsAnalyticsData2;
	}
	
	public void insertNewsData(NewsAnalyticsData NewsAnalyticsData2) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("insert into TFS_NEWS_ANALYTICS (SITENAME, PUBLICATION, PAGE, CTR, CLICKS, POSITION, PRINTS, UPDATED_DATE) values (?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, NewsAnalyticsData2.getSitename());
			stmt.setInt(2, NewsAnalyticsData2.getPublication());
			stmt.setString(3, NewsAnalyticsData2.getPage());
			stmt.setString(4, NewsAnalyticsData2.getCtr());
			stmt.setString(5, NewsAnalyticsData2.getClicks());
			stmt.setString(6, NewsAnalyticsData2.getPosition());
			stmt.setString(7, NewsAnalyticsData2.getPrints());
			stmt.setLong(8, NewsAnalyticsData2.getUpdatedDate());
			stmt.execute();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}

	public void deleteData(String siteName, int publication, String resourcePath) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("delete from TFS_NEWS_ANALYTICS where SITENAME=? and PUBLICATION=? and PAGE = ?");
			stmt.setString(1, siteName);
			stmt.setInt(1, publication);
			stmt.setString(1, resourcePath);

			stmt.execute();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}
	
	public void deleteDataResources (String siteName, int publication, String resourcePathEnable) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("delete from TFS_NEWS_ANALYTICS where SITENAME=? and PUBLICATION=? and PAGE like ?");
			stmt.setString(1, siteName);
			stmt.setInt(1, publication);
			stmt.setString(1, resourcePathEnable);

			stmt.execute();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}
	
	public void updateDataResources (NewsAnalyticsData newsAnalyticsData) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("update TFS_NEWS_ANALYTICS SET CTR = ? AND CLICKS=? AND POSITION=? AND PRINTS=? AND UPDATED_DATE =? WHERE SITENAME=? and PUBLICATION=? and PAGE = ?");
			stmt.setString(4, newsAnalyticsData.getCtr());
			stmt.setString(5, newsAnalyticsData.getClicks());
			stmt.setString(6, newsAnalyticsData.getPosition());
			stmt.setString(7, newsAnalyticsData.getPrints());
			stmt.setLong(8, newsAnalyticsData.getUpdatedDate());
			stmt.setString(1, newsAnalyticsData.getSitename());
			stmt.setInt(2, newsAnalyticsData.getPublication());
			stmt.setString(3, newsAnalyticsData.getPage());
			stmt.executeUpdate();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}
	
	public void clearData(String siteName, int publication, long dateEnable) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("delete from TFS_NEWS_ANALYTICS where SITENAME=? and PUBLICATION=? and UPDATED_DATE < =?");
			stmt.setString(1, siteName);
			stmt.setInt(2, publication);
			stmt.setLong(3,dateEnable);
			
			stmt.execute();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}

	private NewsAnalyticsData fillData(ResultSet rs) throws SQLException {
		NewsAnalyticsData NewsAnalyticsData2 = new NewsAnalyticsData();
		NewsAnalyticsData2.setSitename(rs.getString("SITENAME"));
		NewsAnalyticsData2.setPublication(rs.getInt("PUBLICATION"));
		NewsAnalyticsData2.setPage(rs.getString("PAGE"));
		NewsAnalyticsData2.setClicks(rs.getString("CLICKS"));
		NewsAnalyticsData2.setCtr(rs.getString("CTR"));
		NewsAnalyticsData2.setPosition(rs.getString("POSITION"));
		NewsAnalyticsData2.setPrints(rs.getString("PRINTS"));
		NewsAnalyticsData2.setUpdatedDate(rs.getLong("UPDATED_DATE"));

		return NewsAnalyticsData2;

	}

}