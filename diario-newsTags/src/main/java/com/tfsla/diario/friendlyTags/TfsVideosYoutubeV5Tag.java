package com.tfsla.diario.friendlyTags;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearch;
import org.opencms.search.CmsSearchResult;
import org.opencms.xml.CmsXmlException;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.file.types.TfsResourceTypeVideoEmbedded;
import com.tfsla.diario.model.TfsListaVideos;
import com.tfsla.diario.model.TfsVideo;
import com.tfsla.diario.multiselect.VideoCodeLoader;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsVideosYoutubeV5Tag<TfsVideosYoutube> extends A_TfsNoticiaCollectionWithBlanks  {
	

	TfsVideo video = null;
	TfsVideo previousVideo = null;
	private String siteName;
    private String publication;
    private TipoEdicion currentPublication;
    private String videoIndex = "VIDEOS_OFFLINE";
    protected transient CmsObject m_cms;
    protected CmsFlexController m_controller;
    //int index=0;
	@Override
	public int doStartTag() throws JspException {		

		keyControlName = TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube.id");

		init(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube"));
		
		return (hasMoreContent() ? EVAL_BODY_INCLUDE : SKIP_BODY );
	}

	@Override
	public int doAfterBody() throws JspException {

		if (hasMoreContent()) {
			return EVAL_BODY_AGAIN;
		}
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {

		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return EVAL_PAGE;
	}	
	
	@Override
	protected boolean hasMoreContent() {
		index++;

		boolean withElement=false;
		
		while (index<=lastElement && !withElement) {
			I_TfsNoticia noticia;
			try {
				noticia = getCurrentNews();
			} catch (JspTagException e) {
				return false;
			}
			String controlValue = getIndexElementValue(noticia,keyControlName);
			if (!controlValue.trim().equals("")){
				try {
					exposeVideo(noticia);
				} catch (CmsXmlException e) {
					e.printStackTrace();
				}
				withElement=true;
			}else{
				index++;
				restoreVideo();
			}
		}
		
		return (index<=lastElement);
	}
	
	protected void restoreVideo()
    {    	
		pageContext.getRequest().setAttribute("video", previousVideo);
    }    
    
    protected void exposeVideo(I_TfsNoticia noticia) throws CmsXmlException
    {   	
		m_controller = CmsFlexController.getController(pageContext.getRequest());
        m_cms = m_controller.getCmsObject();
        String videoType = "video-youtube";
        String videoCode = getIndexElementValue(noticia,keyControlName);
        
        
        
        String path = "";
		try {
			path = getCollectionPathName();
		} catch (JspTagException e) {
			e.printStackTrace();
		}

		if (!path.equals(""))
	    	path += "/";
	    
        String videoPath = videoExist(m_cms, videoCode, videoType);
        
        
    	try {
    		
    		if(videoPath!=null && !videoPath.equals(""))
		     	video = new TfsVideo(m_cms, getCurrentNews(), videoPath, path, keyControlName);
    		else
    			video = new TfsVideo(m_cms, getCurrentNews(), path, keyControlName);
    		
    	} catch (JspTagException e) {
			e.printStackTrace();
		}		
		pageContext.getRequest().setAttribute("video", video);
    	
    }
    
    private String videoExist(CmsObject cmsObject, String videoCode, String type)
	{
    	
    	String siteName = OpenCms.getSiteManager().getCurrentSite(m_cms).getSiteRoot();    	
		String project = siteName.substring(siteName.lastIndexOf("/")+1);				
    	TipoEdicionService tService = new TipoEdicionService();
		
		if (tService!=null) {
			try {
				currentPublication = tService.obtenerEdicionOnlineRoot(project);
				
				if (currentPublication !=null) {
					Boolean isProjectOnline = m_cms.getRequestContext().currentProject().isOnlineProject();
				
					if(isProjectOnline)
						videoIndex = currentPublication.getVideosIndex();
					else
						videoIndex = currentPublication.getVideosIndexOffline();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			//videoIndex = currentPublication.getVideosIndex();
			publication = "" + currentPublication.getId();
		}
    	
		return VideoCodeLoader.videoExist(cmsObject, videoIndex, videoCode, type);
	}
    
	@Override
	public int getIndex() {
		return index -1;
	}	

	@Override
	public String getCollectionValue(String name) throws JspTagException {
		if (name.equals("creationDate"))
			return "" + video.getCreationdate().getTime(); 

		if (name.equals("lastmodifieddate"))
			return "" + video.getLastmodifieddate().getTime(); 

		String elementName = getElementName(name);

		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.title")))
			return (video.getTitle() != null ? video.getTitle() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.description")))
			return (video.getDescription() != null ? video.getDescription() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.rated")))
			return (video.getRated() != null ? video.getRated() : "");
				
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.thumbnail")))
			return (video.getThumbnail() != null ? video.getThumbnail() : "");
				
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.source")))
			return (video.getAgency() != null ? video.getAgency() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.author")))
			return (video.getAuthor() != null ? video.getAuthor() : "");

		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.keywords")))
			return (video.getTags() != null ? video.getTags() : "");
		
		//if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.formats")))
			//return video.getFormatslist().get(getElementIndex(name)-1);
			//return (video.getFormats() != null ? video.getFormats() : "");
		

		if (	elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode")) ||
				elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube.id"))
			)
			return (video != null && video.getData() != null ? video.getData() : "");
			//return (video.getData() != null ? video.getData() : "");

		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.video")))
			return (video.getVideo() != null ? video.getVideo() : "");
		
		//if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.format")))
		//	return (video.getFormat() != null ? video.getFormat() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.vfspath")))
			return (video.getVfspath() != null ? video.getVfspath() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.duration")))
			return (video.getDuration() != null ? video.getDuration() : "");
		
		//if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.size")))
		//	return (video.getSize() != null ? video.getSize() : "");
		
	//	if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.bitrate")))
	//		return (video.getBitrate() != null ? video.getBitrate() : "");

		if (elementName.contains(TfsXmlContentNameProvider.getInstance().getTagName("news.video.categories")))
			return video.getCategorylist().get(getElementIndex(name)-1);

		return "";
	}
	
	private String getElementName(String elementName)
	{
		String[] parts = elementName.split("/");
		String element = parts[parts.length-1];
		
		int end = element.indexOf("[");
		
		return element.substring(0, end);
		
	}
	
	private int getElementIndex(String elementName)
	{
		String[] parts = elementName.split("/");
		String element = parts[parts.length-1];
		
		int start = element.indexOf("[")+1;
		int end = element.indexOf("]");
		
		return Integer.parseInt(element.substring(start, end));
		
	}

	@Override
	public String getCollectionIndexValue(String name, int index) {
		if (name.contains(TfsXmlContentNameProvider.getInstance().getTagName("news.image.categories")))
		{
			try {
				return video.getCategorylist().get(index);
			}
			catch (IndexOutOfBoundsException e) {
				return "";
			}
		} 

		return null;
	}
}
