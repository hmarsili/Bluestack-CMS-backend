package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.diario.model.TfsReceta;
import com.tfsla.diario.model.TfsRecipeInstruction;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsPasoRecetaTag extends  A_TfsNoticiaCollectionWithBlanks {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int index=1;
	TfsReceta receta=null;
	
	@Override
    public int doStartTag() throws JspException {

		keyControlName = TfsXmlContentNameProvider.getInstance().getTagName("news.recipe.pasoReceta.titulo"); //"titulo";

		init(TfsXmlContentNameProvider.getInstance().getTagName("news.recipe.pasoReceta")); //pasoReceta
		
		index=1;
		receta =  (TfsReceta)pageContext.getRequest().getAttribute("recipe" );
		
		exposeInstruction ();
		
		return (hasMoreContent() ? EVAL_BODY_INCLUDE : SKIP_BODY );
		
    }
	
	public void exposeInstruction () {
		if (receta !=null) {
			TfsRecipeInstruction instruction = new TfsRecipeInstruction(receta.getRecipeInstructionsCount(), index);
			pageContext.getRequest().setAttribute("recipeInstructionList", instruction );
			index++;
		} else {
			CmsLog.getLog(this).debug("no se puede obtener la cantidad de instrucciones");
		}
	}
	
	@Override
	public int doAfterBody() throws JspException {
		
		
		if (hasMoreContent()) {
			exposeInstruction();
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
