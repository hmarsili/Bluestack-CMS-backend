package com.tfsla.diario.newsletters.common;

public class NewsletterConfiguration {
	String amzAccessID;
	String amzAccessKey;
	String amzRegion;
	String amzSESConfigSet;
	int batchSize;
	int numOfMessagesShuttles=1;
	
	public int getNumOfMessagesShuttles() {
		return numOfMessagesShuttles;
	}
	public void setNumOfMessagesShuttles(int numOfMessagesShuttles) {
		this.numOfMessagesShuttles = numOfMessagesShuttles;
	}
	public int getBatchSize() {
		return batchSize;
	}
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	public String getAmzAccessID() {
		return amzAccessID;
	}
	public void setAmzAccessID(String amzAccessID) {
		this.amzAccessID = amzAccessID;
	}
	public String getAmzAccessKey() {
		return amzAccessKey;
	}
	public void setAmzAccessKey(String amzAccessKey) {
		this.amzAccessKey = amzAccessKey;
	}
	public String getAmzRegion() {
		return amzRegion;
	}
	public void setAmzRegion(String amzRegion) {
		this.amzRegion = amzRegion;
	}
	public String getAmzSESConfigSet() {
		return amzSESConfigSet;
	}
	public void setAmzSESConfigSet(String amzSESConfigSet) {
		this.amzSESConfigSet = amzSESConfigSet;
	}
}
