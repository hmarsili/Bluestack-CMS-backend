package com.tfsla.diario.newsletters.common;

public class ComplaintType {
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Boolean isComplaint() {
		return isComplaint;
	}
	public void setIsComplaint(Boolean isComplaint) {
		this.isComplaint = isComplaint;
	}
	int ID;
	String name;
	Boolean isComplaint;
}
