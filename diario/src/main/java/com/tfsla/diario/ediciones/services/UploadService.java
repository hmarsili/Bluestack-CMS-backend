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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResource.CmsResourceDeleteMode;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsFileUtil;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.ListPartsRequest;
import software.amazon.awssdk.services.s3.model.ListPartsResponse;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.Part;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest.Builder;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedUpload;
import software.amazon.awssdk.transfer.s3.model.Upload;
import software.amazon.awssdk.transfer.s3.model.UploadRequest;
import software.amazon.awssdk.transfer.s3.progress.TransferListener;
import software.amazon.awssdk.transfer.s3.progress.TransferListener.Context.BytesTransferred;
import software.amazon.awssdk.transfer.s3.progress.TransferListener.Context.TransferComplete;
import software.amazon.awssdk.transfer.s3.progress.TransferListener.Context.TransferFailed;
import software.amazon.awssdk.transfer.s3.progress.TransferListener.Context.TransferInitiated;
import software.amazon.awssdk.services.s3.S3AsyncClient;

import com.tfsla.diario.file.types.TfsResourceTypeVideoVodLink;

import net.sf.json.JSONObject;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.file.types.TfsResourceTypeUploadProcessing;
import com.tfsla.diario.file.types.TfsResourceTypeVideoLinkProcessing;

public abstract class UploadService {
	public static final int UPLOAD_FTP = 1;
	public static final int UPLOAD_RFS = 2;
	public static final int UPLOAD_VFS = 3;
	public static final int UPLOAD_AMZ = 4;

	protected static final Log LOG = CmsLog.getLog(UploadService.class);
	protected String publication;
	protected String siteName;
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
	protected int maxPartsNumber = 10000;
	protected int defaultPartSize = 5242880; //5Mibs 		| 1 MiB to byte = 1048576 byte
	

	protected String processPath(String path, String fileName) {
		return path + fileName;
	}
	
