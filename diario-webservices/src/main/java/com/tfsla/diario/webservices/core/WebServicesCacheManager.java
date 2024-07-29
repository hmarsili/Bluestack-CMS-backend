package com.tfsla.diario.webservices.core;

import java.io.InputStream;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;

/**
 * Manages caching into the web services jar
 */
final class WebServicesCacheManager {
	
	/**
	 * Retrieves a cache to be used into the jar
	 * @return Cache instance
	 */
	public synchronized static Cache getCache() {
		CacheManager manager = getCacheManager();
		Cache cache = manager.getCache(CACHE_NAME);
		if(cache == null) {
			CacheConfiguration cacheConfiguration = new CacheConfiguration() {{
				setName(CACHE_NAME);
				setEternal(false);
			}};
			cache = new Cache(cacheConfiguration);
			manager.addCache(cache);
		}
		return cache;
	}
	
	private synchronized static CacheManager getCacheManager() {
		if(cacheManager == null) {
			InputStream is = SessionManager.class
	            .getClassLoader()
	            .getResourceAsStream("com/tfsla/diario/webservices/core/cache.xml");
						
			cacheManager = new CacheManager(is);
		}
		return cacheManager;
	}
	
	private static CacheManager cacheManager = null;
	private static final String CACHE_NAME = "WEB_SERVICES_SESSIONS_CACHE";
}
