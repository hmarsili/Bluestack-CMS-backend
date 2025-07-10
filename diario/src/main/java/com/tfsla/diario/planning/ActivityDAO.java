package com.tfsla.diario.planning;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.data.baseDAO;
import com.tfsla.diario.planning.model.Activity;
import com.tfsla.diario.planning.model.SearchOptions;
import com.tfsla.diario.planning.services.ActivityServices;

public class ActivityDAO extends baseDAO {

	private static final Log LOG = CmsLog.getLog(ActivityServices.class);

	public List<Activity> getActivity(SearchOptions options) throws Exception {
		LOG.debug(options.getSiteName());
		
		List<Activity> Activity = new ArrayList<Activity>();
		String strQuery = "";
		
		try {
			if (!connectionIsOpen())
				OpenConnection();

			strQuery = "SELECT * from TFS_PLANNING WHERE ";
			LOG.debug("strQuery " + strQuery);
	
			if(options.getSiteName() != null && !options.getSiteName().equals("")) {
				strQuery += "SITENAME = ?";
			}
			if(options.getPublication() > 0 ) {
				strQuery += " AND PUBLICATION = ?";
			}
			LOG.debug("options.id" + options.getId());
			
			if(options.isNotNullId() && options.getId() > 0) {
				strQuery += " AND ID = ?";
			}
			
			if(options.getFrom() != null && options.getFrom() > 0 && 
					options.getTo() != null && options.getTo() > 0) {
				strQuery += " AND START_DATE BETWEEN ? AND ?";
			} else if(options.getTo() != null && options.getFrom() > 0) {
				strQuery += " AND START_DATE > ?";
			}
			if(options.getUserName() != null && !options.getUserName().equals("")) {
				strQuery += " AND USERNAME = ?";
			}

			if(options.getText() != null && !options.getText().equals("")) {
				strQuery += " AND TITLE Like ?";
			}
			
			strQuery += "  ORDER BY " + options.getOrderBy();
			
			if (options.getCount() > 0) {
				strQuery += " LIMIT " + options.getCount();
			}

			
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
			if(options.isNotNullId() && options.getId() > 0) {
				filtersCount++;
				stmt.setInt(filtersCount, options.getId());
			}
			LOG.debug(stmt.toString());
			if(options.isNotNullFrom() && options.getFrom() > 0) {
				filtersCount++;
				stmt.setLong(filtersCount, options.getFrom());
			}
			LOG.debug(stmt.toString());
			if(options.isNotNullTo() && options.getTo() > 0) {
				filtersCount++;
				stmt.setLong(filtersCount, options.getTo());
			}
			LOG.debug(stmt.toString());
			if(options.getUserName() != null && !options.getUserName().equals("")) {
				filtersCount++;
				stmt.setString(filtersCount, options.getUserName().toLowerCase());
			}
			LOG.debug(stmt.toString());
			if(options.getText() != null && !options.getText().equals("")) {
				filtersCount++;
				stmt.setString(filtersCount, "%"+options.getText().toLowerCase()+"%");
			}
			
			LOG.debug(stmt.toString());
			ResultSet rs = stmt.executeQuery();
			
			LOG.debug("SIZEEEE " + rs.getFetchSize());
			LOG.debug(stmt.toString());
			
			while (rs.next()) {
				LOG.debug("entro");

				Activity ACTIVITY = fillActivity(rs);
				Activity.add(ACTIVITY);
			}

			rs.close();
			stmt.close();
			
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return Activity;
	}
		
	public Activity getActivity(int publication, String sitename, String id) throws Exception {

		Activity Activity = new Activity();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select * from TFS_PLANNING where PUBLICATION=? and SITENAME=? and ID=?");
			
			stmt.setInt(1, publication);
			stmt.setString(2, sitename);
			stmt.setString(3, id);
						
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Activity = fillActivity(rs);
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return Activity;
	}
	
	public List<Activity> getActivityInPub(int publication, String sitename) throws Exception {

		List<Activity> activityList = new ArrayList<Activity>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select * from TFS_PLANNING where PUBLICATION=? and SITENAME=?");

			stmt.setInt(1, publication);
			stmt.setString(2, sitename);

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Activity activity = fillActivity(rs);
				activityList.add(activity);
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return activityList;
	}

	@SuppressWarnings("finally")
	public int insertActivity(Activity Activity) throws Exception {
		
		int id = 0;
		
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("insert into TFS_PLANNING (SITENAME,PUBLICATION,TITLE,DESCRIPTION,"
					+ "USERNAME,USER_CREATION,COLOR,TYPE_RECURRENCE,PERSONAL_DAYS,REPEAT_TYPE,"
					+ "REPEAT_DAY,REPEAT_END,START_DATE,REPEAT_END_DAYS,DATE_END,TYPE_OF_MONTH,WEEK_POSITION) "
					+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
			
			stmt.setString(1, Activity.getSiteName());
			stmt.setInt(2, Activity.getPublication());
			stmt.setString(3, Activity.getTitle());
			stmt.setString(4, Activity.getDescription());
			stmt.setString(5, Activity.getUserName());	
			stmt.setString(6, Activity.getUserCreation());	
			stmt.setString(7, Activity.getColor());	
			stmt.setString(8, Activity.getType_recurrence());
			stmt.setInt(9, Activity.getPersonal_days());
			stmt.setString(10, Activity.getRepeat_type());
			stmt.setString(11, Activity.getRepeat_day());
			stmt.setInt(12,Activity.getRepeat_end());
			stmt.setLong(13,Activity.getStart_date());
			stmt.setInt(14,Activity.getRepeat_end_days());
			stmt.setLong(15,Activity.getDate_end());
			stmt.setString(16,Activity.getType_of_month());
			stmt.setInt(17,Activity.getWeek_position());

			stmt.executeUpdate();
			
			LOG.debug("agrega");

			
			stmt = conn.prepareStatement("SELECT ID from TFS_PLANNING ORDER BY ID desc LIMIT 1 ");
			LOG.debug("busca ultimo stmt " + stmt.toString());

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				id = rs.getInt("ID");
				LOG.debug("id " + id);
			}
			rs.close();

			stmt.close();
			
			LOG.debug("id " + id);

			return id;
			
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
			
			return id;
		}
		
	}

