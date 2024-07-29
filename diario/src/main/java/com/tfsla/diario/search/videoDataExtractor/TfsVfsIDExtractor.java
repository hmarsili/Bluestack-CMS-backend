package com.tfsla.diario.search.videoDataExtractor;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.search.CmsIndexException;

public class TfsVfsIDExtractor implements I_TfsVideoDataExtractorProcess {

	public String getUid(CmsObject cms, CmsResource resource)
			throws CmsIndexException, CmsException {
		return resource.getResourceId().getStringValue();
	}
	
	public String getCategories(CmsObject cms, CmsResource resource)
			throws CmsIndexException, CmsException {
		
		String   videoPath = resource.getRootPath();
		CmsProperty   prop = cms.readPropertyObject(videoPath, "category", false);  
		String  categories = (prop != null) ? prop.getValue() : null; 
		
		return categories;
		
	}

}
