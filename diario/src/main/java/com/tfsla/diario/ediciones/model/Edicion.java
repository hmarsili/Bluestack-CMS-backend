package com.tfsla.diario.ediciones.model;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.tfsla.diario.ediciones.exceptions.UndefinedTipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;

/**
 * Clase que contiene la informacion de una edicion.
 * @author Victor Podberezski
 *
 */
public class Edicion {
	protected int numero;
	protected Date fecha;
	protected String fechaEdicion;
	protected Date publicacion;
	protected String fechaPublicacion;
	protected int tipo;
	protected String tituloTapa;
	protected String portada;
	protected String logo;

	protected boolean nuevaEdicion=false;
	protected boolean autoNumerico=false;

	protected TipoEdicion tipoEdicion = null;

	/**
	 * Contiene la URL de la edicion.
	 * @return URL de la edicion.
	 * @throws UndefinedTipoEdicion
	 */
	public String getbaseURL() throws UndefinedTipoEdicion {
		String url;
		if (tipoEdicion == null)
			 establecerTipoEdicion();
			//throw new UndefinedTipoEdicion("Debe cargar el tipo de edicion a la que pertenece la edicion");

		Calendar cal = new GregorianCalendar();
		cal.setTime(this.getFecha());
		url = tipoEdicion.baseURL +
				cal.get(Calendar.YEAR) + "/" +
				(cal.get(Calendar.MONTH) + 1) + "/" +
				"edicion_" + numero + "/";
		return url;
	}


	/**
	 * Retorna la publicacion de la edicion.
	 * @return TipoEdicion
	 */
	public TipoEdicion getTipoEdicion() {
		return tipoEdicion;
	}

	/**
	 * Establece la publicacion de la edicion.<br>
	 * Requerido para obtener el path de la edicion.
	 * @param tipoEdicion
	 */
	public void setTipoEdicion(TipoEdicion tipoEdicion) {
		this.tipoEdicion = tipoEdicion;
	}

	private void establecerTipoEdicion() throws UndefinedTipoEdicion
	 {
	  TipoEdicionService tService = new TipoEdicionService();
	  setTipoEdicion(tService.obtenerTipoEdicion(getTipo()));

	  if (this.tipoEdicion == null)
	    throw new UndefinedTipoEdicion("La edicion no pertence a una publicacion valida");
	 }
	
	public int getNumero() {
		return numero;
	}

	public void setNumero(int numero) {
		this.numero = numero;
	}

	public int getTipo() {
		return tipo;
	}

	public void setTipo(int tipo) {
		this.tipo = tipo;
	}

	public Date getFecha() {
		return fecha;
	}


	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}


	public String getFechaEdicion() {
		return fechaEdicion;
	}


	public void setFechaEdicion(String fechaEdicion) {
		this.fechaEdicion = fechaEdicion;
	}


	public boolean isNuevaEdicion() {
		return nuevaEdicion;
	}


	public void setNuevaEdicion(boolean nuevaEdicion) {
		this.nuevaEdicion = nuevaEdicion;
	}


	public String getLogo() {
		return logo;
	}


	public void setLogo(String logo) {
		this.logo = logo;
	}


	public String getPortada() {
		return portada;
	}


	public void setPortada(String portada) {
		this.portada = portada;
	}


	public String getTituloTapa() {
		return tituloTapa;
	}

	public void setTituloTapa(String tituloTapa) {
		this.tituloTapa = tituloTapa;
	}

	public Date getPublicacion() {
		return publicacion;
	}

	public void setPublicacion(Date fechaPublicacion) {
		this.publicacion = fechaPublicacion;
	}


	public String getFechaPublicacion() {
		return fechaPublicacion;
	}


	public void setFechaPublicacion(String strFechaPublicacion) {
		this.fechaPublicacion = strFechaPublicacion;
	}


	public boolean isAutoNumerico() {
		return autoNumerico;
	}


	public void setAutoNumerico(boolean autoNumerico) {
		this.autoNumerico = autoNumerico;
	}


}
