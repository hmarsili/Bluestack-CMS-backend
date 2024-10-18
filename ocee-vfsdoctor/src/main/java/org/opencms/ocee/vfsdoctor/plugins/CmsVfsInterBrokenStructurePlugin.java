package org.opencms.ocee.vfsdoctor.plugins;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opencms.db.CmsDbContext;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsMessages;
import org.opencms.ocee.vfsdoctor.A_CmsVfsDoctorPlugin;
import org.opencms.ocee.vfsdoctor.CmsVfsStructureEntry;
import org.opencms.util.CmsUUID;

public class CmsVfsInterBrokenStructurePlugin extends A_CmsVfsDoctorPlugin {
   private static final String øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interfacesuper = "COPY_STRUCTURE";
   private static final String OÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thisnew = "SELECT_ALL_STRUCTURES";

   public CmsVfsInterBrokenStructurePlugin() {
      super(true, true);
   }

   public CmsMessageContainer getDescription() {
      return Messages.get().container("GUI_OCEE_VFSDOC_PLUGIN_INTERBROKENSTRUCTURE_DESC_0");
   }

   public String getName() {
      return "interbrokenstructure";
   }

   public CmsMessageContainer getNiceName() {
      return Messages.get().container("GUI_OCEE_VFSDOC_PLUGIN_INTERBROKENSTRUCTURE_NAME_0");
   }

   protected int recover(CmsUUID projectId) {
      Set missingParents = this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(projectId);
      if (missingParents.isEmpty()) {
         return 0;
      } else {
         CmsMessages msg = Messages.get().getBundle(this.getReport().getLocale());
         Iterator it = missingParents.iterator();

         while(it.hasNext()) {
            String id = (String)it.next();
            Connection conn = null;
            CmsDbContext dbc = null;
            PreparedStatement stmt = null;

            try {
               dbc = this.getDbContext();
               conn = this.getSqlManager().getConnection(dbc);
               stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "COPY_STRUCTURE");
               stmt.setString(1, id);
               boolean found = stmt.executeUpdate() == 1;
               if (found) {
                  this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_INTERBROKENSTRUCTURE_REC2_2", new Object[]{id, getProjectName(projectId)}));
               } else {
                  this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_INTERBROKENSTRUCTURE_REC3_2", new Object[]{id, getProjectName(projectId)}));
               }
            } catch (Exception var13) {
               this.writeError(var13);
            } finally {
               this.getSqlManager().closeAll(dbc, conn, stmt, (ResultSet)null);
               if (dbc != null) {
                  dbc.clear();
               }

            }
         }

         return missingParents.size();
      }
   }

   protected int validate(CmsUUID projectId) {
      CmsMessages msg = Messages.get().getBundle(this.getReport().getLocale());
      Set missingParents = this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(projectId);
      Iterator it = missingParents.iterator();

      while(it.hasNext()) {
         String id = (String)it.next();
         this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_INTERBROKENSTRUCTURE_VAL1_1", new Object[]{id}));
      }

      this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_INTERBROKENSTRUCTURE_VAL2_1", new Object[]{new Integer(missingParents.size())}));
      return missingParents.size();
   }

   private Set Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(CmsUUID projectId) {
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet res = null;
      Map strEntries = new HashMap();
      List brokenEntries = new ArrayList();
      Set ret = new HashSet();
      CmsDbContext dbc = null;

      try {
         dbc = this.getDbContext();
         conn = this.getSqlManager().getConnection(dbc);
         stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "SELECT_ALL_STRUCTURES");
         res = stmt.executeQuery();

         while(res.next()) {
            CmsVfsStructureEntry strEntry = new CmsVfsStructureEntry(res.getString(1), res.getString(2), res.getString(3), res.getString(4));
            strEntries.put(strEntry.getStructureId(), strEntry);
         }
      } catch (SQLException var13) {
         this.writeError(var13);
      } finally {
         this.getSqlManager().closeAll(dbc, conn, stmt, res);
         if (dbc != null) {
            dbc.clear();
         }

      }

      Iterator it = strEntries.values().iterator();

      CmsVfsStructureEntry strEntry;
      while(it.hasNext()) {
         strEntry = (CmsVfsStructureEntry)it.next();
         if (strEntries.get(strEntry.getParentId()) == null && !strEntry.getResourcePath().equals("/")) {
            brokenEntries.add(strEntry);
         }
      }

      it = brokenEntries.iterator();

      while(it.hasNext()) {
         strEntry = (CmsVfsStructureEntry)it.next();
         ret.add(strEntry.getParentId());
      }

      return ret;
   }
}
