package com.tfsla.diario.newsletters.common;

public enum NewsletterSubscriptionStatus {
	INACTIVE(0),
	ACTIVE(1),
	INVALID(2),
	PENDING(3);
	
	private NewsletterSubscriptionStatus(int value) {
        this.value = value;
    }
    
    private int value;
    
    public int getValue() {
    	return this.value;
    }
}
