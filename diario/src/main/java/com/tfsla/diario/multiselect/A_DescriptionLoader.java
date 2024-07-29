package com.tfsla.diario.multiselect;

import java.util.List;
import java.util.Locale;

import org.opencms.db.CmsDefaultUsers;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsEvent;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.utils.CmsResourceUtils;

public abstract class A_DescriptionLoader {

	/** A cloned cms instance that prevents the broken link remotion during unmarshalling. */
	private CmsObject m_cloneCms;

	public abstract void cmsEvent(CmsEvent event);

	public A_DescriptionLoader() {
		super();
	}

	protected boolean mustAddProperty(CmsObject cmsObject, String value, String propertyName,
			String path) {
				try {
					CmsProperty propertyValue = cmsObject.readPropertyObject(path, propertyName, false);
					
					return (propertyValue==null || propertyValue.getValue()==null || propertyValue.getValue().length()==0)
					&& value!=null && value.trim()!="";
			
				} catch (CmsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
				return false;
			}

	protected String getElementValue(CmsObject cmsObject, CmsXmlContent content, String prefix,
			int idx, String element) {
				//String xmlName = prefix + "[" + idx + "]/" + element + "[1]";
				//I_CmsXmlContentValue value = content.getValue(xmlName, Locale.ENGLISH);
				String elementValue = null;
				String xmlName = prefix + "[" + idx + "]/" + element;
				List<I_CmsXmlContentValue> values = content.getValues(xmlName,  Locale.ENGLISH);
				for (I_CmsXmlContentValue value : values) {
					if (elementValue==null)
						elementValue = value.getStringValue(cmsObject);
					else
						elementValue +=  CmsProperty.VALUE_LIST_DELIMITER + value.getStringValue(cmsObject);
				}
				
				return elementValue;
			}

	protected CmsObject getCms() throws CmsException {
		
		CmsObject cmsObject =CmsWorkplaceAction.getInstance().getCmsAdminObject();
		return OpenCms.initCmsObject(cmsObject);
	
	}

	protected CmsObject getCloneCms(CmsObject cms) throws CmsException {
	
		if (m_cloneCms == null) {
			m_cloneCms = OpenCms.initCmsObject(cms);
			m_cloneCms.getRequestContext().setRequestTime(CmsResource.DATE_RELEASED_EXPIRED_IGNORE);
		}
		return m_cloneCms;
	}

	protected CmsXmlContent getXmlContent(CmsObject cmsObject, String url)
			throws CmsXmlException, CmsException {
				
				CmsFile contentFile = cmsObject.readFile(url);
			
				CmsXmlContent content = CmsXmlContentFactory.unmarshal(getCloneCms(cmsObject), contentFile);
			
				try {	
					CmsXmlUtils.validateXmlStructure(contentFile.getContents(), new CmsXmlEntityResolver(cmsObject));
				} catch (CmsException e) {
					CmsResourceUtils.forceLockResource(cmsObject, cmsObject.getSitePath(contentFile));
					content.setAutoCorrectionEnabled(true);
				    content.correctXmlStructure(cmsObject);
				    CmsResourceUtils.unlockResource(cmsObject, cmsObject.getSitePath(contentFile), false);
				}
				return content;
			}

}