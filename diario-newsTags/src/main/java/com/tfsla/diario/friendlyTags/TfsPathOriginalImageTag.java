package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.TfsJspTagLink;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUriSplitter;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsPathOriginalImageTag  extends A_TfsNoticiaCollectionValue {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1194767256534248964L;

	@Override
	 public int doStartTag() throws JspException {
		 CmsFlexController controller = CmsFlexController.getController(pageContext.getRequest());
		 CmsObject cms = controller.getCmsObject();

		 //A_TfsNoticiaCollection collection = getCurrentCollectionNews();
		 //String content = collection.getIndexElementValue(collection.getCurrentNews(), "imagen");

		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.image.image"));
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 
		 String content = collection.getCollectionValue(getCollectionPathName()); //imagen
		 
		 CmsUriSplitter splitSrc = new CmsUriSplitter(content);
		 try {
			 
			 String imageLink = "";
			 CmsResource imageRes = cms.readResource(splitSrc.getPrefix());
			 
			 CmsProperty propOriginalImage = cms.readPropertyObject(imageRes, "originalImage", false);

			 if (propOriginalImage!=null && propOriginalImage.getValue()!=null && propOriginalImage.getValue().length()>0)
				 imageLink = cms.getRequestContext().removeSiteRoot(propOriginalImage.getValue());
			 else 
				 imageLink = cms.getSitePath(imageRes);
			 
			 printContent(TfsJspTagLink.linkTagAction(imageLink, this.pageContext.getRequest()));
		 } catch (CmsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		 return SKIP_BODY;
	 }
	
	
	@Override
	public int doEndTag() throws JspException {
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
            // need to release manually, JSP container may not call release as required (happens with Tomcat)
            release();
        }

		return super.doEndTag();
	}
}
