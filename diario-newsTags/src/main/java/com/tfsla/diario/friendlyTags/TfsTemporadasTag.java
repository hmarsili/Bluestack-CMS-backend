package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.OpenCms;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsTemporadasTag extends A_TfsNoticiaCollection {

    protected transient CmsObject m_cms;
    protected CmsFlexController m_controller;

	@Override
	public int doStartTag() throws JspException {

		keyName = "";
				//TfsXmlContentNameProvider.getInstance().getTagName("news.vod.seasonpath"); //path

		init(TfsXmlContentNameProvider.getInstance().getTagName("news.vod.season")); //season

		return (hasMoreContent() ? EVAL_BODY_INCLUDE : SKIP_BODY );
	}

	
	/*@Override
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
					exposeVod(noticia, controlValue);
				
				withElement=true;
				
			}else{
				index++;
				restoreVod();
			}
		}
		
		return (index<=lastElement);
	}*/
	
   protected void exposeVod(I_TfsNoticia noticia, String path) {   	
		
		pageContext.getRequest().setAttribute("temporada", path);
    	
   }
   
   protected void restoreVod() {   	
		
		pageContext.getRequest().setAttribute("temporada", null);
   	
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


}

