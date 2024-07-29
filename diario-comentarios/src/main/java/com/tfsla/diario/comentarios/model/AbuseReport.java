package com.tfsla.diario.comentarios.model;

import java.util.Date;

public class AbuseReport {
	
	private int id;
	private Date fecha;
	private String path;
	private String motivo;
	private String usuario;
	private String descripcion;
	private String sitio;
	private int commentId;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public int getCommentId() {
		return commentId;
	}

	public void setCommentId(int commentId) {
		this.commentId = commentId;
	}
	
	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}
	
	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	
	public String getDescription() {
		return descripcion;
	}

	public void setDescription(String descripcion) {
		this.descripcion = descripcion;
	}
	
	public String getSitio() {
		return sitio;
	}

	public void setSitio(String sitio) {
		this.sitio = sitio;
	}

}
