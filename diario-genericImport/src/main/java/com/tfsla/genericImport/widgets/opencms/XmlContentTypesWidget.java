package com.tfsla.genericImport.widgets.opencms;

import java.util.ArrayList;
import java.util.List;

import org.opencms.file.CmsObject;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;

import com.tfsla.genericImport.service.ContentTypeService;

public class XmlContentTypesWidget extends CmsSelectWidget {

	@Override
	protected List parseSelectOptions(CmsObject cms,
			I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {
		// TODO Auto-generated method stub
		
		List<CmsSelectWidgetOption> options = new ArrayList<CmsSelectWidgetOption>();
		
		ContentTypeService ctService = new ContentTypeService();
		List<String> types = ctService.getContentTypes();
		for (String type : types) {
			CmsSelectWidgetOption option = new CmsSelectWidgetOption(type);
			
			options.add(option);
		}
		
		return options;
	}
	
    public I_CmsWidget newInstance() {

        return new XmlContentTypesWidget(getConfiguration());
    }
    
    public XmlContentTypesWidget() {

        // empty constructor is required for class registration
        super();
    }

    
    public XmlContentTypesWidget(List configuration) {

        super(configuration);
    }

    
    public XmlContentTypesWidget(String configuration) {

        super(configuration);
    }


}
