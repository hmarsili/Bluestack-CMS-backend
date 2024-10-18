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
import org.opencms.util.CmsUUID;

public class CmsVfsDuplicateUserPlugin extends A_CmsVfsDoctorPlugin {
   private static final String ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper = "DELETE_USER_BY_NAME";
   private static final String oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000voidsuper = "DELETE_USER_IN_ACE";
   private static final String öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper = "DELETE_USER_IN_GROUP";
   private static final String Òo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000nullsuper = "REPLACE_USER_FOR_PROJECT";
   private static final String Ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000ifsuper = "REPLACE_USER_FOR_RESOURCE_CREATE";
   private static final String Óo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000intsuper = "REPLACE_USER_FOR_RESOURCE_LASTMOD";
   private static final String øO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000classsuper = "REPLACE_USER_IN_ACE";
   private static final String Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper = "REPLACE_USER_IN_GROUP";
   private static final String õO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectsuper = "SELECT_ALL_GROUPS_FOR_USER";
   private static final String ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper = "SELECT_ALL_RESOURCES_FOR_USER";
   private static final String ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper = "SELECT_ALL_USERS";

   public CmsVfsDuplicateUserPlugin() {
      super(true, true);
   }

   public CmsMessageContainer getDescription() {
      return Messages.get().container("GUI_OCEE_VFSDOC_PLUGIN_DUPLICATEUSER_DESC_0");
   }

   public String getName() {
      return "duplicateuser";
   }

   public CmsMessageContainer getNiceName() {
      return Messages.get().container("GUI_OCEE_VFSDOC_PLUGIN_DUPLICATEUSER_NAME_0");
   }

   protected int recover(CmsUUID projectId) {
      CmsMessages msg = Messages.get().getBundle(this.getReport().getLocale());
      Map users = this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
      Iterator itUsers = users.keySet().iterator();

      while(itUsers.hasNext()) {
         String key = (String)itUsers.next();
         List userNames = (List)users.get(key);
         Iterator itNames = userNames.iterator();
         Map masterUser = null;
         List masterGroups = new ArrayList();
         ArrayList masterResources = new ArrayList();

         while(itNames.hasNext()) {
            Map user = (Map)itNames.next();
            CmsDbContext dbc = null;
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
               dbc = this.getDbContext();
               conn = this.getSqlManager().getConnection(dbc);
               if (masterUser == null) {
                  masterUser = user;
                  masterGroups.clear();
                  stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "SELECT_ALL_GROUPS_FOR_USER");
                  stmt.setString(1, (String)user.get("USER_ID"));
                  rs = stmt.executeQuery();

                  while(rs.next()) {
                     masterGroups.add(rs.getString(1));
                  }

                  this.getSqlManager().closeAll(dbc, (Connection)null, stmt, rs);
                  masterResources.clear();
                  stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "SELECT_ALL_RESOURCES_FOR_USER");
                  stmt.setString(1, (String)user.get("USER_ID"));
                  rs = stmt.executeQuery();

                  while(rs.next()) {
                     masterResources.add(rs.getString(1));
                  }

                  this.getSqlManager().closeAll(dbc, (Connection)null, stmt, rs);
               } else {
                  List userGroups = new ArrayList();
                  ArrayList userResources = new ArrayList();

                  try {
                     userGroups.clear();
                     stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "SELECT_ALL_GROUPS_FOR_USER");
                     stmt.setString(1, (String)user.get("USER_ID"));
                     ResultSet res = stmt.executeQuery();

                     while(res.next()) {
                        userGroups.add(res.getString(1));
                     }
                  } catch (Exception var290) {
                     this.writeError(var290);
                  } finally {
                     this.getSqlManager().closeAll(dbc, (Connection)null, stmt, rs);
                  }

                  try {
                     userResources.clear();
                     stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "SELECT_ALL_RESOURCES_FOR_USER");
                     stmt.setString(1, (String)user.get("USER_ID"));
                     rs = stmt.executeQuery();

                     while(rs.next()) {
                        userResources.add(rs.getString(1));
                     }
                  } catch (Exception var288) {
                     this.writeError(var288);
                  } finally {
                     this.getSqlManager().closeAll(dbc, (Connection)null, stmt, rs);
                  }

