package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspException;


/*
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.opencms.file.CmsObject;
*/
import org.opencms.flex.CmsFlexController;
import org.opencms.main.OpenCms;

import com.tfsla.diario.utils.TfsBodyFormatterHelper;
import com.tfsla.diario.utils.TfsBodySplitterFormatHelper;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;
//import com.tfsla.utils.UrlLinkHelper;


//body-splitter
public class TfsCuerpoSeparadoTag extends A_TfsNoticiaSplitElement  {
	
	private String format="";
	
	public String getFormat() {
		
		return format;
	}

	public void setFormat(String format) {
			this.format = format;
	}
	
	public int doStartTag() throws JspException {
		
		items = null;
	    idx=-1;
	    
		I_TfsNoticia noticia = getCurrentNews();
	        
	    String content = getElementValue(noticia,TfsXmlContentNameProvider.getInstance().getTagName("news.body")); //cuerpo
	    
	    if (content.trim().equals(""))
	    	return SKIP_BODY;
	    
	    LOG.debug("TfsCuerpoSeparadoTag -->");
        LOG.debug(content);
        
	    if (format!=null && format.toLowerCase().trim().equals("fia"))  {
	    	content = TfsBodyFormatterHelper.formatAsFacebookInstantArticles(content,noticia.getXmlDocument().getFile(),CmsFlexController.getCmsObject(pageContext.getRequest()),pageContext.getRequest(),pageContext);
        }else if (format!=null && format.toLowerCase().trim().equals("amp"))  {
	    	content = TfsBodyFormatterHelper.formatAsAMP(content,noticia.getXmlDocument().getFile(),CmsFlexController.getCmsObject(pageContext.getRequest()), pageContext.getRequest(), pageContext);
        }else{
	    	content = TfsBodyFormatterHelper.formatWidthControl(content,noticia.getXmlDocument().getFile(),CmsFlexController.getCmsObject(pageContext.getRequest()), pageContext.getRequest(), pageContext);	
	    }
        
	    LOG.debug("TfsCuerpoSeparadoTag (DESPUES)-->");
        LOG.debug(content);
	    
	    //String[] body = content.split("</p>");
        //enclosingTag(body);
	    //items= body;
	    items = TfsBodySplitterFormatHelper.getBodyFormated(content);
	    
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
	
	protected void printContent(String content) throws JspException {
		try {
	            pageContext.getOut().print(content);
	    } catch (IOException e) {
	        if (LOG.isErrorEnabled()) {
	            LOG.error("Error trying to retrieve Title", e);
	        }
	        throw new JspException(e);
	    }
	}
	
	private void enclosingTag(String[] body) {
		for(int i=0; body.length -1 >= i;i++){        	
        		body[i] = body[i] + "</p>";        	       	 
        }
	}


}
