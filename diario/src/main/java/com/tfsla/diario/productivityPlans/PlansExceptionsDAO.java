package com.tfsla.diario.productivityPlans;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.data.baseDAO;
import com.tfsla.diario.productivityPlans.model.*;

public class PlansExceptionsDAO extends baseDAO {
	
	private static final Log LOG = CmsLog.getLog(PlansExceptionsDAO.class);

		public List<PlansExceptions> getPlansExceptions(SearchOptionsExceptions options) throws Exception {
		LOG.debug(options.getSiteName());
		
		List<PlansExceptions> PlansExceptions = new ArrayList<PlansExceptions>();
		String strQuery = "";
		
		try {
			if (!connectionIsOpen())
				OpenConnection();

			strQuery = "SELECT * from TFS_PLANS_EXCEPTIONS WHERE ";
			LOG.debug("strQuery " + strQuery);
	
			if(options.getSiteName() != null && !options.getSiteName().equals("")) {
				strQuery += "SITENAME = ?";
			}
			if(options.getPublication() > 0 ) {
				strQuery += " AND PUBLICATION = ?";
			}

			LOG.debug("strQuery " + strQuery);
			
			if(options.getId() > 0) {
				strQuery += " AND ID = ?";
			}
			LOG.debug("strQuery " + strQuery);

			LOG.debug("options.id" + options.getId());
			
			if(options.getPlanId() != null && !options.getPlanId().equals("")) {
				strQuery += " AND PLANID = ?";
			}
			
			LOG.debug("strQuery " + strQuery);
			
			if(options.getFrom() != null  && options.getFrom()  > 0 && 
					options.getTo() != null  && options.getTo() > 0) {
				strQuery += " AND DATE_FROM >= ? AND DATE_TO <= ?";
			} else if (options.getFrom() != null  && options.getFrom() > 0) {
				strQuery += " AND DATE_FROM > ?";
			}
			
			if(options.getUser() != null && !options.getUser().equals("")) {
				strQuery += " AND USER = ?";
			}
			LOG.debug("strQuery " + strQuery);

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
			if(options.getId() > 0) {
				filtersCount++;
				stmt.setInt(filtersCount, options.getId());
			}
			LOG.debug("strQuery " + strQuery);
			if(options.getPlanId() != null && !options.getPlanId().equals("")) {
				filtersCount++;
				stmt.setString(filtersCount, options.getPlanId());
			}
			LOG.debug(stmt.toString());
			if(options.getFrom() != null  && options.getFrom() > 0) {
				filtersCount++;
				stmt.setLong(filtersCount, options.getFrom());
			}
			LOG.debug(stmt.toString());
			if(options.getTo() != null  && options.getTo() > 0) {
				filtersCount++;
				stmt.setLong(filtersCount, options.getTo());
			}
			
			LOG.debug(stmt.toString());
			if(options.getUser() != null && !options.getUser().equals("")) {
				filtersCount++;
				stmt.setString(filtersCount, options.getUser());
			}
			
			LOG.debug(stmt.toString());
			
			LOG.debug(stmt.toString());
			ResultSet rs = stmt.executeQuery();
			
			LOG.debug("SIZEEEE " + rs.getFetchSize());
			LOG.debug(stmt.toString());
			
			while (rs.next()) {
				LOG.debug("entro");

				PlansExceptions pexception = fillPlansExceptions(rs);
				PlansExceptions.add(pexception);
			}

			rs.close();
			stmt.close();
			
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return PlansExceptions;
	}
		
	@SuppressWarnings("finally")
	public int insertException(PlansExceptions exception) throws Exception {

		int id = 0;
		
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("insert into TFS_PLANS_EXCEPTIONS ("
					+ "SITENAME,PUBLICATION,PLANID,ENABLED,REASONID,USERCREATION,USER,COMMENTS,NEWS,DATE_FROM,DATE_TO) "
					+ "values (?,?,?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1,exception.getSiteName());
			stmt.setInt(2,exception.getPublication());
			stmt.setString(3,exception.getPlanId());
			stmt.setBoolean(4,exception.isEnabled());
			stmt.setInt(5,exception.getReasonId());
			stmt.setString(6,exception.getUsercreation());
			stmt.setString(7,exception.getUser());
			stmt.setString(8,exception.getComments());
			stmt.setInt(9,exception.getNews());
			stmt.setLong(10,exception.getFrom());
			stmt.setLong(11,exception.getTo());
		
			LOG.debug("stmt " + stmt);


			stmt.execute();
			LOG.debug("ejecuto");

			stmt = conn.prepareStatement("SELECT ID from TFS_PLANS_EXCEPTIONS ORDER BY ID desc LIMIT 1 ");
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
	
	public void deleteException(int id) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("delete from TFS_PLANS_EXCEPTIONS where ID=?");
			stmt.setInt(1, id);
			stmt.execute();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}
	
	public void changeStatus(int id, boolean enable) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("update TFS_PLANS_EXCEPTIONS set ENABLED = ? where ID=?");
			stmt.setBoolean(1, enable);
			stmt.setInt(2, id);
			stmt.execute();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}
	
	
	public void updateException(PlansExceptions exception) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("update TFS_PLANS_EXCEPTIONS set ENABLED = ?, REASONID=?, "
					+ "USER=?, DATE_FROM=?, DATE_TO=?, COMMENTS=?, NEWS=? "
					+ "where PUBLICATION=? AND SITENAME=? AND ID=?");
			stmt.setBoolean(1,exception.isEnabled());
			stmt.setInt(2,exception.getReasonId());
			stmt.setString(3,exception.getUser());
			stmt.setLong(4,exception.getFrom());
			stmt.setLong(5,exception.getTo());
			stmt.setString(6,exception.getComments());
			stmt.setInt(7,exception.getNews());
			stmt.setString(8,exception.getSiteName());
			stmt.setInt(9,exception.getPublication());
			stmt.setInt(10,exception.getId());
			stmt.executeUpdate();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}
	
	private PlansExceptions fillPlansExceptions(ResultSet rs) throws SQLException {
		
		PlansExceptions pe = new PlansExceptions();
		pe.setSiteName(rs.getString("SITENAME"));
		pe.setPublication(rs.getInt("PUBLICATION"));
		pe.setId(rs.getInt("ID"));
		pe.setPlanId(rs.getString("PLANID"));
		pe.setEnabled(rs.getBoolean("ENABLED"));
		pe.setReasonId(rs.getInt("REASONID"));
		pe.setUsercreation(rs.getString("USERCREATION"));
		pe.setUser(rs.getString("USER"));
		pe.setFrom(rs.getLong("DATE_FROM"));
		pe.setTo(rs.getLong("DATE_TO"));
		pe.setComments(rs.getString("COMMENTS"));
		pe.setNews(rs.getInt("NEWS"));	
		
		return pe;

	}
	
	

}