package com.tfsla.diario.admin.widgets;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCalendarWidget;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.widgets.Messages;

import com.tfsla.diario.admin.TfsXmlContentEditor;

public class TfsLabelWidget extends A_TfsWidget implements I_TfsWidget {

	Map<String,String> configParams = null;

	public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

		parseParams();

        String id = param.getId();

        String value = param.getStringValue(cms);
        String localizedValue = value;
        if (CmsStringUtil.TRUE.equalsIgnoreCase(value) || CmsStringUtil.FALSE.equalsIgnoreCase(value)) {
            boolean booleanValue = Boolean.valueOf(value).booleanValue();
            if (booleanValue) {
                localizedValue = Messages.get().getBundle(widgetDialog.getLocale()).key(Messages.GUI_LABEL_TRUE_0);
            } else {
                localizedValue = Messages.get().getBundle(widgetDialog.getLocale()).key(Messages.GUI_LABEL_FALSE_0);
            }
        }

        if (id.contains("OpenCmsDateTime")) {
        	localizedValue = getWidgetStringValue(cms, widgetDialog, param);
            
        }        

        //param.getName()
        StringBuffer result = new StringBuffer(16);

        //String inputClass = "input-xlarge";

        String size =configParams.get("size");
        //if (size!=null)
        //	inputClass = size;
        
        
        result.append("<span class=\"label\">");
        result.append(localizedValue);
        result.append("</span>");
        
        //if (!widgetDialog.getConteinerStyleClass().equals("default"))
        //	inputClass ="input-block-level"; //widgetDialog.getConteinerStyleClass();
        
        
        result.append("<input class=\"item-value\" ");
        result.append("id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" name=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" content-definition=\"" + param.getName() + "\"");
        result.append(" content-type=\"" + getTypeName(param) + "\" ");
        result.append(" type=\"hidden\" value=\"");
        result.append(CmsEncoder.escapeXml(param.getStringValue(cms)));        
        result.append("\">\n");
        
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
	
    public String getWidgetStringValue(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

        String result = param.getStringValue(cms);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result) && !"0".equals(result)) {
            try {
                result = getCalendarLocalizedTime(
                    widgetDialog.getLocale(),
                    widgetDialog.getMessages(),
                    Long.parseLong(result));
            } catch (NumberFormatException e) {
                if (!CmsMacroResolver.isMacro(result, CmsMacroResolver.KEY_CURRENT_TIME)) {
                    // neither long nor macro, show empty value
                    result = "";
                }
            }
        } else {
            result = "";
        }
        return result;
    }
    
    public static String getCalendarLocalizedTime(Locale locale, CmsMessages messages, long timestamp) {

        // get the current date & time 
        TimeZone zone = TimeZone.getDefault();
        GregorianCalendar cal = new GregorianCalendar(zone, locale);
        cal.setTimeInMillis(timestamp);
        // format it nicely according to the localized pattern
        DateFormat df = new SimpleDateFormat(
            CmsCalendarWidget.getCalendarJavaDateFormat(messages.key(org.opencms.workplace.Messages.GUI_CALENDAR_DATE_FORMAT_0)
                + " "
                + messages.key(org.opencms.workplace.Messages.GUI_CALENDAR_TIME_FORMAT_0)));
        return df.format(cal.getTime());
    }

	@Override
	public List<String> getOpenCmsWidgetsClassName() {
		// TODO Auto-generated method stub
		List<String> widgets = new ArrayList<String>();
		widgets.add(CmsDisplayWidget.class.getName());
		return widgets;
	}
    

}
