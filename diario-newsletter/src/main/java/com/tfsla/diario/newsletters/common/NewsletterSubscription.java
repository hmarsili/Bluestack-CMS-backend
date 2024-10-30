package com.tfsla.diario.newsletters.common;

import java.util.Date;

public class NewsletterSubscription {
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public Newsletter getNewsletter() {
		return newsletter;
	}
	public void setNewsletter(Newsletter newsletter) {
		this.newsletter = newsletter;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public NewsletterSubscriptionStatus getStatus() {
		return status;
	}
	public void setStatus(NewsletterSubscriptionStatus status) {
		this.status = status;
	}
	public Date getSubscribed() {
		return subscribed;
	}
	public void setSubscribed(Date subscribed) {
		this.subscribed = subscribed;
	}
	public Date getUpdated() {
		return updated;
	}
	public void setUpdated(Date updated) {
		this.updated = updated;
	}
	public ComplaintType getComplaintType() {
		return complaintType;
	}
	public void setComplaintType(ComplaintType complaintType) {
		this.complaintType = complaintType;
	}
	int ID;
	Newsletter newsletter;
	String email;
	NewsletterSubscriptionStatus status;
	Date subscribed;
	Date updated;
	ComplaintType complaintType;
}
