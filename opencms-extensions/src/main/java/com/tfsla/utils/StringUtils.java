package com.tfsla.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.opencms.main.OpenCms;

import com.tfsla.collections.CollectionFactory;
import com.tfsla.exceptions.ApplicationException;

public class StringUtils {

    /**
     * <code>
     * detecta agrupaciones de 2 o m�s <br> y </br> con espacios, cambios de linea (o no)
     * en el medio, y lo cambia por <br><br>
     * </code>
     * 
     * @param string
     * @return
     */
    public static String normalizeBr(String string) {
        return string.replaceAll("<br\\s*/?>(\\s*<br\\s*/?>)+", "<br><br>");
    }

    public static String fromHtml(String string) {
        string = string.replaceAll("&aacute;", "�");
        string = string.replaceAll("&eacute;", "�");
        string = string.replaceAll("&iacute;", "�");
        string = string.replaceAll("&oacute;", "�");
        string = string.replaceAll("&uacute;", "�");
        return string;
    }

    public static String toHtml(String string) {
        string = string.replaceAll("�", "&aacute;");
        string = string.replaceAll("�", "&eacute;");
        string = string.replaceAll("�", "&iacute;");
        string = string.replaceAll("�", "&oacute;");
        string = string.replaceAll("�", "&uacute;");
        return string;
    }

    public static String toUnicode(String string) {
        StringBuffer sb = new StringBuffer(string.length());
        // true if last char was blank
        int len = string.length();
        char c;

        for (int i = 0; i < len; i++) {
            c = string.charAt(i);
            int ci = 0xffff & c;
            if (ci < 160)
                // nothing special only 7 Bit
                sb.append(c);
            else {
                // Not 7 Bit use the unicode system
                sb.append("&#");
                sb.append(new Integer(ci).toString());
                sb.append(';');
            }
        }
        return sb.toString();
    }

    /**
     * Separa un String en distintos tokens y devuelve los resultados en una <code>Collection</code> de Strings.
     * 
     * @param str
     *            El String a separar.
     * @param delim
     *            El caracter delimitador.
     */
    public static Collection<String> collectTokens(String str, String delim) {
        Collection<String> collection = CollectionFactory.createCollection();

        StringTokenizer tokenizer = new StringTokenizer(str, delim);

        while (tokenizer.hasMoreElements()) {
            collection.add(tokenizer.nextToken());
        }

        return collection;
    }

    public static List<String> normalizeToSearch(String string) {
        String out = string;
        // TODO sacar la basura con una regular expresion
        out = out.replaceAll("�|�", "n");
        out = out.replaceAll("�|�", "a");
        out = out.replaceAll("�|�", "e");
        out = out.replaceAll("�|�", "i");
        out = out.replaceAll("�|�", "o");
        out = out.replaceAll("�|�|�|�", "u");
        out = out.replaceAll("\\W", " ");
        List<String> list = CollectionFactory.createList();
        for (String value : out.toLowerCase().split(" ")) {
            if (org.apache.commons.lang.StringUtils.isNotEmpty(value) && !list.contains(value)) {
                list.add(value);
            }
        }
        return list;
    }

    /**
     * Usa el encoding configurado el opencms-system.xml para encodear
     * 
     * @param url
     */
    public static String encodeURL(String url) {
        String enc = OpenCms.getSystemInfo().getDefaultEncoding();
        try {
            return URLEncoder.encode(url, enc);
        }
        catch (UnsupportedEncodingException e) {
            throw new ApplicationException(
                    "Ud. tiene mal configurado el encode del archivo opencms-system.xml. Encode:" + enc, e);
        }
    }

    /**
     * Usa el encoding configurado el opencms-system.xml para decodear
     * 
     * @param url
     */

    public static String decodeURL(String url) {
        String enc = OpenCms.getSystemInfo().getDefaultEncoding();
        try {
            return URLDecoder.decode(url, enc);
        }
        catch (UnsupportedEncodingException e) {
            throw new ApplicationException(
                    "Ud. tiene mal configurado el encode del archivo opencms-system.xml. Encode:" + enc, e);
        }
    }

    // public static void writeError(CmsObject cms, JspWriter out, HttpServletRequest request, HttpServletResponse
    // response, Throwable exception) throws IOException {
    // if(!cms.getRequestContext().currentProject().isOnlineProject()) {
    // out.print("<script>alert('");
    // exception.printStackTrace(new java.io.PrintWriter(out));
    // out.print("')</script>");
    // System.out.println("ALERTA");
    // }
    // else {
    // System.out.println("NO alerta");
    // }
    // exception.printStackTrace();
    // }

    public static String escapeJSQuotes(String string) {
        string = string.replaceAll("\"", "&quot;");
        return string.replaceAll("\'", "&#92;&#39;");
    }

    public static String restoreJSQuotes(String string) {
        string = string.replaceAll("&quot;", "\"");
        return string.replaceAll("&#92;&#39;", "\'");
    }

}
