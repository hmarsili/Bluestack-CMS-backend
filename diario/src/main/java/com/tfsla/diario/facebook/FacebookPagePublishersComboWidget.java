package com.tfsla.diario.facebook;

import org.opencms.file.CmsObject;
import org.opencms.widgets.A_CmsSelectWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;

import java.util.Iterator;
import java.util.List;


public class FacebookPagePublishersComboWidget extends A_CmsSelectWidget {


    /**
     * Creates a new select widget.<p>
     */
    public FacebookPagePublishersComboWidget() {

        // empty constructor is required for class registration
        super();
    }

    /**
     * Creates a select widget with the select options specified in the given configuration List.<p>
     *
     * The list elements must be of type <code>{@link CmsSelectWidgetOption}</code>.<p>
     *
     * @param configuration the configuration (possible options) for the select widget
     *
     * @see CmsSelectWidgetOption
     */
    public FacebookPagePublishersComboWidget(List configuration) {

        super(configuration);
    }

    /**
     * Creates a select widget with the specified select options.<p>
     *
     * @param configuration the configuration (possible options) for the select box
     */
    public FacebookPagePublishersComboWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        StringBuffer result = new StringBuffer(16);

        result.append("<td class=\"xmlTd\" style=\"height: 25px;\"><select class=\"xmlInput");
        if (param.hasError()) {
            result.append(" xmlInputError");
        }
        result.append("\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\">");



        try {
        	List<FacebookPageAccountPublisher> publishers = FacebookPageService.getInstance().getPagePublishers(cms);
        	String selected = getSelectedValue(cms, param);
        	for (Iterator iter = publishers.iterator(); iter.hasNext();) {
        		FacebookPageAccountPublisher publisher = (FacebookPageAccountPublisher) iter.next();

        		String strSelected = (selected!=null && selected.equals("" + publisher.getName())) ? "selected" : "";
        		result.append("	<option value=\"" + publisher.getName() + "\" " + strSelected + " >" + publisher.getName() + "</option>\n");

    		}
        } catch (Exception e) {
			e.printStackTrace();
		}

        result.append("</select>");
        result.append("</td>");

        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new FacebookPagePublishersComboWidget(getConfiguration());
    }

}
