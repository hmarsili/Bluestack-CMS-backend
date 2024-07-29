package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.OpenCms;

import com.tfsla.utils.UrlLinkHelper;

public class TfsItemListFriendlyLinkTag extends A_TfsNoticiaValue {

	private String relative = "true";
	private String item = "current";
	
	public TfsItemListFriendlyLinkTag() {
		relative = "true";
		item="current";
	}
	
	@Override
    public int doStartTag() throws JspException {

	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

        I_TfsNoticia noticia = getCurrentNews();
        
        int nroItem = getItemNum();
        
        String content = UrlLinkHelper.getItemNewsUrlFriendlyLink(noticia.getXmlDocument().getFile(), nroItem, cms, this.pageContext.getRequest(),Boolean.parseBoolean(relative));
        
        printContent(content);

        relative = "true";
        item="current";
        
        return SKIP_BODY;
    }
	
	private int getItemNum() {
		String itemList = pageContext.getRequest().getParameter("itemNumber");
		int current = 1;
		try {
			if (itemList!=null)
				current = Integer.parseInt(itemList);
		}
		catch (NumberFormatException ex) {}
		
		if (item.equals("current"))
			return current;

		if (item.trim().toLowerCase().equals("next"))
				return current+1;

		if (item.trim().toLowerCase().equals("previous"))
			return current-1;
		
		
		try {
			return Integer.parseInt(item);
		}
		catch (NumberFormatException ex) {}
			
		return 1;
	}
	
    public String getRelative() {
		return relative;
	}

	public void setRelative(String relative) {
		if (relative!=null)
			this.relative = relative;
	}

	public void setItem(String item) {
		if (item!=null)
			this.item = item;
	}

	@Override
    public int doEndTag() {

        if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
            // need to release manually, JSP container may not call release as required (happens with Tomcat)
            release();
        }
        
        return EVAL_PAGE;
    }

}
