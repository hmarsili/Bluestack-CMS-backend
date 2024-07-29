package com.tfsla.diario.friendlyTags;
import javax.servlet.jsp.JspException;

import com.tfsla.diario.model.TfsRecipeInstruction;

public class TfsPasoRecetaPosicionTag  extends A_TfsNoticiaCollectionValue {

	 /**
	 * 
	 */
	private static final long serialVersionUID = -6306131189117954470L;

	
	@Override
	 public int doStartTag() throws JspException {

		 I_TfsCollectionListTag collection = getCurrentCollectionNews();
		
		 TfsRecipeInstruction instruccion = (TfsRecipeInstruction)pageContext.getRequest().getAttribute("recipeInstructionList");
		 
		 String content ="";
		 if (instruccion!= null)
			content = String.valueOf(instruccion.getPosition()); 
		 
	     printContent(content);

		 return SKIP_BODY;
	 }

}

