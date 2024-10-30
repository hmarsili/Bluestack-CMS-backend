package com.tfsla.diario.newsletters.common;

import java.util.Date;

public class NewsletterSubscriptionStatistics {
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public long getUsersCount() {
		return usersCount;
	}
	public void setUsersCount(long usersCount) {
		this.usersCount = usersCount;
	}
	public long getDifference() {
		return difference;
	}
	public void setDifference(long difference) {
		this.difference = difference;
	}
	Date date;
	long usersCount = 0;
	long difference = 0;
}