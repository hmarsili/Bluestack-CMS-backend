package org.opencms.ocee.vfsdoctor.plugins;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.opencms.db.CmsDbContext;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsMessages;
import org.opencms.ocee.vfsdoctor.A_CmsVfsDoctorPlugin;
import org.opencms.util.CmsUUID;

public class CmsVfsSiblingsPlugin extends A_CmsVfsDoctorPlugin {
   private static final String õÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000intnew = "SELECT_ALL_SIBLING_COUNT";

   public CmsVfsSiblingsPlugin() {
      super(true, true);
   }

   public CmsMessageContainer getDescription() {
      return Messages.get().container("GUI_OCEE_VFSDOC_PLUGIN_SIBLINGS_DESC_0");
   }

   public String getName() {
      return "siblings";
   }

   public CmsMessageContainer getNiceName() {
      return Messages.get().container("GUI_OCEE_VFSDOC_PLUGIN_SIBLINGS_NAME_0");
   }

   protected int recover(CmsUUID projectId) {
      Map badSiblingCounts = this.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(projectId);
      if (badSiblingCounts.isEmpty()) {
         return 0;
      } else {
         CmsMessages msg = Messages.get().getBundle(this.getReport().getLocale());
         Iterator it = badSiblingCounts.keySet().iterator();

         while(it.hasNext()) {
            Connection conn = null;
            PreparedStatement stmt = null;
            CmsDbContext dbc = null;

            try {
               dbc = this.getDbContext();
               conn = this.getSqlManager().getConnection(dbc);
               String resourceId = (String)it.next();
               int siblingCount = (Integer)badSiblingCounts.get(resourceId);
               stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "SET_SIBLING_COUNT");
               stmt.setInt(1, siblingCount == 0 ? 1 : siblingCount);
               stmt.setString(2, resourceId);
               stmt.executeUpdate();
               this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
               this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_SIBLINGS_REC2_1", new Object[]{resourceId}));
            } catch (Exception var13) {
               this.writeError(var13);
            } finally {
               this.getSqlManager().closeAll(dbc, conn, stmt, (ResultSet)null);
               if (dbc != null) {
                  dbc.clear();
               }

            }
         }

         return badSiblingCounts.size();
      }
   }

   protected int validate(CmsUUID projectId) {
      CmsMessages msg = Messages.get().getBundle(this.getReport().getLocale());
      Map badSiblingCounts = this.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(projectId);
      Iterator it = badSiblingCounts.keySet().iterator();

      while(it.hasNext()) {
         String resourceId = (String)it.next();
         this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_SIBLINGS_VAL1_1", new Object[]{resourceId}));
      }

      this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_SIBLINGS_VAL2_1", new Object[]{new Integer(badSiblingCounts.size())}));
      return badSiblingCounts.size();
   }

   private Map Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(CmsUUID projectId) {
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet res = null;
      Map resSiblings = new HashMap();
      Map ret = new HashMap();
      CmsDbContext dbc = null;

      try {
         dbc = this.getDbContext();
         conn = this.getSqlManager().getConnection(dbc);

         try {
            stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "SELECT_ALL_SIBLING_COUNT");
            res = stmt.executeQuery();

            while(res.next()) {
               resSiblings.put(res.getString("resource_id"), new Integer(res.getInt("sibling_count")));
            }
         } catch (Exception var33) {
            this.writeError(var33);
         } finally {
            this.getSqlManager().closeAll(dbc, (Connection)null, stmt, res);
         }

         stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "SELECT_ACTUAL_SIBLING_COUNT");

         for(Iterator it = resSiblings.keySet().iterator(); it.hasNext(); stmt.clearParameters()) {
            String resourceId = (String)it.next();
            stmt.setString(1, resourceId);

            try {
               res = stmt.executeQuery();
               res.next();
               Integer siblingCount = new Integer(res.getInt(1));
               if (!resSiblings.get(resourceId).equals(siblingCount)) {
                  ret.put(resourceId, siblingCount);
               }
            } catch (Exception var31) {
               this.writeError(var31);
            } finally {
               this.getSqlManager().closeAll(dbc, (Connection)null, (Statement)null, res);
            }
         }
      } catch (SQLException var35) {
         this.writeError(var35);
      } finally {
         this.getSqlManager().closeAll(dbc, conn, stmt, (ResultSet)null);
         if (dbc != null) {
            dbc.clear();
         }

      }

      return ret;
   }
}
