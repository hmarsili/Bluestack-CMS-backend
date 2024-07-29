package org.opencms.workplace.list;

import org.opencms.workplace.CmsWorkplace;

public class CmsListColumnDefinitionExclusiveActions extends
		CmsListColumnDefinition {

	private String compareSubItemName;
	private String subItemName;
	private I_CmsListDirectAction onAction;
	private I_CmsListDirectAction offAction;

	public String getCompareSubItemName() {
		return compareSubItemName;
	}

	public void setCompareSubItemName(String compareSubItemName) {
		this.compareSubItemName = compareSubItemName;
	}

	
	 @Override
	public String htmlCell(CmsListItem item, boolean isPrintable) {
	        StringBuffer html = new StringBuffer(512);
	        int value = (Integer)item.get(subItemName);
	        int compareValue = (Integer)item.get(compareSubItemName);
	        onAction.setItem(item);
	        boolean enabled = onAction.isEnabled();
	        if (isPrintable) {
	        	onAction.setEnabled(false);
	        }
	        if (value!=compareValue)
	        	html.append(onAction.buttonHtml(getWp()));
	        if (isPrintable) {
	        	onAction.setEnabled(enabled);
	        }

	        offAction.setItem(item);
	        enabled = offAction.isEnabled();
	        if (isPrintable) {
	        	offAction.setEnabled(false);
	        }
	        if (value==compareValue)
	        	html.append(offAction.buttonHtml(getWp()));
	        if (isPrintable) {
	        	offAction.setEnabled(enabled);
	        }
	        html.append("\n");
	        return html.toString();
	}

	public String htmlCell(CmsListItem item, CmsWorkplace wp, boolean isPrintable) {
        StringBuffer html = new StringBuffer(512);
        int value = (Integer)item.get(subItemName);
        int compareValue = (Integer)item.get(compareSubItemName);
        onAction.setItem(item);
        boolean enabled = onAction.isEnabled();
        if (isPrintable) {
        	onAction.setEnabled(false);
        }
        if (value!=compareValue)
        	html.append(onAction.buttonHtml(wp));
        if (isPrintable) {
        	onAction.setEnabled(enabled);
        }

        offAction.setItem(item);
        enabled = offAction.isEnabled();
        if (isPrintable) {
        	offAction.setEnabled(false);
        }
        if (value==compareValue)
        	html.append(offAction.buttonHtml(wp));
        if (isPrintable) {
        	offAction.setEnabled(enabled);
        }
        html.append("\n");
        return html.toString();
    }


	public CmsListColumnDefinitionExclusiveActions(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public I_CmsListDirectAction getOffAction() {
		return offAction;
	}

	public void setOffAction(I_CmsListDirectAction offAction) {
		super.addDirectAction(offAction);
		this.offAction = offAction;
	}

	public I_CmsListDirectAction getOnAction() {
		return onAction;
	}

	public void setOnAction(I_CmsListDirectAction onAction) {
		super.addDirectAction(onAction);
		this.onAction = onAction;
	}

	public String getSubItemName() {
		return subItemName;
	}

	public void setSubItemName(String subItemName) {
		this.subItemName = subItemName;
	}

}
