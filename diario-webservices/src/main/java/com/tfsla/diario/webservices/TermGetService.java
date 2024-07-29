package com.tfsla.diario.webservices;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import com.tfsla.diario.ediciones.data.TipoEdicionesDAO;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.terminos.data.TermsDAO;
import com.tfsla.diario.terminos.data.TermsTypesDAO;
import com.tfsla.diario.terminos.model.Terms;
import com.tfsla.diario.terminos.model.TermsTypes;
import com.tfsla.diario.webservices.common.WebSession;
import com.tfsla.diario.webservices.common.interfaces.IAuthorizationService;
import com.tfsla.diario.webservices.common.interfaces.ITermGetService;
import com.tfsla.diario.webservices.common.strings.ExceptionMessages;
import com.tfsla.diario.webservices.core.SessionManager;
import com.tfsla.diario.webservices.helpers.ParametersHelper;

public class TermGetService implements ITermGetService {

	private CmsFlexController m_controller;
	private CmsObject cmsObject;
	private long type = new Long(0);
	private HttpServletRequest request;
	private Log LOG = CmsLog.getLog(this);

	public TermGetService(HttpServletRequest request) {
		this.request = request;
		 m_controller = CmsFlexController.getController(request);
		 cmsObject = m_controller.getCmsObject();
		
	}
	
	@Override
	public JSON getTermById() throws Exception {
		ParametersHelper helper = new ParametersHelper();
		String id = helper.assertRequestParameter("id", request);
		String typeString= request.getParameter("type");
		String publication=request.getParameter("publication");
		if (typeString==null ) {
			String typeTags = "tags";
			if (publication != null) {
				CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
				TipoEdicionesDAO tDAO = new TipoEdicionesDAO ();
				TipoEdicion tEdition = tDAO.getTipoEdicion(Integer.valueOf(publication));
				//OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot()
				if(tEdition!=null)
					typeTags = config.getParam("/sites/"+tEdition.getProyecto(), publication, "terms", "termsType", "tags");
				else
					LOG.debug("No puede obtener la edicion indicada: " + publication);
			} 
			TermsTypesDAO ttDAO = new TermsTypesDAO ();
			TermsTypes oTermTypes = null;
			try {
				oTermTypes = ttDAO.getTermType(typeTags);
				type = oTermTypes.getId_termType();
				
			} catch (Exception e) {
				LOG.debug("No puede obtener el tipo de tag",e);
			}
			
		} else {
			type = new Long(typeString);
		}
		
		TermsDAO dao = new TermsDAO();
		
		try {
			Terms term = dao.getTerminoById(Long.valueOf(id),type);
			if(term.getId_term() == 0) {
				throw new Exception(
					String.format(
							ExceptionMessages.ERROR_RECORD_NOT_FOUND_FORMAT,
							"tag",
							"id",
							id,"type",type
						)
				);
			}
			return JSONObject.fromObject(term);
		} catch(Exception e) {
			LOG.debug(e);
			throw e;
		}
	}
	
	
}