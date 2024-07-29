package com.tfsla.genericImport.service;

import java.util.ArrayList;
import java.util.List;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.loader.CmsXmlContentLoader;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ContentTypeService {
	
	public ContentTypeService() {
		
	}
	
	public List<String> getContentTypes() {
		List<String> types = new ArrayList<String>();
		for (I_CmsResourceType type : (List<I_CmsResourceType>)OpenCms.getResourceManager().getResourceTypes()){
		//for (I_CmsXmlSchemaType type : OpenCms.getXmlContentTypeManager().getRegisteredSchemaTypes()){
			
			types.add(type.getTypeName());
		}
		
		return types;
	}

	public boolean isXmlContentType(String resourceType) throws CmsLoaderException {
		I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(resourceType);
		//return OpenCms.getResourceManager().getLoader(resType.getLoaderId()) instanceof CmsXmlContentLoader;
		
		if(resType.getLoaderId()>0)
			return OpenCms.getResourceManager().getLoader(resType.getLoaderId()) instanceof CmsXmlContentLoader;
		else 
			return false;
	}
	
	public List<I_CmsXmlSchemaType> getSubContentDetail(CmsObject cms, String contentTypeName, String elementName) throws CmsLoaderException, CmsXmlException {
	    	
		I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(contentTypeName);
        // get the schema for the resource type to create
        String schema = (String)resType.getConfiguration().get(CmsResourceTypeXmlContent.CONFIGURATION_SCHEMA);
        CmsXmlContentDefinition contentDefinition = CmsXmlContentDefinition.unmarshal(cms, schema);	
		
        if (!elementName.equals("")) {
	        String[] nameParts = elementName.split("/");
	        for (String part : nameParts) {
	        	for (I_CmsXmlSchemaType type : contentDefinition.getTypeSequence())
	        	{
	        		if (type.getName().equals(part)) {
	        			if (!type.isSimpleType()) {
	                        // get nested content definition for nested types
	                        CmsXmlNestedContentDefinition nestedSchema = (CmsXmlNestedContentDefinition)type;
	                        contentDefinition = nestedSchema.getNestedContentDefinition();
	                    }
	        			break;
	        		}
	        	}
	        }
        }
        return contentDefinition.getTypeSequence();
    }

	public void getElementItems(CmsObject cms, String ctype,String path, String description, JSONArray jsonItems) {
		getElementItems(cms, ctype,path, description, false, jsonItems);
	}
	
	public void getElementItems(CmsObject cms, String ctype,String path, String description, boolean onlycomposite, JSONArray jsonItems) {
		
		try {
			List<I_CmsXmlSchemaType> xmlSchemas = getSubContentDetail(cms, ctype, path);
			
			for (I_CmsXmlSchemaType xmlSchema : xmlSchemas ) {
				String newPath = ""; 
				if (!path.equals("")) 
					newPath = path + "/" + xmlSchema.getName();
				else
					newPath = xmlSchema.getName();
					
				String newDescription = ""; 
				if (!description.equals("")) 
					newDescription = description + " - " + xmlSchema.getName();
				else
					newDescription = xmlSchema.getName();
					
				if (xmlSchema.isSimpleType() ) {
				
					if (!onlycomposite || xmlSchema.getMinOccurs()==0 || xmlSchema.getMaxOccurs()>1) {
						JSONObject jsonitem = new JSONObject();
			
						jsonitem.put("key", newPath);
						jsonitem.put("value",  newDescription);
						jsonItems.add(jsonitem);
					}
				
				}
				else {
					if (onlycomposite) {
						JSONObject jsonitem = new JSONObject();
			
						jsonitem.put("key", newPath);
						jsonitem.put("value",  newDescription);
						jsonItems.add(jsonitem);
					}

					getElementItems(cms, ctype,newPath, newDescription, onlycomposite, jsonItems);
				}
	
	
			}
		}
		catch (CmsLoaderException ex) {}
		catch (CmsXmlException ex) {}
	} 
	
	public void getContentElementItems(CmsObject cms, String ctype, String path, List<CmsResource> resources, String currentPublication, JSONArray jsonItems) {
		
			for (CmsResource file : resources) {
				
				String element = "";
				String publication = "";
				try {
					CmsXmlContent resourceDocument = CmsXmlContentFactory.unmarshal(cms, cms.readFile(file));
					I_CmsXmlContentValue elementValue = resourceDocument.getValue("nombre", cms.getRequestContext().getLocale());
					I_CmsXmlContentValue publicationValue = resourceDocument.getValue("publication", cms.getRequestContext().getLocale());
					
					element = elementValue.getStringValue(cms);		
					publication = publicationValue.getStringValue(cms);
				} catch (CmsXmlException e) {					
					e.printStackTrace();
				} catch (CmsException e) {
					e.printStackTrace();
				}
				
				if(!element.equals("")){
					if(publication.equals("Todas")){
						JSONObject jsonitem = new JSONObject();
							
						jsonitem.put("key", file.getRootPath().toString());
						jsonitem.put("value", CmsStringUtil.escapeHtml(element));
						jsonItems.add(jsonitem);
					}else{
						if(publication.equals(currentPublication)){
							JSONObject jsonitem = new JSONObject();
							
							jsonitem.put("key", file.getRootPath().toString());
							jsonitem.put("value", CmsStringUtil.escapeHtml(element));
							jsonItems.add(jsonitem);
						}
					}
				}
			}			
	} 
	
	public List<CmsPropertyDefinition> getAllProperties(CmsObject cms) throws CmsException {
		return (List<CmsPropertyDefinition>)cms.readAllPropertyDefinitions();
		
	}

}
