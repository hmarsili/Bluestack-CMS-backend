package org.opencms.ocee.vfsdoctor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.apache.commons.logging.Log;
import org.opencms.db.CmsDbContext;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsCoreProvider;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.ocee.vfsdoctor.plugins.CmsVfsDoctorPluginConfiguration;
import org.opencms.report.CmsHtmlReport;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

public abstract class A_CmsVfsDoctorPlugin implements I_CmsVfsDoctorPlugin {
   protected static final String Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return = "DELETE_ACES";
   protected static final String ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface = "DELETE_OFFLINE_CONTENT";
   protected static final String o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = "DELETE_ONLINE_CONTENT";
   protected static final String ÒO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000while = "DELETE_PROPERTIES";
   protected static final String õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int = "DELETE_RESOURCE";
   protected static final String Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new = "DELETE_STRUCTURE";
   protected static final CmsUUID ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private = new CmsUUID();
   protected static final CmsUUID onlineProjectId;
   protected static final String ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if = "SELECT_ACTUAL_SIBLING_COUNT";
   protected static final String ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float = "SET_SIBLING_COUNT";
   protected static final String Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object = "UPDATE_STRUCTURE_STATE_BY_RES_ID";
   private static final Log LOG;
   private final boolean canRecover;
   private final boolean canValidate;
   private CmsObject cmsObject;
   private I_CmsReport cmsReport;
   private CmsVfsDoctorPluginSqlManager vfsDoctorPluginSqlManager;

   protected A_CmsVfsDoctorPlugin(boolean canValidate, boolean canRecover) {
      this.canValidate = canValidate;
      this.canRecover = canRecover;
   }

   public static String getProjectName(CmsUUID projectId) {
      return projectId.equals(onlineProjectId) ? "Online" : "Offline";
   }

   public boolean canRunInMode(CmsVfsDoctorPluginExeMode mode) {
      if (mode.equals(CmsVfsDoctorPluginExeMode.VALIDATE)) {
         return this.canValidate;
      } else {
         return mode.equals(CmsVfsDoctorPluginExeMode.RECOVER) ? this.canRecover : false;
      }
   }

   public void configure(String dbProvider, String poolUrl) throws CmsException {
      CmsVfsDoctorPluginConfiguration configuration = new CmsVfsDoctorPluginConfiguration(this);
      CmsVfsDoctorManager.getInstance().loadXmlConfiguration(configuration);
      this.vfsDoctorPluginSqlManager = new CmsVfsDoctorPluginSqlManager(configuration, dbProvider, poolUrl);
      if (CmsLog.INIT.isInfoEnabled()) {
         CmsLog.INIT.info(Messages.get().getBundle().key("INIT_PLUGIN_CONFIGURED_1", new Object[]{this.getClass().getName()}));
      }

   }

   public int execute(CmsVfsDoctorPluginExeMode mode, I_CmsReport report) {
      if (report == null) {
         this.cmsReport = new CmsLogReport(CmsLocaleManager.getDefaultLocale(), this.getClass());
      } else {
         this.cmsReport = report;
      }

      if (mode.equals(CmsVfsDoctorPluginExeMode.VALIDATE) && this.canRunInMode(CmsVfsDoctorPluginExeMode.VALIDATE)) {
         return this.validate();
      } else {
         return mode.equals(CmsVfsDoctorPluginExeMode.RECOVER) && this.canRunInMode(CmsVfsDoctorPluginExeMode.RECOVER) ? this.recover() : 0;
      }
   }

   public CmsObject getCms() {
      return this.cmsObject;
   }

   public CmsSqlManager getSqlManager() {
      return this.vfsDoctorPluginSqlManager;
   }

   public int recover() {
      int ret = 0;
      this.writeInfo(Messages.get().getBundle(this.getReport().getLocale()).key("GUI_OCEE_VFSDOC_PLUGIN_RECOVER_ONLINE_0"));
      int online = this.recover(onlineProjectId);
      ret = ret + online;
      if (online == 0) {
         this.writeInfo(Messages.get().getBundle(this.getReport().getLocale()).key("GUI_OCEE_VFSDOC_PLUGIN_NOTHING_TORECOVER_0"));
      }

      this.writeInfo(Messages.get().getBundle(this.getReport().getLocale()).key("GUI_OCEE_VFSDOC_PLUGIN_RECOVER_OFFLINE_0"));
      int offline = this.recover(ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private);
      ret += offline;
      if (offline == 0) {
         this.writeInfo(Messages.get().getBundle(this.getReport().getLocale()).key("GUI_OCEE_VFSDOC_PLUGIN_NOTHING_TORECOVER_0"));
      }

      return ret;
   }

