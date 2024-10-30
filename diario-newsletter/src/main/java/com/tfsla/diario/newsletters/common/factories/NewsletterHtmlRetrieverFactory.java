package com.tfsla.diario.newsletters.common.factories;

import com.tfsla.diario.newsletters.common.INewsletterHtmlRetriever;
import com.tfsla.diario.newsletters.service.NewsletterHtmlRetriever;

public class NewsletterHtmlRetrieverFactory implements IAbstractFactory<INewsletterHtmlRetriever> {

	@Override
	public INewsletterHtmlRetriever getInstance() {
		return new NewsletterHtmlRetriever();
	}

}
