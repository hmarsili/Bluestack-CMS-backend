package com.tfsla.diario.ediciones.widgets;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.widgets.A_CmsWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.types.A_CmsXmlContentValue;

import com.tfsla.opencmsdev.module.TfsConstants;
import com.tfsla.utils.CmsResourceUtils;

public class MultipleImageWidget extends A_CmsWidget {

	
	public I_CmsWidget newInstance() {
        return new MultipleImageWidget(getConfiguration());
	}

    public String getDialogInitCall(CmsObject cms, I_CmsWidgetDialog widgetDialog) {
        return "initImageSelector();\n";
    }

    @Override
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("\n<SCRIPT LANGAUGE=\"JavaScript\">\n");
        stringBuffer.append("var path= \"\";\n");
        stringBuffer.append("var title= \"\";\n");

        stringBuffer.append("var maxIdx= 0;\n");


        this.getDialogInitMethod(cms, widgetDialog,stringBuffer);

        stringBuffer.append("</SCRIPT>\n\n");
        return stringBuffer.toString();
    }

    public void getDialogInitMethod(CmsObject cms, I_CmsWidgetDialog widgetDialog,StringBuffer stringBuffer) {

    	
    	stringBuffer.append("function initImageSelector() {\n");
    	stringBuffer.append("	im_initResources(\"");
    	stringBuffer.append(OpenCms.getWorkplaceManager().getEncoding());
    	stringBuffer.append("\", \"");
    	stringBuffer.append(CmsWorkplace.VFS_PATH_MODULES);
    	stringBuffer.append("\", \"");
    	stringBuffer.append(CmsWorkplace.getSkinUri());
    	stringBuffer.append("\", \"");
    	stringBuffer.append(OpenCms.getSystemInfo().getOpenCmsContext());
    	stringBuffer.append("\");\n");
    	stringBuffer.append("}\n");

    	stringBuffer.append("var im_vr = null;\n");


    	stringBuffer.append("function im_resourceObject(encoding, contextPath, workplacePath, skinPath) {\n");
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

    	stringBuffer.append("function im_initResources(encoding, workplacePath, skinPath, contextPath) {\n");
    	stringBuffer.append("	im_vr = new im_resourceObject(encoding, contextPath, workplacePath, skinPath);\n");
    	stringBuffer.append("}\n");

    	stringBuffer.append("var im_fileswin = null;\n");
    	stringBuffer.append("var im_filesForm = null;\n");
    	stringBuffer.append("var im_filesField = null;\n");
    	stringBuffer.append("var im_filesDoc = null;\n");

    	stringBuffer.append("function newWindowImage(formName, fieldName, curDoc, maxIdx, extraParams) {\n");

    	stringBuffer.append("	var paramString = \"?fieldName=\" + fieldName;\n");

    	stringBuffer.append("		paramString += \"&maxIdx=\";\n");
    	stringBuffer.append("		paramString += maxIdx + extraParams;\n");

    	stringBuffer.append(" fileswin = im_openWin(im_vr.contextPath + im_vr.workplacePath + \"com.tfsla.opencmsdev/templates/selectImages.jsp\"  + paramString, \"opencms\", 900, 550);\n");
    	stringBuffer.append("	im_filesForm = formName;\n");
    	stringBuffer.append("	im_filesField = fieldName;\n");
    	stringBuffer.append("	im_filesDoc = curDoc;\n");
    	stringBuffer.append("}\n");

    	stringBuffer.append("function im_openWin(url, name, w, h) {\n");
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

        String imgPre = "";
        if (actualValue!=null && !actualValue.equals(""))
        {
        	
        	
        	if (getContentTypeSelected().equals("3"))
        	{
        		String fullpath = OpenCms.getLinkManager().substituteLinkForUnknownTarget(
        	            cms,
        	            actualValue);
        		
        		imgPre = "<img src=\"" + fullpath + "\" width=\"40px\" heigth=\"40px\" />" ;

        	}
        	else {    		
        		String actualTitle = getActualTitle(cms, actualValue);
        		imgPre = "<label id=\"lblPreview_" + nameField + "\">" + actualTitle + "</label>";
        	}
        }
        stringBuffer.append("<span id=\"preview_" + nameField + "\">" + imgPre + "</span>\n");
        
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

    private String getActualTitle(CmsObject cms, String actualPath) {
        try {
            return !actualPath.equals("") ? cms.readPropertyObject(CmsResourceUtils.getLink(actualPath),
                    TfsConstants.TITULO_PROPERTY, false).getValue("") : "";
        }
        catch (CmsException e) {
            return "El elemento seleccionado no es valido";
        }
    }

    private void appendSeleccionarButton(StringBuffer stringBuffer, String pathField, String elementName, String arrayName)
    {
    	String extraParams = parseConfiguration();
    	stringBuffer.append("<input type=\"button\" name=\"button_" + pathField
                + "\" value=\"Buscar\" onClick=\"javascript:");
    	stringBuffer.append("newWindowImage(\'EDITOR\',  \'");
    	stringBuffer.append(arrayName);
    	stringBuffer.append("\', document");
    	stringBuffer.append("," + elementName + "_maxIdx");
    	stringBuffer.append(",'" + extraParams + "');");
    	stringBuffer.append("\" />\n");

    }

    public MultipleImageWidget() {
        super();
    }

    public MultipleImageWidget(String configuration) {
        super(configuration);
    }

    protected String getTitleField(CmsObject cms,
	I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {
        return "label_" + param.getId();
    }
    
    private String parseConfiguration()
    {
    	String conf = getConfiguration();
    	if (conf!=null && conf!="")
    		conf = "&" + conf.replace(",","&").replace(":","=");
    	return conf;
    }
    
    private String getContentTypeSelected()
    {
    	String conf = getConfiguration();
    	String parts[] = conf.split(",");
    	for (int j=0; j< parts.length; j++)
    	{
    		if (parts[j].indexOf("contentTypes:")>-1)
    		{
    			return parts[j].replace("contentTypes:", "").trim();
    		}
    	}
    	return "";
    }
    
}
