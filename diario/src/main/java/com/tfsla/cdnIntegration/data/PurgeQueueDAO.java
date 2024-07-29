package com.tfsla.cdnIntegration.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;

import com.tfsla.cdnIntegration.model.PurgePackage;
import com.tfsla.data.baseDAO;

public class PurgeQueueDAO extends baseDAO {

	
	private String RESOURCE_TABLE = "TFS_PURGE_QUEUE";
	private String PROCESS_TABLE = "TFS_PROCESS_PURGE_QUEUE";
	private String RESOURCE = "RESOURCE";
	private String PROCESSID = "PROCESSID";
	private String INSTANT = "INSTANT";
	private String STATUS = "STATUS";
	private String RETRIES = "RETRIES";
	private String SITE = "SITE";
	private String PUBLICATION = "PUBLICATION";

	private String site;
	private String publication;
	
	public PurgeQueueDAO(String site, String publication) {
		this.site = site;
		this.publication = publication;
	}
	
	private PurgePackage fillPackage(ResultSet rs) throws SQLException {
		PurgePackage pac = new PurgePackage();

		pac.setTimestamp(new Date(rs.getTimestamp(INSTANT).getTime()));
		pac.setProcessId(rs.getString(PROCESSID));
		pac.setStatus(rs.getInt(STATUS));
		pac.setRetries(rs.getInt(RETRIES));
		
		return pac;
	}

	public List<String> getResourcesFromPackage(String processId) throws Exception {
		List<String> resources = new ArrayList<String>();
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement(
					"SELECT " 
				    + RESOURCE + ", " + PROCESSID + ", " + INSTANT + " " 
					+ "FROM " + RESOURCE_TABLE + " "
					+ "WHERE " + PROCESSID + "=? "
					+ "AND " + SITE + "=? "
					+ "AND " + PUBLICATION + "=? "					
					);

			stmt.setString(1,processId);
			stmt.setString(2,site);
			stmt.setString(3,publication);

			rs = stmt.executeQuery();
					
			while (rs.next()) {
				resources.add(rs.getString(RESOURCE));
			}

			rs.close();
			stmt.close();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return resources;

	}
	
	public PurgePackage getNextPendingPackage() throws Exception {
		PurgePackage pac = null;
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement(
					"SELECT " 
					+ PROCESSID + ", "
					+ INSTANT + ", "
					+ STATUS + ", "
					+ RETRIES + " "
					+ " FROM " + PROCESS_TABLE 
					+ " WHERE " + STATUS + "=?"
					+ " AND " + SITE + "=? "
					+ " AND " + PUBLICATION + "=? "
					+ " ORDER BY " + INSTANT + " ASC " 
					+ " LIMIT 0,1"
					);

			stmt.setInt(1,PurgePackage.STATUS_PENDING);
			stmt.setString(2,site);
			stmt.setString(3,publication);

			rs = stmt.executeQuery();

			if (rs.next()) {
				pac = fillPackage(rs);
			}

			rs.close();
			stmt.close();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return pac;
		
	}
	
	public PurgePackage getpackage(String processId) throws Exception {
		PurgePackage pac = null;
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement(
					"SELECT " 
					+ PROCESSID + ", "
					+ INSTANT + ", "
					+ STATUS + ", "
					+ RETRIES + " "
					+ "FROM " + PROCESS_TABLE 
					+ "WHERE " + PROCESSID + "=?"
					+ "AND " + SITE + "=? "
					+ "AND " + PUBLICATION + "=? "
					);

			stmt.setString(1,processId);
			stmt.setString(2,site);
			stmt.setString(3,publication);
			
			rs = stmt.executeQuery();

			if (rs.next()) {
				pac = fillPackage(rs);
			}

			rs.close();
			stmt.close();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}

