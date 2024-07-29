package com.tfsla.vod.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.main.CmsLog;

import com.tfsla.data.baseDAO;
import com.tfsla.vod.model.TfsVodMyList;
import com.tfsla.vod.model.TfsVodNews;

public class VodMyListDAO extends baseDAO {
	
	private static String MY_LIST_TABLE = "TFS_VOD_MY_LIST";
	
	private static String COLUMN_USER = "USER_ID";
	private static String COLUMN_SOURCE = "SOURCE";
	private static String COLUMN_FECHA = "FECHA";
	private static String COLUMN_SOURCEEXPIRATION = "SOURCE_EXPIRATION";
	private static String COLUMN_SOURCE_MODIFIED = "SOURCE_MODIFIED";
	
	
	private static String VOD_NEWS_TABLE = "TFS_VOD_NEWS";
	private static String COLUMN_DESCRIPTION = "DESCRIPTION";
	private static String COLUMN_DATE = "DATE";
	
	private static String COLUMN_SOURCE_PARENT = "SOURCE_PARENT";
	private static String COLUMN_FECHA_PUBLICACION = "DATE_PUBLICATION";
	private static String COLUMN_FECHA_DISPONIBILIDAD = "DATE_AVAILABILITY";
	
	private int referenceDay = 3;
	
	protected static final Log LOG = CmsLog.getLog(VodMyListDAO.class);

	
	public VodMyListDAO(String refereceDays) {
		referenceDay = Integer.valueOf(refereceDays);
	}
	
	

	public VodMyListDAO() {
	}
	
	
	public List<TfsVodMyList> getListByUser(String userID) throws Exception {
		List<TfsVodMyList> listado = new ArrayList<TfsVodMyList>();
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			String query = "SELECT FECHA,SOURCE, SOURCE_EXPIRATION FROM "+MY_LIST_TABLE+ " WHERE USER_ID = ?" ;

			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

			stmt.setString(1,userID);
				
			ResultSet rs = stmt.executeQuery(query);
			
			while (rs.next()) {
				TfsVodMyList myListElement = fillMyList(rs, userID);
				listado.add(myListElement);
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

	/*Indica si se modifico la fecha de expiracion de los sources que tiene seleccionado un usuario*/
	public boolean getSourceModification (String userID) throws Exception {
		int cantModified = 0;
		
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			String query = "SELECT COUNT(*) as MODIFIED FROM "+MY_LIST_TABLE+ " WHERE USER_ID = ? AND " + COLUMN_SOURCE_MODIFIED +  " =?;";

			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

			stmt.setString(1,userID);
			stmt.setInt(2,1);
				
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				cantModified = rs.getInt("MODIFIED");
			}
			
			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return cantModified>0;
	}
	
	
	
	/*Indica si se modifico la fecha de expiracion de los sources que tiene seleccionado un usuario*/
	public long getSourceModificationDate (String source) throws Exception {
		long modificationDate = 0;
		
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			String query = "SELECT " + COLUMN_SOURCE_MODIFIED +" FROM "+MY_LIST_TABLE+ " WHERE " + COLUMN_SOURCE + " = ? LIMIT 1";

			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

			stmt.setString(1,"/" + source);
				
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				modificationDate = rs.getLong(COLUMN_SOURCE_MODIFIED);
			}
			
			rs.close();
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return modificationDate;
	}
	
	/*Modifica el estado modificado de los sources a false */
	public boolean updateSourceModification (String source,long dateModified) throws Exception {
		int cantModified = 0;
		
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			String query = "UPDATE "+MY_LIST_TABLE+ " SET " + COLUMN_SOURCE_MODIFIED +"= 1, " +COLUMN_SOURCEEXPIRATION + " =? WHERE "+ COLUMN_SOURCE+" = ? ;";

			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
			stmt.setTimestamp(1,new Timestamp( dateModified));	
			stmt.setString(2,"/" + source);
			
			stmt.executeUpdate();
			
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return cantModified>0;
	}

	/*Modifica el estado modificado de los sources a false */
	public void updateMyList(int daysBefore) throws Exception {
		
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			String query = "UPDATE "+MY_LIST_TABLE+ " SET " + COLUMN_SOURCE_MODIFIED +"= 0  WHERE "+ COLUMN_FECHA +" < ? AND "+COLUMN_SOURCE_MODIFIED +" = 1;";

			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
			stmt.setTimestamp(1,new Timestamp( (new Date()).getTime() - daysBefore*24*60*60*1000));	
			
			stmt.executeUpdate();
			
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		
	}

	
	
