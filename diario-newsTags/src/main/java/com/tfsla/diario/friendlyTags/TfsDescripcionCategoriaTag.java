package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;

public class TfsDescripcionCategoriaTag extends A_TfsNoticiaCollectionValue {

	 /**
	 * 
	 */
	private static final long serialVersionUID = -8396496481029634700L;

	@Override
	 public int doStartTag() throws JspException {

		 CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

		 I_TfsCollectionListTag collection = getCurrentCollectionNews();
		
		 setKeyName("");
		 
		 //LOG.error("description categoria: " + getCollectionPathName());
		 
		 String category = collection.getCollectionValue(getCollectionPathName());
		 
		 if (category!=null && !category.trim().equals("")) {
			 CmsProperty property;
			try {
				property = cms.readPropertyObject(category, "Title", false);
				if (property!=null && property.getValue()!=null)
				 printContent(property.getValue());
			} catch (CmsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
		 return SKIP_BODY;
	 }

}
