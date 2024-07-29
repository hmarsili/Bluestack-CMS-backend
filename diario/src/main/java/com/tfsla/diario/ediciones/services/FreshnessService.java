package com.tfsla.diario.ediciones.services;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;
import com.tfsla.diario.freshness.FreshnessDAO;
import com.tfsla.diario.freshness.model.Freshness;

import net.sf.json.JSONObject;

public class FreshnessService extends baseService {


	protected static final Log LOG = CmsLog.getLog(FreshnessService.class);

	/**
	 * Freshness recibe un object con la clave principal compuesta por publicacion, siteName y url
	 * @throws Exception 
	 * @throws NumberFormatException 
	 */
	public boolean hasSetFreshness(int publication, String siteName, String url) throws NumberFormatException, Exception{
		
		FreshnessDAO fDAO = new FreshnessDAO();
		return fDAO.existsFreshness(publication, siteName, url);
		
	}
			
	public Freshness getFreshness(int publication, String siteName, String url) throws NumberFormatException, Exception{
		
		FreshnessDAO fDAO = new FreshnessDAO();
		return fDAO.getFreshness(publication, siteName, url);
		
	}

	public List<Freshness> getFreshness(int publication, String siteName) throws NumberFormatException, Exception{
		
		FreshnessDAO fDAO = new FreshnessDAO();
		return fDAO.getFreshnessInPub(publication, siteName);
		
	}

	public void deleteFreshness(int publication, String siteName, String url) throws Exception{
		
		FreshnessDAO fDAO = new FreshnessDAO();
		fDAO.deleteFreshness(publication, siteName, url);
		
	}

	public void updateFreshness(Freshness freshness) throws Exception{
		
		FreshnessDAO fDAO = new FreshnessDAO();
		fDAO.updateFreshness(freshness);
		
	}
	
	public void createFreshness(Freshness freshness) throws Exception{
			
		FreshnessDAO fDAO = new FreshnessDAO();
		fDAO.insertFreshness(freshness);
		
	}
	/** desde el front recibims un json lo pasamos a un Freshness*/
	
	
	public Freshness formatJsonToFreshness(JSONObject jsonRequest, String pathNew) throws Exception{
		
		Freshness freshness = new Freshness();
		
		if (jsonRequest.getString("type").equals("RECURRENCE")) {
			// se comenta la siguiente linea porque la priemra ejecucion es la fecha que el usuario selecciono desd el front. 
			// freshness.setDate(setDateFreshness(jsonRequest.getLong("dateNew"),jsonRequest.getInt("recurrece")));
			freshness.setDate(jsonRequest.getLong("dateNew"));	
			freshness.setStartDate(jsonRequest.getLong("dateNew"));		
		} else
			freshness.setDate(jsonRequest.getLong("dateNew"));
		
		freshness.setPriority(jsonRequest.has("priority") ? jsonRequest.getInt("priority") : 0);
		freshness.setPublication(jsonRequest.getInt("publication"));
		freshness.setRecurrece(jsonRequest.has("recurrece") ? jsonRequest.getInt("recurrece") : 0);
		freshness.setRepublication(jsonRequest.has("republication") ? jsonRequest.getString("republication") : "");
		freshness.setSiteName(jsonRequest.getString("siteName"));
		freshness.setType(jsonRequest.getString("type"));
		freshness.setUrl(pathNew);
		freshness.setZone(jsonRequest.has("zone") ? jsonRequest.getString("zone") : ""); 
		freshness.setUserName(jsonRequest.has("userName") ? jsonRequest.getString("userName") : ""); 

		
		
		return freshness; 
	}
	
	public Long setDateFreshness(Long dateNews, int frecuency) {
		
		
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(dateNews));
		calendar.add(calendar.DAY_OF_YEAR, frecuency);
		calendar.getTime();
		
