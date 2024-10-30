package com.tfsla.diario.newsletters.common;

import java.util.Date;

public class NewsletterEvent {
	public String getFromEmail() {
		return fromEmail;
	}
	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}
	public String getToEmail() {
		return toEmail;
	}
	public void setToEmail(String toEmail) {
		this.toEmail = toEmail;
	}
	public NewsletterEventType getEventType() {
		return eventType;
	}
	public void setEventType(NewsletterEventType eventType) {
		this.eventType = eventType;
	}
	public String getEventData() {
		return eventData;
	}
	public void setEventData(String eventData) {
		this.eventData = eventData;
	}
	public String getElement() {
		return element;
	}
	public void setElement(String element) {
		this.element = element;
	}
	public int getNewsletterID() {
		return newsletterID;
	}
	public void setNewsletterID(int newsletterID) {
		this.newsletterID = newsletterID;
	}
	public Date getEventDate() {
		return eventDate;
	}
	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}
	String fromEmail;
	String toEmail;
	NewsletterEventType eventType;
	String eventData;
	String element;
	int newsletterID;
	Date eventDate;
}
