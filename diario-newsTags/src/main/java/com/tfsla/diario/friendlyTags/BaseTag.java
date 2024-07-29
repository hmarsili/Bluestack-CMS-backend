package com.tfsla.diario.friendlyTags;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;

import javax.servlet.jsp.tagext.BodyTagSupport;

public abstract class BaseTag extends BodyTagSupport {

	
    /** The CmsObject for the current user. */
    protected transient CmsObject m_cms;

    /** The FlexController for the current request. */
    protected CmsFlexController m_controller;

    
	protected void init()
	{
        m_controller = CmsFlexController.getController(pageContext.getRequest());
        m_cms = m_controller.getCmsObject();
        
	}
	
	

}
