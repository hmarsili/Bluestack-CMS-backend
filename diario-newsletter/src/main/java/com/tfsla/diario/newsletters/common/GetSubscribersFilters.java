package com.tfsla.diario.newsletters.common;

public class GetSubscribersFilters {
	public GetSubscribersFilters() {
		newsletterID = 0;
		pageNumber = 0;
		pageSize = 0;
		orderBy = "";
		searchFilter = "";
		status = -1;
	}
	public String getOrderBy() {
		return orderBy;
	}
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}
	public int getNewsletterID() {
		return newsletterID;
	}
	public void setNewsletterID(int newsletterID) {
		this.newsletterID = newsletterID;
	}
	public String getSearchFilter() {
		return searchFilter;
	}
	public void setSearchFilter(String searchFilter) {
		this.searchFilter = searchFilter;
	}
	public int getPageNumber() {
		return pageNumber;
	}
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	int newsletterID;
	String searchFilter;
	String orderBy;
	int pageNumber;
	int pageSize;
	int status;
}