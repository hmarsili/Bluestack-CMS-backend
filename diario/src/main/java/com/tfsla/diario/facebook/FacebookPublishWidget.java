package com.tfsla.diario.facebook;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

public class FacebookPublishWidget extends CmsWidgetDialog {

	FacebookAccountPublisher accountPublisher;

	private static final String[] PAGES = { "page1" };

	private static final String KEY_PREFIX = "accountPublisher";

	public FacebookPublishWidget(PageContext context, HttpServletRequest req, HttpServletResponse res) {
		super(context, req, res);
	}


	@Override
	public void actionCommit() throws IOException, ServletException {
		FacebookService.getInstance().addPublishers(this.getCms(), accountPublisher);
	}

	@Override
	protected void defineWidgets() {
		setKeyPrefix(KEY_PREFIX);

		initAccountConsumer();
		
		addWidget(newInput("name","Nombre"));

	}

	private CmsWidgetDialogParameter newInput(String propertyName, String title) {
		return new CmsWidgetDialogParameter(this.accountPublisher, propertyName,title, PAGES[0], new CmsInputWidget());
	}

	@Override
	protected String[] getPageArray() {
		return PAGES;
	}
	
	@Override
	protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

		// initialize parameters and dialog actions in super implementation
		super.initWorkplaceRequestValues(settings, request);

		// save the current state of the seccion (may be changed because of the
		// widget values)
		this.setDialogObject(this.accountPublisher);

	}
	
	private void initAccountConsumer() {
		if (this.isInitialCall()) {
			accountPublisher = new FacebookAccountPublisher();			
		}
		else {
			// this is not the initial call, get the project object from
			// session
			accountPublisher = (FacebookAccountPublisher) this.getDialogObject();
		}

	}

	private boolean isInitialCall() {
		return CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction());
	}


	@Override
	protected String defaultActionHtml() throws JspException {
		String content = super.defaultActionHtml();
		
		int idx = content.indexOf("function submitAction");

		String fullpath = OpenCms.getLinkManager().substituteLinkForUnknownTarget(
	            this.getCms(),
	            "/system/modules/com.tfsla.opencmsdev/elements/facebookLogin.jsp");
		
		String filterHTML ="function facebookLogin()\n";
		filterHTML+="{\n";
		filterHTML+="	openPopUpProvider(document.getElementById(\"Nombre.0\").value, '');\n";
		filterHTML+="}\n";
		
		
		filterHTML+="function openPopUpProvider(name)\n";
		filterHTML+="{\n";
		filterHTML+="	var winOpenId = window.open('" + fullpath + "?N=' + name , 'mywindow', 'height=500,width=600,status=no,toolbar=no');\n";
		filterHTML+="	winOpenId.focus();\n";
		filterHTML+="}\n";

		content = content.substring(0,idx) + filterHTML + content.substring(idx);

		idx = content.indexOf("loadingOn();",idx);
		content = content.substring(0,idx) + "facebookLogin();\n" + content.substring(idx);

		return content;
		
	}


}
