package org.opencms.workplace.list;



public class CmsListColumnDefinitionCheck extends CmsListColumnDefinition {


	public CmsListColumnDefinitionCheck(String id) {
		super(id);
	}

	@Override
	public String htmlCell(CmsListItem item, boolean isPrintable) {
		StringBuffer html = new StringBuffer(512);
		html.append("<input type=\"checkbox\" name=\"" + getId() + "\" value=\"" + item.get(getId()) + "\">");
		html.append("\n");
		return html.toString();
	}

	@Override
	public boolean isSorteable() {
		// TODO Auto-generated method stub
		return false;
	}

}
