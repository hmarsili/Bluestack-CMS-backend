package com.tfsla.diario.friendlyTags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlException;

import com.tfsla.diario.eventCollector.A_EventCollector;
import com.tfsla.diario.eventCollector.LuceneEventCollector;
import com.tfsla.diario.model.TfsEvento;
import com.tfsla.diario.model.TfsListaEventos;

public class TfsEventostListTag extends A_XmlContentTag implements I_TfsNoticia, I_TfsCollectionListTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1984143153049823965L;

	private static final Log LOG = CmsLog.getLog(TfsEventostListTag.class);
	TfsEvento previousEvent = null;
	TfsListaEventos previousListaEventos = null;

	//static private List<A_EventCollector> eventCollectors = new ArrayList<A_EventCollector>();

	
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
	public static final String param_resourcefilter="resourcefilter";
	
	public static final String param_showtemporal="showtemporal";

	//static {
	//	eventCollectors.add(new LuceneEventCollector());
	//}
	
	
	CmsObject cms = null;
	private String url = null;
	
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
	
	private CmsResourceFilter resourcefilter = null;
	
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

	
	/*static private A_EventCollector getEventCollector(Map<String,Object> parameters, String order)
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
	}*/
	
	public void exposeEvent() {
		TfsEvento evento = new TfsEvento(m_cms,m_content,m_contentLocale,pageContext);
		
		TfsListaEventos listaEventos = new TfsListaEventos(this.eventos.size(),this.index+1,Integer.parseInt(this.size),Integer.parseInt( this.page));
	    
		pageContext.getRequest().setAttribute("event", evento);
		pageContext.getRequest().setAttribute("eventList", listaEventos);
		
	}
	
	
	@Override
	public int doStartTag() throws JspException {
			
		index = 0;
		
	    cms = CmsFlexController.getCmsObject(pageContext.getRequest());

    	findEvents();
    				
		if (index<eventos.size()) {
			init(eventos.get(index));
			exposeEvent();
			return EVAL_BODY_INCLUDE;
		}
    return SKIP_BODY;
	}
	
	
	@Override
	public int doAfterBody() throws JspException {

		index++;

		if (index==eventos.size())
			restoreEvent();
		
		if (index<eventos.size()) {
			init(eventos.get(index));
			exposeEvent();
			return EVAL_BODY_AGAIN;
		}
		
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return SKIP_BODY;
	}

   protected void restoreEvent() {
    	pageContext.getRequest().setAttribute("event", previousEvent);
       	pageContext.getRequest().setAttribute("eventList", previousListaEventos );
    }
	
	@Override
	public int doEndTag() {
		
		restoreEvent();

		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return EVAL_PAGE;
	}
	
	protected void findEvents() {
		
		eventos = null;
		index=0;
		Map<String,Object> parameters = createParameterMap();
		A_EventCollector collector = new LuceneEventCollector(); //getEventCollector(parameters,order);
		
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
		parameters.put(param_resourcefilter,resourcefilter);

		int paramsWithValues = 
			(category!=null ? 1 : 0) +
			1  + //size
			1  + //page
			(order!=null ? 1 : 0) +
			(advancedfilter!=null ? 1 : 0) +
			(searchindex!=null ? 1 : 0) +
			(tags!=null ? 1 : 0) +
			(name!=null ? 1 : 0) +
			(place!=null ? 1 : 0) +
			(publication!=null ? 1 : 0) +
			(fromDateModification!=null ? 1 : 0) +
			(toDateModification!=null ? 1 : 0) +
			(fromDate!=null ? 1 : 0) +
			(toDate!=null ? 1 : 0) +
			(locality!=null ? 1 : 0) +
			(country!=null ? 1 : 0) +
			(postalCode!=null ? 1 : 0) +
			(region!=null ? 1 : 0) +
			(address!=null ? 1 : 0) +
			(persons!=null ? 1 : 0) +
			(showtemporal!=null ? 1 : 0) +
			(resourcefilter!=null? 1 : 0);
			
			
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
		try {
			return getXmlDocument().getStringValue(m_cms, name, m_locale);
		} catch (CmsXmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	
	public int getCollectionIndexSize(String name, boolean isCollectionPart) {
		return getXmlDocument().getValues(name, m_locale).size();
	}

	public String getCollectionIndexValue(String name, int index) {
		try {
			return getXmlDocument().getStringValue(m_cms, name, m_locale, index);
		} catch (CmsXmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
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

	public void setResourcefilter(String resourceFilter) {
		if (resourceFilter==null || resourceFilter.trim().length()==0)
			this.resourcefilter = null;
		else {
			if (resourceFilter.trim().toUpperCase().equals("ALL"))
				this.resourcefilter = CmsResourceFilter.ALL;
			else if (resourceFilter.trim().toUpperCase().equals("ALL_MODIFIED"))
				this.resourcefilter = CmsResourceFilter.ALL_MODIFIED;
			else if (resourceFilter.trim().toUpperCase().equals("DEFAULT"))
				this.resourcefilter = CmsResourceFilter.DEFAULT;
			else if (resourceFilter.trim().toUpperCase().equals("IGNORE_EXPIRATION"))
				this.resourcefilter = CmsResourceFilter.IGNORE_EXPIRATION;
			else if (resourceFilter.trim().toUpperCase().equals("ONLY_VISIBLE"))
				this.resourcefilter = CmsResourceFilter.ONLY_VISIBLE;
			else if (resourceFilter.trim().toUpperCase().equals("ONLY_VISIBLE_NO_DELETED"))
				this.resourcefilter = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED;
		}
	}

	public String getResourcefilter() {
		if (resourcefilter==null)
			return null;
		
		return resourcefilter.toString();
	}


}
