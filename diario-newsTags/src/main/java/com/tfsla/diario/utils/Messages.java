package com.tfsla.diario.utils;


import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

public class Messages extends A_CmsMessageBundle {


	private static final String BUNDLE_NAME = "com.tfsla.diario.admin.workplace";

      private static final I_CmsMessageBundle INSTANCE = new Messages();

   private Messages() {

        // hide the constructor
   }

   public static I_CmsMessageBundle get() {

       return INSTANCE;
   }

  public String getBundleName() {

       return BUNDLE_NAME;
   }
}
