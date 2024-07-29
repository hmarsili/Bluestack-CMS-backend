package com.tfsla.diario.model;

public class TfsListaVideos {

	public TfsListaVideos(int size, int position, int pageSize, int page) {
		this.size = size;
		this.position = position;
		this.pageSize = pageSize;
		this.page = page;
	}
	
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
