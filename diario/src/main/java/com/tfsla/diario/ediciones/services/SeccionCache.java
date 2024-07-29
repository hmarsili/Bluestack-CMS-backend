package com.tfsla.diario.ediciones.services;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.diario.ediciones.model.Seccion;

public class SeccionCache {
	
	protected static final Log LOG = CmsLog.getLog(SeccionCache.class);

	private static SeccionCache instance = new SeccionCache();
	public static SeccionCache getInstance() { return instance; };
	
	public class Sec_CacheItem {
		Object value = null;
		long timestamp = 0;
		long timeToLive = 1800000;
		
		public Sec_CacheItem(Object value,long timeToLive) {
			this(value);
			this.timeToLive = timeToLive;
		}
		
		public Sec_CacheItem(Object value) {
			this.value = value;
			timestamp = new Date().getTime();
		}
		
		public Object getValue() {
			return value;
		}
		
		public boolean isValid() {
			long now = new Date().getTime();
			return (now - timestamp < timeToLive);
		}
	}

	private SeccionCache(){}
	
	private Map<Integer,Sec_CacheItem> cacheSecciones = new HashMap<Integer,Sec_CacheItem>();
	private Map<String,Sec_CacheItem> cacheSeccionesAlias = new HashMap<String,Sec_CacheItem>();
	
	public void reset() {
		cacheSecciones = new HashMap<Integer,Sec_CacheItem>();
		cacheSeccionesAlias = new HashMap<String,Sec_CacheItem>();
		
		LOG.debug("Cache de seciones reseteado.");
	} 
	
	public void putSeccion(Seccion seccion) {
		Sec_CacheItem item = new Sec_CacheItem(seccion);

		cacheSeccionesAlias.put("publicacion?" + seccion.getIdTipoEdicion() + "&nombre?" + seccion.getName(), new Sec_CacheItem(seccion.getIdSection()));
		cacheSeccionesAlias.put("publicacion?" + seccion.getIdTipoEdicion() + "&pagina?" + seccion.getPage(), new Sec_CacheItem(seccion.getIdSection()));

		cacheSecciones.put(seccion.getIdSection(), item);
			
		LOG.debug("Agregando a cache seccion "+ seccion.getName() + " de publicacion " + seccion.getIdTipoEdicion() + "(" + seccion.getIdSection() + ").");
	}
	
	public Seccion getSeccion(int id){
		Sec_CacheItem item = cacheSecciones.get(id);
		if (item!=null ) {
			Seccion value = (Seccion) item.getValue();
			if (item.isValid())
				return value;
			//si no es valido lo tengo que borrar

			cacheSeccionesAlias.remove("publicacion?" + value.getIdTipoEdicion() + "&nombre?" + value.getName());
			cacheSeccionesAlias.remove("publicacion?" + value.getIdTipoEdicion() + "&pagina?" + value.getPage());

			cacheSecciones.remove(id);
		}
		LOG.debug("No se encuentra en cache la seccion "+ id + ".");

		return null;
	}
	
	public Seccion getSeccion(int tipoEdicion, String nombre){
		Sec_CacheItem item = cacheSeccionesAlias.get("publicacion?" + tipoEdicion + "&nombre?" + nombre);

		LOG.debug("Buscando en cache la seccion "+ nombre + " de la publicacion " + tipoEdicion + ".");

		if (item==null )
			return null;

		int id = (Integer)item.getValue();
		return getSeccion(id);
	}

	public Seccion getSeccionByPage(int tipoEdicion, String pagina) {
		Sec_CacheItem item = cacheSeccionesAlias.get("publicacion?" + tipoEdicion + "&pagina?" + pagina);

		LOG.debug("Buscando en cache la seccion con pagina "+ pagina + " de la publicacion " + tipoEdicion + "");

		if (item==null )
			return null;
		
		int id = (Integer)item.getValue();
		return getSeccion(id);
	}
	

}
