package org.opencms.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opencms.main.CmsLog;

/**
 * @author vpode
 *
 */
public class CPMConfig {
	public static int MODULE_LEVEL_GENERAL = 1;
	public static int MODULE_LEVEL_SITE = 2;
	public static int MODULE_LEVEL_PUBLICATION = 3;
	public static int MODULE_UNDEFINED = 4;
	
	private Map<String,CPMModuleConfig> modules;
	private Map<String,CPMSiteConfig> sites;
	
	
	public CPMConfig()
	{
		modules = new LinkedHashMap<String,CPMModuleConfig>();
		sites = new LinkedHashMap<String,CPMSiteConfig>();
	}
	
	public CPMSiteConfig[] getSites() {
		return ((LinkedHashMap<String,CPMSiteConfig>) sites).values().toArray(new CPMSiteConfig[]{});
	}
	
	public CPMModuleConfig[] getModules() {
		return ((LinkedHashMap<String,CPMModuleConfig>) modules).values().toArray(new CPMModuleConfig[]{});
	}

	
	public void addModule(String site, String publication, CPMModuleConfig module)
	{
		if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(MessagesCmsMedios.get().getBundle().key(MessagesCmsMedios.INIT_CMSMEDIOS_MODULE_ADD_0, module.getName()));
        }
		
		CPMSiteConfig siteConf = sites.get(site);
		if (siteConf==null) {
			siteConf = new CPMSiteConfig();
			siteConf.setName(site);
		}
			
		CPMPublicationConfig pubConf = siteConf.getPublicationConfig(publication);
		if (pubConf==null) {
			pubConf = new CPMPublicationConfig();
			pubConf.setName(site);
			
			siteConf.addPublication(pubConf);
		}
		
