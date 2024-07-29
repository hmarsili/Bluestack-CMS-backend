package com.tfsla.diario.toolbar;

import java.util.ArrayList;
import java.util.List;

public class Menu {

	private List<PrincipalButton> buttons;

	public List<PrincipalButton> getButtons() {
		return buttons;
	}

	public void setButtons(List<PrincipalButton> buttons) {
		this.buttons = buttons;
	}
	
	public Menu() {
		buttons = new ArrayList<PrincipalButton>();
	}
	
	public void addButton (PrincipalButton button) {
		buttons.add(button);
	}
	
}