		return pac;
	}

	
	public void updatePackageStatus(String processId, int status, int retries) throws Exception {
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			
			stmt = conn.prepareStatement(
					"UPDATE " + PROCESS_TABLE
					+ " SET " + STATUS + " = ?, "
					+ RETRIES + " = ? "
					+ " WHERE "
					+ PROCESSID + " = ? "
					+ "AND " + SITE + "=? "
					+ "AND " + PUBLICATION + "=? "					
					);
			
			stmt.setInt(1,status);
			stmt.setInt(2,retries);
			stmt.setString(3,processId);
			stmt.setString(4,site);
			stmt.setString(5,publication);
			
			stmt.executeUpdate();

			stmt.close();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	
	}

	public void incrementPackageRetries(String processId) throws Exception {
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			
			stmt = conn.prepareStatement(
					"UPDATE " + PROCESS_TABLE
					+ " SET " + RETRIES + " = " + RETRIES + "+1"
					+ " WHERE "
					+ PROCESSID + " = ? "
					+ "AND " + SITE + "=? "
					+ "AND " + PUBLICATION + "=? "
					);
			
			stmt.setString(1,processId);
			stmt.setString(2,site);
			stmt.setString(3,publication);
			
			stmt.executeUpdate();

			stmt.close();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	
	}

	public PurgePackage createPackageHead(String processId) throws Exception {
		try {
			
			PurgePackage pac = new PurgePackage();
			
			if (!connectionIsOpen())
				OpenConnection();

			Date jDate = new Date();
			java.sql.Timestamp date = new java.sql.Timestamp(jDate.getTime());
			PreparedStatement stmt;
			
			
			stmt = conn.prepareStatement(
					"insert into " + 
							PROCESS_TABLE + 
					"(" + PROCESSID + ", " + INSTANT + ", " + STATUS + ", " + RETRIES + ", " + SITE + ", " + PUBLICATION + ") "
					+ "values (?,?,?,?,?,?)");
			stmt.setString(1,processId);
			stmt.setTimestamp(2, date);
			stmt.setInt(3,PurgePackage.STATUS_NEW);
			stmt.setInt(4,0);
			stmt.setString(5,site);
			stmt.setString(6,publication);
	
			stmt.executeUpdate();

			stmt.close();
			
			pac.setProcessId(processId);
			pac.setStatus(PurgePackage.STATUS_NEW);
			pac.setRetries(0);
			pac.setTimestamp(jDate);
		
			return pac;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		
	}
	
	public int createPackageList(String processId, int maxSize) throws Exception {
		int affectedRows = 0;
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			
			stmt = conn.prepareStatement(
					"UPDATE " + RESOURCE_TABLE + " resUpd "
					        + " INNER JOIN "
					        + "( "
					        + " SELECT " 
					        	+ RESOURCE + ", " 
					        	+ INSTANT + ", "
					        	+ SITE + ", "
					        	+ PUBLICATION
					            + " FROM " + RESOURCE_TABLE + " resSelIn "
					            + " WHERE "
					            + PROCESSID + "=? "
								+ "AND " + SITE + "=? "
								+ "AND " + PUBLICATION + "=? "
					            + " ORDER BY " + INSTANT + " desc "
					            + " LIMIT 0," + maxSize
					        + " ) resSel "
					        + " ON resSel." + RESOURCE + " = resUpd." + RESOURCE + " AND "
					        + " resSel." + INSTANT + " = resUpd." + INSTANT + " AND "
					        + " resSel." + SITE + " = resUpd." + SITE + " AND "
					        + " resSel." + PUBLICATION + " = resUpd." + PUBLICATION
							+ " SET resUpd." + PROCESSID + " = ? "
							+ " WHERE resUpd." + PROCESSID + "=?");
			stmt.setString(1,"");
			stmt.setString(2,site);
			stmt.setString(3,publication);
			stmt.setString(4,processId);
			stmt.setString(5,"");
			
			affectedRows = stmt.executeUpdate();

			stmt.close();
		}
		catch (Exception e) {
			if (e.getMessage().toLowerCase().contains("duplicate")) {
				//deleteDuplicatedResources(processId);
				CmsLog.getLog(this).debug("CDN - error al insertar el process id en el paquete. Se ejecuta borrado de duplicado",e);
			}
		}
		finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return affectedRows;
	}
	
	public int deleteDuplicatedResources(String processId) throws Exception {
		int affectedRows = 0;
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			
			stmt = conn.prepareStatement(
					
					" DELETE TODELETE FROM `opencms`.TFS_PURGE_QUEUE TODELETE "
					+ " INNER JOIN " 
					+ " (SELECT TOPURGE.RESOURCE,TOPURGE.SITE, TOPURGE.PUBLICATION, TOPURGE.INSTANT,"
					+ " TOPURGE.PROCESSID FROM `opencms`.TFS_PURGE_QUEUE TOPURGE "
					+ " INNER JOIN (  "
					+ " SELECT * FROM `opencms`.TFS_PURGE_QUEUE " 
					+ " WHERE PROCESSID = ? ) PROCESSED "
					+ " ON TOPURGE.RESOURCE = PROCESSED.RESOURCE "
					+ " AND TOPURGE.SITE = PROCESSED.SITE "
					+ " AND TOPURGE.PUBLICATION = PROCESSED.PUBLICATION "
					+ " AND TOPURGE.INSTANT = PROCESSED.INSTANT "
					+ " WHERE TOPURGE.PROCESSID = '') PACKAGE " 
					+ " ON PACKAGE.RESOURCE = TODELETE.RESOURCE "  
					+ " AND PACKAGE.SITE = TODELETE.SITE "
					+ " AND PACKAGE.PUBLICATION = TODELETE.PUBLICATION "
					+ " AND PACKAGE.INSTANT = TODELETE.INSTANT " 
					+ " AND PACKAGE.PROCESSID = TODELETE.PROCESSID "
					);
			
			
			stmt.setString(1,processId);
			
			affectedRows = stmt.executeUpdate();

			stmt.close();
		}
		catch (Exception e) {
			CmsLog.getLog(this).debug("CDN - error al borrar registros duplicados con el process id en el paquete. Se ejecuta borrado de duplicado",e);
			
		}
		finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return affectedRows;
	}
	
	
	
	
	public void insertResource(String resourcePath) throws Exception {
		try {
			if (!connectionIsOpen())
				OpenConnection();

			java.sql.Timestamp date = new java.sql.Timestamp(new Date().getTime());
			PreparedStatement stmt;
			
			
			stmt = conn.prepareStatement(
					"insert into " + 
							RESOURCE_TABLE + 
					"(" + RESOURCE + ", " + PROCESSID + ", " + INSTANT + ", " + SITE + ", " + PUBLICATION + ") "
					+ "values (?,?,?,?,?)");
			stmt.setString(1,resourcePath);
			stmt.setString(2,"");
			stmt.setTimestamp(3, date);
			stmt.setString(4,site);
			stmt.setString(5,publication);			
			stmt.executeUpdate();

			stmt.close();
		}
		catch (Exception e) {
			if (!e.getMessage().toLowerCase().contains("duplicate"))
				throw e;
			else
				CmsLog.getLog(this).debug("CDN - intenta insertar duplicada ",e);
			
		}
		finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		
	}

	public boolean resourceExistsInQueue(CmsResource resource) throws Exception {
		boolean exists = false;
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement("SELECT "
					+ RESOURCE + ", " 
					+ PROCESSID + ", " 
					+ INSTANT
					+ " FROM " + RESOURCE_TABLE 
					+ " WHERE " + RESOURCE + "=?"
					+ " AND " + PROCESSID + "=? "
					+ " AND " + SITE + "=? "
					+ " AND " + PUBLICATION + "=? "
					);

			stmt.setString(1,resource.getRootPath());
			stmt.setString(2,"");
			stmt.setString(3,site);
			stmt.setString(4,publication);
			
			rs = stmt.executeQuery();

			if (rs.next()) {
				exists = true;
			}

			rs.close();
			stmt.close();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		
		return exists;
	}
	
	
	public void removePackage(String processId) throws Exception {
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmtQuery;
			ResultSet rs;
			// Busco los ids de los paquetes
			stmtQuery = conn.prepareStatement(
					"SELECT RESOURCE,SITE,PUBLICATION FROM " + RESOURCE_TABLE
					+ " WHERE "
					+  "PROCESSID  = ? ;"
					);
			
			stmtQuery.setString(1,processId);
			
			rs = stmtQuery.executeQuery();
			
			
			while (rs.next()){
				CmsLog.getLog(this).info("Se elimina el resource: " + rs.getString("RESOURCE") + " - Sitio: " + rs.getString("SITE")
						+ " Publication: " + rs.getString("PUBLICATION"));
			}
			
			rs.close();
			stmtQuery.close();
			
			PreparedStatement stmtSubTable;
			//borro los recursos del paquete
			stmtSubTable = conn.prepareStatement(
					"DELETE FROM " + RESOURCE_TABLE
					+ " WHERE "
					+ PROCESSID + " = ? ;"
					);
			
			stmtSubTable.setString(1,processId);
			
			stmtSubTable.executeUpdate();

			stmtSubTable.close();
			
			
			PreparedStatement stmt;
			//Borro el paquete
			stmt = conn.prepareStatement(
					"DELETE FROM " + PROCESS_TABLE
					+ " WHERE "
					+ PROCESSID + " = ? ;"
					);
			
			stmt.setString(1,processId);
			
			stmt.executeUpdate();

			stmt.close();
			
		}
		catch (Exception e) {
			CmsLog.getLog(this).error("CDN - error al eliminar el process id: " + processId, e);
		}
		finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
	
	}
	
	public void removeOldPackages(int retries) throws Exception {

		List<String> listado = new ArrayList<String>();

		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;
			// Busco los ids de los paquetes
			stmt = conn.prepareStatement(
					"SELECT PROCESSID FROM " + PROCESS_TABLE
					+ " WHERE "
					+  "(RETRIES  >= ? OR STATUS = ?)AND "
					+ "  DATEDIFF(now(),INSTANT) >= ? ;"
					);
			
			stmt.setLong(1,retries);
			stmt.setLong(2, PurgePackage.STATUS_ERROR);
			stmt.setLong(3,retries);
			
			rs = stmt.executeQuery();
			
			
			while (rs.next()){
				listado.add(rs.getString(PROCESSID));
			}
			rs.close();
			stmt.close();
		}
		catch (Exception e) {
			CmsLog.getLog(this).error("CDN - error al buscar los elementos a eliminar", e);
		}
		finally {
			if (connectionIsOpenLocaly() && this.connectionIsOpen())
				closeConnection();
		}
		
		for (String processID : listado) {
			CmsLog.getLog(this).info("CDN - No se puede purgar. Se elimina del listado el processID: " + processID);
			removePackage(processID);
			
		}
	
	
	}
	
	
	public boolean existResource(String resource, String site, String publication) throws Exception {
		int count=0;
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;
			ResultSet rs;

			stmt = conn.prepareStatement(
					"SELECT " 
				    + " COUNT(*) CANTIDAD " 
					+ " FROM " + RESOURCE_TABLE + " "
					+ " WHERE " + PROCESSID + "=? "
					+ " AND " + SITE + "=? "
					+ " AND " + PUBLICATION + "=? "		
					+ " AND " + RESOURCE + "=? "
					);

			stmt.setString(1,"");
			stmt.setString(2,site);
			stmt.setString(3,publication);
			stmt.setString(4, resource);
			
			rs = stmt.executeQuery();
				
			
			while (rs.next()) {
				count = rs.getInt("CANTIDAD");
			}
			
			rs.close();
			stmt.close();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		return count>0;

	}

}
