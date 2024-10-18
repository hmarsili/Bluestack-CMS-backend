package com.tfsla.diario.friendlyTags;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.OpenCms;

import com.tfsla.diario.model.TfsListaVideos;
import com.tfsla.diario.model.TfsVideo;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;
import com.tfsla.diario.videoCollector.LuceneVideoCollector;

public class TfsVideosListTag extends BaseTag implements I_TfsCollectionListTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3804852270415205726L;

	TfsVideo video = null;
	TfsVideo previousVideo = null;
	TfsListaVideos previousListaVideo = null;
	
	public static final String param_from="from";
	public static final String param_to="to";
	public static final String param_agency="agency";
	public static final String param_tags="tags";
	public static final String param_category="category";
	public static final String param_classification="classification";
	public static final String param_order="order";
	public static final String param_searchIndex="searchIndex";
	public static final String param_filter="filter";
	public static final String param_advancedFilter="advancedfilter";
	public static final String param_size="size";
	public static final String param_page="page";
	public static final String param_type="type";
	public static final String param_onnews ="onnews";
	public static final String param_publication = "publication";
	public static final String param_formats="formats";
	public static final String param_searchinhistory="searchinhistory";
	public static final String param_numberOfParamters = "params-count";
	public static final String param_id = "id";

	private String from=null;
	private String to=null;
	private String advancedfilter=null;
	private String tags=null;
	private String category=null;
	private String classification=null;
	private int size=10;
	private int page=1;
	private String agency=null;
	private String order=null;
	private String searchindex = null;
	private Boolean searchinhistory = null;
	private String filter = null;
	private String type = null;
	private String onnews = null;
	private String publication = null;
	private String formats=null;
	private String id=null;
	
	List<CmsResource> videos = null;
	int index=-1;
	
	@Override
	public int doStartTag() throws JspException {
		
		init();
		
		findVideos();
				
		return (hasMoreContent() ? EVAL_BODY_INCLUDE : SKIP_BODY );		
	}
	
	protected Map<String,Object> createParameterMap()
	{
		Map<String,Object> parameters = new HashMap<String,Object>();
				
		parameters.put(param_size,size);
		parameters.put(param_page, page);
		parameters.put(param_agency,agency);		
		parameters.put(param_onnews,onnews);
		parameters.put(param_order,order);
		parameters.put(param_filter,filter);
		parameters.put(param_advancedFilter,advancedfilter);
		parameters.put(param_searchIndex,searchindex);
		parameters.put(param_searchinhistory, searchinhistory);
		parameters.put(param_tags,tags);
		parameters.put(param_category,category);		
		parameters.put(param_formats,formats);		
		parameters.put(param_from,from);
		parameters.put(param_to,to);
		parameters.put(param_publication,publication);
		parameters.put(param_type,type);
		parameters.put(param_classification, classification);
		parameters.put(param_id, id);

		int paramsWithValues = 
			1  + //size
			(param_type!=null ? 1 : 0) +
			(onnews!=null ? 1 : 0) +
			(agency!=null ? 1 : 0) +
			(order!=null ? 1 : 0) +
			(filter!=null ? 1 : 0) +
			(advancedfilter!=null ? 1 : 0) +
			(searchindex!=null ? 1 : 0) +
			(searchinhistory!=null ? 1 : 0) +
			
			(category!=null ? 1 : 0) +
			(formats!=null ? 1 : 0) +
			(tags!=null ? 1 : 0) +
			(classification!=null ? 1 : 0) +
			(from!=null ? 1 : 0) +
			(to!=null ? 1 : 0) +
			(publication!=null ? 1 : 0) +
			(id!=null ? 1 : 0) +
			1 ; //page
			

		parameters.put(param_numberOfParamters,(Integer)paramsWithValues);

		return parameters;
	}
	
	protected void findVideos()
	{
		
		videos = null;
		index=-1;
		
		Map<String,Object> parameters = createParameterMap();

		LuceneVideoCollector collector = new LuceneVideoCollector();

		CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

		saveImagen();
		
		videos = collector.collectVideos(parameters,cms);
	}
	
	public boolean hasMoreContent()
	{
		if (videos==null && !videos.isEmpty())
			return false;
		
		index++;
		
		if (index<videos.size())
			exposeImagen();
		else
			restoreImagen();
		
		return (index<videos.size());
		
		
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
		return (index==videos.size()-1);
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

	public String getSearchinhistory() {
		return searchinhistory.toString();
	}

	public void setSearchinhistory(String searchinhistory) {
		if (searchinhistory==null || searchinhistory.trim().length()==0)
			this.searchinhistory = null;
		else
			this.searchinhistory = Boolean.parseBoolean(searchinhistory);
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

	public String getTags() {
		return tags;
	}

	public void setClassification(String classification) {
		if (classification==null || classification.trim().length()==0)
			this.classification = null;
		else
			this.classification = classification;
	}
	
	public String getClassification() {
		return classification;
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
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAgency() {
		return agency;
	}

	public void setAgency(String agency) {
		if (agency==null || agency.trim().length()==0)
			this.agency = null;
		else
			this.agency = agency;
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

	public String getSearchindex() {
		return searchindex;
	}

	public void setSearchindex(String searchindex) {
		if (searchindex==null || searchindex.trim().length()==0)
			this.searchindex = null;
		else
			this.searchindex = searchindex;
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
	
    protected void restoreImagen()
    {
    	pageContext.getRequest().setAttribute("videolist", previousListaVideo );
		pageContext.getRequest().setAttribute("video", previousVideo);
    }
    
    protected void exposeImagen()
    {
    	
		video = new TfsVideo(m_cms,videos.get(index));
		TfsListaVideos lista = new TfsListaVideos(this.videos.size(),this.index+1,this.size,this.page);

		pageContext.getRequest().setAttribute("videolist", lista);
		pageContext.getRequest().setAttribute("video", video);
    	
    }
    
	protected void saveImagen()
    {
		previousListaVideo = (TfsListaVideos) pageContext.getRequest().getAttribute("videolist");
		previousVideo = (TfsVideo) pageContext.getRequest().getAttribute("video");
    	
    	pageContext.getRequest().setAttribute("videolist",null);
    	pageContext.getRequest().setAttribute("video",null);
    }

	
	public int getIndex() {
		return index;
	}

	public TfsVideo getVideo() {
		return video;
	}
	
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

	public String getCollectionValue(String name) throws JspTagException {

		if (name.equals("creationDate"))
			return "" + video.getCreationdate().getTime(); 

		if (name.equals("lastmodifieddate"))
			return "" + video.getLastmodifieddate().getTime(); 

		String elementName = getElementName(name);

		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.title")))
			return (video.getTitle() != null ? video.getTitle() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.description")))
			return (video.getDescription() != null ? video.getDescription() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.rated")))
			return (video.getRated() != null ? video.getRated() : "");
				
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.thumbnail")))
			return (video.getThumbnail() != null ? video.getThumbnail() : "");
				
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.source")))
			return (video.getAgency() != null ? video.getAgency() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.author")))
			return (video.getAuthor() != null ? video.getAuthor() : "");

		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.keywords")))
			return (video.getTags() != null ? video.getTags() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.formats")))
			//return video.getFormatslist().get(getElementIndex(name)-1);
			return (video.getFormats() != null ? video.getFormats() : "");
		

		if (	elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.videoembedded.videocode")) ||
				elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube.id"))
			)
			return (video.getData() != null ? video.getData() : "");

		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.video")))
			return (video.getVideo() != null ? video.getVideo() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.format")))
			return (video.getFormat() != null ? video.getFormat() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.vfspath")))
			return (video.getVfspath() != null ? video.getVfspath() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.duration")))
			return (video.getDuration() != null ? video.getDuration() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.size")))
			return (video.getSize() != null ? video.getSize() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.video.bitrate")))
			return (video.getBitrate() != null ? video.getBitrate() : "");

		if (elementName.contains(TfsXmlContentNameProvider.getInstance().getTagName("news.image.categories")))
			return video.getCategorylist().get(getElementIndex(name)-1);

		return "";
	}
	
	public String getCollectionIndexValue(String name, int index) {
		if (name.contains(TfsXmlContentNameProvider.getInstance().getTagName("news.image.categories")))
		{
			try {
				return video.getCategorylist().get(index);
			}
			catch (IndexOutOfBoundsException e) {
				return "";
			}
		} 

		return null;
	}
	
	public int getCollectionIndexSize(String name, boolean isCollectionPart)
	{
		return video.getCategorylist().size();
		
	}
	
	public String getCollectionPathName() {
		return "";
	}
	

	public String getOnnews() {
		return onnews;
	}

	public void setOnnews(String onnews) {
		if (onnews==null || onnews.trim().length()==0)
			this.onnews = null;
		else
			this.onnews = onnews;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		if (type==null || type.trim().length()==0)
			this.type = null;
		else
			this.type = type;
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

	public void setCategory(String category) {
		if (category==null || category.trim().length()==0)
			this.category = null;
		else
			this.category = category;	
	}
	
	public String getFormats() {
		return formats;
	}

	public void setFormats(String formats) {
		if (formats==null || formats.trim().length()==0)
			this.formats = null;
		else
			this.formats = formats;	
	}
	
	public int getPage() {
		return page;
	}

	public void setPage(int page) {
			this.page = page;
	}

}
