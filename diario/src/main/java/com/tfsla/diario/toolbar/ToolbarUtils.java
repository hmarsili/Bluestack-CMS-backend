package com.tfsla.diario.toolbar;

import com.tfsla.diario.toolbar.preview.PreviewButton;

public class ToolbarUtils {

	public static Class getToolbarButtonClass (String buttonName) {
		if (buttonName.equals("PreviewButton"))
			return PreviewButton.class;	
		return null;
	}
}
