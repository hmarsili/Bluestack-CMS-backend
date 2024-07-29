package com.tfsla.diario.admin.widgets;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.widgets.CmsHtmlWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentValueSequence;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.admin.TfsXmlContentEditor;

public class TfsHTMLWidget extends A_TfsWidget implements I_TfsWidget {

	Map<String,String> configParams = null;

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

	public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

		parseParams();
		
        String id = param.getId();

        String hideOnEmpty =configParams.get("hideOnEmpty");
        String htmlspecialchars =configParams.get("htmlspecialchars");
        
        String inputClass="";
        if (hideOnEmpty!=null && (hideOnEmpty.toLowerCase().trim().equals("true") || hideOnEmpty.toLowerCase().trim().equals("yes")) && param.getStringValue(cms).length()==0)
        	inputClass = " input-hideOnEmpty";

        StringBuffer result = new StringBuffer(16);

        result.append("<textarea rows=\"3\" class=\"input-xlarge custom_html_editor_on_simple item-value " + inputClass + "\" ");
        result.append("id=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
        result.append(" content-definition=\"" + param.getName() + "\"");
        result.append(" content-type=\"" + getTypeName(param) + "\" ");
        result.append(" name=\"");
        result.append(widgetDialog.getIdElement(id));
        result.append("\"");
     
        result.append(" type=\"text\">");
        
        if (htmlspecialchars!=null && (htmlspecialchars.toLowerCase().trim().equals("true") || htmlspecialchars.toLowerCase().trim().equals("yes")))
        	result.append(stringToHTMLString(param.getStringValue(cms)));  
        else
        	result.append(param.getStringValue(cms));     
        
        result.append("</textarea>\n");
        
//        if (param.hasError()) {
//            result.append(" xmlInputError");
//        }

        return result.toString();
    }
	
	@Override
	public List<String> getOpenCmsWidgetsClassName() {
		// TODO Auto-generated method stub
		List<String> widgets = new ArrayList<String>();
		widgets.add(CmsHtmlWidget.class.getName());
		return widgets;
	}
	
	public static String getConfigHTMLWidget(CmsObject cms, Locale locale, CmsFile file, String elementName, String configName){
		
		String configuration = null;
		String configurationValue = null;
		
		try {
			CmsXmlContent m_content = CmsXmlContentFactory.unmarshal(cms, file);
			
			CmsXmlContentDefinition contentDefinition = m_content.getContentDefinition();

		    CmsXmlContentValueSequence elementSequence = m_content.getValueSequence(elementName, locale);
			
		    I_CmsXmlContentValue value = elementSequence.getValue(0);
		    
		    I_CmsWidget cmsWidget = contentDefinition.getContentHandler().getWidget(value);
		    
		    configuration = cmsWidget.getConfiguration();
		    
		    if(configuration!=null && !configuration.equals("")){
		    	
		    	Map<String,String> configParams = new HashMap<String,String>();
				
				if (configuration!=null) {
					String params[] = configuration.split(",");
					for (int j=0; j< params.length; j++)
			    	{
						String param[] = params[j].split(":");
						if (param.length==2)	
							configParams.put(param[0].trim(), param[1].trim());
			    	}
				}
		    	
				configurationValue = configParams.get(configName);
		    }
		    
		} catch (CmsXmlException e) {
			e.printStackTrace();
		}
		
		return configurationValue;
	}
	
	public static String stringToHTMLString(String aText){
	     final StringBuilder result = new StringBuilder();
	     final StringCharacterIterator iterator = new StringCharacterIterator(aText);
	     char character =  iterator.current();
	     while (character != CharacterIterator.DONE ){
	       if (character == '<') {
	         result.append("&lt;");
	       }
	       else if (character == '>') {
	         result.append("&gt;");
	       }
	       else if (character == '&') {
	         result.append("&amp;");
	      }
	       else if (character == '\"') {
	         result.append("&quot;");
	       }
	       else if (character == '\t') {
	         addCharEntity(9, result);
	       }
	       else if (character == '!') {
	         addCharEntity(33, result);
	       }
	       else if (character == '#') {
	         addCharEntity(35, result);
	       }
	       else if (character == '$') {
	         addCharEntity(36, result);
	       }
	       else if (character == '%') {
	         addCharEntity(37, result);
	       }
	       else if (character == '\'') {
	         addCharEntity(39, result);
	       }
	       else if (character == '(') {
	         addCharEntity(40, result);
	       }
	       else if (character == ')') {
	         addCharEntity(41, result);
	       }
	       else if (character == '*') {
	         addCharEntity(42, result);
	       }
	       else if (character == '+') {
	         addCharEntity(43, result);
	       }
	       else if (character == ',') {
	         addCharEntity(44, result);
	       }
	       else if (character == '-') {
	         addCharEntity(45, result);
	       }
	       else if (character == '.') {
	         addCharEntity(46, result);
	       }
	       else if (character == '/') {
	         addCharEntity(47, result);
	       }
	       else if (character == ':') {
	         addCharEntity(58, result);
	       }
	       else if (character == ';') {
	         addCharEntity(59, result);
	       }
	       else if (character == '=') {
	         addCharEntity(61, result);
	       }
	       else if (character == '?') {
	         addCharEntity(63, result);
	       }
	       else if (character == '@') {
	         addCharEntity(64, result);
	       }
	       else if (character == '[') {
	         addCharEntity(91, result);
	       }
	       else if (character == '\\') {
	         addCharEntity(92, result);
	       }
	       else if (character == ']') {
	         addCharEntity(93, result);
	       }
	       else if (character == '^') {
	         addCharEntity(94, result);
	       }
	       else if (character == '_') {
	         addCharEntity(95, result);
	       }
	       else if (character == '`') {
	         addCharEntity(96, result);
	       }
	       else if (character == '{') {
	         addCharEntity(123, result);
	       }
	       else if (character == '|') {
	         addCharEntity(124, result);
	       }
	       else if (character == '}') {
	         addCharEntity(125, result);
	       }
	       else if (character == '~') {
	         addCharEntity(126, result);
	       }
	       else {
	         result.append(character);
	       }
	       character = iterator.next();
	     }
	     return result.toString();
	  }
	
	private static void addCharEntity(Integer aIdx, StringBuilder aBuilder){
	    String padding = "";
	    if( aIdx <= 9 ){
	       padding = "00";
	    }
	    else if( aIdx <= 99 ){
	      padding = "0";
	    }
	    else {
	      //no prefix
	    }
	    String number = padding + aIdx.toString();
	    aBuilder.append("&#" + number + ";");
	  }
	
}
