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

public class CmsVfsStateConsistency2Plugin extends A_CmsVfsDoctorPlugin {
   private static final String ôÓ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newObject = "SELECT_ALL_WRONG_OFFLINE_ENTRIES";
   private static final String ØÓ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000superObject = "SELECT_PUBLISH_HISTORY_ENTRIES_BY_STRUCTURE_ID";
   private static final String ÖÓ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thisObject = "SET_OFFLINE_STATE_NEW";

   public CmsVfsStateConsistency2Plugin() {
      super(true, true);
   }

   public CmsMessageContainer getDescription() {
      return Messages.get().container("GUI_OCEE_VFSDOC_PLUGIN_STATECONS2_DESC_0");
   }

   public String getName() {
      return "statecons2";
   }

   public CmsMessageContainer getNiceName() {
      return Messages.get().container("GUI_OCEE_VFSDOC_PLUGIN_STATECONS2_NAME_0");
   }

   public int recover() {
      CmsMessages msg = org.opencms.ocee.vfsdoctor.Messages.get().getBundle(this.getReport().getLocale());
      this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_RECOVER_OFFLINE_0"));
      int online = this.recover(ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private);
      if (online == 0) {
         this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_NOTHING_TORECOVER_0"));
      }

      return online;
   }

   public int validate() {
      CmsMessages msg = org.opencms.ocee.vfsdoctor.Messages.get().getBundle(this.getReport().getLocale());
      this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_VALIDATE_OFFLINE_0"));
      return this.validate(ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private);
   }

   protected int recover(CmsUUID projectId) {
      List structure = this.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(projectId);
      if (structure.isEmpty()) {
         return 0;
      } else {
         CmsDbContext dbc = null;
         Connection conn = null;

         try {
            dbc = this.getDbContext();
            conn = this.getSqlManager().getConnection(dbc);
            PreparedStatement stmt = null;
            CmsMessages msg = Messages.get().getBundle(this.getReport().getLocale());
            Iterator it = structure.iterator();

            while(it.hasNext()) {
               CmsVfsStructureEntry strEntry = (CmsVfsStructureEntry)it.next();

               try {
                  stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "SET_OFFLINE_STATE_NEW");
                  stmt.setString(1, strEntry.getStructureId());
                  int res = stmt.executeUpdate();
                  if (res > 0) {
                     this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_STATECONS2_UPDATE_STATE_1", new Object[]{strEntry.getResourcePath()}));
                  }
               } catch (Exception var20) {
                  this.writeError(var20);
               } finally {
                  this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
               }
            }
         } catch (Exception var22) {
            this.writeError(var22);
         } finally {
            if (conn != null) {
               this.getSqlManager().closeAll(dbc, conn, (Statement)null, (ResultSet)null);
            }

            if (dbc != null) {
               dbc.clear();
            }

         }

         return structure.size();
      }
   }

   protected int validate(CmsUUID projectId) {
      CmsMessages msg = Messages.get().getBundle(this.getReport().getLocale());
      List structure = this.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(projectId);
      Iterator it = structure.iterator();

      while(it.hasNext()) {
         CmsVfsStructureEntry strEntry = (CmsVfsStructureEntry)it.next();
         this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_STATECONS2_VAL1_1", new Object[]{strEntry.getResourcePath()}));
      }

      this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_STATECONS2_VAL2_1", new Object[]{new Integer(structure.size())}));
      return structure.size();
   }

   private List õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(CmsUUID projectId) {
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet res = null;
      List ret = new ArrayList();
      CmsDbContext dbc = null;

      try {
         dbc = this.getDbContext();
         conn = this.getSqlManager().getConnection(dbc);
         stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "SELECT_ALL_WRONG_OFFLINE_ENTRIES");
         res = stmt.executeQuery();

         while(res.next()) {
            CmsVfsStructureEntry strEntry = new CmsVfsStructureEntry(res.getString(1), res.getString(2), res.getString(3), res.getString(4));
            ret.add(strEntry);
         }

         this.getSqlManager().closeAll((CmsDbContext)null, (Connection)null, stmt, res);
         Iterator it = ret.iterator();

         while(it.hasNext()) {
            try {
               CmsVfsStructureEntry strEntry = (CmsVfsStructureEntry)it.next();
               stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "SELECT_PUBLISH_HISTORY_ENTRIES_BY_STRUCTURE_ID");
               stmt.setString(1, strEntry.getStructureId());
               res = stmt.executeQuery();
               if (res.next()) {
                  String lastResourcePath = res.getString(2);
                  int lastState = res.getInt(3);
                  if (lastState == 3 && res.next()) {
                     String previousResourcePath = res.getString(2);
                     if (!lastResourcePath.equals(previousResourcePath)) {
                        it.remove();
                     }
                  }
               }
            } catch (SQLException var22) {
               this.writeError(var22);
            } finally {
               this.getSqlManager().closeAll((CmsDbContext)null, (Connection)null, stmt, res);
            }
         }
      } catch (SQLException var24) {
         this.writeError(var24);
      } finally {
         this.getSqlManager().closeAll(dbc, conn, stmt, res);
         if (dbc != null) {
            dbc.clear();
         }

      }

      return ret;
   }
}
