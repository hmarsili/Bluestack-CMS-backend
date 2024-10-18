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

import com.tfsla.diario.audioCollector.LuceneAudioCollector;
import com.tfsla.diario.model.TfsAudio;
import com.tfsla.diario.model.TfsListaAudios;
import com.tfsla.diario.model.TfsListaVideos;
import com.tfsla.diario.model.TfsVideo;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;
import com.tfsla.diario.videoCollector.LuceneVideoCollector;

public class TfsAudiosListTag extends BaseTag implements I_TfsCollectionListTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3804852270415205726L;

	TfsAudio audio = null;
	TfsAudio previousAudio = null;
	TfsListaAudios previousListaAudio = null;
	
	public static final String param_from="from";
	public static final String param_to="to";
	public static final String param_agency="agency";
	public static final String param_tags="tags";
	public static final String param_category="category";
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
	public static final String param_numberOfParamters = "params-count";

	private String from=null;
	private String to=null;
	private String advancedfilter=null;
	private String tags=null;
	private String category=null;
	private int size=10;
	private int page=1;
	private String agency=null;
	private String order=null;
	private String searchindex = null;
	private String filter = null;
	private String type = null;
	private String onnews = null;
	private String publication = null;
	
	List<CmsResource> audios = null;
	int index=-1;
	
	@Override
	public int doStartTag() throws JspException {
		
		init();
		
		findAudios();
				
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
		parameters.put(param_tags,tags);
		parameters.put(param_category,category);		
		//parameters.put(param_formats,formats);		
		parameters.put(param_from,from);
		parameters.put(param_to,to);
		parameters.put(param_publication,publication);
		parameters.put(param_type,type);
		//parameters.put(param_classification, classification);

		int paramsWithValues = 
			1  + //size
			(param_type!=null ? 1 : 0) +
			(onnews!=null ? 1 : 0) +
			(agency!=null ? 1 : 0) +
			(order!=null ? 1 : 0) +
			(filter!=null ? 1 : 0) +
			(advancedfilter!=null ? 1 : 0) +
			(searchindex!=null ? 1 : 0) +

			(category!=null ? 1 : 0) +
			//(formats!=null ? 1 : 0) +
			(tags!=null ? 1 : 0) +
			//(classification!=null ? 1 : 0) +
			(from!=null ? 1 : 0) +
			(to!=null ? 1 : 0) +
			(publication!=null ? 1 : 0) +
			1 ; //page
			

		parameters.put(param_numberOfParamters,(Integer)paramsWithValues);

		return parameters;
	}
	
	protected void findAudios()
	{
		
		audios = null;
		index=-1;
		
		Map<String,Object> parameters = createParameterMap();

		LuceneAudioCollector collector = new LuceneAudioCollector();

		CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

		saveAudio();
		
		audios = collector.collectAudios(parameters,cms);
	}
	
	public boolean hasMoreContent()
	{
		if (audios==null && !audios.isEmpty())
			return false;
		
		index++;
		
		if (index<audios.size())
			exposeAudio();
		else
			restoreAudio();
		
		return (index<audios.size());
		
		
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
		return (index==audios.size()-1);
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

//	public void setClassification(String classification) {
//		if (classification==null || classification.trim().length()==0)
//			this.classification = null;
//		else
//			this.classification = classification;
//	}
	
//	public String getClassification() {
//		return classification;
//	}

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
	
    protected void restoreAudio()
    {
    	pageContext.getRequest().setAttribute("audiolist", previousListaAudio );
		pageContext.getRequest().setAttribute("audio", previousAudio);
    }
    
    protected void exposeAudio()
    {
    	
		audio = new TfsAudio(m_cms,audios.get(index));
		TfsListaAudios lista = new TfsListaAudios(this.audios.size(),this.index+1,this.size,this.page);

		pageContext.getRequest().setAttribute("audiolist", lista);
		pageContext.getRequest().setAttribute("audio", audio);
    	
    }
    
	protected void saveAudio()
    {
		previousListaAudio = (TfsListaAudios) pageContext.getRequest().getAttribute("audiolist");
		previousAudio = (TfsAudio) pageContext.getRequest().getAttribute("audio");
    	
    	pageContext.getRequest().setAttribute("audiolist",null);
    	pageContext.getRequest().setAttribute("audio",null);
    }

	
	public int getIndex() {
		return index;
	}

	public TfsAudio getVideo() {
		return audio;
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
			return "" + audio.getCreationdate().getTime(); 

		if (name.equals("lastmodifieddate"))
			return "" + audio.getLastmodifieddate().getTime(); 

		String elementName = getElementName(name);

		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.title")))
			return (audio.getTitle() != null ? audio.getTitle() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.description")))
			return (audio.getDescription() != null ? audio.getDescription() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.rated")))
			return (audio.getRated() != null ? audio.getRated() : "");
				
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.thumbnail")))
			return (audio.getThumbnail() != null ? audio.getThumbnail() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.source")))
			return (audio.getAgency() != null ? audio.getAgency() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.author")))
			return (audio.getAuthor() != null ? audio.getAuthor() : "");

		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.keywords")))
			return (audio.getTags() != null ? audio.getTags() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.audio")))
			return (audio.getAudio() != null ? audio.getAudio() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.audiopath")))
			return (audio.getAudio() != null ? audio.getAudio() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.duration")))
			return (audio.getDuration() != null ? audio.getDuration() : "");
		
		if (elementName.contains(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.categories")))
			return audio.getCategorylist().get(getElementIndex(name)-1);
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.audio.vfspath")))
			return (audio.getVfspath() != null ? audio.getVfspath() : "");

		return "";
	}
	
	public String getCollectionIndexValue(String name, int index) {
		if (name.contains(TfsXmlContentNameProvider.getInstance().getTagName("news.image.categories")))
		{
			try {
				return audio.getCategorylist().get(index);
			}
			catch (IndexOutOfBoundsException e) {
				return "";
			}
		} 

		return null;
	}
	
	public int getCollectionIndexSize(String name, boolean isCollectionPart)
	{
		return audio.getCategorylist().size();
		
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
	
//	public String getFormats() {
//		return formats;
//	}

//	public void setFormats(String formats) {
//		if (formats==null || formats.trim().length()==0)
//			this.formats = null;
//		else
//			this.formats = formats;	
//	}
	
	public int getPage() {
		return page;
	}

	public void setPage(int page) {
			this.page = page;
	}

}
