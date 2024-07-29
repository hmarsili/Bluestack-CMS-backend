package com.tfsla.opencms.webusers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.opencms.db.CmsDbPool;
import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;

import com.tfsla.opencms.persistence.AbstractBusinessObjectPersitor;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;

/**
 * Facade para operaciones de persistencia de usuarios.
 *
 * @author vpode
 */
public class UserDAO extends AbstractBusinessObjectPersitor {

	public static final String CMS_USERS = "CMS_USERS";
	public static final String CMS_USERDATA = "CMS_USERDATA";

	public static final String USER_EMAIL = "USER_EMAIL";
	public static final String USER_NAME = "USER_NAME";
	public static final String USER_ID = "USER_ID";
	public static final String USER_OU = "USER_OU";
	public static final String USER_OPENID_PASSWORD = "USER_OPENID_PASSWORD";
	

	public boolean chekNewUserMail(CmsObject cms, String mail, String userName) {
		QueryBuilder<Boolean> queryBuilder = new QueryBuilder<Boolean>(cms);
		queryBuilder.setSQLQuery("SELECT * FROM " + CMS_USERS + " WHERE " + USER_EMAIL + " = ? AND " + USER_NAME + "<> ?");

		queryBuilder.addParameter(mail);
		queryBuilder.addParameter(userName);
       
		ResultSetProcessor<Boolean> proc = new ResultSetProcessor<Boolean>() {

			private Boolean mailExists=Boolean.FALSE;

			public void processTuple(ResultSet rs) {
				mailExists=Boolean.TRUE;
			}

			public Boolean getResult() {
				return mailExists;
			}
		};

		return queryBuilder.execute(proc);
	}
	
