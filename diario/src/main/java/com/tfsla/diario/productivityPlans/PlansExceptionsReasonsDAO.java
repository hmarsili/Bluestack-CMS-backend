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

public class PlansExceptionsReasonsDAO extends baseDAO {
	
	private static final Log LOG = CmsLog.getLog(PlansExceptionsReasonsDAO.class);

		public List<PlansExceptionsReasons> getPlansExceptionsReasons(SearchOptionsExcReasons options) throws Exception {
		
		List<PlansExceptionsReasons> PlansExceptionsReasons = new ArrayList<PlansExceptionsReasons>();
		String strQuery = "";
		
		try {
			if (!connectionIsOpen())
				OpenConnection();

			//buscamos todo + lo de la publicacion. 
			strQuery = "SELECT * from TFS_PLANS_EXCEPTIONS_REASONS WHERE ";
			
			//siempre desde el back se envia el sitio y pub. 
			if (!options.isSpecificReasonsPub()) {
				strQuery += " (( SITENAME = \'0\' AND PUBLICATION = 0 ) "
					+ " OR ( SITENAME = \'"+ options.getSiteName()+"\' AND PUBLICATION = "+ options.getPublication()+") ) ";
			} else {
				
				if(options.getSiteName() != null && !options.getSiteName().equals("")) {
					strQuery += " SITENAME = \'"+ options.getSiteName()+"\' AND PUBLICATION = "+ options.getPublication();
				}
			}
				
			if(options.getId()  >  0) {
				strQuery += " AND ID = ?";
			}
	
			if (options.getUserCreation() != null && !options.getUserCreation().equals("")) {
				strQuery += " AND USERCREATION = ?";
			}
			
			strQuery += "  ORDER BY " + options.getOrderBy();
			
			LOG.debug(strQuery);

			
			if (options.getCount() > 0) {
				strQuery += " LIMIT " + options.getCount();
			}

			PreparedStatement stmt = conn.prepareStatement(strQuery);
			
			LOG.debug(stmt.toString());
			
			int filtersCount = 0;

			LOG.debug(stmt.toString());

			if(options.getId() > 0) {
				filtersCount++;
				stmt.setInt(filtersCount, options.getId());
			}
			
			LOG.debug(stmt.toString());

			if(options.getUserCreation() != null && !options.getUserCreation().equals("")) {
				filtersCount++;
				stmt.setString(filtersCount, options.getUserCreation());
			}

			LOG.debug(stmt.toString());
			ResultSet rs = stmt.executeQuery();
			
			LOG.debug("size " + rs.getFetchSize());
			LOG.debug(stmt.toString());
			
			while (rs.next()) {
				PlansExceptionsReasons pexception = fillPlansExceptionsReasons(rs);
				PlansExceptionsReasons.add(pexception);
			}

			rs.close();
			stmt.close();
			
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return PlansExceptionsReasons;
	}
		
	@SuppressWarnings("finally")
	public int insertReason(PlansExceptionsReasons exception) throws Exception, SQLException {

		int id = 0;
		
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			LOG.debug("ENTRO");
			
			stmt = conn.prepareStatement("insert into TFS_PLANS_EXCEPTIONS_REASONS (SITENAME,PUBLICATION,ENABLED,DESCRIPTION,USERCREATION) values (?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1,exception.getSiteName());
			stmt.setInt(2,exception.getPublication());
			stmt.setBoolean(3,exception.isEnabled());
			stmt.setString(4,exception.getDescription());
			stmt.setString(5,exception.getUserCreation());
			
			LOG.debug("stmt " + stmt);

			stmt.executeUpdate();

			LOG.debug("EJECUTO");
			
			stmt = conn.prepareStatement("SELECT ID from TFS_PLANS_EXCEPTIONS_REASONS ORDER BY ID desc LIMIT 1 ");
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
	
	public void deleteReason(int id) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			LOG.debug("ENTRO");

			stmt = conn.prepareStatement("delete from TFS_PLANS_EXCEPTIONS_REASONS where ID=?");
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

			stmt = conn.prepareStatement("update TFS_PLANS_EXCEPTIONS_REASONS set ENABLED = ? where ID=?");
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
	
	
	public void updateReason(PlansExceptionsReasons exception) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			LOG.debug("STMT");
			stmt = conn.prepareStatement("update TFS_PLANS_EXCEPTIONS_REASONS set ENABLED = ?, DESCRIPTION=?, USERCREATION=?"
					+ "where PUBLICATION=? AND SITENAME=? AND ID=?");
			stmt.setBoolean(1,exception.isEnabled());
			stmt.setString(2,exception.getDescription());
			stmt.setString(3,exception.getUserCreation());
			stmt.setInt(4,exception.getPublication());
			stmt.setString(5,exception.getSiteName());
			stmt.setInt(6,exception.getId());

			LOG.debug(stmt);
			
			stmt.executeUpdate();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}
	
	private PlansExceptionsReasons fillPlansExceptionsReasons(ResultSet rs) throws SQLException {
		
		PlansExceptionsReasons pe = new PlansExceptionsReasons();
		pe.setSiteName(rs.getString("SITENAME"));
		pe.setPublication(rs.getInt("PUBLICATION"));
		pe.setId(rs.getInt("ID"));
		pe.setEnabled(rs.getBoolean("ENABLED"));
		pe.setDescription(rs.getString("DESCRIPTION"));
		pe.setUserCreation(rs.getString("USERCREATION"));
		return pe;

	}
	
	

}