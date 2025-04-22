package com.tfsla.cmsMedios.releaseManager.github.common;

/**
 * Represents a file/asset on Github 
 */
public class GitFile {
	private String fileName;
	private String previousFileName;
	private String rawUrl;
	private GitFileStatus status;
	
	public GitFile() { }
	
	public GitFile(String fileName, String rawUrl, GitFileStatus status) {
		this.fileName = fileName;
		this.rawUrl = rawUrl;
		this.status = status;
	}
	
	/**
	 * Return the file name
	 * @return File name
	 */
	public String getFileName() {
		return fileName;
	}
	/**
	 * Sets the file name
	 * @param fileName File name
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	/**
	 * Returns the URL for file's raw content
	 * @return File raw URL
	 */
	public String getRawUrl() {
		return rawUrl;
	}
	/**
	 * Sets file raw URL
	 * @param rawUrl File raw URL
	 */
	public void setRawUrl(String rawUrl) {
		this.rawUrl = rawUrl;
	}
	/**
	 * Represents the status in Github for a file/asset
	 * @see GitFileStatus
	 * @return Github file/asset status
	 */
	public GitFileStatus getStatus() {
		return status;
	}
	/**
	 * Sets file status
	 * @param status File status
	 * @see GitFileStatus
	 */
	public void setStatus(GitFileStatus status) {
		this.status = status;
	}

	/**
	 * Gets previous file name (when renaming)
	 * @return previous file name
	 */
	public String getPreviousFileName() {
		return previousFileName;
	}

	/**
	 * Sets previous file name
	 * @param previousFileName Previous file name (when renaming)
	 */
	public void setPreviousFileName(String previousFileName) {
		this.previousFileName = previousFileName;
	}
}
