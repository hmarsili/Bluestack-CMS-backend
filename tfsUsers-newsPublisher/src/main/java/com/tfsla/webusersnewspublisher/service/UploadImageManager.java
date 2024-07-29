package com.tfsla.webusersnewspublisher.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.DatatypeConverter;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeExternalImage;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.main.CmsException;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.db.CmsDefaultUsers;
import org.opencms.db.CmsPublishList;

import com.tfsla.diario.ediciones.services.ImageOrientationFixer;
import com.tfsla.diario.ediciones.services.ImagenService;
import com.tfsla.diario.ediciones.services.UploadService;
import com.tfsla.utils.TfsAdminUserProvider;
import com.tfsla.utils.CmsResourceUtils;

import org.apache.commons.fileupload.FileItem;
import org.opencms.report.CmsLogReport;
import org.opencms.util.CmsFileUtil;
import org.opencms.workplace.CmsWorkplaceAction;

public class UploadImageManager {
	
	private CmsObject cmsObject = null;
	private String folder = "";
	private String site = "";
	private String publication = "1";
	private String internalUser;
	private String year;
	private String month;
	private String day;
	private boolean yearPathExists;
	private boolean monthPathExists;
	private boolean dayPathExists;
	private boolean autoPublishImages;
	List<CmsResource> publishList = new ArrayList<CmsResource>();
	
	public UploadImageManager() throws CmsException {
		try {
			CmsObject _cmsObject =CmsWorkplaceAction.getInstance().getCmsAdminObject();
			cmsObject = OpenCms.initCmsObject(_cmsObject);
			CmsProject offProject = cmsObject.readProject("Offline");
			cmsObject.getRequestContext().setCurrentProject(offProject);
			autoPublishImages = true;
		} catch (CmsException e) {
			e.printStackTrace();
			throw e;
		}		
	}
	