	public void updateUsername(String userName, String userId) throws SQLException {
		PreparedStatement stmt = null;
		Connection conn = OpenCms.getSqlManager().getConnection(CmsDbPool.getDefaultDbPoolName());
		try {
			stmt = conn.prepareStatement("UPDATE " + CMS_USERS + " SET " + USER_NAME + " = ? WHERE " + USER_ID + " = ?");
			stmt.setString(1, userName);
			stmt.setString(2, userId);
			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
				String additionalMessage = "";
				if(stmt.getWarnings() != null) additionalMessage = stmt.getWarnings().getMessage();
	            throw new SQLException(
            		String.format("Error setting username %s for userId %s", userName, userId) 
            		+ " " + additionalMessage
        		);
	        }
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if(stmt != null) {
				stmt.close();
			}
		}
	}
	
	public boolean existsUsername(CmsObject cms, String userName) {
		QueryBuilder<Boolean> queryBuilder = new QueryBuilder<Boolean>(cms);
		queryBuilder.setSQLQuery("SELECT * FROM " + CMS_USERS + " WHERE " + USER_NAME + " = ?");
		queryBuilder.addParameter(userName);
		
		ResultSetProcessor<Boolean> proc = new ResultSetProcessor<Boolean>() {
			private Boolean usernameExists = false;

			public void processTuple(ResultSet rs) {
				usernameExists = true;
			}

			public Boolean getResult() {
				return usernameExists;
			}
		};

		return queryBuilder.execute(proc);
	}
	
	public boolean checkUsername(CmsObject cms, String userName, String userId) {
		QueryBuilder<Boolean> queryBuilder = new QueryBuilder<Boolean>(cms);
		queryBuilder.setSQLQuery("SELECT * FROM " + CMS_USERS + " WHERE " + USER_NAME + " = ? AND " + USER_ID + " <> ?");
		queryBuilder.addParameter(userName);
		queryBuilder.addParameter(userId);
		
		ResultSetProcessor<Boolean> proc = new ResultSetProcessor<Boolean>() {

			private Boolean usernameExists = false;

			public void processTuple(ResultSet rs) {
				usernameExists = true;
			}

			public Boolean getResult() {
				return usernameExists;
			}
		};

		return queryBuilder.execute(proc);
	}

	public String getUserName(CmsObject cms, String email) {
		QueryBuilder<String> queryBuilder = new QueryBuilder<String>(cms);
		queryBuilder.setSQLQuery("SELECT " + USER_NAME + " FROM " + CMS_USERS + " WHERE " + USER_EMAIL + "= ?");
		queryBuilder.addParameter(email);

		ResultSetProcessor<String> proc = new ResultSetProcessor<String>() {

			private String userName=null;

			public void processTuple(ResultSet rs) {
				try {
					this.userName = rs.getString(USER_NAME);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			public String getResult() {
				return userName;
			}
		};

		return queryBuilder.execute(proc);
	}

	public String getUserNameByAdditionalInfo(CmsObject cms, String key, String valueField) {
		return getUserNameByAdditionalInfo(cms, key, valueField,false);
	}
	
	public String getUserNameByAdditionalInfo(CmsObject cms, String key, String valueField, final boolean fullName) {
		QueryBuilder<String> queryBuilder = new QueryBuilder<String>(cms);
		queryBuilder.setSQLQuery("SELECT " + USER_NAME + ", " + USER_OU + " FROM " + CMS_USERS + " INNER JOIN " + CMS_USERDATA +" ON " + CMS_USERS + "." + USER_ID + " = " + CMS_USERDATA + "." + USER_ID + "  WHERE " + CMS_USERDATA + ".DATA_KEY = ? AND " + CMS_USERDATA + ".DATA_VALUE = ?  COLLATE utf8_general_ci");
				
		queryBuilder.addParameter(key);
		queryBuilder.addParameter(valueField);
		
		ResultSetProcessor<String> proc = new ResultSetProcessor<String>() {

			private String userName=null;

			public void processTuple(ResultSet rs) {
				try {
					String ou = "";
					if (fullName && !rs.getString(USER_OU).equals("/"))
						ou = rs.getString(USER_OU);
					
					this.userName = ou + rs.getString(USER_NAME);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			public String getResult() {
				return userName;
			}
		};

		return queryBuilder.execute(proc);
	}
	
	public boolean checkExistsPropertyInUsers(CmsObject cms, String property, String value, String userName, boolean isExtra, boolean isCaseSensitive) {
		QueryBuilder<Boolean> queryBuilder = new QueryBuilder<Boolean>(cms);
		
		if(!isExtra){
			queryBuilder.setSQLQuery("SELECT 1 FROM " + CMS_USERS + " WHERE " + property + " = ? AND " + USER_NAME + "<> ?");
			queryBuilder.addParameter(value);
			queryBuilder.addParameter(userName);			
		}
		else{
			String queryCI = "";
			
			if(!isCaseSensitive)
				queryCI = " COLLATE utf8_general_ci";
			
			queryBuilder.setSQLQuery(
					"SELECT 1 " +
					" FROM " + CMS_USERS + " " + 
					" INNER JOIN " + CMS_USERDATA +
					" ON " + CMS_USERS + "." + USER_ID + "=" + CMS_USERDATA + "." + USER_ID +
					" WHERE " + CMS_USERS + "." + USER_NAME + "<> ? AND " + CMS_USERDATA +".DATA_KEY  = ? AND " + CMS_USERDATA +".DATA_VALUE = ? " + queryCI);

			queryBuilder.addParameter(userName);
			queryBuilder.addParameter(property);
			queryBuilder.addParameter(value);
		}

		ResultSetProcessor<Boolean> proc = new ResultSetProcessor<Boolean>() {

			private Boolean exists = Boolean.FALSE;

			public void processTuple(ResultSet rs) {
				this.exists = Boolean.TRUE;
			}

			public Boolean getResult() {
				return exists;
			}
		};

		return queryBuilder.execute(proc).booleanValue();
	}

	public boolean chekNewUserDni(CmsObject cms, String dni, String userName) {
		QueryBuilder<Boolean> queryBuilder = new QueryBuilder<Boolean>(cms);
		queryBuilder.setSQLQuery(
				"SELECT 1 " +
				" FROM " + CMS_USERS + " " + 
				" INNER JOIN " + CMS_USERDATA +
				" ON " + CMS_USERS + "." + USER_ID + "=" + CMS_USERDATA + "." + USER_ID +
				" WHERE " + CMS_USERDATA +".DATA_KEY  = 'USER_DNI' AND " + CMS_USERDATA +".DATA_VALUE = ? AND " + CMS_USERS + "." + USER_NAME + "<> ?");

		queryBuilder.addParameter(dni);
		queryBuilder.addParameter(userName);

		ResultSetProcessor<Boolean> proc = new ResultSetProcessor<Boolean>() {

			private Boolean dniExists=Boolean.FALSE;

			public void processTuple(ResultSet rs) {
				this.dniExists = Boolean.TRUE;
			}

			public Boolean getResult() {
				return dniExists;
			}
		};

		return queryBuilder.execute(proc).booleanValue();
	}

	public String getUserNameByDni(CmsObject cms, String dni) {
		QueryBuilder<String> queryBuilder = new QueryBuilder<String>(cms);
		queryBuilder.setSQLQuery(
				"SELECT " + USER_NAME +
				" FROM " + CMS_USERS + " " + 
				" INNER JOIN " + CMS_USERDATA +
				" ON " + CMS_USERS + "." + USER_ID + "=" + CMS_USERDATA + "." + USER_ID +
				" WHERE " + CMS_USERDATA +".DATA_KEY  = 'USER_DNI' AND " + CMS_USERDATA +".DATA_VALUE = ? ");

		queryBuilder.addParameter(dni);

		ResultSetProcessor<String> proc = new ResultSetProcessor<String>() {

			private String userName="";

			public void processTuple(ResultSet rs) {
				try {
					this.userName = rs.getString(USER_NAME);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			public String getResult() {
				return userName;
			}
		};

		return queryBuilder.execute(proc);
	}

	public String getUserNameByProviderNameAndKey(CmsObject cms, String providerName, String providerKey) {
		QueryBuilder<String> queryBuilder = new QueryBuilder<String>(cms);
		queryBuilder.setSQLQuery(
				"SELECT " + USER_NAME +
				" FROM " + CMS_USERS + " " + 
				" INNER JOIN " + CMS_USERDATA +
				" ON " + CMS_USERS + "." + USER_ID + "=" + CMS_USERDATA + "." + USER_ID +
				" WHERE " + CMS_USERDATA +".DATA_KEY  = 'USER_OPENAUTHORIZATION_PROVIDER_" + providerName.toUpperCase() + "_KEY' AND " + CMS_USERDATA +".DATA_VALUE = ? ");

		queryBuilder.addParameter(providerKey);

		ResultSetProcessor<String> proc = new ResultSetProcessor<String>() {

			private String userName = "";

			public void processTuple(ResultSet rs) {
				try {
					this.userName = rs.getString(USER_NAME);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			public String getResult() {
				return userName;
			}
		};

		return queryBuilder.execute(proc);
	}
	
	@SuppressWarnings("rawtypes")
	public void deletePendingUsers(CmsObject cms, String ou) {
		QueryBuilder queryBuilder = new QueryBuilder(cms);
		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.DAY_OF_YEAR, -1 * RegistrationModule.getInstance(cms).getUserPurgeDays());
		
		queryBuilder.setSQLQuery("call dropUsers( " +
			cal.getTimeInMillis() + ",'" + ou + "' ) "
		);

		queryBuilder.execute();
	}
}