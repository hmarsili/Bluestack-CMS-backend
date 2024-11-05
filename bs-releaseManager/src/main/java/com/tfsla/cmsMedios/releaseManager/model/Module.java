package com.tfsla.cmsMedios.releaseManager.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Module {
	private boolean optional;
	private String name;
	private String description;
	private List<String> levels = new ArrayList<String>();
	
	private List<ModuleParameters> parameters = new ArrayList<ModuleParameters>();
	private List<ModuleParamGroup> paramGroups = new ArrayList<ModuleParamGroup>();
	
	private Map<String,ModuleParameters> parametersMap = new HashMap<String,ModuleParameters>();
	private Map<String,ModuleParamGroup> paramGroupsMap = new HashMap<String,ModuleParamGroup>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<String> getLevels() {
		return levels;
	}
	public void setLevels(List<String> levels) {
		this.levels = levels;
	}
	public void addLevel(String level) {
		this.levels.add(level);
	}
	
	public List<ModuleParameters> getParameters() {
		return parameters;
	}
	public ModuleParameters getParameter(String name) {
		return this.parametersMap.get(name);
	}
	public void addParameter(ModuleParameters parameter) {
		this.parameters.add(parameter);
		this.parametersMap.put(parameter.getName(), parameter);
	}
	public boolean isOptional() {
		return optional;
	}
	public void setOptional(boolean optional) {
		this.optional = optional;
	}
	public void setOptional(String optional) {
		this.optional = Boolean.parseBoolean(optional);
	}
	public List<ModuleParamGroup> getParamGroups() {
		return paramGroups;
	}
	public ModuleParamGroup getParamGroup(String name) {
		return this.paramGroupsMap.get(name);
	}
	public void addParamGroup(ModuleParamGroup paramGroup) {
		this.paramGroups.add(paramGroup);
		this.paramGroupsMap.put(paramGroup.getName(), paramGroup);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Module other = (Module) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
