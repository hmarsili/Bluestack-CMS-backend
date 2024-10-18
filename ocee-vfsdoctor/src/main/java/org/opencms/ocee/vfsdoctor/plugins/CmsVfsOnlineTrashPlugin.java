package org.opencms.ocee.vfsdoctor.plugins;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.opencms.db.CmsDbContext;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsMessages;
import org.opencms.ocee.vfsdoctor.A_CmsVfsDoctorPlugin;
import org.opencms.ocee.vfsdoctor.CmsVfsStructureEntry;
import org.opencms.util.CmsUUID;

public class CmsVfsOnlineTrashPlugin extends A_CmsVfsDoctorPlugin {
   private static final String õÓ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000ObjectObject = "SELECT_ALL_TRASH_ENTRIES";

   public CmsVfsOnlineTrashPlugin() {
      super(true, true);
   }

   public CmsMessageContainer getDescription() {
      return Messages.get().container("GUI_OCEE_VFSDOC_PLUGIN_ONLINETRASH_DESC_0");
   }

   public String getName() {
      return "onlinetrash";
   }

   public CmsMessageContainer getNiceName() {
      return Messages.get().container("GUI_OCEE_VFSDOC_PLUGIN_ONLINETRASH_NAME_0");
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
      List structure = this.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if(projectId);
      if (structure.isEmpty()) {
         return 0;
      } else {
         try {
            Iterator it = structure.iterator();

            while(it.hasNext()) {
               CmsVfsStructureEntry strEntry = (CmsVfsStructureEntry)it.next();
               this.deleteStructureEntry(strEntry, projectId);
            }
         } catch (Exception var5) {
            this.writeError(var5);
         }

         return structure.size();
      }
   }

   protected int validate(CmsUUID projectId) {
      CmsMessages msg = Messages.get().getBundle(this.getReport().getLocale());
      List structure = this.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if(projectId);
      Iterator it = structure.iterator();

      while(it.hasNext()) {
         CmsVfsStructureEntry strEntry = (CmsVfsStructureEntry)it.next();
         this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_ONLINETRASH_VAL1_1", new Object[]{strEntry.getResourcePath()}));
      }

      this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_ONLINETRASH_VAL2_1", new Object[]{new Integer(structure.size())}));
      return structure.size();
   }

   private List ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if(CmsUUID projectId) {
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet res = null;
      List ret = new ArrayList();
      CmsDbContext dbc = null;

      try {
         dbc = this.getDbContext();
         conn = this.getSqlManager().getConnection(dbc);
         stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "SELECT_ALL_TRASH_ENTRIES");
         res = stmt.executeQuery();

         while(res.next()) {
            CmsVfsStructureEntry strEntry = new CmsVfsStructureEntry(res.getString(1), res.getString(2), res.getString(3), res.getString(4));
            ret.add(strEntry);
         }
      } catch (SQLException var11) {
         this.writeError(var11);
      } finally {
         this.getSqlManager().closeAll(dbc, conn, stmt, res);
         if (dbc != null) {
            dbc.clear();
         }

      }

      return ret;
   }
}
