package com.tfsla.diario.securityService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.jsp.CmsJspLoginBean;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrgUnitManager;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsUUID;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.*;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.openCmsService;

public class TfsUserAuditPermission {
	
	//permisos
	private boolean Ia                  = false;
	//private boolean Ii                  = false;
	private boolean Iv                  = false;
	private boolean Ie                  = false;
	private boolean Ir                  = false;
	private boolean Ip                  = false;

	//private String PUB;
	//private String MOD;
	
	@SuppressWarnings("unused")
	private CmsJspActionElement cms;
	@SuppressWarnings("unused")
	private CmsJspLoginBean BeanCms;

	private List<CmsGroup> AvailableGroups = null;
	private String ModulePublication    = "administration";
	private String ModuleName    		= "modules";
	private String ModulePermissions	= "permissions";
	
	private String MaxLevelPermission   = "unavailable";
	private String Module               = "";
	@SuppressWarnings("unused")
	private String Site                 = "";
	private String Admin           		= "Administrators";
	//private String Importador           = "Importador";
	//Permiso de Edicion de sus Avisos,Noticias,Encuestas,etc
	private String Edicion         		= "Edit";
	//Permiso de Edicion de Terceros
	private String EdicionTerceros      = "Create";
	//Permiso de Publicacion
	private String Publicacion          = "Publish";
	//Permiso de Visualizacion 
	private String Visualizacion        = "View";
	
	public TfsUserAuditPermission(){		
	}
	
	public TfsUserAuditPermission(String module){
		Module = module;
	}
	
	public TfsUserAuditPermission(String module, CmsJspActionElement jspAction){
		Module = module;
		cms = jspAction;
	}
	
	public TfsUserAuditPermission(String module, CmsJspLoginBean beanCms){
		Module = module;
		BeanCms = beanCms;
	}
	
	public TfsUserAuditPermission(String module, String site){
		Module = module;
		Site = site;
	}
	
	public String getUserPermissionLevel(CmsJspActionElement action){		
		restartVariables();
		cms = action;

		setPermLevel(action.user("name")
				, action.getCmsObject()
				, getCurrentSite(action.getCmsObject())
				, getPublicacionId(action.getCmsObject()));
		
		return maxLevelPermission();
	}
		
	/*public String getUserLevel(CmsJspActionElement cms, String module){		
		List<CmsGroup> availableGroups = getCmsGroupsByUser(cms.getCmsObject(), cms.user("name"));
		String uVal = "";
		if(containsId(availableGroups, Admin)){
			uVal = getValueForImportador();
		}else{
			List<CmsGroup> groups = getModulesAvailablesByUser(cms.getCmsObject(), cms.user("name"), module, getPublicacionId(cms.getCmsObject()));
			
			if(contains(groups, Publicacion)){
				return uVal = getValueForPublicacion();
			}else if(contains(groups, EdicionTerceros)){
				return uVal = getValueForEdicionTerceros();
			}
			else if(contains(groups, Edicion)){
				return uVal = getValueForEdicion();
			}
			else if(contains(groups, Visualizacion)){
				return uVal = getValueForVisualizacion();
			}else{
				uVal = getValueForVisualizacion();
			}
		}
		
		return uVal;
	}*/
	
	public String getUserLevel(CmsJspActionElement cms, TipoEdicion cPublication, String module){		
		List<CmsGroup> availableGroups = getCmsGroupsByUser(cms.getCmsObject(), cms.user("name"));
		String uVal = "";
		if(containsId(availableGroups, Admin)){
			uVal = getValueForImportador();
		}else{
			List<CmsGroup> groups = getModulesAvailablesByUser(cms.getCmsObject(), cms.user("name"), module, cPublication.getId());
			
			if(contains(groups, Publicacion)){
				return uVal = getValueForPublicacion();
			}else 
			if(contains(groups, EdicionTerceros)){
				return uVal = getValueForEdicionTerceros();
			}
			else if(contains(groups, Edicion)){
				return uVal = getValueForEdicion();
			}
			else if(contains(groups, Visualizacion)){
				return uVal = getValueForVisualizacion();
			}else{
				uVal = getValueForVisualizacion();
			}
		}
		
		return uVal;
	}
	
	public boolean canPublish(CmsJspActionElement cms, TipoEdicion cPublication, String module){		
		List<CmsGroup> availableGroups = getCmsGroupsByUser(cms.getCmsObject(), cms.user("name"));
		boolean publish = false;
		if(containsId(availableGroups, Admin)){
			publish = true;
		}else{
			List<CmsGroup> groups = getModulesAvailablesByUser(cms.getCmsObject(), cms.user("name"), module, cPublication.getId());
			
			if(contains(groups, Publicacion)){
				publish = true;
			}else{
				publish = false;
			}			
		}
		
		return publish;
	}
	
