package com.tfsla.diario.ediciones.model;

/**
 * Contains image information such as width, height and orientation 
 */
public class ImageInformation {
	public final int orientation;
	public final int width;
	public final int height;

	public ImageInformation(int orientation, int width, int height) {
	    this.orientation = orientation;
	    this.width = width;
	    this.height = height;
	}
}