	public void updateActivity(Activity Activity) throws Exception {
		try {

			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("update TFS_PLANNING set START_DATE = ?, TITLE=?, "
					+ "DESCRIPTION=?, USERNAME=?, COLOR=?, TYPE_RECURRENCE=?, PERSONAL_DAYS=?, "
					+ "REPEAT_TYPE=?,REPEAT_DAY=?, REPEAT_END=?, REPEAT_END_DAYS=?, DATE_END=?, "
					+ "TYPE_OF_MONTH=?, WEEK_POSITION=? "
					+ "where PUBLICATION=? AND SITENAME=? AND ID=?");
			stmt.setLong(1, Activity.getStart_date());
			stmt.setString(2, Activity.getTitle());
			stmt.setString(3, Activity.getDescription());
			stmt.setString(4, Activity.getUserName());
			stmt.setString(5, Activity.getColor());
			stmt.setString(6, Activity.getType_recurrence());
			stmt.setInt(7, Activity.getPersonal_days());
			stmt.setString(8, Activity.getRepeat_type());
			stmt.setString(9, Activity.getRepeat_day());
			stmt.setInt(10, Activity.getRepeat_end());
			stmt.setInt(11, Activity.getRepeat_end_days());
			stmt.setLong(12, Activity.getDate_end());
			stmt.setString(13, Activity.getType_of_month());
			stmt.setInt(14, Activity.getWeek_position());
			stmt.setInt(15, Activity.getPublication());
			stmt.setString(16, Activity.getSiteName());
			stmt.setInt(17,Activity.getId());			
			stmt.executeUpdate();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}

	public void deleteActivity(int publication, String sitename, int id) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("delete from TFS_PLANNING where PUBLICATION=? AND SITENAME=? AND ID=?");
			stmt.setInt(1, publication);
			stmt.setString(2, sitename);
			stmt.setInt(3, id);

			stmt.execute();

			LOG.debug("borra stmt " + stmt.toString());

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}

	private Activity fillActivity(ResultSet rs) throws SQLException {
		LOG.debug(" llego ");

		Activity Activity = new Activity();
		Activity.setSiteName(rs.getString("SITENAME"));
		Activity.setPublication(rs.getInt("PUBLICATION"));
		Activity.setId(rs.getInt("ID"));
		Activity.setTitle(rs.getString("TITLE"));
		Activity.setDescription(rs.getString("DESCRIPTION"));
		Activity.setUserName(rs.getString("USERNAME"));
		Activity.setUserCreation(rs.getString("USER_CREATION"));
		Activity.setColor(rs.getString("COLOR"));
		Activity.setType_recurrence(rs.getString("TYPE_RECURRENCE"));
		Activity.setPersonal_days(rs.getInt("PERSONAL_DAYS"));
		Activity.setRepeat_type(rs.getString("REPEAT_TYPE"));
		Activity.setRepeat_day(rs.getString("REPEAT_DAY"));
		Activity.setRepeat_end(rs.getInt("REPEAT_END"));
		Activity.setStart_date(rs.getLong("START_DATE"));
		Activity.setRepeat_end_days(rs.getInt("REPEAT_END_DAYS"));
		Activity.setDate_end(rs.getLong("DATE_END"));
		Activity.setType_of_month(rs.getString("TYPE_OF_MONTH"));
		Activity.setWeek_position(rs.getInt("WEEK_POSITION"));

		return Activity;

	}

}