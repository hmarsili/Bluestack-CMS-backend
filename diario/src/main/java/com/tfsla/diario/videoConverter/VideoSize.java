package com.tfsla.diario.videoConverter;

import java.io.Serializable;

/**
 * Instances of this class report informations about videos size (based on FFMPEG)
 * 
 */
public class VideoSize implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The video width.
	 */
	private int width;

	/**
	 * The video height.
	 */
	private int height;

	/**
	 * It builds the bean.
	 * 
	 * @param width
	 *            The video width.
	 * @param height
	 *            The video height.
	 */
	public VideoSize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * Returns the video width.
	 * 
	 * @return The video width.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Returns the video height.
	 * 
	 * @return The video height.
	 */
	public int getHeight() {
		return height;
	}

	public String toString() {
		return getClass().getName() + " (width=" + width + ", height=" + height
				+ ")";
	}

}
