package com.tfsla.webusersposts.helper;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.fileupload.FileItem;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.webusersnewspublisher.model.News;

public class XmlContentHelper {
	
	public XmlContentHelper(CmsObject cmsObject) throws Exception {
		this(cmsObject, false);
	}
	
	public XmlContentHelper(CmsObject cmsObject, CmsFile file) throws Exception {
		this.cmsObject = cmsObject;
		this.locale = cmsObject.getRequestContext().getLocale();
		this.encoding = cmsObject.getRequestContext().getEncoding();
		this.news = new News();
		news.setCmsObject(cmsObject);
		news.setDateCreated(new Date());
		
		if(this.xmlContent == null) {
			this.xmlContent = this.getXmlContent(file);
		}
		
		try {
			xmlContent.validateXmlStructure(new CmsXmlEntityResolver(this.cmsObject));
		} catch (CmsXmlException eXml) {
			xmlContent.setAutoCorrectionEnabled(true);
			xmlContent.correctXmlStructure(this.cmsObject);
        }
	}
	
	public XmlContentHelper(CmsObject cmsObject, Boolean isPreview) throws Exception {
		this.cmsObject = cmsObject;
		this.locale = cmsObject.getRequestContext().getLocale();
		this.encoding = cmsObject.getRequestContext().getEncoding();
		this.news = new News();
		news.setMode(isPreview);
		news.setCmsObject(cmsObject);
		news.setDateCreated(new Date());
		
		if(this.xmlContent == null) {
			this.xmlContent = this.getXmlContent();
		}
	}
	
	public void setXmlValue(String key, String value) {
		if(key != null && key.equals("autor/internalUser")) {
			news.setInternalUser(value);
			//xmlContent.addValue(cmsObject, "autor", locale, 0);
		}
		setXmlContentValue(key, value);
	}
	
	public void setXmlListValue(String keyFormat, List<String> values) {
		this.setXmlListValue(keyFormat, values, null, 0);
	}
	
	public void setXmlListValue(String keyFormat, List<String> values, int fromItem) {
		this.setXmlListValue(keyFormat, values, null, fromItem);
	}
	
	public void setXmlListValue(String keyFormat, List<String> values, String ensureKey) {
		this.setXmlListValue(keyFormat, values, ensureKey, 0);
	}
	
	public void setXmlListValue(String keyFormat, List<String> values, String ensureKey, int fromItem) {		
		int pos = fromItem;
		if(values != null && values.size() > 0 ) {
			for (String item : values) {
				if(ensureKey != null && !ensureKey.equals("")) {
					if (!xmlContent.hasValue(ensureKey, locale, pos))
						xmlContent.addValue(cmsObject, ensureKey, locale, pos);
				}
				
				setXmlContentValue(String.format(keyFormat, pos+1), item, pos+1);	
				pos++;
			}
		}
	}
	
	public void setXmlImageValues(List<FileItem> values) {
		this.setXmlImageValues(values, null);
	}
	
	public void setXmlImageValues(List<FileItem> values, List<String> values64) {
		int pos = 0;
		if(values != null && values.size() > 0) {
			for (FileItem item : values) {
				this.setXmlImageItem(pos, item.getName());
				pos++;
			}
		}
		if(values64 != null && values64.size() > 0) {
			for (String item : values64) {
				this.setXmlImageItem(pos, item);
				pos++;
			}
		}
	}
	
	public void setXmlImageValues(List<FileItem> values, List<String> imageNames, List<String> values64) {
		
	}
	
	public void setXmlImageValues(List<FileItem> values, List<String> imageNames, List<String> values64, List<String> names64) {
		int pos = 0;
		if(values != null && values.size() > 0) {
			for (FileItem item : values) {
				this.setXmlImageItem(pos, item.getName(), imageNames.get(values.indexOf(item)));
				pos++;
			}
		}
		if(values64 != null && values64.size() > 0) {
			for (String item : values64) {
				this.setXmlImageItem(pos, item, names64.get(values64.indexOf(item)));
				pos++;
			}
		}
	}
	
	public CmsXmlContent getXmlContent(String xmlContent) throws Exception {
		try {
			CmsXmlContent content = CmsXmlContentFactory.unmarshal(
				cmsObject,
				xmlContent,
				encoding,
				new CmsXmlEntityResolver(cmsObject)
			);
			this.xmlContent = content;
			return content;
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public CmsXmlContent getXmlContent(CmsFile file) throws Exception {
		try {
			CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmsObject, file);
			this.xmlContent = content;
			return content;
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public CmsXmlContent getXmlContent() throws Exception {
		if(this.xmlContent == null) {
			I_CmsResourceType resourceType = OpenCms.getResourceManager().getResourceType("noticia");
			String schema = (String)resourceType.getConfiguration().get(CmsResourceTypeXmlContent.CONFIGURATION_SCHEMA);
			CmsXmlContentDefinition contentDefinition = CmsXmlContentDefinition.unmarshal(cmsObject, schema);
			
			this.xmlContent = CmsXmlContentFactory.createDocument(
				cmsObject, 
				locale, 
				encoding, 
				contentDefinition
			);
		}
		
		return xmlContent;
	}

	public void removeXmlContentValue(String keyString) {
		this.removeXmlContentValue(keyString, 0);
	}
	
	public void removeXmlContentValue(String keyString, int index) {
		if(xmlContent.hasValue(keyString, locale)) {
			xmlContent.removeValue(keyString, locale, index);
		}
	}
	
	public void setXmlContentValue(String keyString, String valueString) {
		this.setXmlContentValue(keyString, valueString, 0);
	}
	
	public void setXmlContentValue(String keyString, String valueString, int position) {
		if(valueString == null) return;
		
		I_CmsXmlContentValue value = xmlContent.getValue(keyString, locale);
		if(value != null) {
			value.setStringValue(cmsObject, valueString);
		} else {
			xmlContent.addValue(cmsObject, keyString, locale, position).setStringValue(cmsObject, news.removeInvalidXmlCharacters(valueString));
		}
	}
	
	private void setXmlImageItem(int pos, String imageName) {
		if (pos == 0) {
			if(!xmlContent.hasValue("imagenPrevisualizacion", locale, 0))
				xmlContent.addValue(cmsObject,"imagenPrevisualizacion",locale,0);
			
			setXmlContentValue("imagenPrevisualizacion/imagen", news.getFileItemPath(imageName));
		}
		
		if(!xmlContent.hasValue("imagenesFotogaleria", locale, pos))
			xmlContent.addValue(cmsObject,"imagenesFotogaleria", locale, pos);
		
		setXmlContentValue("imagenesFotogaleria[" + (pos + 1) + "]/imagen", news.getFileItemPath(imageName));
	}
	
	private void setXmlImageItem(int pos, String imageName, String imagePath) {
		if (pos == 0) {
			if(!xmlContent.hasValue("imagenPrevisualizacion", locale, 0))
				xmlContent.addValue(cmsObject,"imagenPrevisualizacion",locale,0);
			
			setXmlContentValue("imagenPrevisualizacion/imagen", imagePath);
		}
		
		if(!xmlContent.hasValue("imagenesFotogaleria", locale, pos))
			xmlContent.addValue(cmsObject,"imagenesFotogaleria", locale, pos);
		
		setXmlContentValue("imagenesFotogaleria[" + (pos + 1) + "]/imagen", imagePath);
	}

	private News news;
	private CmsObject cmsObject;
	private Locale locale;
	private String encoding;
	private CmsXmlContent xmlContent;
}