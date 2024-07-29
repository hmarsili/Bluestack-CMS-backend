package org.opencms.configuration;

import java.util.LinkedHashMap;
import java.util.Map;

import org.opencms.main.CmsLog;

public class CPMModuleConfig {
	private Map<String,String> params;
	
	private Map<String,LinkedHashMap<String,String>> paramsGroup;
	
	private LinkedHashMap<String,String> currentGroup = new LinkedHashMap<String,String>();
	
	private String name;
	
	public CPMModuleConfig() {
		params = new LinkedHashMap<String,String>();
		paramsGroup = new LinkedHashMap<String,LinkedHashMap<String,String>>();
	}
	
	public CPMModuleConfig copyOfMe()
	{
		CPMModuleConfig copy = new CPMModuleConfig();
		
		copy.setName(this.name);
		for (String param : getParamsNames()) {
			copy.addParam(param, new String(this.getParam(param)));
		}

		for (String group : getParamsGroupNames()) {
			copy.creataParamGroup(group);
			for (String param : this.getParamsNameFromGroup(group))
				try {
					copy.setParamItemGroup(group,param,new String(this.getParamItemGroup(group, param)));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

		return copy;
	}
	public String[] getParamsNames() {
		return params.keySet().toArray(new String[]{});
	}
	
	public String[] getParamsGroupNames() {
		return paramsGroup.keySet().toArray(new String[]{});
	}
	
	public String[] getParamsNameFromGroup(String name) {
		return paramsGroup.get(name).keySet().toArray(new String[]{});
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getParam(String paramName) {
		return params.get(paramName);
	}

	public void addParam(String paranName, String value) {
		
		if (CmsLog.INIT.isInfoEnabled()) {
			    CmsLog.INIT.debug(MessagesCmsMedios.get().getBundle().key(MessagesCmsMedios.INIT_CMSMEDIOS_PARAM_ADD_0, paranName, value));
        }
		params.put(paranName, value);
	}

	public void creataParamGroup(String name) {
		
		if (CmsLog.INIT.isInfoEnabled()) {
		    CmsLog.INIT.debug(MessagesCmsMedios.get().getBundle().key(MessagesCmsMedios.INIT_CMSMEDIOS_PARAMGROUP_ADD_0, name));
    }

		paramsGroup.put(name, currentGroup);
		
		currentGroup = new LinkedHashMap<String, String>();

	}
	
	public void addParamItemToGroup(String paranName, String value) {
		
		if (CmsLog.INIT.isInfoEnabled()) {
			    CmsLog.INIT.debug(MessagesCmsMedios.get().getBundle().key(MessagesCmsMedios.INIT_CMSMEDIOS_PARAMGROUPITEM_ADD_0, paranName, value));
        }
		
		currentGroup.put(paranName, value);
	}
	
	public LinkedHashMap<String,String> getParamGroup(String groupName) {
		return paramsGroup.get(groupName);
	}

	public String getParamItemGroup(String groupName, String itemName) {
		LinkedHashMap<String, String> group = paramsGroup.get(groupName);
		if (group == null)
			return null;
		return group.get(itemName);
	}

	public void setParamItemGroup(String groupName, String itemName, String value) throws Exception {
		LinkedHashMap<String, String> group = paramsGroup.get(groupName);
		if (group == null)
			throw new Exception("Invalid group name " + groupName + " in module " + name);
		group.put(itemName,value);
	}
	
	public void setOrCreateParamItemGroup(String groupName, String itemName, String value) {
		LinkedHashMap<String, String> group = paramsGroup.get(groupName);
		if (group == null) {
			group = new LinkedHashMap<String, String>();
			paramsGroup.put(groupName,group);
		}
		group.put(itemName,value);
		
	}

}
