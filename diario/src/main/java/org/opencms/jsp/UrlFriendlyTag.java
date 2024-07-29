package org.opencms.jsp;

import javax.servlet.jsp.PageContext;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;

import com.tfsla.utils.UrlLinkHelper;

public class UrlFriendlyTag extends AbstractOpenCmsTag  {

	private CmsObject cms;
	
	private PageContext pageContext;

    public UrlFriendlyTag() {
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
    		{
    			String external = UrlLinkHelper.getExternalLink(file, cms);
    			if (external!=null && !external.isEmpty())
    				return external;
    			
    			return UrlLinkHelper.getUrlFriendlyLink(file,cms,pageContext.getRequest());
    			//return "http://" + pageContext.getRequest().getServerName() + "/" + UrlLinkHelper.getUrlFriendlyLink(file,cms);    			
    			//return "http://" + pageContext.getRequest().getServerName() + ":" + pageContext.getRequest().getServerPort() + "/" + UrlLinkHelper.getUrlFriendlyLink(file,cms);
    		}
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
