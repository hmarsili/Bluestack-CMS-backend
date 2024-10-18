package org.opencms.ocee.vfsdoctor.plugins;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.opencms.db.CmsDbContext;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsMessages;
import org.opencms.ocee.vfsdoctor.A_CmsVfsDoctorPlugin;
import org.opencms.ocee.vfsdoctor.CmsVfsStructureEntry;
import org.opencms.util.CmsUUID;

public class CmsVfsDuplicateStructurePlugin extends A_CmsVfsDoctorPlugin {
   private static final String ØÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000voidnew = "SELECT_ALL_DUPLICATE_STRUCTURES";
   private static final String ôÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000nullnew = "SELECT_DUPLICATE_STRUCTURES_RESOURCE";

   public CmsVfsDuplicateStructurePlugin() {
      super(true, true);
   }

   public CmsMessageContainer getDescription() {
      return Messages.get().container("GUI_OCEE_VFSDOC_PLUGIN_DUPLICATESTRUCTURE_DESC_0");
   }

   public String getName() {
      return "duplicatestructure";
   }

   public CmsMessageContainer getNiceName() {
      return Messages.get().container("GUI_OCEE_VFSDOC_PLUGIN_DUPLICATESTRUCTURE_NAME_0");
   }

   protected int recover(CmsUUID projectId) {
      Map structure = this.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(projectId);
      if (structure.isEmpty()) {
         return 0;
      } else {
         CmsMessages msg = Messages.get().getBundle(this.getReport().getLocale());
         Iterator it = structure.keySet().iterator();

         while(it.hasNext()) {
            String path = (String)it.next();
            Iterator itEntries = ((List)structure.get(path)).iterator();
            boolean resFound = false;

            while(itEntries.hasNext()) {
               CmsVfsStructureEntry strEntry = (CmsVfsStructureEntry)itEntries.next();
               if (!resFound) {
                  Connection conn = null;
                  CmsDbContext dbc = null;
                  PreparedStatement stmt = null;
                  ResultSet res = null;

                  try {
                     dbc = this.getDbContext();
                     conn = this.getSqlManager().getConnection(dbc);
                     stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "SELECT_DUPLICATE_STRUCTURES_RESOURCE");
                     stmt.setString(1, strEntry.getStructureId());
                     res = stmt.executeQuery();
                     if (res.next()) {
                        resFound = true;
                        this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_DUPLICATESTRUCTURE_REC2_2", new Object[]{strEntry.getResourcePath(), strEntry.getResourceId()}));
                     } else {
                        this.deleteStructureEntry(strEntry, projectId);
                     }
                  } catch (Exception var17) {
                     this.writeError(var17);
                  } finally {
                     this.getSqlManager().closeAll(dbc, conn, stmt, res);
                     if (dbc != null) {
                        dbc.clear();
                     }

                  }
               } else {
                  this.deleteStructureEntry(strEntry, projectId);
               }
            }

            if (!resFound) {
               this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_DUPLICATESTRUCTURE_REC3_1", new Object[]{path}));
            }
         }

         return structure.size();
      }
   }

   protected int validate(CmsUUID projectId) {
      CmsMessages msg = Messages.get().getBundle(this.getReport().getLocale());
      Map structure = this.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(projectId);
      Iterator it = structure.keySet().iterator();

      while(it.hasNext()) {
         String strEntry = (String)it.next();
         int occurrences = ((List)structure.get(strEntry)).size();
         this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_DUPLICATESTRUCTURE_VAL1_2", new Object[]{strEntry, new Integer(occurrences)}));
      }

      this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_DUPLICATESTRUCTURE_VAL2_1", new Object[]{new Integer(structure.size())}));
      return structure.size();
   }

   private Map Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(CmsUUID projectId) {
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet res = null;
      Map ret = new HashMap();
      CmsDbContext dbc = null;

      try {
         dbc = this.getDbContext();
         conn = this.getSqlManager().getConnection(dbc);
         stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "SELECT_ALL_DUPLICATE_STRUCTURES");
         res = stmt.executeQuery();

         while(res.next()) {
            CmsVfsStructureEntry strEntry = new CmsVfsStructureEntry(res.getString(1), res.getString(2), res.getString(3), res.getString(4));
            if (!ret.containsKey(strEntry.getResourcePath())) {
               ret.put(strEntry.getResourcePath(), new ArrayList());
            }

            List resources = (List)ret.get(strEntry.getResourcePath());
            resources.add(strEntry);
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
