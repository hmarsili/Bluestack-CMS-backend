package com.tfsla.diario.search.videoDataExtractor;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsIndexException;
import org.opencms.search.documents.Messages;

public class TfsPointerIDExtractor implements I_TfsVideoDataExtractorProcess {

	public String getUid(CmsObject cms,CmsResource resource) throws CmsIndexException, CmsException {
		
		CmsFile file = readFile(cms, resource);
		try {
	        CmsProperty encProp = cms.readPropertyObject(
	                resource,
	                CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING,
	                true);
	            String encoding = encProp.getValue(OpenCms.getSystemInfo().getDefaultEncoding());
	        return new String(file.getContents(), encoding);
		} catch (Exception e) {
	        throw new CmsIndexException(
	            Messages.get().container(Messages.ERR_TEXT_EXTRACTION_1, resource.getRootPath()),
	            e);
	    }
	
	}
	
	public String getCategories(CmsObject cms, CmsResource resource)
			throws CmsIndexException, CmsException {
		
		String   videoPath = resource.getRootPath();
		CmsProperty   prop = cms.readPropertyObject(videoPath, "category", false);  
		String  categories = (prop != null) ? prop.getValue() : null; 
		
		return categories;
		
	}

    protected CmsFile readFile(CmsObject cms, CmsResource resource) throws CmsException, CmsIndexException {

        CmsFile file = cms.readFile(resource);
        if (file.getLength() <= 0) {
            throw new CmsIndexException(Messages.get().container(Messages.ERR_NO_CONTENT_1, resource.getRootPath()));
        }
        return file;
    }

}
