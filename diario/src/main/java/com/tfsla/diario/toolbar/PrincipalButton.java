package com.tfsla.diario.toolbar;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

public abstract class PrincipalButton extends AbstractButton {
	
	private List<AbstractButton> subButtons;
	
	public List<AbstractButton> getSubButtons() {
		return subButtons;
	}

	public void setSubButtons(List<AbstractButton> subButtons) {
		this.subButtons = subButtons;
	} 

	public PrincipalButton() {
		subButtons = new ArrayList<AbstractButton>();
	}
	
	public void addButton (AbstractButton button) {
		this.subButtons.add(button);
	}
	
	public abstract void getButtonStructure (HttpServletRequest request, HttpServletResponse response, PageContext pageContext);

}
