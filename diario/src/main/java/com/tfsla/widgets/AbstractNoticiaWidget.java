package com.tfsla.widgets;

import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.widgets.A_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;

import com.tfsla.opencmsdev.module.TfsConstants;
import com.tfsla.utils.CmsResourceUtils;

/**
 * Clase abstracta con el comportamiento del widget de noticias. Tiene un template method para crear subclases para
 * cada edici�n (diario, edici�n impresa, etc) en la cu�l se define la p�gina del buscador a utilizar.
 *
 * @author mpotelfeola 
 */
public abstract class AbstractNoticiaWidget extends A_CmsWidget {

    public AbstractNoticiaWidget() {
        super();
    }

    public AbstractNoticiaWidget(String configuration) {
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
        this.appendBuscarButton(stringBuffer, pathField, titleField);
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
    
    @SuppressWarnings("unused")
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

    @Override
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("\n<SCRIPT LANGAUGE=\"JavaScript\">\n");
        stringBuffer.append("var path= \"\";\n");
        stringBuffer.append("var title= \"\";\n");

        this.appendRestartFuntion(stringBuffer);
        this.appendNewWindowFunction(stringBuffer);

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
    public abstract String getNombrePagina();
}
