package org.opencms.ocee.ldap;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

public class CmsLdapGroupMemberUrl {
    private static final Log f92x59c4920d = CmsLog.getLog(CmsLdapGroupMemberUrl.class);
    private final int f93x226a583a;
    private final String f94xbb78c9c2;
    private final String f95x350cdc9c;

    public CmsLdapGroupMemberUrl(String memberUrl) {
        if (f92x59c4920d.isDebugEnabled()) {
            f92x59c4920d.debug("new group url: " + memberUrl);
        }
        String root = memberUrl;
        String scope = "base";
        String filter = "(objectclass=*)";
        int pos = memberUrl.indexOf("??");
        if (pos > 0) {
            root = memberUrl.substring(0, pos);
            int pos2 = memberUrl.indexOf("?", pos + 2);
            if (pos2 > 0) {
                scope = memberUrl.substring(pos + 2, pos2);
                filter = memberUrl.substring(pos2 + 1);
            } else {
                scope = memberUrl.substring(pos + 2);
            }
        }
        pos = root.indexOf(":///");
        if (pos > -1) {
            root = root.substring(":///".length() + pos);
        }
        int scp = 2;
        if ("one".equals(scope)) {
            scp = 1;
        } else if ("base".equals(scope)) {
            scp = 0;
        }
        this.f94xbb78c9c2 = root;
        this.f93x226a583a = scp;
        this.f95x350cdc9c = filter;
        if (f92x59c4920d.isDebugEnabled()) {
            f92x59c4920d.debug("group url root: " + root);
            f92x59c4920d.debug("group url scope: " + scope);
            f92x59c4920d.debug("group url filter: " + filter);
        }
    }

    public CmsLdapGroupMemberUrl(String root, int scope, String filter) {
        this.f94xbb78c9c2 = root;
        this.f93x226a583a = scope;
        this.f95x350cdc9c = filter;
    }

    public String getFilter() {
        return this.f95x350cdc9c;
    }

    public String getRoot() {
        return this.f94xbb78c9c2;
    }

    public int getScope() {
        return this.f93x226a583a;
    }

    public String getUserFilter(String mappingId, String userId) {
        StringBuffer filter = new StringBuffer();
        filter.append("(&");
        filter.append(getFilter());
        filter.append("(");
        filter.append(mappingId);
        filter.append("=");
        filter.append(userId);
        filter.append("))");
        if (f92x59c4920d.isDebugEnabled()) {
            f92x59c4920d.debug("user filter: " + filter.toString());
        }
        return filter.toString();
    }

    public String toString() {
        StringBuffer ret = new StringBuffer(256);
        ret.append("root: ");
        ret.append(this.f94xbb78c9c2);
        ret.append("; ");
        ret.append("scope: ");
        ret.append(this.f93x226a583a);
        ret.append("; ");
        ret.append("filter: ");
        ret.append(this.f95x350cdc9c);
        return ret.toString();
    }
}
