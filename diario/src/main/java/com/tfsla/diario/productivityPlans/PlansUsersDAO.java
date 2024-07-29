package com.tfsla.diario.productivityPlans;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.tfsla.data.baseDAO;
import com.tfsla.diario.productivityPlans.model.PlansUsers;
import com.tfsla.diario.productivityPlans.model.ProductivitiyPlans;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PlansUsersDAO extends baseDAO {

	public List<String> getUsersPlans(int publication, String sitename, String id) throws Exception {

		List<String> plansUsers = new ArrayList<String>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select DATA_VALUE from TFS_PLANS_USERS where PUBLICATION=? and SITE=? and ID=?");
			
			stmt.setInt(1, publication);
			stmt.setString(2, sitename);
			stmt.setString(3, id);
						
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				plansUsers.add(rs.getString("DATA_VALUE"));
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return plansUsers;
	}
	
	
	public List<String> getUsersInPlans(int publication, String sitename, String dataKey) throws Exception {

		List<String> plansUsers = new ArrayList<String>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select DATA_VALUE from TFS_PLANS_USERS where PUBLICATION=? and SITE=? and DATA_KEY=?");
			
			stmt.setInt(1, publication);
			stmt.setString(2, sitename);
			stmt.setString(3, dataKey);
						
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				plansUsers.add(rs.getString("DATA_VALUE"));
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return plansUsers;
	}
	
	public JSONArray getUsersDataPlansDate(int publication, String sitename, String id) throws Exception {

		JSONArray plansUsers = new JSONArray();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select DATA_VALUE, START_DAY from TFS_PLANS_USERS where PUBLICATION=? and SITE=? and ID=?");
			
			stmt.setInt(1, publication);
			stmt.setString(2, sitename);
			stmt.setString(3, id);
						
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("userName",rs.getString("DATA_VALUE"));
				item.put("startDate",rs.getString("START_DAY"));
				plansUsers.add(item);
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return plansUsers;
	}
	
	public boolean existUsersPlans(int publication, String sitename, String id) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select DATA_VALUE from TFS_PLANS_USERS where PUBLICATION=? and SITE=? and ID=? LIMIT 1");
			
			stmt.setInt(1, publication);
			stmt.setString(2, sitename);
			stmt.setString(3, id);
						
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				if (rs.getString("DATA_VALUE") != null)
					return true;
				else 
					return false;
			
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return true;
	}

	public void insertUsersPlans(PlansUsers plansUsers) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("insert into TFS_PLANS_USERS (SITE,PUBLICATION,ID,DATA_KEY,DATA_VALUE,START_DAY) values (?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, plansUsers.getSiteName());
			stmt.setInt(2, plansUsers.getPublication());
			stmt.setString(3, plansUsers.getId());
			stmt.setString(4, plansUsers.getDataKey());
			stmt.setString(5, plansUsers.getDataValue());
			stmt.setLong(6, plansUsers.getStartDay());			
			
			stmt.executeUpdate();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}

	//elimina el usuario para el plan del sitio y pub al que pertenece
	public void deleteUsersForPlan(int publication, String sitename, String dataValue) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("delete from TFS_PLANS_USERS where PUBLICATION=? AND SITE=? AND DATA_VALUE=?");
			stmt.setInt(1, publication);
			stmt.setString(2, sitename);
			stmt.setString(3, dataValue);
			stmt.execute();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}
	
	//elimina todos los usuarios de un plan.
	public void deleteUsersPlan(int publication, String sitename, String url) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("delete from TFS_PLANS_USERS where PUBLICATION=? AND SITE=? AND ID=?");
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

	public PlansUsers existsPlanForUser(String siteName, int publication, String dataValue ) throws Exception {

		PlansUsers plansUsers = new PlansUsers();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select * from TFS_PLANS_USERS where  SITE=? AND PUBLICATION=? AND DATA_VALUE=? ");

			stmt.setString(3, dataValue);
			stmt.setString(1, siteName);
			stmt.setInt(2, publication);
			
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				plansUsers = fillPlansUsers(rs);
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return plansUsers;
	}
	
	private PlansUsers fillPlansUsers(ResultSet rs) throws SQLException {
		
		PlansUsers plansUsers = new PlansUsers();
		plansUsers.setPublication(rs.getInt("PUBLICATION"));
		plansUsers.setSiteName(rs.getString("SITE"));
		plansUsers.setId(rs.getString("ID"));
		plansUsers.setDataValue(rs.getString("DATA_VALUE"));
		plansUsers.setDataKey(rs.getString("DATA_KEY"));
		plansUsers.setStartDay(rs.getLong("START_DAY"));
		
		return plansUsers;

	}
	
	
}