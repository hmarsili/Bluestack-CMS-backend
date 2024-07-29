package com.tfsla.diario.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspTagLink;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.NoticiasService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.utils.TfsTagsUtil;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;
import com.tfsla.diario.videoCollector.LuceneVideoCollector;
import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.utils.UrlLinkHelper;

public class TfsPlaylist {
	
	
    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(TfsNoticia.class);

    protected transient I_CmsXmlDocument m_content;
    protected transient Locale locale;
    protected transient CmsFile resource;
    
    protected CmsObject cms = null;
    PageContext pageContext = null;
    
    
    private TipoEdicion tEdicion=null;
	Map<String,Boolean> categories=null;
	Map<String,Boolean> subCategories=null;
	List<String> lCategories=null;
	Map<String,String> elements=null;
	Map<String,String> properties=null;
	Map<String,String> titles=null;
	
	
	public TfsPlaylist() {
		categories=null;
		subCategories=null;
		lCategories=null;
		elements=null;
		properties = null;
	    m_content = null;
	    locale = null;
	    resource = null;
	    cms = null;
	    titles = null;
	}
	
	public TfsPlaylist(CmsObject m_cms, I_CmsXmlDocument m_content, Locale locale, PageContext pageContext) {
		categories=null;
		subCategories=null;
		lCategories=null;
		elements=null;
		properties = null;
	    this.m_content = m_content;
	    this.locale = locale;
	    this.pageContext = pageContext;
	    resource = m_content.getFile();
	    cms = m_cms;
	    tEdicion = getTipoEdicion();
	    titles = null;
	    if (getIsAutomatica().equals("true")) {
	    	getVideosAutomatically();
	    }
	}

