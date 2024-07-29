package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import org.apache.commons.logging.Log;
import org.opencms.file.CmsResourceFilter;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlException;
import com.tfsla.diario.model.TfsTrivia;

public class TfsTriviaTag extends A_XmlContentTag implements I_TfsTrivia, I_TfsCollectionListTag {

	
	TfsTrivia previousTrivia = null;
	
	private static final long serialVersionUID = 1L;

		protected static final Log LOG = CmsLog.getLog(TfsNoticiaTag.class);

	    private String path;
		
		public TfsTriviaTag() {
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
	    			LOG.error("No existe la trivia solicitada: " + path );
	    			return SKIP_BODY;
	    		}
	    		init(path);
	    	}
	    	
			exposeTrivia();
			
			 
			return EVAL_BODY_INCLUDE; //SKIP_BODY;		
		}
	
	    protected void exposeTrivia() {
	    	previousTrivia = (TfsTrivia) pageContext.getRequest().getAttribute("trivia");    	
			TfsTrivia trivia = new TfsTrivia(m_cms,m_content,m_contentLocale,pageContext);		
			pageContext.getRequest().setAttribute("trivia", trivia);
	    }

	    protected void restoreTrivia()    {
			pageContext.getRequest().setAttribute("trivia", previousTrivia);
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
	            release();
	        }
	        restoreTrivia();
	        
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
				return getXmlDocument().getStringValue(m_cms, name, m_locale);
			} catch (CmsXmlException e) {
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
				e.printStackTrace();
			}
			return "";
		}
		
		public String getCollectionPathName() {
			return "";
		}

	}


