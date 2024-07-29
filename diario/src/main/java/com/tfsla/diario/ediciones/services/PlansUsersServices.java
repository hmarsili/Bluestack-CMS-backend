package com.tfsla.diario.ediciones.services;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.productivityPlans.PlansUsersDAO;
import com.tfsla.diario.productivityPlans.model.PlansUsers;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Clase que realiza la administracion de los planes de productividad.
 * @author Veronica Tarletta.
 *
 */
public class PlansUsersServices extends baseService {

	private static final Log LOG = CmsLog.getLog(PlansUsersServices.class);

	private int publication;
	private String siteName;
	private JSONObject jsonRequest;
	private CmsObject cmsObj;

	public boolean isMark = false;	
	
	public PlansUsersServices(CmsObject cmsObject, JSONObject jsonreq) throws Exception{
		
		cmsObj = cmsObject;
		jsonRequest = jsonreq;
		
		siteName = jsonRequest.getJSONObject("authentication").getString("siteName");
		publication = jsonRequest.getJSONObject("authentication").getInt("publication");
		
	}
		
	public PlansUsersServices(JSONObject jsonreq) throws Exception {

		jsonRequest = jsonreq;
		
		siteName = jsonRequest.getJSONObject("authentication").getString("siteName");
		publication = jsonRequest.getJSONObject("authentication").getInt("publication");

	}
	
