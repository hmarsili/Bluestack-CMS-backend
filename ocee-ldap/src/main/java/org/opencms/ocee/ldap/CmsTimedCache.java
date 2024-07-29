package org.opencms.ocee.ldap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

public class CmsTimedCache implements I_CmsEventListener {
    private long f147x226a583a;
    protected Map<CmsTimedCacheKey, CmsTimedCacheEntry> f148xbb78c9c2 = new HashMap();

    public static class CmsTimedCacheEntry {
        private List<?> f142x226a583a;
        private long f143xbb78c9c2 = System.currentTimeMillis();

        public CmsTimedCacheEntry(List<?> cacheData) {
            this.f142x226a583a = cacheData;
        }

        public List<?> getCacheData() {
            return this.f142x226a583a;
        }

        public long getTime() {
            return this.f143xbb78c9c2;
        }
    }

    public static final class CmsTimedCacheKey {
        public static final CmsTimedCacheKey KEY_ALL_USERS = new CmsTimedCacheKey(CmsTimedCacheType.ALL_USERS, "");
        private String f144x226a583a;
        private CmsTimedCacheType f145xbb78c9c2;

        private CmsTimedCacheKey(CmsTimedCacheType type, String name) {
            this.f144x226a583a = name;
            this.f145xbb78c9c2 = type;
        }

        public static CmsTimedCacheKey groupsOfOu(String ouName) {
            String name = ouName;
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
                name = "/";
            }
            return new CmsTimedCacheKey(CmsTimedCacheType.GROUPS_OF_OU, name);
        }

        public static CmsTimedCacheKey groupsOfUser(String userName) {
            return new CmsTimedCacheKey(CmsTimedCacheType.GROUPS_OF_USER, userName);
        }

        public static CmsTimedCacheKey usersOfGroup(String groupName) {
            return new CmsTimedCacheKey(CmsTimedCacheType.USERS_OF_GROUP, groupName);
        }

        public static CmsTimedCacheKey usersOfOu(String ouName) {
            String name = ouName;
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
                name = "/";
            }
            return new CmsTimedCacheKey(CmsTimedCacheType.USERS_OF_OU, name);
        }

        public static CmsTimedCacheKey valueOf(String value) {
            if (value == null) {
                return null;
            }
            CmsTimedCacheType[] arr$ = CmsTimedCacheType.values();
            int len$ = arr$.length;
            int i$ = 0;
            while (i$ < len$) {
                CmsTimedCacheType type = arr$[i$];
                if (!value.startsWith(type.name())) {
                    i$++;
                } else if (type.equals(CmsTimedCacheType.ALL_USERS)) {
                    return KEY_ALL_USERS;
                } else {
                    return new CmsTimedCacheKey(type, value.substring(type.name().length()));
                }
            }
            return null;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CmsTimedCacheKey)) {
                return false;
            }
            CmsTimedCacheKey that = (CmsTimedCacheKey) obj;
            if (this.f145xbb78c9c2 == that.f145xbb78c9c2 && this.f144x226a583a.equals(that.f144x226a583a)) {
                return true;
            }
            return false;
        }

        public String getName() {
            return this.f144x226a583a;
        }

        public CmsTimedCacheType getType() {
            return this.f145xbb78c9c2;
        }

        public int hashCode() {
            return (this.f145xbb78c9c2.ordinal() * 17) + (this.f144x226a583a.hashCode() * 3);
        }

        public String toString() {
            return this.f145xbb78c9c2.name() + this.f144x226a583a;
        }
    }

    public enum CmsTimedCacheType {
        ALL_USERS,
        GROUPS_OF_OU,
        GROUPS_OF_USER,
        USERS_OF_GROUP,
        USERS_OF_OU
    }

    protected CmsTimedCache(long timeout) {
        this.f147x226a583a = timeout;
        OpenCms.addCmsEventListener(this, new int[]{5});
    }

    public void cmsEvent(CmsEvent event) {
        this.f148xbb78c9c2.clear();
    }

    public void flush() {
        this.f148xbb78c9c2.clear();
    }

    public List<?> get(CmsTimedCacheKey cacheKey) {
        if (this.f147x226a583a > 0) {
            CmsTimedCacheEntry cache = (CmsTimedCacheEntry) this.f148xbb78c9c2.get(cacheKey);
            if (cache != null) {
                if (System.currentTimeMillis() - cache.getTime() < this.f147x226a583a) {
                    return cache.getCacheData();
                }
                this.f148xbb78c9c2.remove(cacheKey);
            }
        }
        return null;
    }

    public Map<CmsTimedCacheKey, CmsTimedCacheEntry> getData() {
        return new HashMap(this.f148xbb78c9c2);
    }

    public long getTimeout() {
        return this.f147x226a583a;
    }

    public void put(CmsTimedCacheKey key, List<?> data) {
        if (this.f147x226a583a > 0) {
            this.f148xbb78c9c2.put(key, new CmsTimedCacheEntry(data));
        }
    }

    public void remove(CmsTimedCacheKey cacheKey) {
        this.f148xbb78c9c2.remove(cacheKey);
    }
}
