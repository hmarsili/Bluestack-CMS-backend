package com.tfsla.diario.productivityPlans;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.tfsla.data.baseDAO;
import com.tfsla.diario.productivityPlans.model.ProductivitiyPlans;


public class ProductivityPlansDAO extends baseDAO {

	public List<ProductivitiyPlans> getProductivityPlans(int publication, String sitename) throws Exception {
		List<ProductivitiyPlans> productivityPlans = new ArrayList<ProductivitiyPlans>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select * from TFS_PLANS where PUBLICATION=? and SITE=? ORDER BY ID DESC");
			
			stmt.setInt(1, publication);
			stmt.setString(2, sitename);
						
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				productivityPlans.add(fillProductivityPlans(rs));
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return productivityPlans;
		
	}
	
	public List<ProductivitiyPlans> getProductivityPlans( Map<String, String> parameters, boolean joingTo, int sentenceOR) throws Exception {
		List<ProductivitiyPlans> productivityPlans = new ArrayList<ProductivitiyPlans>();

		String query = "SELECT TFS_PLANS.* FROM TFS_PLANS";
		
		if (joingTo)
			query += ", TFS_PLANS_USERS WHERE TFS_PLANS.ID = TFS_PLANS_USERS.ID AND ";
		else
			query += " WHERE ";
		int i = 0;
		for (String key : parameters.keySet()) {
			i ++;
			if(key.equals("TITLE") || key.equals("DESCRIPTION")) {
				query += "TFS_PLANS.TITLE LIKE " + parameters.get(key);
				query += " OR TFS_PLANS.DESCRIPTION LIKE" + parameters.get(key);
			} else  if(key.equals("rol")) {
				if (sentenceOR > 0 ){
					String rolsplt[] = parameters.get(key).split(";");
					int aux = 0;
					for (String rolUser : rolsplt){
						if(sentenceOR > 0 && aux == 0) {
							query += "(";
						}
						query += "TFS_PLANS_USERS.DATA_VALUE = '" + rolUser + "'" ;
						if(sentenceOR > 0 && sentenceOR > aux) {
							query += " OR ";
						}
						if(sentenceOR > 0 && sentenceOR == aux) {
							query += ") ";
						}
						aux ++;
					}
				} else{
					query += "TFS_PLANS_USERS.DATA_VALUE = '" + parameters.get(key) + "' ";
				}				
			} else if(key.equals("USERS")) {
				query += "TFS_PLANS_USERS.DATA_VALUE = " + parameters.get(key);
			} else 	
				query += "TFS_PLANS."+key +" = " + parameters.get(key);
			
			if (parameters.size() > i )
				query += " AND ";
			
		}
		
		query += "ORDER BY ID DESC";
		
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement(query);
						
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				productivityPlans.add(fillProductivityPlans(rs));
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return productivityPlans;
		
	}

	public ProductivitiyPlans getGeneralProductivitiyPlans(int publication, String sitename) throws Exception {
		ProductivitiyPlans pp = new ProductivitiyPlans();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select * from TFS_PLANS where PUBLICATION=? and SITE=? and type=? ORDER BY ID DESC");
			
			stmt.setInt(1, publication);
			stmt.setString(2, sitename);
			stmt.setString(3, "general");
						
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				pp = fillProductivityPlans(rs);
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return pp;
		
	}
	
	public ProductivitiyPlans getProductivityPlan(String id, int publication, String sitename) throws Exception {
		ProductivitiyPlans pp = new ProductivitiyPlans();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select * from TFS_PLANS where ID = ? and PUBLICATION=? and SITE=? ORDER BY ID DESC");
			
			stmt.setString(1,id);
			stmt.setInt(2, publication);
			stmt.setString(3, sitename);
						
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				pp = fillProductivityPlans(rs);
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return pp;
		
	}
	
