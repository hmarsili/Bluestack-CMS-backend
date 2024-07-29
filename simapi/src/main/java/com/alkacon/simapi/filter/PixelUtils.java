/* * Copyright (C) Jerry Huxtable 1998-2001. All rights reserved. */package com.alkacon.simapi.filter;import java.util.*;import java.awt.Color;/** * Some more useful math functions for image processing */public class PixelUtils {	public final static int REPLACE = 0;	public final static int NORMAL = 1;	public final static int MIN = 2;	public final static int MAX = 3;	public final static int ADD = 4;	public final static int SUBTRACT = 5;	public final static int DIFFERENCE = 6;	public final static int MULTIPLY = 7;	public final static int HUE = 8;	public final static int SATURATION = 9;	public final static int VALUE = 10;	public final static int COLOR = 11;	public final static int SCREEN = 12;	public final static int AVERAGE = 13;	public final static int OVERLAY = 14;	public final static int CLEAR = 15;	public final static int EXCHANGE = 16;	public final static int DISSOLVE = 17;	public final static int DST_IN = 18;	public final static int ALPHA = 19;	public final static int ALPHA_TO_GRAY = 20;	private static Random randomGenerator = new Random();	/**	 * Clamp a value to the range 0..255	 */	public static int clamp(int c) {		if (c < 0)			return 0;		if (c > 255)			return 255;		return c;	}	public static int interpolate(int v1, int v2, float f) {		return clamp((int)(v1+f*(v2-v1)));	}		public static int brightness(int rgb) {		int r = (rgb >> 16) & 0xff;		int g = (rgb >> 8) & 0xff;		int b = rgb & 0xff;		return (r+g+b)/3;	}		public static boolean nearColors(int rgb1, int rgb2, int tolerance) {		int r1 = (rgb1 >> 16) & 0xff;		int g1 = (rgb1 >> 8) & 0xff;		int b1 = rgb1 & 0xff;		int r2 = (rgb2 >> 16) & 0xff;		int g2 = (rgb2 >> 8) & 0xff;		int b2 = rgb2 & 0xff;		return Math.abs(r1-r2) <= tolerance && Math.abs(g1-g2) <= tolerance && Math.abs(b1-b2) <= tolerance;	}		private final static float hsb1[] = new float[3];//FIXME-not thread safe	private final static float hsb2[] = new float[3];//FIXME-not thread safe		// Return rgb1 painted onto rgb2	public static int combinePixels(int rgb1, int rgb2, int op) {		return combinePixels(rgb1, rgb2, op, 0xff);	}		public static int combinePixels(int rgb1, int rgb2, int op, int extraAlpha, int channelMask) {		return (rgb2 & ~channelMask) | combinePixels(rgb1 & channelMask, rgb2, op, extraAlpha);	}		public static int combinePixels(int rgb1, int rgb2, int op, int extraAlpha) {		if (op == REPLACE)			return rgb1;		int a1 = (rgb1 >> 24) & 0xff;		int r1 = (rgb1 >> 16) & 0xff;		int g1 = (rgb1 >> 8) & 0xff;		int b1 = rgb1 & 0xff;		int a2 = (rgb2 >> 24) & 0xff;		int r2 = (rgb2 >> 16) & 0xff;		int g2 = (rgb2 >> 8) & 0xff;		int b2 = rgb2 & 0xff;		switch (op) {		case NORMAL:			break;		case MIN:			r1 = Math.min(r1, r2);			g1 = Math.min(g1, g2);			b1 = Math.min(b1, b2);			break;		case MAX:			r1 = Math.max(r1, r2);			g1 = Math.max(g1, g2);			b1 = Math.max(b1, b2);			break;		case ADD:			r1 = clamp(r1+r2);			g1 = clamp(g1+g2);			b1 = clamp(b1+b2);			break;		case SUBTRACT:			r1 = clamp(r2-r1);			g1 = clamp(g2-g1);			b1 = clamp(b2-b1);			break;		case DIFFERENCE:			r1 = clamp(Math.abs(r1-r2));			g1 = clamp(Math.abs(g1-g2));			b1 = clamp(Math.abs(b1-b2));			break;		case MULTIPLY:			r1 = clamp(r1*r2/255);			g1 = clamp(g1*g2/255);			b1 = clamp(b1*b2/255);			break;		case DISSOLVE:			if ((randomGenerator.nextInt() & 0xff) <= a1) {				r1 = r2;				g1 = g2;				b1 = b2;			}			break;		case AVERAGE:			r1 = (r1+r2)/2;			g1 = (g1+g2)/2;			b1 = (b1+b2)/2;			break;		case HUE:		case SATURATION:		case VALUE:		case COLOR:			Color.RGBtoHSB(r1, g1, b1, hsb1);			Color.RGBtoHSB(r2, g2, b2, hsb2);			switch (op) {			case HUE:				hsb2[0] = hsb1[0];				break;			case SATURATION:				hsb2[1] = hsb1[1];				break;			case VALUE:				hsb2[2] = hsb1[2];				break;			case COLOR:				hsb2[0] = hsb1[0];				hsb2[1] = hsb1[1];				break;			}			rgb1 = Color.HSBtoRGB(hsb2[0], hsb2[1], hsb2[2]);			r1 = (rgb1 >> 16) & 0xff;			g1 = (rgb1 >> 8) & 0xff;			b1 = rgb1 & 0xff;			break;		case SCREEN:			r1 = 255 - ((255 - r1) * (255 - r2)) / 255;			g1 = 255 - ((255 - g1) * (255 - g2)) / 255;			b1 = 255 - ((255 - b1) * (255 - b2)) / 255;			break;		case OVERLAY:			int m, s;			s = 255 - ((255 - r1) * (255 - r2)) / 255;			m = r1 * r2 / 255;			r1 = (s * r1 + m * (255 - r1)) / 255;			s = 255 - ((255 - g1) * (255 - g2)) / 255;			m = g1 * g2 / 255;			g1 = (s * g1 + m * (255 - g1)) / 255;			s = 255 - ((255 - b1) * (255 - b2)) / 255;			m = b1 * b2 / 255;			b1 = (s * b1 + m * (255 - b1)) / 255;			break;		case CLEAR:			r1 = g1 = b1 = 0xff;			break;		case DST_IN:			r1 = clamp((r2*a1)/255);			g1 = clamp((g2*a1)/255);			b1 = clamp((b2*a1)/255);			a1 = clamp((a2*a1)/255);			return (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;		case ALPHA:			a1 = a1*a2/255;			return (a1 << 24) | (r2 << 16) | (g2 << 8) | b2;		case ALPHA_TO_GRAY:			int na = 255-a1;			return (a1 << 24) | (na << 16) | (na << 8) | na;		}		if (extraAlpha != 0xff || a1 != 0xff) {			a1 = a1*extraAlpha/255;			int a3 = (255-a1)*a2/255;			r1 = clamp((r1*a1+r2*a3)/255);			g1 = clamp((g1*a1+g2*a3)/255);			b1 = clamp((b1*a1+b2*a3)/255);			a1 = clamp(a1+a3);		}		return (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;	}/*	public static int HSBtoRGB(int hue, int saturation, int brightness) {		int r = 0, g = 0, b = 0;		if (saturation == 0) {			r = g = b = brightness;		} else {			float h = (float)hue * 6.0f / 255.0f;			float f = h - (int)h;			int p = (int)(brightness * (255 - saturation) / 255);			int q = (int)(brightness * (255 - saturation * f) / 255);			int t = (int)(brightness * (255 - (saturation * (1-f))) / 255);			switch ((int)h) {			case 0:				r = brightness;				g = t;				b = p;				break;			case 1:				r = q;				g = brightness;				b = p;				break;			case 2:				r = p;				g = brightness;				b = t;				break;			case 3:				r = p;				g = q;				b = brightness;				break;			case 4:				r = t;				g = p;				b = brightness;				break;			case 5:				r = brightness;				g = p;				b = q;				break;			}		}		return 0xff000000 | (r << 16) | (g << 8) | (b << 0);	}	public static int RGBtoHSB(int r, int g, int b) {		float hue;		int cmax = (r > g) ? r : g;		if (b > cmax)			cmax = b;		int cmin = (r < g) ? r : g;		if (b < cmin)			cmin = b;		int brightness = cmax;		int saturation;		if (cmax != 0)			saturation = 255 * (cmax - cmin) / cmax;		else			saturation = 0;		if (saturation == 0)			hue = 0;		else {			float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));			float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));			float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));			if (r == cmax)				hue = bluec - greenc;			else if (g == cmax)				hue = 2.0f + redc - bluec;			else				hue = 4.0f + greenc - redc;			hue = hue / 6.0f;			if (hue < 0)				hue = hue + 1.0f;		}		return ((int)(255 * hue) << 16) | (saturation << 8) | brightness;	}*//*	public static float getHue(int rgb) {		int r = (rgb >> 16) & 0xff;		int g = (rgb >> 8) & 0xff;		int b = rgb & 0xff;		float max = Math.max(r, g, b);		float min = Math.min(r, g, b);		if (max == min)			return -1.0f;		else {			float diff = max-min;			float hue;						if (r == max)				hue = (g-b)/diff;			else if (g == max)				hue = 2.0+(b-r)/diff;			else if (b == max)				hue = 4.0+(r-g)/diff;						hue = hue*60.0f;			if (hue < 0.0)				hue = hue+360.0f;			return hue;		}	}	public static float getSaturation(int rgb) {		int r = (rgb >> 16) & 0xff;		int g = (rgb >> 8) & 0xff;		int b = rgb & 0xff;		float max = Math.max(r, g, b);		float min = Math.min(r, g, b);		if (max==min)			return 0.0f;		else {			float d = (max+min)/2.0f;			if (d <= 0.5)				return (max-min)/(max+min);			else				return (max-min)/(2.0-max-min);		}	}	public static float getBrightness(int rgb) {		int r = (rgb >> 16) & 0xff;		int g = (rgb >> 8) & 0xff;		int b = rgb & 0xff;		float max = Math.max(r, g, b);		float min = Math.min(r, g, b);		return (max+min) / 2.0f;	}*/}