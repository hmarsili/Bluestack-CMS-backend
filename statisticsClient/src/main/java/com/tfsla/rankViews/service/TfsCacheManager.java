package com.tfsla.rankViews.service;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.rankViews.model.TfsRankResults;
import com.tfsla.statistics.model.TfsStatisticsOptions;

public class TfsCacheManager {
	
	static Log LOG = CmsLog.getLog(TfsCacheManager.class);

	private static TfsCacheManager instance = new TfsCacheManager();
	
	private CacheManager manager = null;
	private String[] cacheNames;
	
	private TfsCacheManager() {

		LOG.info("Page Statistics Cache : Leyendo archivo de configuracion");

		InputStream is = TfsCacheManager.class
            .getClassLoader()
            .getResourceAsStream("com/tfsla/rankViews/service/cache.xml");
					
		manager = new CacheManager(is);
		
		cacheNames = manager.getCacheNames();
	}
	
	public boolean cacheExists(String name)
	{
		for (int j=0;j<cacheNames.length;j++)
			if (cacheNames[j].equals(name))
				return true;

		return false;
	}

	
	public Cache getCache(String name)
	{
		return manager.getCache(name);
	}

	public Object getObjectFromCache(String cache, TfsStatisticsOptions options)
	{

		//Fuerzo el borrado de los evicted.
		//manager.getCache(cache).evictExpiredElements();
		
		Element element = manager.getCache(cache).get(options);
		if (element !=null)
			return element.getObjectValue();
		
		return null;
	}

	public void putObjectToCache(String cache, TfsStatisticsOptions options, TfsRankResults stats)
	{
		Element element = new Element(options,stats);		
		manager.getCache(cache).put(element);
	}

	public void removeObjectFromCache(String cacheName, String cacheKey) {
		manager.getCache(cacheName).remove(cacheKey);
		
	}


	public void terminate()
	{
		LOG.info("Page Statistics Cache : Cerrando cache");
		
		manager.shutdown();
	}

	public static TfsCacheManager getInstance() {
		return instance;
	}

}
