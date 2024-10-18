package com.tfsla.diario.ediciones.services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.SocketException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import javax.swing.ImageIcon;

import org.apache.commons.logging.Log;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.db.CmsDbSqlException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeExternalImage;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.main.TfsContext;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsFileUtil;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.utils.CmsResourceUtils;

public class ImagenService extends UploadService {

	private static final Log LOG = CmsLog.getLog(ImagenService.class);
	private static Map<String, ImagenService> instances = new HashMap<String, ImagenService>();

	private String cropPredefinedSizes = "";
	private String aspectRatios = "";
	private String defaultAspectRatio = "";
	private String predefinedSizes = "";
	private String defaultPredefinedSize = "";
	private String siteName = "";
	private String publication = "";
	private int maxUploadWidth = 0;
	private int maxUploadHeight = 0;

	public static ImagenService getInstance(CmsObject cms, String siteName, String publication) {
    	
    	String id = siteName + "||" + publication;
    	
    	ImagenService instance = instances.get(id);
    	
    	if (instance == null) {
	    	instance = new ImagenService(cms,siteName, publication);

	    	instances.put(id, instance);
    	}
    	
    	instance.cmsObject = cms;
    	instance.siteName = siteName;
    	instance.publication = publication;
    	
        return instance;
    }
	
	public static ImagenService getInstance(CmsObject cms) {
    	String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	String publication = "0";
    	
    	if (TfsContext.getInstance()!=null && TfsContext.getInstance().getRequest()!=null && TfsContext.getInstance().getRequest().getSession()!=null) {
	    	HttpSession m_session = TfsContext.getInstance().getRequest().getSession();
	    	TipoEdicion currentPublication = (TipoEdicion) m_session.getAttribute("currentPublication");
	    	
	    	if (currentPublication!=null)
	    		publication = String.valueOf(currentPublication.getId()); 
    	}
    	
    	if(publication.equals("0")){
			try {
				publication = String.valueOf(PublicationService.getPublicationId(cms));
			} catch (Exception e) {
				LOG.error(e);
			}
    	}

    	return getInstance( cms, siteName, publication);
    	
    }
	
	public ImagenService(CmsObject cmsObject, String siteName, String publication) {
		this.loadProperties(siteName,publication);
	}

	public void loadProperties(String siteName, String publication) {
	   	String module = getModuleName();
	 	CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();

	 	this.siteName = siteName;
		this.publication = publication;
	 	
		cropPredefinedSizes = config.getParam(siteName, publication, module, "cropPredefinedSizes","");
		aspectRatios = config.getParam(siteName, publication, module, "aspectRatios","");
		defaultAspectRatio = config.getParam(siteName, publication, module, "defaultAspectRatio","");
		predefinedSizes = config.getParam(siteName, publication, module, "predefinedSizes","");
		defaultPredefinedSize = config.getParam(siteName, publication, module, "defaultPredefinedSize","");
		
		String maxUploadWidthStr = config.getParam(siteName, publication, module, "maxUploadWidth", "");
		String maxUploadHeightStr = config.getParam(siteName, publication, module, "maxUploadHeigh", "");
		
		if(maxUploadWidthStr.equals(""))
			maxUploadWidth = 0;
		else 
			maxUploadWidth = Integer.parseInt(maxUploadWidthStr); 
		
		if(maxUploadHeightStr.equals(""))
			maxUploadHeight = 0;
		else 
			maxUploadHeight = Integer.parseInt(maxUploadHeightStr);
			
		
		loadBaseProperties(siteName, publication);
	}
			
	@SuppressWarnings("deprecation")
	public InputStream cropImage(CmsObject cms, CmsFile currentImage, int left, int top, int width, int height) throws IOException {
		String formatName = CmsFileUtil.getFileExtension(currentImage.getName());
		byte[] content = currentImage.getContents();
		int externalImageTypeId = CmsResourceTypePointer.getStaticTypeId();
		
		try {
			 externalImageTypeId = OpenCms.getResourceManager().getResourceType("external-image").getTypeId();
		} catch (CmsLoaderException e) {
			e.printStackTrace();
		}
		
		if (externalImageTypeId == currentImage.getTypeId())
			content = getPointerContent(new String(content));

		InputStream input = new ByteArrayInputStream(content);
		BufferedImage outImageBuff=ImageIO.read(input);
		BufferedImage cropped=outImageBuff.getSubimage(left, top, width, height);
		ByteArrayOutputStream outImage=new ByteArrayOutputStream();
		ImageIO.write(cropped, formatName.replace(".","") , outImage);

		return new ByteArrayInputStream(outImage.toByteArray());
	}
	
