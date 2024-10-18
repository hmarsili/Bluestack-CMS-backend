package com.tfsla.diario.friendlyTags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

import jakarta.servlet.jsp.PageContext;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;

import com.tfsla.diario.model.TfsNoticia;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsTitulosNoticia extends TfsNoticia {

	public int getMostRelevant(I_TfsNoticia noticia, String wordlist, PageContext context) {

		CmsObject cms = CmsFlexController.getCmsObject(context.getRequest());

		I_CmsXmlDocument xmlContent = noticia.getXmlDocument();

		int titles = xmlContent.getIndexCount(TfsXmlContentNameProvider
				.getInstance().getTagName("news.title"), cms
				.getRequestContext().getLocale());

		try {
			ArrayList<String> most_relevant = new ArrayList<String>(); 
			
			int cant_titulos_relevantes = 0;
			
			for (int i=1; i<=titles; i++){
				int cont = 0;
				
				String title = xmlContent.getStringValue(cms, TfsXmlContentNameProvider
						.getInstance().getTagName("news.title") + "["+i+"]",
						noticia.getXmlDocumentLocale());
				
				StringTokenizer stk = new StringTokenizer(wordlist, " ");
				
				while (stk.hasMoreTokens()) {
					String palabra = stk.nextToken().trim();
					if( title.indexOf(palabra) > -1){
						cont++;
					}
				}
				
				most_relevant.add(cont+"_"+i);
				if(cont>0) cant_titulos_relevantes++;
				
			}
			
			int index = 1;
			
			if(cant_titulos_relevantes>0){
				
				Collections.sort(most_relevant);
				String mr = (String) most_relevant.get(titles-1);
				
				index = Integer.parseInt(mr.split("_")[1]);
			}
			
			return index;
			
		} catch (CmsXmlException e) {
			CmsLog.getLog(this).debug(
					"News-tags: Error reading content value "
							+ TfsXmlContentNameProvider.getInstance()
									.getTagName("news.title.section"), e);
			return 1;
		}

		
	}

	public int getTitleIndex(I_TfsNoticia noticia, String elementName,
			PageContext context) {

		CmsObject cms = CmsFlexController.getCmsObject(context.getRequest());
		I_CmsXmlDocument xmlContent = noticia.getXmlDocument();

		String content = null;

		try {
			content = xmlContent.getStringValue(cms, elementName,
					noticia.getXmlDocumentLocale());
		} catch (CmsXmlException e) {
			CmsLog.getLog(this).debug(
					"News-tags: Error reading content value " + elementName, e);
		}
		
		if (content != null)
			content.trim();

		try {
			int index = Integer.parseInt(content);
			return index;

		} catch (NumberFormatException nfe) {
			return 0;
		}
	}

}
