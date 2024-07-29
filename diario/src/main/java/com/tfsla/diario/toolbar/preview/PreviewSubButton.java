package com.tfsla.diario.toolbar.preview;

import com.tfsla.diario.toolbar.AbstractButton;

public class PreviewSubButton extends AbstractButton {

	String width;
	String height;
	String queryString;
	
	
	public PreviewSubButton( String name, String width, String height, String queryString) {
		this.name = name;
		this.width = width;
		this.height = height;
		this.queryString = queryString;
	}


	@Override
	public String getValues() {
		if (width.equals("") && height.equals(""))
			return "name:" + name;
		return "width:" +  width + ";" +
		 			 "heigth:" + height + ";" +
		 			 "queryString:" + queryString + ";" +
		 			 "name:" + name;
		
	}
	
		
}
