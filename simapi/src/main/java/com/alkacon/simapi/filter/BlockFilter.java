/* * Copyright (C) Jerry Huxtable 1998 */package com.alkacon.simapi.filter;import java.awt.*;import java.awt.image.*;/** * A Filter to pixellate images. */public class BlockFilter extends TransformFilter {	static final long serialVersionUID = 8077109551486196569L;		private int blockSize = 2;	/**	 * Set the pixel block size	 * @param blockSize the number of pixels along each block edge	 */	public void setBlockSize(int blockSize) {		this.blockSize = blockSize;	}	/**	 * Get the pixel block size	 * @return the number of pixels along each block edge	 */	public int getBlockSize() {		return blockSize;	}	public BlockFilter() {	}	protected void transform(int x, int y, Point out) {		out.x = (x / blockSize) * blockSize;		out.y = (y / blockSize) * blockSize;	}	protected void transformInverse(int x, int y, float[] out) {		out[0] = (x / blockSize) * blockSize;		out[1] = (y / blockSize) * blockSize;	}	public String toString() {		return "Stylize/Mosaic...";	}}