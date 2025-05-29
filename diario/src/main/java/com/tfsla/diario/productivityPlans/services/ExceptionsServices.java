package com.tfsla.diario.productivityPlans.services;

import org.apache.commons.logging.Log;

import java.util.List;
import org.opencms.main.CmsLog;

import com.tfsla.diario.productivityPlans.PlansExceptionsDAO;
import com.tfsla.diario.productivityPlans.model.*;

public class ExceptionsServices {
	
	private static final Log LOG = CmsLog.getLog(ExceptionsServices.class);

	private int publication;
	private String siteName;

	public ExceptionsServices( String _siteName, int _publication ) throws Exception {
		
		siteName = _siteName;
		publication = _publication;

	}
	
	/**
	 * Metodo que agrega una excepetion de la tabla de CMS
	 * @return
	 */

	public int createException (PlansExceptions exception) throws Exception {
		
		PlansExceptionsDAO exceptionDAO = new PlansExceptionsDAO();
		
		exception.setSiteName(siteName);
		exception.setPublication(publication);
		try {
			return exceptionDAO.insertException(exception);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
			
		}
		
	}
	
	/**
	 * Metodo que elimina una excepetion de la tabla de CMS 
	 * @return
	 */

	public void deletePlansExceptionsDAO(int id) throws Exception {
		
		PlansExceptionsDAO PlansExceptionsDAO = new PlansExceptionsDAO();
		try {
			PlansExceptionsDAO.deleteException(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void desactivePlansExceptionsDAO(int id) throws Exception {
		
		PlansExceptionsDAO PlansExceptionsDAO = new PlansExceptionsDAO();
		try {
			PlansExceptionsDAO.changeStatus(id,false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Metodo que actualiza una excepetion de la tabla de CMS
	 * @return
	 */

	public void updatePlansExceptions(PlansExceptions exception) throws Exception {
		
		PlansExceptionsDAO PlansExceptionsDAO = new PlansExceptionsDAO();
		try {
			PlansExceptionsDAO.updateException(exception);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Metodo que busca una excepetion de la tabla de CMS 
	 * @return
	 */

	public List<PlansExceptions> searchPlansException (SearchOptionsExceptions options) throws Exception {
		
		PlansExceptionsDAO plansExceptionsDAO = new PlansExceptionsDAO();
		try {
			return plansExceptionsDAO.getPlansExceptions(options);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	
}