/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  org.apache.commons.logging.Log
 *  org.opencms.configuration.CmsConfigurationManager
 *  org.opencms.db.CmsDbContext
 *  org.opencms.db.CmsDbSqlException
 *  org.opencms.db.CmsDriverManager
 *  org.opencms.db.CmsResourceState
 *  org.opencms.db.I_CmsDriver
 *  org.opencms.db.I_CmsVfsDriver
 *  org.opencms.db.generic.CmsSqlManager
 *  org.opencms.db.generic.Messages
 *  org.opencms.file.CmsDataAccessException
 *  org.opencms.file.CmsFile
 *  org.opencms.file.CmsFolder
 *  org.opencms.file.CmsProject
 *  org.opencms.file.CmsProperty
 *  org.opencms.file.CmsPropertyDefinition
 *  org.opencms.file.CmsPropertyDefinition$CmsPropertyType
 *  org.opencms.file.CmsRequestContext
 *  org.opencms.file.CmsResource
 *  org.opencms.file.CmsVfsResourceNotFoundException
 *  org.opencms.i18n.CmsMessageContainer
 *  org.opencms.i18n.CmsMessages
 *  org.opencms.main.CmsLog
 *  org.opencms.main.I_CmsEventListener
 *  org.opencms.main.OpenCms
 *  org.opencms.monitor.CmsMemoryMonitor
 *  org.opencms.monitor.CmsMemoryMonitor$CacheType
 *  org.opencms.ocee.base.CmsOceeManager
 *  org.opencms.relations.CmsRelation
 *  org.opencms.relations.CmsRelationFilter
 *  org.opencms.util.CmsUUID
 */
