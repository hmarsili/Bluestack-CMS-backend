package com.tfsla.cmsMedios.releaseManager.installer.jsp;

import com.tfsla.diario.admin.jsp.TfsMessages;

import net.sf.json.JSONObject;

public class ReleaseConfigurationStep {
	public ReleaseConfigurationStep(JSONObject manifest, TfsMessages messages) {
		this.manifest = manifest;
		this.messages = messages;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getHtmlAction() {
		return htmlAction;
	}
	public void setHtmlAction(String htmlAction) {
		this.htmlAction = htmlAction;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	String title;
	String description;
	String htmlAction;
	JSONObject manifest;
	TfsMessages messages;
	int order;
}
