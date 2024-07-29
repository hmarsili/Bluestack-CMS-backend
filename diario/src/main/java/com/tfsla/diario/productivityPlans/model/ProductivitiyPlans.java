package com.tfsla.diario.productivityPlans.model;

import net.sf.json.JSONObject;

public class ProductivitiyPlans {
	
	
	private String siteName;
	private int publication;
	private String id;
	private boolean enabled;
	private String type; //'general'|'rol'|'group'
	private String title;
	private String description;
	private String format; // 'dyary'|'monthly'|'weekly'
	private int newsCount;
	private String method; // 'characters'|'words'
	private int minNum;
	private boolean frecMonday;
	private boolean frecThuesday;
	private boolean frecWednesday;
	private boolean frecThursday;
	private boolean frecFriday;
	private boolean frecSaturday;
	private boolean frecSunday;
	private long frecFrom;
	private long frecTo;
	private String userCreation;
	private String usersType;
	
	public String getSiteName() {
		return siteName;
	}
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	public int getPublication() {
		return publication;
	}
	public void setPublication(int publication) {
		this.publication = publication;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public int getNewsCount() {
		return newsCount;
	}
	public void setNewsCount(int newsCount) {
		this.newsCount = newsCount;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public int getMinNum() {
		return minNum;
	}
	public void setMinNum(int minimum) {
		this.minNum = minimum;
	}
	public boolean isFrecMonday() {
		return frecMonday;
	}
	public void setFrecMonday(boolean frecMonday) {
		this.frecMonday = frecMonday;
	}
	public boolean isFrecThuesday() {
		return frecThuesday;
	}
	public void setFrecThuesday(boolean frecThuesday) {
		this.frecThuesday = frecThuesday;
	}
	public boolean isFrecWednesday() {
		return frecWednesday;
	}
	public void setFrecWednesday(boolean frecWednesday) {
		this.frecWednesday = frecWednesday;
	}
	public boolean isFrecThursday() {
		return frecThursday;
	}
	public void setFrecThursday(boolean frecThursday) {
		this.frecThursday = frecThursday;
	}
	public boolean isFrecFriday() {
		return frecFriday;
	}
	public void setFrecFriday(boolean frecFriday) {
		this.frecFriday = frecFriday;
	}
	public boolean isFrecSaturday() {
		return frecSaturday;
	}
	public void setFrecSaturday(boolean frecSaturday) {
		this.frecSaturday = frecSaturday;
	}
	public boolean isFrecSunday() {
		return frecSunday;
	}
	public void setFrecSunday(boolean frecSunday) {
		this.frecSunday = frecSunday;
	}
	public long getFrecFrom() {
		return frecFrom;
	}
	public void setFrecFrom(long frecFrom) {
		this.frecFrom = frecFrom;
	}
	public long getFrecTo() {
		return frecTo;
	}
	public void setFrecTo(long frecTo) {
		this.frecTo = frecTo;
	}
	
	public String getUserCreation() {
		return userCreation;
	}
	public void setUserCreation(String userCreation) {
		this.userCreation = userCreation;
	}
	
	public String getUsersType() {
		return usersType;
	}
	public void setUsersType(String usersType) {
		this.usersType = usersType;
	}
	public JSONObject formatToJson() throws Exception {
		

		JSONObject jsonPP = new JSONObject();
		
		if (getId() != null && !getId().equals("")) {
			jsonPP.put("siteName",getSiteName());
			jsonPP.put("publication",getPublication());
			jsonPP.put("id",getId());
			jsonPP.put("enabled",isEnabled());
			jsonPP.put("type",getType());
			jsonPP.put("title",getTitle());
			jsonPP.put("description",getDescription());
			jsonPP.put("format",getFormat());
			jsonPP.put("newsCount",getNewsCount());
			jsonPP.put("method",getMethod());
			jsonPP.put("minNum",getMinNum());
			jsonPP.put("frecMonday",isFrecMonday());
			jsonPP.put("frecTuesday",isFrecThuesday());
			jsonPP.put("frecWednesday",isFrecWednesday());
			jsonPP.put("frecThursday",isFrecThursday());
			jsonPP.put("frecFriday",isFrecFriday());
			jsonPP.put("frecSaturday",isFrecSaturday());
			jsonPP.put("frecSunday",isFrecSunday());
			jsonPP.put("frecFrom",getFrecFrom());
			jsonPP.put("frecTo",getFrecTo());
			jsonPP.put("userCreation",getUserCreation());
			jsonPP.put("dateCreation",Long.parseLong(getId().split("_")[1]));
			jsonPP.put("usersType",getUsersType());
		}
		return jsonPP;
		
	}
		
}
