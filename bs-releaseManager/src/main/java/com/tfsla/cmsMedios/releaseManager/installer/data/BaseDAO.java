package com.tfsla.cmsMedios.releaseManager.installer.data;

import java.sql.Connection;
import java.sql.SQLException;

import org.opencms.db.CmsDbPool;
import org.opencms.main.OpenCms;

public abstract class BaseDAO {
	protected Connection conn;
	private boolean isOpenLocaly;

	public void setConnection(Connection conn)
	{
		this.conn = conn;
		isOpenLocaly = false;
	}

	protected boolean connectionIsOpen() {
		return (conn!=null);
	}

	protected boolean connectionIsOpenLocaly() {
		return isOpenLocaly;
	}
	
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
				conn = OpenCms.getSqlManager().getConnection(CmsDbPool.getDefaultDbPoolName());
				isOpenLocaly = true;
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
				if (conn!=null) {
					conn.close();
					conn = null;
				}
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
}
