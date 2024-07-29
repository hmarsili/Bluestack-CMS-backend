package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import org.opencms.flex.CmsFlexController;

public class TfsValoracionesNegRankTag extends A_TfsNoticiaValue {

	private static long idValoration = 1;
	
	public boolean allowvaloration = false;

    @Override
    public int doStartTag() throws JspException {

        I_TfsNoticia noticia = getCurrentNews();
        
	    cms = CmsFlexController.getCmsObject(pageContext.getRequest());

        String newsPath = cms.getSitePath(noticia.getXmlDocument().getFile());
    	long id = getNextValorationId();

        String content = "<div style=\"display:inline\" id=\"neg_" + id + "\" " + (allowvaloration ? "onclick=\"makeVote('neg_" + id + "','" + newsPath+ "','0');\"" : "") + " type=\"ranking\" mode=\"negative-valorations\" path=\"" + newsPath + "\">0</div>";
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
