package com.tfsla.rankViews.service;

import javax.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;

public abstract class A_RankDataCollector {

	public String getValue(CmsObject cms, String uid, String key) throws Exception {
		
		if (key.equals("url"))
			return uid;
		
		CmsProperty property;
		try {
			property = cms.readPropertyObject(uid, key, false);
		} catch (CmsException e) {
			throw new JspException("Error al intentar acceder a la informacion del archivo.",e);
		}
		return property.getValue();

		
	}

	public String getDateValue(CmsMessages msg, CmsObject cms, String uid, String key) throws Exception {

	CmsProperty fechaProperty = cms.readPropertyObject(uid, key, false);
	if (fechaProperty!=null)
	{ 
		String fecha = fechaProperty.getValue();
		if (fecha!=null)
			return msg.getDateTime(Long.parseLong(fecha));
		else
			return "";
	}
	else 
		return "";

	}

}
