package com.tfsla.diario.friendlyTags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.eventCollector.A_EventCollector;
import com.tfsla.diario.eventCollector.LuceneEventCollector;
import com.tfsla.diario.model.TfsEvento;
import com.tfsla.diario.model.TfsListaEventos;
import com.tfsla.diario.utils.TfsIncludeContentUtil;

public class TfsEventosBoxTag extends BodyTagSupport implements I_TfsCollectionListTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1984143153049823965L;

	private static final Log LOG = CmsLog.getLog(TfsEventostListTag.class);
	TfsEvento previousEvent = null;
	TfsListaEventos previousListaEventos = null;

	static private List<A_EventCollector> eventCollectors = new ArrayList<A_EventCollector>();

	
	public static final String param_category="category";
	public static final String param_tags="tags";
	public static final String param_personas = "people";

	public static final String param_size="size";
	public static final String param_page="page";
	public static final String param_order="order";
	public static final String param_title ="title";
	public static String param_shortDescription  = "volanta";
	public static String param_longDescription  = "cuerpo";
	
	public static final String param_numberOfParamters = "params-count";
	public static final String param_advancedFilter="advancedfilter";
	public static final String param_searchIndex="searchIndex";
	public static final String param_publication="publication";
	public static final String param_fromDateModification="fromDateModification";
	public static final String param_toDateModification="toDateModificacion";
	public static final String param_fromDate="fromDate";
	public static final String param_toDate="toDate";
	public static final String param_place = "lugar";
	public static final String param_address = "direccion";
	public static final String param_locality = "localidad";
	public static final String param_region  = "region";
	public static final String param_postalCode = "codigoPostal";
	public static final String param_country = "pais";
	
	public static final String param_showtemporal="showtemporal";

	static {
		eventCollectors.add(new LuceneEventCollector());
	}
	
	
	CmsObject cms = null;
	private String url = null;
	private String style = "";
	
	private String showresult="";
	
	private String tags=null;
	private String persons=null;
	
	private String category=null;
	private String name=null;
	private String place=null;
	
	
	private String size="10";
	private String page="1";
	private String order=null;

	private String advancedfilter=null;
	private String searchindex = null;
	private String publication=null;
	
	private String fromDateModification=null;
	private String toDateModification=null;
	private String fromDate=null;
	private String toDate=null;
	private String address=null;
	
	private String locality = null;
	private String region = null;
	private String country = null;
	private String postalCode = null;
	
	private Boolean showtemporal = null;
	
	private boolean hasBodyContent;
	
	public String getShowtemporal() {
		return showtemporal.toString();
	}

	public void setShowtemporal(String showtemporal) {
		if (showtemporal==null || showtemporal.trim().length()==0)
			this.showtemporal = null;
		else
			this.showtemporal = Boolean.parseBoolean(showtemporal);
	}

	private List<String> eventos=null;
	private int index = 0;

	
	public CmsObject getCms() {
		return cms;
	}

	public void setCms(CmsObject cms) {
		this.cms = cms;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getShowresult() {
		return showresult;
	}

	public void setShowresult(String showresult) {
		this.showresult = showresult;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		if (tags!=null && tags.equals(""))
			tags=null;
		this.tags = tags;
	}

	public String getPersons() {
		return persons;
	}

	public void setPersons(String persons) {
		if (persons!=null && persons.equals(""))
			persons=null;
	
		this.persons = persons;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		if (category!=null && category.equals(""))
			category=null;
	
		this.category = category;
	}

	
	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		if (page==null || page.trim().length()==0)
			this.page = "1";
		else
			this.page = page;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getAdvancedfilter() {
		return advancedfilter;
	}

	public void setAdvancedfilter(String advancedfilter) {
		if (advancedfilter != null && advancedfilter.equals(""))
			advancedfilter = null;
		this.advancedfilter = advancedfilter;
	}

	public String getSearchindex() {
		return searchindex;
	}

	public void setSearchindex(String searchindex) {
		this.searchindex = searchindex;
	}

	public String getPublication() {
		return publication;
	}

	public void setPublication(String publication) {
		this.publication = publication;
	}

	public String getFromDateModification() {
		return fromDateModification;
	}

	public void setFromDateModification(String fromDateModification) {
		if (fromDateModification!= null && fromDateModification.equals(""))
			fromDateModification =null;
	
		this.fromDateModification = fromDateModification;
	}

	public String getToDateModification() {
		return toDateModification;
	}

	public void setToDateModification(String toDateModification) {
		if (toDateModification!= null && toDateModification.equals(""))
			toDateModification =null;
		this.toDateModification = toDateModification;
	}


	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}


	

	public List<String> getEventos() {
		return eventos;
	}

	public void setEventos(List<String> eventos) {
		this.eventos = eventos;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	
	static private A_EventCollector getEventCollector(Map<String,Object> parameters, String order)
	{
		A_EventCollector bestMatchCollector = null;
		
		for (A_EventCollector collector : eventCollectors)
		{
			if (collector.canCollect(parameters))
			{
				if (collector.canOrder(order))
					return collector;
				else if (bestMatchCollector==null)
					bestMatchCollector = collector;
			}
		}
		return bestMatchCollector;
	}
	
	
	
	
	@Override
	public int doStartTag() throws JspException {
		hasBodyContent = false;
		
		index = 0;
		
	    cms = CmsFlexController.getCmsObject(pageContext.getRequest());

	    if (url!=null) {
	    	return EVAL_BODY_INCLUDE;
	    } else {
	    	findEvents();
	    				
			if (index<eventos.size()) {
				return EVAL_BODY_INCLUDE;
			}
	    }
	    return SKIP_BODY;
	}
	
	@Override
	public int doAfterBody() throws JspException {

		hasBodyContent = true;

	    if (url!=null){
	    	showEventos(url);
	    }else{
			if (index<eventos.size())
				showEventos(eventos.get(index));
	
			index++;
			
			if (index<eventos.size()) {
				return EVAL_BODY_AGAIN;
			}
	    }

		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return SKIP_BODY;
	}
	

	   protected void restoreEvent() {
	    	pageContext.getRequest().setAttribute("event", previousEvent);
	    }
	
	@Override
	public int doEndTag() throws JspException {
			
			if (!hasBodyContent) {
				
			    if (url!=null)
			    	showEventos(url);
			
				while (index<eventos.size()) {
					showEventos(eventos.get(index));
					index++;
				}
			}
			
			if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
				release();
			}
			return super.doEndTag();
		}
	
	protected void findEvents() {
		
		eventos = null;
		index=0;
		Map<String,Object> parameters = createParameterMap();
		A_EventCollector collector = getEventCollector(parameters,order);
		
	 	previousEvent = (TfsEvento) pageContext.getRequest().getAttribute("event");
    	pageContext.getRequest().setAttribute("event",null);

		
		if (collector!=null)
			eventos = collector.collectEvent(parameters,cms);

	}
	
	protected Map<String,Object> createParameterMap()
	{
		Map<String,Object> parameters = new HashMap<String,Object>();
		
		parameters.put(param_category,category);
		parameters.put(param_size,Integer.parseInt(size));
		parameters.put(param_page,Integer.parseInt(page));
		parameters.put(param_order,order);
		parameters.put(param_advancedFilter,advancedfilter);
		parameters.put(param_searchIndex,searchindex);
		parameters.put(param_tags,tags);
		parameters.put(param_title,name);
		parameters.put(param_place,place);
		parameters.put(param_publication,publication);
		parameters.put(param_fromDateModification,fromDateModification);
		parameters.put(param_toDateModification,toDateModification);
		parameters.put(param_fromDate,fromDate);
		parameters.put(param_toDate,toDate);
		parameters.put(param_locality, locality);
		parameters.put(param_country,country);
		parameters.put(param_postalCode, postalCode);
		parameters.put(param_region,region);
		parameters.put(param_address,address);
		parameters.put(param_personas,persons);
		parameters.put(param_showtemporal, showtemporal);
		
		int paramsWithValues = 
			(category!=null ? 1 : 0) +
			1  + //size
			1  + //page
			(order!=null ? 1 : 0) +
			(advancedfilter!=null ? 1 : 0) +
			(searchindex!=null ? 1 : 0) +
			(publication!=null ? 1 : 0) +

			(fromDateModification!=null ? 1 : 0) +
			(toDateModification!=null ? 1 : 0) +
			(fromDate!=null ? 1 : 0) +
			(toDate!=null ? 1 : 0) +
			(country!=null ? 1 : 0) +
			(address!=null ? 1 : 0) +
			(place!=null ? 1 : 0) +
			(region!=null ? 1 : 0) +
			(locality!=null ? 1 : 0) +
			(postalCode!=null ? 1 : 0) +
			
			(showtemporal!=null ? 1 : 0) +
			
			(tags!=null ? 1 : 0) +
			(persons!=null ? 1 : 0) +
			(name!=null ? 1 : 0);

		parameters.put(param_numberOfParamters,(Integer)paramsWithValues);

		return parameters;
	}

	public int getIndex() {
		return index;
	}

	public boolean isLast() {
		return (index==eventos.size()-1);
	}

	
	
	public String getCollectionValue(String name) throws JspTagException {
		return null;
	}
	
	
	public int getCollectionIndexSize(String name, boolean isCollectionPart) {
		return 0;
	}

	public String getCollectionIndexValue(String name, int index) {
		return null;
	}

	public String getCollectionPathName() {
		return "";
	}

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		if (fromDate != null && fromDate.equals(""))
			fromDate=null;
		this.fromDate = fromDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		if (toDate != null && toDate.equals(""))
			toDate=null;
	
		this.toDate = toDate;
	}


	private void showEventos(String urlResource) throws JspException  {

		TfsIncludeContentUtil includeContent = new TfsIncludeContentUtil(pageContext);

		String boxDivId = "eventBox_" + new Date().getTime();

		try {
			pageContext.getOut().print("<div  id=\"" + boxDivId + "\" path=\"" + urlResource + "\">");

		} catch (IOException e) {
			LOG.error("inconveniente al imprimir div: ", e);
		}

		try {
			
			CmsResource resource = cms.readResource(urlResource);
			CmsFile file = cms.readFile(resource);

			CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);

		    Locale m_contentLocale = cms.getRequestContext().getLocale();
		        if (!content.hasLocale(m_contentLocale)) {
		            Iterator it = OpenCms.getLocaleManager().getDefaultLocales().iterator();
		            while (it.hasNext()) {
		                Locale locale = (Locale)it.next();
		                if (content.hasLocale(locale)) {
		                    // found a matching locale
		                    m_contentLocale = locale;
		                    break;
		                }
		            }
		        }
			
			TfsEvento evento =  new TfsEvento(cms,content,m_contentLocale,pageContext);
			
			if (style==null || style.trim().equals("")){
				style = evento.getStyle();
				if (style==null || style.trim().equals(""))
					style = "default";
			}
			
			includeContent.setParameterToRequest("position","" + index);
			includeContent.setParameterToRequest("size","" + (eventos==null ? 1: eventos.size()));
			includeContent.setParameterToRequest("path",urlResource);
			includeContent.setParameterToRequest("id",boxDivId);
			includeContent.setParameterToRequest("style",style);
			
			
			
			includeContent.includeWithCache("/system/modules/com.tfsla.diario.newsTags/elements/events/" + style + "/eventView.jsp");

			 
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//includeContent.includeWithCache("/system/modules/com.tfsla.diario.newsTags/elements/pools/" + style + "/pool.jsp");

		try {
			pageContext.getOut().print("</div>");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	

}
