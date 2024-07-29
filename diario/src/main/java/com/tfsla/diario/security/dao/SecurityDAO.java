
package com.tfsla.diario.security.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.tfsla.diario.security.model.Operation;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.tfsla.diario.security.model.Guild;

import com.tfsla.data.baseDAO;



public class SecurityDAO extends baseDAO {

	private static final String TABLE_MODULES = "TFS_SEC_MODULES";
	private static final String TABLE_OPERATIONS = "TFS_SEC_OPERATIONS";
	private static final String TABLE_GUILDS = "TFS_SEC_GUILD";
	private static final String TABLE_GUILD_OPERATIONS = "TFS_SEC_GUILD_OPERATION";
	private static final String TABLE_USER_OPERATIONS = "TFS_SEC_USER_OPERATION";
	private static final String TABLE_USER_GUILDS = "TFS_SEC_USER_GUILD";
	private static final String TABLE_USERS = "CMS_USERS";
	
	private static final String USERNAME_FIELD = "USER_NAME";
	private static final String FIRSTNAME_FIELD = "USER_FIRSTNAME";
	private static final String LASTNAME_FIELD = "USER_LASTNAME";
	private static final String ID_FIELD = "USER_ID";
	private static final String NAME_FIELD = "NAME";
	private static final String MODULE_FIELD = "MODULE";
	private static final String DESCRIPTION_FIELD = "DESCRIPTION";
	private static final String PUBLICATION_FIELD = "PUBLICATION";
	private static final String USER_FIELD = "USER";
	private static final String GUILD_FIELD = "GUILD";
	private static final String OPERATION_FIELD = "OPERATION";
	private static final String REVOKE_FIELD = "REVOKE";
	private static final String ISNEW_FIELD = "ISNEW";

	
	public List<Operation> getModuleOperations(String module) throws Exception  {
		List<Operation> operations = new ArrayList<Operation>();
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		ResultSet rs;

		String query = 
				"SELECT " +
				"	" + NAME_FIELD + "," +
				"	" + MODULE_FIELD + "," +
				" FROM " + TABLE_OPERATIONS + 
				" WHERE " +
				"	" + MODULE_FIELD + "=?";
		
		
		stmt = conn.prepareStatement(query);

		stmt.setString(1,module);
		

		rs = stmt.executeQuery();

		while (rs.next()) {
			Operation op = new Operation();
			op.setName(rs.getString(NAME_FIELD));
			op.setModule(rs.getString(MODULE_FIELD));
			
			operations.add(op);
		}

		rs.close();
		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
		
		return operations;
	}
	
	public List<String> getModules() throws Exception {
		List<String> modules = new ArrayList<String>();

		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		ResultSet rs;

		String query = 
				"SELECT " +
				"	" + NAME_FIELD + 
				" FROM " + TABLE_MODULES; 
			

		stmt = conn.prepareStatement(query);

		
		

		rs = stmt.executeQuery();

		while (rs.next()) {
			modules.add(rs.getString(NAME_FIELD));
		}

		rs.close();
		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();

		return modules;
	}
	
