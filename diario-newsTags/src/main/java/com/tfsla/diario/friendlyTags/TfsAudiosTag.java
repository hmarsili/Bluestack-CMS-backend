package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlException;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.model.TfsAudio;
import com.tfsla.diario.model.TfsVideo;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsAudiosTag extends A_TfsNoticiaCollectionWithBlanks  {

	TfsAudio audio = null;
	TfsAudio previousAudio = null;
	
	private TipoEdicion currentPublication;
    private String audioIndex = "AUDIO_OFFLINE";
	
    protected transient CmsObject m_cms;
    protected CmsFlexController m_controller;
	
	@Override
	public int doStartTag() throws JspException {

		keyControlName = TfsXmlContentNameProvider.getInstance().getTagName("news.audio"); //"audio";

		init(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.audiopath")); //audio

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
					exposeAudio(noticia);
				} catch (CmsXmlException e) {
					e.printStackTrace();
				}
				withElement=true;
			}else{
				index++;
				restoreAudio();
			}
		}
		
		return (index<=lastElement);
	}
	
	protected void restoreAudio()
    {    	
		pageContext.getRequest().setAttribute("audio", previousAudio);
    }    
    
    protected void exposeAudio(I_TfsNoticia noticia) throws CmsXmlException
    {   
		m_controller = CmsFlexController.getController(pageContext.getRequest());
        m_cms = m_controller.getCmsObject();
        
        String audioVfsPath = getIndexElementValue(noticia,keyControlName);
        
        String path = "";
		try {
			path = getCollectionPathName();
		} catch (JspTagException e) {
			e.printStackTrace();
		}

		if (!path.equals(""))
	    	path += "/";
		
        
        //String path = getIndexElementValue(noticia,keyControlName);
		//try {
		//	path = getCollectionPathName();
		//} catch (JspTagException e) {
		//	e.printStackTrace();
		//}

	//	if (!path.equals(""))
	 //   	path += "/";
		
    	try {
			audio = new TfsAudio(m_cms, getCurrentNews(), audioVfsPath, path);
		} catch (JspTagException e) {
			e.printStackTrace();
		}		
		pageContext.getRequest().setAttribute("audio", audio);
    	
    }   
    
	@Override
	public int getIndex() {
		return index -1;
	}	

	@Override
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
		

		if (elementName.contains(TfsXmlContentNameProvider.getInstance().getTagName("news.image.categories")))
			return audio.getCategorylist().get(getElementIndex(name)-1);

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
				return audio.getCategorylist().get(index);
			}
			catch (IndexOutOfBoundsException e) {
				return "";
			}
		} 

		return null;
	}

}
