package com.tfsla.diario.friendlyTags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.model.TfsListaNoticias;
import com.tfsla.diario.model.TfsNoticia;
import com.tfsla.diario.newsCollector.A_NewsCollector;
import com.tfsla.diario.newsCollector.LuceneNewsCollector;
import com.tfsla.diario.utils.TfsJsonLuceneQueryConverter;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsNoticiasRelacionadasTag extends A_TfsNoticiaResourceCollection
		implements I_TfsCollectionListTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String param_section = "section";
	public static final String param_zone="zone";
	public static final String param_category="category";
	public static final String param_size="size";
	public static final String param_page="page";
	public static final String param_author="author";
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
	public static final String param_searchinhistory="searchinhistory";
	
	
	TfsNoticia previousNoticia = null;
	TfsListaNoticias previousListaNoticia = null;

	//protected int index = 0;
	boolean IsAutomatic = false;

	List<CmsResource> noticias = new ArrayList<CmsResource>();

	private String section = null;
	private String zone= null;
	private String category=null;
	private String tags=null;
	private int page=1;
	private String author=null;
	private String order=null;
	private String edition=null;
	private String publication=null;
	private String from=null;
	private String to=null;
	private String advancedfilter=null;
	private String filter = null;
	private String searchindex = null;
	private String age = null;
	private String state = null;
	private String newstype = null;
	private CmsObject CMS;
	private Boolean searchinhistory = null;
	
	
	private int size = 5;
	String onmainpage = "";
	
	private List<String> news= new ArrayList<String>();

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
		this.onmainpage = onmainpage;
	}
	
	public void setPublication(String publication) {
		this.publication = publication;
	}
	
	public String getPublication() {
		return publication;
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
	
	public String getSearchindex() {
		return searchindex;
	}

	public void setSearchindex(String searchindex) {
		if (searchindex==null || searchindex.trim().length()==0)
			this.searchindex = null;
		else
			this.searchindex = searchindex;
	}

	@Override
	public int doStartTag() throws JspException {

		I_TfsNoticia noticia = getCurrentNews();
		getAutomaticRelated(noticia);
		String query = "";
	    String file, orderBy  = "";		
		int i = 0;
		LOG.info("Noticias relacionadas - QueryBuilder ");
		if (IsAutomatic) {
			
			CmsObject cms = CmsFlexController
					.getCmsObject(pageContext.getRequest());			
			CMS = cms;			
			TfsJsonLuceneQueryConverter converter = new TfsJsonLuceneQueryConverter();			
			file = getQueryRelatedNews(noticia);	
			
			if(file == "" || file == null ){				
				file = getQueryRelatedNewsByDefault();
			}
			
			if( file !=null && !file.equals("")){
			
				file = file.replace("~","");
						
				try {
					
					LOG.info("QueryBuilder noticia: " + file);
					query = converter.convertLuceneQueryByJsonAndFile(cms, file, noticia);
					
					CmsXmlContent resourceDocument = CmsXmlContentFactory.unmarshal(cms, cms.readFile(file));
					List<I_CmsXmlContentValue> orderByValues = resourceDocument.getValues("order", cms.getRequestContext().getLocale());
					
					String historical = resourceDocument.getStringValue(cms,"searchHistorical", cms.getRequestContext().getLocale());
					searchinhistory=historical!=null? historical.equals("true"):false;
					
					searchindex = resourceDocument.getStringValue(cms,"index", cms.getRequestContext().getLocale());
					searchindex = (searchindex != null && searchindex.equals(""))?null:searchindex;
					
					for(I_CmsXmlContentValue orderValue : orderByValues){
						i++;
						orderBy += CmsStringUtil.escapeHtml(orderValue.getStringValue(cms));
						
						if(hasMoreItems(i, orderByValues.size())){
							orderBy += " , ";
		    	    	}
					}						
				} catch (CmsException e) {
					LOG.error("Error al crear la query Lucene por JqueryBuilder - " + file + " - ", e);
				}
			
			}
			
			if( query !=null && !query.equals("")){
				LOG.info("QueryBuilder - " + query);
				findNewsByQuery(query, getCurrentPath(noticia), orderBy);			
			}
			
			if (hasMoreContentAutomatic()) {
				return EVAL_BODY_INCLUDE;
			}

		} else {
			LOG.info("Noticias relacionadas - No entro en QueryBuilder");
			keyControlName = TfsXmlContentNameProvider.getInstance()
					.getTagName("news.relatednews.news"); // noticia

			init(TfsXmlContentNameProvider.getInstance().getTagName(
					"news.relatednews")); // noticiasRelacionadas

			saveNoticia();
			
			int indexNews = 0;
			int sizeNews = lastElement;
			news= new ArrayList<String>();
			
			while (indexNews<=sizeNews) {
				
				String controlValue = getIndexElementValue(noticia,TfsXmlContentNameProvider
						.getInstance()
						.getTagName("news.relatednews.news"),indexNews);
				
				boolean showInHome = true;
				
				if(onmainpage.equals("home")){
					
					showInHome = getShowInHome(noticia, indexNews);
				}
				
				if(controlValue!=null && !controlValue.trim().equals("") && showInHome)
					news.add(controlValue);
				
				indexNews++;
			}
			
			lastElement = news.size();
			
			index = 0;
			
			if (hasMoreContent()) {
				return EVAL_BODY_INCLUDE;
			}
			
		}

		return SKIP_BODY;
	}

	private String getQueryRelatedNewsByDefault() {
		try {
			List<CmsResource> files = CMS.readResources("/system/cmsMedios/relatedNewsQueryBuilder/",CmsResourceFilter.ALL);
			for (CmsResource file : files) {
				String defValue = CMS.readPropertyObject(file, "Title", false).getValue("");
				if(defValue.equals(getEntryDefaultQuery(CMS))){
					return file.getRootPath();
				}
			}
		} catch (CmsException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void initSelectedItems() {
		selectedItems = null;
		if (!item.trim().equals("")) {
			String items[] = item.split(",");
			selectedItems = new ArrayList<Integer>();

			for (String value : items) {
				try {
					int idx = Integer.parseInt(value);
					selectedItems.add(idx);

				} catch (Exception e) {
					LOG.error("Invalidad data format in item", e);
				}
			}
			lastElement = selectedItems.size();
		}
	}
	
	@Override
	public boolean isLast() {
		if (IsAutomatic) {
			   return (index == lastElement);
			}else{
				boolean withElement=false;

				int indexAux=index;
				indexAux++;
				while (indexAux<=lastElement && !withElement) {
					I_TfsNoticia noticia;
					try {
						noticia = getCurrentNews();
					} catch (JspTagException e) {
						return false;
					}
					String controlValue = getIndexElementValue(noticia,keyControlName,indexAux);
					if (!controlValue.trim().equals(""))
						withElement=true;
					else
						indexAux++;
				}
				return (indexAux>lastElement);
			}
	}

	
	@Override
	protected boolean hasMoreContent() {
		
		index++;
		
		if (index <= lastElement)
			initResource(news.get(index-1));
		else
			restoreNoticia();

		return (index <= lastElement);
	}
	
	@Override
	public int doAfterBody() throws JspException {

		if (IsAutomatic) {
			if (hasMoreContentAutomatic()) {
				return EVAL_BODY_AGAIN;
			}
		} else {
			if (hasMoreContent()) {
				return EVAL_BODY_AGAIN;
			}
		}
		if (OpenCms.getSystemInfo().getServletContainerSettings()
				.isReleaseTagsAfterEnd()) {
			release();
		}
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {

		if (OpenCms.getSystemInfo().getServletContainerSettings()
				.isReleaseTagsAfterEnd()) {
			release();
		}

		restoreNoticia();

		return EVAL_PAGE;
	}

	protected void saveNoticia() {
		previousListaNoticia = (TfsListaNoticias) pageContext.getRequest()
				.getAttribute("newslist");
		previousNoticia = (TfsNoticia) pageContext.getRequest().getAttribute(
				"news");

		pageContext.getRequest().setAttribute("newslist", null);
		pageContext.getRequest().setAttribute("news", null);

	}

	protected void restoreNoticia() {
		pageContext.getRequest().setAttribute("newslist", previousListaNoticia);
		pageContext.getRequest().setAttribute("news", previousNoticia);
	}

	protected boolean hasMoreContentAutomatic() {
		index++;

		if (index <= lastElement)
			exposeNoticia(getNoticia());
		else
			restoreNoticia();

		return (index <= lastElement);
	}
	
	private boolean hasMoreItems(int index, int size) {
		if(index != size){
			return true;
		}
		return false;
	}

	protected void exposeNoticia(CmsResource resource) {
		try {
			CmsObject cms = CmsFlexController.getCmsObject(pageContext
					.getRequest());
			CmsFile file = cms.readFile(resource);
			m_content = CmsXmlContentFactory.unmarshal(cms, file,
					pageContext.getRequest());

			if (m_locale == null) {
				m_locale = cms.getRequestContext().getLocale();
			}

			m_contentLocale = m_locale;

			TfsNoticia noticia = new TfsNoticia(cms, m_content,
					m_contentLocale, pageContext);

			TfsListaNoticias lista = new TfsListaNoticias(this.noticias.size(),
					index,size,page);
			lista.setCurrentsection(noticia.getSection());

			pageContext.getRequest().setAttribute("newslist", lista);
			pageContext.getRequest().setAttribute("news", noticia);
		} catch (CmsException e) {
			e.printStackTrace();
		}

	}

	protected void getAutomaticRelated(I_TfsNoticia noticia) {

		I_CmsXmlDocument xmlContent = noticia.getXmlDocument();
		CmsObject cms = CmsFlexController
				.getCmsObject(pageContext.getRequest());

		boolean relatedAutomatic = false;

		try {
			String relatedAutomaticValue = xmlContent.getStringValue(
					cms,
					TfsXmlContentNameProvider.getInstance().getTagName(
							"news.relatednews.automatic"),
					noticia.getXmlDocumentLocale());
			if (relatedAutomaticValue != null) {
				relatedAutomatic = relatedAutomaticValue.trim().toLowerCase()
						.equals("true");
			}
		} catch (CmsXmlException e) {
			e.printStackTrace();
		}

		IsAutomatic = relatedAutomatic;
	}
	
	protected String getQueryRelatedNews(I_TfsNoticia noticia) {

		I_CmsXmlDocument xmlContent = noticia.getXmlDocument();
		CmsObject cms = CmsFlexController
				.getCmsObject(pageContext.getRequest());

		String relatedAutomaticValue = "";
		
		try {
			relatedAutomaticValue = xmlContent.getStringValue(
					cms,
					TfsXmlContentNameProvider.getInstance().getTagName(
							"news.relatednews.advancedQuery"),
					noticia.getXmlDocumentLocale());
			
		} catch (CmsXmlException e) {
			e.printStackTrace();
		}

		return relatedAutomaticValue;
	}
	
	protected boolean getShowInHome(I_TfsNoticia noticia, int index) {

		I_CmsXmlDocument xmlContent = noticia.getXmlDocument();
		CmsObject cms = CmsFlexController
				.getCmsObject(pageContext.getRequest());

		boolean showInHome = true;

		try {
			String showInHomeValue = xmlContent.getStringValue(
					cms,
					TfsXmlContentNameProvider.getInstance().getTagName(
							"news.relatednews")+"["+index+"]/"+TfsXmlContentNameProvider.getInstance().getTagName(
							"news.relatednews.showHome"),
					noticia.getXmlDocumentLocale());
			if (showInHomeValue != null) {
				showInHome = showInHomeValue.trim().toLowerCase()
						.equals("true");
			}
		} catch (CmsXmlException e) {
			e.printStackTrace();
		}

		return showInHome;
	}

	protected String getTags(I_TfsNoticia noticia) {

		I_CmsXmlDocument xmlContent = noticia.getXmlDocument();
		CmsObject cms = CmsFlexController
				.getCmsObject(pageContext.getRequest());

		String tags = null;

		try {
			tags = xmlContent.getStringValue(cms, TfsXmlContentNameProvider
					.getInstance().getTagName("news.keywords"), noticia
					.getXmlDocumentLocale());
			return tags;
		} catch (CmsXmlException e) {
			e.printStackTrace();
		}

		return tags;
	}

	protected String getCurrentPath(I_TfsNoticia noticia) {

		I_CmsXmlDocument xmlContent = noticia.getXmlDocument();
		CmsFile file = xmlContent.getFile();

		return file.getRootPath();

	}

	protected void findNewsByQuery(String advancedQuery, String currentPath, String orderBy) {
		saveNoticia();
		
		index = 0;
		lastElement =0;
		noticias = new ArrayList<CmsResource>();

		CmsObject cms = CmsFlexController
				.getCmsObject(pageContext.getRequest());

		A_NewsCollector collector = new LuceneNewsCollector();

		Map<String, Object> parameters = new HashMap<String, Object>();	

		advancedfilter = advancedQuery;

		parameters.put(param_section, section);
		parameters.put(param_zone,zone);
		parameters.put(param_category,category);
		parameters.put(param_size,size + 1);
		parameters.put(param_author,author);
		if(orderBy != ""){
			parameters.put(param_order,orderBy);
		}else{
			parameters.put(param_order,"user-modification-date desc");
		}
		
		parameters.put(param_edition,edition);
		parameters.put(param_publication,publication);
		parameters.put(param_filter,null);
		parameters.put(param_advancedFilter,advancedfilter);
		parameters.put(param_searchIndex,searchindex);
		parameters.put(param_tags, null);
		parameters.put(param_onmainpage, onmainpage);
		parameters.put(param_from,from);
		parameters.put(param_to,to);
		parameters.put(param_age,age);
		parameters.put(param_page, page);
		parameters.put(param_state,state);
		parameters.put(param_newstype,newstype);
		parameters.put(param_searchinhistory, searchinhistory);
		
		int paramsWithValues = 
			(section!=null ? 1 : 0) +
			(zone!=null ? 1 : 0) +
			(category!=null ? 1 : 0) +
			1  + //size
			(author!=null ? 1 : 0) +
			(order!=null ? 1 : 0) +
			(edition!=null ? 1 : 0) +
			(publication!=null ? 1 : 0) +
			0 + //(filter!=null ? 1 : 0) +
			(advancedfilter!=null ? 1 : 0) +
			(searchindex!=null ? 1 : 0) +
		
			0 + //(tags!=null ? 1 : 0) +
			(onmainpage!=null ? 1 : 0) +
			(from!=null ? 1 : 0) +
			(to!=null ? 1 : 0) +
			(age!=null ? 1 : 0) +
			(newstype!=null ? 1 : 0) +
			(state!=null ? 1 : 0) +
			(searchinhistory!=null ? 1 : 0) +
			
			1 ; //page

		parameters.put(param_numberOfParamters,(Integer)paramsWithValues);

		if (size>0) {
			List<CmsResource> news = collector.collectNews(parameters, cms);
	
			Iterator<CmsResource> iter = news.iterator();
	
			while (iter.hasNext()) {
				CmsResource noticia = (CmsResource) iter.next();
	
				if (!currentPath.equals(noticia.getRootPath()) && noticias.size()<size)
					noticias.add(noticia);
			}
		}

		lastElement = noticias.size();

	}

	public CmsResource getNoticia() {
		return noticias.get(index-1);
	}
	
	public int getIndex() {
		return index-1;
	}

	public void setIndex(int index) {
		this.index = index;
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
	
	protected boolean isExternalNews(String elementName){
		return true;
	}
	
	@Override
	public String getCollectionPathName() {
		return "";
	}
	
	@Override
	public int getCollectionIndexSize(String name, boolean isCollectionPart) throws JspTagException {
		
		if (name.equals(""))
			return super.getCollectionIndexSize(name,isCollectionPart);
		
		return getXmlDocument().getValues(name, m_locale).size();
	}

	@Override
	public String getCollectionIndexValue(String name, int index) {
		try {
    	    // get the current users OpenCms context
    	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

			return getXmlDocument().getStringValue(cms, name, m_locale, index);
		} catch (CmsXmlException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String getEntryDefaultQuery(CmsObject cmsObject)
    {
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
    	String siteName = OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot();
    	String publicationName = getPublicationName(cmsObject);
    	String defaultValue = "";
    	
    	defaultValue = config.getParam(siteName, publicationName, "queryBuilder", "defaultQueryName");
    	
    	if(defaultValue.isEmpty()){
    		defaultValue = config.getParam(siteName, "", "queryBuilder", "defaultQueryName");
    	}

    	return defaultValue;
    }

	public String getCollectionValue(String name) throws JspTagException {
		try {
    	    // get the current users OpenCms context
    	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

    	    return getXmlDocument().getStringValue(cms, name, m_locale);
		} catch (CmsXmlException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String getPublicationName(CmsObject cms)
	{
		return getPublicationName(cms, cms.getRequestContext().getUri());
	}
	
	public static String getPublicationName(CmsObject cms, String path)
	{
		try
		{
			TipoEdicionService tService = new TipoEdicionService();
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, path);
			return ""+tEdicion.getId();//.getNombre();
		}
		catch(Exception ex)
		{
			return "online";
		}		
	}

}