package com.tfsla.diario.admin.json;

import java.util.Iterator;

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

	
}