   public void setCms(CmsObject cms) {
      this.cmsObject = cms;
   }

   public int validate() {
      int ret = 0;
      this.writeInfo(Messages.get().getBundle(this.getReport().getLocale()).key("GUI_OCEE_VFSDOC_PLUGIN_VALIDATE_ONLINE_0"));
      ret = ret + this.validate(onlineProjectId);
      this.writeInfo(Messages.get().getBundle(this.getReport().getLocale()).key("GUI_OCEE_VFSDOC_PLUGIN_VALIDATE_OFFLINE_0"));
      ret += this.validate(ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private);
      return ret;
   }

   protected void deleteStructureEntry(CmsVfsStructureEntry strEntry, CmsUUID projectId) {
      CmsDbContext dbc = null;
      Connection conn = null;

      try {
         dbc = this.getDbContext();
         conn = this.getSqlManager().getConnection(dbc);
         PreparedStatement stmt = null;
         ResultSet rs = null;

         int siblingCount;
         try {
            stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "DELETE_PROPERTIES");
            stmt.setString(1, strEntry.getStructureId());
            siblingCount = stmt.executeUpdate();
            if (siblingCount > 0) {
               this.writeInfo(Messages.get().getBundle(this.getReport().getLocale()).key("GUI_OCEE_VFSDOC_PLUGIN_DELETE_PROPS_2", new Object[]{new Integer(siblingCount), strEntry.getResourcePath()}));
            }
         } catch (Exception var234) {
            this.writeError(var234);
         } finally {
            this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
         }

         try {
            stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "DELETE_STRUCTURE");
            stmt.setString(1, strEntry.getStructureId());
            siblingCount = stmt.executeUpdate();
            if (siblingCount > 0) {
               this.writeInfo(Messages.get().getBundle(this.getReport().getLocale()).key("GUI_OCEE_VFSDOC_PLUGIN_DELETE_STRUCTURE_1", new Object[]{strEntry.getResourcePath()}));
            }
         } catch (Exception var232) {
            this.writeError(var232);
         } finally {
            this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
         }

         siblingCount = 0;

