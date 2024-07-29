package com.tfsla.diario.analytics.services;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.diario.analytics.data.AnalyticsDateAutomaticDAO;
import com.tfsla.diario.analytics.model.AnalyticsDateAutomatic;

public class AnalyticsDateAutomaticServices {
	
	private static final Log LOG = CmsLog.getLog(AnalyticsDateAutomaticServices.class);
	private int publication;
	private String siteName;

	public AnalyticsDateAutomaticServices(String _siteName, int _publication) throws Exception {
		siteName = _siteName;
		publication = _publication;
	}
	
	/**
	 * Método que agrega la fecha de última actualización automática en la tabla del CMS. 
	 * @return
	 */

	public AnalyticsDateAutomatic getDate() throws Exception {
		
		AnalyticsDateAutomaticDAO analyticsDAO = new AnalyticsDateAutomaticDAO();
		try {
			return analyticsDAO.getDate(siteName, publication);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Método que actualiza la fecha de última actualización automática en la tabla del CMS.
	 * @return
	 */

	public void updateDate(AnalyticsDateAutomatic dateAutomaticUpdated) throws Exception {
		
		AnalyticsDateAutomaticDAO analyticsDAO = new AnalyticsDateAutomaticDAO();
		try {
			analyticsDAO.updatedDate(dateAutomaticUpdated);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Método que agrega la fecha de última actualización automática en la tabla del CMS. 
	 * @return
	 */

	public void insertDate(AnalyticsDateAutomatic dateAutomaticUpdated) throws Exception {
		
		AnalyticsDateAutomaticDAO analyticsDAO = new AnalyticsDateAutomaticDAO();
		try {
			analyticsDAO.insertDate(dateAutomaticUpdated);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
		
}