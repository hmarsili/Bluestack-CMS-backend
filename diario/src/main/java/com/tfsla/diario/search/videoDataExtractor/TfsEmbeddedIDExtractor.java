package com.tfsla.diario.search.videoDataExtractor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsIndexException;
import org.opencms.search.documents.Messages;

public class TfsEmbeddedIDExtractor implements I_TfsVideoDataExtractorProcess {

	public String getUid(CmsObject cms, CmsResource resource)
			throws CmsIndexException, CmsException {
		
		String content = getContent(cms,resource);
		
		String videoCode  = "";
		
		if(content.indexOf("class=\"twitter-video\"")>-1){
			
			Pattern REGEX_ID = Pattern.compile("<a\\s+(?:[^>]*?\\s+)?href=\"([^\"]*)\"");
			Matcher matcherID = REGEX_ID.matcher(content);
			
			if(matcherID.find())
				videoCode = matcherID.group(1);
			
		}else {
		
			int idx = content.indexOf("src=") + 5;
			int endIdx = content.indexOf("\"", idx);
			
			if(endIdx == -1)
				endIdx = content.indexOf("'", idx);
			
			if(endIdx == -1)
				endIdx = content.length() - 1;
			
			videoCode = content.substring(idx, endIdx);
		}
		
		return videoCode;
	}
	
	public String getCategories(CmsObject cms, CmsResource resource)
			throws CmsIndexException, CmsException {
		
		String   videoPath = resource.getRootPath();
		CmsProperty   prop = cms.readPropertyObject(videoPath, "category", false);  
		String  categories = (prop != null) ? prop.getValue() : null; 
		
		return categories;
		
	}

	protected String getContent(CmsObject cms, CmsResource resource) throws CmsIndexException
	{
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
	
	  protected CmsFile readFile(CmsObject cms, CmsResource resource) throws CmsIndexException {
	
	        CmsFile file;
			try {
				file = cms.readFile(resource);
			} catch (CmsException e) {
	            throw new CmsIndexException(Messages.get().container(Messages.ERR_NO_CONTENT_1, resource.getRootPath()));
			}

			if (file.getLength() <= 0) {
	            throw new CmsIndexException(Messages.get().container(Messages.ERR_NO_CONTENT_1, resource.getRootPath()));
	        }
	        return file;
	    }
}
