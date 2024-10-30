package com.tfsla.diario.newsletters.common;

import java.util.Date;
import java.util.List;

import com.tfsla.diario.newsletters.common.NewsletterEvent;

public interface INewsletterEventsService {
	
	
	void purgeStatistics(Date dateFrom, int newsletterID);
	
	void addEventSummary(NewsletterEvent newsletterEvent);
	
	void addEvent(NewsletterEvent newsletterEvent);

	List<NewsletterEventStatistics> getEventsStatistics(Date dateFrom, int newsletterID) throws Exception;
	
	public List<NewsletterStatistics> getStatistics() throws Exception;
		
	public List<NewsletterStatistics> getStatistics(Date dateFrom, int newsletterID) throws Exception;

	List<NewsletterSubscriptionStatistics> getSubscriptionStatistics() throws Exception;
	
	List<NewsletterSubscriptionStatistics> getSubscriptionStatistics(Date dateFrom, int newsletterID) throws Exception;
}