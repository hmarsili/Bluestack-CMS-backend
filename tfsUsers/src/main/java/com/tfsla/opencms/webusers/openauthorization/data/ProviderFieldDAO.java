package com.tfsla.opencms.webusers.openauthorization.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.opencms.main.CmsLog;

import com.tfsla.opencms.webusers.openauthorization.common.ProviderListField;

/**
 * Data Access Object para administrar info tipo listas de redes sociales
 */
public class ProviderFieldDAO extends OpenAuthorizationDAO {
	
	/**
	 * Indica si un usuario tiene o no asociada una lista
	 */
	public boolean userHasList(String userId, String listName) {
		return this.getUserList(userId, listName).size() > 0;
	}
	
	public String getListId(String listName) {
		String ret = "";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(
					"select ID_COLLECTION from TFS_PROVIDER_COLLECTIONS "
					+ "where DATA_KEY = ? ");
			
			stmt.setString(1,listName);
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				ret = rs.getString("ID_COLLECTION");
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
			try {
				rs.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}
	
	public void deleteUserList(String userId, String listName) {
		PreparedStatement stmt = null;
		try {
			String idCollection = this.getListId(listName);
			
			if(idCollection == null || idCollection.equals("")) return;
			
			stmt = conn.prepareStatement(
					"delete from TFS_PROVIDER_COLLECTIONS_DATA "
					+ "where ID_COLLECTION = ? AND USER_ID = ? ");
			
			stmt.setString(1,idCollection);
			stmt.setString(2,userId);
			stmt.executeUpdate();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public ArrayList<ProviderListField> getListValues(String listName, String userId) {
		ArrayList<ProviderListField> ret = new ArrayList<ProviderListField>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(
					"select distinct cd.DATA_ID, cd.DATA_VALUE from TFS_PROVIDER_COLLECTIONS c "
					+ "inner join TFS_PROVIDER_COLLECTIONS_DATA cd "
					+ "on c.ID_COLLECTION = cd.ID_COLLECTION "
					+ "where DATA_KEY = ? "
					+ (userId != null && !userId.equals("") ? "and USER_ID = ?" : ""));
			
			stmt.setString(1,listName);
			if(userId != null && ! userId.equals("")) {
				stmt.setString(2,userId);
			}
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				ProviderListField newField = new ProviderListField();
				newField.setId(rs.getString("DATA_ID"));
				newField.setValue(rs.getBytes("DATA_VALUE"));
				ret.add(newField);
			}
			
		} catch(Exception e) {
			CmsLog.getLog(this).error(e.getMessage());
		} finally {
			try {
				stmt.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
			try {
				rs.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}
	
	public ArrayList<ProviderListField> getUserList(String userId, String listName) {
		ArrayList<ProviderListField> ret = new ArrayList<ProviderListField>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(
					"select cd.* from TFS_PROVIDER_COLLECTIONS c "
					+ "inner join TFS_PROVIDER_COLLECTIONS_DATA cd "
					+ "on c.ID_COLLECTION = cd.ID_COLLECTION "
					+ "where DATA_KEY = ? and USER_ID = ? ");
			
			stmt.setString(1,listName);
			stmt.setString(2,userId);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				ProviderListField newField = new ProviderListField();
				newField.setId(rs.getString("DATA_ID"));
				newField.setValue(rs.getString("DATA_VALUE"));
				ret.add(newField);
			}
			
		} catch(Exception e) {
			CmsLog.getLog(this).error(e.getMessage());
		} finally {
			try {
				stmt.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
			try {
				rs.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}
	
	public void saveList(String userId, String providerName, String listName, ArrayList<ProviderListField> list) {
		PreparedStatement stmt = null;
		ResultSet generatedKeys = null;
		try {
			String listId = this.getListId(listName);
			
			if(listId == null || listId.equals("")) {
				stmt = conn.prepareStatement(
						"insert into TFS_PROVIDER_COLLECTIONS ( DATA_KEY, PROVIDER_NAME )" +
						" values (?, ?)", Statement.RETURN_GENERATED_KEYS);
				
				stmt.setString(1, listName);
				stmt.setString(2, providerName);
				int affectedRows = stmt.executeUpdate();
				
				if (affectedRows == 0) {
		            throw new SQLException("Creating provider list failed, no rows affected.");
		        }
				generatedKeys = stmt.getGeneratedKeys();
				
		        if (generatedKeys.next()) {
		        	listId = generatedKeys.getString(1);
		        }
			}
	        
	        for(ProviderListField field : list) {
	        	this.saveListItem(listId, userId, field);
	        }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(generatedKeys != null) {
				try {
					generatedKeys.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void saveListItem(String listId, String userId, ProviderListField field) {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(
					"insert into TFS_PROVIDER_COLLECTIONS_DATA ( "
					+ "  ID_COLLECTION, "
					+ "  USER_ID,"
					+ "  DATA_ID,"
					+ "  DATA_VALUE )" +
					" values (?,?,?,?)");
			
			stmt.setString(1, listId);
			stmt.setString(2, userId);
			stmt.setString(3, field.getId());
			stmt.setString(4, (field.getValue() != null ? field.getValue().toString() : ""));
			
			stmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
