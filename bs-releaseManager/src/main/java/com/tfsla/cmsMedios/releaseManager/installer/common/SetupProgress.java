package com.tfsla.cmsMedios.releaseManager.installer.common;

public class SetupProgress {
	int percentage;
	String message;
	Boolean isPartial = false;
	Boolean mustReload = false;
	SetupProgressStatus status;
	public Boolean getMustReload() {
		return mustReload;
	}
	public void setMustReload(Boolean mustReload) {
		this.mustReload = mustReload;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public int getPercentage() {
		return percentage;
	}
	public void setPercentage(int percentage) {
		this.percentage = percentage;
	}
	public SetupProgressStatus getStatus() {
		return status;
	}
	public void setStatus(SetupProgressStatus status) {
		this.status = status;
	}
	public Boolean getIsPartial() {
		return isPartial;
	}
	public void setIsPartial(Boolean isPartial) {
		this.isPartial = isPartial;
	}
}
