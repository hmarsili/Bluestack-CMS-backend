package com.tfsla.diario.freshness.model;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;


public class Freshness {

	protected static final Log LOG = CmsLog.getLog(Freshness.class);

	private long date; //fecha a ejecutarse cuando es fecha exacta. 
	private long startDate; //fecha que comienza la frescura cuando es recurrencia
	private int publication; // 
	private int priority;
	private int recurrece; // 180 - 30 - 15 - 7
	private String republication; // DATE_UPDATED - MANUAL - DATE_ORIGINAL
	private String siteName;
	private String type; // RECURRENCE -  DATE_EXACT;

	private String url;
	private String userName;
	private String zone;
	private String section;
	private String userCreation;
	
	private String TYPE_RECURRENCE =  "RECURRENCE";
	private String REPUBLICATION_DATE_UPDATED = "DATE_UPDATED";
	
	
	
	public Long getDate() {
		return date;
	}
	public void setDate(long date) {
		this.date = date;
	}
	public long getStartDate() {
		return startDate;
	}
	public void setStartDate(long startDate) {
		this.startDate = startDate;
	}
	public int getPublication() {
		return publication;
	}
	public void setPublication(int publication) {
		this.publication = publication;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public int getRecurrece() {
		return recurrece;
	}
	public void setRecurrece(int recurrece) {
		this.recurrece = recurrece;
	}
	public String getRepublication() {
		return republication;
	}
	public void setRepublication(String republication) {
		this.republication = republication;
	}
	public boolean isRepublicationDateUpdate() {
		return this.republication.equals(REPUBLICATION_DATE_UPDATED);
	}
	public String getSiteName() {
		return siteName;
	}
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isTypeRecurrence() {
		return this.type.equals(TYPE_RECURRENCE);
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getZone() {
		return zone;
	}
	public void setZone(String zone) {
		this.zone = zone;
	}
	public String getSection() {
		return section;
	}
	public void setSection(String section) {
		this.section = section;
	}
	public String getUserCreation() {
		return userCreation;
	}
	public void setUserCreation(String userCreation) {
		this.userCreation = userCreation;
	}
	
	
	/**
	 * Freshness recibe un object con la clave principal compuesta por publicacion, siteName y url
	 * @throws Exception 
	 * @throws NumberFormatException 
	 *
	public boolean hasSetFreshness(JSONObject keys) throws NumberFormatException, Exception{
		
		FreshnessDAO fDAO = new FreshnessDAO();
		return fDAO.existsFreshness(keys.getInt("publication"), keys.getString("siteName"), keys.getString("url"));
		
	}
	
	public boolean hasSetFreshness(String publication, String siteName, String url) throws NumberFormatException, Exception{
		
		JSONObject keys = new JSONObject();
		keys.put("publication", publication);
		keys.put("siteName", siteName);
		keys.put("url", url);
		return hasSetFreshness(keys);
		
	}
		
	public Freshness getFreshness(JSONObject keys) throws NumberFormatException, Exception{
		
		FreshnessDAO fDAO = new FreshnessDAO();
		return fDAO.getFreshness(keys.getInt("publication"), keys.getString("siteName"), keys.getString("url"));
		
	}

	public List<Freshness> getFreshness(int publication, String siteName) throws NumberFormatException, Exception{
		
		FreshnessDAO fDAO = new FreshnessDAO();
		return fDAO.getFreshnessInPub(publication, siteName);
		
	}

	public void deleteFreshness(JSONObject keys) throws Exception{
		
		FreshnessDAO fDAO = new FreshnessDAO();
		fDAO.deleteFreshness(keys.getInt("publication"), keys.getString("siteName"), keys.getString("url"));
		
	}
	public Freshness getFreshness(String publication, String siteName, String url) throws NumberFormatException, Exception{
		
		JSONObject keys = new JSONObject();
		keys.put("publication", publication);
		keys.put("siteName", siteName);
		keys.put("url", url);
		return getFreshness(keys);
		
	}

	public void updateFreshness(Freshness freshness) throws Exception{
		
		FreshnessDAO fDAO = new FreshnessDAO();
		fDAO.updateFreshness(freshness);
		
	}
	
	public void createFreshness(Freshness freshness) throws Exception{
			
		FreshnessDAO fDAO = new FreshnessDAO();
		fDAO.insertFreshness(freshness);
		
	}
	
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
	 *
		return calendar.getTimeInMillis();
	
	}
	
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
 */	
}