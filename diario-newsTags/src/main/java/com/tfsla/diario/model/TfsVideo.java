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
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.main.TfsContext;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.file.types.TfsResourceTypeVideoEmbedded;
import com.tfsla.diario.file.types.TfsResourceTypeVideoYoutubeLink;
import com.tfsla.diario.friendlyTags.I_TfsNoticia;
import com.tfsla.diario.multiselect.VideoCodeLoader;
import com.tfsla.diario.utils.TfsVideoHelper;

public class TfsVideo {

    protected static final Log LOG = CmsLog.getLog(TfsVideo.class);

	private String agency;
	private String author;
	private String title;
	private String tags;
	private String formats;
	private String duration;
	private String size;
	private String bitrate;
	private String type;
	private int typeid;
	private int newscount;
	private String thumbnail = "";
	private String data;
	private String video;
	private String link;
	private String vfspath;
	private String format;
	private String description;
	private String rated;
	private boolean hideComments;
	private int formatscount;
	private boolean mostrarEnHome;
	private boolean autoplay;
	private boolean mute;
	
	protected transient CmsFile videoResource;
	
	private Date lastmodifieddate;
	private Date creationdate;
	
	private String creationuser;
	
	private CmsObject cms;
	List<String> categorylist=null;
	List<String> formatslist=null;

	Map<String,Boolean> categories=null;
	Map<String,Boolean> subCategories=null;
	
	Map<String,Boolean> formatsMap=null;

	public Map<String,Boolean> getHascategory()
	{
		return categories;
	}

	public Map<String,Boolean> getIsinsidecategory()
	{			
		return subCategories;
	}
	
	public Map<String,Boolean> getHasformat()
	{			
		return formatsMap;
	}

