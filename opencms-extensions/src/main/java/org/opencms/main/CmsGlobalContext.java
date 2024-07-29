package org.opencms.main;

import java.util.Map;

import com.tfsla.collections.CollectionFactory;

public class CmsGlobalContext {

	private static CmsGlobalContext instance;

	private Map<String, Object> variables = CollectionFactory.createMap();

	public static synchronized CmsGlobalContext getInstance() {
		if (instance == null) {
			instance = new CmsGlobalContext();
		}

		return instance;
	}

	private CmsGlobalContext() {
	}

	public void setVariable(String variableName, Object value) {
		this.variables.put(variableName, value);
	}

	public Boolean getBooleanVariable(String variableName) {
		return (Boolean) this.variables.get(variableName);
	}
}