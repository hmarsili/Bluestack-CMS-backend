package org.opencms.jsp;

import jakarta.servlet.jsp.PageContext;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;

import com.tfsla.utils.UrlLinkHelper;

public class ExternalURLNewsTag extends AbstractOpenCmsTag  {

	private CmsObject cms;
	
	private PageContext pageContext;

    public ExternalURLNewsTag() {
        super();
    }

    @Override
    public int doStartTag() {
    	
    	cms = CmsFlexController.getCmsObject(pageContext.getRequest());

        this.getWriter().print(this.getLink());
        return SKIP_BODY;
    }

    private String getLink() {
    	if (this.getAncestor()!=null && this.getAncestor().getXmlDocument()!=null) {
    		CmsFile file = this.getAncestor().getXmlDocument().getFile();
    		if (file!=null)
    			return UrlLinkHelper.getUrlFriendlyLink(file,cms,pageContext.getRequest());
    		else return "";
    	}
    	else
    		return "";
    }

	public void setPageContext(PageContext arg0) {
		super.setPageContext(arg0);
		this.pageContext = arg0;

	}

}
