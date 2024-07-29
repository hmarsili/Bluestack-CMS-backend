package com.tfsla.diario.imageVariants;

import org.opencms.file.CmsResource;

public class Image {
	private CmsResource res;
	private String focalPoint;
	
	public Image(CmsResource res, String focalPoint) {
		this.res = res;
		this.focalPoint = focalPoint;
	}
	
	public CmsResource getRes() {
		return res;
	}
	
	public void setRes(CmsResource res) {
		this.res = res;
	}
	public String getFocalPoint() {
		return focalPoint;
	}
	public void setFocalPoint(String focalPoint) {
		this.focalPoint = focalPoint;
	}
	
	
}
