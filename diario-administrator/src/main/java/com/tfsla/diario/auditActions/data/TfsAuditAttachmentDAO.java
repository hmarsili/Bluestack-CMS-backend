package com.tfsla.diario.auditActions.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.tfsla.data.baseDAO;
import com.tfsla.diario.auditActions.model.TfsAuditAttachment;

public class TfsAuditAttachmentDAO extends baseDAO{
	
	private static final String TABLE = "TFS_EVENT_FILES";

	private static final String ID_EVENT = "ID_EVENT";
	private static final String ID_COMMENT = "ID_COMMENT";
	private static final String ID_ATTACHMENT = "ID_ATTACHMENT";
	private static final String PATH = "PATH";
	private static final String NAME = "NAME";
	
	
	public void purgeAttachmentEvent(Date to) throws Exception {
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;

		stmt = conn.prepareStatement(
				"DELETE a.* FROM " + TABLE + " a " + 
				"	INNER JOIN TFS_EVENT e ON e." + ID_EVENT + " = a." + ID_EVENT +
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
	
	public void insertAttachmentEvent(TfsAuditAttachment attachment) throws Exception
	{
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;

		stmt = conn.prepareStatement(
				"insert into " + TABLE + "( " +
				"	" + PATH + ", " + 
				"	" + NAME + ", " + 
				"	" + ID_COMMENT + ", " + 
				"	" + ID_EVENT + ") " + 

				" values (?,?,?,?)");

		
		stmt.setString(1,attachment.getPath());
		stmt.setString(2,attachment.getName());
		stmt.setLong(3,attachment.getCommentId());
		stmt.setLong(4,attachment.getEventId());
		
		stmt.executeUpdate();

		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
		
	}

	public List<TfsAuditAttachment> getAttachments(Long eventId, Long commentId) throws Exception {
		List<TfsAuditAttachment> attachments = new ArrayList<TfsAuditAttachment>();

		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		ResultSet rs;

		String query = 
				"SELECT " +
				"	" + TABLE + "." + ID_EVENT + ", " + 
				"	" + TABLE + "." + ID_COMMENT + ", " + 
				"	" + TABLE + "." + ID_ATTACHMENT + ", " + 
				"	" + TABLE + "." + PATH + ", " + 
				"	" + TABLE + "." + NAME + "" + 
				" FROM " + TABLE + " ";
				
		String where = "";
		if (eventId!=null)
			where += " AND " + ID_EVENT + " = ? "; 
		if (commentId!=null)
			where += " AND " + ID_COMMENT + " = ? "; 
			
		if (!where.isEmpty())
			query += where.replaceFirst(" AND ", " WHERE ");
	

		stmt = conn.prepareStatement(query);

		int j=1;
		if (eventId!=null) {
			stmt.setLong(j,eventId);
			j++;
		}
		if (commentId!=null) {
			stmt.setLong(j,commentId);
			j++;
		}

		

		rs = stmt.executeQuery();

		while (rs.next()) {
			attachments.add(fillAttachment(rs));
		}

		rs.close();
		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();

		return attachments;
	}

	private TfsAuditAttachment fillAttachment(ResultSet rs) throws SQLException  {
		TfsAuditAttachment attachment = new TfsAuditAttachment();

		attachment.setAttachId(rs.getLong(ID_ATTACHMENT));
		attachment.setPath(rs.getString(PATH));
		attachment.setName(rs.getString(NAME));
		attachment.setEventId(rs.getLong(ID_EVENT));
		attachment.setCommentId(rs.getLong(ID_COMMENT));

		return attachment;

	}


}
