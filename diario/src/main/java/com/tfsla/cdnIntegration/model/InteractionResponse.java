package com.tfsla.cdnIntegration.model;

import java.util.ArrayList;
import java.util.List;

public class InteractionResponse {
	private boolean success;
	private List<String> errorList;
	private String interactionId;
	private String responseMsg;
	
	public boolean isSuccess() {
		return success;
	}
	
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public List<String> getErrorList() {
		return errorList;
	}
	
	public void addError(String error) {
		if (this.errorList==null)
			errorList = new ArrayList<String>();
		
		errorList.add(error);
	}
	
	public void setErrorList(List<String> errorList) {
		this.errorList = errorList;
	}
	
	public String getInteractionId() {
		return interactionId;
	}
	
	public void setInteractionId(String processId) {
		this.interactionId = processId;
	}
	
	public String getResponseMsg() {
		return responseMsg;
	}
	
	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}
}
