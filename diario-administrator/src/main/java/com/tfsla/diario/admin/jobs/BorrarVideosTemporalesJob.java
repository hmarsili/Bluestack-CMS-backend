package com.tfsla.diario.admin.jobs;

import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.file.CmsResourceFilter;
import org.opencms.loader.CmsLoaderException;
import org.opencms.db.CmsPublishList;
import org.opencms.report.CmsLogReport;
import org.opencms.file.CmsFile;

public class BorrarVideosTemporalesJob implements I_CmsScheduledJob  {
	
	private String moduleName = "videoUpload";
	private String resultados = "";
	private String defaultVideoFlashPath = "";
	private List<CmsResource> resourcesToPublish = new ArrayList<CmsResource>();
	
	protected static final Log LOG = CmsLog.getLog(BorrarVideosTemporalesJob.class);

	@Override
	public String launch(CmsObject cms, Map parameters) throws Exception {
		
		resultados = "";
		int daysBefore=0;

		String _daysBefore = (String)parameters.get("daysBefore");
		String _publication = (String)parameters.get("publication");
		
		if (_daysBefore!=null)
			daysBefore = Integer.parseInt(_daysBefore);
		
		Date now = new Date();

		resultados += "\n";
		
		resultados += "Lanzando eliminacion de videos temporales de los ultimos " + daysBefore + " dias\n";
		
		SimpleDateFormat folderDateFormat = new SimpleDateFormat("yyyy/MM/dd");
		Calendar cal = new GregorianCalendar();
		cal.setTime(now);

		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
		
		this.loadConfiguration(siteName, _publication);
	
		for (int j=0;j<=daysBefore;j++) {
			
			String videosFolder = "";
			
			String baseUrl = "/"+defaultVideoFlashPath;
			
			videosFolder = baseUrl + "/" + folderDateFormat.format(cal.getTime()) + "/";
			
			resultados += "revisando carpeta " + videosFolder + ".\n";
			
			if (cms.existsResource(videosFolder)) {
				deletedFilesInFolder(cms, videosFolder);  
			}
			
			cal.add(Calendar.DAY_OF_MONTH, -1);
			
		}
		
		if (resourcesToPublish.size()>0){
			for (CmsResource resource : resourcesToPublish)
			{
				OpenCms.getPublishManager().publishResource(cms, cms.getSitePath(resource));
			}
		}
		
		com.tfsla.diario.ediciones.services.UploadService.uploadStatus.clear();

		return resultados;
	}
	
	protected void deletedFilesInFolder(CmsObject cms, String folderPath){
		
		CmsResourceFilter filter;
		try {
			filter = CmsResourceFilter.ALL.addRequireType(OpenCms.getResourceManager().getResourceType("video-processing").getTypeId());
			 
	        List<CmsResource> resources = cms.getResourcesInFolder(folderPath, filter);  
	        
	        for (CmsResource resource : resources) {  
                try {  
                    CmsResourceState  estado = resource.getState();
         			
         			String pathResource = cms.getRequestContext().removeSiteRoot(resource.getRootPath());
         			
         			if (!cms.getLock(pathResource).isUnlocked()) {
   				     if(!cms.getLock(pathResource).isOwnedBy(cms.getRequestContext().currentUser()))
   					      cms.changeLock(pathResource);
	   				} else {
	   				     cms.lockResource(pathResource);
	   				}

                    cms.deleteResource(pathResource,CmsResource.DELETE_PRESERVE_SIBLINGS);  
                    resultados += "Video temporal eliminado: " + resource.getName()+ ".\n";
                   
        			if( !estado.isNew() ) {
        				resourcesToPublish.add(resource);
        			}
                    
                } catch (CmsException e) {  
                	resultados +="Error al intentar eliminar el archivo: " + resource.getName() + ". " + e.getMessage()+ ".\n";  
                }  
            }  
	        
		} catch (CmsLoaderException e) {
			e.printStackTrace();
		} catch (CmsException e) {
			e.printStackTrace();
		}
		
		return;
		
	}
	
	protected void loadConfiguration(String siteName, String publication) {
    	String module = getModuleName();
 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		
 		defaultVideoFlashPath = config.getParam(siteName, publication, module, "defaultVideoFlashPath","");
	}
	
	protected String getModuleName() {
		return moduleName;
	}

}
