package com.tfsla.diario.admin.widgets;


import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.widgets.CmsImageGalleryWidget;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.widgets.I_CmsWidgetParameter;

import com.tfsla.diario.admin.TfsXmlContentEditor;
import com.tfsla.diario.ediciones.widgets.MultipleImageWidget;
import com.tfsla.opencmsdev.module.TfsConstants;
import com.tfsla.utils.CmsResourceUtils;

import java.util.*;

public class TfsContentMultiSelectWidget extends A_TfsWidget implements I_TfsWidget {

	Map<String,String> configParams = null;
	
    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(TfsContentMultiSelectWidget.class);

	public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

		parseParams();
		
        String id = param.getId();

        String actualValue = param.getStringValue(cms);
        
        
        StringBuffer result = new StringBuffer(16);

        boolean hasValue = actualValue!=null && !actualValue.equals("");
        	
	 	if ( configParams.get("contentTypes")==null || configParams.get("contentTypes").equals("3") || configParams.get("contentTypes").equals(""))
	    {
	 			String fullpath = "";
	 			if (hasValue)
	 				fullpath = OpenCms.getLinkManager().substituteLinkForUnknownTarget(
	    	            cms,
	    	            actualValue);
	    		if (!fullpath.equals(""))
	    			result.append("<img id=\"img_" + widgetDialog.getIdElement(id) + "\" src=\"" + fullpath + "?__scale=w:120,h:120,t:0,c:cfd8dc\" width=\"120px\" heigth=\"120px\"" + (hasValue ? "" : " style=\"display: none;\"") + " />\n");
	    		else 
	    			result.append("<img id=\"img_" + widgetDialog.getIdElement(id) + "\" src=\"\" width=\"120px\" heigth=\"120px\"" + (hasValue ? "" : " style=\"display: none;\"") + " />\n");
		    	
	    } else { 
	    	 
	    	 if(configParams.get("validateVfsPath")==null || configParams.get("validateVfsPath").equals("true")){
	    		String actualTitle = "";
	 			if (hasValue)
	 				actualTitle = getActualTitle(cms, actualValue);
	 			
	 			result.append("<label id=\"lblPreview_" + widgetDialog.getIdElement(id) + "\"" + (hasValue ? "" : " style=\"display: none;\"") + " >" + actualTitle + "</label>\n");
	    	 }
	     }
	 		
        
        String modalName = "newsModal";
        String className = "input-related-news";
        String buttonClassName = "btn-multiselectNews";
        
        String hideOnEmpty =configParams.get("hideOnEmpty");

        String inputClass="";
        if (hideOnEmpty!=null && (hideOnEmpty.toLowerCase().trim().equals("true") || hideOnEmpty.toLowerCase().trim().equals("yes")) && param.getStringValue(cms).length()==0)
        	inputClass = " input-hideOnEmpty";

 		if ( configParams.get("contentTypes")==null || configParams.get("contentTypes").equals("3") || configParams.get("contentTypes").equals("")) {
        	modalName = "myModal";
        	className = "input-image-path";
        	buttonClassName = "btn-multiselectImages";
        }
        
        if (configParams.get("contentTypes")!=null && configParams.get("contentTypes").equals("5")) {
        	if (configParams.get("fileTypesDescription").equals("Video Flash") || configParams.get("fileTypesDescription").equals("Video Vod Flash") ) {
	        	modalName = "videoFlashModal";
	        	className = "input-flash-path";
	        	buttonClassName = "btn-multiselectFlash";
        	}
        	else if (configParams.get("fileTypesDescription").equals("Video Download")) {
	        	modalName = "videoDownloadModal";
	        	className = "input-download-path";
	        	buttonClassName = "btn-multiselectDownload";
        	}
        	else if (configParams.get("fileTypesDescription").equals("Video Embedded") || configParams.get("fileTypesDescription").equals("Video Vod Embedded")) {
	        	modalName = "videoEmbeddedModal";
	        	className = "input-embedded-path";
	        	buttonClassName = "btn-multiselectEmbedded";
        	}
        	else if (configParams.get("fileTypesDescription").equals("Video Youtube") ||  configParams.get("fileTypesDescription").equals("Video Vod Youtube")) {
	        	modalName = "videoYoutubeModal";
	        	className = "input-youtube-path";
	        	buttonClassName = "btn-multiselectYoutube";
        	}
        	
        	
        }
        
        if (configParams.get("contentTypes")!=null && configParams.get("contentTypes").equals("7")) {
        	if (configParams.get("fileTypesDescription").equals("Audio")) {
	        	modalName = "audioFlashModal";
	        	className = "input-audio-path";
	        	buttonClassName = "btn-multiselectAudio";
        	}
        }
        
