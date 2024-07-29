package com.tfsla.opencms.webusers.openauthorization.common.exceptions;

public class InvalidPathException extends Exception {

	private String path;
	private Object document;
	private Exception innerException;
	private static final long serialVersionUID = 1L;
	
	public InvalidPathException(String path) {
		super("Cannot access the path into the document: " + path);
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public Object getDocument() {
		return document;
	}
	
	public void setDocument(Object document) {
		this.document = document;
	}

	public Exception getInnerException() {
		return innerException;
	}

	public void setInnerException(Exception innerException) {
		this.innerException = innerException;
	}

}
