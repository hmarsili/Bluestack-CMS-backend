package com.tfsla.opencms.webusers.openauthorization.common;

import java.util.Date;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionBaseService;
import com.tfsla.opencms.webusers.openauthorization.UserProfileData;

public class DataSyncProcess {

	private CmsUser user;
	private CmsObject cms;
	private String siteName;
	private String publicationName;
	private int daysInterval;
	private Date lastSync;
	
	public DataSyncProcess(CmsObject cms) throws CmsException {
		this(cms.getRequestContext().currentUser().getName(), cms);
	}
	
	public DataSyncProcess(String userName, CmsObject cms) throws CmsException {
		this.cms = cms;
		publicationName = "0";
     	TipoEdicionBaseService tService = new TipoEdicionBaseService();
    	try {
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());			
			if (tEdicion!=null)
				publicationName = "" + tEdicion.getId();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	user = cms.readUser(userName);
    	siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		daysInterval = config.getIntegerParam(siteName, publicationName, "webusers", "syncProvidersDataInterval", 0);
		
		lastSync = (Date) user.getAdditionalInfo("USER_LAST_SYNC");
	}
	
	public void providerDataSync(String providerName, UserProfileData profileData) throws Exception {
		CmsLog.getLog(this).debug("Checking dataSync for user " + user.getName() + ", lastSync: " + (lastSync == null ? "-" : lastSync.toString()));
		if(!this.mustSync()) return;
		
		CmsLog.getLog(this).debug("Starting dataSync for user : " + user.getName());
		ProviderDataSync dataSync = new ProviderDataSync(user, siteName, publicationName, this.getModuleNameFromProvider(providerName), profileData);
		dataSync.setDataFromProvider();
		CmsLog.getLog(this).debug("Updating provider data");
		dataSync.updateProviderData(cms);
		CmsLog.getLog(this).debug("Finished updating provider data");
		user.setAdditionalInfo("USER_LAST_SYNC", new Date());
		user.setAdditionalInfo("USER_LAST_SYNC_PROVIDER", providerName);
		user.setAdditionalInfo("URL_" + providerName.toUpperCase(), profileData.getUserUrl());
		CmsLog.getLog(this).debug("Writing data for user " + user.getName());
		cms.writeUser(user);
	}
	
	private String getModuleNameFromProvider(String providerName) {
		if(!providerName.startsWith("webusers")) {
			return "webusers-" + providerName;
		}
		return providerName;
	}
	
	private boolean mustSync() {
		if(lastSync != null) {
			long diff = new Date().getTime() - lastSync.getTime();
			long diffDays = diff / (24 * 60 * 60 * 1000);
			if(diffDays < daysInterval) return false;
		}
		return true;
	}
}
