package com.tfsla.diario.admin.widgets;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCalendarWidget;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.widgets.I_CmsWidgetParameter;

import com.tfsla.diario.admin.TfsXmlContentEditor;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionBaseService;

public class TfsCalendarWidget extends A_TfsWidget implements I_TfsWidget {

	public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();

        StringBuffer result = new StringBuffer(16);

        String inputClass = "input-small";
        if (!widgetDialog.getConteinerStyleClass().equals("default"))
        	inputClass = widgetDialog.getConteinerStyleClass();

        result.append("<input class=\"datepicker "+ inputClass +"\" ");
        result.append("id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("_date\"");
        result.append(" name=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("_date\"");
        result.append(" type=\"text\" value=\"");
        result.append(getWidgetDateStringValue(cms, widgetDialog, param));    
        result.append("\" data-date-format=\""+obtenerFormatoFecha(cms).toLowerCase()+"\" placeholder=\""+obtenerFormatoFecha(cms).toLowerCase()+"\">\n");
        result.append("<spam class=\"alert-error hide\" ");
        result.append("id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("_dateInfo\" style=\"float:left; margin-top:5px;\" > </spam>\n");
        //result.append("<div class=\"input-append bootstrap-timepicker-component\">\n");

        //if (inputClass.equals("span12"))
        //	inputClass = "span10";
        
        result.append("<input class=\""+ inputClass +" time-picker\" ");
        result.append("id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("_time\"");
        result.append(" name=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("_time\"");
        result.append(" type=\"text\" value=\"");
        result.append(getWidgetTimeStringValue(cms, widgetDialog, param));        
        result.append("\">\n");
 
        result.append("<input type=\"hidden\" class=\"item-value\" ");
        result.append(" content-definition=\"" + param.getName() + "\" ");
        result.append(" content-type=\"" + getTypeName(param) + "\" ");
        result.append("id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" name=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" value=\"");
        result.append(getWidgetDateStringValue(cms, widgetDialog, param) + " " + getWidgetTimeStringValue(cms, widgetDialog, param));
        result.append("\"");
        result.append(">\n");
        //result.append("<span class=\"add-on\">\n");
        //result.append("<i class=\"icon-time\"></i>\n");
        //result.append("</span>\n");
        
        //result.append("</div>\n");
        
       result.append("<script type=\"text/javascript\">\n");
       result.append("\t$(document).ready(function () { \n");
       result.append("\t\t$('#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "_time').timepicker({\n");
       result.append("\t\t\tminuteStep: 1,\n");
       result.append("\t\t\tdefaultTime: '" + getWidgetTimeStringValue(cms, widgetDialog, param) + "',\n");
       
       //result.append("\t\t\ttemplate: 'modal',\n");
       //result.append("\t\t\tshowSeconds: true,\n");
       result.append("\t\t\tshowMeridian: true\n");
       result.append("\t\t});\n");
     
       result.append("\t$(\"#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "_date\").datepicker({orientation: \"left\",autoclose:true}).on(\"changeDate\", \n");
       result.append("\t\t function(ev){\n");  
       result.append("\t\t$(\"#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "\").val($(\"#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "_date\").val() + \" \" + $(\"#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "_time\").val());");
       result.append("\t\t });\n");
       
       result.append("\t});\n");
       
       result.append("\t$(\"#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "_time\").change(function () { ");       
       result.append("\t\t$(\"#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "\").val($(\"#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "_date\").val() + \" \" + $(\"#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "_time\").val());");
       result.append("\t});\n");
     
       result.append("\t$(\"#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "_date\").change(function () { \n");
       result.append("\t\tif( isDate($(\"#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "_date\").val() ,\""+obtenerFormatoFecha(cms).toLowerCase()+"\")){\n");
	   result.append("\t\t$(\"#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "_dateInfo\").addClass(\"hide\");\n");
       result.append("\t\t$(\"#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "\").val($(\"#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "_date\").val() + \" \" + $(\"#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "_time\").val());");
       result.append("\t\t}else{");
       result.append("\t\t$(\"#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "_dateInfo\").html(\""+widgetDialog.getMessages().key("GUI_WRONG_DATE_FORMAT")+"\");\n");
       result.append("\t\t$(\"#" + widgetDialog.getIdElement(id).replaceAll("\\.", "\\\\\\\\.") + "_dateInfo\").removeClass(\"hide\");\n");
       result.append("\t\t}");
       result.append("\t});\n");

       result.append("</script>\n");
      
        
//        if (param.hasError()) {
//            result.append(" xmlInputError");
//        }

        return result.toString();
    }