	private void getVideosAutomatically() {
		List<String> categorias = getCategories();
		String categoriasStr = String.join(",", categorias);
		String tags = getTags();
		Map<String,Object> parameters = new HashMap<String,Object>();
		
		
		String sizeString = getCantidadAutomatica();
		int size = 100;
		if (sizeString != null) {
			size = Integer.valueOf(sizeString);
		}
		String order = "user-modification-date desc";
		
		parameters.put("size",size);
		parameters.put("page", 1);
		parameters.put("agency",null);		
		parameters.put("onnews",null);
		parameters.put("order",order);
		parameters.put("filter",null);
		parameters.put("advancedFilter",null);
		parameters.put("searchIndex",null);
		parameters.put("tags",tags.equals("")? null : tags);
		parameters.put("category",categoriasStr.equals("")? null : categoriasStr);		
		parameters.put("formats",null);		
		parameters.put("from",null);
		parameters.put("to",null);
		parameters.put("publication",null);
		parameters.put("type",null);
		parameters.put("classification", null);

		int paramsWithValues = 
			1  + //size
			(order!=null ? 1 : 0) +
			(categorias!=null ? 1 : 0) +
			(tags!=null ? 1 : 0) +
			//(from!=null ? 1 : 0) +
			//(to!=null ? 1 : 0) +
			1 ; //page
			

		parameters.put("params-count",(Integer)paramsWithValues);

		LuceneVideoCollector collector = new LuceneVideoCollector();
		
		List<CmsResource> videos = collector.collectVideos(parameters,cms);
		try {
			
			for (CmsResource cmsResource : videos) {
				if ( cmsResource.getTypeId() == OpenCms.getResourceManager().getResourceType("video-link").getTypeId()) {
					addVideoFlahsElement(TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash"), cmsResource);
					
				} else if (cmsResource.getTypeId() == OpenCms.getResourceManager().getResourceType("videoVod-link").getTypeId()) {
					addVideoFlahsElement(TfsXmlContentNameProvider.getInstance().getTagName("news.vod.flash"), cmsResource);
				} else if (cmsResource.getTypeId() == OpenCms.getResourceManager().getResourceType("video-youtube").getTypeId()) {
					addVideoYoutubeElement(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube"), cmsResource);
						
				} else if (cmsResource.getTypeId() == OpenCms.getResourceManager().getResourceType("videoVod-youtube").getTypeId()) {
					addVideoYoutubeElement(TfsXmlContentNameProvider.getInstance().getTagName("news.vod.youTube"), cmsResource);
					
				} else if (cmsResource.getTypeId() == OpenCms.getResourceManager().getResourceType("video-embedded").getTypeId()) {
					addVideoEmbeddedElement(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded"), cmsResource);
					
				} else if (cmsResource.getTypeId() == OpenCms.getResourceManager().getResourceType("videoVod-embedded").getTypeId()) {
					addVideoEmbeddedElement(TfsXmlContentNameProvider.getInstance().getTagName("news.vod.embedded"), cmsResource);
				}
				
			}
		} catch (Exception e) {		
			LOG.error("PlaylistTag - error al agregar el video: " + " content: "+ this.m_content, e);
		}
		
	}

	private void addVideoEmbeddedElement(String tagName, CmsResource cmsResource) {
		CmsXmlContent content;
		try {
			CmsProperty id = cms.readPropertyObject(cmsResource, "video-code", false);
			int index = getElementCountWithValue(tagName,TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode"));
			
			if (id.getValue()!=null) {
				content = CmsXmlContentFactory.unmarshal(cms, resource);
				content.addValue(cms, tagName, Locale.ENGLISH,index++);
				content.getValue(tagName +"[" + index + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode"),
								Locale.ENGLISH).setStringValue(cms, id.getValue());
				resource.setContents(content.marshal());
				this.m_content = content;
			}
		} catch (CmsException e) {
			LOG.error("PlaylistTag - error al agregar el video: " + tagName, e);
		}
		
	}

	private void addVideoYoutubeElement(String tagName, CmsResource cmsResource)  {
		CmsXmlContent content;
		try {
			CmsProperty id = cms.readPropertyObject(cmsResource, "video-code", false);
			int index = getElementCountWithValue(tagName,TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube.id"));
			if (id.getValue() != null) {
				content = CmsXmlContentFactory.unmarshal(cms, resource);
				content.addValue(cms, tagName, Locale.ENGLISH,index++);
				content.getValue(tagName +"[" + index + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube.id"),
								Locale.ENGLISH).setStringValue(cms, id.getValue());
				resource.setContents(content.marshal());
				this.m_content = content;
			}
		} catch (CmsException e) {
			LOG.error("PlaylistTag - error al agregar el video: " + tagName, e);
		}
	}

	private void addVideoFlahsElement(String tagName, CmsResource cmsResource) {
		CmsXmlContent content = null;
		try {
			int index = getElementCountWithValue(tagName,TfsXmlContentNameProvider.getInstance().getTagName("news.video.video"));
			
			content = CmsXmlContentFactory.unmarshal(cms, resource);
			content.addValue(cms, tagName, Locale.ENGLISH,index++);
			content.getValue(tagName +"[" + index + "]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.video.video"),
							Locale.ENGLISH).setStringValue(cms, cms.getRequestContext().removeSiteRoot(cmsResource.getRootPath()));
			resource.setContents(content.marshal());
			this.m_content = content;
		} catch (CmsXmlException e) {
			LOG.error("PlaylistTag - error al agregar el video: " + tagName + " content " + content, e);
		}
		
		
	}

	public Map<String,String> getProperty() {
		if (properties==null) {
			properties = new HashMap<String, String>();
			
				List<CmsProperty> props;
				try {
					props = cms.readPropertyObjects(resource, true);
				
				for (CmsProperty prop : props )
					properties.put(prop.getName(),prop.getValue());
			} catch (CmsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return properties;
	}
	
	public Map<String,String> getGenericElementValue() {
		if (elements==null) {
			elements = new HashMap<String, String>();
			Locale locale = cms.getRequestContext().getLocale();
			for (String key :m_content.getNames(locale)) {
				if (m_content.getValue(key, locale).isSimpleType())
						elements.put(key, getElementValue(key));
			}
		}
		return elements; //getElementValue();
	}

	public boolean getIsExpired() {
		Date date = new Date();
		if(isDateExpiredSet()) {
			if(getLongDateExpired() < date.getTime()) return true;
		}
		if(isDateReleasedSet()) {
			if(getLongDateReleased() > date.getTime()) return true;
		}
		return false;
	}
	
	public boolean isDateExpiredSet() {
		return (resource.getDateExpired() != CmsResource.DATE_EXPIRED_DEFAULT);
	}
	
	public boolean isDateReleasedSet() {
		return (resource.getDateReleased() != CmsResource.DATE_RELEASED_DEFAULT);
	}

	public long getLongDateExpired() {
		return resource.getDateExpired();
	}

	public Date getDateExpired() {
		TimeZone zone = TimeZone.getDefault();
        GregorianCalendar cal = new GregorianCalendar(zone, locale);
        cal.setTimeInMillis(resource.getDateExpired());
        return cal.getTime();
	}

	public long getLongDateReleased() {
		return resource.getDateReleased();
	}

	public Date getDateReleased() {
		TimeZone zone = TimeZone.getDefault();
        GregorianCalendar cal = new GregorianCalendar(zone, locale);
        cal.setTimeInMillis(resource.getDateReleased());
        return cal.getTime();
	}

	public int getCategoriescount() {
			return  m_content.getIndexCount(TfsXmlContentNameProvider.getInstance().getTagName("news.categories"), locale);
	}
	
	public Map<String,Boolean> getHascategory() {
		if (categories==null) {
			String key = TfsXmlContentNameProvider.getInstance().getTagName("news.categories");
			categories = new HashMap<String, Boolean>();
			int lastElement =  m_content.getIndexCount(key, locale);
			for (int j=1;j<=lastElement;j++){
				try {
					String categoria = m_content.getStringValue(cms,  key + "[" + j +"]" , locale);
					categories.put(categoria, true);
				} catch (CmsXmlException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return categories;
	}
	
	public List<String> getCategories() {
		if (lCategories==null) {
			String key = TfsXmlContentNameProvider.getInstance().getTagName("news.categories");
			lCategories = new ArrayList<String>();
			int lastElement =  m_content.getIndexCount(key, locale);
			for (int j=1;j<=lastElement;j++){
				try {
					String categoria = m_content.getStringValue(cms,  key + "[" + j +"]" , locale);
					lCategories.add(categoria);
				} catch (CmsXmlException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return lCategories;
	}

	public Map<String,Boolean> getIsinsidecategory() {
		if (subCategories==null) {
			String key = TfsXmlContentNameProvider.getInstance().getTagName("news.categories");

			subCategories = new HashMap<String, Boolean>();
			int lastElement =  m_content.getIndexCount(key, locale);
			for (int j=1;j<=lastElement;j++){
				try {
					String categoria = m_content.getStringValue(cms,  key + "[" + j +"]" , locale);
					
					String[] subCategoria = categoria.split("/");
					String categ = "";
					for (String part : subCategoria)
					{
						categ += part + "/";
						subCategories.put(categ, true);
					}
				} catch (CmsXmlException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return subCategories;
	}

	public String getLocalpath() {
        return cms.getSitePath(resource);
	}

	public String getAbsolutelink() {
		String link="";
        String external = UrlLinkHelper.getExternalLink(resource, cms);
	
        if (external!=null && !external.isEmpty())
        	link = external;
        else
        	link = UrlLinkHelper.getUrlFriendlyLink(resource, cms, this.pageContext.getRequest(),false);
	
        return link;
	}

	public String getLink() {
		String link="";
        String external = UrlLinkHelper.getExternalLink(resource, cms);
	
        if (external!=null && !external.isEmpty())
        	link = external;
        else
        	link = UrlLinkHelper.getUrlFriendlyLink(resource, cms, this.pageContext.getRequest());
	
        return link;
	}

	public String getLegacylink() {
		String link="";
        String external = UrlLinkHelper.getExternalLink(resource, cms);
    	
        if (external!=null && !external.isEmpty())
        	link = external;
        else
        	link =CmsJspTagLink.linkTagAction("/" + CmsResourceUtils.getLink(resource),  pageContext.getRequest());
	
        return link;
	}
	
	public String getState() {
		return getPropertyValue("s tate",false);
	}
	
	
		
	public String getTitle() {
		String content = "";
		String key = TfsXmlContentNameProvider.getInstance().getTagName("news.title");
		
		Locale locale = cms.getRequestContext().getLocale();
			
		try {
			content = m_content.getStringValue(cms, key + "[1]", locale);
		} catch (CmsXmlException e) {
			e.printStackTrace();
		}
			
		return content; 
	}
	
	
	public CmsUser getCmsUser() throws CmsXmlException{		
		CmsUUID uui = m_content.getFile().getUserCreated();
		CmsUser user = null;
		String author = "";
		try {
			user = cms.readUser(uui);
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
		return user; 
	}
	
	public String getUserName(CmsFile fileCms) throws CmsXmlException{		
		CmsUUID uui = fileCms.getUserCreated();
		CmsUser user;
		String author = "";
		try {
			user = cms.readUser(uui);
			author = user.getFirstname();
			//Verificar cual es el que corresponde
			//author = user.getName();
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
		return author; 
	}
	
	public int getTitlesCount() {
		return m_content.getIndexCount(TfsXmlContentNameProvider.getInstance().getTagName("news.title"), cms.getRequestContext().getLocale());
	}
	
		
	public Map<String,String> getTitles() {
		if (titles==null) {
			String key = TfsXmlContentNameProvider.getInstance().getTagName("news.title");
			titles = new HashMap<String,String>();
			int lastElement =  m_content.getIndexCount(key, locale);
			for (int j=1;j<=lastElement;j++){
				try {
					String title = m_content.getStringValue(cms,  key + "[" + j +"]" , locale);
					titles.put(""+j+"",title);
				} catch (CmsXmlException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
			
		return titles;
	}
	
	public String getTags() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.keywords")); //claves
	}
	
	public String getPeople() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.people")); //personas
	}
	
	public String getIsAutomatica() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.playlist.playlistAutomatica") + "/" +
				TfsXmlContentNameProvider.getInstance().getTagName("news.playlist.automatica"));	
	}
	
	public String getCantidadAutomatica() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.playlist.playlistAutomatica") + "/" +
				TfsXmlContentNameProvider.getInstance().getTagName("news.playlist.cantidadVideos"));	
	}
			
	public int getVideoscount() {
		return getVideosflashcount() + getVideosdownloadcount() + getVideosyoutubecount() + getVideosembeddedcount();
		//NoticiasService nService = new NoticiasService();
		//return nService.cantidadDeVideos(cms, resource);
	}
	
	public int getVideosflashcount() {
		return getElementCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.video.video")
				);
	}

	public int getVideosdownloadcount() {
		return getElementCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.videodownload"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.video.video")
				);
	}

	public int getVideosyoutubecount() {
		return getElementCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube.id")
				);
	}

	public int getVideosembeddedcount() {
		return getElementCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode")
				);
	}
	
	public int getVideoslistcount() {
		return getVideoslistflashcount() + getVideoslistdownloadcount() + getVideoslistyoutubecount() + getVideoslistembeddedcount();
	}
	
	public int getVideoslistflashcount() {
		return getElementListCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.video.video")
				);
	}

	public int getVideoslistdownloadcount() {
		return getElementListCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.videodownload"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.video.video")
				);
	}

	public int getVideoslistyoutubecount()
	{
		return getElementListCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube.id")
				);
	}

	public int getVideoslistembeddedcount() {
		return getElementListCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode")
				);
	}
	
		
	public boolean isImagepreviewset() {
		String xmlName = TfsXmlContentNameProvider.getInstance().getTagName("news.imagepreview")+"[1]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.image.image")+"[1]";
		NoticiasService nService = new NoticiasService();
		boolean isImagepreviewset = nService.tieneImagenPrevisualizacion(cms, resource, xmlName);            
		
		return (isImagepreviewset);
	}
		

	public TipoEdicion getTipoEdicion() {
        TipoEdicionService tService =  new TipoEdicionService();
        
        TipoEdicion tEdicion=null;
           
		try {
			tEdicion = tService.obtenerTipoEdicion(cms, cms.getSitePath(resource));
		} catch (Exception e) {
			LOG.error("Publication not found", e);
			
		}
		return tEdicion;
	}
	
	public String getBody() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.body")); //cuerpo
	}
	
	public String getTargetLink() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.target"));
	}
	
	
	public Date getLastModificationDate() {
		String dateLong = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.lastmodification"));
        Date uModif = new Date(Long.parseLong(dateLong));
        
		return uModif;
	}
	
	public Date getCreationDate() {
		Long dateLong = resource.getDateCreated();
        Date DateCreate = new Date(dateLong);
        
		return DateCreate;
	}
	
	public Date getModificationDate() {
		Long dateLong = resource.getDateLastModified();
        Date DateModification = new Date(dateLong);
        
		return DateModification;
	}
	
	public Date getFirstPublishDate() {
		Date firstPublishDate = null;
		
		if(!resource.getState().equals(CmsResource.STATE_NEW)) {
			String dateStr = getPropertyValue("firstPublishDate",false);
			firstPublishDate = null;
			
			if(dateStr!=null && !dateStr.equals("")) {
				 firstPublishDate = new Date(Long.parseLong(dateStr));
			} else {
				 String dateLongStr = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.lastmodification"));
				 firstPublishDate = new Date(Long.parseLong(dateLongStr));
			}
		}
		
		return firstPublishDate;
	}
	
	public Date getLastPublishDate() {
		Date publishDate = null;
		
		if(!resource.getState().equals(CmsResource.STATE_NEW))
			publishDate =  TfsTagsUtil.getPublishResourceDate(getLocalpath(),cms);
		
		return publishDate;
	}
	
	protected String getPropertyValue(String propertyName, boolean search) {
	
	    CmsProperty property=null;
		try {
			property = cms.readPropertyObject(resource, propertyName, search);
		} catch (CmsException e) {
			LOG.debug("Error reading property " + propertyName,e);
		}
	    
	    if (property!=null)
	    	return property.getValue("");
	    
	    return "";
	}

	protected int getElementCountWithValue(String key, String controlKey, String value) {
		Locale locale = cms.getRequestContext().getLocale();
		int total = m_content.getIndexCount(key, locale);
		
		int different = 0;
		for (int j=1;j<=total;j++) {
			String controlValue;
			try {
				controlValue = m_content.getStringValue(cms, key + "[" + j + "]/" + controlKey, locale);
			
				if (controlValue==null || !controlValue.trim().equals(value))
					different ++;
			} catch (CmsXmlException e) {
				LOG.debug("Error reading content value " + key + "[" + j + "]/" + controlKey + " on content " + m_content.getFile().getRootPath(),e);

			}
		}
		
		
		return total - different;
	}

	protected int getElementCountWithValue(String key, String controlKey) {
		Locale locale = cms.getRequestContext().getLocale();
		int total = m_content.getIndexCount(key, locale);
		
		int blank = 0;
		for (int j=1;j<=total;j++) {
			String controlValue;
			try {
				controlValue = m_content.getStringValue(cms, key + "[" + j + "]/" + controlKey, locale);
			
				if (controlValue==null || controlValue.trim().equals(""))
					blank ++;
			} catch (CmsXmlException e) {
				LOG.debug("Error reading content value " + key + "[" + j + "]/" + controlKey + " on content " + m_content.getFile().getRootPath(),e);

			}
		}
		
		
		return total - blank;
	}
	protected String getElementValue(String elementName) {    
		try {
	    	String value = m_content.getStringValue(cms, elementName, locale);
	    	if (value==null)
	    	{
	    		value = "";
	    		LOG.debug("Content value " + elementName + "not found on content" + m_content.getFile().getRootPath());
	    	}
			return value;
		} catch (CmsXmlException e) {
			LOG.error("Error reading content value " + elementName + " on content " + m_content.getFile().getRootPath(),e);
		}
	
		return "";
	}
	
	protected int getElementListCountWithValue(String key, String controlKey) {
		Locale locale = cms.getRequestContext().getLocale();
		String itemList = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList");
		
		int totalLists = m_content.getIndexCount(itemList, locale);
		int totalwithValues = 0;
		
		for(int i=1;i<=totalLists;i++) {
			
			int blank = 0;
			int total = m_content.getIndexCount(itemList+ "[" +i+ "]/" + key, locale);
			
			for (int j=1;j<=total;j++) {
				String controlValue;
				try {
					controlValue = m_content.getStringValue(cms,itemList+ "[" +i+ "]/" + key + "[" + j + "]/" + controlKey, locale);
				
					if (controlValue==null || controlValue.trim().equals(""))
						blank ++;
				} catch (CmsXmlException e) {
					LOG.debug("Error reading content value " + itemList+ "[" +i+ "]/" + key + "[" + j + "]/" + controlKey + " on content " + m_content.getFile().getRootPath(),e);

				}
			}
			
			totalwithValues = totalwithValues + total - blank;
		}
		
		return totalwithValues;
	}
	
	
	/* properties */

	
	/* Getters & Setters. */

	public void setCms(CmsObject cms) {
		this.cms = cms;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public void setM_content(I_CmsXmlDocument m_content) {
		this.m_content = m_content;
	}

	public void setResource(CmsFile resource) {
		this.resource = resource;
	}
	
	public I_CmsXmlDocument getContent() {
		return m_content;
	}

	public CmsFile getFile() {
		return m_content.getFile();
	}

	public Locale getLocale() {
		return locale;
	}
	public TipoEdicion getPublicacion() {
		return tEdicion;
	}	
	
	public int getPublicacionnotaid() {
		return tEdicion.getId();
	}

	public String getPublicacionnotanombre() {
		return tEdicion.getNombre();
	}
	
	public String getImagePrevisualization () {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.imagepreview") + "/" +
				TfsXmlContentNameProvider.getInstance().getTagName("news.image.image")); //	
	}
	
	public String getZonePriority()
	{
		return getPropertyValue("homepriority",false);
	}
	
	public String getZone()
	{
		return getPropertyValue("homezone",false);
	}
	
	
	
}

