package com.tfsla.genericImport.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.genericImport.dao.QueryBuilder;
import com.tfsla.genericImport.dao.ResultSetProcessor;
import com.tfsla.genericImport.model.DBColumn;


public class DbService {
	final static Log LOG = CmsLog.getLog(DbService.class);

	private String dbPoolName;
	private String dbPoolType;
	
	public DbService(String dbPoolName) {
		this.dbPoolName = dbPoolName;
	}
	
	public DbService(String dbPoolName, String dbPoolType) {
		this.dbPoolName = dbPoolName;
		this.dbPoolType = dbPoolType;
	}
	
	public List<String> getTableNames() throws Exception {
		
		QueryBuilder<List<String>> queryBuilder = new QueryBuilder<List<String>>(dbPoolName);
		LOG.debug("DbService -> poll name: " + dbPoolName + " (" + dbPoolType + ")");
		if(dbPoolType != null){
			if(dbPoolType.equals("MSSQL")){
				queryBuilder.setSQLQuery("SELECT * FROM sys.Tables");
			}else{
				queryBuilder.setSQLQuery("show tables;");
			}
		}else{
			queryBuilder.setSQLQuery("show tables;");
		}		
		
		ResultSetProcessor<List<String>> proc = new ResultSetProcessor<List<String>>() {

			private List<String> tables = new ArrayList<String>();

			public void processTuple(ResultSet rs) throws Exception {

				try {
					tables.add(rs.getString(1));
				}
				catch (SQLException e) {
					throw new Exception("Error al intentar obtener las tablas de la base de datos",e);
				}
			}

			public List<String> getResult() {
				return tables;
			}
		};

		return queryBuilder.execute(proc);

	}
	
	public Map<String,DBColumn> getMapTableDescription(String tableName) throws Exception {
		QueryBuilder<Map<String,DBColumn>> queryBuilder = new QueryBuilder<Map<String,DBColumn>>(dbPoolName);
		if(dbPoolType != null){
			if(dbPoolType.equals("MSSQL")){
				queryBuilder.setSQLQuery("select * from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME='" + tableName + "'");
			}else{
				queryBuilder.setSQLQuery("SHOW COLUMNS FROM " + tableName + ";");
			}
		}else{
			queryBuilder.setSQLQuery("SHOW COLUMNS FROM " + tableName + ";");
		}			
		
		final String tabla = tableName;
		ResultSetProcessor<Map<String,DBColumn>> proc = new ResultSetProcessor<Map<String,DBColumn>>() {

			private Map<String,DBColumn> columns = new LinkedHashMap<String,DBColumn>();

			public void processTuple(ResultSet rs) throws Exception {

				try {
					if(dbPoolType != null){
						if(dbPoolType.equals("MSSQL")){
							DBColumn col = new DBColumn();
							col.setField(rs.getString("COLUMN_NAME"));
							col.setType(rs.getString("DATA_TYPE"));
							col.setIsKey("");
							col.setAllowNull(rs.getString("IS_NULLABLE"));
							col.setDefaultValue(rs.getString("COLUMN_DEFAULT"));
							col.setTable(tabla);
							
							columns.put(rs.getString("COLUMN_NAME"),col);
						}else{
							DBColumn col = new DBColumn();
							col.setField(rs.getString("Field"));
							col.setType(rs.getString("Type"));
							col.setAllowNull(rs.getString("Null"));
							col.setIsKey(rs.getString("Key"));
							col.setDefaultValue(rs.getString("Default"));
							col.setExtra(rs.getString("Extra"));
							col.setTable(tabla);
							
							columns.put(rs.getString("Field"),col);
						}
					}else{
						DBColumn col = new DBColumn();
						col.setField(rs.getString("Field"));
						col.setType(rs.getString("Type"));
						col.setAllowNull(rs.getString("Null"));
						col.setIsKey(rs.getString("Key"));
						col.setDefaultValue(rs.getString("Default"));
						col.setExtra(rs.getString("Extra"));
						col.setTable(tabla);
						
						columns.put(rs.getString("Field"),col);
					}							
				}
				catch (SQLException e) {
					throw new Exception("Error al intentar obtener las tablas de la base de datos",e);
				}
			}

			public Map<String,DBColumn> getResult() {
				return columns;
			}
		};

		return queryBuilder.execute(proc);
		

	}

	public List<DBColumn> getTableDescription(String tableName) throws Exception {
		QueryBuilder<List<DBColumn>> queryBuilder = new QueryBuilder<List<DBColumn>>(dbPoolName);
		if(dbPoolType != null){
			if(dbPoolType.equals("MSSQL")){
				queryBuilder.setSQLQuery("select * from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME='" + tableName + "'");
			}else{
				queryBuilder.setSQLQuery("SHOW COLUMNS FROM " + tableName + ";");
			}
		}else{
			queryBuilder.setSQLQuery("SHOW COLUMNS FROM " + tableName + ";");
		}		
		
		final String tabla = tableName;
		ResultSetProcessor<List<DBColumn>> proc = new ResultSetProcessor<List<DBColumn>>() {

			
			private List<DBColumn> columns = new ArrayList<DBColumn>();

			public void processTuple(ResultSet rs) throws Exception {

				try {
					if(dbPoolType != null){
						if(dbPoolType.equals("MSSQL")){
							DBColumn col = new DBColumn();
							col.setField(rs.getString("COLUMN_NAME"));
							col.setType(rs.getString("DATA_TYPE"));
							col.setIsKey("");
							col.setAllowNull(rs.getString("IS_NULLABLE"));
							col.setDefaultValue(rs.getString("COLUMN_DEFAULT"));
							col.setTable(tabla);
							
							columns.add(col);
						}else{
							DBColumn col = new DBColumn();
							col.setField(rs.getString("Field"));
							col.setType(rs.getString("Type"));
							col.setAllowNull(rs.getString("Null"));
							col.setIsKey(rs.getString("Key"));
							col.setDefaultValue(rs.getString("Default"));
							col.setExtra(rs.getString("Extra"));
							col.setTable(tabla);
							
							columns.add(col);
						}
					}else{
						DBColumn col = new DBColumn();
						col.setField(rs.getString("Field"));
						col.setType(rs.getString("Type"));
						col.setAllowNull(rs.getString("Null"));
						col.setIsKey(rs.getString("Key"));
						col.setDefaultValue(rs.getString("Default"));
						col.setExtra(rs.getString("Extra"));
						col.setTable(tabla);
						
						columns.add(col);
					}
					
					//columns.add(col);
				}
				catch (SQLException e) {
					throw new Exception("Error al intentar obtener las tablas de la base de datos",e);
				}
			}

			public List<DBColumn> getResult() {
				return columns;
			}
		};

		return queryBuilder.execute(proc);
		

	}
	
	public static String getEntryDBName(CmsObject cmsObject)
    {
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
    	String siteName = OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot();

    	return config.getParam(siteName, "", "importManager", "dbPoolName");
    }
	
	public static String getEntryDBType(CmsObject cmsObject)
    {
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
    	String siteName = OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot();

    	return config.getParam(siteName, "", "importManager", "dbPoolType");
    }

}