    public String getWidgetTimeStringValue(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

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
    
    public String getWidgetDateStringValue(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

        String result = param.getStringValue(cms);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result) && !"0".equals(result)) {
            try {
                result = getCalendarLocalizedDate(
                    widgetDialog.getLocale(),
                    widgetDialog.getMessages(),
                    Long.parseLong(result));
                //cambio del formato original al formato nuevo
                DateFormat dfnuevo = new SimpleDateFormat(obtenerFormatoFecha(cms));
                DateFormat dforigen = new SimpleDateFormat( CmsCalendarWidget.getCalendarJavaDateFormat(widgetDialog.getMessages().key(org.opencms.workplace.Messages.GUI_CALENDAR_DATE_FORMAT_0)));
                Date resultdate =  dforigen.parse(result);
		           result=dfnuevo.format(resultdate.getTime());
            } catch (NumberFormatException e) {
                if (!CmsMacroResolver.isMacro(result, CmsMacroResolver.KEY_CURRENT_TIME)) {
                    // neither long nor macro, show empty value
                    result = "";
                }
            }catch (ParseException pe){
            	result="";
            }
        } else {
            result = "";
        }
        return result;
    }
    
    public static long getCalendarDate(CmsMessages messages, String dateString, boolean useTime) throws ParseException {

        long dateLong = 0;

        // substitute some chars because calendar syntax != DateFormat syntax
        String dateFormat = messages.key(org.opencms.workplace.Messages.GUI_CALENDAR_DATE_FORMAT_0);
        if (useTime) {
            dateFormat += " " + messages.key(org.opencms.workplace.Messages.GUI_CALENDAR_TIME_FORMAT_0);
        }
        dateFormat = CmsCalendarWidget.getCalendarJavaDateFormat(dateFormat);

        SimpleDateFormat df = new SimpleDateFormat(dateFormat);
        dateLong = df.parse(dateString).getTime();
        return dateLong;
    }

    public static String getCalendarLocalizedTime(Locale locale, CmsMessages messages, long timestamp) {

        // get the current date & time 
        TimeZone zone = TimeZone.getDefault();
        GregorianCalendar cal = new GregorianCalendar(zone, locale);
        cal.setTimeInMillis(timestamp);
        // format it nicely according to the localized pattern
        DateFormat df = new SimpleDateFormat(
            CmsCalendarWidget.getCalendarJavaDateFormat(
            		messages.key(org.opencms.workplace.Messages.GUI_CALENDAR_TIME_FORMAT_0)));
        return df.format(cal.getTime());
    }
        
    public static String getCalendarLocalizedDate(Locale locale, CmsMessages messages, long timestamp) {

        // get the current date & time 
        TimeZone zone = TimeZone.getDefault();
        GregorianCalendar cal = new GregorianCalendar(zone, locale);
        cal.setTimeInMillis(timestamp);
        // format it nicely according to the localized pattern
        DateFormat df = new SimpleDateFormat(
            CmsCalendarWidget.getCalendarJavaDateFormat(
            		messages.key(org.opencms.workplace.Messages.GUI_CALENDAR_DATE_FORMAT_0)));
        return df.format(cal.getTime());
    }
 
    public static long getCalendarLocalizedDateTime(CmsMessages messages, String time, String dfnuevo) {

        // format it nicely according to the localized pattern
    	   DateFormat dtconfig= new SimpleDateFormat(dfnuevo +" hh:mm a");
    	   DateFormat df = new SimpleDateFormat(
            CmsCalendarWidget.getCalendarJavaDateFormat(
            		messages.key(org.opencms.workplace.Messages.GUI_CALENDAR_DATE_FORMAT_0) +
            		" " + 
            		messages.key(org.opencms.workplace.Messages.GUI_CALENDAR_TIME_FORMAT_0)));
        try {
        	 Date resultdate =  dtconfig.parse(time);
	         String  resultadofecha=df.format(resultdate.getTime());
			return df.parse(resultadofecha).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return 0;
    }
    
    @Override
    public void setEditorValue(
			CmsObject cms, 
			Map formParameters,
			TfsXmlContentEditor widgetDialog, 
			I_CmsWidgetParameter param) {

	        String[] values = (String[])formParameters.get(param.getId());

	        if (values != null && !values[0].trim().equals("")){
	        	param.setStringValue(cms, ""+getCalendarLocalizedDateTime(widgetDialog.getMessages(),values[0],obtenerFormatoFecha(cms)));
	        }
	    }
    public String obtenerFormatoFecha(CmsObject obj) {
		String siteName = OpenCms.getSiteManager().getCurrentSite(obj).getSiteRoot();
		CPMConfig configura = CmsMedios.getInstance().getCmsParaMediosConfiguration();

		String publication = "0";
		TipoEdicionBaseService tService = new TipoEdicionBaseService();
		try {
		TipoEdicion tEdicion = tService.obtenerTipoEdicion(obj, obj.getRequestContext().getUri());
		if (tEdicion!=null)
		publication = "" + tEdicion.getId();
		} catch (Exception e) {
		e.printStackTrace();
		};
		String formatofechaconfig=configura.getParam(siteName,publication,"adminNewsConfiguration","defaultDateFormatSearch");
		if(formatofechaconfig.equals("")||formatofechaconfig == null ){
			formatofechaconfig ="dd/MM/yyyy";
		}

		return formatofechaconfig;
	}
    
	@Override
	public List<String> getOpenCmsWidgetsClassName() {
		// TODO Auto-generated method stub
		List<String> widgets = new ArrayList<String>();
		widgets.add(CmsCalendarWidget.class.getName());
		return widgets;
	}
    

}