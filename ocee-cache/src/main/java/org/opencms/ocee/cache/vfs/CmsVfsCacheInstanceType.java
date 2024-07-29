/*
 * Decompiled with CFR 0_123.
 */
package org.opencms.ocee.cache.vfs;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.opencms.ocee.cache.A_CmsCacheInstanceType;
import org.opencms.ocee.cache.I_CmsCacheInstanceType;
import org.opencms.ocee.cache.vfs.CmsCacheACE;
import org.opencms.ocee.cache.vfs.CmsCacheACEList;
import org.opencms.ocee.cache.vfs.CmsCacheFiles;
import org.opencms.ocee.cache.vfs.CmsCacheNonExistentResources;
import org.opencms.ocee.cache.vfs.CmsCacheParentFolder;
import org.opencms.ocee.cache.vfs.CmsCacheProperties;
import org.opencms.ocee.cache.vfs.CmsCachePropertyDefinitions;
import org.opencms.ocee.cache.vfs.CmsCacheRelations;
import org.opencms.ocee.cache.vfs.CmsCacheResourceLists;
import org.opencms.ocee.cache.vfs.CmsCacheResourceTree;
import org.opencms.ocee.cache.vfs.CmsCacheResources;

public final class CmsVfsCacheInstanceType
extends A_CmsCacheInstanceType {
    public static final CmsVfsCacheInstanceType VFS_ACE = new CmsVfsCacheInstanceType("accessControlEntry", CmsCacheACE.class.getName());
    public static final CmsVfsCacheInstanceType VFS_ACE_LIST = new CmsVfsCacheInstanceType("accessControlEntryList", CmsCacheACEList.class.getName());
    public static final CmsVfsCacheInstanceType VFS_FILES = new CmsVfsCacheInstanceType("files", CmsCacheFiles.class.getName());
    public static final CmsVfsCacheInstanceType VFS_NONEXISTENT_RESOURCES = new CmsVfsCacheInstanceType("nonExistentResources", CmsCacheNonExistentResources.class.getName());
    public static final CmsVfsCacheInstanceType VFS_PARENTFOLDER = new CmsVfsCacheInstanceType("parentfolder", CmsCacheParentFolder.class.getName());
    public static final CmsVfsCacheInstanceType VFS_PROPERTIES = new CmsVfsCacheInstanceType("properties", CmsCacheProperties.class.getName());
    public static final CmsVfsCacheInstanceType VFS_PROPERTYDEFINITIONS = new CmsVfsCacheInstanceType("propertydefinitions", CmsCachePropertyDefinitions.class.getName());
    public static final CmsVfsCacheInstanceType VFS_RESOURCELISTS = new CmsVfsCacheInstanceType("resourcelists", CmsCacheResourceLists.class.getName());
    public static final CmsVfsCacheInstanceType VFS_RESOURCETREE = new CmsVfsCacheInstanceType("resourceTree", CmsCacheResourceTree.class.getName());
    public static final CmsVfsCacheInstanceType VFS_RESOURCERELATIONS = new CmsVfsCacheInstanceType("resourceRelations", CmsCacheRelations.class.getName());
    public static final CmsVfsCacheInstanceType VFS_RESOURCES = new CmsVfsCacheInstanceType("resources", CmsCacheResources.class.getName());
    private static final CmsVfsCacheInstanceType[] \u00d8000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void = new CmsVfsCacheInstanceType[]{VFS_RESOURCES, VFS_PROPERTIES, VFS_ACE_LIST, VFS_PARENTFOLDER, VFS_FILES, VFS_RESOURCERELATIONS, VFS_NONEXISTENT_RESOURCES, VFS_PROPERTYDEFINITIONS, VFS_RESOURCELISTS, VFS_RESOURCETREE, VFS_ACE};
    public static final List<CmsVfsCacheInstanceType> VALUES = Collections.unmodifiableList(Arrays.asList(\u00d8000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void));

    private CmsVfsCacheInstanceType(String type, String defaultClass) {
        super(type, defaultClass, true);
    }

    public static I_CmsCacheInstanceType valueOf(String value) {
        return CmsVfsCacheInstanceType.valueOf(VALUES, value);
    }

    public int getOrder() {
        return VALUES.indexOf(this);
    }
}

