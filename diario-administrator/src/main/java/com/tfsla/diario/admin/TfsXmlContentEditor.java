package com.tfsla.diario.admin;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsSchedulerConfiguration;
import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.report.CmsLogReport;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.scheduler.jobs.CmsPublishScheduledJob;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.widgets.A_CmsWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.commons.Messages;
import org.opencms.workplace.editors.CmsXmlContentWidgetVisitor;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentErrorHandler;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentTab;
import org.opencms.xml.content.CmsXmlContentValueSequence;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import com.tfsla.diario.admin.jsp.TfsMessages;
import com.tfsla.diario.admin.widgets.I_TfsWidget;
import com.tfsla.diario.admin.widgets.TfsWidgetManager;
import com.tfsla.diario.auditActions.resourceMonitor.I_ResourceMonitor;
import com.tfsla.diario.auditActions.resourceMonitor.ResourceMonitorManager;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.OpenCmsBaseService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.securityService.TfsUserAuditPermission;
import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.utils.ScriptsJSFilter;

public class TfsXmlContentEditor {

	private static final Log LOG = CmsLog.getLog(TfsXmlContentEditor.class);

	private String conteinerStyleClass = "default";
	private String m_resourceName;
	private String m_tempFileName;
	
	private CmsFile m_file;
    private CmsXmlContent m_content;
    
    
    private Locale m_elementLocale;
    private String m_fileEncoding;
    
    private CmsObject m_cloneCms = null;
    
    private CmsFlexController m_controller;
    private HttpServletRequest m_request;
    private HttpServletResponse m_response;
    private HttpSession m_session;
    private PageContext m_context;
    private CmsUUID m_currentProjectId;
    private CmsXmlContentErrorHandler m_validationHandler;
    private Locale m_locale;
    private Locale m_userLocale;
    private TfsMessages Tfsmessage;
    
    private CmsWorkplaceSettings m_settings;
    
    private CmsMultiMessages m_messages;
    
    private Map<String,String> editorValues = new HashMap<String, String>();
    
    private List<I_TfsWidget> widgets= null;
    
    public TfsXmlContentEditor(PageContext context, HttpServletRequest req, HttpServletResponse res)
    {
    	m_controller = CmsFlexController.getController(req);
        m_context = context;
        m_request = req;
        m_response = res;
        m_session = req.getSession();
        m_settings = (CmsWorkplaceSettings)m_session.getAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
        
        if (m_settings==null)  {
        	if (LOG.isDebugEnabled()) {
        		LOG.debug("Editing the content " + m_request.getParameter("url")  + ". Session settings not found. Starting context-based configuration.");
        	}
        	m_settings = CmsWorkplace.initWorkplaceSettings(getCmsObject(), m_settings, true);
        	m_session.setAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS, m_settings);
        	
        	if (LOG.isDebugEnabled()) {
        		LOG.debug("Current proyect " + m_settings.getProject() + " - Current site" + m_settings.getSite());
        	}
        	
        	
        } else {
        	if (m_settings.getUserSettings() != null)
        		setUserLocale(m_settings.getUserSettings().getLocale());
        }
        
        
        Tfsmessage = new TfsMessages(m_context,m_request,m_response);
        