	private List<CmsGroup> getModulesAvailablesByUser(CmsObject cmsObject, String user, String module, int pubId){
		List<CmsGroup> groups = getCmsGroupsByUser(cmsObject, user);
		List<CmsGroup> moduleGroupsAvailables = new ArrayList<CmsGroup>();
		String moduleName = "PUB_" + pubId + "_MOD_" + module.toLowerCase();
		for(CmsGroup group : groups){
			if(group.getName().contains(moduleName)){
				moduleGroupsAvailables.add(group);
			}
		}
		return moduleGroupsAvailables;
	}
	
	public String getUserPermissionLevel(CmsJspActionElement action, String user){		
		restartVariables();
		cms = action;

		setPermLevel(user
				, action.getCmsObject()
				, getCurrentSite(action.getCmsObject())
				, getPublicacionId(action.getCmsObject()));
		
		return maxLevelPermission();
	}
	
	public void createGroup(CmsObject cmsObject) throws CmsException{			
		TipoEdicionService tService = new TipoEdicionService();
		List<String> xmlSiteGroup = new ArrayList<String>();
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		
		try {
			@SuppressWarnings("unchecked")
			List<CmsSite> sites = OpenCms.getSiteManager().getAvailableSites(cmsObject, true);
			for(CmsSite site : sites){
				String proyectName = site.getSiteRoot().replaceAll("/sites/", "");
				List<TipoEdicion> publications = tService.obtenerTipoEdiciones(proyectName);
				for(TipoEdicion pub:publications){
					String pubVal = "PUB_" + pub.getId();
					xmlSiteGroup.add(pubVal);
					String pubId = ""+pub.getId();
					//First Modules					
					List<String> modules = config.getParamList(site.getSiteRoot(), pubId, ModulePublication, ModuleName + "_1");
					for(String module:modules){			
						String modVal = pubVal + "_MOD_" + module;
						xmlSiteGroup.add(modVal);			
						int count = 0;
						List<String> permissions = config.getParamList(site.getSiteRoot(), pubId, ModulePublication, ModulePermissions + "_1");
						for(String p : permissions){
							count++;
							String countVal = "_" + count + "_";
							String permissionVal = pubVal + "_MOD_" + module + countVal + p;
							xmlSiteGroup.add(permissionVal);	
						}
					}
					//Second Modules
					List<String> sModules = config.getParamList(site.getSiteRoot(), pubId, ModulePublication, ModuleName + "_2");
					for(String sModule:sModules){			
						String modVal = pubVal + "_MOD_" + sModule;
						xmlSiteGroup.add(modVal);			
						int count = 0;
						List<String> permissions = config.getParamList(site.getSiteRoot(), pubId, ModulePublication, ModulePermissions + "_2");
						for(String p : permissions){
							count++;
							String countVal = "_" + count + "_";
							String permissionVal = pubVal + "_MOD_" + sModule + countVal + p;
							xmlSiteGroup.add(permissionVal);	
						}
					}
					//NewsTypes
					List<String> nTypes = config.getParamList(site.getSiteRoot(), pubId, ModulePublication, "news-types");
					for(String n:nTypes){			
						String modVal = pubVal + "_MOD_" + n;
						xmlSiteGroup.add(modVal);	
					}
				}				
			}
			List<CmsGroup> availableSites = getAvailableCmsGroups(cmsObject, false);
								
			//xmlSiteGroup --> lista de grupos que deben crearce
			//Busca en la lista xmlSiteGroup, si no existe lo crea.
			for(String val:xmlSiteGroup){
				if(!containsId(availableSites, val)){
					String groupDescription = getGroupDescription(val);
					String groupParent = getGroupParent(val);
					try{
						cmsObject.createGroup(val, groupDescription, CmsGroup.FLAG_ENABLED, groupParent);		
					}catch(CmsException ex){						
					}								
				}					
			}
			
			//xmlSiteGroup --> lista de grupos que deben crearce
			//Vuelve a buscar todos los grupos si alguno no existe en la lista xmlSiteGroup lo borra.
			List<CmsGroup> aSites = getAvailableCmsGroups(cmsObject, false);
			for(CmsGroup g:aSites){
				String gValue = g.getName();
				if(!containsValue(xmlSiteGroup, gValue)){
					if(gValue.contains("PUB_")){				
						cmsObject.deleteGroup(gValue);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<String> obtenerTemplates(CmsObject cmsObject, String site, String pubId) throws Exception{
				
		return getCmsMediosTemplatesAvailables(cmsObject, site, pubId);	
	}
	
	public List<String> obtenerTemplateTipoNoticias(CmsObject cmsObject, String site, String pubId) throws Exception{
		
		return getCmsMediosTemplatesTipoNoticia(cmsObject, site, pubId);	
	}
	
	public void setTemplate(CmsObject cmsObject, String userName, String site, String pub, String template){
		List<String> templates = getCmsMediosTemplate(cmsObject, site, pub, template);
		
		List<String> modules = new ArrayList<String>();
		String userLevel = "";
		String userModule = "";
		String[] mod = {};
		try {
			cleanUserGroups(cmsObject, userName);
		} catch (CmsException e1) {
			e1.printStackTrace();
		}
		for(String t : templates){			
			try{
				mod = t.split("_");
			}catch(Exception e){}
			if(mod.length > 1){
				userLevel = mod[1];
				userModule = mod[0];
				try {
					setPermissionsWithoutDeleteGroup(cmsObject, userName, pub, userModule, userLevel);
				} catch (CmsException e) {
					e.printStackTrace();
				}
			}else{
				userModule = t;
			}
			modules.add(userModule);			
		}
		try {
			setPermissionsToUser(cmsObject, userName, pub, modules);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void setTemplateTipoNoticia(CmsObject cmsObject, String userName, String site, String pub, String template) throws CmsException{
		setPermissionNewsType(cmsObject, userName, pub, template);
	}
	/*public void createGroup2(CmsObject cmsObject, String user){
		TipoEdicionService tService = new TipoEdicionService();
		List<String> xmlSiteGroup = new ArrayList<String>();
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		try{
			
			@SuppressWarnings("unchecked")
			List<CmsSite> sites = OpenCms.getSiteManager().getAvailableSites(cmsObject, true);
			for(CmsSite site : sites){
				String proyectName = site.getSiteRoot().replaceAll("/sites/", "");
				List<TipoEdicion> publications = tService.obtenerTipoEdiciones(proyectName);
				for(TipoEdicion pub:publications){
					String pubVal = "PUB_" + pub.getId();
					xmlSiteGroup.add(pubVal);
					String pubId = ""+pub.getId();
					LinkedHashMap<String,String> providerFieldConfiguration = config.getGroupParam(site.getSiteRoot(), pubId, ModulePublication, "admin-template-users");
					
					for(Entry<String,String> e:providerFieldConfiguration.entrySet()){
						String key = e.getKey();
						String value = e.getValue();
						String aa = key + value;
					}
					Collection<String> a = providerFieldConfiguration.values();
					
					String editor = providerFieldConfiguration.get("editor-publicacion");
					String comentarios = providerFieldConfiguration.get("moderador-comentarios");
					String publicidad = providerFieldConfiguration.get("administraror-publicidad");
					String videos = providerFieldConfiguration.get("administrador-videos");
					String redactor = providerFieldConfiguration.get("redactor");
					
					
				}				
			}
			
		}catch(Exception e){
			
		}
	}*/
	
	private List<String> getCmsMediosTemplatesAvailables(CmsObject cmsObject, String site, String pubId) throws Exception{
		
		List<String> availableTemplates = new ArrayList<String>();
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		try{
			LinkedHashMap<String,String> providerFieldConfiguration = config.getGroupParam(site, pubId, ModulePublication, "admin-template-users");
			
			for(Entry<String,String> e:providerFieldConfiguration.entrySet()){
				availableTemplates.add(e.getKey());
			}
		}catch(Exception ex){
			throw ex;
		}
		
		
		return availableTemplates;
	}
	
private List<String> getCmsMediosTemplatesTipoNoticia(CmsObject cmsObject, String site, String pubId) throws Exception{
		
		List<String> availableTemplates = new ArrayList<String>();
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		try{
			String providerFieldConfiguration = config.getParam(site, pubId, ModulePublication, "news-types");
			String[] values = providerFieldConfiguration.split(",");
			for(String v:values){
				availableTemplates.add(v);
			}
		}catch(Exception ex){
			throw ex;
		}		
		
		return availableTemplates;
	}
	
	private List<String> getCmsMediosTemplate(CmsObject cmsObject, String site, String pubId, String template){
		List<String> modules = new ArrayList<String>();
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		String value = "";
		LinkedHashMap<String,String> providerFieldConfiguration = config.getGroupParam(site, pubId, ModulePublication, "admin-template-users");
		
		for(Entry<String,String> e:providerFieldConfiguration.entrySet()){
			String key = e.getKey();
			if(key.contains(template)){
				value = e.getValue();
			}			
		}
		
		String[] v = {};
		try{
			v = value.split(",");
		}catch(Exception e){}
		if(v.length > 0){
			for(String i:v){
				modules.add(i);
			}
		}else{
			modules.add(value);
		}
		
		return modules;
	}
	
	private void cleanUserGroups(CmsObject cmsObject, String user) throws CmsException{
		List<CmsGroup> availableGroupsOfUser = getCmsGroupsByUser(cmsObject, user);
		
		for (CmsGroup gToRemove : availableGroupsOfUser){			
			if(gToRemove.getName().contains("PUB_")){
				cmsObject.removeUserFromGroup(user, gToRemove.getName());
			}		
		}		
	}
	
	private void setPermissionsToUser(CmsObject cmsObject, String user, String pubId, List<String> modules) throws CmsException{
		
		List<CmsGroup> availableGroups = new ArrayList<CmsGroup>();
		for(String module : modules){
			String group = "PUB_" + pubId + "_MOD_" + module;
			for(CmsGroup cms:getModulesAvailablesByModule(cmsObject, group)){
				availableGroups.add(cms);
			}
		}				
		for(CmsGroup cms:availableGroups){
			
			cmsObject.addUserToGroup(user, cms.getName());
		}		
	}
	
	private void setPermissionNewsType(CmsObject cmsObject, String user, String pubId, String module) throws CmsException{
		
		try{
			String group = "PUB_" + pubId + "_MOD_" + module;
			cmsObject.addUserToGroup(user, group);	
		}catch(Exception ex){
		}
	}
	
	private void setPermissionsWithoutDeleteGroup(CmsObject cmsObject, String user, String pubId, String module, String userLevel) throws CmsException{
		List<String> newGroups = new ArrayList<String>();
		List<CmsGroup> availableGroups = new ArrayList<CmsGroup>();
		
		String group = "PUB_" + pubId + "_MOD_" + module;
		if(userLevel.contains("view")){
			newGroups.add(group + "_1_View");
		}else if(userLevel.contains("redactor")){
			newGroups.add(group + "_1_View");
			newGroups.add(group + "_2_Redactor");
		}else if(userLevel.contains("editor")){
			newGroups.add(group + "_1_View");
			newGroups.add(group + "_2_Redactor");
			newGroups.add(group + "_3_Editor");
		}else if(userLevel.contains("publish")){
			newGroups.add(group + "_1_View");
			newGroups.add(group + "_2_Redactor");
			newGroups.add(group + "_3_Editor");
			newGroups.add(group + "_4_Publish");
		}
		
		for(String p:newGroups){
			for(CmsGroup cms:getModulesAvailablesByModuleName(cmsObject, p)){
				availableGroups.add(cms);
			}
		}
		
		for(CmsGroup cms:availableGroups){
			cmsObject.addUserToGroup(user, cms.getName());
		}		
	}
	
	public void updateUserGroups(CmsObject cmsO, CmsUser user, List<String> newGroups, int size) throws NumberFormatException, CmsException{
		//for(int i=1; i<=size;i++) {
			List<CmsGroup> userGroups = getCmsGroupsByUser(cmsO, user.getName());
			
				for (CmsGroup gToRemove : userGroups) {
					if (newGroups == null || !newGroups.contains(gToRemove.getId().toString()) && gToRemove.getName().contains("PUB_") ) {
						try {
							cmsO.removeUserFromGroup(user.getName(), gToRemove.getName());
						} catch(Exception e) {
							@SuppressWarnings("unused")
							String error = e.toString();
						}						
					}
				}
			userGroups = getCmsGroupsByUser(cmsO, user.getName());
					
			if(newGroups != null && newGroups.size() > 0) {
				for (String gId : newGroups) {
					if(gId.length() > 30){
						CmsGroup gTOAdd = cmsO.readGroup(new CmsUUID(gId));	
						if(!containsId(userGroups, gTOAdd.getName())){
							try {
								cmsO.addUserToGroup(user.getName(), gTOAdd.getName());
							} catch (CmsException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		//}
	}
	
	private String getGroupParent(String val) {
		String op = "_";
		String parent = "";
		String[] groupName = val.split("_");
		if(groupName.length == 4){
			parent = groupName[0] + op + groupName[1];
		}if(groupName.length == 6){
			parent = groupName[0] + op + groupName[1] + op + groupName[2] + op + groupName[3];
		}
		return parent;
	}

	private String getGroupDescription(String val) {
		String description = "";
		String[] groupName = val.split("_");
		if(groupName.length == 6){
			if(groupName[5].equalsIgnoreCase("view")){
				description = "Permiso de visualización";
			}if(groupName[5].equalsIgnoreCase("edit")){
				description = "Permiso de edición";
			}if(groupName[5].equalsIgnoreCase("create")){
				description = "Permiso de edición terceros";
			}if(groupName[5].equalsIgnoreCase("publish")){
				description = "Permiso de publicación";
			}
			
		}
		return description;
	}

	public static boolean hasAllPermissions(List<CmsGroup> list, List<String> groups) {
		for(String i : groups){
			if(!containsId(list, i)){
				return false;
			}
		}
	    return true;
	}
	
	public boolean isActionAvailable(String module, TipoEdicion cPublication, CmsJspActionElement cms){
		String groupName = "PUB_" + cPublication.getId() + "_MOD_" + module.toLowerCase();
		
		setPermLevel(cms.user("name"), cms.getCmsObject(), groupName);
		String uL = maxLevelPermission();
		if(!uL.contains(Visualizacion)){
			return true;
		}
		
		return false;
	}
	
	public boolean isAdmin(CmsJspActionElement cms){
		String groupName = Admin;
		List<CmsGroup> groups = getCmsGroupsByUser(cms.getCmsObject(), cms.user("name"));
		if(containsId(groups, groupName)){
			return true;
		}		
		return false;
	}
	
	public boolean isWebUser(String user){
		String groupName = "webUser/";		
		if(user.startsWith(groupName)){
			return true;
		}		
		return false;
	}
	
	public boolean isWebMasterViewAvailable(CmsJspActionElement cmsA, String permissionRole){
		String groupName = permissionRole;
		List<CmsGroup> groups = getCmsGroupsByUser(cmsA.getCmsObject(), cmsA.user("name"));
		if(containsId(groups, groupName)){
			return true;
		}		
		return false;
	}
	
	public String getUserPermissionLevel(String user, CmsObject cms){		
		setPermLevel(user, cms, getCurrentSite(cms), getPublicacionId(cms));
		
		return maxLevelPermission();
	}
	
	/*@SuppressWarnings("unused")
	private String getUserLevel(){
		restartVariables();
		setPermLevel(cms.user("name")
				, cms.getCmsObject()
				, getCurrentSite(cms.getCmsObject())
				, getPublicacionId(cms.getCmsObject()));
		
		return maxLevelPermission();
	}
	
	private String getCurrentUserLevel(){
		restartVariables();
		setPermLevel(BeanCms.getUserName()
				, BeanCms.getCmsObject()
				, getCurrentSite(BeanCms.getCmsObject())
				, getPublicacionId(BeanCms.getCmsObject()));
		
		return maxLevelPermission();
	}*/
	
	@SuppressWarnings("unchecked")
	public boolean hasPermission(String module, TipoEdicion currentPublicacion, CmsJspActionElement cms){		
		@SuppressWarnings("rawtypes")
		List groups = getCmsGroupsByUser(cms.getCmsObject(), cms.user("name"));
		String currentModule = "PUB_" + currentPublicacion.getId() + "_MOD_" + module + "_1_view";		
		if(containsId(groups, Admin)){
			return true;
		}else if(containsId(groups, currentModule)){
			return true;
		}			
		
		return false;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	private List<CmsGroup> getCmsGroupsByUser(CmsObject cmsO, String user){
		List<CmsGroup> groups = new ArrayList<CmsGroup>();
		try {
			groups = cmsO.getGroupsOfUser(user);				
		} catch (CmsException e) {
			e.printStackTrace();
		}
		return groups;
	}
	
	/*public boolean areSameUsers(CmsUser u1, CmsUser currentUser, CmsJspActionElement cms, String module){
		String uL = getUserLevel(cms, module);
		if(uL != Visualizacion){
			if(uL == Edicion){
				if(u1.equals(currentUser)){
					return true;
				}
			}else{
				return true;
			}
		}
		return false;
	}*/
	
	public boolean areSameUsers(CmsUser u1, CmsUser currentUser, CmsJspActionElement cms, TipoEdicion cPublication, String module){
		String uL = getUserLevel(cms, cPublication, module);
		if(uL != Visualizacion){
			if(uL == Edicion){
				if(u1.equals(currentUser)){
					return true;
				}
			}else{
				return true;
			}
		}
		return false;
	}
	
	public boolean areSameUsers(CmsUser u1, CmsUser currentUser, CmsJspActionElement cms, TipoEdicion cPublication, String module, String path){
		
		TipoEdicionService tService = new TipoEdicionService();
		String uL = "";
		try {			
			TipoEdicion filePub = tService.obtenerTipoEdicion(cms.getCmsObject(), path);
			uL = getUserLevel(cms, filePub, module);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(uL != Visualizacion){
			if(uL == Edicion){
				if(u1.equals(currentUser)){
					return true;
				}
			}else{
				return true;
			}
		}
		return false;
	}
	
	/*public boolean isUserAvailable(CmsJspActionElement cms, String module){			
		String uL = getUserLevel(cms, module);

		if(uL != Visualizacion){
			return true;
		}
		return false;
	}*/
	
	public boolean isUserAvailable(CmsJspActionElement cms, TipoEdicion cPublication, String module){			
		String uL = getUserLevel(cms, cPublication, module);

		if(uL != Visualizacion){
			return true;
		}
		return false;
	}

	/*private boolean setPermissionLevel(String user, CmsObject cmsObject, String groupName) {
		
		String currentGroupName = groupName;
		
		List groups = getCmsGroupsByUser(cmsObject, user);
			for(int i = 0; i<groups.size(); i++){
				CmsGroup cmsGroup = (CmsGroup) groups.get(i);
				String parentGroupName = getParentGroupName(cmsObject, cmsGroup);
				
				if(parentGroupName != null && currentGroupName != null){
					if(parentGroupName.equalsIgnoreCase(currentGroupName)){				
						return true;									
					}
				}
			}
		
		return false;
	}*/
	
    public boolean isPublicationAvailable(String user, CmsObject cmsObject, int pubId) {
		String permName = "PUB_" + pubId;		
		List<CmsGroup> groups = getCmsGroupsByUser(cmsObject, user);
		if(containsId(groups, Admin)){
			return true;
		}else if(containsId(groups, permName)){
			return true;
		}			
		return false;
	}
    
    public boolean isSiteAvailable(String user, CmsObject cmsObject, String site) {//antes pubId
		@SuppressWarnings("rawtypes")
		TipoEdicionService tService = new TipoEdicionService();
		List groups = getCmsGroupsByUser(cmsObject, user);
		List<TipoEdicion> publications = tService.obtenerTipoEdiciones(site);
		for(TipoEdicion pub:publications){
			String pubVal = "PUB_" + pub.getId();
			for(int i = 0; i<groups.size(); i++){
				CmsGroup cmsGroup = (CmsGroup) groups.get(i);				
				if(cmsGroup.getName().equalsIgnoreCase(Admin)){
					return true;
				}else if(cmsGroup.getName().equalsIgnoreCase(pubVal)){
					return true;
				}				
			}
		}	
		return false;
	}
	
	private void setPermLevel(String user, CmsObject cmsObject, String groupName, int pubId) {		
		String permName = "PUB_" + pubId + "_MOD_" + Module.toLowerCase();
		@SuppressWarnings("rawtypes")
		List groups = getCmsGroupsByUser(cmsObject, user);
		for(int i = 0; i<groups.size(); i++){
			CmsGroup cmsGroup = (CmsGroup) groups.get(i);
			if(cmsGroup.getName().equalsIgnoreCase("Administrators")){
				Ia = true;
			}else if(cmsGroup.getName().equalsIgnoreCase(permName + "_1_" + Visualizacion)){
				Iv = true;
			}else if(cmsGroup.getName().equalsIgnoreCase(permName + "_2_"+ Edicion)){
				Ie = true;
			}else if(cmsGroup.getName().equalsIgnoreCase(permName + "_3_"+ EdicionTerceros)){
				Ir = true;
			}else if(cmsGroup.getName().equalsIgnoreCase(permName + "_4_"+ Publicacion)){
				Ip = true;					
			}
		}		
	}
	
	private void setPermLevel(String user, CmsObject cmsObject, String groupName) {
		@SuppressWarnings("rawtypes")
	
		List groups = getCmsGroupsByUser(cmsObject, user);
		for(int i = 0; i<groups.size(); i++){
			CmsGroup cmsGroup = (CmsGroup) groups.get(i);
			if(cmsGroup.getName().equalsIgnoreCase("Administrators")){
				Ia = true;
			}else if(cmsGroup.getName().equalsIgnoreCase(groupName + "_1_" + Visualizacion)){
				Iv = true;
			}else if(cmsGroup.getName().equalsIgnoreCase(groupName + "_2_"+ Edicion)){
				Ie = true;
			}else if(cmsGroup.getName().equalsIgnoreCase(groupName + "_3_"+ EdicionTerceros)){
				Ir = true;
			}else if(cmsGroup.getName().equalsIgnoreCase(groupName + "_4_"+ Publicacion)){
				Ip = true;					
			}
		}
		
	}
	
	private boolean isPermLevel(String user, CmsObject cmsObject, String groupName) {
		List<CmsGroup> groups = getCmsGroupsByUser(cmsObject, user);
			if(containsId(groups, groupName)){
				return true;
			}			
		
		return false;
	}
	
	private boolean isModuleChecked(String user, CmsObject cmsObject, List<CmsGroup> aGroup, String groupName) {
		@SuppressWarnings("rawtypes")
		List groups = getCmsGroupsByUser(cmsObject, user);
			int count = 0;
			for(int i = 0; i<groups.size(); i++){
				CmsGroup cGroup = (CmsGroup) groups.get(i);
				if(cGroup.getName().contains(groupName)){
					count++;
				}
			}			
			if(count == aGroup.size()){
				return true;
			}		
		
		return false;
	}
	
	private boolean isPublicationChecked(String user, CmsObject cmsObject, List<CmsGroup> aGroup, String groupName) {
		@SuppressWarnings("rawtypes")
		List groups = getCmsGroupsByUser(cmsObject, user);
			int count = 0;
			for(int i = 0; i<groups.size(); i++){
				CmsGroup cGroup = (CmsGroup) groups.get(i);
				if(cGroup.getName().contains(groupName)){
					count++;
				}
			}
			int countAvailable = 0;
			for(CmsGroup aG : aGroup){				
				if(aG.getName().contains(groupName)){
					countAvailable++;
				}
			}
			if(count == countAvailable && count > 0){
				return true;
			}			
			
		
		return false;
	}	
	
	public static boolean containsId(List<CmsGroup> list, String name) {
	    for (CmsGroup object : list) {
	        if (object.getName().equalsIgnoreCase(name)) {
	            return true;
	        }
	    }
	    return false;
	}
	
	public static boolean contains(List<CmsGroup> list, String name) {
	    for (CmsGroup object : list) {
	        if (object.getName().contains(name)) {
	            return true;
	        }
	    }
	    return false;
	}
	
	public static boolean containsValue(List<String> list, String val) {
	    for (String v : list) {
	        if (v.equalsIgnoreCase(val)) {
	            return true;
	        }
	    }
	    return false;
	}

	/*private String getParentGroupName(CmsObject cmsObject, CmsGroup cmsGroup) {
		try{
			CmsUUID parentId = cmsGroup.getParentId();
			CmsGroup parentGroup = cmsObject.readGroup(parentId);
			return parentGroup.getName();
			
		}catch (CmsException e){
			e.printStackTrace();
		}
		return null;
	}*/
	
	public String getUserName(CmsJspActionElement cms){
		return cms.user("name");
	}
	
	public CmsUser getCmsUser(String fileName, CmsObject cmsO) throws CmsException{
		CmsFile myFile = cmsO.readFile(fileName);
		CmsUUID uui = myFile.getUserCreated();
		CmsUser userCms = cmsO.readUser(uui);
		return userCms;
	}
	
	public String maxLevelPermission(){
		if(Ia){
			MaxLevelPermission = getValueForImportador();
		}else if(Ip){
			MaxLevelPermission = getValueForPublicacion();
		}else if(Ir){
			MaxLevelPermission = getValueForEdicionTerceros();
		}else if(Ie){
			MaxLevelPermission = getValueForEdicion();
		}else if(Iv){
			MaxLevelPermission = getValueForVisualizacion();
		}else{
			MaxLevelPermission = getValueForVisualizacion();
		}
			
		return MaxLevelPermission;
	}
	
	public boolean maxLevelPermissionSite(){
		if(Ia || Iv || Ie || Ir || Ip || Ia){
			return true;
		}
		
		return false;
	}
	
	private String getCurrentSite(CmsObject cms){
		String proyecto = null;
		try {
			proyecto = openCmsService.getCurrentSite(cms);
			return proyecto;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return proyecto;
	}
	
	public int getPublicacionId(CmsObject cms){
		TipoEdicionService tService = new TipoEdicionService();
		int pubId = 0;
		try {			
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());
			pubId= tEdicion.getId();
			return pubId;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pubId;
	}
	
	private void restartVariables(){
		Ia = false;
		Iv = false;
		Ie = false;
		Ir = false;
		Ip = false;
	}
	
	private List<CmsGroup> getModulesAvailablesByModule(CmsObject cmsObject, String module){
		List<CmsGroup> groups = getAvailableCmsGroups(cmsObject, false);
		List<CmsGroup> moduleGroupsAvailables = new ArrayList<CmsGroup>();
		
		for(CmsGroup group : groups){
			if(group.getName().contains(module)){
				moduleGroupsAvailables.add(group);
			}
		}
		return moduleGroupsAvailables;
	}
	
	private List<CmsGroup> getModulesAvailablesByModuleName(CmsObject cmsObject, String module){
		List<CmsGroup> groups = getAvailableCmsGroups(cmsObject, false);
		List<CmsGroup> moduleGroupsAvailables = new ArrayList<CmsGroup>();
		
		for(CmsGroup group : groups){
			if(group.getName().equalsIgnoreCase(module)){
				moduleGroupsAvailables.add(group);
			}
		}
		return moduleGroupsAvailables;
	}
	
	private List<CmsGroup> getModulesAvailablesByPublication(CmsObject cmsObject, String pub){
		List<CmsGroup> groups = getAvailableCmsGroups(cmsObject, false);
		List<CmsGroup> moduleGroupsAvailables = new ArrayList<CmsGroup>();
		
		for(CmsGroup group : groups){
			if(group.getName().contains(pub)){
				moduleGroupsAvailables.add(group);
			}
		}
		return moduleGroupsAvailables;
	}
	
	public List<CmsGroup> getCmsGroupModules(CmsObject object, int pubId){
		List<CmsGroup> groups = getAvailableCmsGroups(object, false);
		String gName = "PUB_" + pubId;
		List<CmsGroup> moduleGroups = new ArrayList<CmsGroup>();
		List<String> modules = new  ArrayList<String>();
		
		for(CmsGroup group : groups){
			if(group.getName().contains(gName)){
			String[] groupName = group.getName().split("_");
				if(groupName.length > 2){
					if(!modules.contains(groupName[3])){
						moduleGroups.add(group);
						modules.add(groupName[3]);
					}
				}
			}
		}
		
		return moduleGroups;
	}
	
	public List<CmsGroup> getCmsGroupPermissions(CmsObject object, int pubId, String module){
		List<CmsGroup> groups = getAvailableCmsGroups(object, false);	
		String gName = "PUB_"+ pubId + "_MOD_" + module;
		List<CmsGroup> moduleGroups = new ArrayList<CmsGroup>();
		List<String> modules = new  ArrayList<String>();
		for(CmsGroup group : groups){
			if(group.getName().contains(gName)){
			String[] groupName = group.getName().split("_");
				if(groupName.length > 4){
					if(!modules.contains(groupName[5])){
						moduleGroups.add(group);
						modules.add(groupName[5]);
					}
				}
			}
		}
		
		return moduleGroups;
	}
	
	@SuppressWarnings("unchecked")
	private List<CmsGroup> getAvailableCmsGroups(CmsObject object, boolean lock){		
		if(AvailableGroups != null && lock){
			return AvailableGroups;
		}		
		CmsOrgUnitManager oum = OpenCms.getOrgUnitManager();
		try {
			AvailableGroups = oum.getGroups(object,"", true);
		} catch (CmsException e) {
			e.printStackTrace();
		}
		
		return AvailableGroups;
	}
	
	public String getNameByCmsGroup(CmsGroup group){
		String groupName = group.getName();
		String[] groupNameArray = groupName.split("_");
				
		return groupNameArray[3];
	}
	
	public String getNameByCmsGroupPermission(CmsGroup group){
		String groupName = group.getName();
		String[] groupNameArray = groupName.split("_");
				
		return groupNameArray[5];
	}

	public boolean isGroupAvailable(CmsGroup group, boolean isUserAdmin){
			if(isUserAdmin){
				if(!group.getName().contains("PUB_")){
					return true;
				}
			}else if(!group.getName().contains("PUB_") && !group.getName().equalsIgnoreCase(Admin)){
				return true;
			}
			
		return false;
	}
		
	public boolean isPublicacionChecked(CmsObject object, String userName, int pubId){
		String pub = "PUB_" + pubId;
		List<CmsGroup> availableGroups = getModulesAvailablesByPublication(object, pub);
				
		if(isPublicationChecked(userName, object, availableGroups, pub)){
			return true;
		}
		
		return false;
	}
	
	public boolean isModuleChecked(CmsObject object, String userName, int pubId, String moduleName){		
		String pub = "PUB_" + pubId +"_MOD_"+ moduleName;
		List<CmsGroup> availableGroups = getModulesAvailablesByModule(object, pub);
		if(isModuleChecked(userName, object, availableGroups, pub)){
			return true;
		}				
		return false;
	}
	
	public boolean isPermissionChecked(CmsObject object, String userName, int pubId, String moduleName, String permissionName){		
		//String pub = "PUB_" + pubId +"_MOD_"+ moduleName + "_" + permissionName;
		if(isPermLevel(userName, object, permissionName)){
			return true;
		}
						
		return false;
	}
	
	private String getValueForVisualizacion() {	
		return Visualizacion;
	}

	private String getValueForPublicacion() {
		return Publicacion;
	}

	private String getValueForEdicion() {
		return Edicion;
	}

	private String getValueForEdicionTerceros() {
		return EdicionTerceros;
	}

	private String getValueForImportador() {
		return Publicacion;
	}
}