         try {
            stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "SELECT_ACTUAL_SIBLING_COUNT");
            stmt.setString(1, strEntry.getResourceId());
            rs = stmt.executeQuery();
            if (rs.next()) {
               siblingCount = rs.getInt(1);
            } else {
               this.writeInfo(Messages.get().getBundle(this.getReport().getLocale()).key("GUI_OCEE_VFSDOC_PLUGIN_MISSING_RESOURCE_2", new Object[]{strEntry.getResourceId(), strEntry.getResourcePath()}));
            }
         } catch (Exception var230) {
            this.writeError(var230);
         } finally {
            this.getSqlManager().closeAll(dbc, (Connection)null, stmt, rs);
         }

         int res;
         if (siblingCount > 0) {
            try {
               stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "SET_SIBLING_COUNT");
               stmt.setInt(1, siblingCount);
               stmt.setString(2, strEntry.getResourceId());
               res = stmt.executeUpdate();
               if (res > 0) {
                  this.writeInfo(Messages.get().getBundle(this.getReport().getLocale()).key("GUI_OCEE_VFSDOC_PLUGIN_UPDATE_SIBCOUNT_1", new Object[]{strEntry.getResourceId()}));
               }
            } catch (Exception var228) {
               this.writeError(var228);
            } finally {
               this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
            }
         } else {
            try {
               stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "DELETE_PROPERTIES");
               stmt.setString(1, strEntry.getResourceId());
               res = stmt.executeUpdate();
               if (res > 0) {
                  this.writeInfo(Messages.get().getBundle(this.getReport().getLocale()).key("GUI_OCEE_VFSDOC_PLUGIN_DELETE_SHAREDPROPS_2", new Object[]{new Integer(res), strEntry.getResourcePath()}));
               }
            } catch (Exception var226) {
               this.writeError(var226);
            } finally {
               this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
            }

            try {
               stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "DELETE_ACES");
               stmt.setString(1, strEntry.getResourceId());
               res = stmt.executeUpdate();
               if (res > 0) {
                  this.writeInfo(Messages.get().getBundle(this.getReport().getLocale()).key("GUI_OCEE_VFSDOC_PLUGIN_DELETE_ACES_2", new Object[]{new Integer(res), strEntry.getResourcePath()}));
               }
            } catch (Exception var224) {
               this.writeError(var224);
            } finally {
               this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
            }

            try {
               if (projectId.equals(CmsProject.ONLINE_PROJECT_ID)) {
                  stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "DELETE_ONLINE_CONTENT");
               } else {
                  stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "DELETE_OFFLINE_CONTENT");
               }

               stmt.setString(1, strEntry.getResourceId());
               res = stmt.executeUpdate();
               if (res > 0) {
                  this.writeInfo(Messages.get().getBundle(this.getReport().getLocale()).key("GUI_OCEE_VFSDOC_PLUGIN_DELETE_CONTENT_1", new Object[]{strEntry.getResourcePath()}));
               }
            } catch (Exception var222) {
               this.writeError(var222);
            } finally {
               this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
            }

            try {
               stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "DELETE_RESOURCE");
               stmt.setString(1, strEntry.getResourceId());
               res = stmt.executeUpdate();
               if (res > 0) {
                  this.writeInfo(Messages.get().getBundle(this.getReport().getLocale()).key("GUI_OCEE_VFSDOC_PLUGIN_DELETE_RESOURCE_1", new Object[]{strEntry.getResourcePath()}));
               }
            } catch (Exception var220) {
               this.writeError(var220);
            } finally {
               this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
            }

            if (projectId.equals(CmsProject.ONLINE_PROJECT_ID)) {
               try {
                  stmt = this.getSqlManager().getPreparedStatement(conn, this.getTheOtherProjectId(projectId), "UPDATE_STRUCTURE_STATE_BY_RES_ID");
                  stmt.setInt(1, CmsResource.STATE_NEW.getState());
                  stmt.setString(2, strEntry.getResourceId());
                  stmt.executeUpdate();
               } catch (Exception var218) {
               } finally {
                  this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
               }
            } else {
               this.deleteStructureEntry(strEntry, CmsProject.ONLINE_PROJECT_ID);
            }
         }
      } catch (Exception var236) {
         this.writeError(var236);
      } finally {
         if (conn != null) {
            this.getSqlManager().closeAll(dbc, conn, (Statement)null, (ResultSet)null);
         }

         if (dbc != null) {
            dbc.clear();
         }

      }

   }

   protected CmsDbContext getDbContext() {
      return this.cmsObject != null ? CmsCoreProvider.getInstance().getNewDbContext(this.cmsObject.getRequestContext()) : CmsCoreProvider.getInstance().getNewDbContext((CmsRequestContext)null);
   }

   protected I_CmsReport getReport() {
      return this.cmsReport;
   }

   protected CmsUUID getTheOtherProjectId(CmsUUID projectId) {
      return projectId.equals(onlineProjectId) ? ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private : onlineProjectId;
   }

   protected abstract int recover(CmsUUID var1);

   protected abstract int validate(CmsUUID var1);

   protected void writeError(Throwable e) {
      this.getReport().println(e);
      if (LOG.isWarnEnabled()) {
         LOG.warn(e);
      }

   }

   protected void writeInfo(String info) {
      if (this.getReport() instanceof CmsHtmlReport) {
         this.getReport().println(org.opencms.report.Messages.get().container("RPT_ARGUMENT_1", this.getNiceName().key() + ": " + info));
      } else {
         this.getReport().println(org.opencms.report.Messages.get().container("RPT_ARGUMENT_1", CmsStringUtil.escapeJavaScript(this.getNiceName().key() + ": " + info)));
      }

      if (LOG.isInfoEnabled()) {
         LOG.info(this.getNiceName().key() + ": " + info);
      }

   }

   static {
      onlineProjectId = CmsProject.ONLINE_PROJECT_ID;
      LOG = CmsLog.getLog(A_CmsVfsDoctorPlugin.class);
   }
}