	public String upload(String base64item, String itemName) throws Exception {
		String folderName = this.processFolders();
		if(base64item == null) return null;
		
		//En JS, FileReader.readAsDataURL() devuelve el siguiente formato:
		//data:image/jpeg;base64,<(esta es la parte base64 que nos interesa)>
		if(base64item.startsWith("data:")) {
			base64item = base64item.substring(base64item.indexOf(',') + 1);
		}
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
	
	@SuppressWarnings("rawtypes")
	public String uploadImage(String folderName, String fileName, byte[] buffer) throws Exception {
		fileName = cmsObject.getRequestContext().getFileTranslator().translateResource(fileName);
		if(fileName.equals("") || !fileName.toLowerCase().matches("\\S+\\.(jpg|png|gif|bmp|jpeg)$"))
			return null;
		
		fileName = getImageName(internalUser, fileName);
		String url = uploadRFSImage(fileName, buffer);
		
		if(!cmsObject.existsResource(folderName + fileName)) {
			String linkName = folderName + fileName;
			cmsObject.createResource(linkName, 
					CmsResourceTypeExternalImage.getStaticTypeId(),
					url.getBytes(),
					new ArrayList());
			
			if(autoPublishImages) {
				try {
					OpenCms.getPublishManager().publishResource(cmsObject, linkName);
				} catch(Exception e) {
					CmsLog.getLog(this).error("Error publicando imagen", e);
					e.printStackTrace();
				}
			} else {
				publishList.add(cmsObject.readResource(linkName));
			}
			
			/*
			cmsObject.createResource(folderName + fileName, type, buffer, new ArrayList());
			cmsObject.unlockResource(folderName + fileName);
			if(autoPublishImages) {
				try {
					OpenCms.getPublishManager().publishResource(cmsObject, folderName + fileName);
				} catch(Exception e) {
					CmsLog.getLog(this).error("Error publicando imagen", e);
					e.printStackTrace();
				}
			} else {
				publishList.add(cmsObject.readResource(folderName + fileName));
			}*/
		}
		
		return fileName;
	}
	
	public String uploadImage(String folderName, FileItem item) throws Exception {
        String fileName = null;
        
		if (!item.isFormField()) {
			byte[] buffer = null;
			InputStream stream = item.getInputStream();
			try {
				stream = ImageOrientationFixer.transformImage(stream);
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			buffer = CmsFileUtil.readFully(stream, false);
			return this.uploadImage(folderName, item.getName(), buffer);
        }
        return fileName;
	}	
	
	public void setSite(String site) {
		this.site = site;
	}
	
	public void setPublication(String publication) {
		this.publication = publication;
	}
	
	public void setAutoPublishImages(boolean autoPublishImages) {
		this.autoPublishImages = autoPublishImages;
	}
	
	public void publish() throws Exception {
		try {
			CmsPublishList publishList = OpenCms.getPublishManager().getPublishList(cmsObject, getPublishList(), false);
			if(publishList.size() > 0) {
				OpenCms.getPublishManager().publishProject(cmsObject,
					new CmsLogReport(Locale.getDefault(), this.getClass()),
					OpenCms.getPublishManager().getPublishList(cmsObject, getPublishList(), false)
				);
			}
		} catch(Exception ex) {
			CmsLog.getLog(this).error("Error publicando con publishList", ex);
			ex.printStackTrace();
			throw ex;
		}
	}
	
	public List<CmsResource> getPublishList() {
		return this.publishList;
	}
	
	public void setMode(boolean isPreview) {
		if (isPreview)
			folder = "/posts/tmp/img";
		else
			folder = "/img";
	}
	
	public void setInternalUser(String user) {
		this.internalUser = user;		
	}

	public synchronized static String getImageName(String internalUser, String fileName) {
		String usr = internalUser;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmssSSS");
		if(usr.contains("@")) {
			usr = usr.split("@")[0];
		}
		if(usr.contains("webUser/")) {
			usr = usr.split("/")[1];
		}
		fileName = usr.replace(".", "") + "_" + sdf.format(new Date()) + "_" + fileName;
		fileName = Normalizer.normalize(fileName, Normalizer.Form.NFC).replaceAll("[^\\p{ASCII}]", "x");
		for(char a : CHARS_TO_REPLACE.toCharArray()) {
			fileName = fileName.replace(a, 'x');
		}
		fileName = fileName.replaceAll("(x)\\1{1,}", "$1");
		fileName = fileName.replaceAll("\\s","_");
		return fileName;
	}
	
	protected String getFolderName() throws Exception {
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
			int timeout = 2000;
			if (!yearPathExists) {
				OpenCms.getPublishManager().publishResource(cmsObject, site + folder + "/" + year);
				OpenCms.getPublishManager().waitWhileRunning(timeout);
			}
			if (!monthPathExists) {
				OpenCms.getPublishManager().publishResource(cmsObject, site + folder + "/" + year + "/" + month);
				OpenCms.getPublishManager().waitWhileRunning(timeout);
			}
			if (!dayPathExists) {
				OpenCms.getPublishManager().publishResource(cmsObject, site + folder + "/" + year + "/" + month + "/" + day);
				OpenCms.getPublishManager().waitWhileRunning(timeout);
			}			
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	protected String processFolders() throws Exception {
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
			e.printStackTrace();
			throw e;
		}
		return folderName;
	}
	
	protected String uploadRFSImage(String fileName, byte[] buffer) throws Exception {
		UploadService svc = ImagenService.getInstance(cmsObject);
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		String rfsDirectory = config.getParam(site, publication, MODULE_NAME, "rfsDirectory","");
		String rfsSubFolderFormat = config.getParam(site, publication, MODULE_NAME, "rfsSubFolderFormat","");
		String rfsVirtualUrl = config.getParam(site, publication, MODULE_NAME, "rfsVirtualUrl","");
		Hashtable<String, String> params = new Hashtable<String, String>();
		params.put("section", "posts");
		
		String subFolderRFSPath = svc.getRFSSubFolderPath(rfsSubFolderFormat, params);
		File dir = new File(rfsDirectory + "/" + subFolderRFSPath);
		if (!dir.exists() && !dir.mkdirs()) {
			throw new Exception("UploadImageManager - Error al intentar crear el directorio " + dir.getAbsolutePath());
	    }

		String fullPath = dir.getAbsolutePath() + "/" + fileName;
		File uploadedFile = new File(fullPath);
		FileOutputStream fOut = new FileOutputStream(uploadedFile);
		fOut.write(buffer);
		fOut.close();
		
		return rfsVirtualUrl + subFolderRFSPath + fileName;
	}
	
	private static String MODULE_NAME = "imageUpload";
	private static String CHARS_TO_REPLACE = "|°¬@·~½¬{[]}\"#$%&/()=?¡¿'!+*~+~^`´:;,<>Ççªº€\\¨";
}