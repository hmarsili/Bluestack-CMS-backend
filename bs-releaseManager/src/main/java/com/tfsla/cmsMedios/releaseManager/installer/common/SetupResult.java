package com.tfsla.cmsMedios.releaseManager.installer.common;

public enum SetupResult {
	OK(1),
	CANCELED(2),
	WARNING(3),
	ERROR(4);

    private SetupResult(int value) {
        this.value = value;
    }
    
    private int value;
    
    public int getValue() {
    	return this.value;
    }
}