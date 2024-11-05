package com.tfsla.cmsMedios.releaseManager.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleParamGroup {
	private String name;
	private String description;
	private ModuleParameters paramList = null;
	
	private List<ModuleParameters> parameters = new ArrayList<ModuleParameters>();
	private Map<String,ModuleParameters> parametersMap = new HashMap<String,ModuleParameters>();
	
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
	public ModuleParameters getParamList() {
		return paramList;
	}
	public void setParamList(ModuleParameters paramList) {
		this.paramList = paramList;
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
}
