package com.tfsla.diario.webservices;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.swing.ImageIcon;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;

import com.tfsla.diario.ediciones.services.ImageMetadataPropertiesService;
import com.tfsla.diario.ediciones.services.ImagenService;
import com.tfsla.diario.webservices.common.interfaces.IImagesAddWebService;
import com.tfsla.diario.webservices.core.services.FilesAddWebService;

public class ImagesAddWebService extends FilesAddWebService implements IImagesAddWebService {

	private static final Log LOG = CmsLog.getLog(ImagesAddWebService.class);

	public ImagesAddWebService(HttpServletRequest request) throws Throwable {
		super(request);
	}

	@Override
	protected List<String> getFileExtensions() {
		return this.configuration.getImageExtensions();
	}

	@Override
	protected String getRFSVirtualUrl() {
		return this.config.getParam(sitename, publication, "imageUpload", "rfsVirtualUrl","");
	}
	
	@Override
	protected String getSubFolderPath() {
		ImagenService service = ImagenService.getInstance(cms);
		String rfsDirectoryFormat = config.getParam(sitename, publication, "imageUpload", "rfsSubFolderFormat","");
		String subFolderRFSPath = "";
		try {
			HashMap<String, String> parameters = new HashMap<String, String>();
			parameters.put("section", this.section);
			subFolderRFSPath = service.getRFSSubFolderPath(rfsDirectoryFormat, parameters);
			LOG.debug("va a buscar el vfsPath - siteName: " + sitename + " - publication: " + publication + " - subFolderRFSPath: " + subFolderRFSPath);
		} catch (Exception e) {
			LOG.error("Error al obtener subfolderPath: " + subFolderRFSPath);
			e.printStackTrace();
		}
		return subFolderRFSPath;
	}
	
	@Override
	protected String getRFSDirectory() {
		String subFolderRFSPath = this.getSubFolderPath();
		String rfsDirectory = config.getParam(sitename, publication, "imageUpload", "rfsDirectory","");
		String rfsPath = rfsDirectory + "/" + subFolderRFSPath;
		return rfsPath;
	}
	
	protected String getVFSDirectory() {
		ImagenService service = ImagenService.getInstance(cms);
		String vfsSubFolderFormat = config.getParam(sitename, publication, "imageUpload", "vfsSubFolderFormat","");
		
		String vfsPath = config.getParam(sitename, publication, "imageUpload", "vfsPath","");
		try {
			HashMap<String, String> parameters = new HashMap<String, String>();
			parameters.put("section", this.section);
			
			LOG.debug("va a buscar el vfsPath - siteName: " + sitename + " - publication: " + publication + " - vfsPath: " + vfsPath);
			vfsPath += "/" + service.getVFSSubFolderPath(vfsPath, service.getFolderType(), vfsSubFolderFormat, parameters);
			LOG.debug("- vfsPath: " + vfsPath);
			
		} catch (Exception e) {
			LOG.error("Error al obtener subfolderPath: " + vfsPath);
			e.printStackTrace();
		}
		return vfsPath;
		
	}
	
