package com.tfsla.diario.friendlyTags;

import java.util.List;

import jakarta.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.openCmsService;
import com.tfsla.diario.model.TfsLista;
import com.tfsla.opencms.webusers.openauthorization.common.ProviderConfigurationLoader;
import com.tfsla.opencms.webusers.openauthorization.common.ProviderField;
import com.tfsla.opencms.webusers.openauthorization.common.exceptions.InvalidConfigurationException;

public class TfsSocialProvidersFieldsTag extends BaseTag {

	@Override
	public int doStartTag() throws JspException {
		ProviderConfigurationLoader loader = new ProviderConfigurationLoader();
		saveFields();
		
		CmsFlexController controller = CmsFlexController.getController(pageContext.getRequest());
	 	CmsObject cms = controller.getCmsObject();
		TipoEdicion tEdicion = getTipoEdicion(cms);
		String siteName = cms.getRequestContext().getSiteRoot();
		String publication = tEdicion != null ? String.valueOf(tEdicion.getId()) : "";
		
		if(source == null || source.equals("")) {
			try {
				fields = loader.getConfiguredFields(siteName, publication);
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
		} else {
			try {
				fields = loader.getConfiguredFields(siteName, publication, source);
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
		return (hasMoreContent() ? EVAL_BODY_INCLUDE : SKIP_BODY );
	}
	
	@Override
	public int doAfterBody() throws JspException {
		if (hasMoreContent()) {
			return EVAL_BODY_AGAIN;
		}
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return SKIP_BODY;
	}
	
	@Override
	public int doEndTag() {
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		index = -1;
		return EVAL_PAGE;
	}
	
	private boolean hasMoreContent() {
		if(fields == null) return false;
		
		index++;
		
		if (index<fields.size())
			exposeField(fields.get(index));
		else
			restoreFields();

		return (index<fields.size());
	}
	
	private void exposeField(ProviderField field) {
		TfsLista lista = new TfsLista(this.fields.size(),this.index+1,this.size,this.page);
		pageContext.getRequest().setAttribute("providerslist", lista);
		pageContext.getRequest().setAttribute("providerfield", field);
	}
	
	private void restoreFields() {
		pageContext.getRequest().setAttribute("providerfield", previousField);
    	pageContext.getRequest().setAttribute("providerslist", previousList);
	}
	
	private void saveFields() {
		previousList = (TfsLista) pageContext.getRequest().getAttribute("providerslist");
		previousField  = (ProviderField) pageContext.getRequest().getAttribute("providerfield");
		
    	pageContext.getRequest().setAttribute("providerslist",null);
    	pageContext.getRequest().setAttribute("providerfield",null);
    }
	
	private TipoEdicion getTipoEdicion(CmsObject cms) {
		TipoEdicionService tEService = new TipoEdicionService();
		TipoEdicion tEdicion = null;
		try {
			tEdicion = tEService.obtenerTipoEdicion(cms,cms.getRequestContext().getUri());
		} catch (Exception e1) {
			e1.printStackTrace();
		}			
		
		if (tEdicion==null) {
			String siteName = openCmsService.getSiteName(cms.getRequestContext().getSiteRoot());
			try {
				tEdicion = tEService.obtenerEdicionOnlineRoot(siteName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return tEdicion;
	}
	
	private int index = -1;
	private int size=0;
	private int page=1;
	private String source;
	private ProviderField previousField;
	private TfsLista previousList;
	private List<ProviderField> fields;
	
	private static final long serialVersionUID = 1543167734031836384L;
	
	public ProviderField getField() {
		return fields.get(index);
	}
	
	public int getIndex() {
		return index;
	}

	public boolean isLast() {
		return (index==fields.size()-1);
	}
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
}
