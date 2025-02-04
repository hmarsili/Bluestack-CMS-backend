package org.opencms.ocee.replication;

import java.util.List;
import java.util.Map;
import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.I_CmsDriver;
import org.opencms.db.I_CmsUserDriver;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsUserExternalProvider;
import org.opencms.file.CmsUserTwoFactor;
import org.opencms.main.CmsInitException;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPasswordEncryptionException;
import org.opencms.util.CmsUUID;

public class CmsReplicationChangedUserDriver implements I_CmsDriver, I_CmsUserDriver {
   protected I_CmsUserDriver userDriver;

   public void addResourceToOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, CmsResource resource) throws CmsDataAccessException {
      this.userDriver.addResourceToOrganizationalUnit(dbc, orgUnit, resource);
   }

   public void createAccessControlEntry(CmsDbContext dbc, CmsProject project, CmsUUID resource, CmsUUID principal, int allowed, int denied, int flags) throws CmsDataAccessException {
      this.userDriver.createAccessControlEntry(dbc, project, resource, principal, allowed, denied, flags);
   }

   public CmsGroup createGroup(CmsDbContext dbc, CmsUUID groupId, String groupFqn, String description, int flags, String parentGroupName) throws CmsDataAccessException {
      return this.userDriver.createGroup(dbc, groupId, groupFqn, description, flags, parentGroupName);
   }

   public CmsOrganizationalUnit createOrganizationalUnit(CmsDbContext dbc, String name, String description, int flags, CmsOrganizationalUnit parent, String associationRootPath) throws CmsDataAccessException {
      return this.userDriver.createOrganizationalUnit(dbc, name, description, flags, parent, associationRootPath);
   }

   public void createRootOrganizationalUnit(CmsDbContext dbc) {
      this.userDriver.createRootOrganizationalUnit(dbc);
   }

   public CmsUser createUser(CmsDbContext dbc, CmsUUID id, String userFqn, String password, String firstname, String lastname, String email, long lastlogin, int flags, long dateCreated, Map additionalInfos) throws CmsDataAccessException {
      additionalInfos.put("USER_LASTMODIFIED", String.valueOf(System.currentTimeMillis()));
      return this.userDriver.createUser(dbc, id, userFqn, password, firstname, lastname, email, lastlogin, flags, dateCreated, additionalInfos);
   }

   public void createUserInGroup(CmsDbContext dbc, CmsUUID userid, CmsUUID groupid) throws CmsDataAccessException {
      this.userDriver.createUserInGroup(dbc, userid, groupid);
   }

   /** @deprecated */
   public void deleteAccessControlEntries(CmsDbContext dbc, CmsProject project, CmsUUID resource) throws CmsDataAccessException {
      this.userDriver.deleteAccessControlEntries(dbc, project, resource);
   }

   public void deleteGroup(CmsDbContext dbc, String groupFqn) throws CmsDataAccessException {
      this.userDriver.deleteGroup(dbc, groupFqn);
   }

   public void deleteOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit organizationalUnit) throws CmsDataAccessException {
      this.userDriver.deleteOrganizationalUnit(dbc, organizationalUnit);
   }

   public void deleteUser(CmsDbContext dbc, String userFqn) throws CmsDataAccessException {
      this.userDriver.deleteUser(dbc, userFqn);
   }

   public void deleteUserInfos(CmsDbContext dbc, CmsUUID userId) throws CmsDataAccessException {
      this.userDriver.deleteUserInfos(dbc, userId);
   }

   public void deleteUserInGroup(CmsDbContext dbc, CmsUUID userId, CmsUUID groupId) throws CmsDataAccessException {
      this.userDriver.deleteUserInGroup(dbc, userId, groupId);
   }

   public void destroy() throws Throwable {
      this.userDriver.destroy();
   }

   public boolean existsGroup(CmsDbContext dbc, String groupFqn) throws CmsDataAccessException {
      return this.userDriver.existsGroup(dbc, groupFqn);
   }

   public boolean existsUser(CmsDbContext dbc, String userFqn) throws CmsDataAccessException {
      return this.userDriver.existsUser(dbc, userFqn);
   }

   public void fillDefaults(CmsDbContext dbc) throws CmsInitException {
      this.userDriver.fillDefaults(dbc);
   }

   public List getGroups(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, boolean includeSubOus, boolean readRoles) throws CmsDataAccessException {
      return this.userDriver.getGroups(dbc, orgUnit, includeSubOus, readRoles);
   }

   public List getOrganizationalUnits(CmsDbContext dbc, CmsOrganizationalUnit parent, boolean includeChildren) throws CmsDataAccessException {
      return this.userDriver.getOrganizationalUnits(dbc, parent, includeChildren);
   }

   public List getResourcesForOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit orgUnit) throws CmsDataAccessException {
      return this.userDriver.getResourcesForOrganizationalUnit(dbc, orgUnit);
   }

   public CmsSqlManager getSqlManager() {
      return this.userDriver.getSqlManager();
   }

   public List getUsers(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, boolean recursive) throws CmsDataAccessException {
      return this.userDriver.getUsers(dbc, orgUnit, recursive);
   }

   public void init(CmsDbContext dbc, CmsConfigurationManager configurationManager, List successiveDrivers, CmsDriverManager driverManager) throws CmsInitException {
      Map configuration = configurationManager.getConfiguration();
      String driverName = (String)configuration.get((String)successiveDrivers.get(0) + ".user.driver");
      successiveDrivers = successiveDrivers.size() > 1 ? successiveDrivers.subList(1, successiveDrivers.size()) : null;
      this.userDriver = (I_CmsUserDriver)driverManager.newDriverInstance(configurationManager, driverName, successiveDrivers);
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

   public List readChildGroups(CmsDbContext dbc, String groupFqn) throws CmsDataAccessException {
      return this.userDriver.readChildGroups(dbc, groupFqn);
   }

   public CmsGroup readGroup(CmsDbContext dbc, CmsUUID groupId) throws CmsDataAccessException {
      return this.userDriver.readGroup(dbc, groupId);
   }

   public CmsGroup readGroup(CmsDbContext dbc, String groupFqn) throws CmsDataAccessException {
      return this.userDriver.readGroup(dbc, groupFqn);
   }

   public List readGroupsOfUser(CmsDbContext dbc, CmsUUID userId, String ouFqn, boolean includeChildOus, String remoteAddress, boolean readRoles) throws CmsDataAccessException {
      return this.userDriver.readGroupsOfUser(dbc, userId, ouFqn, includeChildOus, remoteAddress, readRoles);
   }

   public CmsOrganizationalUnit readOrganizationalUnit(CmsDbContext dbc, String ouFqn) throws CmsDataAccessException {
      return this.userDriver.readOrganizationalUnit(dbc, ouFqn);
   }

   public CmsUser readUser(CmsDbContext dbc, CmsUUID id) throws CmsDataAccessException {
      return this.userDriver.readUser(dbc, id);
   }

   public CmsUser readUser(CmsDbContext dbc, String userFqn) throws CmsDataAccessException {
      return this.userDriver.readUser(dbc, userFqn);
   }

   public CmsUser readUser(CmsDbContext dbc, String userFqn, String password, String remoteAddress) throws CmsDataAccessException, CmsPasswordEncryptionException {
      return this.userDriver.readUser(dbc, userFqn, password, remoteAddress);
   }

   public Map readUserInfos(CmsDbContext dbc, CmsUUID userId) throws CmsDataAccessException {
      return this.userDriver.readUserInfos(dbc, userId);
   }

   public List readUsersOfGroup(CmsDbContext dbc, String groupFqn, boolean includeOtherOuUsers) throws CmsDataAccessException {
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
      return this.userDriver.toString();
   }

   public void writeAccessControlEntry(CmsDbContext dbc, CmsProject project, CmsAccessControlEntry acEntry) throws CmsDataAccessException {
      this.userDriver.writeAccessControlEntry(dbc, project, acEntry);
   }

   public void writeGroup(CmsDbContext dbc, CmsGroup group) throws CmsDataAccessException {
      this.userDriver.writeGroup(dbc, group);
   }

   public void writeOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit organizationalUnit) throws CmsDataAccessException {
      this.userDriver.writeOrganizationalUnit(dbc, organizationalUnit);
   }

   public void writePassword(CmsDbContext dbc, String userFqn, String oldPassword, String newPassword) throws CmsDataAccessException, CmsPasswordEncryptionException {
      this.userDriver.writePassword(dbc, userFqn, oldPassword, newPassword);
   }

   public void writeUser(CmsDbContext dbc, CmsUser user) throws CmsDataAccessException {
      user.setAdditionalInfo("USER_LASTMODIFIED", String.valueOf(System.currentTimeMillis()));
      this.userDriver.writeUser(dbc, user);
   }

   public void writeUserInfo(CmsDbContext dbc, CmsUUID userId, String key, Object value) throws CmsDataAccessException {
      this.userDriver.writeUserInfo(dbc, userId, key, value);
   }

