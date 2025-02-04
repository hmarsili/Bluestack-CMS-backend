package org.opencms.ocee.replication;

import java.util.List;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDriverManager;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.report.I_CmsReport;

public interface I_CmsReplicationHandler {
   void cleanUp(CmsObject var1, CmsDbContext var2, I_CmsReport var3, CmsReplicationServer var4, List var5) throws CmsDataAccessException;

   CmsMessageContainer getHelpText();

   CmsMessageContainer getName();

   CmsMessageContainer getStatDetailHelpText(String var1);

   List getStatDetailKeys();

   CmsMessageContainer getStatDetailName(String var1);

   CmsReplicationStatistics getStatistics();

   void initialize(CmsObject var1, CmsReplicationConfiguration var2, CmsDriverManager var3);

   boolean isInitialized();

   boolean needResources();

   void replicate(CmsObject var1, CmsDbContext var2, I_CmsReport var3, CmsReplicationServer var4, List var5, boolean var6) throws CmsDataAccessException;

   void setStatistics(CmsReplicationStatistics var1);
}
