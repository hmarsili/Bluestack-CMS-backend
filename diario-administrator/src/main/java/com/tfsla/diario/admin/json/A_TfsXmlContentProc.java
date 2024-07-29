package com.tfsla.diario.admin.json;

import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.commons.Messages;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.utils.CmsResourceUtils;


public class A_TfsXmlContentProc {

	protected static final Log LOG = CmsLog.getLog(A_TfsXmlContentProc.class);
	
	protected CmsFile m_file;
	protected Locale m_elementLocale;
	protected String m_fileEncoding;
	protected String m_resourceName;
	protected CmsXmlContent m_content;
	protected CmsMultiMessages m_messages;
	protected CmsObject cms;
	
	private String m_tempFileName;
	private CmsUUID m_currentProjectId;
	private CmsObject m_cloneCms = null;
	
	public void setTempFileName(String tempFileName)
    {
    	m_tempFileName = tempFileName;
    }
    
    public String getTempFileName()
    {
    	return m_tempFileName;
    }
	
	public A_TfsXmlContentProc() {
		super();
	}

	protected String getFileEncoding() {
	
	    return m_fileEncoding;
	}

	protected void setFileEncoding(String value) {
	    m_fileEncoding = CmsEncoder.lookupEncoding(value, value);
	}

	public String getResourceName() {
		return m_resourceName;
	}

	public void setResourceName(String resourceName) {
		m_resourceName = resourceName;
	}

	protected String getFileEncoding(CmsObject cms, String filename) {
	
	    try {
	        return cms.readPropertyObject(filename, CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, true).getValue(
	            OpenCms.getSystemInfo().getDefaultEncoding());
	    } catch (CmsException e) {
	        return OpenCms.getSystemInfo().getDefaultEncoding();
	    }
	}

	public Locale getLocale() {
		return m_elementLocale;
	}

	public void setLocale(Locale locale) {
		m_elementLocale = locale;
	}
	
	public void unlock() throws CmsException {
		cms.unlockResource(m_resourceName);	
    }

    public void lock(CmsLockType type) throws CmsException {
        CmsResource res = cms.readResource(m_resourceName, CmsResourceFilter.ALL);
        CmsLock lock = cms.getLock(res);
        boolean lockable = lock.isLockableBy(cms.getRequestContext().currentUser());

        if (OpenCms.getWorkplaceManager().autoLockResources()) {
            // autolock is enabled, check the lock state of the resource
            if (lockable) {
                // resource is lockable, so lock it automatically
                if (type == CmsLockType.TEMPORARY) {
                	cms.lockResourceTemporary(m_resourceName);
                } else {
                	cms.lockResource(m_resourceName);
                }
            } else {
                throw new CmsException(org.opencms.workplace.Messages.get().container(org.opencms.workplace.Messages.ERR_WORKPLACE_LOCK_RESOURCE_1, m_resourceName));
            }
        } else {
            if (!lockable) {
                throw new CmsException(org.opencms.workplace.Messages.get().container(org.opencms.workplace.Messages.ERR_WORKPLACE_LOCK_RESOURCE_1, m_resourceName));
            }
        }
    }
    
    public void retriveMessages() {
    	
    	if (getLocale()==null)
    	    	setLocale(cms.getRequestContext().getLocale());
    	
    	// initialize messages            
	    CmsMessages messages = OpenCms.getWorkplaceManager().getMessages(getLocale());
	    // generate a new multi messages object and add the messages from the workplace
	    
	    m_messages = new CmsMultiMessages(getLocale());
	    m_messages.addMessages(messages);
    }

    public CmsMultiMessages getMessages()
    {
    	return m_messages;
    }

    public void edit() throws CmsException{

		try {
    		
	    	if (!cms.existsResource(m_resourceName, CmsResourceFilter.ALL)) {
	    		throw new CmsVfsResourceNotFoundException(Messages.get().container(
	                    Messages.ERR_RESOURCE_DOES_NOT_EXIST_3,
	                    m_resourceName,
	                    cms.getRequestContext().currentProject().getName(),
	                    cms.getRequestContext().getSiteRoot()));
	    	}
	    	
	    	checkLock(m_resourceName, CmsLockType.TEMPORARY);
	    	
	    	setTempFileName(CmsWorkplace.getTemporaryFileName(m_resourceName));
	    	
	        if (cms.existsResource(getTempFileName(), CmsResourceFilter.ALL)) {
	            // delete old temporary file
	            if (!cms.getLock(getTempFileName()).isUnlocked()) {
	                // steal lock
	            	cms.changeLock(getTempFileName());
	            } else {
	                // lock resource to current user
	            	cms.lockResource(getTempFileName());
	            }
	        }
	        else
	        	createTempFile();
        
	        m_file = cms.readFile(this.getTempFileName(), CmsResourceFilter.ALL);
	        m_content = CmsXmlContentFactory.unmarshal(getCloneCms(), m_file);

    	}
    	catch (CmsException e) {
    		LOG.error("Error al intentar editar la noticia " + m_resourceName  , e);
			throw e;
		}
		
    }
    
