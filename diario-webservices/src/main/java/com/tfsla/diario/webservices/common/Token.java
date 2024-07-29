package com.tfsla.diario.webservices.common;

public class Token {
	private String value;
	private long duration;
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getIntDuration() {
		return (int)duration;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
}
