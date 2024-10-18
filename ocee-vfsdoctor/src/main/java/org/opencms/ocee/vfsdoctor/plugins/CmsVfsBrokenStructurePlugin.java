package org.opencms.ocee.vfsdoctor.plugins;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.opencms.db.CmsDbContext;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsMessages;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ocee.vfsdoctor.A_CmsVfsDoctorPlugin;
import org.opencms.ocee.vfsdoctor.CmsVfsStructureEntry;
import org.opencms.util.CmsUUID;

public class CmsVfsBrokenStructurePlugin extends A_CmsVfsDoctorPlugin {
   private static final String Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper = "SELECT_ALL_STRUCTURES";
   private static final String Õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000floatsuper = "SELECT_STRUCTURE";
   private static final String Öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000forsuper = "UPDATE_PATH";
   private static final String öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000privatesuper = "UPDATE_PARENT_ID";
   private Map ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000whilesuper;
   private Map õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000publicsuper;

   public CmsVfsBrokenStructurePlugin() {
      super(true, true);
   }

   public CmsMessageContainer getDescription() {
      return Messages.get().container("GUI_OCEE_VFSDOC_PLUGIN_BROKENSTRUCTURE_DESC_0");
   }

   public String getName() {
      return "brokenstructure";
   }

   public CmsMessageContainer getNiceName() {
      return Messages.get().container("GUI_OCEE_VFSDOC_PLUGIN_BROKENSTRUCTURE_NAME_0");
   }

