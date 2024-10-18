package com.tfsla.diario.friendlyTags;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;

import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;

import com.tfsla.diario.model.TfsListaVideos;
import com.tfsla.diario.model.TfsVideo;
import com.tfsla.diario.model.TfsVideoFormat;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsVideoFormats extends BaseTag  implements I_TfsCollectionListTag {
	private static final long serialVersionUID = 6738569134100708681L;
	
	List<TfsVideo> videos = new ArrayList<TfsVideo>();
	TfsVideo video = null;
	int index = -1;
	TfsVideo previousVideo = null;
	TfsListaVideos previousVideos = null;	
	private String format = null;

	@Override
	public int doStartTag() throws JspException {
		 TfsVideo current = (TfsVideo) pageContext.getRequest().getAttribute("video");
		 
		 init();
			
		 findFormats(current.getVfspath());
					
		 return (hasMoreContent() ? EVAL_BODY_INCLUDE : SKIP_BODY );		
	}
	
	public boolean hasMoreContent() {
		if(videos == null)
			return false;
		
		index++;
		
		if (index < videos.size())
			exposeFormats();
		else
			restoreFormats();
		
		return (index < videos.size());
	}
	
	protected void exposeFormats() {
		video = this.videos.get(index);

		pageContext.getRequest().setAttribute("videos", videos);
		pageContext.getRequest().setAttribute("video", video);
    }
	
	 protected void restoreFormats() {
		 pageContext.getRequest().setAttribute("videos", previousVideos );
		 pageContext.getRequest().setAttribute("video", previousVideo);
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
	
	public int getIndex() {
		return index;
	}
	
	public boolean isLast() {
		return (index == videos.size() - 1);
	}
	
	@SuppressWarnings("unchecked")
	public void findFormats(String videoPath) {
		CmsResource res = null;
		videos = null;
		index = -1;
		List<TfsVideo> formatsList = new ArrayList<TfsVideo>();
		
		try {
			 res = m_cms.readResource(videoPath);
			 //Si no se filtra, buscar formatos por relaciones del VFS
			 if(format == null || format.equals("")) {
				 String siteName = m_cms.getRequestContext().getSiteRoot();
				 int videoLinkTypeID = OpenCms.getResourceManager().getResourceType("video-link").getTypeId();
				 int videoVodLinkTypeID = OpenCms.getResourceManager().getResourceType("videoVod-link").getTypeId();
				 List<CmsRelation> relations = (List<CmsRelation>)m_cms.getRelationsForResource(res, CmsRelationFilter.TARGETS);
				 for(CmsRelation relation : relations) {
					 CmsResource resRelation = m_cms.readResource(relation.getTargetPath().replace(siteName, ""));
					 if(resRelation.getTypeId() != videoLinkTypeID  &&  resRelation.getTypeId()!= videoVodLinkTypeID ) continue;
					 String formatPath = relation.getTargetPath().replace(siteName, "");
					 CmsResource resource = m_cms.readResource(formatPath);
					 video = new TfsVideo(m_cms, resource);
					 formatsList.add(video);
					 videos = formatsList;
				 }
			 } else {
				 //Si se filtra, buscar por property video-formats
				 CmsProperty prop;
				 String StrFormats = "";
					
				 prop = m_cms.readPropertyObject(res, "video-formats", false);
				 if (prop != null) {
					StrFormats = prop.getValue();
						
					if (StrFormats != null && !StrFormats.equals("")) {
					    String[] listFormats = StrFormats.split(",");
					    
					    for(int i = 0; i <= listFormats.length-1; i++) {
					    	String listFormat = listFormats[i].trim();
					        
					        if((format != null && format.equals(listFormat)) || format == null) {
						    	TfsVideoFormat videoFormat = new TfsVideoFormat(m_cms, videoPath, listFormat);
						    	String formatPath = videoFormat.getVideoFormatPath();
						    	CmsResource resource = m_cms.readResource(formatPath);
						    	video = new TfsVideo(m_cms, resource);
						    	formatsList.add(video);
						    	videos = formatsList;
					    	}
					    }
					}
				}
			}
			 
			previousVideos = (TfsListaVideos) pageContext.getRequest().getAttribute("videos");
			previousVideo = (TfsVideo) pageContext.getRequest().getAttribute("video");
				
		    pageContext.getRequest().setAttribute("videos", null);
		    pageContext.getRequest().setAttribute("video", null);
		} catch (CmsException e) {
			e.printStackTrace();
		}
	}
	
	public String getCollectionValue(String name) throws JspTagException {
		if (name.equals("creationDate"))
			return "" + video.getCreationdate().getTime(); 

		if (name.equals("lastmodifieddate"))
			return "" + video.getLastmodifieddate().getTime(); 

		String elementName = getElementName(name);
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.formats")))
			return (video.getFormats() != null ? video.getFormats() : "");

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
	
		return "";
	}
	
	private String getElementName(String elementName) {
		String[] parts = elementName.split("/");
		String element = parts[parts.length-1];
		int end = element.indexOf("[");
		
		return element.substring(0, end);
	}

	public String getCollectionPathName() {
		return "";
	}
	
	public String getCollectionIndexValue(String name, int index) {
		if (name.contains(TfsXmlContentNameProvider.getInstance().getTagName("news.video.formats"))) {
			try {
				 TfsVideo formatValue = (TfsVideo) videos.get(index);
				 return formatValue.getFormat();
			} catch (IndexOutOfBoundsException e) {
				return "";
			}
		}

		return null;
	}
	
	public int getCollectionIndexSize(String name, boolean isCollectionPart) {
		return videos.size();
	}
	
	public String getFormat() {
		return format; 
	}

	public void setFormat(String format) {
		this.format = format;
	}
}
