package com.tfsla.diario.ediciones.widgets;


import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.widgets.A_CmsWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.tfsla.diario.ediciones.model.Edicion;
import com.tfsla.diario.ediciones.services.EdicionService;

public class NoticiaImpresaSelectorWidget  extends  A_CmsWidget {


    public NoticiaImpresaSelectorWidget() {
        this("");
    }

    public NoticiaImpresaSelectorWidget(String configuration) {
        super(configuration);
    }

    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {
        StringBuffer result = new StringBuffer(16);
        return result.toString();
    }

    public String getDialogInitCall(CmsObject cms, I_CmsWidgetDialog widgetDialog) {
        return "initNewsSelector();\n";
    }

    public String getDialogInitMethod(CmsObject cms, I_CmsWidgetDialog widgetDialog) {
        StringBuffer result = new StringBuffer(16);
        result.append("function initNewsSelector() {\n");
        result.append("	initResources(\"");
        result.append(OpenCms.getWorkplaceManager().getEncoding());
        result.append("\", \"");
        result.append(CmsWorkplace.VFS_PATH_MODULES);
        result.append("\", \"");
        result.append(CmsWorkplace.getSkinUri());
        result.append("\", \"");
        result.append(OpenCms.getSystemInfo().getOpenCmsContext());
        result.append("\");\n");
        result.append("}\n");

        result.append("var vr = null;\n");


        result.append("function resourceObject(encoding, contextPath, workplacePath, skinPath) {\n");
        result.append("	this.encoding = encoding;\n");
        result.append("	this.actDirId;\n");
        result.append("	this.contextPath = contextPath;\n");
        result.append("	this.workplacePath = workplacePath;\n");
        result.append("	this.skinPath = skinPath;\n");
        result.append("	this.resource = new Array();\n");
        result.append("	this.scrollTopType = 0;\n");
        result.append("	this.scrollTop = 0;\n");
        result.append("	this.scrollLeft = 0;\n");
        result.append("}\n");

 		result.append("function initResources(encoding, workplacePath, skinPath, contextPath) {\n");
		result.append("	vr = new resourceObject(encoding, contextPath, workplacePath, skinPath);\n");
		result.append("}\n");

        result.append("var fileswin = null;\n");
        result.append("var filesForm = null;\n");
        result.append("var filesField = null;\n");
        result.append("var filesDoc = null;\n");

        result.append("function openSelectNews(formName, fieldName, curDoc, tipoEdicion, nroEdicion, seccion) {\n");
        result.append("	var paramString = \"?fieldName=\" + fieldName;\n");

        result.append("		paramString += \"&tipoEdicion=\";\n");
        result.append("		paramString += tipoEdicion;\n");
        result.append("		paramString += \"&nroEdicion=\";\n");
        result.append("		paramString += nroEdicion;\n");
        result.append("		paramString += \"&seccion=\";\n");
        result.append("		paramString += seccion;\n");

        result.append(" fileswin = openWin(vr.contextPath + vr.workplacePath + \"com.tfsla.diario.ediciones/templates/selectNews.jsp\"  + paramString, \"opencms\", 700, 250);\n");
        result.append("	filesForm = formName;\n");
        result.append("	filesField = fieldName;\n");
        result.append("	filesDoc = curDoc;\n");
        result.append("}\n");

        result.append("function openWin(url, name, w, h) {\n");
        result.append("	var newwin = window.open(url, name, 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=150,left=660,width='+w+',height='+h);\n");
        result.append("	if(newwin != null) {\n");
        result.append("		if (newwin.opener == null) {\n");
        result.append("			newwin.opener = self;\n");
        result.append("		}\n");
        result.append("	}\n");
        result.append("	newwin.focus();\n");
        result.append("	return newwin;\n");
        result.append("}\n");

        return result.toString();
    }

    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {
    	Edicion edicion=null;
    	try {
    		edicion = getEdicion(cms,param);
    	} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

    	String resourcePath =((I_CmsXmlContentValue)param).getDocument().getFile().getRootPath();

    	resourcePath = cms.getRequestContext().removeSiteRoot(resourcePath);
    	resourcePath = resourcePath.replaceAll("~", "");

    	String SeccionPage = resourcePath.substring(resourcePath.lastIndexOf("/") +1).replace(".html","").replace(".xml","");


    	String id = param.getId();
        StringBuffer result = new StringBuffer(128);
        result.append("<td class=\"xmlTd\">");
        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"maxwidth\"><tr><td style=\"width: 100%;\">");
        result.append("<input style=\"width: 99%;\" class=\"xmlInput");
        if (param.hasError()) {
            result.append(" xmlInputError");
        }
        result.append("\" value=\"");
        result.append(param.getStringValue(cms));
        result.append("\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\"></td>");
        result.append(widgetDialog.dialogHorizontalSpacer(10));
        result.append("<td><table class=\"editorbuttonbackground\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
        StringBuffer buttonJs = new StringBuffer(8);
        buttonJs.append("javascript:openSelectNews(\'EDITOR\',  \'");
        buttonJs.append(id);
        buttonJs.append("\', document");
        buttonJs.append(", \'" + edicion.getTipo() + "\'");
        buttonJs.append(", \'" + edicion.getNumero() + "\'");
        try {
			buttonJs.append(", \'" + URLEncoder.encode(SeccionPage,"UTF-8") + "\'");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        buttonJs.append(");");

        result.append(widgetDialog.button(buttonJs.toString(), null, "folder", org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_SEARCH_0, widgetDialog.getButtonStyle()));
        result.append("</tr></table>");
        result.append("</td></tr></table>");
        result.append("</td>");
        return result.toString();
    }

    public I_CmsWidget newInstance() {
        return new NoticiaImpresaSelectorWidget(getConfiguration());
    }

    private Edicion getEdicion(CmsObject cms, I_CmsWidgetParameter value) throws Exception
    {
    	EdicionService eService = new EdicionService();

    	String resourcePath =((I_CmsXmlContentValue)value).getDocument().getFile().getRootPath();

    	resourcePath = cms.getRequestContext().removeSiteRoot(resourcePath);
    	resourcePath = resourcePath.replaceAll("~", "");
    	Edicion edicion = eService.obtenerEdicionImpresa(cms, resourcePath);

    	return edicion;

    }


}
