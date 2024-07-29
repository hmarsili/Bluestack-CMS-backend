package com.tfsla.diario.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.opencms.main.CmsLog;

public class TfsBodySplitterFormatHelper {

	
	protected static final Log LOG = CmsLog.getLog(TfsBodySplitterFormatHelper.class);
	
	public static String[] getBodyFormated (String content) { 
		String[] items = null;
		content = content.replaceAll("<span></span>", "").replaceAll("<span> </span>", "").replaceAll("<p></p>", "").replaceAll("<p>&nbsp;</p>", "").replace("\n", "").replace("\r", "").replaceAll("(<.[a-z]>)\\s(<\\/*.>)","$1$2");
		Document doc = Jsoup.parse(content);
		
		List<String> lista = new ArrayList<String>();
		try {		
			Elements body = doc.select("body");
			String html = body.get(0).html().replaceAll("<p>&nbsp;</p>", "");
			Elements ps = doc.select("body > p");
			
			int j=0;
			int i=0;
			for (Element p : ps) {
				try {
					if (!(p.html().equals("&nbsp;") || p.html().equals(""))){
						lista.add(html.substring(i,html.indexOf(p.html(),j) + p.html().length() + 4).replace("&lt;", "<").replace("&gt;", ">"));
								
						j = html.indexOf(p.html(),j) + p.html().length() + 4;
							
						i = j;
					}
					
				} catch (Exception ex) {
					LOG.debug("error al parsear el body " + html + " j: " + j + "- i :" + i , ex);
				}
			}
			if (!html.substring(j).equals(""))
				lista.add (html.substring(j));
			
		} catch (Exception ex) { 
			LOG.error("error al parsear el body", ex);
		}
		items = lista.toArray(new String[lista.size()]);
		return items;
	}
	
	private static String replaceCharacters (String content) {
		return content.replace("&oacute;", "ó").
				replace("&aacute;", "á").
				replace("&eacute;", "é").
				replace("&iacute;", "í").
				replace("&uacute;", "ú");
	}
}
