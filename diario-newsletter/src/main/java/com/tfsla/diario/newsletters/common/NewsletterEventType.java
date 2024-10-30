package com.tfsla.diario.newsletters.common;

public enum NewsletterEventType {
	UNKNOWN(0),
	SEND(1),
	DELIVERY(2),
	OPEN(3),
	CLICK(4),
	BOUNCE(5),
	REJECT(6),
	COMPLAINT(7);
	
	private NewsletterEventType(int value) {
        this.value = value;
    }
    
    private int value;
    
    public int getValue() {
    	return this.value;
    }
    
    public static NewsletterEventType getFromString(String eventType) {
    	if(eventType == null) {
    		return NewsletterEventType.UNKNOWN;
    	}
    	String eventTypeString = eventType.toLowerCase().trim();
    	if(eventTypeString.equals("bounce")) {
			return NewsletterEventType.BOUNCE;
		}
		if(eventTypeString.equals("complaint")) {
			return NewsletterEventType.COMPLAINT;
		}
		if(eventTypeString.equals("delivery")) {
			return NewsletterEventType.DELIVERY;
		}
		if(eventTypeString.equals("send")) {
			return NewsletterEventType.SEND;
		}
		if(eventTypeString.equals("reject")) {
			return NewsletterEventType.REJECT;
		}
		if(eventTypeString.equals("open")) {
			return NewsletterEventType.OPEN;
		}
		if(eventTypeString.equals("click")) {
			return NewsletterEventType.CLICK;
		}
		return NewsletterEventType.UNKNOWN;
    }
}
