package com.tfsla.opencmsdev;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.Messages;
import org.opencms.xml.types.I_CmsXmlContentValue;

public class CmsCustomXmlContentHandler extends CmsDefaultXmlContentHandler 
{
	protected Map<String, Map<String, String>> m_elementMappingsCombined;
	
	protected void init() {
		//Se instancia al map de mapping combinados
		m_elementMappingsCombined = new HashMap<String, Map<String, String>>();
		super.init();
    }
	 	
	//Metodo llamado al momento de agregar los mappings
    protected void addMapping(CmsXmlContentDefinition contentDefinition, String elementName, String mapping) throws CmsXmlException {
    	
    	//Si el mapping es combinado, llamo al método addMappingCombined (propio)
    	if(elementName.indexOf(",") >= 0){
    		String[] elementsName = elementName.split(",");
    		
    		if (elementsName != null)
    			addMappingCombined(contentDefinition, elementsName[0], elementName, mapping);
    	}
    	else
            super.addMapping(contentDefinition, elementName, mapping);
    }
        
    //Metodo llamado al momento de guardar los mappings
    public void resolveMapping(CmsObject cms, CmsXmlContent content, I_CmsXmlContentValue value) throws CmsException {
    	
    	//Si el nodo del contenido tiene un mapping combinado, llamo al método resolveMappingCombined (propio)
    	Map<String, String> mappings = m_elementMappingsCombined.get(value.getPath());
        if (mappings != null)
        	for (String key : mappings.keySet())
        		resolveMappingCombined(cms, content, key.split(","), mappings.get(key));
    	
    	super.resolveMapping(cms, content, value);
    }
    
    //Método propio para agregar mappings combinados
	protected void addMappingCombined(CmsXmlContentDefinition contentDefinition, String elementName, String elementCombined, String mapping) throws CmsXmlException {
		
		String[] arrElementCombined = elementCombined.split(",");
        for (int i = 0; i < arrElementCombined.length; i++) {
            if (contentDefinition.getSchemaType(arrElementCombined[i]) == null) {
                throw new CmsXmlException(Messages.get().container(
                    Messages.ERR_XMLCONTENT_INVALID_ELEM_MAPPING_1,
                    elementName));
            }
        }		

        String xpath = CmsXmlUtils.createXpath(elementName, 1);

        Map<String, String> elementsCombined = m_elementMappingsCombined.get(xpath);
        
        if(elementsCombined == null)
        	elementsCombined = new HashMap<String, String>();
        
        elementsCombined.put(elementCombined, mapping);

        m_elementMappingsCombined.put(xpath, elementsCombined);
	}
    
