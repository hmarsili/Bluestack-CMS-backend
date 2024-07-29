package com.tfsla.rankViews.jsp;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;


import java.io.IOException;

public class DynamicContHitTag  extends TagSupport {

	private static final Log LOG = CmsLog.getLog(DynamicContHitTag.class);

    /** The CmsObject for the current user. */
    protected transient CmsObject m_cms;

    /** The FlexController for the current request. */
    protected CmsFlexController m_controller;

    @Override
    public int doStartTag() throws JspException {
    	try {
    		
            m_controller = CmsFlexController.getController(pageContext.getRequest());
            m_cms = m_controller.getCmsObject();
                        
            String id = m_cms.readResource(m_cms.getRequestContext().getUri()).getStructureId().getStringValue();
			if (id!=null && id.trim().length()>0) {
				pageContext.getOut().print("<div type=\"stat\" id=\"" + id + "\" ></div>");
			}
			else
				LOG.error("Error al utilizar el Tag de conteo de hits dinamicos en " +m_cms.getRequestContext().getUri() + ". Identificador de recurso no obtenido.");
				
		} catch (IOException e) {
			LOG.error("Error al utilizar el Tag de conteo de hits dinamicos en " +m_cms.getRequestContext().getUri(),e);
			//throw new JspException(e);
		} catch (CmsException e) {
			LOG.error("Error al utilizar el Tag de conteo de hits dinamicos en " + m_cms.getRequestContext().getUri(),e);
			//throw new JspException(e);
		}
		
		return SKIP_BODY;
    }

}
