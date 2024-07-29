package com.tfsla.diario.toolbar;

public abstract class AbstractButton {

	protected String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public abstract  String  getValues();
	
}
