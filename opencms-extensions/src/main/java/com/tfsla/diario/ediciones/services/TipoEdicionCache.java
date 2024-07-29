package com.tfsla.diario.ediciones.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.model.TipoPublicacion;

public class TipoEdicionCache {
	
	protected static final Log LOG = CmsLog.getLog(TipoEdicionCache.class);

	private static TipoEdicionCache instance = new TipoEdicionCache();
	public static TipoEdicionCache getInstance() { return instance; };
	
	public class TE_CacheItem {
		Object value = null;
		long timestamp = 0;
		long timeToLive = 1800000;
		
		public TE_CacheItem(Object value,long timeToLive) {
			this(value);
			this.timeToLive = timeToLive;
		}
		
		public TE_CacheItem(Object value) {
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

	private TipoEdicionCache(){}
	
	private Map<Integer,TE_CacheItem> cacheTipoEdiciones = new HashMap<Integer,TE_CacheItem>();
	private Map<String,TE_CacheItem> cacheTipoEdicionesAlias = new HashMap<String,TE_CacheItem>();
	private Map<String,TE_CacheItem> cacheTipoEdicionesPaths = new LRUMap(3000);
	
	private Map<String,Long> cacheFolderPaths = new LRUMap(1000);
	
	public void reset() {
		cacheTipoEdiciones = new HashMap<Integer,TE_CacheItem>();
		cacheTipoEdicionesAlias = new HashMap<String,TE_CacheItem>();
		cacheTipoEdicionesPaths = new LRUMap(2000);
		
		cacheFolderPaths = new LRUMap(1000);
		
		LOG.debug("Cache de tipo de ediciones reseteado.");
	} 
	
	public void putTipoEdicion(TipoEdicion tipoEdicion) {
		putTipoEdicion(tipoEdicion,null);
	}
	
	public void putTipoEdicion(TipoEdicion tipoEdicion, String path) {
		TE_CacheItem item = new TE_CacheItem(tipoEdicion);
		
		if (TipoPublicacion.getTipoPublicacionByCode(tipoEdicion.getTipoPublicacion()).equals(TipoPublicacion.ONLINE_ROOT))		
			cacheTipoEdicionesAlias.put("project?" + tipoEdicion.getProyecto(), new TE_CacheItem(tipoEdicion.getId()));
		cacheTipoEdicionesAlias.put("name?" + tipoEdicion.getProyecto() + "-" + tipoEdicion.getNombre(), new TE_CacheItem(tipoEdicion.getId()));
		cacheTipoEdiciones.put(tipoEdicion.getId(), item);
		if (path!=null) {
			cacheTipoEdicionesPaths.put(formatPath(path), new TE_CacheItem(tipoEdicion.getId(),900000));
		}
		LOG.debug("Agregando a cache publicacion "+ tipoEdicion.getId() + (path!=null ? ". path: " + formatPath(path) : "") + ".");
	}
	
	public TipoEdicion getTipoEdicion(int id){
		TE_CacheItem item = cacheTipoEdiciones.get(id);
		if (item!=null ) {
			TipoEdicion value = (TipoEdicion) item.getValue();
			if (item.isValid())
				return value;
			
			//si no es valido lo tengo que borrar
			if (TipoPublicacion.getTipoPublicacionByCode(value.getTipoPublicacion()).equals(TipoPublicacion.ONLINE_ROOT))
				cacheTipoEdicionesAlias.remove("project?" + value.getProyecto());
			cacheTipoEdicionesAlias.remove("name?" + value.getProyecto() + "-" + value.getNombre());
			cacheTipoEdiciones.remove(id);
		}
		LOG.debug("No se encuentra en cache la publicacion "+ id + ".");

		return null;
	}
	
	@Deprecated
	public TipoEdicion getTipoEdicion(String nombre){
		return getTipoEdicion(nombre, "");
	}
	
	public TipoEdicion getTipoEdicion(String nombre, String proyecto){
		
		
		TE_CacheItem item = cacheTipoEdicionesAlias.get("name?" + proyecto + "-" + nombre);

		

		if (item==null ) {
			LOG.debug("Buscando en cache la publicacion "+ proyecto + "-" + nombre + ": MISS");
			return null;
		}

		LOG.debug("Buscando en cache la publicacion "+ nombre + ": " + (item.isValid() ? "HIT": "ROTTEN" ) );
		int id = (Integer)item.getValue();
		return getTipoEdicion(id);
	}

	public TipoEdicion getTipoEdicionPrincipal(String proyecto) {
		TE_CacheItem item = cacheTipoEdicionesAlias.get("project?" + proyecto);


		if (item==null ) {
			LOG.debug("Buscando en cache la publicacion predeterminada del sitio "+ proyecto + ": MISS");
			return null;
		}
		
		LOG.debug("Buscando en cache la publicacion predeterminada del sitio "+ proyecto + ": " + (item.isValid() ? "HIT": "ROTTEN" ) );
		int id = (Integer)item.getValue();
		return getTipoEdicion(id);
	}
	
	public TipoEdicion getTipoEdicionByPath(String path) {
		
		path = formatPath(path);

		
		TE_CacheItem item = cacheTipoEdicionesPaths.get(path);
		
		LOG.debug("Buscando en cache la publicacion del directorio "+ path + ".");

		if (item==null )
			return null;
		
		if (!item.isValid()) {
			cacheTipoEdicionesPaths.remove(path);
			return null;
		}
		
		int id = (Integer)item.getValue();
		return getTipoEdicion(id);
	}
	
	private String formatPath(String path){
		if (!path.endsWith("/"))
			path = path.substring(0, path.lastIndexOf("/"));
		return path;
	}
	
	public void putFolderPath(String path) {
		cacheFolderPaths.put(path, new Date().getTime());
	}
	
	public boolean isFolderPath(String path) {
		Long timestamp = cacheFolderPaths.get(path);
		
		if (timestamp==null) return false;
		
		LOG.debug("El path " + path + " no es una publicacion. (ya fue verificado antes)");
		//Verifico validez (menos de 30 minutos)
		long now = new Date().getTime();
		return (now - timestamp < 1800000);
		
	}

}
