package org.opencms.ocee.ldap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.I_CmsUserDriver;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsGroup;
import org.opencms.main.CmsLog;
import org.opencms.security.CmsRole;

public class CmsLdapGroupRoleMappings {
    private static final Log f98x350cdc9c = CmsLog.getLog(CmsLdapGroupRoleMappings.class);
    private Set roles = new HashSet();
    private boolean initialized;
    private Map rolesXGroup = new HashMap();
    private List configuredMappings = new ArrayList();

    public void add(CmsLdapGroupRoleMapping groupRoleMapping) {
        this.configuredMappings.add(groupRoleMapping);
        List roles = (List) this.rolesXGroup.get(groupRoleMapping.getGroupName());
        if (roles == null) {
            roles = new ArrayList();
        }
        roles.add(groupRoleMapping.getRoleName());
        this.rolesXGroup.put(groupRoleMapping.getGroupName(), roles);
        this.roles.add(groupRoleMapping.getRoleName());
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_GROUP_ROLE_MAPPING_ADDED_2, groupRoleMapping.getGroupName(), groupRoleMapping.getRoleName()));
        }
    }

    public List getConfiguredMappings() {
        return this.configuredMappings;
    }

    public Set getMappedRoles(List userGroups) {
        Set ret = new HashSet();
        for (CmsGroup group : (List<CmsGroup>)userGroups) {
            if (this.rolesXGroup.containsKey(group.getName())) {
                ret.addAll((List) this.rolesXGroup.get(group.getName()));
            }
        }
        return ret;
    }

    public void initialize(I_CmsUserDriver dbUserDriver, CmsDbContext dbc) {
        Map roles = new HashMap();
        for (String roleName : (Set<String>)this.roles) {
            CmsRole role = CmsRole.valueOfRoleName(roleName);
            if (role != null) {
                try {
                    roles.put(roleName, dbUserDriver.readGroup(dbc, role.getGroupName()));
                } catch (Throwable t) {
                    if (f98x350cdc9c.isErrorEnabled()) {
                        f98x350cdc9c.error(t.getLocalizedMessage(), t);
                    }
                }
            } else if (f98x350cdc9c.isWarnEnabled()) {
                f98x350cdc9c.warn(Messages.get().getBundle().key(Messages.LOG_INVALID_ROLE_NAME_1, roleName));
            }
        }
        for (Entry entry : (Set<Map.Entry>)this.rolesXGroup.entrySet()) {
            boolean isValid = m2x226a583a(dbc, dbUserDriver, (String) entry.getKey());
            List roleObjects = new ArrayList();
            for (String roleName2 : (List<String>) entry.getValue()) {
                CmsGroup role2 = (CmsGroup) roles.get(roleName2);
                if (isValid) {
                    if (role2 != null) {
                        roleObjects.add(role2);
                    } else if (CmsLog.INIT.isWarnEnabled()) {
                        CmsLog.INIT.warn(Messages.get().getBundle().key(Messages.INIT_LDAP_SKIP_MAPPING_2, entry.getKey(), roleName2));
                    }
                } else if (CmsLog.INIT.isWarnEnabled()) {
                    CmsLog.INIT.warn(Messages.get().getBundle().key(Messages.INIT_LDAP_SKIP_MAPPING_2, entry.getKey(), roleName2));
                }
            }
            entry.setValue(roleObjects);
        }
        this.initialized = true;
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    private boolean m2x226a583a(CmsDbContext dbc, I_CmsUserDriver dbUserDriver, String groupName) {
        boolean isValid = true;
        if (CmsLdapManager.getInstance().getLdapGroup().getName().equals(groupName)) {
            return true;
        }
        try {
            CmsLdapManager.getInstance().lookupGroup(dbc, groupName);
        } catch (CmsDataAccessException e) {
            try {
                dbUserDriver.readGroup(dbc, groupName);
            } catch (CmsDataAccessException e2) {
                isValid = false;
                if (f98x350cdc9c.isDebugEnabled()) {
                    f98x350cdc9c.debug(e2.getLocalizedMessage(), e2);
                }
                if (CmsLog.INIT.isWarnEnabled()) {
                    CmsLog.INIT.warn(Messages.get().getBundle().key(Messages.INIT_LDAP_SKIP_GROUP_MAPPING_1, groupName));
                }
            }
        }
        return isValid;
    }
}
