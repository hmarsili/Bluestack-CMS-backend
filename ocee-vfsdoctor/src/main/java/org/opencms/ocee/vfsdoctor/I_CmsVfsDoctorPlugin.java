package org.opencms.ocee.vfsdoctor;

import org.opencms.db.generic.CmsSqlManager;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;
import org.opencms.report.I_CmsReport;

public interface I_CmsVfsDoctorPlugin {
   boolean canRunInMode(CmsVfsDoctorPluginExeMode var1);

   void configure(String var1, String var2) throws CmsException;

   int execute(CmsVfsDoctorPluginExeMode var1, I_CmsReport var2);

   CmsObject getCms();

   CmsMessageContainer getDescription();

   String getName();

   CmsMessageContainer getNiceName();

   CmsSqlManager getSqlManager();

   void setCms(CmsObject var1);
}
