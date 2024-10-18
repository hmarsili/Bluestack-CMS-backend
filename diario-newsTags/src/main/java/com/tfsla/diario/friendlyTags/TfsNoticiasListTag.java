package com.tfsla.diario.friendlyTags;

import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspTagEditable;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.workplace.editors.directedit.CmsDirectEditButtonSelection;
import org.opencms.workplace.editors.directedit.CmsDirectEditMode;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;

import com.tfsla.diario.ediciones.data.ZoneDAO;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.model.Zona;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.model.TfsListaNoticias;
import com.tfsla.diario.model.TfsNoticia;
import com.tfsla.diario.model.TfsPublicacion;
import com.tfsla.diario.newsCollector.A_NewsCollector;
import com.tfsla.diario.newsCollector.EdicionImpresaHomeNewsCollector;
import com.tfsla.diario.newsCollector.EdicionImpresaNewsCollector;
import com.tfsla.diario.newsCollector.LuceneNewsCollector;
import com.tfsla.diario.utils.TfsDirectEditParams;


public class TfsNoticiasListTag extends A_XmlContentTag implements I_TfsNoticia, I_TfsCollectionListTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5413171197869711892L;
	private static final Log LOG = CmsLog.getLog(TfsNoticiasListTag.class);
	TfsNoticia previousNoticia = null;
	TfsListaNoticias previousListaNoticia = null;

	public static final String param_logquery = "logquery";
	public static final String param_section = "section";
	public static final String param_zone="zone";
	public static final String param_category="category";
	public static final String param_size="size";
	public static final String param_page="page";
	public static final String param_author="author";
	public static final String param_newscreator="newscreator";
	public static final String param_group="group";
	public static final String param_order="order";
	public static final String param_edition="edition";
	public static final String param_publication="publication";
	public static final String param_filter="filter";
	public static final String param_advancedFilter="advancedfilter";
	public static final String param_searchIndex="searchIndex";
	public static final String param_tags="tags";
	public static final String param_from="from";
	public static final String param_to="to";
	public static final String param_age="age";
	public static final String param_onmainpage =  "onmainpage";
	public static final String param_state="state";
	public static final String param_numberOfParamters = "params-count";
	public static final String param_newstype="newstype";
	public static final String param_showtemporal="showtemporal";
	public static final String param_searchinhistory="searchinhistory";
	public static final String param_persons="persons";
	public static final String param_url="url";
	public static final String param_resourcefilter="resourcefilter";
	public static final String param_exactpage="exactpage";
	
	private A_NewsCollector getNewsCollector(Map<String,Object> parameters, String order)
	{
		A_NewsCollector bestCollector = new LuceneNewsCollector();
		
		A_NewsCollector collector = new LuceneNewsCollector();
		if (collector.canCollect(parameters)) {
				if (collector.canOrder(order))
					return collector;
				else 
					bestCollector = collector;
		}
		
		/*
		collector = new RankingNewsCollector();
		if (collector.canCollect(parameters)) {
				if (collector.canOrder(order))
					return collector;
				else 
					bestCollector = collector;
		}
		*/
	
		collector = new EdicionImpresaHomeNewsCollector();
		if (collector.canCollect(parameters)) {
			if (collector.canOrder(order))
				return collector;
			else 
				bestCollector = collector;
		}
			
		collector = new EdicionImpresaNewsCollector();
		if (collector.canCollect(parameters)) {
			if (collector.canOrder(order))
				return collector;
			else 
				bestCollector = collector;
		}	
			
		return bestCollector;
	}
	
	private Boolean logquery=null;
	private String section = null;
	private String zone= null;
	private String category=null;
	private String tags=null;
	private int size=0;
	private int page=1;
	private String author=null;
	private String newscreator=null;
	private String group=null;
	private String order=null;
	private String edition=null;
	private String publication=null;
	private String from=null;
	private String to=null;
	private String advancedfilter=null;
	private String filter = null;
	private String onmainpage = null;
	private String searchindex = null;
	private String age = null;
	private String state = null;
	private Boolean showtemporal = null;
	private Boolean searchinhistory = null;
	private String newstype = null;
	private String persons = null;
	private CmsResourceFilter resourcefilter = null;
	private String exactpage = null;
	
	public int getPage() {
		return page;
	}

	public void setPage(int page) {
			this.page = page;
	}

	List<CmsResource> noticias = null;
	int index=0;

	private CmsDirectEditMode m_directEditMode;
	private String m_directEditLinkForNew = null;
	private CmsDirectEditButtonSelection m_directEditFollowButtons;
	private boolean m_directEditOpen=false;
	//private int minValueConfig = getMinValueConfig();
	@Override
	public int doStartTag() throws JspException {
		
		findNews();
		
		initDirectEdit();
		
		if (noticias!=null && noticias.size()>0) {
			init(noticias.get(index));
			exposeNoticia();
			
			openDirectEdit();
			return EVAL_BODY_INCLUDE;
		}
		
		return SKIP_BODY;		
	}

	protected void initDirectEdit()
	{
		m_directEditLinkForNew = null;
		 m_directEditOpen=false;
		if (m_directEditMode == null) {
            m_directEditMode = CmsDirectEditMode.FALSE;
        }
		
        // use "create link" only if collector supports it
        m_directEditLinkForNew = CmsEncoder.encode("Contenidos|/contenidos/noticia_0001.html|50");
	}
	
	protected void findNews()
	{
		
		noticias = null;
		index = 0;
		Map<String,Object> parameters = createParameterMap();
		A_NewsCollector collector = getNewsCollector(parameters,order);
		
	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

	    saveNoticia();
	    
		if (collector!=null){
			if (filter!=null && (!(filter.equals("")))){
				//el parametro filter se ha establecido preguntar si es mayor a al min de caracteres
				if(filter.trim().length()<getMinValueConfig()){
					noticias=null;
				}else{
					noticias = collector.collectNews(parameters,cms);
				}
			}else{
				// el filter es null ,no se ha establecido debo  hacer la busqueda
				noticias = collector.collectNews(parameters,cms);
			}
		}
	}
	
	protected Map<String,Object> createParameterMap()
	{
		if((size ==0 ||  order==null ) && zone!=null && !zone.contains(",")){
		  
			if(size==0) setValuesDefault("size");
				
			if(order==null) setValuesDefault("order");
		}
		
		if (size==0)
			size=100;
		
		Map<String,Object> parameters = new HashMap<String,Object>();
		
		parameters.put(param_logquery,logquery);
		parameters.put(param_section, section);
		parameters.put(param_zone,zone);
		parameters.put(param_category,category);
		parameters.put(param_size,size);
		parameters.put(param_author,author);
		parameters.put(param_newscreator,newscreator);
		parameters.put(param_group,group);
		parameters.put(param_order,order);
		parameters.put(param_edition,edition);
		parameters.put(param_publication,publication);
		parameters.put(param_filter,filter);
		parameters.put(param_advancedFilter,advancedfilter);
		parameters.put(param_searchIndex,searchindex);
		parameters.put(param_tags,tags);
		parameters.put(param_onmainpage, onmainpage);
		parameters.put(param_from,from);
		parameters.put(param_to,to);
		parameters.put(param_age,age);
		parameters.put(param_page, page);
		parameters.put(param_state,state);
		parameters.put(param_showtemporal, showtemporal);
		parameters.put(param_searchinhistory, searchinhistory);
		parameters.put(param_newstype,newstype);
		parameters.put(param_persons,persons);
		parameters.put(param_resourcefilter,resourcefilter);
		parameters.put(param_exactpage,exactpage);
		
		int paramsWithValues = 
			(logquery!=null ? 1 : 0) +
			(section!=null ? 1 : 0) +
			(zone!=null ? 1 : 0) +
			(category!=null ? 1 : 0) +
			1  + //size
			(author!=null ? 1 : 0) +
			(newscreator!=null ? 1 : 0) +
			(group!=null ? 1 : 0) +
			(order!=null ? 1 : 0) +
			(edition!=null ? 1 : 0) +
			(publication!=null ? 1 : 0) +
			(filter!=null ? 1 : 0) +
			(advancedfilter!=null ? 1 : 0) +
			(searchindex!=null ? 1 : 0) +
		
			(tags!=null ? 1 : 0) +
			(onmainpage!=null ? 1 : 0) +
			(from!=null ? 1 : 0) +
			(to!=null ? 1 : 0) +
			(age!=null ? 1 : 0) +
			(state!=null ? 1 : 0) +
			(showtemporal!=null ? 1 : 0) +
			(searchinhistory!=null ? 1 : 0) +
			(newstype!=null ? 1 : 0) +
			(persons!=null ? 1 : 0) +
			(exactpage!=null ? 1 : 0) +
			1 ; //page

		parameters.put(param_numberOfParamters,(Integer)paramsWithValues);

		return parameters;
	}
	
	@Override
	public int doAfterBody() throws JspException {
		
		if (m_directEditOpen)
			CmsJspTagEditable.endDirectEdit(pageContext);
		
		index++;

		if (index==noticias.size())
			restoreNoticia();

		if (index<noticias.size())
		{	
			init(noticias.get(index));
			exposeNoticia();
			
			openDirectEdit();
			return EVAL_BODY_AGAIN;
		}
		
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		
		restoreNoticia();

		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		
		clearParameters();
		
		return EVAL_PAGE;
	}

	protected void clearParameters() {
		logquery=null;
		section = null;
		zone= null;
		category=null;
		tags=null;
		size=0;
		page=1;
		author=null;
		newscreator=null;
		group=null;
		order=null;
		edition=null;
		publication=null;
		from=null;
		to=null;
		advancedfilter=null;
		filter = null;
		onmainpage = null;
		searchindex = null;
		age = null;
		state = null;
		showtemporal = null;
		searchinhistory = null;
		newstype = null;
		persons = null;
		resourcefilter = null;
		
		exactpage = null;
		noticias = null;
	}
	
	protected void openDirectEdit() throws JspException
	{
		
		if (m_directEditMode != CmsDirectEditMode.FALSE)
		{
			CmsDirectEditButtonSelection directEditButtons;
			 if (m_directEditFollowButtons == null) {
	            // this is the first call, calculate the options
	            if (m_directEditLinkForNew == null) {
	                // if create link is null, show only "edit" button for first element
	                directEditButtons = CmsDirectEditButtonSelection.EDIT;
	                // also show only the "edit" button for 2nd to last element
	                m_directEditFollowButtons = directEditButtons;
	            } else {
	                // if create link is not null, show "edit", "delete" and "new" button for first element
	                 directEditButtons = CmsDirectEditButtonSelection.EDIT_DELETE_NEW;
	            	
	                // show "edit" and "delete" button for 2nd to last element
	                m_directEditFollowButtons = CmsDirectEditButtonSelection.EDIT_DELETE;
	            }
	        } else {
	        	directEditButtons = CmsDirectEditButtonSelection.EDIT;
	        }
			 
			CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
			 
			m_directEditOpen = CmsJspTagEditable.startDirectEdit(pageContext, new TfsDirectEditParams(
					   m_cms.getSitePath(m_content.getFile()),
	                   directEditButtons,
	                   m_directEditMode,
	                   m_directEditLinkForNew,
	                   cms.getRequestContext().getUri(), pageContext.getRequest().getServerName() ));
			
		}
	}

	
	protected boolean hasMoreContent() throws JspException {
		
		if (noticias==null)
			return false;
		
		//Si no es el primero
		if (index!=-1 && m_directEditOpen)
			CmsJspTagEditable.endDirectEdit(pageContext);
		
		index++;
		
		if (index<noticias.size())
		{	
			init(noticias.get(index));
			exposeNoticia();
			
			openDirectEdit();
		}
		else
			restoreNoticia();

		return (index<noticias.size());
	}

	
	public I_CmsXmlDocument getXmlDocument() {
		return m_content;
	}

	public Locale getXmlDocumentLocale() {
		return m_contentLocale;
	}

	
	/*GETERS & SETERS*/
	public String getAdvancedfilter() {
		return advancedfilter;
	}

	public void setAdvancedfilter(String advancedfilter) {
		if (advancedfilter==null || advancedfilter.trim().length()==0)
			this.advancedfilter = null;
		else
			this.advancedfilter = advancedfilter;
	}

	
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		if (author==null || author.trim().length()==0)
			this.author = null;
		else
			this.author = author;
	}

	public String getNewscreator() {
		return newscreator;
	}

	public void setNewscreator(String newscreator) {
		if (newscreator==null || newscreator.trim().length()==0)
			this.newscreator = null;
		else
			this.newscreator = newscreator;
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

	public String getEdition() {
		return edition;
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

	public void setEdition(String edition) {
		if (edition==null || edition.trim().length()==0)
			this.edition = null;
		else
			this.edition = edition;
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

	public String getPublication() {
		return publication;
	}

	public void setPublication(String publication) {
		if (publication==null || publication.trim().length()==0)
			this.publication = null;
		else
			this.publication = publication;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		if (section==null || section.trim().length()==0)
			this.section = null;
		else
			this.section = section;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		if (zone==null || zone.trim().length()==0)
			this.zone = null;
		else
			this.zone = zone;
	}

	public String getOnmainpage() {
		return onmainpage;
	}

	public void setOnmainpage(String onmainpage) {
		if (onmainpage==null || onmainpage.trim().length()==0)
			this.onmainpage = null;
		else
			this.onmainpage = onmainpage;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		if (filter==null )
			this.filter = null;
		else
			this.filter = filter;
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

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		if (from==null || from.trim().length()==0)
			this.from = null;
		else
			this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		if (to==null || to.trim().length()==0)
			this.to = null;
		else
			this.to = to;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		if (age==null || age.trim().length()==0)
			this.age = null;
		else
			this.age = age;
	}

	
	public int getIndex() {
		return index;
	}

	public boolean isLast() {
		return (index==noticias.size()-1);
	}

	
	protected void saveNoticia()
    {

		previousListaNoticia = (TfsListaNoticias) pageContext.getRequest().getAttribute("newslist");
    	previousNoticia = (TfsNoticia) pageContext.getRequest().getAttribute("news");
    	
    	pageContext.getRequest().setAttribute("newslist",null);
    	pageContext.getRequest().setAttribute("news",null);
    }
	
    protected void exposeNoticia()
    {
    	
		TfsNoticia noticia = new TfsNoticia(m_cms,m_content,m_contentLocale,pageContext);
		
		TfsNoticia anterior = (TfsNoticia) pageContext.getRequest().getAttribute("news");
		TfsListaNoticias lista = new TfsListaNoticias(this.noticias.size(),this.index+1,this.size, this.page);
		lista.setCurrentsection(noticia.getSection());
		try {
		
			lista.setCurrentPriorityHome(Integer.parseInt(noticia.getPriorityhome()));
		} 
		catch (NumberFormatException e)
		{
			lista.setCurrentPriorityHome(30);				
		}
		try {
				lista.setCurrentprioritysection(Integer.parseInt(noticia.getPrioritysection()));
			} 
			catch (NumberFormatException e)
			{
				lista.setCurrentprioritysection(30);				
			}
		
		if (anterior!=null)
		{
			try {
				lista.setPriorityhomechanged(!anterior.getPriorityhome().equals(noticia.getPriorityhome()));
			} 
			catch (NumberFormatException e)
			{
				lista.setPriorityhomechanged(true);				
			}
			try {
				lista.setPrioritysectionchanged(!anterior.getPrioritysection().equals(noticia.getPrioritysection()));
			} 
			catch (NumberFormatException e)
			{
				lista.setPrioritysectionchanged(true);				
			}

			lista.setSectionchanged(!anterior.getSection().equals(noticia.getSection()));
		}

		pageContext.getRequest().setAttribute("newslist", lista);
		pageContext.getRequest().setAttribute("news", noticia);
    	
		
    }

    protected void restoreNoticia()
    {
    	pageContext.getRequest().setAttribute("newslist", previousListaNoticia );
		pageContext.getRequest().setAttribute("news", previousNoticia);
    }

	public String getEditable() {
		return m_directEditMode != null ? m_directEditMode.toString() : "";
	}

	public void setEditable(String editable) {
		m_directEditMode = CmsDirectEditMode.valueOf(editable);
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
	
	
	private void setValuesDefault(String tipo){
		
		
		ZoneDAO zonaDao = new ZoneDAO();
	   	int publicacionNro = 0;
	   	
	   	try {
	   		if(publication==null){
	   			CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	   			
				TfsPublicacion publicacion = new TfsPublicacion(cms,cms.getRequestContext().getUri());
				publicacionNro = publicacion.getId();
			}else{
				publicacionNro = Integer.parseInt(publication);
			}
	   		
	   		int pageId = 0;
	   		
	   		if(onmainpage.equals("home")) pageId =1;
	   		if(onmainpage.equals("section")) pageId = 2;

			Zona zona = zonaDao.getZone(publicacionNro, pageId , zone);
			
			if(tipo.equals("order")){
				this.order = zona.getOrderDefault();
			}
			
			if(tipo.equals("size")){
				this.size = zona.getSizeDefault();
			}
			
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	public String getState() {
		return state;
	}

	public void setState(String state) {
		if (state==null || state.trim().length()==0)
			this.state = null;
		else
			this.state = state;
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

	
	
	public String getSearchinhistory() {
		return searchinhistory.toString();
	}

	public void setSearchinhistory(String searchinhistory) {
		if (searchinhistory==null || searchinhistory.trim().length()==0)
			this.searchinhistory = null;
		else
			this.searchinhistory = Boolean.parseBoolean(searchinhistory);
	}

	public String getExactpage() {
		return exactpage.toString();
	}
	
	public String getShowtemporal() {
		return showtemporal.toString();
	}

	public void setLogquery(String logquery) {
		if (logquery==null || logquery.trim().length()==0)
			this.logquery = null;
		else
			this.logquery = Boolean.parseBoolean(logquery);
	}
	
	public String getLogquery() {
		return logquery.toString();
	}

	public void setExactpage(String exactpage) {
		if (exactpage==null || exactpage.trim().length()==0)
			this.exactpage = null;
		else
			this.exactpage = exactpage;
		
	}
	
	public void setShowtemporal(String showtemporal) {
		if (showtemporal==null || showtemporal.trim().length()==0)
			this.showtemporal = null;
		else
			this.showtemporal = Boolean.parseBoolean(showtemporal);
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

	public String getNewstype() {
		return newstype;
	}

	public void setNewstype(String newstype) {
		if (newstype==null || newstype.trim().length()==0)
			this.newstype = null;
		else
			this.newstype = newstype;
	}
	public String getPersons() {
		return persons;
	}

	public void setPersons(String persons) {
		if (persons==null || persons.trim().length()==0)
			this.persons = null;
		else
			this.persons = persons;
	}

	@Override
	public String toString() {
		return "TfsNoticiasListTag [section=" + section + ", zone=" + zone
				+ ", category=" + category + ", tags=" + tags + ", size="
				+ size + ", page=" + page + ", author=" + author  + ", newscreator=" + newscreator + ", group="
				+ group + ", order=" + order + ", edition=" + edition
				+ ", publication=" + publication + ", from=" + from + ", to="
				+ to + ", advancedfilter=" + advancedfilter + ", filter="
				+ filter + ", onmainpage=" + onmainpage + ", searchindex="
				+ searchindex + ", age=" + age + ", state=" + state
				+ ", showtemporal=" + showtemporal + ", newstype=" + newstype
				+ ", index=" + index 
				+ ", persons=" + persons + "]";
	}

	private int getMinValueConfig(){
		int rdo= 3;//por defecto es 3
		String siteName = OpenCms.getSiteManager().getCurrentSite(CmsFlexController.getCmsObject(pageContext.getRequest())).getSiteRoot();
    
        	TipoEdicionService tService = new TipoEdicionService();

        	TipoEdicion currentPublication=null;
        	try {
    			currentPublication = tService.obtenerEdicionOnlineRoot(siteName.substring(siteName.lastIndexOf("/")+1));
    		} catch (Exception e) {
    					LOG.error("al intentar traer la publicacion actual "+e.getCause());
    		}
				//currentPublication = tService.obtenerEdicionOnlineRoot(siteName);
			
       	String publication = "" + (currentPublication!=null?currentPublication.getId():"");
    	CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		//rdo=config.getIntegerParam(siteName, publication, "adminNewsConfiguration", "minimunNumberOfSearchCharacters");
		String aux=config.getParam(siteName, publication, "adminNewsConfiguration", "minimunNumberOfSearchCharacters");
		try{
		rdo=Integer.parseInt(aux);
		}catch (NumberFormatException nfe){
			
		}
		return rdo;
	}
	
	public A_NewsCollector getNewCollector(Map<String,Object> parameters, String order) {
		return this.getNewsCollector(parameters,order);
	}
}
