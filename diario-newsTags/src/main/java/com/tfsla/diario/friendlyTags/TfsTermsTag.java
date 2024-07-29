package com.tfsla.diario.friendlyTags;

import java.util.ArrayList;
import java.util.List;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.services.PublicationService;
import com.tfsla.diario.terminos.data.TermsDAO;
import com.tfsla.diario.terminos.data.TermsTypesDAO;
import com.tfsla.diario.terminos.model.SearchOptions;
import com.tfsla.diario.terminos.model.Terms;
import com.tfsla.diario.terminos.model.TermsTypes;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsTermsTag extends A_TfsCollectionTag<Terms> {
	
	private Boolean filterCurrentNew;
	private String orderBy;
	private String text;
	private String from;
	private String to;
	private Long type;
	private int status;
	private String size;
	private Long idTag;
	private static final long serialVersionUID = 6393370317751624642L;

	public TfsTermsTag() {
		this.status = -1;
	}
	
	@Override
	protected List<Terms> getItems() {
		List<Terms> ret = new ArrayList<Terms>();
		TermsDAO dao = new TermsDAO();
		
		if(this.getFilterCurrentNew()) {
			try {
				I_TfsNoticia noticia = getCurrentNews();
	        	String content = getElementValue(noticia, TfsXmlContentNameProvider.getInstance().getTagName("news.keywords"));
	        	if(content != null && !content.trim().equals("")) {
	        		String siteName = OpenCms.getSiteManager().getCurrentSite(CmsFlexController.getCmsObject(pageContext.getRequest())).getSiteRoot();
	            	String publication = "0";
	            	try {
	        			publication = String.valueOf(PublicationService.getCurrentPublicationWithoutSettings(CmsFlexController.getCmsObject(pageContext.getRequest())).getId());
	        		} catch (Exception e) {
	        			LOG.error(e);
	        		}

	            	CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
	            	
	            	String parameterType = config.getParam(siteName, publication, "terms", "termsType","tags");
	        		
	            	TermsTypesDAO ttDAO = new TermsTypesDAO ();
	            	TermsTypes oTermTypes = null;
	            	Long type = new Long(1);
	            	try {
	            		oTermTypes = ttDAO.getTermType(parameterType);
	            		type = oTermTypes.getId_termType();
	            	} catch (Exception e) {
	            		
	            	}
	        		ret = dao.getTerminosByNames(content.split(","),1, type);
	        		List<Terms> termsOrdered = new ArrayList<Terms>();
		        	for (String term : content.split(",")) {
						for (Terms tag : ret) {
							if (tag.getName().trim().equals(term.trim())){
								termsOrdered.add(tag);
								break;
							}
						}
					}
		        	ret = termsOrdered;
		        }
	        	
			} catch(Exception e) {
				e.printStackTrace();
			}
			return ret;
		}
		
		SearchOptions options = new SearchOptions();
		options.setFrom(this.from);
		options.setText(this.text);
		options.setTo(this.to);
		options.setType(this.type);
		options.setId(this.idTag);
		options.setOrderBy(this.orderBy);
		
		
		if(this.status >= 0) {
			options.setStatus(this.status);
		}
		//Si el valor que recibe es -1 muestra todos los elementos, en caso contrario 
		//Si no se setea el parametro size, entonces se asigna por defecto que traiga 100 elementos
		if (size == null) {
			options.setCount(100);
		} else if (!size.equals("all")){
			try {
				options.setCount(Integer.valueOf(size));
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
			}
		}
		try {
			ret = dao.getTerminos(options);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	protected String getItemName() {
		return "term";
	}
	
	public Boolean getFilterCurrentNew() {
		return filterCurrentNew == null ? false : filterCurrentNew;
	}

	public void setFilterCurrentNew(Boolean filterCurrentNew) {
		this.filterCurrentNew = filterCurrentNew;
	}
	
	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
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

	public Long getType() {
		return type;
	}

	public void setType(Long type) {
		this.type = type;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	
	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public Long getIdTag() {
		return idTag;
	}

	public void setIdTag(Long idTag) {
		this.idTag = idTag;
	}

	
	
}