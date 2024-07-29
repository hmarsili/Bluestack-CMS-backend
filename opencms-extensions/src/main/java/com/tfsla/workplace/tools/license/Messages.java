package com.tfsla.workplace.tools.license;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

public final class Messages extends A_CmsMessageBundle {

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "com.tfsla.workplace.tools.license.messages";

    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new Messages();

	public static final String GUI_LICENSE_LABEL_STATUS_BLOCK_0 = "GUI_LICENSE_LABEL_STATUS_BLOCK_0";

    /**
     * Hides the public constructor for this utility class.<p>
     */
    private Messages() {

        // hide the constructor
    }

    /**
     * Returns an instance of this localized message accessor.<p>
     * 
     * @return an instance of this localized message accessor
     */
    public static I_CmsMessageBundle get() {

        return INSTANCE;
    }

    /**
     * Returns the bundle name for this OpenCms package.<p>
     * 
     * @return the bundle name for this OpenCms package
     */
    public String getBundleName() {

        return BUNDLE_NAME;
    }
}