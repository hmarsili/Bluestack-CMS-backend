package com.tfsla.diario.search.videoDataExtractor;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.search.CmsIndexException;

public interface I_TfsVideoDataExtractor {
	public String getUid(CmsObject cms, CmsResource resource) throws CmsIndexException, CmsException;
	public String getCategories(CmsObject cms, CmsResource resource) throws CmsIndexException, CmsException;
}
