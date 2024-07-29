package org.opencms.configuration;

import java.util.LinkedHashMap;
import java.util.Map;

import org.opencms.main.CmsLog;

public class CPMSiteConfig {
	private String name;
	private Map<String,CPMModuleConfig> modules;
	private Map<String,CPMPublicationConfig> publications;
	
	public CPMSiteConfig()
	{
		modules = new LinkedHashMap<String,CPMModuleConfig>();
		publications = new LinkedHashMap<String,CPMPublicationConfig>();
	}

	public CPMPublicationConfig[] getPublications() {
		return ((LinkedHashMap<String,CPMPublicationConfig>) publications).values().toArray(new CPMPublicationConfig[]{});
	}
	
	public CPMModuleConfig[] getModules() {
		return ((LinkedHashMap<String,CPMModuleConfig>) modules).values().toArray(new CPMModuleConfig[]{});
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void addPublication(CPMPublicationConfig publication)
	{
		if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(MessagesCmsMedios.get().getBundle().key(MessagesCmsMedios.INIT_CMSMEDIOS_PUBLICATION_ADD_0, name, publication.getName()));
        }

		publications.put(publication.getName(), publication);
	}
	
	public void addModule(CPMModuleConfig module)
	{
		if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(MessagesCmsMedios.get().getBundle().key(MessagesCmsMedios.INIT_CMSMEDIOS_SITEMODULE_ADD_0, name, module.getName()));
        }
		modules.put(module.getName(), module);
	}
	
	public void removeModule(String moduleName)
	{
		modules.remove(moduleName);
	}
	
	public CPMPublicationConfig getPublicationConfig(String name)
	{
		return publications.get(name);
	}
	
	public CPMModuleConfig getModuleConfig(String name)
	{
		return modules.get(name);
	}
}
