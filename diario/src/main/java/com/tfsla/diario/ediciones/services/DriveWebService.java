package com.tfsla.diario.ediciones.services;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.About.Get;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;



public class DriveWebService extends A_DriveWebService {

	class CustomProgressListener implements MediaHttpUploaderProgressListener {
		  public void progressChanged(MediaHttpUploader uploader) throws IOException {
		    switch (uploader.getUploadState()) {
		      case INITIATION_STARTED:
		        //System.out.println("Initiation has started!");
		        break;
		      case INITIATION_COMPLETE:
		        //System.out.println("Initiation is complete!");
		        break;
		      case MEDIA_IN_PROGRESS:
		        //System.out.println(uploader.getChunkSize() + " - " + uploader.getProgress());
		        break;
		      case MEDIA_COMPLETE:
		        //System.out.println("Upload is complete!");
		    }
		  }
		}
	
	   public static String ORDERBY_CREATEDTIME = "createdTime"; 
	   public static String ORDERBY_FOLDER = "folder";
	   public static String ORDERBY_MODIFIEDBYMETIME = "modifiedByMeTime"; 
	   public static String ORDERBY_MODIFIEDTIME = "modifiedTime"; 
	   public static String ORDERBY_NAME = "name"; 
	   public static String ORDERBY_NAMENATURAL = "name_natural"; 
	   public static String ORDERBY_QUOTABYTESUSED = "quotaBytesUsed"; 
	   public static String ORDERBY_RECENCY = "recency"; 
	   public static String ORDERBY_SHAREDWITHMETIME = "sharedWithMeTime"; 
	   public static String ORDERBY_STARRED = "starred"; 
	   public static String ORDERBY_VIEWEDBYMETIME = "viewedByMeTime";
	   
	   public static String ORDER_DESC = " desc";
	   public static String ORDER_ASC = "";
	    

    
	private Drive drive;
	private Credential credential;
	
	public DriveWebService(Credential credential) {
		this.credential = credential;
	}
	
    
	protected Drive getDriveService() throws IOException {
    	
    	if (drive!=null)
    		return drive;
    	
        drive = new Drive.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        
        return drive;
    }
	
	
	
	public FileList listFilesWithProperties(AbstractMap.SimpleEntry[] propWithValues, boolean matchPatialValues, boolean exclusiveProps) {
		
		String q ="";
		for (AbstractMap.SimpleEntry prop : propWithValues) {
			if (q.length()>0)
				q+= (exclusiveProps ? " and " : " or ");
			q+= "properties has { key='" + prop.getKey() +"' and value='" + prop.getValue() + "' }";
		}
		
		
		return getResourceList(q,null,100,this.ORDERBY_NAME);
		
		
	}
	
