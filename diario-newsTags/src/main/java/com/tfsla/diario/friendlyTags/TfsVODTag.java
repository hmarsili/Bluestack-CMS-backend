package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsResourceFilter;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlException;

import com.tfsla.diario.model.TfsVOD;

public class TfsVODTag extends A_XmlContentTag implements I_TfsNoticia, I_TfsCollectionListTag {

    /**
	 * 
	 */
	
	TfsVOD previousVOD = null;
	
	private static final long serialVersionUID = 1L;

		protected static final Log LOG = CmsLog.getLog(TfsVODTag.class);

	    private String path;
		
		public TfsVODTag () {
			path=null;			
		}
	
		@Override
		public int doStartTag() throws JspException {
	    	
	    	m_cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	    	if (path==null) {
	    		String contextPath = m_cms.getRequestContext().getUri();
	    		init(contextPath);
	    	}
	    	else {
	    		if (!m_cms.existsResource(path, CmsResourceFilter.ALL)) {
	    			LOG.error("No existe el vod solicitado: " + path );
	    			return SKIP_BODY;
	    		}
	    		init(path);
	    	}
	    	
			exposeVOD();
			
			 
			return EVAL_BODY_INCLUDE; //SKIP_BODY;		
		}
	
	    protected void exposeVOD() {
	    	previousVOD = (TfsVOD) pageContext.getRequest().getAttribute("vod");    	
	    	TfsVOD vod = new TfsVOD(m_cms,m_content,m_contentLocale,pageContext);		
			pageContext.getRequest().setAttribute("vod", vod);
	    }

	    protected void restoreVOD()    {
			pageContext.getRequest().setAttribute("vod", previousVOD);
	    }
	    
			
		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}
		
		
	    @Override
	    public int doEndTag() {

	    	 if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
	            // need to release manually, JSP container may not call release as required (happens with Tomcat)
	            release();
	        }
	        restoreVOD();
	        
	        return EVAL_PAGE;
	    }

		public int getIndex() {
			return 0;
		}

		public boolean isLast() {
			return true;
		}

		public String getCollectionValue(String name) throws JspTagException {
			try {
				LOG.debug("TfsEventoListTag - getCollectionValue : " + name);
				return getXmlDocument().getStringValue(m_cms, name, m_locale);
			} catch (CmsXmlException e) {
					e.printStackTrace();
			}
			return "";

		}
		
		public int getCollectionIndexSize(String name, boolean isCollectionPart) {
			LOG.debug("TfsVODListTag - getCollectionIndexSize : " + name);
			LOG.debug("TfsVODListTag - getCollectionIndexSize : " + name  + " --> " + getXmlDocument().getValues(name, m_locale).size());
			return getXmlDocument().getValues(name, m_locale).size();
		}

		public String getCollectionIndexValue(String name, int index) {
			try {
				return getXmlDocument().getStringValue(m_cms, name, m_locale, index);
			} catch (CmsXmlException e) {
				e.printStackTrace();
			}
			return "";
		}
		
		public String getCollectionPathName() {
			return "";
		}

	
	
}


