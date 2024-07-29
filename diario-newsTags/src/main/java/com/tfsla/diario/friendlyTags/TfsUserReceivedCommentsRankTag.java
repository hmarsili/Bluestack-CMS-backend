package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspException;


public class TfsUserReceivedCommentsRankTag extends A_TfsUsuarioValueTag {	

	private static final long serialVersionUID = -9106136178780408143L;

	@Override
    public int doStartTag() throws JspException {

        I_TfsUser user = getCurrentUser();

	  // CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

      //  String newsPath = cms.getSitePath(noticia.getXmlDocument().getFile());

        String content = "<div style=\"display:inline\" type=\"rankinguser\" mode=\"receivedcomments\" user=\"" + user.getUser().getId() + "\">0</div>";
        try {
			pageContext.getOut().print(content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       // printContent(content);

        return SKIP_BODY;
    }

}
