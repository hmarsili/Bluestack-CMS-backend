package com.tfsla.diario.twitter;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

public class TwitterAccountWidget extends CmsWidgetDialog {

	TwitterAccountConsumer accountConsumer;

	private static final String[] PAGES = { "page1" };

	private static final String KEY_PREFIX = "accountConsumer";

	public TwitterAccountWidget(PageContext context, HttpServletRequest req, HttpServletResponse res) {
		super(context, req, res);
	}


	@Override
	public void actionCommit() throws IOException, ServletException {
		TwitterService.getInstance().setAccountConsumer(this.getCms(), accountConsumer);
	}

	@Override
	protected void defineWidgets() {
		setKeyPrefix(KEY_PREFIX);

		initAccountConsumer();
		
		addWidget(newInput("key","Account Key"));
		addWidget(newInput("secret","Account Secret"));

	}

	private CmsWidgetDialogParameter newInput(String propertyName, String title) {
		return new CmsWidgetDialogParameter(this.accountConsumer, propertyName,title, PAGES[0], new CmsInputWidget());
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
		this.setDialogObject(this.accountConsumer);

	}
	
	private void initAccountConsumer() {
		if (this.isInitialCall()) {
			accountConsumer = TwitterService.getInstance().getAccountConsumer(this.getCms());			
		}
		else {
			// this is not the initial call, get the project object from
			// session
			accountConsumer = (TwitterAccountConsumer) this.getDialogObject();
		}

	}

	private boolean isInitialCall() {
		return CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction());
	}


}
