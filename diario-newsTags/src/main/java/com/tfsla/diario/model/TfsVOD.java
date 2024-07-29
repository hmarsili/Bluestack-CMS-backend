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
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.NoticiasService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsVOD {

	  /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(TfsVOD.class);

    protected transient I_CmsXmlDocument m_content;
    protected transient Locale locale;
    protected transient CmsFile resource;
    
    protected CmsObject cms = null;
    PageContext pageContext = null;
    
    private TipoEdicion tEdicion=null;
	Map<String,Boolean> categories=null;
	Map<String,Boolean> subCategories=null;
	List<String> lCategories=null;
	List<String> lEpisodios=null;
	List<String> lSeason=null;
	
	Map<String,String> elements=null;


	public TfsVOD(CmsObject m_cms, I_CmsXmlDocument m_content, Locale locale, PageContext pageContext) {
		categories=null;
		subCategories=null;
		lCategories=null;
		elements=null;
	    this.m_content = m_content;
	    this.locale = locale;
	    this.pageContext = pageContext;
	    resource = m_content.getFile();
	    cms = m_cms;
	    tEdicion = getTipoEdicion();
	}
	
	
	
	public I_CmsXmlDocument getM_content() {
		return m_content;
	}



	public void setM_content(I_CmsXmlDocument m_content) {
		this.m_content = m_content;
	}



	public Locale getLocale() {
		return locale;
	}



	public void setLocale(Locale locale) {
		this.locale = locale;
	}



	public CmsFile getResource() {
		return resource;
	}



	public void setResource(CmsFile resource) {
		this.resource = resource;
	}



	public CmsObject getCms() {
		return cms;
	}



	public void setCms(CmsObject cms) {
		this.cms = cms;
	}



	public PageContext getPageContext() {
		return pageContext;
	}



	public void setPageContext(PageContext pageContext) {
		this.pageContext = pageContext;
	}



	public TipoEdicion gettEdicion() {
		return tEdicion;
	}



	public void settEdicion(TipoEdicion tEdicion) {
		this.tEdicion = tEdicion;
	}



	public Map<String, Boolean> getHascategory() {
		if (categories==null)
		{
			
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
		if (lCategories==null)
		{
			
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

	public Map<String, Boolean> getSubCategories() {
		return subCategories;
	}



	public void setSubCategories(Map<String, Boolean> subCategories) {
		this.subCategories = subCategories;
	}



	public List<String> getlCategories() {
		return lCategories;
	}



	public void setlCategories(List<String> lCategories) {
		this.lCategories = lCategories;
	}



	public Map<String, String> getElements() {
		return elements;
	}



	public void setElements(Map<String, String> elements) {
		this.elements = elements;
	}



	public Map<String,String> getGenericElementValue(){
		if (elements==null) {
			elements = new HashMap<String, String>();
			Locale locale = cms.getRequestContext().getLocale();
			for (String key :m_content.getNames(locale))
			{
				if (m_content.getValue(key, locale).isSimpleType())
						elements.put(key, getElementValue(key));
				
			}
		}
		return elements;
	}

	protected String getElementValue(String elementName) {    
		try {
	    	String value = m_content.getStringValue(cms, elementName, locale);
	    	if (value==null) {
	    		value = "";
	    		LOG.debug("Content value " + elementName + "not found on content" + m_content.getFile().getRootPath());
	    	}
			return value;
		} catch (CmsXmlException e) {
			LOG.error("Error reading content value " + elementName + " on content " + m_content.getFile().getRootPath(),e);
		}
	
		return "";
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
	
	public String getState(){
		return String.valueOf(this.m_content.getFile().getState().getState());
	}
	
	public String getImagePrevisualization () {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.imagepreview") + "/" +
				TfsXmlContentNameProvider.getInstance().getTagName("news.image.image")); //	
	}
	
	public CmsFile getFile() {
		return m_content.getFile();
	}

	public String getTags() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.keywords")); //claves
	}
	
	public String getPeople() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.people")); //personas
	}
	
	public String getCreators() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.vod.creators")); //creadores
	}
	
	public String getDirectors() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.vod.directors")); //directores
	}
	
	public String getStyle() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.event.style")); //estilo
	}
	
	
	public String getCalification() {
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.vod.classification")); //calificacion
	}
	
	public String getType() {
		return OpenCms.getResourceManager().getResourceType(resource).getTypeName();
		
	}
	
	public long getLongDateExpired() {
		return resource.getDateExpired();
	}
	
	public boolean isDateExpiredSet() {
		return (resource.getDateExpired() != CmsResource.DATE_EXPIRED_DEFAULT);
	}
	
	public Date getLastModificationDate()
	{
		String dateLong = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.vod.lastmodification"));
        Date uModif = new Date(Long.parseLong(dateLong));
        
		return uModif;
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
	
	public boolean isDateReleasedSet() {
		return (resource.getDateReleased() != CmsResource.DATE_RELEASED_DEFAULT);
	}
	
	public long getLongDateReleased() {
		return resource.getDateReleased();
	}
	
	public Date getDateExpired() {
		TimeZone zone = TimeZone.getDefault();
        GregorianCalendar cal = new GregorianCalendar(zone, locale);
        cal.setTimeInMillis(resource.getDateExpired());
        return cal.getTime();
	}
	
	public Date getDateReleased() {
		TimeZone zone = TimeZone.getDefault();
        GregorianCalendar cal = new GregorianCalendar(zone, locale);
        cal.setTimeInMillis(resource.getDateReleased());
        return cal.getTime();
	}
	
	public int getSeasonsCount() {
		return getElementCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.vod.season"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.vod.seasonpath")
				);
	}

	public int getVideosflashcount() {
		return getElementCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.video.video")
				);
	}
	
	public int getVideosTeaserflashcount() {
		return getElementCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.vod.flash"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.video.video")
				);
	}
	
	public int getVideosYoutubeCount() {
		return getElementCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube.id")
				);
	}
	
	public int getVideosTeaserYoutubeCount() {
		return getElementCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.vod.youTube"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube.id")
				);
	}
	
	public int getVideosEmbeddedCount() {
		return getElementCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode")
				);
	}
	
	public int getVideosTeaserEmbeddedCount() {
		return getElementCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.vod.embedded"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode")
				);
	}
	
	protected int getElementCountWithValue(String key, String controlKey) {
		Locale locale = cms.getRequestContext().getLocale();
		int total = m_content.getIndexCount(key, locale);
		
		int blank = 0;
		for (int j=1;j<=total;j++)
		{
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
	
	public int getImagescount() {
		NoticiasService nService = new NoticiasService();
		return nService.cantidadDeImagenesEnFotogaleria(cms, resource);
	}
	
	public String getLocalpath()
	{
        return cms.getSitePath(resource);
	}
	
	
	public List<String> getEpisodes() {
		if (lEpisodios==null) {
			
			String key = TfsXmlContentNameProvider.getInstance().getTagName("news.vod.chapter");
			lEpisodios = new ArrayList<String>();
			int lastElement =  m_content.getIndexCount(key, locale);
			for (int j=1;j<=lastElement;j++){
				try {
					String episodio = m_content.getStringValue(cms,  key + "[" + j +"]" , locale);
					if (!episodio.equals(""))
						lEpisodios.add(episodio);
				} catch (CmsXmlException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return lEpisodios;
	}
	
	public boolean getHasEpisodes() {
		if (lEpisodios==null) {
			 getEpisodes();
		}
		return lEpisodios.size()>0;
	}
	
	
	public List<String> getSeasons() {
		if (lSeason==null) {
			
			String key = TfsXmlContentNameProvider.getInstance().getTagName("news.vod.season");
			lSeason = new ArrayList<String>();
			int lastElement =  m_content.getIndexCount(key, locale);
			for (int j=1;j<=lastElement;j++){
				try {
					String season = m_content.getStringValue(cms,  key + "[" + j +"]" , locale);
					if (!season.equals(""))
						lSeason.add(season);
				} catch (CmsXmlException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return lSeason;
	}
	
	public boolean getHasSeason() {
		if (lSeason==null) {
			 getSeasons();
		}
		return lSeason.size()>0;
	}
	
	public String getZonePriority()
	{
		return getPropertyValue("homepriority",false);
	}
	
	public String getZone()
	{
		return getPropertyValue("homezone",false);
	}
	
	public String getSessionRelatedTitle()
	{
		return getPropertyValue("temporada-title",false);
	}
	
	public String getSessionRelatedPath()
	{
		return getPropertyValue("temporada-path",false);
	}

	public String getSerieRelatedTitle()
	{
		return getPropertyValue("serie-title",false);
	}
	
	public String getSerieRelatedPath()
	{
		return getPropertyValue("serie-path",false);
	}

	public String getTitle()
	{
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.title")); //title
	}
	
	public String getBody()
	{
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.body")); //body
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
	
}
