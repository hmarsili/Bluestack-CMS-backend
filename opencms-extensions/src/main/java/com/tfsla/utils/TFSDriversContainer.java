package com.tfsla.utils;

import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.I_CmsDbContextFactory;
//import org.opencms.db.I_CmsWorkflowDriver;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.db.generic.CmsVfsDriver;

import com.tfsla.exceptions.ApplicationException;
import com.tfsla.opencms.vfs.TfsVfsDriver;

public class TFSDriversContainer {

	private static TFSDriversContainer instance;

	private CmsDriverManager driverManager;
	private CmsSystemConfiguration configuration;

	private CmsSqlManager sqlManager;

	private CmsVfsDriver vfsDriver;

	//private I_CmsWorkflowDriver workflowDriver;

	public static synchronized TFSDriversContainer getInstance() {
		if (TFSDriversContainer.instance == null) {
			TFSDriversContainer.instance = new TFSDriversContainer();
		}
		return TFSDriversContainer.instance;
	}

	private TFSDriversContainer() {
		super();
	}

	public CmsDriverManager getDriverManager() {
		if (this.driverManager == null) {
			throw new ApplicationException(
					"Nadie ha seteado el driverManager. Revise que la configuraci�n de opencms.properties tenga a "
							+ TfsVfsDriver.class.getName() + " o una subclase como el vfsDriver usado");
		}
		return this.driverManager;
	}

	public void setDriverManager(CmsDriverManager driverManager) {
		this.driverManager = driverManager;
	}

	public void setCmsSystemCmsSystemConfiguration(CmsSystemConfiguration configuration) {
		this.configuration = configuration;
	}

	public I_CmsDbContextFactory getDBContextFactory() {
		return this.configuration.getRuntimeInfoFactory();
	}

	public void setSqlManager(CmsSqlManager sqlManager) {
		this.sqlManager = sqlManager;
	}

	public CmsSqlManager getSqlManager() {
		return this.sqlManager;
	}

	public void setVfsDriver(CmsVfsDriver driver) {
		this.vfsDriver = driver;
	}

	public CmsVfsDriver getVfsDriver() {
		return this.vfsDriver;
	}

	/*public void setWorkflowDriver(I_CmsWorkflowDriver workflowDriver) {
		this.workflowDriver = workflowDriver;
	}

	public I_CmsWorkflowDriver getWorkflowDriver() {
		return this.workflowDriver;
	}*/
}