   protected int recover(CmsUUID projectId) {
      CmsObject offlineCms = this.getCms();

      try {
         offlineCms = OpenCms.initCmsObject(offlineCms);
         offlineCms.getRequestContext().setCurrentProject(offlineCms.readProject("Offline"));
         offlineCms.getRequestContext().setSiteRoot("/");
      } catch (CmsException var16) {
         this.writeError(var16);
      }

      CmsMessages msg = Messages.get().getBundle(this.getReport().getLocale());
      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(projectId);
      Map brokenIdStructure = this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(this.ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000whilesuper);
      int lastsize = 0;
      int sizeId = brokenIdStructure.size();

      try {
         Iterator it;
         CmsVfsStructureEntry strEntry;
         label181:
         while(brokenIdStructure.size() > 0 && brokenIdStructure.size() != lastsize) {
            lastsize = brokenIdStructure.size();
            it = brokenIdStructure.values().iterator();

            while(true) {
               while(true) {
                  do {
                     if (!it.hasNext()) {
                        continue label181;
                     }

                     strEntry = (CmsVfsStructureEntry)it.next();
                  } while(brokenIdStructure.containsKey(strEntry.getParentId()));

                  if (this.ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000whilesuper.containsKey(strEntry.getParentId())) {
                     it.remove();
                  } else {
                     String parentId = null;
                     String parent = CmsResource.getParentFolder(strEntry.getResourcePath());
                     if (parent != null && parent.length() > 1) {
                        parent = parent.substring(0, parent.length() - 1);
                     }

                     if (this.õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000publicsuper.containsKey(parent)) {
                        parentId = ((CmsVfsStructureEntry)this.õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000publicsuper.get(parent)).getStructureId();
                     }

                     CmsResource offlineResource;
                     if (parentId == null && !projectId.equals(onlineProjectId)) {
                        try {
                           offlineResource = offlineCms.readResource(new CmsUUID(strEntry.getStructureId()), CmsResourceFilter.ALL);
                           if (!offlineResource.getState().isNew()) {
                              CmsVfsStructureEntry onlineEntry = null;

                              try {
                                 onlineEntry = this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(onlineProjectId, strEntry.getStructureId());
                              } catch (SQLException var14) {
                              }

                              if (onlineEntry != null && this.ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000whilesuper.containsKey(onlineEntry.getParentId())) {
                                 parentId = onlineEntry.getParentId();
                              }
                           }
                        } catch (CmsException var15) {
                        }
                     }

                     CmsLock lock;
                     if (parentId != null) {
                        if (!brokenIdStructure.containsKey(parentId) && this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(projectId, strEntry, parentId)) {
                           if (!projectId.equals(onlineProjectId)) {
                              offlineResource = offlineCms.readResource(new CmsUUID(strEntry.getStructureId()), CmsResourceFilter.ALL);
                              if (offlineResource.getState().isUnchanged()) {
                                 offlineResource.setState(CmsResource.STATE_CHANGED);
                                 lock = offlineCms.getLock(offlineResource);
                                 if (lock.isNullLock()) {
                                    offlineCms.lockResource(strEntry.getResourcePath());
                                 } else if (!lock.isExclusiveOwnedBy(offlineCms.getRequestContext().currentUser()) || !lock.isInProject(offlineCms.getRequestContext().currentProject())) {
                                    offlineCms.changeLock(strEntry.getResourcePath());
                                 }

                                 offlineCms.writeResource(offlineResource);
                                 offlineCms.unlockResource(strEntry.getResourcePath());
                              }
                           }

                           CmsVfsStructureEntry recoveredEntry = new CmsVfsStructureEntry(strEntry.getStructureId(), parentId, strEntry.getResourceId(), strEntry.getResourcePath());
                           this.ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000whilesuper.remove(strEntry.getStructureId());
                           this.ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000whilesuper.put(recoveredEntry.getStructureId(), recoveredEntry);
                           this.õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000publicsuper.remove(strEntry.getResourcePath());
                           this.õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000publicsuper.put(recoveredEntry.getResourcePath(), recoveredEntry);
                           it.remove();
                           this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_BROKENSTRUCTURE_REC1_2", new Object[]{recoveredEntry.getResourcePath(), recoveredEntry.getParentId()}));
                        }
                     } else {
                        if (projectId.equals(onlineProjectId)) {
                           this.deleteStructureEntry(strEntry, projectId);
                        } else {
                           offlineResource = offlineCms.readResource(new CmsUUID(strEntry.getStructureId()), CmsResourceFilter.ALL);
                           lock = offlineCms.getLock(offlineResource);
                           if (lock.isNullLock()) {
                              offlineCms.lockResource(strEntry.getResourcePath());
                           } else if (!lock.isExclusiveOwnedBy(offlineCms.getRequestContext().currentUser()) || !lock.isInProject(offlineCms.getRequestContext().currentProject())) {
                              offlineCms.changeLock(strEntry.getResourcePath());
                           }

                           offlineCms.moveToLostAndFound(strEntry.getResourcePath());
                        }

                        this.õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000publicsuper.remove(strEntry.getResourcePath());
                        this.ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000whilesuper.remove(strEntry.getStructureId());
                        it.remove();
                     }
                  }
               }
            }
         }

         it = brokenIdStructure.values().iterator();

         while(it.hasNext()) {
            strEntry = (CmsVfsStructureEntry)it.next();
            if (projectId.equals(onlineProjectId)) {
               this.deleteStructureEntry(strEntry, projectId);
            } else {
               CmsResource offlineResource = offlineCms.readResource(new CmsUUID(strEntry.getStructureId()), CmsResourceFilter.ALL);
               CmsLock lock = offlineCms.getLock(offlineResource);
               if (lock.isNullLock()) {
                  offlineCms.lockResource(strEntry.getResourcePath());
               } else if (!lock.isExclusiveOwnedBy(offlineCms.getRequestContext().currentUser()) || !lock.isInProject(offlineCms.getRequestContext().currentProject())) {
                  offlineCms.changeLock(strEntry.getResourcePath());
               }

               offlineCms.moveToLostAndFound(strEntry.getResourcePath());
            }

            this.õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000publicsuper.remove(strEntry.getResourcePath());
            this.ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000whilesuper.remove(strEntry.getStructureId());
            it.remove();
         }
      } catch (Exception var18) {
         this.writeError(var18);
      }

      int sizePath = 0;
      if (!projectId.equals(CmsProject.ONLINE_PROJECT_ID)) {
         Map brokenPathStructure = this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(projectId);
         sizePath = brokenPathStructure.size();
         lastsize = 0;

         while(brokenPathStructure.size() > 0 && brokenPathStructure.size() != lastsize) {
            lastsize = brokenPathStructure.size();

            try {
               Iterator it = brokenPathStructure.values().iterator();

               while(it.hasNext()) {
                  CmsVfsStructureEntry strEntry = (CmsVfsStructureEntry)it.next();
                  if (!brokenPathStructure.containsKey(strEntry.getParentId())) {
                     this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(projectId, strEntry);
                     it.remove();
                  }
               }
            } catch (Exception var17) {
               this.writeError(var17);
               break;
            }
         }
      }

      return sizeId + sizePath;
   }

   protected int validate(CmsUUID projectId) {
      CmsMessages msg = Messages.get().getBundle(this.getReport().getLocale());
      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(projectId);
      Map brokenIdStructure = this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(this.ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000whilesuper);
      Iterator it = brokenIdStructure.values().iterator();

      while(it.hasNext()) {
         CmsVfsStructureEntry strEntry = (CmsVfsStructureEntry)it.next();
         this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_BROKENSTRUCTURE_VAL1_2", new Object[]{strEntry.getResourcePath(), strEntry.getParentId()}));
      }

      this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_BROKENSTRUCTURE_VAL2_1", new Object[]{new Integer(brokenIdStructure.size())}));
      Map brokenPathStructure = this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(projectId);
      it = brokenPathStructure.keySet().iterator();

      while(it.hasNext()) {
         CmsVfsStructureEntry strEntry = (CmsVfsStructureEntry)brokenPathStructure.get(it.next());
         this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_PARENTSTRUCTURE_VAL1_1", new Object[]{strEntry.getResourcePath()}));
      }

      this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_PARENTSTRUCTURE_VAL2_1", new Object[]{new Integer(brokenPathStructure.size())}));
      return brokenIdStructure.size() + brokenPathStructure.size();
   }

