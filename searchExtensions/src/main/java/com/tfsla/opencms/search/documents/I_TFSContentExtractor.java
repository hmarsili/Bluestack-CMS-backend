package com.tfsla.opencms.search.documents;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.lucene.document.Field;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

public interface I_TFSContentExtractor {
	public boolean isconfiguredExtractor(CmsResource resource);
	public void extractContent(CmsObject cms, CmsFile file, CmsResource resource, Locale locale, StringBuffer content, HashMap items, List<Field> customFieds);
	
}
