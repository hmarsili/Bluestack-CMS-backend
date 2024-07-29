package com.tfsla.diario.model;


public class TfsListaPersonas {

	public TfsListaPersonas(int size, int position, int pageSize, int page) {
		this.size = size;
		this.position = position;
		this.pageSize = pageSize;
		this.page = page;
	}
	
	public TfsListaPersonas() {
		size=0;
		position=0;
		page=1;
		pageSize=0;
	}

	/* properties */
	
	private int size=0;
	private int position=0;
	private int page=1;
	private int pageSize=0;
	
		
	

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
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