                  int res;
                  if (projectId.equals(onlineProjectId)) {
                     try {
                        stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "DELETE_USER_BY_NAME");
                        stmt.setString(1, (String)user.get("USER_NAME"));
                        stmt.setString(2, (String)user.get("USER_OU"));
                        res = stmt.executeUpdate();
                        if (res > 0) {
                           this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_DUPLICATEUSER_DELETE_USER_2", new Object[]{user.get("USER_NAME"), user.get("USER_OU")}));
                        }
                     } catch (Exception var286) {
                        this.writeError(var286);
                     } finally {
                        this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
                     }
                  }

                  try {
                     stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "REPLACE_USER_FOR_PROJECT");
                     stmt.setString(1, (String)masterUser.get("USER_ID"));
                     stmt.setString(2, (String)user.get("USER_ID"));
                     res = stmt.executeUpdate();
                     if (res > 0) {
                        this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_DUPLICATEUSER_REPLACE_PROJ_2", new Object[]{new Integer(res), user.get("USER_NAME")}));
                     }
                  } catch (Exception var284) {
                     this.writeError(var284);
                  } finally {
                     this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
                  }

                  try {
                     stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "REPLACE_USER_FOR_RESOURCE_CREATE");
                     stmt.setString(1, (String)masterUser.get("USER_ID"));
                     stmt.setString(2, (String)user.get("USER_ID"));
                     res = stmt.executeUpdate();
                     if (res > 0) {
                        this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_DUPLICATEUSER_REPLACE_CREATE_2", new Object[]{new Integer(res), user.get("USER_NAME")}));
                     }
                  } catch (Exception var282) {
                     this.writeError(var282);
                  } finally {
                     this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
                  }

                  try {
                     stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "REPLACE_USER_FOR_RESOURCE_LASTMOD");
                     stmt.setString(1, (String)masterUser.get("USER_ID"));
                     stmt.setString(2, (String)user.get("USER_ID"));
                     res = stmt.executeUpdate();
                     if (res > 0) {
                        this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_DUPLICATEUSER_REPLACE_LASTMOD_2", new Object[]{new Integer(res), user.get("USER_NAME")}));
                     }
                  } catch (Exception var280) {
                     this.writeError(var280);
                  } finally {
                     this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
                  }

                  Iterator itGroups = userGroups.iterator();

                  while(itGroups.hasNext()) {
                     String groupId = (String)itGroups.next();
                     
                     if (masterGroups.contains(groupId)) {
                        try {
                           stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "DELETE_USER_IN_GROUP");
                           stmt.setString(1, groupId);
                           stmt.setString(2, (String)user.get("USER_ID"));
                           res = stmt.executeUpdate();
                           if (res > 0) {
                              this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_DUPLICATEUSER_DEL_USERGROUP_2", new Object[]{groupId, user.get("USER_NAME")}));
                           }
                        } catch (Exception var278) {
                           this.writeError(var278);
                        } finally {
                           this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
                        }
                     } else {
                        try {
                           stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "REPLACE_USER_IN_GROUP");
                           stmt.setString(1, (String)masterUser.get("USER_ID"));
                           stmt.setString(2, groupId);
                           stmt.setString(3, (String)user.get("USER_ID"));
                           res = stmt.executeUpdate();
                           if (res > 0) {
                              this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_DUPLICATEUSER_REP_USERGROUP_2", new Object[]{groupId, user.get("USER_NAME")}));
                           }
                        } catch (Exception var276) {
                           this.writeError(var276);
                        } finally {
                           this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
                        }
                     }
                  }

                  Iterator itResources = userResources.iterator();

                  while(itResources.hasNext()) {
                     String resourceId = (String)itResources.next();
                     if (masterGroups.contains(resourceId)) {
                        try {
                           stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "DELETE_USER_IN_ACE");
                           stmt.setString(1, resourceId);
                           stmt.setString(2, (String)user.get("USER_ID"));
                           res = stmt.executeUpdate();
                           if (res > 0) {
                              this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_DUPLICATEUSER_DEL_USERACE_2", new Object[]{resourceId, user.get("USER_NAME")}));
                           }
                        } catch (Exception var274) {
                           this.writeError(var274);
                        } finally {
                           this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
                        }
                     } else {
                        try {
                           stmt = this.getSqlManager().getPreparedStatement(conn, projectId, "REPLACE_USER_IN_ACE");
                           stmt.setString(1, (String)masterUser.get("USER_ID"));
                           stmt.setString(2, resourceId);
                           stmt.setString(3, (String)user.get("USER_ID"));
                           res = stmt.executeUpdate();
                           if (res > 0) {
                              this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_DUPLICATEUSER_REP_USERACE_2", new Object[]{resourceId, user.get("USER_NAME")}));
                           }
                        } catch (Exception var272) {
                           this.writeError(var272);
                        } finally {
                           this.getSqlManager().closeAll(dbc, (Connection)null, stmt, (ResultSet)null);
                        }
                     }
                  }
               }
            } catch (Exception var292) {
               this.writeError(var292);
            } finally {
               this.getSqlManager().closeAll(dbc, conn, stmt, rs);
               if (dbc != null) {
                  dbc.clear();
               }

            }
         }
      }

      if (projectId.equals(ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private)) {
         this.recover(onlineProjectId);
      }

      return users.size();
   }

   public int recover() {
      CmsMessages msg = org.opencms.ocee.vfsdoctor.Messages.get().getBundle(this.getReport().getLocale());
      this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_RECOVER_OFFLINE_0"));
      int offline = this.recover(ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private);
      if (offline == 0) {
         this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_NOTHING_TORECOVER_0"));
      }

      return offline;
   }

   public int validate() {
      CmsMessages msg = org.opencms.ocee.vfsdoctor.Messages.get().getBundle(this.getReport().getLocale());
      this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_VALIDATE_OFFLINE_0"));
      return this.validate(ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private);
   }

   protected int validate(CmsUUID projectId) {
      CmsMessages msg = Messages.get().getBundle(this.getReport().getLocale());
      Map users = this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
      Iterator itUsers = users.keySet().iterator();

      while(itUsers.hasNext()) {
         String key = (String)itUsers.next();
         List userNames = (List)users.get(key);
         this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_DUPLICATEUSER_VAL_DETAIL_3", new Object[]{key, new Integer(userNames.size()), userNames}));
      }

      this.writeInfo(msg.key("GUI_OCEE_VFSDOC_PLUGIN_DUPLICATEUSER_VAL_OVERVIEW_1", new Object[]{new Integer(users.size())}));
      return users.size();
   }

   private Map o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super() {
      Map ret = new HashMap();
      CmsDbContext dbc = null;
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet res = null;

      String key;
      try {
         dbc = this.getDbContext();
         conn = this.getSqlManager().getConnection(dbc);
         stmt = this.getSqlManager().getPreparedStatement(conn, "SELECT_ALL_USERS");
         res = stmt.executeQuery();

         while(res.next()) {
            String userId = res.getString("USER_ID");
            key = res.getString("USER_OU");
            String userName = res.getString("USER_NAME");
            key = key.toUpperCase() + userName.toUpperCase();
            List users = (List)ret.get(key);
            if (users == null) {
               users = new ArrayList();
               ret.put(key, users);
            }

            Map user = new HashMap();
            user.put("USER_ID", userId);
            user.put("USER_OU", key);
            user.put("USER_NAME", userName);
            ((List)users).add(user);
         }
      } catch (SQLException var15) {
         this.writeError(var15);
      } finally {
         this.getSqlManager().closeAll(dbc, conn, stmt, res);
         if (dbc != null) {
            dbc.clear();
         }

      }

      Iterator itUsers = (new ArrayList(ret.keySet())).iterator();

      while(true) {
         List value;
         do {
            if (!itUsers.hasNext()) {
               return ret;
            }

            key = (String)itUsers.next();
            value = (List)ret.get(key);
         } while(value != null && value.size() >= 2);

         ret.remove(key);
      }
   }
}
