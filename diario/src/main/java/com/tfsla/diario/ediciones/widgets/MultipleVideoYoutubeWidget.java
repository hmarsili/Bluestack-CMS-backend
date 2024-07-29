package com.tfsla.diario.ediciones.widgets;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.widgets.A_CmsWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.types.A_CmsXmlContentValue;


public class MultipleVideoYoutubeWidget extends A_CmsWidget {

	
	public I_CmsWidget newInstance() {
        return new MultipleVideoYoutubeWidget(getConfiguration());
	}

    public String getDialogInitCall(CmsObject cms, I_CmsWidgetDialog widgetDialog) {
        return "initVideoSelector();\n";
    }

    @Override
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("\n<SCRIPT LANGAUGE=\"JavaScript\">\n");
        stringBuffer.append("var vi_path= \"\";\n");
        stringBuffer.append("var vi_title= \"\";\n");

        stringBuffer.append("var maxIdx= 0;\n");


        this.getDialogInitMethod(cms, widgetDialog,stringBuffer);

        stringBuffer.append("</SCRIPT>\n\n");
        return stringBuffer.toString();
    }

    public void getDialogInitMethod(CmsObject cms, I_CmsWidgetDialog widgetDialog,StringBuffer stringBuffer) {

    	
    	stringBuffer.append("function initVideoSelector() {\n");
    	stringBuffer.append("	vi_initResources(\"");
    	stringBuffer.append(OpenCms.getWorkplaceManager().getEncoding());
    	stringBuffer.append("\", \"");
    	stringBuffer.append(CmsWorkplace.VFS_PATH_MODULES);
    	stringBuffer.append("\", \"");
    	stringBuffer.append(CmsWorkplace.getSkinUri());
    	stringBuffer.append("\", \"");
    	stringBuffer.append(OpenCms.getSystemInfo().getOpenCmsContext());
    	stringBuffer.append("\");\n");
    	stringBuffer.append("}\n");

    	stringBuffer.append("var vi_vr = null;\n");


    	stringBuffer.append("function vi_resourceObject(encoding, contextPath, workplacePath, skinPath) {\n");
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

    	stringBuffer.append("function vi_initResources(encoding, workplacePath, skinPath, contextPath) {\n");
    	stringBuffer.append("	vi_vr = new vi_resourceObject(encoding, contextPath, workplacePath, skinPath);\n");
    	stringBuffer.append("}\n");

    	stringBuffer.append("var vi_fileswin = null;\n");
    	stringBuffer.append("var vi_filesForm = null;\n");
    	stringBuffer.append("var vi_filesField = null;\n");
    	stringBuffer.append("var vi_filesDoc = null;\n");

    	stringBuffer.append("function newWindowVideo(formName, fieldName, curDoc, maxIdx) {\n");

    	stringBuffer.append("	var paramString = \"?fieldName=\" + fieldName;\n");

    	stringBuffer.append("		paramString += \"&maxIdx=\";\n");
    	stringBuffer.append("		paramString += maxIdx;\n");

    	stringBuffer.append(" fileswin = vi_openWin(vi_vr.contextPath + vi_vr.workplacePath + \"com.tfsla.opencmsdev/templates/selectYoutubeVideos.jsp\"  + paramString, \"opencms\", 900, 550);\n");
    	stringBuffer.append("	vi_filesForm = formName;\n");
    	stringBuffer.append("	vi_filesField = fieldName;\n");
    	stringBuffer.append("	vi_filesDoc = curDoc;\n");
    	stringBuffer.append("}\n");

    	stringBuffer.append("function vi_openWin(url, name, w, h) {\n");
    	stringBuffer.append("	var newwin = window.open(url, name, 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=150,left=660,width='+w+',height='+h);\n");
    	stringBuffer.append("	if(newwin != null) {\n");
    	stringBuffer.append("		if (newwin.opener == null) {\n");
    	stringBuffer.append("			newwin.opener = self;\n");
    	stringBuffer.append("		}\n");
    	stringBuffer.append("	}\n");
    	stringBuffer.append("	newwin.focus();\n");
    	stringBuffer.append("	return newwin;\n");
    	stringBuffer.append("}\n");

    	
    	stringBuffer.append("function checkContent(element) {\n");
    	stringBuffer.append("	var value = document.getElementById(element).value;\n");
    	stringBuffer.append("	if(value=='') {\n");
    	stringBuffer.append("		document.getElementById('preview_' + element).innerHTML='';\n");
    	stringBuffer.append("	}\n");
    	stringBuffer.append("}\n");

    }

    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

    	
    			
    	A_CmsXmlContentValue contentValue = (A_CmsXmlContentValue) param;
    	
    	String name = contentValue.getName();
        String nameField = param.getId();

    	int cantidadMaxima = contentValue.getMaxOccurs();
    	int idx = 1;
    	int idxPadre = 1;

    	int idxElemento = 1;
    	
    	String path = contentValue.getPath();
    	
    	String elementName = "";
    	String arrayName = "";
 
    	if (cantidadMaxima>1)
    	{
	    	int pos = path.indexOf("/" + name + "[");
	    	int len = name.length() + 2;
	    	int posEnd = path.indexOf("]",pos+len);
	    	idx = Integer.parseInt(path.substring(pos+len, posEnd));
	    	idxElemento = idx;
	    	
	    	int posName = path.indexOf("/" + name) + name.length();
	    	elementName = path.substring(0,posName+1);
	    	elementName = elementName.replace("[","").replace("]","").replace("/","_");

	    	String auxArray =path.substring(0,posName+1) + "[{idx}]";
    		auxArray = auxArray.replace("[", "_").replace("]", "_").replace("/", ".");
    		arrayName = nameField.substring(0,nameField.indexOf(".")+1) + auxArray + ".{idx-1}";
    	
    	}
    	else
    	{
    		int pos = path.indexOf("/" + name + "[");
    		int posEnd = path.lastIndexOf("[", pos);
    		idxPadre = Integer.parseInt(path.substring(posEnd+1, pos-1));
    		
    		idxElemento = idxPadre;
    		
    		int posFin = path.indexOf("/" + name);
    		int posInit = path.lastIndexOf("[",posFin);
    		elementName = path.substring(0,posInit) + "_" + path.substring(posFin+1);
    		elementName = elementName.replace("[","").replace("]","").replace("/","_");

    		String auxArray = path.substring(0,posInit) + "[{idx}]" + path.substring(posFin);
    		auxArray = auxArray.replace("[", "_").replace("]", "_").replace("/", ".");
    		arrayName = nameField.substring(0,nameField.indexOf(".")+1) + auxArray + nameField.substring(nameField.lastIndexOf("."));
            
    		//elementName = nameField.substring(nameField.indexOf(".")+1);
            //elementName = elementName.substring(0,elementName.indexOf("_"));

            //arrayName = nameField.substring(0,nameField.indexOf("_")+1) + "{idx}";
            //arrayName = arrayName + nameField.substring(nameField.indexOf("_",nameField.indexOf("_")+1));

    	}
    	
    	boolean mostrarBoton = false;
    	if (cantidadMaxima>1 && idx==1)
    		mostrarBoton = true;
    	if (cantidadMaxima==1 && idxPadre==1) {
    		mostrarBoton = true;
    	}
		//String titleField = getTitleField(cms, widgetDialog, param);

        

        String actualValue = param.getStringValue(cms);

        //CmsJspTagLink.linkTagAction(target, getRequest())
        
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<td class=\"xmlTd\">");
        stringBuffer.append("<input  class=\"xmlInput textInput\" id=\"" + nameField + "\" name=\"" + nameField
                + "\" value=\"" + actualValue + "\" onblur=\"checkContent('" + nameField + "')\" />\n");
        stringBuffer.append("<BR />\n");
        //stringBuffer.append("<label id=\"" + titleField + "\" for=\"" + nameField + "\">" + actualValue //actualTitle
        //        + "</label>\n");

        
        //if (idxPadre==1){
        if (mostrarBoton) {
            stringBuffer.append("\n<SCRIPT LANGAUGE=\"JavaScript\">\n");
            stringBuffer.append("var " + elementName + "_maxIdx=  1;\n");
            stringBuffer.append("</SCRIPT>\n\n");

            appendSeleccionarButton(stringBuffer,nameField,elementName,arrayName);

        }
        else {
        	stringBuffer.append("\n<SCRIPT LANGAUGE=\"JavaScript\">\n");
        	stringBuffer.append("if (" + idxElemento + "> " + elementName + "_maxIdx) " + elementName + "_maxIdx= " + idxElemento + ";\n");
        	stringBuffer.append("</SCRIPT>\n\n");
        }

        stringBuffer.append("</td>");
        
        return stringBuffer.toString();

	}


    private void appendSeleccionarButton(StringBuffer stringBuffer, String pathField, String elementName, String arrayName)
    {
    	stringBuffer.append("<input type=\"button\" name=\"button_" + pathField
                + "\" value=\"Buscar\" onClick=\"javascript:");
    	stringBuffer.append("newWindowVideo(\'EDITOR\',  \'");
    	stringBuffer.append(arrayName);
    	stringBuffer.append("\', document");
    	stringBuffer.append("," + elementName + "_maxIdx");
    	stringBuffer.append(");");
    	stringBuffer.append("\" />\n");

    }

    public MultipleVideoYoutubeWidget() {
        super();
    }

    public MultipleVideoYoutubeWidget(String configuration) {
        super(configuration);
    }

    protected String getTitleField(CmsObject cms,
	I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {
        return "label_" + param.getId();
    }
    
    
    
}
