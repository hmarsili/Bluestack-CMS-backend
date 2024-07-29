package com.tfsla.genericImport.transformation;

import java.net.URLDecoder;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.genericImport.exception.DataTransformartionException;

public class StringEncodingDataTransformation extends A_dataTransformation
	implements I_dataTransformation {

    protected static final Log LOG = CmsLog.getLog(FindReplaceDataTransformation.class);

	public StringEncodingDataTransformation() {
		name = "StringEncodingFormat";
		
		parameters.add("format");
		parameters.add("campos a aplicar separado por comas (vacio aplica a todos)");

	}
	
	@Override
	public String getNiceDescription() {
		return "Codificación de Caracteres";
	}

	@Override
	public String getTransformationDescription(String transformation) {
		String descrip =  "formatear el texto con " + getParameter(transformation,1);
		
		String campos = getParameter(transformation,2);
		if (campos.equals(""))
			descrip += " (todos los campos)"; 
		else
			descrip += " ( campos " + campos + ")";
		
		return descrip;

	}

	@Override
	public String getHelpText() {
		return "Formatea un texto con el formato de Encoding";
	}

	@Override
	public Object[] execute(String transformation, Object[] value) throws DataTransformartionException {

		String format = getParameter(transformation,1);
		String campos = getParameter(transformation,2);
		
		setCamposToProcess(campos);

		if (value==null)
			return null;
		
		
		
		Object[] results = new Object[value.length];
		int campIdx = 1;
		for (Object val : value)
		{
			if (processCampo(campIdx)) {
				if(format.equals("UTF-8")){
					if((String) val != null && (String) val != "")
						results[campIdx-1] = convertFromUTF8((String) val);
				}else if(format.equals("String")){
					if((String) val != null && (String) val != "")
						results[campIdx-1] = convertToUTF8((String) val);
				}else if(format.equals("HTML")){
					if((String) val != null && (String) val != ""){
						results[campIdx-1] = escapeHtml((String) val);
					}
				}
			}
			else
				results[campIdx-1] = val;
			campIdx++;
		}

		return results;

	}
	
	// convert from UTF-8 -> internal Java String format
    public static String convertFromUTF8(String s) {
        String out = null;
        try {
            out = new String(s.getBytes("ISO-8859-1"), "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }
        return out;
    }
    
 
    public static String convertToUTF8(String s) {
        String out = null;
        try {
        	out = URLDecoder.decode(s, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }
        return out;
    }
    
    public static String escapeHtml(String s) {
    	String out = "";
    	out = StringEscapeUtils.unescapeHtml(s);
  	 
  	  return out;
    }
    
    public String ToHTMLEntity(String s) {
        StringBuffer sb = new StringBuffer(s.length());

        boolean lastWasBlankChar = false;
        int len = s.length();
        char c;

        for (int i = 0; i < len; i++) {
             c = s.charAt(i);
             if (c == ' ') {
                  if (lastWasBlankChar) {
                       lastWasBlankChar = false;
                       sb.append(" ");
                  } else {
                       lastWasBlankChar = true;
                       sb.append(' ');
                  }
             } else {
                  lastWasBlankChar = false;
                  //
                  // HTML Special Chars
                  if (c == '"')
                       sb.append("&quot;");
                  else if (c == '&')
                       sb.append("&amp;");
                  else if (c == '<')
                       sb.append("&lt;");
                  else if (c == '>')
                       sb.append("&gt;");
                  else if (c == ' ')
                       // Handle Newline
                       sb.append("&lt;br/&gt;");
                  else {
                       int ci = 0xffff & c;
                       if (ci < 160)

                            sb.append(c);
                       else {

                            sb.append("&#");
                            sb.append(new Integer(ci).toString());
                            sb.append(';');
                       }
                  }
             }
        }
        return sb.toString();
   }    
    
}