   private void o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(CmsUUID projectId) {
      this.ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000whilesuper = new HashMap();
      this.õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000publicsuper = new HashMap();
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
            this.ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000whilesuper.put(strEntry.getStructureId(), strEntry);
            this.õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000publicsuper.put(strEntry.getResourcePath(), strEntry);
         }
      } catch (SQLException var10) {
         this.writeError(var10);
      } finally {
         this.getSqlManager().closeAll(dbc, conn, stmt, res);
         if (dbc != null) {
            dbc.clear();
         }

      }

   }

   private boolean o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(Map brokenEntries, Map strEntries) {
      boolean ret = false;
      Iterator iter = strEntries.values().iterator();

      while(iter.hasNext()) {
         CmsVfsStructureEntry strEntry = (CmsVfsStructureEntry)iter.next();
         if (!brokenEntries.containsValue(strEntry)) {
            String parent = CmsResource.getParentFolder(strEntry.getResourcePath());
            if (parent != null && parent.length() > 1) {
               parent = parent.substring(0, parent.length() - 1);
            }

            CmsVfsStructureEntry entry = (CmsVfsStructureEntry)strEntries.get(parent);
            if (entry == null) {
               if (!strEntry.getResourcePath().equals("/")) {
                  brokenEntries.put(strEntry.getStructureId(), strEntry);
                  ret = true;
               }
            } else if (brokenEntries.containsValue(entry)) {
               brokenEntries.put(strEntry.getStructureId(), strEntry);
               ret = true;
            } else if (!entry.getStructureId().equals(strEntry.getParentId())) {
               brokenEntries.put(strEntry.getStructureId(), strEntry);
               ret = true;
            }
         }
      }

      return ret;
   }

   private Map Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(CmsUUID projectId) {
      HashMap ret = new HashMap();

      while(this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super((Map)ret, (Map)this.õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000publicsuper)) {
      }

      return ret;
   }

   private void o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(CmsUUID projectId, CmsVfsStructureEntry strEntry) {
      CmsDbContext dbc = null;
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet res = null;
      CmsVfsStructureEntry parentEntry = null;

      try {
         parentEntry = this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(projectId, strEntry.getParentId());
         String rightPath = parentEntry.getResourcePath() + "/" + CmsResource.getName(strEntry.getResourcePath());
         dbc = this.getDbContext();
         conn = this.getSqlManager().getConnection(dbc);
         stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "UPDATE_PATH");
         stmt.setString(1, rightPath);
         stmt.setString(2, strEntry.getStructureId().toString());
         stmt.executeUpdate();
         this.writeInfo(Messages.get().getBundle(this.getReport().getLocale()).key("GUI_OCEE_VFSDOC_PLUGIN_PARENTSTRUCTURE_REC2_2", new Object[]{strEntry.getResourcePath(), rightPath}));
      } catch (SQLException var12) {
         this.writeError(var12);
      } finally {
         this.getSqlManager().closeAll(dbc, conn, stmt, (ResultSet)res);
         if (dbc != null) {
            dbc.clear();
         }

      }

   }

   private Map o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(Map entries) {
      Map structure = (Map)((HashMap)entries).clone();
      Map tree = new HashMap();
      int lastSize = 0;

      label33:
      while(structure.size() > 0 && structure.size() != lastSize) {
         lastSize = structure.size();
         Iterator it = structure.values().iterator();

         while(true) {
            CmsVfsStructureEntry entry;
            do {
               if (!it.hasNext()) {
                  continue label33;
               }

               entry = (CmsVfsStructureEntry)it.next();
            } while(!tree.containsKey(entry.getParentId()) && (tree.size() != 0 || !entry.getResourcePath().equals("/")));

            tree.put(entry.getStructureId(), entry);
            it.remove();
         }
      }

      return structure;
   }

   private boolean o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(CmsUUID projectId, CmsVfsStructureEntry entry, String parentId) {
      CmsDbContext dbc = null;
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet res = null;
      boolean marker = false;

      try {
         dbc = this.getDbContext();
         conn = this.getSqlManager().getConnection(dbc);
         stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "UPDATE_PARENT_ID");
         stmt.setString(1, parentId);
         stmt.setString(2, entry.getStructureId().toString());
         stmt.executeUpdate();
         marker = true;
      } catch (SQLException var13) {
         this.writeError(var13);
      } finally {
         this.getSqlManager().closeAll(dbc, conn, stmt, (ResultSet)res);
         if (dbc != null) {
            dbc.clear();
         }

      }

      return marker;
   }

   private CmsVfsStructureEntry o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(CmsUUID projectId, String structureId) throws SQLException {
      CmsDbContext dbc = null;
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet res = null;
      CmsVfsStructureEntry entry = null;

      try {
         dbc = this.getDbContext();
         conn = this.getSqlManager().getConnection(dbc);
         stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "SELECT_STRUCTURE");
         stmt.setString(1, structureId);
         res = stmt.executeQuery();
         res.next();
         entry = new CmsVfsStructureEntry(res.getString(1), res.getString(2), res.getString(3), res.getString(4));
      } catch (SQLException var12) {
         throw var12;
      } finally {
         this.getSqlManager().closeAll(dbc, conn, stmt, res);
         if (dbc != null) {
            dbc.clear();
         }

      }

      return entry;
   }
}
