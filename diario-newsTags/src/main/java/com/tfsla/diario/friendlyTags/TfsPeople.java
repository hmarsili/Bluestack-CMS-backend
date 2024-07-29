package com.tfsla.diario.friendlyTags;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.I_CmsXmlDocument;

import com.tfsla.diario.model.TfsVideo;
import com.tfsla.diario.terminos.data.PersonsDAO;
import com.tfsla.diario.terminos.model.Persons;

public class TfsPeople {
	protected static final Log LOG = CmsLog.getLog(TfsVideo.class);

	protected long id_person;
	protected String lastmodified;
	protected String name; 
	protected String email; 
	protected Date birthdate;
	protected String nickname; 
	protected String photo; 
	protected String url; 
	protected String nacionality; 
	protected String twitter;
	protected String facebook;
	protected String google;
	protected String linkedin;
	protected String custom1;
	protected String custom2;
	protected String shortdescription;
	protected String longdescription;
	protected String type;
	protected int approved;
	
	List<String> categorylist=null;
	List<String> formatslist=null;

	Map<String,Boolean> categories=null;
	Map<String,Boolean> subCategories=null;
	
	Map<String,Boolean> formatsMap=null;

	public Map<String,Boolean> getHascategory()
	{
		return categories;
	}

	public Map<String,Boolean> getIsinsidecategory()
	{			
		return subCategories;
	}
	
	public Map<String,Boolean> getHasformat()
	{			
		return formatsMap;
	}
	
	public TfsPeople(String wordPerson, int cantidad, String order) throws Exception{
		//CmsResource resource = null;
		Persons people = new Persons();
		PersonsDAO peopledao = new PersonsDAO();
		
		try {
			people = peopledao.getPersonasByWord(wordPerson, cantidad, order).get(0);		
			
			id_person = people.getId_person();
			lastmodified = people.getLastmodified();
			name = people.getName(); 
			email = people.getEmail(); 
			birthdate = people.getBirthdate();
			nickname = people.getNickname(); 
			photo = people.getPhoto(); 
			url = people.getUrl(); 
			nacionality = people.getNacionality(); 
			twitter = people.getTwitter();
			facebook = people.getFacebook();
			google = people.getGoogle();
			linkedin = people.getLinkedin();
			custom1 = people.getCustom1();
			custom2 = people.getCustom2();
			shortdescription = people.getShortdescription();
			longdescription = people.getLongdescription();
			type = people.getType();
			approved = people.getApproved();
		
		}catch (CmsException e) {
			LOG.error("Error al obtener la informacion del video",e);
		}
	}
	
	public TfsPeople(long idPerson) throws Exception{
		//CmsResource resource = null;
		Persons people = new Persons();
		PersonsDAO peopledao = new PersonsDAO();
		
		try {
			people = peopledao.getPersonaById(idPerson);		
			
			id_person = people.getId_person();
			lastmodified = people.getLastmodified();
			name = people.getName(); 
			email = people.getEmail(); 
			birthdate = people.getBirthdate();
			nickname = people.getNickname(); 
			photo = people.getPhoto(); 
			url = people.getUrl(); 
			nacionality = people.getNacionality(); 
			twitter = people.getTwitter();
			facebook = people.getFacebook();
			google = people.getGoogle();
			linkedin = people.getLinkedin();
			custom1 = people.getCustom1();
			custom2 = people.getCustom2();
			shortdescription = people.getShortdescription();
			longdescription = people.getLongdescription();
			type = people.getType();
			approved = people.getApproved();
		
		}catch (CmsException e) {
			LOG.error("Error al obtener la informacion del video",e);
		}
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
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getNacionality() {
		return nacionality;
	}
	public void setNacionality(String nacionality) {
		this.nacionality = nacionality;
	}
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
