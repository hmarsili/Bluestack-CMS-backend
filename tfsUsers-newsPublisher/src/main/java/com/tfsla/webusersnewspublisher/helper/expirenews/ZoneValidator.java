package com.tfsla.webusersnewspublisher.helper.expirenews;

import java.util.List;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.xml.types.I_CmsXmlContentValue;

/**
 * Validates the zones of every publication for a content to verify if can be deleted/expired or not 
 */
public class ZoneValidator extends CmsXmlValidator {
	
	public ZoneValidator(CmsObject cmsObject, String resourcePath) throws Exception {
		super(cmsObject, resourcePath);
	}

	/**
	 * Validates that every zone in the content's publications are no_mostrar  
	 * @return true if the validation succeded, otherwise false
	 * @throws CmsException
	 */
	public Boolean validate() throws Exception {
		if(!this.validatePublicationZones("")) return false;
		
		List<I_CmsXmlContentValue> publications = xmlContent.getValues(Strings.PUBLICATIONS_XML_PATH, locale);
		String pathFormat = Strings.PUBLICATIONS_XML_PATH_FORMAT;
		
		if(publications != null && publications.size() > 0) {
			for(int i=0; i < publications.size(); i++) {
				String publication = String.format(pathFormat, i+1);
				if(!this.validatePublicationZones(publication)) return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Validates the values of the zonahome and zonaseccion elements on a specific publication
	 * @param publication the publication name
	 * @return true if both values equals to 'no_mostrar'
	 */
	private Boolean validatePublicationZones(String publication) {
		if(!this.getStringValue(publication + Strings.ZONAHOME).equals(Strings.NO_MOSTRAR)) return false;
		if(!this.getStringValue(publication + Strings.ZONASECCION).equals(Strings.NO_MOSTRAR)) return false;
		
		return true;
	}
}
