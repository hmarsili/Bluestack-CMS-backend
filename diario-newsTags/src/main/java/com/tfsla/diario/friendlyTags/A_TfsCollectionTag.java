package com.tfsla.diario.friendlyTags;

import java.util.List;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;

import com.tfsla.diario.ediciones.services.PublicationService;
import com.tfsla.diario.model.TfsLista;

@SuppressWarnings("serial")
public abstract class A_TfsCollectionTag<T> extends TagSupport {
	
	public A_TfsCollectionTag() {
		
	}
	
	protected abstract List<T> getItems();
	protected abstract String getItemName();
	
	@Override
	public int doStartTag() throws JspException {
		saveItems();
		
		CmsFlexController controller = CmsFlexController.getController(pageContext.getRequest());
	 	this.cmsObject = controller.getCmsObject();
	 	this.site = this.cmsObject.getRequestContext().getSiteRoot();
		this.publication = "";
		try {
			this.publication = String.valueOf(PublicationService.getPublicationId(this.cmsObject));
		} catch(Exception e) {
			e.printStackTrace();
		}

		//retrieve items at inherited class
		items = this.getItems();
		
		return (hasMoreContent() ? EVAL_BODY_INCLUDE : SKIP_BODY);
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
		index = -1;
		return EVAL_PAGE;
	}
	
	protected boolean hasMoreContent() {
		if(items == null) return false;
		
		index++;
		if (index < items.size()) {
			exposeItem(items.get(index));
		} else {
			restoreTerm();
		}

		return (index < items.size());
	}
	
	protected void exposeItem(T item) {
		TfsLista lista = new TfsLista(this.items.size(), this.index+1, this.size, this.page);
		pageContext.getRequest().setAttribute(this.getItemName() + "sist", lista);
		pageContext.getRequest().setAttribute(this.getItemName(), item);
	}
	
	protected void restoreTerm() {
		pageContext.getRequest().setAttribute(this.getItemName(), previousItem);
    	pageContext.getRequest().setAttribute(this.getItemName() + "slist", previousList);
	}
	
	@SuppressWarnings("unchecked")
	protected void saveItems() {
		previousList = (TfsLista) pageContext.getRequest().getAttribute("termslist");
		previousItem  = (T)pageContext.getRequest().getAttribute(this.getItemName());
    	pageContext.getRequest().setAttribute(this.getItemName() + "slist", null);
    	pageContext.getRequest().setAttribute(this.getItemName(), null);
    }
	
	protected I_TfsNoticia getCurrentNews() throws JspTagException {
	    Tag ancestor = findAncestorWithClass(this, I_TfsNoticia.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag News not accesible");
	    }
	
	    I_TfsNoticia noticia = (I_TfsNoticia) ancestor;
		return noticia;
	}
	
	protected String getElementValue(I_TfsNoticia noticia, String elementName) {
	    try {
	    	I_CmsXmlDocument xmlContent = noticia.getXmlDocument();
			return xmlContent.getStringValue(this.cmsObject, elementName, noticia.getXmlDocumentLocale());
		} catch (CmsXmlException e) {
			LOG.error("Error reading content value " + elementName,e);
		}
	
		return "";
	}
	
	protected int index = -1;
	protected int size = 0;
	protected int page = 1;
	protected T previousItem;
	protected TfsLista previousList;
	protected List<T> items;
	protected CmsObject cmsObject;
	protected String site;
	protected String publication;
	protected final Log LOG = CmsLog.getLog(this);
}