package com.tfsla.genericImport.widgets.opencms;

import java.util.ArrayList;
import java.util.List;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.main.CmsException;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;

import com.tfsla.genericImport.service.ContentTypeService;


public class PropertySelectorWidget extends CmsSelectWidget {

	@Override
	protected List parseSelectOptions(CmsObject cms,
			I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {
		// TODO Auto-generated method stub
		
		List<CmsSelectWidgetOption> options = new ArrayList<CmsSelectWidgetOption>();
		
		ContentTypeService ctService = new ContentTypeService();
		List<CmsPropertyDefinition> props;
		try {
			props = ctService.getAllProperties(cms);
		
			for (CmsPropertyDefinition prop : props) {
				CmsSelectWidgetOption option = new CmsSelectWidgetOption(prop.getName());
				
				options.add(option);
			}
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return options;
	}

    public I_CmsWidget newInstance() {

        return new PropertySelectorWidget(getConfiguration());
    }
    
    public PropertySelectorWidget() {

        // empty constructor is required for class registration
        super();
    }

    
    public PropertySelectorWidget(List configuration) {

        super(configuration);
    }

    
    public PropertySelectorWidget(String configuration) {

        super(configuration);
    }


}
