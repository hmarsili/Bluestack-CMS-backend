package com.tfsla.diario.terminos.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Persons {

	protected long id_person;
	protected String lastmodified;
	protected String name; 
	protected String email; 
	protected Date birthdate;
	protected String nickname; 
	protected String photo; 
	//protected String title; 
	//protected String role; 
	protected String url; 
/*	protected String affiliation; 
	protected String friend; 
	protected String contact; 
	protected String acquaintance; 
	protected String streetadress; 
	protected String adresslocality; 
	protected String adressregion; 
	protected String postalcode; 
	*/
	protected String nacionality; 
	//protected String telephone;
	protected String twitter;
	protected String facebook;
	protected String google;
	protected String linkedin;
	protected String custom1;
	protected String custom2;
	/*protected String custom3;
	protected String custom4;
	protected String custom5;
	protected String custom6;
	protected String custom7;
	protected String custom8;
	protected String custom9;
	protected String custom10;
	
	*/
	protected String shortdescription;
	protected String longdescription;
	protected String type;
	protected int approved;
	protected String synonymous = ""; 
	
	public String getSynonymous() {
		return synonymous;
	}
	public void setSynonymous(String synonymous) {
		if (synonymous== null || synonymous.trim().equals("null"))
			synonymous="";
		this.synonymous = synonymous;
	}
	public long getId_person() {
		return id_person;
	}
	public void setId_person(long id_person) {
		this.id_person = id_person;
	}
	public String getLastmodified() {
		return lastmodified;
	}
	public void setLastmodified(String lastmodified) {
		this.lastmodified = lastmodified;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Date getBirthdate() {
		return birthdate;
	}
	
	public void setBirthdate(String birthdate) {
		
		 try {
		      SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyyy-MM-dd", Locale.getDefault());
		      formatoFecha.setLenient(false);
		      this.birthdate = formatoFecha.parse(birthdate);
		  } catch (ParseException e) {
			  this.birthdate =null;
		  }catch(Exception e1){
			  this.birthdate =null; 
		  }
		 
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getPhoto() {
		return photo;
	}
	public void setPhoto(String photo) {
		this.photo = photo;
	}
	/*public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	*/
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	/*
	public String getAffiliation() {
		return affiliation;
	}
	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}
	public String getFriend() {
		return friend;
	}
	public void setFriend(String friend) {
		this.friend = friend;
	}
	public String getContact() {
		return contact;
	}
	public void setContact(String contact) {
		this.contact = contact;
	}
	public String getAcquaintance() {
		return acquaintance;
	}
	public void setAcquaintance(String acquaintance) {
		this.acquaintance = acquaintance;
	}
	public String getStreetadress() {
		return streetadress;
	}
	public void setStreetadress(String streetadress) {
		this.streetadress = streetadress;
	}
	public String getAdresslocality() {
		return adresslocality;
	}
	public void setAdresslocality(String adresslocality) {
		this.adresslocality = adresslocality;
	}
	public String getAdressregion() {
		return adressregion;
	}
	public void setAdressregion(String adressregion) {
		this.adressregion = adressregion;
	}
	public String getPostalcode() {
		return postalcode;
	}
	public void setPostalcode(String postalcode) {
		this.postalcode = postalcode;
	}
	*/
	public String getNacionality() {
		return nacionality;
	}
	public void setNacionality(String nacionality) {
		this.nacionality = nacionality;
	}
	/*
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	*/
	public int getApproved() {
		return approved;
	}
	public void setApproved(int approved) {
		this.approved = approved;
	}
	public String getTwitter() {
		return twitter;
	}
	public void setTwitter(String twitter) {
		this.twitter = twitter;
	}
	public String getFacebook() {
		return facebook;
	}
	public void setFacebook(String facebook) {
		this.facebook = facebook;
	}
	public String getGoogle() {
		return google;
	}
	public void setGoogle(String google) {
		this.google = google;
	}
	public String getLinkedin() {
		return linkedin;
	}
	public void setLinkedin(String linkedin) {
		this.linkedin = linkedin;
	}
	public String getCustom1() {
		return custom1;
	}
	public void setCustom1(String custom1) {
		this.custom1 = custom1;
	}
	public String getCustom2() {
		return custom2;
	}
	public void setCustom2(String custom2) {
		this.custom2 = custom2;
	}
	/*
	public String getCustom3() {
		return custom3;
	}
	public void setCustom3(String custom3) {
		this.custom3 = custom3;
	}
	public String getCustom4() {
		return custom4;
	}
	public void setCustom4(String custom4) {
		this.custom4 = custom4;
	}
	public String getCustom5() {
		return custom5;
	}
	public void setCustom5(String custom5) {
		this.custom5 = custom5;
	}
	public String getCustom6() {
		return custom6;
	}
	public void setCustom6(String custom6) {
		this.custom6 = custom6;
	}
	public String getCustom7() {
		return custom7;
	}
	public void setCustom7(String custom7) {
		this.custom7 = custom7;
	}
	public String getCustom8() {
		return custom8;
	}
	public void setCustom8(String custom8) {
		this.custom8 = custom8;
	}
	public String getCustom9() {
		return custom9;
	}
	public void setCustom9(String custom9) {
		this.custom9 = custom9;
	}
	public String getCustom10() {
		return custom10;
	}
	public void setCustom10(String custom10) {
		this.custom10 = custom10;
	}
	 */
	public String getShortdescription() {
		return shortdescription;
	}
	public void setShortdescription(String shortdescription) {
		this.shortdescription = shortdescription;
	}
	public String getLongdescription() {
		return longdescription;
	}
	public void setLongdescription(String longdescription) {
		this.longdescription = longdescription;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

}
