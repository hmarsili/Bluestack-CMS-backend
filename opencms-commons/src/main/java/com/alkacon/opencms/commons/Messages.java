package com.alkacon.opencms.commons;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

public final class Messages extends A_CmsMessageBundle {
   public static final String ERR_COLLECTOR_CONFIG_INVALID_1 = "ERR_COLLECTOR_CONFIG_INVALID_1";
   private static final String BUNDLE_NAME = "com.alkacon.opencms.commons.messages";
   public static final String LOG_ERROR_DECRPYT_0 = "LOG_ERROR_DECRPYT_0";
   public static final String LOG_ERROR_ENCRYPT_0 = "LOG_ERROR_ENCRYPT_0";
   public static final String LOG_ERROR_CREATE_KEY_0 = "LOG_ERROR_CREATE_KEY_0";
   public static final String LOG_WARN_INVALID_DECRYPT_STRING_1 = "LOG_WARN_INVALID_DECRYPT_STRING_1";
   public static final String LOG_WARN_INVALID_ENCRYPT_STRING_1 = "LOG_WARN_INVALID_ENCRYPT_STRING_1";
   private static final I_CmsMessageBundle INSTANCE = new Messages();

   private Messages() {
   }

   public static I_CmsMessageBundle get() {
      return INSTANCE;
   }

   public String getBundleName() {
      return "com.alkacon.opencms.commons.messages";
   }
}
