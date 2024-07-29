package com.tfsla.opencmsdev.encuestas;

public interface EncuestasSQLConstants {

	public static final String URL_ENCUESTA = "URL_ENCUESTA";
	public static final String NRO_RESPUESTA = "NRO_RESPUESTA";
	public static final String CANT_VOTOS = "CANT_VOTOS";
	public static final String GRUPO = "GRUPO";
	public static final String ESTADO_PUBLICACION = "ESTADO_PUBLICACION";
	public static final String TFS_ENCUESTA = "TFS_ENCUESTA";
	public static final String FECHA_CIERRE = "FECHA_CIERRE";
	public static final String ID_ENCUESTA = "ID_ENCUESTA";
	public static final String TFS_RESPUESTA_ENCUESTA = "TFS_RESPUESTA_ENCUESTA";
	public static final String ID_RESPUESTA = "ID_RESPUESTA";
	public static final String PUBLICACION = "PUBLICACION";
	public static final String SITIO = "SITIO";
	public static final String VOTOS_PURGADOS = "VOTOS_PURGADOS";

	/** uso este identificador para no usar null * */
	public static final String NADA = "NADA";
	
	public static final String TABLA_ENCUESTA_VOTOS = "TFS_ENCUESTAS_VOTOS";
	public static final String CANT_VOTOS_IP = "CANT";
	public static final String REMOTE_IP = "IP";
	public static final String FECHA_ULTIMA_VOTACION = "FECHA_VOTO";
	public static final String VOTO_USUARIO = "USUARIO"; 
	

	// ***************************
	// ** Tablas para el online
	// ***************************
	public static final String _ONLINE = "_ONLINE";
	public static final String TFS_ENCUESTA_ONLINE = "TFS_ENCUESTA" + _ONLINE;
	public static final String TFS_RESPUESTA_ENCUESTA_ONLINE = "TFS_RESPUESTA_ENCUESTA" + _ONLINE;
}