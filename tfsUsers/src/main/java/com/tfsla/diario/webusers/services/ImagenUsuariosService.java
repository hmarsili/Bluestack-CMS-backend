package com.tfsla.diario.webusers.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.db.CmsDbSqlException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsFileUtil;
import org.opencms.workplace.CmsWorkplaceAction;

import com.tfsla.diario.ediciones.services.ImagenService;
import com.tfsla.diario.ediciones.services.PublicationService;
import com.tfsla.utils.TfsAdminUserProvider;

public class ImagenUsuariosService extends ImagenService {

	private static final Log LOG = CmsLog.getLog(ImagenService.class);
	private static CmsObject adminCmsObject = null;
	private static Map<String, ImagenUsuariosService> instances = new HashMap<String, ImagenUsuariosService>();
	
	public ImagenUsuariosService(CmsObject cmsObject, String siteName,
			String publication) {
		super(cmsObject, siteName, publication);
	}
	
	public static ImagenUsuariosService getInstance(CmsObject cms) {
		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	String publication = "0";
    	try {
			publication = String.valueOf(PublicationService.getPublicationId(cms));
		} catch (Exception e) {
			LOG.error(e);
		}

    	String id = siteName + "||" + publication;
    	ImagenUsuariosService instance = instances.get(id);
    	
    	if (instance == null) {
	    	instance = new ImagenUsuariosService(cms, siteName, publication);
	    	instances.put(id, instance);
    	}
    	
    	instance.cmsObject = cms;
    	
    	try {
			instance.cmsObject = getAdminCmsObject(cms);
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	
        return instance;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public CmsResource uploadVFSFile(String path, String fileName, InputStream content, List properties) throws CmsException, IOException {
		byte[] buffer = CmsFileUtil.readFully(content, false);
		String prefixedPath = getPrefixedPath(path, fileName, adminCmsObject);
		
		try {
			if(cmsObject.existsResource(prefixedPath + fileName)) {
				LOG.debug("Eliminando del VFS el archivo " + prefixedPath + fileName);
				
				com.tfsla.utils.CmsResourceUtils.forceLockResource(cmsObject, prefixedPath + fileName);
				cmsObject.deleteResource(prefixedPath + fileName, CmsResource.DELETE_REMOVE_SIBLINGS);
			}
			int type = getVFSResourceType(fileName);
			CmsResource res = cmsObject.createResource(prefixedPath + fileName, type, buffer, properties);
			return res;
		} catch (CmsSecurityException e) {
			// in case of not enough permissions, try to create a plain text file	
			CmsResource res = cmsObject.createResource(prefixedPath + fileName, CmsResourceTypePlain.getStaticTypeId(), buffer, properties);
			return res;
		} catch (CmsDbSqlException sqlExc) {
			// SQL error, probably the file is too large for the database settings, delete file
			cmsObject.lockResource(prefixedPath + fileName);
			cmsObject.deleteResource(prefixedPath + fileName, CmsResource.DELETE_PRESERVE_SIBLINGS);
		    throw  sqlExc;   		        
		}
	}
	
	@Override
	protected void deleteResource(String link) throws Exception {
		try {
			if(cmsObject.existsResource(link)) {
				LOG.debug("Eliminando del VFS link a imagen de usuario " + link);
    			com.tfsla.utils.CmsResourceUtils.forceLockResource(cmsObject,link);
				cmsObject.deleteResource(link, CmsResource.DELETE_REMOVE_SIBLINGS);
			}
		} catch(Exception e) {
			LOG.error(e);
			throw e;
		}
	}
	
	@Override
	public String getDefaultVFSUploadFolder(Map<String,String> parameters) throws Exception {
		return vfsPath;
	}
	
	@Override
	public String getRFSSubFolderPath(String sff, Map<String,String> parameters) throws Exception {
		return "";
	}
	
	@Override
	public void loadBaseProperties(String siteName, String publication) {
		String module = getModuleName();
 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
 		
 		vfsPath = config.getParam(siteName, publication, module, "vfsPath","");
 		
 		amzAccessID = config.getParam(siteName, publication, module, "amzAccessID", ""); 
		amzAccessKey = config.getParam(siteName, publication, module, "amzAccessKey","");
		amzBucket = config.getParam(siteName, publication, module, "amzBucket","");
		amzDirectory = config.getParam(siteName, publication, module, "amzDirectory","");
		amzRegion = config.getParam(siteName, publication, module, "amzRegion","");
 		
		rfsDirectory = config.getParam(siteName, publication, module, "rfsDirectory","");
		rfsVirtualUrl = config.getParam(siteName, publication, module, "rfsVirtualUrl","");

		ftpServer = config.getParam(siteName, publication, module, "ftpServer","");
		ftpUser = config.getParam(siteName, publication, module, "ftpUser","");
		ftpPassword  = config.getParam(siteName, publication, module, "ftpPassword","");
		ftpDirectory = config.getParam(siteName, publication, module, "ftpDirectory","");
		ftpVirtualUrl = config.getParam(siteName, publication, module, "ftpVirtualUrl","");
		
 		maxUploadSize = config.getIntegerParam(siteName, publication, module, "maxUploadSize",5);
		allowedFileTypes = config.getParam(siteName, publication, module, "allowedFileTypes","*");
		
		ftpUploadEnabled = config.getBooleanParam(siteName, publication, module, "ftpUploadEnabled",false); 
		rfsUploadEnabled = config.getBooleanParam(siteName, publication, module, "rfsUploadEnabled",false);
		vfsUploadEnabled = config.getBooleanParam(siteName, publication, module, "vfsUploadEnabled",false);
		amzUploadEnabled = config.getBooleanParam(siteName, publication, module, "amzUploadEnabled",false);
		
		defaultUploadDestination = config.getParam(siteName, publication, module, "defaultUploadDestination","server");
	}
	
	@Override
	protected String getModuleName() {
		return "userImageUpload";
	}
	
	@Override
	protected String processPath(String path, String fileName) {
		return getPrefixedPath(path, fileName, adminCmsObject) + fileName;
	}
	
	protected static synchronized CmsObject getAdminCmsObject(CmsObject cms) throws CmsException {
		if(adminCmsObject == null) {
			CmsObject _cmsObject =CmsWorkplaceAction.getInstance().getCmsAdminObject();
			adminCmsObject = OpenCms.initCmsObject(_cmsObject);
			adminCmsObject.getRequestContext().setSiteRoot("/");
			adminCmsObject.getRequestContext().setCurrentProject(cms.readProject("Offline"));
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
				LOG.info("Creating user picture prefixed path - " + retPath);
				adminCms.createResource(retPath, getFolderType());
				OpenCms.getPublishManager().publishResource(adminCms, retPath);
				OpenCms.getPublishManager().waitWhileRunning(5000);
			}
		} catch(Exception e) {
			LOG.error("Error when creating user picture prefixed path - " + retPath + " :: " + e.getMessage(), e);
			e.printStackTrace();
		}
		
		return retPath;
	}
}
