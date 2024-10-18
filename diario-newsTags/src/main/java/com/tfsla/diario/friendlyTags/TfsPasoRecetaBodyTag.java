package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import org.opencms.flex.CmsFlexController;

import com.tfsla.diario.utils.TfsBodyFormatterHelper;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsPasoRecetaBodyTag  extends A_TfsNoticiaCollectionValue {

	 /**
	 * 
	 */
	private static final long serialVersionUID = -6306131189117954470L;

	private String format="";
	public String getFormat() {
		
		return format;
	}

	public void setFormat(String format) {
			this.format = format;
	}
	
	@Override
	 public int doStartTag() throws JspException {

		 I_TfsCollectionListTag collection = getCurrentCollectionNews();
			
		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.recipe.pasoReceta.descripcion"));
		 
		 String content = collection.getCollectionValue(getCollectionPathName());
		 /*if (format!=null) {
			 I_TfsNoticia noticia = getCurrentNews();
			 if (format.toLowerCase().trim().equals("fia"))  {
				 content = TfsBodyFormatterHelper.formatAsFacebookInstantArticles(content,noticia.getXmlDocument().getFile(),CmsFlexController.getCmsObject(pageContext.getRequest()),pageContext.getRequest(),pageContext);
			 }
			 if (format.toLowerCase().trim().equals("amp"))  {
		    	content = TfsBodyFormatterHelper.formatAsAMP(content,noticia.getXmlDocument().getFile(),CmsFlexController.getCmsObject(pageContext.getRequest()), pageContext.getRequest(), pageContext);
			 }
		 }*/
	     printContent(content);

		 return SKIP_BODY;
	 }

}

