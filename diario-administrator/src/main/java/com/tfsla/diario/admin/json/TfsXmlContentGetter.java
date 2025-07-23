package com.tfsla.diario.admin.json;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentValueSequence;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

public class TfsXmlContentGetter extends A_TfsXmlContentProc {

	public TfsXmlContentGetter(CmsObject cms, String resource) throws CmsException {
		
		setResourceName(resource);
		
		m_file = cms.readFile(resource, CmsResourceFilter.ALL);
        m_content = CmsXmlContentFactory.unmarshal(cms, m_file);
        this.cms = cms;
        
        setFileEncoding(getFileEncoding(cms, resource));
        
        setLocale(cms.getRequestContext().getLocale());
        
	}
	
	public JSONObject getResourceContent(JSONArray paginationArray) {
		
		List<String> fields = new ArrayList<>();
		
		for (int i = 0; i < paginationArray.size(); i++) {
            JSONObject item = paginationArray.getJSONObject(i);
            fields.add(item.getString("field"));
		}
		
		JSONObject jsonObject = getSubJsonContent("", m_content.getContentDefinition(),fields);
		JSONArray jsonItems = new JSONArray();
		
		for (int i = 0; i < paginationArray.size(); i++) {
			
			JSONObject item = paginationArray.getJSONObject(i);
			
			String fieldName = item.optString("field");
			int pageSize = item.optInt("page_size", 5);
	        String order = item.optString("order", "asc");
	        int page = 1;
	        
			String fieldCountName = fieldName+"_count";
			
			int fieldCount = (int) jsonObject.get(fieldCountName);
			
			jsonItems = getItemsByPage(fieldName, fieldCount, pageSize, page, order);
			
			jsonObject.put(fieldName,jsonItems);

		}
		
		return jsonObject;
	}
	
	public JSONArray getItemsByPage(String fieldName,int fieldCount,int pageSize, int page, String order) {
		
		JSONObject jsonObject = new JSONObject();
		
		int startIndex, endIndex, to;
		
		if(order.equals("asc")){
			startIndex = ((page-1 )* pageSize);
			to = startIndex + pageSize-1;
			
			endIndex = Math.min(to, fieldCount-1);
			
		}else {
			endIndex = (fieldCount-1) - (pageSize*(page-1));
			startIndex = endIndex - (pageSize - 1);
			
			if (startIndex<0) startIndex = 0;
		}
		
		CmsXmlContentDefinition contentDefinition =  m_content.getContentDefinition();
		 
		I_CmsXmlSchemaType type =  contentDefinition.getSchemaType(fieldName);
		
		CmsXmlNestedContentDefinition nestedDefinition = (CmsXmlNestedContentDefinition) type;
		CmsXmlContentDefinition innerDefinition = nestedDefinition.getNestedContentDefinition();
		
		JSONArray jsonItems = new JSONArray();
		
		if(order.equals("asc")){
			for(int c = startIndex; c <=endIndex; c++) {
				 int fieldInd = c+1;
				 jsonObject = getSubJsonContent(fieldName+"["+fieldInd+"]/",innerDefinition);
				 jsonItems.add(jsonObject);
			}
		}else {
			for(int c = endIndex; c >=startIndex; c--) {
				 int fieldInd = c+1;
				 jsonObject = getSubJsonContent(fieldName+"["+fieldInd+"]/",innerDefinition);
				 jsonItems.add(jsonObject);
			}
		}
		
		return jsonItems;
	}
	
	public JSONObject getResourceContentByPage(String field, int pageSize, int page, String order) {
		
        List<String> fields = new ArrayList<>();
           			 fields.add(field);
		
		JSONObject jsonObject = getSubJsonContent("", m_content.getContentDefinition(),fields);
		String fieldCountName = field+"_count";
		
		int fieldCount = (int) jsonObject.get(fieldCountName);
		
		JSONArray jsonItems = getItemsByPage(field, fieldCount, pageSize, page, order);
		
		JSONObject jsonObjectItems = new JSONObject();
				   jsonObjectItems.put(field,jsonItems);
		
		return jsonObjectItems;
	}
	
