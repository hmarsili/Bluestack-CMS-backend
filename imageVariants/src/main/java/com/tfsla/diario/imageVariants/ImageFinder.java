package com.tfsla.diario.imageVariants;

import java.util.List;
import java.util.Locale;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.loader.CmsExternalImageScaler;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;



public class ImageFinder {
	
	protected static final Log logger = CmsLog.getLog(ImageFinder.class);

	
	protected List<Image> getImagesInBody(CmsResource resource, CmsObject cmsObject) {
		List<Image> images = new ArrayList<Image>();
		
		try {
			
			List<CmsRelation> relations = cmsObject.getRelationsForResource(resource, CmsRelationFilter.ALL);
		
	       	for ( CmsRelation relation : relations) {

	       		String rel1 = relation.getSourcePath();
   				String rel2 = relation.getTargetPath();

   				String rel = "";
   				if (rel1.equals(resource.getRootPath()))
   					rel = rel2;
   				else
   					rel = rel1;
   				
   				
       			logger.debug(rel);
       			logger.debug(relation.getType().getName());
	       		CmsResource resourceRelation = cmsObject.readResource(cmsObject.getRequestContext().removeSiteRoot(rel), CmsResourceFilter.IGNORE_EXPIRATION);
				
				String typeRelation = OpenCms.getResourceManager().getResourceType(resourceRelation).getTypeName();
				logger.debug(typeRelation);
				logger.debug("----------------------------");
       			
				
				if( typeRelation.indexOf("external-image") == 0 && relation.getType().getName().equals("IMG") ){
					
					CmsProperty prop = cmsObject.readPropertyObject(resourceRelation, "image.focalPoint",false);
					String focalPoint =prop.getValue("");
					
					Image img = new Image(resourceRelation,focalPoint);
					
					images.add(img);
				}
				
				
   			}
	       		       	
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return images;
	}
	
	protected CmsXmlContent getNewsContent(CmsResource resource, CmsObject cmsObject) {
		
		CmsFile file;
		try {
			file = cmsObject.readFile(resource);
			CmsXmlContent  content = CmsXmlContentFactory.unmarshal(cmsObject, file);

			return content;
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	protected String getFocalPoint(CmsXmlContent  content, CmsObject cmsObject, String pathXml, CmsResource res) {
		I_CmsXmlContentValue value = content.getValue(pathXml+ "focalPoint", Locale.ENGLISH);
		String focalPoint = value.getStringValue(cmsObject);
		
		if (focalPoint==null || focalPoint.length()==0 || focalPoint.trim().equals("")) {
			CmsProperty prop;
			try {
				prop = cmsObject.readPropertyObject(res, "image.focalPoint",false);
				focalPoint =prop.getValue("");
			} catch (CmsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				focalPoint ="";
			}
			
		}
		
		return focalPoint;
		
	}
	
	protected List<Image> getImagesInNotaLista(CmsXmlContent  content, CmsObject cmsObject) {
		List<Image> images = new ArrayList<Image>();
			int lastElement =  content.getIndexCount("noticiaLista", Locale.ENGLISH);
			
			for (int idx = 1;idx<=lastElement;idx++) {
				String xmlPath = "noticiaLista[" + idx + "]/imagenlista/";
				I_CmsXmlContentValue value = content.getValue(xmlPath + "imagen", Locale.ENGLISH);
				String imgPath = value.getStringValue(cmsObject);
				
				if (imgPath!=null && imgPath.length()>0) {
					CmsResource resourceImage;
					try {
						resourceImage = cmsObject.readResource(cmsObject.getRequestContext().removeSiteRoot(imgPath), CmsResourceFilter.IGNORE_EXPIRATION);
						String focalPoint = getFocalPoint(content,cmsObject,xmlPath, resourceImage);
						
						Image img = new Image(resourceImage,focalPoint);
						images.add(img);
					} catch (CmsException e) {
						logger.error("Error obteniendo imagen",e);
					}
				}
			
				
			}
			           		
		return images;
       
	}
	
	protected List<Image> getImagesInFotogaleria(CmsXmlContent  content, CmsObject cmsObject) {
		List<Image> images = new ArrayList<Image>();
			int lastElement =  content.getIndexCount("imagenesFotogaleria", Locale.ENGLISH);
			
			for (int idx = 1;idx<=lastElement;idx++) {
				String xmlPath = "imagenesFotogaleria[" + idx + "]/";
				I_CmsXmlContentValue value = content.getValue(xmlPath + "imagen", Locale.ENGLISH);
				String imgPath = value.getStringValue(cmsObject);
				
				if (imgPath!=null && imgPath.length()>0) {
					CmsResource resourceImage;
					try {
						resourceImage = cmsObject.readResource(cmsObject.getRequestContext().removeSiteRoot(imgPath), CmsResourceFilter.IGNORE_EXPIRATION);
						String focalPoint = getFocalPoint(content,cmsObject,xmlPath, resourceImage);
						
						Image img = new Image(resourceImage,focalPoint);
						images.add(img);
					} catch (CmsException e) {
						logger.error("Error obteniendo imagen",e);
					}
				}
				
			}
			           		
		return images;
       
	}
	
	protected List<Image> getImagesInPersonalizada(CmsXmlContent  content, CmsObject cmsObject) {
		List<Image> images = new ArrayList<Image>();
			int lastElement =  content.getIndexCount("imagenPersonalizada", Locale.ENGLISH);
			
			for (int idx = 1;idx<=lastElement;idx++) {
				String xmlPath = "imagenPersonalizada[" + idx + "]/";
				I_CmsXmlContentValue value = content.getValue(xmlPath + "imagen", Locale.ENGLISH);
				String imgPath = value.getStringValue(cmsObject);
				
				if (imgPath!=null && imgPath.length()>0) {
					
					CmsResource resourceImage;
					try {
						resourceImage = cmsObject.readResource(cmsObject.getRequestContext().removeSiteRoot(imgPath), CmsResourceFilter.IGNORE_EXPIRATION);
						String focalPoint = getFocalPoint(content,cmsObject,xmlPath, resourceImage);
						
						Image img = new Image(resourceImage,focalPoint);
						images.add(img);
					} catch (CmsException e) {
						logger.error("Error obteniendo imagen",e);
					}
				}
				
			}
			           		
		return images;
       
	}
		
	protected List<Image> getImagesInPrevisualizacion(CmsXmlContent  content, CmsObject cmsObject) {
		List<Image> images = new ArrayList<Image>();
			int lastElement =  content.getIndexCount("imagenPrevisualizacion", Locale.ENGLISH);
			
			for (int idx = 1;idx<=lastElement;idx++) {
				String xmlPath = "imagenPrevisualizacion[" + idx + "]/";
				I_CmsXmlContentValue value = content.getValue(xmlPath + "imagen", Locale.ENGLISH);
				String imgPath = value.getStringValue(cmsObject);
				
				if (imgPath!=null && imgPath.length()>0) {
					
					CmsResource resourceImage;
					try {
						resourceImage = cmsObject.readResource(cmsObject.getRequestContext().removeSiteRoot(imgPath), CmsResourceFilter.IGNORE_EXPIRATION);
						String focalPoint = getFocalPoint(content,cmsObject,xmlPath, resourceImage);
						
						Image img = new Image(resourceImage,focalPoint);
						images.add(img);
					} catch (CmsException e) {
						logger.error("Error obteniendo imagen",e);
					}
				}
				
			}
			           		
		return images;
       
	}
	
	
	public void publishImages(CmsResource resource, CmsObject cmsObject, Boolean force) throws Exception {
		
		
		logger.debug("Comienza verificación del recurso " + resource.getRootPath() + " para publicacion de variantes");
		
		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion currentPublication = tService.obtenerTipoEdicion(cmsObject,resource.getRootPath());
		
		String siteName = OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot();
		
		logger.debug(resource.getRootPath() + "> En sitio: " + siteName + " | proyecto: " + cmsObject.getRequestContext().currentProject().getName() + " | publicacion: " + (currentPublication != null ? currentPublication.getNombre() : "No encontrada"));

		
		
		String publication = "" + currentPublication.getId();
		
		VariantsConfiguration conf = new VariantsConfiguration(siteName, publication);
		
		
		logger.debug("Se verifica que el recurso " + resource.getRootPath() + " corresponde a una publicación donde el modulo de exportacion de imagenes esta " + (conf.isModuleEnabled() ? "habilitado" : "deshabilitado"));
		
		//Si el modulo esta deshabilitado para la publicacion. no proseguir.
		if (!conf.isModuleEnabled())
			return;
		

		
		Long dateLong = resource.getDateCreated();
        Date dateCreated = new Date(dateLong);
        
		Date startDate = conf.getDateStart();
	
		//Si la fecha de la noticia es posteior 
		if (dateCreated.before(startDate))
			return;
	
		logger.debug("El recurso " + resource.getRootPath() + " corresponde a una publicación de fecha posterior al limite en la exportacion");
		
		List<Image> images = getImagesInBody(resource, cmsObject);
		String imagePosition = "body";
		sendImages(cmsObject, images, imagePosition, conf, force);
		
		CmsXmlContent content = getNewsContent(resource, cmsObject);
		images = getImagesInPrevisualizacion(content, cmsObject);
		imagePosition = "previsualizacion";
		sendImages(cmsObject, images, imagePosition, conf, force);
		
		images = getImagesInPersonalizada(content, cmsObject);
		imagePosition = "personalizada";
		sendImages(cmsObject, images, imagePosition, conf, force);
		
		images = getImagesInFotogaleria(content, cmsObject);
		imagePosition = "fotogaleria";
		sendImages(cmsObject, images, imagePosition, conf, force);

		images = getImagesInNotaLista(content, cmsObject);
		imagePosition = "notalista";
		sendImages(cmsObject, images, imagePosition, conf, force);
		
		/*
		 
		 {
    "force": "true",
    "publication": "1",
    "image": "/2023/02/02/Fronalpstock_big.jpg",
    "formats": ["2000x2000&t:3&p:9&fpx:700&fpy:500","autox1000&t:3&p:9&fpx:700&fpy:500","1024x1024&t:3&p:9&fpx:700&fpy:500"],
    "types" : ["webp","avif"]
}
		 
		 
		 */

	
	}
	
	public void publishImages(String newsPath, CmsObject cmsObject, Boolean force) throws Exception {
	
		CmsResource resource = cmsObject.readResource(cmsObject.getRequestContext().removeSiteRoot(newsPath));
		publishImages(resource, cmsObject, force);
	}

	private void sendImages(CmsObject cmsObject, List<Image> images, String imagePosition, VariantsConfiguration conf, Boolean force) {
		
		JSONArray arrayjsonformats = new JSONArray();
		List<String> formats = conf.getFormats(imagePosition);
		for (String format : formats) {
			arrayjsonformats.add(format);
		}
		
		
		List<String> variants = conf.getVariant(imagePosition);
		if (variants.size()>0) {
		
			for (Image image : images ) {
		
				try {
					logger.debug("Se solicita las variantes para la imagen " + image.getRes().getRootPath());
					
					sendImage(cmsObject, force, conf, arrayjsonformats, variants, image);
				} catch (CmsException e) {
					logger.error("Error enviando la imagen ",e);
				}
	
			}
			
		}
	}
	
	
	 public String toVariantString(String width, String height, String type, String position, String color, String focalpoint) {
	    	StringBuffer result = new StringBuffer(128);
	        
	        result.append(width.equals("") ? "auto" : width);
	        result.append('x');
	        result.append(height.equals("") ? "auto" : height);
	        
	        StringBuffer params = new StringBuffer(128);
	        
	        if (!type.equals("")) {
	        	params.append('-');
	        	params.append(CmsExternalImageScaler.SCALE_PARAM_TYPE);
	        	params.append('_');
	        	params.append(type);
	        }
	        
	        if (!type.equals("3")) {
		        if (focalpoint!=null && focalpoint.length()>0) {
		        	params.append('-');
		        	params.append(CmsExternalImageScaler.SCALE_PARAM_POS);
		        	params.append('_');
		        	params.append('9');
		        }
		        else if (!position.equals("")) {
		        	params.append('-');
		        	params.append(CmsExternalImageScaler.SCALE_PARAM_POS);
		        	params.append('_');
		        	params.append(position);
		        }
	        }
	        
	        if (!color.equals("")) {
	        	params.append('-');
	        	params.append(CmsExternalImageScaler.SCALE_PARAM_COLOR);
	        	params.append('_');
	        	params.append(color.replace("#",""));
	        }
	        
	        if (!type.equals("3") && !focalpoint.equals("")) {
	        	params.append('-');
	        	params.append(focalpoint);
	        }
	        
	        String paramsStr = params.toString();
	        if (paramsStr.startsWith("-")) {
	        	paramsStr = "/" + paramsStr.substring(1);
	        }
	        
	        
	        return result.toString() + paramsStr;
	    }


	 
	private boolean mustUseModule(CmsObject cmsObject, CmsResource newsFile, VariantsConfiguration conf) {
		if (!cmsObject.getRequestContext().currentProject().isOnlineProject())
			return false;
		
		if (!conf.isModuleEnabled())
			return false;
		
		if (conf.getDateStart().after(new Date(newsFile.getDateCreated())))
			return false;
		
		return true;
	} 
		 
	private VariantsConfiguration getModuleConfiguration(CmsObject cmsObject, CmsResource newsFile) throws Exception {
		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion currentPublication = tService.obtenerTipoEdicion(cmsObject,cmsObject.getSitePath(newsFile));
		
		String siteName = OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot();
		String publication = "" + currentPublication.getId();
		
		VariantsConfiguration conf = new VariantsConfiguration(siteName, publication);
		return conf;
	}

	private String getImageFormatBody(CmsObject cmsObject, CmsResource image, int maxWidth ) throws CmsException {
		String formatSize = "";
		String width="";
		
		CmsProperty prop = cmsObject.readPropertyObject(image, "image.size", false);      
		String imageSize = (prop != null) ? prop.getValue() : null;
		if(imageSize != null){
		    String[] ImgSize = imageSize.split(",");
		    
		    if(ImgSize[0] != null) {
		       String[] w = ImgSize[0].split(":");
		            width = w[1];
		    }
		    
		    if (Integer.parseInt(width)>maxWidth) {
		    	formatSize = maxWidth + "xauto";
		    }
		    else {
		    	//small
		    	String height = "";
			    
			    formatSize = "small";
		    }
		}
		
		formatSize += "/t_3";
		    
		return formatSize;
	}
	
	public String getImageVariantPathBody(CmsObject cmsObject, CmsResource newsFile, String pathImage, int maxWidth)  throws Exception {
		VariantsConfiguration conf = getModuleConfiguration(cmsObject, newsFile);
		
		
		if (!mustUseModule(cmsObject, newsFile, conf))
			return "";
		

		if (pathImage.indexOf("/img") > 0) {
			if (pathImage.matches(".*.[a-z]*_[0-9]*.[a-z]*"))
				pathImage = pathImage.substring(pathImage.indexOf("/img"), pathImage.lastIndexOf("_"));
			else 
				pathImage = pathImage.substring(pathImage.indexOf("/img"));
		}
		
		String removePrefix = conf.getPrefixToRemove();
		
		CmsFile file = cmsObject.readFile(pathImage);
		
		
		
		String variantConf=getImageFormatBody(cmsObject,file,maxWidth);
		
		String s3Path = getS3Path(variantConf, removePrefix, file);
		
		return s3Path;
		
	}

	 
	public String getImageVariantPath(CmsObject cmsObject, CmsResource newsFile, CmsResource imageRes, String variantConf) throws Exception {
	
		VariantsConfiguration conf = getModuleConfiguration(cmsObject, newsFile);
		
		
		if (!mustUseModule(cmsObject, newsFile, conf))
			return "";
			
		String removePrefix = conf.getPrefixToRemove();
		
		CmsFile file = cmsObject.readFile(imageRes);
		String s3Path = getS3Path(variantConf, removePrefix, file);
		
		return s3Path;
	}

	private String getS3Path(String variantConf, String removePrefix, CmsFile file) {
		String data = new String(file.getContents());
		
		int idxFileExtension = data.lastIndexOf(".");
		String extension = data.substring(idxFileExtension+1);
		
		int index = data.indexOf(".com/")+5;
		String s3Path = "/images-formats/" + variantConf + "/" + extension + data.substring(index).replaceFirst(removePrefix,"");
		return s3Path;
	}
	
	private void sendImage(CmsObject cmsObject, Boolean force, VariantsConfiguration conf, JSONArray arrayjsonformats, List<String> variants, Image image) throws CmsException {

		
		String endpoint = conf.getEndPointGenerateVariants();
		String removePrefix = conf.getPrefixToRemove();
		
		CmsFile file = cmsObject.readFile(image.getRes());
		String data = new String(file.getContents());
		int index = data.indexOf(".com/")+5;
		String s3Path = data.substring(index).replaceFirst(removePrefix,"");
		
		
		 //fpx:440,fpy:659
		//&fpx:700&fpy:500
		
		
			
		List<String> variantsfixed = new ArrayList<>();
		
		String _focalPoint="";
		if (!image.getFocalPoint().equals("")) {
			_focalPoint = "-" + image.getFocalPoint().replace(",","-").replace(":", "_");
			
			for (String variant : variants) {
				if (variant.indexOf("-t_3")==-1) { //Si el escalado es tipo 3 (no croppea) entonces no se pone focalpoint
					int idxPos = variant.indexOf("-p_");
					if (idxPos>-1) {
						if (variant.indexOf("-p_9")>-1)
							variant = variant.replaceFirst("-p_\\d", "-p_9") + _focalPoint;
					} else {
						int idxType = variant.indexOf("-t_");
						if (idxType>-1) {
							variant = variant.substring(0,idxType+4) + "-p_9" + variant.substring(idxType+4) + _focalPoint;
						}
					}
				}
				variantsfixed.add(variant);
			}
		}
		else {
			for (String variant : variants) 
				variantsfixed.add(variant);
		}
	
		new Thread(new Runnable() {
		     @Override
		     public void run() {
		    	 try {
		    		 synchronized (this) {
		    			 HttpClient httpClient = new HttpClient();
		    			 
		    			 
		    			 JSONArray arrayjsonvariants = new JSONArray();
		    				for (String variant : variantsfixed) {
		    					arrayjsonvariants.add(variant);
		    				}
		    				
		    				JSONObject jsonbody = new JSONObject();
		    				jsonbody.put("force", force.toString());
		    				jsonbody.put("publication", conf.getPubId());
		    				jsonbody.put("image", s3Path);
		    				jsonbody.put("formats", arrayjsonvariants);
		    				jsonbody.put("types", arrayjsonformats);
		    				
		    				String sBody = jsonbody.toString(2);

		    				logger.debug("Sending to " + endpoint + " --> " + sBody);

		    				StringRequestEntity requestEntity = new StringRequestEntity(
		    						sBody,
		    					    "application/json",
		    					    "UTF-8");

		    				
		    				PostMethod postMethod = new PostMethod(endpoint);
		    				postMethod.setRequestEntity(requestEntity);

		    				int statusCode = httpClient.executeMethod(postMethod);
		    				if (statusCode == HttpStatus.SC_OK) {
		    	                String response = postMethod.getResponseBodyAsString();
		    	                logger.debug(response);
		    	            }
		    	            

		    		 }
				} catch (IOException e) {
					logger.error("Error",e );
				}
		     }
		}).start();
	}

	public void unpublishImage(String siteName, String publication, String imageVfsPath, CmsObject cmsObject) {
		VariantsConfiguration conf = new VariantsConfiguration(siteName, publication);
		
		//Si el modulo esta deshabilitado para la publicacion. no proseguir.
		if (!conf.isModuleEnabled())
			return;
		
		
		String endpoint = conf.getEndPointDeleteVariants();
		String removePrefix = conf.getPrefixToRemove();
		
		CmsFile file;
		try {
			file = cmsObject.readFile(cmsObject.getRequestContext().removeSiteRoot(imageVfsPath), CmsResourceFilter.IGNORE_EXPIRATION);
		
			String data = new String(file.getContents());
			int index = data.indexOf(".com/")+5;
			String s3Path = data.substring(index).replaceFirst(removePrefix,"");
			
			
			JSONObject jsonbody = new JSONObject();
			jsonbody.put("image", s3Path);
			jsonbody.put("publication", conf.getPubId());
			
			HttpClient httpClient = new HttpClient();
			String sBody = jsonbody.toString(2);

			logger.debug("Sending to " + endpoint + " --> " + sBody);

			StringRequestEntity requestEntity = new StringRequestEntity(
					sBody,
				    "application/json",
				    "UTF-8");

			
			PostMethod postMethod = new PostMethod(endpoint);
			postMethod.setRequestEntity(requestEntity);

			int statusCode = httpClient.executeMethod(postMethod);
			if (statusCode == HttpStatus.SC_OK) {
                String response = postMethod.getResponseBodyAsString();
                logger.debug(response);
            }
			
		/*
		{
		    "image":"2023/02/02/Fronalpstock_big.jpg",
		    "publication":"1" 
		}
		
		*/
		
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