	public String getCurrentUserEmail() {
		Drive service;
		try {
			service = getDriveService();
			
			Get get = service.about().get();
			
			get.setFields("user, storageQuota");
			About about = get.execute();
			
			return about.getUser().getEmailAddress();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public FileList listFolders(String folderId) {
		   String query = "mimeType = 'application/vnd.google-apps.folder'";
		   
		   if (folderId!=null) {
			   query += " and '" + folderId + "' in parents";
		   }
		   
		   return getResourceList(query,null,100);
	   }
	   
	 public String findOrCreatePath(String path) throws IOException {
		   String[] folders = path.split("/");
		   
		   String parentFolder = "root";
		   for (String folder : folders) {
			   DriveQueryBuilder builder = new DriveQueryBuilder();
			   builder.initClause().setWithNameQuery(folder).and().setInParentFolderQuery(parentFolder);
			   FileList fileList = getResourceList(builder.build(),1);
			   if (fileList.getFiles().size()>0) {
				   parentFolder = fileList.getFiles().get(0).getId();
			   }
			   else {
				   parentFolder = createFolder(folder,parentFolder);
			   }
		   }
		   return parentFolder;
	   }
	   
	 
	 public void setProperty(String fileId, String name, String value ) throws IOException {
		   Drive service = getDriveService();
		   
		   File file = getMataData(fileId);
		   
		   if (file.getProperties()==null) {
			   Map<String,String> properties = new HashMap<>();
			   file.setProperties(properties);
		   }
		   file.getProperties().put(name, value);
		   
		   service.files().update(fileId, file).execute();
	   }
	 
	   public String uploadFile(InputStream inputStream, long size, String name, String folderId, String description, Map<String,String> properties, String tags) throws IOException {
		   
		   try {
		   Drive service = getDriveService();
		   
		   File fileMetadata = new File();
	       fileMetadata.setName(name);
	       
	       if (folderId!=null)
	    	   fileMetadata.setParents(Collections.singletonList(folderId));
	       
	       if (description!=null)
	    	   fileMetadata.setDescription(description);
	       
	       if (properties!=null)
	    	   fileMetadata.setProperties(properties);
	       
	       if (tags!=null) {
		       File.ContentHints hints = new File.ContentHints();
		       hints.setIndexableText(tags);
		       fileMetadata.setContentHints(hints);
	       }
	       
	       String type = getMimeType(inputStream, name);
	    		   
	       InputStreamContent mediaContent =
	       	    new InputStreamContent(type,
	       	        new BufferedInputStream(inputStream));
	       	mediaContent.setLength(size);
	       	Drive.Files.Create request = service.files().create(fileMetadata, mediaContent);
	       	request.getMediaHttpUploader().setProgressListener(new CustomProgressListener());
	       	File file = request.execute();
	       
	       	return file.getId();
		   }
		   catch (IOException e) {
			   e.printStackTrace();
			   throw e;
		   }
	   }
	   
		public String getMimeType(InputStream inputStream, String fileName) {
			
			TikaConfig config = TikaConfig.getDefaultConfig();
			 Detector detector = config.getDetector();

			 TikaInputStream stream = TikaInputStream.get(inputStream);

			 Metadata metadata = new Metadata();
			 metadata.add(Metadata.RESOURCE_NAME_KEY, fileName);
			 try {
				MediaType mediaType = detector.detect(stream, metadata);
				
				return mediaType.getType() + "/" + mediaType.getSubtype();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			return null;
		}
	
	   public String createFolder(String name, String folderId) throws IOException {
		   
		   Drive service = getDriveService();
		   
		   File fileMetadata = new File();
		   fileMetadata.setName(name);
		   
		   if (folderId!=null)
			   fileMetadata.setParents(Collections.singletonList(folderId));
		   
		   fileMetadata.setMimeType("application/vnd.google-apps.folder");

		   File file = service.files().create(fileMetadata)
		       .setFields("id")
		       .execute();
		   
		  return file.getId();
	   }
	   
	   public File getMataData(String fileId) throws IOException {
		   
		   Drive service = getDriveService();
		   
		   File file = service.files()
				   .get(fileId)
				   .setFields("properties")
				   .execute();
		   
		   
		   return file;
	   }

	   public FileList getResourceList(String query, int size) {
		   return getResourceList(query,null,size, "");
	 	   
	   }

	   public FileList getResourceList(String query,String pageToken, int size) {
		   return getResourceList(query,pageToken,size, "");
	 	   
	   }
	    
	    public FileList getResourceList(String query,String pageToken, int size, String order) {
	        

	         // Print the names and IDs for up to 10 files.
	         FileList result;
	 		try {
	 			
	 			Drive service = getDriveService();
	 					
	 			Drive.Files.List list = service.files().list()
	 			     .setPageSize(size)
	 			     .setFields("nextPageToken, files(id, name, modifiedTime, createdTime, description, webContentLink, size, webViewLink, mimeType)")
	 			     .setQ(query);

	 			if (order!=null && order.trim().length()>0)
	 				list.setOrderBy(order);
	 			
	 			if (pageToken!=null)
	 				list.setPageToken(pageToken);
	 			
	 			result = list.execute();
	 	        
	 	        return result;
	 	        
	 		} catch (IOException e) {
	 			// TODO Auto-generated catch block
	 			e.printStackTrace();
	 		}
	     	return null;
	     }	
	    

}