	public JSONObject getResourceContent() {
		
		JSONObject jsonObject = getSubJsonContent("", m_content.getContentDefinition());
		
		return jsonObject;
	}
	
	protected JSONObject getSubJsonContent(String pathPrefix, CmsXmlContentDefinition contentDefinition) {
		JSONObject jsonOBject = new JSONObject();
	
		for (Iterator<I_CmsXmlSchemaType> i = contentDefinition.getTypeSequence().iterator(); i.hasNext();) {
            // get the type
            I_CmsXmlSchemaType type = i.next();

            CmsXmlContentDefinition nestedContentDefinition = contentDefinition;
            if (!type.isSimpleType()) {
                // get nested content definition for nested types
                CmsXmlNestedContentDefinition nestedSchema = (CmsXmlNestedContentDefinition)type;
                nestedContentDefinition = nestedSchema.getNestedContentDefinition();
            }
            
         // create xpath to the current element
            String jsonName = type.getName();
            String name = pathPrefix + type.getName();
            
         // get the element sequence of the current type
            CmsXmlContentValueSequence elementSequence = m_content.getValueSequence(name, getLocale());
            int elementCount = elementSequence.getElementCount();
            
            JSONArray jsonItems = new JSONArray();
            for (int j = 0; j < elementCount; j++) {
            	I_CmsXmlContentValue value = elementSequence.getValue(j);
         
	           	if (!type.isSimpleType()) {
	           		 //llamarse recursivo
	           		String newPath = CmsXmlUtils.createXpathElement(value.getName(), value.getIndex() + 1);
	           		JSONObject jsonInnerObject = getSubJsonContent(
	                        pathPrefix + newPath + "/",
	                        nestedContentDefinition);
	           		
	           		jsonItems.add(jsonInnerObject);
	           	}
	           	else {
	           		
	           		jsonItems.add(value.getStringValue(cms));
	           		
	           	}
	           	jsonOBject.put(jsonName,jsonItems);
            }
		}
		return jsonOBject;
	}

	protected JSONObject getSubJsonContent(String pathPrefix, CmsXmlContentDefinition contentDefinition, List<String> paginationItems) {
		JSONObject jsonOBject = new JSONObject();
	
		for (Iterator<I_CmsXmlSchemaType> i = contentDefinition.getTypeSequence().iterator(); i.hasNext();) {
            // get the type
            I_CmsXmlSchemaType type = i.next();

            CmsXmlContentDefinition nestedContentDefinition = contentDefinition;
            if (!type.isSimpleType()) {
                // get nested content definition for nested types
                CmsXmlNestedContentDefinition nestedSchema = (CmsXmlNestedContentDefinition)type;
                nestedContentDefinition = nestedSchema.getNestedContentDefinition();
            }
            
            // create xpath to the current element
            String jsonName = type.getName();
            String name = pathPrefix + type.getName();

            boolean isInPaginationList = paginationItems.contains(name);
            
            // get the element sequence of the current type
            CmsXmlContentValueSequence elementSequence = m_content.getValueSequence(name, getLocale());
            int elementCount = elementSequence.getElementCount();
            
            if(!isInPaginationList){
	            JSONArray jsonItems = new JSONArray();
	            for (int j = 0; j < elementCount; j++) {
	            	I_CmsXmlContentValue value = elementSequence.getValue(j);
	         
		           	if (!type.isSimpleType()) {
		           		String newPath = CmsXmlUtils.createXpathElement(value.getName(), value.getIndex() + 1);
		           		JSONObject jsonInnerObject = getSubJsonContent(
		                        pathPrefix + newPath + "/",
		                        nestedContentDefinition);
		           		
		           		jsonItems.add(jsonInnerObject);
		           	}
		           	else {
		           		jsonItems.add(value.getStringValue(cms));
		           	}
		           	jsonOBject.put(jsonName,jsonItems);
	            }
            }else
            	jsonOBject.put(jsonName+"_count",elementCount);
            
		}
		
		return jsonOBject;
	}
	
}
