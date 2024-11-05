package com.tfsla.cmsMedios.releaseManager.installer.jsp;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

public class Messages extends A_CmsMessageBundle {

	private static final String BUNDLE_NAME = "com.tfsla.cmsMedios.releaseManager.workplace";
    private static final I_CmsMessageBundle INSTANCE = new Messages();
	
    private Messages() { }
    
	@Override
	public String getBundleName() {
		return BUNDLE_NAME;
	}
	
	public static I_CmsMessageBundle get() {
		return INSTANCE;
	}
}