	public void addUserToGuild(String guild, String user, int publication) throws Exception {
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		
		stmt = conn.prepareStatement(
				"insert into " + TABLE_USER_GUILDS + "( " +
				"	" + PUBLICATION_FIELD + ", " + 
				"	" + GUILD_FIELD + ", " + 
				"	" + USER_FIELD + ") " +
				" values (?,?,?)");

		
		stmt.setInt(1,publication);
		stmt.setString(2,guild);
		stmt.setString(3,user);

		int affectedRows = stmt.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Error agregando al usuario al agrupamiento.");
        }

		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
	}

	public void removeUserToGuild(String guild, String user, int publication) throws Exception {
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		
		stmt = conn.prepareStatement(
				"delete from " + TABLE_USER_GUILDS + "" +
				" where	" + PUBLICATION_FIELD + "=? AND " + 
				"	" + GUILD_FIELD + "=? AND " + 
				"	" + USER_FIELD + "=? ");

		
		stmt.setInt(1,publication);
		stmt.setString(2,guild);
		stmt.setString(3,user);

		int affectedRows = stmt.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Error quitando al usuario al agrupamiento.");
        }

		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
	}

	public List<Guild> getUserGuilds(String user, int publication) throws Exception {
		List<Guild> guilds = new ArrayList<Guild>();

		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		ResultSet rs;

		String query = 
				"SELECT " +
				"	" + TABLE_GUILDS + "." + NAME_FIELD + ", " + 
				"	" + TABLE_GUILDS + "." + DESCRIPTION_FIELD +
				" FROM " + TABLE_GUILDS + " " +
				" INNER JOIN " +
				"    " + TABLE_USER_GUILDS + " " +
				" ON " + TABLE_USER_GUILDS + "." + GUILD_FIELD + " = " + TABLE_GUILDS + "." + NAME_FIELD + 
				" WHERE " +
				"	" + TABLE_USER_GUILDS + "." + USER_FIELD + " =? AND " + 
				"	" + TABLE_USER_GUILDS + "." + PUBLICATION_FIELD + " =? ";
				
			

		stmt = conn.prepareStatement(query);

		stmt.setString(1, user);
		stmt.setInt(2, publication);
		
		rs = stmt.executeQuery();

		while (rs.next()) {
			Guild g = new Guild();
			g.setName(rs.getString(NAME_FIELD));
			g.setDescription(rs.getString(DESCRIPTION_FIELD));
			guilds.add(g);
		}

		rs.close();
		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();

		return guilds;
		
	}
	/**
	 * Trae todos los usuarios de una publicacion (admin y no admins).
	 * Si pedimos los usuarios admin eliminamos los superadmin
	 * @param publication
	 * @return
	 * @throws Exception
	 */
	public List<String> getUserPublication(int publication) throws Exception {
		List<String> users = new ArrayList<String>();

		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		ResultSet rs;
		
		String query = 
				"SELECT " +		
				"	" + TABLE_USERS + "." + USERNAME_FIELD + " " + 
				" FROM " + TABLE_USER_GUILDS + " " +
				" INNER JOIN " +
				"    " + TABLE_USERS + " " +
				" ON " + TABLE_USERS + "." + ID_FIELD + " = " + TABLE_USER_GUILDS + "." + USER_FIELD + 
				" WHERE " +
				"	" + TABLE_USER_GUILDS + "." + PUBLICATION_FIELD + " = ? ";
		if(publication == 0) {
			query +=" AND " + TABLE_USER_GUILDS + "." + GUILD_FIELD + " != '0'";
		}
		query += " GROUP BY " + TABLE_USERS + "." + ID_FIELD + "," + TABLE_USERS + "." + USERNAME_FIELD;
			query += " ORDER BY " + TABLE_USERS + "." + ID_FIELD + " ASC";


		stmt = conn.prepareStatement(query);

		stmt.setInt(1, publication);
		
		rs = stmt.executeQuery();

		while (rs.next()) {
			users.add(rs.getString(USERNAME_FIELD));
		}

		rs.close();
		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();

		return users;
		
	}
	/**
	 * Traen todos los usuarios que coincidan con el texto buscado (admin y no admin)
	 * Si pedimos los usuarios admin eliminamos los superadmin
	 * @param publication
	 * @param text
	 * @return
	 * @throws Exception
	 */
	public List<String> getSearchUser(int publication,String text) throws Exception {
		List<String> users = new ArrayList<String>();

		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		ResultSet rs;
		
		String query = 
				"SELECT " +		
				" " + TABLE_USERS + "." + USERNAME_FIELD + " " + 
				" FROM " + TABLE_USER_GUILDS + " " +
				" INNER JOIN " +
				" " + TABLE_USERS + " " +
				" ON " + TABLE_USERS + "." + ID_FIELD + " = " + TABLE_USER_GUILDS + "." + USER_FIELD + 
				" WHERE " +
				" " + TABLE_USER_GUILDS + "." + PUBLICATION_FIELD + " = ? AND" +
				" " + TABLE_USERS + "." + USERNAME_FIELD + " LIKE '?' 0R" +
				" " + TABLE_USERS + "." + FIRSTNAME_FIELD + " LIKE '?' 0R" +
				" " + TABLE_USERS + "." + LASTNAME_FIELD + " LIKE ?" ;
				
		if(publication == 0) {
				query +=" AND " + TABLE_USER_GUILDS + "." + GUILD_FIELD + " != '0'";
		}
				query += " ORDER BY " + TABLE_USERS + "." + ID_FIELD + " ASC";

		stmt = conn.prepareStatement(query);

		stmt.setInt(1,publication);
		stmt.setString(2,text);
		stmt.setString(3,text);
		stmt.setString(4,text);
		
		rs = stmt.executeQuery();

		while (rs.next()) {
			users.add(rs.getString(USERNAME_FIELD));
		}

		rs.close();
		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();

		return users;
		
	}
	
	public List<Guild> getGuilds() throws Exception {
		List<Guild> guilds = new ArrayList<Guild>();

		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		ResultSet rs;

		String query = 
				"SELECT " +
				"	" + NAME_FIELD + ", " + 
				"	" + DESCRIPTION_FIELD + 
				" FROM " + TABLE_GUILDS; 
			

		stmt = conn.prepareStatement(query);

		rs = stmt.executeQuery();

		while (rs.next()) {
			Guild g = new Guild();
			g.setName(rs.getString(NAME_FIELD));
			g.setDescription(rs.getString(DESCRIPTION_FIELD));
			guilds.add(g);
		}

		rs.close();
		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();

		return guilds;
	}
	
	public List<Operation> getOperationsFromGuild(String guild, String module ) throws Exception {
		List<Operation> operations = new ArrayList<Operation>();
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		ResultSet rs;

		String query = 
				"SELECT " +
				"	" + TABLE_OPERATIONS + "." + NAME_FIELD + "," +
				"	" + TABLE_OPERATIONS + "." + MODULE_FIELD + "," +
				"	" + TABLE_GUILD_OPERATIONS + "." + GUILD_FIELD + ", " + 
				"	" + TABLE_GUILD_OPERATIONS + "." + OPERATION_FIELD + " " +
				" FROM " + TABLE_GUILD_OPERATIONS + 
				" INNER JOIN " + TABLE_OPERATIONS + 
				" ON " + TABLE_GUILD_OPERATIONS + "." + OPERATION_FIELD + " = " + TABLE_OPERATIONS + "." + NAME_FIELD + 
				" WHERE " +
				"	" + TABLE_GUILD_OPERATIONS + "." + GUILD_FIELD + "=?";

		if (module!=null)
			query +=" AND " + TABLE_OPERATIONS + "." + MODULE_FIELD + "=?";
		
		stmt = conn.prepareStatement(query);

		stmt.setString(1,guild);
		
		if (module!=null)
			stmt.setString(2, module);
		

		rs = stmt.executeQuery();

		while (rs.next()) {
			Operation op = new Operation();
			op.setName(rs.getString(TABLE_OPERATIONS + "." + NAME_FIELD));
			op.setModule(rs.getString(TABLE_OPERATIONS + "." + MODULE_FIELD));
			
			operations.add(op);
		}

		rs.close();
		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
		
		return operations;
	}
	
	public void RevokeOperationToGuild(String guild, String operation) throws Exception {
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		
		stmt = conn.prepareStatement(
				"delete from " + TABLE_GUILD_OPERATIONS + " " +
				"   where " +
				"	" + GUILD_FIELD + "=? AND " + 
				"	" + OPERATION_FIELD + "=?");

		
		stmt.setString(1,guild);
		stmt.setString(2,operation);

		int affectedRows = stmt.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Error removiendo permiso de operacion al agrupado.");
        }

		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
			
	}
	
	public void grantOperationToGuild(String guild, String operation) throws Exception {

		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		
		stmt = conn.prepareStatement(
				"insert into " + TABLE_GUILD_OPERATIONS + "( " +
				"	" + GUILD_FIELD + ", " + 
				"	" + OPERATION_FIELD + ") " +
				" values (?,?)");

		
		stmt.setString(1,guild);
		stmt.setString(2,operation);

		int affectedRows = stmt.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Error otorgando permiso de operacion al agrupado.");
        }

		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
			
	}

	public void deleteOperationToUser(String user, int publication, String operation) throws Exception {
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		
		stmt = conn.prepareStatement(
				"DELETE " + TABLE_USER_OPERATIONS + " " +
				"	WHERE " + 
				"	" + PUBLICATION_FIELD + "=? AND" + 
				"	" + USER_FIELD + "=? AND " + 
				"	" + OPERATION_FIELD + "=?  ");


		
		stmt.setInt(1,publication);
		stmt.setString(2,user);
		stmt.setString(3,operation);

		int affectedRows = stmt.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Error borrando permiso de operacion al agrupado.");
        }

		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
			
	
	}
	
	public void RevokeOperationToUser(String user, int publication, String operation) throws Exception {
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		
		stmt = conn.prepareStatement(
				"insert into " + TABLE_USER_OPERATIONS + "( " +
				"	" + PUBLICATION_FIELD + ", " + 
				"	" + USER_FIELD + ", " + 
				"	" + OPERATION_FIELD + ", " +
				"	`" + REVOKE_FIELD + "`) " +
				" values (?,?,?,TRUE) " + 
				"ON DUPLICATE KEY UPDATE " + 
				"  `" + REVOKE_FIELD + "`=TRUE"
				);


		
		stmt.setInt(1,publication);
		stmt.setString(2,user);
		stmt.setString(3,operation);

		int affectedRows = stmt.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Error removiendo permiso de operacion al agrupado.");
        }

		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
			
	}
	
	public void grantOperationToUser(String user, int publication, String operation) throws Exception {
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		
		stmt = conn.prepareStatement(
				"insert into " + TABLE_USER_OPERATIONS + "( " +
				"	" + PUBLICATION_FIELD + ", " + 
				"	" + USER_FIELD + ", " + 
				"	" + OPERATION_FIELD + ", " +
				"	`" + REVOKE_FIELD + "`) " +
				" values (?,?,?,FALSE) " + 
				"ON DUPLICATE KEY UPDATE " + 
				"  `" + REVOKE_FIELD + "`=FALSE"
				);

		
		stmt.setInt(1,publication);
		stmt.setString(2,user);
		stmt.setString(2,operation);

		int affectedRows = stmt.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Error otorgando permiso de operacion al agrupado.");
        }

		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
			
	}

	public List<Operation> getGrantedOperationsFromUser(String user, int publication, String module) throws Exception {
		List<Operation> operations = new ArrayList<Operation>();
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		ResultSet rs;

		String query = 
				"SELECT " +
				"	" + TABLE_OPERATIONS + "." + NAME_FIELD + "," +
				"	" + TABLE_OPERATIONS + "." + MODULE_FIELD + "," +
				"	" + TABLE_USER_OPERATIONS + "." + PUBLICATION_FIELD + ", " + 
				"	" + TABLE_USER_OPERATIONS + "." + USER_FIELD + ", " + 
				"	" + TABLE_USER_OPERATIONS + "." + OPERATION_FIELD + 
				" FROM " + TABLE_USER_OPERATIONS + 
				" INNER JOIN " + TABLE_OPERATIONS + 
				" ON " + TABLE_USER_OPERATIONS + "." + OPERATION_FIELD + " = " + TABLE_OPERATIONS + "." + NAME_FIELD + 
				" WHERE " +
				"	" + TABLE_USER_OPERATIONS + ".`" + REVOKE_FIELD + "`=FALSE AND " +
				"	" + TABLE_USER_OPERATIONS + "." + USER_FIELD + "=? AND " +
				"	" + TABLE_USER_OPERATIONS + "." + PUBLICATION_FIELD + "=? ";

		if (module!=null)
			query +=" AND " + TABLE_OPERATIONS + "." + MODULE_FIELD + "=?";
		
		stmt = conn.prepareStatement(query);

		stmt.setString(1,user);
		stmt.setInt(2,publication);
		
		if (module!=null)
			stmt.setString(3, module);
		

		rs = stmt.executeQuery();

		while (rs.next()) {
			Operation op = new Operation();
			op.setName(rs.getString(TABLE_OPERATIONS + "." + NAME_FIELD));
			op.setModule(rs.getString(TABLE_OPERATIONS + "." + MODULE_FIELD));
			
			operations.add(op);
		}

		rs.close();
		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
		
		return operations;
	}

	public List<Operation> getRevokedOperationsFromUser(String user, int publication, String module) throws Exception {
		List<Operation> operations = new ArrayList<Operation>();
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		ResultSet rs;

		String query = 
				"SELECT " +
				"	" + TABLE_OPERATIONS + "." + NAME_FIELD + "," +
				"	" + TABLE_OPERATIONS + "." + MODULE_FIELD + "," +
				"	" + TABLE_USER_OPERATIONS + "." + PUBLICATION_FIELD + ", " + 
				"	" + TABLE_USER_OPERATIONS + "." + USER_FIELD + ", " + 
				"	" + TABLE_USER_OPERATIONS + "." + OPERATION_FIELD + " " +
				" FROM " + TABLE_USER_OPERATIONS + 
				" INNER JOIN " + TABLE_OPERATIONS + 
				" ON " + TABLE_USER_OPERATIONS + "." + OPERATION_FIELD + " = " + TABLE_OPERATIONS + "." + NAME_FIELD + 
				" WHERE " +
				"	" + TABLE_USER_OPERATIONS + ".`" + REVOKE_FIELD + "`=TRUE AND " +
				"	" + TABLE_USER_OPERATIONS + "." + USER_FIELD + "=? AND " +
				"	" + TABLE_USER_OPERATIONS + "." + PUBLICATION_FIELD + "=?";

		if (module!=null)
			query +=" AND " + TABLE_OPERATIONS + "." + MODULE_FIELD + "=?";
		
		stmt = conn.prepareStatement(query);

		stmt.setString(1,user);
		stmt.setInt(2,publication);
		
		if (module!=null)
			stmt.setString(3, module);
		

		rs = stmt.executeQuery();

		while (rs.next()) {
			Operation op = new Operation();
			op.setName(rs.getString(TABLE_OPERATIONS + "." + NAME_FIELD));
			op.setModule(rs.getString(TABLE_OPERATIONS + "." + MODULE_FIELD));
			
			operations.add(op);
		}

		rs.close();
		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
		
		return operations;
	}

	
	public boolean userHasOperationGrantedByGuild(String user, int publication, String operation) throws Exception {
		
		boolean hasGranted = false;
		
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		ResultSet rs;

		String query = 
				"SELECT " +
				"	1 " +
				" FROM " + TABLE_GUILD_OPERATIONS + 
				" WHERE " +
				"	" + OPERATION_FIELD + "=? AND " +
				"	" + GUILD_FIELD + " IN ( " + 
				"	SELECT " +
				"	" + GUILD_FIELD + " " +
				"	FROM " +
				"	" + TABLE_USER_GUILDS + " " +
				"	WHERE " + 
				"		" + USER_FIELD + " =? AND " +
				"		" + PUBLICATION_FIELD + " =?" + 
				" ) ";
				
		stmt = conn.prepareStatement(query);

		stmt.setString(1,operation);
		stmt.setString(2,user);
		stmt.setInt(3,publication);
		
		

		rs = stmt.executeQuery();

		if (rs.next()) {
			hasGranted = true;
		}

		rs.close();
		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
		
		return hasGranted;
	}

	
	public boolean userHasPersonalizedOperationGranted(String user, int publication, String operation) throws Exception {
		
		boolean hasGranted = false;
		
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		ResultSet rs;

		String query = 
				"SELECT " +
				"	1 " +
				" FROM " + TABLE_USER_OPERATIONS + 
				" WHERE " +
				"	" + OPERATION_FIELD + "=? AND " +
				"	" + USER_FIELD + " =? AND " +
				"	" + PUBLICATION_FIELD + "=? AND " + 
				"	`" + REVOKE_FIELD + "`=FALSE ";
				
		stmt = conn.prepareStatement(query);

		stmt.setString(1,operation);
		stmt.setString(2,user);
		stmt.setInt(3,publication);
		
		rs = stmt.executeQuery();

		if (rs.next()) {
			hasGranted = true;
		}

		rs.close();
		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
		
		return hasGranted;
	}

	public boolean userHasPersonalizedOperationRevoked(String user, int publication, String operation) throws Exception {
		
		boolean hasGranted = false;
		
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		ResultSet rs;

		String query = 
				"SELECT " +
				"	1 " +
				" FROM " + TABLE_USER_OPERATIONS + 
				" WHERE " +
				"	" + OPERATION_FIELD + "=? AND " +
				"	" + USER_FIELD + " =? AND " +
				"	" + PUBLICATION_FIELD + "=? AND " + 
				"	`" + REVOKE_FIELD + "`=TRUE ";
				
		stmt = conn.prepareStatement(query);

		stmt.setString(1,operation);
		stmt.setString(2,user);
		stmt.setInt(3,publication);
		
		rs = stmt.executeQuery();

		if (rs.next()) {
			hasGranted = true;
		}

		rs.close();
		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
		
		return hasGranted;
	}
	

	public boolean isUserSuperAdmin(String user) throws Exception {
		
		boolean hasGranted = false;
		
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		ResultSet rs;

		String query = 
				"SELECT " +
				"	1 " +
				" FROM " + TABLE_USER_GUILDS + 
				" WHERE " +
				"	" + PUBLICATION_FIELD + "=0 AND " + 
				"	" + GUILD_FIELD + "='0' AND " + 
				"	" + USER_FIELD + "=? ";
				
		stmt = conn.prepareStatement(query);

		stmt.setString(1,user);
		
		rs = stmt.executeQuery();

		if (rs.next()) {
			hasGranted = true;
		}

		rs.close();
		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
		
		return hasGranted;
	}
	
	public void removeUserAsSuperAdmin(String user) throws Exception {
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		
		stmt = conn.prepareStatement(
				"delete from " + TABLE_USER_GUILDS + " " +
				" where " + 
				"	" + PUBLICATION_FIELD + "=0 AND " + 
				"	" + GUILD_FIELD + "='0' AND " + 
				"	" + USER_FIELD + "=? ");

		
		stmt.setString(1,user);

		int affectedRows = stmt.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Error removiendo al usuario como superusuario.");
        }

		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
	}

	public void setUserAsSuperAdmin(String user) throws Exception {
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		
		stmt = conn.prepareStatement(
				"insert into " + TABLE_USER_GUILDS + "( " +
				"	" + PUBLICATION_FIELD + ", " + 
				"	" + GUILD_FIELD + ", " + 
				"	" + USER_FIELD + ") " +
				" values (0,0,?)");

		
		stmt.setString(1,user);

		int affectedRows = stmt.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Error agregando al usuario como superusuario.");
        }

		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
	}

	public boolean isUserPublicationAdmin(String user, int publication) throws Exception {
		
		boolean hasGranted = false;
		
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		ResultSet rs;

		String query = 
				"SELECT " +
				"	1 " +
				" FROM " + TABLE_USER_GUILDS + 
				" WHERE " +
				"	" + PUBLICATION_FIELD + "=? AND " + 
				"	" + GUILD_FIELD + "='0' AND " + 
				"	" + USER_FIELD + "=? ";
				
		stmt = conn.prepareStatement(query);

		stmt.setInt(1,publication);
		stmt.setString(2,user);
		
		rs = stmt.executeQuery();

		if (rs.next()) {
			hasGranted = true;
		}

		rs.close();
		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
		
		return hasGranted;
	}
	
	public void removeUserAsPublicationAdmin(String user, int publication) throws Exception {
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		
		stmt = conn.prepareStatement(
				"delete from " + TABLE_USER_GUILDS + " " +
				" where " + 
				"	" + PUBLICATION_FIELD + "=? AND " + 
				"	" + GUILD_FIELD + "='0' AND " + 
				"	" + USER_FIELD + "=? ");

		
		stmt.setInt(1,publication);
		stmt.setString(2,user);

		int affectedRows = stmt.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Error removiendo al usuario como administrador de la publicacion " + publication );
        }

		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
	}

	public void setUserAsPublicationAdmin(String user, int publication) throws Exception {
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		
		stmt = conn.prepareStatement(
				"insert into " + TABLE_USER_GUILDS + "( " +
				"	" + PUBLICATION_FIELD + ", " + 
				"	" + GUILD_FIELD + ", " + 
				"	" + USER_FIELD + ") " +
				" values (?,0,?)");

		
		stmt.setInt(1,publication);
		stmt.setString(2,user);

		int affectedRows = stmt.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Error agregando al usuario como administrador de la publicacion " + publication);
        }

		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
	}
	