    //Método propio para resolver cada mapping
	protected void resolveMappingCombined(CmsObject cms, CmsXmlContent content, String[] elementsName, String mapping) throws CmsException {
    	
        //get the original VFS file from the content
        CmsFile file = content.getFile();
        if (file == null) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_XMLCONTENT_RESOLVE_FILE_NOT_FOUND_0));
        }

        // create OpenCms user context initialized with "/" as site root to read all siblings
        CmsObject rootCms = OpenCms.initCmsObject(cms);
        rootCms.getRequestContext().setSiteRoot("/");
        // read all siblings of the file
        List<CmsResource> siblings = rootCms.readSiblings(
            content.getFile().getRootPath(),
            CmsResourceFilter.IGNORE_EXPIRATION);

        for (int i = (siblings.size() - 1); i >= 0; i--) {
            // get filename
            String filename = (siblings.get(i)).getRootPath();

            // make sure the file is locked
            CmsLock lock = rootCms.getLock(filename);
            if (lock.isUnlocked()) {
                rootCms.lockResource(filename);
            } else if (!lock.isExclusiveOwnedBy(rootCms.getRequestContext().currentUser())) {
                rootCms.changeLock(filename);
            }

            //get the string value of the current node
            String stringValue = "";
            for (int p = 0; p < elementsName.length; p++) {
            	stringValue += content.getValue(elementsName[p], Locale.ENGLISH).getStringValue(rootCms);
            	if((p+1) < elementsName.length)
            		stringValue +=  "|";
            }
            
            if (mapping.startsWith(MAPTO_PROPERTY)) {

                boolean mapToShared;
                int prefixLength;
                // check which mapping is used (shared or individual)                        
                if (mapping.startsWith(MAPTO_PROPERTY_SHARED)) {
                    mapToShared = true;
                    prefixLength = MAPTO_PROPERTY_SHARED.length();
                } else if (mapping.startsWith(MAPTO_PROPERTY_INDIVIDUAL)) {
                    mapToShared = false;
                    prefixLength = MAPTO_PROPERTY_INDIVIDUAL.length();
                } else {
                    mapToShared = false;
                    prefixLength = MAPTO_PROPERTY.length();
                }

                // this is a property mapping
                String property = mapping.substring(prefixLength);

                CmsProperty p;
                if (mapToShared) {
                    // map to shared value
                    p = new CmsProperty(property, null, stringValue);
                } else {
                    // map to individual value
                    p = new CmsProperty(property, stringValue, null);
                }
                // just store the string value in the selected property
                rootCms.writePropertyObject(filename, p);
                if (mapToShared) {
                    // special case: shared mappings must be written only to one sibling, end loop
                    i = 0;
                }

            } else if (mapping.startsWith(MAPTO_ATTRIBUTE)) {

                // this is an attribute mapping                        
                String attribute = mapping.substring(MAPTO_ATTRIBUTE.length());
                switch (ATTRIBUTES.indexOf(attribute)) {
                    case 0: // date released
                        long date = 0;
                        try {
                            date = Long.valueOf(stringValue).longValue();
                        } catch (NumberFormatException e) {
                            // ignore, value can be a macro
                        }
                        if (date == 0) {
                            date = CmsResource.DATE_RELEASED_DEFAULT;
                        }
                        // set the sibling release date
                        rootCms.setDateReleased(filename, date, false);
                        // set current file release date
                        if (filename.equals(rootCms.getSitePath(file))) {
                            file.setDateReleased(date);
                        }
                        break;
                    case 1: // date expired
                        date = 0;
                        try {
                            date = Long.valueOf(stringValue).longValue();
                        } catch (NumberFormatException e) {
                            // ignore, value can be a macro
                        }
                        if (date == 0) {
                            date = CmsResource.DATE_EXPIRED_DEFAULT;
                        }
                        // set the sibling expired date
                        rootCms.setDateExpired(filename, date, false);
                        // set current file expired date
                        if (filename.equals(rootCms.getSitePath(file))) {
                            file.setDateExpired(date);
                        }
                        break;
                    default:
                        // ignore invalid / other mappings                                
                }
            }
        }
        // make sure the original is locked
        CmsLock lock = rootCms.getLock(file);
        if (lock.isUnlocked()) {
            rootCms.lockResource(file.getRootPath());
        } else if (!lock.isExclusiveOwnedBy(rootCms.getRequestContext().currentUser())) {
            rootCms.changeLock(file.getRootPath());
        }
    }
	
    //Metodo llamado al momento de borrar los mappings
    protected void removeEmptyMappings(CmsObject cms, CmsXmlContent content) throws CmsException {
    	
    	if(m_elementMappingsCombined != null){
	    	for (String key : m_elementMappingsCombined.keySet()){
	    		
	            Map<String, String> mappings = m_elementMappingsCombined.get(key);
	            
	            for (String key2 : mappings.keySet()){
	            	removeEmptyMappingsCombined(cms, content, key2.split(","), mappings.get(key2));	
	            }	            
	    	}
    	}
    	
    	super.removeEmptyMappings(cms, content);
    }	
    
    protected void removeEmptyMappingsCombined(CmsObject cms, CmsXmlContent content, String[] elementsName, String mapping) throws CmsException {

        List<CmsResource> siblings = null;
        CmsObject rootCms = null;

        if ((siblings == null) || (rootCms == null)) {
            // create OpenCms user context initialized with "/" as site root to read all siblings
            rootCms = OpenCms.initCmsObject(cms);
            rootCms.getRequestContext().setSiteRoot("/");
            siblings = rootCms.readSiblings(content.getFile().getRootPath(), CmsResourceFilter.IGNORE_EXPIRATION);
        }
        
        if (mapping.startsWith(MAPTO_PROPERTY_LIST) || mapping.startsWith(MAPTO_PROPERTY)) {

            for (int i = 0; i < siblings.size(); i++) {

                // get siblings filename and locale
                String filename = siblings.get(i).getRootPath();
                Locale locale = OpenCms.getLocaleManager().getDefaultLocale(rootCms, filename);

                if (!content.hasLocale(locale)) {
                    // only remove property if the locale fits
                    continue;
                }
                                
                boolean continueProcess = true;
                
                for(int p=0; p < elementsName.length; p++){
            		if (content.hasValue(elementsName[p], locale)) {
            			continueProcess = false;
            			break;
            		}
                }
                
                if(!continueProcess)
                	continue;

                String property;
                if (mapping.startsWith(MAPTO_PROPERTY_LIST)) {
                    // this is a property list mapping
                    property = mapping.substring(MAPTO_PROPERTY_LIST.length());
                } else {
                    // this is a property mapping
                	property = mapping.substring(MAPTO_PROPERTY.length());
                }
                // delete the property value for the not existing node
                rootCms.writePropertyObject(filename, new CmsProperty(property, CmsProperty.DELETE_VALUE, null));
            }
        }
    }
}
