package com.tfsla.diario.webservices;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.ediciones.data.ZoneDAO;
import com.tfsla.diario.ediciones.model.Zona;
import com.tfsla.diario.friendlyTags.TfsNoticiasListTag;
import com.tfsla.diario.newsCollector.A_NewsCollector;
import com.tfsla.diario.webservices.common.ServiceHelper;
import com.tfsla.diario.webservices.common.ServiceType;
import com.tfsla.diario.webservices.common.interfaces.INewsListService;
import com.tfsla.diario.webservices.core.services.*;
import com.tfsla.genericImport.service.ContentTypeService;
import com.tfsla.utils.UrlLinkHelper;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class NewsListService extends TfsListWebService implements INewsListService {
	
	public NewsListService(PageContext context, HttpServletRequest request, HttpServletResponse response) throws Throwable {
		super(context, request, response);
		this.includeEmpty = false;
		String includeEmptyParam = request.getParameter("includeEmpty");
		try {
			this.includeEmpty = Boolean.parseBoolean(includeEmptyParam);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected ServiceType getServiceType() {
		return ServiceType.NEWS;
	}
	
	@Override
	public JSON doExecute() {
		JSONArray jsonResponse = new JSONArray();
		String fields = request.getParameter("fields");
		JSONArray jsonValues;
		CmsXmlContent content = null;
		JSONObject jsonitem = null;
		if(request.getParameter("site") != null && !request.getParameter("site").equals("")) {
			cms.getRequestContext().setSiteRoot(request.getParameter("site"));
		}
		List<CmsResource> noticias = this.getNewsList();
		if(noticias == null || noticias.size() == 0) return jsonResponse;
		
		for(CmsResource noticia : noticias) {
			try {
				jsonValues = new JSONArray();
				content = CmsXmlContentFactory.unmarshal(cms, noticia, request);
				
				if(fields != null && !fields.equals("")) {
					
					//Proceso según listado de campos provistos en parámetro 'fields'
					String [] fieldsArray = fields.split(",");
					for(String field : fieldsArray) {
						jsonitem = this.getJSONObjectFromContent(field, content);
						if(jsonitem == null) continue;
						jsonValues.add(jsonitem);
					}
				} else {
					
					//Proceso todos los campos en el contenido estructurado
					JSONArray jsonContentDefinition = new JSONArray();
					CmsXmlContentDefinition contentDefinition = content.getContentDefinition();
					ContentTypeService service = new ContentTypeService();
					service.getElementItems(cms, contentDefinition.getInnerName(), "", "", jsonContentDefinition);
					
					for(int i = 0; i < jsonContentDefinition.size(); i++) {
						jsonitem = this.getJSONObjectFromContent(jsonContentDefinition.getJSONObject(i).getString("key"), content);
						if(jsonitem == null) continue;
						jsonValues.add(jsonitem);
					}
				}
				JSONObject jsonItem = new JSONObject();
				jsonItem.put("url", noticia.getRootPath());
				jsonItem.put("urlFriendly", UrlLinkHelper.getUrlFriendlyLink(noticia, cms, request,true,false));
				jsonItem.put("data", jsonValues);
				jsonResponse.add(jsonItem);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		return jsonResponse;
	}
	
	private List<CmsResource> getNewsList() {
		List<CmsResource> noticias = new ArrayList<CmsResource>();
		String order = "user-modification-date desc";
		Map<String,Object> filters = this.getFilters();
		String filter = "";
		
		if(filters.containsKey(TfsNoticiasListTag.param_url) && filters.get(TfsNoticiasListTag.param_url) != null) {
			try {
				CmsResource res = this.cms.readResource(filters.get(TfsNoticiasListTag.param_url).toString());
				noticias.add(res);
			} catch (CmsException e) {
				e.printStackTrace();
			}
			return noticias;
		}
		
		if(filters.get(TfsNoticiasListTag.param_publication) == null ) {
			if(this.session.getPublication() != null && !this.session.getPublication().equals("")) 
				filters.put(TfsNoticiasListTag.param_publication, this.session.getPublication());
		}
	
		if(filters.containsKey(TfsNoticiasListTag.param_order) && filters.get(TfsNoticiasListTag.param_order) != null) {
			order = filters.get(TfsNoticiasListTag.param_order).toString();
		} else {
			if( filters.get(TfsNoticiasListTag.param_zone) != null ) {
				String zone = filters.get(TfsNoticiasListTag.param_zone).toString();
				if (zone.contains(",")) {
					String orderByZone = this.getOrderByZone(filters, zone);
					if(orderByZone != null && !orderByZone.equals("")) {
						order = orderByZone;
					}
				}
			}
		}
		filters.put(TfsNoticiasListTag.param_order, order);

		A_NewsCollector collector = ServiceHelper.getNewsCollector(filters, order);
		
		if(collector == null) return null;
		
		if(filters.containsKey(TfsNoticiasListTag.param_filter) && filters.get(TfsNoticiasListTag.param_filter) != null)
			filter = filters.get(TfsNoticiasListTag.param_filter).toString();
		
		if (filter!=null && (!(filter.equals("")))) {
			// el parametro filter se ha establecido preguntar si es mayor a al min de caracteres
			if(filter.trim().length() >= ServiceHelper.getMinValueConfig(request)) {
				noticias = collector.collectNews(filters, cms);
			}
		} else {
			// el filter es null, no se ha establecido debo hacer la busqueda
			noticias = collector.collectNews(filters, cms);
		}
		
		return noticias;
	}

	private JSONObject getJSONObjectFromContent(String key, CmsXmlContent content) {
		I_CmsXmlContentValue value = content.getValue(key, locale);
		String stringValue = "";
		JSONObject jsonitem = new JSONObject();
		if(value != null) stringValue = value.getStringValue(cms);
		if(stringValue.equals("") && !this.includeEmpty) return null;
		
		if(value == null) {
			jsonitem.put(key, "");
			return jsonitem;
		}
		//if(key.contains("titulo")) stringValue = StringEscapeUtils.escapeHtml(stringValue);
		jsonitem.put(value.getPath(), stringValue);
		
		if(value.getMaxIndex() > 1) {
			for(int i = 2; i <= value.getMaxIndex(); i++) {
				value = content.getValue(String.format("%s[%s]", key, i), locale);
				if(value == null) continue;
				stringValue = value.getStringValue(cms);
				//if(key.contains("titulo")) stringValue = StringEscapeUtils.escapeHtml(stringValue);
				jsonitem.put(value.getPath(), stringValue);
			}
		}
		return jsonitem;
	}
	
	private String getOrderByZone(Map<String,Object> filters, String zone) {
		ZoneDAO zonaDao = new ZoneDAO();
	   	try {
	   		int publication = Integer.valueOf((String) filters.get(TfsNoticiasListTag.param_publication));
	   		int pageId = 0;
	   		String onmainpage = (String) filters.get(TfsNoticiasListTag.param_onmainpage);
	   		if(onmainpage.equals("home")) pageId = 1;
	   		if(onmainpage.equals("section")) pageId = 2;

			Zona zona = zonaDao.getZone(publication, pageId, zone);
			
			return zona.getOrderDefault();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	   	return "";
	}
	
	private Boolean includeEmpty = false;
}