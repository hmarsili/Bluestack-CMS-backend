package com.tfsla.diario.ediciones.model;

import java.util.HashMap;
import java.util.Map;

public class TipoPublicacion {
	private static Map<Integer, TipoPublicacion> tipoPublicaciones = new HashMap<Integer, TipoPublicacion>();	
	public static TipoPublicacion ONLINE_ROOT = new TipoPublicacion("Online Predeterminada", 1); 
	public static TipoPublicacion ONLINE = new TipoPublicacion("Online", 2);
	public static TipoPublicacion EDICION_IMPRESA = new TipoPublicacion("Edición Impresa", 3);

	private int code;
	private String description;
	
	private TipoPublicacion(String description, int code) {
		this.code = code;
		this.description = description;
		
		tipoPublicaciones.put(this.code, this);
	}
	
	public int getCode() {
		return this.code;
	}
	 
	public String getDescription() {
		return this.description;
	}

	public static TipoPublicacion getTipoPublicacionByCode(String code){
		return tipoPublicaciones.get(Integer.parseInt(code));
	}
	
	public static Map<Integer, TipoPublicacion> getTipoPublicaciones() {
		return tipoPublicaciones;
	}
}
