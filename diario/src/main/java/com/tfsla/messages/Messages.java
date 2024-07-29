package com.tfsla.messages;

import org.opencms.configuration.CPMConfig;
import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

import org.opencms.configuration.CmsMedios;

public class Messages extends A_CmsMessageBundle {

   private static String site = null;	
   private static String publication = null;
   
   private static String BUNDLE_NAME;

   private static final I_CmsMessageBundle INSTANCE = new Messages();

   private Messages() {

        // hide the constructor
   }

   public static I_CmsMessageBundle get(String siteName, String publicationID) {

	   site = siteName;
	   publication = publicationID;
	   
	   setConfigBundleName();
	   
       return INSTANCE;
   }

  public String getBundleName() {

       return BUNDLE_NAME;
   }
  
  public static void setConfigBundleName(){
	  
	  CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
	  
	  BUNDLE_NAME = config.getParam(site, publication, "internationalization", "bundleName", "");
	  
	  return;
  }
}