public boolean isModuleNewAdmin(String module) throws Exception {
		
		boolean isNew = false;
		
		if (!connectionIsOpen())
			OpenConnection();

		PreparedStatement stmt;
		ResultSet rs;

		String query = 
				"SELECT " + ISNEW_FIELD +
				" FROM " + TABLE_MODULES + 
				" WHERE " + NAME_FIELD + " = ? " ;
				
		stmt = conn.prepareStatement(query);

		stmt.setString(1,module);
		
		rs = stmt.executeQuery();

		if (rs.next()) {
			if (rs.getInt(1) == 1)
				return true;
		}

		rs.close();
		stmt.close();

		if (connectionIsOpenLocaly())
			closeConnection();
		
		return isNew;
	}
	
	public JSONArray getUsersInRolsPublication(String id, String guild) throws Exception {
	
		
		if (!connectionIsOpen())
			OpenConnection();

		
		JSONArray result = new JSONArray();
		
		String query = "select CMS_USERS.USER_NAME,CMS_USERS.USER_FIRSTNAME,CMS_USERS.USER_LASTNAME " +
		" from CMS_USERS INNER JOIN TFS_SEC_USER_GUILD ON CMS_USERS.USER_ID = TFS_SEC_USER_GUILD.USER " + 
		" where TFS_SEC_USER_GUILD.PUBLICATION=? AND TFS_SEC_USER_GUILD.GUILD=?;";
		
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1,id);
		stmt.setString(2,guild);
		
		ResultSet rs = stmt.executeQuery();
		
		while (rs.next()) {
			JSONObject user = new JSONObject();
			user.put("userName",rs.getString("USER_NAME"));
			user.put("firstname",rs.getString("USER_FIRSTNAME"));
			user.put("lastname",rs.getString("USER_LASTNAME"));
			result.add(user);
		}
		
		stmt.close();
		
		conn.close();
		
		return result;
		
	
	}

	
}
