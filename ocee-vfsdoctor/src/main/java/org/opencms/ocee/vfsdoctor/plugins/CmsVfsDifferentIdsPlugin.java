package org.opencms.ocee.vfsdoctor.plugins;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.opencms.db.CmsDbContext;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsMessages;
import org.opencms.ocee.vfsdoctor.A_CmsVfsDoctorPlugin;
import org.opencms.ocee.vfsdoctor.CmsVfsStructureEntry;
import org.opencms.util.CmsUUID;

public class CmsVfsDifferentIdsPlugin extends A_CmsVfsDoctorPlugin {
   private static final String ÔÓ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000privatenew = "SELECT_ALL_UNSYNC_RES";
   private static final String oÓ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000donew = "UPDATE_ONLINE_ACE_IDS";
   private static final String OÓ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000fornew = "UPDATE_ONLINE_CONTENT_IDS";
   private static final String ÕÓ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interfacenew = "UPDATE_ONLINE_PARENT_IDS";
   private static final String ÒÓ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000whilenew = "UPDATE_ONLINE_PROPERTY_IDS";
   private static final String ÓÓ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000publicnew = "UPDATE_ONLINE_RESOURCE_IDS";
   private static final String øÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000floatnew = "UPDATE_ONLINE_STRUCTURE_IDS";

   public CmsVfsDifferentIdsPlugin() {
      super(true, true);
   }

   public CmsMessageContainer getDescription() {
      return Messages.get().container("GUI_OCEE_VFSDOC_PLUGIN_DIFFERENTIDS_DESC_0");
   }

   public String getName() {
      return "differentids";
   }

   public CmsMessageContainer getNiceName() {
      return Messages.get().container("GUI_OCEE_VFSDOC_PLUGIN_DIFFERENTIDS_NAME_0");
   }

   public int recover() {
      CmsMessages msg = org.opencms.ocee.vfsdoctor.Messages.get().getBundle(this.getReport().getLocale());
      this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_RECOVER_ONLINE_0"));
      int online = this.recover(onlineProjectId);
      if (online == 0) {
         this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_NOTHING_TORECOVER_0"));
      }

      return online;
   }

   public int validate() {
      CmsMessages msg = org.opencms.ocee.vfsdoctor.Messages.get().getBundle(this.getReport().getLocale());
      this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_VALIDATE_ONLINE_0"));
      return this.validate(onlineProjectId);
   }

   protected int recover(CmsUUID projectId) {
      List diffs = this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object();
      if (diffs.isEmpty()) {
         return 0;
      } else {
         try {
            Iterator it = diffs.iterator();

            while(it.hasNext()) {
               List diff = (List)it.next();
               CmsVfsStructureEntry offlineEntry = (CmsVfsStructureEntry)diff.get(0);
               CmsVfsStructureEntry onlineEntry = (CmsVfsStructureEntry)diff.get(1);
               this.updateOnlineData(offlineEntry, onlineEntry);
            }
         } catch (Exception var7) {
            this.writeError(var7);
         }

         return diffs.size();
      }
   }

