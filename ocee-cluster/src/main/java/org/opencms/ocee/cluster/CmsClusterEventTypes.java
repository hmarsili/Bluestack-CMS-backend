package org.opencms.ocee.cluster;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsIllegalArgumentException;

public final class CmsClusterEventTypes {
   public static final CmsClusterEventTypes CHECK_SOURCE = new CmsClusterEventTypes(2006, Messages.get().container("GUI_EVENT_TYPE_CHECK_SOURCE_0"));
   public static final CmsClusterEventTypes CLEAR_CACHES = new CmsClusterEventTypes(2030, Messages.get().container("GUI_EVENT_TYPE_CLEAR_CACHES_0"));
   public static final CmsClusterEventTypes CLEAR_PRINCIPAL_CACHES = new CmsClusterEventTypes(2031, Messages.get().container("GUI_EVENT_TYPE_CLEAR_PRINCIPAL_CACHES_0"));
   public static final CmsClusterEventTypes FLEX_CLEAR_CACHES = new CmsClusterEventTypes(2033, Messages.get().container("GUI_EVENT_TYPE_FLEX_CLEAR_CACHES_0"));
   public static final CmsClusterEventTypes FLEX_PURGE_JSP_REPOSITORY = new CmsClusterEventTypes(2034, Messages.get().container("GUI_EVENT_TYPE_FLEX_PURGE_JSP_REPOSITORY_0"));
   public static final int EVENT_REPLICATION_END = 2132;
   public static final int EVENT_REPLICATION_START = 2131;
   public static final CmsClusterEventTypes FULLSTATIC_EXPORT = new CmsClusterEventTypes(2024, Messages.get().container("GUI_EVENT_TYPE_UPDATE_EXPORTS_0"));
   public static final CmsClusterEventTypes GET_CONFIG_FILE = new CmsClusterEventTypes(2021, Messages.get().container("GUI_EVENT_TYPE_GET_CONFIG_FILE_0"));
   public static final CmsClusterEventTypes GET_INSTANCE_INFO = new CmsClusterEventTypes(2026, Messages.get().container("GUI_EVENT_TYPE_GET_INSTANCE_INFO_0"));
   public static final CmsClusterEventTypes GET_LICENSE_INFO = new CmsClusterEventTypes(2022, Messages.get().container("GUI_EVENT_TYPE_GET_LICENSE_INFO_0"));
   public static final CmsClusterEventTypes HOOK = new CmsClusterEventTypes(2007, Messages.get().container("GUI_EVENT_TYPE_HOOK_0"));
   public static final String KEY_DESTINATION_SERVER = "destinationServer";
   public static final String KEY_ORIGIN_SERVER = "originServer";
   public static final CmsClusterEventTypes PUBLISH_PROJECT = new CmsClusterEventTypes(2020, Messages.get().container("GUI_EVENT_TYPE_PUBLISH_PROJECT_0"));
   public static final CmsClusterEventTypes REBUILD_SEARCHINDEX = new CmsClusterEventTypes(2032, Messages.get().container("GUI_EVENT_TYPE_REBUILD_SEARCHINDEX_0"));
   public static final CmsClusterEventTypes SET_WP_SERVER = new CmsClusterEventTypes(2023, Messages.get().container("GUI_EVENT_TYPE_SET_WP_SERVER_0"));
   public static final CmsClusterEventTypes UNCACHE_USER = new CmsClusterEventTypes(2035, Messages.get().container("GUI_EVENT_TYPE_UNCACHE_USER_0"));
   public static final CmsClusterEventTypes UNCACHE_OU = new CmsClusterEventTypes(2036, Messages.get().container("GUI_EVENT_TYPE_UNCACHE_OU_0"));
   public static final CmsClusterEventTypes UNCACHE_GROUP = new CmsClusterEventTypes(2037, Messages.get().container("GUI_EVENT_TYPE_UNCACHE_GROUP_0"));
   public static final CmsClusterEventTypes UPDATE_EXPORTS = new CmsClusterEventTypes(2025, Messages.get().container("GUI_EVENT_TYPE_UPDATE_EXPORTS_0"));
   private static final CmsClusterEventTypes[] C_VALUES;
   public static final List VALUES;
   private final CmsMessageContainer m_name;
   private final int m_type;

   private CmsClusterEventTypes(int type, CmsMessageContainer name) {
      this.m_type = type;
      this.m_name = name;
   }

   public static CmsClusterEventTypes valueOf(int value) {
      Iterator iter = VALUES.iterator();

      CmsClusterEventTypes type;
      do {
         if (!iter.hasNext()) {
            throw new CmsIllegalArgumentException(Messages.get().container("ERR_CLUSTER_EVENT_TYPE_PARSE_1", new Integer(value)));
         }

         type = (CmsClusterEventTypes)iter.next();
      } while(value != type.getType());

      return type;
   }

   public CmsMessageContainer getName() {
      return this.m_name;
   }

   public int getType() {
      return this.m_type;
   }

   public String toString() {
      return this.m_name.key();
   }

   static {
      C_VALUES = new CmsClusterEventTypes[]{PUBLISH_PROJECT, CHECK_SOURCE, CLEAR_CACHES, CLEAR_PRINCIPAL_CACHES, FLEX_CLEAR_CACHES, FLEX_PURGE_JSP_REPOSITORY, FULLSTATIC_EXPORT, GET_CONFIG_FILE, GET_INSTANCE_INFO, GET_LICENSE_INFO, HOOK, UPDATE_EXPORTS, SET_WP_SERVER, REBUILD_SEARCHINDEX, UNCACHE_USER, UNCACHE_OU, UNCACHE_GROUP};
      VALUES = Collections.unmodifiableList(Arrays.asList(C_VALUES));
   }
}