	protected String getVFSDirectory(HashMap<String, String> parameters) {
		ImagenService service = ImagenService.getInstance(cms);
		String vfsSubFolderFormat = config.getParam(sitename, publication, "imageUpload", "vfsSubFolderFormat","");
		
		String vfsPath = config.getParam(sitename, publication, "imageUpload", "vfsPath","");
		try {
			
			LOG.debug("va a buscar el vfsPath - siteName: " + sitename + " - publication: " + publication + " - vfsPath: " + vfsPath);
			vfsPath += "/" + service.getVFSSubFolderPath(vfsPath, service.getFolderType(), vfsSubFolderFormat, parameters);
			LOG.debug("- vfsPath: " + vfsPath);
			
		} catch (Exception e) {
			LOG.error("Error al obtener subfolderPath: " + vfsPath);
			e.printStackTrace();
		}
		return vfsPath;
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void setProperties(List properties, String fullPath) {
		try {
			ImageMetadataPropertiesService service = new ImageMetadataPropertiesService();
			service.setMetaDataProperties(fullPath, properties);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ImageIcon icono = new ImageIcon(fullPath);
		java.awt.Image imagen = icono.getImage();
		CmsProperty p = new CmsProperty(CmsPropertyDefinition.PROPERTY_IMAGE_SIZE, null,"w:"+imagen.getWidth(null)+",h:"+imagen.getHeight(null));
		properties.add(p);
	}

	public String uploadFile(CmsObject cms, InputStream fileStream,
			String filename, Date folderdate) throws Exception {
		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("section", this.section);
		if (folderdate!=null)
		{
			SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy"); 
			parameters.put("date",sdf.format(folderdate));
		}
		String path = this.getVFSDirectory(parameters);
		String fileNameVFS = "";
		try {
			if( ImagenService.getInstance(cms).getDefaultUploadDestination().equals("vfs")) {
				ImagenService.getInstance(cms).uploadVFSFile(path,cms.getRequestContext().getFileTranslator().translateResource(filename.toLowerCase()),fileStream);
			} else if( ImagenService.getInstance(cms).getDefaultUploadDestination().equals("ftp"))
				fileNameVFS = ImagenService.getInstance(cms).uploadFTPFile(path,filename,parameters,fileStream); 
			else if( ImagenService.getInstance(cms).getDefaultUploadDestination().equals("server"))
				fileNameVFS = ImagenService.getInstance(cms).uploadRFSFile(path,filename,parameters,fileStream);
			else if( ImagenService.getInstance(cms).getDefaultUploadDestination().equals("amz"))
				fileNameVFS = ImagenService.getInstance(cms).uploadAmzFile(cms, path,filename,parameters,fileStream);
		} catch (CmsException e) {
			LOG.error("Error en crecion de imagen - webservice: " + e.getMessage());
			throw e;
		} catch (IOException e) {
			LOG.error("Error en crecion de imagen - webservice: " + e.getMessage());
			throw e;
		} catch (Exception e) {
			LOG.error("Error en crecion de imagen - webservice: " + e.getMessage());
			throw e;
		}
		return fileNameVFS;

	}

	
	@Override
	public String uploadFile(CmsObject cms, InputStream fileStream,
			String filename) throws Exception {
		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("section", this.section);
		String path = this.getVFSDirectory();
		String fileNameVFS = "";
		try {
			if( ImagenService.getInstance(cms).getDefaultUploadDestination().equals("vfs")) {
				ImagenService.getInstance(cms).uploadVFSFile(path,cms.getRequestContext().getFileTranslator().translateResource(filename.toLowerCase()),fileStream);
			} else if( ImagenService.getInstance(cms).getDefaultUploadDestination().equals("ftp"))
				fileNameVFS = ImagenService.getInstance(cms).uploadFTPFile(path,filename,parameters,fileStream); 
			else if( ImagenService.getInstance(cms).getDefaultUploadDestination().equals("server"))
				fileNameVFS = ImagenService.getInstance(cms).uploadRFSFile(path,filename,parameters,fileStream);
			else if( ImagenService.getInstance(cms).getDefaultUploadDestination().equals("amz"))
				fileNameVFS = ImagenService.getInstance(cms).uploadAmzFile(cms, path,filename,parameters,fileStream);
		} catch (CmsException e) {
			LOG.error("Error en crecion de imagen - webservice: " + e.getMessage());
			throw e;
		} catch (IOException e) {
			LOG.error("Error en crecion de imagen - webservice: " + e.getMessage());
			throw e;
		} catch (Exception e) {
			LOG.error("Error en crecion de imagen - webservice: " + e.getMessage());
			throw e;
		}
		return fileNameVFS;

	}

	}
