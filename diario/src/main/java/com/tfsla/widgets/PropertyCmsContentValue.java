package com.tfsla.widgets;

import java.lang.reflect.Constructor;
import java.util.Locale;

import org.dom4j.Element;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.types.A_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import com.tfsla.exceptions.ApplicationException;
import com.tfsla.utils.CmsResourceUtils;

public abstract class PropertyCmsContentValue extends A_CmsXmlContentValue {

	protected String m_stringValue;
	
	public PropertyCmsContentValue(I_CmsXmlDocument document, Element element, Locale locale, I_CmsXmlSchemaType type) {
		super(document, element, locale, type);
		// TODO Auto-generated constructor stub
	}

	public PropertyCmsContentValue(String name, String minOccurs, String maxOccurs) {
		super(name, minOccurs, maxOccurs);
		// TODO Auto-generated constructor stub
	}

	public PropertyCmsContentValue() {
		super();
	}
	
	public  abstract String getPropertyName();
	public abstract String getTypeName();
	
	public String getStringValue(CmsObject cms) {
		try {
			CmsResource resource = this.getDocument().getFile();
			if(resource != null) {
				return cms.readPropertyObject(resource, getPropertyName(), false).getValue();
			}
			else {
				return defaultStringValue();
			}
		}
		catch (CmsException e) {
			throw new ApplicationException("No se pudo leer la property " + getPropertyName() + " en el archivo " + this.getDocument().getFile() , e);
		}
	}

	protected String defaultStringValue() {
		return null;
	}

	public void setStringValue(CmsObject cms, String value) {
		try {
			cms.writePropertyObject(CmsResourceUtils.getLink(this.getDocument().getFile()), new CmsProperty(this.getPropertyName(), value, value));
		}
		catch (NullPointerException e) {
			m_element.clearContent();
		    if (CmsStringUtil.isNotEmpty(value)) {
		    	m_element.addCDATA(value);
		    }
		    m_stringValue = value;
		}
		catch (CmsException e) {	
			throw new ApplicationException("", e);
		}
	}

	public I_CmsXmlContentValue createValue(I_CmsXmlDocument document, Element element, Locale locale) {
		Constructor constructor;
		try {
			constructor = this.getClass().getConstructor(I_CmsXmlDocument.class, Element.class, Locale.class, I_CmsXmlSchemaType.class);
			return (I_CmsXmlContentValue) constructor.newInstance(document, element, locale, this);
			
		}
		catch (Exception e) {
			throw new ApplicationException("No se pudo construir un nuevo " + this.getClass().getName() , e);
		}
	}

	/**
	 * Es el mismo que el CmsXmlStringValue
	 * No estoy 
	 */
	public String getSchemaDefinition() {
		return "<xsd:simpleType name=\"" + this.getTypeName() + "\"><xsd:restriction base=\"xsd:string\" /></xsd:simpleType>";
	}


	public I_CmsXmlSchemaType newInstance(String name, String minOccurs, String maxOccurs) {
		Constructor constructor;
		try {
			constructor = this.getClass().getConstructor(String.class, String.class, String.class);
			return (I_CmsXmlContentValue) constructor.newInstance(name, minOccurs, maxOccurs);
		}
		catch (Exception e) {
			throw new ApplicationException("No se pudo construir un nuevo " + this.getClass().getName() , e);
		}
	}


	
	
}
