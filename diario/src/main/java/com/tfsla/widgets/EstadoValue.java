package com.tfsla.widgets;

import java.util.Locale;

import org.dom4j.Element;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import com.tfsla.opencmsdev.module.TfsConstants;
import com.tfsla.planilla.herramientas.*;

public class EstadoValue extends PropertyCmsContentValue {

	
	
	public EstadoValue() {
		super();
		// TODO Auto-generated constructor stub
	}

	public EstadoValue(I_CmsXmlDocument document, Element element, Locale locale, I_CmsXmlSchemaType type) {
		super(document, element, locale, type);
		// TODO Auto-generated constructor stub
	}

	public EstadoValue(String name, String minOccurs, String maxOccurs) {
		super(name, minOccurs, maxOccurs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getPropertyName() {
		return TfsConstants.STATE_PROPERTY;
	}

	@Override
	public String getTypeName() {
		return "Estado";
	}

	@Override
	protected String defaultStringValue() {
		return PlanillaFormConstants.REDACCION_VALUE;
	}
}
