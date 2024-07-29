package com.tfsla.opencms.webusers.openauthorization.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.opencms.main.CmsLog;

import com.tfsla.opencms.webusers.openauthorization.common.UserDataValue;

public class UserDataDAO extends OpenAuthorizationDAO {
	
	public ArrayList<UserDataValue> getGroupedUserDataValues(String dataKey) {
		ArrayList<UserDataValue> ret = new ArrayList<UserDataValue>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = conn.prepareStatement(
					"select COUNT(1) AS CANTIDAD, DATA_VALUE from CMS_USERDATA "
					+ "where DATA_KEY = ? "
					+ "group by DATA_VALUE "
					+ "order by CANTIDAD DESC");
			
			stmt.setString(1, dataKey);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				UserDataValue value = new UserDataValue();
				value.setKey(rs.getString("DATA_VALUE"));
				value.setValue(Integer.parseInt(rs.getString("CANTIDAD")));
				ret.add(value);
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
}
