package com.tfsla.diario.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.jsp.PageContext;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceSettings;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.opencms.webusers.TfsUserHelper;
import com.tfsla.opencms.webusers.openauthorization.common.ProviderConfigurationLoader;
import com.tfsla.opencms.webusers.openauthorization.common.ProviderField;
import com.tfsla.opencms.webusers.openauthorization.common.exceptions.InvalidConfigurationException;
import com.tfsla.utils.CmsResourceUtils;

public class TfsUserAdmin {

    private CmsFlexController m_controller;
    private HttpSession m_session;
    
    private String siteName;
    private TipoEdicion currentPublication;
    private String publication;
    private String moduleConfigName;
    private CPMConfig config;
    
    private CmsMultiMessages m_messages;
    private CmsWorkplaceSettings m_settings;
    private Locale m_elementLocale;
    
	public TfsUserAdmin(PageContext context, HttpServletRequest req, HttpServletResponse res) throws Exception
    {
    	m_controller = CmsFlexController.getController(req);
        m_session = req.getSession();
        
        m_settings = (CmsWorkplaceSettings)m_session.getAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
        
        if (m_settings==null)  {
        	m_settings = CmsWorkplace.initWorkplaceSettings(getCmsObject(), m_settings, true);
        	m_session.setAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS, m_settings);
        	
        }
        retriveMessages();

    	siteName = OpenCms.getSiteManager().getCurrentSite(getCmsObject()).getSiteRoot();
    	String proyecto  = siteName.replaceAll("/sites/","");

    	currentPublication = (TipoEdicion) m_session.getAttribute("currentPublication");

    	if (currentPublication==null) {
        	TipoEdicionService tService = new TipoEdicionService();

    		currentPublication = tService.obtenerEdicionOnlineRoot(proyecto);
    		m_session.setAttribute("currentPublication",currentPublication);
    	}
    	