	public String checkFileName(String path,String fileName){
		
		String newFileName = fileName;
		
		int count = 0;
		boolean isExist = true;
		
		String linkName = processPath(path, fileName);
		//System.out.println("fileName: " + fileName +" - lastindex: "+  fileName.lastIndexOf("."));
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
			AwsBasicCredentials awsCreds = AwsBasicCredentials.create(amzAccessID, amzAccessKey);

			S3Client s3 = null;
			
			if(amzRegion != null && !amzRegion.equals("")) {
				
				s3 = S3Client.builder()
					.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
					.region(Region.of(amzRegion))
					.build();
			}else {
				
				s3 = S3Client.builder()
						.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
						.build();
			}
			
			long contentLength = 0;
			contentLength = content.available();
			
			Builder putObjectRequestBuilder = PutObjectRequest.builder();
			putObjectRequestBuilder.bucket(amzBucket.replace("/", ""));
			if(parameters.containsKey("ContentType")) {
				putObjectRequestBuilder.contentType(parameters.get("ContentType"));
			}
			putObjectRequestBuilder.contentLength(contentLength);
			putObjectRequestBuilder.acl(ObjectCannedACL.PRIVATE);
			putObjectRequestBuilder.key(fullPath);
			s3.putObject( putObjectRequestBuilder.build(), RequestBody.fromInputStream(content, contentLength));

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
		
	
	public class preMultiUploadResponse {
		List<String> presignedUrls;
		String vfsPath;
		String site;
		String user;
		String publication;
		String lang;
		String uploadId;
		int numberOfParts;
		int partsSize;
		
		public preMultiUploadResponse(List<String> presignedUrls, String uploadId, int numberOfParts, int partsSize, String vfsPath, String site, String user, String publication, String lang) {
			this.presignedUrls = presignedUrls;
			this.vfsPath = vfsPath;
			this.site = site;
			this.user = user;
			this.publication = publication;
			this.lang = lang;
			this.uploadId = uploadId;
			this.numberOfParts = numberOfParts;
			this.partsSize = partsSize;
		}
		
		public String getUploadId() {
			return uploadId;
		}

		public List<String> getPresignedUrl() {
			return presignedUrls;
		}

		public String getVfsPath() {
			return vfsPath;
		}

		public final int getNumberOfParts() {
			return numberOfParts;
		}
		
		public final int getPartsSize() {
			return partsSize;
		}

		public String getSite() {
			return site;
		}

		public String getUser() {
			return user;
		}

		public String getPublication() {
			return publication;
		}

		public String getLang() {
			return lang;
		}

	}
	
	public class preUploadResponse {
		String presignedUrl;
		String vfsPath;
		String site;
		String user;
		String publication;
		String lang;
		
		public preUploadResponse(String presignedUrl, String vfsPath, String site, String user, String publication, String lang) {
			this.presignedUrl = presignedUrl;
			this.vfsPath = vfsPath;
			this.site = site;
			this.user = user;
			this.publication = publication;
			this.lang = lang;
		}
		
		public String getPresignedUrl() {
			return presignedUrl;
		}

		public void setPresignedUrl(String presignedUrl) {
			this.presignedUrl = presignedUrl;
		}

		public String getVfsPath() {
			return vfsPath;
		}

		public void setVfsPath(String vfsPath) {
			this.vfsPath = vfsPath;
		}

		public String getSite() {
			return site;
		}

		public String getUser() {
			return user;
		}

		public String getPublication() {
			return publication;
		}

		public String getLang() {
			return lang;
		}

	}
	
	protected void abortMultiPartUpload(String fileName, String uploadId ) {

		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(amzAccessID, amzAccessKey);

		S3Client s3 = null;
		
		if(amzRegion != null && !amzRegion.equals("")) {
			
			s3 = S3Client.builder()
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.region(Region.of(amzRegion))
				.build();
		}else {
			
			s3 = S3Client.builder()
					.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
					.build();
		}
		
        try {
            
            AbortMultipartUploadRequest abortMultipartUploadRequest;
           
            abortMultipartUploadRequest = AbortMultipartUploadRequest.builder()
                    .bucket(amzBucket)
                    .key(fileName)
                    .uploadId(uploadId)
                    .build();

            s3.abortMultipartUpload(abortMultipartUploadRequest);

        } 
        
        catch (S3Exception e) {
        	LOG.error("Error al intentar abortar un multipart upload a S3 del archivo " + fileName,e);
			throw e;
        }
    }
	
	public List<Part> listMultiPartUploadsParts(String bucketName, String filename, String uploadID) {
        
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(amzAccessID, amzAccessKey);

		S3Client s3 = null;
		
		if(amzRegion != null && !amzRegion.equals("")) {
			
			s3 = S3Client.builder()
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.region(Region.of(amzRegion))
				.build();
		}else {
			
			s3 = S3Client.builder()
					.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
					.build();
		}
        try {
            ListPartsRequest listPartsRequest = ListPartsRequest.builder()
                .bucket(bucketName)
                .uploadId(uploadID)
                .key(filename)
                .build();

            ListPartsResponse response = s3.listParts(listPartsRequest);
            List<Part> parts = response.parts();
            
            return parts;
            
        } 
        
        catch (S3Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return null;
        
    }
	
	protected void completeMultipartUpload(String filename, String uploadId) {
        
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(amzAccessID, amzAccessKey);

		S3Client s3 = null;
		
		if(amzRegion != null && !amzRegion.equals("")) {
			
			s3 = S3Client.builder()
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.region(Region.of(amzRegion))
				.build();
		}else {
			
			s3 = S3Client.builder()
					.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
					.build();
		}
		
		List<Part> parts = listMultiPartUploadsParts(amzBucket, filename, uploadId);
		
		List<CompletedPart> completedParts = new ArrayList<>();

		for (int i = 0; i < parts.size(); i++) {
            Part part = parts.get(i);
            completedParts.add(CompletedPart.builder().eTag(part.eTag()).partNumber(part.partNumber()).build());
        }
	
		CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                .parts(completedParts)
                .build();

        CompleteMultipartUploadRequest completeMultipartUploadRequest =
                CompleteMultipartUploadRequest.builder()
                        .bucket(amzBucket)
                        .key(filename)
                        .uploadId(uploadId)
                        .multipartUpload(completedMultipartUpload)
                        .build();

        s3.completeMultipartUpload(completeMultipartUploadRequest);
    }
	
	protected String createPresignedMultipartUploadPart(String bucketName, String filename, String uploadId, int partNumber) {
		
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(amzAccessID, amzAccessKey);

    	try (S3Presigner presigner = S3Presigner.builder()
    			.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
    			.build()
    		) {

    	    // Create a UploadPartRequest to be pre-signed
    	     UploadPartRequest uploadPartRequest = UploadPartRequest
    	    		 .builder()
    	    		 .bucket(bucketName)
    	    		 .uploadId(uploadId)
    	    		 .key(filename)
    	    		 .partNumber(partNumber)
    	    		 .build();

    	     // Create a UploadPartPresignRequest to specify the signature duration
    	     UploadPartPresignRequest uploadPartPresignRequest =
    	         UploadPartPresignRequest.builder()
    	                                 .signatureDuration(Duration.ofDays(1))
    	                                 .uploadPartRequest(uploadPartRequest)
    	                                 .build();

    	     // Generate the presigned request
    	     PresignedUploadPartRequest presignedUploadPartRequest =
    	         presigner.presignUploadPart(uploadPartPresignRequest);
    		
    	     
    	     return presignedUploadPartRequest.url().toString();
    	     
    	}
	}
	
	protected String createMultipartUpload(String bucketName, String fileName, String contentType, Map<String, String> metadata ) {
		
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(amzAccessID, amzAccessKey);

		S3Client s3 = null;
		
		if(amzRegion != null && !amzRegion.equals("")) {
			
			s3 = S3Client.builder()
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.region(Region.of(amzRegion))
				.build();
		}else {
			
			s3 = S3Client.builder()
					.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
					.build();
		}
		
		CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder() 
				.bucket(bucketName)
				.key(fileName)
				.contentType(contentType)
				.metadata(metadata)
				.build();
             
		String uploadId = null;
     
		try {
			CreateMultipartUploadResponse response = s3.createMultipartUpload(createMultipartUploadRequest);
			uploadId = response.uploadId();
		}
		catch (S3Exception e) {
			LOG.error("Error al intentar crear un multipart upload a S3 del archivo " + fileName,e);
			throw e;
		}
		return uploadId;
	}
	
	protected abstract void addPreloadParameters(Map<String, String> metadata);
	
	public void completeUploadAmzMultiPartsFile(CmsObject cmsObject, String vfspath, String UploadId) throws Exception {

		CmsFile file = cmsObject.readFile(vfspath);
		String amzUrl = new String(file.getContents());
		amzUrl = amzUrl.replace("https://", "");
		amzUrl = amzUrl.substring(amzUrl.indexOf("/")+1);
		
		LOG.debug("Se solicita completar el upload en aws el archivo : " + amzUrl + " con upload Id: " + UploadId);
		completeMultipartUpload(amzUrl, UploadId);
		
	}
	
	public void cancelUploadAmzMultiPartsFile(CmsObject cmsObject, String vfspath, String UploadId) throws Exception {

		CmsFile file = cmsObject.readFile(vfspath);
		String amzUrl = new String(file.getContents());
		amzUrl = amzUrl.replace("https://", "");
		amzUrl = amzUrl.substring(amzUrl.indexOf("/")+1);
		
		LOG.debug("Se solicita eliminar en aws el archivo : " + amzUrl + " con upload Id: " + UploadId);
		abortMultiPartUpload(amzUrl, UploadId);
		
		cmsObject.lockResource(vfspath);
		cmsObject.deleteResource(amzUrl,CmsResource.DELETE_REMOVE_SIBLINGS);	
	}

	public void finalizeUploadAmzMultiPartsFile(CmsObject cmsObject, String vfspath, String UploadId) throws Exception {

		CmsFile file = cmsObject.readFile(vfspath);
		String amzUrl = new String(file.getContents());
		amzUrl = amzUrl.replace("https://", "");
		amzUrl = amzUrl.substring(amzUrl.indexOf("/")+1);
		
		LOG.debug("Se solicita completar en aws el archivo : " + amzUrl + " con upload Id: " + UploadId);
		completeMultipartUpload(amzUrl, UploadId);
		
		
	}
	
	
	public preMultiUploadResponse preUploadAmzMultiPartsFile(CmsObject cmsObject, String path, String fileName, String contentType, int fileSize, Map<String,String> parameters, List properties) throws Exception {
		fileName = getValidFileName(fileName);
		fileName = checkFileName(path,fileName);
		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion tEdicion = tService.obtenerTipoEdicion(Integer.parseInt(publication));
		String lang = tEdicion.getLanguage();
		
		LOG.debug("Nombre corregido del archivo a subir al s3 de amazon: " + fileName);
		String subFolderRFSPath = getRFSSubFolderPath(vfsSubFolderFormat, parameters);
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
			String linkName = path + fileName;
			LOG.debug("creando link a S3 en vfs: " + linkName + " en " + cmsObject.getRequestContext().getSiteRoot() + " (" + cmsObject.getRequestContext().currentProject().getName()  + "|" + cmsObject.getRequestContext().currentUser().getFullName() + " )");
			int type = TfsResourceTypeUploadProcessing.getStaticTypeId();
			
			cmsObject.createResource(linkName, 
					type,
					amzUrl.getBytes(),
					properties);

			cmsObject.unlockResource(linkName);
			
			Map<String, String> metadata = new HashMap<String,String>();
			metadata.put("vfsUrl", linkName);
			metadata.put("site", cmsObject.getRequestContext().getSiteRoot());
			metadata.put("user", cmsObject.getRequestContext().currentUser().getName());
			metadata.put("publication", publication);
			metadata.put("lang", lang);
			addPreloadParameters(metadata);
			
			int partSize = defaultPartSize; 
			int parts = (int)Math.ceil((double)fileSize / (double)defaultPartSize);
			if ((parts)> maxPartsNumber) {
				parts = maxPartsNumber;
				partSize = (int)Math.ceil((double)fileSize / (double)maxPartsNumber);
			}
			
			String uploadId = createMultipartUpload(amzBucket.replace("/", ""),fullPath,contentType, metadata);
			List<String> partsUrls = new ArrayList<>();
			for (int i=0; i<parts;i++) {
				String presignedUrl = createPresignedMultipartUploadPart(amzBucket.replace("/", ""), fullPath, uploadId, i+1);
				partsUrls.add(presignedUrl);
			}
			
			preMultiUploadResponse response = new preMultiUploadResponse(
					partsUrls, 
					uploadId, 
					parts, 
					partSize, 
					linkName,
					cmsObject.getRequestContext().getSiteRoot(),
					cmsObject.getRequestContext().currentUser().getName(),
					publication, 
					lang);
			
			return response;
			
		} catch (CmsException e) {
			LOG.error("Error al intentar subir a S3 el archivo " + fullPath,e);
			throw e;
		}
	}
	
	public preUploadResponse preUploadAmzFile(CmsObject cmsObject, String path, String fileName, Map<String,String> parameters, List properties) throws Exception {
		fileName = getValidFileName(fileName);
		fileName = checkFileName(path,fileName);
		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion tEdicion = tService.obtenerTipoEdicion(Integer.parseInt(publication));
		String lang = tEdicion.getLanguage();
		
		LOG.debug("Nombre corregido del archivo a subir al s3 de amazon: " + fileName);
		String subFolderRFSPath = getRFSSubFolderPath(vfsSubFolderFormat, parameters);
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
			String linkName = path + fileName;
			LOG.debug("creando link a S3 en vfs: " + linkName + " en " + cmsObject.getRequestContext().getSiteRoot() + " (" + cmsObject.getRequestContext().currentProject().getName()  + "|" + cmsObject.getRequestContext().currentUser().getFullName() + " )");
			int type = TfsResourceTypeUploadProcessing.getStaticTypeId();
			
			cmsObject.createResource(linkName, 
					type,
					amzUrl.getBytes(),
					properties);

			cmsObject.unlockResource(linkName);
			
			Map<String, String> metadata = new HashMap<String,String>();
			metadata.put("vfsUrl", linkName);
			metadata.put("site", cmsObject.getRequestContext().getSiteRoot());
			metadata.put("user", cmsObject.getRequestContext().currentUser().getName());
			metadata.put("publication", publication);
			metadata.put("lang", lang);
			addPreloadParameters(metadata);
			
			
			String presignedUrl = createPresignedUrl(amzBucket.replace("/", ""),fullPath,metadata);
			return new preUploadResponse(presignedUrl,linkName,cmsObject.getRequestContext().getSiteRoot(),cmsObject.getRequestContext().currentUser().getName(),publication,lang);
			
			//return linkName;
		} catch (CmsException e) {
			LOG.error("Error al intentar subir a S3 el archivo " + fullPath,e);
			throw e;
		}
	}
	
