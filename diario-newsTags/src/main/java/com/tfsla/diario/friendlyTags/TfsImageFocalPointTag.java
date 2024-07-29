package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUriSplitter;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsImageFocalPointTag extends A_TfsNoticiaCollectionValue {

	/**
	 * 
	 */
	private static final long serialVersionUID = 725407952091036013L;

	@Override
	 public int doStartTag() throws JspException {

		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.image.focalPoint"));
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 String content = collection.getCollectionValue(getCollectionPathName()); //

		 if (content != null)
			 printContent(content);
		 else {
			 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.image.image"));
			 content = collection.getCollectionValue(getCollectionPathName()); //imagen
			 
			 
			 CmsFlexController controller = CmsFlexController.getController(pageContext.getRequest());
			 CmsObject cms = controller.getCmsObject();

			 CmsUriSplitter splitSrc = new CmsUriSplitter(content);
			 CmsProperty prop;
			 try {
				prop = cms.readPropertyObject(splitSrc.getPrefix(), "image.focalPoint", false);
				if (prop!=null)
					printContent(prop.getValue()!=null? prop.getValue():"");
				else
					printContent("");
			} catch (CmsException e) {
					e.printStackTrace();
					printContent("");
			}
		 } 
		 return SKIP_BODY;
	 }
}
