package com.tfsla.diario.friendlyTags;


import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import com.tfsla.diario.model.TfsVideo;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsVideoFSTag extends BaseTag  implements I_TfsCollectionListTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = -758226680220488939L;
	
	CmsResource resVideo = null;
	TfsVideo video = null;
	TfsVideo previousVideo = null;

	private String path;
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

    protected void restoreVideo()
    {
		pageContext.getRequest().setAttribute("video", previousVideo);
    }
    
    protected void exposeVideo()
    {
    	
		video = new TfsVideo(m_cms,resVideo);

		pageContext.getRequest().setAttribute("video", video);
    	
    }
    
	protected void saveVideo()
    {
		previousVideo = (TfsVideo) pageContext.getRequest().getAttribute("video");
    	
    	pageContext.getRequest().setAttribute("video",null);
    }

	@Override
	public int doStartTag() throws JspException {
		
		
		init();
		try {
			findVideos();
		} catch (CmsException e) {
			e.printStackTrace();
		}
				
		return (resVideo!=null ? EVAL_BODY_INCLUDE : SKIP_BODY );		
	}
	
	@Override
	public int doEndTag() {
		restoreVideo();
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return EVAL_PAGE;
	}

	protected void findVideos() throws CmsException
	{
		
		resVideo = null;

		saveVideo();
		
		resVideo = m_cms.readResource(path,CmsResourceFilter.ALL);
		exposeVideo();
	}
	
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

		if (	elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode")) ||
				elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube.id"))
			)
			return (video.getData() != null ? video.getData() : "");

		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.video")))
			return (video.getVideo() != null ? video.getVideo() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.formats")))
			return (video.getFormats() != null ? video.getFormats() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.duration")))
			return (video.getDuration() != null ? video.getDuration() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.size")))
			return (video.getSize() != null ? video.getSize() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.bitrate")))
			return (video.getBitrate() != null ? video.getBitrate() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.format")))
			return (video.getFormat() != null ? video.getFormat() : "");

		if (elementName.contains(TfsXmlContentNameProvider.getInstance().getTagName("news.image.categories")))
			return video.getCategorylist().get(getElementIndex(name)-1);
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.vfspath")))
			return (video.getVfspath() != null ? video.getVfspath() : "");

		return "";
	}
	
	private int getElementIndex(String elementName)
	{
		String[] parts = elementName.split("/");
		String element = parts[parts.length-1];
		
		int start = element.indexOf("[")+1;
		int end = element.indexOf("]");
		
		return Integer.parseInt(element.substring(start, end));
		
	}

	private String getElementName(String elementName)
	{
		String[] parts = elementName.split("/");
		String element = parts[parts.length-1];
		
		int end = element.indexOf("[");
		
		return element.substring(0, end);
		
	}

	@Override
	public int getIndex() {
		return 0;
	}

	@Override
	public boolean isLast() {
		return true;
	}

	@Override
	public String getCollectionPathName() throws JspTagException {
		return "";
	}

	@Override
	public String getCollectionIndexValue(String name, int index)
			throws JspTagException {
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

	@Override
	public int getCollectionIndexSize(String name, boolean isCollectionPart)
			throws JspTagException {
		// TODO Auto-generated method stub
		return 0;
	}


}
