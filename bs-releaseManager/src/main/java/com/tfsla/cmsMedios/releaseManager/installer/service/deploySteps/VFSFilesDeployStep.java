package com.tfsla.cmsMedios.releaseManager.installer.service.deploySteps;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;

import com.tfsla.cmsMedios.releaseManager.installer.common.DeployMessage;
import com.tfsla.cmsMedios.releaseManager.installer.service.ReleaseDeployer;
import com.tfsla.cmsMedios.releaseManager.installer.service.SetupProgressService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class VFSFilesDeployStep extends DeployStepContext {

	@SuppressWarnings("rawtypes")
	@Override
	public void deploy() throws Exception {
		customizedFiles = this.deployRequest.getConfig().getCustomizedFiles();
		cmsObject.getRequestContext().setSiteRoot("/");
		
		Thread.sleep(2000);
		List<CmsResource> publishList = new ArrayList<CmsResource>();
		String indexJSP = "/system/modules/com.tfsla.diario.admin/templates/index.jsp";
		String rmFriendlyName = deployRequest.getReleaseName().replace("_", " build ").replaceAll("rm|v", "");
		stealLock(cmsObject, indexJSP);
		CmsProperty property = new CmsProperty("version", rmFriendlyName, rmFriendlyName, true);
		cmsObject.writePropertyObject(indexJSP, property);
		
		String wsJSP = "/system/modules/com.tfsla.diario.webservices/adminConfiguration";
		stealLock(cmsObject, wsJSP);
		CmsProperty propertyWS = new CmsProperty("version", rmFriendlyName, rmFriendlyName, true);
		cmsObject.writePropertyObject(wsJSP, propertyWS);
		
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String timestampStr = ""+timestamp.getTime();
		propertyWS = new CmsProperty("deployDate", timestampStr, timestampStr, true);
		cmsObject.writePropertyObject(wsJSP, propertyWS);
		
		String updateBannerLinkStr = (String)manifest.get("updateBannerLink");
		propertyWS = new CmsProperty("updateBannerLink", updateBannerLinkStr, updateBannerLinkStr, true);
		cmsObject.writePropertyObject(wsJSP, propertyWS);
		
		String updateBannerVFSLinkStr = (String)manifest.get("updateBannerVfsLink");
		propertyWS = new CmsProperty("updateBannerVFSLink", updateBannerVFSLinkStr, updateBannerVFSLinkStr, true);
		cmsObject.writePropertyObject(wsJSP, propertyWS);
		
		JSONObject files = manifest.getJSONObject("files");
		
		if (files.containsKey("removed")) {
			for (Object file : files.getJSONArray("removed")) {
				SetupProgressService.reportProgress("Removing " + file.toString());
				try {
					stealLock(cmsObject, file.toString());
					cmsObject.deleteResource(file.toString(), CmsResource.DELETE_REMOVE_SIBLINGS);
					OpenCms.getPublishManager().publishResource(cmsObject, file.toString());
					publishList.add(cmsObject.readResource(file.toString()));
				} catch (Exception e) {
					SetupProgressService.warning("File: " + file.toString());
					//SetupProgressService.error("Error: " + e.toString());
					continue;
				}
			}
		}
		if (publishList.size() > 0){
			reprocessPublishedFiles(files.getJSONArray("removed"));
			Thread.sleep(3000);
		}
		
		if (files.containsKey("added")) {
			putVFSFiles(files.getJSONArray("added"), releasePath+"files/added/", cmsObject, publishList);
		}
		if (files.containsKey("modified")) {
			putVFSFiles(files.getJSONArray("modified"), releasePath+"files/modified/", cmsObject, publishList);
		}
		
		if (publishList.size() > 0) {
			publishList.add(cmsObject.readResource(indexJSP));
			publishList.add(cmsObject.readResource(wsJSP));
			
			SetupProgressService.reportProgress("Publishing changes...");
			OpenCms.getPublishManager().publishProject(cmsObject,
				new CmsLogReport(Locale.getDefault(), ReleaseDeployer.class),
				OpenCms.getPublishManager().getPublishList(cmsObject, publishList, false)
			);
			OpenCms.getPublishManager().waitWhileRunning();
			Thread.sleep(3000);
			
			//ensure all the files were published
			if (files.containsKey("added")) {
				reprocessPublishedFiles(files.getJSONArray("added"));
				Thread.sleep(2000);
			}
			if (files.containsKey("modified")) {
				reprocessPublishedFiles(files.getJSONArray("modified"));
				Thread.sleep(2000);
			}
			
			//re-initialize the workplace
			SetupProgressService.reportProgress("Re-initializing workspace...");
			OpenCms.getWorkplaceManager().initialize(cmsObject);
			//fire "clear caches" event to reload all cached resource bundles
			OpenCms.fireCmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, new HashMap());
			Thread.sleep(3000);
		} else {
			SetupProgressService.reportProgress("There are no changes to be made on VFS");
		}
	}
	
	protected void reprocessPublishedFiles(JSONArray items) throws Exception {
		SetupProgressService.reportProgress("Validating publish status on " + items.size() + " files");
		for (Object item : items) {
			//check if the file is published
			CmsResource file = cmsObject.readResource(item.toString());
			if (file.getState() != CmsResourceState.STATE_UNCHANGED) {
				OpenCms.getPublishManager().publishResource(cmsObject, item.toString());
				SetupProgressService.reportProgress("Publishing file " + item.toString());
			}
		}
		OpenCms.getPublishManager().waitWhileRunning();
	}

	protected void putVFSFiles(JSONArray files, String releaseDir, CmsObject cmsObject, List<CmsResource> publishList) throws Exception {
		for (Object item : files) {
			CmsResource cmsResource = null;
			String path = item.toString();
			byte[] fileContents = FileUtils.readFileToByteArray(new File(releaseDir + path));
			String stringContents = new String(fileContents);
			if(customizedFiles.contains(path)) {
				DeployMessage message = new DeployMessage();
				message.setFileName(path);
				message.setMessage("ADVERTENCIA: se modificó el archivo");
				message.setContents(stringContents);
				deployMessages.add(message);
				SetupProgressService.warning("Custom file changed: " + path);
				continue;
			}
			processFolders(cmsObject, path, publishList);
			if(cmsObject.existsResource(path)) {
				cmsResource = cmsObject.readResource(path);
				SetupProgressService.reportProgress("Updating " + path);
				
				// if tipo es plano reviso el contenido del archivo
				int resourceType = cmsResource.getTypeId();
				if(resourceType == CmsResourceTypePlain.getStaticTypeId()) {
					if (stringContents.contains("<%")) {
						SetupProgressService.reportProgress("Se cambia el tipo de " + path);
						cmsResource.setType(CmsResourceTypeJsp.getStaticTypeId());
					}
				}
				
			} else {
				
				String typeByName = OpenCms.getResourceManager().getDefaultTypeForName(path).getTypeName();
				int typeID = OpenCms.getResourceManager().getDefaultTypeForName(path).getTypeId();
				
				if(typeByName.equals("image")){
					cmsResource = cmsObject.createResource(path, typeID);
				}else{
					// Verify if JS or any other scripting files contains JSP contents
					if (stringContents.contains("<%")) {
						cmsResource = cmsObject.createResource(path, CmsResourceTypeJsp.getStaticTypeId());
					} else {
						cmsResource = cmsObject.createResource(path, typeID);
					}
				}
				
				SetupProgressService.reportProgress("Creating " + path);
			}
			
			stealLock(cmsObject, path);
			CmsFile cmsFile = cmsObject.readFile(cmsResource);
			cmsFile.setContents(fileContents);
			cmsObject.writeFile(cmsFile);
			//cmsObject.unlockResource(path);
			
			publishList.add(cmsResource);
		}
	}
	
	protected static void processFolders(CmsObject cmsObject, String path, List<CmsResource> publishList) throws Exception {
		String[] paths = path.split("/");
		String resourcename = "";
		for (int i=0; i<paths.length-1; i++) {
			if (paths[i].equals("")) continue;
			resourcename += "/" + paths[i];
			if (!cmsObject.existsResource(resourcename)) {
				SetupProgressService.reportProgress("Creating " + resourcename);
				cmsObject.createResource(resourcename, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
				OpenCms.getPublishManager().publishResource(cmsObject, resourcename);
				OpenCms.getPublishManager().waitWhileRunning();
			}
		}
	}
	
	protected static void stealLock(CmsObject cmsObject, String path) throws CmsException {
		CmsLock lock = cmsObject.getLock(path);
		if (lock.getUserId().toString().equals(cmsObject.getRequestContext().currentUser().getId().toString())) {
			return;
		}
		if (!lock.isUnlocked()) {
			cmsObject.changeLock(path);
			cmsObject.unlockResource(path);
		}
		cmsObject.lockResource(path);
	}
	
	protected List<String> customizedFiles;
	
	@Override
	public String getPartialMessage() {
		return "VFS update finished";
	}

	@Override
	public String getStepName() {
		return "Actualizar VFS";
	}

}