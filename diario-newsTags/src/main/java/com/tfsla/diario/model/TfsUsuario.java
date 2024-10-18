package com.tfsla.diario.model;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionBaseService;
import com.tfsla.diario.ediciones.services.TriviasService;
import com.tfsla.opencms.webusers.TfsUserHelper;
import com.tfsla.opencms.webusers.openauthorization.common.ProviderConfigurationLoader;
import com.tfsla.opencms.webusers.openauthorization.common.SocialProvider;


public class TfsUsuario {

	CmsUser user = null;
	Map<String,Boolean> providers=null;
	Map<String,Boolean> grupos=null;
	Map<String,String> infoextra=null;
	protected CmsObject cms = null;
	public TfsUsuario()
	{
		grupos=null;	
		infoextra=null;
		cms=null;
	}
	
	public TfsUsuario(CmsUser user,CmsObject cms)
	{
		this.user = user;
		this.cms=cms;
		grupos=null;	
		infoextra=null;
	}
	public Map<String,String> getInfoextra()
	{
		if (infoextra==null)
		{
			infoextra=new HashMap<String, String>();
			String key, value;
			@SuppressWarnings("rawtypes")
			Iterator iterator = user.getAdditionalInfo().keySet().iterator();
			while (iterator.hasNext()) {
				try{
			    key = iterator.next().toString();
			    value = (user.getAdditionalInfo().get(key)!=null?user.getAdditionalInfo().get(key).toString():"");
			    if( key!=null && value!=null){
			    infoextra.put(key, value);
			    }
				}catch(Exception e){
					e.printStackTrace();
			    	
			    }
			}
			
						
		}
		return infoextra; 
	}
	@SuppressWarnings("unchecked")
	public Map<String,Boolean> getIsingroup()
	{
		if (grupos==null)
		{
			List<CmsGroup> listagrupos=null;
			// CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
			grupos = new HashMap<String, Boolean>();
			
			try {
				listagrupos =cms.getGroupsOfUser(user.getName(),true, true);
			} catch (CmsException e) {
				
				e.printStackTrace();
			}
			for(CmsGroup gr: listagrupos){
			
			grupos.put(gr.getName(), true);
			}

		}
			
		return grupos;
		
	}
	@SuppressWarnings("unchecked")
	public Map<String,Boolean> getIsassociatedto()
	{
		if (providers == null)
		{
			List<SocialProvider> socialProviders = null;
			providers = new HashMap<String, Boolean>();
			
			String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
			String publication = "0";
			TipoEdicionBaseService tService = new TipoEdicionBaseService();
			try {
				TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());			
				if (tEdicion!=null)
					publication = "" + tEdicion.getId();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			ProviderConfigurationLoader configLoader = new ProviderConfigurationLoader();
			socialProviders = configLoader.getProviders(siteName, publication);
			TfsUserHelper userHelper = new TfsUserHelper(user);
			
			for(SocialProvider pr : socialProviders) {
				providers.put(pr.getName(), userHelper.hasProviderAssociated(pr.getName()));
			}
		}
		
		return providers;
	}
	
	public void setCms(CmsObject cms) {
		this.cms = cms;
	}
	
	public String getEmail()
	{
		return user.getEmail();
	}
	
	public String getLastname()
	{
		return user.getLastname();
	}

	public String getFirstname()
	{
		return user.getFirstname();
	}

	public String getId()
	{
		return user.getId().getStringValue();
	}
	
	public boolean isIsloggedin()
	{
		return !user.isGuestUser();
	}
	public String getFullname()
	{
		return user.getFullName();
	}
	public String getOu()
	{
		return user.getOuFqn();
	}
	public String getNickname()
	{
		return user.getAdditionalInfo("APODO")==null?"":user.getAdditionalInfo("APODO").toString();
		
	}
	public String getUsername()
	{
		return user.getName();
		
	}
	
	public String getState()
	{
		String descripcion="";
		if (user!=null){
			String pending =(String) user.getAdditionalInfo("USER_PENDING");

			if (!user.isEnabled()) {
				descripcion = "Inactivo";
				
			}
			else if (pending !=null && pending.equals("true")) {
				descripcion = "Pendiente";
				
			}
			else {
				descripcion = "Activo";
				
			}
		}
		return descripcion;
	}

	
	
	private int countTrivias;
	private int countAllTrivias;
	private int countTriviasByScale;
	private int countTriviasByClassification;
	
	
		
	public int getUserCountTriviasByScale() {
		
		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
			   siteName = siteName.replaceAll("/sites/", "");
		
		int publication = 0;
		TipoEdicionBaseService tService = new TipoEdicionBaseService();
		try {
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());			
			if (tEdicion!=null)
				publication = tEdicion.getId();
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		TriviasService triviaService = new TriviasService();
		
		countTriviasByScale = triviaService.getCountTriviasUsers(siteName,publication,user.getId().toString(),"scale");
	       
		return countTriviasByScale;
	}
	
	public int getUserCountTriviasByClassification() {
		
		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
		       siteName = siteName.replaceAll("/sites/", "");
		
		int publication = 0;
		TipoEdicionBaseService tService = new TipoEdicionBaseService();
		try {
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());			
			if (tEdicion!=null)
				publication = tEdicion.getId();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		TriviasService triviaService = new TriviasService();
		
		countTriviasByClassification = triviaService.getCountTriviasUsers(siteName,publication,user.getId().toString(),"classification");
	       
		return countTriviasByClassification;
	}
	
	public int getUserCountTrivias() {
		
		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
		       siteName = siteName.replaceAll("/sites/", "");
		
		int publication = 0;
		TipoEdicionBaseService tService = new TipoEdicionBaseService();
		try {
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());			
			if (tEdicion!=null)
				publication = tEdicion.getId();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		TriviasService triviaService = new TriviasService();
		
		countTrivias = triviaService.getCountTriviasUsers(siteName,publication,user.getId().toString());
	  
		return countTrivias;
	}
	
	public int getUserCountAllResultsTrivias() {
		
		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
			   siteName = siteName.replaceAll("/sites/", "");
		
		int publication = 0;
		TipoEdicionBaseService tService = new TipoEdicionBaseService();
		try {
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());			
			if (tEdicion!=null)
				publication = tEdicion.getId();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		TriviasService triviaService = new TriviasService();
		
		countAllTrivias = triviaService.getCountAllResultsUsers(siteName,publication,user.getId().toString(),null);
        
		return countAllTrivias;
	}
	
}
