package com.tfsla.diario.newsletters.common;

import java.util.Date;
import java.util.List;

import com.tfsla.diario.newsletters.common.NewsletterConfiguration;
import com.tfsla.diario.newsletters.common.NewsletterStatistics;

public interface INewsletterStatisticsService {
	long getPerformanceFromDate(Date date) throws Exception;
	
	long getPerformanceFromDate(Date date, int newsletterID) throws Exception;
	
	long getSubscriptionsUpTo(Date date, int newsletterID) throws Exception;
	
	NewsletterSubscriptionStatistics getAudienceFromDate(Date date, int newsletterID) throws Exception;
	
	List<NewsletterStatistics> getAmazonStatistics(NewsletterConfiguration config);

	List<NewsletterStatistics> getAmazonStatistics(NewsletterConfiguration config, Boolean groupByDay) throws Exception;
	
	NewsletterStatistics getAmazonLastSendStatistics(NewsletterConfiguration config);
}