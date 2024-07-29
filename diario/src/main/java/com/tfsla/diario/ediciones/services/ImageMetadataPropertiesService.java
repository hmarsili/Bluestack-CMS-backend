package com.tfsla.diario.ediciones.services;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;

import com.adobe.xmp.XMPMeta;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.xmp.XmpDirectory;
import com.tfsla.diario.ediciones.model.ImageInformation;

public class ImageMetadataPropertiesService {
	
	public ImageMetadataPropertiesService() {
		this("", "");
	}	
	
	public ImageMetadataPropertiesService(String siteName, String publication) {
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		try {
			String copyMetadataVal = config.getParam(siteName, publication, "imageUpload", "setMetadata", "true");
			this.setMetadata = Boolean.parseBoolean(copyMetadataVal);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public ImageInformation readImageInformation(InputStream inputStream) throws Exception {
		try {
			return this.processMetadata(ImageMetadataReader.readMetadata(inputStream, -1));
		} catch(Exception e) {
			return new ImageInformation(1, 0, 0);
		}
	}
	
	public ImageInformation readImageInformation(String filePath) throws Exception {
		try {
			File file = new File(filePath);
			return this.processMetadata(ImageMetadataReader.readMetadata(file));
		} catch(Exception e) {
			return new ImageInformation(1, 0, 0);
		}
	}
		
	@SuppressWarnings("unchecked")
	public void setMetaDataProperties(String filePath, List properties) {
		if(!this.setMetadata) return;
		
		try {
			File file = new File(filePath);
			Metadata metadata = ImageMetadataReader.readMetadata(file);
			ArrayList<String> config = this.getMetaDataConfiguration();
			Hashtable<String, String> values = new Hashtable<String, String>();
			String key = "";
			for(Directory dir : metadata.getDirectories()) {
				for (Tag tag : dir.getTags()) {
					key = tag.getTagName().toLowerCase().trim().replace(" ", "");
					if(config.contains(key)) {
						String tagDescription = fixSpecialCharacters(tag.getDescription());
						       tagDescription = com.tfsla.utils.StringEncoding.fixEncoding(tagDescription);
						values.put(key, tagDescription);
					}
			    }
			}
			
			String xmpArtist = "";
			String xmpTitle = "";
			if(metadata.containsDirectoryOfType(XmpDirectory.class)) {
				try {
					Collection<XmpDirectory> dirs = metadata.getDirectoriesOfType(XmpDirectory.class);
					for(XmpDirectory dir : dirs) {
						XMPMeta meta = dir.getXMPMeta();
						if(meta.doesPropertyExist(XMP_NAMESPACE, "creator"))
							xmpArtist = meta.getArrayItem(XMP_NAMESPACE, "creator", 1).toString();
						
						if(meta.doesPropertyExist(XMP_NAMESPACE, "title"))
							xmpTitle = meta.getArrayItem(XMP_NAMESPACE, "title", 1).toString();
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			
			CmsProperty prop = null;
			
			String agency = values.get("copyrightnotice"); 
			if(agency == null || agency.trim().equals("")) agency = "";
			if(agency.trim().equals("")) agency += (values.get("credit") != null ? values.get("credit") : "");
			if(!agency.trim().equals("") && values.get("source") != null && !values.get("source").trim().equals("")) agency += ", ";
			agency += (values.get("source") != null ? values.get("source") : "");
			//if(!agency.trim().equals("") && values.get("copyright") != null && !values.get("copyright").trim().equals("")) agency += ", ";
			//agency += values.get("copyright");
			if(agency != null && agency.toLowerCase().trim().equals("null")) agency = "";
			
			prop = new CmsProperty();
			prop.setName("Agency");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(filterEmpty(agency));
			properties.add(prop);
			
			if(values.get("artist") != null && !values.get("artist").trim().equals(""))
				xmpArtist = values.get("artist");
			if(xmpArtist != null && xmpArtist.toLowerCase().trim().equals("null")) xmpArtist = "";
			prop = new CmsProperty();
			prop.setName("Author");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(filterEmpty(xmpArtist));
			properties.add(prop);
			
			String keywords = "";
			if(values.get("keywords") != null && !values.get("keywords").equals("")) {
				keywords = StringUtils.join(values.get("keywords").split("\\|"), ", ");
				keywords = StringUtils.join(keywords.split(";"), ", ");
				if(keywords != null && keywords.toLowerCase().trim().equals("null")) keywords = "";
				prop = new CmsProperty();
				prop.setName("Keywords");
				prop.setAutoCreatePropertyDefinition(true);
				prop.setStructureValue(filterEmpty(keywords));
				properties.add(prop);
			}
			
			String title = values.get("headline")==null?"": values.get("headline").trim();
			
			if(!title.equals("") && values.get("title") != null && !values.get("title").trim().equals("")) 
				title += " - " + values.get("title").trim();
			
			if(title.trim().equals("") && !xmpTitle.equals("")) title = xmpTitle;
			
			//if(title != null && title.toLowerCase().trim().equals("null")) title = "";
			
			prop = new CmsProperty();
			prop.setName("Title");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(filterEmpty(title));
			properties.add(prop);
			
			String description = values.get("imagedescription")!=null?values.get("imagedescription").trim():"";
			String specialInstructions = values.get("specialinstructions") != null? values.get("specialinstructions").trim():"";
			if (!description.equals("") && (!description.toLowerCase().equals("null"))) {
				if (!specialInstructions.equals("") && !specialInstructions.toLowerCase().equals("null"))
					description += "-" + specialInstructions;
			} else if (!specialInstructions.equals("") && !specialInstructions.toLowerCase().equals("null"))
					description = specialInstructions;
			else
				description ="";
					
			prop = new CmsProperty();
			prop.setName("Description");
			prop.setAutoCreatePropertyDefinition(true);
			prop.setStructureValue(filterEmpty(description));
			properties.add(prop);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public JSONObject getMetadata(CmsObject cmsObject, CmsResource resource) {
		return this.getMetadata(cmsObject, resource, null,cmsObject.getRequestContext().getEncoding());
	}
	
	public JSONObject getMetadata(CmsObject cmsObject, String resourcePath, String encoding) {
		return this.getMetadata(cmsObject, null, resourcePath, encoding);	
	}
	
	public JSONObject getMetadata(CmsObject cmsObject, String resourcePath) {
		return this.getMetadata(cmsObject, null, resourcePath,cmsObject.getRequestContext().getEncoding());
	}
	
	private String filterEmpty(String value) {
		if(value == null) return "";
		String lower = value.trim().toLowerCase();
		if(lower.equals("null") || lower.equals("undefined")) return "";
		return value;
	}
	
	private JSONObject getMetadata(CmsObject cmsObject, CmsResource resource, String resourcePath, String encoding) {
		JSONObject ret = new JSONObject();
		CmsProperty property = null;
		ArrayList<String> properties = new ArrayList<String>();
		properties.add("Agency");
		properties.add("Author");
		properties.add("Keywords");
		properties.add("Title");
			
		for(String propertyName : properties) {
			property = null;
			try {
				if(resource != null)
					property = cmsObject.readPropertyObject(resource, propertyName, false);
				else
					property = cmsObject.readPropertyObject(resourcePath, propertyName, false);
			} catch(Exception e) {
				e.printStackTrace();
			}
			try {
				
				if (!cmsObject.getRequestContext().getEncoding().equals(encoding)) {
					String value = (property != null && property.getValue()!=null ? property.getValue() : "");
					if (isWellEncoded(value,encoding)) 
						ret.put(propertyName, value);
					else
						ret.put(propertyName, new String(value.getBytes(cmsObject.getRequestContext().getEncoding()), encoding));
				}
				else{
					String propertyVal = "";
					
					if(property != null && property.getValue()!=null){
						propertyVal = fixSpecialCharacters(property.getValue());
						propertyVal = com.tfsla.utils.StringEncoding.fixEncoding(propertyVal);
					}
					
					ret.put(propertyName,propertyVal);
				}
			} catch (UnsupportedEncodingException e) {
				ret.put(propertyName, (property != null ? property.getValue() : ""));
				e.printStackTrace();
			}
			LOG.debug("property " + propertyName + " val " + (property != null ? property.getValue() : ""));
		}
		return ret;
	}

	public static boolean isWellEncoded( String input, String encoding) {

//	    CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
	    CharsetEncoder encoder = Charset.forName(encoding).newEncoder();
	    ByteBuffer tmp;
	    try {
	        tmp = encoder.encode(CharBuffer.wrap(input));
	    }

	    catch(CharacterCodingException e) {
	        return false;
	    }
/*
	    try {
	        decoder.decode(tmp);
	        return true;
	    }
	    catch(CharacterCodingException e){
	        return false;
	    } 
*/
	    return true;
	}

	protected ArrayList<String> getMetaDataConfiguration() {
		ArrayList<String> ret = new ArrayList<String>();
		ret.add("artist");
		ret.add("copyright");
		ret.add("copyrightnotice");
		ret.add("specialinstructions");
		ret.add("credit");
		ret.add("keywords");
		ret.add("source");
		ret.add("imagedescription");
		ret.add("headline");
		return ret;
	}
	
	protected ImageInformation processMetadata(Metadata metadata) throws Exception {
		Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
		JpegDirectory jpegDirectory = (JpegDirectory)metadata.getFirstDirectoryOfType(JpegDirectory.class);
		
		if(directory == null || jpegDirectory == null) return null;
		 
		int orientation = 1;
		try {
			
			if(directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION))
				orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	 
		int width = jpegDirectory.getImageWidth();
		int height = jpegDirectory.getImageHeight();

		return new ImageInformation(orientation, width, height);
	}

	private Log LOG = CmsLog.getLog(this);
	protected static final String XMP_NAMESPACE = "http://purl.org/dc/elements/1.1/";
	protected Boolean setMetadata = true;
	
	protected String fixSpecialCharacters(String input)
	{
		if(input.equals(""))
			return input;
		
		Pattern frenchPattern = Pattern.compile("(?i)[âàçéêëèïîôûùœ�]");
	    if(frenchPattern.matcher(input).find()){
	    	
	    	String fixed = input.replaceAll("â","a");
	    	       fixed = fixed.replaceAll("à","a");
	    	       fixed = fixed.replaceAll("ç","c");
	    	       fixed = fixed.replaceAll("é","e");
	    	       fixed = fixed.replaceAll("ê","e");
	    	       fixed = fixed.replaceAll("ë","e");
	    	       fixed = fixed.replaceAll("è","e");
	    	       fixed = fixed.replaceAll("ï","i");
	    	       fixed = fixed.replaceAll("î","i");
	    	       fixed = fixed.replaceAll("ô","o");
	    	       fixed = fixed.replaceAll("û","u");
	    	       fixed = fixed.replaceAll("ù","u");
	    	       fixed = fixed.replaceAll("œ","oi");
	    	       fixed = fixed.replaceAll("�","");
	    	return fixed;
	    	
	    }else
	    	return input;
	}
}
