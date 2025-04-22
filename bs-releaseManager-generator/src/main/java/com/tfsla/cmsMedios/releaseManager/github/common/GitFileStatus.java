package com.tfsla.cmsMedios.releaseManager.github.common;

/**
 * Represents the status of a file/asset in Github 
 */
public enum GitFileStatus {
	/**
	 * The asset has been deleted
	 */
	DELETED(0),
	/**
	 * The asset has been added
	 */
	ADDED(1),
	/**
	 * The asset has been modified
	 */
	MODIFIED(2),
	/**
	 * The asset has been copied
	 */
	COPIED(3),
	/**
	 * The asset has been renamed
	 */
	RENAMED(4),
	/**
	 * The asset is unmodified
	 */
	UNMODIFIED(5),
	/**
	 * The asset has been updated but unmerged
	 */
	UPDATED_UNMERGED(5),
	/**
	 * Undefined status
	 */
	UNDEFINED(6);
	
	private GitFileStatus(int value) {
        this.value = value;
	}
	
	private int value;
    
    public int getValue() {
    	return this.value;
    }
    
    /**
     * Retrieves a GitFileStatus instance by having the status string
     * @param status Github status in a string
     * @return GitFileStatus
     */
    public static GitFileStatus getByString(String status) {
    	if(status == null) return GitFileStatus.UNDEFINED;
    	switch(status.toLowerCase()) {
    	case "deleted" : return GitFileStatus.DELETED;
    		
    	case "added" : return GitFileStatus.ADDED;
    	
    	case "modified" : return GitFileStatus.MODIFIED;
    	
    	case "copied" : return GitFileStatus.COPIED;
    	
    	case "renamed" : return GitFileStatus.RENAMED;
    	
    	case "" : return GitFileStatus.UNMODIFIED;
    	
    	default: return GitFileStatus.UNDEFINED;
    	}
    }
}