	/**
	 * Metodo que valida si un usuario esta en un plan para esa publicacion, si está devuelve el mismo.
	 * @param dataValue
	 * @param siteName
	 * @param publication
	 * @throws Exception
	 */
	public PlansUsers existPlanForUser(String dataValue) throws CmsException {
		
		PlansUsers planToUser =  new PlansUsers();
		
		PlansUsersDAO ppDAO = new PlansUsersDAO();
	
		try {
			planToUser =  ppDAO.existsPlanForUser(siteName, publication, dataValue);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return planToUser;
	}
	
	/**
	 * Metodo que valida si un usuario esta en un plan para esa publicacion, si está devuelve el mismo.
	 * @param dataValue
	 * @param siteName
	 * @param publication
	 * @throws Exception
	 */
	public PlansUsers getPlanForUser(String dataValue, boolean date) throws CmsException {
		PlansUsers planToUser =  new PlansUsers();
		
		PlansUsersDAO ppDAO = new PlansUsersDAO();
	
		try {
			planToUser =  ppDAO.existsPlanForUser(siteName, publication, dataValue);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return planToUser;
	}
	/*
	 * agrega los usuarios al plan
	 */
	public void addUsersPlans(String planId, String dataKey, JSONArray jsonUsers) throws Exception{
		
		PlansUsersDAO ppDAO = new PlansUsersDAO();
		
		for(int i =0; i<jsonUsers.size(); i++){
			PlansUsers newPlan = new PlansUsers();
			newPlan.setPublication(publication);
			newPlan.setSiteName(siteName);
			newPlan.setId(planId);
			newPlan.setDataKey(dataKey);			
			newPlan.setDataValue(jsonUsers.getJSONObject(i).getString("userName"));
			if (jsonUsers.getJSONObject(i).has("startDay"))
				newPlan.setStartDay(jsonUsers.getJSONObject(i).getLong("startDay"));
			else
				newPlan.setStartDay(Long.parseLong(planId.replaceAll("plan_", "")));
			ppDAO.insertUsersPlans(newPlan);
		}
		
	}
	
	/*
	 * agrega los roles al plan
	 */
	public void addRolsPlans(String planId, String dataKey, JSONArray jsonRols) throws Exception{
		
		PlansUsersDAO ppDAO = new PlansUsersDAO();
		
		for(int i =0; i<jsonRols.size(); i++){
			PlansUsers newPlan = new PlansUsers();
			newPlan.setPublication(publication);
			newPlan.setSiteName(siteName);
			newPlan.setId(planId);
			newPlan.setDataKey(dataKey);
			newPlan.setDataValue(jsonRols.getJSONObject(i).getString("userName"));
			if (jsonRols.getJSONObject(i).has("startDay"))
				newPlan.setStartDay(jsonRols.getJSONObject(i).getLong("startDay"));
			else
				newPlan.setStartDay(Long.parseLong(planId.replaceAll("plan_", "")));
			LOG.debug(" lo que se manda a guardar en el nuevo plan.  " + newPlan);
			ppDAO.insertUsersPlans(newPlan);
		}
		
	}
	
	/**
	 * Metodo que agrega la relacion plan usuario..
	 * @param url: url del plan al que se va a mover
	 * @param userName: userName del usuario 
	 * @throws Exception
	 */
	
	public void setMoveUserToPlan(String url, String dataValue) throws Exception{
	
		PlansUsersDAO ppDAO = new PlansUsersDAO();
		ppDAO.deleteUsersForPlan(publication, siteName, dataValue);
		
		long dateNowToAddPlan = new Date().getTime();

		PlansUsers newPlan = new PlansUsers();
		newPlan.setPublication(publication);
		newPlan.setSiteName(siteName);
		newPlan.setId(url);
		newPlan.setDataKey("group");
		newPlan.setDataValue(dataValue);
		newPlan.setStartDay(dateNowToAddPlan);
		ppDAO.insertUsersPlans(newPlan);
	
	}
	
	/**
	 * Elimina un usuairo de un plan..
	 * @param url: url del plan al que se va a mover
	 * @param userName: userName del usuario 
	 * @throws Exception
	 */
	
	public void deleteUserToPlan(String dataValue) throws Exception{
	
		PlansUsersDAO ppDAO = new PlansUsersDAO();
		ppDAO.deleteUsersForPlan(publication, siteName, dataValue);
		
	}
	
	
	/**
	 * Elimina todos los usuarios de un plan.
	 * @param url: url del plan al que se va a mover
	 * @param userName: userName del usuario 
	 * @throws Exception
	 */
	
	public void deletePlansUsers(String url) throws Exception{
	
		PlansUsersDAO ppDAO = new PlansUsersDAO();
		ppDAO.deleteUsersPlan(publication, siteName, url);
			
	}
	/**
	 * Metodo que obtiene todos los usarios de un plan.
	 * @param userToPlan
	 * @throws Exception
	 */
	
	
	public List getPlansUsersDate(String id) throws Exception{
		PlansUsersDAO ppDAO = new PlansUsersDAO();
		return ppDAO.getUsersDataPlansDate(publication, siteName, id);
		
	}
	public List getPlansUsers(String id) throws Exception{
		PlansUsersDAO ppDAO = new PlansUsersDAO();
		return ppDAO.getUsersPlans(publication, siteName, id);
		
	}
	
	/**
	 * Otiene todos los usuarios que pertenecen a un plan para una publicacion. 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	
	public List getUsersInPlans(String datakey) throws Exception{
		PlansUsersDAO ppDAO = new PlansUsersDAO();
		return ppDAO.getUsersInPlans(publication, siteName,datakey);
		
	}
	
	/**
	 * Valida si existen usuarios para un plan
	 * @param userToPlan
	 * @throws Exception
	 */
	public boolean exitPlansUsers(String id) throws Exception{
		
		PlansUsersDAO ppDAO = new PlansUsersDAO();
		return ppDAO.existUsersPlans(publication, siteName, id);
		
	}
	
	public JSONArray getPlansUsersData(String id ) throws Exception{
		return getPlansUsersData(id,false,"");
		
	}
	public JSONArray getPlansUsersData(String id, boolean getStadistic, String period) throws Exception{
		PlansUsersDAO ppDAO = new PlansUsersDAO();
		JSONArray userToPlans = ppDAO.getUsersDataPlansDate(publication, siteName, id);
		
		JSONArray ppsUsers = new JSONArray();
		
		String staticsUserStr = "";
	
		if(getStadistic) {
			
			ProductivityPlanAWS pAWS = new ProductivityPlanAWS(jsonRequest);
			staticsUserStr = pAWS.getStaticsPeriod(id, period);
		}
		
		for (int i=0; i<userToPlans.size();i++) {
			JSONObject userToPlan = userToPlans.getJSONObject(i);
			JSONObject jsonUser =  new JSONObject();
				try{
					CmsUser cuser = cmsObj.readUser(userToPlan.getString("userName"));					
					jsonUser.put("fullName", cuser.getFullName());
					jsonUser.put("image", cuser.getAdditionalInfo("USER_PICTURE"));
					jsonUser.put("userName", userToPlan.getString("userName"));
					jsonUser.put("startDate", userToPlan.getString("startDate"));
					jsonUser.put("exist", true);
					jsonUser.put("fulfilled",false); // or default no cumple. Luego se valida en jsp segun AWS.
					ppsUsers.add(jsonUser);
				}catch (CmsDbEntryNotFoundException nfEx) {
					jsonUser.put("fullName", "");
					jsonUser.put("image","" );
					jsonUser.put("userName", userToPlan.getString("userName"));
					jsonUser.put("exist", false);
					ppsUsers.add(jsonUser);
				}
				
			
	
		}
		return  ppsUsers;
	}
	
	public JSONArray getPlansRolData(String id) throws Exception{
		
		PlansUsersDAO ppDAO = new PlansUsersDAO();
		List<String> rolsToPlans = ppDAO.getUsersPlans(publication, siteName, id);
		
		JSONArray ppsRols = new JSONArray();
			
			for(String rol : rolsToPlans) { 
				JSONObject jsonUser =  new JSONObject();
				jsonUser.put("rolName", rol);
				ppsRols.add(jsonUser);
			
	
		}
		return  ppsRols;
	}
	
	/**
	 * Metodo que obtiene todos los usuarios de la publicacion indicando si el usario pertenece o no a un plan.
	 * @param userToPlan
	 * @throws Exception
	 */
	public List getUsersAvailable(String id) throws Exception{
		
		PlansUsersDAO ppDAO = new PlansUsersDAO();
		return ppDAO.getUsersPlans(publication, siteName, id);
		
	}
	
}

