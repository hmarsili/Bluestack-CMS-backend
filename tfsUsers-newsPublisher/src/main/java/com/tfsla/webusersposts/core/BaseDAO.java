package com.tfsla.webusersposts.core;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.data.baseDAO;

/**
 * This class implements a database connection
 * and manages instances as database records 
 */
public abstract class BaseDAO extends baseDAO {
	
	/**
	 * Opens a database connection as a local context
	 * @return true if the connection was successfully opened
	 */
	public boolean openConnection() {
		return this.openConnection(true);
	}
	
	/**
	 * Opens a database connection as a local context
	 * @param autoCommit sets the autoCommit property on the current connection
	 * @return true if the connection was successfully opened
	 */
	public boolean openConnection(boolean autoCommit) {
		if (!connectionIsOpen()) {
			try {
				OpenConnection();
				this.conn.setAutoCommit(autoCommit);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Closes the local context database connection
	 */
	public void closeConnection() {
		if (connectionIsOpenLocaly()) {
			try {
				super.closeConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Commits the current transaction
	 * @throws SQLException
	 */
	public void commit() throws SQLException {
		this.conn.commit();
	}
	
	/**
	 * Rollbacks the current SQL transaction
	 * @throws SQLException
	 */
	public void rollback() throws SQLException {
		this.conn.rollback();
	}
	
	/**
	 * Instance to be used to log debug info and exceptions
	 */
	protected Log LOG = CmsLog.getLog(this);
}
