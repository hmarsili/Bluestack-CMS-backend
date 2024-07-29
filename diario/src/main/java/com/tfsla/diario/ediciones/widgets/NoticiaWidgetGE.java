package com.tfsla.diario.ediciones.widgets;

import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.widgets.A_CmsWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.ediciones.model.Edicion;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.model.TipoPublicacion;
import com.tfsla.diario.ediciones.services.EdicionService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.openCmsService;
import com.tfsla.opencmsdev.module.TfsConstants;
import com.tfsla.utils.CmsResourceUtils;

/**
 * Clase abstracta con el comportamiento del widget de noticias. Tiene un template method para crear subclases para
 * cada edici�n (diario, edici�n impresa, etc) en la cu�l se define la p�gina del buscador a utilizar.
 *
 * @author mpotelfeola / vpodberezski
 */
public class NoticiaWidgetGE extends A_CmsWidget {


	public I_CmsWidget newInstance() {
        return new NoticiaWidgetGE(getConfiguration());
    }

    public NoticiaWidgetGE() {
        super();
    }

    public NoticiaWidgetGE(String configuration) {
        super(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject,
     *      org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String titleField = getTitleField(cms, widgetDialog, param);
        String pathField = param.getId();

        String actualPath = param.getStringValue(cms);
        actualPath = actualPath != null ? actualPath : "";
        String actualTitle = getActualTitle(cms, actualPath);

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<td class=\"xmlTd\">");
        stringBuffer.append("<input  class=\"xmlInput textInput\" id=\"" + pathField + "\" name=\"" + pathField
                + "\" value=\"" + actualPath + "\"  />\n");
        stringBuffer.append("<BR />\n");
        stringBuffer.append("<label id=\"" + titleField + "\" for=\"" + pathField + "\">" + actualTitle
                + "</label>\n");

        TipoEdicion tEdicion;
		try {
			tEdicion = getTipoEdicion(cms, param);

	        if (TipoPublicacion.getTipoPublicacionByCode(tEdicion.getTipoPublicacion()).equals(TipoPublicacion.ONLINE_ROOT))
	        	this.appendBuscarButton(stringBuffer, pathField, titleField);
	        else {
	        	Edicion edicion = getEdicion(cms, param);
	        	this.appendBuscarButtonImpreso(stringBuffer, edicion, pathField);
	        }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        stringBuffer.append("</td>");

        return stringBuffer.toString();
    }

    private String getActualTitle(CmsObject cms, String actualPath) {
        try {
            return !actualPath.equals("") ? cms.readPropertyObject(CmsResourceUtils.getLink(actualPath),
                    TfsConstants.TITULO_PROPERTY, false).getValue("") : "";
        }
        catch (CmsException e) {
            return "El elemento seleccionado no es valido";
        }
    }

    private void appendBuscarButton(StringBuffer stringBuffer, String pathField, String titleField) {
        stringBuffer.append("<input type=\"button\" name=\"button_" + pathField
                + "\" value=\"Buscar\" onClick=\"javascript:newWindow('" + pathField + "', '" + titleField
                + "');\" />\n");
    }

    private void appendBuscarButtonImpreso(StringBuffer stringBuffer, Edicion edicion, String pathField)
    {
    	stringBuffer.append("<input type=\"button\" name=\"button_" + pathField
                + "\" value=\"Buscar\" onClick=\"javascript:");
    	stringBuffer.append("openSelectNews(\'EDITOR\',  \'");
    	stringBuffer.append(pathField);
    	stringBuffer.append("\', document");
    	stringBuffer.append(", \'" + edicion.getTipo() + "\'");
    	stringBuffer.append(", \'" + edicion.getNumero() + "\'");
    	stringBuffer.append(", \'index\'");
    	stringBuffer.append(");");
    	stringBuffer.append("\" />\n");

    }

    protected String getTitleField(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {
        return "label_" + param.getId();
    }

    protected void appendRestartFuntion(StringBuffer stringBuffer) {
        stringBuffer.append("function restart(pathField, label) {\n");
        stringBuffer.append("	if (navigator.appName.indexOf(\"Explorer\") != -1) {\n");
        stringBuffer.append("		document.getElementById(pathField).value = '/' + path;\n");
        stringBuffer.append("		document.getElementById(label).innerHTML = title;\n");
        stringBuffer.append("	}\n");
        stringBuffer.append("	else {\n");
        stringBuffer.append("    	document.all[pathField].value = '/' + path;;\n");
        stringBuffer.append("    	document.all[label].innerHTML = title;\n");
        stringBuffer.append("    }\n");
        stringBuffer.append("    mywindow.close();\n");
        stringBuffer.append("}\n");
    }

    public String getDialogInitCall(CmsObject cms, I_CmsWidgetDialog widgetDialog) {
        return "initNewsSelector();\n";
    }

    @Override
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("\n<SCRIPT LANGAUGE=\"JavaScript\">\n");
        stringBuffer.append("var path= \"\";\n");
        stringBuffer.append("var title= \"\";\n");

        this.appendRestartFuntion(stringBuffer);
        this.appendNewWindowFunction(stringBuffer);

        this.getDialogInitMethod(cms, widgetDialog,stringBuffer);

        stringBuffer.append("</SCRIPT>\n\n");
        return stringBuffer.toString();
    }

    private void appendNewWindowFunction(StringBuffer stringBuffer) {
        stringBuffer.append("function newWindow(field, label) {\n");
        stringBuffer.append("    mywindow=open('");
        stringBuffer.append(this.getNombrePagina());
        stringBuffer.append("', 'Buscador', 'left=400,top=60,width=540,height=600,scrollbars=1,toolbar=0');\n");

        stringBuffer.append("     mywindow.location.href = '" + this.getNombrePagina() + "?field=' + field + '&label=' + label;\n");

        stringBuffer.append("    if (mywindow.opener == null) mywindow.opener = self;\n");
        stringBuffer.append("}\n\n");
    }

    @Override
    public void setEditorValue(CmsObject cms, Map formParameters, I_CmsWidgetDialog widgetDialog,
            I_CmsWidgetParameter param) {
        super.setEditorValue(cms, formParameters, widgetDialog, param);
    }

    /**
     * Template method. Define el nombre del buscador para poder reutilizar el widget en varias publicaciones.
     */
    public String getNombrePagina() {
        return "buscador.html";
    }

    /**
     * Agregado para el gestor de ediciones.
     */
    public void getDialogInitMethod(CmsObject cms, I_CmsWidgetDialog widgetDialog,StringBuffer stringBuffer) {

    	stringBuffer.append("function initNewsSelector() {\n");
    	stringBuffer.append("	initResources(\"");
    	stringBuffer.append(OpenCms.getWorkplaceManager().getEncoding());
    	stringBuffer.append("\", \"");
    	stringBuffer.append(CmsWorkplace.VFS_PATH_MODULES);
    	stringBuffer.append("\", \"");
    	stringBuffer.append(CmsWorkplace.getSkinUri());
    	stringBuffer.append("\", \"");
    	stringBuffer.append(OpenCms.getSystemInfo().getOpenCmsContext());
    	stringBuffer.append("\");\n");
    	stringBuffer.append("}\n");

    	stringBuffer.append("var vr = null;\n");


    	stringBuffer.append("function resourceObject(encoding, contextPath, workplacePath, skinPath) {\n");
    	stringBuffer.append("	this.encoding = encoding;\n");
    	stringBuffer.append("	this.actDirId;\n");
    	stringBuffer.append("	this.contextPath = contextPath;\n");
    	stringBuffer.append("	this.workplacePath = workplacePath;\n");
    	stringBuffer.append("	this.skinPath = skinPath;\n");
    	stringBuffer.append("	this.resource = new Array();\n");
    	stringBuffer.append("	this.scrollTopType = 0;\n");
    	stringBuffer.append("	this.scrollTop = 0;\n");
    	stringBuffer.append("	this.scrollLeft = 0;\n");
    	stringBuffer.append("}\n");

    	stringBuffer.append("function initResources(encoding, workplacePath, skinPath, contextPath) {\n");
    	stringBuffer.append("	vr = new resourceObject(encoding, contextPath, workplacePath, skinPath);\n");
    	stringBuffer.append("}\n");

    	stringBuffer.append("var fileswin = null;\n");
    	stringBuffer.append("var filesForm = null;\n");
    	stringBuffer.append("var filesField = null;\n");
    	stringBuffer.append("var filesDoc = null;\n");

    	stringBuffer.append("function openSelectNews(formName, fieldName, curDoc, tipoEdicion, nroEdicion, seccion) {\n");
    	stringBuffer.append("	var paramString = \"?fieldName=\" + fieldName;\n");

    	stringBuffer.append("		paramString += \"&tipoEdicion=\";\n");
    	stringBuffer.append("		paramString += tipoEdicion;\n");
    	stringBuffer.append("		paramString += \"&nroEdicion=\";\n");
    	stringBuffer.append("		paramString += nroEdicion;\n");
    	stringBuffer.append("		paramString += \"&seccion=\";\n");
    	stringBuffer.append("		paramString += seccion;\n");

    	stringBuffer.append(" fileswin = openWin(vr.contextPath + vr.workplacePath + \"com.tfsla.diario.ediciones/templates/selectNews.jsp\"  + paramString, \"opencms\", 700, 250);\n");
    	stringBuffer.append("	filesForm = formName;\n");
    	stringBuffer.append("	filesField = fieldName;\n");
    	stringBuffer.append("	filesDoc = curDoc;\n");
    	stringBuffer.append("}\n");

    	stringBuffer.append("function openWin(url, name, w, h) {\n");
    	stringBuffer.append("	var newwin = window.open(url, name, 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=150,left=660,width='+w+',height='+h);\n");
    	stringBuffer.append("	if(newwin != null) {\n");
    	stringBuffer.append("		if (newwin.opener == null) {\n");
    	stringBuffer.append("			newwin.opener = self;\n");
    	stringBuffer.append("		}\n");
    	stringBuffer.append("	}\n");
    	stringBuffer.append("	newwin.focus();\n");
    	stringBuffer.append("	return newwin;\n");
    	stringBuffer.append("}\n");

    }

    private TipoEdicion getTipoEdicion(CmsObject cms, I_CmsWidgetParameter value) throws Exception
    {
    	TipoEdicionService tService = new TipoEdicionService();

    	String resourcePath =((I_CmsXmlContentValue)value).getDocument().getFile().getRootPath();

    	resourcePath = cms.getRequestContext().removeSiteRoot(resourcePath);
    	resourcePath = resourcePath.replaceAll("~", "");

    	TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, resourcePath);
    	if (tEdicion==null) {
    		String siteName = openCmsService.getSiteName(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot());
    		tEdicion = tService.obtenerEdicionOnlineRoot(siteName);
    	}
    	return tEdicion;

    }

    private Edicion getEdicion(CmsObject cms, I_CmsWidgetParameter value) throws Exception
    {
    	EdicionService eService = new EdicionService();

    	String resourcePath =((I_CmsXmlContentValue)value).getDocument().getFile().getRootPath();

    	resourcePath = cms.getRequestContext().removeSiteRoot(resourcePath);
    	resourcePath = resourcePath.replaceAll("~", "");

    	Edicion edicion = null;
    	edicion = eService.obtenerEdicionImpresa(cms, resourcePath);

    	return edicion;

    }


}
