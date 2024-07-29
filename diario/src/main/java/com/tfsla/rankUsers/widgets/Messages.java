package com.tfsla.rankUsers.widgets;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;


public class Messages extends A_CmsMessageBundle {

	private static final String BUNDLE_NAME = "com.tfsla.rankUsers.widgets.messages";

    private static final I_CmsMessageBundle INSTANCE = new Messages();

	public static final String GUI_RNK_USUARIOS_LIST_NAME_0 = "GUI_RNK_USUARIOS_LIST_NAME_0";

	public static final String RNK_USUARIOS_LIST_USUARIO_COLUMN = "RNK_USUARIOS_LIST_USUARIO_COLUMN";

	public static final String RNK_USUARIOS_LIST_GRUPO_COLUMN = "RNK_USUARIOS_LIST_GRUPO_COLUMN";

	public static final String RNK_USUARIOS_LIST_RANKING_COLUMN = "RNK_USUARIOS_LIST_RANKING_COLUMN";

	public static final String RNK_USUARIOS_LIST_UO_COLUMN = "RNK_USUARIOS_LIST_UO_COLUMN";

    
    
	public String getBundleName() {
        return BUNDLE_NAME;
    }

    public static I_CmsMessageBundle get() {
        return INSTANCE;
    }

}
