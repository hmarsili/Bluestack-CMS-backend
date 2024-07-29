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
import com.tfsla.rankUsers.model.TfsUserRankResults;
import com.tfsla.rankUsers.service.RankService;
import com.tfsla.statistics.model.TfsUserStatsOptions;

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
	private float general=-1F;
	private int receivedviews=-1;
	private int publicatednews=-1;
	private int receivedrecomendations=-1;
	private int receivedcomments=-1;
	private int makedcomments=-1;
	private int declinedcomments=-1;
	private int countvalorations=-1;
	private int averagevalorations=-1;
	private int positivevalorations=-1;
	private int negativevalorations=-1;
	private int custom1=-1;
	private int custom2=-1;
	private int custom3=-1;
	private int custom4=-1;
	private int custom5=-1;
	private int custom6=-1;
	private int custom7=-1;
	private int custom8=-1;
	private int custom9=-1;
	private int custom10=-1;
	
	private int countTrivias;
	private int countAllTrivias;
	private int countTriviasByScale;
	private int countTriviasByClassification;
	
	private void loadUserStats()
	{
		RankService rank = new RankService();

		//Options rank processor
		TfsUserStatsOptions options = new TfsUserStatsOptions();
		/*
		options.setShowHits(true);
		options.setShowRecomendacion(true);*/
		options.setShowGeneralRank(true);
		options.setShowCantidadValoraciones(true);
		options.setShowComentariosRealizados(true);
		options.setShowComentariosRechazados(true);
		options.setShowComentariosRecibidos(true);
		options.setShowNotasPublicadas(true);
		options.setShowRecomendacionesRecibidas(true);
		options.setShowValoracionesRecibidas(true);
		options.setShowVisitasRecibidas(true);
		options.setShowCustom1(true);
		options.setShowCustom2(true);
		options.setShowCustom3(true);
		options.setShowCustom4(true);
		options.setShowCustom5(true);
		options.setShowCustom6(true);
		options.setShowCustom7(true);
		options.setShowCustom8(true);
		options.setShowCustom9(true);
		options.setShowCustom10(true);
		
	    options.setUsuario(user.getId().toString());
		
		options.setRankMode(TfsUserStatsOptions.RANK_GENERAL);
		 TfsUserRankResults result=null;
			try {
				result = rank.getStatistics(options);
			} catch (RemoteException e1) {
				//LOG.error(e1);
			}

		//TfsUserRankResults res = rank.getStatistics(cms, options);
		if ( result != null && result.getRank() != null && result.getRank().length > 0 ) {
			general = result.getRank()[0].getGeneralRank();
			receivedviews=result.getRank()[0].getVisitasRecibidas();
			publicatednews=result.getRank()[0].getNotasPublicadas();
			receivedrecomendations=result.getRank()[0].getRecomendacionesRecibidas();
			receivedcomments=result.getRank()[0].getComentariosRecibidos();
			makedcomments=result.getRank()[0].getComentariosRealizados();
			declinedcomments=result.getRank()[0].getComentariosRechazados();
			countvalorations=result.getRank()[0].getCantidadValoraciones();
			averagevalorations=(result.getRank()[0].getCantidadValoraciones()==0)?0:result.getRank()[0].getValoracionesRecibidas()/result.getRank()[0].getCantidadValoraciones();
			positivevalorations=result.getRank()[0].getValoracionesRecibidas();
			negativevalorations=result.getRank()[0].getValoracionesRecibidas()-result.getRank()[0].getCantidadValoraciones();
			custom1=result.getRank()[0].getCustom1();
			custom2=result.getRank()[0].getCustom2();
			custom3=result.getRank()[0].getCustom3();
			custom4=result.getRank()[0].getCustom4();
			custom5=result.getRank()[0].getCustom5();
			custom6=result.getRank()[0].getCustom6();
			custom7=result.getRank()[0].getCustom7();
			custom8=result.getRank()[0].getCustom8();
			custom9=result.getRank()[0].getCustom9();
			custom10=result.getRank()[0].getCustom10();
		}
		else
		{
			general = 0;
			receivedviews=0;
			publicatednews=0;
			receivedrecomendations=0;
			receivedcomments=0;
			makedcomments=0;
			declinedcomments=0;
			countvalorations=0;
			averagevalorations=0;
			positivevalorations=0;
			negativevalorations=0;
			custom1=0;
			custom2=0;
			custom3=0;
			custom4=0;
			custom5=0;
			custom6=0;
			custom7=0;
			custom8=0;
			custom9=0;
			custom10=0;
		}
	}

	
	public float getGeneral() {
		if (general==-1)
			loadUserStats();
		return general;
	}

	public int getReceivedviews() {
		if (receivedviews==-1)
			loadUserStats();
		return receivedviews;
	}

	public int getPublicatednews() {
		if (publicatednews==-1)
			loadUserStats();
		return publicatednews;
	}

	public int getReceivedrecomendations() {
		if (receivedrecomendations==-1)
			loadUserStats();
		return receivedrecomendations;
	}

	public int getReceivedcomments() {
		if (receivedcomments==-1)
			loadUserStats();
		return receivedcomments;
	}

	public int getMakedcomments() {
		if (makedcomments==-1)
			loadUserStats();
		return makedcomments;
	}

	public int getDeclinedcomments() {
		if (declinedcomments==-1)
			loadUserStats();
		return declinedcomments;
	}

	public int getCountvalorations() {
		if (countvalorations==-1)
			loadUserStats();
		return countvalorations;
	}

	public int getAveragevalorations() {
		if (averagevalorations==-1)
			loadUserStats();
		return averagevalorations;
	}

	public int getPositivevalorations() {
		if (positivevalorations==-1)
			loadUserStats();
		return positivevalorations;
	}

	public int getNegativevalorations() {
		if (negativevalorations==-1)
			loadUserStats();
		return negativevalorations;
	}

	public int getCustom1() {
		if (custom1==-1)
			loadUserStats();
		return custom1;
	}

	public int getCustom2() {
		if (custom2==-1)
			loadUserStats();
		return custom2;
	}

	public int getCustom3() {
		if (custom3==-1)
			loadUserStats();
		return custom3;
	}

	public int getCustom4() {
		if (custom4==-1)
			loadUserStats();
		return custom4;
	}

	public int getCustom5() {
		if (custom5==-1)
			loadUserStats();
		return custom5;
	}

	public int getCustom6() {
		if (custom6==-1)
			loadUserStats();
		return custom6;
	}

	public int getCustom7() {
		if (custom7==-1)
			loadUserStats();
		return custom7;
	}

	public int getCustom8() {
		if (custom8==-1)
			loadUserStats();
		return custom8;
	}

	public int getCustom9() {
		if (custom9==-1)
			loadUserStats();
		return custom9;
	}

	public int getCustom10() {
		if (custom10==-1)
			loadUserStats();
		return custom10;
	}
	
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
