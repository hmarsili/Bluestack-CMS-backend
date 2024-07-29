package org.opencms.configuration;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

public class MessagesCmsMedios extends A_CmsMessageBundle  {

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.configuration.CmsMedios";

    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new MessagesCmsMedios();

    /** Message constant for key in the resource bundle. */
    public static final String INIT_CMSMEDIOS_CONFIG_INIT_0 = "INIT_CMSMEDIOS_CONFIG_INIT_0";

	public static final String INIT_CMSMEDIOS_CONFIG_ADD_0 = "INIT_CMSMEDIOS_CONFIG_ADD_0";

	public static final String INIT_CMSMEDIOS_SITE_ADD_0 = "INIT_CMSMEDIOS_SITE_ADD_0";

	public static final String INIT_CMSMEDIOS_MODULE_ADD_0 = "INIT_CMSMEDIOS_MODULE_ADD_0";

	public static final String INIT_CMSMEDIOS_PUBLICATION_ADD_0 = "INIT_CMSMEDIOS_PUBLICATION_ADD_0";

	public static final String INIT_CMSMEDIOS_SITEMODULE_ADD_0 = "INIT_CMSMEDIOS_SITEMODULE_ADD_0";

	public static final String INIT_CMSMEDIOS_PUBLICATIONMODULE_ADD_0 = "INIT_CMSMEDIOS_PUBLICATIONMODULE_ADD_0";

	public static final String INIT_CMSMEDIOS_PARAM_ADD_0 = "INIT_CMSMEDIOS_PARAM_ADD_0";

	public static final String INIT_CMSMEDIOS_PARAMGROUPITEM_ADD_0 = "INIT_CMSMEDIOS_PARAMGROUPITEM_ADD_0";

	public static final String INIT_CMSMEDIOS_PARAMGROUP_ADD_0 = "INIT_CMSMEDIOS_PARAMGROUP_ADD_0";
    
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
