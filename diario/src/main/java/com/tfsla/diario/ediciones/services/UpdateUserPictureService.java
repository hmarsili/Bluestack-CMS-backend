package com.tfsla.diario.ediciones.services;

import java.text.SimpleDateFormat;
import java.util.List;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.workplace.CmsWorkplaceAction;

import com.tfsla.utils.TfsAdminUserProvider;

public class UpdateUserPictureService implements I_CmsEventListener {

	@SuppressWarnings("unchecked")
	public void cmsEvent(CmsEvent event) {
		if(event.getType() != I_CmsEventListener.EVENT_LOGIN_USER) return;
		
		try {
			CmsObject cms = getAdminCmsObject();
			CmsUser user = (CmsUser)event.getData().get("data");
			//if(!user.getOuFqn().toLowerCase().contains("webuser")) return;
			if(user.getAdditionalInfo("USER_PICTURE") == null || user.getAdditionalInfo("USER_PICTURE").toString().equals("")) return;
			
			CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration(); 
			String userPicture = user.getAdditionalInfo("USER_PICTURE").toString();
			
			for (CmsSite site : (List<CmsSite>)OpenCms.getSiteManager().getAvailableSites(cms,true)) {
				String siteName = site.getSiteRoot();
				if(site.getTitle().equals("/")) continue;
				cms.getRequestContext().setSiteRoot(siteName);
				
				String path = config.getParam(siteName, String.valueOf(PublicationService.getPublicationId(cms)), "userImageUpload", "vfsPath");
				if(userPicture.contains(path)) return;
				
				if(!path.endsWith("/")) path += "/";
				if(!path.startsWith("/")) path = "/" + path;
				int index = userPicture.indexOf("?");
				if (index != -1) {
					userPicture = userPicture.substring(0, index);
				}
				
				if(cms.existsResource(userPicture, CmsResourceFilter.ALL)) {
					String resourceName = cms.readResource(userPicture, CmsResourceFilter.ALL).getName();
					String stamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(System.currentTimeMillis());
					String[] splits = resourceName.split("\\.");
					resourceName = String.format("%s_%s.%s", splits[0], stamp, splits[1]);
					cms.getRequestContext().setSiteRoot("/");
					this.stealLock(cms, siteName + userPicture);
					String newPath = this.getPrefixedPath(path, resourceName, cms);
					if(cms.existsResource(newPath, CmsResourceFilter.ALL)) {
						this.stealLock(cms, newPath);
					}
					cms.copyResource(siteName + userPicture, newPath);
					user.setAdditionalInfo("USER_PICTURE", newPath);
					cms.writeUser(user);
					OpenCms.getPublishManager().publishResource(cms, newPath);
					if(!cms.getLock(siteName + userPicture).isUnlocked()) {
						cms.unlockResource(siteName + userPicture);
					}
					if(!cms.getLock(newPath).isUnlocked()) {
						cms.unlockResource(newPath);
					}
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void stealLock(CmsObject cms, String path) throws Exception {
		CmsLock lock = cms.getLock(path);
		if(!lock.isUnlocked()) {
			cms.changeLock(path);
			cms.unlockResource(path);
		}
		cms.lockResource(path);
	}
	
	protected static synchronized CmsObject getAdminCmsObject() throws CmsException {
		if(adminCmsObject == null) {
			CmsObject cmsObject =CmsWorkplaceAction.getInstance().getCmsAdminObject();
			adminCmsObject = OpenCms.initCmsObject(cmsObject);
			adminCmsObject.getRequestContext().setSiteRoot("/");
			adminCmsObject.getRequestContext().setCurrentProject(adminCmsObject.readProject("Offline"));
		}
		
		return adminCmsObject;
	}
	
	protected String getPrefixedPath(String path, String fileName, CmsObject adminCms) {
		String retPath = path;
		if(!retPath.endsWith("/")) {
			retPath += "/";
		}
		retPath += fileName.substring(0, 1) + "/";
		
		try {
			if(!adminCms.existsResource(retPath)) {
				adminCms.createResource(retPath, OpenCms.getResourceManager().getResourceType("imagegallery").getTypeId());
				OpenCms.getPublishManager().publishResource(adminCms, retPath);
				OpenCms.getPublishManager().waitWhileRunning(5000);
			}
			CmsLock lock = adminCms.getLock(retPath);
			if(!lock.isUnlocked()) {
				adminCms.changeLock(retPath);
				adminCms.unlockResource(retPath);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return retPath + fileName;
	}
	
	private static CmsObject adminCmsObject = null;
}
