package com.tfsla.diario.friendlyTags;

import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlException;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.model.TfsVideo;
import com.tfsla.diario.multiselect.VideoCodeLoader;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsVideosVodEmbeddedRelatedTag  extends A_TfsNoticiaCollectionWithBlanks  {

	TfsVideo video = null;
	TfsVideo previousVideo = null;

    private TipoEdicion currentPublication;
    private String videoIndex = "VIDEOS_OFFLINE";
    private String category;
    
    protected transient CmsObject m_cms;
    protected CmsFlexController m_controller;
    	
	@Override
	public int doStartTag() throws JspException {

		keyControlName = TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode"); //codigo

		init(TfsXmlContentNameProvider.getInstance().getTagName("news.vod.embedded")); //videoEmbedded

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
			
			boolean showVideo = category==null ;
			
			String controlValue = getIndexElementValue(noticia,keyControlName);
			if (!controlValue.trim().equals("")){
				try {
					exposeVideo(noticia);
				} catch (CmsXmlException e) {
					e.printStackTrace();
				}
				if (category!=null) {
					List<String> categoriaVideo  = video.getCategorylist();
					String[] categorias = category.split(",");
					for (String catEnVideo : categoriaVideo) {
						for (String categoria : categorias) {
							if (catEnVideo.toUpperCase().contains(categoria.toUpperCase())) {
								showVideo = true;
							}
						}
					}
					if (!showVideo) {
						video = null;
						index++;
						restoreVideo();
					} else {
						withElement=true;
					}
				} else {
					withElement=true;
				}
				
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
        String videoType = "videoVod-embedded";
        String videoCode = getIndexElementValue(noticia,keyControlName, index);
        
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
    		int idx = selectedItems!=null && !selectedItems.isEmpty() ? selectedItems.get(index-1) : index;
    		
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
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (currentPublication !=null) {
				Boolean isProjectOnline = m_cms.getRequestContext().currentProject().isOnlineProject();
			
				if(isProjectOnline)
					videoIndex = currentPublication.getVideosIndex();
				else
					videoIndex = currentPublication.getVideosIndexOffline();
			}
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
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.formats")))
			//return video.getFormatslist().get(getElementIndex(name)-1);
			return (video.getFormats() != null ? video.getFormats() : "");
		

		if (	elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode")) ||
				elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube.id"))
			)
			return (video.getData() != null ? video.getData() : "");

		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.video")))
			return (video.getVideo() != null ? video.getVideo() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.format")))
			return (video.getFormat() != null ? video.getFormat() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.vfspath")))
			return (video.getVfspath() != null ? video.getVfspath() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.duration")))
			return (video.getDuration() != null ? video.getDuration() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.size")))
			return (video.getSize() != null ? video.getSize() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.bitrate")))
			return (video.getBitrate() != null ? video.getBitrate() : "");

		if (elementName.contains(TfsXmlContentNameProvider.getInstance().getTagName("news.image.categories")))
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
	
	private int getElementIndex(String elementName) {
		String[] parts = elementName.split("/");
		String element = parts[parts.length-1];
		
		int start = element.indexOf("[")+1;
		int end = element.indexOf("]");
		
		return Integer.parseInt(element.substring(start, end));
		
	}
	
	 public String getCategory() {
			return category;
		}

		public void setCategory(String categoria) {
			this.category = categoria;
		}
}

