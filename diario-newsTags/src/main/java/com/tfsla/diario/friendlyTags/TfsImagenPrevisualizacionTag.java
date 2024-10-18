package com.tfsla.diario.friendlyTags;

import java.util.ArrayList;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;

import org.opencms.main.OpenCms;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsImagenPrevisualizacionTag  extends A_TfsNoticiaCollectionWithBlanks  {
	
	private String viewportConfiguration = null;

	public String getViewportConfiguration() {
		return viewportConfiguration;
	}

	public void setViewportConfiguration(String viewportConfiguration) {
		this.viewportConfiguration = viewportConfiguration;
	}

	@Override
	public int doStartTag() throws JspException {

		keyControlName = TfsXmlContentNameProvider.getInstance().getTagName("news.image.image"); //"imagen";

		String key = "";
		
		if (viewportConfiguration!=null && viewportConfiguration.trim().length()>0) {
			//Si lo encontre ahora la clave es imagen personalizada en el indice correspondiente.
			
			key = TfsXmlContentNameProvider.getInstance().getTagName("news.customimage"); //imagenPersonalizada
			
			init(key);
			
			try {
				I_TfsNoticia noticia = getCurrentNews();
				int idx = getIndexValueInCollection(noticia,TfsXmlContentNameProvider.getInstance().getTagName("news.image.viewport"),viewportConfiguration);
				if (idx>0)
				{
					index=0;
					selectedItems = new ArrayList<Integer>();
					selectedItems.add(idx);
					lastElement=1;
					//this.setItem("" + idx);
					//init(key);
				}
				else 
					initNoViewportSelected();
				
				//LOG.debug("TfsImagenPrevisualizacionTag -> news: " + noticia.getXmlDocument().getFile().getRootPath() + " - idx: " + idx);
			} catch (JspTagException e) {
				initNoViewportSelected();
			}
			
		}
		else
			initNoViewportSelected();

		return (hasMoreContent() ? EVAL_BODY_INCLUDE : SKIP_BODY );
	}

	public void initNoViewportSelected() throws JspTagException {
		String key = TfsXmlContentNameProvider.getInstance().getTagName("news.imagepreview"); //imagenPrevisualizacion
		keyControlName = TfsXmlContentNameProvider.getInstance().getTagName("news.image.image"); //"imagen";
		init(key);
		
	}
	@Override
	public int doAfterBody() throws JspException {

		if (hasMoreContent()) {
			return EVAL_BODY_AGAIN;
		}
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {

		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return EVAL_PAGE;
	}

}
