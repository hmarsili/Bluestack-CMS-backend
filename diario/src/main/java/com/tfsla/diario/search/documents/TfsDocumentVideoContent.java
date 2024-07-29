package com.tfsla.diario.search.documents;

import java.util.HashMap;
import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsIndexException;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.documents.A_CmsVfsDocument;
import org.opencms.search.documents.Messages;
import org.opencms.search.extractors.CmsExtractionResult;
import org.opencms.search.extractors.I_CmsExtractionResult;

import com.tfsla.diario.search.videoDataExtractor.I_TfsVideoDataExtractor;

public class TfsDocumentVideoContent extends A_CmsVfsDocument {

    public TfsDocumentVideoContent(String name) {

        super(name);
    }

    public I_CmsExtractionResult extractContent(CmsObject cms, CmsResource resource, CmsSearchIndex index)
    throws CmsException {

        try {
        	
        	Map<String, String> items = new HashMap<String, String>();
        	items.put("typeId", "" + resource.getTypeId());
        	
        	items.put("type", OpenCms.getResourceManager().getResourceType(resource.getTypeId()).getTypeName());
        	
        	I_TfsVideoDataExtractor extractor = (I_TfsVideoDataExtractor) OpenCms.getResourceManager().getResourceType(resource.getTypeId());        	
        	items.put("uid", extractor.getUid(cms, resource));	
        	
        	String  categories = extractor.getCategories(cms, resource);
        	
        	if(categories!=null && !categories.equals("")){
        	     String[] categoriesArr = categories.split("\\|"); 
        	     
        	     for(int x=0; x<categoriesArr.length;x++){
        	    	 String categoria = categoriesArr[x];
        	    	 categoria = categoria.replaceAll("/", " ");
        	    	 categoria = categoria.replaceAll("[-_]", "");
        	    	 
        	    	 int idx=x+1;
     				 items.put("Category["+ idx + "]", categoria);
        	     }
        	}
        	
        	CmsProperty prop = cms.readPropertyObject(resource, "Title", false);
        	String title = (prop != null)? prop.getValue() : "";
        	
        	items.put("Title[1]", title);
        	
        	prop = cms.readPropertyObject(resource, "Description", false);
        	String description= (prop != null)? prop.getValue() : "";
        	
        	if(description !=null && !description.equals(""))
        		items.put("Description[1]", description);
        	
            return new CmsExtractionResult("", items);

        } catch (Exception e) {
            throw new CmsIndexException(
                Messages.get().container(Messages.ERR_TEXT_EXTRACTION_1, resource.getRootPath()),
                e);
        }
    }

 
    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#isLocaleDependend()
     */
    public boolean isLocaleDependend() {

        return false;
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#isUsingCache()
     */
    public boolean isUsingCache() {

        return false;
    }
}