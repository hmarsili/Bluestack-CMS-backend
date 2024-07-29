package com.tfsla.opencms.vfs;

import java.util.List;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.mysql.CmsVfsDriver;

import com.tfsla.utils.TFSDriversContainer;

public class TfsVfsDriver extends CmsVfsDriver {

	private CmsDriverManager driverManager;

	@Override
	public void init(CmsDbContext dbc, CmsConfigurationManager configurationManager, List successiveDrivers,
			CmsDriverManager driverManager) {
		super.init(dbc, configurationManager, successiveDrivers, driverManager);
		this.initTfsDrivercontainer(configurationManager, driverManager);
		this.driverManager = driverManager;
	}

	private void initTfsDrivercontainer(CmsConfigurationManager configurationManager,
			CmsDriverManager driverManager) {
		TFSDriversContainer.getInstance().setDriverManager(driverManager);
		// get the system configuration
		TFSDriversContainer.getInstance().setCmsSystemCmsSystemConfiguration(
				(CmsSystemConfiguration) configurationManager.getConfiguration(CmsSystemConfiguration.class));
		TFSDriversContainer.getInstance().setSqlManager(this.getSqlManager());
		TFSDriversContainer.getInstance().setVfsDriver(this);
	}

	protected final CmsDriverManager getDriverManager() {
		return this.driverManager;
	}
}
