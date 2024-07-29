package com.tfsla.diario.ediciones.model;

public class Categoria {

	private String descripcion="";
	private String nombre="";
	private String padre="";
	private String descripcionlarga="";
	private String orden ="";
	private String tags="";
	
	private boolean nuevaCategoria;
	
	public boolean isNuevaCategoria() {
		return nuevaCategoria;
	}
	public void setNuevaCategoria(boolean nuevaCategoria) {
		this.nuevaCategoria = nuevaCategoria;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getPadre() {
		return padre;
	}
	public void setPadre(String padre) {
		this.padre = padre;
	}
	public String getDescripcionlarga() {
		return descripcionlarga;
	}
	public void setDescripcionlarga(String descripcionlarga) {
		this.descripcionlarga = descripcionlarga;
	}
	public String getOrden() {
		return orden;
	}
	public void setOrden(String orden) {
		this.orden = orden;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	
}
