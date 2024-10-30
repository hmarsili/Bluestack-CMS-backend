package com.tfsla.diario.newsletters.common;

import java.util.Date;

import software.amazon.awssdk.services.ses.model.SendDataPoint;

public class NewsletterStatistics implements Comparable<NewsletterStatistics> {
	public NewsletterStatistics() { }
	
	public NewsletterStatistics(SendDataPoint point) {
		
		this.setTimestamp(new Date(point.timestamp().toEpochMilli()));
		this.setBounces(point.bounces());
		this.setComplaints(point.complaints());
		this.setDeliveryAttempts(point.deliveryAttempts());
		this.setRejects(point.rejects());
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date currentDate) {
		this.timestamp = currentDate;
	}
	public Long getBounces() {
		return bounces;
	}
	public void setBounces(Long bounces) {
		this.bounces = bounces;
	}
	public Long getComplaints() {
		return complaints;
	}
	public void setComplaints(Long complaints) {
		this.complaints = complaints;
	}
	public Long getDeliveryAttempts() {
		return deliveryAttempts;
	}
	public void setDeliveryAttempts(Long deliveryAttempts) {
		this.deliveryAttempts = deliveryAttempts;
	}
	public Long getRejects() {
		return rejects;
	}
	public void setRejects(Long rejects) {
		this.rejects = rejects;
	}
	public Long getOpen() {
		return open;
	}
	public void setOpen(Long open) {
		this.open = open;
	}
	Date timestamp;
	Long bounces = (long) 0;
	Long complaints = (long) 0;
	Long deliveryAttempts = (long) 0;
	Long rejects = (long) 0;
	Long open = (long) 0;
	
	@Override
	public int compareTo(NewsletterStatistics o) {
		return getTimestamp().compareTo(o.getTimestamp());
	}
}
