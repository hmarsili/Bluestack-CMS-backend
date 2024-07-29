package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspTagEditable;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.workplace.editors.directedit.CmsDirectEditButtonSelection;
import org.opencms.workplace.editors.directedit.CmsDirectEditMode;
import org.opencms.xml.CmsXmlException;

import com.tfsla.diario.model.TfsNoticia;
import com.tfsla.diario.utils.TfsDirectEditParams;

public class TfsNoticiaTag extends A_XmlContentTag implements I_TfsNoticia, I_TfsCollectionListTag {

    protected static final Log LOG = CmsLog.getLog(TfsNoticiaTag.class);

	private CmsDirectEditMode m_directEditMode;
	private String m_directEditLinkForNew = null;
	private CmsDirectEditButtonSelection m_directEditFollowButtons;
	private boolean m_directEditOpen;
	
	public TfsNoticiaTag()
	{
		
		path=null;
		previousNoticia = null;
		m_directEditOpen=false;
	}
	
	TfsNoticia previousNoticia = null;
	
	protected void openDirectEdit() throws JspException
	{
		
		if (m_directEditMode != CmsDirectEditMode.FALSE)
		{
			CmsDirectEditButtonSelection directEditButtons;
			 if (m_directEditFollowButtons == null) {
	            // this is the first call, calculate the options
	            if (m_directEditLinkForNew == null) {
	                // if create link is null, show only "edit" button for first element
	                directEditButtons = CmsDirectEditButtonSelection.EDIT;
	                // also show only the "edit" button for 2nd to last element
	                m_directEditFollowButtons = directEditButtons;
	            } else {
	                // if create link is not null, show "edit", "delete" and "new" button for first element
	                directEditButtons = CmsDirectEditButtonSelection.EDIT_DELETE_NEW;
	                // show "edit" and "delete" button for 2nd to last element
	                m_directEditFollowButtons = CmsDirectEditButtonSelection.EDIT_DELETE;
	            }
	        } else {
	            // re-use pre calculated options
	            directEditButtons = m_directEditFollowButtons;
	        }
			 
			CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
			
			m_directEditOpen = CmsJspTagEditable.startDirectEdit(pageContext, new TfsDirectEditParams(
					   m_cms.getSitePath(m_content.getFile()),
	                   directEditButtons,
	                   m_directEditMode,
	                   m_directEditLinkForNew,
	                   cms.getRequestContext().getUri(), pageContext.getRequest().getServerName()));
		}
	}
	
    @Override
	public int doStartTag() throws JspException {
    	
    	m_cms = CmsFlexController.getCmsObject(pageContext.getRequest());
    	if (path==null)
    	{
    		String contextPath = m_cms.getRequestContext().getUri();
    		init(contextPath);
    	}
    	else {
    		if (!m_cms.existsResource(path, CmsResourceFilter.ALL)) {
    			LOG.error("No existe noticia solicitada: " + path );
    			return SKIP_BODY;
    		}
    		init(path);
    	}
    	initDirectEdit();
    	
		exposeNoticia();
		
		openDirectEdit();
		 
		return EVAL_BODY_INCLUDE; //SKIP_BODY;		
	}

    protected void initDirectEdit()
	{
    	m_directEditLinkForNew = null;
    	m_directEditOpen=false;
		if (m_directEditMode == null) {
            m_directEditMode = CmsDirectEditMode.FALSE;
        }
		
        // use "create link" only if collector supports it
        m_directEditLinkForNew = CmsEncoder.encode("Contenidos|/contenidos/noticia_0001.html|50");
	}
    
    protected void exposeNoticia()
    {
    	previousNoticia = (TfsNoticia) pageContext.getRequest().getAttribute("news");    	
		TfsNoticia noticia = new TfsNoticia(m_cms,m_content,m_contentLocale,pageContext);		
		pageContext.getRequest().setAttribute("news", noticia);
    	
    }

    protected void restoreNoticia()
    {
		pageContext.getRequest().setAttribute("news", previousNoticia);
    }
    
	private String path;
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public String getEditable() {
		return m_directEditMode != null ? m_directEditMode.toString() : "";
	}

	public void setEditable(String editable) {
		m_directEditMode = CmsDirectEditMode.valueOf(editable);
	}
	
    @Override
    public int doEndTag() {

    	if (m_directEditOpen)
			try {
				CmsJspTagEditable.endDirectEdit(pageContext);
			} catch (JspException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	
        if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
            // need to release manually, JSP container may not call release as required (happens with Tomcat)
            release();
        }
        restoreNoticia();
        
        return EVAL_PAGE;
    }

	public int getIndex() {
		return 0;
	}

	public boolean isLast() {
		// TODO Auto-generated method stub
		return true;
	}

	public String getCollectionValue(String name) throws JspTagException {
		try {
			LOG.debug("TfsNoticiaTag - getCollectionValue : " + name);
			return getXmlDocument().getStringValue(m_cms, name, m_locale);
		} catch (CmsXmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";

	}
	
	public int getCollectionIndexSize(String name, boolean isCollectionPart) {
		LOG.debug("TfsNoticiaTag - getCollectionIndexSize : " + name);
		LOG.debug("TfsNoticiaTag - getCollectionIndexSize : " + name  + " --> " + getXmlDocument().getValues(name, m_locale).size());
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
