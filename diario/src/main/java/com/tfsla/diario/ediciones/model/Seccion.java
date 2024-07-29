package com.tfsla.diario.ediciones.model;

public class Seccion {

	private int idSection=-1;
	private String name;
	private String description;
	private String page;
	private int idTipoEdicion;
	private int order;
	private boolean visibility=true;
	
	public boolean getVisibility(){
		return visibility;
	}
	
	public void setVisibility(boolean visibility){
		this.visibility = visibility;
	}

	public int getOrder() {
		return order;
	}


	public void setOrder(int order) {
		this.order = order;
	}


	public boolean isNew()
	{
		return (getIdSection() == -1);
	}


	public void setIdSection(int idSection) {
		this.idSection = idSection;
	}


	public int getIdSection() {
		return idSection;
	}


	public void setName(String sectionName) {
		this.name = sectionName;
	}


	public String getName() {
		return name;
	}


	public void setDescription(String sectionDescription) {
		this.description = sectionDescription;
	}


	public String getDescription() {
		return description;
	}


	public void setPage(String sectionPage) {
		this.page = sectionPage;
	}


	public String getPage() {
		return page;
	}


	public void setIdTipoEdicion(int idTipoEdicion) {
		this.idTipoEdicion = idTipoEdicion;
	}


	public int getIdTipoEdicion() {
		return idTipoEdicion;
	}

}
