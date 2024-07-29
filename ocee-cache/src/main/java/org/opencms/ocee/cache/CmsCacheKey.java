/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  org.opencms.db.CmsCacheKey
 *  org.opencms.db.CmsDbContext
 *  org.opencms.file.CmsProject
 *  org.opencms.file.CmsRequestContext
 *  org.opencms.file.CmsResource
 *  org.opencms.file.CmsUser
 *  org.opencms.security.CmsPermissionSet
 *  org.opencms.util.CmsUUID
 */
package org.opencms.ocee.cache;

import org.opencms.db.CmsDbContext;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsUUID;

public final class CmsCacheKey
extends org.opencms.db.CmsCacheKey {
    public String getCacheKeyForUserGroups(String prefix, CmsDbContext context, CmsUser user) {
        StringBuffer cacheBuffer = new StringBuffer(64);
        cacheBuffer.append(prefix);
        cacheBuffer.append('_');
        cacheBuffer.append(user.getName());
        cacheBuffer.append('_');
        cacheBuffer.append(context.getRequestContext().getRemoteAddress());
        return cacheBuffer.toString();
    }

    public String getCacheKeyForUserPermissions(String prefix, CmsDbContext context, CmsResource resource, CmsPermissionSet requiredPermissions) {
        StringBuffer cacheBuffer = new StringBuffer(64);
        cacheBuffer.append(prefix);
        cacheBuffer.append('_');
        cacheBuffer.append(context.currentUser().getName());
        cacheBuffer.append('_');
        cacheBuffer.append(context.getRequestContext().getRemoteAddress());
        cacheBuffer.append('_');
        cacheBuffer.append(context.currentProject().isOnlineProject() ? "_0_" : "_1_");
        cacheBuffer.append(requiredPermissions.getPermissionString());
        cacheBuffer.append('_');
        cacheBuffer.append(resource.getStructureId().toString());
        return cacheBuffer.toString();
    }
}

