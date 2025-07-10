package com.tfsla.diario.planning.services;

import org.apache.commons.logging.Log;

import java.util.List;
import org.opencms.main.CmsLog;

import com.tfsla.diario.planning.ActivityDAO;
import com.tfsla.diario.planning.model.Activity;
import com.tfsla.diario.planning.model.SearchOptions;

public class ActivityServices {
	
	private static final Log LOG = CmsLog.getLog(ActivityServices.class);

	private int publication;
	private String siteName;

	public ActivityServices( String _siteName, int _publication ) throws Exception {
		
		siteName = _siteName;
		publication = _publication;

	}
	
	/**
	 * Metodo que agrega una actividad de la tabla de CMS
	 * @return
	 */

	public int createActivity (Activity activity) throws Exception {
		
		ActivityDAO activityDAO = new ActivityDAO();
		
		activity.setSiteName(siteName);
		activity.setPublication(publication);
		try {
			return activityDAO.insertActivity(activity);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
			
		}
		
	}
	
	/**
	 * Metodo que elimina una actividad de la tabla de CMS 
	 * @return
	 */

	public void deleteActivity(int id) throws Exception {
		
		ActivityDAO activityDAO = new ActivityDAO();
		try {
			activityDAO.deleteActivity(publication, siteName, id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Metodo que actualiza una actividad de la tabla de CMS
	 * @return
	 */

	public void updateActivity(Activity activity) throws Exception {
		
		ActivityDAO activityDAO = new ActivityDAO();
		try {
			activity.setSiteName(siteName);
			activity.setPublication(publication);
			activityDAO.updateActivity(activity);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Metodo que busca una actividad de la tabla de CMS 
	 * @return
	 */

	public List<Activity> searchActivity (SearchOptions options) throws Exception {
		
		LOG.debug("ENTRO " + options.getSiteName() + " " + options.getPublication() + " " + options.getTo() + " " + options.getFrom());
		
		ActivityDAO activityDAO = new ActivityDAO();
		try {
			return activityDAO.getActivity(options);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	
}