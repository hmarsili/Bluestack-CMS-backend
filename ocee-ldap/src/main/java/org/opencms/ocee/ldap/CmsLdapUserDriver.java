package org.opencms.ocee.ldap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.I_CmsUserDriver;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsUserExternalProvider;
import org.opencms.file.CmsUserToken;
import org.opencms.file.CmsUserTwoFactor;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsInitException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.ldap.CmsLdapAccessException;
import org.opencms.ocee.ldap.CmsLdapConfiguration;
import org.opencms.ocee.ldap.CmsLdapGroupRoleMappings;
import org.opencms.ocee.ldap.CmsLdapManager;
import org.opencms.ocee.ldap.CmsLdapUserDefinition;
import org.opencms.ocee.ldap.Messages;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsDefaultPasswordHandler;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPasswordEncryptionException;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.I_CmsPasswordHandler;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

public class CmsLdapUserDriver
implements I_CmsUserDriver {
    public static final String ATTRIBUTE_FORCE_UPDATE_MEMBERSHIP = "__FORCE_UPDATE_MEMBERSHIP";
    public static final String TRIGGER_UPDATE_MEMBERSHIP = "_UPDATE_MEMBERSHIP_";
    private static final Log LOG = CmsLog.getLog(CmsLdapUserDriver.class);
    private I_CmsUserDriver userDriver;
    private String pwdDigestEncoding;
    private String pwdDigestType;
    private CmsDriverManager driverManager;
    private long lastCheck;
    private I_CmsPasswordHandler passwordHandler;

    public void addResourceToOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, CmsResource resource) throws CmsDataAccessException {
        this.userDriver.addResourceToOrganizationalUnit(dbc, orgUnit, resource);
    }

    public void createAccessControlEntry(CmsDbContext dbc, CmsProject project, CmsUUID resource, CmsUUID principal, int allowed, int denied, int flags) throws CmsDataAccessException {
        this.userDriver.createAccessControlEntry(dbc, project, resource, principal, allowed, denied, flags);
    }

    public CmsGroup createGroup(CmsDbContext dbc, CmsUUID groupId, String groupName, String description, int flags, String parentGroupName) throws CmsDataAccessException {
        CmsLdapManager manager = CmsLdapManager.getInstance();
        if (manager != null && manager.hasConnection(dbc)) {
            if (manager.isLdapOnly() && !CmsLdapManager.hasLdapFlag(flags) && !this.isCallFromReplication()) {
                throw new CmsLdapAccessException(Messages.get().container("ERR_LDAP_CREATE_CMSGROUP_1", (Object)groupName));
            }
            if (manager.getLdapGroup().getName().equals(groupName)) {
                throw new CmsLdapAccessException(Messages.get().container("ERR_LDAP_VRGROUP_INMUTABLE_0"));
            }
        }
        return this.userDriver.createGroup(dbc, groupId, groupName, description, flags, parentGroupName);
    }

    public CmsOrganizationalUnit createOrganizationalUnit(CmsDbContext dbc, String name, String description, int flags, CmsOrganizationalUnit parent, String associationRootPath) throws CmsDataAccessException {
        return this.userDriver.createOrganizationalUnit(dbc, name, description, flags, parent, associationRootPath);
    }

    public void createRootOrganizationalUnit(CmsDbContext dbc) {
        this.userDriver.createRootOrganizationalUnit(dbc);
    }

    public CmsUser createUser(CmsDbContext dbc, CmsUUID id, String userFqn, String password, String firstname, String lastname, String email, long lastlogin, int flags, long dateCreated, Map additionalInfos) throws CmsDataAccessException {
        CmsLdapManager manager = CmsLdapManager.getInstance();
        if (manager != null && manager.hasConnection(dbc) && !CmsLdapManager.hasLdapFlag(flags)) {
            if (manager.isLdapOnly() && !this.isCallFromReplication()) {
                if (OpenCms.getDefaultUsers().isDefaultUser(userFqn)) {
                    try {
                        CmsUser user = manager.lookupUser(dbc, userFqn, null);
                        return this.addCmsLdapUser(dbc, user);
                    }
                    catch (CmsDbEntryNotFoundException e) {
                        throw new CmsIllegalArgumentException(Messages.get().container("ERR_LDAP_CREATE_CMSUSER_1", (Object)userFqn));
                    }
                }
                throw new CmsIllegalArgumentException(Messages.get().container("ERR_LDAP_CREATE_CMSUSER_1", (Object)userFqn));
            }
            if (!OpenCms.getDefaultUsers().isDefaultUser(userFqn)) {
                try {
                    manager.lookupUser(dbc, userFqn, null);
                    throw new CmsIllegalArgumentException(Messages.get().container("ERR_LDAP_USER_IN_USE_0"));
                }
                catch (CmsDbEntryNotFoundException e) {
                    // empty catch block
                }
            }
        }
        return this.userDriver.createUser(dbc, id, userFqn, password, firstname, lastname, email, lastlogin, flags, dateCreated, additionalInfos);
    }

    public void createUserInGroup(CmsDbContext dbc, CmsUUID userid, CmsUUID groupid) throws CmsDataAccessException {
        CmsLdapManager manager = CmsLdapManager.getInstance();
        if (manager != null && manager.hasConnection(dbc) && CmsOceeManager.LDAP_GROUP_ID.equals((Object)groupid)) {
            if (!this.isCallFromDeleteMethod() && !CmsLdapManager.hasLdapFlag(this.userDriver.readUser(dbc, userid).getFlags())) {
                throw new CmsIllegalArgumentException(Messages.get().container("ERR_LDAP_VRGROUP_INMUTABLE_0"));
            }
            return;
        }
        this.userDriver.createUserInGroup(dbc, userid, groupid);
    }

    public void deleteAccessControlEntries(CmsDbContext dbc, CmsProject project, CmsUUID resource) throws CmsDataAccessException {
        this.userDriver.deleteAccessControlEntries(dbc, project, resource);
    }

    public void deleteGroup(CmsDbContext dbc, String name) throws CmsDataAccessException {
        CmsLdapManager manager = CmsLdapManager.getInstance();
        if (manager != null && manager.hasConnection(dbc) && manager.getLdapGroup().getName().equals(name)) {
            throw new CmsIllegalArgumentException(Messages.get().container("ERR_LDAP_VRGROUP_INMUTABLE_0"));
        }
        this.userDriver.deleteGroup(dbc, name);
    }

    public void deleteOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit organizationalUnit) throws CmsDataAccessException {
        this.userDriver.deleteOrganizationalUnit(dbc, organizationalUnit);
    }

    public void deleteUser(CmsDbContext dbc, String userName) throws CmsDataAccessException {
        this.userDriver.deleteUser(dbc, userName);
    }

    public void deleteUserInfos(CmsDbContext dbc, CmsUUID userId) throws CmsDataAccessException {
        this.userDriver.deleteUserInfos(dbc, userId);
    }

    public void deleteUserInGroup(CmsDbContext dbc, CmsUUID userId, CmsUUID groupId) throws CmsDataAccessException {
        CmsLdapManager manager = CmsLdapManager.getInstance();
        if (manager != null && manager.hasConnection(dbc)) {
            CmsGroup group;
            if (CmsOceeManager.LDAP_GROUP_ID.equals((Object)groupId)) {
                return;
            }
            CmsUser user = this.readUser(dbc, userId);
            if (CmsLdapManager.hasLdapFlag(user.getFlags()) && CmsLdapManager.hasLdapFlag((group = this.readGroup(dbc, groupId)).getFlags()) && !this.isCallFromDeleteMethod() && manager.lookupUserNames(dbc, group).contains(user.getName())) {
                throw new CmsLdapAccessException(Messages.get().container("ERR_LDAP_CONSISTENCY_2", (Object)user.getName(), (Object)group.getName()));
            }
        }
        this.userDriver.deleteUserInGroup(dbc, userId, groupId);
    }

    public void destroy() throws Throwable, CmsException {
        this.userDriver.destroy();
    }

    public boolean existsGroup(CmsDbContext dbc, String groupName) throws CmsDataAccessException {
        CmsLdapManager manager = CmsLdapManager.getInstance();
        if (manager != null && manager.hasConnection(dbc) && manager.getLdapGroup().getName().equals(groupName)) {
            return true;
        }
        return this.userDriver.existsGroup(dbc, groupName);
    }

    public boolean existsUser(CmsDbContext dbc, String userName) throws CmsDataAccessException {
        return this.userDriver.existsUser(dbc, userName);
    }

    public void fillDefaults(CmsDbContext dbc) throws CmsInitException {
        this.userDriver.fillDefaults(dbc);
    }

    public List getGroups(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, boolean includeSubOus, boolean readRoles) throws CmsDataAccessException {
        ArrayList<CmsGroup> ret = new ArrayList<CmsGroup>(this.userDriver.getGroups(dbc, orgUnit, includeSubOus, readRoles));
        CmsLdapManager manager = CmsLdapManager.getInstance();
        if (manager != null && manager.hasConnection(dbc) && !readRoles && orgUnit.getParentFqn() == null) {
            ret.add(manager.getLdapGroup());
        }
        return this.removeDeletedGroups(dbc, ret);
    }

    public List getOrganizationalUnits(CmsDbContext dbc, CmsOrganizationalUnit parent, boolean includeChilds) throws CmsDataAccessException {
        return this.userDriver.getOrganizationalUnits(dbc, parent, includeChilds);
    }

    public List getResourcesForOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit orgUnit) throws CmsDataAccessException {
        return this.userDriver.getResourcesForOrganizationalUnit(dbc, orgUnit);
    }

    public CmsSqlManager getSqlManager() {
        return this.userDriver.getSqlManager();
    }

    public List getUsers(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, boolean recursive) throws CmsDataAccessException {
        List users = this.userDriver.getUsers(dbc, orgUnit, recursive);
        CmsLdapManager manager = CmsLdapManager.getInstance();
        if (manager != null && manager.hasConnection(dbc)) {
            return this.removeDeletedUsers(dbc, users);
        }
        return users;
    }

    public void init(CmsDbContext dbc, CmsConfigurationManager configurationManager, List successiveDrivers, CmsDriverManager driverManager) {
        ExtendedProperties config;
        Map configuration = configurationManager.getConfiguration();
        if (configuration instanceof ExtendedProperties) {
            config = (ExtendedProperties)configuration;
        } else {
            config = new ExtendedProperties();
            config.putAll(configuration);
        }
        String driverName = config.getString((String)successiveDrivers.get(0) + ".user.driver");
        successiveDrivers = successiveDrivers.size() > 1 ? successiveDrivers.subList(1, successiveDrivers.size()) : null;
        this.driverManager = driverManager;
        this.userDriver = (I_CmsUserDriver)driverManager.newDriverInstance(configurationManager, driverName, successiveDrivers);
        CmsLdapManager manager = CmsLdapManager.getInstance();
        if (manager != null) {
            manager.checkOpenCmsConfiguration(configuration);
            this.pwdDigestType = manager.getConfiguration().getPwdDigestType();
            this.pwdDigestEncoding = manager.getConfiguration().getPwdDigestEncoding();
            this.passwordHandler = new CmsDefaultPasswordHandler();
        }
    }

    public CmsSqlManager initSqlManager(String classname) {
        return this.userDriver.initSqlManager(classname);
    }

    public void publishAccessControlEntries(CmsDbContext dbc, CmsProject offlineProject, CmsProject onlineProject, CmsUUID offlineId, CmsUUID onlineId) throws CmsDataAccessException {
        this.userDriver.publishAccessControlEntries(dbc, offlineProject, onlineProject, offlineId, onlineId);
    }

    public List readAccessControlEntries(CmsDbContext dbc, CmsProject project, CmsUUID resource, boolean inheritedOnly) throws CmsDataAccessException {
        return this.userDriver.readAccessControlEntries(dbc, project, resource, inheritedOnly);
    }

    public CmsAccessControlEntry readAccessControlEntry(CmsDbContext dbc, CmsProject project, CmsUUID resource, CmsUUID principal) throws CmsDataAccessException {
        return this.userDriver.readAccessControlEntry(dbc, project, resource, principal);
    }

    public List readChildGroups(CmsDbContext dbc, String groupname) throws CmsDataAccessException {
        CmsLdapManager manager = CmsLdapManager.getInstance();
        if (manager != null && manager.hasConnection(dbc) && manager.getLdapGroup().getName().equals(groupname)) {
            return Collections.EMPTY_LIST;
        }
        return this.userDriver.readChildGroups(dbc, groupname);
    }

    public CmsGroup readGroup(CmsDbContext dbc, CmsUUID groupId) throws CmsDataAccessException {
        CmsLdapManager manager = CmsLdapManager.getInstance();
        if (manager != null && manager.hasConnection(dbc) && CmsOceeManager.LDAP_GROUP_ID.equals((Object)groupId)) {
            return manager.getLdapGroup();
        }
        return this.userDriver.readGroup(dbc, groupId);
    }

    public CmsGroup readGroup(CmsDbContext dbc, String groupName) throws CmsDataAccessException {
        CmsLdapManager manager = CmsLdapManager.getInstance();
        if (manager != null && manager.hasConnection(dbc) && manager.getLdapGroup().getName().equals(groupName)) {
            return manager.getLdapGroup();
        }
        return this.userDriver.readGroup(dbc, groupName);
    }

    public List readGroupsOfUser(CmsDbContext dbc, CmsUUID userId, String ouFqn, boolean includeChildOus, String remoteAddress, boolean readRoles) throws CmsDataAccessException {
        ArrayList<CmsGroup> ret = new ArrayList<CmsGroup>(this.userDriver.readGroupsOfUser(dbc, userId, ouFqn, includeChildOus, remoteAddress, readRoles));
        CmsLdapManager manager = CmsLdapManager.getInstance();
        if (manager != null && manager.hasConnection(dbc)) {
            CmsUser user = this.userDriver.readUser(dbc, userId);
            
            //LOG.error("readGroupsOfUser " + user.getName() + " - has ldap Connection!");
            
            boolean isLdapUser = CmsLdapManager.hasLdapFlag(user.getFlags());
            
            //LOG.error("readGroupsOfUser " + user.getName() + " - isLdapUser: " + isLdapUser);
            
            if (!readRoles && isLdapUser) {
                if (CmsStringUtil.isEmptyOrWhitespaceOnly((String)ouFqn)) {
                    ret.add(manager.getLdapGroup());
                }
                return ret;
            }
            
            CmsLdapGroupRoleMappings groupRoleMappings = manager.getConfiguration().getGroupRoleMappings();
            if (readRoles && groupRoleMappings != null) {
                if (!groupRoleMappings.isInitialized()) {
                    groupRoleMappings.initialize(this.userDriver, dbc);
                }
                List userGroups = this.userDriver.readGroupsOfUser(dbc, userId, ouFqn, includeChildOus, remoteAddress, false);
                
                //for (CmsGroup groups : (List<CmsGroup>)userGroups) {
                //	 LOG.error("readGroupsOfUser " + user.getName() + " - group: " + groups.getName());
                //}
                ArrayList<CmsGroup> userGroupsForRoleMapping = new ArrayList<CmsGroup>(userGroups);
                if (isLdapUser) {
                    userGroupsForRoleMapping.add(CmsLdapManager.getInstance().getLdapGroup());
                }
                for (CmsGroup roleGroup : (Collection<CmsGroup>)groupRoleMappings.getMappedRoles(userGroupsForRoleMapping)) {
                	 //LOG.error("readGroupsOfUser " + user.getName() + " - getMappedRoles: " + roleGroup.getName());
                	if (ret.contains((Object)roleGroup)) continue;
                    ret.add(roleGroup);
                }
            }
        }
        return ret;
    }

    public CmsOrganizationalUnit readOrganizationalUnit(CmsDbContext dbc, String ouFqn) throws CmsDataAccessException {
        return this.userDriver.readOrganizationalUnit(dbc, ouFqn);
    }

    public CmsUser readUser(CmsDbContext dbc, CmsUUID id) throws CmsDataAccessException {
        CmsUser dbu = this.userDriver.readUser(dbc, id);
        CmsLdapManager manager = CmsLdapManager.getInstance();
        if (manager != null && manager.hasConnection(dbc)) {
            CmsUser user = null;
            user = !this.isCallFromDeleteMethod() && (dbc.getRequestContext() == null || dbc.getRequestContext().getAttribute("SKIP_SYNC") == null || (Boolean)dbc.getRequestContext().getAttribute("SKIP_SYNC") == false) ? manager.synchronizeUser(dbc, dbu, null) : dbu;
            if (user == null) {
                CmsMessageContainer message = org.opencms.db.generic.Messages.get().container("ERR_NO_USER_WITH_ID_1", (Object)id.toString());
                if (LOG.isDebugEnabled()) {
                    LOG.debug((Object)message.key());
                }
                throw new CmsDbEntryNotFoundException(message);
            }
            return user;
        }
        return dbu;
    }

    public CmsUser readUser(CmsDbContext dbc, String userFqn) throws CmsDataAccessException {
        if (dbc.getRequestContext() == null || dbc.getRequestContext().getAttribute("SKIP_SYNC") == null || !((Boolean)dbc.getRequestContext().getAttribute("SKIP_SYNC")).booleanValue()) {
            try {
                String password = null;
                if (LOG.isDebugEnabled()) {
                    LOG.debug((Object)("Request context info: " + (dbc.getRequestContext() == null ? "null" : new StringBuilder().append("context available, force_membership attribute: ").append(String.valueOf(dbc.getRequestContext().getAttribute("__FORCE_UPDATE_MEMBERSHIP"))).toString())));
                }
                if (dbc.getRequestContext() != null && dbc.getRequestContext().getAttribute("__FORCE_UPDATE_MEMBERSHIP") != null && ((Boolean)dbc.getRequestContext().getAttribute("__FORCE_UPDATE_MEMBERSHIP")).booleanValue()) {
                    password = "_UPDATE_MEMBERSHIP_";
                    if (LOG.isDebugEnabled()) {
                        LOG.debug((Object)"Reading user with forced update membership");
                    }
                }
                return this.internalReadUser(dbc, userFqn, password, false);
            }
            catch (CmsPasswordEncryptionException e) {
                LOG.error((Object)e);
                throw new CmsDataAccessException(e.getMessageContainer(), (Throwable)e);
            }
        }
        try {
            return this.userDriver.readUser(dbc, userFqn);
        }
        catch (CmsDataAccessException e) {
            try {
                String password = null;
                if (LOG.isDebugEnabled()) {
                    LOG.debug((Object)("Request context info: " + (dbc.getRequestContext() == null ? "null" : new StringBuilder().append("context available, force_membership attribute: ").append(String.valueOf(dbc.getRequestContext().getAttribute("__FORCE_UPDATE_MEMBERSHIP"))).toString())));
                }
                if (dbc.getRequestContext() != null && dbc.getRequestContext().getAttribute("__FORCE_UPDATE_MEMBERSHIP") != null && ((Boolean)dbc.getRequestContext().getAttribute("__FORCE_UPDATE_MEMBERSHIP")).booleanValue()) {
                    password = "_UPDATE_MEMBERSHIP_";
                    if (LOG.isDebugEnabled()) {
                        LOG.debug((Object)"Reading user with forced update membership");
                    }
                }
                return this.internalReadUser(dbc, userFqn, password, false);
            }
            catch (CmsPasswordEncryptionException e1) {
                LOG.error((Object)e1);
                throw new CmsDataAccessException(e1.getMessageContainer(), (Throwable)e1);
            }
        }
    }

    public CmsUser readUser(CmsDbContext dbc, String userFqn, String password) throws CmsDataAccessException, CmsPasswordEncryptionException {
        return this.internalReadUser(dbc, userFqn, password, true);
    }

    public CmsUser readUser(CmsDbContext dbc, String userFqn, String password, String remoteAddr) throws CmsDataAccessException, CmsPasswordEncryptionException {
        return this.readUser(dbc, userFqn, password);
    }

    public Map readUserInfos(CmsDbContext dbc, CmsUUID userId) throws CmsDataAccessException {
        return this.userDriver.readUserInfos(dbc, userId);
    }

    public List readUsersOfGroup(CmsDbContext dbc, String groupFqn, boolean includeOtherOuUsers) throws CmsDataAccessException {
        CmsLdapManager manager = CmsLdapManager.getInstance();
        if (manager != null && manager.hasConnection(dbc) && manager.getLdapGroup().getName().equals(groupFqn)) {
            ArrayList ret = new ArrayList(this.userDriver.getUsers(dbc, this.userDriver.readOrganizationalUnit(dbc, ""), true));
            return CmsPrincipal.filterFlag(ret, (int)131072);
        }
        return this.userDriver.readUsersOfGroup(dbc, groupFqn, includeOtherOuUsers);
    }

    public void removeAccessControlEntries(CmsDbContext dbc, CmsProject project, CmsUUID resource) throws CmsDataAccessException {
        this.userDriver.removeAccessControlEntries(dbc, project, resource);
    }

    public void removeAccessControlEntriesForPrincipal(CmsDbContext dbc, CmsProject project, CmsProject onlineProject, CmsUUID principal) throws CmsDataAccessException {
        this.userDriver.removeAccessControlEntriesForPrincipal(dbc, project, onlineProject, principal);
    }

    public void removeAccessControlEntry(CmsDbContext dbc, CmsProject project, CmsUUID resource, CmsUUID principal) throws CmsDataAccessException {
        this.userDriver.removeAccessControlEntry(dbc, project, resource, principal);
    }

    public void removeResourceFromOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, CmsResource resource) throws CmsDataAccessException {
        this.userDriver.removeResourceFromOrganizationalUnit(dbc, orgUnit, resource);
    }

    public void setUsersOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, CmsUser user) throws CmsDataAccessException {
        this.userDriver.setUsersOrganizationalUnit(dbc, orgUnit, user);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[" + this.getClass().getName() + "]:\n");
        buffer.append(this.userDriver.toString());
        return buffer.toString();
    }

    public void writeAccessControlEntry(CmsDbContext dbc, CmsProject project, CmsAccessControlEntry acEntry) throws CmsDataAccessException {
        this.userDriver.writeAccessControlEntry(dbc, project, acEntry);
    }

    public void writeGroup(CmsDbContext dbc, CmsGroup group) throws CmsDataAccessException {
        CmsLdapManager manager = CmsLdapManager.getInstance();
        if (manager != null && manager.hasConnection(dbc) && CmsOceeManager.LDAP_GROUP_ID.equals((Object)group.getId())) {
            manager.getConfiguration().setLdapGroup(group.getName(), Boolean.toString(group.isEnabled()));
            OpenCms.writeConfiguration(manager.getConfiguration().getClass());
        }
        this.userDriver.writeGroup(dbc, group);
    }

    public void writeOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit organizationalUnit) throws CmsDataAccessException {
        this.userDriver.writeOrganizationalUnit(dbc, organizationalUnit);
    }

    public void writePassword(CmsDbContext dbc, String userFqn, String oldPassword, String newPassword) throws CmsDataAccessException, CmsPasswordEncryptionException {
        CmsLdapManager manager = CmsLdapManager.getInstance();
        if (manager != null && manager.hasConnection(dbc)) {
            CmsUser user;
            if (LOG.isDebugEnabled()) {
                LOG.debug((Object)("writePassword for user " + userFqn));
            }
            if (CmsLdapManager.hasLdapFlag((user = this.readUser(dbc, userFqn)).getFlags())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug((Object)("ldap user " + userFqn));
                }
                if (manager.getConfiguration().isPwdEditable()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug((Object)"write pwd back to ldap server");
                    }
                    HashMap<String, String> attrs = new HashMap<String, String>();
                    attrs.put(manager.getConfiguration().getPwdMapping(), ("plain".equals(this.pwdDigestType.toLowerCase()) ? "" : new StringBuilder().append("{").append(this.pwdDigestType).append("}").toString()) + this.passwordHandler.digest(newPassword, this.pwdDigestType, this.pwdDigestEncoding));
                    String dn = (String)user.getAdditionalInfo("dn");
                    manager.updateObject(dbc, dn, attrs);
                } else {
                    throw new CmsLdapAccessException(Messages.get().container("ERR_PWD_CHANGE_NOT_ALLOWED_0"));
                }
            }
        }
        this.userDriver.writePassword(dbc, userFqn, oldPassword, newPassword);
    }

    public void writeUser(CmsDbContext dbc, CmsUser user) throws CmsDataAccessException {
        CmsLdapManager manager = CmsLdapManager.getInstance();
        String att_login = "";
        try {
            att_login = (String)dbc.getAttribute("A_LOGIN");
        }
        catch (NoSuchMethodError nsme) {
            LOG.debug((Object)nsme.getLocalizedMessage(), (Throwable)nsme);
        }
        LOG.debug((Object)("writeUser: att_login=" + att_login));
        if (CmsStringUtil.isEmpty((String)att_login) && manager != null && manager.hasConnection(dbc)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug((Object)("writing user " + user.getName()));
            }
            if (CmsLdapManager.hasLdapFlag(user.getFlags())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug((Object)("ldap user " + user.getName()));
                }
                CmsLdapUserDefinition userDef = manager.lookupUserDefinition(dbc, user);
                Map additionalMappings = userDef.getAdditionalMappings();
                Map additionalInfo = user.getAdditionalInfo();
                Iterator itAddInfo = additionalInfo.entrySet().iterator();
                HashMap attrs = new HashMap();
                String dn = (String)user.getAdditionalInfo("dn");
                while (itAddInfo.hasNext()) {
                    Map.Entry addInfoEntry = (Map.Entry)itAddInfo.next();
                    String key = (String)addInfoEntry.getKey();
                    Object value = addInfoEntry.getValue();
                    if (key == null || value == null || !(value instanceof String) || !userDef.isWriteBackMapping(key)) continue;
                    String mappingTargetAttr = (String)additionalMappings.get(key);
                    LOG.debug((Object)("preparing to write " + mappingTargetAttr + "='" + value + "' at " + dn));
                    attrs.put(mappingTargetAttr, value);
                }
                if (!attrs.isEmpty()) {
                    LOG.debug((Object)"Updating LDAP user");
                    manager.updateObject(dbc, dn, attrs);
                    LOG.debug((Object)"Updated LDAP user");
                }
            }
        }
        this.userDriver.writeUser(dbc, user);
    }

    public void writeUserInfo(CmsDbContext dbc, CmsUUID userId, String key, Object value) throws CmsDataAccessException {
        this.userDriver.writeUserInfo(dbc, userId, key, value);
    }

    protected CmsUser addCmsLdapUser(CmsDbContext dbc, CmsUser newUser) throws CmsDataAccessException {
        String pwd;
        if (LOG.isDebugEnabled()) {
            LOG.debug((Object)("add new user " + newUser.getName()));
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly((String)(pwd = newUser.getPassword()))) {
            pwd = "X2p8uy8kD";
        }
        CmsUser user = this.userDriver.createUser(dbc, CmsUUID.getConstantUUID((String)("user-" + newUser.getName())), newUser.getName(), pwd, newUser.getFirstname(), newUser.getLastname(), newUser.getEmail(), newUser.getLastlogin(), newUser.getFlags(), newUser.getDateCreated(), newUser.getAdditionalInfo());
        return user;
    }

    protected CmsUser internalReadUser(CmsDbContext dbc, String userFqn, String password, boolean withPwd) throws CmsDataAccessException, CmsPasswordEncryptionException {
        CmsUser user = null;
        CmsLdapManager manager = CmsLdapManager.getInstance();
        if (manager != null) {
            boolean updateMembership;
            if (!CmsLdapManager.getInstance().getConfiguration().isLookupDefaultUsers() && OpenCms.getDefaultUsers().isDefaultUser(userFqn)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug((Object)("Skip synchronization of default user " + userFqn));
                }
                if (withPwd) {
                    return this.userDriver.readUser(dbc, userFqn, password, null);
                }
                return this.userDriver.readUser(dbc, userFqn);
            }
            if (!manager.hasConnection(dbc)) {
                if (withPwd) {
                    return this.userDriver.readUser(dbc, userFqn, password, null);
                }
                return this.userDriver.readUser(dbc, userFqn);
            }
            CmsUser dbu = null;
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug((Object)("reading user " + userFqn));
                }
                dbu = this.userDriver.readUser(dbc, userFqn);
            }
            catch (CmsDbEntryNotFoundException e) {
                userFqn = this.updateUserName(dbc, userFqn);
                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug((Object)("reading user " + userFqn));
                    }
                    dbu = this.userDriver.readUser(dbc, userFqn);
                }
                catch (CmsDbEntryNotFoundException e2) {
                    // empty catch block
                }
            }
            if (dbu != null) {
                String dbDn = (String)dbu.getAdditionalInfo("dn");
                user = manager.synchronizeUser(dbc, dbu, withPwd ? password : null);
                if (user != null && (CmsStringUtil.isEmptyOrWhitespaceOnly((String)dbDn) || !dbDn.equals(user.getAdditionalInfo("dn")))) {
                    this.userDriver.writeUser(dbc, user);
                }
                if (withPwd) {
                    if (user == null) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug((Object)("treat user as common cms user: " + userFqn));
                        }
                        user = this.userDriver.readUser(dbc, userFqn, password, null);
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug((Object)("login write pwd for user " + userFqn));
                        }
                        this.userDriver.writePassword(dbc, userFqn, null, password);
                    }
                }
            }
            boolean bl = updateMembership = (withPwd || "_UPDATE_MEMBERSHIP_".equals(password)) && user != null && user.getAdditionalInfo("dn") != null;
            if (user == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug((Object)("wrong pwd or NO user for username " + userFqn + " trying to look up user in ldap"));
                }
                user = this.addCmsLdapUser(dbc, manager.lookupUser(dbc, userFqn, withPwd ? password : null));
                boolean bl2 = updateMembership = user != null;
            }
            if (updateMembership) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug((Object)("Updating group membership for user: " + userFqn));
                }
                this.updateMembership(dbc, user);
            }
        } else {
            if (withPwd) {
                return this.userDriver.readUser(dbc, userFqn, password, null);
            }
            return this.userDriver.readUser(dbc, userFqn);
        }
        return user;
    }

    protected boolean isCallFromDeleteMethod() {
        boolean fromDelete = false;
        try {
            throw new Exception();
        }
        catch (Exception e) {
            for (int i = 0; i < e.getStackTrace().length; ++i) {
                StackTraceElement ste = e.getStackTrace()[i];
                if (!ste.getClassName().equals(CmsDriverManager.class.getName()) || !ste.getMethodName().equals("deleteGroup") && !ste.getMethodName().equals("deleteUser")) continue;
                fromDelete = true;
                break;
            }
            return fromDelete;
        }
    }

    protected boolean isCallFromReplication() {
        boolean fromReplication = false;
        try {
            throw new Exception();
        }
        catch (Exception e) {
            for (int i = 0; i < e.getStackTrace().length; ++i) {
                StackTraceElement ste = e.getStackTrace()[i];
                if (!ste.getClassName().equals("org.opencms.ocee.replication.CmsReplicationAccountsHandler")) continue;
                fromReplication = true;
                break;
            }
            return fromReplication;
        }
    }

    protected List removeDeletedGroups(CmsDbContext dbc, List groups) throws CmsDataAccessException {
        ArrayList ret = new ArrayList(groups);
        CmsLdapManager manager = CmsLdapManager.getInstance();
        if (manager != null && System.currentTimeMillis() - this.lastCheck > manager.getConfiguration().getSyncInterval()) {
            this.lastCheck = System.currentTimeMillis();
            for (CmsGroup group : (List<CmsGroup>)groups) {
                List ldapGroupnames;
                if (!CmsLdapManager.hasLdapFlag(group.getFlags()) || group.getId().equals((Object)CmsOceeManager.LDAP_GROUP_ID) || (ldapGroupnames = manager.lookupGroupNames(dbc, group.getOuFqn())).contains(group.getName())) continue;
                try {
                    this.driverManager.deleteGroup(dbc, group, null);
                    ret.remove((Object)group);
                }
                catch (CmsException e) {
                    if (!LOG.isErrorEnabled()) continue;
                    LOG.error((Object)e.getLocalizedMessage(), (Throwable)e);
                }
            }
        }
        return ret;
    }

    protected List removeDeletedUsers(CmsDbContext dbc, List users) throws CmsDataAccessException {
        ArrayList ret = new ArrayList(users);
        CmsLdapManager manager = CmsLdapManager.getInstance();
        if (manager != null && System.currentTimeMillis() - this.lastCheck > manager.getConfiguration().getSyncInterval()) {
            this.lastCheck = System.currentTimeMillis();
            for (CmsUser user : (List<CmsUser>)users) {
                if (!CmsLdapManager.hasLdapFlag(user.getFlags())) continue;
                try {
                    manager.lookupUser(dbc, user, null);
                }
                catch (CmsDbEntryNotFoundException e) {
                    try {
                        this.driverManager.deleteUser(dbc, dbc.currentProject(), user.getName(), null);
                        ret.remove((Object)user);
                    }
                    catch (CmsException e1) {
                        if (!LOG.isErrorEnabled()) continue;
                        LOG.error((Object)e1.getLocalizedMessage(), (Throwable)e1);
                    }
                }
            }
        }
        return ret;
    }

    protected void updateMembership(CmsDbContext dbc, CmsUser user) throws CmsDataAccessException {
        HashMap<String, Object> eventData;
        if (CmsLdapManager.getInstance().getConfiguration().getGroupConsistency().equals("none") || !CmsLdapManager.hasLdapFlag(user.getFlags())) {
            return;
        }
        if (!CmsLdapManager.getInstance().getConfiguration().isLookupDefaultUsers() && OpenCms.getDefaultUsers().isDefaultUser(user.getName())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug((Object)("Skip membership update of default user " + user.getName()));
            }
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug((Object)("Updating membership of user \"" + user.getName() + "\"."));
        }
        ArrayList<CmsGroup> groups = new ArrayList<CmsGroup>();
        List ldapGroupNames = CmsLdapManager.getInstance().lookupGroupNames(dbc, user);
        for (String groupName : (List<String>)ldapGroupNames) {
            if (CmsLdapManager.getInstance().getLdapGroup().getName().equals(groupName)) continue;
            try {
                groups.add(this.userDriver.readGroup(dbc, groupName));
                if (!LOG.isDebugEnabled()) continue;
                LOG.debug((Object)("check user membership in group " + groupName));
            }
            catch (CmsException exc) {
                if (!CmsLdapManager.getInstance().getConfiguration().getGroupConsistency().equals("all")) continue;
                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug((Object)("update user membership creating group " + groupName));
                    }
                    CmsGroup group = CmsLdapManager.getInstance().lookupGroup(dbc, groupName);
                    group = this.userDriver.createGroup(dbc, CmsUUID.getConstantUUID((String)("group-" + group.getName())), group.getName(), group.getDescription(), group.getFlags(), null);
                    groups.add(group);
                }
                catch (CmsException e) {
                    if (!LOG.isErrorEnabled()) continue;
                    LOG.error((Object)Messages.get().getBundle().key("ERR_LDAP_ADD_GROUP_1", (Object)groupName), (Throwable)e);
                }
            }
        }
        List groupsOfUser = this.userDriver.readGroupsOfUser(dbc, user.getId(), "", true, null, false);
        for (CmsGroup group : (List<CmsGroup>)groupsOfUser) {
            if (!CmsLdapManager.hasLdapFlag(group.getFlags()) || ldapGroupNames.contains(group.getName())) continue;
            if (LOG.isDebugEnabled()) {
                LOG.debug((Object)("delete user membership for group " + group.getName()));
            }
            this.userDriver.deleteUserInGroup(dbc, user.getId(), group.getId());
            eventData = new HashMap<String, Object>();
            eventData.put("userId", (Object)user.getId());
            eventData.put("userName", user.getName());
            eventData.put("groupName", group.getName());
            eventData.put("userAction", "removeUserFromGroup");
            OpenCms.fireCmsEvent((int)29, eventData);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug((Object)"Current groups in OpenCms:");
            for (CmsGroup group : (List<CmsGroup>) groupsOfUser) {
                LOG.debug((Object)group.toString());
            }
            LOG.debug((Object)"Current groups in LDAP:");
            for (CmsGroup group : groups) {
                LOG.debug((Object)group.toString());
            }
        }
        for (CmsGroup group : groups) {
            if (groupsOfUser.contains((Object)group)) continue;
            if (LOG.isDebugEnabled()) {
                LOG.debug((Object)("add user membership for group " + group.getName()));
            }
            this.userDriver.createUserInGroup(dbc, user.getId(), group.getId());
            eventData = new HashMap();
            eventData.put("userId", (Object)user.getId());
            eventData.put("userName", user.getName());
            eventData.put("groupName", group.getName());
            eventData.put("userAction", "addUserToGroup");
            OpenCms.fireCmsEvent((int)29, eventData);
        }
    }

    protected String updateUserName(CmsDbContext dbc, String name) {
        String ldapName = null;
        try {
            ldapName = CmsLdapManager.getInstance().lookupUser(dbc, name, null).getName();
        }
        catch (Exception e) {
            // empty catch block
        }
        if (ldapName != null) {
            this.writeName(dbc, ldapName);
            name = ldapName;
        }
        return name;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void writeName(CmsDbContext dbc, String ldapName) {
        PreparedStatement stmt = null;
        Connection conn = null;
        if (LOG.isDebugEnabled()) {
            LOG.debug((Object)("Actualizing username from LDAP: " + ldapName));
        }
        String sql = "UPDATE CMS_USERS SET USER_NAME=? WHERE UPPER(USER_NAME)=UPPER(?) AND USER_OU='/'";
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug((Object)"Setting up connection to OpenCms user database...");
            }
            conn = this.getSqlManager().getConnection(dbc);
            stmt = this.getSqlManager().getPreparedStatementForSql(conn, "UPDATE CMS_USERS SET USER_NAME=? WHERE UPPER(USER_NAME)=UPPER(?) AND USER_OU='/'");
            stmt.setString(1, ldapName);
            stmt.setString(2, ldapName);
            stmt.executeUpdate();
            if (LOG.isDebugEnabled()) {
                LOG.debug((Object)"User data updated");
            }
        }
        catch (SQLException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn((Object)org.opencms.db.generic.Messages.get().getBundle().key("ERR_GENERIC_SQL_1", new Object[]{CmsDbSqlException.getErrorQuery((Statement)stmt)}), (Throwable)e);
            }
        }
        catch (Throwable t) {
            if (LOG.isWarnEnabled()) {
                LOG.warn((Object)"Error during updating user data: ", t);
            }
        }
        finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug((Object)"Closing connection to User database...");
            }
            this.getSqlManager().closeAll(dbc, conn, (Statement)stmt, null);
            if (LOG.isDebugEnabled()) {
                LOG.debug((Object)"Connection closed.");
            }
        }
    }
    
	@Override
	public CmsUserExternalProvider readExternalUserProvider(CmsDbContext dbc, String providerName, String id)
			throws CmsDataAccessException {
		return this.userDriver.readExternalUserProvider(dbc, providerName, id);
	}

	@Override
	public void createExternalProvider(CmsDbContext dbc, CmsUserExternalProvider externalProvider)
			throws CmsDataAccessException {
		this.userDriver.createExternalProvider(dbc, externalProvider);
		
	}

	@Override
	public void updateExternalProvider(CmsDbContext dbc, CmsUserExternalProvider userProv)
			throws CmsDataAccessException {
		this.updateExternalProvider(dbc, userProv);
		
	}

	@Override
	public void writeTwoFactor(CmsDbContext dbc, CmsUserTwoFactor twoFactor) throws CmsDataAccessException {
		this.userDriver.writeTwoFactor(dbc,twoFactor);
	}

	@Override
	public void updateTwoFactor(CmsDbContext dbc, CmsUserTwoFactor twoFactor) throws CmsDataAccessException {
		this.userDriver.updateTwoFactor(dbc,twoFactor);
	}

	@Override
	public CmsUserTwoFactor readTwoFactor(CmsDbContext dbc, CmsUUID id) throws CmsDataAccessException {

		return this.userDriver.readTwoFactor(dbc,id);
	}

	@Override
	public void deleteTwoFactor(CmsDbContext dbc, CmsUUID userId) throws CmsDataAccessException {
		this.userDriver.deleteTwoFactor(dbc,userId);
		
	}
	
	@Override
	public CmsUserTwoFactor readTwoFactorByTemp(CmsDbContext dbc, String tempId) throws CmsDataAccessException {
		
		return this.userDriver.readTwoFactorByTemp(dbc,tempId);
	}
}

