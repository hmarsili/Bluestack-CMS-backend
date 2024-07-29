package com.tfsla.opencms.webusers.openauthorization.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;

public class UsersCommunityDAO extends OpenAuthorizationDAO {
	
	private Hashtable<String, String> conditions = new Hashtable<String, String>();
	public static final String FILTER_KEY_SOCIAL_LOGINS = "FILTER_KEY_SOCIAL_LOGINS";
	public static final String FILTER_KEY_ACTIVE_USERS = "FILTER_KEY_ACTIVE_USERS";
	public static final String FILTER_KEY_PENDING_USERS = "FILTER_KEY_PENDING_USERS";
	
	public UsersCommunityDAO() {
		this.conditions.put(FILTER_KEY_SOCIAL_LOGINS, "DATA_KEY LIKE 'USER_OPENAUTHORIZATION_PROVIDER%'");
		this.conditions.put(FILTER_KEY_ACTIVE_USERS, "USER_LASTLOGIN <> 0");
		this.conditions.put(FILTER_KEY_PENDING_USERS, "USER_FLAGS & 1 = 1");
	}
	
	public Integer getCommunityValue(String table, String conditionKey) {
		Integer ret = 0;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String countItem = "1";
			if(conditionKey.equals(FILTER_KEY_SOCIAL_LOGINS)) {
				countItem = "distinct USER_ID";
			}
			String query = String.format("select COUNT(%s) as CANTIDAD from %s", countItem, table);
			
			if(conditionKey != null && !conditionKey.equals("") && this.conditions.keySet().contains(conditionKey)) {
				query += " where " + this.conditions.get(conditionKey);
			}
			stmt = conn.prepareStatement(query);
			
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				ret = Integer.parseInt(rs.getString("CANTIDAD"));
			}
		} catch(Exception e) {
			e.printStackTrace();
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
}
