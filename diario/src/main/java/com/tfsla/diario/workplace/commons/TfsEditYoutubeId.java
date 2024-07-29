package com.tfsla.diario.workplace.commons;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;


public class TfsEditYoutubeId extends CmsDialog {

    /** The dialog type.<p> */
    public static final String DIALOG_TYPE = "newyoutubeid";

    /** Request parameter name for the link target.<p> */
    public static final String PARAM_YOUTUBEID = "youtubeid";

    /** Stores the value of the link target.<p> */
    private String m_paramYoutubeId;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public TfsEditYoutubeId(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public TfsEditYoutubeId(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Changes the link target of the pointer.<p>
     * 
     * @throws JspException if inclusion of error dialog fails
     */
    public void actionChangeIdTarget() throws JspException {

        try {
            // check the resource lock state
            checkLock(getParamResource());
            // change the link target
            CmsFile editFile = getCms().readFile(getParamResource());
            editFile.setContents(getParamYoutubeId().getBytes());
            getCms().writeFile(editFile);
            // close the dialog window
            actionCloseDialog();
        } catch (Throwable e) {
            // error changing link target, show error dialog
            setParamMessage(Messages.get().getBundle(getLocale()).key(Messages.ERR_CHANGE_YOUTUBE_ID_TARGET_0));
            includeErrorpage(this, e);
        }
    }

    public String getMessage(String msg)
    {
    	return Messages.get().getBundle(getLocale()).key(
                msg);
    }
    
    /**
     * Returns the old link target value of the pointer resource to edit.<p>
     * 
     * @return the old link target value
     * @throws JspException if problems including sub-elements occur 
     * 
     */
    public String getOldTargetValue() throws JspException {

        String linkTarget = "";
        if (CmsStringUtil.isEmpty(getParamYoutubeId())) {
            // this is the initial dialog call, get link target value
            try {
                // get pointer contents
                CmsFile file = getCms().readFile(getParamResource());
                linkTarget = new String(file.getContents());
            } catch (Throwable e1) {
                // error reading file, show error dialog
                setParamMessage(Messages.get().getBundle(getLocale()).key(
                    Messages.ERR_GET_YOUTUBE_ID_TARGET_1,
                    getParamResource()));
                includeErrorpage(this, e1);

            }
        }
        return CmsEncoder.escapeXml(linkTarget);
    }

    /**
     * Returns the link target request parameter value.<p>
     * 
     * @return the link target request parameter value
     */
    public String getParamYoutubeId() {

        return m_paramYoutubeId;
    }

    /**
     * Sets the link target request parameter value.<p>
     * 
     * @param linkTarget the link target request parameter value
     */
    public void setParamYoutubeId(String youtubeid) {

    	m_paramYoutubeId = youtubeid;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);

        // check the required permissions to edit the pointer    
        if (!checkResourcePermissions(CmsPermissionSet.ACCESS_WRITE, false)) {
            // no write permissions for the resource, set cancel action to close dialog
            setParamAction(DIALOG_CANCEL);
        }

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch 
        if (DIALOG_OK.equals(getParamAction())) {
            // ok button pressed, change link target
            setAction(ACTION_OK);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            // cancel button pressed
            setAction(ACTION_CANCEL);
        } else {
            // first call of dialog
            setAction(ACTION_DEFAULT);
            // build title for change link target dialog     
            setParamTitle(
            		Messages.get().getBundle(getLocale()).key(
                            Messages.GUI_CHYT_ID_1,
                            new Object[] {CmsResource.getName(getParamResource())})
            		//key(Messages.GUI_CHYT_ID_1, new Object[] {CmsResource.getName(getParamResource())})
            		);
        }
    }
    
    public String dialogButtonPreview()
    {
    	StringBuffer result = new StringBuffer();
    	
    	result.append("<input name=\"preview\" type=\"button\" value=\"");
        result.append(Messages.get().getBundle(getLocale()).key(
                Messages.GUI_PREVIEW_YOUTUBE_ID_0) + "\"");
        result.append(" class=\"dialogbutton\"");
        result.append(" onclick=\"imagesPreviewYoutube();\"");
        result.append(">\n");
        
    	return result.toString();
    }

}