	@Override
	public String uploadFTPFile(String path, String fileName, Map<String,String> parameters, InputStream content) throws Exception {
		List properties = new ArrayList(0);
		return uploadFTPFile(path, fileName, parameters, content,properties);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String uploadFTPFile(String path, String fileName, Map<String,String> parameters, InputStream content, List properties) throws Exception {
		String ftpSubfolder = "";
		fileName = getValidFileName(fileName);
		FTPClient client = new FTPClient();
		client.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

		try {
			client.connect(ftpServer);
			client.login(ftpUser, ftpPassword);
			client.changeWorkingDirectory(ftpDirectory);

			ftpSubfolder = getFTPSubFolderPath(client,parameters);
			
			client.enterLocalPassiveMode();
			client.setFileType(FTPClient.BINARY_FILE_TYPE);

			// Upload a file
			OutputStream os = client.storeFileStream(fileName);
			try {
				content = ImageOrientationFixer.transformImage(content);
			} catch(Exception e) {
				e.printStackTrace();
			}
			byte[] buffer = CmsFileUtil.readFully(content, false);

			os.write(buffer);
			os.close();

			client.completePendingCommand();

			//System.out.println(client.printWorkingDirectory());

			client.logout();
			client.disconnect();

			String tmpFile = tmpFileFFmepg(fileName, buffer);
			
			ImageMetadataPropertiesService service = new ImageMetadataPropertiesService(siteName, publication);
			service.setMetaDataProperties(tmpFile, properties);
		} catch (SocketException e1) {
			LOG.error("Error al intentar subir el archivo a " + ftpServer,e1);
			throw e1;
		} catch (IOException e1) {
			LOG.error("Error al intentar subir el archivo a " + ftpServer,e1);
			throw e1;
		}

		String audioMultimediaPath = ftpVirtualUrl + ftpSubfolder + fileName;
		
		ImageIcon icono = new ImageIcon(audioMultimediaPath);
		java.awt.Image imagen = icono.getImage();

		CmsProperty p = new CmsProperty(CmsPropertyDefinition.PROPERTY_IMAGE_SIZE, null,"w:"+imagen.getWidth(null)+",h:"+imagen.getHeight(null));
		properties.add(p);

		LOG.debug("upload - uploadFTPFile imagen.getWidth(null): " + imagen.getWidth(null));
		LOG.debug("imagen.getHeight(null): " + imagen.getHeight(null));
		
		if (!String.valueOf(imagen.getWidth(null)).equals(null) && !String.valueOf(imagen.getHeight(null)).equals(null)) {
			int fpx = imagen.getWidth(null)/2;
			int fpy = imagen.getHeight(null)/2;
			String focalPoint = "fpx:"+fpx+",fpy:"+fpy; 
			p = new CmsProperty(CmsPropertyDefinition.PROPERTY_FOCALPOINT, null,focalPoint);
			properties.add(p);
			LOG.debug("focalPoint: " + focalPoint);
		}
		
		try {
			String linkName = path + fileName;
			
			cmsObject.createResource(linkName, 
					getPointerType(),
					audioMultimediaPath.getBytes(),
					properties);
			
			CmsResourceUtils.unlockResource(cmsObject,linkName, false);
			
			return linkName;
		} catch (CmsIllegalArgumentException e) {
			LOG.error("Error al intentar subir el archivo a " + ftpServer,e);
			throw e;
		} catch (CmsLoaderException e) {
			LOG.error("Error al intentar subir el archivo a " + ftpServer,e);
			throw e;
		} catch (CmsException e) {
			LOG.error("Error al intentar subir el archivo a " + ftpServer,e);
			throw e;
		}
	}
	
	@Override
	public CmsResource uploadVFSFile(String path, String fileName, InputStream content) throws CmsException, IOException {
		List properties = new ArrayList(0);
		return uploadVFSFile(path,fileName,content,properties);
	}
	
	@Override
	public CmsResource uploadVFSFile(String path, String fileName, InputStream content, List properties) throws CmsException, IOException {
		byte[] buffer = CmsFileUtil.readFully(content, false);
		fileName = checkFileName(path,fileName);
		
		try {
			String tmpFile = tmpFileFFmepg(fileName, buffer);
			ImageMetadataPropertiesService service = new ImageMetadataPropertiesService();
			service.setMetaDataProperties(tmpFile, properties);
			
			int type = getVFSResourceType(fileName);
			
			CmsResource res = cmsObject.createResource(path + fileName, type, buffer, properties);
			cmsObject.unlockResource(path + fileName);
			
			return res;
		} catch (CmsSecurityException e) {
			// in case of not enough permissions, try to create a plain text file	
			CmsResource res = cmsObject.createResource(path + fileName, CmsResourceTypePlain.getStaticTypeId(), buffer, properties);
			cmsObject.unlockResource(path + fileName);
			return res;
		} catch (CmsDbSqlException sqlExc) {
			// SQL error, probably the file is too large for the database settings, delete file
			cmsObject.lockResource(path + fileName);
			cmsObject.deleteResource(path + fileName, CmsResource.DELETE_PRESERVE_SIBLINGS);
		    throw  sqlExc;   		        
		}
	}
	
	@Override
	public String uploadRFSFile(String path, String fileName, Map<String,String> parameters, InputStream content) throws Exception {
		List properties = new ArrayList(0);
		return uploadRFSFile(path, fileName, parameters, content,properties);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String uploadRFSFile(String path, String fileName, Map<String,String> parameters, InputStream content, List properties) throws Exception {
		fileName = getValidFileName(fileName);

		LOG.debug("Nombre corregido del archivo a subir al rfs: " + fileName);
		
		       fileName = checkFileName(path,fileName);
		String linkName = processPath(path, fileName);
		
		String subFolderRFSPath = getRFSSubFolderPath(rfsSubFolderFormat, parameters);
		LOG.debug("subcarpeta: " + subFolderRFSPath);

		File dir = new File(rfsDirectory + "/" + subFolderRFSPath);
		
		if (!dir.exists())
			if (dir.mkdirs())
				checkOwnerFolder(dir);
			else
			    LOG.error("Error al intentar crear el directorio " + dir.getAbsolutePath());
	   
		String fullPath = dir.getAbsolutePath() + "/" + fileName;
		String url = rfsVirtualUrl + subFolderRFSPath + fileName;
		
		LOG.debug("url : " + url);

		File uploadedFile = new File(fullPath);
		try {
			try {
				content = ImageOrientationFixer.transformImage(content);
			} catch(Exception e) {
				e.printStackTrace();
			}
			FileOutputStream fOut = new FileOutputStream(uploadedFile);
			fOut.write(CmsFileUtil.readFully(content, true));
			fOut.close();
			
			ImageMetadataPropertiesService service = new ImageMetadataPropertiesService(siteName, publication);
			service.setMetaDataProperties(fullPath, properties);
		} catch (Exception e1) {
			LOG.error("Error al intentar subir el archivo " + fullPath,e1);
			throw e1;
		}
		
		ImageIcon icono = new ImageIcon(fullPath);
		java.awt.Image imagen = icono.getImage();

		CmsProperty p = new CmsProperty(CmsPropertyDefinition.PROPERTY_IMAGE_SIZE, null,"w:"+imagen.getWidth(null)+",h:"+imagen.getHeight(null));
		properties.add(p);
		
		p = new CmsProperty(CmsPropertyDefinition.PROPERTY_IMAGE_DIMENSION_WIDTH, null,String.valueOf(imagen.getWidth(null)));
		properties.add(p);
		
		System.out.println("upload - uploadRFSFile imagen.getWidth(null): " + imagen.getWidth(null));
		System.out.println("imagen.getHeight(null): " + imagen.getHeight(null));
		LOG.debug("upload - uploadRFSFile imagen.getWidth(null): " + imagen.getWidth(null));
		LOG.debug("imagen.getHeight(null): " + imagen.getHeight(null));
		
		if (!String.valueOf(imagen.getWidth(null)).equals(null) && !String.valueOf(imagen.getHeight(null)).equals(null)) {
			int fpx = imagen.getWidth(null)/2;
			int fpy = imagen.getHeight(null)/2;
			String focalPoint = "fpx:"+fpx+",fpy:"+fpy; 
			p = new CmsProperty(CmsPropertyDefinition.PROPERTY_FOCALPOINT, null,focalPoint);
			properties.add(p);
			LOG.debug("focalPoint: " + focalPoint);
			System.out.println("focalPoint: " + focalPoint);
		}
		
		try {
			
			LOG.debug("creando link en vfs: " + linkName);
			deleteResource(linkName);
			cmsObject.createResource(linkName, 
				getPointerType(),
				url.getBytes(),
				properties
			);
			
			CmsResourceUtils.unlockResource(cmsObject,linkName, false);

			return linkName;
		} catch (CmsIllegalArgumentException e) {
			LOG.error("Error al intentar subir el archivo " + fullPath,e);
			throw e;
		} catch (CmsLoaderException e) {
			LOG.error("Error al intentar subir el archivo " + fullPath,e);
			throw e;
		} catch (CmsException e) {
			LOG.error("Error al intentar subir el archivo " + fullPath,e);
			throw e;
		}
	}
	
	@Override
	@Deprecated
	public String uploadAmzFile(String path, String fileName, Map<String,String> parameters, InputStream content) throws Exception {
		List properties = new ArrayList(0);
		return uploadAmzFile(path, fileName, parameters, content, properties);
	}
	
	@Override
	@Deprecated
	public String uploadAmzFile(String path, String fileName, Map<String,String> parameters, InputStream content, List properties) throws Exception {
		return uploadAmzFile(cmsObject, path, fileName, parameters, content, properties);
	}

	@Override
	public String uploadAmzFile(CmsObject cmsObject, String path, String fileName, Map<String,String> parameters, InputStream content) throws Exception {
		List properties = new ArrayList(0);
		return uploadAmzFile(cmsObject, path, fileName, parameters, content, properties);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String uploadAmzFile(CmsObject cmsObject, String path, String fileName, Map<String,String> parameters, InputStream content, List properties) throws Exception {
		
		fileName = getValidFileName(fileName);
		fileName = checkFileName(path,fileName);
		
		String linkName = processPath(path, fileName);
		
		if(cmsObject.existsResource(linkName)){
			throw new Exception("Ya existe un recurso con el nombre: "+linkName);
		}
		
		String subFolderRFSPath = getRFSSubFolderPath(rfsSubFolderFormat, parameters);

		File dir = new File(rfsDirectory + "/" + subFolderRFSPath);
		
		if (!dir.exists())
			if (dir.mkdirs())
				checkOwnerFolder(dir);
			else
			    LOG.error("Error al intentar crear el directorio " + dir.getAbsolutePath());

		String fullPath = dir.getAbsolutePath() + "/" + fileName;
		File uploadedFile = new File(fullPath);
		try {
			try {
				content = ImageOrientationFixer.transformImage(content);
			} catch(Exception e) {
				e.printStackTrace();
			}
			FileOutputStream fOut = new FileOutputStream(uploadedFile);
			fOut.write(CmsFileUtil.readFully(content, true));
			fOut.close();
			
			ImageMetadataPropertiesService service = new ImageMetadataPropertiesService(siteName, publication);
			service.setMetaDataProperties(fullPath, properties);
		} catch (Exception e1) {
			LOG.error("Error al intentar subir el archivo " + fullPath,e1);
			throw e1;
		}
		
		//URL urlDeLaImagen = uploadedFile.toURL();
		ImageIcon icono = new ImageIcon(fullPath);
		java.awt.Image imagen = icono.getImage();

		CmsProperty p = new CmsProperty(CmsPropertyDefinition.PROPERTY_IMAGE_SIZE, null,"w:"+imagen.getWidth(null)+",h:"+imagen.getHeight(null));
		properties.add(p);
		
		p = new CmsProperty(CmsPropertyDefinition.PROPERTY_IMAGE_DIMENSION_WIDTH, null,String.valueOf(imagen.getWidth(null)));
		properties.add(p);

		LOG.debug("upload - uploadAMZ imagen.getWidth(null): " + imagen.getWidth(null));
		LOG.debug("imagen.getHeight(null): " + imagen.getHeight(null));
		
		if (!String.valueOf(imagen.getWidth(null)).equals(null) && !String.valueOf(imagen.getHeight(null)).equals(null)) {
			int fpx = imagen.getWidth(null)/2;
			int fpy = imagen.getHeight(null)/2;
			String focalPoint = "fpx:"+fpx+",fpy:"+fpy; 
			p = new CmsProperty(CmsPropertyDefinition.PROPERTY_FOCALPOINT, focalPoint,null);
			properties.add(p);
			LOG.debug("focalPoint: " + focalPoint);
		}
		
		InputStream fileStream = CloneStreamsService.getInstance().clone(uploadedFile);
		try {
        	uploadedFile.delete();
        } catch(Exception e) {
        	e.printStackTrace();
        }
		
		return super.uploadAmzFile(cmsObject, path, fileName, parameters, fileStream, properties);
	}
	
	protected String processPath(String path, String fileName) {
		return path + fileName;
	}
	
	protected void deleteResource(String link) throws Exception {
		//To be implemented in child classes
	}
	
	public int getFolderType() {
		return this.getVfsFolderType();
	}
	
	@Override
	protected int getVfsFolderType() {
		try {
			return OpenCms.getResourceManager().getResourceType("imagegallery").getTypeId();
		} catch (CmsLoaderException e) {
			LOG.error(e);
			return CmsResourceTypeFolder.getStaticTypeId();
		}
	}

	@Override
	protected String getModuleName() {
		return "imageUpload";
	}

	@Override
	protected int getPointerType() throws CmsLoaderException {
		return CmsResourceTypeExternalImage.getStaticTypeId();
	}

	public String getCropPredefinedSizes() {
		return cropPredefinedSizes;
	}

	public void setCropPredefinedSizes(String cropPredefinedSizes) {
		this.cropPredefinedSizes = cropPredefinedSizes;
	}
	
	public String getAspectRatios() {
		return aspectRatios;
	}

	public void setAspectRatios(String aspectRatios) {
		this.aspectRatios = aspectRatios;
	}
	
	public String getDefaultAspectRatio() {
		return defaultAspectRatio;
	}
	
	public void setDefaultAspectRatio(String defaultAspectRatio) {
		this.defaultAspectRatio = defaultAspectRatio;
	}
	
	public String getPredefinedSizes() {
		return predefinedSizes;
	}

	public void setPredefinedSizes(String predefinedSizes) {
		this.predefinedSizes = predefinedSizes;
	}
	
	public String getDefaultPredefinedSize() {
		return defaultPredefinedSize;
	}

	public void setDefaultPredefinedSize(String defaultPredefinedSize) {
		this.defaultPredefinedSize = defaultPredefinedSize;
	}
	
	public int getMaxUploadWidth() {
		return maxUploadWidth;
	}

	public void setMaxUploadWidth(int maxUploadWidth) {
		this.maxUploadWidth = maxUploadWidth;
	}
	
	public int getMaxUploadHeight() {
		return maxUploadHeight;
	}

	public void setMaxUploadHeight(int maxUploadHeight) {
		this.maxUploadHeight = maxUploadHeight;
	}
	
	
	public String checkFileName(String path,String fileName){
		
		String newFileName = fileName;
		
		int count = 0;
		boolean isExist = true;
		
		String linkName = processPath(path, fileName);
		
		String tmpName =  fileName;
		String fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf("."));
		String fileNameExt = fileName.substring(fileName.lastIndexOf(".")+1,fileName.length());
		
		while (isExist){
			
			if(cmsObject.existsResource(linkName)){
				count++;
				tmpName = fileNameWithoutExt+"_"+count+"."+fileNameExt;
				linkName = processPath(path, tmpName);
			}else{
				isExist = false;
			}
		}
		
		newFileName = tmpName;
		
		return newFileName;
	}
	
	public void checkOwnerFolder(File dir){
		
		File rfsDir = new File(rfsDirectory + "/");
		
		String newFolder = dir.getPath().substring(rfsDirectory.length()+1);
		
		String [] folders = newFolder.split("/");
		
		String folder = "";
		
		for (int i=0; i< folders.length; i++ ){
			
			folder = folder + "/" +folders[i];
			String newDirStr = rfsDirectory + folder;
		
			File newDir = new File(newDirStr + "/");
			
			try{
				
				UserPrincipal rfsOwner = java.nio.file.Files.getOwner(rfsDir.toPath());
				String rfsOwnerStr = rfsOwner.getName();
				
				UserPrincipal dirOwner = java.nio.file.Files.getOwner(newDir.toPath());
				String dirOwnerStr = dirOwner.getName();
				
				if(rfsOwnerStr!=null && dirOwnerStr!=null && !dirOwnerStr.equals(rfsOwnerStr))
				{
					Path p = newDir.toPath();
					String group = rfsOwner.getName();
					UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
					GroupPrincipal groupPrincipal = lookupService.lookupPrincipalByGroupName(group);
					Files.getFileAttributeView(p, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS).setGroup(groupPrincipal);
					Files.getFileAttributeView(p, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS).setOwner(rfsOwner);	
				}
				
			}catch(UnsupportedOperationException error){
				LOG.error("Error al intentar revisar permisos del directorio " + rfsDir.getAbsolutePath()+"EROR: "+error.getMessage());
			}catch(Exception error){
				LOG.error("Error al intentar revisar permisos del directorio " + rfsDir.getAbsolutePath()+"EROR: "+error.getMessage());
			}
			
		}
		
		return;
	}
	
	
}
