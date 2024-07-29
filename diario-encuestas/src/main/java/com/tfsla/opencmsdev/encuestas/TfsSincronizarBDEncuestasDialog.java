package com.tfsla.opencmsdev.encuestas;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import com.tfsla.diario.ediciones.services.openCmsService;

public class TfsSincronizarBDEncuestasDialog extends CmsDialog {

    public TfsSincronizarBDEncuestasDialog(PageContext context,
            HttpServletRequest req, HttpServletResponse res) {
        super(new CmsJspActionElement(context, req, res));
    }

    public void sincronizarBD() throws JspException {

        // save initialized instance of this class in request attribute for
        // included sub-elements
        // getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
        switch (getAction()) {
        case ACTION_CONFIRMED:
            try {
                // re-initialize the workplace
                // OpenCms.getWorkplaceManager().initialize(getCms());
                // fire "clear caches" event to reload all cached resource
                // bundles
                // OpenCms.fireCmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES,
                // new HashMap());
            	
            	String currentSite = this.getJsp().getRequestContext().getSiteRoot();
            	
            	if(!currentSite.equals("")){
                   ModuloEncuestas.sincronizarBDyContenidos(this.getCms());
            	}
            	   actionCloseDialog();
            	   
            } catch (Throwable t) {
                // create a new Exception with custom message
                includeErrorpage(this, t);
            }
            break;
        }
    }

    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings,
            HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        //setParamDialogtype("sincronizarbd");
        // set the action for the JSP switch
        if (DIALOG_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_CONFIRMED);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            setAction(ACTION_DEFAULT);
            // add the title for the dialog
            setParamTitle(key(Messages.GUI_ENCUESTAS_SINCRONIZAR_0));
        }
    }
}