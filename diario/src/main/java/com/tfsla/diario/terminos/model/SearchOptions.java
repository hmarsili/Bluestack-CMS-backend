package com.tfsla.diario.terminos.model;

public class SearchOptions {
	
	private String orderBy;
	private String text;
	private String from;
	private String to;
	private Long id;
	Long type;
	int status;
	private int count;
	private boolean showSynonymous;
	
	public SearchOptions() {
		this.status = -1;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public Long getType() {
		return type;
	}

	public void setType(Long type) {
		this.type = type;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getOrderBy() {
		return orderBy == null || orderBy.equals("") ? " NAME asc " : orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	
	
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean getShowSynonymous() {
		return showSynonymous;
	}

	public void setShowSynonymous(boolean showSynonymous) {
		this.showSynonymous = showSynonymous;
	}

}
