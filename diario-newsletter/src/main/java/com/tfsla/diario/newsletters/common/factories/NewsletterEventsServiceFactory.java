package com.tfsla.diario.newsletters.common.factories;

import com.tfsla.diario.newsletters.common.INewsletterEventsService;
import com.tfsla.diario.newsletters.service.NewsletterEventsService;

public class NewsletterEventsServiceFactory implements IAbstractFactory<INewsletterEventsService> {

	@Override
	public INewsletterEventsService getInstance() {
		return new NewsletterEventsService();
	}

}
