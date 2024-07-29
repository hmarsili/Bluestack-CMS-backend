package com.tfsla.diario.auditActions.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opencms.file.CmsObject;

import com.tfsla.data.baseDAO;
import com.tfsla.diario.auditActions.model.TfsAuditAction;
import com.tfsla.workflow.QueryBuilder;


public class TfsAuditActionDAO extends baseDAO {

	private static final String TABLE = "TFS_EVENT";
	private static final String TABLE_USER = "TFS_USER_EVENT";

	private static final String TIMESTAMP = "TIMESTAMP";
	private static final String LASTMODIFIED = "LASTMODIFIED";
	private static final String USERNAME = "USERNAME";
	private static final String ACTIONID = "ACTIONID";
	private static final String TARGETID = "TARGETID";
	private static final String DESCRIPTION = "DESCRIPTION";
	private static final String SITIO = "SITIO";
	private static final String PUBLICACION = "PUBLICACION";
	private static final String ID_EVENT = "ID_EVENT";
	private static final String COMMENTS = "COMMENTS";
	private static final String ATTACHMENTS = "ATTACHMENTS";
	
	private static final String TFS_WEBHOOKS_QUEUE = "TFS_WEBHOOKS_QUEUE";
    
    private static final String DB_SITE         = "SITE";
    private static final String DB_PUBLICATION  = "PUBLICATION";
    private static final String DB_PATH         = "PATH";
    private static final String DB_PUBLISH_DATE = "PUBLISH_DATE";
    private static final String DB_TEXT         = "TEXT";

	public void insertNotificationAuditEvent(long idEvent, Date timeStamp, String userName) throws Exception
	{
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;

		//BORRAR EL IDENTIFICADOR DE ID_USER_EVENT;
		stmt = conn.prepareStatement(
				"insert into " + TABLE_USER + "( " +
				"	" + TIMESTAMP + ", " + 
				"	" + USERNAME + ", " + 
				"	" + ID_EVENT + ") " + 

				" values (?,?,?)" +
				" ON DUPLICATE KEY UPDATE TIMESTAMP=NOW();"
				);

		long timeNow = timeStamp.getTime(); 
		java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);
		stmt.setTimestamp(1,ts);
		
		stmt.setString(2,userName);
		stmt.setLong(3,idEvent);
		
