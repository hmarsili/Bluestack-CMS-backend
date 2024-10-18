package org.opencms.ocee.vfsdoctor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.file.CmsProject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.ocee.vfsdoctor.plugins.CmsVfsBasePlugin;
import org.opencms.ocee.vfsdoctor.plugins.CmsVfsDoctorPluginConfiguration;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

public class CmsVfsDoctorPluginSqlManager extends CmsSqlManager {
   protected static final String o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = "_${OTHER_PROJECT}_";
   private static final Log Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new = CmsLog.getLog(CmsVfsDoctorPluginSqlManager.class);
   private static CmsVfsDoctorPluginConfiguration Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return = null;
   private static final long Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class = 2080728410928087893L;
   private String Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String;
   private Map Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object = new HashMap();

   public CmsVfsDoctorPluginSqlManager(CmsVfsDoctorPluginConfiguration pluginConfig, String dbProvider, String poolUrl) {
      this.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String = dbProvider;
      this.init(3, poolUrl);
      if (Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return == null) {
         Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return = new CmsVfsDoctorPluginConfiguration(new CmsVfsBasePlugin());

         try {
            CmsVfsDoctorManager.getInstance().loadXmlConfiguration(Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return);
         } catch (CmsException var7) {
            if (Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.isErrorEnabled()) {
               Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.error(var7.getMessageContainer().key(), var7);
            }
         }
      }

      Iterator itBaseQueries = Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return.getQueries().iterator();

      while(itBaseQueries.hasNext()) {
         CmsVfsDoctorPluginSqlQuery query = (CmsVfsDoctorPluginSqlQuery)itBaseQueries.next();
         this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.put(query.getName(), query.getQuery(this.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String));
      }

      Iterator itQueries = pluginConfig.getQueries().iterator();

      while(itQueries.hasNext()) {
         CmsVfsDoctorPluginSqlQuery query = (CmsVfsDoctorPluginSqlQuery)itQueries.next();
         this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.put(query.getName(), query.getQuery(this.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String));
      }

   }

   public static CmsSqlManager getSqlManager(Class plugin) {
      return CmsVfsDoctorManager.getInstance().getPluginManager().getPlugin(plugin).getSqlManager();
   }

   protected static String replaceTheOtherProjectPattern(CmsUUID projectId, String query) {
      String replacePattern = projectId.equals(CmsProject.ONLINE_PROJECT_ID) ? "_OFFLINE_" : "_ONLINE_";
      query = CmsStringUtil.substitute(query, "_${OTHER_PROJECT}_", replacePattern);
      return query;
   }

   public PreparedStatement getPreparedStatement(Connection con, CmsProject project, String queryKey) throws SQLException {
      CmsVfsDoctorPreparedStatement ps = new CmsVfsDoctorPreparedStatement(super.getPreparedStatement(con, project, queryKey));
      ps.setSql(this.readQuery(project.getUuid(), queryKey));
      return ps;
   }

   public PreparedStatement getPreparedStatement(Connection con, CmsUUID projectId, String queryKey) throws SQLException {
      CmsVfsDoctorPreparedStatement ps = new CmsVfsDoctorPreparedStatement(super.getPreparedStatement(con, projectId, queryKey));
      ps.setSql(this.readQuery(projectId, queryKey));
      return ps;
   }

   public PreparedStatement getPreparedStatement(Connection con, String queryKey) throws SQLException {
      CmsVfsDoctorPreparedStatement ps = new CmsVfsDoctorPreparedStatement(super.getPreparedStatement(con, queryKey));
      ps.setSql(this.readQuery(CmsUUID.getNullUUID(), queryKey));
      return ps;
   }

   public String readQuery(CmsUUID projectId, String queryKey) {
      String newKey = queryKey + (projectId.isNullUUID() ? "" : (projectId.equals(CmsProject.ONLINE_PROJECT_ID) ? "_ONLINE" : "_OFFLINE"));
      if (this.m_cachedQueries.containsKey(newKey)) {
         return (String)this.m_cachedQueries.get(newKey);
      } else {
         String query = super.readQuery(projectId, queryKey);
         String newQuery = replaceTheOtherProjectPattern(projectId, query);
         this.m_cachedQueries.put(newKey, newQuery);
         return newQuery;
      }
   }

   public String readQuery(String queryKey) {
      if (this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object == null) {
         return super.readQuery(queryKey);
      } else {
         String value = (String)this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object.get(queryKey);
         return value == null ? super.readQuery(queryKey) : value;
      }
   }
}
