package org.opencms.configuration;

import java.util.LinkedHashMap;
import java.util.Map;

import org.opencms.main.CmsLog;

public class CPMPublicationConfig {

	private Map<String,CPMModuleConfig> modules;

	private String name;
	
	public CPMPublicationConfig() {
		modules = new LinkedHashMap<String,CPMModuleConfig>();
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
	
	public void addModule(CPMModuleConfig module) {
		if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(MessagesCmsMedios.get().getBundle().key(MessagesCmsMedios.INIT_CMSMEDIOS_PUBLICATIONMODULE_ADD_0, name, module.getName()));
        }
		modules.put(module.getName(), module);
	}
	
	public void removeModule(String moduleName)
	{
		modules.remove(moduleName);
	}
	
	public CPMModuleConfig getModule(String name)
	{
		return modules.get(name);
	}
}
