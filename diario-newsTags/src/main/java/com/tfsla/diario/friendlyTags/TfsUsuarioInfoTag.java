package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;

public class TfsUsuarioInfoTag extends A_TfsUsuarioValueTag {

	private static final long serialVersionUID = -2420261427115940526L;
	private static final Log LOG = CmsLog.getLog(TfsUsuarioInfoTag.class);
	private static final String moduleConfigName = "webusers";
	private String value = null;
	private String entryName = null;
	private String defaultValue = "";
	
	@Override
    public int doStartTag() throws JspException {
    	try {
    		CmsUser user = getCurrentUser().getUser();
    		String userInfo = "";
    		Object info = null;
    		
    		if ((user != null) && (value != null || entryName != null)) {
    			if(this.entryName != null && !this.entryName.equals("")) {
    				info = user.getAdditionalInfo(entryName);
    				if(info != null) userInfo = info.toString();
    			} else {
	    			String	siteName = OpenCms.getSiteManager().getCurrentSite(CmsFlexController.getCmsObject(pageContext.getRequest())).getSiteRoot();
		    	    TipoEdicionService tService = new TipoEdicionService();
	    	    	TipoEdicion currentPublication=null;
	    	    	
		    		try {
		    			currentPublication = tService.obtenerEdicionOnlineRoot(siteName.substring(siteName.lastIndexOf("/") + 1));
		    		} catch (Exception e) {
    					LOG.error("Error al intentar traer la publicacion actual: " + e.getCause());
		    		}
		    		
		    		String publication = "" + (currentPublication!=null?currentPublication.getId():"");
		    		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
	
					String nombrekey =	config.getItemGroupParam(siteName, publication, moduleConfigName, value, "entryname",value);
					//String type = config.getItemGroupParam(siteName, publication, moduleConfigName, value, "type","string");
					info = user.getAdditionalInfo(nombrekey);
					if(info != null)
						userInfo = info.toString();
    			}
    			
    			if(userInfo == null || userInfo.equals("")) userInfo = defaultValue;
    			pageContext.getOut().print(userInfo);
    		}
		} catch (IOException e) {
			throw new JspException(e);
		}
		
		return SKIP_BODY;
    }

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getEntryName() {
		return entryName;
	}

	public void setEntryName(String entryName) {
		this.entryName = entryName;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
}