package com.tfsla.opencms.webusers.openauthorization.common;

import java.util.ArrayList;

import com.tfsla.opencms.webusers.openauthorization.data.UserDataDAO;

public class UserCountryService {
	
	private ArrayList<UserDataValue> countries;
	
	public ArrayList<UserDataValue> getCountries() {
		UserDataDAO dao = new UserDataDAO();
		ArrayList<UserDataValue> ret = new ArrayList<UserDataValue>();
		
		try {
			dao.openConnection();
			ret = dao.getGroupedUserDataValues("USER_COUNTRY");
		} catch(Exception e) {
			e.printStackTrace();
		}finally{
			dao.closeConnection();
		}
		
		return ret;
	}
	
	public Boolean hasCountries() {
		if(this.countries == null) {
			this.countries = this.getCountries();
		}
		
		return this.countries.size() > 0;
	}
	
	public Boolean countryHasCount(String countryName) {
		if(this.countries == null) {
			this.countries = this.getCountries();
		}
		
		return this.containsCountry(countryName);
	}
	
	public Integer getCountryCount(String countryName) {
		if(this.countries == null) {
			this.countries = this.getCountries();
		}
		
		if(!this.containsCountry(countryName)) return 0;
		
		return (Integer)this.getCountry(countryName).getValue();
	}
	
	private UserDataValue getCountry(String countryName) {
		for(UserDataValue value : this.countries) {
			if(value.getKey().equals(countryName)) return value;
		}
		return null;
	}
	
	private Boolean containsCountry(String countryName) {
		return this.getCountry(countryName) != null;
	}
}
