package com.tfsla.diario.freshness;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.data.baseDAO;
import com.tfsla.diario.freshness.model.Freshness;
import com.tfsla.diario.freshness.model.SearchOptions;

public class FreshnessDAO extends baseDAO {
	
	private static final Log LOG = CmsLog.getLog(FreshnessDAO.class);

	public List<Freshness> getFreshnessNEW(int publication, Long dateFrom, Long dateTo) throws Exception {
		List<Freshness> freshness = new ArrayList<Freshness>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select * from TFS_NEWSFRESHNESS where "
					+ " PUBLICATION=? and DATE BETWEEN ? AND ? " );

			stmt.setInt(1, publication);
			stmt.setLong(2, dateFrom);
			stmt.setLong(3, dateTo);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Freshness fresh = fillFreshness(rs);
				freshness.add(fresh);
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return freshness;
	}
	
	public ArrayList<Freshness> getFreshness(int publication, Long dateFrom, Long dateTo) throws Exception {
		ArrayList<Freshness> freshness = new ArrayList<Freshness>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select * from TFS_NEWSFRESHNESS where "
					+ " PUBLICATION=? and DATE BETWEEN ? AND ? " );

			stmt.setInt(1, publication);
			stmt.setLong(2, dateFrom);
			stmt.setLong(3, dateTo);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Freshness fresh = fillFreshness(rs);
				freshness.add(fresh);
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return freshness;
	}
	

	public boolean existsFreshness(int publication, String sitename, String url ) throws Exception {

		Boolean freshnessExit = false;

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select SITE from TFS_NEWSFRESHNESS where PUBLICATION=? and SITE=? and URL=?");

			stmt.setInt(1, publication);
			stmt.setString(2, sitename);
			stmt.setString(3, url);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				freshnessExit = true;
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return freshnessExit;
	}
	
	public Freshness getFreshness(int publication, String sitename, String url) throws Exception {

		Freshness freshness = new Freshness();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select * from TFS_NEWSFRESHNESS where PUBLICATION=? and SITE=? and URL=?");
			
			stmt.setInt(1, publication);
			stmt.setString(2, sitename);
			stmt.setString(3, url);
						
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				freshness = fillFreshness(rs);
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return freshness;
	}
	
	public List<Freshness> getFreshnessInPub(int publication, String sitename) throws Exception {

		List<Freshness> freshness = new ArrayList<Freshness>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select * from TFS_NEWSFRESHNESS where PUBLICATION=? and SITE=?");
			
			stmt.setInt(1, publication);
			stmt.setString(2, sitename);
						
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				freshness.add(fillFreshness(rs));
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return freshness;
	}