        if (configParams.get("contentTypes")!=null && configParams.get("contentTypes").equals("8")) {
      	    if (configParams.get("fileTypesDescription").equals("Capitulo")) {
		        	modalName = "capituloModal";
		        	className = "input-capitulo-path";
		        	buttonClassName = "btn-multiselectCapitulo";
	        } else if (configParams.get("fileTypesDescription").equals("Temporada")) {
	        	modalName = "temporadaModal";
	        	className = "input-temporada-path";
	        	buttonClassName = "btn-multiselectTemporada";
	        } else if (configParams.get("fileTypesDescription").equals("VideoOnDemand")) {
	        	modalName = "vodModal";
	        	className = "input-vod-path";
	        	buttonClassName = "btn-multiselectVod";
	        }
        }
        
        if (configParams.get("contentTypes")!=null && configParams.get("contentTypes").equals("9")) {
      	    if (configParams.get("fileTypesDescription").equals("Recetas")) {
		        	modalName = "recetaModal";
		        	className = "input-receta-path";
		        	buttonClassName = "btn-multiselectReceta";
	        } 
        }
        
        if (configParams.get("contentTypes")!=null && configParams.get("contentTypes").equals("10")) {
      	    if (configParams.get("fileTypesDescription").equals("Trivias")) {
		        	modalName = "triviaModal";
		        	className = "input-trivia-path";
		        	buttonClassName = "btn-multiselectTrivia";
	        } 
        }
        
        if(configParams.get("enableMultipublication")!=null && configParams.get("enableMultipublication").equals("true"))
        {
	            modalName = "newsMultiPublicationModal";
	            className = "input-linked-news";
	            buttonClassName = "btn-multiselectLinkedNews";
        }
        
     	result.append("<div class=\"input-append\">\n");
     	
     	result.append("<input class=\"input-xlarge focused item-value " + className + " " + inputClass + "\" type=\"text\" ");
        result.append("id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" content-definition=\"" + param.getName() + "\"");
        result.append(" content-type=\"" + getTypeName(param) + "\" ");
        result.append(" name=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" value=\"");
        result.append(CmsEncoder.escapeXml(param.getStringValue(cms)));
        result.append("\"");
        result.append(">\n");

        
        // data-toggle=\"modal\" 
        result.append("<a data-target=\"#" + modalName + "\" data-input-destination=\"" + widgetDialog.getIdElement(id) + "\" class=\"btn btn-success "+ buttonClassName + "\" rel=\"tooltip\" data-placement=\"top\" data-original-title=\"Seleccionar\"");
        if (configParams.get("enableUpload")!=null)
        {
        	result.append(" data-enableUpload=\"");
            result.append(configParams.get("enableUpload"));
            result.append("\"");
        }
        if (configParams.get("enableFTPUpload")!=null)
        {
        	result.append(" data-enableFTPUpload=\"");
            result.append(configParams.get("enableFTPUpload"));
            result.append("\"");
        }
        if (configParams.get("enableFSUpload")!=null)
        {
        	result.append(" data-enableFSUpload=\"");
            result.append(configParams.get("enableFSUpload"));
            result.append("\"");
        }
        if (configParams.get("enableVFSUpload")!=null)
        {
        	result.append(" data-enableVFSUpload=\"");
            result.append(configParams.get("enableVFSUpload"));
            result.append("\"");
        }
        if (configParams.get("fileTypes")!=null)
        {
        	result.append(" data-fileTypes=\"");
            result.append(configParams.get("fileTypes"));
            result.append("\"");
        }
        if (configParams.get("maxFileSize")!=null)
        {
        	result.append(" data-maxFileSize=\"");
            result.append(configParams.get("maxFileSize"));
            result.append("\"");
        } 
        
        if(configParams.get("enableMultipublication")!=null)
        {
        	result.append(" data-enableMultipublication=\"");
        	result.append(configParams.get("enableMultipublication"));
        	result.append("\"");
        }
        result.append("><i class=\"material-icons\">search</i></a>");
        result.append("</div>");
	   	
//        if (param.hasError()) {
//            result.append(" xmlInputError");
//        }

        return result.toString();
    }
		   
	private void parseParams(){
		
		configParams = new HashMap<String,String>();
		
		String conf = getConfiguration();
		if (conf!=null) {
			String params[] = conf.split(",");
			for (int j=0; j< params.length; j++)
	    	{
				String param[] = params[j].split(":");
				if (param.length==2)	
					configParams.put(param[0].trim(), param[1].trim());
	    		
	    	}
		}
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
	    
		@Override
		public List<String> getOpenCmsWidgetsClassName() {
			// TODO Auto-generated method stub
			List<String> widgets = new ArrayList<String>();
			widgets.add(MultipleImageWidget.class.getName());
			widgets.add(CmsImageGalleryWidget.class.getName());
			return widgets;
		}
	    
}