package org.opencms.ocee.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsResourceState;
import org.opencms.db.I_CmsDriver;
import org.opencms.db.I_CmsVfsDriver;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.cache.CmsCacheManager;
import org.opencms.ocee.cache.I_CmsCacheInstanceType;
import org.opencms.ocee.cache.Messages;
import org.opencms.ocee.cache.vfs.CmsVfsCacheEventHandler;
import org.opencms.ocee.cache.vfs.CmsVfsCacheInstanceType;
import org.opencms.ocee.cache.vfs.CmsVfsCacheKey;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.util.CmsUUID;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class CmsVfsCache
implements I_CmsDriver,
I_CmsVfsDriver {
    private CmsVfsCacheEventHandler vfsCacheEventHandler;
    private static final Log LOG = CmsLog.getLog(CmsVfsCache.class);
    private I_CmsVfsDriver cmsVfsDriver;

    public int countSiblings(CmsDbContext dbc, CmsUUID projectId, CmsUUID resourceId) throws CmsDataAccessException {
        return this.cmsVfsDriver.countSiblings(dbc, projectId, resourceId);
    }

    public void createContent(CmsDbContext dbc, CmsUUID project, CmsUUID resourceId, byte[] content) throws CmsDataAccessException {
        this.cmsVfsDriver.createContent(dbc, project, resourceId, content);
    }

    public CmsFile createFile(ResultSet res, CmsUUID projectId) throws SQLException {
        return this.cmsVfsDriver.createFile(res, projectId);
    }

    public CmsFile createFile(ResultSet res, CmsUUID projectId, boolean hasFileContentInResultSet) throws SQLException {
        return this.cmsVfsDriver.createFile(res, projectId, hasFileContentInResultSet);
    }

    public CmsFolder createFolder(ResultSet res, CmsUUID projectId, boolean hasProjectIdInResultSet) throws SQLException {
        return this.cmsVfsDriver.createFolder(res, projectId, hasProjectIdInResultSet);
    }

    public void createOnlineContent(CmsDbContext dbc, CmsUUID resourceId, byte[] contents, int publishTag, boolean keepOnline, boolean needToUpdateContent) throws CmsDataAccessException {
        this.cmsVfsDriver.createOnlineContent(dbc, resourceId, contents, publishTag, keepOnline, needToUpdateContent);
    }

    public CmsPropertyDefinition createPropertyDefinition(CmsDbContext dbc, CmsUUID projectId, String name, CmsPropertyDefinition.CmsPropertyType type) throws CmsDataAccessException {
        return this.cmsVfsDriver.createPropertyDefinition(dbc, projectId, name, type);
    }

    public void createRelation(CmsDbContext dbc, CmsUUID projectId, CmsRelation relation) throws CmsDataAccessException {
        this.cmsVfsDriver.createRelation(dbc, projectId, relation);
    }

    public CmsResource createResource(CmsDbContext dbc, CmsUUID projectId, CmsResource resource, byte[] content) throws CmsDataAccessException {
        return this.cmsVfsDriver.createResource(dbc, projectId, resource, content);
    }

    public CmsResource createResource(ResultSet res, CmsUUID projectId) throws SQLException {
        return this.cmsVfsDriver.createResource(res, projectId);
    }

    public void createSibling(CmsDbContext dbc, CmsProject project, CmsResource resource) throws CmsDataAccessException {
        this.cmsVfsDriver.createSibling(dbc, project, resource);
    }

    public void deletePropertyDefinition(CmsDbContext dbc, CmsPropertyDefinition metadef) throws CmsDataAccessException {
        this.cmsVfsDriver.deletePropertyDefinition(dbc, metadef);
    }

    public void deletePropertyObjects(CmsDbContext dbc, CmsUUID projectId, CmsResource resource, int deleteOption) throws CmsDataAccessException {
        this.cmsVfsDriver.deletePropertyObjects(dbc, projectId, resource, deleteOption);
    }

    public void deleteRelations(CmsDbContext dbc, CmsUUID projectId, CmsResource resource, CmsRelationFilter filter) throws CmsDataAccessException {
        this.cmsVfsDriver.deleteRelations(dbc, projectId, resource, filter);
    }

    public void destroy() throws Throwable {
        this.cmsVfsDriver.destroy();
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info((Object)Messages.get().getBundle().key("INIT_CACHE_SHUTDOWN_OK_1", new Object[]{this.getClass().getName()}));
        }
        this.finalize();
    }

    public CmsSqlManager getSqlManager() {
        return this.cmsVfsDriver.getSqlManager();
    }

    public void init(CmsDbContext dbc, CmsConfigurationManager configurationManager, List<String> successiveDrivers, CmsDriverManager driverManager) {
        Map configuration = configurationManager.getConfiguration();
        String driverName = (String)configuration.get(successiveDrivers.get(0) + ".vfs.driver");
        successiveDrivers = successiveDrivers.size() > 1 ? successiveDrivers.subList(1, successiveDrivers.size()) : null;
        this.cmsVfsDriver = (I_CmsVfsDriver)driverManager.newDriverInstance(configurationManager, driverName, successiveDrivers);
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !manager.isInitialized()) {
            return;
        }
        if (CmsOceeManager.getInstance().checkCoreVersion("7.5.2")) {
            OpenCms.getMemoryMonitor().disableCache(new CmsMemoryMonitor.CacheType[]{CmsMemoryMonitor.CacheType.RESOURCE});
            OpenCms.getMemoryMonitor().disableCache(new CmsMemoryMonitor.CacheType[]{CmsMemoryMonitor.CacheType.RESOURCE_LIST});
            OpenCms.getMemoryMonitor().disableCache(new CmsMemoryMonitor.CacheType[]{CmsMemoryMonitor.CacheType.PROPERTY});
            OpenCms.getMemoryMonitor().disableCache(new CmsMemoryMonitor.CacheType[]{CmsMemoryMonitor.CacheType.PROPERTY_LIST});
        } else if (CmsOceeManager.getInstance().checkCoreVersion("7.0.5")) {
            OpenCms.getMemoryMonitor().setCacheResourceList(false);
            OpenCms.getMemoryMonitor().setCacheResource(false);
            OpenCms.getMemoryMonitor().setCachePropertyList(false);
            OpenCms.getMemoryMonitor().setCacheProperty(false);
        }
        manager.setVfsDriver(this);
        this.vfsCacheEventHandler = new CmsVfsCacheEventHandler();
        OpenCms.addCmsEventListener((I_CmsEventListener)this.vfsCacheEventHandler, (int[])new int[]{14, 15, 27, 11, 12, 23, 24, 22, 25, 2, 5, 17, 16, 26, 28});
    }

    public CmsSqlManager initSqlManager(String classname) {
        return null;
    }

    public void moveRelations(CmsDbContext dbc, CmsUUID projectId, CmsResource resource) throws CmsDataAccessException {
        CmsSqlManager sqlManager = this.cmsVfsDriver.getSqlManager();
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = sqlManager.getConnection(dbc);
            stmt = sqlManager.getPreparedStatement(conn, projectId, "C_MOVE_RELATIONS_SOURCE");
            stmt.setString(1, resource.getRootPath());
            stmt.setString(2, resource.getStructureId().toString());
            stmt.executeUpdate();
            sqlManager.closeAll(dbc, null, (Statement)stmt, null);
            stmt = sqlManager.getPreparedStatement(conn, projectId, "C_MOVE_RELATIONS_TARGET");
            stmt.setString(1, resource.getRootPath());
            stmt.setString(2, resource.getStructureId().toString());
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CmsDbSqlException(org.opencms.db.generic.Messages.get().container("ERR_GENERIC_SQL_1", (Object)CmsDbSqlException.getErrorQuery((Statement)stmt)), (Throwable)e);
        }
        finally {
            sqlManager.closeAll(dbc, conn, (Statement)stmt, null);
        }
    }

    public void moveResource(CmsDbContext dbc, CmsUUID projectId, CmsResource source, String destinationPath) throws CmsDataAccessException {
        this.cmsVfsDriver.moveResource(dbc, projectId, source, destinationPath);
    }

    public void publishResource(CmsDbContext dbc, CmsProject onlineProject, CmsResource onlineResource, CmsResource offlineResource) throws CmsDataAccessException {
        this.cmsVfsDriver.publishResource(dbc, onlineProject, onlineResource, offlineResource);
    }

    public void publishVersions(CmsDbContext dbc, CmsResource resource, boolean firstSibling) throws CmsDataAccessException {
        this.cmsVfsDriver.publishVersions(dbc, resource, firstSibling);
    }

    public List readChildResources(CmsDbContext dbc, CmsProject currentProject, CmsResource resource, boolean getFolders, boolean getFiles) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID()) {
            return this.cmsVfsDriver.readChildResources(dbc, currentProject, resource, getFolders, getFiles);
        }
        String key = getFolders && getFiles ? "_all_" : (getFolders ? "_folders_" : "_files_");
        String cacheKey = CmsVfsCacheKey.getCacheKeyForResourceList(key, resource.getRootPath());
        List subResources = (List)manager.cacheLookup(CmsVfsCacheInstanceType.VFS_RESOURCELISTS, currentProject.getUuid(), cacheKey);
        if (subResources == null) {
            subResources = this.cmsVfsDriver.readChildResources(dbc, currentProject, resource, getFolders, getFiles);
            manager.cacheSet(CmsVfsCacheInstanceType.VFS_RESOURCELISTS, currentProject.getUuid(), cacheKey, subResources);
        }
        return subResources != null ? this.clone(subResources) : Collections.emptyList();
    }

    public byte[] readContent(CmsDbContext dbc, CmsUUID projectId, CmsUUID resourceId) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID()) {
            return this.cmsVfsDriver.readContent(dbc, projectId, resourceId);
        }
        String cacheKey = CmsVfsCacheKey.getCacheKeyForResource("resources", resourceId);
        byte[] content = (byte[])manager.cacheLookup(CmsVfsCacheInstanceType.VFS_FILES, projectId, cacheKey);
        if (content == null) {
            content = this.cmsVfsDriver.readContent(dbc, projectId, resourceId);
            manager.cacheSet(CmsVfsCacheInstanceType.VFS_FILES, projectId, cacheKey, content);
        }
        return content != null ? (byte[])content.clone() : null;
    }

    public CmsFolder readFolder(CmsDbContext dbc, CmsUUID projectId, CmsUUID folderId) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID()) {
            return this.cmsVfsDriver.readFolder(dbc, projectId, folderId);
        }
        String cacheKeyId = CmsVfsCacheKey.getCacheKeyForResource("resources", folderId);
        CmsResource resource = (CmsResource)manager.cacheLookup(CmsVfsCacheInstanceType.VFS_RESOURCES, projectId, cacheKeyId);
        if (resource == null) {
            resource = this.cmsVfsDriver.readFolder(dbc, projectId, folderId);
            manager.cacheSet(CmsVfsCacheInstanceType.VFS_RESOURCES, projectId, cacheKeyId, (Object)resource);
            String cacheKeyPath = CmsVfsCacheKey.getCacheKeyForResource("resources", resource.getRootPath());
            manager.cacheSet(CmsVfsCacheInstanceType.VFS_RESOURCES, projectId, cacheKeyPath, (Object)resource);
        }
        if (resource instanceof CmsFolder) {
            return (CmsFolder)resource.clone();
        }
        CmsFolder folder = new CmsFolder(resource);
        manager.cacheSet(CmsVfsCacheInstanceType.VFS_RESOURCES, projectId, cacheKeyId, (Object)folder);
        String cacheKeyPath = CmsVfsCacheKey.getCacheKeyForResource("resources", resource.getRootPath());
        manager.cacheSet(CmsVfsCacheInstanceType.VFS_RESOURCES, projectId, cacheKeyPath, (Object)folder);
        return folder;
    }

    public CmsFolder readFolder(CmsDbContext dbc, CmsUUID projectId, String path) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID() || OpenCms.getRunLevel() < 3) {
            return this.cmsVfsDriver.readFolder(dbc, projectId, path);
        }
        String cacheKeyPath = CmsVfsCacheKey.getCacheKeyForResource("resources", path);
        CmsResource resource = (CmsResource)manager.cacheLookup(CmsVfsCacheInstanceType.VFS_RESOURCES, projectId, cacheKeyPath);
        if (resource == null) {
            resource = this.cmsVfsDriver.readFolder(dbc, projectId, path);
            manager.cacheSet(CmsVfsCacheInstanceType.VFS_RESOURCES, projectId, cacheKeyPath, (Object)resource);
            String cacheKeyId = CmsVfsCacheKey.getCacheKeyForResource("resources", resource.getStructureId());
            manager.cacheSet(CmsVfsCacheInstanceType.VFS_RESOURCES, projectId, cacheKeyId, (Object)resource);
        }
        if (resource instanceof CmsFolder) {
            return (CmsFolder)resource.clone();
        }
        CmsFolder folder = new CmsFolder(resource);
        manager.cacheSet(CmsVfsCacheInstanceType.VFS_RESOURCES, projectId, cacheKeyPath, (Object)folder);
        String cacheKeyId = CmsVfsCacheKey.getCacheKeyForResource("resources", resource.getStructureId());
        manager.cacheSet(CmsVfsCacheInstanceType.VFS_RESOURCES, projectId, cacheKeyId, (Object)folder);
        return folder;
    }

    public CmsFolder readParentFolder(CmsDbContext dbc, CmsUUID projectId, CmsUUID structureId) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID()) {
            return this.cmsVfsDriver.readParentFolder(dbc, projectId, structureId);
        }
        String cacheKeyParent = CmsVfsCacheKey.getCacheKeyForParentFolder("parent_folder", structureId);
        CmsUUID parentId = (CmsUUID)manager.cacheLookup(CmsVfsCacheInstanceType.VFS_PARENTFOLDER, projectId, cacheKeyParent);
        if (parentId != null) {
            return this.readFolder(dbc, projectId, parentId);
        }
        CmsFolder folder = this.cmsVfsDriver.readParentFolder(dbc, projectId, structureId);
        if (folder != null) {
            String cacheKeyId = CmsVfsCacheKey.getCacheKeyForResource("resources", folder.getStructureId());
            manager.cacheSet(CmsVfsCacheInstanceType.VFS_RESOURCES, projectId, cacheKeyId, (Object)folder);
            String cacheKeyPath = CmsVfsCacheKey.getCacheKeyForResource("resources", folder.getRootPath());
            manager.cacheSet(CmsVfsCacheInstanceType.VFS_RESOURCES, projectId, cacheKeyPath, (Object)folder);
            manager.cacheSet(CmsVfsCacheInstanceType.VFS_PARENTFOLDER, projectId, cacheKeyParent, (Object)folder.getStructureId());
        }
        return folder != null ? (CmsFolder)folder.clone() : null;
    }

    public CmsPropertyDefinition readPropertyDefinition(CmsDbContext dbc, String name, CmsUUID projectId) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID()) {
            return this.cmsVfsDriver.readPropertyDefinition(dbc, name, projectId);
        }
        String cacheKey = CmsVfsCacheKey.getCacheKeyForPropertyDefinition("property_definition", name);
        CmsPropertyDefinition propertyDefinition = (CmsPropertyDefinition)manager.cacheLookup(CmsVfsCacheInstanceType.VFS_PROPERTYDEFINITIONS, projectId, cacheKey);
        if (propertyDefinition == null) {
            propertyDefinition = this.cmsVfsDriver.readPropertyDefinition(dbc, name, projectId);
            if (propertyDefinition == null) {
                propertyDefinition = CmsPropertyDefinition.getNullPropertyDefinition();
            }
            manager.cacheSet(CmsVfsCacheInstanceType.VFS_PROPERTYDEFINITIONS, projectId, cacheKey, (Object)propertyDefinition);
        }
        return propertyDefinition.equals((Object)CmsPropertyDefinition.getNullPropertyDefinition()) ? null : propertyDefinition;
    }

    public List readPropertyDefinitions(CmsDbContext dbc, CmsUUID projectId) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID()) {
            return this.cmsVfsDriver.readPropertyDefinitions(dbc, projectId);
        }
        String cacheKey = CmsVfsCacheKey.getCacheKeyForAllPropertyDefinitions();
        List propertyDefinitions = (List)manager.cacheLookup(CmsVfsCacheInstanceType.VFS_PROPERTYDEFINITIONS, projectId, cacheKey);
        if (propertyDefinitions == null) {
            propertyDefinitions = this.cmsVfsDriver.readPropertyDefinitions(dbc, projectId);
            manager.cacheSet(CmsVfsCacheInstanceType.VFS_PROPERTYDEFINITIONS, projectId, cacheKey, propertyDefinitions);
        }
        return propertyDefinitions;
    }

    public CmsProperty readPropertyObject(CmsDbContext dbc, String key, CmsProject project, CmsResource resource) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID()) {
            return this.cmsVfsDriver.readPropertyObject(dbc, key, project, resource);
        }
        String cacheKey = CmsVfsCacheKey.getCacheKeyForProperty("property", resource, key);
        CmsProperty property = (CmsProperty)manager.cacheLookup(CmsVfsCacheInstanceType.VFS_PROPERTIES, project.getUuid(), cacheKey);
        if (property == null) {
            property = this.cmsVfsDriver.readPropertyObject(dbc, key, project, resource);
            if (property == null) {
                property = CmsProperty.getNullProperty();
            }
            manager.cacheSet(CmsVfsCacheInstanceType.VFS_PROPERTIES, project.getUuid(), cacheKey, (Object)property);
        }
        return property;
    }

    public List readPropertyObjects(CmsDbContext dbc, CmsProject project, CmsResource resource) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID()) {
            return this.cmsVfsDriver.readPropertyObjects(dbc, project, resource);
        }
        String cacheKey = CmsVfsCacheKey.getCacheKeyForPropertyList("properties", resource);
        List properties = (List)manager.cacheLookup(CmsVfsCacheInstanceType.VFS_PROPERTIES, project.getUuid(), cacheKey);
        if (properties == null) {
            properties = this.cmsVfsDriver.readPropertyObjects(dbc, project, resource);
            manager.cacheSet(CmsVfsCacheInstanceType.VFS_PROPERTIES, project.getUuid(), cacheKey, properties);
        }
        return properties != null ? this.copyProperties(properties) : Collections.emptyList();
    }

    public List readRelations(CmsDbContext dbc, CmsUUID projectId, CmsResource resource, CmsRelationFilter filter) throws CmsDataAccessException {
        List relations;
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID() || filter.isIncludeSubresources()) {
            return this.cmsVfsDriver.readRelations(dbc, projectId, resource, filter);
        }
        String cacheKey = CmsVfsCacheKey.getCacheKeyForRelation("resource_relations", resource.getStructureId());
        HashMap allRelations = (HashMap)manager.cacheLookup(CmsVfsCacheInstanceType.VFS_RESOURCERELATIONS, projectId, cacheKey);
        if (allRelations == null) {
            allRelations = new HashMap();
        }
        if ((relations = (List)allRelations.get(filter.toString())) == null) {
            relations = this.cmsVfsDriver.readRelations(dbc, projectId, resource, filter);
            relations = Collections.unmodifiableList(relations);
            allRelations.put(filter.toString(), relations);
            manager.cacheSet(CmsVfsCacheInstanceType.VFS_RESOURCERELATIONS, projectId, cacheKey, allRelations);
        }
        return relations;
    }

    public CmsResource readResource(CmsDbContext dbc, CmsUUID projectId, CmsUUID resourceId, boolean includeDeleted) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID()) {
            return this.cmsVfsDriver.readResource(dbc, projectId, resourceId, includeDeleted);
        }
        String cacheKeyId = CmsVfsCacheKey.getCacheKeyForResource("resources", resourceId);
        CmsResource resource = (CmsResource)manager.cacheLookup(CmsVfsCacheInstanceType.VFS_RESOURCES, projectId, cacheKeyId);
        if (resource == null && (resource = this.cmsVfsDriver.readResource(dbc, projectId, resourceId, true)) != null) {
            manager.cacheSet(CmsVfsCacheInstanceType.VFS_RESOURCES, projectId, cacheKeyId, (Object)resource);
            String cacheKeyPath = CmsVfsCacheKey.getCacheKeyForResource("resources", resource.getRootPath());
            manager.cacheSet(CmsVfsCacheInstanceType.VFS_RESOURCES, projectId, cacheKeyPath, (Object)resource);
        }
        if (!includeDeleted && resource != null && resource.getState().isDeleted()) {
            throw new CmsVfsResourceNotFoundException(org.opencms.db.generic.Messages.get().container("ERR_READ_DELETED_RESOURCE_1", (Object)dbc.removeSiteRoot(resource.getRootPath())));
        }
        return resource != null ? (CmsResource)resource.clone() : null;
    }

    public CmsResource readResource(CmsDbContext dbc, CmsUUID projectId, String path, boolean includeDeleted) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID() || OpenCms.getRunLevel() < 3) {
            return this.cmsVfsDriver.readResource(dbc, projectId, path, includeDeleted);
        }
        String cacheKeyPath = CmsVfsCacheKey.getCacheKeyForResource("resources", path);
        boolean isOnline = CmsProject.isOnlineProject((CmsUUID)projectId);
        String cacheKeyPath2 = CmsVfsCacheKey.getCacheKeyForNonExistentResource("resources", path);
        if (isOnline && manager.cacheLookup(CmsVfsCacheInstanceType.VFS_NONEXISTENT_RESOURCES, projectId, cacheKeyPath2) != null) {
            LOG.debug((Object)("non-existent resource got in cache: " + path + ", project: " + (Object)projectId));
            throw new CmsVfsResourceNotFoundException(org.opencms.db.generic.Messages.get().container("ERR_READ_RESOURCE_1", (Object)dbc.removeSiteRoot(path)));
        }
        CmsResource resource = (CmsResource)manager.cacheLookup(CmsVfsCacheInstanceType.VFS_RESOURCES, projectId, cacheKeyPath);
        if (resource != null && resource.isFile() && path.endsWith("/")) {
            resource = null;
        }
        if (resource == null) {
            try {
                resource = this.cmsVfsDriver.readResource(dbc, projectId, path, true);
                manager.cacheSet(CmsVfsCacheInstanceType.VFS_RESOURCES, projectId, cacheKeyPath, (Object)resource);
                String cacheKeyId = CmsVfsCacheKey.getCacheKeyForResource("resources", resource.getStructureId());
                manager.cacheSet(CmsVfsCacheInstanceType.VFS_RESOURCES, projectId, cacheKeyId, (Object)resource);
            }
            catch (CmsVfsResourceNotFoundException e) {
                if (isOnline) {
                    LOG.debug("non-existent resource put in cache: " + path + ", project: " + projectId);
                    manager.cacheSet(CmsVfsCacheInstanceType.VFS_NONEXISTENT_RESOURCES, projectId, cacheKeyPath2, Boolean.TRUE);
                }
                throw e;
            }
        }
        if (!includeDeleted && resource.getState().isDeleted()) {
            throw new CmsVfsResourceNotFoundException(org.opencms.db.generic.Messages.get().container("ERR_READ_DELETED_RESOURCE_1", (Object)dbc.removeSiteRoot(resource.getRootPath())));
        }
        return (CmsResource)resource.clone();
    }

    public List readResources(CmsDbContext dbc, CmsUUID projectId, CmsResourceState state, int mode) throws CmsDataAccessException {
        return this.cmsVfsDriver.readResources(dbc, projectId, state, mode);
    }

    public List readResourcesForPrincipalACE(CmsDbContext dbc, CmsProject project, CmsUUID principalId) throws CmsDataAccessException {
        return this.cmsVfsDriver.readResourcesForPrincipalACE(dbc, project, principalId);
    }

    public List readResourcesForPrincipalAttr(CmsDbContext dbc, CmsProject project, CmsUUID principalId) throws CmsDataAccessException {
        return this.cmsVfsDriver.readResourcesForPrincipalAttr(dbc, project, principalId);
    }

    public List readResourcesWithProperty(CmsDbContext dbc, CmsUUID projectId, CmsUUID propertyDefinition, String path, String value) throws CmsDataAccessException {
        return this.cmsVfsDriver.readResourcesWithProperty(dbc, projectId, propertyDefinition, path, value);
    }

    
    public List readResourceTree(CmsDbContext dbc, CmsUUID projectId, String parentPath, int type, CmsResourceState state, long startTime, long endTime, long releasedAfter, long releasedBefore, long expiredAfter, long expiredBefore, int mode) throws CmsDataAccessException
    {
    	return readResourceTree(dbc, projectId, parentPath, type, state, startTime, endTime, releasedAfter, releasedBefore, expiredAfter, expiredBefore, mode,null,null);
    }
    
    public List readResourceTree(CmsDbContext dbc, CmsUUID projectId, String parentPath, int type, CmsResourceState state, long startTime, long endTime, long releasedAfter, long releasedBefore, long expiredAfter, long expiredBefore, int mode, Integer size, Integer offset) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID()) {
            return this.cmsVfsDriver.readResourceTree(dbc, projectId, parentPath, type, state, startTime, endTime, releasedAfter, releasedBefore, expiredAfter, expiredBefore, mode, size, offset);
        }
        if (!CmsProject.isOnlineProject((CmsUUID)projectId) || (mode & 1) > 0 || (mode & 0) <= 0) {
            return this.cmsVfsDriver.readResourceTree(dbc, projectId, parentPath, type, state, startTime, endTime, releasedAfter, releasedBefore, expiredAfter, expiredBefore, mode, size, offset);
        }
        String cacheKeyPath = CmsVfsCacheKey.getCacheKeyForResourceTree("resource_tree", parentPath);
        String cacheKeyParams = CmsVfsCacheKey.getCacheKeyForResourceTreeParams(type, state, startTime, endTime, releasedAfter, releasedBefore, expiredAfter, expiredBefore, mode, size, offset);
        HashMap<String, List> treeMap = (HashMap<String, List>)manager.cacheLookup(CmsVfsCacheInstanceType.VFS_RESOURCETREE, projectId, cacheKeyPath);
        if (treeMap != null) {
            if (treeMap.containsKey(cacheKeyParams)) {
                return (List)treeMap.get(cacheKeyParams);
            }
        } else {
            treeMap = new HashMap<String, List>();
        }
        List treeList = this.cmsVfsDriver.readResourceTree(dbc, projectId, parentPath, type, state, startTime, endTime, releasedAfter, releasedBefore, expiredAfter, expiredBefore, mode, size, offset);
        treeMap.put(cacheKeyParams, treeList);
        manager.cacheSet(CmsVfsCacheInstanceType.VFS_RESOURCETREE, projectId, cacheKeyPath, treeMap);
        return treeList;
    }

    public List readSiblings(CmsDbContext dbc, CmsUUID projectId, CmsResource resource, boolean includeDeleted) throws CmsDataAccessException {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null || !dbc.getProjectId().isNullUUID()) {
            return this.cmsVfsDriver.readSiblings(dbc, projectId, resource, includeDeleted);
        }
        String cacheKey = CmsVfsCacheKey.getCacheKeyForResourceList("siblings", resource.getResourceId().toString() + Boolean.toString(includeDeleted));
        List siblings = (List)manager.cacheLookup(CmsVfsCacheInstanceType.VFS_RESOURCELISTS, projectId, cacheKey);
        if (siblings == null) {
            siblings = this.cmsVfsDriver.readSiblings(dbc, projectId, resource, includeDeleted);
            manager.cacheSet(CmsVfsCacheInstanceType.VFS_RESOURCELISTS, projectId, cacheKey, siblings);
        }
        return siblings != null ? this.clone(siblings) : Collections.emptyList();
    }

    public Map readVersions(CmsDbContext dbc, CmsUUID projectId, CmsUUID structureId) throws CmsDataAccessException {
        int resourceVersion;
        int structureVersion;
        structureVersion = -1;
        resourceVersion = -1;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        CmsSqlManager sqlManager = this.cmsVfsDriver.getSqlManager();
        try {
            conn = sqlManager.getConnection(dbc);
            stmt = sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ_VERSIONS");
            stmt.setString(1, structureId.toString());
            res = stmt.executeQuery();
            if (res.next()) {
                resourceVersion = res.getInt(sqlManager.readQuery("C_RESOURCES_VERSION"));
                structureVersion = res.getInt(sqlManager.readQuery("C_RESOURCES_STRUCTURE_VERSION"));
                while (res.next()) {
                }
            }
        }
        catch (SQLException e) {
            throw new CmsDbSqlException(org.opencms.db.generic.Messages.get().container("ERR_GENERIC_SQL_1", (Object)CmsDbSqlException.getErrorQuery((Statement)stmt)), (Throwable)e);
        }
        finally {
            sqlManager.closeAll(dbc, conn, (Statement)stmt, res);
        }
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        result.put("structure", new Integer(structureVersion));
        result.put("resource", new Integer(resourceVersion));
        return result;
    }

    public Map readVersions(CmsDbContext dbc, CmsUUID projectId, CmsUUID resourceId, CmsUUID structureId) throws CmsDataAccessException {
        return this.cmsVfsDriver.readVersions(dbc, projectId, resourceId, structureId);
    }

    public void removeFile(CmsDbContext dbc, CmsUUID projectId, CmsResource resource) throws CmsDataAccessException {
        this.cmsVfsDriver.removeFile(dbc, projectId, resource);
    }

    public void removeFolder(CmsDbContext dbc, CmsProject currentProject, CmsResource resource) throws CmsDataAccessException {
        this.cmsVfsDriver.removeFolder(dbc, currentProject, resource);
    }

    public void replaceResource(CmsDbContext dbc, CmsResource newResource, byte[] newResourceContent, int newResourceType) throws CmsDataAccessException {
        this.cmsVfsDriver.replaceResource(dbc, newResource, newResourceContent, newResourceType);
    }

    public void transferResource(CmsDbContext dbc, CmsProject project, CmsResource resource, CmsUUID createdUser, CmsUUID lastModifiedUser) throws CmsDataAccessException {
        this.cmsVfsDriver.transferResource(dbc, project, resource, createdUser, lastModifiedUser);
    }

    public void updateBrokenRelations(CmsDbContext dbc, CmsResource resource, boolean update) throws CmsDataAccessException {
        if (update) {
            this.updateBrokenRelations(dbc, dbc.getRequestContext().currentProject().getUuid());
        } else {
            this.repairBrokenRelations(dbc, dbc.getRequestContext().currentProject().getUuid(), resource.getStructureId(), resource.getRootPath());
        }
    }

    public void updateRelations(CmsDbContext dbc, CmsProject onlineProject, CmsResource offlineResource) throws CmsDataAccessException {
        this.cmsVfsDriver.updateRelations(dbc, onlineProject, offlineResource);
    }

    public boolean validateResourceIdExists(CmsDbContext dbc, CmsUUID projectId, CmsUUID resourceId) throws CmsDataAccessException {
        return this.cmsVfsDriver.validateResourceIdExists(dbc, projectId, resourceId);
    }

    public boolean validateStructureIdExists(CmsDbContext dbc, CmsUUID projectId, CmsUUID structureId) throws CmsDataAccessException {
        return this.cmsVfsDriver.validateStructureIdExists(dbc, projectId, structureId);
    }

    public void writeContent(CmsDbContext dbc, CmsUUID contentId, byte[] content) throws CmsDataAccessException {
        this.cmsVfsDriver.writeContent(dbc, contentId, content);
    }

    public void writeLastModifiedProjectId(CmsDbContext dbc, CmsProject project, CmsUUID projectId, CmsResource resource) throws CmsDataAccessException {
        this.cmsVfsDriver.writeLastModifiedProjectId(dbc, project, projectId, resource);
    }

    public void writePropertyObject(CmsDbContext dbc, CmsProject project, CmsResource resource, CmsProperty property) throws CmsDataAccessException {
        this.cmsVfsDriver.writePropertyObject(dbc, project, resource, property);
    }

    public void writePropertyObjects(CmsDbContext dbc, CmsProject project, CmsResource resource, List properties) throws CmsDataAccessException {
        this.cmsVfsDriver.writePropertyObjects(dbc, project, resource, properties);
    }

    public void writeResource(CmsDbContext dbc, CmsUUID projectId, CmsResource resource, int changed) throws CmsDataAccessException {
        this.cmsVfsDriver.writeResource(dbc, projectId, resource, changed);
        this.readSiblings(dbc, projectId, resource, true);
    }

    public void writeResourceState(CmsDbContext dbc, CmsProject project, CmsResource resource, int changed, boolean isPublishing) throws CmsDataAccessException {
        this.cmsVfsDriver.writeResourceState(dbc, project, resource, changed, isPublishing);
        if (changed == 3 || changed == 1 || changed == 4) {
            this.readSiblings(dbc, project.getUuid(), resource, true);
        }
    }

    protected List<CmsProperty> copyProperties(List<CmsProperty> props) {
        return new ArrayList<CmsProperty>(props);
    }

    protected void repairBrokenRelations(CmsDbContext dbc, CmsUUID projectId, CmsUUID structureId, String rootPath) throws CmsDataAccessException {
        CmsSqlManager sqlManager = this.cmsVfsDriver.getSqlManager();
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = sqlManager.getConnection(dbc);
            stmt = sqlManager.getPreparedStatement(conn, projectId, "C_RELATIONS_REPAIR_BROKEN");
            stmt.setString(1, structureId.toString());
            stmt.setString(2, rootPath);
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container("ERR_GENERIC_SQL_1", (Object)CmsDbSqlException.getErrorQuery((Statement)stmt)), (Throwable)e);
        }
        finally {
            sqlManager.closeAll(dbc, conn, (Statement)stmt, null);
        }
    }

    protected void updateBrokenRelations(CmsDbContext dbc, CmsUUID projectId) throws CmsDataAccessException {
        CmsSqlManager sqlManager = this.cmsVfsDriver.getSqlManager();
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = sqlManager.getConnection(dbc);
            stmt = sqlManager.getPreparedStatement(conn, projectId, "C_RELATIONS_UPDATE_BROKEN");
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container("ERR_GENERIC_SQL_1", (Object)CmsDbSqlException.getErrorQuery((Statement)stmt)), (Throwable)e);
        }
        finally {
            sqlManager.closeAll(dbc, conn, (Statement)stmt, null);
        }
    }

    private List<CmsResource> clone(List<CmsResource> resources) {
        ArrayList<CmsResource> result = new ArrayList<CmsResource>(resources.size());
        for (CmsResource resource : resources) {
            result.add((CmsResource)resource.clone());
        }
        return result;
    }

	@Override
	public List readResourcesWithProperty(CmsDbContext dbc, CmsUUID projectId, CmsUUID propertyDefinition, String path,
			String value, Integer size, Integer offset) throws CmsDataAccessException {
		return this.cmsVfsDriver.readResourcesWithProperty(dbc, projectId, propertyDefinition, path, value, size, offset);
	}
}

