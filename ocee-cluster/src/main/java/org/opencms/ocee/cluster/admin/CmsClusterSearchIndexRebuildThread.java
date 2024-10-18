package org.opencms.ocee.cluster.admin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.opencms.db.CmsDbContext;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsCoreProvider;
import org.opencms.ocee.cluster.CmsClusterEventTypes;
import org.opencms.ocee.cluster.CmsClusterManager;
import org.opencms.ocee.cluster.CmsClusterServer;
import org.opencms.report.A_CmsReportThread;
import org.opencms.util.CmsStringUtil;

public class CmsClusterSearchIndexRebuildThread extends A_CmsReportThread {
   private Throwable m_error;
   private String m_servers = "";

   public CmsClusterSearchIndexRebuildThread(CmsObject cms, String servers) {
      super(cms, Messages.get().getBundle(cms.getRequestContext().getLocale()).key("RPT_CLUSTER_SEARCHINDEX_THREAD_NAME_0"));
      this.m_servers = servers;
      this.initHtmlReport(cms.getRequestContext().getLocale());
   }

   public Throwable getError() {
      return this.m_error;
   }

   public String getReportUpdate() {
      return this.getReport().getReportUpdate();
   }

   public void run() {
      CmsDbContext dbc = null;

      try {
         this.getReport().println(Messages.get().container("RPT_CLUSTER_SEARCHINDEX_BEGIN_0"), 2);
         Map eventData = new HashMap();
         dbc = CmsCoreProvider.getInstance().getNewDbContext(this.getCms().getRequestContext());
         eventData.put("dbContext", dbc);
         eventData.put("report", this.getReport());
         Iterator itServers;
         CmsClusterServer server;
         if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.m_servers)) {
            this.m_servers = "";
            itServers = CmsClusterManager.getInstance().getOtherServers().iterator();

            while(itServers.hasNext()) {
               server = (CmsClusterServer)itServers.next();
               this.m_servers = this.m_servers + server.getName();
               if (itServers.hasNext()) {
                  this.m_servers = this.m_servers + "|";
               }
            }
         }

         itServers = CmsStringUtil.splitAsList(this.m_servers, "|", true).iterator();

         while(itServers.hasNext()) {
            server = CmsClusterManager.getInstance().getServer(itServers.next().toString());
            eventData.put("IP", server.getIp());
            CmsClusterManager.getInstance().getEventHandler().forwardEvent(server, CmsClusterEventTypes.REBUILD_SEARCHINDEX.getType(), eventData, eventData);
         }

         this.getReport().println(Messages.get().container("RPT_CLUSTER_SEARCHINDEX_END_0"), 2);
      } catch (Throwable var9) {
         this.getReport().println(var9);
      } finally {
         if (dbc != null) {
            dbc.clear();
         }

      }

   }
}
