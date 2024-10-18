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
import com.tfsla.opencms.webusers.openauthorization.common.SocialProvider;

public class TfsSocialProvidersTag extends BaseTag {

	@Override
	public int doStartTag() throws JspException {
		ProviderConfigurationLoader loader = new ProviderConfigurationLoader();
		saveFields();
		
		CmsFlexController controller = CmsFlexController.getController(pageContext.getRequest());
	 	CmsObject cms = controller.getCmsObject();
		TipoEdicion tEdicion = getTipoEdicion(cms);
		String siteName = cms.getRequestContext().getSiteRoot();
		String publication = tEdicion != null ? tEdicion.getId() + "" : "";
		
		providers = loader.getProviders(siteName, publication);
		
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
		if(providers == null) return false;
		
		index++;
		
		if (index < providers.size())
			exposeField(providers.get(index));
		else
			restoreFields();

		return (index < providers.size());
	}
	
	private void exposeField(SocialProvider provider) {
		TfsLista lista = new TfsLista(this.providers.size(),this.index+1,this.size,this.page);
		pageContext.getRequest().setAttribute("providerslist", lista);
		pageContext.getRequest().setAttribute("provider", provider);
	}
	
	private void restoreFields() {
		pageContext.getRequest().setAttribute("provider", previous);
    	pageContext.getRequest().setAttribute("providerslist", previousList);
	}
	
	private void saveFields() {
		previousList = (TfsLista) pageContext.getRequest().getAttribute("providerslist");
		previous  = (SocialProvider) pageContext.getRequest().getAttribute("provider");
		
    	pageContext.getRequest().setAttribute("providerslist",null);
    	pageContext.getRequest().setAttribute("provider",null);
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
	private int size = 0;
	private int page = 1;
	private SocialProvider previous;
	private TfsLista previousList;
	private List<SocialProvider> providers;
	
	private static final long serialVersionUID = -7369229074190337934L;
	
}
