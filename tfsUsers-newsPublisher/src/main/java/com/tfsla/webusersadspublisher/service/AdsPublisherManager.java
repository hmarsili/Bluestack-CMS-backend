package com.tfsla.webusersadspublisher.service;

import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.utils.TfsAdminUserProvider;
//import com.tfsla.webusersadspublisher.helper.HttpRequest;
import com.tfsla.webusersadspublisher.model.Ads;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
//import org.apache.commons.io.FilenameUtils;
import org.opencms.db.CmsDefaultUsers;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
//import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

//import java.text.SimpleDateFormat;
import java.util.*;

import org.opencms.xml.content.CmsXmlContentValueSequence;
//import org.opencms.xml.types.I_CmsXmlContentValue;

public class AdsPublisherManager{
	
	String SITE = "";
	private CmsObject cmsObject = null;
	private HttpServletRequest request = null;
	List<Part> images = new ArrayList<Part>();
	List<String> videos = new ArrayList<String>();
	List<CmsResource> publishList = new ArrayList<CmsResource>();

	
	public AdsPublisherManager(HttpServletRequest request) throws CmsException {      
		
		try {
			this.request = request;
			CmsObject _cmsObject =CmsWorkplaceAction.getInstance().getCmsAdminObject();
			cmsObject = OpenCms.initCmsObject(_cmsObject);
			
			org.opencms.file.CmsProject offProject = cmsObject.readProject("Offline");
            cmsObject.getRequestContext().setCurrentProject(offProject);		
		}
		catch (CmsException e) {
			throw e;
		}
	}
	
	private void publishList(List<CmsResource> list) throws Exception, CmsException {
		try{
			OpenCms.getPublishManager().publishProject(cmsObject, 
					new CmsLogReport(Locale.getDefault(), this.getClass()),
						OpenCms.getPublishManager().getPublishList(cmsObject, list, false));
			
			/*
			HttpRequest request = new HttpRequest();
			request.setUrl(OpenCms.getSiteManager().getWorkplaceServer() + "/purgecache.html");
			request.sendRequest();
			*/
		}
		catch(Exception e){
			throw e;
		}
	}
	
	public Map<String, Object> getParamsForm() throws Exception
	{
		Map<String, Object> map = new HashMap<String, Object>();
	
		try
        {
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setHeaderEncoding("UTF-8");
			
			Collection<Part> items = request.getParts();
			Iterator itr = items.iterator();
			
	    	//List items = upload.parseRequest(request);
	    	
			Iterator<String> atrNames = request.getParameterNames().asIterator();
			while (atrNames.hasNext()) {
				String atrName = atrNames.next();
			
				if (atrName.indexOf("video") >= 0)
				{
					//String codigo = item.getFieldName();
					
					if (atrName != null && atrName.indexOf("<script") < 0 && atrName.indexOf("</script>") < 0 && !atrName.equals(""))
						videos.add(request.getParameter(atrName));
				}
				else
					map.put(atrName, request.getParameter(atrName));
			}
			
			while (itr.hasNext()) {
				Part item = (Part)itr.next();
				String fileName = cmsObject.getRequestContext().getFileTranslator().translateResource(item.getSubmittedFileName());
				if (fileName != null && !fileName.equals("")) {
					images.add(item);
				}
			}
			
			/*
	    	if (items != null) {
				Iterator itr = items.iterator();
				String fileName = "";
				
				while (itr.hasNext()) {
					FileItem item = (FileItem) itr.next();
					
					if (item.isFormField())
					{
						if (item.getFieldName().indexOf("video") >= 0)
						{
							String codigo = item.getFieldName();
							
							if (codigo != null && codigo.indexOf("<script") < 0 && codigo.indexOf("</script>") < 0 && !codigo.equals(""))
								videos.add(item.getString());
						}
						else
							map.put(item.getFieldName(), item);
					}else{
						fileName = cmsObject.getRequestContext().getFileTranslator().translateResource(item.getName());
						if (fileName != null && !fileName.equals(""))
							images.add(item);
					}
				}
	    	}
	    	*/
    		map.put("images", images);
			map.put("videos", videos);
        }
    	catch (Exception e) {
    		throw e;
    	}      	
		return map;
	}
		
	public void publish(Ads ads, String adsName) throws Exception
	{
		try{
			ads.setCmsObject(cmsObject);
			ads.setSite(SITE);
			ads.publish(adsName);
			publishList(ads.getPublishList());
			
		}
		catch(Exception ex){
			throw ex;
		}
	}
	
	public void delete(String AdsName)  throws Exception 
	{
		try{
			Ads ads = new Ads();
			ads.setCmsObject(cmsObject);
			ads.setSite(SITE);
			ads.delete(AdsName);
			OpenCms.getPublishManager().publishResource(cmsObject, AdsName);
		}
		catch(Exception ex){
			throw ex;
		}		
	}
		
	public void setSite(String site) {
		this.SITE = site;
	}
	
	public void moveAd(String ad) throws Exception
	{	
		try{
			if ( ad != null ){
				
				Ads ads = new Ads();
				ads.setCmsObject(cmsObject);
				ads.setSite(SITE);
				
				String array[] = ad.split("/ad_");
				String folder = array[0];
				String newFolder = folder.replace("/tmp", "");
								
				if (!cmsObject.existsResource(newFolder))
					createFolderForFile(ad, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
								
				String newName = ads.getAdsName(newFolder);
								
				lockResource(ad);
				cmsObject.moveResource(ad, newName);
				
				updateImagePath(newName);
								
				//OpenCms.getPublishManager().publishResource(cmsObject, newName);
				publishList.add(cmsObject.readResource(newName));
				ads.setPath(newName);
				
				//publica todas las carpetas y archivos
				publishList(publishList);
				
			}
		}
		catch(Exception ex){
			throw ex;
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
				imgNewPath = imgOldPath.replace("/tmp", "");
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
					imgNewPath = imgOldPath.replace("/tmp", "");
					xmlContent.getValue("imagenesFotogaleria[" + j + "]/imagen",locale).setStringValue(cmsObject, imgNewPath);
					
					lockResource(imgOldPath);
					
					cmsObject.moveResource(imgOldPath, imgNewPath);
					publishList.add(cmsObject.readResource(imgNewPath));    
	            }
			}
		} catch (CmsException ex) {
			throw ex;
		}
	}

	private void lockResource(String resource) throws CmsException
	{
		if (cmsObject.getLock(resource).isUnlocked())
			cmsObject.lockResource(resource);
        else
        {
        	try {
        		cmsObject.unlockResource(resource);
        		cmsObject.lockResource(resource);
        	}
        	catch (Exception e)
        	{
        		cmsObject.changeLock(resource);	            		
        	}
        }
	}
	
	private void createFolderForFile(String file, int resourceTypeID) throws Exception
	{
		///sites/generic/avisos/tmp/img/2012/10/24/pepe.jpg
		///sites/generic/avisos/tmp/contenidos/2012/10/24/ad_0001.html
		try {
			String[] array = file.split("/");
			
			String year = array[6];
			String month = array[7];
			String day = array[8];
	
			String folderName =  SITE + "/" + array[3] + "/" + array[5] + "/" + year;
			
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
		}
    	catch (Exception e)
    	{
    		throw e;      		
    	}
	}
}