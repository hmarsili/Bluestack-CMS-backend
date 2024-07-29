package com.tfsla.diario.model;

import com.tfsla.diario.ediciones.model.Seccion;

public class TfsSeccion {

	private int id;
	private String name;
	private String description;
	private String page;
	
	TfsSeccion(Seccion section)
	{
		id = section.getIdSection();
		name = section.getName();
		description = section.getDescription();
		page = section.getPage();
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
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
	public String getPage() {
		return page;
	}
	public void setPage(String page) {
		this.page = page;
	}

}
