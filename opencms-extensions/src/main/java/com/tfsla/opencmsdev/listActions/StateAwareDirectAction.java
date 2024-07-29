package com.tfsla.opencmsdev.listActions;

import org.opencms.workplace.list.CmsListDirectAction;

/**
 * Es una CmsLisDirectAction que se configura con una lista de strings. Esos strings representan estados de un
 * objeto, la accion estara visible solo si el campo "Estado" del objeto actual es igual a alguno de los items
 * del array.
 * 
 * @author jpicasso
 */
public class StateAwareDirectAction extends CmsListDirectAction {
	private String[] estados;

	public StateAwareDirectAction(String id, String[] estados) {
		super(id);
		this.estados = estados;
	}

	@Override
	public boolean isVisible() {
		if (getItem() != null) {
			String estado = (String) getItem().get("Estado");

			for (int i = 0; i < this.estados.length; i++) {
				if (this.estados[i].equals(estado)) {
					return true;
				}
			}
			return false;
		}
		return super.isEnabled();
	}
}