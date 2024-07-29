package com.tfsla.diario.webservices.core.services;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.webusersposts.core.BaseDAO;

/**
 * Represents a service instance that contains a transactionable DAO
 */
public abstract class TransactionableService {

	protected BaseDAO transactionableDAO = null;
	
	protected abstract BaseDAO getTransactionableDAO();
	
	/**
	 * Begins a SQL transaction by using a Connection with autoCommit in false 
	 */
	public void beginTransaction() {
		this.transactionableDAO = getTransactionableDAO();
		this.transactionableDAO.openConnection(false);
	}
	
	/**
	 * Commits a SQL transaction
	 */
	public void commit() {
		try {
			this.transactionableDAO.commit();
			this.transactionableDAO.closeConnection();
		} catch(Exception e) {
			LOG.error(e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Rollbacks a SQL transaction
	 */
	public void rollback() {
		try {
			this.transactionableDAO.rollback();
			this.transactionableDAO.closeConnection();
		} catch(Exception e) {
			LOG.error(e);
			e.printStackTrace();
		}
	}
	
	private Log LOG = CmsLog.getLog(this);
}
