package com.tfsla.diario.webservices.common;

import java.util.Calendar;
import java.util.Date;

import com.tfsla.diario.webservices.common.strings.TimeUnits;

public class TopicConfiguration {
	public Date getNextExecutionForDate(Date date) {
		return this.getNextExecutionForDate(date, false);
	}
	
	public Date getNextExecutionForDate(Date date, Boolean skipReschedule) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		//Check if we are in the running window
		if(skipReschedule || !this.isInIdleTimeWindow(calendar)) {
			if(this.getUnit() == Calendar.MINUTE) {
				int currentMins = calendar.get(Calendar.MINUTE);
				int runMins = (int)(this.getInterval()*(Math.ceil(Math.abs(currentMins/this.getInterval()))));
				if(runMins >= 60) {
					calendar.set(Calendar.MINUTE, 0);
					calendar.add(Calendar.HOUR_OF_DAY, 1);
				} else {
					calendar.set(Calendar.MINUTE, runMins + this.getInterval());
				}
			} else {
				calendar.add(this.getUnit(), this.getInterval());
			}
			
			this.isInIdleTimeWindow(calendar);
		}
		
		return calendar.getTime();
	}
	
	/**
	 * Checks if the time scheduled is into the idle window and changes
	 * the schedule to the next execution if it needs to be re-scheduled
	 * @param calendar Calendar instance with the time scheduled
	 * @return true if the time was re-scheduled, false otherwise
	 */
	public Boolean isInIdleTimeWindow(Calendar calendar) {
		int scheduledTime = calendar.get(Calendar.HOUR_OF_DAY) * 100 + calendar.get(Calendar.MINUTE);
		int fromTime = this.getFromHour() * 100 + this.getFromMinutes();
		int toTime = this.getToHour() * 100 + this.getToMinutes();
		if(scheduledTime <= fromTime || scheduledTime >= toTime) {
			calendar.set(Calendar.HOUR_OF_DAY, this.getFromHour());
			calendar.set(Calendar.MINUTE, this.getFromMinutes());
			if(scheduledTime >= toTime)
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			return true;
		}
		return false;
	}
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getFromHour() {
		return fromHour;
	}
	public void setFromHour(int fromHour) {
		this.fromHour = fromHour;
	}
	public int getToMinutes() {
		return toMinutes;
	}
	public void setToMinutes(int toTime) {
		this.toMinutes = toTime;
	}
	public int getInterval() {
		return interval;
	}
	public void setInterval(int interval) {
		this.interval = interval;
	}
	public String getIntervalUnit() {
		return intervalUnit;
	}
	public void setIntervalUnit(String intervalUnit) {
		this.intervalUnit = intervalUnit;
	}
	public int getToHour() {
		return toHour;
	}
	public void setToHour(int toHour) {
		this.toHour = toHour;
	}
	public int getFromMinutes() {
		return fromMinutes;
	}
	public void setFromMinutes(int fromMinutes) {
		this.fromMinutes = fromMinutes;
	}

	private int getUnit() {
		if(this.getIntervalUnit() == null) return Calendar.MINUTE;
		if(this.getIntervalUnit().toLowerCase().equals(TimeUnits.MINUTES)) {
			return Calendar.MINUTE;
		}
		if(this.getIntervalUnit().toLowerCase().equals(TimeUnits.HOURS)) {
			return Calendar.HOUR_OF_DAY;
		}
		if(this.getIntervalUnit().toLowerCase().equals(TimeUnits.DAYS)) {
			return Calendar.DAY_OF_MONTH;
		}
		return Calendar.MINUTE;
	}
	public Boolean getIsJobScheduled() {
		return isJobScheduled;
	}

	public void setIsJobScheduled(Boolean isJobScheduled) {
		this.isJobScheduled = isJobScheduled;
	}
	
	public void setPushUrlParams(String urlParams) {
		this.urlParams = urlParams;
		
	}

	public String getPushUrlParams() {
		return urlParams;
	}
	
	public void setSite(String site) {
		this.site = site;
	}
	
	public String getSite() {
		return this.site;
	}
	public void setPublication(int publication) {
		this.publication = publication;
	}
	public int getPublication() {
		return publication;
	}
	
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}


	private String name;
	private String description;
	private String site;
	private int publication;
	private String urlParams;
	private int fromHour;
	private int fromMinutes;
	private int toHour;
	private int toMinutes;
	private int interval;
	private String intervalUnit;
	private Boolean isJobScheduled;
	private int priority;
	
	
	

}
