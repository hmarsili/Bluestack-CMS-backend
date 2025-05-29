package com.tfsla.diario.productivityPlans.services;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsMedios;
import org.opencms.main.CmsLog;

import java.util.List;

import com.tfsla.diario.productivityPlans.PlansExceptionsReasonsDAO;
import com.tfsla.diario.productivityPlans.model.*;

public class ExceptionsReasonsServices {
	
	private int publication;
	private String siteName;

	private static final Log LOG = CmsLog.getLog(ExceptionsReasonsServices.class);

	
	public ExceptionsReasonsServices( String _siteName, int _publication ) throws Exception {
		
		siteName = _siteName;
		publication = _publication;

	}
	
	protected String getModuleName() {
		return "productivityPlans";
	}

	/**
	 * Metodo que agrega una excepetion de la tabla de CMS
	 * @return
	 */

	public boolean isIntegrationEnabled() {
    	return CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(siteName, ""+publication, getModuleName(), "specificReasonsPub", false);		
	}

	public int createPlansExceptionsReason(PlansExceptionsReasons reason) throws Exception {
		
		PlansExceptionsReasonsDAO exceptionDAO = new PlansExceptionsReasonsDAO();
		
		if (reason.getSiteName()==null) reason.setSiteName("0");
		
		try {
			return exceptionDAO.insertReason(reason);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
			
		}
		
	}
	
	/**
	 * Metodo que elimina una excepetion de la tabla de CMS 
	 * @return
	 */

	public void deletePlansExceptionsReasons(int id) throws Exception {
		
		PlansExceptionsReasonsDAO PlansExceptionsReasonsDAO = new PlansExceptionsReasonsDAO();
		try {
			PlansExceptionsReasonsDAO.deleteReason(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Metodo que actualiza una excepetion de la tabla de CMS
	 * @return
	 */

	public void updatePlansExceptionsReasons(PlansExceptionsReasons reason) throws Exception {
		
		PlansExceptionsReasonsDAO PlansExceptionsReasonsDAO = new PlansExceptionsReasonsDAO();
				reason.setSiteName(siteName);
		reason.setPublication(publication);
		
		try {
			PlansExceptionsReasonsDAO.updateReason(reason);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Metodo que busca una excepetion de la tabla de CMS 
	 * @return
	 */

	public List<PlansExceptionsReasons> searchReason(SearchOptionsExcReasons options) throws Exception {
		
	
		PlansExceptionsReasonsDAO PlansExceptionsReasonsDAO = new PlansExceptionsReasonsDAO();
		try {
			return PlansExceptionsReasonsDAO.getPlansExceptionsReasons(options);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	
}