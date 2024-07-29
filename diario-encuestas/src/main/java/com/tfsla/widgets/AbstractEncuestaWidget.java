package com.tfsla.widgets;

import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.widgets.A_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;

import com.tfsla.opencmsdev.encuestas.Encuesta;

public abstract class AbstractEncuestaWidget extends A_CmsWidget{
	
	public AbstractEncuestaWidget() {
        super();
    }

    public AbstractEncuestaWidget(String configuration) {
        super(configuration);
    }
   
    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject,
     *      org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String titleField = getTitleEncuestaField(cms, widgetDialog, param);
        String pathField = param.getId();

        String actualPath = param.getStringValue(cms);
        actualPath = actualPath != null ? actualPath : "";
        String actualTitle = "";
        
        if (!actualPath.equals("")){
           actualTitle = getActualEncuestaTitle(cms, actualPath);
        }

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<td class=\"xmlTd\">");
        stringBuffer.append("<input  class=\"xmlInput textInput\" id=\"" + pathField + "\" name=\"" + pathField
                + "\" value=\"" + actualPath + "\"  />\n");
        stringBuffer.append("<BR />\n");
        stringBuffer.append("<label id=\"" + titleField + "\" for=\"" + pathField + "\">" + actualTitle
                + "</label>\n");
        this.appendBuscarEncuestaButton(stringBuffer, pathField, titleField);
        stringBuffer.append("</td>");

        return stringBuffer.toString();
    }

    private String getActualEncuestaTitle(CmsObject cms, String actualPath){
        try {
        	Encuesta encuesta = Encuesta.getEncuestaFromURL(cms, actualPath);
        	
            return !actualPath.equals("") ? encuesta.getPregunta() : "";
        }
        catch (CmsException e) {
            return "El elemento seleccionado no es valido";
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return null;
    }

    private void appendBuscarEncuestaButton(StringBuffer stringBuffer, String pathField, String titleField) {
        stringBuffer.append("<input type=\"button\" name=\"button_" + pathField
                + "\" value=\"Buscar\" onClick=\"javascript:newWindow_encuesta('" + pathField + "', '" + titleField
                + "');\" />\n");
    }
    
    @SuppressWarnings("unused")
    protected String getTitleEncuestaField(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {
        return "label_" + param.getId();
    }

    protected void appendRestartFuntionEncuesta(StringBuffer stringBuffer) {
        stringBuffer.append("function restart_encuesta(pathField_encuesta, label_encuesta) {\n");
        stringBuffer.append("	if (navigator.appName.indexOf(\"Explorer\") != -1) {\n");
        stringBuffer.append("		document.getElementById(pathField_encuesta).value = path_encuesta;\n");
        stringBuffer.append("		document.getElementById(label_encuesta).innerHTML = title_encuesta;\n");
        stringBuffer.append("	}\n");
        stringBuffer.append("	else {\n");
        stringBuffer.append("    	document.all[pathField_encuesta].value = path_encuesta;\n");
        stringBuffer.append("    	document.all[label_encuesta].innerHTML = title_encuesta;\n");
        stringBuffer.append("    }\n");
        stringBuffer.append("    mywindow_encuesta.close();\n");
        stringBuffer.append("}\n");
    }

    @Override
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("\n<SCRIPT LANGAUGE=\"JavaScript\">\n");
        stringBuffer.append("var path_encuesta = \"\";\n");
        stringBuffer.append("var title_encuesta = \"\";\n");

        this.appendRestartFuntionEncuesta(stringBuffer);
        this.appendNewWindowFunctionEncuesta(stringBuffer);

        stringBuffer.append("</SCRIPT>\n\n");
        return stringBuffer.toString();
    }

    private void appendNewWindowFunctionEncuesta(StringBuffer stringBuffer) {
        stringBuffer.append("function newWindow_encuesta(field, label) {\n");
        stringBuffer.append("    mywindow_encuesta=open('");
        stringBuffer.append(this.getNombrePagina());
        stringBuffer.append("', 'BuscadorEncuestas', 'left=400,top=60,width=540,height=600,scrollbars=1,toolbar=0');\n");

        stringBuffer.append("     mywindow_encuesta.location.href = '" + this.getNombrePagina() + "?field=' + field + '&label=' + label;\n");

        stringBuffer.append("    if (mywindow_encuesta.opener == null) mywindow_encuesta.opener = self;\n");
        stringBuffer.append("}\n\n");
    }

    @Override
    public void setEditorValue(CmsObject cms, Map formParameters, I_CmsWidgetDialog widgetDialog,
            I_CmsWidgetParameter param) {
        super.setEditorValue(cms, formParameters, widgetDialog, param);
    }

    /**
     * Template method. 
     */
    public abstract String getNombrePagina();
}
