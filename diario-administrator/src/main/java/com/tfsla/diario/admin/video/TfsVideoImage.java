package com.tfsla.diario.admin.video;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.jsp.PageContext;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFileUtil;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.ImagenService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.VideosService;
import com.tfsla.diario.videoConverter.VideoCapture;
import com.tfsla.diario.videoConverter.jsp.TfsVideosAdmin;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public class TfsVideoImage {
	
	private CPMConfig config;
	private String siteName;
    private String publication;
    private TipoEdicion currentPublication;
    private String moduleConfigName;
    private HttpSession m_session;
    private CmsFlexController m_controller;
    private CmsObject m_cms;
    private String initialPosition;
    private String frameRate;
    private String interval;
    private String rfsVideoDirectory;
    private String rfsVideoVirtualUrl;
    private String ftpVideoServer;
    private String ftpVideoUser;
    private String ftpVideoPassword;
    private String ftpVideoDirectory;
    private String rfsTempDirectory;
    private String rfsTempVirtualUrl;
	
	public TfsVideoImage(PageContext context, HttpServletRequest req, HttpServletResponse res) throws Exception {
		m_session = req.getSession();
		m_controller = CmsFlexController.getController(req);
		m_cms = m_controller.getCmsObject();
		
		siteName = OpenCms.getSiteManager().getCurrentSite(m_cms).getSiteRoot();
		
		currentPublication = (TipoEdicion) m_session.getAttribute("currentPublication");

    	if (currentPublication==null) {
        	TipoEdicionService tService = new TipoEdicionService();

    		currentPublication = tService.obtenerEdicionOnlineRoot(siteName);
    		m_session.setAttribute("currentPublication",currentPublication);
    	}
    	
    	publication = "" + currentPublication.getId();
    	moduleConfigName = "videoConvert";
    	
    	config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
    	
    	loadBaseProperties();
		
    }
	
	public TfsVideoImage(PageContext context, HttpServletRequest req, HttpServletResponse res, String siteNAME, String publicationID) throws Exception {
		m_session = req.getSession();
		m_controller = CmsFlexController.getController(req);
		m_cms = m_controller.getCmsObject();
		
		siteName = siteNAME;
    	publication = publicationID;
    	
    	TipoEdicionService tService = new TipoEdicionService();
		currentPublication = tService.obtenerTipoEdicion(Integer.parseInt(publication));
		
		m_session.setAttribute("currentPublication",currentPublication);
		
    	moduleConfigName = "videoConvert";
    	
    	config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
    	
    	loadBaseProperties();
		
    }
	
	private void loadBaseProperties() {
		initialPosition = config.getItemGroupParam(siteName, publication, moduleConfigName,"videoCapture","initialPosition");
	          frameRate = config.getItemGroupParam(siteName, publication, moduleConfigName,"videoCapture","frameRate");
	           interval = config.getItemGroupParam(siteName, publication, moduleConfigName,"videoCapture","interval");
	   rfsTempDirectory = config.getParam(siteName, publication, moduleConfigName, "tempFolder","");
	  rfsTempVirtualUrl = config.getParam(siteName, publication, moduleConfigName, "tempVirtualUrl","");
	           
	  rfsVideoDirectory = config.getParam(siteName, publication, "videoUpload", "rfsDirectory","");
	 rfsVideoVirtualUrl = config.getParam(siteName, publication, "videoUpload", "rfsVirtualUrl","");
	 
	     ftpVideoServer = config.getParam(siteName, publication, "videoUpload", "ftpServer","");
		   ftpVideoUser = config.getParam(siteName, publication, "videoUpload", "ftpUser","");
	  ftpVideoPassword  = config.getParam(siteName, publication, "videoUpload", "ftpPassword","");
	  ftpVideoDirectory = config.getParam(siteName, publication, "videoUpload", "ftpDirectory","");
	}
	
	public List<String> getThumbnails(String sourceVFSPath, String initPos, String frameR, boolean reload) {
		CmsResource    resource;
		String    sourcePath="";
		List<String> thumbnails = new ArrayList<String>();
		String captureInitPosition = initPos;
		
		if(initPos == null) captureInitPosition = initialPosition;
		
		String captureFrameRate = frameR;
		
		if(frameR == null) captureFrameRate = frameRate;
		
		try {
			
			TfsVideosAdmin tfsVideosAdmin = new TfsVideosAdmin(m_cms, siteName, publication);
			
			           resource = m_cms.readResource(sourceVFSPath);
		     CmsFile       file = m_cms.readFile(resource);

			String         data = new String(file.getContents());
			int          typeid = resource.getTypeId();
			String resourceType = OpenCms.getResourceManager().getResourceType(typeid).getTypeName();  
			String         type = "VFS";
	
			if(resourceType.equals("video-link") || resourceType.equals("videoVod-link")) {
				  boolean isInRFS = isInRFS(sourceVFSPath);
				    
				  if(isInRFS)
			         type = "RFS";
				  else {
					  if(VideosService.getInstance(m_cms).getDefaultUploadDestination().equals("amz")) {
						  type = "AMZ";
					  } else {
						  type = "FTP";
					  }
				  }
			}
			
			if(type.equals("RFS")){
				
				sourcePath = data.replace(rfsVideoVirtualUrl, rfsVideoDirectory+"/" );
				
			}else if(type.equals("VFS")){
				
				CmsFile cmsFile;
				cmsFile = m_cms.readFile(sourceVFSPath);
				
				String fileName = sourceVFSPath.substring(sourceVFSPath.lastIndexOf("/")); 
				
				
				String tmpFile = tfsVideosAdmin.tmpFileFFmepg(fileName, cmsFile.getContents());
				
				if(tmpFile != null)
					sourcePath = tmpFile;
				
			}else if(type.equals("FTP")){
				
				FTPClient client = new FTPClient();
				client.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

				client.connect(ftpVideoServer);
				client.login(ftpVideoUser, ftpVideoPassword);
				client.changeWorkingDirectory(ftpVideoDirectory);
					
				client.enterLocalPassiveMode();
					
				InputStream content = client.retrieveFileStream(data);
					
				byte[] buffer = CmsFileUtil.readFully(content, true);
					
				String fileName = sourceVFSPath.substring(sourceVFSPath.lastIndexOf("/")); 
					
				String tmpFile = tfsVideosAdmin.tmpFileFFmepg(fileName, buffer);
					
				if(tmpFile != null)
					sourcePath = tmpFile;
			} else if(type.equals("AMZ")) {
				String amzBucket = config.getParam(siteName, publication, "videoUpload", "amzBucket","");
				String amzAccessID  = config.getParam(siteName, publication, "videoUpload", "amzAccessID","");
				String amzAccessKey = config.getParam(siteName, publication, "videoUpload", "amzAccessKey","");
				String amzRegion = config.getParam(siteName, publication, "videoUpload", "amzRegion","");
				
				CmsResource vfsResource = m_cms.readResource(sourceVFSPath);
				CmsFile vfsFile = m_cms.readFile(vfsResource);
				String sourceUrl = new String(vfsFile.getContents());
				
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
				
				
				// Paso el archivo del bucket de amz a un tmp del server
				int ix = 0;
				if(sourceUrl.contains(amzBucket + "/")) {
					ix = sourceUrl.indexOf(amzBucket) + amzBucket.length() + 1;
				} else {
					String amzDomain = "amazonaws.com";
					ix = sourceUrl.indexOf(amzDomain) + amzDomain.length() + 1;
				}
				String fileName = sourceUrl.substring(ix);
				CmsLog.getLog(this).info("Videos - AMZ Key: " + fileName);
	
				ResponseBytes<GetObjectResponse> response = s3.getObjectAsBytes(
						GetObjectRequest.builder()
							.bucket(amzBucket.replace("/", ""))
							.key(fileName)
							.build());
						
						
				byte[] dataBytes = response.asByteArray();
								
				fileName = fileName.substring(fileName.lastIndexOf("/")+1);
				CmsLog.getLog(this).info("Videos - Temp File Name: " + fileName);
				String tmpFile = tfsVideosAdmin.tmpFileFFmepg(fileName, dataBytes);
				CmsLog.getLog(this).info("Videos - TMP file: " + tmpFile);
				
				if(tmpFile != null)
					sourcePath = tmpFile;
			}
			
			if(!sourcePathExist(sourcePath)) {
				CmsLog.getLog(this).info("Videos - SourcePath not exists: " + sourcePath);
				return thumbnails;
			}
		
			String subFolderTarget = resource.getResourceId().getStringValue();
		    boolean subFolderExist = checkSubFolderExist(rfsTempDirectory,subFolderTarget);
		    String targetFolder = rfsTempDirectory +"/"+ subFolderTarget+"/";
		    int dif = 0;
		    
		    if(!reload && subFolderExist) {
		    	File folder = new File(targetFolder);
		    	String[] filesList = folder.list();
		    	int count = Integer.parseInt(captureFrameRate);
		    	CmsLog.getLog(this).info("Videos - subFolderExist - FFmpeg images: " + count);
		    	if(filesList!=null) {
		    		for(int x=0; x<filesList.length;x++) {
	    				String src = filesList[x];
	    	            src = rfsTempVirtualUrl + subFolderTarget+"/" + src;
	    	        
	    	            if(x<count) {
	    	                 thumbnails.add(src);
	    	            }
		    		}
		    		dif = count - filesList.length;
		    	}
		    }
		    
		    if(reload) 
		    	cleanFolder(targetFolder);
		    
		    if(reload || !subFolderExist || dif>0) {
		    	
		    	String cant = captureFrameRate;
		    	
		    	if(dif>0) cant = String.valueOf(dif);
			
			    VideoCapture videoCapture = new VideoCapture();
			                 videoCapture.setInitPosition(captureInitPosition);
			                 videoCapture.setSource(sourcePath);
			               videoCapture.setDirTarget(targetFolder);
			               videoCapture.setFrameRate(cant);
			               videoCapture.setInterval(interval);
			  	       videoCapture.executeVideoCapture();
			  	       
			  	       String msgFFmpeg = videoCapture.getMsgStatus();
			  	       		  msgFFmpeg = msgFFmpeg.toLowerCase();
			  	       
			  	       if((msgFFmpeg.indexOf("error")>-1 || msgFFmpeg.indexOf("exception")>-1) && !msgFFmpeg.contains("a non-intra slice in an idr nal unit") ) {
			  	    	   CmsLog.getLog(this).error("Videos - FFmpeg error: " + msgFFmpeg);
			  	    	   return thumbnails;
			  	       }
			  	       
			  	       List<String> images = videoCapture.GetListImg();
			  	       CmsLog.getLog(this).info("Videos - FFmpeg images: " + images.size());
			  	       for(int j=0;j<=images.size()-1;j++) {
			  	    	   String src = images.get(j);
			  	    	   src = rfsTempVirtualUrl + subFolderTarget+"/" + src;
			  	    	   thumbnails.add(src);
				      }
		    }
		} catch (CmsException e) {
			CmsLog.getLog(this).error("Videos - Error getting thumbnail with FFMPEG: "+e.getMessage(), e);
		} catch (Exception e) {
			CmsLog.getLog(this).error("Videos - Error getting thumbnail with FFMPEG: "+e.getMessage(), e);
		}
		
		return thumbnails;
	}
	
public String uploadFFmpegImage(String imgPath, String type) throws Exception{
		
		String imageRFSPath = imgPath.replace(rfsTempVirtualUrl, rfsTempDirectory+"/");
		
		String fileName = imgPath.substring(imgPath.lastIndexOf("/")+1);
		
		String imageVFSPath = null;
		
		if(type.equals("VFS"))
		{
			imageVFSPath = uploadImageVFS(imageRFSPath, fileName);
		}else if(type.equals("RFS")){
			imageVFSPath = uploadImageRFS(imageRFSPath, fileName);
		}else if(type.equals("FTP")){
			imageVFSPath = uploadImageFTP(imageRFSPath, fileName);
		} else if(type.equals("AMZ")){
			imageVFSPath = uploadImageAMZ(imageRFSPath, fileName);
		}
		
		return imageVFSPath;
	}
	
	public String uploadImageToCms(String videoPath, String imgPath, String type) throws Exception{
		CmsLog.getLog(this).info(String.format("Videos - Uploading video image %s to %s - video %s", imgPath, type, videoPath));
		String status = null;
		String imageRFSPath = imgPath.replace(rfsTempVirtualUrl, rfsTempDirectory+"/");
		String fileName = imgPath.substring(imgPath.lastIndexOf("/")+1);
		String imageVFSPath = null;
		
		CmsLog.getLog(this).info(String.format("Videos - imageRFSPath: %s", imageRFSPath));
		CmsLog.getLog(this).info(String.format("Videos - fileName: %s", fileName));
		
		if(type.equals("VFS")) {
			imageVFSPath = uploadImageVFS(imageRFSPath, fileName);
		}else if(type.equals("RFS")){
			imageVFSPath = uploadImageRFS(imageRFSPath, fileName);
		}else if(type.equals("FTP")){
			imageVFSPath = uploadImageFTP(imageRFSPath, fileName);
		}else if(type.equals("AMZ")){
			imageVFSPath = uploadImageAMZ(imageRFSPath, fileName);
		}
		
		CmsLog.getLog(this).info(String.format("Videos - imageVFSPath: %s", imageVFSPath));
		
		if(imageVFSPath!=null) {
			if (!m_cms.getLock(videoPath).isUnlocked()){
			     if(!m_cms.getLock(videoPath).isOwnedBy(m_cms.getRequestContext().currentUser())){
				      m_cms.changeLock(videoPath);
			    }
			}else{
			     m_cms.lockResource(videoPath);
			}
			
			if (!m_cms.getLock(imageVFSPath).isUnlocked()){
			     if(!m_cms.getLock(imageVFSPath).isOwnedBy(m_cms.getRequestContext().currentUser())){
				      m_cms.changeLock(imageVFSPath);
			    }
			}else{
			     m_cms.lockResource(imageVFSPath);
			}
			
			CmsProperty prop = new CmsProperty();
            prop.setName("prevImage");
            prop.setValue(imageVFSPath, CmsProperty.TYPE_INDIVIDUAL);
            m_cms.writePropertyObject(videoPath, prop);
			m_cms.addRelationToResource( videoPath, imageVFSPath, "videoImage");
			
			CmsProperty titleVideoProp = m_cms.readPropertyObject(videoPath, "Title", false);  
			String titleVideo = null;
			
			if(titleVideoProp!=null) {
				titleVideo = titleVideoProp.getValue();
				
				if(titleVideo.equals(""))
					titleVideo = videoPath.substring(videoPath.lastIndexOf('/') + 1);  
			}
			
			prop = new CmsProperty();
            prop.setName("Title");
            prop.setValue(titleVideo, CmsProperty.TYPE_INDIVIDUAL);
            m_cms.writePropertyObject(imageVFSPath, prop);
            
            prop = new CmsProperty();
            prop.setName("Description");
            prop.setValue(titleVideo, CmsProperty.TYPE_INDIVIDUAL);
            m_cms.writePropertyObject(imageVFSPath, prop);
			
			m_cms.unlockResource(videoPath);
			m_cms.unlockResource(imageVFSPath);
			
			status = imageVFSPath;
		}
		
		return status;
	}
	
	protected String uploadImageVFS(String imgPath,String fileName) throws Exception{
		
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put("section","videos");
		
		String path = "/" + ImagenService.getInstance(m_cms).getDefaultVFSUploadFolder(parameters);
	
		String vfsImgPath = path + fileName;
		
		FileInputStream			  in = new FileInputStream(imgPath);
		ByteArrayOutputStream outVfs = new ByteArrayOutputStream();
		byte[] 					 buf = new byte[1024];
		int                        n = 0;
		        
		while (-1 != (n = in.read(buf))) {
			outVfs.write(buf, 0, n);
		}
		     
		outVfs.close();
		in.close();
		byte[] buffer = outVfs.toByteArray();
		
		m_cms.createResource(vfsImgPath, OpenCms.getResourceManager().getResourceType("image").getTypeId(),buffer,null);
		
		return vfsImgPath;
	}
	
	protected String uploadImageAMZ(String imgPath, String fileName) throws Exception {
		
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put("section","videos");
		
		String path = "/" + ImagenService.getInstance(m_cms).getDefaultVFSUploadFolder(parameters);
	
		BufferedInputStream buffIn = null;
        buffIn = new BufferedInputStream(new FileInputStream(imgPath));
		
		String lcFileName = fileName.toLowerCase();
		String imageVFS = ImagenService.getInstance(m_cms).uploadAmzFile(path,lcFileName,parameters,buffIn);
		
		buffIn.close();
		
		return imageVFS;
		
	}
	
	protected String uploadImageRFS(String imgPath,String fileName) throws Exception{
		
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put("section","videos");
		
		String path = "/" + ImagenService.getInstance(m_cms).getDefaultVFSUploadFolder(parameters);
	
		BufferedInputStream buffIn = null;
        buffIn = new BufferedInputStream(new FileInputStream(imgPath));
		
		String lcFileName = fileName.toLowerCase();
		String imageVFS = ImagenService.getInstance(m_cms).uploadRFSFile(path,lcFileName,parameters,buffIn);
		
		buffIn.close();
		
		return imageVFS;
	}
	
	protected String uploadImageFTP(String imgPath,String fileName) throws Exception{
		
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put("section","videos");
		
		String path = "/" + ImagenService.getInstance(m_cms).getDefaultVFSUploadFolder(parameters);
		
		BufferedInputStream buffIn = null;
        buffIn = new BufferedInputStream(new FileInputStream(imgPath));
	    
	    String lcFileName = fileName.toLowerCase();
		String imageFTP = ImagenService.getInstance(m_cms).uploadFTPFile(path,lcFileName,parameters,buffIn);
		
		buffIn.close();
		
		return imageFTP;
	}
	
	public void assignImage(String videoPath, String imagePath) throws CmsException{
		
		if (!m_cms.getLock(videoPath).isUnlocked()){
		     if(!m_cms.getLock(videoPath).isOwnedBy(m_cms.getRequestContext().currentUser())){
			      m_cms.changeLock(videoPath);
		    }
		}else{
		     m_cms.lockResource(videoPath);
		}
		
		if (!m_cms.getLock(imagePath).isUnlocked()){
		     if(!m_cms.getLock(imagePath).isOwnedBy(m_cms.getRequestContext().currentUser())){
			      m_cms.changeLock(imagePath);
		    }
		}else{
		     m_cms.lockResource(imagePath);
		}
		
		CmsProperty prop = new CmsProperty();
        prop.setName("prevImage");
        prop.setValue(imagePath, CmsProperty.TYPE_INDIVIDUAL);
        m_cms.writePropertyObject(videoPath,prop);
      
		m_cms.addRelationToResource( videoPath, imagePath, "videoImage");
		
		m_cms.unlockResource(videoPath);
		m_cms.unlockResource(imagePath);
	}
	
	protected boolean isInRFS(String sourceVFSPath){
		
		boolean isInRFS = false;
		
		try { 
		
			CmsResource resource = m_cms.readResource(sourceVFSPath);
			CmsFile 	    file = m_cms.readFile(resource);
			String    sourcePath = new String(file.getContents());
	
			if(sourcePath.indexOf(rfsVideoVirtualUrl)>-1)
				isInRFS = true;
			
		} catch (CmsException e) {
			CmsLog.getLog(this).equals("Error al verificar destino de video: "+e.getMessage());
		}
		
		return isInRFS;
	}
	
	public String getDefaultInitialPosition(){
		return initialPosition;
	}
	
	public String getDefaultFrameRate(){
		return frameRate;
	}
	
	public boolean checkSubFolderExist(String tempDirectory,String subFolderTarget){
		
		boolean exist = false;
		
		String fullPath = tempDirectory+"/"+subFolderTarget;
		
		File temp = new File(fullPath);
		if (!temp.exists()) {
			temp.mkdirs();
			
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec(new String[] { "/bin/chmod", "775",temp.getAbsolutePath() });
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}else{
			exist = true;
		}
		
		return exist;
	}
	
	protected boolean sourcePathExist(String sourcePath) {
		boolean exist = false;
		
		File temp = new File(sourcePath);
		
		if (temp.exists()) {
			exist = true;
		}
		
		return exist;
	}
	
	protected void cleanFolder(String targetFolder){
		File        folder = new File(targetFolder);
    	File[]   filesList = folder.listFiles();
    	
    	if(filesList!=null) {
    		for(int x=0; x<filesList.length;x++) {
    			filesList[x].delete();
    		}
    	}
	}
}