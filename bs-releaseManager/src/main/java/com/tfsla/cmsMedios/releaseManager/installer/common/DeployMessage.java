package com.tfsla.cmsMedios.releaseManager.installer.common;

import java.io.Serializable;

@SuppressWarnings("serial")
public class DeployMessage implements Serializable {
	String message;
	String fileName;
	String contents;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getContents() {
		return contents;
	}
	public void setContents(String contents) {
		this.contents = contents;
	}
}