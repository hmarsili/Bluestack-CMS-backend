package com.tfsla.diario.admin.widgets;

import java.util.ArrayList;
import java.util.List;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.OpenCms;
import org.opencms.widgets.CmsLinkGalleryWidget;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.widgets.I_CmsWidgetParameter;

import com.tfsla.diario.admin.TfsXmlContentEditor;

public class TfsLinkGalleryWidget extends A_TfsWidget implements I_TfsWidget {
	
	public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {
		
        String id = param.getId();

		StringBuffer result = new StringBuffer(16);
		
		result.append("<div class=\"input-append\">\n");
     	
     	result.append("<input class=\"input-xlarge item-value focused input-vfsFile \" type=\"text\" ");
        result.append("id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" name=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" content-definition=\"" + param.getName() + "\"");
        result.append(" content-type=\"" + getTypeName(param) + "\" ");
        result.append(" value=\"");
        result.append(CmsEncoder.escapeXml(param.getStringValue(cms)));
        result.append("\"");
        result.append(">\n");
        
        result.append("<a data-target=\"#vfsLinkModal\" data-input-destination=\"" + widgetDialog.getIdElement(id) + "\" class=\"btn btn-success btn-multiselectVfsLink\" rel=\"tooltip\" data-placement=\"top\" data-original-title=\"Seleccionar\"");
     
        try {
			int fileType = OpenCms.getResourceManager().getResourceType("pointer").getTypeId();
		

	    	result.append(" data-fileTypes=\"");
	        result.append("" + fileType);
	        result.append("\"");
        
        } catch (CmsLoaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        try {
	        int folderType = OpenCms.getResourceManager().getResourceType("linkgallery").getTypeId();
			
	        
	    	result.append(" data-folderTypes=\"");
	        result.append("" + folderType);
	        result.append("\"");
	        
		} catch (CmsLoaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        result.append("><i class=\"material-icons\">search</i></a>");
        result.append("</div>");
	   	
//        if (param.hasError()) {
//            result.append(" xmlInputError");
//        }

        return result.toString();
	}

	@Override
	public List<String> getOpenCmsWidgetsClassName() {
		// TODO Auto-generated method stub
		List<String> widgets = new ArrayList<String>();
		widgets.add(CmsLinkGalleryWidget.class.getName());
		return widgets;
	}
	
}
