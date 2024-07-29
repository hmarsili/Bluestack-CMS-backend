/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsObject.java,v $
 * Date   : $Date: 2011/03/23 14:51:10 $
 * Version: $Revision: 1.172 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file;

import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsResourceState;
import org.opencms.db.CmsSecurityManager;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.history.CmsHistoryPrincipal;
import org.opencms.file.history.CmsHistoryProject;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockFilter;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.I_CmsPermissionHandler;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This pivotal class provides all authorized access to the OpenCms VFS resources.<p>
 * 
 * It encapsulates user identification and permissions.
 * Think of it as an initialized "shell" to access the OpenCms VFS.
 * Every call to a method here will be checked for user permissions
 * according to the <code>{@link org.opencms.file.CmsRequestContext}</code> this CmsObject instance was created with.<p>
 * 
 * From a JSP page running in OpenCms, use <code>{@link org.opencms.jsp.CmsJspBean#getCmsObject()}</code> to gain 
 * access to the current users CmsObject. Usually this is done with a <code>{@link org.opencms.jsp.CmsJspActionElement}</code>.<p>
 * 
 * To generate a new instance of this class in your application, use 
 * <code>{@link org.opencms.main.OpenCms#initCmsObject(String)}</code>. The argument String should be 
 * the name of the guest user, usually "Guest" and more formally obtained by <code>{@link org.opencms.db.CmsDefaultUsers#getUserGuest()}</code>.
 * This will give you an initialized context with guest user permissions.
 * Then use <code>{@link CmsObject#loginUser(String, String)}</code> to log in the user you want.
 * Obviously you need the password for the new user.
 * You should never try to create an instance of this class using the constructor, 
 * this is reserved for internal operation only.<p> 
 *
 * @author Alexander Kandzior 
 * @author Thomas Weckert  
 * @author Carsten Weinholz 
 * @author Andreas Zahner 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.172 $
 * 
 * @since 6.0.0 
 */
public class CmsObject {

    /** The request context. */
    protected CmsRequestContext m_context;

    /** The security manager to access the cms. */
    protected CmsSecurityManager m_securityManager;

    /**
     * Connects an OpenCms user context to a running database.<p>
     * 
     * <b>Please note:</b> This constructor is internal to OpenCms and not for public use.
     * If you want to create a new instance of a <code>{@link CmsObject}</code> in your application,
     * use <code>{@link org.opencms.main.OpenCms#initCmsObject(String)}</code>.<p>
     * 
     * @param securityManager the security manager
     * @param context the request context that contains the user authentication
     */
    public CmsObject(CmsSecurityManager securityManager, CmsRequestContext context) {

        init(securityManager, context);
    }

    /**
     * Adds a new relation to the given resource.<p>
     * 
     * @param resourceName the name of the source resource
     * @param targetPath the path of the target resource
     * @param type the type of the relation
     * 
     * @throws CmsException if something goes wrong
     */
    public void addRelationToResource(String resourceName, String targetPath, String type) throws CmsException {

        createRelation(resourceName, targetPath, type, false);
    }

    /**
     * Adds a user to a group.<p>
     * 
     * @param username the name of the user that is to be added to the group
     * @param groupname the name of the group
     * 
     * @throws CmsException if something goes wrong
     */
    public void addUserToGroup(String username, String groupname) throws CmsException {

        m_securityManager.addUserToGroup(m_context, username, groupname, false);
    }

    /**
     * Creates a new web user.<p>
     * 
     * A web user has no access to the workplace but is able to access personalized
     * functions controlled by the OpenCms.<br>
     * 
     * Moreover, a web user can be created by any user, the intention being that
     * a "Guest" user can create a personalized account for himself.<p>
     *
     * @param name the name for the new web user
     * @param password the password for the user
     * @param group the default group name for the user
     * @param description the description for the user
     * @param additionalInfos a <code>{@link Map}</code> with additional infos for the user
     * 
     * @return the newly created user
     * 
     * @throws CmsException if something goes wrong
     * 
     * @deprecated there are no more web users, use a user without any role!
     */
    public CmsUser addWebUser(String name, String password, String group, String description, Map additionalInfos)
    throws CmsException {

        CmsUser user = m_securityManager.createUser(m_context, name, password, description, additionalInfos);
        addUserToGroup(name, group);
        return user;
    }

    /**
     * Creates a backup of the current project.<p>
     * 
     * @param versionId the version of the backup
     * @param publishDate the date of publishing
     *
     * @throws CmsException if operation was not successful
     * 
     * @deprecated Use {@link #writeHistoryProject(int,long)} instead
     */
    public void backupProject(int versionId, long publishDate) throws CmsException {

        writeHistoryProject(versionId, publishDate);
    }

    /**
     * Changes the access control for a given resource and a given principal(user/group).<p>
     * 
     * @param resourceName name of the resource
     * @param principalType the type of the principal (currently group or user):
     *      <ul>
     *          <li><code>{@link I_CmsPrincipal#PRINCIPAL_USER}</code></li>
     *          <li><code>{@link I_CmsPrincipal#PRINCIPAL_GROUP}</code></li>
     *      </ul>
     * @param principalName name of the principal
     * @param allowedPermissions bit set of allowed permissions
     * @param deniedPermissions bit set of denied permissions
     * @param flags additional flags of the access control entry
     * 
     * @throws CmsException if something goes wrong
     */
    public void chacc(
        String resourceName,
        String principalType,
        String principalName,
        int allowedPermissions,
        int deniedPermissions,
        int flags) throws CmsException {

        CmsResource res = readResource(resourceName, CmsResourceFilter.ALL);

        CmsAccessControlEntry acEntry = null;
        try {
            I_CmsPrincipal principal = CmsPrincipal.readPrincipal(this, principalType, principalName);
            acEntry = new CmsAccessControlEntry(
                res.getResourceId(),
                principal.getId(),
                allowedPermissions,
                deniedPermissions,
                flags);
            acEntry.setFlagsForPrincipal(principal);
        } catch (CmsDbEntryNotFoundException e) {
            // check for special ids
            if (principalName.equalsIgnoreCase(CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME)) {
                acEntry = new CmsAccessControlEntry(
                    res.getResourceId(),
                    CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID,
                    allowedPermissions,
                    deniedPermissions,
                    flags);
                acEntry.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_ALLOTHERS);
            } else if (principalName.equalsIgnoreCase(CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_NAME)) {
                acEntry = new CmsAccessControlEntry(
                    res.getResourceId(),
                    CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID,
                    allowedPermissions,
                    deniedPermissions,
                    flags);
                acEntry.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE_ALL);
            } else if (principalType.equalsIgnoreCase(CmsRole.PRINCIPAL_ROLE)) {
                // only vfs managers can set role based permissions
                m_securityManager.checkRoleForResource(m_context, CmsRole.VFS_MANAGER, res);
                // check for role
                CmsRole role = CmsRole.valueOfRoleName(principalName);
                // role based permissions can only be set in the system folder
                if ((role == null) || (!res.getRootPath().startsWith(CmsWorkplace.VFS_PATH_SYSTEM))) {
                    throw e;
                }
                acEntry = new CmsAccessControlEntry(
                    res.getResourceId(),
                    role.getId(),
                    allowedPermissions,
                    deniedPermissions,
                    flags);
                acEntry.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_ROLE);
            } else {
                throw e;
            }
        }

        m_securityManager.writeAccessControlEntry(m_context, res, acEntry);
    }

    /**
     * Changes the access control for a given resource and a given principal(user/group).<p>
     * 
     * @param resourceName name of the resource
     * @param principalType the type of the principal (group or user):
     *      <ul>
     *          <li><code>{@link I_CmsPrincipal#PRINCIPAL_USER}</code></li>
     *          <li><code>{@link I_CmsPrincipal#PRINCIPAL_GROUP}</code></li>
     *      </ul>
     * @param principalName name of the principal
     * @param permissionString the permissions in the format ((+|-)(r|w|v|c|i|o))*
     * 
     * @throws CmsException if something goes wrong
     */
    public void chacc(String resourceName, String principalType, String principalName, String permissionString)
    throws CmsException {

        CmsResource res = readResource(resourceName, CmsResourceFilter.ALL);

        CmsAccessControlEntry acEntry = null;
        try {
            I_CmsPrincipal principal = CmsPrincipal.readPrincipal(this, principalType, principalName);
            acEntry = new CmsAccessControlEntry(res.getResourceId(), principal.getId(), permissionString);
            acEntry.setFlagsForPrincipal(principal);
        } catch (CmsDbEntryNotFoundException e) {
            // check for special ids
            if (principalName.equalsIgnoreCase(CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME)) {
                acEntry = new CmsAccessControlEntry(
                    res.getResourceId(),
                    CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID,
                    permissionString);
                acEntry.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_ALLOTHERS);
            } else if (principalName.equalsIgnoreCase(CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_NAME)) {
                acEntry = new CmsAccessControlEntry(
                    res.getResourceId(),
                    CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID,
                    permissionString);
                acEntry.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE_ALL);
            } else if (principalType.equalsIgnoreCase(CmsRole.PRINCIPAL_ROLE)) {
                // only vfs managers can set role based permissions
                m_securityManager.checkRoleForResource(m_context, CmsRole.VFS_MANAGER, res);
                // check for role
                CmsRole role = CmsRole.valueOfRoleName(principalName);
                // role based permissions can only be set in the system folder
                if ((role == null)
                    || (!res.getRootPath().startsWith(CmsWorkplace.VFS_PATH_SYSTEM) && !res.getRootPath().equals("/") && !res.getRootPath().equals(
                        "/system"))) {
                    throw e;
                }
                acEntry = new CmsAccessControlEntry(res.getResourceId(), role.getId(), permissionString);
                acEntry.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_ROLE);
            } else {
                throw e;
            }
        }

        m_securityManager.writeAccessControlEntry(m_context, res, acEntry);
    }

    /**
     * Changes the lock of a resource to the current user,
     * that is "steals" the lock from another user.<p>
     * 
     * This is the "steal lock" operation.<p>
     * 
     * @param resourcename the name of the resource to change the lock with complete path
     * 
     * @throws CmsException if something goes wrong
     */
    public void changeLock(String resourcename) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        getResourceType(resource).changeLock(this, m_securityManager, resource);
    }

    /**
     * Returns a list with all sub resources of a given folder that have set the given property, 
     * matching the current property's value with the given old value and replacing it by a given new value.<p>
     *
     * @param resourcename the name of the resource to change the property value
     * @param property the name of the property to change the value
     * @param oldValue the old value of the property, can be a regular expression
     * @param newValue the new value of the property
     * @param recursive if true, change recursively all property values on sub-resources (only for folders)
     *
     * @return a list with the <code>{@link CmsResource}</code>'s where the property value has been changed
     *
     * @throws CmsException if operation was not successful
     */
    public List changeResourcesInFolderWithProperty(
        String resourcename,
        String property,
        String oldValue,
        String newValue,
        boolean recursive) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        return m_securityManager.changeResourcesInFolderWithProperty(
            m_context,
            resource,
            property,
            oldValue,
            newValue,
            recursive);
    }

    /**
     * Checks if the given base publish list can be published by the current user.<p>
     * 
     * @param publishList the base publish list to check
     * 
     * @throws CmsException in case the publish permissions are not granted
     * 
     * @deprecated notice that checking is no longer possible from the CmsObject
     */
    public void checkPublishPermissions(CmsPublishList publishList) throws CmsException {

        m_securityManager.checkPublishPermissions(m_context, publishList);
    }

    /**
     * Checks if the user of this OpenCms context is a member of the given role.<p>
     *  
     * This method can only be used for roles that are not organizational unit dependent.<p>
     *  
     * @param role the role to check
     * 
     * @throws CmsRoleViolationException if the user does not have the required role permissions
     * 
     * @see CmsRole#isOrganizationalUnitIndependent()
     *      
     * @deprecated use {@link OpenCms#getRoleManager()} methods instead
     */
    public void checkRole(CmsRole role) throws CmsRoleViolationException {

        OpenCms.getRoleManager().checkRole(this, role);
    }

    /**
     * Changes the resource flags of a resource.<p>
     * 
     * The resource flags are used to indicate various "special" conditions
     * for a resource. Most notably, the "internal only" setting which signals 
     * that a resource can not be directly requested with it's URL.<p>
     *
     * @param resourcename the name of the resource to change the flags for (full current site relative path)
     * @param flags the new flags for this resource
     *
     * @throws CmsException if something goes wrong
     */
    public void chflags(String resourcename, int flags) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource).chflags(this, m_securityManager, resource, flags);
    }

    /**
     * Changes the resource type of a resource.<p>
     * 
     * OpenCms handles resources according to the resource type,
     * not the file suffix. This is e.g. why a JSP in OpenCms can have the 
     * suffix ".html" instead of ".jsp" only. Changing the resource type
     * makes sense e.g. if you want to make a plain text file a JSP resource,
     * or a binary file an image, etc.<p> 
     *
     * @param resourcename the name of the resource to change the type for (full current site relative path)
     * @param type the new resource type for this resource
     *
     * @throws CmsException if something goes wrong
     */
    public void chtype(String resourcename, int type) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource).chtype(this, m_securityManager, resource, type);
    }

    /**
     * Copies a resource.<p>
     * 
     * The copied resource will always be locked to the current user
     * after the copy operation.<p>
     * 
     * Siblings will be treated according to the
     * <code>{@link org.opencms.file.CmsResource#COPY_PRESERVE_SIBLING}</code> mode.<p>
     * 
     * @param source the name of the resource to copy (full current site relative path)
     * @param destination the name of the copy destination (full current site relative path)
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the <code>destination</code> argument is null or of length 0
     * 
     * @see #copyResource(String, String, CmsResource.CmsResourceCopyMode)
     */
    public void copyResource(String source, String destination) throws CmsException, CmsIllegalArgumentException {

        copyResource(source, destination, CmsResource.COPY_PRESERVE_SIBLING);
    }

    /**
     * Copies a resource.<p>
     * 
     * The copied resource will always be locked to the current user
     * after the copy operation.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the copy operation.<br>
     * Possible values for this parameter are: <br>
     * <ul>
     * <li><code>{@link CmsResource#COPY_AS_NEW}</code></li>
     * <li><code>{@link CmsResource#COPY_AS_SIBLING}</code></li>
     * <li><code>{@link CmsResource#COPY_PRESERVE_SIBLING}</code></li>
     * </ul><p>
     * 
     * @param source the name of the resource to copy (full current site relative path)
     * @param destination the name of the copy destination (full current site relative path)
     * @param siblingMode indicates how to handle siblings during copy
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the <code>destination</code> argument is null or of length 0
     */
    public void copyResource(String source, String destination, CmsResource.CmsResourceCopyMode siblingMode)
    throws CmsException, CmsIllegalArgumentException {

        CmsResource resource = readResource(source, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource).copyResource(this, m_securityManager, resource, destination, siblingMode);
    }

    /**
     * Copies a resource.<p>
     * 
     * The copied resource will always be locked to the current user
     * after the copy operation.<p>
     * 
     * @param source the name of the resource to copy (full path)
     * @param destination the name of the copy destination (full path)
     * @param siblingMode indicates how to handle siblings during copy
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the <code>destination</code> argument is null or of length 0
     * 
     * @deprecated use {@link #copyResource(String, String, CmsResource.CmsResourceCopyMode)} method instead
     */
    public void copyResource(String source, String destination, int siblingMode)
    throws CmsException, CmsIllegalArgumentException {

        copyResource(source, destination, CmsResource.CmsResourceCopyMode.valueOf(siblingMode));
    }

    /**
     * Copies a resource to the current project of the user.<p>
     * 
     * This is used to extend the current users project with the
     * specified resource, in case that the resource is not yet part of the project.
     * The resource is not really copied like in a regular copy operation, 
     * it is in fact only "enabled" in the current users project.<p>   
     * 
     * @param resourcename the name of the resource to copy to the current project (full current site relative path)
     * 
     * @throws CmsException if something goes wrong
     */
    public void copyResourceToProject(String resourcename) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        getResourceType(resource).copyResourceToProject(this, m_securityManager, resource);
    }

    /**
     * Counts the locked resources in a project.<p>
     *
     * @param id the id of the project
     * 
     * @return the number of locked resources in this project
     *
     * @throws CmsException if operation was not successful
     */
    public int countLockedResources(CmsUUID id) throws CmsException {

        return m_securityManager.countLockedResources(m_context, id);
    }

    /**
     * Counts the locked resources in a project.<p>
     *
     * @param id the id of the project
     * 
     * @return the number of locked resources in this project
     *
     * @throws CmsException if operation was not successful
     * 
     * @deprecated use {@link #countLockedResources(CmsUUID)} instead
     */
    public int countLockedResources(int id) throws CmsException {

        return countLockedResources(m_securityManager.getProjectId(m_context, id));
    }

    /**
     * Copies access control entries of a given resource to another resource.<p>
     * 
     * Already existing access control entries of the destination resource are removed.<p>
     * 
     * @param sourceName the name of the resource of which the access control entries are copied
     * @param destName the name of the resource to which the access control entries are applied
     * 
     * @throws CmsException if something goes wrong
     */
    public void cpacc(String sourceName, String destName) throws CmsException {

        CmsResource source = readResource(sourceName);
        CmsResource dest = readResource(destName);
        m_securityManager.copyAccessControlEntries(m_context, source, dest);
    }

    /**
     * Creates a new user group.<p>
     * 
     * @param groupFqn the name of the new group
     * @param description the description of the new group
     * @param flags the flags for the new group
     * @param parent the parent group (or <code>null</code>)
     *
     * @return a <code>{@link CmsGroup}</code> object representing the newly created group
     *
     * @throws CmsException if operation was not successful
     */
    public CmsGroup createGroup(String groupFqn, String description, int flags, String parent) throws CmsException {

        return m_securityManager.createGroup(m_context, groupFqn, description, flags, parent);
    }

    /**
     * Creates a new project.<p>
     *
     * @param name the name of the project to create
     * @param description the description for the new project
     * @param groupname the name of the project user group
     * @param managergroupname the name of the project manager group
     * 
     * @return the created project
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsProject createProject(String name, String description, String groupname, String managergroupname)
    throws CmsException {

        return m_securityManager.createProject(
            m_context,
            name,
            description,
            groupname,
            managergroupname,
            CmsProject.PROJECT_TYPE_NORMAL);
    }

    /**
     * Creates a new project.<p>
     *
     * @param name the name of the project to create
     * @param description the description for the new project
     * @param groupname the name of the project user group
     * @param managergroupname the name of the project manager group
     * @param projecttype the type of the project (normal or temporary)
     * 
     * @return the created project
     * 
     * @throws CmsException if operation was not successful
     */
    public CmsProject createProject(
        String name,
        String description,
        String groupname,
        String managergroupname,
        CmsProject.CmsProjectType projecttype) throws CmsException {

        return m_securityManager.createProject(m_context, name, description, groupname, managergroupname, projecttype);
    }

    /**
     * Creates a new project.<p>
     *
     * @param name the name of the project to create
     * @param description the description for the new project
     * @param groupname the name of the project user group
     * @param managergroupname the name of the project manager group
     * @param projecttype the type of the project (normal or temporary)
     * 
     * @return the created project
     * 
     * @throws CmsException if operation was not successful
     * @deprecated use {@link #createProject(String,String,String,String,CmsProject.CmsProjectType)} method instead
     */
    public CmsProject createProject(
        String name,
        String description,
        String groupname,
        String managergroupname,
        int projecttype) throws CmsException {

        return createProject(
            name,
            description,
            groupname,
            managergroupname,
            CmsProject.CmsProjectType.valueOf(projecttype));
    }

    /**
     * Creates a property definition.<p>
     *
     * Property definitions are valid for all resource types.<p>
     * 
     * @param name the name of the property definition to create
     * 
     * @return the created property definition
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsPropertyDefinition createPropertyDefinition(String name) throws CmsException {

        return (m_securityManager.createPropertyDefinition(m_context, name));
    }

    /**
     * Creates a new resource of the given resource type with 
     * empty content and no properties.<p>
     * 
     * @param resourcename the name of the resource to create (full current site relative path)
     * @param type the type of the resource to create
     * 
     * @return the created resource
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the given <code>resourcename</code> is null or of length 0
     * 
     * @see #createResource(String, int, byte[], List)
     */
    public CmsResource createResource(String resourcename, int type) throws CmsException, CmsIllegalArgumentException {

        return createResource(resourcename, type, new byte[0], Collections.EMPTY_LIST);
    }

    /**
     * Creates a new resource of the given resource type
     * with the provided content and properties.<p>
     * 
     * @param resourcename the name of the resource to create (full current site relative path)
     * @param type the type of the resource to create
     * @param content the contents for the new resource
     * @param properties the properties for the new resource
     * 
     * @return the created resource
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the <code>resourcename</code> argument is null or of length 0
     */
    public CmsResource createResource(String resourcename, int type, byte[] content, List properties)
    throws CmsException, CmsIllegalArgumentException {

        return getResourceType(type).createResource(this, m_securityManager, resourcename, content, properties);
    }

    /**
     * Creates a new sibling of the source resource.<p>
     * 
     * @param source the name of the resource to create a sibling for with complete path
     * @param destination the name of the sibling to create with complete path
     * @param properties the individual properties for the new sibling
     * 
     * @return the new created sibling
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsResource createSibling(String source, String destination, List properties) throws CmsException {

        CmsResource resource = readResource(source, CmsResourceFilter.IGNORE_EXPIRATION);
        return getResourceType(resource).createSibling(this, m_securityManager, resource, destination, properties);
    }

    /**
     * Creates the project for the temporary workplace files.<p>
     *
     * @return the created project for the temporary workplace files
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsProject createTempfileProject() throws CmsException {

        return m_securityManager.createTempfileProject(m_context);
    }

    /**
     * Creates a new user.<p>
     * 
     * @param userFqn the name for the new user
     * @param password the password for the new user
     * @param description the description for the new user
     * @param additionalInfos the additional infos for the user
     *
     * @return the created user
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsUser createUser(String userFqn, String password, String description, Map additionalInfos)
    throws CmsException {

        return m_securityManager.createUser(m_context, userFqn, password, description, additionalInfos);
    }

    /**
     * Deletes all published resource entries.<p>
     * 
     * @param linkType the type of resource deleted (0= non-parameter, 1=parameter)
     * 
     * @throws CmsException if something goes wrong
     */
    public void deleteAllStaticExportPublishedResources(int linkType) throws CmsException {

        m_securityManager.deleteAllStaticExportPublishedResources(m_context, linkType);
    }

    /**
     * Deletes the versions from the backup tables that are older then the given time stamp  
     * and/or number of remaining versions.<p>
     * 
     * The number of versions always wins, i.e. if the given time stamp would delete more versions 
     * than given in the versions parameter, the time stamp will be ignored. <p>
     * 
     * Deletion will delete file header, content and properties. <p>
     * 
     * @param timestamp time stamp which defines the date after which backup resources must be deleted.
     *                  This parameter must be 0 if the backup should be deleted by number of version
     * @param versions the number of versions per file which should kept in the system. 
     * @param report the report for output logging
     * 
     * @throws CmsException if something goes wrong
     * 
     * @deprecated use {@link #deleteHistoricalVersions(int, int, long, I_CmsReport)} instead,
     *             notice that there is no longer possible to delete historical versions by date
     */
    public void deleteBackups(long timestamp, int versions, I_CmsReport report) throws CmsException {

        if (timestamp != 0) {
            if (versions == 0) {
                // use default value
                versions = OpenCms.getSystemInfo().getHistoryVersions();
            }
        }
        deleteHistoricalVersions(versions, versions, timestamp, report);
    }

    /**
     * Deletes a group, where all permissions, users and children of the group
     * are transfered to a replacement group.<p>
     * 
     * @param groupId the id of the group to be deleted
     * @param replacementId the id of the group to be transfered, can be <code>null</code>
     *
     * @throws CmsException if operation was not successful
     */
    public void deleteGroup(CmsUUID groupId, CmsUUID replacementId) throws CmsException {

        m_securityManager.deleteGroup(m_context, groupId, replacementId);
    }

    /**
     * Deletes a user group.<p>
     *
     * Only groups that contain no subgroups can be deleted.<p>
     * 
     * @param group the name of the group
     * 
     * @throws CmsException if operation was not successful
     */
    public void deleteGroup(String group) throws CmsException {

        m_securityManager.deleteGroup(m_context, group);
    }

    /**
     * Deletes the versions from the history tables, keeping the given number of versions per resource.<p>
     * 
     * @deprecated this method has been replaced by <code>{@link #deleteHistoricalVersions(int, int, long, I_CmsReport)}</code> 
     *      because it works globally (not site - dependent) and the folder argument is misleading. 
     * 
     * @param folderName will not be used as this operation is global (this method is deprecated due to the misleading signature) 
     * @param versionsToKeep number of versions to keep, is ignored if negative 
     * @param versionsDeleted number of versions to keep for deleted resources, is ignored if negative
     * @param timeDeleted deleted resources older than this will also be deleted, is ignored if negative
     * @param report the report for output logging
     * 
     * @throws CmsException if operation was not successful
     */
    public void deleteHistoricalVersions(
        String folderName,
        int versionsToKeep,
        int versionsDeleted,
        long timeDeleted,
        I_CmsReport report) throws CmsException {

        deleteHistoricalVersions(versionsToKeep, versionsDeleted, timeDeleted, report);
    }

    /**
     * Deletes the versions from the history tables, keeping the given number of versions per resource.<p>
     * 
     * @param versionsToKeep number of versions to keep, is ignored if negative 
     * @param versionsDeleted number of versions to keep for deleted resources, is ignored if negative
     * @param timeDeleted deleted resources older than this will also be deleted, is ignored if negative
     * @param report the report for output logging
     * 
     * @throws CmsException if operation was not successful
     */
    public void deleteHistoricalVersions(int versionsToKeep, int versionsDeleted, long timeDeleted, I_CmsReport report)
    throws CmsException {

        m_securityManager.deleteHistoricalVersions(m_context, versionsToKeep, versionsDeleted, timeDeleted, report);
    }

    /**
     * Deletes a project.<p>
     *
     * All resources inside the project have to be be reset to their online state.<p>
     * 
     * @param id the id of the project to delete
     *
     * @throws CmsException if operation was not successful
     */
    public void deleteProject(CmsUUID id) throws CmsException {

        m_securityManager.deleteProject(m_context, id);
    }

    /**
     * Deletes a project.<p>
     *
     * All resources inside the project have to be be reset to their online state.<p>
     * 
     * @param id the id of the project to delete
     *
     * @throws CmsException if operation was not successful
     * 
     * @deprecated use {@link #deleteProject(CmsUUID)} instead
     */
    public void deleteProject(int id) throws CmsException {

        deleteProject(m_securityManager.getProjectId(m_context, id));
    }

    /**
     * Deletes a property for a file or folder.<p>
     *
     * @param resourcename the name of a resource for which the property should be deleted
     * @param key the name of the property
     * 
     * @throws CmsException if something goes wrong
     * 
     * @deprecated use <code>{@link #writePropertyObject(String, CmsProperty)}</code> instead.
     */
    public void deleteProperty(String resourcename, String key) throws CmsException {

        CmsProperty property = new CmsProperty();
        property.setName(key);
        property.setStructureValue(CmsProperty.DELETE_VALUE);

        writePropertyObject(resourcename, property);
    }

    /**
     * Deletes a property definition.<p>
     *
     * @param name the name of the property definition to delete
     *
     * @throws CmsException if something goes wrong
     */
    public void deletePropertyDefinition(String name) throws CmsException {

        m_securityManager.deletePropertyDefinition(m_context, name);
    }

    /**
     * Deletes the relations to a given resource.<p>
     *
     * @param resourceName the resource to delete the relations from
     * @param filter the filter to use for deleting the relations
     *
     * @throws CmsException if something goes wrong
     */
    public void deleteRelationsFromResource(String resourceName, CmsRelationFilter filter) throws CmsException {

        CmsResource resource = readResource(resourceName, CmsResourceFilter.ALL);
        m_securityManager.deleteRelationsForResource(m_context, resource, filter);
    }

    /**
     * Deletes a resource given its name.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the delete operation.<br>
     * Possible values for this parameter are: <br>
     * <ul>
     * <li><code>{@link CmsResource#DELETE_REMOVE_SIBLINGS}</code></li>
     * <li><code>{@link CmsResource#DELETE_PRESERVE_SIBLINGS}</code></li>
     * </ul><p>
     * 
     * @param resourcename the name of the resource to delete (full current site relative path)
     * @param siblingMode indicates how to handle siblings of the deleted resource
     *
     * @throws CmsException if something goes wrong
     */
    public void deleteResource(String resourcename, CmsResource.CmsResourceDeleteMode siblingMode) throws CmsException {

        // throw the exception if resource name is an empty string
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(resourcename)) {
            throw new CmsVfsResourceNotFoundException(Messages.get().container(
                Messages.ERR_DELETE_RESOURCE_1,
                resourcename));
        }

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource).deleteResource(this, m_securityManager, resource, siblingMode);
    }

    /**
     * Deletes a resource given its name.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the delete operation.<br>
     * 
     * @param resourcename the name of the resource to delete (full path)
     * @param siblingMode indicates how to handle siblings of the deleted resource
     *
     * @throws CmsException if something goes wrong
     *      
     * @deprecated use {@link #deleteResource(String, CmsResource.CmsResourceDeleteMode)} method instead
     */
    public void deleteResource(String resourcename, int siblingMode) throws CmsException {

        deleteResource(resourcename, CmsResource.CmsResourceDeleteMode.valueOf(siblingMode));
    }

    /**
     * Deletes a published resource entry.<p>
     * 
     * @param resourceName The name of the resource to be deleted in the static export
     * @param linkType the type of resource deleted (0= non-parameter, 1=parameter)
     * @param linkParameter the parameters of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public void deleteStaticExportPublishedResource(String resourceName, int linkType, String linkParameter)
    throws CmsException {

        m_securityManager.deleteStaticExportPublishedResource(m_context, resourceName, linkType, linkParameter);
    }

    /**
     * Deletes a user.<p>
     *
     * @param userId the id of the user to be deleted
     *
     * @throws CmsException if operation was not successful
     */
    public void deleteUser(CmsUUID userId) throws CmsException {

        m_securityManager.deleteUser(m_context, userId);
    }

    /**
     * Deletes a user, where all permissions and resources attributes of the user
     * were transfered to a replacement user.<p>
     *
     * @param userId the id of the user to be deleted
     * @param replacementId the id of the user to be transfered, can be <code>null</code>
     *
     * @throws CmsException if operation was not successful
     */
    public void deleteUser(CmsUUID userId, CmsUUID replacementId) throws CmsException {

        m_securityManager.deleteUser(m_context, userId, replacementId);
    }

    /**
     * Deletes a user.<p>
     * 
     * @param username the name of the user to be deleted
     *
     * @throws CmsException if operation was not successful
     */
    public void deleteUser(String username) throws CmsException {

        m_securityManager.deleteUser(m_context, username);
    }

    /**
     * Deletes a web user.<p>
     *
     * @param userId the id of the user to be deleted
     *
     * @throws CmsException if operation was not successful
     * 
     * @deprecated there are no more web users, use a user without any role!
     */
    public void deleteWebUser(CmsUUID userId) throws CmsException {

        m_securityManager.deleteUser(m_context, userId);
    }

    /**
     * Checks the availability of a resource in the VFS,
     * using the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p> 
     *
     * A resource may be of type <code>{@link CmsFile}</code> or 
     * <code>{@link CmsFolder}</code>.<p>
     * 
     * This method also takes into account the user permissions, so if 
     * the given resource exists, but the current user has not the required 
     * permissions, then this method will return <code>false</code>.<p>
     *
     * @param resourcename the name of the resource to check (full current site relative path)
     *
     * @return <code>true</code> if the resource is available
     *
     * @see #readResource(String)
     * @see #existsResource(String, CmsResourceFilter)
     */
    public boolean existsResource(String resourcename) {

        return existsResource(resourcename, CmsResourceFilter.ALL);
    }

    /**
     * Checks the availability of a resource in the VFS,
     * using the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p> 
     *
     * A resource may be of type <code>{@link CmsFile}</code> or 
     * <code>{@link CmsFolder}</code>.<p>  
     *
     * The specified filter controls what kind of resources should be "found" 
     * during the read operation. This will depend on the application. For example, 
     * using <code>{@link CmsResourceFilter#DEFAULT}</code> will only return currently
     * "valid" resources, while using <code>{@link CmsResourceFilter#IGNORE_EXPIRATION}</code>
     * will ignore the date release / date expired information of the resource.<p>
     * 
     * This method also takes into account the user permissions, so if 
     * the given resource exists, but the current user has not the required 
     * permissions, then this method will return <code>false</code>.<p>
     *
     * @param resourcename the name of the resource to check (full current site relative path)
     * @param filter the resource filter to use while checking
     *
     * @return <code>true</code> if the resource is available
     * 
     * @see #readResource(String)
     * @see #readResource(String, CmsResourceFilter)
     */
    public boolean existsResource(String resourcename, CmsResourceFilter filter) {

        if (resourcename == null) {
            return false;
        }
        return m_securityManager.existsResource(m_context, addSiteRoot(resourcename), filter);
    }

    /**
     * Returns the list of access control entries of a resource given its name.<p>
     * 
     * @param resourceName the name of the resource
     * 
     * @return a list of <code>{@link CmsAccessControlEntry}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List getAccessControlEntries(String resourceName) throws CmsException {

        return getAccessControlEntries(resourceName, true);
    }

    /**
     * Returns the list of access control entries of a resource given its name.<p>
     * 
     * @param resourceName the name of the resource
     * @param getInherited <code>true</code>, if inherited access control entries should be returned, too
     * 
     * @return a list of <code>{@link CmsAccessControlEntry}</code> objects defining all permissions for the given resource
     * 
     * @throws CmsException if something goes wrong
     */
    public List getAccessControlEntries(String resourceName, boolean getInherited) throws CmsException {

        CmsResource res = readResource(resourceName, CmsResourceFilter.ALL);
        return m_securityManager.getAccessControlEntries(m_context, res, getInherited);
    }

    /**
     * Returns the access control list (summarized access control entries) of a given resource.<p>
     * 
     * @param resourceName the name of the resource
     * 
     * @return the access control list of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsAccessControlList getAccessControlList(String resourceName) throws CmsException {

        return getAccessControlList(resourceName, false);
    }

    /**
     * Returns the access control list (summarized access control entries) of a given resource.<p>
     * 
     * If <code>inheritedOnly</code> is set, only inherited access control entries are returned.<p>
     * 
     * @param resourceName the name of the resource
     * @param inheritedOnly if set, the non-inherited entries are skipped
     * 
     * @return the access control list of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsAccessControlList getAccessControlList(String resourceName, boolean inheritedOnly) throws CmsException {

        CmsResource res = readResource(resourceName, CmsResourceFilter.ALL);
        return m_securityManager.getAccessControlList(m_context, res, inheritedOnly);
    }

    /**
     * Returns all projects which are owned by the current user or which are 
     * accessible for the group of the user.<p>
     *
     * @return a list of objects of type <code>{@link CmsProject}</code>
     *
     * @throws CmsException if operation was not successful
     * 
     * @deprecated use {@link org.opencms.security.CmsOrgUnitManager#getAllAccessibleProjects(CmsObject, String, boolean) OpenCms.getOrgUnitManager().getAllAccessibleProjects(CmsObject, String, boolean)} instead
     */
    public List getAllAccessibleProjects() throws CmsException {

        return OpenCms.getOrgUnitManager().getAllAccessibleProjects(this, "", true);
    }

    /**
     * Returns a list with all projects from history.<p>
     *
     * @return list of <code>{@link CmsHistoryProject}</code> objects 
     *           with all projects from history.
     *
     * @throws CmsException  if operation was not successful
     * 
     * @deprecated Use {@link #getAllHistoricalProjects()} instead
     */
    public List getAllBackupProjects() throws CmsException {

        return getAllHistoricalProjects();
    }

    /**
     * Returns a list with all projects from history.<p>
     *
     * @return list of <code>{@link CmsHistoryProject}</code> objects 
     *           with all projects from history
     *
     * @throws CmsException  if operation was not successful
     */
    public List getAllHistoricalProjects() throws CmsException {

        return m_securityManager.getAllHistoricalProjects(m_context);
    }

    /**
     * Returns all projects which are owned by the current user or which are manageable
     * for the group of the user.<p>
     *
     * @return a list of objects of type <code>{@link CmsProject}</code>
     *
     * @throws CmsException if operation was not successful
     * 
     * @deprecated use {@link org.opencms.security.CmsOrgUnitManager#getAllManageableProjects(CmsObject, String, boolean) OpenCms.getOrgUnitManager().getAllManageableProjects(CmsObject, String, boolean)} instead
     */
    public List getAllManageableProjects() throws CmsException {

        return OpenCms.getOrgUnitManager().getAllManageableProjects(this, "", true);
    }

    /**
     * Returns the next version id for the published backup resources.<p>
     *
     * @return int the new version id
     * 
     * @throws CmsException if operation was not successful
     * 
     * @deprecated this concept has been abandoned for OpenCms version 7
     */
    public int getBackupTagId() throws CmsException {

        return ((CmsHistoryProject)getAllHistoricalProjects().get(0)).getPublishTag() + 1;
    }

    /**
     * Returns all child groups of a group.<p>
     *
     * @param groupname the name of the group
     * 
     * @return a list of all child <code>{@link CmsGroup}</code> objects or <code>null</code>
     * 
     * @throws CmsException if operation was not successful
     * 
     * @deprecated use {@link #getChildren(String, boolean)} with <code>false</code> instead.
     */
    public List getChild(String groupname) throws CmsException {

        return getChildren(groupname, false);
    }

    /**
     * Returns all child groups of a group.<p>
     * 
     * @param groupname the name of the group
     * @param includeSubChildren if set also returns all sub-child groups of the given group
     *
     * @return a list of all child <code>{@link CmsGroup}</code> objects or <code>null</code>
     * 
     * @throws CmsException if operation was not successful
     */
    public List getChildren(String groupname, boolean includeSubChildren) throws CmsException {

        return m_securityManager.getChildren(m_context, groupname, includeSubChildren);
    }

    /**
     * Returns all child groups of a group.<p>
     * 
     * This method also returns all sub-child groups of the current group.<p>
     * 
     * @param groupname the name of the group
     * 
     * @return a list of all child <code>{@link CmsGroup}</code> objects or <code>null</code>
     * 
     * @throws CmsException if operation was not successful
     * 
     * @deprecated use {@link #getChildren(String, boolean)} with <code>true</code> instead.
     */
    public List getChilds(String groupname) throws CmsException {

        return getChildren(groupname, true);
    }

    /**
     * Returns all groups to which a given user directly belongs.<p>
     *
     * @param username the name of the user to get all groups for
     * 
     * @return a list of <code>{@link CmsGroup}</code> objects
     *
     * @throws CmsException if operation was not successful
     * 
     * @deprecated use {@link #getGroupsOfUser(String, boolean)} instead
     */
    public List getDirectGroupsOfUser(String username) throws CmsException {

        return getGroupsOfUser(username, true);
    }

    /**
     * Returns all file resources contained in a folder.<p>
     * 
     * The result is filtered according to the rules of 
     * the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p>
     * 
     * @param resourcename the full current site relative path of the resource to return the child resources for
     * 
     * @return a list of all child files as <code>{@link CmsResource}</code> objects
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #getFilesInFolder(String, CmsResourceFilter)
     */
    public List getFilesInFolder(String resourcename) throws CmsException {

        return getFilesInFolder(resourcename, CmsResourceFilter.DEFAULT);
    }

    /**
     * Returns all file resources contained in a folder.<p>
     * 
     * With the <code>{@link CmsResourceFilter}</code> provided as parameter
     * you can control if you want to include deleted, invisible or 
     * time-invalid resources in the result.<p>
     * 
     * @param resourcename the full path of the resource to return the child resources for
     * @param filter the resource filter to use
     * 
     * @return a list of all child file as <code>{@link CmsResource}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List getFilesInFolder(String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return m_securityManager.readChildResources(m_context, resource, filter, false, true);
    }

    /**
     * Returns all groups.<p>
     *
     * @return a list of all <code>{@link CmsGroup}</code> objects
     *
     * @throws CmsException if operation was not successful
     * 
     * @deprecated use {@link org.opencms.security.CmsOrgUnitManager#getGroups(CmsObject, String, boolean) OpenCms.getOrgUnitManager().getGroups(CmsObject, String, boolean)} instead
     */
    public List getGroups() throws CmsException {

        return OpenCms.getOrgUnitManager().getGroups(this, "", true);
    }

    /**
     * Returns all the groups the given user, directly or indirectly, belongs to.<p>
     *
     * @param username the name of the user
     * 
     * @return a list of <code>{@link CmsGroup}</code> objects
     * 
     * @throws CmsException if operation was not successful
     * 
     * @deprecated use {@link #getGroupsOfUser(String, boolean)} instead
     */
    public List getGroupsOfUser(String username) throws CmsException {

        return getGroupsOfUser(username, false);
    }

    /**
     * Returns all the groups the given user belongs to.<p>
     *
     * @param username the name of the user
     * @param directGroupsOnly if set only the direct assigned groups will be returned, if not also indirect roles
     * 
     * @return a list of <code>{@link CmsGroup}</code> objects
     * 
     * @throws CmsException if operation was not successful
     */
    public List getGroupsOfUser(String username, boolean directGroupsOnly) throws CmsException {

        return getGroupsOfUser(username, directGroupsOnly, true);
    }

    /**
     * Returns all the groups the given user belongs to.<p>
     *
     * @param username the name of the user
     * @param directGroupsOnly if set only the direct assigned groups will be returned, if not also indirect roles
     * @param includeOtherOus if to include groups of other organizational units
     * 
     * @return a list of <code>{@link CmsGroup}</code> objects
     * 
     * @throws CmsException if operation was not successful
     */
    public List getGroupsOfUser(String username, boolean directGroupsOnly, boolean includeOtherOus) throws CmsException {

        return getGroupsOfUser(username, directGroupsOnly, includeOtherOus, m_context.getRemoteAddress());
    }

    /**
     * Returns the groups of a user filtered by the specified IP address.<p>
     *
     * @param username the name of the user
     * @param directGroupsOnly if set only the direct assigned groups will be returned, if not also indirect roles
     * @param remoteAddress the IP address to filter the groups in the result list
     * @param includeOtherOus if to include groups of other organizational units
     * 
     * @return a list of <code>{@link CmsGroup}</code> objects filtered by the specified IP address
     * 
     * @throws CmsException if operation was not successful
     */
    public List getGroupsOfUser(String username, boolean directGroupsOnly, boolean includeOtherOus, String remoteAddress)
    throws CmsException {

        return m_securityManager.getGroupsOfUser(m_context, username, (includeOtherOus
        ? ""
        : CmsOrganizationalUnit.getParentFqn(username)), includeOtherOus, false, directGroupsOnly, remoteAddress);
    }

    /**
     * Returns the groups of a user filtered by the specified IP address.<p>
     *
     * @param username the name of the user
     * @param remoteAddress the IP address to filter the groups in the result list
     * 
     * @return a list of <code>{@link CmsGroup}</code> objects filtered by the specified IP address
     * 
     * @throws CmsException if operation was not successful
     * 
     * @deprecated use {@link #getGroupsOfUser(String, boolean, boolean, String)} instead
     */
    public List getGroupsOfUser(String username, String remoteAddress) throws CmsException {

        return getGroupsOfUser(username, false, false, remoteAddress);
    }

    /**
     * Returns the edition lock state for a specified resource.<p>
     * 
     * If the resource is waiting to be publish you might get a lock of type {@link CmsLockType#PUBLISH}.<p>
     * 
     * @param resource the resource to return the edition lock state for
     * 
     * @return the edition lock state for the specified resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsLock getLock(CmsResource resource) throws CmsException {

        return m_securityManager.getLock(m_context, resource);
    }

    /**
     * Returns the lock state for a specified resource name.<p>
     * 
     * If the resource is waiting to be publish you might get a lock of type {@link CmsLockType#PUBLISH}.<p>
     * 
     * @param resourcename the name if the resource to get the lock state for (full current site relative path)
     * 
     * @return the lock state for the specified resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsLock getLock(String resourcename) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return getLock(resource);
    }

    /**
     * Returns all locked resources within a folder.<p>
     *
     * @param foldername the name of the folder
     * @param filter the lock filter
     * 
     * @return a list of locked resource paths (relative to current site)
     *
     * @throws CmsException if operation was not successful
     */
    public List getLockedResources(String foldername, CmsLockFilter filter) throws CmsException {

        CmsResource resource = readResource(foldername, CmsResourceFilter.ALL);
        return m_securityManager.getLockedResources(m_context, resource, filter);
    }

    /**
     * Returns the name a resource would have if it were moved to the
     * "lost and found" folder. <p>
     * 
     * In general, it is the same name as the given resource has, the only exception is
     * if a resource in the "lost and found" folder with the same name already exists. 
     * In such case, a counter is added to the resource name.<p>
     * 
     * @param resourcename the name of the resource to get the "lost and found" name for (full current site relative path)
     *
     * @return the tentative name of the resource inside the "lost and found" folder
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #moveToLostAndFound(String)
     */
    public String getLostAndFoundName(String resourcename) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return m_securityManager.moveToLostAndFound(m_context, resource, true);
    }

    /**
     * Returns the parent group of a group.<p>
     *
     * @param groupname the name of the group
     * 
     * @return group the parent group or <code>null</code>
     * 
     * @throws CmsException if operation was not successful
     */
    public CmsGroup getParent(String groupname) throws CmsException {

        return m_securityManager.getParent(m_context, groupname);
    }

    /**
     * Returns the set of permissions of the current user for a given resource.<p>
     * 
     * @param resourceName the name of the resource
     * 
     * @return the bit set of the permissions of the current user
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsPermissionSet getPermissions(String resourceName) throws CmsException {

        return getPermissions(resourceName, m_context.currentUser().getName());
    }

    /**
     * Returns the set of permissions of a given user for a given resource.<p>
     * 
     * @param resourceName the name of the resource
     * @param userName the name of the user
     * 
     * @return the current permissions on this resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsPermissionSet getPermissions(String resourceName, String userName) throws CmsException {

        // reading permissions is allowed even if the resource is marked as deleted
        CmsResource resource = readResource(resourceName, CmsResourceFilter.ALL);
        CmsUser user = readUser(userName);
        return m_securityManager.getPermissions(m_context, resource, user);
    }

    /**
     * Returns a publish list with all new/changed/deleted resources of the current (offline)
     * project that actually get published.<p>
     * 
     * @return a publish list
     * 
     * @throws CmsException if something goes wrong
     * 
     * @deprecated use <code>{@link OpenCms#getPublishManager()}.{@link org.opencms.publish.CmsPublishManager#getPublishList(CmsObject) getPublishList(CmsObject)}</code> instead
     */
    public CmsPublishList getPublishList() throws CmsException {

        return OpenCms.getPublishManager().getPublishList(this);
    }

    /**
     * Returns a publish list with all new/changed/deleted resources of the current (offline)
     * project that actually get published for a direct publish of a single resource.<p>
     * 
     * @param directPublishResource the resource which will be directly published
     * @param directPublishSiblings <code>true</code>, if all eventual siblings of the direct 
     *                      published resource should also get published.
     * 
     * @return a publish list
     * 
     * @throws CmsException if something goes wrong
     * 
     * @deprecated use <code>{@link OpenCms#getPublishManager()}.{@link org.opencms.publish.CmsPublishManager#getPublishList(CmsObject, CmsResource, boolean) getPublishList(CmsObject, CmsResource, boolean)}</code> instead
     */
    public CmsPublishList getPublishList(CmsResource directPublishResource, boolean directPublishSiblings)
    throws CmsException {

        return OpenCms.getPublishManager().getPublishList(this, directPublishResource, directPublishSiblings);
    }

    /**
     * Returns a publish list with all new/changed/deleted resources of the current (offline)
     * project that actually get published for a direct publish of a List of resources.<p>
     * 
     * @param directPublishResources the resources which will be directly published
     * @param directPublishSiblings <code>true</code>, if all eventual siblings of the direct 
     *                      published resources should also get published.
     * 
     * @return a publish list
     * 
     * @throws CmsException if something goes wrong
     * 
     * @deprecated use <code>{@link OpenCms#getPublishManager()}.{@link org.opencms.publish.CmsPublishManager#getPublishList(CmsObject, List, boolean) getPublishList(CmsObject, List, boolean)}</code> instead
     */
    public CmsPublishList getPublishList(List directPublishResources, boolean directPublishSiblings)
    throws CmsException {

        return OpenCms.getPublishManager().getPublishList(this, directPublishResources, directPublishSiblings, true);
    }

    /**
     * Returns a publish list with all new/changed/deleted resources of the current (offline)
     * project that actually get published for a direct publish of a List of resources.<p>
     * 
     * @param directPublishResources the {@link CmsResource} objects which will be directly published
     * @param directPublishSiblings <code>true</code>, if all eventual siblings of the direct 
     *                      published resources should also get published.
     * @param publishSubResources indicates if sub-resources in folders should be published (for direct publish only)
     * 
     * @return a publish list
     * 
     * @throws CmsException if something goes wrong
     * 
     * @deprecated use <code>{@link OpenCms#getPublishManager()}.{@link org.opencms.publish.CmsPublishManager#getPublishList(CmsObject, List, boolean, boolean) getPublishList(CmsObject, List, boolean)}</code> instead
     */
    public CmsPublishList getPublishList(
        List directPublishResources,
        boolean directPublishSiblings,
        boolean publishSubResources) throws CmsException {

        return OpenCms.getPublishManager().getPublishList(
            this,
            directPublishResources,
            directPublishSiblings,
            publishSubResources);
    }

    /**
     * Returns all relations for the given resource matching the given filter.<p> 
     * 
     * You should have view/read permissions on the given resource.<p>
     * 
     * You may become source and/or target paths to resource you do not have view/read permissions on.<p> 
     * 
     * @param resource the resource to retrieve the relations for
     * @param filter the filter to match the relation 
     * 
     * @return a List containing all {@link org.opencms.relations.CmsRelation} 
     *          objects for the given resource matching the given filter
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsSecurityManager#getRelationsForResource(CmsRequestContext, CmsResource, CmsRelationFilter)
     */
    public List getRelationsForResource(CmsResource resource, CmsRelationFilter filter) throws CmsException {

        return m_securityManager.getRelationsForResource(m_context, resource, filter);
    }

    /**
     * Returns all relations for the given resource matching the given filter.<p> 
     * 
     * You should have view/read permissions on the given resource.<p>
     * 
     * You may become source and/or target paths to resource you do not have view/read permissions on.<p> 
     * 
     * @param resourceName the name of the resource to retrieve the relations for
     * @param filter the filter to match the relation 
     * 
     * @return a List containing all {@link org.opencms.relations.CmsRelation} 
     *          objects for the given resource matching the given filter
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsSecurityManager#getRelationsForResource(CmsRequestContext, CmsResource, CmsRelationFilter)
     */
    public List getRelationsForResource(String resourceName, CmsRelationFilter filter) throws CmsException {

        return getRelationsForResource(readResource(resourceName, CmsResourceFilter.ALL), filter);
    }

    /**
     * Returns the current users request context.<p>
     *
     * This request context is used to authenticate the user for all 
     * OpenCms operations. It also contains the request runtime settings, e.g.
     * about the current site this request was made on.<p>
     *
     * @return the current users request context
     */
    public CmsRequestContext getRequestContext() {

        return m_context;
    }

    /**
     * Returns all resources associated to a given principal via an ACE with the given permissions.<p> 
     * 
     * If the <code>includeAttr</code> flag is set it returns also all resources associated to 
     * a given principal through some of following attributes.<p> 
     * 
     * <ul>
     *    <li>User Created</li>
     *    <li>User Last Modified</li>
     * </ul><p>
     * 
     * @param principalId the id of the principal
     * @param permissions a set of permissions to match, can be <code>null</code> for all ACEs
     * @param includeAttr a flag to include resources associated by attributes
     * 
     * @return a set of <code>{@link CmsResource}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public Set getResourcesForPrincipal(CmsUUID principalId, CmsPermissionSet permissions, boolean includeAttr)
    throws CmsException {

        return m_securityManager.getResourcesForPrincipal(getRequestContext(), principalId, permissions, includeAttr);
    }

    /**
     * Returns all child resources of a resource, that is the resources
     * contained in a folder.<p>
     * 
     * With the <code>{@link CmsResourceFilter}</code> provided as parameter
     * you can control if you want to include deleted, invisible or 
     * time-invalid resources in the result.<p>
     * 
     * This method is mainly used by the workplace explorer.<p>
     * 
     * @param resourcename the full current site relative path of the resource to return the child resources for
     * @param filter the resource filter to use
     * 
     * @return a list of all child <code>{@link CmsResource}</code>s
     * 
     * @throws CmsException if something goes wrong
     */
    public List getResourcesInFolder(String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return m_securityManager.readChildResources(m_context, resource, filter, true, true);
    }

    /**
     * Returns a list with all sub resources of the given parent folder (and all of it's subfolders) 
     * that have been modified in the given time range.<p>
     * 
     * The result list is descending sorted (newest resource first).<p>
     *
     * @param folder the folder to get the sub resources from
     * @param starttime the begin of the time range
     * @param endtime the end of the time range
     * 
     * @return a list with all <code>{@link CmsResource}</code> objects 
     *               that have been modified in the given time range.
     *
     * @throws CmsException if operation was not successful
     * 
     * @deprecated use {@link #readResources(String, CmsResourceFilter)} and create a filter 
     *      based on {@link CmsResourceFilter#IGNORE_EXPIRATION}
     *      using {@link CmsResourceFilter#addRequireLastModifiedAfter(long)} and
     *      {@link CmsResourceFilter#addRequireLastModifiedBefore(long)} instead
     */
    public List getResourcesInTimeRange(String folder, long starttime, long endtime) throws CmsException {

        CmsResourceFilter filter = CmsResourceFilter.IGNORE_EXPIRATION;
        filter = filter.addRequireLastModifiedAfter(starttime);
        filter = filter.addRequireLastModifiedBefore(endtime);

        return readResources(folder, filter);
    }

    /**
     * Adjusts the absolute resource root path for the current site.<p> 
     * 
     * The full root path of a resource is always available using
     * <code>{@link CmsResource#getRootPath()}</code>. From this name this method cuts 
     * of the current site root using 
     * <code>{@link CmsRequestContext#removeSiteRoot(String)}</code>.<p>
     * 
     * If the resource root path does not start with the current site root,
     * it is left untouched.<p>
     * 
     * @param resource the resource to get the adjusted site root path for
     * 
     * @return the absolute resource path adjusted for the current site
     * 
     * @see CmsRequestContext#removeSiteRoot(String)
     * @see CmsRequestContext#getSitePath(CmsResource)
     * @see CmsResource#getRootPath()
     */
    public String getSitePath(CmsResource resource) {

        return m_context.getSitePath(resource);
    }

    /**
     * Returns all folder resources contained in a folder.<p>
     * 
     * The result is filtered according to the rules of 
     * the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p>
     * 
     * @param resourcename the full current site relative path of the resource to return the child resources for. 
     * 
     * @return a list of all child file as <code>{@link CmsResource}</code> objects
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #getSubFolders(String, CmsResourceFilter)
     */
    public List getSubFolders(String resourcename) throws CmsException {

        return getSubFolders(resourcename, CmsResourceFilter.DEFAULT);
    }

    /**
     * Returns all folder resources contained in a folder.<p>
     * 
     * With the <code>{@link CmsResourceFilter}</code> provided as parameter
     * you can control if you want to include deleted, invisible or 
     * time-invalid resources in the result.<p>
     * 
     * @param resourcename the full current site relative path of the resource to return the child resources for. 
     * 
     * @return a list of all child folder <code>{@link CmsResource}</code>s
     * @param filter the resource filter to use
     * 
     * @throws CmsException if something goes wrong
     */
    public List getSubFolders(String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return m_securityManager.readChildResources(m_context, resource, filter, true, false);
    }

    /**
     * Returns all users.<p>
     *
     * @return a list of all <code>{@link CmsUser}</code> objects
     * 
     * @throws CmsException if operation was not successful
     * 
     * @deprecated use {@link org.opencms.security.CmsOrgUnitManager#getUsers(CmsObject, String, boolean) OpenCms.getOrgUnitManager().getUsersForOrganizationalUnit(CmsObject, String, boolean)} instead
     */
    public List getUsers() throws CmsException {

        return OpenCms.getOrgUnitManager().getUsers(this, "", true);
    }

    /**
     * Returns all direct users of a given group.<p>
     *
     * Users that are "indirectly" in the group are not returned in the result.<p>
     *
     * @param groupname the name of the group to get all users for
     * 
     * @return all <code>{@link CmsUser}</code> objects in the group
     *
     * @throws CmsException if operation was not successful
     */
    public List getUsersOfGroup(String groupname) throws CmsException {

        return getUsersOfGroup(groupname, true);
    }

    /**
     * Returns all direct users of a given group.<p>
     *
     * Users that are "indirectly" in the group are not returned in the result.<p>
     *
     * @param groupname the name of the group to get all users for
     * @param includeOtherOus if the result should include users of other ous
     * 
     * @return all <code>{@link CmsUser}</code> objects in the group
     *
     * @throws CmsException if operation was not successful
     */
    public List getUsersOfGroup(String groupname, boolean includeOtherOus) throws CmsException {

        return m_securityManager.getUsersOfGroup(m_context, groupname, includeOtherOus, true, false);
    }

    /**
     * Checks if the current user has required permissions to access a given resource.<p>
     * 
     * @param resource the resource to check the permissions for
     * @param requiredPermissions the set of permissions to check for
     * 
     * @return <code>true</code> if the required permissions are satisfied
     * 
     * @throws CmsException if something goes wrong
     */
    public boolean hasPermissions(CmsResource resource, CmsPermissionSet requiredPermissions) throws CmsException {

        return m_securityManager.hasPermissions(m_context, resource, requiredPermissions, true, CmsResourceFilter.ALL).isAllowed();
    }

    /**
     * Checks if the current user has required permissions to access a given resource.<p>
     * 
     * @param resource the resource to check the permissions for
     * @param requiredPermissions the set of permissions to check for
     * @param checkLock if <code>true</code>, a lock for the current user is required for 
     *      all write operations, if <code>false</code> it's ok to write as long as the resource
     *      is not locked by another user.
     * @param filter the resource filter to use
     * 
     * @return <code>true</code> if the required permissions are satisfied
     * 
     * @throws CmsException if something goes wrong
     */
    public boolean hasPermissions(
        CmsResource resource,
        CmsPermissionSet requiredPermissions,
        boolean checkLock,
        CmsResourceFilter filter) throws CmsException {

        return I_CmsPermissionHandler.PERM_ALLOWED == m_securityManager.hasPermissions(
            m_context,
            resource,
            requiredPermissions,
            checkLock,
            filter);
    }

    /**
     * Checks if the given resource or the current project can be published by the current user 
     * using his current OpenCms context.<p>
     * 
     * If the resource parameter is <code>null</code>, then the current project is checked,
     * otherwise the resource is checked for direct publish permissions.<p>
     * 
     * @param resourcename the direct publish resource name (optional, if null only the current project is checked)
     * 
     * @return <code>true</code>, if the current user can direct publish the given resource in his current context
     * 
     * @deprecated notice that checking is no longer possible from the CmsObject
     */
    public boolean hasPublishPermissions(String resourcename) {

        CmsResource resource = null;
        if (resourcename != null) {
            // resource name is optional
            try {
                resource = readResource(resourcename, CmsResourceFilter.ALL);
                checkPublishPermissions(new CmsPublishList(Collections.singletonList(resource), false));
            } catch (CmsException e) {
                // if any exception (e.g. security) occurs the result is false
                return false;
            }
        }
        // no exception means permissions are granted
        return true;
    }

    /**
     * Checks if the user of the current OpenCms context 
     * is a member of at last one of the roles in the given role set.<p>
     *  
     * @param role the role to check
     * 
     * @return <code>true</code> if the user of the current OpenCms context is at a member of at last 
     *      one of the roles in the given role set
     *      
     * @deprecated use {@link OpenCms#getRoleManager()} methods instead
     */
    public boolean hasRole(CmsRole role) {

        return OpenCms.getRoleManager().hasRole(this, role);
    }

    /**
     * Writes a list of access control entries as new access control entries of a given resource.<p>
     * 
     * Already existing access control entries of this resource are removed before.<p>
     * 
     * @param resource the resource to attach the control entries to
     * @param acEntries a list of <code>{@link CmsAccessControlEntry}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public void importAccessControlEntries(CmsResource resource, List acEntries) throws CmsException {

        m_securityManager.importAccessControlEntries(m_context, resource, acEntries);
    }

    /**
     * Imports a new relation to the given resource.<p>
     * 
     * @param resourceName the name of the source resource
     * @param targetPath the path of the target resource
     * @param relationType the type of the relation
     * 
     * @throws CmsException if something goes wrong
     */
    public void importRelation(String resourceName, String targetPath, String relationType) throws CmsException {

        createRelation(resourceName, targetPath, relationType, true);
    }

    /**
     * Imports a resource to the OpenCms VFS.<p>
     * 
     * If a resource already exists in the VFS (i.e. has the same name and 
     * same id) it is replaced by the imported resource.<p>
     * 
     * If a resource with the same name but a different id exists, 
     * the imported resource is (usually) moved to the "lost and found" folder.<p> 
     *
     * @param resourcename the name for the resource after import (full current site relative path)
     * @param resource the resource object to be imported
     * @param content the content of the resource
     * @param properties the properties of the resource
     * 
     * @return the imported resource
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#moveToLostAndFound(String)
     */
    public CmsResource importResource(String resourcename, CmsResource resource, byte[] content, List properties)
    throws CmsException {

        return getResourceType(resource).importResource(
            this,
            m_securityManager,
            resourcename,
            resource,
            content,
            properties);
    }

    /**
     * Creates a new user by import.<p>
     * 
     * @param id the id of the user
     * @param name the new name for the user
     * @param password the new password for the user
     * @param firstname the first name of the user
     * @param lastname the last name of the user
     * @param email the email of the user
     * @param flags the flags for a user (for example <code>{@link I_CmsPrincipal#FLAG_ENABLED}</code>)
     * @param dateCreated the creation date
     * @param additionalInfos the additional user infos
     * 
     * @return the imported user
     *
     * @throws CmsException if something goes wrong
     */
    public CmsUser importUser(
        String id,
        String name,
        String password,
        String firstname,
        String lastname,
        String email,
        int flags,
        long dateCreated,
        Map additionalInfos) throws CmsException {

        return m_securityManager.importUser(
            m_context,
            id,
            name,
            password,
            firstname,
            lastname,
            email,
            flags,
            dateCreated,
            additionalInfos);
    }

    /**
     * Creates a new user by import.<p>
     * 
     * @param id the id of the user
     * @param name the new name for the user
     * @param password the new password for the user
     * @param description the description for the user
     * @param firstname the first name of the user
     * @param lastname the last name of the user
     * @param email the email of the user
     * @param address the address of the user
     * @param flags the flags for a user (for example <code>{@link I_CmsPrincipal#FLAG_ENABLED}</code>)
     * @param additionalInfos the additional user infos
     * 
     * @return the imported user
     *
     * @throws CmsException if something goes wrong
     * 
     * @deprecated use {@link #importUser(String, String, String, String, String, String, int, long, Map)} instead
     */
    public CmsUser importUser(
        String id,
        String name,
        String password,
        String description,
        String firstname,
        String lastname,
        String email,
        String address,
        int flags,
        Map additionalInfos) throws CmsException {

        Map info = new HashMap();
        if (additionalInfos != null) {
            info.putAll(additionalInfos);
        }
        if (description != null) {
            info.put(CmsUserSettings.ADDITIONAL_INFO_DESCRIPTION, description);
        }
        if (address != null) {
            info.put(CmsUserSettings.ADDITIONAL_INFO_ADDRESS, address);
        }
        return importUser(id, name, password, firstname, lastname, email, flags, System.currentTimeMillis(), info);
    }

    /**
     * Checks if the current user has role access to <code>{@link CmsRole#ROOT_ADMIN}</code>.<p>
     *
     * @return <code>true</code>, if the current user has role access to <code>{@link CmsRole#ROOT_ADMIN}</code>
     * 
     * @deprecated use <code>{@link #hasRole(CmsRole)}</code> or <code>{@link #checkRole(CmsRole)}</code> instead
     */
    public boolean isAdmin() {

        return hasRole(CmsRole.ROOT_ADMIN);
    }

    /**
     * Checks if the specified resource is inside the current project.<p>
     * 
     * The project "view" is determined by a set of path prefixes. 
     * If the resource starts with any one of this prefixes, it is considered to 
     * be "inside" the project.<p>
     * 
     * @param resourcename the specified resource name (full current site relative path)
     * 
     * @return <code>true</code>, if the specified resource is inside the current project
     */
    public boolean isInsideCurrentProject(String resourcename) {

        return m_securityManager.isInsideCurrentProject(m_context, addSiteRoot(resourcename));
    }

    /**
     * Checks if the current user has management access to the current project.<p>
     *
     * @return <code>true</code>, if the user has management access to the current project
     */

    public boolean isManagerOfProject() {

        return m_securityManager.isManagerOfProject(m_context);
    }

    /**
     * Locks a resource.<p>
     *
     * This will be an exclusive, persistent lock that is removed only if the user unlocks it.<p>
     *
     * @param resourcename the name of the resource to lock (full current site relative path)
     * 
     * @throws CmsException if something goes wrong
     */
    public void lockResource(String resourcename) throws CmsException {

        lockResource(resourcename, CmsLockType.EXCLUSIVE);
    }

    /**
     * Locks a resource.<p>
     *
     * The <code>mode</code> parameter controls what kind of lock is used.<br>
     * 
     * @param resourcename the name of the resource to lock (full path)
     * @param mode flag indicating the mode for the lock
     * 
     * @throws CmsException if something goes wrong
     * 
     * @deprecated use {@link #lockResource(String)} or {@link #lockResourceTemporary(String)} instead
     */
    public void lockResource(String resourcename, int mode) throws CmsException {

        lockResource(resourcename, CmsLockType.valueOf(mode));
    }

    /**
     * Locks a resource temporary.<p>
     *
     * This will be an exclusive, temporary lock valid only for the current users session.
     * Usually this should not be used directly, this method is intended for the OpenCms workplace only.<p>
     *
     * @param resourcename the name of the resource to lock (full current site relative path)
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#lockResource(String)
     */
    public void lockResourceTemporary(String resourcename) throws CmsException {

        lockResource(resourcename, CmsLockType.TEMPORARY);
    }

    public String loginUserEncrypted(String data)  throws CmsException {
    	 // login the user
        CmsUser newUser = m_securityManager.loginUserEncrypted(m_context, data, m_context.getRemoteAddress());
        // set the project back to the "Online" project
        CmsProject newProject = m_securityManager.readProject(CmsProject.ONLINE_PROJECT_ID);
        // switch the cms context to the new user and project
        m_context.switchUser(newUser, newProject, newUser.getOuFqn());
        // init this CmsObject with the new user
        init(m_securityManager, m_context);
        // fire a login event
        fireEvent(I_CmsEventListener.EVENT_LOGIN_USER, newUser);
        // return the users login name
        return newUser.getName();
    }
    
    /**
     * Logs a user into the Cms, if the password is correct.<p>
     *
     * @param username the name of the user
     * @param password the password of the user
     * 
     * @return the name of the logged in user
     *
     * @throws CmsException if the login was not successful
     */
    public String loginUser(String username, String password) throws CmsException {

        return loginUser(username, password, m_context.getRemoteAddress());
    }
    
    /**
     * Logs a user into the Cms<p>
     *
     * @param username the name of the user
     * 
     * @return the name of the logged in user
     *
     * @throws CmsException if the login was not successful
     */
    public CmsUser loginUser(String username) throws CmsException {
        
    	/*
    	StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

    	StackTraceElement element = stackTrace[1];
    	
    	if (element.getClassName().startsWith("org.apache.jsp.WEB_002dINF.jsp.")) {
    		throw new CmsException(Messages.get().container(
                    Messages.ERR_ILLEGAL_OPERATION_1));
    	}	
    	*/
    	CmsUser newUser = m_securityManager.loginUser(m_context,username) ;
        CmsProject newProject = m_securityManager.readProject(CmsProject.ONLINE_PROJECT_ID);
        m_context.switchUser(newUser, newProject, newUser.getOuFqn());
        // init this CmsObject with the new user
        init(m_securityManager, m_context);
        return newUser;
    }
    
    public String loginUserByExternalProvider(String providerName, String id, String remoteAddress) throws CmsException {
    	 CmsUser newUser = m_securityManager.loginUserByExternalProvider(m_context, providerName, id, remoteAddress);
         // set the project back to the "Online" project
         CmsProject newProject = m_securityManager.readProject(CmsProject.ONLINE_PROJECT_ID);
         // switch the cms context to the new user and project
         m_context.switchUser(newUser, newProject, newUser.getOuFqn());
         // init this CmsObject with the new user
         init(m_securityManager, m_context);
         // fire a login event
         fireEvent(I_CmsEventListener.EVENT_LOGIN_USER, newUser);
         // return the users login name
         return newUser.getName();
    } 
    
    public String loginUserByToken(String token, String browserId, String remoteAddress) throws CmsException {
    	// login the user
        CmsUser newUser = m_securityManager.loginUserByToken(m_context, token, browserId, remoteAddress);
        // set the project back to the "Online" project
        CmsProject newProject = m_securityManager.readProject(CmsProject.ONLINE_PROJECT_ID);
        // switch the cms context to the new user and project
        m_context.switchUser(newUser, newProject, newUser.getOuFqn());
        // init this CmsObject with the new user
        init(m_securityManager, m_context);
        // fire a login event
        fireEvent(I_CmsEventListener.EVENT_LOGIN_USER, newUser);
        // return the users login name
        return newUser.getName();
    }
    
    /**
     * Logs a user with a given ip address into the Cms, if the password is correct.<p>
     *
     * @param username the name of the user
     * @param password the password of the user
     * @param remoteAddress the ip address
     * 
     * @return the name of the logged in user
     *
     * @throws CmsException if the login was not successful
     */
    public String loginUser(String username, String password, String remoteAddress) throws CmsException {

        // login the user
        CmsUser newUser = m_securityManager.loginUser(m_context, username, password, remoteAddress);
        // set the project back to the "Online" project
        CmsProject newProject = m_securityManager.readProject(CmsProject.ONLINE_PROJECT_ID);
        // switch the cms context to the new user and project
        m_context.switchUser(newUser, newProject, newUser.getOuFqn());
        // init this CmsObject with the new user
        init(m_securityManager, m_context);
        // fire a login event
        fireEvent(I_CmsEventListener.EVENT_LOGIN_USER, newUser);
        // return the users login name
        return newUser.getName();
    }

    /**
     * Logs a web user into the Cms, if the password is correct.
     *
     * @param username the name of the user
     * @param password the password of the user
     * 
     * @return the name of the logged in user
     *
     * @throws CmsException if the login was not successful
     * 
     * @deprecated there are no more web users, use a user without any role!
     */
    public String loginWebUser(String username, String password) throws CmsException {

        return loginUser(username, password, m_context.getRemoteAddress());
    }

    /**
     * Lookups and reads the user or group with the given UUID.<p>
     *   
     * @param principalId the uuid of a user or group
     * 
     * @return the user or group with the given UUID
     */
    public I_CmsPrincipal lookupPrincipal(CmsUUID principalId) {

        return m_securityManager.lookupPrincipal(m_context, principalId);
    }

    /**
     * Lookups and reads the user or group with the given name.<p>
     * 
     * @param principalName the name of the user or group
     * 
     * @return the user or group with the given name
     */
    public I_CmsPrincipal lookupPrincipal(String principalName) {

        return m_securityManager.lookupPrincipal(m_context, principalName);
    }

    /**
     * Moves a resource to the given destination.<p>
     * 
     * A move operation in OpenCms is always a copy (as sibling) followed by a delete,
     * this is a result of the online/offline structure of the 
     * OpenCms VFS. This way you can see the deleted files/folders in the offline
     * project, and you will be unable to undelete them.<p>
     * 
     * @param source the name of the resource to move (full current site relative path)
     * @param destination the destination resource name (full current site relative path)
     *
     * @throws CmsException if something goes wrong
     * 
     * @see #renameResource(String, String)
     */
    public void moveResource(String source, String destination) throws CmsException {

        CmsResource resource = readResource(source, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource).moveResource(this, m_securityManager, resource, destination);
    }

    /**
     * Moves a resource to the "lost and found" folder.<p>
     * 
     * The "lost and found" folder is a special system folder. 
     * 
     * This operation is used e.g. during import of resources
     * when a resource with the same name but a different resource ID
     * already exists in the VFS. In this case, the imported resource is 
     * moved to the "lost and found" folder.<p>
     * 
     * @param resourcename the name of the resource to move to "lost and found" (full current site relative path)
     *
     * @return the name of the resource inside the "lost and found" folder
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #getLostAndFoundName(String)
     */
    public String moveToLostAndFound(String resourcename) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return m_securityManager.moveToLostAndFound(m_context, resource, false);
    }

    /**
     * Publishes the current project, printing messages to a shell report.<p>
     *
     * @return the publish history id of the published project
     * 
     * @throws Exception if something goes wrong
     * 
     * @see CmsShellReport
     * 
     * @deprecated use <code>{@link OpenCms#getPublishManager()}.{@link org.opencms.publish.CmsPublishManager#publishProject(CmsObject) publishProject(CmsObject)}</code> instead
     */
    public CmsUUID publishProject() throws Exception {

        CmsUUID publishHistoryId = OpenCms.getPublishManager().publishProject(
            this,
            new CmsShellReport(m_context.getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();
        return publishHistoryId;
    }

    /**
     * Publishes the current project.<p>
     *
     * @param report an instance of <code>{@link I_CmsReport}</code> to print messages
     * 
     * @return the publish history id of the published project
     * 
     * @throws CmsException if something goes wrong
     * 
     * @deprecated use <code>{@link OpenCms#getPublishManager()}.{@link org.opencms.publish.CmsPublishManager#publishProject(CmsObject, I_CmsReport) publishProject(CmsObject, I_CmsReport)}</code> instead
     */
    public CmsUUID publishProject(I_CmsReport report) throws CmsException {

        CmsUUID publishHistoryId = OpenCms.getPublishManager().publishProject(
            this,
            report,
            OpenCms.getPublishManager().getPublishList(this));
        OpenCms.getPublishManager().waitWhileRunning();
        return publishHistoryId;
    }

    /**
     * Publishes the resources of a specified publish list.<p>
     * 
     * @param report an instance of <code>{@link I_CmsReport}</code> to print messages
     * @param publishList a publish list
     * 
     * @return the publish history id of the published project
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #getPublishList()
     * @see #getPublishList(CmsResource, boolean)
     * @see #getPublishList(List, boolean)
     * 
     * @deprecated use <code>{@link OpenCms#getPublishManager()}.{@link org.opencms.publish.CmsPublishManager#publishProject(CmsObject, I_CmsReport, CmsPublishList) publishProject(CmsObject, I_CmsReport, CmsPublishList)}</code> instead
     */
    public CmsUUID publishProject(I_CmsReport report, CmsPublishList publishList) throws CmsException {

        CmsUUID publishHistoryId = OpenCms.getPublishManager().publishProject(this, report, publishList);
        OpenCms.getPublishManager().waitWhileRunning();
        return publishHistoryId;
    }

    /**
     * Direct publishes a specified resource.<p>
     * 
     * @param report an instance of <code>{@link I_CmsReport}</code> to print messages
     * @param directPublishResource a <code>{@link CmsResource}</code> that gets directly published; 
     *                          or <code>null</code> if an entire project gets published.
     * @param directPublishSiblings if a <code>{@link CmsResource}</code> that should get published directly is 
     *                          provided as an argument, all eventual siblings of this resource 
     *                          get publish too, if this flag is <code>true</code>.
     * 
     * @return the publish history id of the published project
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #publishResource(String)
     * @see #publishResource(String, boolean, I_CmsReport)
     * 
     * @deprecated use <code>{@link OpenCms#getPublishManager()}.{@link org.opencms.publish.CmsPublishManager#publishProject(CmsObject, I_CmsReport, CmsResource, boolean) publishProject(CmsObject, I_CmsReport, CmsResource, boolean)}</code> instead
     */
    public CmsUUID publishProject(I_CmsReport report, CmsResource directPublishResource, boolean directPublishSiblings)
    throws CmsException {

        CmsUUID publishHistoryId = OpenCms.getPublishManager().publishProject(
            this,
            report,
            OpenCms.getPublishManager().getPublishList(this, directPublishResource, directPublishSiblings));
        OpenCms.getPublishManager().waitWhileRunning();
        return publishHistoryId;
    }

    /**
     * Publishes a single resource, printing messages to a shell report.<p>
     * 
     * The siblings of the resource will not be published.<p>
     *
     * @param resourcename the name of the resource to be published
     * 
     * @return the publish history id of the published project
     * 
     * @throws Exception if something goes wrong
     * 
     * @see CmsShellReport
     * 
     * @deprecated use <code>{@link OpenCms#getPublishManager()}.{@link org.opencms.publish.CmsPublishManager#publishResource(CmsObject, String) publishResource(CmsObject, String)}</code> instead
     */
    public CmsUUID publishResource(String resourcename) throws Exception {

        CmsUUID publishHistoryId = OpenCms.getPublishManager().publishResource(
            this,
            resourcename,
            false,
            new CmsShellReport(m_context.getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();
        return publishHistoryId;
    }

    /**
     * Publishes a single resource.<p>
     * 
     * @param resourcename the name of the resource to be published
     * @param publishSiblings if <code>true</code>, all siblings of the resource are also published
     * @param report the report to write the progress information to
     * 
     * @return the publish history id of the published project
     * 
     * @throws Exception if something goes wrong
     * 
     * @deprecated use <code>{@link OpenCms#getPublishManager()}.{@link org.opencms.publish.CmsPublishManager#publishResource(CmsObject, String, boolean, I_CmsReport) publishResource(CmsObject, String, boolean, I_CmsReport)}</code> instead
     */
    public CmsUUID publishResource(String resourcename, boolean publishSiblings, I_CmsReport report) throws Exception {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        CmsUUID publishHistoryId = OpenCms.getPublishManager().publishProject(this, report, resource, publishSiblings);
        OpenCms.getPublishManager().waitWhileRunning();
        return publishHistoryId;
    }

    /**
     * Reads all historical versions of a resource.<br>
     * 
     * The reading excludes the file content, if the resource is a file.<p>
     *
     * @param resourceName the name of the resource to be read
     *
     * @return a list of historical resources, as <code>{@link I_CmsHistoryResource}</code> objects
     *
     * @throws CmsException if operation was not successful
     */
    public List readAllAvailableVersions(String resourceName) throws CmsException {

        CmsResource resource = readResource(resourceName, CmsResourceFilter.ALL);
        return m_securityManager.readAllAvailableVersions(m_context, resource);
    }

    /**
     * Reads all file headers of a file.<br>
     * 
     * This method returns a list with the history of all file headers, i.e.
     * the file headers of a file, independent of the project they were attached to.<br>
     *
     * The reading excludes the file content.<p>
     *
     * @param filename the name of the file to be read
     *
     * @return a list of file headers, as <code>{@link I_CmsHistoryResource}</code> objects, read from the Cms
     *
     * @throws CmsException  if operation was not successful
     * 
     * @deprecated Use {@link #readAllAvailableVersions(String)} instead
     */
    public List readAllBackupFileHeaders(String filename) throws CmsException {

        return readAllAvailableVersions(filename);
    }

    /**
     * Reads all property definitions.<p>
     *
     * @return a list with the <code>{@link CmsPropertyDefinition}</code> objects (may be empty)
     *
     * @throws CmsException if something goes wrong
     */
    public List readAllPropertyDefinitions() throws CmsException {

        return m_securityManager.readAllPropertyDefinitions(m_context);
    }

    /**
     * Returns the first ancestor folder matching the filter criteria.<p>
     * 
     * If no folder matching the filter criteria is found, null is returned.<p>
     * 
     * @param resourcename the name of the resource to start (full current site relative path)
     * @param filter the resource filter to match while reading the ancestors
     * 
     * @return the first ancestor folder matching the filter criteria or null if no folder was found
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsFolder readAncestor(String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return m_securityManager.readAncestor(m_context, resource, filter);
    }

    /**
     * Returns the first ancestor folder matching the resource type.<p>
     * 
     * If no folder with the requested resource type is found, null is returned.<p>
     * 
     * @param resourcename the name of the resource to start (full current site relative path)
     * @param type the resource type of the folder to match
     * 
     * @return the first ancestor folder matching the filter criteria or null if no folder was found
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsFolder readAncestor(String resourcename, int type) throws CmsException {

        return readAncestor(resourcename, CmsResourceFilter.requireType(type));
    }

    /**
     * Returns a file from the history.<br>
     * 
     * The reading includes the file content.<p>
     *
     * @param structureId the structure id of the file to be read
     * @param publishTag the tag id of the resource
     *
     * @return the file read
     *
     * @throws CmsException if the user has not the rights to read the file, or 
     *                      if the file couldn't be read.
     *                      
     * @deprecated use {@link #readResourceByPublishTag(CmsUUID, int)} or {@link #readResource(CmsUUID, int)} instead, 
     *             but notice that the <code>publishTag != version</code>
     *             and there is no possibility to access to a historical entry with just the filename.
     */
    public I_CmsHistoryResource readBackupFile(CmsUUID structureId, int publishTag) throws CmsException {

        return readResourceByPublishTag(structureId, publishTag);
    }

    /**
     * Returns a resource from the historical archive.<br>
     * 
     * The reading includes the file content, if the resource is a file.<p>
     *
     * @param filename the path of the file to be read
     * @param publishTag the publish tag
     *
     * @return the file read
     *
     * @throws CmsException if the user has not the rights to read the file, or 
     *                      if the file couldn't be read.
     *                      
     * @deprecated use {@link #readResource(CmsUUID, int)} instead, 
     *             but notice that the <code>publishTag != version</code>
     *             and there is no possibility to access to an historical entry with just the filename.
     */
    public I_CmsHistoryResource readBackupFile(String filename, int publishTag) throws CmsException {

        CmsResource resource = readResource(filename, CmsResourceFilter.ALL);
        return readResourceByPublishTag(resource.getStructureId(), publishTag);
    }

    /**
     * Returns a backup project.<p>
     *
     * @param tagId the tag of the backup project to be read
     * 
     * @return the requested backup project
     * 
     * @throws CmsException if operation was not successful
     * 
     * @deprecated Use {@link #readHistoryProject(int)} instead
     */
    public CmsHistoryProject readBackupProject(int tagId) throws CmsException {

        return readHistoryProject(tagId);
    }

    /**
     * Reads the list of <code>{@link CmsProperty}</code> objects that belong the the given backup resource.<p>
     * 
     * @param resource the backup resource to read the properties from
     * 
     * @return the list of <code>{@link CmsProperty}</code> objects that belong the the given backup resource
     * 
     * @throws CmsException if something goes wrong
     * 
     * @deprecated Use {@link #readHistoryPropertyObjects(I_CmsHistoryResource)} instead
     */
    public List readBackupPropertyObjects(I_CmsHistoryResource resource) throws CmsException {

        return readHistoryPropertyObjects(resource);
    }

    /**
     * Returns the default resource for the given folder.<p>
     * 
     * If the given resource name or id identifies a file, then this file is returned.<p>
     * 
     * Otherwise, in case of a folder:<br> 
     * <ol>
     *   <li>the {@link CmsPropertyDefinition#PROPERTY_DEFAULT_FILE} is checked, and
     *   <li>if still no file could be found, the configured default files in the 
     *       <code>opencms-vfs.xml</code> configuration are iterated until a match is 
     *       found, and
     *   <li>if still no file could be found, <code>null</code> is returned
     * </ol>
     * 
     * @param resourceNameOrID the name or id of the folder to read the default file for
     * 
     * @return the default file for the given folder
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsSecurityException if the user has no permissions to read the resulting file
     * 
     * @see CmsSecurityManager#readDefaultFile(CmsRequestContext, CmsResource)
     */
    public CmsResource readDefaultFile(String resourceNameOrID) throws CmsException, CmsSecurityException {

        CmsResource resource;
        try {
            resource = readResource(new CmsUUID(resourceNameOrID));
        } catch (NumberFormatException e) {
            resource = readResource(resourceNameOrID);
        }
        return m_securityManager.readDefaultFile(m_context, resource);
    }

    /**
     * Reads all deleted (historical) resources below the given path, 
     * including the full tree below the path, if required.<p>
     * 
     * The result list may include resources with the same name of  
     * resources (with different id's).<p>
     * 
     * Use in conjunction with the {@link #restoreDeletedResource(CmsUUID)} 
     * method.<p>
     * 
     * @param resourcename the parent path to read the resources from
     * @param readTree <code>true</code> to read all sub resources
     * 
     * @return a list of <code>{@link I_CmsHistoryResource}</code> objects
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #readResource(CmsUUID, int)
     * @see #readResources(String, CmsResourceFilter, boolean)
     */
    public List readDeletedResources(String resourcename, boolean readTree) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return m_securityManager.readDeletedResources(m_context, resource, readTree);
    }

    /**
     * Reads a file resource (including it's binary content) from the VFS,
     * for the given resource (this may also be an historical version of the resource).<p>
     * 
     * In case the input {@link CmsResource} object already is a {@link CmsFile} with contents
     * available, it is casted to a file and returned unchanged. Otherwise the file is read 
     * from the VFS.<p>
     * 
     * In case you do not need the file content, 
     * use <code>{@link #readResource(String)}</code> or
     * <code>{@link #readResource(String, CmsResourceFilter)}</code> instead.<p>
     * 
     * No resource filter is applied when reading the resource, since we already have
     * a full resource instance and assume we just want the content for that instance. 
     * In case you need to apply a filter, use {@link #readFile(String, CmsResourceFilter)} instead.<p>
     * 
     * @param resource the resource to read
     *
     * @return the file resource that was read
     *
     * @throws CmsException if the file resource could not be read for any reason
     * 
     * @see #readFile(String)
     * @see #readFile(String, CmsResourceFilter)
     */
    public CmsFile readFile(CmsResource resource) throws CmsException {

        // test if we already have a file
        if (resource instanceof CmsFile) {
            // resource is already a file
            CmsFile file = (CmsFile)resource;
            if ((file.getContents() != null) && (file.getContents().length > 0)) {
                // file has the contents already available
                return file;
            }
        }

        return m_securityManager.readFile(m_context, resource);
    }

    /**
     * Reads a file resource (including it's binary content) from the VFS,
     * using the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p>
     *  
     * In case you do not need the file content, 
     * use <code>{@link #readResource(String)}</code> instead.<p>
     *
     * @param resourcename the name of the resource to read (full current site relative path)
     *
     * @return the file resource that was read
     *
     * @throws CmsException if the file resource could not be read for any reason
     * 
     * @see #readFile(String, CmsResourceFilter)
     * @see #readFile(CmsResource)
     * @see #readResource(String)
     */
    public CmsFile readFile(String resourcename) throws CmsException {

        return readFile(resourcename, CmsResourceFilter.ALL);
    }

    /**
     * Reads a file resource (including it's binary content) from the VFS,
     * using the specified resource filter.<p>
     * 
     * In case you do not need the file content, 
     * use <code>{@link #readResource(String, CmsResourceFilter)}</code> instead.<p>
     * 
     * The specified filter controls what kind of resources should be "found" 
     * during the read operation. This will depend on the application. For example, 
     * using <code>{@link CmsResourceFilter#DEFAULT}</code> will only return currently
     * "valid" resources, while using <code>{@link CmsResourceFilter#IGNORE_EXPIRATION}</code>
     * will ignore the date release / date expired information of the resource.<p>
     *
     * @param resourcename the name of the resource to read (full current site relative path)
     * @param filter the resource filter to use while reading
     *
     * @return the file resource that was read
     *
     * @throws CmsException if the file resource could not be read for any reason
     *
     * @see #readFile(String)
     * @see #readFile(CmsResource)
     * @see #readResource(String, CmsResourceFilter)
     */
    public CmsFile readFile(String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource resource = readResource(resourcename, filter);
        return readFile(resource);
    }

    /**
     * Reads a resource from the VFS,
     * using the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p> 
     *
     * @param resourcename the name of the resource to read (full current site relative path)
     *
     * @return the file resource that was read
     *
     * @throws CmsException if something goes wrong
     *
     * @deprecated use <code>{@link #readResource(String, CmsResourceFilter)}</code> instead.
     */
    public CmsResource readFileHeader(String resourcename) throws CmsException {

        return readResource(resourcename, CmsResourceFilter.DEFAULT);
    }

    /**
     * Reads a folder resource from the VFS,
     * using the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p> 
     *
     * @param resourcename the name of the folder resource to read (full current site relative path)
     *
     * @return the folder resource that was read
     *
     * @throws CmsException if the resource could not be read for any reason
     *
     * @see #readResource(String, CmsResourceFilter)
     * @see #readFolder(String, CmsResourceFilter)
     */
    public CmsFolder readFolder(String resourcename) throws CmsException {

        return readFolder(resourcename, CmsResourceFilter.DEFAULT);
    }

    /**
     * Reads a folder resource from the VFS,
     * using the specified resource filter.<p>
     *
     * The specified filter controls what kind of resources should be "found" 
     * during the read operation. This will depend on the application. For example, 
     * using <code>{@link CmsResourceFilter#DEFAULT}</code> will only return currently
     * "valid" resources, while using <code>{@link CmsResourceFilter#IGNORE_EXPIRATION}</code>
     * will ignore the date release / date expired information of the resource.<p>
     * 
     * @param resourcename the name of the folder resource to read (full current site relative path)
     * @param filter the resource filter to use while reading
     *
     * @return the folder resource that was read
     *
     * @throws CmsException if the resource could not be read for any reason
     * 
     * @see #readResource(String, CmsResourceFilter)
     */
    public CmsFolder readFolder(String resourcename, CmsResourceFilter filter) throws CmsException {

        return m_securityManager.readFolder(m_context, addSiteRoot(resourcename), filter);
    }

    /**
     * Reads the group of a project.<p>
     *
     * @param project the project to read the group from
     * 
     * @return the group of the given project
     */
    public CmsGroup readGroup(CmsProject project) {

        return m_securityManager.readGroup(m_context, project);
    }

    /**
     * Reads a group based on its id.<p>
     *
     * @param groupId the id of the group to be read
     * 
     * @return the group that has the provided id
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #readHistoryPrincipal(CmsUUID) for retrieving deleted groups
     */
    public CmsGroup readGroup(CmsUUID groupId) throws CmsException {

        return m_securityManager.readGroup(m_context, groupId);
    }

    /**
     * Reads a group based on its name.<p>
     * 
     * @param groupName the name of the group to be read
     * 
     * @return the group that has the provided name
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsGroup readGroup(String groupName) throws CmsException {

        return m_securityManager.readGroup(m_context, groupName);
    }

    /**
     * Reads a principal (an user or group) from the historical archive based on its ID.<p>
     * 
     * @param principalId the id of the principal to read
     * 
     * @return the historical principal entry with the given id
     * 
     * @throws CmsException if something goes wrong, ie. {@link org.opencms.db.CmsDbEntryNotFoundException}
     * 
     * @see #readUser(CmsUUID)
     * @see #readGroup(CmsUUID)
     */
    public CmsHistoryPrincipal readHistoryPrincipal(CmsUUID principalId) throws CmsException {

        return m_securityManager.readHistoricalPrincipal(m_context, principalId);
    }

    /**
     * Returns the latest historical project entry with the given id.<p>
     *
     * @param projectId the project id
     * 
     * @return the requested historical project entry
     * 
     * @throws CmsException if operation was not successful
     */
    public CmsHistoryProject readHistoryProject(CmsUUID projectId) throws CmsException {

        return (m_securityManager.readHistoryProject(m_context, projectId));
    }

    /**
     * Returns a historical project entry.<p>
     *
     * @param publishTag publish tag of the project
     * 
     * @return the requested historical project entry
     * 
     * @throws CmsException if operation was not successful
     */
    public CmsHistoryProject readHistoryProject(int publishTag) throws CmsException {

        return (m_securityManager.readHistoryProject(m_context, publishTag));
    }

    /**
     * Reads the list of all <code>{@link CmsProperty}</code> objects that belong to the given historical resource version.<p>
     * 
     * @param resource the historical resource version to read the properties for
     * 
     * @return the list of <code>{@link CmsProperty}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List readHistoryPropertyObjects(I_CmsHistoryResource resource) throws CmsException {

        return m_securityManager.readHistoryPropertyObjects(m_context, resource);
    }

    /**
     * Returns the project manager group of a project.<p>
     *
     * @param project the project
     * 
     * @return the manager group of the project
     */
    public CmsGroup readManagerGroup(CmsProject project) {

        return m_securityManager.readManagerGroup(m_context, project);
    }

    /**
     * Reads the owner of a project.<p>
     *
     * @param project the project to read the owner from
     * 
     * @return the owner of the project
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOwner(CmsProject project) throws CmsException {

        return m_securityManager.readOwner(m_context, project);
    }

    /**
     * Builds a list of resources for a given path.<p>
     * 
     * @param path the requested path
     * @param filter a filter object (only "includeDeleted" information is used!)
     * 
     * @return list of <code>{@link CmsResource}</code>s
     * 
     * @throws CmsException if something goes wrong
     */
    public List readPath(String path, CmsResourceFilter filter) throws CmsException {

        return m_securityManager.readPath(m_context, addSiteRoot(path), filter);
    }

    /**
     * Reads the project with the given id.<p>
     *
     * @param id the id of the project
     * 
     * @return the project with the given id
     *
     * @throws CmsException if operation was not successful
     */
    public CmsProject readProject(CmsUUID id) throws CmsException {

        return m_securityManager.readProject(id);
    }

    /**
     * Reads the project with the given id.<p>
     *
     * @param id the id of the project
     * 
     * @return the project with the given id
     *
     * @throws CmsException if operation was not successful
     * 
     * @deprecated use {@link #readProject(CmsUUID)} instead
     */
    public CmsProject readProject(int id) throws CmsException {

        return readProject(m_securityManager.getProjectId(m_context, id));
    }

    /**
     * Reads the project with the given name.<p>
     *
     * @param name the name of the project
     * 
     * @return the project with the given name
     * 
     * @throws CmsException if operation was not successful
     */
    public CmsProject readProject(String name) throws CmsException {

        return m_securityManager.readProject(name);
    }

    /**
     * Returns the list of all resource names that define the "view" of the given project.<p>
     * 
     * @param project the project to get the project resources for
     * 
     * @return the list of all resource names (root paths), as <code>{@link String}</code> 
     *              objects that define the "view" of the given project
     * 
     * @throws CmsException if something goes wrong
     */
    public List readProjectResources(CmsProject project) throws CmsException {

        return m_securityManager.readProjectResources(m_context, project);
    }

    /**
     * Reads all resources of a project that match a given state from the VFS.<p>
     * 
     * Possible values for the <code>state</code> parameter are:<br>
     * <ul>
     * <li><code>{@link CmsResource#STATE_CHANGED}</code>: Read all "changed" resources in the project</li>
     * <li><code>{@link CmsResource#STATE_NEW}</code>: Read all "new" resources in the project</li>
     * <li><code>{@link CmsResource#STATE_DELETED}</code>: Read all "deleted" resources in the project</li>
     * <li><code>{@link CmsResource#STATE_KEEP}</code>: Read all resources either "changed", "new" or "deleted" in the project</li>
     * </ul><p>
     * 
     * @param projectId the id of the project to read the file resources for
     * @param state the resource state to match
     *
     * @return all <code>{@link CmsResource}</code>s of a project that match a given criteria from the VFS
     * 
     * @throws CmsException if something goes wrong
     */
    public List readProjectView(CmsUUID projectId, CmsResourceState state) throws CmsException {

        return m_securityManager.readProjectView(m_context, projectId, state);
    }

    /**
     * @see #readProjectView(CmsUUID, CmsResourceState)
     * 
     * @param projectId the id of the project to read the file resources for
     * @param state the resource state to match
     *
     * @return all <code>{@link CmsResource}</code>s of a project that match a given criteria from the VFS
     * 
     * @throws CmsException if something goes wrong
     * 
     * @deprecated use {@link #readProjectView(CmsUUID, CmsResourceState)} instead
     */
    public List readProjectView(int projectId, CmsResourceState state) throws CmsException {

        return readProjectView(m_securityManager.getProjectId(m_context, projectId), state);
    }

    /**
     * Reads all resources of a project that match a given state from the VFS.<p>
     * 
     * 
     * @param projectId the id of the project to read the file resources for
     * @param state the resource state to match
     *
     * @return all <code>{@link CmsResource}</code>s of a project that match a given criteria from the VFS
     * 
     * @throws CmsException if something goes wrong
     * 
     * @deprecated use {@link #readProjectView(CmsUUID, CmsResourceState)} instead
     */
    public List readProjectView(int projectId, int state) throws CmsException {

        return readProjectView(m_securityManager.getProjectId(m_context, projectId), CmsResourceState.valueOf(state));
    }

    /**
     * Reads the (compound) values of all properties mapped to a specified resource.<p>
     * 
     * @param resourcePath the resource to look up the property for
     * 
     * @return a map of <code>String</code> objects representing all properties of the resource
     * 
     * @throws CmsException in case there where problems reading the properties
     * 
     * @deprecated use <code>{@link #readPropertyObjects(String, boolean)}</code> instead.
     */
    public Map readProperties(String resourcePath) throws CmsException {

        CmsResource resource = readResource(resourcePath, CmsResourceFilter.ALL);
        List properties = m_securityManager.readPropertyObjects(m_context, resource, false);
        return CmsProperty.toMap(properties);
    }

    /**
     * Reads the (compound) values of all properties mapped to a specified resource
     * with optional direcory upward cascading.<p>
     * 
     * @param resourcePath the resource to look up the property for
     * @param search if <code>true</code>, the properties will also be looked up on all parent folders and the results will be merged, if <code>false</code> not (ie. normal property lookup)
     * 
     * @return Map of <code>String</code> objects representing all properties of the resource
     * 
     * @throws CmsException in case there where problems reading the properties
     * 
     * @deprecated use <code>{@link #readPropertyObjects(String, boolean)}</code> instead.
     */
    public Map readProperties(String resourcePath, boolean search) throws CmsException {

        CmsResource resource = readResource(resourcePath, CmsResourceFilter.ALL);
        List properties = m_securityManager.readPropertyObjects(m_context, resource, search);
        return CmsProperty.toMap(properties);
    }

    /**
     * Reads the (compound) value of a property mapped to a specified resource.<p>
     *
     * @param resourcePath the resource to look up the property for
     * @param property the name of the property to look up
     * 
     * @return the value of the property found, <code>null</code> if nothing was found
     * 
     * @throws CmsException in case there where problems reading the property
     * 
     * @see CmsProperty#getValue()
     * 
     * @deprecated use <code>{@link #readPropertyObject(String, String, boolean)}</code> instead.
     */
    public String readProperty(String resourcePath, String property) throws CmsException {

        CmsResource resource = readResource(resourcePath, CmsResourceFilter.ALL);
        CmsProperty value = m_securityManager.readPropertyObject(m_context, resource, property, false);
        return value.isNullProperty() ? null : value.getValue();
    }

    /**
     * Reads the (compound) value of a property mapped to a specified resource 
     * with optional direcory upward cascading.<p>
     * 
     * @param resourcePath the resource to look up the property for
     * @param property the name of the property to look up
     * @param search if <code>true</code>, the property will be looked up on all parent folders if it is not attached to the the resource, if false not (ie. normal property lookup)
     * 
     * @return the value of the property found, <code>null</code> if nothing was found
     * 
     * @throws CmsException in case there where problems reading the property
     * 
     * @see CmsProperty#getValue()
     * 
     * @deprecated use <code>{@link #readPropertyObject(String, String, boolean)}</code> instead.
     */
    public String readProperty(String resourcePath, String property, boolean search) throws CmsException {

        CmsResource resource = readResource(resourcePath, CmsResourceFilter.ALL);
        CmsProperty value = m_securityManager.readPropertyObject(m_context, resource, property, search);
        return value.isNullProperty() ? null : value.getValue();
    }

    /**
     * Reads the (compound) value of a property mapped to a specified resource 
     * with optional directory upward cascading, a default value will be returned if the property 
     * is not found on the resource (or it's parent folders in case search is set to <code>true</code>).<p>
     * 
     * @param resourcePath the resource to look up the property for
     * @param property the name of the property to look up
     * @param search if <code>true</code>, the property will be looked up on all parent folders if it is not attached to the the resource, if <code>false</code> not (ie. normal property lookup)
     * @param propertyDefault a default value that will be returned if the property was not found on the selected resource
     * 
     * @return the value of the property found, if nothing was found the value of the <code>propertyDefault</code> parameter is returned
     * 
     * @throws CmsException in case there where problems reading the property
     * 
     * @see CmsProperty#getValue()
     * 
     * @deprecated use <code>{@link #readPropertyObject(String, String, boolean)}</code> instead.
     */
    public String readProperty(String resourcePath, String property, boolean search, String propertyDefault)
    throws CmsException {

        CmsResource resource = readResource(resourcePath, CmsResourceFilter.ALL);
        CmsProperty value = m_securityManager.readPropertyObject(m_context, resource, property, search);
        return value.isNullProperty() ? propertyDefault : value.getValue();
    }

    /**
     * Reads a property definition.<p>
     *
     * If no property definition with the given name is found, 
     * <code>null</code> is returned.<p>
     * 
     * @param name the name of the property definition to read
     * 
     * @return the property definition that was read
     *
     * @throws CmsException a CmsDbEntryNotFoundException is thrown if the property definition does not exist
     */
    public CmsPropertyDefinition readPropertyDefinition(String name) throws CmsException {

        return (m_securityManager.readPropertyDefinition(m_context, name));
    }

    /**
     * Reads a property object from a resource specified by a property name.<p>
     * 
     * Returns <code>{@link CmsProperty#getNullProperty()}</code> if the property is not found.<p>
     * 
     * This method is more efficient then using <code>{@link CmsObject#readPropertyObject(String, String, boolean)}</code>
     * if you already have an instance of the resource to look up the property from.<p>
     * 
     * @param resource the resource where the property is attached to
     * @param property the property name
     * @param search if true, the property is searched on all parent folders of the resource, 
     *      if it's not found attached directly to the resource
     * 
     * @return the required property, or <code>{@link CmsProperty#getNullProperty()}</code> if the property was not found
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsProperty readPropertyObject(CmsResource resource, String property, boolean search) throws CmsException {

        return m_securityManager.readPropertyObject(m_context, resource, property, search);
    }

    /**
     * Reads a property object from a resource specified by a property name.<p>
     * 
     * Returns <code>{@link CmsProperty#getNullProperty()}</code> if the property is not found.<p>
     * 
     * @param resourcePath the name of resource where the property is attached to
     * @param property the property name
     * @param search if true, the property is searched on all parent folders of the resource, 
     *      if it's not found attached directly to the resource
     * 
     * @return the required property, or <code>{@link CmsProperty#getNullProperty()}</code> if the property was not found
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsProperty readPropertyObject(String resourcePath, String property, boolean search) throws CmsException {

        CmsResource resource = readResource(resourcePath, CmsResourceFilter.ALL);
        return m_securityManager.readPropertyObject(m_context, resource, property, search);
    }

    /**
     * Reads all property objects from a resource.<p>
     * 
     * Returns an empty list if no properties are found.<p>
     * 
     * This method is more efficient then using <code>{@link CmsObject#readPropertyObjects(String, boolean)}</code>
     * if you already have an instance of the resource to look up the property from.<p>
     * 
     * If the <code>search</code> parameter is <code>true</code>, the properties of all 
     * parent folders of the resource are also read. The results are merged with the 
     * properties directly attached to the resource. While merging, a property
     * on a parent folder that has already been found will be ignored.
     * So e.g. if a resource has a property "Title" attached, and it's parent folder 
     * has the same property attached but with a different value, the result list will
     * contain only the property with the value from the resource, not form the parent folder(s).<p>
     * 
     * @param resource the resource where the property is mapped to
     * @param search if <code>true</code>, the properties of all parent folders of the resource 
     *      are merged with the resource properties.
     * 
     * @return a list of <code>{@link CmsProperty}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List readPropertyObjects(CmsResource resource, boolean search) throws CmsException {

        return m_securityManager.readPropertyObjects(m_context, resource, search);
    }

    /**
     * Reads all property objects from a resource.<p>
     * 
     * Returns an empty list if no properties are found.<p>
     * 
     * All properties in the result List will be in frozen (read only) state, so you can't change the values.<p>
     * 
     * If the <code>search</code> parameter is <code>true</code>, the properties of all 
     * parent folders of the resource are also read. The results are merged with the 
     * properties directly attached to the resource. While merging, a property
     * on a parent folder that has already been found will be ignored.
     * So e.g. if a resource has a property "Title" attached, and it's parent folder 
     * has the same property attached but with a different value, the result list will
     * contain only the property with the value from the resource, not form the parent folder(s).<p>
     * 
     * @param resourcePath the name of resource where the property is mapped to
     * @param search if <code>true</code>, the properties of all parent folders of the resource 
     *      are merged with the resource properties.
     * 
     * @return a list of <code>{@link CmsProperty}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List readPropertyObjects(String resourcePath, boolean search) throws CmsException {

        CmsResource resource = readResource(resourcePath, CmsResourceFilter.ALL);
        return m_securityManager.readPropertyObjects(m_context, resource, search);
    }

    /**
     * Reads the resources that were published in a publish task for a given publish history ID.<p>
     * 
     * @param publishHistoryId unique ID to identify each publish task in the publish history
     * 
     * @return a list of <code>{@link org.opencms.db.CmsPublishedResource}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List readPublishedResources(CmsUUID publishHistoryId) throws CmsException {

        return m_securityManager.readPublishedResources(m_context, publishHistoryId);
    }

    /**
     * Reads a resource from the VFS,
     * using the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p> 
     *
     * A resource may be of type <code>{@link CmsFile}</code> or 
     * <code>{@link CmsFolder}</code>. In case of
     * a file, the resource will not contain the binary file content. Since reading 
     * the binary content is a cost-expensive database operation, it's recommended 
     * to work with resources if possible, and only read the file content when absolutely
     * required. To "upgrade" a resource to a file, 
     * use <code>{@link #readFile(CmsResource)}</code>.<p> 
     *
     * @param structureID the structure ID of the resource to read
     *
     * @return the resource that was read
     *
     * @throws CmsException if the resource could not be read for any reason
     *
     * @see #readFile(String) 
     * @see #readResource(CmsUUID, CmsResourceFilter)
     */
    public CmsResource readResource(CmsUUID structureID) throws CmsException {

        return readResource(structureID, CmsResourceFilter.ALL);
    }

    /**
     * Reads a resource from the VFS,
     * using the specified resource filter.<p>
     *
     * A resource may be of type <code>{@link CmsFile}</code> or 
     * <code>{@link CmsFolder}</code>. In case of
     * a file, the resource will not contain the binary file content. Since reading 
     * the binary content is a cost-expensive database operation, it's recommended 
     * to work with resources if possible, and only read the file content when absolutely
     * required. To "upgrade" a resource to a file, 
     * use <code>{@link #readFile(CmsResource)}</code>.<p> 
     *
     * The specified filter controls what kind of resources should be "found" 
     * during the read operation. This will depend on the application. For example, 
     * using <code>{@link CmsResourceFilter#DEFAULT}</code> will only return currently
     * "valid" resources, while using <code>{@link CmsResourceFilter#IGNORE_EXPIRATION}</code>
     * will ignore the date release / date expired information of the resource.<p>
     * 
     * @param structureID the structure ID of the resource to read
     * @param filter the resource filter to use while reading
     *
     * @return the resource that was read
     *
     * @throws CmsException if the resource could not be read for any reason
     * 
     * @see #readFile(String, CmsResourceFilter)
     * @see #readFolder(String, CmsResourceFilter)
     */
    public CmsResource readResource(CmsUUID structureID, CmsResourceFilter filter) throws CmsException {

        return m_securityManager.readResource(m_context, structureID, filter);
    }

    /**
     * Reads the historical resource with the given version for the resource given 
     * the given structure id.<p>
     *
     * A resource may be of type <code>{@link CmsFile}</code> or 
     * <code>{@link CmsFolder}</code>. In case of a file, the resource will not 
     * contain the binary file content. Since reading the binary content is a 
     * cost-expensive database operation, it's recommended to work with resources 
     * if possible, and only read the file content when absolutely required. To 
     * "upgrade" a resource to a file, use 
     * <code>{@link #readFile(CmsResource)}</code>.<p> 
     *
     * Please note that historical versions are just generated during publishing, 
     * so the first version with version number 1 is generated during publishing 
     * of a new resource (exception is a new sibling, that may also contain some 
     * relevant versions of already published siblings) and the last version 
     * available is the version of the current online resource.<p>
     * 
     * @param structureID the structure ID of the resource to read
     * @param version the version number you want to retrieve
     *
     * @return the resource that was read
     *
     * @throws CmsException if the resource could not be read for any reason
     * @throws CmsVfsResourceNotFoundException if the version does not exists
     * 
     * @see #restoreResourceVersion(CmsUUID, int)
     */
    public I_CmsHistoryResource readResource(CmsUUID structureID, int version)
    throws CmsException, CmsVfsResourceNotFoundException {

        CmsResource resource = readResource(structureID, CmsResourceFilter.ALL);
        return m_securityManager.readResource(m_context, resource, version);
    }

    /**
     * Reads a resource from the VFS,
     * using the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p> 
     *
     * A resource may be of type <code>{@link CmsFile}</code> or 
     * <code>{@link CmsFolder}</code>. In case of
     * a file, the resource will not contain the binary file content. Since reading 
     * the binary content is a cost-expensive database operation, it's recommended 
     * to work with resources if possible, and only read the file content when absolutely
     * required. To "upgrade" a resource to a file, 
     * use <code>{@link #readFile(CmsResource)}</code>.<p> 
     *
     * @param resourcename the name of the resource to read (full current site relative path)
     *
     * @return the resource that was read
     *
     * @throws CmsException if the resource could not be read for any reason
     *
     * @see #readFile(String) 
     * @see #readResource(String, CmsResourceFilter)
     */
    public CmsResource readResource(String resourcename) throws CmsException {

        return readResource(resourcename, CmsResourceFilter.DEFAULT);
    }

    /**
     * Reads a resource from the VFS,
     * using the specified resource filter.<p>
     *
     * A resource may be of type <code>{@link CmsFile}</code> or 
     * <code>{@link CmsFolder}</code>. In case of
     * a file, the resource will not contain the binary file content. Since reading 
     * the binary content is a cost-expensive database operation, it's recommended 
     * to work with resources if possible, and only read the file content when absolutely
     * required. To "upgrade" a resource to a file, 
     * use <code>{@link #readFile(CmsResource)}</code>.<p> 
     *
     * The specified filter controls what kind of resources should be "found" 
     * during the read operation. This will depend on the application. For example, 
     * using <code>{@link CmsResourceFilter#DEFAULT}</code> will only return currently
     * "valid" resources, while using <code>{@link CmsResourceFilter#IGNORE_EXPIRATION}</code>
     * will ignore the date release / date expired information of the resource.<p>
     * 
     * @param resourcename the name of the resource to read (full current site relative path)
     * @param filter the resource filter to use while reading
     *
     * @return the resource that was read
     *
     * @throws CmsException if the resource could not be read for any reason
     * 
     * @see #readFile(String, CmsResourceFilter)
     * @see #readFolder(String, CmsResourceFilter)
     */
    public CmsResource readResource(String resourcename, CmsResourceFilter filter) throws CmsException {

        return m_securityManager.readResource(m_context, addSiteRoot(resourcename), filter);
    }

    /**
     * Reads an historical resource in the current project with the given publish tag 
     * from the historical archive.<p>
     * 
     * @param structureId the structure id of the resource to restore from the archive
     * @param publishTag the publish tag of the resource
     * 
     * @return the file in the current project with the given publish tag from the historical 
     *         archive, or {@link CmsVfsResourceNotFoundException} if not found 
     *
     * @throws CmsException if something goes wrong
     * 
     * @see #readResource(CmsUUID, int)
     * 
     * @deprecated use {@link #readResource(CmsUUID, int)} instead
     *             but notice that the <code>publishTag != version</code>
     */
    public I_CmsHistoryResource readResourceByPublishTag(CmsUUID structureId, int publishTag) throws CmsException {

        CmsResource resource = readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
        return m_securityManager.readResourceForPublishTag(m_context, resource, publishTag);
    }

    /**
     * Reads all resources below the given path matching the filter criteria, 
     * including the full tree below the path.<p>
     * 
     * @param resourcename the parent path to read the resources from
     * @param filter the filter
     * 
     * @return a list of <code>{@link CmsResource}</code> objects matching the filter criteria
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #readResources(String, CmsResourceFilter, boolean)
     */
    public List readResources(String resourcename, CmsResourceFilter filter) throws CmsException {

        return readResources(resourcename, filter, true);
    }

    /**
     * Reads all resources below the given path matching the filter criteria,
     * including the full tree below the path only in case the <code>readTree</code> 
     * parameter is <code>true</code>.<p>
     * 
     * @param resourcename the parent path to read the resources from
     * @param filter the filter
     * @param readTree <code>true</code> to read all sub resources
     * 
     * @return a list of <code>{@link CmsResource}</code> objects matching the filter criteria
     * 
     * @throws CmsException if something goes wrong
     */
    public List readResources(String resourcename, CmsResourceFilter filter, boolean readTree) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return m_securityManager.readResources(m_context, resource, filter, readTree,null,null);
    }

    public List readResources(String resourcename, CmsResourceFilter filter, boolean readTree, Integer size, Integer offset) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        return m_securityManager.readResources(m_context, resource, filter, readTree, size, offset);
    }
    
    /**
     * Reads all resources that have a value set for the specified property.<p>
     * 
     * Both individual and shared properties of a resource are checked.<p>
     * 
     * Will use the {@link CmsResourceFilter#ALL} resource filter.<p>
     *
     * @param propertyDefinition the name of the property to check for
     * 
     * @return a list of all <code>{@link CmsResource}</code> objects 
     *          that have a value set for the specified property.
     * 
     * @throws CmsException if something goes wrong
     */
    public List readResourcesWithProperty(String propertyDefinition) throws CmsException {

        return readResourcesWithProperty("/", propertyDefinition);
    }

    /**
     * Reads all resources that have a value set for the specified property in the given path.<p>
     * 
     * Both individual and shared properties of a resource are checked.<p>
     * 
     * Will use the {@link CmsResourceFilter#ALL} resource filter.<p>
     *
     * @param path the folder to get the resources with the property from
     * @param propertyDefinition the name of the property to check for
     * 
     * @return all <code>{@link CmsResource}</code> objects 
     *          that have a value set for the specified property in the given path.
     * 
     * @throws CmsException if something goes wrong
     */
    public List readResourcesWithProperty(String path, String propertyDefinition) throws CmsException {

        return readResourcesWithProperty(path, propertyDefinition, null);
    }

    /**
     * Reads all resources that have a value (containing the specified value) set 
     * for the specified property in the given path.<p>
     * 
     * Both individual and shared properties of a resource are checked.<p>
     *
     * If the <code>value</code> parameter is <code>null</code>, all resources having the
     * given property set are returned.<p>
     * 
     * Will use the {@link CmsResourceFilter#ALL} resource filter.<p>
     * 
     * @param path the folder to get the resources with the property from
     * @param propertyDefinition the name of the property to check for
     * @param value the string to search in the value of the property
     * 
     * @return all <code>{@link CmsResource}</code> objects 
     *          that have a value set for the specified property in the given path.
     * 
     * @throws CmsException if something goes wrong
     */
    public List readResourcesWithProperty(String path, String propertyDefinition, String value) throws CmsException {

        CmsResource resource = readResource(path, CmsResourceFilter.IGNORE_EXPIRATION);
        return m_securityManager.readResourcesWithProperty(
            m_context,
            resource,
            propertyDefinition,
            value,
            CmsResourceFilter.ALL);
    }

    /**
     * Reads all resources that have a value (containing the specified value) set 
     * for the specified property in the given path.<p>
     * 
     * Both individual and shared properties of a resource are checked.<p>
     *
     * If the <code>value</code> parameter is <code>null</code>, all resources having the
     * given property set are returned.<p>
     * 
     * Will use the given resource filter.<p>
     * 
     * @param path the folder to get the resources with the property from
     * @param propertyDefinition the name of the property to check for
     * @param value the string to search in the value of the property
     * @param filter the resource filter to apply to the result set
     * 
     * @return all <code>{@link CmsResource}</code> objects 
     *          that have a value set for the specified property in the given path.
     * 
     * @throws CmsException if something goes wrong
     */
    public List readResourcesWithProperty(String path, String propertyDefinition, String value, CmsResourceFilter filter)
    throws CmsException {

        CmsResource resource = readResource(path, CmsResourceFilter.IGNORE_EXPIRATION);
        return m_securityManager.readResourcesWithProperty(m_context, resource, propertyDefinition, value, filter);
    }
    
    public List readResourcesWithProperty(String path, String propertyDefinition, String value, CmsResourceFilter filter, Integer size, Integer offset)
    throws CmsException {

        CmsResource resource = readResource(path, CmsResourceFilter.IGNORE_EXPIRATION);
        return m_securityManager.readResourcesWithProperty(m_context, resource, propertyDefinition, value, filter, size, offset);
    }
    

    /**
     * Returns a set of principals that are responsible for a specific resource.<p>
     * 
     * @param resource the resource to get the responsible principals from
     * 
     * @return the set of principals that are responsible for a specific resource
     * 
     * @throws CmsException if something goes wrong
     */
    public Set readResponsiblePrincipals(CmsResource resource) throws CmsException {

        return m_securityManager.readResponsiblePrincipals(m_context, resource);
    }

    /**
     * Returns a set of users that are responsible for a specific resource.<p>
     * 
     * @param resource the resource to get the responsible users from
     * 
     * @return the set of users that are responsible for a specific resource
     * 
     * @throws CmsException if something goes wrong
     */
    public Set readResponsibleUsers(CmsResource resource) throws CmsException {

        return m_securityManager.readResponsibleUsers(m_context, resource);
    }

    /**
     * Returns a list of all siblings of the specified resource,
     * the specified resource being always part of the result set.<p>
     * 
     * @param resourcename the name of the specified resource
     * @param filter a resource filter
     * 
     * @return a list of <code>{@link CmsResource}</code>s that 
     *          are siblings to the specified resource, 
     *          including the specified resource itself.
     * 
     * @throws CmsException if something goes wrong
     */
    public List readSiblings(String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource resource = readResource(resourcename, filter);
        return m_securityManager.readSiblings(m_context, resource, filter);
    }

    /**
     * Returns the parameters of a resource in the list of all published template resources.<p>
     * 
     * @param rfsName the rfs name of the resource
     * 
     * @return the parameter string of the requested resource
     * 
     * @throws CmsException if something goes wrong
     */
    public String readStaticExportPublishedResourceParameters(String rfsName) throws CmsException {

        return m_securityManager.readStaticExportPublishedResourceParameters(m_context, rfsName);
    }

    /**
     * Returns a list of all template resources which must be processed during a static export.<p>
     * 
     * @param parameterResources flag for reading resources with parameters (1) or without (0)
     * 
     * @param timestamp a time stamp for reading the data from the db
     * 
     * @return a list of template resources as <code>{@link String}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public List readStaticExportResources(int parameterResources, long timestamp) throws CmsException {

        return m_securityManager.readStaticExportResources(m_context, parameterResources, timestamp);
    }

    /**
     * Reads a user based on its id.<p>
     *
     * @param userId the id of the user to be read
     * 
     * @return the user with the given id
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #readHistoryPrincipal(CmsUUID) for retrieving data of deleted users
     */
    public CmsUser readUser(CmsUUID userId) throws CmsException {

        return m_securityManager.readUser(m_context, userId);
    }

    /**
     * Reads a user based on its name.<p>
     *
     * @param username the name of the user to be read
     * 
     * @return the user with the given name
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsUser readUser(String username) throws CmsException {

        return m_securityManager.readUser(m_context, username);
    }

    /**
     * Returns a user, if the password is correct.<p>
     * 
     * If the user/pwd pair is not valid a <code>{@link CmsException}</code> is thrown.<p>
     *
     * @param username the name of the user to be returned
     * @param password the password of the user to be returned
     * 
     * @return the validated user
     *
     * @throws CmsException if operation was not successful
     */
    public CmsUser readUser(String username, String password) throws CmsException {

        return m_securityManager.readUser(m_context, username, password);
    }

    /**
     * Returns a web user.<p>
     *
     * @param username the user name of the web user that is to be read
     * 
     * @return the web user
     *
     * @throws CmsException if operation was not successful
     * 
     * @deprecated there are no more web users, use a user without any role!
     */
    public CmsUser readWebUser(String username) throws CmsException {

        return m_securityManager.readUser(m_context, username);
    }

    /**
     * Returns a web user if the password for the user is correct.<p>
     *
     * If the user/password pair is not valid a <code>{@link CmsException}</code> is thrown.<p>
     *
     * @param username the user name of the user that is to be read
     * @param password the password of the user that is to be read
     * 
     * @return a web user
     *
     * @throws CmsException if something goes wrong
     * 
     * @deprecated there are no more web users, use a user without any role!
     */
    public CmsUser readWebUser(String username, String password) throws CmsException {

        return m_securityManager.readUser(m_context, username, password);
    }

    /**
     * Removes a resource from the current project of the user.<p>
     * 
     * This is used to reduce the current users project with the
     * specified resource, in case that the resource is already part of the project.
     * The resource is not really removed like in a regular copy operation, 
     * it is in fact only "disabled" in the current users project.<p>   
     * 
     * @param resourcename the name of the resource to remove to the current project (full current site relative path)
     * 
     * @throws CmsException if something goes wrong
     */
    public void removeResourceFromProject(String resourcename) throws CmsException {

        // TODO: this should be also possible if the resource has been deleted
        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        getResourceType(resource).removeResourceFromProject(this, m_securityManager, resource);
    }

    /**
     * Removes a user from a group.<p>
     *
     * @param username the name of the user that is to be removed from the group
     * @param groupname the name of the group
     * 
     * @throws CmsException if operation was not successful
     */
    public void removeUserFromGroup(String username, String groupname) throws CmsException {

        m_securityManager.removeUserFromGroup(m_context, username, groupname, false);
    }

    /**
     * Renames a resource to the given destination name,
     * this is identical to a <code>move</code> operation.<p>
     *
     * @param source the name of the resource to rename (full current site relative path)
     * @param destination the new resource name (full path)
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see #moveResource(String, String)
     */
    public void renameResource(String source, String destination) throws CmsException {

        moveResource(source, destination);
    }

    /**
     * Replaces the content, type and properties of a resource.<p>
     * 
     * @param resourcename the name of the resource to replace (full current site relative path)
     * @param type the new type of the resource
     * @param content the new content of the resource
     * @param properties the new properties of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    public void replaceResource(String resourcename, int type, byte[] content, List properties) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource).replaceResource(this, m_securityManager, resource, type, content, properties);
    }

    /**
     * Restores a deleted resource identified by its structure id from the historical archive.<p>
     * 
     * These ids can be obtained from the {@link #readDeletedResources(String, boolean)} method.<p>
     * 
     * @param structureId the structure id of the resource to restore
     * 
     * @throws CmsException if something goes wrong
     */
    public void restoreDeletedResource(CmsUUID structureId) throws CmsException {

        m_securityManager.restoreDeletedResource(m_context, structureId);
    }

    /**
     * Restores a file in the current project with a version from the backup archive.<p>
     * 
     * @param resourcename the name of the resource to restore from the archive (full current site relative path)
     * @param publishTag the desired tag ID of the resource
     *
     * @throws CmsException if something goes wrong
     * 
     * @deprecated use {@link #restoreResourceVersion(CmsUUID, int)} instead,
     *             but notice that the <code>publishTag != version</code>
     *             and there is no possibility to access to an historical entry with just the filename.
     */
    public void restoreResourceBackup(String resourcename, int publishTag) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        I_CmsHistoryResource history = readResourceByPublishTag(resource.getStructureId(), publishTag);
        restoreResourceVersion(resource.getStructureId(), history.getVersion());
    }

    /**
     * Restores a resource in the current project with a version from the historical archive.<p>
     * 
     * @param structureId the structure id of the resource to restore from the archive
     * @param version the desired version of the resource to be restored
     *
     * @throws CmsException if something goes wrong
     * 
     * @see #readResource(CmsUUID, int)
     */
    public void restoreResourceVersion(CmsUUID structureId, int version) throws CmsException {

        CmsResource resource = readResource(structureId, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource).restoreResource(this, m_securityManager, resource, version);
    }

    /**
     * Removes an access control entry of a given principal from a given resource.<p>
     * 
     * @param resourceName name of the resource
     * @param principalType the type of the principal (currently group or user)
     * @param principalName the name of the principal
     * 
     * @throws CmsException if something goes wrong
     */
    public void rmacc(String resourceName, String principalType, String principalName) throws CmsException {

        CmsResource res = readResource(resourceName, CmsResourceFilter.ALL);

        if (CmsUUID.isValidUUID(principalName)) {
            // principal name is in fact a UUID, probably the user was already deleted
            m_securityManager.removeAccessControlEntry(m_context, res, new CmsUUID(principalName));
        } else {
            try {
                // principal name not a UUID, assume this is a normal group or user name
                I_CmsPrincipal principal = CmsPrincipal.readPrincipal(this, principalType, principalName);
                m_securityManager.removeAccessControlEntry(m_context, res, principal.getId());
            } catch (CmsDbEntryNotFoundException e) {
                // role case
                CmsRole role = CmsRole.valueOfRoleName(principalName);
                if (role == null) {
                    throw e;
                }
                m_securityManager.removeAccessControlEntry(m_context, res, role.getId());
            }
        }
    }

    /**
     * Changes the "expire" date of a resource.<p>
     * 
     * @param resourcename the name of the resource to change (full current site relative path)
     * @param dateExpired the new expire date of the changed resource
     * @param recursive if this operation is to be applied recursively to all resources in a folder
     * 
     * @throws CmsException if something goes wrong
     */
    public void setDateExpired(String resourcename, long dateExpired, boolean recursive) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource).setDateExpired(this, m_securityManager, resource, dateExpired, recursive);
    }

    /**
     * Changes the "last modified" time stamp of a resource.<p>
     * 
     * @param resourcename the name of the resource to change (full current site relative path)
     * @param dateLastModified time stamp the new time stamp of the changed resource
     * @param recursive if this operation is to be applied recursively to all resources in a folder
     * 
     * @throws CmsException if something goes wrong
     */
    public void setDateLastModified(String resourcename, long dateLastModified, boolean recursive) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource).setDateLastModified(this, m_securityManager, resource, dateLastModified, recursive);
    }

    /**
     * Changes the "release" date of a resource.<p>
     * 
     * @param resourcename the name of the resource to change (full current site relative path)
     * @param dateReleased the new release date of the changed resource
     * @param recursive if this operation is to be applied recursively to all resources in a folder
     * 
     * @throws CmsException if something goes wrong
     */
    public void setDateReleased(String resourcename, long dateReleased, boolean recursive) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource).setDateReleased(this, m_securityManager, resource, dateReleased, recursive);
    }

    /**
     * Sets a new parent-group for an already existing group.<p>
     *
     * @param groupName the name of the group that should be updated
     * @param parentGroupName the name of the parent group to set, 
     *                      or <code>null</code> if the parent
     *                      group should be deleted.
     * 
     * @throws CmsException  if operation was not successful
     */
    public void setParentGroup(String groupName, String parentGroupName) throws CmsException {

        m_securityManager.setParentGroup(m_context, groupName, parentGroupName);
    }

    /**
     * Sets the password for a user.<p>
     *
     * @param username the name of the user
     * @param newPassword the new password
     *
     * @throws CmsException if operation was not successful
     */
    public void setPassword(String username, String newPassword) throws CmsException {

        m_securityManager.setPassword(m_context, username, newPassword);
    }

    /**
     * Sets the password for a specified user.<p>
     *
     * @param username the name of the user
     * @param oldPassword the old password
     * @param newPassword the new password
     * 
     * @throws CmsException if the user data could not be read from the database
     */
    public void setPassword(String username, String oldPassword, String newPassword) throws CmsException {

        m_securityManager.resetPassword(m_context, username, oldPassword, newPassword);
    }

    /**
     * Changes the time stamp information of a resource.<p>
     * 
     * This method is used to set the "last modified" date
     * of a resource, the "release" date of a resource, 
     * and also the "expire" date of a resource.<p>
     * 
     * @param resourcename the name of the resource to change (full current site relative path)
     * @param dateLastModified time stamp the new time stamp of the changed resource
     * @param dateReleased the new release date of the changed resource, 
     *              set it to <code>{@link CmsResource#TOUCH_DATE_UNCHANGED}</code> to keep it unchanged.
     * @param dateExpired the new expire date of the changed resource. 
     *              set it to <code>{@link CmsResource#TOUCH_DATE_UNCHANGED}</code> to keep it unchanged.
     * @param recursive if this operation is to be applied recursively to all resources in a folder
     * 
     * @deprecated use <code>{@link #setDateLastModified(String, long, boolean)}</code>, 
     *                 <code>{@link #setDateReleased(String, long, boolean)}</code> or
     *                 <code>{@link #setDateExpired(String, long, boolean)}</code> instead
     * 
     * @throws CmsException if something goes wrong
     */
    public void touch(String resourcename, long dateLastModified, long dateReleased, long dateExpired, boolean recursive)
    throws CmsException {

        if (dateReleased != CmsResource.TOUCH_DATE_UNCHANGED) {
            setDateReleased(resourcename, dateReleased, recursive);
        }
        if (dateExpired != CmsResource.TOUCH_DATE_UNCHANGED) {
            setDateExpired(resourcename, dateExpired, recursive);
        }
        if (dateLastModified != CmsResource.TOUCH_DATE_UNCHANGED) {
            setDateLastModified(resourcename, dateLastModified, recursive);
        }
    }

    /**
     * Undeletes a resource (this is the same operation as "undo changes").<p>
     * 
     * Only resources that have already been published once can be undeleted,
     * if a "new" resource is deleted it can not be undeleted.<p>
     * 
     * Internally, this method undoes all changes to a resource by restoring 
     * the version from the online project, that is to the state of last 
     * publishing.<p>
     * 
     * @param resourcename the name of the resource to undelete (full path)
     *
     * @throws CmsException if something goes wrong
     * 
     * @deprecated use {@link #undeleteResource(String,boolean)} methods instead
     */
    public void undeleteResource(String resourcename) throws CmsException {

        undeleteResource(resourcename, false);
    }

    /**
     * Undeletes a resource.<p>
     * 
     * Only resources that have already been published once can be undeleted,
     * if a "new" resource is deleted it can not be undeleted.<p>
     * 
     * @param resourcename the name of the resource to undelete
     * @param recursive if this operation is to be applied recursively to all resources in a folder
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#undoChanges(String, CmsResource.CmsResourceUndoMode)
     */
    public void undeleteResource(String resourcename, boolean recursive) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        getResourceType(resource).undelete(this, m_securityManager, resource, recursive);
    }

    /**
     * Undoes all changes to a resource by restoring the version from the 
     * online project to the current offline project.<p>
     * 
     * @param resourcename the name of the resource to undo the changes for (full path)
     * @param recursive if this operation is to be applied recursively to all resources in a folder
     *
     * @throws CmsException if something goes wrong
     * 
     * @deprecated use {@link #undoChanges(String,CmsResource.CmsResourceUndoMode)} methods instead
     */
    public void undoChanges(String resourcename, boolean recursive) throws CmsException {

        if (recursive) {
            undoChanges(resourcename, CmsResource.UNDO_CONTENT_RECURSIVE);
        } else {
            undoChanges(resourcename, CmsResource.UNDO_CONTENT);
        }
    }

    /**
     * Undoes all changes to a resource by restoring the version from the 
     * online project to the current offline project.<p>
     * 
     * @param resourcename the name of the resource to undo the changes for
     * @param mode the undo mode, one of the <code>{@link CmsResource.CmsResourceUndoMode}#UNDO_XXX</code> constants
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsResource#UNDO_CONTENT
     * @see CmsResource#UNDO_CONTENT_RECURSIVE
     * @see CmsResource#UNDO_MOVE_CONTENT
     * @see CmsResource#UNDO_MOVE_CONTENT_RECURSIVE
     */
    public void undoChanges(String resourcename, CmsResource.CmsResourceUndoMode mode) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        getResourceType(resource).undoChanges(this, m_securityManager, resource, mode);
    }

    /**
     * Unlocks all resources of a project.
     *
     * @param id the id of the project to be unlocked
     *
     * @throws CmsException if operation was not successful
     */
    public void unlockProject(CmsUUID id) throws CmsException {

        m_securityManager.unlockProject(m_context, id);
    }

    /**
     * Unlocks all resources of a project.
     *
     * @param id the id of the project to be unlocked
     *
     * @throws CmsException if operation was not successful
     * 
     * @deprecated use {@link #unlockProject(CmsUUID)} instead
     */
    public void unlockProject(int id) throws CmsException {

        unlockProject(m_securityManager.getProjectId(m_context, id));
    }

    /**
     * Unlocks a resource.<p>
     * 
     * @param resourcename the name of the resource to unlock (full current site relative path)
     * 
     * @throws CmsException if something goes wrong
     */
    public void unlockResource(String resourcename) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        getResourceType(resource).unlockResource(this, m_securityManager, resource);
    }

    /**
     * Tests if a user is member of the given group.<p>
     *
     * @param username the name of the user to test
     * @param groupname the name of the group to test
     * 
     * @return <code>true</code>, if the user is in the group; or <code>false</code> otherwise
     *
     * @throws CmsException if operation was not successful
     */
    public boolean userInGroup(String username, String groupname) throws CmsException {

        return (m_securityManager.userInGroup(m_context, username, groupname));
    }

    /**
     * This method checks if a new password follows the rules for
     * new passwords, which are defined by a Class implementing the 
     * <code>{@link org.opencms.security.I_CmsPasswordHandler}</code> 
     * interface and configured in the opencms.properties file.<p>
     * 
     * If this method throws no exception the password is valid.<p>
     *
     * @param password the new password that has to be checked
     *
     * @throws CmsSecurityException if the password is not valid
     */
    public void validatePassword(String password) throws CmsSecurityException {

        m_securityManager.validatePassword(password);
    }

    /**
     * Writes a resource to the OpenCms VFS, including it's content.<p>
     * 
     * Applies only to resources of type <code>{@link CmsFile}</code>
     * i.e. resources that have a binary content attached.<p>
     * 
     * Certain resource types might apply content validation or transformation rules 
     * before the resource is actually written to the VFS. The returned result
     * might therefore be a modified version from the provided original.<p>
     *
     * @param resource the resource to write
     *
     * @return the written resource (may have been modified)
     *
     * @throws CmsException if something goes wrong
     */
    public CmsFile writeFile(CmsFile resource) throws CmsException {

        return getResourceType(resource).writeFile(this, m_securityManager, resource);
    }

    /**
     * Writes a file-header.<p>
     *
     * @param file the file to write
     *
     * @throws CmsException if resource type is set to folder, or
     *                      if the user has not the rights to write the file header.
     *                      
     * @deprecated use {@link #writeResource(CmsResource)} instead
     */
    public void writeFileHeader(CmsFile file) throws CmsException {

        writeResource(file);
    }

    /**
     * Writes an already existing group.<p>
     *
     * The group has to be a valid OpenCms group.<br>
     * 
     * The group will be completely overridden by the given data.<p>
     *
     * @param group the group that should be written
     * 
     * @throws CmsException if operation was not successful
     */
    public void writeGroup(CmsGroup group) throws CmsException {

        m_securityManager.writeGroup(m_context, group);
    }

    /**
     * Creates a historical entry of the current project.<p>
     * 
     * @param publishTag the correlative publish tag
     * @param publishDate the date of publishing

     * @throws CmsException if operation was not successful
     */
    public void writeHistoryProject(int publishTag, long publishDate) throws CmsException {

        m_securityManager.writeHistoryProject(m_context, publishTag, publishDate);
    }

    /**
     * Writes an already existing project.<p>
     *
     * The project id has to be a valid OpenCms project id.<br>
     * 
     * The project with the given id will be completely overridden
     * by the given data.<p>
     *
     * @param project the project that should be written
     * 
     * @throws CmsException if operation was not successful
     */
    public void writeProject(CmsProject project) throws CmsException {

        m_securityManager.writeProject(m_context, project);
    }

    /**
     * Writes a couple of properties as structure values for a file or folder.
     *
     * @param resourceName the resource-name of which the Property has to be set
     * @param properties a map with property-definitions and property values as Strings
     * 
     * @throws CmsException if operation was not successful
     * 
     * @deprecated use <code>{@link #writePropertyObjects(String, List)}</code> instead.
     */
    public void writeProperties(String resourceName, Map properties) throws CmsException {

        writePropertyObjects(resourceName, CmsProperty.toList(properties));
    }

    /**
     * Writes a couple of Properties for a file or folder.
     *
     * @param name the resource-name of which the Property has to be set
     * @param properties a map with property-definitions and property values as Strings
     * @param addDefinition flag to indicate if unknown definitions should be added
     * 
     * @throws CmsException if operation was not successful
     * 
     * @deprecated use <code>{@link #writePropertyObjects(String, List)}</code> instead.
     */
    public void writeProperties(String name, Map properties, boolean addDefinition) throws CmsException {

        writePropertyObjects(name, CmsProperty.setAutoCreatePropertyDefinitions(
            CmsProperty.toList(properties),
            addDefinition));
    }

    /**
     * Writes a property as a structure value for a file or folder.<p>
     *
     * @param resourceName the resource-name for which the property will be set
     * @param key the property definition name
     * @param value the value for the property to be set
     * 
     * @throws CmsException if operation was not successful
     * 
     * @deprecated use <code>{@link #writePropertyObject(String, CmsProperty)}</code> instead.
     */
    public void writeProperty(String resourceName, String key, String value) throws CmsException {

        CmsProperty property = new CmsProperty();
        property.setName(key);
        property.setStructureValue(value);

        writePropertyObject(resourceName, property);
    }

    /**
     * Writes a property for a file or folder.<p>
     *
     * @param resourcename the resource-name for which the property will be set
     * @param key the property-definition name
     * @param value the value for the property to be set
     * @param addDefinition flag to indicate if unknown definitions should be added
     * 
     * @throws CmsException if operation was not successful
     * 
     * @deprecated use <code>{@link #writePropertyObject(String, CmsProperty)}</code> instead.
     */
    public void writeProperty(String resourcename, String key, String value, boolean addDefinition) throws CmsException {

        CmsProperty property = new CmsProperty();
        property.setName(key);
        property.setStructureValue(value);
        property.setAutoCreatePropertyDefinition(addDefinition);

        writePropertyObject(resourcename, property);
    }

    /**
     * Writes a property for a specified resource.<p>
     * 
     * @param resourcename the name of resource with complete path
     * @param property the property to write
     * 
     * @throws CmsException if something goes wrong
     */
    public void writePropertyObject(String resourcename, CmsProperty property) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource).writePropertyObject(this, m_securityManager, resource, property);
    }

    /**
     * Writes a list of properties for a specified resource.<p>
     * 
     * Code calling this method has to ensure that the no properties 
     * <code>a, b</code> are contained in the specified list so that <code>a.equals(b)</code>, 
     * otherwise an exception is thrown.<p>
     * 
     * @param resourcename the name of resource with complete path
     * @param properties the list of properties to write
     * 
     * @throws CmsException if something goes wrong
     */
    public void writePropertyObjects(String resourcename, List properties) throws CmsException {

        CmsResource resource = readResource(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        getResourceType(resource).writePropertyObjects(this, m_securityManager, resource, properties);
    }

    /**
     * Writes a resource.<p>
     *
     * @param resource the file to write
     *
     * @throws CmsException if resource type is set to folder, or
     *                      if the user has not the rights to write the file header.
     */
    public void writeResource(CmsResource resource) throws CmsException {

        m_securityManager.writeResource(m_context, resource);
    }

    /**
     * Writes a published resource entry.<p>
     * 
     * This is done during static export.<p>
     * 
     * @param resourceName The name of the resource to be added to the static export
     * @param linkType the type of resource exported (0= non-parameter, 1=parameter)
     * @param linkParameter the parameters added to the resource
     * @param timestamp a time stamp for writing the data into the db
     * 
     * @throws CmsException if something goes wrong
     */
    public void writeStaticExportPublishedResource(
        String resourceName,
        int linkType,
        String linkParameter,
        long timestamp) throws CmsException {

        m_securityManager.writeStaticExportPublishedResource(
            m_context,
            resourceName,
            linkType,
            linkParameter,
            timestamp);
    }

    public void updateUserExternalProvider(CmsUserExternalProvider userProv) throws CmsDataAccessException {
    	m_securityManager.updateUserExternalProvider(m_context, userProv); 
    }
    public void createUserExternalProvider(CmsUserExternalProvider userProv) throws CmsDataAccessException {
    	m_securityManager.createUserExternalProvider(m_context, userProv); 
    }
    
    public String createToken(CmsUser user, String browserId)  throws CmsException {
    	return m_securityManager.createToken(m_context, user,browserId);
    }
    
    public String getNext2FAAccessCode(String tempId, boolean timebased) throws CmsException {
    	return m_securityManager.getNext2FAAccessCode(m_context, tempId, timebased);    			
    }
    
    public CmsUser getUser2FAByTemp(String tempId) throws CmsException {
    	return m_securityManager.getUser2FAByTemp(m_context, tempId);    			
    }
    
    public boolean use2FAUniqueCode(String tempId, String code) throws CmsException {
    	return m_securityManager.validate2FAUniqueCode(m_context, tempId, code, m_context.getRemoteAddress());    			
    }
    
    public boolean validate2FACode(CmsUser user, String code, boolean timebased) throws CmsException {
    	return m_securityManager.validate2FACode(m_context, user, code, timebased, m_context.getRemoteAddress());    			
    }
    
    public boolean validate2FACode(String tempId, String code, boolean timebased) throws CmsException {
    	return m_securityManager.validate2FACode(m_context, tempId, code, timebased, m_context.getRemoteAddress());    			
    }
    
    public String FirstStep2FA(String username, String password) throws CmsException {
        return FirstStep2FA(username, password,  m_context.getRemoteAddress());
    }
    
    public String FirstStep2FA(String data) throws CmsException {
    	return m_securityManager.validateCredentials2FA(m_context,data,  m_context.getRemoteAddress());
    }
    
    public String FirstStep2FA(String username, String password, String remoteAddress) throws CmsException {
        return m_securityManager.validateCredentials2FA(m_context,username, password, remoteAddress);
    }

    public boolean validate2FACode(String tempId, String code, boolean timebased, String remoteAddress) throws CmsException {
    	return m_securityManager.validate2FACode(m_context, tempId, code, timebased, remoteAddress);    			
    }

    public void createOrRecreate2FA(CmsUser user)  throws CmsException {
    	m_securityManager.createOrRecreateTwoFactorCode(m_context, user);    			
    }
    
    public String genereteQRCodeTwoFactor(CmsUser user, boolean timebased) {
    	return m_securityManager.genereteQRCodeTwoFactor(m_context,user,timebased);
    }
    
    public String[] getUniqueCodes2FA(CmsUser user) {
    	return m_securityManager.getUniqueCodes2FA(m_context,user);
    }
    
    /**
     * Updates the user information. <p>
     * 
     * The user id has to be a valid OpenCms user id.<br>
     * 
     * The user with the given id will be completely overriden
     * by the given data.<p>
     *
     * @param user the user to be written
     *
     * @throws CmsException if operation was not successful
     */
    public void writeUser(CmsUser user) throws CmsException {

        m_securityManager.writeUser(m_context, user);
    }

    /**
     * Updates the user information of a web user.<br>
     * 
     * Only a web user can be updated this way.<p>
     *
     * The user id has to be a valid OpenCms user id.<br>
     * 
     * The user with the given id will be completely overridden
     * by the given data.<p>
     *
     * @param user the user to be written
     *
     * @throws CmsException if operation was not successful
     * 
     * @deprecated there are no more web users, use a user without any role!
     */
    public void writeWebUser(CmsUser user) throws CmsException {

        m_securityManager.writeUser(m_context, user);
    }

    /**
     * Convenience method to add the site root from the current user's 
     * request context to the given resource name.<p>
     *
     * @param resourcename the resource name
     * 
     * @return the resource name with the site root added
     * 
     * @see CmsRequestContext#addSiteRoot(String)
     */
    private String addSiteRoot(String resourcename) {

        return m_context.addSiteRoot(resourcename);
    }

    /**
     * Adds a new relation to the given resource.<p>
     * 
     * @param resourceName the name of the source resource
     * @param targetPath the path of the target resource
     * @param relationType the type of the relation
     * @param importCase if importing relations
     * 
     * @throws CmsException if something goes wrong
     */
    private void createRelation(String resourceName, String targetPath, String relationType, boolean importCase)
    throws CmsException {

        CmsResource resource = readResource(resourceName, CmsResourceFilter.IGNORE_EXPIRATION);
        CmsResource target = readResource(targetPath, CmsResourceFilter.IGNORE_EXPIRATION);
        CmsRelationType type = CmsRelationType.valueOf(relationType);
        m_securityManager.addRelationToResource(m_context, resource, target, type, importCase);
    }

    /**
     * Notify all event listeners that a particular event has occurred.<p>
     * 
     * The event will be given to all registered <code>{@link I_CmsEventListener}</code>s.<p>
     * 
     * @param type the type of the event
     * @param data a data object that contains data used by the event listeners
     * 
     * @see OpenCms#addCmsEventListener(I_CmsEventListener)
     * @see OpenCms#addCmsEventListener(I_CmsEventListener, int[])
     */
    private void fireEvent(int type, Object data) {

        OpenCms.fireCmsEvent(type, Collections.singletonMap("data", data));
    }

    /**
     * Convenience method to get the initialized resource type instance for the given resource, 
     * with a fall back to special "unknown" resource types in case the resource type is not configured.<p>
     * 
     * @param resource the resource to get the type for
     * 
     * @return the initialized resource type instance for the given resource
     * 
     * @see org.opencms.loader.CmsResourceManager#getResourceType(int)
     */
    private I_CmsResourceType getResourceType(CmsResource resource) {

        return OpenCms.getResourceManager().getResourceType(resource);
    }

    /**
     * Convenience method to return the initialized resource type 
     * instance for the given id.<p>
     * 
     * @param resourceType the id of the resource type to get
     * 
     * @return the initialized resource type instance for the given id
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see org.opencms.loader.CmsResourceManager#getResourceType(int)
     */
    private I_CmsResourceType getResourceType(int resourceType) throws CmsException {

        return OpenCms.getResourceManager().getResourceType(resourceType);
    }

    /**
     * Initializes this <code>{@link CmsObject}</code> with the provided user context and database connection.<p>
     * 
     * @param securityManager the security manager
     * @param context the request context that contains the user authentication
     */
    private void init(CmsSecurityManager securityManager, CmsRequestContext context) {

        m_securityManager = securityManager;
        m_context = context;
    }

    /**
     * Locks a resource.<p>
     *
     * The <code>type</code> parameter controls what kind of lock is used.<br>
     * Possible values for this parameter are: <br>
     * <ul>
     * <li><code>{@link org.opencms.lock.CmsLockType#EXCLUSIVE}</code></li>
     * <li><code>{@link org.opencms.lock.CmsLockType#TEMPORARY}</code></li>
     * </ul><p>
     * 
     * @param resourcename the name of the resource to lock (full current site relative path)
     * @param type type of the lock
     * 
     * @throws CmsException if something goes wrong
     */
    private void lockResource(String resourcename, CmsLockType type)
    throws CmsException, CmsVfsResourceNotFoundException {

        // throw the exception if resource name is an empty string
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(resourcename)) {
            throw new CmsVfsResourceNotFoundException(Messages.get().container(
                Messages.ERR_LOCK_RESOURCE_1,
                resourcename));
        }

        CmsResource resource = readResource(resourcename, CmsResourceFilter.ALL);
        getResourceType(resource).lockResource(this, m_securityManager, resource, type);
    }
}