@Override
public void createExternalProvider(CmsDbContext dbc, CmsUserExternalProvider externalProvider)
		throws CmsDataAccessException {
	userDriver.createExternalProvider(dbc, externalProvider);
	
}

@Override
public CmsUserExternalProvider readExternalUserProvider(CmsDbContext dbc, String providerName, String id)
		throws CmsDataAccessException {
	
	return userDriver.readExternalUserProvider(dbc, providerName, id);
}

@Override
public void updateExternalProvider(CmsDbContext dbc, CmsUserExternalProvider userProv) throws CmsDataAccessException {
	userDriver.updateExternalProvider(dbc, userProv);
	
}

@Override
public void writeTwoFactor(CmsDbContext dbc, CmsUserTwoFactor twoFactor) throws CmsDataAccessException {
	userDriver.writeTwoFactor(dbc, twoFactor);
	
}

@Override
public void updateTwoFactor(CmsDbContext dbc, CmsUserTwoFactor twoFactor) throws CmsDataAccessException {
	userDriver.updateTwoFactor(dbc, twoFactor);
	
}

@Override
public CmsUserTwoFactor readTwoFactor(CmsDbContext dbc, CmsUUID id) throws CmsDataAccessException {
	return userDriver.readTwoFactor(dbc, id);
}

@Override
public CmsUserTwoFactor readTwoFactorByTemp(CmsDbContext dbc, String tempId) throws CmsDataAccessException {
	return userDriver.readTwoFactorByTemp(dbc, tempId);
}

@Override
public void deleteTwoFactor(CmsDbContext dbc, CmsUUID userId) throws CmsDataAccessException {
	userDriver.deleteTwoFactor(dbc, userId);
	
}
}