//Vero 
	public List<Freshness> getFreshness(SearchOptions options) throws Exception {
		LOG.debug(options.getSiteName());
		
		List<Freshness> Freshness = new ArrayList<Freshness>();
		String strQuery = "";
		
		try {
			if (!connectionIsOpen())
				OpenConnection();

			strQuery = "SELECT * from TFS_NEWSFRESHNESS WHERE ";
	
			if(options.getSiteName() != null && !options.getSiteName().equals("")) {
				strQuery += "SITE = ?";
			}
			
			if(options.getPublication() > 0 ) {
				strQuery += " AND PUBLICATION = ?";
			}
				
			if(options.isNullUrl() && !options.getUrl().equals("")) {
				strQuery += " AND URL = ?";
			}
			
			if(options.isNullSection() && !options.getSection().equals("")) {
				strQuery += " AND SECTION = ?";
			}
			
			if(options.isNullUserName() && !options.getUserName().equals("")) {
				strQuery += " AND USERNAME = ?";
			}
			
			LOG.debug("strQuery " + strQuery);

			if(options.isNullFrom() && options.getFrom() > 0 && 
					options.getTo() != null && options.getTo() > 0) {
				strQuery += " AND DATE BETWEEN ? AND ?";
			} else if(options.isNullTo() && options.getFrom() > 0) {
				strQuery += " AND DATE > ?";
			}
			
			LOG.debug("options.getHistory " + options.getHistory());
			//historial de noticias con frescura
			if(options.isNullFrom() && options.getFrom() > 0 && 
			options.isNotNullHistory() && options.getHistory().equals("true")) {
				strQuery += " AND STARTDATE < ?";
			}
			
			strQuery += "  ORDER BY " + options.getOrderBy();
			
			LOG.debug("strQuery " + strQuery);
			PreparedStatement stmt = conn.prepareStatement(strQuery);
			
			LOG.debug(stmt.toString());
			
			int filtersCount = 0;

			if(options.getSiteName() != null && !options.getSiteName().equals("")) {
				LOG.debug("entro  " + options.getSiteName());
				filtersCount++;
				stmt.setString(filtersCount, options.getSiteName().toLowerCase());
			}
			LOG.debug("strQuery " + strQuery);
			if(options.getPublication() > 0) {
				filtersCount++;
				stmt.setInt(filtersCount, options.getPublication());
			}
			LOG.debug("strQuery " + strQuery);
			if(options.isNullUrl() && !options.getUrl().equals("")) {
				filtersCount++;
				stmt.setString(filtersCount, options.getUrl());
			}
			LOG.debug("strQuery " + strQuery);
			if(options.isNullSection() && !options.getSection().equals("")) {
				filtersCount++;
				stmt.setString(filtersCount, options.getSection());
			}
			LOG.debug("strQuery " + strQuery);
			if(options.isNullUserName() && !options.getUserName().equals("")) {
				filtersCount++;
				stmt.setString(filtersCount, options.getUserName());
			}
			LOG.debug(stmt.toString());
			if(options.isNullFrom() && options.getFrom() > 0) {
				filtersCount++;
				stmt.setLong(filtersCount, options.getFrom());
			}
			LOG.debug(stmt.toString());
			if(options.isNullTo() && options.getTo() > 0) {
				filtersCount++;
				stmt.setLong(filtersCount, options.getTo());
			}
			
			//historial de noticias con frescura
			if(options.isNullFrom() && options.getFrom() > 0 && 
			options.isNotNullHistory() && options.getHistory().equals("true")) {
				filtersCount++;
				stmt.setLong(filtersCount, options.getFrom());
			}
			
			LOG.debug(stmt.toString());
			ResultSet rs = stmt.executeQuery();
			
			LOG.debug("SIZEEEE " + rs.getFetchSize());
			LOG.debug(stmt.toString());
			
			while (rs.next()) {
				Freshness FRESS  = fillFreshness(rs);
				Freshness.add(FRESS);
			}

			rs.close();
			stmt.close();
			
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return Freshness;
	}
	public void insertFreshness(Freshness freshness) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("insert into TFS_NEWSFRESHNESS (SITE,PUBLICATION,URL,TYPE_FRESHNESS,RECURRENCE,DATE,REPUBLICATION,ZONE,PRIORITY,USERNAME,STARTDATE,SECTION) values (?,?,?,?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
			
			stmt.setString(1, freshness.getSiteName());
			stmt.setInt(2, freshness.getPublication());
			stmt.setString(3, freshness.getUrl());
			stmt.setString(4, freshness.getType());
			stmt.setInt(5, freshness.getRecurrece());
			stmt.setLong(6, freshness.getDate());
			stmt.setString(7, freshness.getRepublication());
			stmt.setString(8, freshness.getZone());
			stmt.setInt(9, freshness.getPriority());
			stmt.setString(10, freshness.getUserName());
			stmt.setLong(11,freshness.getStartDate());
			stmt.setString(12, freshness.getSection());
			stmt.executeUpdate();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}

	public void updateFreshness(Freshness freshness) throws Exception {
		try {

			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("update TFS_NEWSFRESHNESS set DATE = ? where PUBLICATION=? AND SITE=? AND URL=?");
			stmt.setLong(1, freshness.getDate());
			stmt.setInt(2, freshness.getPublication());
			stmt.setString(3, freshness.getSiteName());
			stmt.setString(4, freshness.getUrl());
			
			stmt.executeUpdate();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}

	public void deleteFreshness(int publication, String sitename, String url) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("delete from TFS_NEWSFRESHNESS where PUBLICATION=? AND SITE=? AND URL=?");
			stmt.setInt(1, publication);
			stmt.setString(2, sitename);
			stmt.setString(3, url);
			stmt.execute();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}

	private Freshness fillFreshness(ResultSet rs) throws SQLException {
		
		Freshness freshness = new Freshness();
		freshness.setDate(rs.getLong("DATE"));
		freshness.setPublication(rs.getInt("PUBLICATION"));
		freshness.setPriority(rs.getInt("PRIORITY"));
		freshness.setRecurrece(rs.getInt("RECURRENCE"));
		freshness.setRepublication(rs.getString("REPUBLICATION"));
		freshness.setSiteName(rs.getString("SITE"));
		freshness.setType(rs.getString("TYPE_FRESHNESS"));
		freshness.setUrl(rs.getString("URL"));
		freshness.setZone(rs.getString("ZONE"));
		freshness.setUserName(rs.getString("USERNAME"));
		freshness.setStartDate(rs.getLong("STARTDATE"));
		freshness.setSection(rs.getString("SECTION"));
		freshness.setUserName(rs.getString("USERNAME"));

		
		return freshness;

	}
	
	/**
	 * queries para la sección
	 */

	public List<Freshness> getFreshNotSection(int publication, String sitename) throws Exception {

		List<Freshness> freshness = new ArrayList<Freshness>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select * from TFS_NEWSFRESHNESS where PUBLICATION=? and SITE=? AND SECTION IS NULL ");
			
			stmt.setInt(1, publication);
			stmt.setString(2, sitename);
						
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				freshness.add(fillFreshness(rs));
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return freshness;
	}
	
	public void updateSection(Freshness freshness) throws Exception {
		try {

			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("update TFS_NEWSFRESHNESS set SECTION = ? where PUBLICATION=? AND SITE=? AND URL=?");
			stmt.setString(1, freshness.getSection());
			stmt.setInt(2, freshness.getPublication());
			stmt.setString(3, freshness.getSiteName());
			stmt.setString(4, freshness.getUrl());
			
			stmt.executeUpdate();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}
}