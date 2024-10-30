package com.tfsla.diario.newsletters.common;

public class NewsletterUnsubscribeRequest {
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public int getNewsletterID() {
		return newsletterID;
	}
	public void setNewsletterID(int newsletterID) {
		this.newsletterID = newsletterID;
	}
	String email;
	int newsletterID;
}
