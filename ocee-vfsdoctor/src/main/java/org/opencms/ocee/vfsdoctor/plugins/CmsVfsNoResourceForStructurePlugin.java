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

public class CmsVfsNoResourceForStructurePlugin extends A_CmsVfsDoctorPlugin {
   private static final String öÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000ifnew = "SELECT_ALL_STRUCTURES_WITH_NO_RESOURCE";

   public CmsVfsNoResourceForStructurePlugin() {
      super(true, true);
   }

   public CmsMessageContainer getDescription() {
      return Messages.get().container("GUI_OCEE_VFSDOC_PLUGIN_RESOURCESTRUCTURE_DESC_0");
   }

   public String getName() {
      return "resourcestructure";
   }

   public CmsMessageContainer getNiceName() {
      return Messages.get().container("GUI_OCEE_VFSDOC_PLUGIN_RESOURCESTRUCTURE_NAME_0");
   }

   protected int recover(CmsUUID projectId) {
      List structure = this.ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(projectId);
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
      List structure = this.ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(projectId);
      Iterator it = structure.iterator();

      while(it.hasNext()) {
         CmsVfsStructureEntry strEntry = (CmsVfsStructureEntry)it.next();
         this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_RESOURCESTRUCTURE_VAL1_2", new Object[]{strEntry.getResourcePath(), strEntry.getResourceId()}));
      }

      this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_RESOURCESTRUCTURE_VAL2_1", new Object[]{new Integer(structure.size())}));
      return structure.size();
   }

   private List ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(CmsUUID projectId) {
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet res = null;
      List ret = new ArrayList();
      CmsDbContext dbc = null;

      try {
         dbc = this.getDbContext();
         conn = this.getSqlManager().getConnection(dbc);
         stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "SELECT_ALL_STRUCTURES_WITH_NO_RESOURCE");
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
