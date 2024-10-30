package com.tfsla.diario.newsletters.common.factories;

import com.tfsla.diario.newsletters.common.INewslettersService;
import com.tfsla.diario.newsletters.service.NewslettersService;

public class NewslettersServiceFactory implements IAbstractFactory<INewslettersService> {

	@Override
	public INewslettersService getInstance() {
		return new NewslettersService();
	}

}
