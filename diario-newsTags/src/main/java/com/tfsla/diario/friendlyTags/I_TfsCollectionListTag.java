package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspTagException;

public interface I_TfsCollectionListTag {
	public int getIndex();
	public boolean isLast();
	
	public String getCollectionPathName() throws JspTagException;
	public String getCollectionValue(String name) throws JspTagException;
	public String getCollectionIndexValue(String name, int index) throws JspTagException;
	public int getCollectionIndexSize(String name, boolean isCollectionPart) throws JspTagException;

}
