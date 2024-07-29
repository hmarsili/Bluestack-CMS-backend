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

import com.tfsla.diario.imageCollector.LuceneImageCollector;
import com.tfsla.diario.model.TfsListaImagenes;
import com.tfsla.diario.model.TfsImagen;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsImagenesListTag extends BaseTag implements I_TfsCollectionListTag {

    protected static final Log LOG = CmsLog.getLog(TfsImagenesListTag.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = -3804852270415205726L;


	TfsImagen imagen = null;
	TfsImagen previousImagen = null;
	TfsListaImagenes previousListaImagen = null;
	
	public static final String param_searchinhistory="searchinhistory";
	public static final String param_from="from";
	public static final String param_to="to";
	public static final String param_author="author";
	public static final String param_agency="agency";
	public static final String param_tags="tags";
	public static final String param_category="category";
	public static final String param_includeCropped = "includeCropped";
	public static final String param_originalImage="originalImage";
	public static final String param_order="order";
	public static final String param_searchIndex="searchIndex";
	public static final String param_filter="filter";
	public static final String param_advancedFilter="advancedfilter";
	public static final String param_size="size";
	public static final String param_onnews = "onnews";
	public static final String param_publication = "publication";
	public static final String param_numberOfParamters = "params-count";
	public static final String param_page="page";

	private String from=null;
	private String to=null;
	private String advancedfilter=null;
	private String tags=null;
	private String category=null;
	private int size=10;
	private int page=1;
	private String author=null;
	private String agency=null;
	private Boolean includeCropped= null;
	private String originalImage = null;
	private String order=null;
	private String searchindex = null;
	private String filter = null;
	private String onnews = null;
	private String publication = null;
	private Boolean searchinhistory = null;
	
	List<CmsResource> imagenes = null;
	int index=-1;
	
	@Override
	public int doStartTag() throws JspException {
		
		init();
		
		findImages();
				
		return (hasMoreContent() ? EVAL_BODY_INCLUDE : SKIP_BODY );		
	}
	
	protected Map<String,Object> createParameterMap()
	{
		Map<String,Object> parameters = new HashMap<String,Object>();
		
		parameters.put(param_size,size);
		parameters.put(param_agency,agency);		
		parameters.put(param_author,author);
		parameters.put(param_order,order);
		parameters.put(param_filter,filter);
		parameters.put(param_advancedFilter,advancedfilter);
		parameters.put(param_searchIndex,searchindex);
		parameters.put(param_tags,tags);
		parameters.put(param_category,category);		
		parameters.put(param_from,from);
		parameters.put(param_to,to);
		parameters.put(param_onnews,onnews);
		parameters.put(param_publication,publication);
		parameters.put(param_page,page);
		parameters.put(param_searchinhistory, searchinhistory);
		parameters.put(param_includeCropped, includeCropped);
		parameters.put(param_originalImage, originalImage);
		int paramsWithValues = 
			1  + //size
			1  + //page
			(onnews!=null ? 1 : 0) +
			(author!=null ? 1 : 0) +
			(agency!=null ? 1 : 0) +
			(order!=null ? 1 : 0) +
			(filter!=null ? 1 : 0) +
			(advancedfilter!=null ? 1 : 0) +
			(searchindex!=null ? 1 : 0) +
			(searchinhistory!=null ? 1 : 0) +
			(category!=null ? 1 : 0) +
			(tags!=null ? 1 : 0) +
			(from!=null ? 1 : 0) +
			(to!=null ? 1 : 0) +
			(originalImage!=null ? 1 : 0) +
			(includeCropped!=null ? 1 : 0) +
			(publication!=null ? 1 : 0);

		parameters.put(param_numberOfParamters,(Integer)paramsWithValues);

		return parameters;
	}
	
	protected void findImages()
	{
		
		imagenes = null;
		index=-1;
		
		Map<String,Object> parameters = createParameterMap();

		LuceneImageCollector collector = new LuceneImageCollector();

		CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

		saveImagen();
		
		imagenes = collector.collectImages(parameters,cms);
	}
	
	public boolean hasMoreContent()
	{
		if (imagenes==null)
			return false;
		
		index++;
		
		if (index<imagenes.size())
			exposeImagen();
		else
			restoreImagen();
		
		return (index<imagenes.size());
		
		
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
		return (index==imagenes.size()-1);
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

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
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
    	pageContext.getRequest().setAttribute("imagelist", previousListaImagen );
		pageContext.getRequest().setAttribute("image", previousImagen);
    }
    
    protected void exposeImagen()
    {
    	
		imagen = new TfsImagen(m_cms,imagenes.get(index));
		TfsListaImagenes lista = new TfsListaImagenes(this.imagenes.size(),this.index+1,this.size,this.page);

		pageContext.getRequest().setAttribute("imagelist", lista);
		pageContext.getRequest().setAttribute("image", imagen);
    	
    }
    
	protected void saveImagen()
    {
		previousListaImagen = (TfsListaImagenes) pageContext.getRequest().getAttribute("imagelist");
		previousImagen = (TfsImagen) pageContext.getRequest().getAttribute("image");
    	
    	pageContext.getRequest().setAttribute("imagelist",null);
    	pageContext.getRequest().setAttribute("image",null);
    }

	public int getIndex() {
		return index;
	}

	public TfsImagen getImagen() {
		return imagen;
	}
	
	public String getCollectionValue(String name) throws JspTagException {
		LOG.debug("TfsImagenesListTag - getCollectionValue (name:" + name + ")");

		if (name.equals("creationDate"))
			return "" + imagen.getCreationdate().getTime(); 

		if (name.equals("lastmodifieddate"))
			return "" + imagen.getLastmodifieddate().getTime(); 

		String elementName = getElementName(name);
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.image.title")))
			return (imagen.getTitle()!=null ? imagen.getTitle() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("news.image.description")))
			return (imagen.getDescription()!=null ? imagen.getDescription() : "");

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
		if (elementName.contains(TfsXmlContentNameProvider.getInstance().getTagName("news.image.size")))
			return (imagen.getSize()!=null ? imagen.getSize() : "");
		if (elementName.contains(TfsXmlContentNameProvider.getInstance().getTagName("news.image.focalPoint")))
			return (imagen.getFocalPoint()!=null ? imagen.getFocalPoint() : "");
	
	
		return null;
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

	public String getCollectionIndexValue(String name, int index) {
		LOG.debug("TfsImagenesListTag - getCollectionIndexValue (name:" + name + " - index:" + index + ")");
		if (name.contains(TfsXmlContentNameProvider.getInstance().getTagName("news.image.categories")))
		{
			try {
				return imagen.getCategorylist().get(index);
			}
			catch (IndexOutOfBoundsException e) {
				return "";
			}
		} 

		return null;
	}
	
	public int getCollectionIndexSize(String name, boolean isCollectionPart)
	{
		return imagen.getCategorylist().size();
		
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
	
	public String getCollectionPathName() {
		return "";
	}

	public String getIncludeCropped() {
		return includeCropped.toString();
	}

	public void setIncludeCropped(String isCrop) {
		if (isCrop==null || isCrop.trim().length()==0)
			this.includeCropped = null;
		else
			this.includeCropped = Boolean.parseBoolean(isCrop);
	}

	public String getOriginalImage() {
		return originalImage;
	}

	public void setOriginalImage(String originalImage) {
		if (originalImage==null || originalImage.trim().length()==0)
			this.originalImage=null;
		else
			this.originalImage = originalImage;
	}
	
	
}
