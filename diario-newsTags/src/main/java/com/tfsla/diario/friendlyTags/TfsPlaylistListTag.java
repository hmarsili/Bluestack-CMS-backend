package com.tfsla.diario.friendlyTags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
import org.opencms.xml.I_CmsXmlDocument;

import com.tfsla.diario.model.TfsListaPlaylist;
import com.tfsla.diario.model.TfsPlaylist;
import com.tfsla.diario.playlistCollector.A_playlistCollector;
import com.tfsla.diario.playlistCollector.LucenePlaylistCollector;


public class TfsPlaylistListTag extends A_XmlContentTag implements I_TfsNoticia, I_TfsCollectionListTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5413171197869711892L;
	private static final Log LOG = CmsLog.getLog(TfsPlaylistListTag.class);
	TfsPlaylist previousPlaylist = null;
	TfsListaPlaylist previousListaPlaylist = null;

	//static private List<A_playlistCollector> playlistCollectors = new ArrayList<A_playlistCollector>();
	public static final String param_zone="zone";
	
	public static final String param_category="category";
	public static final String param_size="size";
	public static final String param_page="page";
	public static final String param_order="order";
	public static final String param_edition="edition";
	public static final String param_publication="publication";
	public static final String param_advancedFilter="advancedfilter";
	public static final String param_searchIndex="searchIndex";
	public static final String param_tags="tags";
	public static final String param_from="from";
	public static final String param_to="to";
	public static final String param_state="state";
	public static final String param_numberOfParamters = "params-count";
	public static final String param_showtemporal="showtemporal";
	public static final String param_persons="persons";
	public static final String param_titulo="title";
	public static final String param_cuerpo = "cuerpo";
	public static final String param_automatica ="automatica";
	public static final String param_url="url";
	public static final String param_resourcefilter="resourcefilter";
	//static {
	//	playlistCollectors.add(new LucenePlaylistCollector());
	//}
	
	private String category=null;
	private String tags=null;
	private int size=0;
	private int page=1;
	private String zone= null;
	private String order=null;
	private String edition=null;
	private String publication=null;
	private String from=null;
	private String to=null;
	private String title = null;
	private String cuerpo = null;
	private String advancedfilter=null;
	private String filter = null;
	private String onmainpage = null;
	private String searchindex = null;
	private String age = null;
	private String state = null;
	private Boolean showtemporal = null;
	private String persons = null;
	private String automatica = null;
	private CmsResourceFilter resourcefilter = null;
	
	List<String> playlists = null;
	int index=0;
	
	/*static private A_playlistCollector getPlaylistCollector(Map<String,Object> parameters, String order) {
		A_playlistCollector bestMatchCollector = null;
		
		for (A_playlistCollector collector : playlistCollectors ) {
			if (collector.canCollect(parameters)) {
				if (collector.canOrder(order))
					return collector;
				else if (bestMatchCollector==null)
					bestMatchCollector = collector;
			}
		}
		return bestMatchCollector;
	}*/
	
	
	public int getPage() {
		return page;
	}

	public void setPage(int page) {
			this.page = page;
	}

	@Override
	public int doStartTag() throws JspException {
		
		findPlaylists();
		
		
		if (playlists!=null && playlists.size()>0) {
			init(playlists.get(index));
			exposePlaylist();
			
			return EVAL_BODY_INCLUDE;
		}
		
		return SKIP_BODY;		
	}

	
	protected void findPlaylists() {
		
		playlists = null;
		index = 0;
		Map<String,Object> parameters = createParameterMap();
		A_playlistCollector collector = new LucenePlaylistCollector();//getPlaylistCollector(parameters,order);
		
	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

	    savePlaylist();
	    
		if (collector!=null) {
				playlists = collector.collectPlaylist(parameters,cms);
		}
	}
	
	protected Map<String,Object> createParameterMap() {
		
		if (size==0)
			size=100;
		
		Map<String,Object> parameters = new HashMap<String,Object>();
		
		parameters.put(param_zone,zone);
		parameters.put(param_category,category);
		parameters.put(param_size,size);
		parameters.put(param_order,order);
		parameters.put(param_edition,edition);
		parameters.put(param_publication,publication);
		parameters.put(param_titulo,title);
		parameters.put(param_advancedFilter,advancedfilter);
		parameters.put(param_searchIndex,searchindex);
		parameters.put(param_tags,tags);
		parameters.put(param_from,from);
		parameters.put(param_to,to);
		parameters.put(param_page, page);
		parameters.put(param_state,state);
		parameters.put(param_showtemporal, showtemporal);
		parameters.put(param_automatica, automatica);
		parameters.put(param_persons,persons);
		parameters.put(param_resourcefilter,resourcefilter);
		
		int paramsWithValues = 
			(zone!=null ? 1 : 0) +
			(category!=null ? 1 : 0) +
			1  + //size
			(order!=null ? 1 : 0) +
			(edition!=null ? 1 : 0) +
			(publication!=null ? 1 : 0) +
			(title!=null ? 1 : 0) +
			(cuerpo!=null ? 1 : 0) +
			(advancedfilter!=null ? 1 : 0) +
			(searchindex!=null ? 1 : 0) +
		
			(tags!=null ? 1 : 0) +
			(onmainpage!=null ? 1 : 0) +
			(from!=null ? 1 : 0) +
			(to!=null ? 1 : 0) +
			(age!=null ? 1 : 0) +
			(state!=null ? 1 : 0) +
			(showtemporal!=null ? 1 : 0) +
			(automatica!=null ? 1 : 0) +
			(persons!=null ? 1 : 0) +
			1 ; //page

		parameters.put(param_numberOfParamters,(Integer)paramsWithValues);

		return parameters;
	}
	
	@Override
	public int doAfterBody() throws JspException {
		index++;

		if (index==playlists.size())
			restorePlaylist();

		if (index<playlists.size()) {	
			init(playlists.get(index));
			exposePlaylist();
			return EVAL_BODY_AGAIN;
		}
		
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		
		restorePlaylist();

		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return EVAL_PAGE;
	}
	
	protected boolean hasMoreContent() throws JspException {
		
		if (playlists==null)
			return false;
		
		index++;
		
		if (index<playlists.size()) {	
			init(playlists.get(index));
			exposePlaylist();
		} else
			restorePlaylist();

		return (index<playlists.size());
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

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
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
		return (index==playlists.size()-1);
	}

	
	protected void savePlaylist()
    {

		previousListaPlaylist = (TfsListaPlaylist) pageContext.getRequest().getAttribute("playlistList");
    	previousPlaylist = (TfsPlaylist) pageContext.getRequest().getAttribute("playlist");
    	
    	pageContext.getRequest().setAttribute("playlistList",null);
    	pageContext.getRequest().setAttribute("playlist",null);
    }
	
    protected void exposePlaylist() {
    	
    	TfsPlaylist playlist = new TfsPlaylist(m_cms,m_content,m_contentLocale,pageContext);
		TfsListaPlaylist lista = new TfsListaPlaylist(this.playlists.size(),this.index+1,this.size, this.page);
		lista.setCurrentPriorityZone(playlist.getZonePriority());
		lista.setCurrentZone(playlist.getZone());
	
		pageContext.getRequest().setAttribute("playlistList", lista);
		pageContext.getRequest().setAttribute("playlist", playlist);
    }

    protected void restorePlaylist()  {
    	pageContext.getRequest().setAttribute("playlistList", previousListaPlaylist );
		pageContext.getRequest().setAttribute("playlist", previousPlaylist);
    }
	
	public String getCollectionValue(String name) throws JspTagException {
		try {
			return getXmlDocument().getStringValue(m_cms, name, m_locale);
		} catch (CmsXmlException e) {
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

		public String getShowtemporal() {
		return showtemporal.toString();
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
		return "TfsPlaylistListTag ["
				+ ", category=" + category + ", tags=" + tags + ", size="
				+ size + ", page=" + page + ",  order=" + order + ", edition=" + edition
				+ ", publication=" + publication + ", from=" + from + ", to="
				+ to + ", advancedfilter=" + advancedfilter + ", filter="
				+ filter + ", onmainpage=" + onmainpage + ", searchindex="
				+ searchindex + ", age=" + age + ", state=" + state 
				+ ", showtemporal=" + showtemporal + " , index= " + index 
				+ ", persons=" + persons + "]";
	}

	public String getAutomatica() {
		return automatica;
	}


	public void setAutomatica(String automatica) {
		this.automatica = automatica;
	}
	
	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public String getCuerpo() {
		return cuerpo;
	}


	public void setCuerpo(String cuerpo) {
		this.cuerpo = cuerpo;
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

	
	
}

