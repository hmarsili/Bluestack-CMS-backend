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
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

public class TriviaContentExtractor implements I_TFSContentExtractor {

	public boolean isconfiguredExtractor(CmsResource resource) {
		int tipo = resource.getTypeId();
		try {
			return (tipo == OpenCms.getResourceManager().getResourceType("trivia").getTypeId());
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
		
		String absolutePath = cms.getSitePath(file);
		A_CmsXmlDocument xmlContent;
		try {
			xmlContent = CmsXmlContentFactory.unmarshal(cms, file);

			List<Locale> locales = xmlContent.getLocales();
			if (locales.size() == 0) {
				locales = OpenCms.getLocaleManager().getDefaultLocales(cms, absolutePath);
			}

		    
			Locale contentLocale = OpenCms.getLocaleManager().getBestMatchingLocale(
					locale,
					OpenCms.getLocaleManager().getDefaultLocales(cms, absolutePath),
					locales);
		
			List<I_CmsXmlContentValue> values = xmlContent.getValues("category", contentLocale);
			int idx=1;
			for (I_CmsXmlContentValue value : values) {
				String categoria = xmlContent.getStringValue(cms, "category[" + idx + "]", contentLocale);
				
				categoria = categoria.replaceAll("/", " ");
		    	 	categoria = categoria.replaceAll("[-_]", "");
				items.put("categ["+ idx + "]", categoria);
				idx++;
			}
		} catch (CmsXmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}


}
