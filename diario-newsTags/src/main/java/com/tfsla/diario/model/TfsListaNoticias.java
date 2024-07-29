package com.tfsla.diario.model;


public class TfsListaNoticias {

	public TfsListaNoticias(int size, int position, int pageSize, int page) {
		this.size = size;
		this.position = position;
		this.pageSize = pageSize;
		this.page = page;
	}
	
	public TfsListaNoticias() {
		size=0;
		position=0;
		page=1;
		pageSize=0;
		
		sectionchanged=true;
		priorityhomechanged = true;
		prioritysectionchanged = true;

		currentprioritysection=0;
		currentpriorityhome=0;
		currentsection="";
		
	}

	/* properties */
	
	private int size=0;
	private int position=0;
	private int page=1;
	private int pageSize=0;
	
	private boolean sectionchanged=true;
	private boolean priorityhomechanged = true;
	private boolean prioritysectionchanged = true;

	private int currentprioritysection;
	private int currentpriorityhome;
	private String currentsection;
		
	
	public int getCurrentPriorityHome() {
		return currentpriorityhome;
	}

	public void setCurrentPriorityHome(int currentpriorityhome) {
		this.currentpriorityhome = currentpriorityhome;
	}

	public int getCurrentprioritysection() {
		return currentprioritysection;
	}

	public void setCurrentprioritysection(int currentprioritysection) {
		this.currentprioritysection = currentprioritysection;
	}

	public String getCurrentsection() {
		return currentsection;
	}

	public void setCurrentsection(String currentsection) {
		this.currentsection = currentsection;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public boolean isPriorityhomechanged() {
		return priorityhomechanged;
	}

	public void setPriorityhomechanged(boolean priorityhomechanged) {
		this.priorityhomechanged = priorityhomechanged;
	}

	public boolean isPrioritysectionchanged() {
		return prioritysectionchanged;
	}

	public void setPrioritysectionchanged(boolean prioritysectionchanged) {
		this.prioritysectionchanged = prioritysectionchanged;
	}

	public boolean isSectionchanged() {
		return sectionchanged;
	}

	public void setSectionchanged(boolean sectionchanged) {
		this.sectionchanged = sectionchanged;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}


	public int getAbsoluteposition() {
		return (this.page-1)*this.pageSize+position;
	}
}
