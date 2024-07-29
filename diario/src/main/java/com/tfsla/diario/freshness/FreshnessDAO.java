package com.tfsla.diario.freshness;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.tfsla.data.baseDAO;
import com.tfsla.diario.freshness.model.Freshness;

public class FreshnessDAO extends baseDAO {

	public ArrayList getFreshness(int publication, Long dateFrom, Long dateTo) throws Exception {
		ArrayList freshness = new ArrayList<Freshness>();

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

	public void insertFreshness(Freshness freshness) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("insert into TFS_NEWSFRESHNESS (SITE,PUBLICATION,URL,TYPE_FRESHNESS,RECURRENCE,DATE,REPUBLICATION,ZONE,PRIORITY,USERNAME,STARTDATE) values (?,?,?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
			
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
		
		return freshness;

	}

}