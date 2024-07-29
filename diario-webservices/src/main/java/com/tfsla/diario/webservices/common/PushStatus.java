package com.tfsla.diario.webservices.common;

public enum PushStatus {
	PENDING(0),
	PUSHED(1),
	ERROR(2),
	WARNING(3);
	
	private PushStatus(int value) {
        this.value = value;
    }
    
    private int value;
    
    public int getValue() {
    	return this.value;
    }
}
