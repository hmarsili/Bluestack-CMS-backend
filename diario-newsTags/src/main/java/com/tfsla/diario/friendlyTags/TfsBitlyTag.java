package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;

public class TfsBitlyTag  extends A_TfsNoticiaValue {
	
	@Override
    public int doStartTag() throws JspException {
	
		CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

        I_TfsNoticia noticia = getCurrentNews();
        
		try {
			CmsProperty bitlyUrl = cms.readPropertyObject(noticia.getXmlDocument().getFile(), "bitlyUrl", false);
			if (bitlyUrl==null || bitlyUrl.getValue()==null || bitlyUrl.getValue().equals(""))
				printContent("");
			else
				printContent(bitlyUrl.getValue());
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return SKIP_BODY;
	}
}
