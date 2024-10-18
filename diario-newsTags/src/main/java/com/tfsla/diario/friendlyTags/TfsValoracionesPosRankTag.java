package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;
import org.opencms.flex.CmsFlexController;

public class TfsValoracionesPosRankTag extends A_TfsNoticiaValue {

	private static long idValoration = 1;
	
	public boolean allowvaloration = false;
	
    @Override
    public int doStartTag() throws JspException {

        
    	I_TfsNoticia noticia = getCurrentNews();
        
	    cms = CmsFlexController.getCmsObject(pageContext.getRequest());

        String newsPath = cms.getSitePath(noticia.getXmlDocument().getFile());
    	long id = getNextValorationId();

        String content = "<div style=\"display:inline\" id=\"pos_" + id + "\" " + (allowvaloration ? "onclick=\"makeVote('pos_" + id + "','" + newsPath+ "','1');\"" : "") + " type=\"ranking\" mode=\"positive-valorations\" path=\"" + newsPath + "\">0</div>";
        printContent(content);

        return SKIP_BODY;
    }

    protected static long getNextValorationId()
    {
    	long nextID = idValoration;
    	
    	if (idValoration== Long.MAX_VALUE)
    		idValoration=1;
    	else
    		idValoration++;
    	
    	return nextID;
    }

	public boolean isAllowvaloration() {
		return allowvaloration;
	}

	public void setAllowvaloration(boolean allowvaloration) {
		this.allowvaloration = allowvaloration;
	}

}
