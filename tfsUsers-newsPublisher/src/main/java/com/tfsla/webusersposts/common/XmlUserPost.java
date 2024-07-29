package com.tfsla.webusersposts.common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.opencms.file.CmsObject;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.types.I_CmsXmlContentValue;

/**
 * Provides an interface to interact with a post XmlContent 
 */
public class XmlUserPost {
	
	public XmlUserPost(CmsXmlContent xmlContent, CmsObject cms) {
		this(xmlContent, cms, new UserPost());
	}
	
	public XmlUserPost(CmsXmlContent xmlContent, CmsObject cms, UserPost userPost) {
		this.xmlContent = xmlContent;
		this.cms = cms;
		this.locale = cms.getRequestContext().getLocale();
		this.userPost = userPost;
	}

	public String getModerationMessage() {
		if(this.userPost != null)
			return this.userPost.getModerationMessage();
		return "";
	}
	
	public PostStatus getStatus() {
		if(this.userPost != null)
			return this.userPost.getStatus();
		return null;
	}
	
	public String getRelativeurl() {
		if(this.userPost != null && this.userPost.getUrl() != null)
			return this.userPost.getUrl().replace(this.userPost.getSite(), "");
		return "";
	}
	
	public String getUrl() {
		if(this.userPost != null)
			return this.userPost.getUrl();
		return "";
	}
	
	public String getTitle() {
		return getStringValue("titulo");
	}
	
	public Date getCreationDate() {
		if(this.userPost != null)
			return this.userPost.getCreationDate();
		return null;
	}
	
	public String getDate() {
		return getStringValue("ultimaModificacion");
	}
	
	public String getFormattedDate(String format) {
		Date uModif = new Date(Long.parseLong(this.getDate()));
		return new SimpleDateFormat(format).format(uModif);
	}
	
	public String getFormattedDate(String format, String language) {
		Date uModif = new Date(Long.parseLong(this.getDate()));
		return new SimpleDateFormat(format, new Locale(language)).format(uModif);
	}
	
	public String getFormattedDate(String format, String language, String country) {
		Date uModif = new Date(Long.parseLong(this.getDate()));
		return new SimpleDateFormat(format, new Locale(language, country)).format(uModif);
	}
	
	public String getBody() {
		return getStringValue("cuerpo");
	}
	
	public String getVolanta() {
		return getStringValue("volanta");
	}
	
	public String getAuthor() {
		return getStringValue("autor[1]/internalUser");
	}
	
	public String getPreviewImage() {
		return getStringValue("imagenPrevisualizacion[1]/imagen");
	}
	
	public String getKeywords() {
		String value = getStringValue("claves");
		if(value != null && value.trim().endsWith(",")) {
			value = value.trim();
			value = value.substring(0, value.length()-1);
		}
		return value;
	}
	
	public List<String> getSources() {
		return getListValue("fuente", "fuente[%s]/nombre");
	}
	
	public List<String> getImages() {
		List<String> images = getListValue("imagenesFotogaleria", "imagenesFotogaleria[%s]/imagen");
		List<String> ret = new ArrayList<String>();
		for(String img : images) {
			if(img != null && !img.equals(""))
				ret.add(img);
		}
		return ret;
	}
	
	public List<String> getVideos() {
		return getListValue("videoEmbedded", "videoEmbedded[%s]/codigo");
	}
	
	public CmsXmlContent getXml() {
		return this.xmlContent;
	}
	
	public List<String> getListValue(String valueName) {
		return this.getListValue(valueName, null);
	}
	
	public List<String> getListValue(String valueName, String pathFormat) {
		List<I_CmsXmlContentValue> values = xmlContent.getValues(valueName, locale);
		List<String> ret = new ArrayList<String>();
		if(pathFormat != null && !pathFormat.equals("")) {
			for(int i=0; i < values.size(); i++) {
				String key = String.format(pathFormat, i+1);
				ret.add(xmlContent.getStringValue(cms, key, locale));
			}
		} else {
			for(I_CmsXmlContentValue value : values) {
				ret.add(value.getStringValue(cms));
			}
		}
		return ret;
	}
	
	public String getStringValue(String valueName) {
		I_CmsXmlContentValue value = xmlContent.getValue(valueName, locale);
		if(value != null) return value.getStringValue(cms);
		return "";
	}
	
	private UserPost userPost;
	private Locale locale;
	private CmsObject cms;
	private CmsXmlContent xmlContent;
}