   protected void updateOnlineData(CmsVfsStructureEntry offlineEntry, CmsVfsStructureEntry onlineEntry) {
      CmsMessages msg = Messages.get().getBundle(this.getReport().getLocale());
      CmsDbContext dbc = null;
      Connection conn = null;

      try {
         dbc = this.getDbContext();
         conn = this.getSqlManager().getConnection(dbc);
         PreparedStatement stmt = null;
         int res;
         if (!offlineEntry.getStructureId().equals(onlineEntry.getStructureId())) {
            try {
               stmt = this.getSqlManager().getPreparedStatement(conn, onlineProjectId, "UPDATE_ONLINE_PROPERTY_IDS");
               stmt.setString(1, offlineEntry.getStructureId());
               stmt.setString(2, onlineEntry.getStructureId());
               res = stmt.executeUpdate();
               if (res > 0) {
                  this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_DIFFERENTIDS_UPDATE_PROPS_2", new Object[]{new Integer(res), offlineEntry.getResourcePath()}));
               }
            } catch (Exception var156) {
               this.writeError(var156);
            } finally {
               this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
            }
         }

         if (!offlineEntry.getResourceId().equals(onlineEntry.getResourceId())) {
            try {
               stmt = this.getSqlManager().getPreparedStatement(conn, onlineProjectId, "UPDATE_ONLINE_PROPERTY_IDS");
               stmt.setString(1, offlineEntry.getResourceId());
               stmt.setString(2, onlineEntry.getResourceId());
               res = stmt.executeUpdate();
               if (res > 0) {
                  this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_DIFFERENTIDS_UPDATE_PROPS_2", new Object[]{new Integer(res), offlineEntry.getResourcePath()}));
               }
            } catch (Exception var154) {
               this.writeError(var154);
            } finally {
               this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
            }
         }

         try {
            stmt = this.getSqlManager().getPreparedStatement(conn, onlineProjectId, "UPDATE_ONLINE_STRUCTURE_IDS");
            stmt.setString(1, offlineEntry.getStructureId());
            stmt.setString(2, offlineEntry.getParentId());
            stmt.setString(3, offlineEntry.getResourceId());
            stmt.setString(4, onlineEntry.getStructureId());
            res = stmt.executeUpdate();
            if (res > 0) {
               this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_DIFFERENTIDS_UPDATE_STRUCTURE_1", new Object[]{offlineEntry.getResourcePath()}));
            }
         } catch (Exception var152) {
            this.writeError(var152);
         } finally {
            this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
         }

         if (!offlineEntry.getParentId().equals(onlineEntry.getParentId())) {
            try {
               stmt = this.getSqlManager().getPreparedStatement(conn, onlineProjectId, "UPDATE_ONLINE_PARENT_IDS");
               stmt.setString(1, offlineEntry.getParentId());
               stmt.setString(2, onlineEntry.getParentId());
               res = stmt.executeUpdate();
               if (res > 0) {
                  this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_DIFFERENTIDS_UPDATE_PARENTS_2", new Object[]{new Integer(res), offlineEntry.getResourcePath()}));
               }
            } catch (Exception var150) {
               this.writeError(var150);
            } finally {
               this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
            }
         }

         if (!offlineEntry.getResourceId().equals(onlineEntry.getResourceId())) {
            try {
               stmt = this.getSqlManager().getPreparedStatement(conn, onlineProjectId, "UPDATE_ONLINE_ACE_IDS");
               stmt.setString(1, offlineEntry.getResourceId());
               stmt.setString(2, onlineEntry.getResourceId());
               res = stmt.executeUpdate();
               if (res > 0) {
                  this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_DIFFERENTIDS_UPDATE_ACES_2", new Object[]{new Integer(res), offlineEntry.getResourcePath()}));
               }
            } catch (Exception var148) {
               this.writeError(var148);
            } finally {
               this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
            }

            try {
               stmt = this.getSqlManager().getPreparedStatement(conn, onlineProjectId, "UPDATE_ONLINE_RESOURCE_IDS");
               stmt.setString(1, offlineEntry.getResourceId());
               stmt.setString(2, onlineEntry.getResourceId());
               res = stmt.executeUpdate();
               if (res > 0) {
                  this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_DIFFERENTIDS_UPDATE_RESOURCE_1", new Object[]{offlineEntry.getResourcePath()}));
               }
            } catch (Exception var146) {
               this.writeError(var146);
            } finally {
               this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
            }

            try {
               stmt = this.getSqlManager().getPreparedStatement(conn, onlineProjectId, "UPDATE_ONLINE_CONTENT_IDS");
               stmt.setString(1, offlineEntry.getResourceId());
               stmt.setString(2, onlineEntry.getResourceId());
               res = stmt.executeUpdate();
               if (res > 0) {
                  this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_DIFFERENTIDS_UPDATE_CONTENT_1", new Object[]{offlineEntry.getResourcePath()}));
               }
            } catch (Exception var144) {
               this.writeError(var144);
            } finally {
               this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
            }
         }
      } catch (Exception var158) {
         this.writeError(var158);
      } finally {
         if (conn != null) {
            this.getSqlManager().closeAll(dbc, conn, (Statement)null, (ResultSet)null);
         }

         if (dbc != null) {
            dbc.clear();
         }

      }

   }

   protected int validate(CmsUUID projectId) {
      CmsMessages msg = Messages.get().getBundle(this.getReport().getLocale());
      List diffs = this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object();
      Iterator it = diffs.iterator();

      while(it.hasNext()) {
         List diff = (List)it.next();
         CmsVfsStructureEntry offlineEntry = (CmsVfsStructureEntry)diff.get(0);
         this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_DIFFERENTIDS_VAL1_1", new Object[]{offlineEntry.getResourcePath()}));
      }

      this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_DIFFERENTIDS_VAL2_1", new Object[]{new Integer(diffs.size())}));
      return diffs.size();
   }

   private List Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object() {
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet res = null;
      List ret = new ArrayList();
      CmsDbContext dbc = null;

      try {
         dbc = this.getDbContext();
         conn = this.getSqlManager().getConnection(dbc);
         stmt = this.getSqlManager().getPreparedStatement(conn, onlineProjectId, "SELECT_ALL_UNSYNC_RES");
         res = stmt.executeQuery();

         while(res.next()) {
            CmsVfsStructureEntry offlineEntry = new CmsVfsStructureEntry(res.getString(1), res.getString(2), res.getString(3), res.getString(4));
            CmsVfsStructureEntry onlineEntry = new CmsVfsStructureEntry(res.getString(5), res.getString(6), res.getString(7), res.getString(8));
            List diff = new ArrayList();
            diff.add(offlineEntry);
            diff.add(onlineEntry);
            ret.add(diff);
         }
      } catch (SQLException var12) {
         this.writeError(var12);
      } finally {
         this.getSqlManager().closeAll(dbc, conn, stmt, res);
         if (dbc != null) {
            dbc.clear();
         }

      }

      return ret;
   }
}
