package com.tfsla.diario.friendlyTags;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.OpenCms;

import com.tfsla.diario.model.TfsEncuesta;
import com.tfsla.diario.pollsCollector.A_PollsCollector;
import com.tfsla.diario.pollsCollector.DBPollCollector;
import com.tfsla.diario.pollsCollector.LucenePollCollector;
import com.tfsla.opencmsdev.encuestas.Encuesta;
import com.tfsla.opencmsdev.encuestas.ModuloEncuestas;
import com.tfsla.opencmsdev.encuestas.ResultadoEncuestaBean;

public class TfsEncuestasListTag extends BodyTagSupport implements I_TfsCollectionListTag,I_TfsEncuesta {


	/**
	 * 
	 */
	private static final long serialVersionUID = -1821075480136484532L;

	//static private List<A_PollsCollector> pollsCollectors = new ArrayList<A_PollsCollector>();

	private Encuesta encuesta = null;
	private TfsEncuesta previousEncuesta = null;
	private ResultadoEncuestaBean resultados = null;
	private String pollurl;
	
	public static final String param_category="category";
	public static final String param_tags="tags";
	public static final String param_size="size";
	public static final String param_page="page";
	public static final String param_order="order";
	public static final String param_group="group";
	public static final String param_state="state";
	public static final String param_numberOfParamters = "params-count";
	public static final String param_advancedFilter="advancedfilter";
	public static final String param_searchIndex="searchIndex";
	public static final String param_publication="publication";
	public static final String param_fromDateCreation="fromDateCreation";
	public static final String param_toDateCreation="toDateCreation";
	public static final String param_fromDatePublication="fromDatePublication";
	public static final String param_toDatePublication="toDatePublication";
	public static final String param_fromDateDeadline="fromDateDeadline";
	public static final String param_toDateDeadline="toDateDeadline";
	public static final String param_fromDateExpiration="fromDateExpiration";
	public static final String param_toDateExpiration="toDateExpiration";
	public static final String param_showtemporal="showtemporal";
	
	/*static {
		
		pollsCollectors.add(new DBPollCollector());
		pollsCollectors.add(new LucenePollCollector());
	}*/
	
	
	private A_PollsCollector getPollsCollector(Map<String,Object> parameters, String order)
	{
		
		A_PollsCollector bestMatchCollector = null;

		A_PollsCollector collector = new DBPollCollector();
		if (collector.canCollect(parameters)) {
				if (collector.canOrder(order))
					return collector;
				else 
					bestMatchCollector = collector;
		}
		
		collector = new LucenePollCollector();
		if (collector.canCollect(parameters)) {
				if (collector.canOrder(order))
					return collector;
				else 
					bestMatchCollector = collector;
		}
		
		return bestMatchCollector;
	}
	
	CmsObject cms = null;
	private String url = null;
	
	private String showresult="";
	
	private String tags=null;
	private String category=null;
	private String group=null;
	private String state=null;
	private String size="10";
	private String page="1";
	private String order=null;

	private String advancedfilter=null;
	private String searchindex = null;
	private String publication=null;
	
	private String fromDateCreation=null;
	private String toDateCreation=null;
	private String fromDatePublication=null;
	private String toDatePublication=null;
	private String fromDateDeadline=null;
	private String toDateDeadline=null;
	private String fromDateExpiration=null;
	private String toDateExpiration=null;
	private Boolean showtemporal = null;
	
	private List<String> encuestas=null;
	private int index = 0;
	
