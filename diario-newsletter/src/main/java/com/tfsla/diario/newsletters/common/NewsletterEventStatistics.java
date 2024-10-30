package com.tfsla.diario.newsletters.common;

import java.util.Date;

public class NewsletterEventStatistics {
	public NewsletterEventType getEventType() {
		return eventType;
	}
	public void setEventType(NewsletterEventType eventType) {
		this.eventType = eventType;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public long getEventsCount() {
		return eventsCount;
	}
	public void setEventsCount(long eventsCount) {
		this.eventsCount = eventsCount;
	}
	NewsletterEventType eventType;
	Date date;
	long eventsCount;
}
