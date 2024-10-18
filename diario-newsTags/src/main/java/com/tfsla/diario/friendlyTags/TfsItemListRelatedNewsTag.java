package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlException;

import com.tfsla.diario.model.TfsListaNoticias;
import com.tfsla.diario.model.TfsNoticia;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsItemListRelatedNewsTag 
	extends A_TfsNoticiaResourceCollection
	implements I_TfsNoticia, I_TfsCollectionListTag {

	 /**
	 * 
	 */
	private static final long serialVersionUID = -6306131189117954470L;

	TfsNoticia previousNoticia = null;
	TfsListaNoticias previousListaNoticia = null;
	
	@Override
	 public int doStartTag() throws JspException {

		init(TfsXmlContentNameProvider.getInstance().getTagName("news.itemList.relatedNews")); 

		
		saveNoticia();
		
		//index = -1;
		
		if (hasMoreContent()) {
			return EVAL_BODY_INCLUDE;
		}
		
		return SKIP_BODY;
		
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
			String controlValue = getIndexElementValue(noticia,"",index);
			
			LOG.debug("TfsItemListRelatedNewsTag > hasMoreContent (" + index + "/" + lastElement + "). Noticia: " + controlValue);

			if(controlValue!=null && !controlValue.trim().equals(""))
			{
				withElement=true;
				initResource(controlValue);
			}else{
				index++;
			}

			setIndex(index);
		}

		if (index > lastElement)
			restoreNoticia();
		
		return (index<=lastElement);
	}

    
	@Override
	public int doAfterBody() throws JspException {

		if (hasMoreContent()) {
			return EVAL_BODY_AGAIN;
		}
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		
		restoreNoticia();
		
		return SKIP_BODY;
	}
	
	protected void saveNoticia() {
		previousListaNoticia = (TfsListaNoticias) pageContext.getRequest()
				.getAttribute("newslist");
		previousNoticia = (TfsNoticia) pageContext.getRequest().getAttribute(
				"news");

		pageContext.getRequest().setAttribute("newslist", null);
		pageContext.getRequest().setAttribute("news", null);

	}

	protected void restoreNoticia() {
		pageContext.getRequest().setAttribute("newslist", previousListaNoticia);
		pageContext.getRequest().setAttribute("news", previousNoticia);
	}

	@Override
	public String getCollectionPathName() {
		return "";
	}
	
	@Override
	public int getCollectionIndexSize(String name, boolean isCollectionPart) throws JspTagException {
		
		if (name.equals(""))
			return super.getCollectionIndexSize(name,isCollectionPart);
		
		return getXmlDocument().getValues(name, m_locale).size();
	}

	@Override
	public String getCollectionIndexValue(String name, int index) {
		try {
    	    // get the current users OpenCms context
    	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

			return getXmlDocument().getStringValue(cms, name, m_locale, index);
		} catch (CmsXmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	public String getCollectionValue(String name) throws JspTagException {
		try {
    	    // get the current users OpenCms context
    	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

    	    return getXmlDocument().getStringValue(cms, name, m_locale);
		} catch (CmsXmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}


}