	/* Create a presigned URL to use in a subsequent PUT request */
    protected String createPresignedUrl(String bucketName, String keyName, Map<String, String> metadata) {
        
    	AwsBasicCredentials awsCreds = AwsBasicCredentials.create(amzAccessID, amzAccessKey);

    	try (S3Presigner presigner = S3Presigner.builder()
    			.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
    			.build()
    		) {

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .metadata(metadata)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            		.signatureDuration(Duration.ofDays(1))  // The URL expires in 10 minutes.
            		 //.signatureDuration(Duration.ofMinutes(10))  // The URL expires in 10 minutes.
                    .putObjectRequest(objectRequest)
                    .build();


            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
            String myURL = presignedRequest.url().toString();
            LOG.info("bucketName: " + bucketName);
            LOG.info("keyName: " + keyName);
            LOG.info("Presigned URL to upload a file to: " + myURL);
            LOG.info("HTTP method: " + presignedRequest.httpRequest().method());

            LOG.info( presignedRequest.signedHeaders());
            
            return presignedRequest.url().toExternalForm();
        }
    }
	
	private static boolean shouldCancel = false;
	public static HashMap<String, String> uploadStatus = new HashMap<String, String>();
	
	public static void setUploadStatus(String path,String status) {
		
		if (path.startsWith("/"))
            path = path.substring(1); 
		
		String oldStatus = uploadStatus.get(path);
		String[] parts = oldStatus.split("\\|");
		
		String newStatus = parts[0]+"|"+status;
		
		uploadStatus.put(path, newStatus);
	}
	