	/*Modifica el estado modificado de los sources a false */
	public boolean updateSourceModification (String userID) throws Exception {
		int cantModified = 0;
		
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			String query = "UPDATE "+MY_LIST_TABLE+ " SET " + COLUMN_SOURCE_MODIFIED +"= 0  WHERE USER_ID = ? AND " + COLUMN_SOURCE_MODIFIED +  " =1";

			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

			stmt.setString(1,userID);
			
			stmt.executeUpdate();
			
			stmt.close();

		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return cantModified>0;
	}
	
	
	
	public List<TfsVodMyList> getListByUserAndOrder(String userID,String order) throws Exception {
		List<TfsVodMyList> listado = new ArrayList<TfsVodMyList>();
		String query = "";
		try {
			if (!connectionIsOpen())
				OpenConnection();

			Statement stmt;

			stmt = conn.createStatement();
			
			query = "SELECT FECHA,SOURCE, SOURCE_EXPIRATION FROM "+MY_LIST_TABLE+ " WHERE USER_ID =" + "'"+ userID + "'"+ " ORDER BY " + order;

			LOG.debug("getListByUserAndOrder - query:" + query);
			
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				TfsVodMyList myListElement = fillMyList(rs, userID);
				listado.add(myListElement);
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			LOG.error("getListByUserAndOrder");
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return listado;
	}

	public List<TfsVodMyList> getList(Map<String, String> parameters) throws Exception {
		List<TfsVodMyList> listado = new ArrayList<TfsVodMyList>();
		String query = "";
		try {
			String order = " ";  
			String conditions = "";
			for (String keyParameter : parameters.keySet()) {
				if (keyParameter.equals("order")) {
					order = " ORDER BY " + parameters.get(keyParameter);
				} else  {
					if (conditions.equals(""))
						conditions+= " where ";
					conditions += " " +keyParameter + " =  '" + parameters.get(keyParameter) + "' ";
				} 
			} 
			
			
			if (!connectionIsOpen())
				OpenConnection();

			Statement stmt;

			stmt = conn.createStatement();
			
			query = "SELECT USER_ID, FECHA,SOURCE,SOURCE_EXPIRATION FROM " + MY_LIST_TABLE +  conditions +  order + ";";

			LOG.debug("getList - query:" + query);
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				TfsVodMyList myListElement = fillMyList(rs);
				listado.add(myListElement);
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			LOG.error("getList: " + query,  e);
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return listado;
	}

	
	public int getCantUsersFollowing(String source) throws Exception {
		int cantidad =0;
		String query = "";
		try {
			if (!connectionIsOpen())
				OpenConnection();

			Statement stmt;

			stmt = conn.createStatement();
			
			query = "SELECT count(*) as cant FROM "+MY_LIST_TABLE+ " WHERE "+ COLUMN_SOURCE + " = " + source; 
			LOG.debug("getCantUsersFollowing - Query: " +query);
			
			ResultSet rs = stmt.executeQuery(query);

			cantidad = rs.getInt("cant");

			rs.close();
			stmt.close();

		} catch (Exception e) {
			LOG.error("getCantUsersFollowing / error al buscar cantidad de usuarios siguiento un source",e);
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return cantidad;
	}


	private TfsVodMyList fillMyList(ResultSet rs) throws SQLException {
		return fillMyList(rs, null);

	}
	
	private TfsVodMyList fillMyList(ResultSet rs, String userID) throws SQLException {
		TfsVodMyList myList = new TfsVodMyList();

		myList.setFecha(rs.getTimestamp(COLUMN_FECHA));
		myList.setSource(rs.getString(COLUMN_SOURCE));
		myList.setSourceExpiration(rs.getTimestamp(COLUMN_SOURCEEXPIRATION));
		
		if (userID == null)
			myList.setUserId(rs.getString(COLUMN_USER));
		else
			myList.setUserId(userID);
		
		return myList;

	}

	

	public void deleteSource(TfsVodMyList myList) throws Exception {
		String query = "";
		try {

			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			query = "delete from "+MY_LIST_TABLE +" WHERE "+COLUMN_USER+" = ? AND "+COLUMN_SOURCE+"= ?;";
			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

			stmt.setString(1,myList.getUserId());
			stmt.setString(2,myList.getSource());
			
			stmt.executeUpdate();

			stmt.close();
			
			LOG.debug("deleteSource - query: " + query);
			

		} catch (Exception e) {
			LOG.error("deleteSource - query: " + query, e);
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

	}

	public void insertSource(TfsVodMyList myList) throws Exception {
		String query = "";
		try {

			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			query = "insert into "+MY_LIST_TABLE +" ("+COLUMN_USER+","+COLUMN_SOURCE+","+COLUMN_FECHA+","+ COLUMN_SOURCE_MODIFIED +") values (?,?,?,?)";
			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

			stmt.setString(1,myList.getUserId());
			stmt.setString(2,myList.getSource());
			stmt.setTimestamp(3,new Timestamp((new Date()).getTime()));
			stmt.setInt(4,1);
			
			stmt.executeUpdate();

			stmt.close();
			
			LOG.debug("insertSource - query: " + query);
			

		} catch (Exception e) {
			LOG.error("insertSource - query: " + query, e);
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

	}
	
	
	public boolean getNewsChanged () throws Exception {
		int cant =0;
		
		String query = "";
		try {
			if (!connectionIsOpen())
				OpenConnection();

			Statement stmt;

			stmt = conn.createStatement();
			
			query = "SELECT count(*) as MODIFIED FROM opencms.TFS_VOD_NEWS where date > '" + new Timestamp((new Date()).getTime() - referenceDay*24*60*60*1000) + "';";

			LOG.debug("getNewsChanged - query:" + query);
			
			ResultSet rs = stmt.executeQuery(query);
			
			while (rs.next()) {
				cant = rs.getInt("MODIFIED");
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			LOG.error("getNewsChanged",e);
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return cant>0;
		
	}
	
	public void removeOldNews (int removeDays) throws Exception {
		
		String query = "";
		try {
			if (!connectionIsOpen())
				OpenConnection();

			Statement stmt;

			stmt = conn.createStatement();
			
			query = "DELETE FROM opencms.TFS_VOD_NEWS where date < '" + new Timestamp((new Date()).getTime() - removeDays*24*60*60*1000) + "';";

			LOG.debug("removeOldNews - query:" + query);
			
			stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
			
			stmt.close();

		} catch (Exception e) {
			LOG.error("removeOldNews",e);
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	}
	
	public List<TfsVodNews> getNews(String order) throws Exception  {
		List<TfsVodNews> listado = new ArrayList<TfsVodNews>();
		String query = "";
		try {
			if (!connectionIsOpen())
				OpenConnection();

			Statement stmt;

			stmt = conn.createStatement();
			
			query = "SELECT DATE,DATE_AVAILABILITY,DATE_PUBLICATION, SOURCE, SOURCE_PARENT,DESCRIPTION FROM "+VOD_NEWS_TABLE+ " ORDER BY " + order;

			LOG.debug("geNews - query:" + query);
			
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				TfsVodNews myListElement = fillNewsList(rs);
				listado.add(myListElement);
			}

			rs.close();
			stmt.close();

		} catch (Exception e) {
			LOG.error("geNews",e);
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return listado;
	}

	private TfsVodNews fillNewsList(ResultSet rs) throws SQLException {
		TfsVodNews myList = new TfsVodNews();

		myList.setFecha(rs.getTimestamp(COLUMN_DATE));
		myList.setSource(rs.getString(COLUMN_SOURCE));
		myList.setSourceParent(rs.getString(COLUMN_SOURCE_PARENT));
		myList.setDescripcion(rs.getString(COLUMN_DESCRIPTION));
		myList.setDisponibility(rs.getTimestamp(COLUMN_FECHA_DISPONIBILIDAD));
		myList.setFechaPublicacion(rs.getTimestamp(COLUMN_FECHA_PUBLICACION));
		
		
		return myList;
	}

	public void insertNews(TfsVodNews news) throws Exception  {
		String query = "";
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			
			query = "INSERT INTO "+VOD_NEWS_TABLE+ " ( DATE,DATE_AVAILABILITY,DATE_PUBLICATION, SOURCE, SOURCE_PARENT,DESCRIPTION) VALUES (?,?,?,?,?,?);";

			LOG.debug("insertNews - query:" + query);
			
			stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

			stmt.setTimestamp(1,news.getFecha());
			stmt.setTimestamp(2,news.getDisponibility());
			stmt.setTimestamp(3,news.getFechaPublicacion());
			stmt.setString(4,news.getSource());
			stmt.setString(5, news.getSourceParent());
			stmt.setString(6, news.getDescripcion());
			
			stmt.execute();
			
			stmt.close();

		} catch (Exception e) {
			LOG.error("insertNews",e);
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	
	}
}

