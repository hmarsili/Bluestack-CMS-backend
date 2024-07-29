package com.tfsla.genericImport.service;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentErrorHandler;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlSchemaType;

public class XmlContentService {
	
	private static final Log LOG = CmsLog.getLog(XmlContentService.class);

	private String m_resourceName;

    private CmsObject m_cloneCms = null;

	private CmsFile m_file;
    private CmsXmlContent m_content;
    
    private Locale m_elementLocale;
    private String m_fileEncoding;

    private CmsFlexController m_controller;
    private HttpServletRequest m_request;
    private HttpServletResponse m_response;
    private HttpSession m_session;
    private PageContext m_context;
    private CmsUUID m_currentProjectId;
    private CmsXmlContentErrorHandler m_validationHandler;
    private Locale m_locale;
    
    private CmsWorkplaceSettings m_settings;
    
    private CmsMultiMessages m_messages;

    public XmlContentService(PageContext context, HttpServletRequest req, HttpServletResponse res)
    {
    	m_controller = CmsFlexController.getController(req);
        m_context = context;
        m_request = req;
        m_response = res;
        m_session = req.getSession();
        m_settings = (CmsWorkplaceSettings)m_session.getAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
        
        if (m_settings==null)  {
        	m_settings = CmsWorkplace.initWorkplaceSettings(getCmsObject(), m_settings, true);
        	m_session.setAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS, m_settings);
        }
        
        
        retriveMessages();
    }
    
    public void initResource() throws CmsException {
    	setResourceName(m_request.getParameter("url"));

        m_file = getCmsObject().readFile(this.getResourceName(), CmsResourceFilter.ALL);
        m_content = CmsXmlContentFactory.unmarshal(getCloneCms(), m_file);

        setFileEncoding(getFileEncoding(getCmsObject(), this.getResourceName()));

        // check the XML content against the given XSD
        try {
            m_content.validateXmlStructure(new CmsXmlEntityResolver(getCmsObject()));
        } catch (CmsXmlException eXml) {
            // validation failed, check the settings for handling the correction
        	lockResource(m_resourceName);
        	correctXmlStructure();
            getCmsObject().unlockResource(m_resourceName);
        }

    }

    private void correctXmlStructure() throws CmsException {

        m_content.setAutoCorrectionEnabled(true);
        m_content.correctXmlStructure(getCmsObject());
        writeContent();
        
    }

    private void writeContent() throws CmsException {

        String decodedContent = m_content.toString();
        try {
            m_file.setContents(decodedContent.getBytes(getFileEncoding()));
        } catch (UnsupportedEncodingException e) {
            throw new CmsException(org.opencms.workplace.editors.Messages.get().container(org.opencms.workplace.editors.Messages.ERR_INVALID_CONTENT_ENC_1, getResourceName()), e);
        }
        // the file content might have been modified during the write operation    
        m_file = getCloneCms().writeFile(m_file);
        m_content = CmsXmlContentFactory.unmarshal(getCloneCms(), m_file);
    }

    public void lockResource(String resource) throws CmsException {

        CmsResource res = getCmsObject().readResource(resource, CmsResourceFilter.ALL);
        CmsLock lock = getCmsObject().getLock(res);
        boolean lockable = lock.isLockableBy(getCmsObject().getRequestContext().currentUser());

        if (OpenCms.getWorkplaceManager().autoLockResources()) {
            // autolock is enabled, check the lock state of the resource
            if (lockable) {
                	getCmsObject().lockResource(resource);
            } else {
                throw new CmsException(org.opencms.workplace.Messages.get().container(org.opencms.workplace.Messages.ERR_WORKPLACE_LOCK_RESOURCE_1, resource));
            }
        } else {
            if (!lockable) {
                throw new CmsException(org.opencms.workplace.Messages.get().container(org.opencms.workplace.Messages.ERR_WORKPLACE_LOCK_RESOURCE_1, resource));
            }
        }
    }

    protected CmsObject getCloneCms() throws CmsException {

        if (m_cloneCms == null) {
            m_cloneCms = OpenCms.initCmsObject(getCmsObject());
            m_cloneCms.getRequestContext().setRequestTime(CmsResource.DATE_RELEASED_EXPIRED_IGNORE);
        }
        return m_cloneCms;
    }
    
    public List<I_CmsXmlSchemaType> getSubContentDetail() {
    	String elementName = m_request.getParameter("name"); // ej: imagenesFotogaleria

        CmsXmlContentDefinition contentDefinition = m_content.getContentDefinition();

        if (!elementName.equals("")) {
	        String[] nameParts = elementName.split("/");
	        for (String part : nameParts) {
	        	for (I_CmsXmlSchemaType type : contentDefinition.getTypeSequence())
	        	{
	        		if (type.getName().equals(part)) {
	        			if (!type.isSimpleType()) {
	                        // get nested content definition for nested types
	                        CmsXmlNestedContentDefinition nestedSchema = (CmsXmlNestedContentDefinition)type;
	                        contentDefinition = nestedSchema.getNestedContentDefinition();
	                    }
	        			break;
	        		}
	        	}
	        }
        }
        return contentDefinition.getTypeSequence();
    }
    
    
    public void retriveMessages() {
    	
    	if (getLocale()==null)
    	    	setLocale(getCmsObject().getRequestContext().getLocale());
    	
    	// initialize messages            
	    CmsMessages messages = OpenCms.getWorkplaceManager().getMessages(getLocale());
	    // generate a new multi messages object and add the messages from the workplace
	    
	    m_messages = new CmsMultiMessages(getLocale());
	    m_messages.addMessages(messages);
    }
    
	public Locale getLocale()
	{
		return m_elementLocale;
	}
	
	public void setLocale(Locale locale)
	{
		m_elementLocale = locale;
	}
	
    protected void setFileEncoding(String value) {
        m_fileEncoding = CmsEncoder.lookupEncoding(value, value);
    }
    
    protected String getFileEncoding(CmsObject cms, String filename) {

        try {
            return cms.readPropertyObject(filename, CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, true).getValue(
                OpenCms.getSystemInfo().getDefaultEncoding());
        } catch (CmsException e) {
            return OpenCms.getSystemInfo().getDefaultEncoding();
        }
    }
    protected String getFileEncoding() {

        return m_fileEncoding;
    }
    
	
	public CmsWorkplaceSettings getSettings()
	{
		return m_settings;
	}
	
    public CmsObject getCmsObject() {
        return m_controller.getCmsObject();
    }
    
    public PageContext getJspContext() {
        return m_context;
    }

    public HttpServletRequest getRequest() {
        return m_request;
    }
    
    public HttpServletResponse getResponse() {
        return m_response;
    }
    
    public CmsMultiMessages getMessages()
    {
    	return m_messages;
    }
    
    public void setResourceName(String resourceName)
    {
    	m_resourceName = resourceName;
    }
    
    public String getResourceName()
    {
    	return m_resourceName;
    }

}