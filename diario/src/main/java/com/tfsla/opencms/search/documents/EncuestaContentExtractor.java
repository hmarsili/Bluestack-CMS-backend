package com.tfsla.opencms.search.documents;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.lucene.document.Field;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.OpenCms;

public class EncuestaContentExtractor implements I_TFSContentExtractor {

	public boolean isconfiguredExtractor(CmsResource resource) {
		int tipo = resource.getTypeId();
		try {
			return (tipo == OpenCms.getResourceManager().getResourceType("encuesta").getTypeId());
		} catch (CmsLoaderException e) {
			return false;
		}
	}

	public void extractContent(CmsObject cms, CmsFile file,
			CmsResource resource, Locale locale, StringBuffer content,
			HashMap items, List<Field> customFieds) {
		
		String rootPath = resource.getRootPath();
		String fileName = rootPath.substring(rootPath.lastIndexOf("/")+1);

		items.put("temporal[1]", "" + fileName.contains("~"));

	}

}
