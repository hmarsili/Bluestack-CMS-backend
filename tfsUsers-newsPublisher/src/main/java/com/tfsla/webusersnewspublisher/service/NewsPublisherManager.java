package com.tfsla.webusersnewspublisher.service;

import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.webusersnewspublisher.helper.expirenews.ExpireNewsService;
import com.tfsla.webusersnewspublisher.helper.expirenews.ExpireNewsValidator;
import com.tfsla.webusersnewspublisher.helper.expirenews.Strings;
import com.tfsla.webusersnewspublisher.model.News;
import com.tfsla.webusersposts.service.PostsMailingService;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentValueSequence;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import java.util.*;

public class NewsPublisherManager{
	
	String SITE = "";
	String PUBLICATION = "";
	private CmsObject cmsObject = null;
	private CmsProject currentProject = null;
	private HttpServletRequest request = null;
	private CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
	private Log LOG = CmsLog.getLog(this);
	List<CmsResource> publishList = new ArrayList<CmsResource>();
	
	public NewsPublisherManager(HttpServletRequest request) throws CmsException {      
		try {
			this.request = request;
			CmsObject _cmsObject =CmsWorkplaceAction.getInstance().getCmsAdminObject();
			cmsObject = OpenCms.initCmsObject(_cmsObject);
            org.opencms.file.CmsProject offProject = cmsObject.readProject(Strings.OFFLINE);
            currentProject = cmsObject.getRequestContext().currentProject();
            cmsObject.getRequestContext().setCurrentProject(offProject);
		} catch (CmsException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private void publishList(List<CmsResource> list) throws Exception, CmsException {
		try {
			OpenCms.getPublishManager().publishProject(cmsObject, 
					new CmsLogReport(Locale.getDefault(), this.getClass()),
						OpenCms.getPublishManager().getPublishList(cmsObject, list, false));
			/*
			HttpRequest request = new HttpRequest();
			request.setUrl(OpenCms.getSiteManager().getWorkplaceServer() + "/purgecache.html");
			request.sendRequest();
			*/
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public Map<String, Object> getParamsForm() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
	
		try {
			//FileItemFactory factory = new DiskFileItemFactory();
			//ServletFileUpload upload = new ServletFileUpload(factory);
			//upload.setHeaderEncoding("UTF-8");
			Collection<Part> items = request.getParts();
	    	//List items = upload.parseRequest(request);
	    	
	    	
			Iterator itr = items.iterator();
			List<Part> images = new ArrayList<Part>();
			List<String> imagesBase64 = new ArrayList<String>();
			List<String> imagesBase64Names = new ArrayList<String>();
			List<String> videos = new ArrayList<String>();
			List<String> fuentes = new ArrayList<String>();
			List<String> categories = new ArrayList<String>();
			String fileName = "";
			
			Iterator<String> paramsName = request.getParameterNames().asIterator();
			while (paramsName.hasNext()) {
				String atrName = paramsName.next();
				
				if (atrName.indexOf("video") >= 0) {
					if (atrName != null && atrName.indexOf("<script") < 0 && atrName.indexOf("</script>") < 0 && !atrName.equals("")) {
						videos.add(request.getParameter(atrName));
					}
				} else if (atrName.indexOf("fuente") >= 0) {
					fuentes.add(request.getParameter(atrName));
				} else if (atrName.indexOf("imagen.data64") >= 0) {
					imagesBase64.add(request.getParameter(atrName));
				} else if (atrName.indexOf("imagen.name") >= 0) {
					imagesBase64Names.add(request.getParameter(atrName));
				} else if (atrName.indexOf("categoria") >= 0) {
					if (request.getParameter(atrName) != null && !request.getParameter(atrName).equals("")) {
						categories.add(request.getParameter(atrName));	
					}
				} else {
					map.put(atrName, request.getParameter(atrName));
				}

			}
			
			while (itr.hasNext()) {
				Part item = (Part)itr.next();
				fileName = cmsObject.getRequestContext().getFileTranslator().translateResource(item.getSubmittedFileName());
				if (fileName != null && !fileName.equals("")) {
					images.add(item);
				}
				
				/*
				if (item.isFormField()) {
					if (item.getFieldName().indexOf("video") >= 0) {
						String codigo = item.getFieldName();
						if (codigo != null && codigo.indexOf("<script") < 0 && codigo.indexOf("</script>") < 0 && !codigo.equals("")) {
							videos.add(item.getString());
						}
					} else if (item.getFieldName().indexOf("fuente") >= 0) {
						fuentes.add(item.getString());
					} else if (item.getFieldName().indexOf("imagen.data64") >= 0) {
						imagesBase64.add(item.getString());
					} else if (item.getFieldName().indexOf("imagen.name") >= 0) {
						imagesBase64Names.add(item.getString());
					} else if (item.getFieldName().indexOf("categoria") >= 0) {
						if (item.getString() != null && !item.getString().equals("")) {
							categories.add(item.getString());	
						}
					} else {
						map.put(item.getFieldName(), item);
					}
				} else {
					fileName = cmsObject.getRequestContext().getFileTranslator().translateResource(item.getName());
					if (fileName != null && !fileName.equals("")) {
						images.add(item);
					}
				}
				*/
		
				map.put("images", images);
				map.put("images64.data", imagesBase64);
				map.put("images64.names", imagesBase64Names);
				map.put("videos", videos);
				map.put("fuentes", fuentes);
				map.put("categories", categories);
	    	}
        } catch (Exception e) {
        	e.printStackTrace();
    		throw e;
    	}
		return map;
	}
	
	public void publish(News news, String newsName) throws Exception {
		try {
			news.setCmsObject(cmsObject);
			news.setSite(SITE);
			news.publish(newsName);
			if(!news.getIsPreview())
				publishList(news.getPublishList());
		} catch(Exception ex) {
			throw ex;
		}
	}
	
	public Boolean expire(String newsName, String userName) throws Exception {
		String deleteMode = config.getParam(SITE, PUBLICATION, Strings.NEWSPUBLISHER_MODULE, Strings.DELETE_USER_POSTS);
		String pageBuilder = config.getParam(SITE, PUBLICATION, Strings.NEWSPUBLISHER_MODULE, Strings.PAGE_BUILDER);
		
		if(deleteMode == null || deleteMode.equals(Strings.DISABLED)) {
			return false;
		}
		
		String resourcePath = SITE + newsName;
		String siteRoot = cmsObject.getRequestContext().getSiteRoot();
		
		try {
			cmsObject.getRequestContext().setSiteRoot(Strings.SITE_ROOT);
			ExpireNewsValidator validator = new ExpireNewsValidator();
			
			if(validator.validate(deleteMode, cmsObject, resourcePath, pageBuilder)) {
				cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject(Strings.OFFLINE));
				ExpireNewsService service = new ExpireNewsService();
				service.expire(cmsObject, resourcePath, userName);
				return true;
			}
			
			String mailsConfig = config.getParam(SITE, PUBLICATION, Strings.NEWSPUBLISHER_MODULE, Strings.DELETE_POSTS_NOTIFICATIONS);
			if(mailsConfig != null && !mailsConfig.equals("")) {
				String[] mails = mailsConfig.split(",");
				PostsMailingService.requestDelete(mails, resourcePath, cmsObject, SITE, PUBLICATION, userName);
			}
			return false;
		} catch(Exception ex) {
			LOG.error("Error al expirar nota " + newsName, ex);
			throw ex;
		} finally {
			cmsObject.getRequestContext().setSiteRoot(siteRoot);
			cmsObject.getRequestContext().setCurrentProject(currentProject);
		}
	}
	
	public void delete(String newsName)  throws Exception {
		try {
			News news = new News();
			news.setCmsObject(cmsObject);
			news.setSite(SITE);
			news.delete(newsName);
			OpenCms.getPublishManager().publishResource(cmsObject, newsName);
		} catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}		
	}
	
	public void setSite(String site) {
		this.SITE = site;
	}
	
	public void setPublication(String publication) {
		this.PUBLICATION = publication;
	}
	
	public void moveNews(String PathNews) throws Exception {
		try {
			if ( PathNews != null ) {
				
				News news = new News();
				news.setCmsObject(cmsObject);
				news.setSite(SITE);
				
				String array[] = PathNews.split("/noticia_");
				String folder = array[0];
				String newFolder = folder.replace("/posts/tmp", "");
								
				if (!cmsObject.existsResource(newFolder))
					createFolderForFile(PathNews, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
								
				String newName = news.getNewsName(newFolder);
								
				lockResource(PathNews);
				cmsObject.moveResource(PathNews, newName);
				
				updateImagePath(newName);
								
				//OpenCms.getPublishManager().publishResource(cmsObject, newName);
				publishList.add(cmsObject.readResource(newName));
				news.setPath(newName);
				
				//publica todas las carpetas y archivos
				publishList(publishList);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}		
	}
	
	public void deleteTmp(String path) {
		 try {
			if (!cmsObject.getLock(path).isUnlocked()) {
                 cmsObject.unlockResource(path);
	        }
	        cmsObject.lockResource(path);
			 
			CmsFile file = cmsObject.readFile(path);
			CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cmsObject, file);
				
			List<Locale> locales = xmlContent.getLocales();
			if (locales.size() == 0) {
				locales = OpenCms.getLocaleManager().getDefaultLocales(cmsObject,path);
			}

			Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(
				CmsLocaleManager.getLocale(""),
			 	OpenCms.getLocaleManager().getDefaultLocales(cmsObject, path),
			 	locales
			);
			
			if(xmlContent.hasValue("imagenPrevisualizacion", locale, 0)) {
				String imgPath = xmlContent.getValue("imagenPrevisualizacion/imagen",locale).getStringValue(cmsObject);
		   
				if(!imgPath.equals("")){
					if (!cmsObject.getLock(imgPath).isUnlocked()) {
						 cmsObject.unlockResource(imgPath);
					}
					cmsObject.lockResource(imgPath);
					cmsObject.deleteResource(imgPath, CmsResource.DELETE_PRESERVE_SIBLINGS);
					 
					try {
						OpenCms.getPublishManager().publishResource(cmsObject, imgPath);
					} catch (Exception e) {
						OpenCms.getLog(cmsObject).error("No se pudo eliminar el recurso temporal: " + imgPath);
						e.printStackTrace();
					}
				}
			}
			
			if(xmlContent.hasValue("imagenesFotogaleria", locale, 0)) {
				CmsXmlContentValueSequence elementSequence = xmlContent.getValueSequence("imagenesFotogaleria", locale);
	            int elementCount = elementSequence.getElementCount();
	            
	            for (int j = 2; j < elementCount + 1; j++) {
	            	String imgPath = xmlContent.getValue("imagenesFotogaleria[" + j + "]/imagen",locale).getStringValue(cmsObject);
	            	if(imgPath != ""){
	            		if (!cmsObject.getLock(imgPath).isUnlocked()){
						   cmsObject.unlockResource(imgPath);
					    }
					    cmsObject.lockResource(imgPath);
					    cmsObject.deleteResource(imgPath, CmsResource.DELETE_PRESERVE_SIBLINGS);
	            	
					    try {
							OpenCms.getPublishManager().publishResource(cmsObject, imgPath);
						} catch (Exception e) {
							OpenCms.getLog(cmsObject).error("No se pudo eliminar el recurso temporal: "+imgPath);
							e.printStackTrace();
						}
	            	}
	            }
			}
			cmsObject.deleteResource(path, CmsResource.DELETE_PRESERVE_SIBLINGS);
		} catch (CmsException e) {
			e.printStackTrace();
		}
	}
	
	private void updateImagePath(String ad) throws Exception {
		try {
			String imgOldPath = "";
			String imgNewPath = "";
			
			CmsFile file = cmsObject.readFile(ad);
			CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cmsObject, file);
			
			List<Locale> locales = xmlContent.getLocales();
			if (locales.size() == 0) {
				locales = OpenCms.getLocaleManager().getDefaultLocales(cmsObject,ad);
			}

			Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(CmsLocaleManager.getLocale(""),
				OpenCms.getLocaleManager().getDefaultLocales(cmsObject, ad),locales);
			
			//muevo y cambio el path de imagenPrevisualizacion
			if(xmlContent.hasValue("imagenPrevisualizacion", locale, 0)) {
				imgOldPath = xmlContent.getValue("imagenPrevisualizacion/imagen",locale).getStringValue(cmsObject);
				imgNewPath = imgOldPath.replace("/posts/tmp", "");
				xmlContent.getValue("imagenPrevisualizacion/imagen",locale).setStringValue(cmsObject, imgNewPath);
				xmlContent.getValue("imagenesFotogaleria[1]/imagen",locale).setStringValue(cmsObject, imgNewPath);
				
				lockResource(imgOldPath);
				
				if (!cmsObject.existsResource(imgNewPath))
					createFolderForFile(imgOldPath, 8);
										
				cmsObject.moveResource(imgOldPath, imgNewPath);
				publishList.add(cmsObject.readResource(imgNewPath));
			}
			
			if(xmlContent.hasValue("imagenesFotogaleria", locale, 0)) {
				CmsXmlContentValueSequence elementSequence = xmlContent.getValueSequence("imagenesFotogaleria", locale);
	            int elementCount = elementSequence.getElementCount();
	            
	            //muevo y cambio el path de las imagenesFotogaleria (menos la primera que ya la cambié arriba)
	            for (int j = 2; j < elementCount + 1; j++) {
	                imgOldPath = xmlContent.getValue("imagenesFotogaleria[" + j + "]/imagen",locale).getStringValue(cmsObject);
					imgNewPath = imgOldPath.replace("/posts/tmp", "");
					xmlContent.getValue("imagenesFotogaleria[" + j + "]/imagen",locale).setStringValue(cmsObject, imgNewPath);
					
					if(imgOldPath!=""){
						lockResource(imgOldPath);
						
						if (!cmsObject.existsResource(imgNewPath))
							createFolderForFile(imgOldPath, 8);
						
						cmsObject.moveResource(imgOldPath, imgNewPath);
						publishList.add(cmsObject.readResource(imgNewPath));  
					}
	            }
			}
		} catch (CmsException ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	private void lockResource(String resource) throws CmsException {
		if (cmsObject.getLock(resource).isUnlocked()) {
			cmsObject.lockResource(resource);
		} else {
        	try {
        		cmsObject.unlockResource(resource);
        		cmsObject.lockResource(resource);
        	} catch (Exception e) {
        		cmsObject.changeLock(resource);	            		
        	}
        }
	}
	
	private void createFolderForFile(String file, int resourceTypeID) throws Exception {
		// /sites/generic/posts/tmp/img/2012/10/24/pepe.jpg
		// /sites/generic/posts/tmp/contenidos/2012/10/24/ad_0001.html
		try {
			String[] array = file.split("/");
			String year = array[6];
			String month = array[7];
			String day = array[8];
			String folderName =  SITE + "/" + array[5] + "/" + year;
			
			if (!cmsObject.existsResource(folderName)) {
				cmsObject.createResource(folderName, resourceTypeID);
				CmsResourceUtils.unlockResource(cmsObject, folderName, false);
				//OpenCms.getPublishManager().publishResource(cmsObject, folderName);
				publishList.add(cmsObject.readResource(folderName));
			}
			
			folderName = folderName + "/" + month;
	
			if (!cmsObject.existsResource(folderName)) {
				cmsObject.createResource(folderName, resourceTypeID);
				CmsResourceUtils.unlockResource(cmsObject, folderName, false);
				//OpenCms.getPublishManager().publishResource(cmsObject, folderName);
				publishList.add(cmsObject.readResource(folderName));
			}
			
			folderName = folderName + "/" + day;
	
			if (!cmsObject.existsResource(folderName)) {
				cmsObject.createResource(folderName, resourceTypeID);
				CmsResourceUtils.unlockResource(cmsObject, folderName, false);
				//OpenCms.getPublishManager().publishResource(cmsObject, folderName);
				publishList.add(cmsObject.readResource(folderName));
			}
		} catch (Exception e) {
			e.printStackTrace();
    		throw e;      		
    	}
	}
}