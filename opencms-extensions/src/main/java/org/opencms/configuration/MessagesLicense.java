package org.opencms.configuration;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

public class MessagesLicense extends A_CmsMessageBundle {



    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.configuration.license";

    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new MessagesLicense();

    /** Message constant for key in the resource bundle. */
    public static final String INIT_LICENSE_CONFIG_INIT_0 = "INIT_LICENSE_CONFIG_INIT_0";

    public static final String INIT_LICENSE_CONFIG_ADD_0 = "INIT_LICENSE_CONFIG_ADD_0";

	public static final String INIT_LICENSE_CONFIG_FINISHED_0 = "INIT_LICENSE_CONFIG_FINISHED_0";

	public static final String INIT_LICENSE_ERROR_0 = "INIT_LICENSE_ERROR_0";


    private MessagesLicense() {
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