		pubConf.addModule(module);
	}
	
	public void addModule(String site, CPMModuleConfig module)
	{
		if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(MessagesCmsMedios.get().getBundle().key(MessagesCmsMedios.INIT_CMSMEDIOS_MODULE_ADD_0, module.getName()));
        }
		
		CPMSiteConfig siteConf = sites.get(site);
		if (siteConf==null) {
			siteConf = new CPMSiteConfig();
			siteConf.setName(site);
		}
		siteConf.addModule(module);
	}

	public void addModule(CPMModuleConfig module)
	{
		if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(MessagesCmsMedios.get().getBundle().key(MessagesCmsMedios.INIT_CMSMEDIOS_MODULE_ADD_0, module.getName()));
        }
		modules.put(module.getName(), module);
	}
	

	public void addSite(CPMSiteConfig site)
	{
		if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(MessagesCmsMedios.get().getBundle().key(MessagesCmsMedios.INIT_CMSMEDIOS_SITE_ADD_0, site.getName()));
        }
		sites.put(site.getName(), site);
	}

	/**
	 * Copy a module configuration to a publication.
	 * The module should exist in the site or in general otherwise an exception is thrown.
	 * If the module definition doesnt exist an exception is thrown.
	 *
	 * @param site
	 * @param publication
	 * @param module
	 * @throws Exception
	 */
	public void copyModule(String site, String publication, String module) throws Exception{
		
		int currentLevel = getCurrentModuleLevel(site, publication, module);
		if (currentLevel==MODULE_UNDEFINED)
			throw new Exception("Module " + module + " undefined ");
			
		if (currentLevel==MODULE_LEVEL_PUBLICATION)
			throw new Exception("Module " + module + " already existis in publication " + publication);

		CPMModuleConfig moduleConfig = getModule(site,publication,module);		
		CPMModuleConfig copyModule = moduleConfig.copyOfMe();
		
		CPMSiteConfig siteConfig = sites.get(site);
		if (siteConfig==null) {
			siteConfig = new CPMSiteConfig();
			siteConfig.setName(site);
			sites.put(site, siteConfig);
		}
		
		CPMPublicationConfig publicationConfig = siteConfig.getPublicationConfig(publication);
		if (publicationConfig==null)
		{
			publicationConfig = new CPMPublicationConfig();
			publicationConfig.setName(publication);
			siteConfig.addPublication(publicationConfig);
		}	
		publicationConfig.addModule(copyModule);
	}

	/**
	 * Copy a module configuration to a site.
	 * The module should exist in general otherwise an exception is thrown.
	 * If the module definition doesnt exist an exception is thrown.
	 *
	 * @param site
	 * @param module
	 * @throws Exception
	 */
	public void copyModule(String site, String module) throws Exception{
		
		int currentLevel = getCurrentModuleLevel(site, null, module);
		if (currentLevel==MODULE_UNDEFINED)
			throw new Exception("Module " + module + " undefined ");
			
		if (currentLevel==MODULE_LEVEL_SITE)
			throw new Exception("Module " + module + " already existis in site " + site);

		CPMModuleConfig moduleConfig = getModule(site,null,module);		
		CPMModuleConfig copyModule = moduleConfig.copyOfMe();
		
		CPMSiteConfig siteConfig = sites.get(site);
		if (siteConfig==null) {
			siteConfig = new CPMSiteConfig();
			siteConfig.setName(site);
			sites.put(site, siteConfig);
		}
		
			
		siteConfig.addModule(copyModule);
	}

	
	/**
	 * Removes a module configuration defined in general.
	 * @param moduleName
	 */
	public void removeModule(String moduleName)
	{
		modules.remove(moduleName);
	}

	/**
	 * Removes a module configuration defined in a site.
	 * @param site
	 * @param module
	 * @throws Exception
	 */
	public void removeModule(String module, String site) throws Exception{
		CPMSiteConfig siteConfig = sites.get(site);
		if (siteConfig!=null)
				siteConfig.removeModule(module);
		else
			throw new Exception("Invalid site " + site);

	}

	/**
	 * Removes a module configuration defined in a publication.
	 * @param site
	 * @param publication
	 * @param module
	 * @throws Exception
	 */
	public void removeModule(String module, String site, String publication) throws Exception{
		CPMSiteConfig siteConfig = sites.get(site);
		if (siteConfig!=null)
		{
			CPMPublicationConfig publicationConfig = siteConfig.getPublicationConfig(publication);
			if (publicationConfig!=null)
				publicationConfig.removeModule(module);
			else
				throw new Exception("Invalid publication " + publication);
		}
		else
			throw new Exception("Invalid site " + publication);

	}
	
	/**
	 * Remove a module from all the places
	 * @param moduleName
	 */
	public void removeFullModule(String moduleName) {
		removeModule(moduleName);
		for (CPMSiteConfig site :getSites()) {
			site.removeModule(moduleName);
			for (CPMPublicationConfig publication : site.getPublications()) {
				publication.removeModule(moduleName);
			}
		}
	}
	
	public boolean isModuleDefinedAtLevel(String site, String publication, String module, int level) {
		if (level==MODULE_LEVEL_GENERAL) {
			CPMModuleConfig moduleConfig = modules.get(module);
			return (moduleConfig!=null); 
		}
		
		CPMSiteConfig siteConfig = sites.get(site);
		if (level==MODULE_LEVEL_SITE) {
			if (siteConfig == null) return false;
			CPMModuleConfig moduleConfig = siteConfig.getModuleConfig(module);
			return (moduleConfig!=null);
		}

		CPMPublicationConfig publicationConfig = siteConfig.getPublicationConfig(publication);
		if (level==MODULE_LEVEL_PUBLICATION) {
			if (publicationConfig == null) return false;
			CPMModuleConfig moduleConfig = publicationConfig.getModule(module);
			return (moduleConfig!=null);			
		}
		return false;	
	}
	
	/**
	 * Return the level in witch a module configuration is defined based on the parameters.
	 * @param site
	 * @param publication
	 * @param module
	 */
	public int getCurrentModuleLevel(String site, String publication, String module) {
		CPMSiteConfig siteConfig = sites.get(site);
		if (siteConfig!=null)
		{
			CPMPublicationConfig publicationConfig = siteConfig.getPublicationConfig(publication);
			if (publicationConfig!=null)
			{
				CPMModuleConfig moduleConfig = publicationConfig.getModule(module);
				if (moduleConfig!=null) 
					return MODULE_LEVEL_PUBLICATION;
			}
			
			CPMModuleConfig moduleConfig = siteConfig.getModuleConfig(module);
			if (moduleConfig!=null) 
				return MODULE_LEVEL_SITE;
		}
		CPMModuleConfig moduleConfig = modules.get(module);
		if (moduleConfig!=null) 
			return MODULE_LEVEL_GENERAL;
		
		return MODULE_UNDEFINED;
	}
	
	/**
	 * Get the module configuration given the parameters
	 * @param site
	 * @param publication
	 * @param module
	 * @return CPMModuleConfig
	 */
	public CPMModuleConfig getModule(String site, String publication, String module) {
		CPMSiteConfig siteConfig = sites.get(site);
		if (siteConfig!=null)
		{
			CPMPublicationConfig publicationConfig = siteConfig.getPublicationConfig(publication);
			if (publicationConfig!=null)
			{
				CPMModuleConfig moduleConfig = publicationConfig.getModule(module);
				if (moduleConfig!=null) 
					return moduleConfig;
			}
			
			CPMModuleConfig moduleConfig = siteConfig.getModuleConfig(module);
			if (moduleConfig!=null) 
				return moduleConfig;
		}
		CPMModuleConfig moduleConfig = modules.get(module);
		if (moduleConfig!=null) 
			return moduleConfig;
		
		return null;
	}
	
	/**
	 * Set/change the parameters value from a module configuration
	 * @param site
	 * @param publication
	 * @param module
	 * @param param
	 * @param value
	 * @throws Exception
	 */
	public void setParam(String site, String publication, String module, String param, String value) throws Exception{
		CPMSiteConfig siteConfig = sites.get(site);
		if (siteConfig!=null)
		{
			CPMPublicationConfig publicationConfig = siteConfig.getPublicationConfig(publication);
			if (publicationConfig!=null)
			{
				CPMModuleConfig moduleConfig = publicationConfig.getModule(module);
				if (moduleConfig!=null) {
					moduleConfig.addParam(param,value);
					return;
				}
			}
			
			CPMModuleConfig moduleConfig = siteConfig.getModuleConfig(module);
			if (moduleConfig!=null) {
				moduleConfig.addParam(param,value);
				return;
			}
		}
		CPMModuleConfig moduleConfig = modules.get(module);
		if (moduleConfig!=null) { 
			moduleConfig.addParam(param,value);
			return;
		}
		else
			throw new Exception("unkown module " +  module);
		
	}


	/**
	 * Set/change the parameters value from group inside a module configuration
	 * @param site
	 * @param publication
	 * @param module
	 * @param group
	 * @param param
	 * @param value
	 * @throws Exception
	 */
	public void setGroupParam(String site, String publication, String module, String group, String param, String value) throws Exception{
		CPMSiteConfig siteConfig = sites.get(site);
		if (siteConfig!=null)
		{
			CPMPublicationConfig publicationConfig = siteConfig.getPublicationConfig(publication);
			if (publicationConfig!=null)
			{
				CPMModuleConfig moduleConfig = publicationConfig.getModule(module);
				if (moduleConfig!=null) {
					moduleConfig.setParamItemGroup(group, param, value);
					return;
				}
			}
			
			CPMModuleConfig moduleConfig = siteConfig.getModuleConfig(module);
			if (moduleConfig!=null) {
				moduleConfig.setParamItemGroup(group, param, value);
				return;
			}
		}
		CPMModuleConfig moduleConfig = modules.get(module);
		if (moduleConfig!=null) {
			moduleConfig.setParamItemGroup(group, param, value);
			return;
		}
		else
			throw new Exception("unkown module " +  module);
		
	}

	public String getParam(String site, String publication, String module, String param){
		CPMSiteConfig siteConfig = sites.get(site);
		if (siteConfig!=null)
		{
			CPMPublicationConfig publicationConfig = siteConfig.getPublicationConfig(publication);
			if (publicationConfig!=null)
			{
				CPMModuleConfig moduleConfig = publicationConfig.getModule(module);
				if (moduleConfig!=null) 
					return moduleConfig.getParam(param);
			}
			
			CPMModuleConfig moduleConfig = siteConfig.getModuleConfig(module);
			if (moduleConfig!=null) 
				return moduleConfig.getParam(param);
			
		}
		CPMModuleConfig moduleConfig = modules.get(module);
		if (moduleConfig!=null) 
			return moduleConfig.getParam(param);
		
		return "";
	}
	
	
	public boolean getBooleanParam(String site, String publication, String module, String param) {
		return Boolean.parseBoolean(getParam(site, publication, module, param));
	}
	
	public int getIntegerParam(String site, String publication, String module, String param) {
		return Integer.parseInt(getParam(site, publication, module, param));
	}

	public boolean getBooleanParam(String site, String publication, String module, String param, boolean defaultValue) {
		String value = getParam(site,publication,module,param);
		return (value != null ? Boolean.parseBoolean(value) : defaultValue);
	}
	
	public int getIntegerParam(String site, String publication, String module, String param, int defaultValue) {
		String value = getParam(site,publication,module,param);
		return (value != null ? Integer.parseInt(value) : defaultValue);
	}

	public String getParam(String site, String publication, String module, String param, String defaultValue) {
		String value = getParam(site,publication,module,param);
		return (value != null ? value : defaultValue);
	}
	
	public List<String> getParamList(String site, String publication, String module, String param) {
		List<String> list = new ArrayList<String>();
		
		String value = getParam(site,publication,module,param);
		if (value != null && value.trim().length()>0) {
			value = value.trim().replaceAll(", +", ",");
			String[] values = value.split(",");
			list = Arrays.asList(values);
		}
		return list;
	}
	
	public String getItemGroupParam(String site, String publication, String module, String group, String param)
	{
		CPMSiteConfig siteConfig = sites.get(site);
		if (siteConfig!=null)
		{
			CPMPublicationConfig publicationConfig = siteConfig.getPublicationConfig(publication);
			if (publicationConfig!=null)
			{
				CPMModuleConfig moduleConfig = publicationConfig.getModule(module);
				if (moduleConfig!=null) 
					return moduleConfig.getParamItemGroup(group, param);
			}
			
			CPMModuleConfig moduleConfig = siteConfig.getModuleConfig(module);
			if (moduleConfig!=null) 
				return moduleConfig.getParamItemGroup(group, param);
			
		}
		CPMModuleConfig moduleConfig = modules.get(module);
		if (moduleConfig!=null) 
			return moduleConfig.getParamItemGroup(group, param);
		
		return "";
		
	}

	public boolean getBooleanItempGroupParam(String site, String publication, String module, String group, String param) {
		return Boolean.getBoolean(getItemGroupParam(site, publication, module, group, param));
	}
	
	public int getIntegerItempGroupParam(String site, String publication, String module, String group, String param) {
		return Integer.getInteger(getItemGroupParam(site, publication, module, group, param));
	}

	public boolean getBooleanItempGroupParam(String site, String publication, String module, String group, String param, boolean defaultValue) {
		String value = getItemGroupParam(site,publication,module,group,param);
		return (value != null ? Boolean.parseBoolean(value) : defaultValue);
	}
	
	public int getIntegerItempGroupParam(String site, String publication, String module, String group, String param, int defaultValue) {
		String value = getItemGroupParam(site,publication,module,group,param);
		return (value != null ? Integer.parseInt(value) : defaultValue);
	}

	public List<String> getListItempGroupParam(String site, String publication, String module, String group, String param) {
		List<String> list = new ArrayList<String>();
		
		String value = getItemGroupParam(site,publication,module,group,param);
		if (value != null) {
			String[] values = value.split(",");
			list = Arrays.asList(values);
		}
		return list;
	}
	
	public String getItemGroupParam(String site, String publication, String module, String group, String param, String defaultValue) {
		LinkedHashMap<String,String> values = getGroupParam(site,publication,module,group);
		if (values==null)
			return defaultValue;
		String value = values.get(param);
		return (value != null ? value : defaultValue);
	}

	public CPMSiteConfig getSiteConfiguration(String site) {
		return sites.get(site);
	}
	
	public CPMPublicationConfig getPublicationConfiguration(String site, String publication) {
		return sites.get(site).getPublicationConfig(publication);
	}
	
	public CPMModuleConfig getModule(String name)
	{
		return modules.get(name);
	}

	public LinkedHashMap<String,String> getGroupParam(String site, String publication, String module, String group)
	{
		CPMSiteConfig siteConfig = sites.get(site);
		if (siteConfig!=null)
		{
			CPMPublicationConfig publicationConfig = siteConfig.getPublicationConfig(publication);
			if (publicationConfig!=null)
			{
				CPMModuleConfig moduleConfig = publicationConfig.getModule(module);
				if (moduleConfig!=null) 
					return moduleConfig.getParamGroup(group);
			}
			
			CPMModuleConfig moduleConfig = siteConfig.getModuleConfig(module);
			if (moduleConfig!=null) 
				return moduleConfig.getParamGroup(group);
			
		}
		CPMModuleConfig moduleConfig = modules.get(module);
		if (moduleConfig!=null) 
			return moduleConfig.getParamGroup(group);
		
		return null;
		
	}

}
