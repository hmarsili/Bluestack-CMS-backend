package com.tfsla.diario.workplace.commons;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;


public final class Messages extends A_CmsMessageBundle {


    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "com.tfsla.diario.workplace.commons.messages";

    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new Messages();

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CHYT_ID_1 = "GUI_CHYT_ID_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_GET_YOUTUBE_ID_TARGET_1 = "ERR_GET_YOUTUBE_ID_TARGET_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CHANGE_YOUTUBE_ID_TARGET_0 = "ERR_CHANGE_YOUTUBE_ID_TARGET_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EDIT_YOUTUBE_ID_0 = "GUI_EDIT_YOUTUBE_ID_0";

	public static final String GUI_PREVIEW_YOUTUBE_ID_0 = "GUI_PREVIEW_YOUTUBE_ID_0";

	public static final String ERR_CHANGE_VIMEO_ID_TARGET_0 = "ERR_CHANGE_VIMEO_ID_TARGET_0";

	public static final String ERR_GET_VIMEO_ID_TARGET_1 = "ERR_GET_VIMEO_ID_TARGET_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EDIT_VIMEO_ID_0 = "GUI_EDIT_VIMEO_ID_0";

	public static final String GUI_CHVM_ID_1 = "GUI_CHVM_ID_1";
    
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