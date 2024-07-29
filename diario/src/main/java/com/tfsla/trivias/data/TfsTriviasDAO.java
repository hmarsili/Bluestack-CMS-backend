package com.tfsla.trivias.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.data.baseDAO;
import com.tfsla.trivias.model.TfsTrivia;

public class TfsTriviasDAO extends baseDAO {
	
	private static String TABLE_TRIVIAS = "TFS_TRIVIAS";
	private static String TFS_TRIVIAS_RESULTS_BY_CATEGORIES = "TFS_TRIVIAS_RESULTS_BY_CLASSIFICATION";
	private static String TFS_TRIVIAS_RESULTS_BY_USERS = "TFS_TRIVIAS_RESULTS_BY_USERS";
	
	private static String COL_SITE = "SITE";
	private static String COL_PUBLICATION = "PUBLICATION";
	private static String COL_PATH = "PATH";
	private static String COL_CANT_USUARIOS = "CANT_USERS";
	private static String COL_ID_TRIVIA = "ID_TRIVIA";
	
	private static String COL_RESULT_NAME = "RESULT_NAME";
	private static String COL_CANT_USERS = "CANT_USERS";
	private static String COL_RESULT_TYPE = "RESULT_TYPE";
	private static String COL_RESULT_POINTS = "RESULT_POINTS";
	private static String COL_DATE = "DATE";
	private static String COL_TIME_RESOLUTION = "TIME_RESOLUTION";
	private static String COL_USER_ID = "USER_ID";
	private static String COL_CLOSE_DATE = "CLOSE_DATE";
	private static String COL_IP = "IP";	
	
	protected static final Log LOG = CmsLog.getLog(TfsTriviasDAO.class);
	
	
	public int insertTrivia(TfsTrivia trivia) throws Exception{
		
		String query = "";
		int last_inserted_id = 0;
		
		try {

			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			query = "INSERT into "+TABLE_TRIVIAS +" ("+COL_SITE+","+COL_PUBLICATION+","+COL_PATH+","+ COL_CANT_USUARIOS +") values (?,?,?,?)";
			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

			stmt.setString(1,trivia.getSite());
			stmt.setInt(2,trivia.getPublication());
			stmt.setString(3,trivia.getPath());
			stmt.setInt(4,1);
			
			stmt.executeUpdate();
			
			ResultSet rs = stmt.getGeneratedKeys();
            if(rs.next())
                last_inserted_id = rs.getInt(1);
            
			stmt.close();
			
		} catch (Exception e) {
			LOG.error("Insert Trivia error: " + query, e);
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		
		return last_inserted_id;

	}
	
	public void updateCantUsersTrivia(int idTrivia) throws Exception{
		
		String query = "";
		
		try {

			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			query = "UPDATE "+TABLE_TRIVIAS +" SET "+ COL_CANT_USUARIOS +" = "+ COL_CANT_USUARIOS +"+1 WHERE "+ COL_ID_TRIVIA+" = ?";
			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

			stmt.setInt(1,idTrivia);
			
			stmt.executeUpdate();
			stmt.close();
			
		} catch (Exception e) {
			LOG.error("Insert Trivia error: " + query, e);
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

	}
	
	public int getIDTrivia(String path, String site, int publication) throws Exception {
		
		int idTrivia = 0;
		
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			String query = "SELECT " + COL_ID_TRIVIA + " as ID_TRIVIA FROM " + TABLE_TRIVIAS + " WHERE "+ COL_PATH +" = ? AND " + COL_SITE +  " =? AND " + COL_PUBLICATION +  " =?;";

			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

			stmt.setString(1,path);
			stmt.setString(2,site);
			stmt.setInt(3,publication);
				
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				idTrivia = rs.getInt("ID_TRIVIA");
			}
			
			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return idTrivia;
	}
	
	public boolean existTriviaResult(int idTrivia, String resultName) throws Exception {
		
		int cantReg = 0;
		
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			String query = "SELECT COUNT(*) as CANTIDAD FROM " + TFS_TRIVIAS_RESULTS_BY_CATEGORIES + " WHERE "+ COL_ID_TRIVIA +" = ? AND " + COL_RESULT_NAME +  " =?;";

			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

			stmt.setInt(1,idTrivia);
			stmt.setString(2,resultName);
				
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				cantReg = rs.getInt("CANTIDAD");
			}
			
			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return cantReg>0;
	}
	
