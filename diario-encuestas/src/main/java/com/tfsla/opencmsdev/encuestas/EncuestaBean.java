package com.tfsla.opencmsdev.encuestas;

/**
 * Reprsenta una encuesta traida de la tabla del modulo (a diferencia de la clase Encuesta que representa el
 * xmlcontent de opencms).
 * 
 * TODO: no se cuanto durara aca esta clase, se podria hacer un manejo mas limpio probablemente.
 * 
 * @author jpicasso
 */
public class EncuestaBean {

	private String url;
	private String estado;
	private String fechaCierre;
	private String fechaCreacion;

	public EncuestaBean(String url, String estado) {
		this.url = url;
		this.estado = estado;
	}

	public EncuestaBean(String url, String estado, String fechaCierre) {
		this(url, estado);
		this.fechaCierre = fechaCierre;
	}
	
	public EncuestaBean(String url, String estado, String fechaCierre, String fechaCreacion) {
		this(url, estado);
		this.fechaCierre = fechaCierre;
		this.fechaCreacion = fechaCreacion;
	}

	public String getEstado() {
		return this.estado;
	}

	public String getUrl() {
		return this.url;
	}
	
	public String getFechaCierre() {
		return this.fechaCierre;
	}
	
	public String getFechaCreacion() {
		return this.fechaCreacion;
	}
}