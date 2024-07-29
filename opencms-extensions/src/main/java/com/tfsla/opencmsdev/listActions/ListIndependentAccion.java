package com.tfsla.opencmsdev.listActions;

import org.opencms.workplace.list.CmsListIndependentAction;

public class ListIndependentAccion extends CmsListIndependentAction {

	public ListIndependentAccion(String id, String name, String helpText, String iconPath) {
		super(id);
		this.setName(new FixedCmsMessageContainer(name));
		this.setHelpText(new FixedCmsMessageContainer(helpText));
		this.setIconPath(iconPath);
	}

}