	/**	
	  	Calendar nowCal = Calendar.getInstance();
		nowCal.getTime();
		while (nowCal.getTime().before(calendar.getTime())) {
			LOG.debug("FECHA ACTUAL" + nowCal.getTime() + "FECHA NUEVA " + calendar.getTime());
			calendar.add(calendar.DAY_OF_YEAR, frecuency);
			calendar.getTime();
		}
	 */
		return calendar.getTimeInMillis();
	
	}

	/** se usa para mandar al front de angular*/
	public JSONObject formatFreshnessToJSON(Freshness freshness) throws Exception{
		
		JSONObject freshnessJson = new JSONObject();
		
		freshnessJson.put("startDate", freshness.getStartDate());
		freshnessJson.put("date", freshness.getDate());
		freshnessJson.put("priority", freshness.getPriority());
		freshnessJson.put("publication", freshness.getPublication());
		freshnessJson.put("recurrence", freshness.getRecurrece());
		freshnessJson.put("republication", freshness.getRepublication());
		freshnessJson.put("siteName",freshness.getSiteName());
		freshnessJson.put("type",freshness.getType());
		freshnessJson.put("zone",freshness.getZone());
		freshnessJson.put("userName",freshness.getUserName());
		
		return freshnessJson; 
	}
	
	public boolean isFreshnessEquals(Freshness freshness, Freshness freshness2) throws Exception{
	
		if (freshness.getStartDate() != freshness2.getStartDate()) {
			LOG.debug("SE FUE POR getStartDate" + freshness.getStartDate() + " - " +freshness2.getStartDate());
			return false;
		}else	if (freshness.getPriority() != freshness2.getPriority()) {
			LOG.debug("SE FUE POR getPriority" + freshness.getPriority() + " - " +freshness2.getPriority());
			return false;
		}else if (freshness.getRecurrece() != freshness2.getRecurrece()) {
			LOG.debug("SE FUE POR getRecurrece" + freshness.getRecurrece() + " - " +freshness2.getRecurrece());
			return false;
		} else if (!freshness.getRepublication().equals(freshness2.getRepublication())) {
			LOG.debug("SE FUE POR getRepublication" + freshness.getRepublication() + " - " +freshness2.getRepublication());
			return false;
		} else if (!freshness.getType().equals(freshness2.getType())) {
			LOG.debug("SE FUE POR getType" + freshness.getType() + " - " +freshness2.getType());
			return false;
		} else if (!freshness.getZone().equals(freshness2.getZone())) {
			LOG.debug("SE FUE POR getZone" + freshness.getZone() + " - " +freshness2.getZone());
			return false;
		} else if (freshness.getType().equals("DATE_EXACT") && freshness.getDate() != freshness2.getDate()) {
			LOG.debug("SE FUE POR getDate" + freshness.getZone() + " - " +freshness2.getZone());
			return false;
		}
			
		
		return true; 
	}
	
	public String isFreshnessEqualsJSON(Freshness freshness, Freshness freshness2) throws Exception{
		
		String resultresult = "OK";
		
		if (freshness.getPriority() != freshness2.getPriority())
			{resultresult += "false - ";
			resultresult += "Priority";
			resultresult += "freshness:" + freshness.getPriority();
			resultresult += "freshness2:" + freshness2.getPriority();}
		
		else if (freshness.getRecurrece() != freshness2.getRecurrece())
			{resultresult += "false - ";
			resultresult += "getRecurrece";
			resultresult += "freshness:" + freshness.getRecurrece();
			resultresult += "freshness2:" + freshness2.getRecurrece();}
		
		else if (freshness.getRepublication() != freshness2.getRepublication())
			{resultresult += "false - ";
			resultresult += "getRepublication";
			resultresult += "freshness:" + freshness.getRepublication();
			resultresult += "freshness2:" + freshness2.getRepublication();}
		
		else if (!freshness.getType().equals(freshness2.getType()))
			{resultresult += "false - ";
			resultresult += "getType";
			resultresult += "freshness:" + freshness.getType();
			resultresult += "freshness2:" + freshness2.getType();}
			
		else if (!freshness.getZone().equals(freshness2.getZone()))
			{resultresult += "false - ";
			resultresult += "getZone";
			resultresult += "freshness:" + freshness.getZone();
			resultresult += "freshness2:" + freshness2.getZone();}
		
		return resultresult; 
	}
	

}
