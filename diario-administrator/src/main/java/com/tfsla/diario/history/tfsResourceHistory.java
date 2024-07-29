package com.tfsla.diario.history;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.history.CmsHistoryFile;
import org.opencms.file.history.CmsHistoryProject;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;

import com.tfsla.collections.CollectionFactory;

public class tfsResourceHistory {
	
	private static tfsResourceHistory instance;
	private List<tfsResourceVersion> versions = null;
	
	
	public synchronized static tfsResourceHistory getInstance() {
		if (instance == null) {
			instance = new tfsResourceHistory();
		}
		return instance;
	}
	
	public List<tfsResourceVersion> tfsHistory(CmsObject cms, String path){
	
		versions = CollectionFactory.createList();
		
		TimeZone         zone = TimeZone.getDefault();
		GregorianCalendar cal = new GregorianCalendar(zone, cms.getRequestContext().getLocale());
		DateFormat         df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		int                 v = 0;
		
		try {
			tfsResourceVersion  version_0 = new tfsResourceVersion();
			CmsResource          resource = cms.readResource(path,CmsResourceFilter.ALL);
			CmsUUID UIDUserLastModified_0 = resource.getUserLastModified();
			CmsUser    UserLastModified_0 = cms.readUser(UIDUserLastModified_0);
			
			version_0.setCmsUserLastModified(UserLastModified_0);
			version_0.setUserLastModified(UserLastModified_0.getFullName());
			version_0.setUsernameLastModified(UserLastModified_0.getName());
	        version_0.setVersion(resource.getVersion());
	        
	        
	        CmsResourceState      state_0 = resource.getState();
	        String             stateStr_0 = state_0.toString();
	        
	        if( stateStr_0.equals("0") ){
	        	version_0.setStrVersion(resource.getVersion()+" (Online)");
	        	
	        	I_CmsHistoryResource histRes = (I_CmsHistoryResource) cms.readAllAvailableVersions(path).get(0);
	            int publishTag = histRes.getPublishTag();
	            CmsHistoryProject project = cms.readHistoryProject(publishTag); 
	            
	            cal.setTimeInMillis(project.getPublishingDate());
				String PublishedDate = df.format(cal.getTime());
	            
	    		version_0.setDatePublished(PublishedDate);
	    		
	    		version_0.setProyect("Online");
	    		
	        }else{
	    		   version_0.setStrVersion(resource.getVersion()+" (Offline)");
	    		   version_0.setDatePublished(null);
	    		   version_0.setProyect("Offline");
	    	}
	        
	        version_0.setUrlVersion("/system/shared/showversion"+resource.getRootPath()+"?version=2147483647");
	        
			cal.setTimeInMillis(resource.getDateLastModified());
			String LastModificationDate = df.format(cal.getTime());
	        
			version_0.setDateLastModified(LastModificationDate);
			
			version_0.setSize(resource.getLength());
			
	        this.versions.add(version_0);
	        
	        v = resource.getVersion();
	        
		} catch (CmsException e1) {
			CmsLog.getLog(this).error("No se pudo obtener la version actual del recurso "+path+" ["+e1.getMessage()+"]");
		}
		
		
		try {
			List<I_CmsHistoryResource> historyResource = cms.readAllAvailableVersions(path);
			
			for(I_CmsHistoryResource res: historyResource){
			  
			  tfsResourceVersion version = new tfsResourceVersion();
				  
			  if(res.getVersion()!= v ){	
		    	    String versionNr = Integer.toString(res.getVersion());
			        version.setStrVersion(versionNr);
					
					CmsUUID UIDUserLastModified = res.getUserLastModified();
		        	CmsUser UserLastModified = cms.readUser(UIDUserLastModified);
					
		        	version.setCmsUserLastModified(UserLastModified);
		        	version.setUserLastModified(UserLastModified.getFullName());
					version.setUsernameLastModified(UserLastModified.getName());
		        	
		        	version.setVersion(res.getVersion());
		        	
		        	int publishTag = res.getPublishTag();
		            CmsHistoryProject project = cms.readHistoryProject(publishTag);    
		             
		        	cal.setTimeInMillis(project.getPublishingDate());
		    		String datePublished = df.format(cal.getTime());
		   
		    		version.setDatePublished(datePublished);
		    			    
		    		cal.setTimeInMillis(res.getDateLastModified());
		    		String LastModificationDate = df.format(cal.getTime());
		    		
		    		version.setSize(res.getLength());
		   
		    		version.setDateLastModified(LastModificationDate);
		    		
		    		version.setUrlVersion("/system/shared/showversion"+res.getRootPath()+"?version="+res.getVersion());
		    		 
		    		version.setProyect("Online");
		    		
		        	this.versions.add(version);
		        	
			  }
			}
			
		} catch (CmsException e) {
			CmsLog.getLog(this).error("No se pudo obtener el historial del recurso "+path+" ["+e.getMessage()+"]");
			e.printStackTrace();
		}
		
		return versions;
	}
	
	public boolean restoreVersion(CmsObject cms, String path,int version){
			
			boolean status = false;
			
			try{
				
				CmsUUID StructureId = null;
				
				List<I_CmsHistoryResource> historyResource = cms.readAllAvailableVersions(path);
				
				for(I_CmsHistoryResource res: historyResource){
					
					if(version==res.getVersion()){
						StructureId = res.getStructureId();
					}
				}
	
				cms.lockResource(path);
				cms.restoreResourceVersion(StructureId, version);
				
			    return true;
			    
			} catch (CmsException e) {
				
				CmsLog.getLog(this).error("No se pudo recuperar la version "+version+" del recurso "+path+" ["+e.getMessage()+"]");
				
			}
			
			return status;
	}
	
	public CmsResource getResource(CmsObject cms, CmsUUID id, String version) throws CmsException {
	
	        if (Integer.parseInt(version) == CmsHistoryResourceHandler.PROJECT_OFFLINE_VERSION) {
	            return cms.readResource(id, CmsResourceFilter.IGNORE_EXPIRATION);
	        } else {
	            int ver = Integer.parseInt(version);
	            if (ver < 0) {
	                CmsProject project = cms.getRequestContext().currentProject();
	                try {
	                    cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
	                    return cms.readResource(id, CmsResourceFilter.IGNORE_EXPIRATION);
	                } finally {
	                    cms.getRequestContext().setCurrentProject(project);
	                }
	            }
	            return (CmsResource)cms.readResource(id, ver);
	        }
	}
	
	public CmsFile readFile(CmsObject cms, CmsUUID structureId, String version) throws CmsException {

        if (Integer.parseInt(version) == CmsHistoryResourceHandler.PROJECT_OFFLINE_VERSION) {
            // offline
            CmsResource resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
            return cms.readFile(resource);
        } else {
            int ver = Integer.parseInt(version);
            if (ver < 0) {
                // online
                CmsProject project = cms.getRequestContext().currentProject();
                try {
                    cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
                    CmsResource resource = cms.readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
                    return cms.readFile(resource);
                } finally {
                    cms.getRequestContext().setCurrentProject(project);
                }
            }
            // backup
            return cms.readFile((CmsHistoryFile)cms.readResource(structureId, ver));
        }
    }

}