	@Override
    public int doStartTag() throws JspException {
		
		index = 0;
		
	    cms = CmsFlexController.getCmsObject(pageContext.getRequest());

	    if (url!=null) {
	    	showEncuesta(url);
	    	
	    	return EVAL_BODY_INCLUDE;
	    }
	    else {
	    	findPolls();
	    				
			if (index<encuestas.size()) {
				showEncuesta(encuestas.get(index));
			
				return EVAL_BODY_INCLUDE;
			}
	    	
	    }
	    return SKIP_BODY;
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
		parameters.put(param_group,group);
		parameters.put(param_state,state);
		parameters.put(param_publication,publication);
		parameters.put(param_fromDateCreation,fromDateCreation);
		parameters.put(param_toDateCreation,toDateCreation);
		parameters.put(param_fromDatePublication,fromDatePublication);
		parameters.put(param_toDatePublication,toDatePublication);
		parameters.put(param_fromDateDeadline,fromDateDeadline);
		parameters.put(param_toDateDeadline,toDateDeadline);
		parameters.put(param_fromDateExpiration,fromDateExpiration);
		parameters.put(param_toDateExpiration,toDateExpiration);
		parameters.put(param_showtemporal, showtemporal);
		
		int paramsWithValues = 
			(category!=null ? 1 : 0) +
			1  + //size
			1  + //page
			(order!=null ? 1 : 0) +
			(advancedfilter!=null ? 1 : 0) +
			(searchindex!=null ? 1 : 0) +
			(publication!=null ? 1 : 0) +

			(fromDateCreation!=null ? 1 : 0) +
			(toDateCreation!=null ? 1 : 0) +
			(fromDatePublication!=null ? 1 : 0) +
			(toDatePublication!=null ? 1 : 0) +
			(fromDateDeadline!=null ? 1 : 0) +
			(toDateDeadline!=null ? 1 : 0) +
			(fromDateExpiration!=null ? 1 : 0) +
			(toDateExpiration!=null ? 1 : 0) +
			(showtemporal!=null ? 1 : 0) +
			
			(tags!=null ? 1 : 0) +
			(group!=null ? 1 : 0) +
			(state!=null ? 1 : 0);

		parameters.put(param_numberOfParamters,(Integer)paramsWithValues);

		return parameters;
	}

	protected void findPolls()
	{
		
		encuestas = null;
		index=0;
		Map<String,Object> parameters = createParameterMap();
		A_PollsCollector collector = getPollsCollector(parameters,order);
			    
		if (collector!=null)
			encuestas = collector.collectPolls(parameters,cms);

	}
	
