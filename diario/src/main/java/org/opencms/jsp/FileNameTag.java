package org.opencms.jsp;

import javax.servlet.jsp.PageContext;

import org.opencms.file.CmsFile;
import org.opencms.flex.CmsFlexController;
import org.opencms.file.CmsObject;

import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.utils.UrlLinkHelper;

public class FileNameTag extends AbstractOpenCmsTag {

	private CmsObject cms;

	private PageContext pageContext;

    public FileNameTag() {
        super();
    }

    @Override
    public int doStartTag() {
    	
    	cms = CmsFlexController.getCmsObject(pageContext.getRequest());

        this.getWriter().print(this.getFileName());
        return SKIP_BODY;
    }

    private String getFileName() {
        CmsFile file = this.getAncestor().getXmlDocument().getFile();
        
		String external = UrlLinkHelper.getExternalLink(file, cms);
		if (external!=null && !external.isEmpty())
			return external;

		return CmsJspTagLink.linkTagAction("/" + CmsResourceUtils.getLink(file),pageContext.getRequest());
        //return "http://" + pageContext.getRequest().getServerName() + "/" + CmsResourceUtils.getLink(file);
        //return "http://" + pageContext.getRequest().getServerName() + ":" + pageContext.getRequest().getServerPort() + "/" + CmsResourceUtils.getLink(file);
    }
    
	public void setPageContext(PageContext arg0) {
		super.setPageContext(arg0);
		this.pageContext = arg0;

	}

}