		stmt.executeUpdate();

		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
		
	}

	public void insertUserAuditEvent(TfsAuditAction action) throws Exception
	{
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		ResultSet generatedKeys = null;

		stmt = conn.prepareStatement(
				"insert into " + TABLE + "( " +
				"	" + TIMESTAMP + ", " + 
				"	" + USERNAME + ", " + 
				"	" + ACTIONID + ", " + 
				"	" + TARGETID + ", " + 
				"	" + DESCRIPTION + ", " + 
				"	" + SITIO + ", " + 
				"	" + PUBLICACION + ", " +
				"	" + COMMENTS + ", " + 
				"	" + ATTACHMENTS + ") " +

				" values (?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);

		long timeNow = action.getTimeStamp().getTime(); 
		java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);
		stmt.setTimestamp(1,ts);
		
		stmt.setString(2,action.getUserName());

		stmt.setInt(3,action.getActionId());

		stmt.setString(4,action.getTargetId());
		stmt.setString(5,action.getDescription());
		stmt.setString(6,action.getSitio());
		stmt.setString(7,action.getPublicacion());

		stmt.setInt(8,action.getComments());
		stmt.setInt(9,action.getAttachments());
		
		int affectedRows = stmt.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Error insertando un evento de auditoria.");
        }

        generatedKeys = stmt.getGeneratedKeys();
        if (generatedKeys.next()) {
            action.setEventId(generatedKeys.getLong(1));
        } else {
            throw new SQLException("Error insertando un evento de auditoria, No se pudo obtener el identificador del registro.");
        }


		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
		
	}
	
	private TfsAuditAction fillAction(ResultSet rs) throws SQLException  {
		TfsAuditAction action = new TfsAuditAction();

		action.setActionId(rs.getInt(ACTIONID));
		action.setUserName(rs.getString(USERNAME));
		action.setTimeStamp(rs.getTimestamp(TIMESTAMP));
		action.setTargetId(rs.getString(TARGETID));
		action.setDescription(rs.getString(DESCRIPTION));
		action.setSitio(rs.getString(SITIO));
		action.setPublicacion(rs.getString(PUBLICACION));
		action.setEventId(rs.getLong(ID_EVENT));
		action.setComments(rs.getInt(COMMENTS));
		action.setAttachments(rs.getInt(ATTACHMENTS));
		action.setLastModified(rs.getTimestamp(LASTMODIFIED));

		return action;

	}

	public List<TfsAuditAction> getActions(Date fromTimeStamp, Date toTimeStamp, String userName, Integer actionId, int count) throws Exception {
		return getActions(fromTimeStamp, toTimeStamp, userName, null, actionId, count);

	}
	
	public int unreadUserNotifications(Date fromTimeStamp, String userName) throws Exception
	{
		
		int count=0;
		
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		ResultSet rs;

		String query = 
				"SELECT " +
				"	COUNT(" + USERNAME + ") AS CANTIDAD " + 
				" FROM " + TABLE_USER;
				
		String where = "";
		if (fromTimeStamp!=null)
			where += " AND " + TIMESTAMP + " > ? "; 
		if (userName!=null)
			where += " AND " + USERNAME + " = ? "; 
			
		if (!where.isEmpty())
			query += where.replaceFirst(" AND ", " WHERE ");
		
		query += 
				" GROUP BY " + USERNAME;

		stmt = conn.prepareStatement(query);

		int j=1;
		if (fromTimeStamp!=null) {
			long timeNow = fromTimeStamp.getTime(); 
			java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);
			stmt.setTimestamp(j,ts);
			j++;
		}
		if (userName!=null) {
			stmt.setString(j, userName);
			j++;
		}

		rs = stmt.executeQuery();

		if (rs.next()) {
			count = rs.getInt("CANTIDAD");
		}

		rs.close();
		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();

		return count;
	}

	public void purgeActions(Date to)  throws Exception {
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		
		stmt = conn.prepareStatement(
				"delete from " + TABLE + 
				" where TIMESTAMP <= ?");

		long timeNow = to.getTime(); 
		java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);
		stmt.setTimestamp(1,ts);
		
		stmt.executeUpdate();

		stmt.close();
		
		if (connectionIsOpenLocaly())
			closeConnection();
	}
	
	public List<TfsAuditAction> getActions(Date fromTimeStamp, Date toTimeStamp, String userName, String userNotificated, Integer actionId, int count) throws Exception {
		Integer[] ids=null;
		if (actionId!=null) 
			ids = new Integer[] { actionId };
		
		return getActionsById(fromTimeStamp, toTimeStamp, userName, userNotificated, ids, count);
	}
	
	public List<TfsAuditAction> getActionsById(Date fromTimeStamp, Date toTimeStamp, String userName, String userNotificated, Integer[] actionId, int count) throws Exception {
		List<TfsAuditAction> actionList = new ArrayList<TfsAuditAction>();

		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		ResultSet rs;

		String query = 
				"SELECT " +
				"	" + TABLE + "." + ID_EVENT + ", " + 
				"	" + TABLE + "." + TIMESTAMP + ", " + 
				"	" + TABLE + "." + LASTMODIFIED + ", " + 
				"	" + TABLE + "." + USERNAME + ", " + 
				"	" + TABLE + "." + ACTIONID + ", " + 
				"	" + TABLE + "." + TARGETID + ", " + 
				"	" + TABLE + "." + DESCRIPTION + ", " + 
				"	" + TABLE + "." + SITIO + ", " + 
				"	" + TABLE + "." + PUBLICACION + ", " + 
				"	" + TABLE + "." + COMMENTS + ", " + 
				"	" + TABLE + "." + ATTACHMENTS + "" + 
				" FROM " + TABLE;
		
		if (userNotificated!=null) {
			query += 
					" INNER JOIN " + TABLE_USER +
					" ON " + TABLE + "." + ID_EVENT + " = " + TABLE_USER + "." + ID_EVENT ;
		}

		String where = "";
		if (userNotificated==null) {
			if (fromTimeStamp!=null)
				where += " AND " + TABLE + "." + LASTMODIFIED + " > ? "; 
			if (toTimeStamp!=null)
				where += " AND " + TABLE + "." + LASTMODIFIED + " < ? ";
		}
		else {
			if (fromTimeStamp!=null)
				where += " AND " + TABLE_USER + "." + TIMESTAMP + " > ? "; 
			if (toTimeStamp!=null)
				where += " AND " + TABLE_USER + "." + TIMESTAMP + " < ? ";

		}
		if (userName!=null)
			where += " AND " + TABLE + "." + USERNAME + " = ? "; 
		if (actionId!=null) {
			where += " AND " + TABLE + "." + ACTIONID + " IN ( "; 
			for (int size = 0; size<actionId.length-1;size++)
				where += "?,";
			where += "? )";
			
		}
		if (userNotificated!=null) { 
			where += " AND " + TABLE_USER + "." + USERNAME + " = ? "; 
		}
			
		if (!where.isEmpty())
			query += where.replaceFirst(" AND ", " WHERE ");
		
		query += 
				" ORDER BY " + TABLE + "." + LASTMODIFIED + " DESC " +
				" LIMIT 0,?";

		stmt = conn.prepareStatement(query);

		int j=1;
		if (fromTimeStamp!=null) {
			long timeNow = fromTimeStamp.getTime(); 
			java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);
			stmt.setTimestamp(j,ts);
			j++;
		}
		if (toTimeStamp!=null) {
			long timeNow = toTimeStamp.getTime(); 
			java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);
			stmt.setTimestamp(j,ts);
			j++;
		}
		if (userName!=null) {
			stmt.setString(j, userName);
			j++;
		}
		if (actionId!=null) {
			for (int size = 0; size<actionId.length;size++) {
				stmt.setInt(j,actionId[size]);
				j++;
			}
		}
		
		if (userNotificated!=null) {
			stmt.setString(j, userNotificated);
			j++;
		}
		stmt.setInt(j,count);

		rs = stmt.executeQuery();

		while (rs.next()) {
			actionList.add(fillAction(rs));
		}

		rs.close();
		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();

		return actionList;
	}

	public TfsAuditAction getAction(long eventId) throws Exception {
		TfsAuditAction action = null;

		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		ResultSet rs;

		String query = 
				"SELECT " +
				"	" + TABLE + "." + ID_EVENT + ", " + 
				"	" + TABLE + "." + TIMESTAMP + ", " + 
				"	" + TABLE + "." + LASTMODIFIED + ", " + 
				"	" + TABLE + "." + USERNAME + ", " + 
				"	" + TABLE + "." + ACTIONID + ", " + 
				"	" + TABLE + "." + TARGETID + ", " + 
				"	" + TABLE + "." + DESCRIPTION + ", " + 
				"	" + TABLE + "." + SITIO + ", " + 
				"	" + TABLE + "." + PUBLICACION + ", " + 
				"	" + TABLE + "." + COMMENTS + ", " + 
				"	" + TABLE + "." + ATTACHMENTS + "" + 
				" FROM " + TABLE + " " +
				" WHERE  " + TABLE + "." + ID_EVENT + " = ? "; 
			

		stmt = conn.prepareStatement(query);

		
		stmt.setLong(1,eventId);
		

		rs = stmt.executeQuery();

		if (rs.next()) {
			action = fillAction(rs);
		}

		rs.close();
		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();

		return action;
	}
	
	public void addCommentCount(int eventId) throws Exception {

		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;

		stmt = conn.prepareStatement(
				"UPDATE " + TABLE + " SET " +
				"	" + COMMENTS + "= " + COMMENTS + "+1 " +
				"	WHERE " + ID_EVENT + " =? ");

		stmt.setLong(1,eventId);
		
		stmt.executeUpdate();

		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
	}
	
	public void scheduleMessageWebhooks(CmsObject cmsObject, String site, int publicacion, String path, String message, Date publish_date){
		
		QueryBuilder queryBuilder = new QueryBuilder(cmsObject);
		queryBuilder.setSQLQuery("INSERT INTO "+TFS_WEBHOOKS_QUEUE+" ("+DB_SITE+","+DB_PUBLICATION+", "+DB_PATH+","+DB_PUBLISH_DATE+","+DB_TEXT+") " +
				"VALUES (?,?,?,?,?);");
		
		queryBuilder.addParameter(site);
		queryBuilder.addParameter(publicacion);
		queryBuilder.addParameter(path);
		queryBuilder.addParameter(publish_date);
		queryBuilder.addParameter(message);
		
		queryBuilder.execute();
	}

}