	public boolean existPlan(String planID ) throws Exception {


		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("Select ID from TFS_PLANS where ID=? ");

			stmt.setString(1, planID);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				if (rs.getString("ID") != null)
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

		return false;
	}

	
	public void deletePlan(String planID) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("delete from TFS_PLANS where ID=?");
			stmt.setString(1, planID);
			stmt.execute();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}
	
	public void changeStatus(String id, int publication, String sitename) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("update TFS_PLANS SET ENABLED = NOT ENABLED where ID = ? and PUBLICATION=? and SITE=?");
			
			stmt.setString(1,id);
			stmt.setInt(2,publication);
			stmt.setString(3,sitename);
						
			stmt.executeUpdate();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		
	}
	
	private ProductivitiyPlans fillProductivityPlans(ResultSet rs) throws SQLException {
		
		ProductivitiyPlans pp = new ProductivitiyPlans();
		pp.setSiteName(rs.getString("SITE"));
		pp.setPublication(rs.getInt("PUBLICATION"));
		pp.setId(rs.getString("ID"));
		pp.setEnabled(rs.getBoolean("ENABLED"));
		pp.setType(rs.getString("TYPE"));
		pp.setTitle(rs.getString("TITLE"));
		pp.setDescription(rs.getString("DESCRIPTION"));
		pp.setFormat(rs.getString("FORMAT"));
		pp.setNewsCount(rs.getInt("NEWSCOUNT"));
		pp.setMethod(rs.getString("METHOD"));
		pp.setMinNum(rs.getInt("MINNUM"));
		pp.setFrecMonday(rs.getBoolean("FRECMONDAY"));
		pp.setFrecThuesday(rs.getBoolean("FRECTHUESDAY"));
		pp.setFrecWednesday(rs.getBoolean("FRECWEDNESDAY"));
		pp.setFrecThursday(rs.getBoolean("FRECTHURSDAY"));
		pp.setFrecFriday(rs.getBoolean("FRECFRIDAY"));
		pp.setFrecSaturday(rs.getBoolean("FRECSATURDAY"));
		pp.setFrecSunday(rs.getBoolean("FRECSUNDAY"));
		pp.setFrecFrom(rs.getLong("FRECFROM"));
		pp.setFrecTo(rs.getLong("FRECTO"));
		pp.setUserCreation(rs.getString("USERCREATION"));
		pp.setUsersType(rs.getString("USERSTYPE"));

		
		
		return pp;

	}
	
	public void insertPlans(ProductivitiyPlans plans) throws Exception {

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("insert into TFS_PLANS (SITE,PUBLICATION,ID,ENABLED,TYPE,TITLE,DESCRIPTION,FORMAT,NEWSCOUNT,METHOD,MINNUM,FRECMONDAY,FRECTHUESDAY,FRECWEDNESDAY,FRECTHURSDAY,FRECFRIDAY,FRECSATURDAY,FRECSUNDAY,FRECFROM,FRECTO,USERCREATION, USERSTYPE) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1,plans.getSiteName());
			stmt.setInt(2,plans.getPublication());
			stmt.setString(3,plans.getId());
			stmt.setBoolean(4,plans.isEnabled());
			stmt.setString(5,plans.getType());
			stmt.setString(6,plans.getTitle());
			stmt.setString(7,plans.getDescription());
			stmt.setString(8,plans.getFormat());
			stmt.setInt(9,plans.getNewsCount());
			stmt.setString(10,plans.getMethod());
			stmt.setInt(11,plans.getMinNum());
			stmt.setBoolean(12,plans.isFrecMonday());
			stmt.setBoolean(13,plans.isFrecThuesday());
			stmt.setBoolean(14,plans.isFrecWednesday());
			stmt.setBoolean(15,plans.isFrecThursday());
			stmt.setBoolean(16,plans.isFrecFriday());
			stmt.setBoolean(17,plans.isFrecSaturday());
			stmt.setBoolean(18,plans.isFrecSunday());
			stmt.setLong(19,plans.getFrecFrom());
			stmt.setLong(20,plans.getFrecTo());
			stmt.setString(21,plans.getUserCreation());
			stmt.setString(22,plans.getUsersType());
			
			stmt.executeUpdate();

			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}

}