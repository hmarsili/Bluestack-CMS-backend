package com.tfsla.diario.webservices.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.tfsla.diario.webservices.common.strings.SqlQueries;
import com.tfsla.webusersposts.core.BaseDAO;

public class UserInfoDAO extends BaseDAO {
	
	public String getUsernameByEmail(String email) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.GET_USER_NAME_FROM_EMAIL);
			stmt.setString(1, email);
			stmt.setString(2, email);
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				return rs.getString("USER_NAME");
			}
		} catch(Exception ex) {
			ex.printStackTrace();
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
	
	public String getNickMaxId(String userName, String nickName) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.GET_NICK_MAX_ID);
			stmt.setString(1, userName);
			stmt.setString(2, nickName + "%");
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				return rs.getString("MAX_ID");
			}
		} catch(Exception ex) {
			ex.printStackTrace();
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
	
	public String getUserForInfo(String key, String value) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(SqlQueries.GET_USER_ID_FROM_INFO);
			stmt.setString(1, key);
			stmt.setString(2, value);
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				return rs.getString("USER_ID");
			}
		} catch(Exception ex) {
			ex.printStackTrace();
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
