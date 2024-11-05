package com.tfsla.cmsMedios.releaseManager.installer.data;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.List;

public class GenericDAO extends BaseDAO {
	
	public int runSQL(String sqlString, List<SQLParameter> parameters) throws SQLException, ParseException {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(sqlString.replace("\n", " ").replace("\r", ""));
			for(SQLParameter parameter : parameters) {
				this.setParameter(stmt, parameter);
			}
			return stmt.executeUpdate();
		} catch(SQLException ex) {
			throw ex;
		} finally {
			try {
				stmt.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void setParameter(PreparedStatement stmt, SQLParameter parameter) throws SQLException, ParseException {
		
		switch(parameter.getParameterType()) {
		
			case INTEGER:
			case BIT:
				stmt.setInt(parameter.getIndex(), Integer.parseInt(parameter.getValue().toString())); 
				break;
		
			case DOUBLE:
				stmt.setDouble(parameter.getIndex(), Double.parseDouble(parameter.getValue().toString()));
				break;
				
			case LONG:
				stmt.setLong(parameter.getIndex(), Long.parseLong(parameter.getValue().toString()));
				break;
				
			case FLOAT:
				stmt.setFloat(parameter.getIndex(), Float.parseFloat(parameter.getValue().toString()));
				break;
			
			case BOOLEAN:
				stmt.setBoolean(parameter.getIndex(), Boolean.valueOf(parameter.getValue().toString()));
				
			case DATE:
				if(parameter.getFormat() != null && !parameter.getFormat().equals("")) {
					SimpleDateFormat sdf = new SimpleDateFormat(parameter.getFormat());
					java.sql.Timestamp date = new java.sql.Timestamp(sdf.parse(parameter.getValue().toString()).getTime());
					stmt.setTimestamp(parameter.getIndex(), date);
				} else {
					java.sql.Timestamp date = new java.sql.Timestamp(Instant.parse(parameter.getValue().toString()).toEpochMilli());
					stmt.setTimestamp(parameter.getIndex(), date);
				}
				break;
			
			default:
				stmt.setString(parameter.getIndex(), parameter.getValue().toString());
				break;
		}
	}
}
