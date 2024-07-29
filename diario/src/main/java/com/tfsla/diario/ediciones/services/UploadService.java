package com.tfsla.diario.ediciones.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.validator.routines.UrlValidator;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.db.CmsDbSqlException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsFileUtil;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.transfer.*;
import com.amazonaws.services.s3.transfer.Transfer.TransferState;


import com.tfsla.diario.file.types.TfsResourceTypeVideoVodLink;
import com.tfsla.diario.file.types.TfsResourceTypeVideoLinkProcessing;

public abstract class UploadService {
	public static final int UPLOAD_FTP = 1;
	public static final int UPLOAD_RFS = 2;
	public static final int UPLOAD_VFS = 3;
	public static final int UPLOAD_AMZ = 4;

	protected static final Log LOG = CmsLog.getLog(UploadService.class);
	protected CmsObject cmsObject = null;
	protected String amzDirectory = "";
	protected String amzBucket = "";
	protected String amzAccessID = "";
	protected String amzAccessKey = "";
	protected String amzRegion = "";
	protected String vfsPath = "";
	protected String vfsSubFolderFormat = "";
	protected String rfsDirectory = "";
	protected String rfsVirtualUrl = "";
	protected String rfsSubFolderFormat = "";
	protected String ftpServer = "";
	protected String ftpUser= "";
	protected String ftpPassword = "";
	protected String ftpDirectory = "";
	protected String ftpSubFolderFormat = "";
	protected String ftpVirtualUrl = "";
	protected String allowedFileTypes;
	protected int maxUploadSize;
	protected boolean ftpUploadEnabled;
	protected boolean rfsUploadEnabled;
	protected boolean vfsUploadEnabled;
	protected boolean amzUploadEnabled;
	protected String defaultUploadDestination = "server";
	protected String videoType = "";
	

	protected int getVFSResourceType(String fileName) throws CmsException {
		return OpenCms.getResourceManager().getDefaultTypeForName(fileName).getTypeId(); 
	}

	public CmsResource uploadVFSFile(String path, String fileName, InputStream content) throws CmsException, IOException {
		List properties = new ArrayList(0);
		return uploadVFSFile(path,fileName,content,properties);
	}
	
