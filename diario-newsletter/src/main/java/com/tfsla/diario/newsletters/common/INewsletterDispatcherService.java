package com.tfsla.diario.newsletters.common;

import java.util.List;

import org.opencms.file.CmsObject;

import software.amazon.awssdk.services.ses.SesClient;
import com.tfsla.diario.newsletters.service.NewsletterUnsubscribeTokenManager;

public interface INewsletterDispatcherService {	
	void addDispatch(NewsletterDispatch dispatch);
	List<NewsletterDispatch> getDispatches(int newsletterID) throws Exception;
	int dispatchNewsletter(int newsletterID, NewsletterConfiguration config, CmsObject cmsObject) throws Throwable;
	void singleDispatch(String email, Newsletter newsletter, NewsletterConfiguration config) throws Throwable;
	void doDispatch(String email, Newsletter newsletter, NewsletterUnsubscribeTokenManager tokenManager, String htmlContent, SesClient client);
}
