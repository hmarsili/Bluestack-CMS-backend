package com.tfsla.diario.webservices.core.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.types.CmsResourceTypeExternalImage;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import com.tfsla.diario.webservices.common.strings.ExceptionMessages;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.core.SessionManager;
import com.tfsla.utils.CmsObjectUtils;
import com.tfsla.utils.CmsResourceUtils;

/**
 * A service to add files as linked resources to the VFS
 */
public abstract class FilesAddWebService extends OfflineProjectService {

		
	public FilesAddWebService(HttpServletRequest request) throws Throwable {
		super(request);
	}

	/**
	 * Processes a list of files to be uploaded then adds them to the RFS and
	 * creates the links into the VFS
	 */
	@SuppressWarnings("rawtypes")
	@Override
	protected JSON doExecute() throws Throwable {
		
		Object publish = this.getPostRequestParam("publish");
		if(publish != null && publish.toString().trim().equals("false"))
			this.publish = false;
		else
			this.publish = true;
		
		JSONArray jsonResponse = new JSONArray();
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		try {
			this.switchToOfflineSession();
			int itemsCount = this.processItemsRequest(params, encoding);
		
			LOG.debug("AddWebServices - Items a agregar: " + itemsCount);
			
			for(int i=0; i<itemsCount; i++) {
				JSONObject jsonItem = new JSONObject();
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_hhmmssSSS_");
					String fileIndex = String.format("file[%s]", i);
					String filename = sdf.format(new Date()) + params.get(fileIndex + ".name").toString().replaceAll("\\s","_");
					
					File dir = new File(this.getRFSDirectory());
					LOG.debug ("AddWebServices - filename: " + filename + " dir: " + dir);
					//Will throw an exception if the extension is not supported
					this.processFileName(filename);
					if (!dir.exists() && !dir.mkdirs()) {
						throw new Exception(ExceptionMessages.ERROR_UPLOADING_FILE);
					}
										
					List properties = new ArrayList(0);
					
					InputStream fileStream = (InputStream)params.get(fileIndex);
					String  fileNameVFS = "";
					
					if (cms.getRequestContext().currentUser().isWebuser()) {
						String site = cms.getRequestContext().getSiteRoot();
						fileNameVFS = this.uploadFile(SessionManager.getAdminCmsObject(site), fileStream, filename);
						this.setCommonProperties(properties, params, fileIndex);
						
						saveProperties(properties,fileNameVFS, SessionManager.getAdminCmsObject(site));
						
						if(fileNameVFS!= null && !fileNameVFS.equals("") && this.publish)
							OpenCms.getPublishManager().publishResource(SessionManager.getAdminCmsObject(site), fileNameVFS);
						
					} else {
							// Clono el cmsObject y me aseguro que este en el offline.
							// Un problema encontrado es que si este webservice es llamado en corto tiempo por algun motivo el cmsobject pasa al online.
							CmsObject cmsClone = CmsObjectUtils.getClone(cms);
							SessionManager.switchToProject(cmsClone, SessionManager.PROJECT_OFFLINE);
							fileNameVFS = this.uploadFile(cmsClone, fileStream, filename);
							this.setCommonProperties(properties, params, fileIndex);
							
							saveProperties(properties,fileNameVFS, cmsClone);
							
							if(fileNameVFS!= null && !fileNameVFS.equals("") && this.publish)
								OpenCms.getPublishManager().publishResource(cmsClone, fileNameVFS);
					}
					
					LOG.debug ("AddWebServices - Publica el recurso");
				
					jsonItem.put(StringConstants.STATUS, StringConstants.OK);
					jsonItem.put(StringConstants.NAME, fileNameVFS);
				} catch(Exception ex) {
					jsonItem.put(StringConstants.STATUS, StringConstants.ERROR);
					jsonItem.put(StringConstants.ERROR, ex.getMessage());
					this.LOG.error(String.format(ExceptionMessages.ERROR_UPLOADING_AT_INDEX, i), ex);
				} finally {
					jsonItem.put("index", i);
					jsonResponse.add(jsonItem);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			this.LOG.error(ExceptionMessages.ERROR_UPLOADING_GENERIC, e);
		} finally {
			this.restoreSession();
		}
		return jsonResponse;
	}
	
	protected void processFileName(String filename) throws Exception {
		this.assertFileExtension(filename);
		
		//Do some other jobs at inherited classes
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void setCommonProperties(List properties, Hashtable<String, Object> params, String fileIndex) {
		for(String propName : this.getCommonPropertyNames()) {
			String paramName = String.format("%s.%s", fileIndex, propName.toLowerCase());
			if(params.containsKey(paramName)) {
				if (!params.get(paramName).toString().equals("")) {
					CmsProperty prop = new CmsProperty();
					prop.setName(propName);
					prop.setAutoCreatePropertyDefinition(true);
					prop.setStructureValue(params.get(paramName).toString());
					properties.add(prop);
				}
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void saveProperties (List properties, String fileName, CmsObject cms) {
		try {
			cms.lockResource(fileName);
		} catch (CmsException e1) {
			LOG.error("No puede lockear el archivo para guardar properties",e1);
		}	
		for (Object object : properties) {
				CmsProperty prop = (CmsProperty)object;
				if (prop.getName().equals("keywords")) {
					try {
						CmsProperty propTags = cms.readPropertyObject(fileName, "keywords", false);
						if (propTags!=null && !propTags.getStructureValue().equals("") ) {
							prop.setStructureValue(propTags.getStructureValue() + "," + prop.getStructureValue());
						}
					} catch (CmsException e) {
						LOG.error ("error al leer propiedad de elemento subido " + fileName, e);
					}
				} 
				try {
		
					cms.writePropertyObject(fileName, prop);
				} catch (CmsException e) {
					LOG.error ("error al guardar propiedades del elemento subido: " +fileName,e);
				}
			}
			CmsResourceUtils.unlockResource(cms,fileName, false);

		
	}

	private void assertFileExtension(String filename) throws Exception {
		if(!filename.contains(".")) {
			throw new Exception(ExceptionMessages.UNRECOGNIZED_FILE_EXTENSION);
		}
		if(this.getFileExtensions() != null && this.getFileExtensions().size() > 0) {
			for(String extension : this.getFileExtensions()) {
				if(filename.toLowerCase().endsWith(extension)) return;
			}
			String[] split = filename.split("\\.");
			String extension = split[split.length-1];
			throw new Exception(String.format(ExceptionMessages.FILE_EXTENSION_NOT_ALLOWED_FORMAT, extension));
		}
	}
	
	@SuppressWarnings("rawtypes")
	protected int processItemsRequest(Hashtable<String, Object> params, String encoding) throws Exception {
		List items = this.getRequestAsList();
		int itemsCount = 0;
		
		if (items != null) {
			Iterator itr = items.iterator();
			while (itr.hasNext()) {
				FileItem item = (FileItem) itr.next();
				if (item.isFormField()) {
					params.put(item.getFieldName(), new String(item.getString().getBytes(StringConstants.ENCODING_ISO), encoding));
					this.mapFileItem(item);
				} else {
					params.put(item.getFieldName(), item.getInputStream());
					itemsCount++;
				}
			}
		}
		
		return itemsCount;
	}

	@SuppressWarnings("rawtypes")
	protected Object getPostRequestParam(String paramName) throws FileUploadException, IOException {
		List items = this.getRequestAsList();
		if (items != null) {
			Iterator itr = items.iterator();
			while (itr.hasNext()) {
				FileItem item = (FileItem) itr.next();
				if(item.getFieldName().equals(paramName)) {
					if (item.isFormField()) {
						return item.getString();
					} else {
						return item.getInputStream();
					}
				}
			}
		}
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	private List getRequestAsList() throws FileUploadException {
		if(this.requestAsList == null) {
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setHeaderEncoding(StringConstants.ENCODING_UTF8);
			
			requestAsList = upload.parseRequest(request);
		}
		return requestAsList;
	}
	
	protected void mapFileItem(FileItem item) {
		if(item.getFieldName().toLowerCase().equals(StringConstants.PUBLICATION)) {
			this.publication = item.getString();
		}
		if(item.getFieldName().toLowerCase().equals(StringConstants.SITE)) {
			this.sitename = item.getString();
		}
		if(item.getFieldName().toLowerCase().equals(StringConstants.SECTION)) {
			this.section = item.getString();
		}
	}

	@SuppressWarnings("rawtypes")
	protected abstract void setProperties(List properties, String fullPath);
	
	protected abstract List<String> getFileExtensions();
	
	protected abstract String getRFSVirtualUrl();
	
	protected abstract String getRFSDirectory();
	
	protected abstract String getVFSDirectory();
	
	protected abstract String getSubFolderPath();
	
	protected abstract String uploadFile(CmsObject cms,InputStream fileStream, String filename) throws Exception;
	
	protected int getPointerType() throws CmsLoaderException {
		return CmsResourceTypeExternalImage.getStaticTypeId();
	}
	
	protected List<String> getCommonPropertyNames() {
		List<String> ret = new ArrayList<String>();
		ret.add("Agency");
		ret.add("Author");
		ret.add("Keywords");
		ret.add("Title");
		ret.add("Description");
		ret.add("prevImage");
		
		return ret;
	}
	
	@SuppressWarnings("rawtypes")
	protected List requestAsList;
	protected String section;
	protected String sitename;
	protected String publication;
	
	protected boolean publish;
	
}
