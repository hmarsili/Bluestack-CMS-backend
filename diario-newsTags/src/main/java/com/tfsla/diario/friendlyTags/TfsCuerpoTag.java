package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;

/*
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
*/

import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;

import com.tfsla.diario.utils.TfsBodyFormatterHelper;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;
//import com.tfsla.utils.UrlLinkHelper;

public class TfsCuerpoTag extends A_TfsNoticiaValue {

	protected static final Log LOG = CmsLog.getLog(TfsCuerpoTag.class);
	private String format="";
	public String getFormat() {
		
		return format;
	}

	public void setFormat(String format) {
			this.format = format;
	}
	
    @Override
    public int doStartTag() throws JspException {

        I_TfsNoticia noticia = getCurrentNews();
        
        
        String content = getElementValue(noticia,TfsXmlContentNameProvider.getInstance().getTagName("news.body")); //cuerpo
        LOG.debug("TfsCuerpoTag -->");
        LOG.debug(content);
        
	    if (format!=null && format.toLowerCase().trim().equals("fia"))  {
	    	content = TfsBodyFormatterHelper.formatAsFacebookInstantArticles(content,noticia.getXmlDocument().getFile(),CmsFlexController.getCmsObject(pageContext.getRequest()),pageContext.getRequest(),pageContext);
	    }else if (format!=null && format.toLowerCase().trim().equals("amp"))  {
	    	content = TfsBodyFormatterHelper.formatAsAMP(content,noticia.getXmlDocument().getFile(),CmsFlexController.getCmsObject(pageContext.getRequest()), pageContext.getRequest(), pageContext);	
	    }else{
	    	content = TfsBodyFormatterHelper.formatWidthControl(content,noticia.getXmlDocument().getFile(),CmsFlexController.getCmsObject(pageContext.getRequest()), pageContext.getRequest(), pageContext);	
	    }
	    
	    LOG.debug("TfsCuerpoTag (DESPUES)-->");
        LOG.debug(content);
        
	    printContent(content);

        return SKIP_BODY;
    }

}
