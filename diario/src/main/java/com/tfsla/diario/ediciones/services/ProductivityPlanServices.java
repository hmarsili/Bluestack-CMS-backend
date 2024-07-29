package com.tfsla.diario.ediciones.services;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.productivityPlans.ProductivityPlansDAO;
import com.tfsla.diario.productivityPlans.model.PlansUsers;
import com.tfsla.diario.productivityPlans.model.ProductivitiyPlans;

import net.sf.json.JSONObject;

/**
 * Clase que realiza la administracion de los planes de productividad.
 * @author Veronica Tarletta.
 *
 */
public class ProductivityPlanServices extends baseService {

	private static final Log LOG = CmsLog.getLog(ProductivityPlanServices.class);

	private int publication;
	private String siteName;
	private JSONObject jsonRequest;

	public boolean isMark = false;	

	/** 
	 * No sirve para publicacion, solo sirve para sitios principales.
	 *  
	 * public ProductivityPlanServices(CmsObject cmsObj) throws Exception {

		siteName = OpenCms.getSiteManager().getCurrentSite(cmsObj).getSiteRoot();
		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion currentPublication = tService.obtenerEdicionOnlineRoot(siteName.replaceAll("/sites/", ""));
		publication = currentPublication.getId();

	}*/ 

	
	public ProductivityPlanServices(JSONObject jsonreq) throws Exception {

		jsonRequest = jsonreq;
		
		siteName = jsonRequest.getJSONObject("authentication").getString("siteName");
		publication = jsonRequest.getJSONObject("authentication").getInt("publication");

	}
	
	public String newProductivityPlans() throws Exception 
	{
		return "plan_" + new Date().getTime();

	}
	public List<ProductivitiyPlans> getProductivityPlans() throws Exception {
			
		ProductivityPlansDAO ppDAO = new ProductivityPlansDAO();
		return ppDAO.getProductivityPlans(publication, siteName);
			
	}
	
	
	public List<ProductivitiyPlans> getProductivityPlans(Map<String, String> parameters, boolean isJoin, int sentenceOR) throws Exception {
		
		ProductivityPlansDAO ppDAO = new ProductivityPlansDAO();
		return ppDAO.getProductivityPlans(parameters, isJoin, sentenceOR);
			
	}
	
	public ProductivitiyPlans getGeneralProductivitiyPlans () throws Exception {
		
		ProductivityPlansDAO ppDAO = new ProductivityPlansDAO();
		return ppDAO.getGeneralProductivitiyPlans(publication,siteName);
		
	}
	
	public ProductivitiyPlans getProductivitiyPlans (String id) throws Exception {
		
		ProductivityPlansDAO ppDAO = new ProductivityPlansDAO();
		return ppDAO.getProductivityPlan(id, publication, siteName);
		
	}
	

	public void deletePlan(String planID) throws Exception {
			
		ProductivityPlansDAO ppDAO = new ProductivityPlansDAO();
		ppDAO.deletePlan(planID);
			
	}

	public void insertPlan(ProductivitiyPlans productivityPlan) throws Exception {
		
		ProductivityPlansDAO ppDAO = new ProductivityPlansDAO();
		ppDAO.insertPlans(productivityPlan);
			
	}
	
	 public boolean existPlan(String planID) throws Exception {
		
		ProductivityPlansDAO ppDAO = new ProductivityPlansDAO();
		return ppDAO.existPlan(planID);
			
	}
	
	public void changeStatus(String id) {
		ProductivityPlansDAO ppDAO = new ProductivityPlansDAO();
		try {
			ppDAO.changeStatus(id, publication, siteName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ProductivitiyPlans formatToPlan(JSONObject jsonRequest) throws CmsException {
		
		ProductivitiyPlans pp = new ProductivitiyPlans();
		
		if (jsonRequest.getJSONObject("content").has("siteName"))
			pp.setSiteName(jsonRequest.getJSONObject("content").getString("siteName"));
		else
			pp.setSiteName(jsonRequest.getJSONObject("authentication").getString("siteName"));
		
		if (jsonRequest.getJSONObject("content").has("publication"))
			pp.setPublication(jsonRequest.getJSONObject("content").getInt("publication"));
		else
			pp.setPublication(jsonRequest.getJSONObject("authentication").getInt("publication"));
		
		pp.setId(jsonRequest.getString("path"));
		pp.setEnabled(jsonRequest.getJSONObject("content").getBoolean("enabled"));
		pp.setType(jsonRequest.getJSONObject("content").getString("type"));
		pp.setTitle(jsonRequest.getJSONObject("content").getString("title"));
		pp.setDescription(jsonRequest.getJSONObject("content").getString("description"));
		pp.setFormat(jsonRequest.getJSONObject("content").getString("format"));
		pp.setNewsCount(jsonRequest.getJSONObject("content").getInt("newsCount"));
		pp.setMethod(jsonRequest.getJSONObject("content").getString("method"));
		pp.setMinNum(jsonRequest.getJSONObject("content").getInt("minNum"));
		pp.setFrecMonday(jsonRequest.getJSONObject("content").getBoolean("frecMonday"));
		pp.setFrecThuesday(jsonRequest.getJSONObject("content").getBoolean("frecTuesday"));
		pp.setFrecWednesday(jsonRequest.getJSONObject("content").getBoolean("frecWednesday"));
		pp.setFrecThursday(jsonRequest.getJSONObject("content").getBoolean("frecThursday"));
		pp.setFrecFriday(jsonRequest.getJSONObject("content").getBoolean("frecFriday"));
		pp.setFrecSaturday(jsonRequest.getJSONObject("content").getBoolean("frecSaturday"));
		pp.setFrecSunday(jsonRequest.getJSONObject("content").getBoolean("frecSunday"));
		pp.setFrecFrom(jsonRequest.getJSONObject("content").getInt("frecFrom"));
		pp.setFrecTo(jsonRequest.getJSONObject("content").getInt("frecTo"));
		pp.setUserCreation(jsonRequest.getJSONObject("content").getString("userCreation"));
		pp.setUsersType(jsonRequest.getJSONObject("content").getString("usersType"));
		
		return pp;
		
	}
}

