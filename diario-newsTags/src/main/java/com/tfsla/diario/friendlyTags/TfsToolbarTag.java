package com.tfsla.diario.friendlyTags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;

import com.tfsla.diario.toolbar.Menu;
import com.tfsla.diario.toolbar.PrincipalButton;
import com.tfsla.diario.toolbar.ToolbarUtils;
import com.tfsla.templateManager.service.TemplateToolbarProcessor;

public class TfsToolbarTag extends TagSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private static final Log LOG = CmsLog.getLog(TfsToolbarTag.class);

	protected String buttons; 

	public String getButtons() {
		return buttons;
	}

	public void setButtons(String buttons) {
		this.buttons = buttons;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
    public int doStartTag() throws JspException {

		 CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	
		 CmsRequestContext reqContext = cms.getRequestContext();
		 String projectName = reqContext.currentProject().getName();
		 
		 if (projectName.equals("Offline")) {
			Menu menu = new Menu();
			HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
			HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();

			String[] buttonsArray = buttons.split(",");
			for (String option : buttonsArray) {
				PrincipalButton button = null;
				try {
					button = (PrincipalButton) ToolbarUtils.getToolbarButtonClass(option).newInstance();
				} catch (InstantiationException e) {
					LOG.error(e);
				} catch (IllegalAccessException e) {
					LOG.error(e);
				} 
				//Llama al metodo para armar la estructura
				button.getButtonStructure(request, response, pageContext);
				menu.addButton(button);
			}
			TemplateToolbarProcessor template = new TemplateToolbarProcessor(pageContext, request, response,  projectName);
			template.printHTML(menu);
		 }	
		return SKIP_BODY;
	}

}
