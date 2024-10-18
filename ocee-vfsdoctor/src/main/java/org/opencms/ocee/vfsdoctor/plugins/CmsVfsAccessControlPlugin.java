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
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsUUID;

public class CmsVfsAccessControlPlugin extends A_CmsVfsDoctorPlugin {
   private static final String ÕÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000classnew = "RECOVER_ACCESS_RESOURCE";
   private static final String ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew = "SELECT_ALL_ACCESS_STRUCTURE";
   private static final String oÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supernew = "SELECT_ALL_GROUPS";
   private static final String ÔÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringnew = "SELECT_ALL_USERS";
   private static final List ÓÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectnew = new ArrayList();

   public CmsVfsAccessControlPlugin() {
      super(true, true);
   }

   public CmsMessageContainer getDescription() {
      return Messages.get().container("GUI_OCEE_VFSDOC_PLUGIN_ACCESSCONTROL_DESC_0");
   }

   public String getName() {
      return "accesscontrol";
   }

   public CmsMessageContainer getNiceName() {
      return Messages.get().container("GUI_OCEE_VFSDOC_PLUGIN_ACCESSCONTROL_NAME_0");
   }

   protected int recover(CmsUUID projectId) {
      Map orphanACEs = this.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(projectId);
      if (orphanACEs.isEmpty()) {
         return 0;
      } else {
         Connection conn = null;
         PreparedStatement stmt = null;
         //int res = (int)false;
         CmsDbContext dbc = null;
         CmsMessages msg = Messages.get().getBundle(this.getReport().getLocale());

         try {
            dbc = this.getDbContext();
            conn = this.getSqlManager().getConnection(dbc);
            stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "RECOVER_ACCESS_RESOURCE");
            Iterator it = orphanACEs.keySet().iterator();

            while(it.hasNext()) {
               String principalId = (String)it.next();
               stmt.setString(1, principalId);
               int res = stmt.executeUpdate();
               this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_ACCESSCONTROL_REC2_2", new Object[]{new Object[]{new Integer(res), principalId}}));
               stmt.clearParameters();
            }
         } catch (SQLException var13) {
            this.writeError(var13);
         } finally {
            this.getSqlManager().closeAll(dbc, conn, stmt, (ResultSet)null);
            if (dbc != null) {
               dbc.clear();
            }

         }

         return orphanACEs.size();
      }
   }

   protected int validate(CmsUUID projectId) {
      CmsMessages msg = Messages.get().getBundle(this.getReport().getLocale());
      Map orphanACEs = this.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(projectId);
      Iterator it = orphanACEs.keySet().iterator();

      while(it.hasNext()) {
         String principalId = (String)it.next();
         String resourcePath = (String)orphanACEs.get(principalId);
         this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_ACCESSCONTROL_VAL1_2", new Object[]{resourcePath, principalId}));
      }

      this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_ACCESSCONTROL_VAL2_1", new Object[]{new Integer(orphanACEs.size())}));
      return orphanACEs.size();
   }

   private Map Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(CmsUUID projectId) {
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet res = null;
      Map ret = new HashMap();
      CmsDbContext dbc = null;

      try {
         dbc = this.getDbContext();
         conn = this.getSqlManager().getConnection(dbc);
         stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "SELECT_ALL_ACCESS_STRUCTURE");
         res = stmt.executeQuery();

         while(res.next()) {
            ret.put(res.getString("principal_id"), res.getString("resource_path"));
         }

         this.getSqlManager().closeAll(dbc, (Connection)null, stmt, res);
         stmt = this.getSqlManager().getPreparedStatement(conn, "SELECT_ALL_USERS");
         res = stmt.executeQuery();

         while(res.next()) {
            ret.remove(res.getString("user_id"));
         }

         this.getSqlManager().closeAll(dbc, (Connection)null, stmt, res);
         stmt = this.getSqlManager().getPreparedStatement(conn, "SELECT_ALL_GROUPS");
         res = stmt.executeQuery();

         while(res.next()) {
            ret.remove(res.getString("group_id"));
         }
      } catch (SQLException var11) {
         this.writeError(var11);
      } finally {
         this.getSqlManager().closeAll(dbc, conn, stmt, res);
         if (dbc != null) {
            dbc.clear();
         }

      }

      Iterator it = ÓÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectnew.iterator();

      while(it.hasNext()) {
         String id = (String)it.next();
         ret.remove(id);
      }

      return ret;
   }

   static {
      ÓÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectnew.add(CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID.toString());
      ÓÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectnew.add(CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID.toString());
      Iterator it = CmsRole.getSystemRoles().iterator();

      while(it.hasNext()) {
         CmsRole role = (CmsRole)it.next();
         ÓÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectnew.add(role.getId().toString());
      }

   }
}
