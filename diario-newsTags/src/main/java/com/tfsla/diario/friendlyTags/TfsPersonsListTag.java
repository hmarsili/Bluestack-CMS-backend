package com.tfsla.diario.friendlyTags;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;


import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;

import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;


import com.tfsla.diario.model.TfsListaPersonas;
import com.tfsla.diario.model.TfsPersona;
import com.tfsla.diario.terminos.model.Persons;
import com.tfsla.diario.personsCollector.DataPersonsCollector;




public class TfsPersonsListTag extends BaseTag implements I_TfsPerson,I_TfsCollectionListTag{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1193029694999850086L;

	private static final Log LOG = CmsLog.getLog(TfsPersonsListTag.class);
	
	TfsPersona persona = null;
	TfsPersona previousPersona = null;
	TfsListaPersonas previousListaPersonas = null;
	
	public static final String param_tipo="tipo";
	public static final String param_state="state";
	public static final String param_filter="filter";
	public static final String param_order="order";
	public static final String param_size="size";
	public static final String param_page="page";
	public static final String param_from="from";
	public static final String param_to="to";
	public static final String param_numberOfParamters = "params-count";
	
	private String tipo= null;
	private String state=null;
	private String filter=null;
	private String order=null;
	private int size=0;
	private int page=1;
	private String from=null;
	private String to=null;
		
	
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
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
	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		if (filter==null || filter.trim().length()==0)
			this.filter = null;
		else
			this.filter = filter;
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

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	public int getPage() {
		return page;
	}
	
	public void setPage(int page) {
			this.page = page;
	}

	
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	
	List<Persons> persons = null;
	
	int index=-1;

	
	@Override
	public int doStartTag() throws JspException {
		
		
			findPersons();
		
		return (hasMoreContent() ? EVAL_BODY_INCLUDE : SKIP_BODY );		
	}

		
	protected void findPersons()
	{
		
		persons = null;
		index=-1;
		Map<String,Object> parameters = createParameterMap();
	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	    savePersona();
		
			DataPersonsCollector collector= new DataPersonsCollector();
			
	
	    
	    persons = collector.collectPersons(parameters, cms);
	    
	  

	}
	
	protected Map<String,Object> createParameterMap()
	{
		
		
		Map<String,Object> parameters = new HashMap<String,Object>();
		
		parameters.put(param_tipo,tipo);
		parameters.put(param_state,state);
		parameters.put(param_filter,filter);
		parameters.put(param_order,order);
		parameters.put(param_size,size);
		parameters.put(param_page, page);
		parameters.put(param_to,to);
		parameters.put(param_from,from);
		
		int paramsWithValues = 
			(tipo!=null ? 1 : 0) +
			(state!=null ? 1 : 0) +
			(filter!=null ? 1 : 0) +
			(order!=null ? 1 : 0) +
			1  + //size
			1  + //page
			(to!=null ? 1 : 0)+
			(from!=null ? 1 : 0);
			return parameters;
	}
	
	@Override
	public int doAfterBody() throws JspException {

		if (hasMoreContent()) {
			return EVAL_BODY_AGAIN;
		}
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {

		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return EVAL_PAGE;
	}

	
	

	
	protected boolean hasMoreContent() throws JspException {
		
		if (persons==null)
			return false;
		
		index++;
		
		if (index<persons.size())
		{	
			exposePersona(persons.get(index));
			
			
		}
		else
			restorePersona();

		return (index<persons.size());
	}
	 protected void exposePersona(Persons person)
	    {
	       
		 TfsPersona persona = new TfsPersona(person,CmsFlexController.getCmsObject(pageContext.getRequest()));
			TfsListaPersonas lista = new TfsListaPersonas(this.persons.size(),this.index+1,this.size,this.page);
			pageContext.getRequest().setAttribute("personslist", lista);
			pageContext.getRequest().setAttribute("persona", persona);
	    }
	    
	    protected void restorePersona()
	    {
	    	pageContext.getRequest().setAttribute("persona", previousPersona );
	    	pageContext.getRequest().setAttribute("personslist", previousListaPersonas);
	    }

		protected void savePersona()
	    {
			
	       	previousListaPersonas = (TfsListaPersonas) pageContext.getRequest().getAttribute("userslist");
	    	
	    	previousPersona  = (TfsPersona) pageContext.getRequest().getAttribute("persona");
	    	pageContext.getRequest().setAttribute("personslist",null);
	    	pageContext.getRequest().setAttribute("persona",null);
	    	
	    }
		
	public int getIndex() {
		return index;
	}

	public boolean isLast() {
		return (index==persons.size()-1);
	}
	
	
	public Persons getPerson() {
		return persons.get(index);
		//return null;
	}
	
	public String getCollectionPathName() throws JspTagException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getCollectionValue(String name) throws JspTagException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getCollectionIndexValue(String name, int index)
			throws JspTagException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int getCollectionIndexSize(String name, boolean isCollectionPart)
			throws JspTagException {
		// TODO Auto-generated method stub
		return 0;
	}
	

	
	 
	


	

	
	
}
