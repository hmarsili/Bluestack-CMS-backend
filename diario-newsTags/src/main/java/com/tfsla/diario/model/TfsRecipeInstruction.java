package com.tfsla.diario.model;

public class TfsRecipeInstruction {

	private int size=0;
	private int position=0;
	
		
	public TfsRecipeInstruction(int size, int position) {
		this.size = size;
		this.position = position;
		
	}
	
	public TfsRecipeInstruction() {
		size=0;
		position=0;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	
}

