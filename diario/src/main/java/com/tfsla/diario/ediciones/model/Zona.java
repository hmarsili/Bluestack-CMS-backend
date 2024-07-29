package com.tfsla.diario.ediciones.model;

/**
 * Clase que representa una zona de una edicion online o publicacion
 * @author Victor Podberezski
 *
 */
public class Zona {

	private int idZone=-1;
	private String name;
	private String description;
	private String color="000000";
	private int order;
	private int idPage;
	private int idTipoEdicion;
	private String orderDefault="modification-date";
	private int sizeDefault=10;
	private boolean visibility;

	public void setIdZone(int idZone) {
		this.idZone = idZone;
	}
	public int getIdZone() {
		return idZone;
	}

	public void setName(String zoneName) {
		this.name = zoneName;
	}
	public String getName() {
		return name;
	}

	public void setDescription(String zoneDescription) {
		this.description = zoneDescription;
	}
	public String getDescription() {
		return description;
	}

	public void setColor(String color) {
		this.color = color;
	}
	public String getColor() {
		return color;
	}

	public void setIdPage(int idPage) {
		this.idPage = idPage;
	}
	public int getIdPage() {
		return idPage;
	}

	public void setOrder(int zoneOrder) {
		this.order = zoneOrder;
	}
	public int getOrder() {
		return order;
	}

	public boolean isNew()
	{
		return (idZone == -1);
	}

	public int getIdTipoEdicion() {
		return idTipoEdicion;
	}
	public void setIdTipoEdicion(int idTipoEdicion) {
		this.idTipoEdicion = idTipoEdicion;
	}

	public String getOrderDefault(){
		return orderDefault;
	}
	
	public void setOrderDefault( String orderDefault){
		this.orderDefault = orderDefault;
	}
	
	public int getSizeDefault(){
		return sizeDefault;
	}
	
	public void setSizeDefault(int sizeDefault){
		this.sizeDefault = sizeDefault;
	}

	public boolean getVisibility(){
		return visibility;
	}
	
	public void setVisibility(boolean visibility){
		this.visibility = visibility;
	}

}