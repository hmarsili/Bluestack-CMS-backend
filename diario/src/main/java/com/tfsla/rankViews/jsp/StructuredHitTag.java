package com.tfsla.rankViews.jsp;


import javax.servlet.jsp.PageContext;
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.AbstractOpenCmsTag;
import com.tfsla.rankViews.service.RankService;
import org.opencms.file.CmsFile;

public class StructuredHitTag extends AbstractOpenCmsTag {


	private CmsObject cms;
	
	private PageContext pageContext;

    public StructuredHitTag() {
        super();
    }

    @Override
    public int doStartTag() {
    	
    	cms = CmsFlexController.getCmsObject(pageContext.getRequest());

    	if (this.getAncestor()!=null && this.getAncestor().getXmlDocument()!=null) {
    		CmsFile file = this.getAncestor().getXmlDocument().getFile();
    		if (file!=null)
    		{

    			RankService rService = new RankService();
    			rService.countHitView(file, cms, pageContext.getSession());
    			
    			        
    		}
    	}
        return SKIP_BODY;
    }
    
	public void setPageContext(PageContext arg0) {
		super.setPageContext(arg0);
		this.pageContext = arg0;

	}


}
