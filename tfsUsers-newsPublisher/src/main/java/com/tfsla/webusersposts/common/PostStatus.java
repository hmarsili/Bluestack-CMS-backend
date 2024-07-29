package com.tfsla.webusersposts.common;

/**
 * Represents different statuses during a user posts life cycle
 */
public enum PostStatus {
	DRAFT(0),
	PENDING(1),
	PUBLISHED(2),
	MODERATED(3),
	IMPORTED(4),
	ERROR(5),
	PENDING_USER(6);

    private PostStatus(int value) {
        this.value = value;
    }
    
    private int value;
    
    public int getValue() {
    	return this.value;
    }
}