	public TfsVideo(CmsObject m_cms, CmsResource res)
	{
		try {
			this.cms = m_cms;
			TimeZone  zone = TimeZone.getDefault();
			GregorianCalendar cal = new GregorianCalendar(zone, m_cms.getRequestContext().getLocale());
			
			cal.setTimeInMillis(res.getDateLastModified());
			lastmodifieddate = cal.getTime();

			cal.setTimeInMillis(res.getDateCreated());
			creationdate = cal.getTime();
			
			CmsProperty prop = m_cms.readPropertyObject(res, "Keywords", false);
			if (prop!=null)
				tags = prop.getValue();
			
			prop = m_cms.readPropertyObject(res, "video-formats", false);
			if (prop!=null)
				formats = prop.getValue();
			
			formatsMap = new HashMap<String, Boolean>(); 
			
			formatscount = 0;
			
			if(formats!=null){
				
                String[] listFormats = formats.split(",");
                
				if (listFormats!=null) {
					for (String formato : listFormats)
					{
						formatsMap.put(formato, true);
						formatscount++;
					}
				}
			}
			
			prop = m_cms.readPropertyObject(res, "video-duration", false);
			if (prop!=null)
				duration = prop.getValue();
			
			prop = m_cms.readPropertyObject(res, "video-size", false);
			if (prop!=null)
				size = prop.getValue();
			
			prop = m_cms.readPropertyObject(res, "video-bitrate", false);
			if (prop!=null)
				bitrate = prop.getValue();
			
			prop = m_cms.readPropertyObject(res, "video-format", false);
			if (prop!=null)
				format = prop.getValue();

			prop = m_cms.readPropertyObject(res, "Title", false);
			if (prop!=null)
				title = prop.getValue();
			
			prop = m_cms.readPropertyObject(res, "Description", false);
			if (prop!=null)
				description = prop.getValue();
			
			prop = m_cms.readPropertyObject(res, "video-rated", false);
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
			
			prop = m_cms.readPropertyObject(res, "video-autoplay", false);
			autoplay = false;
			if (prop!=null && prop.getValue()!=null){
				if(prop.getValue().trim().toLowerCase().equals("true"))
					autoplay = true;
			}
			
			prop = m_cms.readPropertyObject(res, "video-mute", false);
			mute = false;
			if (prop!=null && prop.getValue()!=null){
				if(prop.getValue().trim().toLowerCase().equals("true"))
					mute = true;
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
			
			CmsFile file = m_cms.readFile(res);
			videoResource = file;
			data = new String(file.getContents());
			
			typeid = res.getTypeId();
			type = OpenCms.getResourceManager().getResourceType(typeid).getTypeName();
			
			video = "";
			if (type.equals("video"))
				video = m_cms.getSitePath(res);
			else if (type.equals("video-link")) {
				video = data;
				setLink(video);
			}else{
				video = m_cms.getSitePath(res);
				setLink(video);
			}
			
			vfspath = m_cms.getSitePath(res);

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
			
			creationuser = getCreationUserName(res);

		} catch (CmsException e) {
			LOG.error("Error al obtener la informacion del video",e);
		}
		
	}
	
	public TfsVideo(CmsObject m_cms, I_TfsNoticia res, String videoPath, String videoTag, String codeVideo) throws CmsXmlException
	{
		CmsResource resource = null;
		String      videoVFS = null;
		
		this.cms = m_cms;
			try {
				if(videoPath == ""){
					resource = m_cms.readResource(res.getXmlDocument().getFile().getStructureId(),CmsResourceFilter.ALL);
				}
					resource = m_cms.readResource(videoPath,CmsResourceFilter.ALL);
				
			TimeZone  zone = TimeZone.getDefault();
			GregorianCalendar cal = new GregorianCalendar(zone, m_cms.getRequestContext().getLocale());

			cal.setTimeInMillis(resource.getDateLastModified());
			lastmodifieddate = cal.getTime();

			cal.setTimeInMillis(resource.getDateCreated());
			creationdate = cal.getTime();
			
			String mtags = res.getXmlDocument().getStringValue(m_cms,  videoTag +"keywords", res.getXmlDocumentLocale());
			if (mtags!=null)
				tags = mtags;
			
			CmsProperty prop = m_cms.readPropertyObject(resource, "video-formats", false);
			if (prop!=null)
				formats = prop.getValue();
			
			formatsMap = new HashMap<String, Boolean>(); 
			
			formatscount = 0;
			
			if(formats!=null){
				
			   String[] listFormats = formats.split(",");
                
                	if (listFormats!=null) {
						for (String formato : listFormats)
							{
								formatsMap.put(formato, true);
								formatscount++;
							}
					}
				}
			
			prop = m_cms.readPropertyObject(resource, "video-size", false);
			if (prop!=null)
				size = prop.getValue();
			
			prop = m_cms.readPropertyObject(resource, "video-bitrate", false);
			if (prop!=null)
				bitrate = prop.getValue();
			
			prop = m_cms.readPropertyObject(resource, "video-format", false);
			if (prop!=null)
				format = prop.getValue();

			String mtitulo = res.getXmlDocument().getStringValue(m_cms,  videoTag +"titulo", res.getXmlDocumentLocale());
			if (mtitulo!=null)
				title = mtitulo;
			
			String mdescripcion = res.getXmlDocument().getStringValue(m_cms,  videoTag +"descripcion", res.getXmlDocumentLocale());
			if (mdescripcion!=null)
				description = mdescripcion;
			
			String mrated = res.getXmlDocument().getStringValue(m_cms,  videoTag +"calificacion", res.getXmlDocumentLocale());
			if (mrated!=null)
				rated = mrated;
			
			prop = m_cms.readPropertyObject(resource, "hideComments", false);
			hideComments = false;
			if (prop!=null && prop.getValue()!=null){
				if(prop.getValue().trim().toLowerCase().equals("true"))
				   hideComments = true;
			}

			String mEnHome = res.getXmlDocument().getStringValue(m_cms,  videoTag + "mostrarEnHome", res.getXmlDocumentLocale());
			mostrarEnHome = false;
			if (mEnHome!=null){
				if(mEnHome.equals("true"))
					mostrarEnHome = true;
			}
			
			String mautor = res.getXmlDocument().getStringValue(m_cms,  videoTag + "autor", res.getXmlDocumentLocale());
			if (mautor!=null)
				author = mautor;
			
			String magency = res.getXmlDocument().getStringValue(m_cms,  videoTag + "fuente", res.getXmlDocumentLocale());
			if (magency!=null)
				agency = magency;

			String image = res.getXmlDocument().getStringValue(m_cms,  videoTag + "imagen", res.getXmlDocumentLocale());
			if (image!=null)
				thumbnail = image;
			
			String mAutoplay = res.getXmlDocument().getStringValue(m_cms,  videoTag + "autoplay", res.getXmlDocumentLocale());
			autoplay = false;
			if (mAutoplay!=null){
				if(mAutoplay.equals("true"))
					autoplay = true;
			}
			
			String mMute = res.getXmlDocument().getStringValue(m_cms,  videoTag + "mute", res.getXmlDocumentLocale());
			mute = false;
			if (mMute!=null){
				if(mMute.equals("true"))
					mute = true;
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
			
			data = res.getXmlDocument().getStringValue(m_cms,  videoTag + "" + codeVideo, res.getXmlDocumentLocale());
			
			if(videoPath==null || (videoPath!=null && videoPath.equals("")))
				videoPath = VideoCodeLoader.videoExistInBD(m_cms,data);
			 
			if(videoPath!=null && !videoPath.equals("")) {
				 CmsResource vidResource = cms.readResource(videoPath,CmsResourceFilter.ALL);  
				 videoResource = cms.readFile(vidResource);  
			}
			
			LOG.debug("TfsVideo >> " + videoTag + "" + codeVideo + " >> " + data);
			typeid = resource.getTypeId();
			video = "";
			
			if(!videoPath.equals("")){
			   type = OpenCms.getResourceManager().getResourceType(typeid).getTypeName();
			   vfspath = m_cms.getSitePath(resource);
			   video = m_cms.getSitePath(resource);
			   
			   prop = m_cms.readPropertyObject(resource, "video-duration", false);
				if (prop!=null)
					duration = prop.getValue();
				
			}else{
				
				String  videoIndex = getIndexVideos( m_cms, resource, type, data);
				
				if(videoTag.toLowerCase().indexOf("youtube")>-1){
					videoVFS = VideoCodeLoader.videoExist(m_cms,videoIndex, data, TfsResourceTypeVideoYoutubeLink.getStaticTypeName());
					type = TfsResourceTypeVideoYoutubeLink.getStaticTypeName();
				}
				if(videoTag.toLowerCase().indexOf("embedded")>-1){
					//VideoEmbeddedService videoEmbeddedService = new VideoEmbeddedService();
					//String videoCode = videoEmbeddedService.extractVideoCode(data);
					videoVFS = VideoCodeLoader.videoExist(m_cms,videoIndex,data , TfsResourceTypeVideoEmbedded.getStaticTypeName());
					type = TfsResourceTypeVideoEmbedded.getStaticTypeName();
				}
				
				video = data;
				vfspath = videoVFS;
				
				if(videoVFS==null || (videoVFS!=null && videoVFS.equals("")))
					 videoVFS = VideoCodeLoader.videoExistInBD(m_cms,data);
				
				if(videoVFS!=null && !videoVFS.equals("")){
				       CmsResource resourceVideoVFS = m_cms.readResource(videoVFS,CmsResourceFilter.ALL);
				       videoResource = cms.readFile(resourceVideoVFS);
				       
				       prop = m_cms.readPropertyObject(resourceVideoVFS, "video-duration", false);
						if (prop!=null)
							duration = prop.getValue();
				}
			}
			
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
			
			creationuser = getCreationUserName(resource);
			
			}catch (CmsException e) {
				LOG.error("Error al obtener la informacion del video",e);
				}
		
			
	}
	
	public TfsVideo(CmsObject m_cms, I_TfsNoticia res, String videoTag, String codeVideo) throws CmsXmlException
	{
		CmsResource resource = null;
		String      videoVFS = null;
		this.cms = m_cms;
			try {
				resource = m_cms.readResource(res.getXmlDocument().getFile().getStructureId(),CmsResourceFilter.ALL);
			
			    TimeZone  zone = TimeZone.getDefault();
			    GregorianCalendar cal = new GregorianCalendar(zone, m_cms.getRequestContext().getLocale());

			    cal.setTimeInMillis(resource.getDateLastModified());
			    lastmodifieddate = cal.getTime();

			    cal.setTimeInMillis(resource.getDateCreated());
			    creationdate = cal.getTime();
			
			    String mtags = res.getXmlDocument().getStringValue(m_cms,  videoTag +"keywords", res.getXmlDocumentLocale());
			    if (mtags!=null)
				    tags = mtags;
			
			    String mtitulo = res.getXmlDocument().getStringValue(m_cms,  videoTag +"titulo", res.getXmlDocumentLocale());
			    if (mtitulo!=null)
				    title = mtitulo;
			
			    String mdescripcion = res.getXmlDocument().getStringValue(m_cms,  videoTag +"descripcion", res.getXmlDocumentLocale());
			    if (mdescripcion!=null)
				     description = mdescripcion;
			
			     String mrated = res.getXmlDocument().getStringValue(m_cms,  videoTag +"calificacion", res.getXmlDocumentLocale());
			     if (mrated!=null)
				      rated = mrated;

			     String mEnHome = res.getXmlDocument().getStringValue(m_cms,  videoTag + "mostrarEnHome", res.getXmlDocumentLocale());
			     mostrarEnHome = false;
			     if (mEnHome!=null){
				     if(mEnHome.equals("true"))
					     mostrarEnHome = true;
			     }
			
			     String mautor = res.getXmlDocument().getStringValue(m_cms,  videoTag + "autor", res.getXmlDocumentLocale());
			     if (mautor!=null)
				    author = mautor;
			
			     String magency = res.getXmlDocument().getStringValue(m_cms,  videoTag + "fuente", res.getXmlDocumentLocale());
			     if (magency!=null)
				    agency = magency;

			     String image = res.getXmlDocument().getStringValue(m_cms,  videoTag + "imagen", res.getXmlDocumentLocale());
			     if (image!=null)
				     thumbnail = image;
			
			     String mAutoplay = res.getXmlDocument().getStringValue(m_cms,  videoTag + "autoplay", res.getXmlDocumentLocale());
			     autoplay = false;
			     if (mAutoplay!=null){
				 if(mAutoplay.equals("true"))
					  autoplay = true;
			     }
			     
			     String mMute = res.getXmlDocument().getStringValue(m_cms,  videoTag + "mute", res.getXmlDocumentLocale());
			     mute = false;
			     if (mMute!=null){
				 if(mMute.equals("true"))
					  mute = true;
			     }
	
					LOG.debug("TfsVideo >> " + videoTag + "" + codeVideo + " >> " + data);
			     data = res.getXmlDocument().getStringValue(m_cms,  videoTag + "" + codeVideo, res.getXmlDocumentLocale());
			     typeid = resource.getTypeId();
			     video = "";
			
				 String  videoIndex = getIndexVideos( m_cms, resource, type, data);
				
				 if(videoTag.toLowerCase().indexOf("youtube")>-1){
					videoVFS = VideoCodeLoader.videoExist(m_cms,videoIndex, data, TfsResourceTypeVideoYoutubeLink.getStaticTypeName());
					type = TfsResourceTypeVideoYoutubeLink.getStaticTypeName();
				 }
				 if(videoTag.toLowerCase().indexOf("embedded")>-1){
					//VideoEmbeddedService videoEmbeddedService = new VideoEmbeddedService();
					//String videoCode = videoEmbeddedService.extractVideoCode(data);
					videoVFS = VideoCodeLoader.videoExist(m_cms,videoIndex, data , TfsResourceTypeVideoEmbedded.getStaticTypeName());
					type = TfsResourceTypeVideoEmbedded.getStaticTypeName();
				 }
				
				 if(videoVFS==null || (videoVFS!=null && videoVFS.equals("")))
					 videoVFS = VideoCodeLoader.videoExistInBD(m_cms,data);
				 
				 if(videoVFS!=null && !videoVFS.equals("")) {
					 CmsResource vidResource = cms.readResource(videoVFS,CmsResourceFilter.ALL);  
					 videoResource = cms.readFile(vidResource);  
				 }
				 
				 video = data;
				 
				 subCategories = new HashMap<String, Boolean>();
				 categories = new HashMap<String, Boolean>();
				 
				 String xmlName = videoTag + "categoria";
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
					
				 creationuser = getCreationUserName(resource);
				 
			}catch (CmsException e) {
				LOG.error("Error al obtener la informacion del video",e);
			}
		
	}
	
	public TfsVideo(CmsObject m_cms, I_TfsNoticia res, int index, String videoPath, String videoTag, String codeVideo) throws CmsXmlException
	{
		CmsResource resource = null;
		String      videoVFS = null;
		this.cms = m_cms;
			try {
				if(videoPath == ""){
					resource = m_cms.readResource(res.getXmlDocument().getFile().getStructureId(),CmsResourceFilter.ALL);
				}
					resource = m_cms.readResource(videoPath,CmsResourceFilter.ALL);
			
			TimeZone  zone = TimeZone.getDefault();
			GregorianCalendar cal = new GregorianCalendar(zone, m_cms.getRequestContext().getLocale());

			cal.setTimeInMillis(resource.getDateLastModified());
			lastmodifieddate = cal.getTime();

			cal.setTimeInMillis(resource.getDateCreated());
			creationdate = cal.getTime();
			
			String mtags = res.getXmlDocument().getStringValue(m_cms,  videoTag +"["+index+"]/keywords", res.getXmlDocumentLocale());
			if (mtags!=null)
				tags = mtags;
			
			CmsProperty prop = m_cms.readPropertyObject(resource, "video-formats", false);
			if (prop!=null)
				formats = prop.getValue();
			
			formatsMap = new HashMap<String, Boolean>(); 
			
			formatscount = 0;
			
			if(formats!=null){
				
			   String[] listFormats = formats.split(",");
                
                	if (listFormats!=null) {
						for (String formato : listFormats)
							{
								formatsMap.put(formato, true);
								formatscount++;
							}
					}
				}
			
			prop = m_cms.readPropertyObject(resource, "video-size", false);
			if (prop!=null)
				size = prop.getValue();
			
			prop = m_cms.readPropertyObject(resource, "video-bitrate", false);
			if (prop!=null)
				bitrate = prop.getValue();
			
			prop = m_cms.readPropertyObject(resource, "video-format", false);
			if (prop!=null)
				format = prop.getValue();

			String mtitulo = res.getXmlDocument().getStringValue(m_cms,  videoTag +"["+index+"]/titulo", res.getXmlDocumentLocale());
			if (mtitulo!=null)
				title = mtitulo;
			
			String mdescripcion = res.getXmlDocument().getStringValue(m_cms,  videoTag +"["+index+"]/descripcion", res.getXmlDocumentLocale());
			if (mdescripcion!=null)
				description = mdescripcion;
			
			String mrated = res.getXmlDocument().getStringValue(m_cms,  videoTag +"["+index+"]/calificacion", res.getXmlDocumentLocale());
			if (mrated!=null)
				rated = mrated;
			
			prop = m_cms.readPropertyObject(resource, "hideComments", false);
			hideComments = false;
			if (prop!=null && prop.getValue()!=null){
				if(prop.getValue().trim().toLowerCase().equals("true"))
				   hideComments = true;
			}

			String mEnHome = res.getXmlDocument().getStringValue(m_cms,  videoTag + "["+index+"]/mostrarEnHome", res.getXmlDocumentLocale());
			mostrarEnHome = false;
			if (mEnHome!=null){
				if(mEnHome.equals("true"))
					mostrarEnHome = true;
			}
			
			String mautor = res.getXmlDocument().getStringValue(m_cms,  videoTag + "["+index+"]/autor", res.getXmlDocumentLocale());
			if (mautor!=null)
				author = mautor;
			
			String magency = res.getXmlDocument().getStringValue(m_cms,  videoTag + "["+index+"]/fuente", res.getXmlDocumentLocale());
			if (magency!=null)
				agency = magency;

			String image = res.getXmlDocument().getStringValue(m_cms,  videoTag + "["+index+"]/imagen", res.getXmlDocumentLocale());
			if (image!=null)
				thumbnail = image;
			
			String mAutoplay = res.getXmlDocument().getStringValue(m_cms,  videoTag + "["+index+"]/autoplay", res.getXmlDocumentLocale());
			autoplay = false;
			if (mAutoplay!=null){
				if(mAutoplay.equals("true"))
					autoplay = true;
			}
			
			String mMute = res.getXmlDocument().getStringValue(m_cms,  videoTag + "["+index+"]/mute", res.getXmlDocumentLocale());
			mute = false;
			if (mMute!=null){
				if(mMute.equals("true"))
					mute = true;
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
			
			data = res.getXmlDocument().getStringValue(m_cms,  videoTag + "["+index+"]/" + codeVideo, res.getXmlDocumentLocale());
			
			if(videoPath==null || (videoPath!=null && videoPath.equals("")))
				videoPath = VideoCodeLoader.videoExistInBD(m_cms,data);
			 
			if(videoPath!=null && !videoPath.equals("")) {
				 CmsResource vidResource = cms.readResource(videoPath,CmsResourceFilter.ALL);  
				 videoResource = cms.readFile(vidResource);  
			}
			
			LOG.debug("TfsVideo >> " + videoTag + "["+index+"]/" + codeVideo + " >> " + data);
			typeid = resource.getTypeId();
			video = "";
			
			if(!videoPath.equals("")){
			   type = OpenCms.getResourceManager().getResourceType(typeid).getTypeName();
			   vfspath = m_cms.getSitePath(resource);
			   video = m_cms.getSitePath(resource);
			   
			   prop = m_cms.readPropertyObject(resource, "video-duration", false);
				if (prop!=null)
					duration = prop.getValue();
				
			}else{
				
				String  videoIndex = getIndexVideos( m_cms, resource, type, data);
				
				if(videoTag.toLowerCase().indexOf("youtube")>-1){
					videoVFS = VideoCodeLoader.videoExist(m_cms,videoIndex, data, TfsResourceTypeVideoYoutubeLink.getStaticTypeName());
					type = TfsResourceTypeVideoYoutubeLink.getStaticTypeName();
				}
				if(videoTag.toLowerCase().indexOf("embedded")>-1){
					//VideoEmbeddedService videoEmbeddedService = new VideoEmbeddedService();
					//String videoCode = videoEmbeddedService.extractVideoCode(data);
					videoVFS = VideoCodeLoader.videoExist(m_cms,videoIndex, data , TfsResourceTypeVideoEmbedded.getStaticTypeName());
					type = TfsResourceTypeVideoEmbedded.getStaticTypeName();
				}
				
				video = data;
				vfspath = videoVFS;
				
				if(videoVFS==null || (videoVFS!=null && videoVFS.equals("")))
					videoVFS = VideoCodeLoader.videoExistInBD(m_cms,data);
				
				if(videoVFS!=null && !videoVFS.equals("")){
				       CmsResource resourceVideoVFS = m_cms.readResource(videoVFS,CmsResourceFilter.ALL);
				       videoResource = cms.readFile(resourceVideoVFS); 
				       
				       prop = m_cms.readPropertyObject(resourceVideoVFS, "video-duration", false);
						if (prop!=null)
							duration = prop.getValue();
				}
			}
			
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
			
			creationuser = getCreationUserName(resource);
			
			}catch (CmsException e) {
				LOG.error("Error al obtener la informacion del video",e);
				}
		
	}
	
	public TfsVideo(CmsObject m_cms, I_TfsNoticia res, int index, String videoTag, String codeVideo) throws CmsXmlException
	{
		CmsResource resource = null;
		String      videoVFS = null;
		this.cms = m_cms;
			try {
				resource = m_cms.readResource(res.getXmlDocument().getFile().getStructureId(),CmsResourceFilter.ALL);
			
			    TimeZone  zone = TimeZone.getDefault();
			    GregorianCalendar cal = new GregorianCalendar(zone, m_cms.getRequestContext().getLocale());

			    cal.setTimeInMillis(resource.getDateLastModified());
			    lastmodifieddate = cal.getTime();

			    cal.setTimeInMillis(resource.getDateCreated());
			    creationdate = cal.getTime();
			
			    String mtags = res.getXmlDocument().getStringValue(m_cms,  videoTag +"["+index+"]/keywords", res.getXmlDocumentLocale());
			    if (mtags!=null)
				    tags = mtags;
			
			    String mtitulo = res.getXmlDocument().getStringValue(m_cms,  videoTag +"["+index+"]/titulo", res.getXmlDocumentLocale());
			    if (mtitulo!=null)
				    title = mtitulo;
			
			    String mdescripcion = res.getXmlDocument().getStringValue(m_cms,  videoTag +"["+index+"]/descripcion", res.getXmlDocumentLocale());
			    if (mdescripcion!=null)
				     description = mdescripcion;
			
			     String mrated = res.getXmlDocument().getStringValue(m_cms,  videoTag +"["+index+"]/calificacion", res.getXmlDocumentLocale());
			     if (mrated!=null)
				      rated = mrated;

			     String mEnHome = res.getXmlDocument().getStringValue(m_cms,  videoTag + "["+index+"]/mostrarEnHome", res.getXmlDocumentLocale());
			     mostrarEnHome = false;
			     if (mEnHome!=null){
				     if(mEnHome.equals("true"))
					     mostrarEnHome = true;
			     }
			
			     String mautor = res.getXmlDocument().getStringValue(m_cms,  videoTag + "["+index+"]/autor", res.getXmlDocumentLocale());
			     if (mautor!=null)
				    author = mautor;
			
			     String magency = res.getXmlDocument().getStringValue(m_cms,  videoTag + "["+index+"]/fuente", res.getXmlDocumentLocale());
			     if (magency!=null)
				    agency = magency;

			     String image = res.getXmlDocument().getStringValue(m_cms,  videoTag + "["+index+"]/imagen", res.getXmlDocumentLocale());
			     if (image!=null)
				     thumbnail = image;
			
			     String mAutoplay = res.getXmlDocument().getStringValue(m_cms,  videoTag + "["+index+"]/autoplay", res.getXmlDocumentLocale());
			     autoplay = false;
			     if (mAutoplay!=null){
				 if(mAutoplay.equals("true"))
					  autoplay = true;
			     }
			     
			     String mMute = res.getXmlDocument().getStringValue(m_cms,  videoTag + "["+index+"]/mute", res.getXmlDocumentLocale());
			     mute = false;
			     if (mMute!=null){
				 if(mMute.equals("true"))
					  mute = true;
			     }
	
					LOG.debug("TfsVideo >> " + videoTag + "["+index+"]/" + codeVideo + " >> " + data);
			     data = res.getXmlDocument().getStringValue(m_cms,  videoTag + "["+index+"]/" + codeVideo, res.getXmlDocumentLocale());
			     typeid = resource.getTypeId();
			     video = "";
			
				 String  videoIndex = getIndexVideos( m_cms, resource, type, data);
				
				 if(videoTag.toLowerCase().indexOf("youtube")>-1){
					videoVFS = VideoCodeLoader.videoExist(m_cms,videoIndex, data, TfsResourceTypeVideoYoutubeLink.getStaticTypeName());
					type = TfsResourceTypeVideoYoutubeLink.getStaticTypeName();
				 }
				 if(videoTag.toLowerCase().indexOf("embedded")>-1){
					//VideoEmbeddedService videoEmbeddedService = new VideoEmbeddedService();
					//String videoCode = videoEmbeddedService.extractVideoCode(data);
					videoVFS = VideoCodeLoader.videoExist(m_cms,videoIndex, data , TfsResourceTypeVideoEmbedded.getStaticTypeName());
					type = TfsResourceTypeVideoEmbedded.getStaticTypeName();
				 }
				 
				 if(videoVFS==null || (videoVFS!=null && videoVFS.equals("")))
						videoVFS = VideoCodeLoader.videoExistInBD(m_cms,data);
				 
				 if(!videoVFS.equals("")) {
					 CmsResource vidResource = cms.readResource(videoVFS,CmsResourceFilter.ALL);  
					 videoResource = cms.readFile(vidResource);  
				 }
				 
				 video = data;
				 
				 subCategories = new HashMap<String, Boolean>();
				 categories = new HashMap<String, Boolean>();
				 
				 String xmlName = videoTag + "["+index+"]/categoria";
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
				
				 creationuser = getCreationUserName(resource);
			}catch (CmsException e) {
				LOG.error("Error al obtener la informacion del video",e);
			}
		
	}
	public String getVfspath(){
		return vfspath;
	}
	
	public void setVfspath(String path){
		this.vfspath = path;
	}
	
	public String getFormat(){
		return format;
	}
	
	public void setFormat(String format){
		this.format = format;
	}
	
	public String getDuration(){
		return duration;
	}
	
	public void setDuration(String duration){
		this.duration = duration;
	}
	
	public String getBitrate(){
		return bitrate;
	}
	
	public void setBitrate(String bitrate){
		this.bitrate = bitrate;
	}
	
	public String getSize(){
		return size;
	}
	
	public void setSize(String size){
		this.size = size;
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
	
	public String getFormats() {
		return formats;
	}

	public void setFormats(String formats) {
		this.formats = formats;
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

	public String getData() {
		return data;
	}

	public void setFormatscount(int formatscount){
		this.formatscount = formatscount;
	}
	
	public int getFormatscount(){
		return formatscount;
	}
	
	public String getVideo() {
		return video;
	}

	public void setVideo(String video) {
		this.video = video;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
	
	public void setData(String data) {
		this.data = data;
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

	public void setFormatslist(List<String> formatslist) {
		this.formatslist = formatslist;
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
	
	public void setMute(boolean mute)
	{
		this.mute = mute;
	}
	
	public boolean isMute()
	{
		return this.mute;
	}
	
	public String getEmbedCode(){
		return TfsVideoHelper.getVideoEmbedCode(this.vfspath,this.cms);
	}
	
	public String getCreationUser(){
		return creationuser;
	}
	
	private String getIndexVideos(CmsObject m_cms, CmsResource resource, String type, String videoCode){
		
		String videoIndex = "VIDEOS_OFFLINE";
		
		TipoEdicionService tEService = new TipoEdicionService();
		try {
			TipoEdicion tEdicion = tEService.obtenerTipoEdicion(m_cms, m_cms.getRequestContext().removeSiteRoot(resource.getRootPath()));
			
			if (tEdicion!=null) {
				Boolean isProjectOnline = m_cms.getRequestContext().currentProject().isOnlineProject();
			
				if(isProjectOnline)
					videoIndex = tEdicion.getVideosIndex();
				else
					videoIndex = tEdicion.getVideosIndexOffline();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return videoIndex;
	}
	
	private String getCreationUserName(CmsResource resource) {
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
	
	public long getLongDateExpired() {
		return videoResource.getDateExpired();
	}
	
	public boolean isDateExpiredSet() {
		if(videoResource==null)
			return false;
		else
			return videoResource.getDateExpired() != CmsResource.DATE_EXPIRED_DEFAULT;
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
		if(videoResource==null)
			return false;
		else
			return videoResource.getDateReleased() != CmsResource.DATE_RELEASED_DEFAULT;
	}
	
	public long getLongDateReleased() {
		return videoResource.getDateReleased();
	}
	
	public Date getDateExpired() {
		TimeZone zone = TimeZone.getDefault();
        GregorianCalendar cal = new GregorianCalendar(zone, cms.getRequestContext().getLocale());
        cal.setTimeInMillis(videoResource.getDateExpired());
        return cal.getTime();
	}
	
	public Date getDateReleased() {
		TimeZone zone = TimeZone.getDefault();
        GregorianCalendar cal = new GregorianCalendar(zone, cms.getRequestContext().getLocale());
        cal.setTimeInMillis(videoResource.getDateReleased());
        return cal.getTime();
	}
}
