package com.tfsla.diario.auditActions.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.tfsla.data.baseDAO;
import com.tfsla.diario.auditActions.model.TfsAuditComment;

public class TfsAuditCommentDAO  extends baseDAO {
	private static final String TABLE = "TFS_EVENT_COMMENTS";
	
	private static final String TIMESTAMP = "TIMESTAMP";
	private static final String USERNAME = "USERNAME";
	private static final String ID_EVENT = "ID_EVENT";
	private static final String DESCRIPTION = "DESCRIPTION";
	private static final String ID_COMMENT = "ID_COMMENT";
	
	
	public void purgeCommentAuditEvent(Date to) throws Exception {
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;

		stmt = conn.prepareStatement(
				"DELETE c.* FROM " + TABLE + " c " + 
				"	INNER JOIN TFS_EVENT e ON e." + ID_EVENT + " = c." + ID_EVENT +
				"	WHERE e.TIMESTAMP <= ? "
				);
		
		long timeNow = to.getTime(); 
		java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);
		stmt.setTimestamp(1,ts);
		
		stmt.executeUpdate();

		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
	}
	
	public void insertCommentAuditEvent(TfsAuditComment comment) throws Exception
	{
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		ResultSet generatedKeys = null;

		stmt = conn.prepareStatement(
				"insert into " + TABLE + "( " +
				"	" + TIMESTAMP + ", " + 
				"	" + USERNAME + ", " + 
				"	" + DESCRIPTION + ", " + 
				"	" + ID_EVENT + ") " + 

				" values (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);

		long timeNow = comment.getTimeStamp().getTime(); 
		java.sql.Timestamp ts = new java.sql.Timestamp(timeNow);
		stmt.setTimestamp(1,ts);
		
		stmt.setString(2,comment.getUserName());

		stmt.setString(3,comment.getDescription());

		stmt.setLong(4,comment.getEventId());
		
		int affectedRows = stmt.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Error insertando un comentario a un evento de auditoria.");
        }

        generatedKeys = stmt.getGeneratedKeys();
        if (generatedKeys.next()) {
            comment.setCommentId(generatedKeys.getLong(1));
        } else {
            throw new SQLException("Error insertando un comentario a un evento de auditoria, No se pudo obtener el identificador del registro.");
        }


		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
		
	}
	
	private TfsAuditComment fillComment(ResultSet rs) throws SQLException  {
		TfsAuditComment comment = new TfsAuditComment();

		comment.setUserName(rs.getString(USERNAME));
		comment.setTimeStamp(rs.getTimestamp(TIMESTAMP));
		comment.setDescription(rs.getString(DESCRIPTION));
		comment.setEventId(rs.getLong(ID_EVENT));
		comment.setCommentId(rs.getLong(ID_COMMENT));

		return comment;

	}

	public TfsAuditComment getComment(Long commentID)  throws Exception {
		TfsAuditComment comment = null;
		
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		ResultSet rs;

		String query = 
				"SELECT " +
				"	" + TABLE + "." + ID_EVENT + ", " + 
				"	" + TABLE + "." + TIMESTAMP + ", " + 
				"	" + TABLE + "." + USERNAME + ", " + 
				"	" + TABLE + "." + ID_COMMENT + ", " + 
				"	" + TABLE + "." + DESCRIPTION + " " + 
				" FROM " + TABLE + 
				" WHERE " + TABLE + "." + ID_COMMENT + " = ?";

				stmt = conn.prepareStatement(query);
				
				stmt.setLong(1,commentID);
				
				rs = stmt.executeQuery();

				
				if (rs.next()) {
					comment = fillComment(rs);
				}

				rs.close();
				stmt.close();

				if (connectionIsOpenLocaly())
					closeConnection();

				return comment;

	}
	
	public List<TfsAuditComment> getComments(Date fromTimeStamp, Date toTimeStamp, String userName, Long eventId, Integer count) throws Exception {
		List<TfsAuditComment> commentList = new ArrayList<TfsAuditComment>();

		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		ResultSet rs;

		String query = 
				"SELECT " +
				"	" + TABLE + "." + ID_EVENT + ", " + 
				"	" + TABLE + "." + TIMESTAMP + ", " + 
				"	" + TABLE + "." + USERNAME + ", " + 
				"	" + TABLE + "." + ID_COMMENT + ", " + 
				"	" + TABLE + "." + DESCRIPTION + " " + 
				" FROM " + TABLE;
		
		
		String where = "";
		if (fromTimeStamp!=null)
			where += " AND " + TABLE + "." + TIMESTAMP + " > ? "; 
		if (toTimeStamp!=null)
			where += " AND " + TABLE + "." + TIMESTAMP + " < ? "; 
		if (userName!=null)
			where += " AND " + TABLE + "." + USERNAME + " = ? "; 
		if (eventId!=null)
			where += " AND " + TABLE + "." + ID_EVENT + " = ? "; 
			
		if (!where.isEmpty())
			query += where.replaceFirst(" AND ", " WHERE ");
		
		query += 
				" ORDER BY " + TABLE + "." + TIMESTAMP + " DESC ";
		
		if (count!=null)
			query += 
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
		if (eventId!=null) {
			stmt.setLong(j,eventId);
			j++;
		}
		if (count!=null)
			stmt.setInt(j,count);

		rs = stmt.executeQuery();

		while (rs.next()) {
			commentList.add(fillComment(rs));
		}

		rs.close();
		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();

		return commentList;
	}

	

}
