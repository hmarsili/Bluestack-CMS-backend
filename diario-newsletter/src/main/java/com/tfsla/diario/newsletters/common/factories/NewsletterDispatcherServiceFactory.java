package com.tfsla.diario.newsletters.common.factories;

import com.tfsla.diario.newsletters.common.INewsletterDispatcherService;
import com.tfsla.diario.newsletters.service.NewsletterDispatcherService;

public class NewsletterDispatcherServiceFactory implements IAbstractFactory<INewsletterDispatcherService> {

	@Override
	public INewsletterDispatcherService getInstance() {
		return new NewsletterDispatcherService();
	}
	
}
