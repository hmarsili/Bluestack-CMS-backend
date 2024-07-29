package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.*;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.OpenCmsBaseService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.model.TfsListaNoticias;
import com.tfsla.diario.model.TfsNoticia;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsLinkedArticle extends A_TfsNoticiaResourceCollection
		implements I_TfsCollectionListTag {
	
	TfsNoticia previousNoticia = null;
	TfsListaNoticias previousListaNoticia = null;
	
	protected int index = 0;

	List<CmsResource> noticias = new ArrayList<CmsResource>();
	
	private String publication=null;
	
	public String getPublication() {
		return publication;
	}

	public void setPublication(String publication) {
		this.publication = publication;
	}

	@Override
	public int doStartTag() throws JspException {
		
		keyControlName = "path"; 

		init("linkedArticles"); 

		saveNoticia();

		index = 0;
			
		if (hasMoreContent()) {
			return EVAL_BODY_INCLUDE;
		}

		return SKIP_BODY;
	}
	
    @Override
	protected boolean hasMoreContent() {
    	index++;

		boolean withElement=false;
		
		CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
		
		boolean publicationFilter = false;
		int  paramPublicationId = -1;
			
		if(publication !=null){
		  paramPublicationId = getTipoEdicionIdFromName(publication,cms);
		  
		  if (paramPublicationId >-1) publicationFilter = true;
		}
		
		while (index<=lastElement && !withElement) {
			I_TfsNoticia noticia;
			try {
				noticia = getCurrentNews();
			} catch (JspTagException e) {
				return false;
			}
			
			String path = getCurrentPath(noticia);
			int currentPublicationId = getTipoEdicionId(path, cms);
			
			String controlValue = getIndexElementValue(noticia,"path",index);
			
			if(controlValue!=null && !controlValue.trim().equals(""))
			{
				int linkedPublicationId = getTipoEdicionId(controlValue, cms);
				
				if (publicationFilter){
					if( paramPublicationId == linkedPublicationId){
						withElement=true;
    					initResource(controlValue);
					}else{
						index++;
					}
					
				}else{
					if(linkedPublicationId!=currentPublicationId){
						withElement=true;
						initResource(controlValue);
					}else{
						index++;
					}
				}
				//if(linkedPublicationId!=currentPublicationId){
				//	withElement=true;
				//	initResource(controlValue);
			//	}else
				//	index++;
			}
			else
				index++;
		}
		
		if (index > lastElement)
			restoreNoticia();
		
		return (index<=lastElement);
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
					index,this.noticias.size(),1);
			lista.setCurrentsection(noticia.getSection());

			pageContext.getRequest().setAttribute("newslist", lista);
			pageContext.getRequest().setAttribute("news", noticia);
		} catch (CmsException e) {
			e.printStackTrace();
		}

	}

	public CmsResource getNoticia() {
		return noticias.get(index);
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	@Override
	public String getCollectionPathName() {
		return "";
	}
	
	protected String getCurrentPath(I_TfsNoticia noticia) {

		I_CmsXmlDocument xmlContent = noticia.getXmlDocument();
		CmsFile file = xmlContent.getFile();

		return file.getRootPath();
	}
	
	public int getTipoEdicionId(String resourcePath, CmsObject cms) {
        TipoEdicionService tService =  new TipoEdicionService();
        
        TipoEdicion tEdicion=null;
        int id = 1;
        
		try {
			tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().removeSiteRoot(resourcePath));
			id = tEdicion.getId();
		} catch (Exception e) {
			LOG.error("Publication not found", e);
			
		}
			
		return id;
	}
	
	public int getTipoEdicionIdFromName(String name, CmsObject cms){
		
		TipoEdicionService tService =  new TipoEdicionService();
        
        TipoEdicion tEdicion=null;
        int id = -1;
        
        String siteName = OpenCmsBaseService.getCurrentSite(cms);
        
        try {
			tEdicion = tService.obtenerTipoEdicion(name,siteName);
			id = tEdicion.getId();
        } catch (Exception e) {
			LOG.error("Publication not found", e);
			
		}
			
		return id;
        
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	public String getCollectionValue(String name) throws JspTagException {
		try {
    	    // get the current users OpenCms context
    	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

    	    return getXmlDocument().getStringValue(cms, name, m_locale);
		} catch (CmsXmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
}
