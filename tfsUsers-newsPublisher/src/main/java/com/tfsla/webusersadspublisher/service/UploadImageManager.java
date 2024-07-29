package com.tfsla.webusersadspublisher.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.DatatypeConverter;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.main.OpenCms;
import org.opencms.main.CmsException;
import org.opencms.db.CmsDefaultUsers;

import com.tfsla.utils.TfsAdminUserProvider;
import com.tfsla.utils.CmsResourceUtils;

import org.apache.commons.fileupload.FileItem;
import org.opencms.report.CmsLogReport;
import org.opencms.util.CmsFileUtil;
import org.opencms.workplace.CmsWorkplaceAction;

public class UploadImageManager {
	
	private CmsObject cmsObject = null;
	private String internalUser;
	private String year;
	private String month;
	private String day;
	private String folder = "";
	private String site = "";
	private boolean yearPathExists;
	private boolean monthPathExists;
	private boolean dayPathExists;
	List<CmsResource> publishList = new ArrayList<CmsResource>();
	
	public UploadImageManager() throws CmsException {
		try {
			CmsObject _cmsObject =CmsWorkplaceAction.getInstance().getCmsAdminObject();
			cmsObject = OpenCms.initCmsObject(_cmsObject);
			CmsProject offProject = cmsObject.readProject("Offline");
			cmsObject.getRequestContext().setCurrentProject(offProject);
		} catch (CmsException e) {
			throw e;
		}
	}
	
	public String upload(String base64item, String itemName) throws Exception {
		String folderName = this.processFolders();
		byte[] imageData = DatatypeConverter.parseBase64Binary(base64item);
		String fileName = uploadImage(folderName, itemName, imageData);
		if(fileName != null)
			return (folderName + fileName).replace(site, "");
		else
			return null;
	}
	
	public String upload(FileItem item) throws Exception {		
		String folderName = this.processFolders();
		String fileName = uploadImage(folderName, item);
		if(fileName != null)
			return (folderName + fileName).replace(site, "");
		else
			return null;
	}	
	
	private String processFolders() throws Exception {
		String folderName = getFolderName();
		publishFolder(folderName);
		
		try {
			if (!yearPathExists)
				publishList.add(cmsObject.readResource(site + folder + "/" + year));
			else if (!monthPathExists)
				publishList.add(cmsObject.readResource(site + folder + "/" + year + "/" + month));
			else if (!dayPathExists)
				publishList.add(cmsObject.readResource(site + folder + "/" + year + "/" + month + "/" + day));
		} catch(Exception e) {
			throw e;
		}
		
		return folderName;
	}
	
	private String getFolderName() throws Exception	{
		yearPathExists = true;
		monthPathExists = true;
		dayPathExists = true;
		
		Date date = new Date();
		year = new SimpleDateFormat("yyyy").format(date);
		month = new SimpleDateFormat("MM").format(date);
		day = new SimpleDateFormat("dd").format(date);

		String folderName =  site + folder + "/" + year;
		int imageGalleryType = 8;

		if (!cmsObject.existsResource(folderName)) {
			cmsObject.createResource(folderName, imageGalleryType);
			CmsResourceUtils.unlockResource(cmsObject, folderName, false);
			yearPathExists = false;
		}
		
		folderName = folderName + "/" + month;

		if (!cmsObject.existsResource(folderName)) {
			cmsObject.createResource(folderName, imageGalleryType);
			CmsResourceUtils.unlockResource(cmsObject, folderName, false);
			monthPathExists = false;
		}
		
		folderName = folderName + "/" + day;

		if (!cmsObject.existsResource(folderName)) {
			cmsObject.createResource(folderName, imageGalleryType);
			CmsResourceUtils.unlockResource(cmsObject, folderName, false);
			dayPathExists = false;
		}
		
		return folderName + "/";
	}
	
	protected void publishFolder(String folderName) throws Exception {
		try {
			if (!yearPathExists)
				OpenCms.getPublishManager().publishResource(cmsObject, site + folder + "/" + year);
			else if (!monthPathExists)
				OpenCms.getPublishManager().publishResource(cmsObject, site + folder + "/" + year + "/" + month);
			else if (!dayPathExists)
				OpenCms.getPublishManager().publishResource(cmsObject, site + folder + "/" + year + "/" + month + "/" + day);
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public String uploadImage(String folderName, String fileName, byte[] buffer) throws Exception {
		fileName = cmsObject.getRequestContext().getFileTranslator().translateResource(fileName);
		if(fileName.equals("") || !fileName.toLowerCase().matches("\\S+\\.(jpg|png|gif|bmp|jpeg)$"))
			return null;
		
		fileName = internalUser.replace("@", "").replace(".","").replace("webUser/", "") + "_" + fileName;
		if(!cmsObject.existsResource(folderName + fileName)) {
			int type = OpenCms.getResourceManager().getDefaultTypeForName(fileName).getTypeId();
			cmsObject.createResource(folderName + fileName, type, buffer, new ArrayList());
			cmsObject.unlockResource(folderName + fileName);
			try {
				if(dayPathExists) {
					publishList.add(cmsObject.readResource(folderName + fileName));
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
		}
		
		return fileName;
	}
	
	public String uploadImage(String folderName, FileItem item) throws Exception {
        String fileName = null;
        
		if (!item.isFormField()) {
			byte[] buffer = null;
			if (item.getSize() == -1)
				buffer = CmsFileUtil.readFully(item.getInputStream(), false);
			else
				buffer = CmsFileUtil.readFully(item.getInputStream(), (int)item.getSize(), false);
			return this.uploadImage(folderName, item.getName(), buffer);
        }
        return fileName;		
	}
	
	public void publish() throws Exception {
		try {
			OpenCms.getPublishManager().publishProject(cmsObject, 
					new CmsLogReport(Locale.getDefault(), this.getClass()),
						OpenCms.getPublishManager().getPublishList(cmsObject, getPublishList(), false));
			/*
			HttpRequest request = new HttpRequest();
			request.setUrl(OpenCms.getSiteManager().getWorkplaceServer() + "/purgecache.html");
			request.sendRequest();
			*/
		} catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}
		
	public List<CmsResource> getPublishList() {
		return this.publishList;
	}
	
	public void setMode(boolean isPreview) {
		folder = isPreview ? "/avisos/tmp/img" : "/avisos/img";
	}
	
	public void setInternalUser(String user) {
		this.internalUser = user;		
	}
	
	public void setSite(String site) {
		this.site = site;
	}
}