	private void showEncuesta(String urlResource) throws JspException 
	{

		saveEncuesta();
		encuesta = null;
		resultados = null;
		pollurl = urlResource;
		try {
			encuesta = Encuesta.getEncuestaFromURL(cms, urlResource);
			resultados = ModuloEncuestas.getResultado(cms, urlResource);
			
			 
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		exposeEncuesta(encuesta,resultados);

	}
	

	@Override
	public int doAfterBody() throws JspException {

		index++;

		if (index==encuestas.size())
			restoreEncuesta();
		
		if (index<encuestas.size()) {
			showEncuesta(encuestas.get(index));
			return EVAL_BODY_AGAIN;
		}
		
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return SKIP_BODY;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		if (group==null || group.trim().length()==0)
			this.group = null;
		else
			this.group = group;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		if (state==null || state.trim().length()==0)
			this.state = null;
		else
			this.state = state;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		if (size==null || size.trim().length()==0)
			this.size = "10";
		else
			this.size = size;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		if (order==null || order.trim().length()==0)
			this.order = null;
		else
			this.order = order;
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
	@Override
	public int doEndTag() throws JspException {
		
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return super.doEndTag();
	}

	public int getIndex() {
		// TODO Auto-generated method stub
		return index;
	}

	public boolean isLast() {
		return (index==encuestas.size()-1);
	}

	public String getCollectionValue(String name) throws JspTagException {
		return null;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		if (tags==null || tags.trim().length()==0)
			this.tags = null;
		else
			this.tags = tags;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		if (category==null || category.trim().length()==0)
			this.category = null;
		else
			this.category = category;
	}

	public String getAdvancedfilter() {
		return advancedfilter;
	}

	public void setAdvancedfilter(String advancedfilter) {
		if (advancedfilter==null || advancedfilter.trim().length()==0)
			this.advancedfilter = null;
		else
			this.advancedfilter = advancedfilter;
	}

	public String getSearchindex() {
		return searchindex;
	}

	public void setSearchindex(String searchindex) {
		if (searchindex==null || searchindex.trim().length()==0)
			this.searchindex = null;
		else
			this.searchindex = searchindex;
	}

	public String getShowresult() {
		return showresult;
	}

	public void setShowresult(String showresult) {
		if (showresult==null || showresult.trim().length()==0)
			this.showresult = null;
		else
			this.showresult = showresult;
	}
	
    protected void exposeEncuesta(Encuesta poll, ResultadoEncuestaBean results)
    {
    	TfsEncuesta encuesta = new TfsEncuesta(poll,results);
		pageContext.getRequest().setAttribute("poll", encuesta);
    }
    
    protected void restoreEncuesta()
    {
    	pageContext.getRequest().setAttribute("poll", previousEncuesta);
    }

	protected void saveEncuesta()
    {
		previousEncuesta = (TfsEncuesta) pageContext.getRequest().getAttribute("poll");
    	pageContext.getRequest().setAttribute("poll",null);
    }

	public Encuesta getEncuesta() {
		return encuesta;
	}
	

	public ResultadoEncuestaBean getResultadosEncuesta() {
		return resultados;
	}

	public String getPublication() {
		return publication;
	}
	
	public void setPublication(String publication) {
		if (publication==null || publication.trim().length()==0)
			this.publication = null;
		else
		this.publication = publication;
	}

	public String getFromDateCreation() {
		return fromDateCreation;
	}

	public void setFromDateCreation(String fromDateCreation) {
		if (fromDateCreation==null || fromDateCreation.trim().length()==0)
			this.fromDateCreation = null;
		else 
			this.fromDateCreation = fromDateCreation;
	}

	public String getFromDatePublication() {
		return fromDatePublication;
	}

	public void setFromDatePublication(String fromDatePublication) {
		if (fromDatePublication==null || fromDatePublication.trim().length()==0)
			this.fromDatePublication = null;
		else
			this.fromDatePublication = fromDatePublication;
	}

	public String getFromDateDeadline() {
		return fromDateDeadline;
	}

	public void setFromDateDeadline(String fromDateDeadline) {
		if (fromDateDeadline==null || fromDateDeadline.trim().length()==0)
			this.fromDateDeadline = null;
		else
			this.fromDateDeadline = fromDateDeadline;
	}

	public String getFromDateExpiration() {
		return fromDateExpiration;
	}

	public void setFromDateExpiration(String fromDateExpiration) {
		if (fromDateExpiration==null || fromDateExpiration.trim().length()==0)
			this.fromDateExpiration = null;
		else
			this.fromDateExpiration = fromDateExpiration;
	}
	
	public String getToDateCreation() {
		return toDateCreation;
	}

	public void setToDateCreation(String toDateCreation) {
		if (toDateCreation==null || toDateCreation.trim().length()==0)
			this.toDateCreation = null;
		else 
			this.toDateCreation = toDateCreation;
	}

	public String getToDatePublication() {
		return toDatePublication;
	}

	public void setToDatePublication(String toDatePublication) {
		if (toDatePublication==null || toDatePublication.trim().length()==0)
			this.toDatePublication = null;
		else
			this.toDatePublication = toDatePublication;
	}

	public String getToDateDeadline() {
		return toDateDeadline;
	}

	public void setToDateDeadline(String toDateDeadline) {
		if (toDateDeadline==null || toDateDeadline.trim().length()==0)
			this.toDateDeadline = null;
		else
			this.toDateDeadline = toDateDeadline;
	}

	public String getToDateExpiration() {
		return toDateExpiration;
	}

	public void setToDateExpiration(String toDateExpiration) {
		if (toDateExpiration==null || toDateExpiration.trim().length()==0)
			this.toDateExpiration = null;
		else
			this.toDateExpiration = toDateExpiration;
	}

	public String getShowtemporal() {
		return showtemporal.toString();
	}

	public void setShowtemporal(String showtemporal) {
		if (showtemporal==null || showtemporal.trim().length()==0)
			this.showtemporal = null;
		else
			this.showtemporal = Boolean.parseBoolean(showtemporal);
	}
	
	public String getCollectionIndexValue(String name, int index)
			throws JspTagException {
		return null;
	}

	public int getCollectionIndexSize(String name, boolean isCollectionPart) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public String getCollectionPathName() {
		return "";
	}

	//@Override
	public String getEncuestaUrl() {
		return pollurl;
	}
	
	public A_PollsCollector getPollCollector(Map<String,Object> parameters, String order){
		return this.getPollsCollector(parameters,order);
	}
	
}
