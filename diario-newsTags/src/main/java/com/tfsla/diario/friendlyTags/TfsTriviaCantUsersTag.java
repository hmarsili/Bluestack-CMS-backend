package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import org.opencms.flex.CmsFlexController;

import com.tfsla.diario.ediciones.services.TriviasService;

public class TfsTriviaCantUsersTag extends  A_TfsTriviaValueTag {

	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	 public int doStartTag() throws JspException {

		I_TfsTrivia trivia = getCurrentTrivia();
		String triviaPath = CmsFlexController.getCmsObject(pageContext.getRequest()).getRequestContext().removeSiteRoot(trivia.getXmlDocument().getFile().getRootPath());  
		
		TriviasService service = new TriviasService();
		int cantUseres = service.getCantUsers(triviaPath);
		
		printContent(String.valueOf(cantUseres));

		return SKIP_BODY;
	 }
}

