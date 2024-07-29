package com.tfsla.opencms.webusers.rankings;

import java.util.ArrayList;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;

import com.tfsla.opencms.webusers.openauthorization.common.exceptions.InvalidConfigurationException;
import com.tfsla.opencms.webusers.rankings.data.RankingsDAO;

public class RankingsService {
	
	private RankingDimensionsManager dimensionsManager;
	
	public RankingsService(CmsObject cms) throws InvalidConfigurationException {
		this.dimensionsManager = new RankingDimensionsManager(cms);
	}
	
	public ArrayList<RankingItem> getStatistics(RankingsServiceRequest request) {
		UserDimension dimensionX = this.dimensionsManager.getDimension(request.getDimensionX());
		UserDimension dimensionY = null;
		ArrayList<RankingItem> results = new ArrayList<RankingItem>();
		
		if(request.getDimensionY() != null && !request.getDimensionY().equals("")) {
			dimensionY = this.dimensionsManager.getDimension(request.getDimensionY());
		}
		
		// Inicializar las dimensiones, se tienen solamente los nombres
		this.setFiltersDimensions(request);
		
		RankingsDAO dao = new RankingsDAO();
		try {
			if(!dao.openConnection()) {
				throw new Exception("Cannot open a DB connection");
			}
			
			results = dao.getSimpleReport(dimensionX, dimensionY, request.getFilters());
			this.formatResults(results);
			
		} catch(Exception e) {
			e.printStackTrace();
			CmsLog.getLog(this).debug("Error inserting data into the DB: " + e.getMessage());
		} finally {
			dao.closeConnection();
		}
		
		return results;
	}
	
	private void formatResults(ArrayList<RankingItem> results) {
		for(RankingItem result : results) {
			result.setName(result.getName().replace("usuarios/", ""));
		}
	}
	
	private void setFiltersDimensions(RankingsServiceRequest request) {
		for(int i=0; i<request.getFilters().size(); i++) {
			UserDimension dimension = request.getFilters().get(i).getDimension();
			if(dimension.getName() != null && !dimension.getName().equals(""))
				request.getFilters().get(i).setDimension(this.dimensionsManager.getDimension(dimension.getName()));
		}
	}
}
