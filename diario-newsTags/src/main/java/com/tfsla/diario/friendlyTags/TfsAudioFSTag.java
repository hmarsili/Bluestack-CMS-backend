package com.tfsla.diario.friendlyTags;


import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;

import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import com.tfsla.diario.model.TfsAudio;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsAudioFSTag extends BaseTag  implements I_TfsCollectionListTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = -758226680220488939L;
	
	CmsResource resAudio = null;
	TfsAudio audio = null;
	TfsAudio previousAudio = null;

	private String path;
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

    protected void restoreAudio()
    {
		pageContext.getRequest().setAttribute("audio", previousAudio);
    }
    
    protected void exposeVideo()
    {
    	
		audio = new TfsAudio(m_cms,resAudio);

		pageContext.getRequest().setAttribute("audio", audio);
    	
    }
    
	protected void saveVideo()
    {
		previousAudio = (TfsAudio) pageContext.getRequest().getAttribute("audio");
    	
    	pageContext.getRequest().setAttribute("audio",null);
    }

	@Override
	public int doStartTag() throws JspException {
		
		
		init();
		try {
			findAudios();
		} catch (CmsException e) {
			e.printStackTrace();
		}
				
		return (resAudio!=null ? EVAL_BODY_INCLUDE : SKIP_BODY );		
	}
	
	@Override
	public int doEndTag() {
		restoreAudio();
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return EVAL_PAGE;
	}

	protected void findAudios() throws CmsException
	{
		
		resAudio = null;

		saveVideo();
		
		resAudio = m_cms.readResource(path);
		exposeVideo();
	}
	
	public String getCollectionValue(String name) throws JspTagException {

		if (name.equals("creationDate"))
			return "" + audio.getCreationdate().getTime(); 

		if (name.equals("lastmodifieddate"))
			return "" + audio.getLastmodifieddate().getTime(); 

		String elementName = getElementName(name);

		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.title")))
			return (audio.getTitle() != null ? audio.getTitle() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.description")))
			return (audio.getDescription() != null ? audio.getDescription() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.rated")))
			return (audio.getRated() != null ? audio.getRated() : "");
				
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.thumbnail")))
			return (audio.getThumbnail() != null ? audio.getThumbnail() : "");
				
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.source")))
			return (audio.getAgency() != null ? audio.getAgency() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.author")))
			return (audio.getAuthor() != null ? audio.getAuthor() : "");

		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.keywords")))
			return (audio.getTags() != null ? audio.getTags() : "");


		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.audio")))
			return (audio.getAudio() != null ? audio.getAudio() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.audiopath")))
			return (audio.getAudio() != null ? audio.getAudio() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.vfspath")))
			return (audio.getVfspath() != null ? audio.getVfspath() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.duration")))
			return (audio.getDuration() != null ? audio.getDuration() : "");
		
		if (elementName.contains(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.categories")))
			return audio.getCategorylist().get(getElementIndex(name)-1);

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

	public String getCollectionIndexValue(String name, int index) {
		if (name.contains(TfsXmlContentNameProvider.getInstance().getTagName("news.image.categories")))
		{
			try {
				return audio.getCategorylist().get(index);
			}
			catch (IndexOutOfBoundsException e) {
				return "";
			}
		} 

		return null;
	}
	
	public int getCollectionIndexSize(String name, boolean isCollectionPart)
	{
		return audio.getCategorylist().size();
		
	}


}
