package com.tfsla.genericImport.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.utils.TFSDriversContainer;


public class QueryBuilder<T> {

	final static Log LOG = CmsLog.getLog(QueryBuilder.class);
	
	private CmsSqlManager sqlManager;

	private ResultSet res = null;
    private PreparedStatement stmt = null;
    private Connection conn = null;
	private int nextColumn = 1;

	private String query;
	private List<Object> parameters = new ArrayList<Object>();
	private boolean open = true;

	

	public QueryBuilder(String dbPoolName) throws Exception {
		
		this.sqlManager = TFSDriversContainer.getInstance().getSqlManager();
		try {
			
			this.conn = OpenCms.getSqlManager().getConnection(dbPoolName);
		} catch (SQLException e) {
			throw new Exception("No se pudo conseguir la connection para " + dbPoolName, e);
		}
	}

	public QueryBuilder<T> setQueryKeyWithoutProject(String queryKey) throws Exception {
		this.query = queryKey;
		try {
			this.stmt = this.sqlManager.getPreparedStatement(this.conn, this.query);
		} catch (SQLException e) {
			this.close();
			throw new Exception("No se pudo armar el preparedStament para " + queryKey, e);
		}
		return this;
	}

	public QueryBuilder<T> setQueryKeyInThisProject(String queryKey) throws Exception {
		this.query = queryKey;
		try {
			this.stmt = conn.prepareStatement(this.query);
		} catch (SQLException e) {
			this.close();
			throw new Exception("No se pudo armar el preparedStament para " + queryKey, e);
		}
		return this;
	}

	public QueryBuilder<T> setSQLQuery(String sql) throws Exception {
		this.query = sql;
		try {
			this.stmt = this.sqlManager.getPreparedStatementForSql(this.conn, sql);
		} catch (SQLException e) {
			this.close();
			throw new Exception("No se pudo armar el preparedStament para " + sql, e);
		}
		return this;
	}

	public QueryBuilder<T> addParameter(Date value) throws Exception {
		try {
			this.parameters.add(value);
			if (value!=null)
				this.stmt.setTimestamp(this.nextColumn++, new Timestamp(value.getTime()));
			else
				this.stmt.setTimestamp(this.nextColumn++, null);
			return this;
		} catch (SQLException e) {
			this.close();
			throw new Exception("No se pudo setear el valor " + value + " en la pos " + (this.nextColumn - 1)  + " en " + this.stmt , e);
		}
	}


	public QueryBuilder<T> addParameter(int value) throws Exception {
		try {
			this.parameters.add(value);
			this.stmt.setInt(this.nextColumn++, value);
			return this;
		} catch (SQLException e) {
			this.close();
			throw new Exception("No se pudo setear el valor " + value + " en la pos " + (this.nextColumn - 1)  + " en " + this.stmt , e);
		}
	}

	public QueryBuilder<T> addParameter(long value) throws Exception {
		try {
			this.parameters.add(value);
			this.stmt.setLong(this.nextColumn++, value);
			return this;
		} catch (SQLException e) {
			this.close();
			throw new Exception("No se pudo setear el valor " + value + " en la pos " + (this.nextColumn - 1)  + " en " + this.stmt , e);
		}
	}

	public QueryBuilder<T> addParameter(String value) throws Exception {
		try {
			this.parameters.add(value);
			this.stmt.setString(this.nextColumn++, value);
			return this;
		} catch (SQLException e) {
			this.close();
			throw new Exception("No se pudo setear el valor " + value + " en la pos " + (this.nextColumn - 1)  + " en " + this.stmt , e);
		}
	}

	public boolean execute() throws Exception {
        try {
        	this.logExecuting();
            return this.stmt.execute();
        }
        catch (Exception exc) {
        	throw new Exception("Error ejecutando la query " + this, exc);
        } finally {
        	this.close();
        }
	}



	public T execute(ResultSetProcessor<T> processor) throws Exception {
        try {
        	this.logExecuting();
        	this.res = this.stmt.executeQuery();

        	while (this.res.next()) {
        		LOG.debug("execute - row: " + res.toString());
                
                processor.processTuple(this.res);
            }
            return processor.getResult();
        }
        catch (Exception exc) {
        	LOG.error("Error ejecutando la query ",exc);
        	throw new Exception("Error ejecutando la query " + this, exc);
        } finally {
        	this.close();
        }
	}

	private synchronized void close() throws SQLException {
		if(this.open ) {
			res.close();
			stmt.close();
			conn.close();				
			this.open = false;
		}
	}

	@Override
	public String toString() {
		return this.query + this.parameters;
	}

	@Override
	protected void finalize() throws Throwable {
		this.close();
	}


	private void logExecuting() {
		LOG.debug("Executing query: " + this);
	}



	public QueryBuilder<T> addParameter(Object value) throws Exception {
		if (value instanceof String) 
			addParameter((String) value);
		else if (value instanceof Date) 
			addParameter((Date) value);
		else if (value instanceof Integer) 
			addParameter(((Integer) value).intValue());
		else if (value instanceof Long) 
			addParameter(((Long) value).longValue());
		
		return this;
		
	}
}
