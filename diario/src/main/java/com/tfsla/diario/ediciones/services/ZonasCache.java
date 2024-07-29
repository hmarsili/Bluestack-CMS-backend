package com.tfsla.diario.ediciones.services;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;
import com.tfsla.diario.ediciones.model.Zona;

public class ZonasCache {
	
	protected static final Log LOG = CmsLog.getLog(ZonasCache.class);

	private static ZonasCache instance = new ZonasCache();
	public static ZonasCache getInstance() { return instance; };
	
	public class Zon_CacheItem {
		Object value = null;
		long timestamp = 0;
		long timeToLive = 1800000;
		
		public Zon_CacheItem(Object value,long timeToLive) {
			this(value);
			this.timeToLive = timeToLive;
		}
		
		public Zon_CacheItem(Object value) {
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

	private ZonasCache(){}
	
	private Map<Integer,Zon_CacheItem> cachezonas = new HashMap<Integer,Zon_CacheItem>();
	
	public void reset() {
		cachezonas = new HashMap<Integer,Zon_CacheItem>();
		
		LOG.debug("Cache de zonas reseteado.");
	} 
	
	public void putZona(Zona zona) {
		Zon_CacheItem item = new Zon_CacheItem(zona);

		cachezonas.put(zona.getIdPage(), item);
			
		LOG.debug("Agregando a cache zona "+ zona.getName() + " de publicacion " + zona.getIdTipoEdicion() + "(" + zona.getIdZone() + ").");
	}
	
	public Zona getZona(int id){
		Zon_CacheItem item = cachezonas.get(id);
		if (item!=null ) {
			Zona value = (Zona) item.getValue();
			if (item.isValid())
				return value;

			cachezonas.remove(id);
		}
		LOG.debug("No se encuentra en cache la zona "+ id + ".");

		return null;
	}
	
}