	public void insertTriviaResults(int idTrivia, String resultName) throws Exception{
		
		String query = "";
		
		try {

			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			query = "INSERT into "+TFS_TRIVIAS_RESULTS_BY_CATEGORIES +" ("+COL_ID_TRIVIA+","+COL_RESULT_NAME+","+COL_CANT_USERS+") values (?,?,?)";
			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

			stmt.setInt(1,idTrivia);
			stmt.setString(2,resultName);
			stmt.setInt(3,1);
			
			stmt.executeUpdate();
			stmt.close();
			
		} catch (Exception e) {
			LOG.error("Insert Trivia error: " + query, e);
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

	}
	
	public void updateCantUsersTriviaResults(int idTrivia, String resultName) throws Exception{
		
		String query = "";
		
		try {

			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			query = "UPDATE "+TFS_TRIVIAS_RESULTS_BY_CATEGORIES +" SET "+ COL_CANT_USUARIOS +" = "+ COL_CANT_USUARIOS +"+1 WHERE "+ COL_ID_TRIVIA+" = ? AND "+ COL_RESULT_NAME+" = ?; ";
			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

			stmt.setInt(1,idTrivia);
			stmt.setString(2,resultName);
			
			stmt.executeUpdate();
			stmt.close();
			
		} catch (Exception e) {
			LOG.error("Insert Trivia error: " + query, e);
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}
	
	public void insertUserResults(TfsTrivia trivia, long dateModified, String remoteIp) throws Exception{
		
		String query = "";
		
		try {

			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			query = "INSERT into "+ TFS_TRIVIAS_RESULTS_BY_USERS +" ("+COL_ID_TRIVIA+","+COL_PATH+","+COL_USER_ID +","+ COL_RESULT_TYPE+","+COL_RESULT_NAME+","+COL_RESULT_POINTS+","+COL_DATE+","+COL_TIME_RESOLUTION+","+ COL_IP +") values (?,?,?,?,?,?,?,?,?)";
			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

			stmt.setInt(1,trivia.getIdTrivia());
			stmt.setString(2,trivia.getPath());
			stmt.setString(3,trivia.getUserId());
			stmt.setString(4,trivia.getResultType());
			stmt.setString(5,trivia.getResultName());
			stmt.setString(6,trivia.getResultPoints());
			stmt.setTimestamp(7,new Timestamp(dateModified));	
			stmt.setInt(8,trivia.getTimeResolution());
			stmt.setString(9,remoteIp);
			
			stmt.executeUpdate();
			stmt.close();
			
		} catch (Exception e) {
			LOG.error("Insert Trivia error: " + query, e);
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}
	
	public boolean existUserTriviaResult (int idTrivia, String userID) throws Exception {
		
		int cantReg = 0;
		
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			String query = "SELECT COUNT(*) as CANTIDAD FROM " + TFS_TRIVIAS_RESULTS_BY_USERS + " WHERE "+ COL_ID_TRIVIA +" = ? AND " + COL_USER_ID +  " =?;";

			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

			stmt.setInt(1,idTrivia);
			stmt.setString(2,userID);
				
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				cantReg = rs.getInt("CANTIDAD");
			}
			
			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return cantReg>0;
	}
	
	public boolean existIPTriviaResult (int idTrivia, String remoteIP) throws Exception {
		
		int cantReg = 0;
		
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			String query = "SELECT COUNT(*) as CANTIDAD FROM " + TFS_TRIVIAS_RESULTS_BY_USERS + " WHERE "+ COL_ID_TRIVIA +" = ? AND " + COL_IP +  " =?;";

			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

			stmt.setInt(1,idTrivia);
			stmt.setString(2,remoteIP);
				
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				cantReg = rs.getInt("CANTIDAD");
			}
			
			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return cantReg>0;
	}
	
public void deleteTriviaByCloseDate(Timestamp closeDate) throws Exception {
		
		String query = "";
		
		try {

			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			query = "DELETE FROM "+TABLE_TRIVIAS +" WHERE "+ COL_CLOSE_DATE +" <= ?";
			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

			stmt.setTimestamp(1, closeDate);
			
			stmt.executeUpdate();
			stmt.close();
			
		} catch (Exception e) {
			LOG.error("delete Trivia error: " + query, e);
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

	}
	
	public void updateCloseDate(int idTrivia, String closeDate) throws Exception{
		
		String query = "";
		
		try {

			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			query = "UPDATE "+TABLE_TRIVIAS+" SET "+ COL_CLOSE_DATE +" = ?  WHERE "+ COL_ID_TRIVIA+" = ?; ";
			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

			stmt.setTimestamp (1,new Timestamp(Long.valueOf(closeDate)));
			stmt.setInt(2,idTrivia);
			
			stmt.executeUpdate();
			stmt.close();
			
		} catch (Exception e) {
			LOG.error("update close date Trivia error: " + query, e);
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}
	
	public List<TfsTrivia> getResultsByUser (Map<String, String> parameters) throws Exception {
		
		String query="";
		List<TfsTrivia> listado = new ArrayList<TfsTrivia>();
		
		try {

			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			query = "select trivias.ID_TRIVIA ID_TRIVIA, te.descripcion DESCRIPTION, trivias.PUBLICATION PUBLICATION, trivias.PATH PATH, trivias.CANT_USERS CANT_USERS , trivias.CLOSE_DATE CLOSE_DATE, " + 
					" results_user.USER_ID USER_ID, users.USER_NAME  USER_USERNAME, concat(users.USER_FIRSTNAME,' ',users.USER_LASTNAME) USER_NAME, results_user.RESULT_TYPE RESULT_TYPE," +
					" results_user.RESULT_POINTS RESULT_POINTS,  \n" + 
					" results_user.TIME_RESOLUTION TIME_RESOLUTION, results_user.DATE , results_user.RESULT_NAME RESULT_NAME " + 
					" from TFS_TRIVIAS  as trivias " + 
					" inner join TFS_TRIVIAS_RESULTS_BY_USERS  as results_user " + 
					" on trivias.id_trivia = results_user.id_trivia " + 
					" inner join CMS_USERS as users on users.USER_ID = results_user.USER_ID " + 
					" inner join TFS_TIPO_EDICIONES as te on te.id = trivias.publication";
			
			
			List<String> options = new ArrayList<String>();
			if (parameters.containsKey("trivia")) {
					options.add(" trivias.PATH = ? " );
			}
			if (parameters.containsKey("publication")) {
					options.add("  trivias.publication = ?");
			}
			if (parameters.containsKey("dateFrom")) {
					options.add( " results_user.DATE >= ?");
			}
			if (parameters.containsKey("dateTo")) {
					options.add( " results_user.DATE <= ?" );
			}
			
 			String aditional = "";
 			if (options.size() > 0) {
 				for (String condicion : options) {
					if (aditional.equals(""))
						aditional = " WHERE " + condicion;
					else
						aditional += " AND " + condicion;
 				}
 			}
 			query += aditional;

			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

			int i=1;
 			if (parameters.containsKey("trivia")) {
 				stmt.setString(i++, parameters.get("trivia"));
 			}
			if (parameters.containsKey("publication")) {
				stmt.setInt(i++, Integer.valueOf(parameters.get("publication")));
	 		}
			if (parameters.containsKey("dateFrom")) {
				stmt.setString(i++, parameters.get("dateFrom") );
			}
			if (parameters.containsKey("dateTo")) {
				stmt.setString(i++, parameters.get("dateTo"));
			}
 			
			ResultSet rs = stmt.executeQuery();
			
			
			while (rs.next()) {
				listado.add(getTrivia(rs));
			}
			
			rs.close();
			stmt.close();
			
		} catch (Exception e) {
			LOG.error("Error al obtner el listado de Trivias : " + query, e);
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return listado;
	}

	private TfsTrivia getTrivia(ResultSet rs) {
		TfsTrivia trivia = new TfsTrivia (); 
		try {
			trivia.setUserId(rs.getString("USER_ID"));
			trivia.setUser(rs.getString("USER_NAME"));
			trivia.setUserName(rs.getString("USER_USERNAME"));
			trivia.setIdTrivia(rs.getInt("ID_TRIVIA"));
			trivia.setCloseDate(rs.getTimestamp("CLOSE_DATE"));
			trivia.setPath(rs.getString("PATH"));
			trivia.setPublication(rs.getInt("PUBLICATION"));
			trivia.setPublicationName(rs.getString("DESCRIPTION"));
			trivia.setResultName(rs.getString("RESULT_NAME"));
			trivia.setResultPoints(rs.getString("RESULT_POINTS"));
			trivia.setResultType(rs.getString("RESULT_TYPE"));
			trivia.setTimeResolution(rs.getInt("TIME_RESOLUTION"));
			trivia.setCantUsers(rs.getInt("CANT_USERS"));
			trivia.setResultDate(rs.getDate("DATE"));
		} catch (SQLException e) {
			LOG.error("Error al obtner los registros del resultset de Trivias : " , e);
		}
		return trivia;
	}
	
	public List<TfsTrivia> getTriviasByUser(String siteName,int publication, String userId) throws Exception {
		
		return getTriviasByUser(siteName,publication,userId,-1, -1,null);
	}
	
	public List<TfsTrivia> getTriviasByUser(String siteName,int publication, String userId, int size, int page, String resultsType) throws Exception {
		
		return getTriviasByUser(siteName,publication,userId,-1, -1,null, null,null,null,null);
	}
	
	public List<TfsTrivia> getTriviasByUser(String siteName,int publication, String userId, int size, int page, String resultsType, String path, String from, String to, String order) throws Exception {
		
		List<TfsTrivia> listado = new ArrayList<TfsTrivia>();
		
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			String query = "SELECT trivias."+COL_ID_TRIVIA+" ID_TRIVIA, results_user."+COL_PATH+" PATH, results_user."+COL_RESULT_TYPE+"  RESULT_TYPE, results_user."+COL_TIME_RESOLUTION+" TIME_RESOLUTION, results_user."+COL_RESULT_NAME+" RESULT_NAME, results_user."+COL_RESULT_POINTS+" RESULT_POINTS, results_user."+COL_DATE+" DATE "+
					       "FROM "+TABLE_TRIVIAS+" as trivias "+ 
					       "INNER JOIN "+TFS_TRIVIAS_RESULTS_BY_USERS+" as results_user "+
					       "ON trivias."+COL_ID_TRIVIA+" = results_user."+COL_ID_TRIVIA+" AND results_user."+COL_USER_ID+" = ?";
			
			if(path!=null && !path.equals(""))
				query = query + " and trivias."+COL_PATH+"='"+path+"' ";
			
			if(siteName!=null && !siteName.equals(""))
				query = query + " and trivias."+COL_SITE+"='"+siteName+"' ";
			
			if(publication>0)
				query = query + " and trivias."+COL_PUBLICATION+" ='"+publication+"' ";
			
			if(resultsType!=null && !resultsType.equals(""))
				query = query + " and results_user."+COL_RESULT_TYPE+" ='"+resultsType+"' ";
			
			if(from!=null && !from.equals(""))
				query = query + " and results_user."+COL_DATE+" >='"+from+"' ";
				
			if(to!=null && !to.equals(""))
				query = query + " and results_user."+COL_DATE+" <='"+to+"' ";
			
			if(order!=null && !order.equals("")){
				
				if(order.toLowerCase().indexOf("asc")==-1 && order.toLowerCase().indexOf("desc")==-1 )
					order = order + " desc";
					
				query = query + " order by "+order;
			}else
				query = query + " order by DATE desc";
			
			if(size >0){
				int offset = (page - 1) * size;
				query = query + " limit "+offset+","+size;
			}
					
			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

			stmt.setString(1,userId);
				
			ResultSet rs = stmt.executeQuery();
		
			while (rs.next()) {
				TfsTrivia trivia = new TfsTrivia (); 
				
				trivia.setIdTrivia(rs.getInt("ID_TRIVIA"));
				trivia.setPath(rs.getString("PATH"));
				trivia.setResultType(rs.getString("RESULT_TYPE"));
				trivia.setResultName(rs.getString("RESULT_NAME"));
				trivia.setResultPoints(rs.getString("RESULT_POINTS"));
				trivia.setTimeResolution(rs.getInt("TIME_RESOLUTION"));
				trivia.setResultDate(rs.getDate("DATE"));
				
				listado.add(trivia);
			}
			
			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return listado;
	}
	
	public int getTriviasByUserCount(String siteName,int publication, String userId) throws Exception {
		return getTriviasByUserCount(siteName,publication,userId, null);
	}	
	
	public int getTriviasByUserCount(String siteName,int publication, String userId, String resultsType) throws Exception {
		
		int triviasCount = 0;
		
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			String query = "SELECT count(DISTINCT trivias."+COL_ID_TRIVIA+") as CANT "+
					       "FROM "+TABLE_TRIVIAS+" as trivias "+ 
					       "INNER JOIN "+TFS_TRIVIAS_RESULTS_BY_USERS+" as results_user "+
					       "ON trivias."+COL_ID_TRIVIA+" = results_user."+COL_ID_TRIVIA+" AND results_user."+COL_USER_ID+" = ?";
			
			if(siteName!=null && !siteName.equals(""))
				query = query + " and trivias."+COL_SITE+"='"+siteName+"' ";
			
			if(publication>0)
				query = query + " and trivias."+COL_PUBLICATION+" ='"+publication+"' ";
			
			if(resultsType!=null && !resultsType.equals(""))
				query = query + " and results_user."+COL_RESULT_TYPE+" ='"+resultsType+"' ";
					
			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

			stmt.setString(1,userId);
				
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				triviasCount = rs.getInt("CANT");
			}
			
			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return triviasCount;
		
	}
	
	public int getAllTriviasByUserCount(String siteName,int publication, String userId, String resultsType) throws Exception {
		
		int triviasCount = 0;
		
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			String query = "SELECT count(*) as CANT "+
					       "FROM "+TABLE_TRIVIAS+" as trivias "+ 
					       "INNER JOIN "+TFS_TRIVIAS_RESULTS_BY_USERS+" as results_user "+
					       "ON trivias."+COL_ID_TRIVIA+" = results_user."+COL_ID_TRIVIA+" AND results_user."+COL_USER_ID+" = ?";
			
			if(siteName!=null && !siteName.equals(""))
				query = query + " and trivias."+COL_SITE+"='"+siteName+"' ";
			
			if(publication>0)
				query = query + " and trivias."+COL_PUBLICATION+" ='"+publication+"' ";
			
			if(resultsType!=null && !resultsType.equals(""))
				query = query + " and results_user."+COL_RESULT_TYPE+" ='"+resultsType+"' ";
					
			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

			stmt.setString(1,userId);
				
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				triviasCount = rs.getInt("CANT");
			}
			
			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return triviasCount;
		
	}
	
	public List<TfsTrivia> getTopTenByResultType (String  triviaPath,String resultType,String limit) throws Exception{
		String query="";
		List<TfsTrivia> listado = new ArrayList<TfsTrivia>();
		
		try {

			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			query = "Select trivias.ID_TRIVIA ID_TRIVIA, users.USER_NAME  USER_USERNAME,"
					+ " concat(users.USER_FIRSTNAME,' ',users.USER_LASTNAME) USER_NAME, "
					+ "trivias_users.RESULT_POINTS RESULT_POINTS ,"
					+ " trivias_users.RESULT_NAME RESULT_NAME, "
					+ " trivias_users." + COL_TIME_RESOLUTION + " TIME_RESOLUTION, "
					+ " trivias_users." + COL_DATE + " FECHA "
					+ " from opencms.TFS_TRIVIAS trivias   "
					+ " inner join opencms.TFS_TRIVIAS_RESULTS_BY_USERS trivias_users on  trivias.ID_TRIVIA  = trivias_users.id_trivia  "
					+ " Inner join opencms.CMS_USERS users on users.USER_ID = trivias_users.USER_ID "
					+ " where trivias.PATH = ? ";
			if (resultType.equals("scale"))
					query += " ORDER BY trivias_users.result_points desc, trivias_users.TIME_RESOLUTION asc, trivias_users.DATE asc limit "+ limit +";" ;
			else
				query += " ORDER BY trivias_users.result_name desc limit "+ limit + ";" ;
			
			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
			
			stmt.setString(1, triviaPath);
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				TfsTrivia trivia = new TfsTrivia();
				trivia.setUser(rs.getString("USER_USERNAME"));
				trivia.setResultPoints(rs.getString("RESULT_POINTS"));
				trivia.setTimeResolution(rs.getInt("TIME_RESOLUTION"));
				trivia.setResultName(rs.getString("RESULT_NAME")==null? "":rs.getString("RESULT_NAME"));
				trivia.setResultDate(rs.getDate("FECHA"));
				listado.add(trivia);
			}
			
			rs.close();
			stmt.close();
			
		} catch (Exception e) {
			LOG.error("Error al obtner el listado de Trivias : " + query, e);
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return listado;
	}
	
	public int getCantUsers (String path) throws Exception{
		String query="";
		int result=0;
		try {

			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			query = "Select CANT_USERS "
					+ " from opencms.TFS_TRIVIAS trivias   "
					+ " where trivias.PATH = ? ";
			
			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
			
			stmt.setString(1, path);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				result = rs.getInt("CANT_USERS");
			}
			
			rs.close();
			stmt.close();
			
		} catch (Exception e) {
			LOG.error("Error al obtner el listado de Trivias : " + query, e);
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return result;
	}
	
	public List<TfsTrivia> getCantUsersByClassification (String path) throws Exception{
		String query="";
		List<TfsTrivia> trivias= new ArrayList<TfsTrivia>() ;
		try {

			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			/*query = "select results_user.RESULT_NAME result_name, count(*) CANT_USERS" + 
					" from TFS_TRIVIAS  as trivias " + 
					" inner join TFS_TRIVIAS_RESULTS_BY_USERS  as results_user  on trivias.id_trivia = results_user.id_trivia " + 
					" WHERE  trivias.path =? " + 
					" group by results_user.RESULT_NAME; ";*/
			query ="SELECT class.RESULT_NAME,class.CANT_USERS FROM opencms.TFS_TRIVIAS_RESULTS_BY_CLASSIFICATION class " + 
					"inner join opencms.TFS_TRIVIAS trivias on trivias.ID_TRIVIA = class.ID_TRIVIA " + 
					" where trivias.PATH = ? ORDER BY class.CANT_USERS DESC;";
			
			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
			
			stmt.setString(1, path);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				TfsTrivia trivia = new TfsTrivia ();
				trivia.setCantUsers(rs.getInt("CANT_USERS"));
				trivia.setResultName(rs.getString("result_name"));
				trivias.add(trivia);
			}
			
			rs.close();
			stmt.close();
			
		} catch (Exception e) {
			LOG.error("Error al obtener el listado de Trivias : " + query, e);
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return trivias;
	}
}
