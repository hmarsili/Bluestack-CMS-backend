package com.tfsla.diario.ediciones.services;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException; 
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.DetectModerationLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectModerationLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.IndexFacesRequest;
import com.amazonaws.services.rekognition.model.IndexFacesResult;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.rekognition.model.ModerationLabel;
import com.amazonaws.services.rekognition.model.RecognizeCelebritiesRequest;
import com.amazonaws.services.rekognition.model.RecognizeCelebritiesResult;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.SearchFacesByImageRequest;
import com.amazonaws.services.rekognition.model.SearchFacesByImageResult;
import com.amazonaws.util.IOUtils;
import com.amazonaws.services.rekognition.model.AgeRange; 
import com.amazonaws.services.rekognition.model.Attribute;
import com.amazonaws.services.rekognition.model.BoundingBox;
import com.amazonaws.services.rekognition.model.Celebrity;
import com.amazonaws.services.rekognition.model.CreateCollectionRequest;
import com.amazonaws.services.rekognition.model.CreateCollectionResult;
import com.amazonaws.services.rekognition.model.DeleteCollectionRequest;
import com.amazonaws.services.rekognition.model.DeleteCollectionResult;
import com.amazonaws.services.rekognition.model.DeleteFacesRequest;
import com.amazonaws.services.rekognition.model.DeleteFacesResult;
import com.amazonaws.services.rekognition.model.DetectFacesRequest; 
import com.amazonaws.services.rekognition.model.DetectFacesResult; 
import com.amazonaws.services.rekognition.model.FaceDetail;
import com.amazonaws.services.rekognition.model.FaceMatch;
import com.amazonaws.services.rekognition.model.FaceRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AmzRekognitionService {

	private static final Log LOG = CmsLog.getLog(AmzRekognitionService.class);
	private static Map<String, AmzRekognitionService> instances = new HashMap<String, AmzRekognitionService>();


	protected CmsObject cmsObject = null;
	protected String siteName;
	protected String publication;
	
	protected String amzBucket;
	protected String amzAccessID;
	protected String amzAccessKey;
	protected String amzRegion;

	
	
	
	public static AmzRekognitionService getInstance(CmsObject cms) {
    	String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	String publication = "0";
    	try {
			publication = String.valueOf(PublicationService.getPublicationId(cms));
		} catch (Exception e) {
			LOG.error(e);
		}

    	String id = siteName + "||" + publication;
    	
    	AmzRekognitionService instance = instances.get(id);
    	
    	if (instance == null) {
	    	instance = new AmzRekognitionService(cms,siteName, publication);

	    	instances.put(id, instance);
    	}
    	
    	instance.cmsObject = cms;
    	
    	
        return instance;
    }

	public AmzRekognitionService() {}
	
	public AmzRekognitionService(CmsObject cmsObject, String siteName, String publication) {
		this.siteName = siteName;
		this.publication = publication;
	}

	
	protected String getModuleName() {
		return "imageUpload";
	}
	
	public boolean isAmzRekognitionEnabled() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(siteName, publication, getModuleName(), "amzRekognitionEnabled", false);
	}

	public boolean isAmzRekognitionCelebritiesEnabled() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(siteName, publication, getModuleName(), "amzRekognitionCelebritiesEnabled", false);
	}

	private String getAmzAccessID() {
		return (this.amzAccessID!=null ? this.amzAccessID : CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "amzAccessID", "")); 
	}

	private String getAmzAccessKey() {
		return (this.amzAccessKey!=null ? this.amzAccessKey : CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "amzAccessKey",""));
	}
	
	private String getAmzBucket() {
		return (this.amzBucket!=null ? this.amzBucket : CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "amzBucket",""));
	}
	
	private String getAmzRegion() {
		return (this.amzRegion!=null ? this.amzRegion : CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "amzRegion","")).toLowerCase().replaceAll("_","-");
	}
	
	
	public float[] getPosition(BoundingBox box, float imageWidth, float imageHeight, String rotation) {
		
		//https://docs.aws.amazon.com/es_es/rekognition/latest/dg/images-orientation.html
		float left;
		float top;
		if (rotation==null) {
			left = imageWidth * (box.getLeft() + box.getWidth()/2);
	    	 top = imageHeight * (box.getTop() + box.getHeight()/2);
		}
		else {
		switch (rotation) {
	     case "ROTATE_0":
	        left = imageWidth * (box.getLeft() + box.getWidth()/2);
	        top = imageHeight * (box.getTop()+ box.getHeight()/2);
	        break;
	     case "ROTATE_90":
	        left = imageHeight * (1 - (box.getTop() + box.getHeight()));
	        top = imageWidth * (box.getLeft() + box.getWidth()/2);
	        break;
	     case "ROTATE_180":
	        left = imageWidth - (imageWidth * (box.getLeft() + box.getWidth()/2));
	        top = imageHeight * (1 - (box.getTop() + box.getHeight()/2));
	        break;
	     case "ROTATE_270":
	        left = imageHeight * (box.getTop()+ box.getHeight()/2);
	        top = imageWidth * (1 - box.getLeft() - box.getWidth()/2);
	        break;
	     default:
	    	 left = imageWidth * (box.getLeft() + box.getWidth()/2);
	    	 top = imageHeight * (box.getTop()+ box.getHeight()/2);;
	  }
		}
		float[] result = new float[2];
		result[0]= left;
		result[1]=top;
	
		return result;
	}
	
	public float[] estimateFocalPoint(int imageWidth, int imageHeight, DetectFacesResult faces) throws CmsException {
		float[] focalPosition=null;
		
		//https://docs.aws.amazon.com/es_es/rekognition/latest/dg/images-orientation.html
		String orientation = faces.getOrientationCorrection();
		/*
		CmsProperty p = cmsObject.readPropertyObject(cmsObject.getSitePath(imageVfsFile), CmsPropertyDefinition.PROPERTY_IMAGE_SIZE,false);
		String size = p.getValue();
		String[] partSize = size.split(",");
		
		float width = Float.parseFloat(partSize[0].replace("w:", ""));
		float height = Float.parseFloat(partSize[0].replace("h:", ""));
		*/
		
		float width = (float) imageWidth;
		float height = (float) imageHeight;
		
		float midwidth = width/2;
		float midHeight = height/2;
				
		
		List<FaceDetail> faceDetails = faces.getFaceDetails();
		if (faceDetails.size()==0) { //Sin caras el punto focal en el medio de la imagen.
			focalPosition=new float[2];
			focalPosition[0] = midwidth;
			focalPosition[1] = midHeight;
		}
		else if (faceDetails.size()==1) { //Una cara: el punto focal en la cara.
			return getPosition(faceDetails.get(0).getBoundingBox(),width,height,orientation);
		}
		else if (faceDetails.size()==2) { //Dos caras: el punto focal en la mas cercana al medio.
			float[] position1 = getPosition(faceDetails.get(0).getBoundingBox(),width,height,orientation);
			float[] position2 = getPosition(faceDetails.get(1).getBoundingBox(),width,height,orientation);

			float dist1 = (float) Math.sqrt(Math.pow(midwidth - position1[0],2)+Math.pow(midHeight - position1[1],2));
			float dist2 = (float) Math.sqrt(Math.pow(midwidth - position2[0],2)+Math.pow(midHeight - position2[1],2));
			
			focalPosition= (dist1<dist2 ? position1 : position2);
			
		}
		else { //mas de 2 caras: el punto focal en el medio de las caras.
			focalPosition=new float[2];
			 for (FaceDetail faceDetail : faceDetails) {
				 BoundingBox box = faceDetail.getBoundingBox();
				 
				 //Todo: terminar getPosition...
				 float[] position = getPosition(box,width,height,orientation); 
				 focalPosition[0]+=position[0];
				 focalPosition[1]+=position[1];
			 }
			 focalPosition[0]=focalPosition[0] /faceDetails.size();
			 focalPosition[1]=focalPosition[1] /faceDetails.size();
			 
		}
		 
		 return focalPosition;
	}
	
	public DetectFacesResult detectFaces(String photo) {
		AmazonRekognition rekognitionClient = getRekognitionClient();
		
		 DetectFacesRequest request = new DetectFacesRequest()
				 .withImage(new Image()
				 .withS3Object(new S3Object()
						 .withName(photo)
						 .withBucket(getAmzBucket())))
				 .withAttributes(Attribute.ALL);      
		 // Replace Attribute.ALL with Attribute.DEFAULT to get default values.
	     
		 try {         
			 DetectFacesResult result = rekognitionClient.detectFaces(request);         
			 return result;
	      } catch (AmazonRekognitionException e) {
	    	  e.printStackTrace();      
	   
	      }
		 return null;
	}
	
	public DetectModerationLabelsResult detectUnsafeContent(String photo) {
		 AmazonRekognition rekognitionClient = getRekognitionClient();
	      
	      DetectModerationLabelsRequest request = new DetectModerationLabelsRequest()
	        .withImage(new Image().withS3Object(new S3Object().withName(photo).withBucket(getAmzBucket())))
	        .withMinConfidence(60F);
	      try
	      {
	           DetectModerationLabelsResult result = rekognitionClient.detectModerationLabels(request);
	           
	           return result;

/*	           
 		List<ModerationLabel> labels = result.getModerationLabels();
 		System.out.println("Detected labels for " + photo);
	           for (ModerationLabel label : labels)
	           {
	              System.out.println("Label: " + label.getName()
	               + "\n Confidence: " + label.getConfidence().toString() + "%"
	               + "\n Parent:" + label.getParentName());
	          }
*/	
	       }
	       catch (AmazonRekognitionException e)
	       {
	         e.printStackTrace();
	       }
	      return null;
	}

	public DetectLabelsResult getLabels(String photo) {	

		AmazonRekognition rekognitionClient = getRekognitionClient();

		DetectLabelsRequest request = new DetectLabelsRequest()
				.withImage(new Image()
						.withS3Object(new S3Object()        
								.withName(photo).withBucket(getAmzBucket())))        
				.withMaxLabels(10)        
				.withMinConfidence(77F);
		try {         

			DetectLabelsResult result = rekognitionClient.detectLabels(request);
			return result;

		} catch(AmazonRekognitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public RecognizeCelebritiesResult RecognizeCelebrities(String photo) {
		
		AmazonRekognition rekognitionClient = getRekognitionClient();
		
		RecognizeCelebritiesRequest request = new RecognizeCelebritiesRequest()
	            .withImage(new Image()
						.withS3Object(new S3Object()        
						.withName(photo).withBucket(getAmzBucket())));

		RecognizeCelebritiesResult result=rekognitionClient.recognizeCelebrities(request);

		return result;
	}
	
	public String removeFaceFromCollection(String collectionId, String faceId) {
		
		String deletedFace = null;
		AmazonRekognition rekognitionClient = getRekognitionClient();
		
		DeleteFacesRequest deleteFacesRequest = new DeleteFacesRequest()
				.withFaceIds(faceId)
				.withCollectionId(collectionId);
		
		DeleteFacesResult result = rekognitionClient.deleteFaces(deleteFacesRequest);
		
		for (String deletedFaceItem : result.getDeletedFaces()){
			deletedFace = deletedFaceItem;
		}
		
		return deletedFace;
	}
	
	public String addFaceToCollection(String collectionId, String photo, String faceName) {
		
		String faceId = null;
		
		AmazonRekognition rekognitionClient = getRekognitionClient();
		
		Image image =  new Image()
				.withS3Object(new S3Object()
				.withBucket(getAmzBucket())
				.withName(photo)); 
		
		 IndexFacesRequest indexFacesRequest = new IndexFacesRequest()
				 .withImage(image)
				 .withCollectionId(collectionId)
				 .withExternalImageId(faceName)
				 .withDetectionAttributes("ALL");
		 
		 IndexFacesResult indexFacesResult = rekognitionClient.indexFaces(indexFacesRequest);
		 
		System.out.println(photo + " added");      
		
		List < FaceRecord > faceRecords = indexFacesResult.getFaceRecords();      
		
		for (FaceRecord faceRecord: faceRecords) {
			faceId = faceRecord.getFace().getFaceId();
			System.out.println("Face detected: Faceid is " +
					faceRecord.getFace().getFaceId());
			}
		
		 return faceId;

	}
	
	public int deleteFaceCollection(String collectionId) {
		
		AmazonRekognition rekognitionClient = getRekognitionClient();
		
		 DeleteCollectionRequest request = new DeleteCollectionRequest()
				 .withCollectionId(collectionId);      
		 DeleteCollectionResult  result = rekognitionClient.deleteCollection(request); 
		 
		 return result.getStatusCode();
	}
	
	public String createFaceCollection(String collectionId) {
		AmazonRekognition rekognitionClient = getRekognitionClient();
		
		System.out.println("Creating collection: " 
		+         collectionId);
		
		CreateCollectionRequest request = new CreateCollectionRequest()         
				.withCollectionId(collectionId);     
		
		CreateCollectionResult createCollectionResult =  
				rekognitionClient.createCollection(request);
		
		System.out.println("Statis code: " +
				createCollectionResult.getStatusCode() +
				" - CollectionArn : " +
				createCollectionResult.getCollectionArn());
		
		return createCollectionResult.getCollectionArn();
	}

	public void searchFace(String collectionId, InputStream inputStream ) {
		 Float threshold = 70F;
		 int maxFaces = 2;

		AmazonRekognition rekognitionClient = getRekognitionClient();

		try {
			ByteBuffer imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
		
			Image image =  new Image()
					.withBytes(imageBytes); 

			 SearchFacesByImageResult searchFacesByImageResult = searchFace(collectionId, threshold, maxFaces,
						rekognitionClient, image); 
				
				 System.out.println("Faces matching largest face in image  ");
				 List < FaceMatch > faceImageMatches = searchFacesByImageResult.getFaceMatches();
				 for (FaceMatch face: faceImageMatches) {
					 System.out.println(face.getFace().toString());
					 System.out.println();
				 }

		 } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		
	}
	
	public void searchFace(String collectionId, String photo) {
		 Float threshold = 70F;
		 int maxFaces = 2;
		 
		AmazonRekognition rekognitionClient = getRekognitionClient();
		
		Image image =  new Image()
				.withS3Object(new S3Object()
				.withBucket(getAmzBucket())
				.withName(photo)); 
		
		 SearchFacesByImageResult searchFacesByImageResult = searchFace(collectionId, threshold, maxFaces,
				rekognitionClient, image); 
		
		 System.out.println("Faces matching largest face in image  " + photo);
		 List < FaceMatch > faceImageMatches = searchFacesByImageResult.getFaceMatches();
		 for (FaceMatch face: faceImageMatches) {
			 System.out.println(face.getFace().toString());
			 System.out.println();
		 }
		 
	}


	private SearchFacesByImageResult searchFace(String collectionId, Float threshold, int maxFaces,
			AmazonRekognition rekognitionClient, Image image) {
		SearchFacesByImageRequest searchFacesByImageRequest = new SearchFacesByImageRequest()
				 .withCollectionId(collectionId)
				 .withImage(image)
				 .withFaceMatchThreshold(threshold)
				 .withMaxFaces(maxFaces);      
		 
		 SearchFacesByImageResult searchFacesByImageResult = rekognitionClient.searchFacesByImage(searchFacesByImageRequest);
		return searchFacesByImageResult;
	}
	
	private AmazonRekognition getRekognitionClient() {
		AWSCredentials awsCreds = new BasicAWSCredentials(getAmzAccessID(), getAmzAccessKey());

		AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder
				.standard()
				.withRegion(Regions.fromName(getAmzRegion()))
				.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
				.build();
		return rekognitionClient;
	}
	
	
	
	public static void main(String [] args)
	{
		AmzRekognitionService rekService = new AmzRekognitionService();
		
		rekService.amzBucket = "cmsmedios-core";
		rekService.amzAccessID = "";
		rekService.amzAccessKey = "";
		rekService.amzRegion = "US_EAST_1".toLowerCase().replaceAll("_","-");
		
		//DetectLabelsResult resultLabel = rekService.getLabels("images/2016/10/20/lighthouse.jpg");
		
		//List <Label> labels = resultLabel.getLabels();
		
		//System.out.println("Detected labels for " + "images/2016/10/20/lighthouse.jpg");         
		//for (Label label: labels) {            
		//	System.out.println(label.getName() + ": " + label.getConfidence().toString());         
		//}
		
		
		//resultLabel = rekService.getLabels("images/2017/04/03/noe_0755_jpg_285432573.jpg"); //images/2017/05/22/img_3887.jpg");
		
		//System.out.println("Detected labels for " + "images/2017/04/03/noe_0755_jpg_285432573.jpg");         
		//for (Label label: labels) {            
		//	System.out.println(label.getName() + ": " + label.getConfidence().toString());         
		//}
		
		
		DetectFacesResult resultFaces = rekService.detectFaces("images/2017/04/03/noe_0755_jpg_285432573.jpg");
 
		 List < FaceDetail > faceDetails = resultFaces.getFaceDetails();
		 
		 try {
			 float[] focalPoint = rekService.estimateFocalPoint(373, 554, resultFaces);
			System.out.println(focalPoint[0] + "," + focalPoint[1]);
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 
		 for (FaceDetail face: faceDetails) {
			 System.out.println("cara -->" + face.getBoundingBox().getLeft() + "," + face.getBoundingBox().getTop());
			 
		 
		 }
		 
		 resultFaces = rekService.detectFaces( "images/2016/10/07/cr7.jpg");
		 
		faceDetails = resultFaces.getFaceDetails();
		 
		 try {
			 float[] focalPoint = rekService.estimateFocalPoint(620, 444, resultFaces);
			System.out.println(focalPoint[0] + "," + focalPoint[1]);
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 
		 for (FaceDetail face: faceDetails) {
			 System.out.println("cara -->" + face.getBoundingBox().getLeft() + "," + face.getBoundingBox().getTop());
			 
		 
		 }
		 
		
		 /*
        for (FaceDetail face: faceDetails) {
       	   
       		 AgeRange ageRange = face.getAgeRange();    
       		 System.out.println("The detected face is estimated to be between "
       		 + ageRange.getLow().toString() + " and " + ageRange.getHigh().toString()                  + " years old."); 
       		 System.out.println("Here's the complete set of attributes:");
       	
           ObjectMapper objectMapper = new ObjectMapper();
           try {
			System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(face));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        }
        */
        

/*		
		RecognizeCelebritiesResult result = rekService.RecognizeCelebrities("images/2016/10/07/cr7.jpg");
		List<Celebrity> celebs=result.getCelebrityFaces();
		
		for (Celebrity celebrity: celebs) {
            System.out.println("Celebrity recognized: " + celebrity.getName());
            System.out.println("Celebrity ID: " + celebrity.getId());
            BoundingBox boundingBox=celebrity.getFace().getBoundingBox();
            System.out.println("position: " +
               boundingBox.getLeft().toString() + " " +
               boundingBox.getTop().toString());
            System.out.println("Further information (if available):");
            for (String url: celebrity.getUrls()){
               System.out.println(url);
            }
            System.out.println();
         }
         System.out.println(result.getUnrecognizedFaces().size() + " face(s) were unrecognized.");

         
         DetectModerationLabelsResult resultUnsafeContent = rekService.detectUnsafeContent("images/2016/10/07/cr7.jpg");
         List<ModerationLabel> labelsUnsafe = resultUnsafeContent.getModerationLabels();
  		
 	           for (ModerationLabel label : labelsUnsafe)
 	           {
 	              System.out.println("Label: " + label.getName()
 	               + "\n Confidence: " + label.getConfidence().toString() + "%"
 	               + "\n Parent:" + label.getParentName());
 	          }
  */       
		/*
		String collectionId = "personajes";
		
		rekService.createFaceCollection(collectionId);

		rekService.addFaceToCollection(collectionId, "images/2017/03/14/logan.jpg","logan"); //Logan
		rekService.addFaceToCollection(collectionId, "images/2016/10/26/scioli_12.jpg","scioli"); //scioli
		rekService.addFaceToCollection(collectionId, "images/2016/10/07/cr7.jpg","ronaldo"); //Cristiano ronaldo
		rekService.addFaceToCollection(collectionId, "images/2017/06/08/paul_erdos.jpg","erdos");
		String faceId = rekService.addFaceToCollection(collectionId, "images/2017/06/08/markzukerberg.png","zukerberg");
		rekService.addFaceToCollection(collectionId, "images/2017/06/08/asimov.jpg","asimov");

		
		rekService.searchFace(collectionId, "images/2017/06/08/150925062933-zuckerberg-un-poverty-global-internet-access-780x439.jpg");
		
		rekService.removeFaceFromCollection(collectionId, faceId);

		rekService.searchFace(collectionId, "images/2017/06/08/150925062933-zuckerberg-un-poverty-global-internet-access-780x439.jpg");

		//falta buscar deteccion desde imagen en buket
		//falta buscar detecion desde imagen externa
		//falta borrar cara y luego volver a buscar.
		
		//Borro la coleccion.
		rekService.deleteFaceCollection(collectionId);
		*/
	}
}


