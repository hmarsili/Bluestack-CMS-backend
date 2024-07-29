package com.tfsla.rankUsers.action;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.digester.Digester;
import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.file.CmsObject;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.module.A_CmsModuleAction;
import org.opencms.module.CmsModule;
import org.xml.sax.SAXException;



public class RankUserAction extends A_CmsModuleAction {

	public void initialize(CmsObject adminCms, CmsConfigurationManager configurationManager, CmsModule module)
	{
		Digester digester = new Digester();
		
		digester.setValidating(false);
		digester.push(this);
		digester.addObjectCreate(
				"events/actionEvent",
				"class",
	            Exception.class);
		
	    digester.addSetNext("events/actionEvent", "addActionEvent");

	    InputStream is = null;
	    try {
	    	is = RankUserAction.class
            .getClassLoader()
            .getResourceAsStream("com/tfsla/rankUsers/action/ActionEvents.xml");
	    	
			digester.parse(is
                    );

		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}

	}
	
    public void addActionEvent(I_CmsEventListener eventListener) {
		OpenCms.addCmsEventListener(eventListener);

    }

	
}