	public static String getUploadStatus(String fullPath) {
		
		if (fullPath.startsWith("/"))
            fullPath = fullPath.substring(1);  
		
		return uploadStatus.get(fullPath);
	}
	
	public static void uploadCancel(boolean cancel) {
		shouldCancel = cancel;
	}
	
	
	public static class customTransferListener implements TransferListener {

		private String fullpath;
		HashMap<String, String> uploadStatus;
		
		public customTransferListener(HashMap<String, String> uploadStatus, String fullpath) {
			this.fullpath = fullpath; 
			this.uploadStatus = uploadStatus;
		}
		
		@Override
		public void transferInitiated(TransferInitiated context) {
			uploadStatus.put(fullpath, "|Init");
			TransferListener.super.transferInitiated(context);
		}

		@Override
		public void bytesTransferred(BytesTransferred context) {
			double porcentaje = 0;
			try {
				porcentaje = context.progressSnapshot().ratioTransferred().getAsDouble()* 100;
			}
			catch (Exception e) {
				// TODO: handle exception
			}
			uploadStatus.put(fullpath, porcentaje+"|Uploading");
		
			TransferListener.super.bytesTransferred(context);
		}

		@Override
		public void transferComplete(TransferComplete context) {
			// TODO Auto-generated method stub
			uploadStatus.put(fullpath, "Done");
			TransferListener.super.transferComplete(context);
		}

