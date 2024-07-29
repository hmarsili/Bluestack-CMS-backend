package com.tfsla.diario.webservices.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.friendlyTags.TfsNoticiasListTag;
import com.tfsla.diario.friendlyTags.TfsUserListTag;
import com.tfsla.diario.newsCollector.A_NewsCollector;
import com.tfsla.diario.newsCollector.EdicionImpresaHomeNewsCollector;
import com.tfsla.diario.newsCollector.EdicionImpresaNewsCollector;
import com.tfsla.diario.newsCollector.LuceneNewsCollector;
import com.tfsla.diario.newsCollector.RankingNewsCollector;

public class ServiceHelper {
	
	private static final String MODULE_CONFIGURATION_NAME = "webservices";
	
	public static String getRequestAsString(HttpServletRequest request) throws IOException {
		StringBuffer jb = new StringBuffer();
		String line = null;
		BufferedReader reader = request.getReader();
		while ((line = reader.readLine()) != null)
			jb.append(line);
		return jb.toString();
	}
	
	public static synchronized WebServicesConfiguration getWSConfiguration() {
		return getWSConfiguration("", "");
	}
	
	public static synchronized WebServicesConfiguration getWSConfiguration(String siteName, String publication) {
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		WebServicesConfiguration configuration = new WebServicesConfiguration();
		configuration.setUsersHiddenFields(config.getParamList(siteName, publication, MODULE_CONFIGURATION_NAME, "usersHiddenFields"));
		configuration.setImageExtensions(config.getParamList(siteName, publication, MODULE_CONFIGURATION_NAME, "imageExtensions"));
		configuration.setVideoExtensions(config.getParamList(siteName, publication, MODULE_CONFIGURATION_NAME, "videoExtensions"));
		configuration.setGuestToken(config.getParam(siteName, publication, MODULE_CONFIGURATION_NAME, "guestToken"));
		configuration.setDefaultSite(config.getParam(siteName, publication, MODULE_CONFIGURATION_NAME, "defaultSite"));
		configuration.setDefaultPublication(config.getParam(siteName, publication, MODULE_CONFIGURATION_NAME, "defaultPublication"));
	
		String param = config.getParam(siteName, publication, MODULE_CONFIGURATION_NAME, "tokensDuration");
		if(param != null && !param.equals("")) {
			try {
				configuration.setTokensDuration(Integer.parseInt(param));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		param = config.getParam(siteName, publication, MODULE_CONFIGURATION_NAME, "allowProjectSwitch");
		if(param != null && !param.equals("")) {
			try {
				configuration.setAllowProjectSwitch(Boolean.parseBoolean(param));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return configuration;
	}
	
	public static synchronized int getMinValueConfig(ServletRequest request) {
		int rdo= 3; //por defecto es 3
		String siteName = OpenCms.getSiteManager().getCurrentSite(CmsFlexController.getCmsObject(request)).getSiteRoot();

	   	TipoEdicionService tService = new TipoEdicionService();

	   	TipoEdicion currentPublication=null;
	   	try {
			currentPublication = tService.obtenerEdicionOnlineRoot(siteName.substring(siteName.lastIndexOf("/")+1));
		} catch (Exception e) {
			e.printStackTrace();
		}
			
	   	String publication = "" + (currentPublication!=null?currentPublication.getId():"");
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		String aux = config.getParam(siteName, publication, "adminNewsConfiguration", "minimunNumberOfSearchCharacters");
		try{
			rdo = Integer.parseInt(aux);
		}catch (NumberFormatException nfe){
			nfe.printStackTrace();
		}
		return rdo;
	}

	
	public static  A_NewsCollector getNewsCollector(Map<String,Object> parameters, String order)
	{
		A_NewsCollector bestCollector = new LuceneNewsCollector();
		
		A_NewsCollector collector = new LuceneNewsCollector();
		if (collector.canCollect(parameters)) {
				if (collector.canOrder(order))
					return collector;
				else 
					bestCollector = collector;
		}
		
		collector = new RankingNewsCollector();
		if (collector.canCollect(parameters)) {
				if (collector.canOrder(order))
					return collector;
				else 
					bestCollector = collector;
		}
	
		collector = new EdicionImpresaHomeNewsCollector();
		if (collector.canCollect(parameters)) {
			if (collector.canOrder(order))
				return collector;
			else 
				bestCollector = collector;
		}
			
		collector = new EdicionImpresaNewsCollector();
		if (collector.canCollect(parameters)) {
			if (collector.canOrder(order))
				return collector;
			else 
				bestCollector = collector;
		}	
			
		return bestCollector;
	}

	
	public static synchronized String[] getFilterParameters(ServiceType serviceType) {
		switch(serviceType) {
			case NEWS: return getNewsFilterParameters();
			
			case USERS_LIST: return getUsersFilterParameters();
			
			case POSTS_LIST: return getPostsFilterParameters();
		}
		
		return null;
	}
	
	private static String[] getNewsFilterParameters() {
		String[] parameters = {
			TfsNoticiasListTag.param_advancedFilter, TfsNoticiasListTag.param_age, TfsNoticiasListTag.param_author,
			TfsNoticiasListTag.param_category, TfsNoticiasListTag.param_edition, TfsNoticiasListTag.param_filter,
			TfsNoticiasListTag.param_from, TfsNoticiasListTag.param_group, TfsNoticiasListTag.param_newscreator,
			TfsNoticiasListTag.param_newstype, TfsNoticiasListTag.param_onmainpage, TfsNoticiasListTag.param_order,
			TfsNoticiasListTag.param_persons, TfsNoticiasListTag.param_publication, TfsNoticiasListTag.param_to,
			TfsNoticiasListTag.param_searchIndex, TfsNoticiasListTag.param_section, TfsNoticiasListTag.param_showtemporal,
			TfsNoticiasListTag.param_state, TfsNoticiasListTag.param_tags, TfsNoticiasListTag.param_zone,
			TfsNoticiasListTag.param_url
		};

		return parameters;
	}
	
	private static String[] getUsersFilterParameters() {
		String[] parameters = {
				TfsUserListTag.param_age, TfsUserListTag.param_filter, TfsUserListTag.param_from,
				TfsUserListTag.param_group, TfsUserListTag.param_ou, TfsUserListTag.param_state,
				TfsUserListTag.param_to, TfsUserListTag.param_order, "id", "username"
		};

		return parameters;
	}
	
	private static String[] getPostsFilterParameters() {
		String[] parameters = {
				"userid", "site", "publication", "from", "status"
		};

		return parameters;
	}
}
