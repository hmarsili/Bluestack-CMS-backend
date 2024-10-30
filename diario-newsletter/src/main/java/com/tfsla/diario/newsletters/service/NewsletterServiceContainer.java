package com.tfsla.diario.newsletters.service;

import java.util.Hashtable;

import com.tfsla.diario.newsletters.common.INewsletterDispatcherService;
import com.tfsla.diario.newsletters.common.INewsletterEventsService;
import com.tfsla.diario.newsletters.common.INewsletterHtmlRetriever;
import com.tfsla.diario.newsletters.common.INewsletterStatisticsService;
import com.tfsla.diario.newsletters.common.INewslettersService;
import com.tfsla.diario.newsletters.common.factories.IAbstractFactory;
import com.tfsla.diario.newsletters.common.factories.NewsletterDispatcherServiceFactory;
import com.tfsla.diario.newsletters.common.factories.NewsletterEventsServiceFactory;
import com.tfsla.diario.newsletters.common.factories.NewsletterHtmlRetrieverFactory;
import com.tfsla.diario.newsletters.common.factories.NewsletterStatisticsServiceFactory;
import com.tfsla.diario.newsletters.common.factories.NewslettersServiceFactory;

@SuppressWarnings("rawtypes")
public class NewsletterServiceContainer {
	
	private static Hashtable<Class, IAbstractFactory> _instances;
	
	public static synchronized void setInstances(Hashtable<Class, IAbstractFactory> instances) {
		_instances = instances;
	}
	
	private static synchronized Hashtable<Class, IAbstractFactory> getInstances() {
		if(_instances == null) {
			_instances = new Hashtable<Class, IAbstractFactory>();
			_instances.put(INewsletterDispatcherService.class, new NewsletterDispatcherServiceFactory());
			_instances.put(INewslettersService.class, new NewslettersServiceFactory());
			_instances.put(INewsletterStatisticsService.class, new NewsletterStatisticsServiceFactory());
			_instances.put(INewsletterHtmlRetriever.class, new NewsletterHtmlRetrieverFactory());
			_instances.put(INewsletterEventsService.class, new NewsletterEventsServiceFactory());
		}
		return _instances;
	}
	
	@SuppressWarnings("unchecked")
	public static synchronized <T> T getInstance(Class<T> type) throws Exception {
		try {
			IAbstractFactory factory = getInstances().get(type);
			return (T) factory.getInstance();
		} catch(Exception e) {
			throw e;
		}
	}
}