        retriveMessages();
    }
    
    public void checkLock(String resource, CmsLockType type) throws CmsException {

        CmsResource res = getCmsObject().readResource(resource, CmsResourceFilter.ALL);
        CmsLock lock = getCmsObject().getLock(res);
        boolean lockable = lock.isLockableBy(getCmsObject().getRequestContext().currentUser());

        if (OpenCms.getWorkplaceManager().autoLockResources()) {
            // autolock is enabled, check the lock state of the resource
            if (lockable) {
                // resource is lockable, so lock it automatically
                if (type == CmsLockType.TEMPORARY) {
                	getCmsObject().lockResourceTemporary(resource);
                } else {
                	getCmsObject().lockResource(resource);
                }
            } else {
                throw new CmsException(org.opencms.workplace.Messages.get().container(org.opencms.workplace.Messages.ERR_WORKPLACE_LOCK_RESOURCE_1, resource));
            }
        } else {
            if (!lockable) {
                throw new CmsException(org.opencms.workplace.Messages.get().container(org.opencms.workplace.Messages.ERR_WORKPLACE_LOCK_RESOURCE_1, resource));
            }
        }
    }
    
    public boolean preExistsTemporalFile() {
    	try {  
	    	setResourceName(m_request.getParameter("url"));
	    	
	    	checkLock(m_resourceName, CmsLockType.TEMPORARY);
	    	
	    	setTempFileName(CmsWorkplace.getTemporaryFileName(m_resourceName));
	    	
	        return (getCmsObject().existsResource(getTempFileName(), CmsResourceFilter.ALL));
    	}
    	catch (CmsException e) {
    		return false;
    	}

    }
    
    public void discardTemporalFile() {
    	try {
	    	setResourceName(m_request.getParameter("url"));
	    	
	    	checkLock(m_resourceName, CmsLockType.TEMPORARY);
	    	
	    	setTempFileName(CmsWorkplace.getTemporaryFileName(m_resourceName));
	    	
	        if (getCmsObject().existsResource(getTempFileName(), CmsResourceFilter.ALL)) {
	            if (!getCmsObject().getLock(getTempFileName()).isUnlocked()) {
	                // steal lock
	            	getCmsObject().changeLock(getTempFileName());
	            } else {
	                // lock resource to current user
	            	getCmsObject().lockResource(getTempFileName());
	            }
	        	deleteTempFile();
	        }
    	}
    	catch (CmsException e) {
    		LOG.error("Error discarting temporal file " + getTempFileName(),e);
    	}
    }
    
    public void editResource() throws CmsException
    {
    	
    	try {
    		
	    	setResourceName(m_request.getParameter("url"));
	    	
	    	if (!getCmsObject().existsResource(m_resourceName, CmsResourceFilter.ALL)) {
	    		throw new CmsVfsResourceNotFoundException(Messages.get().container(
	                    Messages.ERR_RESOURCE_DOES_NOT_EXIST_3,
	                    m_resourceName,
	                    getCmsObject().getRequestContext().currentProject().getName(),
	                    getCmsObject().getRequestContext().getSiteRoot()));
	    	}
	    	
	    	checkLock(m_resourceName, CmsLockType.TEMPORARY);
	    	
	    	
	    	
	    	setTempFileName(CmsWorkplace.getTemporaryFileName(m_resourceName));
	    	
	        if (getCmsObject().existsResource(getTempFileName(), CmsResourceFilter.ALL)) {
	            // delete old temporary file
	            if (!getCmsObject().getLock(getTempFileName()).isUnlocked()) {
	                // steal lock
	            	getCmsObject().changeLock(getTempFileName());
	            } else {
	                // lock resource to current user
	            	getCmsObject().lockResource(getTempFileName());
	            }
	        }
	        else
	        	createTempFile();
	    	
	        m_file = getCmsObject().readFile(this.getTempFileName(), CmsResourceFilter.ALL);
	        m_content = CmsXmlContentFactory.unmarshal(getCloneCms(), m_file);

	        setFileEncoding(getFileEncoding(getCmsObject(), this.getTempFileName()));

	        // check the XML content against the given XSD
	        try {
	            m_content.validateXmlStructure(new CmsXmlEntityResolver(getCmsObject()));
	        } catch (CmsXmlException eXml) {
	            // validation failed, check the settings for handling the correction
	            correctXmlStructure();
	        }
        
    	}
    	catch (CmsException e) {
    		LOG.error("Error al intentar editar la noticia " + m_request.getParameter("url"), e);
			throw e;
		}

    }
    
    public void cancelEdit() {
    	try {
    		CmsResourceUtils.createPropertyInShadowMode(getCmsObject(), getCmsObject().readResource(getTempFileName()),
    				"disardChanges", "true");
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void endEdit() {
		deleteTempFile();
		String keepLocked = m_request.getParameter("keepLocked");
		try {
			if ( keepLocked!=null && keepLocked.equals("true")) {
				getCmsObject().lockResource(getResourceName());
			} else {
				getCmsObject().unlockResource(getResourceName());
			}
		} catch (CmsException e) {
			        
		}
		
    }

        
    protected CmsObject getCloneCms() throws CmsException {

        if (m_cloneCms == null) {
            m_cloneCms = OpenCms.initCmsObject(getCmsObject());
            m_cloneCms.getRequestContext().setRequestTime(CmsResource.DATE_RELEASED_EXPIRED_IGNORE);
        }
        return m_cloneCms;
    }
    
    protected String createTempFile() throws CmsException {
        return OpenCms.getWorkplaceManager().createTempFile(getCmsObject(), getResourceName(), getSettings().getProject());
        

    }
    
    private void correctXmlStructure() throws CmsException {

        m_content.setAutoCorrectionEnabled(true);
        m_content.correctXmlStructure(getCmsObject());
        // write the corrected temporary file
        writeContent();
    }
    
    private void writeContent() throws CmsException {

        String decodedContent = m_content.toString();
         //decodedContent = decodedContent.replaceAll("\\p{C}", "");
        decodedContent = decodedContent.replaceAll("(?!\\n)[\\p{C}]", "");
        
        try {
            m_file.setContents(decodedContent.getBytes(getFileEncoding()));
        } catch (UnsupportedEncodingException e) {
            throw new CmsException(org.opencms.workplace.editors.Messages.get().container(org.opencms.workplace.editors.Messages.ERR_INVALID_CONTENT_ENC_1, getResourceName()), e);
        }
        // the file content might have been modified during the write operation    
        m_file = getCloneCms().writeFile(m_file);
        m_content = CmsXmlContentFactory.unmarshal(getCloneCms(), m_file);
    }
    
    protected CmsUUID switchToTempProject() throws CmsException {

        // store the current project id in member variable
        m_currentProjectId = getSettings().getProject();
        CmsUUID tempProjectId = OpenCms.getWorkplaceManager().getTempFileProjectId();
        getCmsObject().getRequestContext().setCurrentProject(getCmsObject().readProject(tempProjectId));
        return tempProjectId;
    }
    
    protected void switchToCurrentProject() throws CmsException {

        if (m_currentProjectId != null) {
            // switch back to the current users project
        	getCmsObject().getRequestContext().setCurrentProject(getCmsObject().readProject(m_currentProjectId));
        }
    }
    
    protected void commitTempFile() throws CmsException {

        CmsObject cms = getCmsObject();
        CmsFile tempFile;
        List properties;
        try {
            switchToTempProject();
            tempFile = cms.readFile(getTempFileName(), CmsResourceFilter.ALL);
            properties = cms.readPropertyObjects(getTempFileName(), false);
        } finally {
            // make sure the project is reset in case of any exception
            switchToCurrentProject();
        }
        if (cms.existsResource(getResourceName(), CmsResourceFilter.ALL)) {
            // update properties of original file first (required if change in encoding occurred)
            cms.writePropertyObjects(getResourceName(), properties);
            // now replace the content of the original file
            CmsFile orgFile = cms.readFile(getResourceName(), CmsResourceFilter.ALL);
            orgFile.setContents(tempFile.getContents());
            getCloneCms().writeFile(orgFile);
        } else {
            // original file does not exist, remove visibility permission entries and copy temporary file

            // switch to the temporary file project
            cms.getRequestContext().setCurrentProject(cms.readProject(getSettings().getProject()));
            // lock the temporary file
            cms.changeLock(getResourceName());
            // remove visibility permissions for everybody on temporary file if possible
            if (cms.hasPermissions(tempFile, CmsPermissionSet.ACCESS_CONTROL)) {
                cms.rmacc(getTempFileName(), I_CmsPrincipal.PRINCIPAL_GROUP, OpenCms.getDefaultUsers().getGroupUsers());
                cms.rmacc(
                		getTempFileName(),
                    I_CmsPrincipal.PRINCIPAL_GROUP,
                    OpenCms.getDefaultUsers().getGroupProjectmanagers());
            }

            cms.copyResource(getTempFileName(), getResourceName(), CmsResource.COPY_AS_NEW);
        }
        // remove the temporary file flag
        int flags = cms.readResource(getResourceName(), CmsResourceFilter.ALL).getFlags();
        if ((flags & CmsResource.FLAG_TEMPFILE) == CmsResource.FLAG_TEMPFILE) {
            flags ^= CmsResource.FLAG_TEMPFILE;
            cms.chflags(getResourceName(), flags);
        }
        
        m_file = getCloneCms().readFile(getResourceName());
        m_content = CmsXmlContentFactory.unmarshal(getCloneCms(), m_file);
    }

    protected void deleteTempFile() {

        try {
            // switch to the temporary file project
            switchToTempProject();
            // delete the temporary file
            getCmsObject().deleteResource(getTempFileName(), CmsResource.DELETE_PRESERVE_SIBLINGS);
        } catch (CmsException e) {
        	LOG.error("Error deleting temporal file " + getTempFileName(),e);
        } finally {
            try {
                // switch back to the current project
                switchToCurrentProject();
            } catch (CmsException e) {
            	LOG.error("Error deleting temporal file " + getTempFileName(),e);
            }
        }
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
    
    public void setResourceName(String resourceName)
    {
    	m_resourceName = resourceName;
    }
    
    public String getResourceName()
    {
    	return m_resourceName;
    }
    
    public void setTempFileName(String tempFileName)
    {
    	m_tempFileName = tempFileName;
    }
    
    public String getTempFileName()
    {
    	return m_tempFileName;
    }
    
	public Locale getLocale()
	{
		return m_elementLocale;
	}
	
	public void setLocale(Locale locale)
	{
		m_elementLocale = locale;
	}
	
	public CmsFile getCmsFile(){
		return m_file;
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
    
    public CmsMultiMessages getTfsMessages()
    {
    	Locale usrLocale = m_settings.getUserSettings().getLocale();
    	
    	CmsMessages TfsMessages = OpenCms.getWorkplaceManager().getMessages(usrLocale);
	    
    	CmsMultiMessages m_tfsMessages = new CmsMultiMessages(usrLocale);
    	                 m_tfsMessages.addMessages(TfsMessages);
    	
    	return m_tfsMessages;
    }
    
    public String keyDefault(String keyName, String defaultValue) {

        //return getMessages().keyDefault(keyName, defaultValue);
    	
       return Tfsmessage.keyDefault(keyName, defaultValue);
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
    
    
    public String getXmlEditorIncludes() throws JspException {

        StringBuffer result = new StringBuffer(1024);

        try {
            // iterate over unique widgets from collector
            
            for (I_TfsWidget widget :getWidgetCollection()) {
                result.append(widget.getDialogIncludes(getCmsObject(), this));
                result.append("\n");
            }
        } catch (Exception e) {
        }
        return result.toString();
    }

    public List<I_TfsWidget> getWidgetCollection() {

    	if (widgets==null) {
    		
    		widgets = new ArrayList<I_TfsWidget>();
	    	CmsXmlContentWidgetVisitor m_widgetCollector = new CmsXmlContentWidgetVisitor(getLocale());
	        m_content.visitAllValuesWith(m_widgetCollector);
	        
	    	for (Object widget : m_widgetCollector.getUniqueWidgets())
	    	{
	    		I_TfsWidget tfsWidget = TfsWidgetManager.getWidget((I_CmsWidget)widget);
	    		if (!widgets.contains(tfsWidget))
	    			widgets.add(tfsWidget);
	    	}
    	}
        return widgets;
    }
    
    public String getXmlEditorFormHeader()
    {    	
    	String header = "<div id=\"resource-name\" url=\"" + getResourceName() + "\" type=\"" + OpenCms.getResourceManager().getResourceType(m_file).getTypeName() + "\">";
		
    	return header;
    }
    
    public String getXmlEditorFormFooter()
    {
    	String footer = "</div>";
    	return footer;
    }
    
    public String getXmlEditorForm() {
    	conteinerStyleClass = "default";
    	
        return getXmlEditorForm(m_content.getContentDefinition(), "", true, false,null,false,false,false,true).toString();
    }
    
    public String getXmlEditorFormByUserLevel(String tabs, boolean exclude, boolean compactView, boolean showActionsButtons) {
    	return getXmlEditorForm(tabs, exclude, compactView, false, showActionsButtons);
    }

    public String getXmlEditorForm(String tabs, boolean exclude, boolean compactView) {
    	return getXmlEditorForm(tabs, exclude, compactView, false);
    }
    
    public String getXmlEditorForm(String tabs, boolean exclude, boolean compactView, boolean closed) {
    	return getXmlEditorForm(tabs, exclude, compactView, closed, true);
    }
    
    public String getXmlEditorForm(String tabs, boolean exclude, boolean compactView, boolean closed, boolean showActionsButtons) {
    	List<String> tabsToShow = new ArrayList<String>();
    	String aTabs[] = tabs.split(",");
    	if (!exclude)
    		for (String tab : aTabs)
    			tabsToShow.add(tab);
    	else
    	{
    		for (CmsXmlContentTab checkTab : m_content.getContentDefinition().getContentHandler().getTabs())
    		{
    			boolean mustInclude = true;
    			for (String tab : aTabs)
    			{
    				if (checkTab.getTabName().equals(tab.trim()))
    					mustInclude = false;
    			}
    			if (mustInclude)
    				tabsToShow.add(checkTab.getTabName());
    		}
    	}
    	if (!compactView)
    		conteinerStyleClass = "default";
    	else
    		conteinerStyleClass = "span12";
    	
        return getXmlEditorForm(m_content.getContentDefinition(), "", true, false,tabsToShow,true,compactView,closed, showActionsButtons).toString();
    }

    public String getXmlElementEditorForm(String pathPrefix, boolean compactView)
    {
    	if (!compactView)
    		conteinerStyleClass = "default";
    	else
    		conteinerStyleClass = "span12";

    	 return getXmlEditorForm(m_content.getContentDefinition(), pathPrefix, true, false,null,false,false,false,true).toString();
    }
    
    private StringBuffer getXmlEditorForm(
            CmsXmlContentDefinition contentDefinition,
            String pathPrefix,
            boolean showHelpBubble,
            boolean superTabOpened,
            List<String> tabsToShow,
            boolean showTabsSubSet,
            boolean compactView,
            boolean closed,
            boolean showActionsButtons) {

            StringBuffer result = new StringBuffer(2048);
            
            try {
            	
       	
                // check if we are in a nested content definition
                boolean nested = CmsStringUtil.isNotEmpty(pathPrefix);
                boolean useTabs = contentDefinition.getContentHandler().getTabs().size() > 0;
                
                boolean tabOpened = false;
               
                boolean collapseLabel = false;
                boolean firstElement = true;

                boolean excludeContentInTab = false;
                
                // iterate the type sequence        
                for (Iterator<I_CmsXmlSchemaType> i = contentDefinition.getTypeSequence().iterator(); i.hasNext();) {
                    // get the type
                    I_CmsXmlSchemaType type = i.next();

                    boolean tabCurrentlyOpened = false;

                    if (useTabs) {
                        // check if a tab is starting with this element
                        for (int tabIndex = 0; tabIndex < contentDefinition.getContentHandler().getTabs().size(); tabIndex++) {
                            CmsXmlContentTab checkTab = contentDefinition.getContentHandler().getTabs().get(tabIndex);
                            
	                            if (checkTab.getStartName().equals(type.getName())) {
	                            	
	                            	excludeContentInTab = showTabsSubSet && !tabsToShow.contains(checkTab.getTabName());
	                            	
	                                // a tab is starting, add block element
	                            	if (!excludeContentInTab) {
	                            		if (tabOpened) {
		                                	result.append("</fieldset>\n");		                                	
		                                	result.append("</form>\n");		                                	
		                                	result.append("</div>\n");
		                                	result.append("</div>\n");
		                                	result.append("</div>\n");
		                                }
	                                
	                                
		                                //result.append("<div class=\"panel\" style=\"display:none\" tab=\""+checkTab.getStartName()+"\" id=\"box-10\">\n");
	                            		result.append("<div class=\"panel\" tab=\""+checkTab.getStartName()+"\" id=\"box-10\">\n");
		                                result.append("<h4 class=\"box-header " + (!closed ? "round-top":"round-all") + "\">\n");
		                                result.append(keyDefault(A_CmsWidget.LABEL_PREFIX
		                                        + contentDefinition.getInnerName()
		                                        + "."
		                                        + checkTab.getTabName(), checkTab.getTabName()));
		                                result.append("  <a class=\"box-btn\" title=\"toggle\"><i class=\"material-icons\">" + (!closed ? "expand_less":"expand_more") + "</i></a>\n");
		                                //result.append("    <a class=\"box-btn\" title=\"config\" data-toggle=\"modal\" href=\"#box-config-modal\"><i class=\"icon-cog\"></i></a>\n");
		                                result.append("</h4>");         
		                                result.append("<div class=\"box-container-toggle " + (!closed ? "":"box-container-closed") + "\">\n");
		                                result.append("<div class=\"box-content\">\n");
		                                if (!compactView)
		                                	result.append("<form class=\"form-horizontal\">\n");
		                                else
			                                result.append("<form>\n");


		                                result.append("<fieldset>\n");
		                                /*
		                                result.append("<legend>\n");
		                                result.append(keyDefault(A_CmsWidget.LABEL_PREFIX
		                                        + contentDefinition.getInnerName()
		                                        + "."
		                                        + checkTab.getTabName(), checkTab.getTabName()));
		                                result.append("</legend>\n");
		                                */
		                                // set necessary values
		                                tabOpened = true;
		                                tabCurrentlyOpened = true;
		                                collapseLabel = checkTab.isCollapsed();
		                                //m_currentTab = checkTab;
		                                //m_currentTabIndex = tabIndex;
		                                // leave loop
	                                }
	                                break;
	                            }
                        }
                    }
                    else if (!nested && firstElement && !excludeContentInTab)
                    {
                    	result.append("<div class=\"panel\" id=\"box-10\">\n");
                        result.append("<h4 class=\"box-header " + (!closed ? "round-top":"round-all") + "\">\n");
                        result.append("");
                        result.append("  <a class=\"box-btn\" title=\"toggle\"><i class=\"material-icons\">" + (!closed ? "expand_less":"expand_more") + "</i></a>\n");
                        //result.append("    <a class=\"box-btn\" title=\"config\" data-toggle=\"modal\" href=\"#box-config-modal\"><i class=\"icon-cog\"></i></a>\n");
                        result.append("</h4>");         
                        result.append("<div class=\"box-container-toggle " + (!closed ? "":"box-container-closed") + "\">\n");
                        result.append("<div class=\"box-content\">\n");
                        if (!compactView)
                        	result.append("<form class=\"form-horizontal\">\n");
                        else
                            result.append("<form>\n");

                        result.append("<fieldset>\n");
                    }

                    if (!excludeContentInTab) {
                    	
                    
	                    if (firstElement || tabCurrentlyOpened) {
	                        // create table before firelementNamest element or if a tab has been opened before
	                        firstElement = false;
	                    }
	
	                    CmsXmlContentDefinition nestedContentDefinition = contentDefinition;
	                    if (!type.isSimpleType()) {
	                        // get nested content definition for nested types
	                        CmsXmlNestedContentDefinition nestedSchema = (CmsXmlNestedContentDefinition)type;
	                        nestedContentDefinition = nestedSchema.getNestedContentDefinition();
	                    }
	                    // create xpath to the current element
	                    String name = pathPrefix + type.getName();
	
	                    // get the element sequence of the current type
	                    CmsXmlContentValueSequence elementSequence = m_content.getValueSequence(name, getLocale());
	                    int elementCount = elementSequence.getElementCount();
	
	                    // check if value is optional or multiple
	                    boolean addValue = false;
	                    if (elementCount < type.getMaxOccurs()) {
	                        addValue = true;
	                    }
	                    boolean removeValue = false;
	                    if (elementCount > type.getMinOccurs()) {
	                        removeValue = true;
	                    }
	
	                    // assure that at least one element is present in sequence
	                    boolean disabledElement = false;
	                    if (elementCount < 1) {
	                        // current element is disabled, create dummy element
	                        elementCount = 1;
	                        elementSequence.addValue(getCmsObject(), 0);
	                        disabledElement = true;
	                        //m_optionalElementPresent = true;
	                    }
	
	                    boolean isSortable = type.getMaxOccurs()>1;
	                    boolean isOptional = type.getMinOccurs()<1;
	                    
	                    //Si puede haber 0 o mas de 1. pongo un elemento ficticio (por si se borra todo.).
	                    if (isSortable || isOptional )
	                    {
	                    	I_CmsXmlContentValue value = elementSequence.getValue(0);
	                    	if(showActionsButtons){
	                    		result.append("<div id=\""+  getBaseElement(name) + "_empty\" class=\"dragdrop-item\" action-buttons=\""+  getBaseElement(name) + "_empty_buttons\"  pathPrefix=\"" + pathPrefix + "\" element=\"" + type.getName() + "\" idElement=\""+ getIdElement(name) + "\"");
	                    	}else{
	                    		result.append("<div id=\""+  getBaseElement(name) + "_empty\" class=\"dragdrop-item\" pathPrefix=\"" + pathPrefix + "\" element=\"" + type.getName() + "\" idElement=\""+ getIdElement(name) + "\"");
	                    	}
	                    	
	                    	if (!disabledElement)
	                    		result.append(" style=\"display:none;\"" );
	                    	result.append(">\n");
							result.append("<div class=\"control-group\">\n");
							result.append("<label class=\"control-label\" for=\"focusedInput\">");
							result.append(keyDefault(A_CmsWidget.getLabelKey((I_CmsWidgetParameter)value), value.getName()));
							result.append("</label>\n");
							result.append("<div class=\"controls\" id=\""+  getBaseElement(name) + "_empty_buttons\" >\n");
							result.append("<div class=\"control-group\">\n");

							int disponibles =  type.getMaxOccurs();
							
							result.append("<div class=\"btn-toolbar\">\n");
							
							if (disponibles == 1)
								result.append("<a class=\"btn btn-mini btn-success\" rel=\"tooltip\" data-placement=\"top\" data-original-title=\""+keyDefault("GUI_ADD","Agregar")+"\" onclick=\"addFirstItem('" + pathPrefix + "','" + type.getName() + "','.dragdrop-"+ getIdElement(name) +"');\"><i class=\"material-icons\">add</i></a>\n");

							if (disponibles > 1) {							
						
								result.append("\t\t<div class=\"btn-group\" >\n");

								result.append("<a class=\"btn btn-mini btn-success\" rel=\"tooltip\" data-placement=\"top\" data-original-title=\""+keyDefault("GUI_ADD","Agregar")+"\" onclick=\"addFirstItem('" + pathPrefix + "','" + type.getName() + "','.dragdrop-"+ getIdElement(name) +"');\"><i class=\"material-icons\">add</i></a>\n");

								result.append("\t\t<a class=\"btn btn-mini btn-success dropdown-toggle\" data-toggle=\"dropdown\" href=\"\"><i class=\"material-icons \">arrow_drop_down</i></a>\n");
								result.append("\t\t<ul class=\"dropdown-menu\">\n");
							
							
								for (int j=1; j<5 && j<=disponibles;j++)
									result.append("\t\t\t<li><a onclick=\"addFirstItems('" + pathPrefix + "','" + type.getName() + "','.dragdrop-"+ getIdElement(name) +"'," + j + ");\">" + j + "</a></li>\n");
							
								for (int j=5; j<=20 && j<=disponibles;j+=5)
									result.append("\t\t\t<li><a onclick=\"addFirstItems('" + pathPrefix + "','" + type.getName() + "','.dragdrop-"+ getIdElement(name) +"'," + j + ");\">" + j + "</a></li>\n");
							
								result.append("\t\t</ul>\n");

								result.append("\t\t</div>\n");


							}
							
							result.append("</div>\n");
							
							result.append("</div>\n");
							result.append("</div>\n");
							result.append("</div>\n");
							result.append("</div>\n");

							result.append("<div class=\"dragdrop-list dragdrop-"+ getIdElement(name) +"\" max=\"" + type.getMaxOccurs() + "\" min=\"" + type.getMinOccurs() + "\" pathPrefix=\"" + pathPrefix + "\" element=\"" + type.getName() + "\">\n");
							
	                    }
	                    
	                    if (!disabledElement)
	                    // loop through multiple elements
	                    for (int j = 0; j < elementCount; j++) {
	                    	
	                    	 I_CmsXmlContentValue value = elementSequence.getValue(j);
	                    	 
	                    	if (isSortable || isOptional )
	                    	{
	                    		result.append("<div id=\""+ getIdElement(name,value.getIndex()) + "\" "); 
	                    		if(showActionsButtons){
	                    			result.append("class=\"control-group dragdrop-item dragdrop-" + getBaseElement(name) + "-item\"" + " element=\"" + type.getName() + "\"" +
		                    				" dragdrop-item=\"dragdrop-" + getBaseElement(name) + "-item\" dragdrop-empty=\""+  getBaseElement(name) + "_empty\"" +
		                    				" action-buttons=\""+ getIdElement(name,value.getIndex()) + "_buttons\">\n");
	                    		}else{
	                    			result.append("class=\"control-group dragdrop-item dragdrop-" + getBaseElement(name) + "-item\"" + " element=\"" + type.getName() + "\"" +
		                    				" dragdrop-item=\"dragdrop-" + getBaseElement(name) + "-item\" dragdrop-empty=\""+  getBaseElement(name) + "_empty\">\n");
	                    		}
	                    	}
	                    	else //if (!compactView)
		                    		result.append("<div class=\"control-group\">\n");
	
	                        // append element operation (add, remove, move) buttons if required
	                    	
	                    	if(type.getName().equals("publicaciones")){
	                    		
	                    		String proyecto = OpenCmsBaseService.getCurrentSite(getCmsObject());
	                    		int indexVal = value.getIndex() + 1;
	                            String siteName = m_content.getStringValue(getCmsObject(),  value.getPath()+ "/publicacion[" + indexVal + "]", getCmsObject().getRequestContext().getLocale());
	                            boolean isButtonAvailable = true;
	                            
	                            if(siteName!=null && !siteName.isEmpty()){    	
	                	        	TipoEdicionService tService = new TipoEdicionService();
	                				TipoEdicion tEdicion;
	                				int idE = 0;
	                				try {
	                					tEdicion = tService.obtenerTipoEdicion(siteName,proyecto);
	                					idE = tEdicion.getId();
	                				} catch (Exception e) {
	                					e.printStackTrace();
	                				}
	                	        	
	                	        	TfsUserAuditPermission tfsAudit = new TfsUserAuditPermission();
	                	        	String user = getCmsObject().getRequestContext().currentUser().getName();
	                	        	isButtonAvailable = tfsAudit.isPublicationAvailable(user, getCmsObject(), idE);
	                        	}
	                    		
	                    		result.append(buildElementButtons(pathPrefix, type.getName(), value.getIndex(), addValue, isButtonAvailable, type.getMaxOccurs()));
	                    	}else{
	                    		result.append(buildElementButtons(pathPrefix, type.getName(), value.getIndex(), addValue, removeValue, type.getMaxOccurs()));
	                    	}
	                    	// get value and corresponding widget
	                       
	                        //I_CmsWidget widget = null;
	                        //if (type.isSimpleType()) {
	                        //    widget = contentDefinition.getContentHandler().getWidget(value);
	                        //}
	
	                        //String key = value.getPath();
	
	                        
	                        // create label and help bubble cells
	                        if (!collapseLabel) {
	                        	if (!compactView)
	                        		result.append("<label class=\"control-label\" for=\"focusedInput\">\n");
	                        	else
		                            result.append("<label class=\"breakline\">\n");

	                            if (disabledElement) {
	                                // element is disabled, mark it with css
	                                //result.append("Disabled");
	                            }
	                            //result.append("\">");
	
	                            result.append(keyDefault(A_CmsWidget.getLabelKey((I_CmsWidgetParameter)value), value.getName()));
	                            
								if (isSortable)
									result.append("<span class=\"elementIdx\" nicename=\"" + keyDefault(A_CmsWidget.getLabelKey((I_CmsWidgetParameter)value), value.getName()) + "\" element=\"" + type.getName() + "\" position=\"" + (value.getIndex() + 1) + "\">&nbsp;" + (value.getIndex() + 1) + "</span>\n");

	                            //if (elementCount > 1) {
	                            //    result.append(" (").append(value.getIndex() + 1).append(")");
	                            //}
	                            result.append("</label>\n");
	                            if (!compactView)
	                            	result.append("<div class=\"controls\">\n");	                            
	                            
	                        }
	
	                        // append individual widget html cell if element is enabled
	                        if (!disabledElement) {
	                        	if (!type.isSimpleType()) {
	                            //if (widget == null) {
	                                // recurse into nested type sequence
	                                String newPath = CmsXmlUtils.createXpathElement(value.getName(), value.getIndex() + 1);
	                                result.append("<span class=\"sub-item\" content-definition=\"" + value.getName() + "\">");
	                                boolean showHelp = (j == 0);
	                                superTabOpened = !nested && tabOpened && collapseLabel;
	                                result.append(getXmlEditorForm(
	                                    nestedContentDefinition,
	                                    pathPrefix + newPath + "/",
	                                    showHelp,
	                                    superTabOpened,
	                                    null,
	                                    false,
	                                    compactView,
	                                    closed,
	                                    showActionsButtons));
	                                result.append("</span>");
	                            } else {
	                                // this is a simple type, display widget
	                            	result.append(TfsWidgetManager.getWidget(contentDefinition, value).getWidgetHtml(getCmsObject(), this, (I_CmsWidgetParameter)value));
	                            	//result.append("<input class=\"input-xlarge focused\" id=\"focusedInput\" type=\"text\" value=\"\">\n");
	                                //result.append(widget.getDialogWidget(getCmsObject(), this, (I_CmsWidgetParameter)value));
	                            	
	                            }
	                        } else {
	                            // disabled element, show message for optional element
	                            //result.append("<td class=\"xmlTdDisabled maxwidth\">");
	                            //result.append(key(Messages.GUI_EDITOR_XMLCONTENT_OPTIONALELEMENT_0));
	                            //result.append("</td>");
	                        }
	
	
	                        // close row
	                        //result.append("</tr>\n");

// LINEA COMENTADA PARA LA VISTA COMPACTA 10/12/12
//	                        if (!compactView)
	                        	result.append("</div>\n");

	                        if (isSortable || isOptional || !compactView) {
	                      
	                        	result.append("</div>\n");
	                        }
	                    }
	                    
	                    
	                    //Cierro el sortable.
	                    if (isSortable || isOptional)
	                    {
	                    	result.append("</div>\n");
	                    }
                    }
                }
                 // close table
                //result.append("</tabcompactViewle>\n");
                
                if (tabOpened || (!nested && !useTabs)) {
                	//close the last tab opened
                	result.append("</fieldset>\n");
                	
                	result.append("</form>\n");

                	result.append("</div>\n");
                	result.append("</div>\n");
                	result.append("</div>\n");
                }
                
            } catch (Throwable t) {
            }

            return result;
        }
    
    public String getXmlElementEditor(String elementName, String pathPrefix, boolean compactView)
    {
    	if (!compactView)
    		conteinerStyleClass = "default";
    	else
    		conteinerStyleClass = "span12";

    	 return getXmlEditorElementForm(elementName, m_content.getContentDefinition(), pathPrefix, true, false,null,false,false,false,true).toString();
    }
    
    private StringBuffer getXmlEditorElementForm(
    		String elementName,
            CmsXmlContentDefinition contentDefinition,
            String pathPrefix,
            boolean showHelpBubble,
            boolean superTabOpened,
            List<String> tabsToShow,
            boolean showTabsSubSet,
            boolean compactView,
            boolean closed,
            boolean showActionsButtons) {

            StringBuffer result = new StringBuffer(2048);
            
            try {
            	
                boolean nested = CmsStringUtil.isNotEmpty(pathPrefix);
                
                boolean tabOpened = false;
               
                boolean collapseLabel = false;
                boolean firstElement = true;

                for (Iterator<I_CmsXmlSchemaType> i = contentDefinition.getTypeSequence().iterator(); i.hasNext();) {
                   
                    I_CmsXmlSchemaType type = i.next();

                    boolean tabCurrentlyOpened = false;

                    if(type.getName().equals(elementName)){	
                    
	                    if (firstElement || tabCurrentlyOpened) {
	                        firstElement = false;
	                    }
	
	                    CmsXmlContentDefinition nestedContentDefinition = contentDefinition;
	                    if (!type.isSimpleType()) {
	                        CmsXmlNestedContentDefinition nestedSchema = (CmsXmlNestedContentDefinition)type;
	                        nestedContentDefinition = nestedSchema.getNestedContentDefinition();
	                    }

	                    String name = pathPrefix + type.getName();
	
	                    CmsXmlContentValueSequence elementSequence = m_content.getValueSequence(name, getLocale());
	                    int elementCount = elementSequence.getElementCount();
	
	                    boolean addValue = false;
	                    if (elementCount < type.getMaxOccurs()) {
	                        addValue = true;
	                    }
	                    boolean removeValue = false;
	                    if (elementCount > type.getMinOccurs()) {
	                        removeValue = true;
	                    }
	
	                    boolean disabledElement = false;
	                    if (elementCount < 1) {
	                        elementCount = 1;
	                        elementSequence.addValue(getCmsObject(), 0);
	                        disabledElement = true;
	                    }
	
	                    boolean isSortable = type.getMaxOccurs()>1;
	                    boolean isOptional = type.getMinOccurs()<1;
	                    
	                    //Si puede haber 0 o mas de 1. pongo un elemento ficticio (por si se borra todo.).
	                    if (isSortable || isOptional )
	                    {
	                    	I_CmsXmlContentValue value = elementSequence.getValue(0);
	                    	if(showActionsButtons){
	                    		result.append("<div id=\""+  getBaseElement(name) + "_empty\" class=\"dragdrop-item\" action-buttons=\""+  getBaseElement(name) + "_empty_buttons\"  pathPrefix=\"" + pathPrefix + "\" element=\"" + type.getName() + "\" idElement=\""+ getIdElement(name) + "\"");
	                    	}else{
	                    		result.append("<div id=\""+  getBaseElement(name) + "_empty\" class=\"dragdrop-item\" pathPrefix=\"" + pathPrefix + "\" element=\"" + type.getName() + "\" idElement=\""+ getIdElement(name) + "\"");
	                    	}
	                    	
	                    	if (!disabledElement)
	                    		result.append(" style=\"display:none;\"" );
	                    	result.append(">\n");
							result.append("<div class=\"control-group\">\n");
							result.append("<label class=\"control-label\" for=\"focusedInput\">");
							result.append(keyDefault(A_CmsWidget.getLabelKey((I_CmsWidgetParameter)value), value.getName()));
							result.append("</label>\n");
							result.append("<div class=\"controls\" id=\""+  getBaseElement(name) + "_empty_buttons\" >\n");
							result.append("<div class=\"control-group\">\n");

							int disponibles =  type.getMaxOccurs();
							
							result.append("<div class=\" btn-toolbar\">\n");
							
							if (disponibles == 1)
								result.append("<a class=\"btn btn-mini btn-success\" rel=\"tooltip\" data-placement=\"top\" data-original-title=\""+keyDefault("GUI_ADD","Agregar")+"\" onclick=\"addFirstItem('" + pathPrefix + "','" + type.getName() + "','.dragdrop-"+ getIdElement(name) +"');\"><i class=\"material-icons\">add</i></a>\n");

							if (disponibles > 1) {							
						
								result.append("\t\t<div class=\"btn-group\" >\n");

								result.append("<a class=\"btn btn-mini btn-success\" rel=\"tooltip\" data-placement=\"top\" data-original-title=\""+keyDefault("GUI_ADD","Agregar")+"\" onclick=\"addFirstItem('" + pathPrefix + "','" + type.getName() + "','.dragdrop-"+ getIdElement(name) +"');\"><i class=\"material-icons\">add</i></a>\n");

								result.append("\t\t<a class=\"btn btn-mini btn-success dropdown-toggle\" data-toggle=\"dropdown\" href=\"\"><i class=\"material-icons \">arrow_drop_down</i></a>\n");
								result.append("\t\t<ul class=\"dropdown-menu\">\n");
							
							
								for (int j=1; j<5 && j<=disponibles;j++)
									result.append("\t\t\t<li><a onclick=\"addFirstItems('" + pathPrefix + "','" + type.getName() + "','.dragdrop-"+ getIdElement(name) +"'," + j + ");\">" + j + "</a></li>\n");
							
								for (int j=5; j<=20 && j<=disponibles;j+=5)
									result.append("\t\t\t<li><a onclick=\"addFirstItems('" + pathPrefix + "','" + type.getName() + "','.dragdrop-"+ getIdElement(name) +"'," + j + ");\">" + j + "</a></li>\n");
							
								result.append("\t\t</ul>\n");

								result.append("\t\t</div>\n");


							}
							
							result.append("</div>\n");
							
							result.append("</div>\n");
							result.append("</div>\n");
							result.append("</div>\n");
							result.append("</div>\n");

							result.append("<div class=\"dragdrop-list dragdrop-"+ getIdElement(name) +"\" max=\"" + type.getMaxOccurs() + "\" min=\"" + type.getMinOccurs() + "\" pathPrefix=\"" + pathPrefix + "\" element=\"" + type.getName() + "\">\n");
							
	                    }
	                    
	                    if (!disabledElement)
	                    // loop through multiple elements
	                    for (int j = 0; j < elementCount; j++) {
	                    	
	                    	 I_CmsXmlContentValue value = elementSequence.getValue(j);
	                    	 
	                    	if (isSortable || isOptional )
	                    	{
	                    		result.append("<div id=\""+ getIdElement(name,value.getIndex()) + "\" "); 
	                    		if(showActionsButtons){
	                    			result.append("class=\"control-group dragdrop-item dragdrop-" + getBaseElement(name) + "-item\"" + " element=\"" + type.getName() + "\"" +
		                    				" dragdrop-item=\"dragdrop-" + getBaseElement(name) + "-item\" dragdrop-empty=\""+  getBaseElement(name) + "_empty\"" +
		                    				" action-buttons=\""+ getIdElement(name,value.getIndex()) + "_buttons\">\n");
	                    		}else{
	                    			result.append("class=\"control-group dragdrop-item dragdrop-" + getBaseElement(name) + "-item\"" + " element=\"" + type.getName() + "\"" +
		                    				" dragdrop-item=\"dragdrop-" + getBaseElement(name) + "-item\" dragdrop-empty=\""+  getBaseElement(name) + "_empty\">\n");
	                    		}
	                    	}
	                    	else 
		                    		result.append("<div class=\"control-group\">\n");
	
	                    	
	                    	if(type.getName().equals("publicaciones")){
	                    		
	                    		int indexVal = value.getIndex() + 1;
	                            String siteName = m_content.getStringValue(getCmsObject(),  value.getPath()+ "/publicacion[" + indexVal + "]", getCmsObject().getRequestContext().getLocale());
	                            boolean isButtonAvailable = true;
	                            
	                            String proyecto = OpenCmsBaseService.getCurrentSite(getCmsObject());
	                            
	                            if(siteName!=null && !siteName.isEmpty()){	
	                	        	TipoEdicionService tService = new TipoEdicionService();
	                				TipoEdicion tEdicion;
	                				int idE = 0;
	                				try {
	                					tEdicion = tService.obtenerTipoEdicion(siteName,proyecto);
	                					idE = tEdicion.getId();
	                				} catch (Exception e) {
	                					e.printStackTrace();
	                				}
	                	        	
	                	        	TfsUserAuditPermission tfsAudit = new TfsUserAuditPermission();
	                	        	String user = getCmsObject().getRequestContext().currentUser().getName();
	                	        	isButtonAvailable = tfsAudit.isPublicationAvailable(user, getCmsObject(), idE);
	                        	}
	                    		
	                    		result.append(buildElementButtons(pathPrefix, type.getName(), value.getIndex(), addValue, isButtonAvailable, type.getMaxOccurs()));
	                    	}else{
	                    		result.append(buildElementButtons(pathPrefix, type.getName(), value.getIndex(), addValue, removeValue, type.getMaxOccurs()));
	                    	}
	                    	
	                        // create label and help bubble cells
	                        if (!collapseLabel) {
	                        	if (!compactView)
	                        		result.append("<label class=\"control-label\" for=\"focusedInput\">\n");
	                        	else
		                            result.append("<label class=\"breakline\">\n");

	                           
	
	                            result.append(keyDefault(A_CmsWidget.getLabelKey((I_CmsWidgetParameter)value), value.getName()));
	                            
								if (isSortable)
									result.append("<span class=\"elementIdx\" nicename=\"" + keyDefault(A_CmsWidget.getLabelKey((I_CmsWidgetParameter)value), value.getName()) + "\" element=\"" + type.getName() + "\" position=\"" + (value.getIndex() + 1) + "\">&nbsp;" + (value.getIndex() + 1) + "</span>\n");

	                           
	                            result.append("</label>\n");
	                            if (!compactView)
	                            	result.append("<div class=\"controls\">\n");	                            
	                            
	                        }
	
	                        // append individual widget html cell if element is enabled
	                        if (!disabledElement) {
	                        	if (!type.isSimpleType()) {
	                           
	                                String newPath = CmsXmlUtils.createXpathElement(value.getName(), value.getIndex() + 1);
	                                result.append("<span class=\"sub-item\" content-definition=\"" + value.getName() + "\">");
	                                boolean showHelp = (j == 0);
	                                superTabOpened = !nested && tabOpened && collapseLabel;
	                                result.append(getXmlEditorForm(
	                                    nestedContentDefinition,
	                                    pathPrefix + newPath + "/",
	                                    showHelp,
	                                    superTabOpened,
	                                    null,
	                                    false,
	                                    compactView,
	                                    closed,
	                                    showActionsButtons));
	                                result.append("</span>");
	                            } else {
	                               
	                            	result.append(TfsWidgetManager.getWidget(contentDefinition, value).getWidgetHtml(getCmsObject(), this, (I_CmsWidgetParameter)value));
	                            	
	                            }
	                        }
	                        	result.append("</div>\n");

	                        if (isSortable || isOptional || !compactView) {
	                      
	                        	result.append("</div>\n");
	                        }
	                    }
	                    
	                    //Cierro el sortable.
	                    if (isSortable || isOptional)
	                    {
	                    	result.append("</div>\n");
	                    }
                    }
	                    
                }
             
            } catch (Throwable t) {
            }

            return result;
    }
    
    public boolean hasErrors() throws CmsException
    {
    	setEditorValues(getLocale());
    	m_validationHandler = m_content.validate(getCmsObject());
    		return m_validationHandler.hasErrors();
    }
    
    private void replaceMessageErrors () {
    	
    	for ( String key : m_validationHandler.getErrors(getLocale()).keySet()) {
			if (key.toLowerCase().contains("categoria")){
				String[] reference = key.split("/");
				String finalKey = "";
				if (reference.length == 3){
					finalKey = reference[0].substring(0, reference[0].indexOf("[")).toLowerCase() + "-" + reference[1].substring(0, reference[1].indexOf("[")).toLowerCase();
				} else
					finalKey = reference[0].substring(0, reference[0].indexOf("[")).toLowerCase();
				//String value = m_messages.key(finalKey);
				//if (!value.equals("")) {
					
					for (Locale locale : m_validationHandler.getErrors().keySet()) {
						m_validationHandler.getErrors().get(locale).remove(m_validationHandler.getErrors().get(locale).get(key)); 
						m_validationHandler.getErrors().get(locale).put(key, finalKey);
					}
					 
					 
				//}
			}
		}
    }
    
    public Map<String,String> getErrors()
    {
    	if (m_validationHandler.hasErrors())
    		replaceMessageErrors();
    
    	return m_validationHandler.getErrors(getLocale());
    }
    
    
    public void saveTempContent() throws CmsException
    {
    	setEditorValues(getLocale());
    	writeContent();
    }
    
    public void saveContent() throws CmsException
    {
    	setEditorValues(getLocale());
    	writeContent();
    	
    	auditChanges();
    	commitTempFile();
    }
    
    private void auditChanges() {
    	I_ResourceMonitor monitor = ResourceMonitorManager.getInstance().getResourceMonitor(m_file.getTypeId());
    	if (monitor!=null) {
	    	try {
	    		
				monitor.auditChanges(getCloneCms(), m_content, m_resourceName, getLocale());
			} catch (CmsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    public List<CmsResource> addRelatedResourcesToPublish(CmsResource resource, boolean recursivePublish ,List<CmsResource> resourcesList) {
		CmsObject cms = getCmsObject();

		try {

   			int tipoNoticia = OpenCms.getResourceManager().getResourceType("noticia").getTypeId();
   			int tipoVideoLink = OpenCms.getResourceManager().getResourceType("video-link").getTypeId();
   			int tipoVideo = OpenCms.getResourceManager().getResourceType("video").getTypeId();

			int tipoOrigen = resource.getTypeId();
			boolean esVideoOrigen = (tipoOrigen == tipoVideo) || (tipoOrigen == tipoVideoLink);

			List<CmsRelation> relations = cms.getRelationsForResource(cms.getSitePath(resource), CmsRelationFilter.ALL);
   		
			CmsLog.getLog(this).debug("TfsXmlContentEditor - Lista de relaciones: "+relations);
				
       		for ( CmsRelation relation : relations) {
       			
       			try {
       				String rel1 = relation.getTargetPath();
       				String rel2 = relation.getTargetPath();

       				String rel = "";
       				if (rel1.equals(resource.getRootPath()))
       					rel = rel2;
       				else
       					rel = rel1;
       				
       				CmsResource res = cms.readResource(cms.getRequestContext().removeSiteRoot(rel),CmsResourceFilter.ALL);
				
       				CmsResourceState estado = res.getState();

       				if (estado.equals(CmsResourceState.STATE_UNCHANGED))
       					continue;
       				
		    	    int tipo = res.getTypeId();
				
       				boolean esNoticia = (tipo == tipoNoticia);
       				boolean esVideo = (tipo == tipoVideo) || (tipo == tipoVideoLink);
			
       				CmsResourceUtils.forceLockResource(cms,cms.getRequestContext().removeSiteRoot(res.getRootPath()));

       				if (esVideoOrigen && esVideo) {
							if (!resourcesList.contains(res))
								resourcesList.add(res);	
       					
       				}
       				else if (!esNoticia) {
							if (!resourcesList.contains(res))
								resourcesList.add(res);	
						
						if (esVideo)
							addRelatedResourcesToPublish(res,true, resourcesList);
					}
					else if (recursivePublish) {
						if (!resourcesList.contains(res))
							resourcesList.add(res);
						addRelatedResourcesToPublish(res,false, resourcesList);

					}
				
			} catch (CmsException e) {
				CmsLog.getLog(this).error("Error al obtener las relaciones para publicar de la noticia: "+e.getMessage());
		      	}
			}
		}
		catch (CmsException e) {
			CmsLog.getLog(this).error("Error al obtener las relaciones para publicar de la noticia: "+e.getMessage());
		}
       	return resourcesList;

	}
    
    public void publishContent() throws Exception
    {
    	CmsObject cms = getCloneCms();
    
    	List<CmsResource> resources = new ArrayList<CmsResource>();
    	CmsResource res = cms.readResource(m_resourceName,CmsResourceFilter.ALL);    	
    	resources.add(res);
    	
    	//CmsPublishList pList = OpenCms.getPublishManager().getPublishList(cms, resources, true);
		//CmsPublishList pRelated = OpenCms.getPublishManager().getRelatedResourcesToPublish(cms,pList);
		//CmsPublishList pall = OpenCms.getPublishManager().mergePublishLists(
        //                    cms,
        //                    pList ,
        //                    pRelated );
    	
    	List<CmsResource> relatedResources = new ArrayList<CmsResource>();
		for (CmsResource resource : resources ) {
			
			if(resource.getTypeId() == OpenCms.getResourceManager().getResourceType("noticia").getTypeId() ){
					ScriptsJSFilter scriptsJSFilter = new com.tfsla.utils.ScriptsJSFilter(cms,cms.getRequestContext().removeSiteRoot(resource.getRootPath()));
				scriptsJSFilter.cleanResource("noticia");
			}
			
			List<CmsResource> currentRelResources = addRelatedResourcesToPublish(resource,true, new ArrayList<CmsResource>());
			currentRelResources.removeAll(relatedResources);
			currentRelResources.removeAll(resources);
			relatedResources.addAll(currentRelResources);
		}
		
		resources.addAll(relatedResources);
		
		CmsPublishList pList = OpenCms.getPublishManager().getPublishList(cms, resources, false);
		
		CmsLogReport report = new CmsLogReport(getLocale(),this.getClass());
		
        OpenCms.getPublishManager().publishProject(cms, report, pList);
        OpenCms.getPublishManager().waitWhileRunning();
         	
        OpenCms.getPublishManager().publishResource(cms, m_resourceName);
    }
    
    public void scheduledPublishContent() throws CmsException, ParseException
    {
	        // get the request parameters for resource and publish scheduled date
	        String publishScheduledDate = m_request.getParameter("publishDate");
	        String userName = getCmsObject().getRequestContext().currentUser().getName();
	
	        // get the java date format
	        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, getLocale());
	        Date date = dateFormat.parse(publishScheduledDate);
	
	        // check if the selected date is in the future
	        if (date.getTime() < new Date().getTime()) {
	            // the selected date in in the past, this is not possible
	            throw new CmsException(Messages.get().container(Messages.ERR_PUBLISH_SCHEDULED_DATE_IN_PAST_1, date));
	        }
	
	        // make copies from the admin cmsobject and the user cmsobject
	        // get the admin cms object
	        CmsWorkplaceAction action = CmsWorkplaceAction.getInstance();
	        CmsObject cmsAdmin = action.getCmsAdminObject();
	        // get the user cms object
	        CmsObject cms = OpenCms.initCmsObject(getCmsObject());
	
	        // set the current user site to the admin cms object
	        cmsAdmin.getRequestContext().setSiteRoot(cms.getRequestContext().getSiteRoot());
	
	        // create the temporary project, which is deleted after publishing
	        // the publish scheduled date in project name
	        String dateTime = CmsDateUtil.getDateTime(date, DateFormat.SHORT, getLocale());
	        
	        // the resource name to publish scheduled
	        String resName = CmsResource.getName(m_resourceName);
	        String projectName = key(Messages.GUI_PUBLISH_SCHEDULED_PROJECT_NAME_2, new Object[] {resName, dateTime});
	        // the HTML encoding for slashes is necessary because of the slashes in english date time format
	        // in project names slahes are not allowed, because these are separators for organizaional units
	        projectName = projectName.replace("/", "&#47;");
	        // create the project
	        CmsProject tmpProject = cmsAdmin.createProject(
	            projectName,
	            "",
	            CmsRole.WORKPLACE_USER.getGroupName(),
	            CmsRole.PROJECT_MANAGER.getGroupName(),
	            CmsProject.PROJECT_TYPE_TEMPORARY);
	        // make the project invisible for all users
	        tmpProject.setHidden(true);
	        // write the project to the database
	        cmsAdmin.writeProject(tmpProject);
	        // set project as current project
	        cmsAdmin.getRequestContext().setCurrentProject(tmpProject);
	        cms.getRequestContext().setCurrentProject(tmpProject);
			
	        // copy the resource to the project
	        cmsAdmin.copyResourceToProject(m_resourceName);
	
	        // lock the resource in the current project
	        CmsLock lock = cms.getLock(m_resourceName);
	        // prove is current lock from current but not in current project
	        if ((lock != null)
	            && lock.isOwnedBy(cms.getRequestContext().currentUser())
	            && !lock.isOwnedInProjectBy(cms.getRequestContext().currentUser(), cms.getRequestContext().currentProject())) {
	            // file is locked by current user but not in current project
	            // change the lock from this file
	            cms.changeLock(m_resourceName);
	        }
	        // lock resource from current user in current project
	        cms.lockResource(m_resourceName);
	        // get current lock
	        lock = cms.getLock(m_resourceName);
	        
	      
	        CmsResource resource = cms.readResource(cms.getRequestContext().removeSiteRoot(m_resourceName),CmsResourceFilter.ALL);
	                          
	        //Limpiamos los campos del recurso
	        if(resource.getTypeId() == OpenCms.getResourceManager().getResourceType("noticia").getTypeId() ){
	             ScriptsJSFilter scriptsJSFilter = new com.tfsla.utils.ScriptsJSFilter(cms,cms.getRequestContext().removeSiteRoot(resource.getRootPath()));
	             scriptsJSFilter.cleanResource("noticia");
	        }
	        
	        // Agreagamos tambien los recursos relacionados
	        List<CmsResource> relatedResources = new ArrayList<CmsResource>();
            relatedResources = addRelatedResourcesToPublish(resource,true, new ArrayList<CmsResource>());
				
	        for (CmsResource relatedResource : relatedResources ) {
	        	
	        	// Se limpian las noticias relacionadas si las tiene
	        	if(relatedResource.getTypeId() == OpenCms.getResourceManager().getResourceType("noticia").getTypeId() ){
					ScriptsJSFilter scriptsJSFilter = new com.tfsla.utils.ScriptsJSFilter(cms,cms.getRequestContext().removeSiteRoot(relatedResource.getRootPath()));
					scriptsJSFilter.cleanResource("noticia");
	        	}
	        	
	        	String rel_resourceName = cms.getRequestContext().removeSiteRoot(relatedResource.getRootPath());
	        	cmsAdmin.copyResourceToProject(rel_resourceName); 
	        	CmsLock lockRel = cms.getLock(rel_resourceName);
	            
	            if ((lockRel != null)
	                && lockRel.isOwnedBy(cms.getRequestContext().currentUser())
	                && !lockRel.isOwnedInProjectBy(cms.getRequestContext().currentUser(), cms.getRequestContext().currentProject())) {
	                cms.changeLock(rel_resourceName);
	            }
	
	            cms.lockResource(rel_resourceName);
	        }
	
	        // create a new scheduled job
	        CmsScheduledJobInfo job = new CmsScheduledJobInfo();
	        // the job name
	        String jobName = projectName;
	        // set the job parameters
	        job.setJobName(jobName);
	        job.setClassName("org.opencms.scheduler.jobs.CmsPublishScheduledJob");
	        // create the cron expression
	        Calendar calendar = Calendar.getInstance();
	        calendar.setTime(date);
	        String cronExpr = ""
	            + calendar.get(Calendar.SECOND)
	            + " "
	            + calendar.get(Calendar.MINUTE)
	            + " "
	            + calendar.get(Calendar.HOUR_OF_DAY)
	            + " "
	            + calendar.get(Calendar.DAY_OF_MONTH)
	            + " "
	            + (calendar.get(Calendar.MONTH) + 1)
	            + " "
	            + "?"
	            + " "
	            + calendar.get(Calendar.YEAR);
	        // set the cron expression
	        job.setCronExpression(cronExpr);
	        // set the job active
	        job.setActive(true);
	        // create the context info
	        CmsContextInfo contextInfo = new CmsContextInfo();
	        contextInfo.setProjectName(projectName);
	        
	        contextInfo.setUserName(cms.getRequestContext().currentUser().getName());
	        //contextInfo.setUserName(cmsAdmin.getRequestContext().currentUser().getName());
	        
	        // create the job schedule parameter
	        SortedMap<String, String> params = new TreeMap<String, String>();
	        // the user to send mail to
	        params.put(CmsPublishScheduledJob.PARAM_USER, userName);
	        // the job name
	        params.put(CmsPublishScheduledJob.PARAM_JOBNAME, jobName);
	        // the link check
	        params.put(CmsPublishScheduledJob.PARAM_LINKCHECK, "true");
	        // add the job schedule parameter
	        job.setParameters(params);
	        // add the context info to the scheduled job
	        job.setContextInfo(contextInfo);
	        // add the job to the scheduled job list
	        OpenCms.getScheduleManager().scheduleJob(cmsAdmin, job);
	        OpenCms.writeConfiguration(CmsSchedulerConfiguration.class);
	        
    }

    public void removeAllElement()
    {
    	String pathElement = m_request.getParameter("name"); // ej: imagenesFotogaleria
    	int count = m_content.getIndexCount(pathElement, getLocale());
    	for (int j=0;j<count;j++)
    		m_content.removeValue(pathElement, getLocale(), 0);
    	
        try {
			writeContent();
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void moveElement()
    {
    	
    	//LOG.debug("moveElement >> antes");
    	//LOG.debug(m_content.toString());

    	String pathElement = m_request.getParameter("name"); // ej: imagenesFotogaleria

    	String currrentpositionElement = m_request.getParameter("currentPosition"); 
    	String newPositionElement = m_request.getParameter("newPosition"); 


    	int currIdx = 0;
    	
    	if (currrentpositionElement!=null)
    		currIdx = Integer.parseInt(currrentpositionElement)-1;

    	int newIdx = 0;
    	
    	if (newPositionElement!=null)
    		newIdx = Integer.parseInt(newPositionElement)-1;

    	int it = currIdx-newIdx;

    	//LOG.debug("moveElement > " + pathElement + " --> " + currrentpositionElement + " a " + newPositionElement);
    	//LOG.debug("moveElement > " + pathElement + " --> idx " + currIdx + " a " + newIdx);

        I_CmsXmlContentValue value = m_content.getValue(pathElement, getLocale(), currIdx);
        //LOG.debug("element " + value.getElement().asXML().toString());
        
    	if (it>0)
    	{
    		for (;it>0;it--){
    			value.moveDown();
    			//move up!
    			//LOG.debug("moveElement > " + pathElement + " --> move up!");
    			//LOG.debug("new index position " + value.getIndex());
    		}
    	}
    	else {
    		//move down!
    		for (;it<0;it++) {
    			value.moveUp();
    			//LOG.debug("moveElement > " + pathElement + " --> move down!");
    			//LOG.debug("new index position " + value.getIndex());
    		}
    	}
    	
        try {
			writeContent();
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        //LOG.debug("moveElement >> despues");
        //LOG.debug(m_content.toString());
    }
    
    public String replaceElement()
    {
    	String pathElement = m_request.getParameter("name"); // ej: imagenesFotogaleria
    	String parentPath = m_request.getParameter("parentPath"); // ej raiz: ""
    	String elementName = m_request.getParameter("element"); // ej: imagenesFotogaleria

    	int count = m_content.getIndexCount(pathElement, getLocale());
    	for (int j=0;j<count;j++)
    		m_content.removeValue(pathElement, getLocale(), 0);

    	int elementCount = 1;
    	try {
    		elementCount =Integer.parseInt(m_request.getParameter("count"));
    	}
    	catch (NumberFormatException e) {
		}
    	
    	boolean compactView = false;
    	
    	if (m_request.getParameter("compact")!=null && m_request.getParameter("compact").equals("true"))
    		compactView = true;

    	
    	String fullName = parentPath + elementName;
    	
//    	if (m_content.hasValue(fullName, getLocale())) {
//            // when other values are present, increase index to use right position
//            index += 1;
//        }
    	
    	int index = m_content.getIndexCount(fullName, getLocale());
    	
    	for (int j=0;j<elementCount;j++)
    		m_content.addValue(getCmsObject(), fullName, getLocale(), index);
        
        CmsXmlContentDefinition contentdefinition = m_content.getContentDefinition();
        
        if (!parentPath.equals(""))
        	contentdefinition = contentdefinition.getSchemaType(parentPath + elementName).getContentDefinition();
        
        try {
			writeContent();
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        String content = "";
        for (int j=0;j<elementCount;j++)
        	content += getXmlEditorSubForm(contentdefinition,parentPath,elementName,index+j,true,compactView).toString();
        
        return content;

    }
    
    public void removeElement()
    {
    	//LOG.error("removeElement >> antes");
    	//LOG.error(m_content.toString());

    	String pathElement = m_request.getParameter("name"); // ej: imagenesFotogaleria

    	String positionElement = m_request.getParameter("position"); 
    	
    	int idx = 0;
    	
    	if (positionElement!=null)
    		idx = Integer.parseInt(positionElement)-1;
    	
    	m_content.removeValue(pathElement, getLocale(), idx);
    
        try {
			writeContent();
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    	//LOG.error("removeElement >> despues");
    	//LOG.error(m_content.toString());

    }
    
    public String compositeAddElements() throws JSONException
    {
    	JSONObject json = new JSONObject();

    	JSONArray jsonItems = new JSONArray();

    	boolean compactView = false;
    	
    	if (m_request.getParameter("compact")!=null && m_request.getParameter("compact").equals("true"))
    		compactView = true;

    	String[] actions = m_request.getParameterValues("actions");
    	String[] elements = m_request.getParameterValues("element");
    	String[] parentPaths = m_request.getParameterValues("parentPath"); // ej raiz: ""
    	String[] count = m_request.getParameterValues("count");
    	
    	int actionIdx = 0;
    	for (String action : actions) {
			String elementName = elements[actionIdx];
			String parentPath = parentPaths[actionIdx];
	    	String fullName = parentPath + elementName;

			int elementCount = 1;
	    	try {
	    		elementCount =Integer.parseInt(count[actionIdx]);
	    	}
	    	catch (NumberFormatException e) {
			}

	    	if (action.equals("replace"))
	    	{
	        	int currentCount = m_content.getIndexCount(fullName, getLocale());
	        	for (int j=0;j<currentCount;j++)
	        		m_content.removeValue(fullName, getLocale(), 0);

	    	}
	    	
    		if (action.equals("add") || action.equals("replace")){
    	    	
    	    	int index = m_content.getIndexCount(fullName, getLocale());
    	    	
    	    	for (int j=0;j<elementCount;j++)
    	    		m_content.addValue(getCmsObject(), fullName, getLocale(), index);
    	        
    	        CmsXmlContentDefinition contentdefinition = m_content.getContentDefinition();
    	        
    	        if (!parentPath.equals(""))
    	        	contentdefinition = contentdefinition.getSchemaType(parentPath + elementName).getContentDefinition();
    	    	

    	        String content = "";
    	        for (int j=0;j<elementCount;j++)
    	        	content += getXmlEditorSubForm(contentdefinition,parentPath,elementName,index+j,true,compactView).toString();

    	        JSONObject jsonitem = new JSONObject();
    	        jsonitem.put("value", content);
    	        jsonitem.put("name", fullName);
    	        
    	        jsonItems.put(jsonitem);
    		}
    		
    		actionIdx++;
    	}

    	json.put("elements", jsonItems);
        try {
			writeContent();
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	return json.toString();
    }   
    
    public String addElement()
    {
    	//LOG.error("addElement >> antes");
        //LOG.error(m_content.toString());

    	
    	String parentPath = m_request.getParameter("parentPath"); // ej raiz: ""
    	String elementName = m_request.getParameter("element"); // ej: imagenesFotogaleria

    	int elementCount = 1;
    	try {
    		elementCount =Integer.parseInt(m_request.getParameter("count"));
    	}
    	catch (NumberFormatException e) {
		}
    	
    	boolean compactView = false;
    	
    	if (m_request.getParameter("compact")!=null && m_request.getParameter("compact").equals("true"))
    		compactView = true;

    	
    	String fullName = parentPath + elementName;
    	
    	int index = 0;
    	if (m_request.getParameter("index")!=null)
	    	try {
	    		index =Integer.parseInt(m_request.getParameter("index"));
	    	}
	    	catch (NumberFormatException e) {
	        	index = m_content.getIndexCount(fullName, getLocale());
	    	}
    	else {
        	index = m_content.getIndexCount(fullName, getLocale());
    	}
    	
    	try{
    		for (int j=0;j<elementCount;j++){   		
        		m_content.addValue(getCmsObject(), fullName, getLocale(), index);
        	}
    	}catch(Exception ex){
    		ex.printStackTrace();
    		LOG.error("Error Importador Noticias " + fullName + " " + index);
    	}
    	
        CmsXmlContentDefinition contentdefinition = m_content.getContentDefinition();
        
        if (!parentPath.equals(""))
        	contentdefinition = contentdefinition.getSchemaType(parentPath + elementName).getContentDefinition();

        
        try {
			writeContent();
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	//LOG.error("addElement >> despues");
        //LOG.error(m_content.toString());

        String content = "";
        for (int j=0;j<elementCount;j++)
        	content += getXmlEditorSubForm(contentdefinition,parentPath,elementName,index+j,true,compactView).toString();
        
        return content;
    }
    
    private StringBuffer getXmlEditorSubForm(
            CmsXmlContentDefinition contentDefinition,
            String pathPrefix,
            String elementName,
            int index,
            boolean showHelpBubble,
            boolean compactView) {

        StringBuffer result = new StringBuffer(2048);
        
        try {
        	
   	
            // check if we are in a nested content definition
            //boolean nested = CmsStringUtil.isNotEmpty(pathPrefix);
            
           
            boolean collapseLabel = false;
            
            // iterate the type sequence        
            for (Iterator<I_CmsXmlSchemaType> i = contentDefinition.getTypeSequence().iterator(); i.hasNext();) {
                // get the type
                I_CmsXmlSchemaType type = i.next();

                if (type.getName().equals(elementName)) {
                	
                
                    CmsXmlContentDefinition nestedContentDefinition = contentDefinition;
                    if (!type.isSimpleType()) {
                        // get nested content definition for nested types
                        CmsXmlNestedContentDefinition nestedSchema = (CmsXmlNestedContentDefinition)type;
                        nestedContentDefinition = nestedSchema.getNestedContentDefinition();
                    }
                    // create xpath to the current element
                    String name = pathPrefix + type.getName();

                    // get the element sequence of the current type
                    CmsXmlContentValueSequence elementSequence = m_content.getValueSequence(name, getLocale());
                    int elementCount = elementSequence.getElementCount();

                    // check if value is optional or multiple
                    boolean addValue = false;
                    if (elementCount < type.getMaxOccurs()) {
                        addValue = true;
                    }
                    boolean removeValue = false;
                    if (elementCount > type.getMinOccurs()) {
                        removeValue = true;
                    }


                    boolean isSortable = type.getMaxOccurs()>1;
                    boolean isOptional = type.getMinOccurs()<1;
                    
                    	
                    	 I_CmsXmlContentValue value = elementSequence.getValue(index);
                    	 
                    	if (isSortable || isOptional )
                    	{
                    		result.append("<div id=\""+ getIdElement(name,value.getIndex()) + "\" "); 
                    		result.append("class=\"control-group dragdrop-item dragdrop-" + getBaseElement(name) + "-item\"" + " element=\"" + type.getName() + "\"" +
                    				" dragdrop-item=\"dragdrop-" + getBaseElement(name) + "-item\" dragdrop-empty=\""+  getBaseElement(name) + "_empty\"" +
                    				" action-buttons=\""+ getIdElement(name,value.getIndex()) + "_buttons\">\n");
                    	}
                    	else if (!compactView)
	                    		result.append("<div class=\"control-group\">\n");


                        // append element operation (add, remove, move) buttons if required
                    	
                    	result.append(buildElementButtons(pathPrefix, type.getName(), value.getIndex(), addValue, removeValue, type.getMaxOccurs()));
                    	
                    	
                                  // create label and help bubble cells
                        if (!collapseLabel) {
                        	if (!compactView)
                        		result.append("<label class=\"control-label\" for=\"focusedInput\">\n");
                        	else
	                            result.append("<label class=\"breakline\">\n");


                            result.append(keyDefault(A_CmsWidget.getLabelKey((I_CmsWidgetParameter)value), value.getName()));
							if (isSortable || !type.isSimpleType())
								result.append("<span class=\"elementIdx\" element=\"" + type.getName() + "\" nicename=\"" + keyDefault(A_CmsWidget.getLabelKey((I_CmsWidgetParameter)value), value.getName()) + "\" position=\"" + (value.getIndex() + 1) + "\">&nbsp;</span>\n");

                            //if (elementCount > 1) {
                            //    result.append(" (").append(value.getIndex() + 1).append(")");
                            //}
                            result.append("</label>\n");
                            if (!compactView)
                            	result.append("<div class=\"controls\">\n");
                            
                        }

                        // append individual widget html cell if element is enabled
                        	if (!type.isSimpleType()) {
                            //if (widget == null) {
                                // recurse into nested type sequence
                                
                        		 result.append("<span class=\"sub-item\" content-definition=\"" + value.getName() + "\">");
                        		 
                        		String newPath = CmsXmlUtils.createXpathElement(value.getName(), value.getIndex() + 1);
                                
                                boolean showHelp = (index == 0);
                                result.append(getXmlEditorForm(
                                    nestedContentDefinition,
                                    pathPrefix + newPath + "/",
                                    showHelp,
                                    false,
                                    null,
                                    false,
                                    compactView,
                                    false,
                                    true));
                                result.append("</span>");
                            } else {
                                // this is a simple type, display widget
                            	result.append(TfsWidgetManager.getWidget(contentDefinition, value).getWidgetHtml(getCmsObject(), this, (I_CmsWidgetParameter)value));
                            	
                            }


                        if (!compactView) {
                        	result.append("</div>\n");                      
                        	result.append("</div>\n");
                        }
                    }
                    
            }
                        
        } catch (Throwable t) {
        }

        return result;
    }

    private Map<String,String> uids = new HashMap<String,String>();
    
    private String getUID(String idElementName)
    {
    	String uid = uids.get(idElementName);
    	if (uid==null) {
    		uid = idElementName + "_" + new Date().getTime();
    		uids.put(idElementName, uid);
    	}
    	return uid;
    }
    
    private String getBaseElement(String elementName)
    {
    	 StringBuffer result = new StringBuffer(128);
         // the '[', ']' and '/' chars from the xpath are invalid for html id's
         result.append(elementName.replace('[', '_').replace(']', '_').replace('/', '.'));
         
         return result.toString();
    }
    
    public void setEditorValues(Locale locale) throws CmsXmlException {

        List valueNames = getSimpleValueNames(m_content.getContentDefinition(), "", locale);
        Iterator i = valueNames.iterator();
        while (i.hasNext()) {
            String valueName = (String)i.next();
            I_CmsXmlContentValue value = m_content.getValue(valueName, locale);
            
            I_TfsWidget widget = TfsWidgetManager.getWidget(value.getContentDefinition(), value);
            widget.setEditorValue(getCmsObject(), m_request.getParameterMap(), this, (I_CmsWidgetParameter)value);
        }
    }
    
    private List getSimpleValueNames(CmsXmlContentDefinition contentDefinition, String pathPrefix, Locale locale) {

        List valueNames = new ArrayList();
        Iterator i = contentDefinition.getTypeSequence().iterator();
        while (i.hasNext()) {

            I_CmsXmlSchemaType type = (I_CmsXmlSchemaType)i.next();

            String name = pathPrefix + type.getName();

            // get the element sequence of the type
            CmsXmlContentValueSequence elementSequence = m_content.getValueSequence(name, locale);
            int elementCount = elementSequence.getElementCount();
            // loop through elements
            for (int j = 0; j < elementCount; j++) {
                I_CmsXmlContentValue value = elementSequence.getValue(j);

                StringBuffer xPath = new StringBuffer(pathPrefix.length() + 16);
                xPath.append(pathPrefix);
                xPath.append(CmsXmlUtils.createXpathElement(type.getName(), value.getIndex() + 1));

                if (!type.isSimpleType()) {
                    // recurse into nested type sequence
                    CmsXmlNestedContentDefinition nestedSchema = (CmsXmlNestedContentDefinition)type;
                    xPath.append("/");
                    valueNames.addAll(getSimpleValueNames(
                        nestedSchema.getNestedContentDefinition(),
                        xPath.toString(),
                        locale));
                } else {
                    // this is a simple type, get widget to display
                    valueNames.add(xPath.toString());
                }
            }
        }
        
        return valueNames;
    }

    public String getIdElement(String elementName)
    {
        StringBuffer result = new StringBuffer(128);
        // the '[', ']' and '/' chars from the xpath are invalid for html id's
        result.append(getUID(elementName.replace('[', '_').replace(']', '_').replace('/', '.')));
        
        return result.toString();
    }

    private String getIdElement(String elementName, int index)
    {
        StringBuffer result = new StringBuffer(128);
        // the '[', ']' and '/' chars from the xpath are invalid for html id's
        result.append(getUID(elementName.replace('[', '_').replace(']', '_').replace('/', '.') + '.' + index));
        //result.append('.');
        //result.append(index);
        return result.toString();
    }
    
    private String buildElementButtons(String pathPrefix, String type, int index, boolean addElement, boolean removeElement, int maxElements) {
    	
    	String elementName = pathPrefix + type;
    	
        StringBuffer jsCall = new StringBuffer(512);

        
        // indicates if at least one button is active
        boolean buttonPresent = false;

        if (removeElement)
            buttonPresent = true;

        if (index > 0)
            buttonPresent = true;

        // build the move up button (move up in API is move down for content editor)
        int indexCount = m_content.getIndexCount(elementName, getLocale());
        if (index < (indexCount - 1))
            buttonPresent = true;

        // build the add element button if required
        if (addElement)
            buttonPresent = true;

        if (buttonPresent) {
        	
        	int disponibles = maxElements - indexCount;
        	
        	jsCall.append("<div id=\""+ getIdElement(elementName,index) + "_buttons\" class=\"control-buttons span12\" >\n"); 
			jsCall.append("\t<div class=\" btn-toolbar\">\n");
			jsCall.append("\t\t<a" + (indexCount > 1 ? "" : " style=\"display: none;\"") + " class=\"btn btn-mini  btn-move\" rel=\"tooltip\" data-placement=\"top\" data-original-title=\""+keyDefault("GUI_MOV","Mover")+"\"><i class=\"material-icons dragdrop-image-handle\">reorder</i></a>\n");	
			
			jsCall.append("\t\t<a" + (removeElement ? "" : " style=\"display: none;\"") + " class=\"btn btn-mini btn-remove\" rel=\"tooltip\" data-placement=\"top\" data-original-title=\""+keyDefault("GUI_REMOVE","Quitar")+"\" onclick=\"removeItem('" + pathPrefix + type + "','#"+ getIdElement(elementName,index) + "');\"><i class=\"material-icons\">delete</i></a>\n");
					
			jsCall.append("\t\t<a" + (disponibles == 1 ? "" : " style=\"display: none;\"") + " class=\"btn btn-mini btn-add\" rel=\"tooltip\" data-placement=\"top\" data-original-title=\""+keyDefault("GUI_ADD","Agregar")+"\" onclick=\"addItem('" + pathPrefix + "','" + type + "','#"+ getIdElement(elementName,index) + "');\"><i class=\"material-icons\">add</i></a>\n");
			
			jsCall.append("\t\t<div" + (disponibles > 1 ? "" : " style=\"display: none;\"") + " class=\" btn-group btn-addmassive\">\n");
			
			jsCall.append("\t\t<a class=\"btn btn-mini  \" rel=\"tooltip\" data-placement=\"top\" data-original-title=\""+keyDefault("GUI_ADD","Agregar")+"\" onclick=\"addItem('" + pathPrefix + "','" + type + "','#"+ getIdElement(elementName,index) + "');\"><i class=\"material-icons\">add</i></a>\n");
			
			jsCall.append("\t\t<a class=\"btn btn-mini  dropdown-toggle\" data-toggle=\"dropdown\" href=\"\"><i class=\"material-icons \">arrow_drop_down</i></a>\n");
			jsCall.append("\t\t<ul class=\"dropdown-menu\">\n");
			
			
			for (int j=1; j<5;j++)
				jsCall.append("\t\t\t<li" + (disponibles >= j ? "" : " style=\"display: none;\"") + " ><a onclick=\"addItems('" + pathPrefix + "','" + type + "','#"+ getIdElement(elementName,index) + "'," + j + ");\">" + j + "</a></li>\n");
		
			for (int j=5; j<=20;j+=5)
				jsCall.append("\t\t\t<li" + (disponibles >= j ? "" : " style=\"display: none;\"") + " ><a onclick=\"addItems('" + pathPrefix + "','" + type + "','#"+ getIdElement(elementName,index) + "'," + j + ");\">" + j + "</a></li>\n");
		
			jsCall.append("\t\t</ul>\n");
			jsCall.append("\t\t</div>\n");
			
			jsCall.append("\t</div>\n");
			jsCall.append("</div>\n");

        }                

        return jsCall.toString();
    }

    public String key(String keyName) {

        return getMessages().key(keyName);
    }
    
    public String key(String keyName, Object[] params) {

        return getMessages().key(keyName, params);
    }


	public String getConteinerStyleClass() {
		return conteinerStyleClass;
	}

	public void setConteinerStyleClass(String conteinerStyleClass) {
		this.conteinerStyleClass = conteinerStyleClass;
	}

	public Map<String, String> getEditorValues() {
		return editorValues;
	}

	public void setEditorValues(Map<String, String> editorValues) {
		this.editorValues = editorValues;
	}

	public CmsXmlContent getXmlContent() {
		return m_content;
	}

	public Locale getUserLocale() {
		return m_userLocale;
	}

	public void setUserLocale(Locale m_userLocale) {
		this.m_userLocale = m_userLocale;
	}


}
