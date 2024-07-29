package com.tfsla.opencms.search.documents;

import org.apache.lucene.document.Field;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsIndexException;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.documents.A_CmsVfsDocument;
import org.opencms.search.extractors.CmsExtractionResult;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.search.documents.Messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class CmsDocumentXmlContentTFS extends A_CmsVfsDocument {

	static List<I_TFSContentExtractor> extractors = new ArrayList<I_TFSContentExtractor>();
	
	public static void addExtractor(I_TFSContentExtractor extractor) {
		extractors.add(extractor);
	}
	
	public CmsDocumentXmlContentTFS(String name) {
		super(name);
		
		//extractors.add(new NoticiacontentExtrator());
		//extractors.add(new EncuestaContentExtractor());
	}
	
	@SuppressWarnings("unchecked")
	public I_CmsExtractionResult extractContent(CmsObject cms, CmsResource resource, CmsSearchIndex index)
    throws CmsException {

        try {
            CmsFile file = readFile(cms, resource);
            String absolutePath = cms.getSitePath(file);
            A_CmsXmlDocument xmlContent = CmsXmlContentFactory.unmarshal(cms, file);

            List locales = xmlContent.getLocales();
            if (locales.size() == 0) {
                locales = OpenCms.getLocaleManager().getDefaultLocales(cms, absolutePath);
            }
            Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(
                index.getLocale(),
                OpenCms.getLocaleManager().getDefaultLocales(cms, absolutePath),
                locales);

            List elements = xmlContent.getNames(locale);
            StringBuffer content = new StringBuffer();
            HashMap items = new HashMap();
            List<Field> customFieds = new ArrayList<Field>();
            
             
            for (Iterator i = elements.iterator(); i.hasNext();) {
                String xpath = (String)i.next();
                // xpath will have the form "Text[1]" or "Nested[1]/Text[1]"
                I_CmsXmlContentValue value = xmlContent.getValue(xpath, locale);
                if (value.getContentDefinition().getContentHandler().isSearchable(value)) {
                    // the content value is searchable
                    String extracted = value.getPlainText(cms);
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(extracted)) {
                        items.put(xpath, extracted);
                        content.append(extracted);
                        content.append('\n');
                    }
                 } 
               }
            
            for (I_TFSContentExtractor extractor : extractors) {
            	if (extractor.isconfiguredExtractor(resource))
            	extractor.extractContent(cms, file,resource,index.getLocale(),content, items, customFieds);
            }
            
           return new CmsExtractionResult(content.toString(), items, customFieds);
          
           
        } catch (Exception e) {
            throw new CmsIndexException(
                Messages.get().container(Messages.ERR_TEXT_EXTRACTION_1, resource.getRootPath()),
                e);
        }
    }

	 public List getDocumentKeys(List resourceTypes, List mimeTypes) throws CmsException {

	        if (resourceTypes.contains("*")) {
	            // we need to find all configured XML content types
	            ArrayList allTypes = new ArrayList();
	            for (Iterator i = OpenCms.getResourceManager().getResourceTypes().iterator(); i.hasNext();) {
	                I_CmsResourceType resourceType = (I_CmsResourceType)i.next();
	                if ((resourceType instanceof CmsResourceTypeXmlContent)
	                // either we need a configured schema, or another class name (which must then contain an inline schema)
	                    && (((CmsResourceTypeXmlContent)resourceType).getConfiguration().containsKey(
	                        CmsResourceTypeXmlContent.CONFIGURATION_SCHEMA) || !CmsResourceTypeXmlContent.class.equals(resourceType.getClass()))) {
	                    // add the XML content resource type name
	                    allTypes.add(resourceType.getTypeName());
	                }
	            }
	            resourceTypes = allTypes;
	        }
	        
	        return super.getDocumentKeys(resourceTypes, mimeTypes);
	    }
	 
	public boolean isLocaleDependend() {
		
		return true;
	}

	public boolean isUsingCache() {
		
		return true;
	}

}

