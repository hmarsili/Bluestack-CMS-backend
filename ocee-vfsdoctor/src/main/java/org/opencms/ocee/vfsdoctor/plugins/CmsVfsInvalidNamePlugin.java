package org.opencms.ocee.vfsdoctor.plugins;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.opencms.db.CmsDbContext;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsMessages;
import org.opencms.ocee.vfsdoctor.A_CmsVfsDoctorPlugin;
import org.opencms.ocee.vfsdoctor.CmsVfsStructureEntry;
import org.opencms.util.CmsResourceTranslator;
import org.opencms.util.CmsUUID;

public class CmsVfsInvalidNamePlugin extends A_CmsVfsDoctorPlugin {
   private static final String øÓ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000classObject = "SELECT_ALL_STRUCTURES";
   private static final String öÓ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000StringObject = "UPDATE_RESOURCE_PATH";

   public CmsVfsInvalidNamePlugin() {
      super(true, true);
   }

   public CmsMessageContainer getDescription() {
      return Messages.get().container("GUI_OCEE_VFSDOC_PLUGIN_INVALIDNAME_DESC_0");
   }

   public String getName() {
      return "invalidname";
   }

   public CmsMessageContainer getNiceName() {
      return Messages.get().container("GUI_OCEE_VFSDOC_PLUGIN_INVALIDNAME_NAME_0");
   }

   protected int recover(CmsUUID projectId) {
      List structure = this.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(projectId);
      if (structure.isEmpty()) {
         return 0;
      } else {
         CmsMessages msg = Messages.get().getBundle(this.getReport().getLocale());

         try {
            Iterator it = structure.iterator();

            while(it.hasNext()) {
               CmsVfsStructureEntry strEntry = (CmsVfsStructureEntry)it.next();
               String translatedPath = this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(projectId, strEntry);
               this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_INVALIDNAME_REC2_2", new Object[]{strEntry.getResourcePath(), translatedPath}));
            }
         } catch (Exception var7) {
            this.writeError(var7);
         }

         return structure.size();
      }
   }

   protected int validate(CmsUUID projectId) {
      CmsResourceTranslator translator = this.getCms().getRequestContext().getFileTranslator();
      CmsMessages msg = Messages.get().getBundle(this.getReport().getLocale());
      List structure = this.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(projectId);
      Iterator it = structure.iterator();

      while(it.hasNext()) {
         CmsVfsStructureEntry strEntry = (CmsVfsStructureEntry)it.next();
         String translatedPath = translator.translateResource(strEntry.getResourcePath());
         this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_INVALIDNAME_VAL1_2", new Object[]{strEntry.getResourcePath(), translatedPath}));
      }

      this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_INVALIDNAME_VAL2_1", new Object[]{new Integer(structure.size())}));
      return structure.size();
   }

   private boolean o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(List brokenEntries, Map strEntries) {
      CmsResourceTranslator translator = this.getCms().getRequestContext().getFileTranslator();
      boolean ret = false;
      Iterator iter = strEntries.values().iterator();

      while(iter.hasNext()) {
         CmsVfsStructureEntry strEntry = (CmsVfsStructureEntry)iter.next();
         String translatedPath = translator.translateResource(strEntry.getResourcePath());
         if (!translatedPath.equals(strEntry.getResourcePath())) {
            brokenEntries.add(strEntry);
            ret = true;
         }
      }

      return ret;
   }

   private List ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(CmsUUID projectId) {
      Map strEntries = new HashMap();
      List ret = new ArrayList();
      CmsDbContext dbc = null;
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet res = null;

      try {
         dbc = this.getDbContext();
         conn = this.getSqlManager().getConnection(dbc);
         stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "SELECT_ALL_STRUCTURES");
         res = stmt.executeQuery();

         while(res.next()) {
            CmsVfsStructureEntry strEntry = new CmsVfsStructureEntry(res.getString(1), res.getString(2), res.getString(3), res.getString(4));
            strEntries.put(strEntry.getStructureId(), strEntry);
         }
      } catch (SQLException var12) {
         this.writeError(var12);
      } finally {
         this.getSqlManager().closeAll(dbc, conn, stmt, res);
         if (dbc != null) {
            dbc.clear();
         }

      }

      if (this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(ret, strEntries)) {
         Collections.sort(ret, new Comparator() {
            public int compare(Object o1, Object o2) {
               return String.CASE_INSENSITIVE_ORDER.compare(((CmsVfsStructureEntry)o1).getResourcePath(), ((CmsVfsStructureEntry)o2).getResourcePath());
            }
         });
      }

      return ret;
   }

   private String Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(CmsUUID projectId, CmsVfsStructureEntry strEntry) {
      CmsResourceTranslator translator = this.getCms().getRequestContext().getFileTranslator();
      String translatedName = translator.translateResource(strEntry.getResourcePath());
      CmsDbContext dbc = null;
      Connection conn = null;
      PreparedStatement stmt = null;

      try {
         dbc = this.getDbContext();
         conn = this.getSqlManager().getConnection(dbc);
         stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "UPDATE_RESOURCE_PATH");
         stmt.setString(1, translatedName);
         stmt.setString(2, strEntry.getStructureId());
         stmt.executeUpdate();
      } catch (SQLException var12) {
         this.writeError(var12);
      } finally {
         this.getSqlManager().closeAll(dbc, conn, stmt, (ResultSet)null);
         if (dbc != null) {
            dbc.clear();
         }

      }

      return translatedName;
   }
}
