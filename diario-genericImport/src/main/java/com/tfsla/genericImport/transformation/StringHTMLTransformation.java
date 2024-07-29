package com.tfsla.genericImport.transformation;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.jsoup.Jsoup;
import org.opencms.main.CmsLog;

import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import com.tfsla.genericImport.exception.DataTransformartionException;

public class StringHTMLTransformation extends A_dataTransformation
	implements I_dataTransformation {

    protected static final Log LOG = CmsLog.getLog(FindReplaceDataTransformation.class);

	public StringHTMLTransformation() {
		name = "StringCleanHTML";
		
		parameters.add("tipo");
		parameters.add("campos a aplicar separado por comas (vacio aplica a todos)");

	}
	
	@Override
	public String getNiceDescription() {
		return "Limpiar HTML del texto";
	}

	@Override
	public String getTransformationDescription(String transformation) {
		String descrip =  "limpiar el html con " + getParameter(transformation,1);
		
		String campos = getParameter(transformation,2);
		
		if (campos.equals(""))
			descrip += " (todos los campos)"; 
		else
			descrip += " ( campos " + campos + ")";
		
		return descrip;

	}

	@Override
	public String getHelpText() {
		return "Ingresar el tipo (UNESCAPE o HTML). Si se deja el tipo en blanco, devuelve texto plano <br><br> UNESCAPE = Devuelve HTML limpio. Ejemplo: entra &#38;lt;p&#38;gt;&#38;amp;nbsp;&#38;lt;/p&#38;gt; devuelve &lt;p&gt;&amp;nbsp;&lt;/p&gt; .<br> 	HTML = Limpia los tags HTML del texto. Devuelve texto plano.  Ejemplo:  entra &lt;p&gt;Texto de ejemplo. Limpia tambi&amp;eacute;n los caracteres especiales&lt;/p&gt; devuelve:  Texto de ejemplo. Limpia también los caracteres especiales<br>&nbsp;";
	}

	@Override
	public Object[] execute(String transformation, Object[] value) throws DataTransformartionException {

		String type = getParameter(transformation,1);
		String campos = getParameter(transformation,2);
		
		if(type.trim().equals(""))
			type = "HTML";
		
		setCamposToProcess(campos);

		if (value==null)
			return null;
		
		Object[] results = new Object[value.length];
		int campIdx = 1;
		for (Object val : value)
		{
			if (processCampo(campIdx)) {
				if(type.equals("UNESCAPE")){
					
					if((String) val != null && (String) val != "")
						results[campIdx-1] = escapeHtml((String) val);
				
				}else if(type.equals("HTML")){
					if((String) val != null && (String) val != ""){
						String text = escapeHtml((String) val);
						results[campIdx-1] =  getPlainText(Jsoup.parse(text));
						
					}
				}
			}
			else
				results[campIdx-1] = val;
			campIdx++;
		}

		return results;

	}
	
    public static String escapeHtml(String s) {
    	String out = "";
    	out = StringEscapeUtils.unescapeHtml(s);
  	 
  	  return out;
    }
    
    public String getPlainText(Element element) {
        FormattingVisitor formatter = new FormattingVisitor();
        NodeTraversor traversor = new NodeTraversor(formatter);
        traversor.traverse(element); 

        return formatter.toString();
    }

    private class FormattingVisitor implements NodeVisitor {
        private static final int maxWidth = 80;
        private int width = 0;
        private StringBuilder accum = new StringBuilder(); 

        public void head(Node node, int depth) {
            String name = node.nodeName();
            if (node instanceof TextNode)
                append(((TextNode) node).text()); 
            else if (name.equals("li"))
                append("\n * ");
            else if (name.equals("dt"))
                append("  ");
            else if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5", "tr"))
                append("\n");
        }

        public void tail(Node node, int depth) {
            String name = node.nodeName();
            if (StringUtil.in(name, "br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5"))
                append("\n");
            else if (name.equals("a"))
                append(String.format(" <%s>", node.absUrl("href")));
        }

        private void append(String text) {
            if (text.startsWith("\n"))
                width = 0; 
            if (text.equals(" ") &&
                    (accum.length() == 0 || StringUtil.in(accum.substring(accum.length() - 1), " ", "\n")))
                return; 

            if (text.length() + width > maxWidth) { 
                String words[] = text.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    boolean last = i == words.length - 1;
                    if (!last) 
                        word = word + " ";
                    if (word.length() + width > maxWidth) { 
                        accum.append("\n").append(word);
                        width = word.length();
                    } else {
                        accum.append(word);
                        width += word.length();
                    }
                }
            } else { 
                accum.append(text);
                width += text.length();
            }
        }

        @Override
        public String toString() {
            return accum.toString();
        }
    }
    
}