package com.tfsla.diario.model;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import org.opencms.file.CmsObject;
import com.tfsla.diario.terminos.model.Persons;
import org.opencms.main.CmsException;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;


public class TfsPersona {

	Persons persona = null;
	Map<String,Boolean> tipos=null;
	protected CmsObject cms = null;
	public TfsPersona()
	{
		tipos=null;
		cms=null;
		
	}
	
	public TfsPersona(Persons persona,CmsObject cms)
	{
		this.persona = persona;
		this.cms=cms;
		tipos=null;	
		
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Boolean> getIsintype()
	{
		if (tipos==null)
		{
			
			// CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
			tipos = new HashMap<String, Boolean>();
			
			try {
				//traer la lista de categorias de personas
				
				
				String parentPath = "/system/categories/personas/";
									
				String referencePath = cms.getRequestContext().getUri();
				
				CmsCategoryService cService = new CmsCategoryService();
				List<CmsCategory> categories = cService.readCategories(cms,
			                                                  parentPath,
			                                                  false,
			                                                  referencePath);
			                                                  
			        for (CmsCategory category : categories)
			        {
			
			        	if(persona.getType().contains(category.getPath())){
			        		tipos.put(category.getTitle(), true);
			        	}
			
			        }
			} catch (CmsException e) {
				
				e.printStackTrace();
			}
			

		}
			
		return tipos;
		
	}
	public void setCms(CmsObject cms) {
		this.cms = cms;
	}
	public String getEmail()
	{
		return persona.getEmail();
	}
	
	public long getId_person() {
		return persona.getId_person();
	}
	
	public String getLastmodified() {
		return persona.getLastmodified();
	}
	
	public String getName() {
		return persona.getName();
	}
	
	public Date getBirthdate() {
		return persona.getBirthdate();
	}
	
	
	public String getNickname() {
		return persona.getNickname();
	}
	
	public String getPhoto() {
		return persona.getPhoto();
	}
	

	public String getUrl() {
		return persona.getUrl();
	}
	
	
	public String getNacionality() {
		return persona.getNacionality();
	}
	
	
	public int getApproved() {
		return persona.getApproved();
	}
	
	public String getTwitter() {
		return persona.getTwitter();
	}
	
	public String getFacebook() {
		return persona.getFacebook();
	}
	
	public String getGoogle() {
		return persona.getGoogle();
	}
	
	public String getLinkedin() {
		return persona.getLinkedin();
	}
	
	public String getCustom1() {
		return persona.getCustom1();
	}
	
	public String getCustom2() {
		return persona.getCustom2();
	}
	
	
	public String getShortdescription() {
		return persona.getShortdescription();
	}
	
	public String getLongdescription() {
		return persona.getLongdescription();
	}
	
	public String getType() {
		return persona.getType();
	}
	

	
	
}
