package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import org.opencms.flex.CmsFlexController;

import com.tfsla.diario.comentarios.services.CommentsModule;

public class TfsComentariosRankTag extends A_TfsNoticiaValue {

	
    @Override
    public int doStartTag() throws JspException {

        I_TfsNoticia noticia = getCurrentNews();
        
	    cms = CmsFlexController.getCmsObject(pageContext.getRequest());

        String newsPath = cms.getSitePath(noticia.getXmlDocument().getFile());
        
        String masterNews = newsPath;
		String m_news = CommentsModule.getInstance(cms).getMasterNews(cms,newsPath);
		
		if(m_news!=null && m_news != "" ) masterNews = m_news;

        String content = "<div style=\"display:inline\" type=\"ranking\" mode=\"comments\" path=\"" + masterNews + "\">0</div>";
        printContent(content);

        return SKIP_BODY;
    }


}
