package com.tfsla.diario.friendlyTags;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.diario.model.TfsTwitterFeed;
import com.tfsla.diario.twitterFeedCollector.LuceneTwitterFeedCollector;


public class TfsTwitterFeedListTag extends BaseTag implements I_TfsCollectionListTag {

	/**
	 *
	 */
	private static final long serialVersionUID = 5282721382489844149L;

	protected static final Log LOG = CmsLog.getLog(TfsTwitterFeedListTag.class);

	private CmsObject cms = null;

	TfsTwitterFeed previousFeed = null;
	TfsTwitterFeed feed = null;

	public static final String param_name = "name";
	public static final String param_tags = "tags";
	public static final String param_size = "size";
	public static final String param_searchIndex = "searchIndex";
	public static final String param_publication = "publication";
	public static final String param_categories = "category";
	public static final String param_numberOfParamters = "params-count";

	private int size=1;
	private String name = null;
	private String tags=null;
	private String category=null;
	private String searchindex = null;
	private String publication = null;

	List<CmsResource> feeds = null;
	int index=-1;

	@Override
	public int doStartTag() throws JspException {

		init();

		findFeeds();

		return (hasMoreContent() ? EVAL_BODY_INCLUDE : SKIP_BODY );	
	}

	protected Map<String,Object> createParameterMap()
	{
		Map<String,Object> parameters = new HashMap<String,Object>();

		parameters.put(param_size,size);
		parameters.put(param_name,name);	
		parameters.put(param_searchIndex,searchindex);
		parameters.put(param_tags,tags);
		parameters.put(param_categories,category);	
		parameters.put(param_publication,publication);
		int paramsWithValues =
				1 + //size
				(name!=null ? 1 : 0) +
				(searchindex!=null ? 1 : 0) +

				(category!=null ? 1 : 0) +
				(tags!=null ? 1 : 0) +
				(publication!=null ? 1 : 0);

		parameters.put(param_numberOfParamters,(Integer)paramsWithValues);

		return parameters;
	}

	protected void findFeeds()
	{

		feeds = null;
		index=-1;

		Map<String,Object> parameters = createParameterMap();

		LuceneTwitterFeedCollector collector = new LuceneTwitterFeedCollector();

		cms = CmsFlexController.getCmsObject(pageContext.getRequest());

		saveFeed();

		feeds = collector.collectFeeds(parameters,cms);
	}

	public boolean hasMoreContent()
	{
		if (feeds==null)
			return false;

		index++;

		if (index<feeds.size())
			exposeFeed();
		else
			restoreFeed();

		return (index<feeds.size());


	}

	protected void restoreFeed()
	{
		pageContext.getRequest().setAttribute("feed", previousFeed);
	}

	protected void exposeFeed()
	{
		feed = new TfsTwitterFeed(cms,feeds.get(index));
		pageContext.getRequest().setAttribute("feed", feed);

	}

	protected void saveFeed()
	{
		previousFeed = (TfsTwitterFeed) pageContext.getRequest().getAttribute("feed");
		pageContext.getRequest().setAttribute("feed",null);
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

	public boolean isLast() {
		return (index==feeds.size()-1);
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

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
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

	public String getPublication() {
		return publication;
	}

	public void setPublication(String publication) {
		if (publication==null || publication.trim().length()==0)
			this.publication = null;
		else
			this.publication = publication;
	}

	public String getCategory() {
		return category;
	}

	public void setName(String name) {
		if (name==null || name.trim().length()==0)
			this.name = null;
		else
			this.name = name;	
	}

	public String getName() {
		return name;
	}

	public void setCategory(String category) {
		if (category==null || category.trim().length()==0)
			this.category = null;
		else
			this.category = category;	
	}

	public String getCollectionPathName() {
		return "";
	}

	public int getIndex() {
		return index;
	}

	public String getCollectionValue(String name) throws JspTagException {
		/*
LOG.debug("TfsImagenesListTag - getCollectionValue (name:" + name + ")");

if (name.equals("creationDate"))
return "" + imagen.getCreationdate().getTime();

if (name.equals("lastmodifieddate"))
return "" + imagen.getLastmodifieddate().getTime();

String elementName = getElementName(name);
if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.image.description"))) //"descripcion"
return (imagen.getTitle()!=null ? imagen.getTitle() : "");

if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.image.image")))
return m_cms.getRequestContext().removeSiteRoot(imagenes.get(index).getRootPath());
if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.image.source")))
return (imagen.getAgency()!=null ? imagen.getAgency() : "");

if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.image.keywords")))
return (imagen.getTags()!=null ? imagen.getTags() : "");

if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.image.source")))
return (imagen.getAuthor()!=null ? imagen.getAuthor() : "");

if (elementName.contains(TfsXmlContentNameProvider.getInstance().getTagName("news.image.categories")))
return imagen.getCategorylist().get(getElementIndex(name)-1);
		 */
		return null;
	}

	/*
private int getElementIndex(String elementName)
{
String[] parts = elementName.split("/");
String element = parts[parts.length-1];
int start = element.indexOf("[")+1;
int end = element.indexOf("]");
return Integer.parseInt(element.substring(start, end));
}

private String getElementName(String elementName)
{
String[] parts = elementName.split("/");
String element = parts[parts.length-1];
int end = element.indexOf("[");
return element.substring(0, end);
}
	 */
	public String getCollectionIndexValue(String name, int index) {
		//LOG.debug("TfsImagenesListTag - getCollectionIndexValue (name:" + name + " - index:" + index + ")");
		//if (name.contains(TfsXmlContentNameProvider.getInstance().getTagName("news.image.categories")))
		//{
		// try {
		// return imagen.getCategorylist().get(index);
		// }
		// catch (IndexOutOfBoundsException e) {
		// return "";
		// }
		//}

		return null;
	}

	public int getCollectionIndexSize(String name, boolean isCollectionPart)
	{
		// return imagen.getCategorylist().size();
		return 0;
	}

}