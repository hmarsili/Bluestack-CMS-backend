package com.tfsla.workplace.tools.license;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.opencms.configuration.CmsMediosInit;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;

public class TfsLicenseOverviewDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "license.stats";
    
    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};
    
    private String views;
    private String violations;

    

	public String getViews() {
		return views;
	}

	public void setViews(String views) {
		this.views = views;
	}

	public String getViolations() {
		return violations;
	}

	public void setViolations(String violations) {
		this.violations = violations;
	}

    public TfsLicenseOverviewDialog(CmsJspActionElement jsp) {

        super(jsp);

    }

    public TfsLicenseOverviewDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }
    
	@Override
	public void actionCommit() throws IOException, ServletException {
		setCommitErrors(new ArrayList());
	}

	   @Override
	    protected String createDialogHtml(String dialog) {

	        StringBuffer result = new StringBuffer(1024);

	        // create widget table
	        result.append(createWidgetTableStart());

	        // show error header once if there were validation errors
	        result.append(createWidgetErrorHeader());

	        if (dialog.equals(PAGES[0])) {
	            // create the widgets for the first dialog page
	            result.append(dialogBlockStart(key(Messages.GUI_LICENSE_LABEL_STATUS_BLOCK_0)));
	            result.append(createWidgetTableStart());
	            result.append(createDialogRowsHtml(0, 1));
	            result.append(createWidgetTableEnd());
	            result.append(dialogBlockEnd());
	        }

	        // close widget table
	        result.append(createWidgetTableEnd());

	        return result.toString();
	    }

	   
	@Override
	protected void defineWidgets() {
        // initialize the cache object to use for the dialog
        initLicenseObject();

        setKeyPrefix(KEY_PREFIX);

        // widgets to display
        addWidget(new CmsWidgetDialogParameter(this, "views", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "violations", PAGES[0], new CmsDisplayWidget()));
		
	}
	
	protected void initLicenseObject() {
		setViolations("" + CmsMediosInit.getInstance().licViolations(this.getCms()));
		setViews("" + CmsMediosInit.getInstance().getViews(this.getCms()));		
    }

	@Override
	protected String[] getPageArray() {
        return PAGES;
	}
	
    @Override
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

}
