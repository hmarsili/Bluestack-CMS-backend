package com.tfsla.diario.admin.jsp;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

public class TfsContentCombo {

	public TfsContentCombo() {

	}

	public void getContentElementItems(CmsObject cms, String ctype, String path, List<CmsResource> resources, String currentPublication, JSONArray jsonItems) {

		for (CmsResource file : resources) {

			if (file!= null && file.getRootPath()!= null  && !file.getRootPath().contains("~")){
				String element = "";
				String publication = "";
				try {
					CmsXmlContent resourceDocument = CmsXmlContentFactory.unmarshal(cms, cms.readFile(file));
					I_CmsXmlContentValue elementValue = resourceDocument.getValue("nombre", cms.getRequestContext().getLocale());
					I_CmsXmlContentValue publicationValue = resourceDocument.getValue("publication", cms.getRequestContext().getLocale());

					element = elementValue.getStringValue(cms);		
					publication = publicationValue.getStringValue(cms);
				} catch (CmsXmlException e) {					
					e.printStackTrace();
				} catch (CmsException e) {
					e.printStackTrace();
				}

				if(!element.equals("")){
					if(publication.equals("Todas")){
						JSONObject jsonitem = new JSONObject();
						jsonitem.put("key", file.getRootPath().toString());
						jsonitem.put("value", CmsStringUtil.escapeHtml(element));
						jsonItems.add(jsonitem);
					}else{
						if(publication.equals(currentPublication)){
							JSONObject jsonitem = new JSONObject();

							jsonitem.put("key", file.getRootPath().toString());
							jsonitem.put("value", CmsStringUtil.escapeHtml(element));
							jsonItems.add(jsonitem);
						}
					}	
				}
			}
		}			
	} 

}