    public void endEdit(String keepLocked) {
    	discardTemporalFile();
		
		try {
			if ( keepLocked!=null && keepLocked.equals("true")) {
				cms.lockResource(getResourceName());
			} else {
				cms.unlockResource(getResourceName());
			}
		} catch (CmsException e) {
			        
		}
		
    }
    
    protected String createTempFile() throws CmsException {
    	CmsProject project = cms.getRequestContext().currentProject();
        m_currentProjectId = project.getUuid();
        return OpenCms.getWorkplaceManager().createTempFile(cms, getResourceName(), m_currentProjectId);
    }
    
    protected void deleteTempFile() {

        try {
            // switch to the temporary file project
            switchToTempProject();
            // delete the temporary file
            cms.deleteResource(getTempFileName(), CmsResource.DELETE_PRESERVE_SIBLINGS);
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
    
    protected CmsUUID switchToTempProject() throws CmsException {
        // store the current project id in member variable
    	CmsProject project = cms.getRequestContext().currentProject();
        m_currentProjectId = project.getUuid();
        CmsUUID tempProjectId = OpenCms.getWorkplaceManager().getTempFileProjectId();
        cms.getRequestContext().setCurrentProject(cms.readProject(tempProjectId));
        return tempProjectId;
    }
    
    protected void switchToCurrentProject() throws CmsException {

        if (m_currentProjectId != null) {
            // switch back to the current users project
        	cms.getRequestContext().setCurrentProject(cms.readProject(m_currentProjectId));
        }
    }
    
    public void checkLock(String resource, CmsLockType type) throws CmsException {

        CmsResource res = cms.readResource(resource, CmsResourceFilter.ALL);
        CmsLock lock = cms.getLock(res);
        boolean lockable = lock.isLockableBy(cms.getRequestContext().currentUser());

        if (OpenCms.getWorkplaceManager().autoLockResources()) {
            // autolock is enabled, check the lock state of the resource
            if (lockable) {
                // resource is lockable, so lock it automatically
                if (type == CmsLockType.TEMPORARY) {
                	cms.lockResourceTemporary(resource);
                } else {
                	cms.lockResource(resource);
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
	    	
	    	checkLock(m_resourceName, CmsLockType.TEMPORARY);
	    	
	    	setTempFileName(CmsWorkplace.getTemporaryFileName(m_resourceName));
	    	
	        return (cms.existsResource(getTempFileName(), CmsResourceFilter.ALL));
    	}
    	catch (CmsException e) {
    		return false;
    	}

    }
    
    public void discardTemporalFile() {
    	try {
	    	
	    	checkLock(m_resourceName, CmsLockType.TEMPORARY);
	    	
	    	setTempFileName(CmsWorkplace.getTemporaryFileName(m_resourceName));
	    	
	        if (cms.existsResource(getTempFileName(), CmsResourceFilter.ALL)) {
	            if (!cms.getLock(getTempFileName()).isUnlocked()) {
	                // steal lock
	            	cms.changeLock(getTempFileName());
	            } else {
	                // lock resource to current user
	            	cms.lockResource(getTempFileName());
	            }
	        	deleteTempFile();
	        }
    	}
    	catch (CmsException e) {
    		LOG.error("Error discarting temporal file " + getTempFileName(),e);
    	}
    }
    
    
    public void cancelEdit() {
    	try {
    		CmsResourceUtils.createPropertyInShadowMode(cms, cms.readResource(getTempFileName()),
    				"disardChanges", "true");
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    protected CmsObject getCloneCms() throws CmsException {

        if (m_cloneCms == null) {
            m_cloneCms = OpenCms.initCmsObject(cms);
            m_cloneCms.getRequestContext().setRequestTime(CmsResource.DATE_RELEASED_EXPIRED_IGNORE);
        }
        return m_cloneCms;
    }
    
    public void commitTempFile() throws CmsException {

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
        	switchToTempProject();
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
}