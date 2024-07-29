/* * Copyright (C) Jerry Huxtable 1998 */package com.alkacon.simapi.filter;import com.alkacon.simapi.filter.math.*;import java.awt.image.*;public class WaterFilter extends WholeImageFilter {	static final long serialVersionUID = 8789236343162990941L;		private float wavelength = 16;	private float amplitude = 10;	private float phase = 0;	private boolean antialias = true;	public WaterFilter() {	}	public void setWavelength(float wavelength) {		this.wavelength = wavelength;	}	public float getWavelength() {		return wavelength;	}	public void setAmplitude(float amplitude) {		this.amplitude = amplitude;	}	public float getAmplitude() {		return amplitude;	}	public void setPhase(float phase) {		this.phase = phase;	}	public float getPhase() {		return phase;	}	public void setAntialias(boolean antialias) {		this.antialias = antialias;	}	public boolean getAntialias() {		return antialias;	}	private boolean inside(int v, int a, int b) {		return a <= v && v <= b;	}		public void imageComplete(int status) {		if (status == IMAGEERROR || status == IMAGEABORTED) {			consumer.imageComplete(status);			return;		}		int width = originalSpace.width;		int height = originalSpace.height;		int index = 0;		int centreX, centreY;		centreX = width/2;		centreY = height/2;			int[] outPixels = new int[width * height];		int[] a = new int[4];				for (int y = 0; y < height; y++) {			for (int x = 0; x < width; x++) {				int rgb;				float dx = x-centreX;				float dy = y-centreY;				float distance = (float)Math.sqrt(dx*dx+dy*dy);				float amount = amplitude * (float)Math.sin(distance / wavelength * ImageMath.TWO_PI + phase);				float tx = centreX + dx + amount;				float ty = centreY + dy + amount;				if (true) {					tx = ImageMath.clamp(tx, 0, width-1);					ty = ImageMath.clamp(ty, 0, height-1);				}				if (antialias) {					int nx = (int)tx;					int ny = (int)ty;					boolean xl = inside(nx, 0, width-1);					boolean yt = inside(ny, 0, height-1);					boolean xr = inside(nx, 0, width-2);					boolean yb = inside(ny, 0, height-2);					int i = ny*width+nx;					if (xl && yt)						a[0] = inPixels[i];					else						a[0] = 0xff000000;					if (xr && yt)						a[1] = inPixels[i+1];					else						a[1] = 0xff000000;					if (xl && yb)						a[2] = inPixels[i+width];					else						a[2] = 0xff000000;					if (xr && yb)						a[3] = inPixels[i+width+1];					else						a[3] = 0xff000000;					tx = ImageMath.mod(tx, 1.0f);					ty = ImageMath.mod(ty, 1.0f);					rgb = ImageMath.bilinearInterpolate(tx, ty, a);				} else {					int nx = ImageMath.clamp((int)(tx+0.5f), 0, width-1);					int ny = ImageMath.clamp((int)(ty+0.5f), 0, height-1);					rgb = inPixels[ny*width+nx];				}				outPixels[index++] = rgb;			}		}		consumer.setPixels(0, 0, width, height, defaultRGBModel, outPixels, 0, width);		consumer.imageComplete(status);		inPixels = null;	}	public String toString() {		return "Distort/Water Ripples...";	}	}