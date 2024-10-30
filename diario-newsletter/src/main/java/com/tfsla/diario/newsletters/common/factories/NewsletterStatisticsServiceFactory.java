package com.tfsla.diario.newsletters.common.factories;

import com.tfsla.diario.newsletters.common.INewsletterStatisticsService;
import com.tfsla.diario.newsletters.service.NewsletterStatisticsService;

public class NewsletterStatisticsServiceFactory implements IAbstractFactory<INewsletterStatisticsService> {

	@Override
	public INewsletterStatisticsService getInstance() {
		return new NewsletterStatisticsService();
	}

}
