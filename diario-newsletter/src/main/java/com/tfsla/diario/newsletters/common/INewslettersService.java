package com.tfsla.diario.newsletters.common;

import java.util.List;

public interface INewslettersService {

	List<Newsletter> getNewsletters() throws Exception;

	Newsletter getNewsletter(int newsletterID) throws Exception;
	
	int getNewsletterSubscriptors(int newsletterID) throws Exception;
	
	int getNewsletterDispatchesCount(int newsletterID) throws Exception;

	List<NewsletterSubscription> getNewsletterSubscriptions(int newsletterID) throws Exception;
	
	List<NewsletterSubscription> getSubscribers(GetSubscribersFilters filters) throws Exception;

	List<String> getNewsletterSubscriptionsEmails(int newsletterID) throws Exception;
	
	List<String> getNewsletterSubscriptionsEmails(int newsletterID, int startFrom, int batchSize) throws Exception;

	int subscribeToNewsletter(String email, int newsletterID) throws Exception;

	void unsubscribeEmail(String email) throws Exception;
	
	void unsubscribeFromNewsletter(String email, int newsletterID) throws Exception;
	
	void unsubscribeFromNewsletter(String token) throws Exception;

	NewsletterSubscription getNewsletterSubscription(String email, int newsletterID) throws Exception;
	
	ComplaintType getComplaintType(int complaintTypeID) throws Exception;
	
	List<ComplaintType> getComplaintTypes() throws Exception;
	
	List<ComplaintType> getComplaintTypes(Boolean onlyComplaints) throws Exception;
	
	void addComplaint(String token, int complaintID, String message) throws Exception;
	
	void updateSubject(int newsletterID, String subject) throws Exception;

}