    	publication = "" + currentPublication.getId();
    	moduleConfigName = "webusers";
 		config = CmsMedios.getInstance().getCmsParaMediosConfiguration();

    }

    public CmsMultiMessages getMessages()
    {
    	return m_messages;
    }
    
    public String keyDefault(String keyName, String defaultValue) {

        return getMessages().keyDefault(keyName, defaultValue);
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

	
    public CmsObject getCmsObject() {
        return m_controller.getCmsObject();
    }
    
    public List<AdminSearchField> getAdvancedFiltersList()
    {
    	List<String> additionalInfo = this.getAdditionalInfoList();
    	List<String> advancedFilterData = config.getParamList(siteName, publication, moduleConfigName, "adminAdvancedFilterData");
    	ArrayList<AdminSearchField> ret = new ArrayList<AdminSearchField>();
    	List<ProviderField> providerFields = null;
    	
    	ProviderConfigurationLoader configLoader = new ProviderConfigurationLoader();
    	try {
			providerFields = configLoader.getAllFields(siteName, publication);
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
    	
    	for(String field : advancedFilterData) {
    		AdminSearchField newField = new AdminSearchField();
    		newField.setFieldName(field);
    		newField.setIsProviderField(!additionalInfo.contains(field));
    		
    		if(providerFields != null) {
    			for(ProviderField providerField : providerFields) {
    				if(providerField.getName().equals(field)) {
    					newField.setProviderField(providerField);
    				}
    			}
    		}
    		
    		ret.add(newField);
    	}
    	
    	return ret;
    }
    
    public ProviderField getProviderField(String fieldName) {
    	ProviderField ret = null;
    	List<ProviderField> providerFields = null;
    	ProviderConfigurationLoader configLoader = new ProviderConfigurationLoader();
    	try {
			providerFields = configLoader.getAllFields(siteName, publication);
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
    	
    	if(providerFields != null) {
    		for(ProviderField field : providerFields) {
    			if(field.getName().equals(fieldName))
    				return field;
    		}
    	}
    	
    	return ret;
    }
    
    public List<String> getFiltersList()
    {
    	return config.getParamList(siteName, publication, moduleConfigName, "adminFilterData");
    }

    public List<String> getAdditionalInfoList()
    {
    	return config.getParamList(siteName, publication, moduleConfigName, "additionalInfo");
    }

    public String getEntryType(String entry)
    {
    	return config.getItemGroupParam(siteName, publication, moduleConfigName, entry, "type","string");
    }

    public String getEntryDBName(String entry)
    {
    	return config.getItemGroupParam(siteName, publication, moduleConfigName, entry, "entryname",entry);
    }

    public String getEntryNiceName(String entry)
    {
    	return config.getItemGroupParam(siteName, publication, moduleConfigName, entry, "niceName",entry);
    }
    
    public String getDependsOn(String entry)
    {
    	return config.getItemGroupParam(siteName, publication, moduleConfigName, entry, "dependsOn","");
    }
    
    public String getCategoryBasePath(String entry)
    {
    	return config.getItemGroupParam(siteName, publication, moduleConfigName, entry, "basePath","");
    }

    public int getMaxLength(String entry)
    {
    	return config.getIntegerItempGroupParam(siteName, publication, moduleConfigName, entry, "maxLength",Integer.MAX_VALUE);
    }

    public String getRegularExpression(String entry)
    {
    	return config.getItemGroupParam(siteName, publication, moduleConfigName, entry, "regExp","");
    }

    public boolean isEntryUnique(String entry)
    {
    	return config.getBooleanItempGroupParam(siteName, publication, moduleConfigName, entry, "unique",false);
    }

    public boolean isEntryOptional(String entry)
    {
    	return config.getBooleanItempGroupParam(siteName, publication, moduleConfigName, entry, "optional",true);
    }

    public List<String> getValueList(String entry)
    {
    	return config.getListItempGroupParam(siteName, publication, moduleConfigName, entry, "values");
    }

    public List<String> getComparatorList(String entry)
    {
    	return config.getListItempGroupParam(siteName, publication, moduleConfigName, entry, "comprarators");
    }

    public String getValueId(String value) {
    	if (value.contains(":")) {
    		String[] parts = value.split(":");
    		return parts[0];
    	}
    	return value;
    }

    public String getValueDescription(String value) {
    	if (value.contains(":")) {
    		String[] parts = value.split(":");
    		return parts[1];
    	}
    	return value;
    }
    
    public String getImgAdmin(CmsUser user, String noPictureImg){
    	
    	String     imgPath = "";
    	String imgFileName = "";
    	
    	String usersImageFolder = "/system/cmsMedios/users/";
    	String           userId = user.getId().toString();
    	
    	TfsUserHelper tfsUser = new TfsUserHelper(user);
        String picture = tfsUser.getValorAdicional("USER_PICTURE");
        
        if(picture != null && !picture.equals("")){
        	imgPath = picture;
        	String [] imgPathParts = imgPath.split("/");
    		           imgFileName = imgPathParts[imgPathParts.length -1];
        }
        
        if(!imgPath.equals("") && getCmsObject().existsResource(imgPath))
        	return imgPath;
        else if(!imgFileName.equals("") && getCmsObject().existsResource(usersImageFolder+imgFileName))
        	return usersImageFolder+imgFileName;
        else if(getCmsObject().existsResource(usersImageFolder+userId+".jpg"))
			return usersImageFolder+userId+".jpg";
        else if(getCmsObject().existsResource(usersImageFolder+userId+".jpeg"))
	        return usersImageFolder+userId+".jpeg";
        else if(getCmsObject().existsResource(usersImageFolder+userId+".gif"))
			return usersImageFolder+userId+".gif";
        else if(getCmsObject().existsResource(usersImageFolder+userId+".png"))
			return usersImageFolder+userId+".png";
        else if(getCmsObject().existsResource(usersImageFolder+userId+".bmp"))
			return usersImageFolder+userId+".bmp";
        else{
        
        	try {
        	
	        	String       imageUser = "";
	        	String    existsInSite = "";
	        	boolean resourceExists = false;
	        	String   pathImageUser = "/usuarios/img/";
	        	
	        	CmsWorkplaceAction action = CmsWorkplaceAction.getInstance();
				CmsObject        cmsAdmin = action.getCmsAdminObject();
				
				cmsAdmin.getRequestContext().setCurrentProject(getCmsObject().readProject("Offline"));
				cmsAdmin.getRequestContext().setSiteRoot("/");
	        	
	        	for (CmsSite site : (List<CmsSite>)OpenCms.getSiteManager().getAvailableSites(getCmsObject(),true)) {
					
	        		String    cmsAdminSite = site.getSiteRoot();
					
					if(!imgPath.equals("") && cmsAdmin.existsResource(cmsAdminSite+imgPath)){
						resourceExists = true;
						     imageUser = imgPath;
						  existsInSite = cmsAdminSite; 
					}
					
					if(!resourceExists && cmsAdmin.existsResource(cmsAdminSite+pathImageUser+userId+".jpg")){
			              resourceExists = true;
			                existsInSite = cmsAdminSite; 
			                   imageUser = pathImageUser+userId+".jpg";
				    }
					
					if(!resourceExists && cmsAdmin.existsResource(cmsAdminSite+pathImageUser+userId+".jpeg")){
			              resourceExists = true;
			                existsInSite = cmsAdminSite; 
				               imageUser = pathImageUser+userId+".jpeg";
				    }
					
					if(!resourceExists && cmsAdmin.existsResource(cmsAdminSite+pathImageUser+userId+".gif")){
			              resourceExists = true;
			                existsInSite = cmsAdminSite; 
					           imageUser = pathImageUser+userId+".gif";
				    }
					
					if(!resourceExists && cmsAdmin.existsResource(cmsAdminSite+pathImageUser+userId+".png")){
			              resourceExists = true;
			                existsInSite = cmsAdminSite; 
						       imageUser = pathImageUser+userId+".png";
				    }
					
					if(!resourceExists && cmsAdmin.existsResource(cmsAdminSite+pathImageUser+userId+".bmp")){
			              resourceExists = true;
			                existsInSite = cmsAdminSite; 
							   imageUser = pathImageUser+userId+".bmp";
				    }
				}
	        	
	        	if(!imageUser.equals("")){
	        		if(existsInSite.equals(siteName))
	        			return imageUser;
	        		else{
	        			String [] imgPathParts = imageUser.split("/");
						           imgFileName = imgPathParts[imgPathParts.length -1];
						String     pathSibling = usersImageFolder+imgFileName;
						
						cmsAdmin.copyResource(existsInSite+imageUser, pathSibling, CmsResource.COPY_AS_SIBLING); 
						OpenCms.getPublishManager().publishResource(cmsAdmin, pathSibling);
						
						return pathSibling;
	        		}
	        	}
	        } catch (Exception e) {
				CmsLog.getLog(this).error("No se pudo obtener la imagen del usuario administrador "+e.getMessage());
			}
        }
        
    	return noPictureImg;
    }

}
