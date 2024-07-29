package com.tfsla.diario.facebook;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;


public class Messages extends A_CmsMessageBundle {

	private static final String BUNDLE_NAME = "com.tfsla.diario.facebook.messages";

    private static final I_CmsMessageBundle INSTANCE = new Messages();

	public static final String FACEBOOK_LIST_NAME_PUBLISHER_COLUMN = "FACEBOOK_LIST_NAME_PUBLISHER_COLUMN";
	public static final String FACEBOOK_LIST_KEY_PUBLISHER_COLUMN = "FACEBOOK_LIST_KEY_PUBLISHER_COLUMN";
	public static final String FACEBOOK_LIST_SECRET_PUBLISHER_COLUMN = "FACEBOOK_LIST_SECRET_PUBLISHER_COLUMN";

	public static final String GUI_FACEBOOK_LIST_BORRAR_COLUMN_0 = "GUI_FACEBOOK_LIST_BORRAR_COLUMN_0";
	public static final String GUI_FACEBOOK_LIST_ACTION_BORRAR_HELP_0 = "GUI_FACEBOOK_LIST_ACTION_BORRAR_HELP_0";
	public static final String GUI_FACEBOOK_LIST_ACTION_BORRAR_NAME_0 = "GUI_FACEBOOK_LIST_ACTION_BORRAR_NAME_0";
	public static final String GUI_FACEBOOK_LIST_ACTION_BORRAR_CONF_0 = "GUI_FACEBOOK_LIST_ACTION_BORRAR_CONF_0";

	public static final String GUI_FACEBOOK_LIST_NAME_0 = "GUI_FACEBOOK_LIST_NAME_0";
	
	public static final String FACEBOOK_LIST_TOKEN_PUBLISHER_COLUMN = "GUI_FACEBOOK_LIST_TOKEN_PUBLISHER_COLUM_0";
	public static final String FACEBOOK_LIST_PAGE_ID_PUBLISHER_COLUMN = "GUI_FACEBOOK_LIST_PAGE_ID_PUBLISHER_COLUMN";


	public String getBundleName() {
        return BUNDLE_NAME;
    }

    public static I_CmsMessageBundle get() {
        return INSTANCE;
    }

}
