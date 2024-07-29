package com.tfsla.opencms.follow.model;

import java.util.Date;

public class UserFollow {
	private String seguido;
	private String seguidor;	
	private Date fecha;
	
	public String getSeguidor() {
		return seguidor;
	}
	public void setSeguidor(String seguidor) {
		this.seguidor = seguidor;
	}
	public String getSeguido() {
		return seguido;
	}
	public void setSeguido(String seguido) {
		this.seguido = seguido;
	}
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
}
