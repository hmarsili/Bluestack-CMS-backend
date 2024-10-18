package com.tfsla.diario.friendlyTags;

import java.util.ArrayList;
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
import org.opencms.xml.CmsXmlException;

import com.tfsla.diario.triviasCollector.A_TriviasCollector;
import com.tfsla.diario.triviasCollector.LuceneTriviaCollector;
import com.tfsla.diario.model.TfsTrivia;
import com.tfsla.diario.model.TfsListaTrivias;

public class TfsTriviasListTag extends A_XmlContentTag implements I_TfsTrivia, I_TfsCollectionListTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1984143153049823965L;

	private static final Log LOG = CmsLog.getLog(TfsTriviasListTag.class);
	TfsTrivia previousTrivia = null;
	TfsListaTrivias previousListaTrivias = null;

	static private List<A_TriviasCollector> triviaCollectors = new ArrayList<A_TriviasCollector>();

	
	public static final String param_category="category";
	public static final String param_tags="tags";

	public static final String param_size="size";
	public static final String param_page="page";
	public static final String param_order="order";
	public static final String param_title ="title";
	public static final String param_description  = "description";
	
	public static final String param_numberOfParamters = "params-count";
	public static final String param_advancedFilter="advancedfilter";
	public static final String param_searchIndex="searchIndex";
	public static final String param_publication="publication";
	public static final String param_fromStartDate="fromStartDate";
	public static final String param_toStartDate="toStartDate";
	public static final String param_fromCloseDate="fromCloseDate";
	public static final String param_toCloseDate="toCloseDate";
	
	public static final String param_status ="status";
	public static final String param_author ="author";
	public static final String param_multipleGame ="multipleGame";
	public static final String param_storeResults ="storeResults";
	public static final String param_registeredUser ="registeredUser";
	public static final String param_resultType = "resultsType";
	
	public static final String param_showtemporal="showtemporal";
	

	static {
		triviaCollectors.add(new LuceneTriviaCollector());
	}
	
	CmsObject cms = null;
	private String url = null;
	
	private String showresult="";
	
	private String tags=null;
	private String category=null;
	
	private String size="10";
	private String page="1";
	private String order=null;

	private String advancedfilter=null;
	private String searchindex = null;
	private String publication=null;
	
	private String fromStartDate=null;
	private String toStartDate=null;
	private String fromCloseDate=null;
	private String toCloseDate=null;
	
	private String title=null;
	private String description=null;
	
	private String status=null;
	private String author=null;
	private Boolean multipleGame = null;
	private Boolean storeResults = null;
	private Boolean registeredUser = null;
	private String resultsType = null;
	
	private List<String> trivias=null;
	private int index = 0;
	
	private Boolean showtemporal = null;
	
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

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		if (category!=null && category.equals(""))
			category=null;
	
		this.category = category;
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

	public String getFromStartDate() {
		return fromStartDate;
	}
	
	public String getToStartDate() {
		return toStartDate;
	}

	public void setFromStartDate(String fromStartDate) {
		if (fromStartDate!= null && fromStartDate.equals(""))
			fromStartDate =null;
	
		this.fromStartDate = fromStartDate;
	}
	
	public void setToStartDate(String toStartDate) {
		if (toStartDate!= null && toStartDate.equals(""))
			toStartDate =null;
	
		this.toStartDate = toStartDate;
	}

	public String getFromCloseDate() {
		return fromCloseDate;
	}
	
	public String getToCloseDate() {
		return toCloseDate;
	}

	public void setToCloseDate(String toCloseDate) {
		if (toCloseDate!= null && toCloseDate.equals(""))
			toCloseDate =null;
		this.toCloseDate = toCloseDate;
	}
	
	public void setFromCloseDate(String fromCloseDate) {
		if (fromCloseDate!= null && fromCloseDate.equals(""))
			fromCloseDate =null;
		this.fromCloseDate = fromCloseDate;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		if (title!= null && title.equals(""))
			title =null;
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		if (description!= null && description.equals(""))
			description =null;
		this.description = description;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		if (status!= null && status.equals(""))
			status =null;
		this.status = status;
	}
	
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		if (author!= null && author.equals(""))
			author =null;
		this.author = author;
	}

	public String getMultipleGame() {
		return multipleGame.toString();
	}

	public void setMultipleGame(String multipleGame) {
		if (multipleGame==null || multipleGame.trim().length()==0)
			this.multipleGame = null;
		else
			this.multipleGame = Boolean.parseBoolean(multipleGame);
	}
	
	public String getStoreResults() {
		return storeResults.toString();
	}

	public void setStoreResults(String storeResults) {
		if (storeResults==null || storeResults.trim().length()==0)
			this.storeResults = null;
		else
			this.storeResults = Boolean.parseBoolean(storeResults);
	}
	
	public String getRegisteredUser() {
		return registeredUser.toString();
	}

	public void setRegisteredUser(String registeredUser) {
		if (registeredUser==null || registeredUser.trim().length()==0)
			this.registeredUser = null;
		else
			this.registeredUser = Boolean.parseBoolean(registeredUser);
	}
	
	public void setResultsType(String resultsType) {
		if (resultsType!= null && resultsType.equals(""))
			resultsType =null;
		this.resultsType = resultsType;
	}
	
	public String getResultsType() {
		return resultsType;
	}

	public List<String> getTrivias() {
		return trivias;
	}

	public void setTrivias(List<String> trivias) {
		this.trivias = trivias;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	
	static private A_TriviasCollector getTriviaCollector(Map<String,Object> parameters, String order)
	{
		A_TriviasCollector bestMatchCollector = null;
		
		for (A_TriviasCollector collector : triviaCollectors)
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
	
	public void exposeTrivia() {
		TfsTrivia trivia = new TfsTrivia(m_cms,m_content,m_contentLocale,pageContext);
		
		TfsListaTrivias listaTrivias = new TfsListaTrivias(this.trivias.size(),this.index+1,Integer.parseInt(this.size),Integer.parseInt( this.page));
	    
		pageContext.getRequest().setAttribute("trivia", trivia);
		pageContext.getRequest().setAttribute("triviaList", listaTrivias);
		
	}
	
	
	@Override
	public int doStartTag() throws JspException {
			
		index = 0;
		
	    cms = CmsFlexController.getCmsObject(pageContext.getRequest());

    	findTrivias();
    				
		if (index<trivias.size()) {
			init(trivias.get(index));
			exposeTrivia();
			return EVAL_BODY_INCLUDE;
		}
    return SKIP_BODY;
	}
	
	
	@Override
	public int doAfterBody() throws JspException {

		index++;

		if (index==trivias.size())
			restoreTrivia();
		
		if (index<trivias.size()) {
			init(trivias.get(index));
			exposeTrivia();
			return EVAL_BODY_AGAIN;
		}
		
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return SKIP_BODY;
	}

   protected void restoreTrivia() {
    	pageContext.getRequest().setAttribute("trivia", previousTrivia);
       	pageContext.getRequest().setAttribute("triviaList", previousListaTrivias );
    }
	
	@Override
	public int doEndTag() {
		
		restoreTrivia();

		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return EVAL_PAGE;
	}
	
	protected void findTrivias() {
		
		trivias = null;
		index=0;
		Map<String,Object> parameters = createParameterMap();
		A_TriviasCollector collector = getTriviaCollector(parameters,order);
		
	 	previousTrivia = (TfsTrivia) pageContext.getRequest().getAttribute("trivia");
    	pageContext.getRequest().setAttribute("trivia",null);

		if (collector!=null)
			trivias = collector.collectTrivias(parameters,cms);

	}
	
	protected Map<String,Object> createParameterMap()
	{
		Map<String,Object> parameters = new HashMap<String,Object>();
		
		parameters.put(param_size,Integer.parseInt(size));
		parameters.put(param_page,Integer.parseInt(page));
		parameters.put(param_order,order);
		parameters.put(param_advancedFilter,advancedfilter);
		parameters.put(param_searchIndex,searchindex);
		parameters.put(param_publication,publication);
		
		parameters.put(param_tags,tags);
		parameters.put(param_title,title);
		parameters.put(param_category,category);
		
		parameters.put(param_description,description);
		
		parameters.put(param_fromStartDate,fromStartDate);
		parameters.put(param_toStartDate,toStartDate);
		parameters.put(param_fromCloseDate,fromCloseDate);
		parameters.put(param_toCloseDate,toCloseDate);
		parameters.put(param_status,status);
		parameters.put(param_author,author);
		parameters.put(param_multipleGame, multipleGame);
		parameters.put(param_storeResults,storeResults);
		parameters.put(param_registeredUser, registeredUser);
		parameters.put(param_showtemporal, showtemporal);
		parameters.put(param_resultType, resultsType);
		
		int paramsWithValues = 
			(category!=null ? 1 : 0) +
			1  + //size
			1  + //page
			(order!=null ? 1 : 0) +
			(advancedfilter!=null ? 1 : 0) +
			(searchindex!=null ? 1 : 0) +
			(publication!=null ? 1 : 0) +

			(fromStartDate!=null ? 1 : 0) +
			(toStartDate!=null ? 1 : 0) +
			(fromCloseDate!=null ? 1 : 0) +
			(toCloseDate!=null ? 1 : 0) +
			(description!=null ? 1 : 0) +
			(tags!=null ? 1 : 0) +
			(title!=null ? 1 : 0) +
			(status!=null ? 1 : 0) +
			(author!=null ? 1 : 0) +
			(multipleGame!=null ? 1 : 0) +
			(storeResults!=null ? 1 : 0) +
			(registeredUser!=null ? 1 : 0) +
			(resultsType!=null ? 1 : 0) +
			
			(showtemporal!=null ? 1 : 0) +
			
			(tags!=null ? 1 : 0);

		parameters.put(param_numberOfParamters,(Integer)paramsWithValues);

		return parameters;
	}

	public int getIndex() {
		return index;
	}

	public boolean isLast() {
		return (index==trivias.size()-1);
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

}
