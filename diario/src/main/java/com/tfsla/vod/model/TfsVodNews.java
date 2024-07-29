package com.tfsla.vod.model;

import java.sql.Timestamp;

public class TfsVodNews {

	
	private String descripcion;
	private String source;
	private Timestamp fecha;
	private Timestamp fechaPublicacion;
	private String sourceParent;
	private Timestamp disponibility;
	
	
	
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public Timestamp getFecha() {
		return fecha;
	}
	public void setFecha(Timestamp fecha) {
		this.fecha = fecha;
	}
	public Timestamp getFechaPublicacion() {
		return fechaPublicacion;
	}
	public void setFechaPublicacion(Timestamp fechaPublicacion) {
		this.fechaPublicacion = fechaPublicacion;
	}
	public String getSourceParent() {
		return sourceParent;
	}
	public void setSourceParent(String sourceParent) {
		this.sourceParent = sourceParent;
	}
	public Timestamp getDisponibility() {
		return disponibility;
	}
	public void setDisponibility(Timestamp disponibility) {
		this.disponibility = disponibility;
	}
	
	
	
	


}
