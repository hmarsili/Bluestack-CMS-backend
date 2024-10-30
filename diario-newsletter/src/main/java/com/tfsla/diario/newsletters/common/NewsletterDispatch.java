package com.tfsla.diario.newsletters.common;

import java.util.Date;

public class NewsletterDispatch {
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public int getSent() {
		return sent;
	}
	public void setSent(int sent) {
		this.sent = sent;
	}
	public int getOpened() {
		return opened;
	}
	public void setOpened(int opened) {
		this.opened = opened;
	}
	public int getRejected() {
		return rejected;
	}
	public void setRejected(int rejected) {
		this.rejected = rejected;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Newsletter getNewsletter() {
		return newsletter;
	}
	public void setNewsletter(Newsletter newsletter) {
		this.newsletter = newsletter;
	}
	int ID;
	int sent;
	int opened;
	int rejected;
	Date date;
	Newsletter newsletter;
}
