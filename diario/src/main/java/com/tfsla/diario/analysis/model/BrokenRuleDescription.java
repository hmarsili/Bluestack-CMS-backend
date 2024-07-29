package com.tfsla.diario.analysis.model;

public class BrokenRuleDescription {
	
	public static final int BR_SUGGESTION=0;
	public static final int BR_WARNING=1;
	public static final int BR_DANGER=2;
	
	private String message;
	private int type;
	private String title;
	
	public BrokenRuleDescription(String message, int type) {
		super();
		this.message = message;
		this.type = type;
		this.title = "";
	}
	
	public BrokenRuleDescription(String message, int type, String title) {
		super();
		this.message = message;
		this.type = type;
		this.title = title;
	}
	
	public String getMessage() {
		return message;
	}
	public int getType() {
		return type;
	}
	
	public String getTitle(){
		return title;
	}
	
}
