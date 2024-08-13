package com.tfsla.diario.ediciones.services;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.RekognitionException; 
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.DetectModerationLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectModerationLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.IndexFacesRequest;
import software.amazon.awssdk.services.rekognition.model.IndexFacesResponse;
import software.amazon.awssdk.services.rekognition.model.RecognizeCelebritiesRequest;
import software.amazon.awssdk.services.rekognition.model.RecognizeCelebritiesResponse;
import software.amazon.awssdk.services.rekognition.model.S3Object;
import software.amazon.awssdk.services.rekognition.model.SearchFacesByImageRequest;
import software.amazon.awssdk.services.rekognition.model.SearchFacesByImageResponse;
import software.amazon.awssdk.utils.IoUtils;
import software.amazon.awssdk.services.rekognition.model.Attribute;
import software.amazon.awssdk.services.rekognition.model.BoundingBox;
import software.amazon.awssdk.services.rekognition.model.CreateCollectionRequest;
import software.amazon.awssdk.services.rekognition.model.CreateCollectionResponse;
import software.amazon.awssdk.services.rekognition.model.DeleteCollectionRequest;
import software.amazon.awssdk.services.rekognition.model.DeleteCollectionResponse;
import software.amazon.awssdk.services.rekognition.model.DeleteFacesRequest;
import software.amazon.awssdk.services.rekognition.model.DeleteFacesResponse;
import software.amazon.awssdk.services.rekognition.model.DetectFacesRequest; 
import software.amazon.awssdk.services.rekognition.model.DetectFacesResponse; 
import software.amazon.awssdk.services.rekognition.model.FaceDetail;
import software.amazon.awssdk.services.rekognition.model.FaceMatch;
import software.amazon.awssdk.services.rekognition.model.FaceRecord;

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
			left = imageWidth * (box.left() + box.width()/2);
	    	 top = imageHeight * (box.top() + box.height()/2);
		}
		else {
		switch (rotation) {
	     case "ROTATE_0":
	        left = imageWidth * (box.left() + box.width()/2);
	        top = imageHeight * (box.top()+ box.height()/2);
	        break;
	     case "ROTATE_90":
	        left = imageHeight * (1 - (box.top() + box.height()));
	        top = imageWidth * (box.left() + box.width()/2);
	        break;
	     case "ROTATE_180":
	        left = imageWidth - (imageWidth * (box.left() + box.width()/2));
	        top = imageHeight * (1 - (box.top() + box.height()/2));
	        break;
	     case "ROTATE_270":
	        left = imageHeight * (box.top()+ box.height()/2);
	        top = imageWidth * (1 - box.left() - box.width()/2);
	        break;
	     default:
	    	 left = imageWidth * (box.left() + box.width()/2);
	    	 top = imageHeight * (box.top()+ box.height()/2);;
	  }
		}
		float[] Response = new float[2];
		Response[0]= left;
		Response[1]=top;
	
		return Response;
	}
	
	public float[] estimateFocalPoint(int imageWidth, int imageHeight, DetectFacesResponse faces) throws CmsException {
		float[] focalPosition=null;
		
		//https://docs.aws.amazon.com/es_es/rekognition/latest/dg/images-orientation.html
		String orientation = faces.orientationCorrectionAsString();
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
				
		
		List<FaceDetail> faceDetails = faces.faceDetails();
		if (faceDetails.size()==0) { //Sin caras el punto focal en el medio de la imagen.
			focalPosition=new float[2];
			focalPosition[0] = midwidth;
			focalPosition[1] = midHeight;
		}
		else if (faceDetails.size()==1) { //Una cara: el punto focal en la cara.
			return getPosition(faceDetails.get(0).boundingBox(),width,height,orientation);
		}
		else if (faceDetails.size()==2) { //Dos caras: el punto focal en la mas cercana al medio.
			float[] position1 = getPosition(faceDetails.get(0).boundingBox(),width,height,orientation);
			float[] position2 = getPosition(faceDetails.get(1).boundingBox(),width,height,orientation);

			float dist1 = (float) Math.sqrt(Math.pow(midwidth - position1[0],2)+Math.pow(midHeight - position1[1],2));
			float dist2 = (float) Math.sqrt(Math.pow(midwidth - position2[0],2)+Math.pow(midHeight - position2[1],2));
			
			focalPosition= (dist1<dist2 ? position1 : position2);
			
		}
		else { //mas de 2 caras: el punto focal en el medio de las caras.
			focalPosition=new float[2];
			 for (FaceDetail faceDetail : faceDetails) {
				 BoundingBox box = faceDetail.boundingBox();
				 
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
	
	public DetectFacesResponse detectFaces(String photo) {
		RekognitionClient rekognitionClient = getRekognitionClient();
		
		 DetectFacesRequest request = 
				 DetectFacesRequest.builder()
				 .image(
						 Image.builder()
						 	.s3Object(
								 S3Object.builder()
								 .name(photo)
								 .bucket(getAmzBucket())
								 .build()
								 )
						 	.build()
				 )
				 .attributes(Attribute.ALL)
				 .build();
			// Replace Attribute.ALL with Attribute.DEFAULT to get default values.
		 try {         
			 DetectFacesResponse Response = rekognitionClient.detectFaces(request);         
			 return Response;
	      } catch (RekognitionException e) {
	    	  e.printStackTrace();      
	   
	      }
		 return null;
	}
	
	public DetectModerationLabelsResponse detectUnsafeContent(String photo) {
		RekognitionClient rekognitionClient = getRekognitionClient();
	      
	      DetectModerationLabelsRequest request = DetectModerationLabelsRequest.builder()
	    		  .image(
	    				  Image.builder()
	    				  .s3Object(
									 S3Object.builder()
									 .name(photo)
									 .bucket(getAmzBucket())
									 .build()
									 )
							 	.build()
					 )
	    		  .minConfidence(60F)
	    		  .build();
	      try
	      {
	           DetectModerationLabelsResponse Response = rekognitionClient.detectModerationLabels(request);
	           
	           return Response;

/*	           
 		List<ModerationLabel> labels = Response.getModerationLabels();
 		System.out.println("Detected labels for " + photo);
	           for (ModerationLabel label : labels)
	           {
	              System.out.println("Label: " + label.getName()
	               + "\n Confidence: " + label.getConfidence().toString() + "%"
	               + "\n Parent:" + label.getParentName());
	          }
*/	
	       }
	       catch (RekognitionException e)
	       {
	         e.printStackTrace();
	       }
	      return null;
	}

	public DetectLabelsResponse getLabels(String photo) {	

		RekognitionClient rekognitionClient = getRekognitionClient();

		DetectLabelsRequest request = DetectLabelsRequest.builder()
	    		  .image(
	    				  Image.builder()
	    				  .s3Object(
									 S3Object.builder()
									 .name(photo)
									 .bucket(getAmzBucket())
									 .build()
									 )
							 	.build()
					 )      
				.maxLabels(10)        
				.minConfidence(77F)
				.build();
		try {         

			DetectLabelsResponse Response = rekognitionClient.detectLabels(request);
			return Response;

		} catch(RekognitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public RecognizeCelebritiesResponse RecognizeCelebrities(String photo) {
		
		RekognitionClient rekognitionClient = getRekognitionClient();
		
		RecognizeCelebritiesRequest request = RecognizeCelebritiesRequest.builder()
	    		  .image(
	    				  Image.builder()
	    				  .s3Object(
									 S3Object.builder()
									 .name(photo)
									 .bucket(getAmzBucket())
									 .build()
									 )
							 	.build()
					 )
	    		  	.build();
		
		RecognizeCelebritiesResponse Response=rekognitionClient.recognizeCelebrities(request);

		return Response;
	}
	
	public String removeFaceFromCollection(String collectionId, String faceId) {
		
		String deletedFace = null;
		RekognitionClient rekognitionClient = getRekognitionClient();
		
		DeleteFacesRequest deleteFacesRequest = DeleteFacesRequest.builder()
				.faceIds(faceId)
				.collectionId(collectionId).build();
		
		DeleteFacesResponse Response = rekognitionClient.deleteFaces(deleteFacesRequest);
		
		for (String deletedFaceItem : Response.deletedFaces()){
			deletedFace = deletedFaceItem;
		}
		
		return deletedFace;
	}
	
	public String addFaceToCollection(String collectionId, String photo, String faceName) {
		
		String faceId = null;
		
		RekognitionClient rekognitionClient = getRekognitionClient();
		
		Image image =  Image.builder()
				.s3Object(
						S3Object.builder()
						.bucket(getAmzBucket())
						.name(photo)
						.build())
				.build(); 
		
		 IndexFacesRequest indexFacesRequest = IndexFacesRequest.builder()
				 .image(image)
				 .collectionId(collectionId)
				 .externalImageId(faceName)
				 .detectionAttributes(Attribute.ALL)
				 .build();
		 
		 IndexFacesResponse indexFacesResponse = rekognitionClient.indexFaces(indexFacesRequest);
		 
		System.out.println(photo + " added");      
		
		List < FaceRecord > faceRecords = indexFacesResponse.faceRecords();      
		
		for (FaceRecord faceRecord: faceRecords) {
			faceId = faceRecord.face().faceId();
			System.out.println("Face detected: Faceid is " +
					faceRecord.face().faceId());
			}
		
		 return faceId;

	}
	
	public int deleteFaceCollection(String collectionId) {
		
		RekognitionClient rekognitionClient = getRekognitionClient();
		
		 DeleteCollectionRequest request = DeleteCollectionRequest.builder()
				 .collectionId(collectionId)
				 .build();      
		 
		 DeleteCollectionResponse  Response = rekognitionClient.deleteCollection(request); 
		 
		 return Response.statusCode();
	}
	
	public String createFaceCollection(String collectionId) {
		RekognitionClient rekognitionClient = getRekognitionClient();
		
		System.out.println("Creating collection: " 
		+         collectionId);
		
		CreateCollectionRequest request = CreateCollectionRequest.builder()         
				.collectionId(collectionId)
				.build();     
		
		CreateCollectionResponse createCollectionResponse =  
				rekognitionClient.createCollection(request);
		
		System.out.println("Statis code: " +
				createCollectionResponse.statusCode() +
				" - CollectionArn : " +
				createCollectionResponse.collectionArn());
		
		return createCollectionResponse.collectionArn();
	}

	public void searchFace(String collectionId, InputStream inputStream ) {
		 Float threshold = 70F;
		 int maxFaces = 2;

		RekognitionClient rekognitionClient = getRekognitionClient();

		try {
			SdkBytes bytesImage = SdkBytes.fromByteArray(IoUtils.toByteArray(inputStream));
			
			
			Image image =  Image.builder()
					.bytes(bytesImage)
					.build(); 

			 SearchFacesByImageResponse searchFacesByImageResponse = searchFace(collectionId, threshold, maxFaces,
						rekognitionClient, image); 
				
				 System.out.println("Faces matching largest face in image  ");
				 List < FaceMatch > faceImageMatches = searchFacesByImageResponse.faceMatches();
				 for (FaceMatch face: faceImageMatches) {
					 System.out.println(face.face().toString());
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
		 
		RekognitionClient rekognitionClient = getRekognitionClient();
		
		Image image =  Image.builder()
	    				  .s3Object(
							 S3Object.builder()
							 .name(photo)
							 .bucket(getAmzBucket())
							 .build()
							 )
					 .build();
				
		 SearchFacesByImageResponse searchFacesByImageResponse = searchFace(collectionId, threshold, maxFaces,
				rekognitionClient, image); 
		
		 System.out.println("Faces matching largest face in image  " + photo);
		 List < FaceMatch > faceImageMatches = searchFacesByImageResponse.faceMatches();
		 for (FaceMatch face: faceImageMatches) {
			 System.out.println(face.face().toString());
			 System.out.println();
		 }
		 
	}


	private SearchFacesByImageResponse searchFace(String collectionId, Float threshold, int maxFaces,
			RekognitionClient rekognitionClient, Image image) {
		SearchFacesByImageRequest searchFacesByImageRequest = SearchFacesByImageRequest.builder()
				 .collectionId(collectionId)
				 .image(image)
				 .faceMatchThreshold(threshold)
				 .maxFaces(maxFaces)
				 .build();      
		 
		 SearchFacesByImageResponse searchFacesByImageResponse = rekognitionClient.searchFacesByImage(searchFacesByImageRequest);
		return searchFacesByImageResponse;
	}
	
	private RekognitionClient getRekognitionClient() {
		
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(getAmzAccessID(), getAmzAccessKey());

		RekognitionClient rekognitionClient = RekognitionClient.builder()
				.region(Region.of(getAmzRegion()))
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
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
		
		//DetectLabelsResponse ResponseLabel = rekService.getLabels("images/2016/10/20/lighthouse.jpg");
		
		//List <Label> labels = ResponseLabel.getLabels();
		
		//System.out.println("Detected labels for " + "images/2016/10/20/lighthouse.jpg");         
		//for (Label label: labels) {            
		//	System.out.println(label.getName() + ": " + label.getConfidence().toString());         
		//}
		
		
		//ResponseLabel = rekService.getLabels("images/2017/04/03/noe_0755_jpg_285432573.jpg"); //images/2017/05/22/img_3887.jpg");
		
		//System.out.println("Detected labels for " + "images/2017/04/03/noe_0755_jpg_285432573.jpg");         
		//for (Label label: labels) {            
		//	System.out.println(label.getName() + ": " + label.getConfidence().toString());         
		//}
		
		
		DetectFacesResponse ResponseFaces = rekService.detectFaces("images/2017/04/03/noe_0755_jpg_285432573.jpg");
 
		 List < FaceDetail > faceDetails = ResponseFaces.faceDetails();
		 
		 try {
			 float[] focalPoint = rekService.estimateFocalPoint(373, 554, ResponseFaces);
			System.out.println(focalPoint[0] + "," + focalPoint[1]);
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 
		 for (FaceDetail face: faceDetails) {
			 System.out.println("cara -->" + face.boundingBox().left() + "," + face.boundingBox().top());
			 
		 
		 }
		 
		 ResponseFaces = rekService.detectFaces( "images/2016/10/07/cr7.jpg");
		 
		faceDetails = ResponseFaces.faceDetails();
		 
		 try {
			 float[] focalPoint = rekService.estimateFocalPoint(620, 444, ResponseFaces);
			System.out.println(focalPoint[0] + "," + focalPoint[1]);
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 
		 for (FaceDetail face: faceDetails) {
			 System.out.println("cara -->" + face.boundingBox().left() + "," + face.boundingBox().top());
			 
		 
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
		RecognizeCelebritiesResponse Response = rekService.RecognizeCelebrities("images/2016/10/07/cr7.jpg");
		List<Celebrity> celebs=Response.getCelebrityFaces();
		
		for (Celebrity celebrity: celebs) {
            System.out.println("Celebrity recognized: " + celebrity.getName());
            System.out.println("Celebrity ID: " + celebrity.getId());
            BoundingBox boundingBox=celebrity.getFace().boundingBox();
            System.out.println("position: " +
               boundingBox.left().toString() + " " +
               boundingBox.top().toString());
            System.out.println("Further information (if available):");
            for (String url: celebrity.getUrls()){
               System.out.println(url);
            }
            System.out.println();
         }
         System.out.println(Response.getUnrecognizedFaces().size() + " face(s) were unrecognized.");

         
         DetectModerationLabelsResponse ResponseUnsafeContent = rekService.detectUnsafeContent("images/2016/10/07/cr7.jpg");
         List<ModerationLabel> labelsUnsafe = ResponseUnsafeContent.getModerationLabels();
  		
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


