package com.tfsla.opencms.follow.data;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.opencms.main.CmsLog;

import com.tfsla.data.baseDAO;
import com.tfsla.opencms.follow.model.UserFollow;

public class FollowDAO extends baseDAO {
	
	public ArrayList<UserFollow> getFollowersPerUser(String userId) {
		ArrayList<UserFollow> ret = new ArrayList<UserFollow>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			if(conn == null){
				OpenConnection();
			}
			stmt = conn.prepareStatement(
					"select * from TFS_USER_FOLLOWERS "
					+ "where SEGUIDO = ? ");
			
			stmt.setString(1,userId);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				UserFollow user = new UserFollow();
				user.setFecha(rs.getDate("FECHA"));
				user.setSeguidor(rs.getString("SEGUIDOR"));
				user.setSeguido(rs.getString("SEGUIDO"));
				ret.add(user);
			}
		} catch(Exception e) {
			CmsLog.getLog(this).error(e.getMessage());
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
	
	public ArrayList<UserFollow> getFollowingByUser(String userId) {
		ArrayList<UserFollow> ret = new ArrayList<UserFollow>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			if(conn == null){
				OpenConnection();
			}
			stmt = conn.prepareStatement(
					"select * from TFS_USER_FOLLOWERS "
					+ "where SEGUIDOR = ? ");
			
			stmt.setString(1,userId);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				UserFollow user = new UserFollow();
				user.setFecha(rs.getDate("FECHA"));
				user.setSeguidor(rs.getString("SEGUIDOR"));
				user.setSeguido(rs.getString("SEGUIDO"));
				ret.add(user);
			}
		} catch(Exception e) {
			CmsLog.getLog(this).error(e.getMessage());
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
	
	public int getCountFollowersPerUser(String userId) {
		int count = 0;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			if(conn == null){
				OpenConnection();
			}
			stmt = conn.prepareStatement(
					"select COUNT(*) AS CANTIDAD from TFS_USER_FOLLOWERS "
					+ "where SEGUIDO = ? ");
			
			stmt.setString(1,userId);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				count = Integer.parseInt(rs.getString("CANTIDAD"));
			}
		} catch(Exception e) {
			CmsLog.getLog(this).error(e.getMessage());
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
		
		return count;
	}
	
	public int getCountFollowingPerUser(String userId) {
		int count = 0;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			if(conn == null){
				OpenConnection();
			}
			stmt = conn.prepareStatement(
					"select COUNT(*) AS CANTIDAD from TFS_USER_FOLLOWERS "
					+ "where SEGUIDOR = ? ");
			
			stmt.setString(1,userId);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				count = Integer.parseInt(rs.getString("CANTIDAD"));
			}
		} catch(Exception e) {
			CmsLog.getLog(this).error(e.getMessage());
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
		
		return count;
	}
	
	public void deleteFollower(String userId, String userIdFollower) {
		PreparedStatement stmt = null;
		try {		
			if(conn == null){
				OpenConnection();
			}
			stmt = conn.prepareStatement(
					"delete from TFS_USER_FOLLOWERS "
					+ "where SEGUIDO = ? AND SEGUIDOR = ? ");
			
			stmt.setString(1,userId);
			stmt.setString(2,userIdFollower);
			stmt.executeUpdate();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void addFollower(UserFollow user) {
		PreparedStatement stmt = null;
		try {
			if(conn == null){
				OpenConnection();
			}
			stmt = conn.prepareStatement(
					"insert into TFS_USER_FOLLOWERS ( FECHA, SEGUIDOR, SEGUIDO )" +
					" values (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			stmt.setTimestamp(1, new Timestamp(user.getFecha().getTime()));//.setDate(1, new Timestamp(user.getFecha()));
			stmt.setString(2, user.getSeguidor());
			stmt.setString(3, user.getSeguido());
			stmt.executeUpdate();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}	
}