	public CmsResource uploadVFSFile(String path, String fileName, InputStream content, List properties) throws CmsException, IOException {
		byte[] buffer = CmsFileUtil.readFully(content, false);
		
		try {
			
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

	public String uploadRFSFile(String path, String fileName, Map<String,String> parameters, InputStream content) throws Exception {
		List properties = new ArrayList(0);
		return uploadRFSFile(path, fileName, parameters, content,properties);
	}
	
	public String uploadRFSFile(String path, String fileName, Map<String,String> parameters, InputStream content, List properties) throws Exception {
		fileName = getValidFileName(fileName);

		LOG.debug("Nombre corregido del archivo a subir al rfs: " + fileName);
		String subFolderRFSPath = getRFSSubFolderPath(rfsSubFolderFormat, parameters);
		
		LOG.debug("subcarpeta: " + subFolderRFSPath);

		File dir = new File(rfsDirectory + "/" + subFolderRFSPath);
		if (!dir.exists() && !dir.mkdirs()) {
			LOG.error("Error al intentar crear el directorio " + dir.getAbsolutePath());
	    }

		String fullPath = dir.getAbsolutePath() + "/" + fileName;
		String url = rfsVirtualUrl + subFolderRFSPath + fileName;
		
		LOG.debug("url : " + url);

		File uploadedFile = new File(fullPath);
		try {
			FileOutputStream fOut = new FileOutputStream(uploadedFile);
			fOut.write(CmsFileUtil.readFully(content, true));
			fOut.close();
		} catch (Exception e1) {
			LOG.error("Error al intentar subir el archivo " + fullPath,e1);
			throw e1;
		}
		
		try {
			String linkName = path + fileName;
			LOG.debug("creando link en vfs: " + linkName);
			
			cmsObject.createResource(linkName, 
					getPointerType(),
					url.getBytes(),
					properties);

			cmsObject.unlockResource(linkName);
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

	public String uploadFTPFile(String path, String fileName, Map<String,String> parameters, InputStream content) throws Exception {
		List properties = new ArrayList(0);
		return uploadFTPFile(path, fileName, parameters, content,properties);
	}
	
	public String uploadFTPFile(String path, String fileName, Map<String,String> parameters, InputStream content, List properties) throws Exception {
		String ftpSubfolder = "";
		fileName = getValidFileName(fileName);

		FTPClient client = new FTPClient();
		client.addProtocolCommandListener(new PrintCommandListener(new
				PrintWriter(System.out)));

		byte[] buffer = CmsFileUtil.readFully(content, false);
		
		try {
			client.connect(ftpServer);
			client.login(ftpUser, ftpPassword);
			client.changeWorkingDirectory(ftpDirectory);

			ftpSubfolder = getFTPSubFolderPath(client,parameters);
			
			client.enterLocalPassiveMode();
			client.setFileType(FTPClient.BINARY_FILE_TYPE);

			// Upload a file
			OutputStream os = client.storeFileStream( fileName);

			os.write(buffer);
			os.close();

			client.completePendingCommand();

			//System.out.println(client.printWorkingDirectory());

			client.logout();
			client.disconnect();
		} catch (SocketException e1) {
			LOG.error("Error al intentar subir el archivo a " + ftpServer,e1);
			throw e1;
		} catch (IOException e1) {
			LOG.error("Error al intentar subir el archivo a " + ftpServer,e1);
			throw e1;
		}

		String audioMultimediaPath = ftpVirtualUrl + ftpSubfolder + fileName;
		
		try {

			String linkName = path + fileName;
			cmsObject.createResource(linkName, 
					getPointerType(),
					audioMultimediaPath.getBytes(),
					properties);

			cmsObject.unlockResource(linkName);
			
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

	@Deprecated
	public String uploadAmzFile(String path, String fileName, Map<String,String> parameters, InputStream content) throws Exception {
		List properties = new ArrayList(0);
		return uploadAmzFile(path, fileName, parameters, content,properties);
	}
	
	@Deprecated
	public String uploadAmzFile(String path, String fileName, Map<String,String> parameters, InputStream content, List properties) throws Exception {
		return uploadAmzFile(cmsObject, path, fileName, parameters, content, properties);
	}
	
	public String uploadAmzFile(CmsObject cmsObject, String path, String fileName, Map<String,String> parameters, InputStream content) throws Exception {
		List properties = new ArrayList(0);
		return uploadAmzFile(cmsObject, path, fileName, parameters, content,properties);
		
	}
	
	public String uploadAmzFile(CmsObject cmsObject, String path, String fileName, Map<String,String> parameters, InputStream content, List properties) throws Exception {
		fileName = getValidFileName(fileName);

		LOG.debug("Nombre corregido del archivo a subir al s3 de amazon: " + fileName);
		String subFolderRFSPath = getRFSSubFolderPath(rfsSubFolderFormat, parameters);
		LOG.debug("subcarpeta: " + subFolderRFSPath);

		String dir = amzDirectory + "/" + subFolderRFSPath;
		if(!dir.endsWith("/")) {
			dir += "/";
		}
		String fullPath = dir + fileName;
		String urlRegion = amzRegion.toLowerCase().replace("_", "-");
		String amzUrl = String.format("https://%s.s3.dualstack.%s.amazonaws.com/%s", amzBucket, urlRegion, fullPath);
		LOG.debug("S3 url: " + amzUrl);
		try {
			LOG.debug("S3 credentials: " + amzAccessID + " : " + amzAccessKey);
			AWSCredentials awsCreds = new BasicAWSCredentials(amzAccessID, amzAccessKey);
			AmazonS3 s3 = new AmazonS3Client(awsCreds);
			if(amzRegion != null && !amzRegion.equals("")) {
				com.amazonaws.regions.Region region = com.amazonaws.regions.Region.getRegion(com.amazonaws.regions.Regions.valueOf(amzRegion));
				s3.setRegion(region);
			}
			
			ObjectMetadata metadata = new ObjectMetadata();
			long contentLength = 0;
			contentLength = content.available();
			metadata.setContentLength(contentLength);
			if(parameters.containsKey("ContentType")) {
				metadata.setContentType(parameters.get("ContentType"));
			}
			//PutObjectResult result = s3.putObject(amzBucket, fullPath, content, metadata);
			s3.putObject(
				new PutObjectRequest(amzBucket.replace("/", ""), fullPath, content, metadata).withCannedAcl(CannedAccessControlList.Private)
			);
		} catch (Exception e1) {
			LOG.error("Error al intentar subir el archivo " + fullPath,e1);
			throw e1;
		}
		
		try {
			String linkName = path + fileName;
			LOG.debug("creando link a S3 en vfs: " + linkName + " en " + cmsObject.getRequestContext().getSiteRoot() + " (" + cmsObject.getRequestContext().currentProject().getName()  + "|" + cmsObject.getRequestContext().currentUser().getFullName() + " )");
			int type = getPointerType();
			if (this.videoType.equals("video-processing")) {
				type = TfsResourceTypeVideoLinkProcessing.getStaticTypeId();
			}
			if (!this.videoType.equals("video-processing") && !this.videoType.equals("")) {
				type = TfsResourceTypeVideoVodLink.getStaticTypeId();
			}
			
			cmsObject.createResource(linkName, 
					type,
					amzUrl.getBytes(),
					properties);

			cmsObject.unlockResource(linkName);
			return linkName;
		} catch (CmsException e) {
			LOG.error("Error al intentar subir a S3 el archivo " + fullPath,e);
			throw e;
		}
	}
	
	private static long transferredBytes = 0;
	
	// Upload con Transfer Manager para manejar avance y cancelacion
	public String uploadAmzFileTM(String fullPath, Map<String,String> parameters, InputStream content) throws Exception {
		/*fileName = getValidFileName(fileName);

		LOG.debug("Nombre corregido del archivo a subir al s3 de amazon: " + fileName);
		String subFolderRFSPath = getRFSSubFolderPath(rfsSubFolderFormat, parameters);
		LOG.debug("subcarpeta: " + subFolderRFSPath);

		String dir = amzDirectory + "/" + subFolderRFSPath;
		if(!dir.endsWith("/")) {
			dir += "/";
		}
		String fullPath = dir + fileName;
		*/
		String urlRegion = amzRegion.toLowerCase().replace("_", "-");
		String amzUrl = String.format("https://%s.s3.dualstack.%s.amazonaws.com/%s", amzBucket, urlRegion, fullPath);
		LOG.debug("S3 url: " + amzUrl);
		
		
		LOG.debug("S3 credentials: " + amzAccessID + " : " + amzAccessKey);
		AWSCredentials awsCreds = new BasicAWSCredentials(amzAccessID, amzAccessKey);
		
		TransferManager xfer_mgr = TransferManagerBuilder.standard()
					.withS3Client(AmazonS3ClientBuilder.standard()
			        .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
			        .build()).build();
		
		String status = null;
		
		try {
			
			ObjectMetadata metadata = new ObjectMetadata();
			long contentLength = 0;
			contentLength = content.available();
			metadata.setContentLength(contentLength);
			if(parameters.containsKey("ContentType")) {
				metadata.setContentType(parameters.get("ContentType"));
			}
			
			// Crea un ProgressListener para mostrar el avance del upload
			ProgressListener progressListener = new ProgressListener() {
					@Override
					public void progressChanged(ProgressEvent progressEvent) {
						transferredBytes = progressEvent.getBytesTransferred();
						System.out.println("Progress: " + progressEvent.getBytesTransferred() + " bytes transferred");
					
					}
			};
			
			PutObjectRequest request = new PutObjectRequest(amzBucket.replace("/", ""), fullPath, content, metadata)
					.withCannedAcl(CannedAccessControlList.Private)
					.withGeneralProgressListener(progressListener);
			
			 Upload upload = xfer_mgr.upload(request);
			 
		     try {
		    	    // Bblock and wait for the upload to finish
		    	    upload.waitForCompletion();
		    	    
		    	    TransferState xfer_state = upload.getState();
		            System.out.println(": " + xfer_state);
		            
		            status = xfer_state.name();
		    	    
		     } catch (AmazonClientException amazonClientException) {
		        	System.out.println("Unable to upload file, upload aborted.");
		        	amazonClientException.printStackTrace();
		        	status = "Error";
		     }
		     
		     xfer_mgr.shutdownNow();
			   
		} catch (Exception e1) {
			status = "Error";
			LOG.error("Error al intentar subir el archivo " + fullPath,e1);
			throw e1;
		}
		
		return status;

	}
	
	
	protected String getRFSSubFolderPath(Map<String,String> parameters) throws Exception {
		Date now = null;
		
		String date = parameters.get("date");
		if (date==null)
			now = new Date();
		else {
			try {
			SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy"); 
			now = sdf.parse(date);
			}
			catch (ParseException ex) {
				LOG.error(ex);
				now = new Date();
			}
		}

		if (rfsSubFolderFormat.trim().equals(""))
			return "";

		String subFolder = "";
		String[] parts = rfsSubFolderFormat.split("/");

		for (String part : parts) {
			String subfolderName = "";
			if (parameters.get(part)!=null) {
				subfolderName = parameters.get(part);
			} else {
				SimpleDateFormat sdf = new SimpleDateFormat(part);
				subfolderName = sdf.format(now);
			}
			subFolder += subfolderName + "/";
		}
		return subFolder;
	}
	
	protected String getFTPSubFolderPath(FTPClient client, Map<String,String> parameters) throws Exception {
		Date now = null;
		
		String date = parameters.get("date");
		if (date==null) {
			now = new Date();
		} else {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy"); 
				now = sdf.parse(date);
			} catch (ParseException ex) {
				LOG.error(ex);
				now = new Date();
			}
		}

		String subFolder="";
		if (ftpSubFolderFormat.trim().equals(""))
			return "";
		
		boolean dirExists = true;
		String[] parts = ftpSubFolderFormat.split("/");
		
		for (String part : parts) {
			String subfolderName = "";
			if (parameters.get(part) != null) {
				subfolderName = parameters.get(part);
			} else {
				SimpleDateFormat sdf = new SimpleDateFormat(part);
				subfolderName = sdf.format(now);
			}
			
			if (!subfolderName.isEmpty()) {
			      if (dirExists) {
			    	  dirExists = client.changeWorkingDirectory(subfolderName);
			      }
			      if (!dirExists) {
			        if (!client.makeDirectory(subfolderName)) {
			          throw new IOException("Error al crear el directorio remoto '" + subfolderName + "'.  error='" + client.getReplyString()+"'");
			        }
			        if (!client.changeWorkingDirectory(subfolderName)) {
			          throw new IOException("Error al cambiar al nuevo directorio remoto creado '" + subfolderName + "'.  error='" + client.getReplyString()+"'");
			        }
			      }
			}

			subFolder += subfolderName + "/";
		}

		return subFolder;
	}

	protected String getValidFileName(String fileName) {
		String validFileName = cmsObject.getRequestContext().getFileTranslator().translateResource(fileName);
		validFileName = Normalizer.normalize(validFileName, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
		validFileName = validFileName.replaceAll("\\s","");
		
		String extFilename = FilenameUtils.getExtension(validFileName);
		String fileNameWhithoutExt = FilenameUtils.getBaseName(validFileName);
		
		validFileName = fileNameWhithoutExt.replaceAll("\\.", "_")+"."+extFilename;
		
		return validFileName;
	}
	
	public String getDefaultVFSUploadFolder(Map<String,String> parameters) throws Exception {
		return vfsPath + "/" + getVFSSubFolderPath(vfsPath, getVfsFolderType(), vfsSubFolderFormat, parameters);
	}
	
	public void loadBaseProperties(String siteName, String publication) {
    	String module = getModuleName();
 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
 		
 		vfsPath = config.getParam(siteName, publication, module, "vfsPath","");
 		vfsSubFolderFormat = config.getParam(siteName, publication, module, "vfsSubFolderFormat","");

		rfsDirectory = config.getParam(siteName, publication, module, "rfsDirectory","");
		rfsSubFolderFormat = config.getParam(siteName, publication, module, "rfsSubFolderFormat",""); 
		rfsVirtualUrl = config.getParam(siteName, publication, module, "rfsVirtualUrl","");

		ftpServer = config.getParam(siteName, publication, module, "ftpServer","");
		ftpUser = config.getParam(siteName, publication, module, "ftpUser","");
		ftpPassword  = config.getParam(siteName, publication, module, "ftpPassword","");
		ftpDirectory = config.getParam(siteName, publication, module, "ftpDirectory","");
		ftpVirtualUrl = config.getParam(siteName, publication, module, "ftpVirtualUrl","");
		ftpSubFolderFormat = config.getParam(siteName, publication, module, "ftpSubFolderFormat","");

		amzAccessID = config.getParam(siteName, publication, module, "amzAccessID", ""); 
		amzAccessKey = config.getParam(siteName, publication, module, "amzAccessKey","");
		amzBucket = config.getParam(siteName, publication, module, "amzBucket","");
		amzDirectory = config.getParam(siteName, publication, module, "amzDirectory","");
		amzRegion = config.getParam(siteName, publication, module, "amzRegion","");
		
 		maxUploadSize = config.getIntegerParam(siteName, publication, module, "maxUploadSize",5);
		allowedFileTypes = config.getParam(siteName, publication, module, "allowedFileTypes","*");
		
		ftpUploadEnabled = config.getBooleanParam(siteName, publication, module, "ftpUploadEnabled",false); 
		rfsUploadEnabled = config.getBooleanParam(siteName, publication, module, "rfsUploadEnabled",false);
		vfsUploadEnabled = config.getBooleanParam(siteName, publication, module, "vfsUploadEnabled",false);
		amzUploadEnabled = config.getBooleanParam(siteName, publication, module, "amzUploadEnabled",false);
		
		defaultUploadDestination = config.getParam(siteName, publication, module, "defaultUploadDestination","server");
	}
	
	protected int getPointerType() throws CmsLoaderException {
		return OpenCms.getResourceManager().getResourceType("pointer").getTypeId();
	}
	
	protected abstract int getVfsFolderType();
	protected abstract String getModuleName();

	public String getRFSSubFolderPath(String sff, Map<String,String> parameters) throws Exception {
		Date now = null;
		
		String date = parameters.get("date");
		if (date==null)
			now = new Date();
		else {
			try {
			SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy"); 
			now = sdf.parse(date);
			}
			catch (ParseException ex) {
				LOG.error(ex);
				now = new Date();
			}
		}
		if (sff.trim().equals("")) {
			return "";
		}

		String subFolder = "";
		String[] parts = sff.split("/");

		for (String part : parts) {
			String subfolderName = "";
			if (parameters.get(part) != null) {
				subfolderName = parameters.get(part);
			} else {
				SimpleDateFormat sdf = new SimpleDateFormat(part);
				subfolderName = sdf.format(now);
			}
			subFolder += subfolderName + "/";
		}

		return subFolder;
	}

	public String getVFSSubFolderPath(String parentPath, int folderType, String subFolderFormat, Map<String,String> parameters) throws Exception {
		Date now = null;
		String date = parameters.get("date");
		if (date == null) {
			now = new Date();
		} else {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy"); 
				now = sdf.parse(date);
			}
			catch (ParseException ex) {
				LOG.error(ex);
				now = new Date();
			}
		}

		String subFolder = "";
		if (subFolderFormat.trim().equals(""))
			return "";
		
		String partialFolder = parentPath + "/";
		String firstFolderCreated  = "";
		String[] parts = subFolderFormat.split("/");
		
		for (String part : parts) {
			String subfolderName = "";
			if (parameters.get(part)!=null) {
				subfolderName = parameters.get(part);
			} else {
				SimpleDateFormat sdf = new SimpleDateFormat(part);
				subfolderName = sdf.format(now);
			}
			partialFolder += subfolderName;
			subFolder += subfolderName;
			
			if (!cmsObject.existsResource(partialFolder)) {
				cmsObject.createResource(partialFolder, folderType);
				if (firstFolderCreated.equals(""))
					firstFolderCreated  = partialFolder;
			}
			partialFolder += "/";
			subFolder += "/";
		}
		if (!firstFolderCreated.equals("")) {
			OpenCms.getPublishManager().publishResource(cmsObject, firstFolderCreated);
		}
		return subFolder;
	}

	protected byte[] getPointerContent(String urlFile) throws IOException {
		String[] schemes = {"http","https"};
	    UrlValidator urlValidator = new UrlValidator(schemes);
	    if (urlValidator.isValid(urlFile)) {
			URL url = new URL(urlFile);
	        URLConnection connection = url.openConnection();
	        return CmsFileUtil.readFully(connection.getInputStream());
	    } else {
	       File file = new File(urlFile);
	       return CmsFileUtil.readFile(file);
	    }
	}
	
	public boolean isFTPUploadEnabled() {
		return ftpUploadEnabled;
	}
	
	public boolean isRFSUploadEnabled() {
		return rfsUploadEnabled;
	}
	
	public boolean isVFSUploadEnabled() {
		return vfsUploadEnabled;
	}
	
	public boolean isAmzUploadEnabled() {
		return amzUploadEnabled;
	}
	
	public String getDefaultUploadDestination() {
		return defaultUploadDestination;
	}

	public boolean fileNameExists(String fileName) {
		boolean resourceExists;
		try {
			cmsObject.lockResource(fileName);
			cmsObject.readResource(fileName);
			resourceExists = true;
			
			cmsObject.unlockResource(fileName);
		} catch (CmsException e) {
    		resourceExists = false;
		}
		
		return resourceExists;
	}

	public int getMaxUploadSize() {
		return maxUploadSize;
	}

	public void setMaxUploadSize(int maxUploadSize) {
		this.maxUploadSize = maxUploadSize;
	}

	public String getAllowedFileTypes() {
		return allowedFileTypes;
	}

	public boolean isFtpUploadEnabled() {
		return ftpUploadEnabled;
	}

	public void setFtpUploadEnabled(boolean ftpUploadEnabled) {
		this.ftpUploadEnabled = ftpUploadEnabled;
	}

	public boolean isRfsUploadEnabled() {
		return rfsUploadEnabled;
	}

	public void setRfsUploadEnabled(boolean rfsUploadEnabled) {
		this.rfsUploadEnabled = rfsUploadEnabled;
	}

	public boolean isVfsUploadEnabled() {
		return vfsUploadEnabled;
	}

	public void setVfsUploadEnabled(boolean vfsUploadEnabled) {
		this.vfsUploadEnabled = vfsUploadEnabled;
	}
	
	public String tmpFileFFmepg(String filename, byte[] contentFFmpeg){
		String tmpFilePath = null;
		File temp = new File(System.getProperty("java.io.tmpdir"), "cmsMediosVideoConverter");
		if (!temp.exists()) {
			temp.mkdirs();
			temp.deleteOnExit();
		}
		
		Runtime runtime = Runtime.getRuntime();
		try {
			runtime.exec(new String[] { "/bin/chmod", "775",temp.getAbsolutePath() });
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String folderConvertPath = temp.getAbsolutePath();
		String fileFFmpegPath = folderConvertPath + "/" + filename;
		File uploadedFile = new File(fileFFmpegPath);
		try {
			FileOutputStream fOut = new FileOutputStream(uploadedFile);
			fOut.write(contentFFmpeg);
			fOut.close();
			tmpFilePath = fileFFmpegPath;
		} catch (Exception e1) {
		   CmsLog.getLog(this).error("Error al intentar subir el archivo " + fileFFmpegPath +" :"+e1.getMessage());
		}
		
		return tmpFilePath;
	}
	
	public Boolean isAmazonS3Enabled() {
		if(amzAccessID != null && !amzAccessID.equals("") &&
				amzAccessKey != null && !amzAccessKey.equals("") && 
				amzBucket != null && !amzBucket.equals("") && 
				amzDirectory != null && !amzDirectory.equals("")) {
			return true;
		}
		
		return false;
	}
}
