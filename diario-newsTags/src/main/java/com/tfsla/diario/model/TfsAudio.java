package com.tfsla.diario.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

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
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.friendlyTags.I_TfsNoticia;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsAudio {

    protected static final Log LOG = CmsLog.getLog(TfsAudio.class);

	private String agency;
	private String author;
	private String title;
	private String tags;
	private String duration;
	private String type;
	private int typeid;
	private int newscount;
	private String thumbnail = "";
	private String audio;
	private String vfspath;
	private String description;
	private String rated;
	private boolean hideComments;
	private boolean mostrarEnHome;
	private boolean autoplay;
	
	private Date lastmodifieddate;
	private Date creationdate;
	
	List<String> categorylist=null;

	Map<String,Boolean> categories=null;
	Map<String,Boolean> subCategories=null;
	
	public Map<String,Boolean> getHascategory()
	{
		return categories;
	}

	public Map<String,Boolean> getIsinsidecategory()
	{			
		return subCategories;
	}
	

	public TfsAudio(CmsObject m_cms, CmsResource res)
	{
		try {
			
			TimeZone  zone = TimeZone.getDefault();
			GregorianCalendar cal = new GregorianCalendar(zone, m_cms.getRequestContext().getLocale());
			
			cal.setTimeInMillis(res.getDateLastModified());
			lastmodifieddate = cal.getTime();

			cal.setTimeInMillis(res.getDateCreated());
			creationdate = cal.getTime();
			
			CmsProperty prop = m_cms.readPropertyObject(res, "Keywords", false);
			if (prop!=null)
				tags = prop.getValue();
			
			prop = m_cms.readPropertyObject(res, "audio-duration", false);
			if (prop!=null)
				duration = prop.getValue();
									
			prop = m_cms.readPropertyObject(res, "Title", false);
			if (prop!=null)
				title = prop.getValue();
			
			prop = m_cms.readPropertyObject(res, "Description", false);
			if (prop!=null)
				description = prop.getValue();
			
			prop = m_cms.readPropertyObject(res, "audio-rated", false);
			if (prop!=null)
				rated = prop.getValue();
			
			prop = m_cms.readPropertyObject(res, "hideComments", false);
			hideComments = false;
			if (prop!=null && prop.getValue()!=null){
				if(prop.getValue().trim().toLowerCase().equals("true"))
				   hideComments = true;
			}
			
			prop = m_cms.readPropertyObject(res, "mostrarEnHome", false);
			mostrarEnHome = false;
			if (prop!=null && prop.getValue()!=null){
				if(prop.getValue().trim().toLowerCase().equals("true"))
					mostrarEnHome = true;
			}
			
			prop = m_cms.readPropertyObject(res, "audio-autoplay", false);
			autoplay = false;
			if (prop!=null && prop.getValue()!=null){
				if(prop.getValue().trim().toLowerCase().equals("true"))
					autoplay = true;
			}

			prop = m_cms.readPropertyObject(res, "Author", false);
			if (prop!=null)
				author = prop.getValue();

			prop = m_cms.readPropertyObject(res, "Agency", false);
			if (prop!=null)
				agency = prop.getValue();

			prop = m_cms.readPropertyObject(res, "prevImage", false);
			if (prop!=null)
				thumbnail = prop.getValue();
			
			prop = m_cms.readPropertyObject(res, "newsCount", false);
			if (prop!=null)
			{
				try {
					newscount = Integer.parseInt(prop.getValue());
				}
				catch (NumberFormatException e)
				{
					newscount=0;
				}
			}
			
			
			typeid = res.getTypeId();
			type = OpenCms.getResourceManager().getResourceType(typeid).getTypeName();
			
			audio = "";
			vfspath = m_cms.getSitePath(res);
			
			if (type.equals("audio"))
				audio = vfspath;
			else if (type.equals("audio-link")) {
				CmsFile file = m_cms.readFile(res);
				audio = new String(file.getContents());
			}
			
			
			subCategories = new HashMap<String, Boolean>();
			categories = new HashMap<String, Boolean>();
			prop = m_cms.readPropertyObject(res, "category", false);
			if (prop!=null) {
				categorylist = (List<String>) prop.getValueList();
				
				if (categorylist!=null) {
					for (String category : categorylist)
					{
						categories.put(category, true);
	
						String[] subCategoria = category.split("/");
						String categ = "/";
						for (String part : subCategoria)
						{
							categ += part + "/";
							subCategories.put(categ, true);
						}
	
					}
				}
				else
					categorylist = new ArrayList<String>();
			}

		} catch (CmsException e) {
			LOG.error("Error al obtener la informacion del audio",e);
		}
		
	}
	
	public TfsAudio(CmsObject m_cms, I_TfsNoticia res, String audioPath, String audioTag) throws CmsXmlException
	{
		CmsResource resource = null;
		
			try {
				if(audioPath == ""){
					resource = m_cms.readResource(res.getXmlDocument().getFile().getStructureId());
				}
					resource = m_cms.readResource(audioPath);
				
			
			I_CmsXmlDocument xmlContent = res.getXmlDocument();
			TimeZone  zone = TimeZone.getDefault();
			GregorianCalendar cal = new GregorianCalendar(zone, m_cms.getRequestContext().getLocale());

			cal.setTimeInMillis(resource.getDateLastModified());
			lastmodifieddate = cal.getTime();

			cal.setTimeInMillis(resource.getDateCreated());
			creationdate = cal.getTime();
			
			String mtags = res.getXmlDocument().getStringValue(m_cms,  audioTag +"keywords", res.getXmlDocumentLocale());
			if (mtags!=null)
				tags = mtags;
			
			String mtitulo = res.getXmlDocument().getStringValue(m_cms,  audioTag +"titulo", res.getXmlDocumentLocale());
			if (mtitulo!=null)
				title = mtitulo;
			
			String mdescripcion = res.getXmlDocument().getStringValue(m_cms,  audioTag +"descripcion", res.getXmlDocumentLocale());
			if (mdescripcion!=null)
				description = mdescripcion;
			
			String mrated = res.getXmlDocument().getStringValue(m_cms,  audioTag +"calificacion", res.getXmlDocumentLocale());
			if (mrated!=null)
				rated = mrated;
			
			CmsProperty prop = m_cms.readPropertyObject(resource, "hideComments", false);
			hideComments = false;
			if (prop!=null && prop.getValue()!=null){
				if(prop.getValue().trim().toLowerCase().equals("true"))
				   hideComments = true;
			}

			String mEnHome = res.getXmlDocument().getStringValue(m_cms,  audioTag + "mostrarEnHome", res.getXmlDocumentLocale());
			mostrarEnHome = false;
			if (mEnHome!=null){
				if(mEnHome.equals("true"))
					mostrarEnHome = true;
			}
			
			String mautor = res.getXmlDocument().getStringValue(m_cms,  audioTag + "autor", res.getXmlDocumentLocale());
			if (mautor!=null)
				author = mautor;
			
			String magency = res.getXmlDocument().getStringValue(m_cms,  audioTag + "fuente", res.getXmlDocumentLocale());
			if (magency!=null)
				agency = magency;

			String image = res.getXmlDocument().getStringValue(m_cms,  audioTag + "imagen", res.getXmlDocumentLocale());
			if (image!=null)
				thumbnail = image;
			
			String mAutoplay = res.getXmlDocument().getStringValue(m_cms,  audioTag + "autoplay", res.getXmlDocumentLocale());
			autoplay = false;
			if (mAutoplay!=null){
				if(mAutoplay.equals("true"))
					autoplay = true;
			}
			
			prop = m_cms.readPropertyObject(resource, "newsCount", false);
			if (prop!=null)
			{
				try {
						newscount = Integer.parseInt(prop.getValue());
				}
				catch (NumberFormatException e)
				{
					newscount=0;
				}
			}
			
			typeid = resource.getTypeId();
			audio = "";
			
		   type = OpenCms.getResourceManager().getResourceType(typeid).getTypeName();
		   vfspath = m_cms.getSitePath(resource);
		   audio = m_cms.getSitePath(resource);
		   
		   prop = m_cms.readPropertyObject(resource, "audio-duration", false);
			if (prop!=null)
				duration = prop.getValue();
				
			
			subCategories = new HashMap<String, Boolean>();
			categories = new HashMap<String, Boolean>();
			prop = m_cms.readPropertyObject(resource, "category", false);
			if (prop!=null) {
				categorylist = (List<String>) prop.getValueList();
				
				if (categorylist!=null) {
					for (String category : categorylist)
					{
						categories.put(category, true);
	
						String[] subCategoria = category.split("/");
						String categ = "/";
						for (String part : subCategoria)
						{
							categ += part + "/";
							subCategories.put(categ, true);
						}
					}
				}
				else
					categorylist = new ArrayList<String>();
			}
			}catch (CmsException e) {
				LOG.error("Error al obtener la informacion del audio",e);
				}
		
	}
	
	
	public TfsAudio(CmsObject m_cms, I_TfsNoticia res, int index, String audioPath, String audioTag) throws CmsXmlException
	{
		CmsResource resource = null;
		
			try {
				if(audioPath == ""){
					resource = m_cms.readResource(res.getXmlDocument().getFile().getStructureId());
				}
					resource = m_cms.readResource(audioPath);
				
			
			TimeZone  zone = TimeZone.getDefault();
			GregorianCalendar cal = new GregorianCalendar(zone, m_cms.getRequestContext().getLocale());

			cal.setTimeInMillis(resource.getDateLastModified());
			lastmodifieddate = cal.getTime();

			cal.setTimeInMillis(resource.getDateCreated());
			creationdate = cal.getTime();
			
			String mtags = res.getXmlDocument().getStringValue(m_cms,  audioTag +"["+index+"]/keywords", res.getXmlDocumentLocale());
			if (mtags!=null)
				tags = mtags;
			
			
			String mtitulo = res.getXmlDocument().getStringValue(m_cms,  audioTag +"["+index+"]/titulo", res.getXmlDocumentLocale());
			if (mtitulo!=null)
				title = mtitulo;
			
			String mdescripcion = res.getXmlDocument().getStringValue(m_cms,  audioTag +"["+index+"]/descripcion", res.getXmlDocumentLocale());
			if (mdescripcion!=null)
				description = mdescripcion;
			
			String mrated = res.getXmlDocument().getStringValue(m_cms,  audioTag +"["+index+"]/calificacion", res.getXmlDocumentLocale());
			if (mrated!=null)
				rated = mrated;
			
			CmsProperty prop = m_cms.readPropertyObject(resource, "hideComments", false);
			hideComments = false;
			if (prop!=null && prop.getValue()!=null){
				if(prop.getValue().trim().toLowerCase().equals("true"))
				   hideComments = true;
			}

			String mEnHome = res.getXmlDocument().getStringValue(m_cms,  audioTag + "["+index+"]/mostrarEnHome", res.getXmlDocumentLocale());
			mostrarEnHome = false;
			if (mEnHome!=null){
				if(mEnHome.equals("true"))
					mostrarEnHome = true;
			}
			
			String mautor = res.getXmlDocument().getStringValue(m_cms,  audioTag + "["+index+"]/autor", res.getXmlDocumentLocale());
			if (mautor!=null)
				author = mautor;
			
			String magency = res.getXmlDocument().getStringValue(m_cms,  audioTag + "["+index+"]/fuente", res.getXmlDocumentLocale());
			if (magency!=null)
				agency = magency;

			String image = res.getXmlDocument().getStringValue(m_cms,  audioTag + "["+index+"]/imagen", res.getXmlDocumentLocale());
			if (image!=null)
				thumbnail = image;
			
			String mAutoplay = res.getXmlDocument().getStringValue(m_cms,  audioTag + "["+index+"]/autoplay", res.getXmlDocumentLocale());
			autoplay = false;
			if (mAutoplay!=null){
				if(mAutoplay.equals("true"))
					autoplay = true;
			}
			
			prop = m_cms.readPropertyObject(resource, "newsCount", false);
			if (prop!=null)
			{
				try {
						newscount = Integer.parseInt(prop.getValue());
				}
				catch (NumberFormatException e)
				{
					newscount=0;
				}
			}
			
			typeid = resource.getTypeId();
			
		   type = OpenCms.getResourceManager().getResourceType(typeid).getTypeName();
		   vfspath = m_cms.getSitePath(resource);
		   audio = m_cms.getSitePath(resource);
		   
		   prop = m_cms.readPropertyObject(resource, "audio-duration", false);
			if (prop!=null)
				duration = prop.getValue();
				
			
			subCategories = new HashMap<String, Boolean>();
			categories = new HashMap<String, Boolean>();
			prop = m_cms.readPropertyObject(resource, "category", false);
			if (prop!=null) {
				categorylist = (List<String>) prop.getValueList();
				
				if (categorylist!=null) {
					for (String category : categorylist)
					{
						categories.put(category, true);
	
						String[] subCategoria = category.split("/");
						String categ = "/";
						for (String part : subCategoria)
						{
							categ += part + "/";
							subCategories.put(categ, true);
						}
					}
				}
				else
					categorylist = new ArrayList<String>();
			}
			}catch (CmsException e) {
				LOG.error("Error al obtener la informacion del audio",e);
				}
		
	}
	
	public TfsAudio(CmsObject m_cms, I_TfsNoticia res, int index, String audioTag) throws CmsXmlException
	{
		CmsResource resource = null;
		
			try {
				resource = m_cms.readResource(res.getXmlDocument().getFile().getStructureId());
			
			    I_CmsXmlDocument xmlContent = res.getXmlDocument();
			    TimeZone  zone = TimeZone.getDefault();
			    GregorianCalendar cal = new GregorianCalendar(zone, m_cms.getRequestContext().getLocale());

			    cal.setTimeInMillis(resource.getDateLastModified());
			    lastmodifieddate = cal.getTime();

			    cal.setTimeInMillis(resource.getDateCreated());
			    creationdate = cal.getTime();
			
			    String mtags = res.getXmlDocument().getStringValue(m_cms,  audioTag +"["+index+"]/keywords", res.getXmlDocumentLocale());
			    if (mtags!=null)
				    tags = mtags;
			
			    String mtitulo = res.getXmlDocument().getStringValue(m_cms,  audioTag +"["+index+"]/titulo", res.getXmlDocumentLocale());
			    if (mtitulo!=null)
				    title = mtitulo;
			
			    String mdescripcion = res.getXmlDocument().getStringValue(m_cms,  audioTag +"["+index+"]/descripcion", res.getXmlDocumentLocale());
			    if (mdescripcion!=null)
				     description = mdescripcion;
			
			     String mrated = res.getXmlDocument().getStringValue(m_cms,  audioTag +"["+index+"]/calificacion", res.getXmlDocumentLocale());
			     if (mrated!=null)
				      rated = mrated;

			     String mEnHome = res.getXmlDocument().getStringValue(m_cms,  audioTag + "["+index+"]/mostrarEnHome", res.getXmlDocumentLocale());
			     mostrarEnHome = false;
			     if (mEnHome!=null){
				     if(mEnHome.equals("true"))
					     mostrarEnHome = true;
			     }
			
			     String mautor = res.getXmlDocument().getStringValue(m_cms,  audioTag + "["+index+"]/autor", res.getXmlDocumentLocale());
			     if (mautor!=null)
				    author = mautor;
			
			     String magency = res.getXmlDocument().getStringValue(m_cms,  audioTag + "["+index+"]/fuente", res.getXmlDocumentLocale());
			     if (magency!=null)
				    agency = magency;

			     String image = res.getXmlDocument().getStringValue(m_cms,  audioTag + "["+index+"]/imagen", res.getXmlDocumentLocale());
			     if (image!=null)
				     thumbnail = image;
			
			     String mAutoplay = res.getXmlDocument().getStringValue(m_cms,  audioTag + "["+index+"]/autoplay", res.getXmlDocumentLocale());
			     autoplay = false;
			     if (mAutoplay!=null){
				 if(mAutoplay.equals("true"))
					  autoplay = true;
			     }
	
			     vfspath= res.getXmlDocument().getStringValue(m_cms,  audioTag + "["+index+"]" + TfsXmlContentNameProvider.getInstance().getTagName("news.audio.audio"),  Locale.ENGLISH);
			     CmsFile file = m_cms.readFile(vfspath);
			     typeid = file.getTypeId();
			     
			     if (type.equals("audio"))
						audio = vfspath;
				else if (type.equals("audio-link")) {
					audio = new String(file.getContents());
				}
				 
				 subCategories = new HashMap<String, Boolean>();
				 categories = new HashMap<String, Boolean>();
				 
				 String xmlName = audioTag + "["+index+"]/categoria";
				 List<I_CmsXmlContentValue> categoriesList = res.getXmlDocument().getValues(xmlName,  Locale.ENGLISH);
				 
				 categorylist = new ArrayList<String>();
				 
				 for (I_CmsXmlContentValue category : categoriesList) {
					 
					 categorylist.add(category.getStringValue(m_cms));
					 
					 categories.put(category.getStringValue(m_cms), true);
					 
					 String categoria = category.getStringValue(m_cms);
					 
					 if(categoria!=null)
					 {
					    String[] subCategoria = categoria.split("/");
						String categ = "/";
						for (String part : subCategoria)
						{
							categ += part + "/";
							subCategories.put(categ, true);
						}
					 }
				 }
					
			}catch (CmsException e) {
				LOG.error("Error al obtener la informacion del audio",e);
			}
		
	}
	public String getVfspath(){
		return vfspath;
	}
	
	public void setVfspath(String path){
		this.vfspath = path;
	}
	
	public String getDuration(){
		return duration;
	}
	
	public void setDuration(String duration){
		this.duration = duration;
	}
		
	public String getAgency() {
		return agency;
	}

	public void setAgency(String agency) {
		this.agency = agency;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getTypeid() {
		return typeid;
	}

	public void setTypeid(int typeid) {
		this.typeid = typeid;
	}

	public int getNewscount() {
		return newscount;
	}

	public void setNewscount(int newscount) {
		this.newscount = newscount;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	
	public String getAudio() {
		return audio;
	}

	public void setAudio(String audios) {
		this.audio = audio;
	}
	
	public List<String> getCategorylist() {
		return categorylist;
	}

	public void setCategorylist(List<String> categorylist) {
		this.categorylist = categorylist;
	}
	
	public List<String> getFormatslist() {
		return categorylist;
	}

	public Date getLastmodifieddate() {
		return lastmodifieddate;
	}

	public void setLastmodifieddate(Date lastmodifieddate) {
		this.lastmodifieddate = lastmodifieddate;
	}

	public Date getCreationdate() {
		return creationdate;
	}

	public void setCreationdate(Date creationdate) {
		this.creationdate = creationdate;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRated() {
		return rated;
	}

	public void setRated(String rated) {
		this.rated = rated;
	}
	
	public boolean isHideComments()
	{
		return this.hideComments;
	}
	
	public boolean ismostrarEnHome()
	{
		return this.mostrarEnHome;
	}
	
	public void setHideComments(boolean hideComments)
	{
		this.hideComments = hideComments;
	}
	
	public void setAutoplay(boolean autoplay)
	{
		this.autoplay = autoplay;
	}
	
	public boolean isAutoplay()
	{
		return this.autoplay;
	}

}
