package com.tfsla.diario.admin;

import org.opencms.file.*;
import org.opencms.file.history.*;
import java.util.*;


public class TfsResourceHelper {
	public static Date getLastPublishedDate(CmsObject cmso, CmsResource resource) throws Exception {
	    String sitePath = cmso.getSitePath(resource);

	    if (cmso.readAllAvailableVersions(sitePath).size() > 0) {
	        I_CmsHistoryResource histRes = (I_CmsHistoryResource) cmso.readAllAvailableVersions(sitePath).get(0);
	        int publishTag = histRes.getPublishTag();
	        CmsHistoryProject project = cmso.readHistoryProject(publishTag);            
	        return new Date(project.getPublishingDate());                                   
	    } else {
	        return null;
	    }   
	}
	
	public static boolean isNew(CmsObject cmso, CmsResource resource) throws Exception {    
		return resource.getState().isNew();
	}
	
	public static boolean isLastVersionPublished(CmsObject cmso, CmsResource resource) throws Exception {
	    
		return  (!resource.getState().isNew() && !resource.getState().isChanged());
	}
	
	public static String getUserNamePublisher(CmsObject cmso, CmsResource resource) throws Exception {
	    String sitePath = cmso.getSitePath(resource);

	    if (cmso.readAllAvailableVersions(sitePath).size() > 0) {
	        I_CmsHistoryResource histRes = (I_CmsHistoryResource) cmso.readAllAvailableVersions(sitePath).get(0);
	        int publishTag = histRes.getPublishTag();
	        CmsHistoryProject project = cmso.readHistoryProject(publishTag);            
	        return project.getPublishedByName(cmso);
	        
	    } else {
	        return null;
	    }   
	}


}