		@Override
		public void transferFailed(TransferFailed context) {
			uploadStatus.put(fullpath, "Failed");
			TransferListener.super.transferFailed(context);
		}

		public static customTransferListener create(HashMap<String, String> uploadStatus, String fullpath) {
			return new customTransferListener(uploadStatus, fullpath);
		}
		
	}
	
	// Upload con Transfer Manager para manejar avance y cancelacion
	public String uploadAmzFileTM(String fullPath, Map<String,String> parameters, InputStream content) throws IOException, Exception {
		
		uploadStatus.put(fullPath, "0|Uploading");
		
		String urlRegion = amzRegion.toLowerCase().replace("_", "-");
		String amzUrl = String.format("https://%s.s3.dualstack.%s.amazonaws.com%s", amzBucket, urlRegion, "/"+fullPath);
		LOG.debug("S3 url: " + amzUrl);
		
		
		LOG.debug("S3 credentials: " + amzAccessID + " : " + amzAccessKey);
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(amzAccessID, amzAccessKey);

		S3TransferManager transferManager  = S3TransferManager.builder()
				.s3Client(
						S3AsyncClient.builder()
							.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
							.build()
						)
				.build();
		
		long contentLength = 0;
		try {
			contentLength = content.available();
		} catch (IOException e) {
			LOG.error("Error al intentar subir el archivo " + fullPath,e);
			throw e;
		}
		
		Builder putObjectRequestBuilder = PutObjectRequest.builder();
		putObjectRequestBuilder.bucket(amzBucket.replace("/", ""));
		if(parameters.containsKey("ContentType")) {
			putObjectRequestBuilder.contentType(parameters.get("ContentType"));
		}
			
		putObjectRequestBuilder.contentLength(contentLength);
		putObjectRequestBuilder.acl(ObjectCannedACL.PRIVATE);
		putObjectRequestBuilder.key(fullPath);

		//s3.putObject( putObjectRequestBuilder.build(), RequestBody.fromInputStream(content, contentLength));

		
		customTransferListener listener = customTransferListener.create(uploadStatus, fullPath);
		
		UploadRequest uploaRequest = UploadRequest.builder()
	            .putObjectRequest(putObjectRequestBuilder.build())
	            .requestBody(AsyncRequestBody.fromInputStream(content, contentLength, null))
	            .addTransferListener(listener)
	            .build();
		
			
		Upload fileUpload = transferManager.upload(uploaRequest);
	
		software.amazon.awssdk.transfer.s3.model.CompletedFileUpload a;
		
		if(shouldCancel){
			fileUpload.completionFuture().cancel(true);
		}
		
		CompletedUpload uploadResult = fileUpload.completionFuture().join();
		
		//fileUpload.completionFuture().cancel(true);
		
		return uploadResult.response().eTag();
		
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
		
		//System.out.println("getValidFileName:" + fileName);
		String validFileName = cmsObject.getRequestContext().getFileTranslator().translateResource(fileName);
		//System.out.println("validFileName:" + validFileName);
		validFileName = Normalizer.normalize(validFileName, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
		//System.out.println("Normalizer validFileName:" + validFileName);
		validFileName = validFileName.replaceAll("\\s","");
		//System.out.println("unspaced validFileName:" + validFileName);
		
		String extFilename = FilenameUtils.getExtension(validFileName);
		String fileNameWhithoutExt = FilenameUtils.getBaseName(validFileName);
		
		//System.out.println("extFilename:" + extFilename);
		//System.out.println("fileNameWhithoutExt:" + fileNameWhithoutExt);
		validFileName = fileNameWhithoutExt.replaceAll("\\.", "_")+"."+extFilename;
		
		return validFileName;
	}
	
	public String getDefaultVFSUploadFolder(Map<String,String> parameters) throws Exception {
		return vfsPath + "/" + getVFSSubFolderPath(vfsPath, getVfsFolderType(), vfsSubFolderFormat, parameters);
	}
	
	public void loadBaseProperties(String siteName, String publication) {
    	String module = getModuleName();
 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
 		
 		this.siteName = siteName;
 		this.publication = publication;
 		
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
		
		maxPartsNumber = config.getIntegerParam(siteName, publication, module, "amzMaxPartsNumber",10000);
		defaultPartSize = config.getIntegerParam(siteName, publication, module, "amzDefaultPartSize",5242880);
        
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
	
	protected void createAndPublishVFSSubfolders(CmsObject cms,String subFolder, int folderType) throws Exception {
		
		String[] parts = subFolder.split("/");
		
		String firstFolderCreated  = "";
		String partialFolder = "";
		for (String part : parts) {
			
			partialFolder += part;
			
			if (!cms.existsResource(partialFolder)) {
				cms.createResource(partialFolder, folderType);
				if (firstFolderCreated.equals(""))
					firstFolderCreated  = partialFolder;
			}
			partialFolder += "/";
		}
		if (!firstFolderCreated.equals("")) {
			OpenCms.getPublishManager().publishResource(cms, firstFolderCreated);
		}
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

	abstract public JSONObject callbackUpload(JSONObject data);
}
