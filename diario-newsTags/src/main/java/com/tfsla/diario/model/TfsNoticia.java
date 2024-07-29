package com.tfsla.diario.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.jsp.PageContext;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspTagLink;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.main.TfsContext;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.comentarios.services.CommentsModule;
import com.tfsla.diario.ediciones.model.Seccion;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.NoticiasService;
import com.tfsla.diario.ediciones.services.OpenCmsBaseService;
import com.tfsla.diario.ediciones.services.SeccionesService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;
import com.tfsla.rankViews.model.TfsRankResults;
import com.tfsla.rankViews.service.RankService;
import com.tfsla.statistics.model.TfsStatisticsOptions;
import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.utils.UrlLinkHelper;
import com.tfsla.diario.utils.TfsTagsUtil;

public class TfsNoticia {
	
	public TfsNoticia()
	{
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
	
	public TfsNoticia(CmsObject m_cms, I_CmsXmlDocument m_content, Locale locale, PageContext pageContext) {
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
	}

	public Map<String,String> getProperty()
	{
		if (properties==null)
		{
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
	
	public Map<String,String> getGenericElementValue()
	{
		if (elements==null)
		{
			elements = new HashMap<String, String>();
			Locale locale = cms.getRequestContext().getLocale();
			for (String key :m_content.getNames(locale))
			{
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

	public boolean isHideAds() {
		return Boolean.parseBoolean(getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.hideAds")));
	}

	public boolean isItemlistintegrated() {
		return Boolean.parseBoolean(getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.itemListIntegrated")));
	}

	public boolean isNewswithitemlist() {
		return (m_content.getIndexCount(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList"), locale)>0);
	}
	
	public int getItemlistcount()
    {
	      return m_content.getIndexCount(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList"), locale);
    }

	public int getCategoriescount()
	{
			return  m_content.getIndexCount(TfsXmlContentNameProvider.getInstance().getTagName("news.categories"), locale);
	}
	
	public int getSourcescount()
    {
	      return  m_content.getIndexCount(TfsXmlContentNameProvider.getInstance().getTagName("news.sources"), locale);
    }

	public int getCurrentitemlist() {
		String itemNumber = pageContext.getRequest().getParameter("itemNumber");
		if (itemNumber==null) return 1;
		try {
		return Integer.parseInt(itemNumber);
		}
		catch (NumberFormatException ex) {
			LOG.error("Error al obtener el numero de item a mostrar");
			return 1;
		}
	}
	
	public Map<String,Boolean> getHascategory()
	{
		
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
	
	public List<String> getCategories()
	{
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
	public Map<String,Boolean> getIsinsidecategory()
	{
		if (subCategories==null)
		{
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

	public String getLocalpath()
	{
        return cms.getSitePath(resource);
	}

	public String getAbsolutelink()
	{
		String link="";
        String external = UrlLinkHelper.getExternalLink(resource, cms);
	
        if (external!=null && !external.isEmpty())
        	link = external;
        else
        	link = UrlLinkHelper.getUrlFriendlyLink(resource, cms, this.pageContext.getRequest(),false);
	
        return link;
	}

	public String getLink()
	{
		String link="";
        String external = UrlLinkHelper.getExternalLink(resource, cms);
	
        if (external!=null && !external.isEmpty())
        	link = external;
        else
        	link = UrlLinkHelper.getUrlFriendlyLink(resource, cms, this.pageContext.getRequest());
	
        return link;
	}

	public String getLegacylink()
	{
		String link="";
        String external = UrlLinkHelper.getExternalLink(resource, cms);
    	
        if (external!=null && !external.isEmpty())
        	link = external;
        else
        	link =CmsJspTagLink.linkTagAction("/" + CmsResourceUtils.getLink(resource),  pageContext.getRequest());
	
        return link;
	}
	
	public String getCanonicalLink()
	{
		String link="";
        String external = UrlLinkHelper.getExternalLink(resource, cms);
    	
        if (external!=null && !external.isEmpty())
        	link = external;
        else
        	link =UrlLinkHelper.getCanonicalLink(resource, cms, this.pageContext.getRequest());
	
        return link;
	}

	
	public String getState()
	{
		return getPropertyValue("state",false);
	}
	
	public String getPrioritysection()
	{
		return getPropertyValue("section.priority",false);
	}
	
	public String getPriorityhome()
	{
		return getPropertyValue("home.priority",false);
	}
	
	public String getZonehome()
	{
		return getPropertyValue("home.zone",false);
	}
	
	public String getZonesection()
	{
		return getPropertyValue("section.zone",false);
	}
	
	public String getTitle()
	{
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
	
	public String getAuthorName() throws CmsXmlException{
		return getAuthor("autor",1,"internalUser");
	}
	
	public String getAuthor(String fieldList, int key, String fieldItem) throws CmsXmlException{
		String content = ""; 
		String fieldContent = fieldList ;
		
		if (key > 0)
			fieldContent += "[" + key + "]";
		
		if (!fieldItem.equals(""))
			fieldContent += "/" + fieldItem;
					
		try{
				content = getElementValue(fieldContent);}
			catch(Exception e){
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
	
	public int getTitlesCount()
	{
		return m_content.getIndexCount(TfsXmlContentNameProvider.getInstance().getTagName("news.title"), cms.getRequestContext().getLocale());
	}
	
	public String getTitleHomeNum(){

		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.title.home"));
	}
	
	public String getTitleSectionNum(){

		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.title.section"));
	}
	
	public String getTitleDetailNum(){

		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.title.detail"));
	}
	
	public String getTitleSeoNum(){
		
		String titleSeo = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.title.seo"));
		if (titleSeo.equals(""))
			return getTitleHomeNum();
		else 
			return titleSeo;
	}

	
	public Map<String,String> getTitles()
	{
		if (titles==null)
		{
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
	
	public boolean getRelatedNewsAutomatic()
	{
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.relatednews.automatic")).trim().toLowerCase().equals("true");
	}
	
	public String getSubtitle()
	{
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.subtitle")); //copete
	}

	public String getSummary()
	{
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.summary")); //resumen
	}

	public String getTags()
	{
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.keywords")); //claves
	}
	
	public String getHiddentags()
	{
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.hiddenkeywords")); //claves
	}
	
	public String getUppertitle()
	{
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.uppertitle")); //volanta
	}

	public String getSectionname()
	{
		return getPropertyValue(TfsXmlContentNameProvider.getInstance().getTagName("news.section"),false); //seccion
	}
	
	
	public String getHomePreview()
	{
		return getElementValue("prioridadVideoEnHome");
	}

	public String getDetailPreview()
	{
		return getElementValue("prioridadVideoEnArticulo");
	}

	public String getHomePreviewOrientation()
	{
		return getElementValue("fotoenhome");
	}

	public String getSectionPreviewOrientation()
	{
		return getElementValue("fotoenseccion");
	}
	
	public boolean isHideVolanta()
	{
		return getElementValue("ocultarVolanta").trim().toLowerCase().equals("true");
	}
	public boolean isHideCopete()
	{
		return getElementValue("ocultarCopete").trim().toLowerCase().equals("true");
	}
	public boolean isHideAuthor()
	{
		return getElementValue("ocultarAutor").trim().toLowerCase().equals("true");
	}
	public boolean isHideComments()
	{
		return getElementValue("ocultarComentarios").trim().toLowerCase().equals("true");
	}
	public int getRelatednewsinhomecount() {
		return getElementCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.relatednews"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.relatednews.showHome"),
				"true");
	}
	
	public boolean isHideTime()
	{
		return getElementValue("ocultarHora").trim().toLowerCase().equals("true");
	}

	public int getAudioscount()
	{
		return getElementCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.audio"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.audio.audiopath")
				);
	}

	public int getFilescount()
	{
		return getElementCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.files"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.files.filepath")
				);
	}

	
	public int getVideoscount()
	{
		return getVideosflashcount() + getVideosdownloadcount() + getVideosyoutubecount() + getVideosembeddedcount();
		//NoticiasService nService = new NoticiasService();
		//return nService.cantidadDeVideos(cms, resource);
	}
	
	public int getVideosflashcount()
	{
		return getElementCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.video.video")
				);
	}

	public int getVideosdownloadcount()
	{
		return getElementCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.videodownload"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.video.video")
				);
	}

	public int getVideosyoutubecount()
	{
		return getElementCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube.id")
				);
	}

	public int getVideosembeddedcount()
	{
		return getElementCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode")
				);
	}
	
	public int getVideoslistcount()
	{
		return getVideoslistflashcount() + getVideoslistdownloadcount() + getVideoslistyoutubecount() + getVideoslistembeddedcount();
	}
	
	public int getVideoslistflashcount()
	{
		return getElementListCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.video.video")
				);
	}

	public int getVideoslistdownloadcount()
	{
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

	public int getVideoslistembeddedcount()
	{
		return getElementListCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode")
				);
	}
	
	public Map<Integer,Integer> getVideositemlistcount()
	{
		Map<Integer,Integer> counts= new HashMap<Integer,Integer>();
		
		Locale locale = cms.getRequestContext().getLocale();
		String itemList = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList");
		
		int totalLists = m_content.getIndexCount(itemList, locale);
		
		for(int i=1;i<=totalLists;i++){
			
			int totalItem = 0;
			
			totalItem = MapUtils.getIntValue(getVideositemlistflashcount(), i) + MapUtils.getIntValue(getVideositemlistdownloadcount(), i) + MapUtils.getIntValue(getVideositemlistyoutubecount(), i) + MapUtils.getIntValue(getVideositemlistembeddedcount(), i);
			
			counts.put(i,totalItem);
		}
		
		return counts;
	}
	
	public Map<Integer,Integer> getVideositemlistflashcount()
	{
		return getElementItemListCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoflash"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.video.video")
				);
	}

	public Map<Integer,Integer> getVideositemlistdownloadcount()
	{
		return getElementItemListCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.videodownload"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.video.video")
				);
	}

	public Map<Integer,Integer> getVideositemlistyoutubecount()
	{
		return getElementItemListCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube.id")
				);
	}

	public Map<Integer,Integer> getVideositemlistembeddedcount()
	{
		return getElementItemListCountWithValue(
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded"),
				TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode")
				);
	}
	
	public int getImagescount()
	{
		NoticiasService nService = new NoticiasService();
		return nService.cantidadDeImagenesEnFotogaleria(cms, resource);
	}
	
	public int getAuthorscount()
	{
		return m_content.getIndexCount(TfsXmlContentNameProvider.getInstance().getTagName("news.authors"), cms.getRequestContext().getLocale());
	} 
	
	public boolean isImagepreviewset()
	{
		String xmlName = TfsXmlContentNameProvider.getInstance().getTagName("news.imagepreview")+"[1]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.image.image")+"[1]";
		NoticiasService nService = new NoticiasService();
		boolean isImagepreviewset = nService.tieneImagenPrevisualizacion(cms, resource, xmlName);            
		
		return (isImagepreviewset);
	}
	
	public boolean isImagepreviewportset()
	{
		String xmlName = TfsXmlContentNameProvider.getInstance().getTagName("news.customimage")+"[1]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.image.image")+"[1]";
		NoticiasService nService = new NoticiasService();
		boolean isImagepreviewset = nService.tieneImagenPrevisualizacion(cms, resource, xmlName);            
		
		return (isImagepreviewset);
	}
	
	public int getImagepreviewsetPortcount()
	{
		return m_content.getIndexCount(TfsXmlContentNameProvider.getInstance().getTagName("news.customimage"), cms.getRequestContext().getLocale());
	} 
	
	public boolean hasImagepreviewporset(String previreport)
	{
		for (int i =1; i<= getImagepreviewsetPortcount(); i++) {
			
			String xmlName = TfsXmlContentNameProvider.getInstance().getTagName("news.customimage")+"["+i+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.image.viewport")+"[1]";
			String imageViewPort =  getElementValue(xmlName);
			
			if (imageViewPort.equals(previreport)) {
				return true;
			}else{
				return false;
			}
		}
		return false;
	} 
	
	public boolean isImagepreviewsetPort(String previreport )
	{
		for (int i =1; i<= getImagepreviewsetPortcount(); i++) {
			
			String xmlName = TfsXmlContentNameProvider.getInstance().getTagName("news.customimage")+"["+i+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.image.viewport")+"[1]";
			String imageViewPort = getElementValue(xmlName);
			
			if (imageViewPort.equals(previreport)) {
			
				String xmlNameViewPort = TfsXmlContentNameProvider.getInstance().getTagName("news.customimage")+"["+i+"]/"+TfsXmlContentNameProvider.getInstance().getTagName("news.image.image")+"[1]";
				String imageViewThisPort = getElementValue(xmlNameViewPort); 

				if (imageViewThisPort!=null  && imageViewThisPort.trim().length()>0){
					return true;
				}else{
					return false;
				}
			} else {
				return false;
			}
		}
		return false;

	} 
	

	public String getSection()
	{
        String seccionName = getPropertyValue(TfsXmlContentNameProvider.getInstance().getTagName("news.section"),false); //seccion
        
        SeccionesService sService = new SeccionesService();
        
        if (tEdicion == null)
        	return "";
        
        Seccion seccion = sService.obtenerSeccion(seccionName, tEdicion.getId());
        
        if (seccion!=null)
        	return seccion.getDescription();
        
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
	
	public String getTextLabel()
	{
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.text.label")); //Cintillo
	}
	
	public String getTextLabelColor()
	{
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.text.label.color")); //Color del Cintillo
	}
	
	public String getImageLabel()
	{
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.image.label")); //Etiqueta
	}
	
	public String getBody()
	{
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.body")); //cuerpo
	}
	
	public String getTargetLink()
	{
		return getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.target"));
	}
	
	public int getCommentcount()
	{ 
		return CommentsModule.getInstance(cms).getCommentsCount(cms, "/" + CmsResourceUtils.getLink(resource));
	}
	
	public int getCommentCountFeatured()
	{ 
		return CommentsModule.getInstance(cms).getCommentsWhitMoreAnswersCount(cms, getLocalpath(), CommentsModule.getInstance(cms).getMinAnswers());
	}
	
	public Date getLastModificationDate()
	{
		String dateLong = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.lastmodification"));
        Date uModif = new Date(Long.parseLong(dateLong));
        
		return uModif;
	}
	
	public Date getCreationDate()
	{
		Long dateLong = resource.getDateCreated();
        Date DateCreate = new Date(dateLong);
        
		return DateCreate;
	}
	
	public Date getModificationDate()
	{
		Long dateLong = resource.getDateLastModified();
        Date DateModification = new Date(dateLong);
        
		return DateModification;
	}
	
	public Date getFirstPublishDate()
	{
		Date firstPublishDate = null;
		
		if(!resource.getState().equals(CmsResource.STATE_NEW)) {
			String dateStr = getPropertyValue("firstPublishDate",false);
			firstPublishDate = null;
			
			if(dateStr!=null && !dateStr.equals(""))
			{
				 firstPublishDate = new Date(Long.parseLong(dateStr));
			}else{
				 String dateLongStr = getElementValue(TfsXmlContentNameProvider.getInstance().getTagName("news.lastmodification"));
				 firstPublishDate = new Date(Long.parseLong(dateLongStr));
			}
		}
		
		return firstPublishDate;
	}
	
	public Date getLastPublishDate()
	{
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

	protected int getElementCountWithValue(String key, String controlKey, String value)
	{
		Locale locale = cms.getRequestContext().getLocale();
		int total = m_content.getIndexCount(key, locale);
		
		int different = 0;
		for (int j=1;j<=total;j++)
		{
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

	protected int getElementCountWithValue(String key, String controlKey)
	{
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
	
	protected int getElementListCountWithValue(String key, String controlKey)
	{
		Locale locale = cms.getRequestContext().getLocale();
		String itemList = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList");
		
		int totalLists = m_content.getIndexCount(itemList, locale);
		int totalwithValues = 0;
		
		for(int i=1;i<=totalLists;i++){
			
			int blank = 0;
			int total = m_content.getIndexCount(itemList+ "[" +i+ "]/" + key, locale);
			
			for (int j=1;j<=total;j++)
			{
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
	
	protected Map<Integer,Integer> getElementItemListCountWithValue(String key, String controlKey)
	{
		Map<Integer,Integer> counts= new HashMap<Integer,Integer>();
		
		Locale locale = cms.getRequestContext().getLocale();
		String itemList = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList");
		
		int totalLists = m_content.getIndexCount(itemList, locale);
		
		for(int i=1;i<=totalLists;i++){
			
			int blank = 0;
			int totalwithValues = 0;
			int total = m_content.getIndexCount(itemList+ "[" +i+ "]/" + key, locale);
			
			for (int j=1;j<=total;j++)
			{
				String controlValue;
				try {
					controlValue = m_content.getStringValue(cms,itemList+ "[" +i+ "]/" + key + "[" + j + "]/" + controlKey, locale);
				
					if (controlValue==null || controlValue.trim().equals(""))
						blank ++;
				} catch (CmsXmlException e) {
					LOG.debug("Error reading content value " + itemList+ "[" +i+ "]/" + key + "[" + j + "]/" + controlKey + " on content " + m_content.getFile().getRootPath(),e);

				}
			}
			
			totalwithValues =  total - blank;
			counts.put(i,totalwithValues);
		}
		
		return counts;
	}

	public Map<Integer,Date> getItemListDate() {
		
		if (itemDate==null) {
			String key = TfsXmlContentNameProvider.getInstance().getTagName("news.itemList");
			itemDate = new HashMap<Integer,Date>();
			int lastElement =  m_content.getIndexCount(key, locale);
			for (int j=1;j<=lastElement;j++){
				try {
					String dateStr = m_content.getStringValue(cms,  key + "[" + j +"]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.itemList.date") , locale);
					if (!dateStr.equals("0")) {
						Date uModif = new Date(Long.parseLong(dateStr));
						itemDate.put(j,uModif);
					} else {
						itemDate.put(j,null);
					}
				} catch (CmsXmlException e) {
					LOG.error("Error al buscar las fechs de los items", e);
				}
			}
		}
			
		return itemDate;
	}
	
	
	/* properties */
	
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
	Map<Integer,Date> itemDate=null;
	
	
	private List<TfsNotaEnPortada> indexPages = null;
	
	private int positevalorations=-1;
	private int totalvalorations=-1;
	private int views=-1;
	private int recommendations=-1;
	private float generalrank=-1F;
	int commentfeatured = 0;
	
	
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
	
	private void loadNewsStats()
	{
		RankService rank = new RankService();

		//Options rank processor
		TfsStatisticsOptions options = new TfsStatisticsOptions();
		options.setTags(new String[0]);
		options.setUrl(getLocalpath());
		options.setShowValoracion(true);
		options.setShowCantidadValoracion(true);
		options.setShowHits(true);
		options.setShowRecomendacion(true);
		options.setShowGeneralRank(true);
		
		options.setRankMode(TfsStatisticsOptions.RANK_HITS);

		TfsRankResults res = rank.getStatistics(cms, options);
		if ( res != null && res.getRank() != null && res.getRank().length > 0 ) {
			positevalorations = res.getRank()[0].getValoracion();
			totalvalorations = res.getRank()[0].getCantidadValoracion();
			views = res.getRank()[0].getCantidad();
			recommendations = res.getRank()[0].getRecomendacion();
			generalrank = res.getRank()[0].getGeneralRank();
		}
		else
		{
			positevalorations = 0;
			totalvalorations = 0;
			views = 0;
			recommendations = 0;
			generalrank = 0;
		}
	}

	public int getPositevalorations() {
		if (positevalorations==-1)
			loadNewsStats();
			
		return positevalorations;
	}

	public int getTotalvalorations() {
		if (totalvalorations==-1)
			loadNewsStats();
		return totalvalorations;
	}

	public int getViews() {
		if (views==-1)
			loadNewsStats();
		
		return views;
	}

	public int getRecommendations() {
		if (recommendations==-1)
			loadNewsStats();
		
		return recommendations;
	}

	public float getGeneralrank() {
		if (generalrank==-1)
			loadNewsStats();
		
		return generalrank;
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

	public List<TfsNotaEnPortada> getIndexPages() {
		if (indexPages==null)
		{
			indexPages = new ArrayList<TfsNotaEnPortada>();
			List<I_CmsXmlContentValue> values = m_content.getValues("publicaciones", locale);
			for (I_CmsXmlContentValue value : values) {
				String preffix = "publicaciones["  + (value.getIndex()+1) + "]/";

				try {
					String publicacion = m_content.getStringValue(cms, preffix + "publicacion", locale);
				
					
					String siteName = OpenCmsBaseService.getCurrentSite(cms);
					TipoEdicionService tService =  new TipoEdicionService();
					TipoEdicion tEdicion=tService.obtenerTipoEdicion(publicacion,siteName);
					
					if (tEdicion!= null) {
						String zonahome = m_content.getStringValue(cms, preffix + "zonahome", locale);
						String prioridadhome = m_content.getStringValue(cms, preffix + "prioridadhome", locale);
						String seccion = m_content.getStringValue(cms, preffix + "seccion", locale);
						String zonaseccion = m_content.getStringValue(cms, preffix + "zonaseccion", locale);
						String prioridadseccion = m_content.getStringValue(cms, preffix + "prioridadseccion", locale);
	
						TfsNotaEnPortada noticiaEnPortada = new TfsNotaEnPortada(
								tEdicion,
								seccion,
								zonaseccion,
								zonahome,
								Integer.parseInt(prioridadhome),
								Integer.parseInt(prioridadseccion)						    	
						);
						
						indexPages.add(noticiaEnPortada);
					}
				} catch (CmsXmlException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		
		return indexPages;
	}
	
	//contenido del body
	//imagenes
	public boolean getHasEmbeddedImage(){
		return hasEmbedded("<img");
	}
	
	//eventos
		public boolean getHasEmbeddedEvent(){
			return hasEmbedded("ck-events");
		}
		
	//noticias relacionadas
	public boolean getHasEmbeddedRelatedNews(){
		return hasEmbedded("ck-related-news");
	}
				
	//videos
	public boolean getHasEmbeddedVideoYoutube(){
		return (hasEmbedded("ck-video-player") && hasEmbedded("youtube-")) || (hasEmbedded("ck-video-gallery") && hasEmbedded("video-type=\"youtube\""));
	}
	
	public boolean getHasEmbeddedVideoLink(){
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		String embedCode = config.getParam(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot(), String.valueOf(tEdicion.getId()), "videos", "embedCode", "");
		String showIframeAMP = config.getParam(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot(), String.valueOf(tEdicion.getId()), "videos", "showIframeAMP", "");
		if (showIframeAMP.equals("true") &&  !embedCode.equals("") )
			return false;
		else
			return (hasEmbedded("ck-video-player") && hasEmbedded("video-type=\"link\"") )
				|| (hasEmbedded("ck-video-gallery") && hasEmbedded("video-type=\"link\""));
		
	}
	
	public boolean getHasEmbeddedVideoEmbedded(){
		return (hasEmbedded("ck-video-player") && hasEmbedded("embedded-"))
				|| (hasEmbedded("ck-video-gallery") && hasEmbedded("video-type=\"embedded\""));
	}
	
	public boolean getHasEmbeddedVideoGallery(){
		return hasEmbedded("ck-video-gallery");
	}
	
	public boolean getHasEmbeddedVideoList(){
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		String embedCode = config.getParam(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot(), String.valueOf(tEdicion.getId()), "videos", "embedCode", "");
		String showIframeAMP = config.getParam(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot(), String.valueOf(tEdicion.getId()), "videos", "showIframeAMP", "");
		if (showIframeAMP.equals("true") &&  !embedCode.equals("") )
			return false;
		else
			return hasEmbedded("list-video-player");
		
	}
	
	public boolean getHasEmbeddedImageGallery(){
		return hasEmbedded("ck-image-gallery");
	}
	
	public boolean getHasEmbeddedAudio(){
		return hasEmbedded("ck-audio-player");
	}
	
	public boolean getHasEmbeddedAudioGallery(){
		return hasEmbedded("ck-audio-gallery");
	}
	
	public boolean getHasEmbeddedAudioList(){
		return hasEmbedded("ck-audio-list");
	}
	

	public boolean getHasEmbeddedPinterest(){
		return hasEmbedded("ck-pinterest");
	}
	public boolean getHasEmbeddedVine(){
		return hasEmbedded("ck-vine");
	}
	
	public boolean getHasEmbeddedInstagram(){
		return hasEmbedded("ck-instagram");
	}
	
	public boolean getHasEmbeddedTwitter(){
		return hasEmbedded("ck-twitter");
	}
	
	public boolean getHasEmbeddedTiktok(){
		return hasEmbedded("ck-tiktok");
	}
	
	public boolean getHasEmbeddedJWPlayer(){
		return hasEmbedded("ck-jwplayer");
	}
	
	public boolean getHasEmbeddedFacebook(){
		return (hasEmbedded("ckeditor-fb") || hasEmbedded("ckeditor-ifb"));
	}
	
	public boolean getHasEmbeddedEmbedded(){

		String body = getBody() + getItemListBodyContent();
		body = body.replaceAll("\n","");
		 
		Pattern REGEX = Pattern.compile("[^>]class=\".*?ckeditor-em.*?\"*>(.*?)<\\/");
		Matcher matcher = REGEX.matcher(body);
		
		int em = 0;
		int brid = 0;
		
		while(matcher.find()){
			String content = "";
			       content = matcher.group(1);
			em++;
			
			if(content.contains(".brid") || content.contains("class=\"brid\""))
				brid++;
		}
		
		if(em>brid)
			return true;
		else
			return false;
		
		//return hasEmbedded("ckeditor-em");
	}
	
	public boolean getHasEmbeddedPoll(){
		return hasEmbedded("ckeditor-poll");
	}
	
	public boolean getHasEmbeddedStorify(){
		return hasEmbedded("ck-storify");
	}
	
	public boolean getHasEmbeddedFlickr(){
		return hasEmbedded("ck-flickr");
	}
	
	public boolean getHasEmbeddedImageComparator(){
		return hasEmbedded("ckeditor-comparationimg");
	}
	
	public boolean getHasEmbeddedYoutube(){
		return hasEmbedded("ck-youtube");
	}
	
	public boolean getHasEmbeddedBrid(){
		return hasEmbedded("ckeditor-brid") || (hasEmbedded("ckeditor-em") && hasEmbedded(".brid"));
	}
		
	public boolean getHasEmbeddedIframe(){
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		String embedCode = config.getParam(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot(), String.valueOf(tEdicion.getId()), "videos", "embedCode", "");
		String showIframeAMP = config.getParam(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot(), String.valueOf(tEdicion.getId()), "videos", "showIframeAMP", "");
		if (showIframeAMP.equals("true") &&  !embedCode.equals("") )
			return (hasEmbedded("ck-video-player") && hasEmbedded("video-type=\"link\"") )
					|| (hasEmbedded("ck-video-gallery") && hasEmbedded("video-type=\"link\""))
					|| hasEmbedded("list-video-player") || hasEmbeddedIframe(getBody()) || hasEmbeddedIframe(getItemListBodyContent());
		else {	
			return hasEmbeddedIframe(getBody()) || hasEmbeddedIframe(getItemListBodyContent()); 
		} 		
	}
	
	public String getCreationUser() {
		CmsUUID uui ;
		CmsUser user;
		String author = "";
		try {
			uui = resource.getUserCreated();
			user = TfsContext.getInstance().getCmsObject().readUser(uui); 
			author = user.getName();
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
		
		return author;
	}
	
	private boolean hasEmbeddedIframe (String body) {
		
		body = body.replace(textToReplaceIframeValidation(body, "ck-youtube"),"");
		body = body.replace(textToReplaceIframeValidation(body, "ckeditor-em"),"");
		body = body.replace(textToReplaceIframeValidation(body, "ck-flickr"),"");
		body = body.replace(textToReplaceIframeValidation(body, "ckeditor-fb"),"");
		body = body.replace(textToReplaceIframeValidation(body, "ckeditor-ifb"),"");
		body = body.replace(textToReplaceIframeValidation(body, "ck-twitter"),"");
		body = body.replace(textToReplaceIframeValidation(body, "ck-tiktok"),"");
		body = body.replace(textToReplaceIframeValidation(body, "ck-instagram"),"");
		body = body.replace(textToReplaceIframeValidation(body, "ck-vine"),"");
		body = body.replace(textToReplaceIframeValidation(body, "ck-pinterest"),"");
		body = body.replace(textToReplaceIframeValidation(body, "ck-video-player"),"");
		body = body.replace(textToReplaceIframeValidation(body, "ckeditor-brid"),"");
		return body.contains("<iframe");
	}
	
	private String textToReplaceIframeValidation (String body, String reference) {
		if (body.indexOf(reference) != -1)
			return body.substring (body.indexOf(reference), body.indexOf ("</div>", body.indexOf(reference) + 1));
		return "";
	}
	
	private boolean hasEmbedded (String value) {
		
		// busco el contenido tanto en el body como en itemList.Body
		return getBody().contains(value) || 
				getItemListBodyContent().contains(value);
	}
	
	public String getItemListBodyContent(){
		int listCount = getItemlistcount();
		String content = "";
		for (int i = 1; i<=listCount; i++){
			String elementName= TfsXmlContentNameProvider.getInstance().getTagName("news.itemList") + "["+i+"]/" + TfsXmlContentNameProvider.getInstance().getTagName("news.itemList.body") +"[1]";
			content += getElementValue(elementName);
		}
		return content;
	}
	
	public String getNewsType()
	{
		return getPropertyValue("newsType",false);
	}
	
	public String getIsACopy()
	{
		return getPropertyValue("isACopy",false);
	}
	
	public String getQualityImage()
	{
		return getPropertyValue("image.qualityError",false);
	}
	
	public String getOriginalNote()
	{
		return getPropertyValue("originalNote",false);
	}
	
	public String getComplianceData()
	{
		return getPropertyValue("complianceData",false);
	}
	
	public String getCompliance()
	{
		return getPropertyValue("compliance",false);
	}
	
	public String getUnsafeLabels()
	{
		return getPropertyValue("UnsafeLabels",false);
	}
	
	public String getPushId()
	{
		return getPropertyValue("pushId",false);
	}
	
	public boolean hasPushId()
	{
		if (!getPushId().equals(""))
			return true;
		else
			return false;
	}
	
	
}
