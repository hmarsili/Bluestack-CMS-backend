package org.opencms.ocee.ldap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import javax.naming.ldap.Rdn;
import org.apache.commons.logging.Log;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsCoreProvider;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.base.CmsReloadingClassLoader;
import org.opencms.ocee.ldap.CmsLdapAccessException;
import org.opencms.ocee.ldap.CmsLdapConfiguration;
import org.opencms.ocee.ldap.CmsLdapGroupDefinition;
import org.opencms.ocee.ldap.CmsLdapGroupMemberUrl;
import org.opencms.ocee.ldap.CmsLdapProvider;
import org.opencms.ocee.ldap.CmsLdapUserDefinition;
import org.opencms.ocee.ldap.CmsLdapUserDriver;
import org.opencms.ocee.ldap.CmsTimedCache;
import org.opencms.ocee.ldap.Messages;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class CmsLdapManager {
    public static final String C_LDAP_DN = "dn";
    public static final String C_LDAP_DN_PREFIX = "ldap";
    public static final String C_LDAP_KEY = "ldap";
    public static final int LDAP_FLAG = 131072;
    public static final String REQUEST_ATTR_SKIP_SYNC = "SKIP_SYNC";
    private static final Log LOG = CmsLog.getLog(CmsLdapManager.class);
    private static String getDirContext;
    private CmsTimedCache name;
    private CmsLdapProvider provider;
    private boolean dn;
    private long groupDef;
    private CmsLdapConfiguration ldapConfig;
    private CmsGroup group;

    public static List<CmsLdapGroupDefinition> filterByOrgUnit(Collection<CmsLdapGroupDefinition> groupDefs, String ou) {
        ArrayList<CmsLdapGroupDefinition> result = new ArrayList<CmsLdapGroupDefinition>();
        for (CmsLdapGroupDefinition groupDef : groupDefs) {
            if (!groupDef.getOuName().equals(ou)) continue;
            result.add(groupDef);
        }
        return result;
    }

    public static CmsLdapManager getInstance() {
        try {
            if (getDirContext == null && (CmsLdapManager.getDirContext = CmsCoreProvider.getInstance().getSqlManagerClassnameForDriver("ldap")) == null) {
                getDirContext = CmsLdapManager.class.getName();
            }
            return (CmsLdapManager)CmsOceeManager.getInstance().getClassLoader().loadObject(getDirContext);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static boolean hasLdapFlag(int flags) {
        return (flags & 131072) == 131072;
    }

    protected static String getStringValue(List<String> list) {
        if ((list == null || list.isEmpty() || list.size() > 1) && LOG.isWarnEnabled()) {
            LOG.warn((Object)Messages.get().getBundle().key("LOG_WARN_OBJECT_VALUE_LIST_1", list));
        }
        return list == null || list.isEmpty() ? "" : list.get(0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addGroup(CmsObject cms, String groupName) {
        CmsGroup group = null;
        CmsDbContext dbc = CmsCoreProvider.getInstance().getNewDbContext(cms.getRequestContext());
        try {
            try {
                group = this.lookupGroup(dbc, groupName);
                CmsGroup oldGroup = cms.readGroup(group.getName());
                oldGroup.setDescription(group.getDescription());
                if (!CmsLdapManager.hasLdapFlag(oldGroup.getFlags())) {
                    oldGroup.setFlags(oldGroup.getFlags() ^ 131072);
                }
                cms.writeGroup(oldGroup);
                group = oldGroup;
            }
            catch (CmsDbEntryNotFoundException e) {
                if (group != null) {
                    try {
                        group = cms.createGroup(group.getName(), group.getDescription(), group.getFlags(), null);
                    }
                    catch (CmsException e1) {
                        CmsMessageContainer msg = Messages.get().container("ERR_LDAP_ADD_GROUP_1", (Object)groupName);
                        if (LOG.isErrorEnabled()) {
                            LOG.error((Object)msg.key(), (Throwable)e1);
                        }
                        throw new CmsRuntimeException(msg, (Throwable)e1);
                    }
                }
                CmsMessageContainer msg = Messages.get().container("ERR_LDAP_ADD_GROUP_1", (Object)groupName);
                if (LOG.isErrorEnabled()) {
                    LOG.error((Object)msg.key(), (Throwable)e);
                }
                throw new CmsRuntimeException(msg, (Throwable)e);
            }
            catch (CmsException e) {
                CmsMessageContainer msg = Messages.get().container("ERR_LDAP_ADD_GROUP_1", (Object)groupName);
                if (LOG.isErrorEnabled()) {
                    LOG.error((Object)msg.key(), (Throwable)e);
                }
                throw new CmsRuntimeException(msg, (Throwable)e);
            }
            try {
                this.updateUsersForGroup(dbc, cms, group);
            }
            catch (CmsException e1) {
                CmsMessageContainer msg = Messages.get().container("ERR_LDAP_ADD_GROUP_1", (Object)groupName);
                if (LOG.isErrorEnabled()) {
                    LOG.error((Object)msg.key(), (Throwable)e1);
                }
                throw new CmsRuntimeException(msg, (Throwable)e1);
            }
        }
        finally {
            dbc.clear();
        }
    }

    public void addUser(CmsObject cms, String userName)
    {
      CmsUser user = null;
      CmsDbContext dbc = CmsCoreProvider.getInstance().getNewDbContext(cms.getRequestContext());
      try {
        user = lookupUser(dbc, userName, null);
        if (LOG.isDebugEnabled()) {
          LOG.debug("addUser: update user " + userName);
        }
        
        CmsUser oldUser = cms.readUser(user.getName());
        oldUser.setName(user.getName());
        oldUser.setDescription(user.getDescription());
        Iterator itInfos = user.getAdditionalInfo().keySet().iterator();
        while (itInfos.hasNext()) {
          String key = (String)itInfos.next();
          oldUser.setAdditionalInfo(key, user.getAdditionalInfo(key));
        }
        oldUser.setFirstname(user.getFirstname());
        oldUser.setLastname(user.getLastname());
        oldUser.setEmail(user.getEmail());
        oldUser.setAddress(user.getAddress());
        if (!hasLdapFlag(oldUser.getFlags())) {
          oldUser.setFlags(oldUser.getFlags() ^ 0x20000);
        }
        cms.writeUser(oldUser);
        user = oldUser;
      }
      catch (CmsDbEntryNotFoundException e) {
        if (user != null) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("addUser: create new user " + userName);
          }
          try
          {
            CmsUser newUser = cms.createUser(user.getName(), "password", user.getDescription(), user.getAdditionalInfo());
            
            newUser.setFirstname(user.getFirstname());
            newUser.setLastname(user.getLastname());
            newUser.setEmail(user.getEmail());
            newUser.setAddress(user.getAddress());
            newUser.setFlags(user.getFlags());
            cms.writeUser(newUser);
            user = newUser;
          }
          catch (CmsException e1) {
            CmsMessageContainer msg = Messages.get().container("ERR_LDAP_ADD_USER_1", userName);
            if (LOG.isErrorEnabled()) {
              LOG.error(msg.key(), e1);
            }
            throw new CmsRuntimeException(msg, e1);
          }
        }
        else {
          CmsMessageContainer msg = Messages.get().container("ERR_LDAP_ADD_USER_1", userName);
          if (LOG.isErrorEnabled()) {
            LOG.error(msg.key(), e);
          }
          throw new CmsRuntimeException(msg, e);
        }
      }
      catch (CmsException e) {
        CmsMessageContainer msg = Messages.get().container("ERR_LDAP_ADD_USER_1", userName);
        if (LOG.isErrorEnabled()) {
          LOG.error(msg.key(), e);
        }
        throw new CmsRuntimeException(msg, e);
      } finally {
        dbc.clear();
      }
    }
    
    public void checkOpenCmsConfiguration(Map configuration) throws CmsIllegalStateException {
        try {
            this.dn = configuration.get("ldap.user.driver").equals(CmsLdapUserDriver.class.getName()) && configuration.get("driver.user").toString().indexOf("ldap,") >= 0;
        }
        catch (Throwable e) {
            throw new CmsIllegalStateException(Messages.get().container("ERR_MANAGER_NOT_INITIALIZED_0"));
        }
    }

    public CmsTimedCache getCache() {
        if (this.name == null || this.name.getTimeout() != this.getConfiguration().getCacheLive()) {
            this.name = new CmsTimedCache(this.getConfiguration().getCacheLive());
        }
        return this.name;
    }

    public CmsLdapConfiguration getConfiguration() {
        return this.ldapConfig;
    }

    public String getDNforGroup(CmsDbContext dbc, String name) throws CmsDataAccessException {
        String ouName = CmsOrganizationalUnit.getParentFqn(name);
        if (!ouName.endsWith("/")) {
            ouName = ouName + "/";
        }
        if (ouName.startsWith("/")) {
            ouName = ouName.substring("/".length());
        }
        DirContext ctx = null;
        try {
            ctx = getDirContext(dbc);
            for (CmsLdapGroupDefinition groupConf : (List<CmsLdapGroupDefinition>)getConfiguration().getGroupDefinitions()) {
                if (groupConf.getOuName().equals(ouName)) {
                    try {
                        Map res = getDirContext(ctx, (String[]) groupConf.getAccessContexts().toArray(new String[0]), groupConf.getFilterByName(name), groupConf.getSearchAttributes());
                        if (res != null && !res.isEmpty()) {
                            String value = groupConf.getValue(res, "dn", null, null);
                            if (ctx != null) {
                                try {
                                    ctx.close();
                                } catch (NamingException e) {
                                }
                            }
                            return value;
                        } else if (LOG.isDebugEnabled()) {
                            LOG.debug(Messages.get().getBundle().key("ERR_UNKNOWN_GROUP_1", new Object[]{name}));
                        }
                    } catch (NamingException exc) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(Messages.get().getBundle().key(Messages.LOG_WARN_LOOKING_UP_GROUP_1, new Object[]{name}), exc);
                        }
                    }
                }
            }
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e2) {
                }
            }
        } catch (NamingException exc2) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_WARN_LOOKING_UP_GROUP_1, new Object[]{name}), exc2);
            }
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e3) {
                }
            }
        } catch (Throwable th) {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e4) {
                }
            }
        }
        throw new CmsDbEntryNotFoundException(Messages.get().container("ERR_UNKNOWN_GROUP_1", name));
    }

    public CmsGroup getLdapGroup() {
        if (this.group == null) {
            CmsUUID id = CmsOceeManager.LDAP_GROUP_ID;
            String name = this.ldapConfig.getLdapGroupName();
            String desc = "";
            int flags = 131072;
            if (!this.ldapConfig.isLdapGroupEnabled()) {
                flags ^= 1;
            }
            this.group = new CmsGroup(id, CmsUUID.getNullUUID(), name, desc, flags);
        }
        return this.group;
    }

    public String getRelativeDN(String fullDN) {
        int p;
        String result = fullDN;
        if (fullDN.startsWith("ldap")) {
            result = fullDN.substring(fullDN.lastIndexOf("/") + 1);
        }
        if ((p = result.indexOf("?")) >= 0) {
            result = result.substring(0, p);
        }
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean hasConnection(CmsDbContext dbc) {
        Context ctx = null;
        try {
            long currentTime = System.currentTimeMillis();
            if (currentTime - this.groupDef > 30000) {
                ctx = this.getDirContext(dbc);
                this.groupDef = currentTime;
            }
            boolean bl = true;
            return bl;
        }
        catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.error((Object)e.getLocalizedMessage(), (Throwable)e);
            } else if (LOG.isErrorEnabled()) {
                LOG.error((Object)e.getLocalizedMessage());
            }
        }
        finally {
            if (ctx != null) {
                try {
                    ctx.close();
                }
                catch (NamingException exc) {}
            }
        }
        return false;
    }

    public void initialize(CmsLdapConfiguration ldapConfig) {
        this.ldapConfig = ldapConfig;
        this.provider = null;
        for (CmsLdapProvider provider : (Collection<CmsLdapProvider>)this.ldapConfig.getLdapProviders().values()) {
            if (!provider.isDefault()) continue;
            this.provider = provider;
            break;
        }
        String userDriverConf = null;
        try {
            userDriverConf = CmsCoreProvider.getInstance().getSequenceForDriver("user");
        }
        catch (Throwable e) {
            // empty catch block
        }
        if ((userDriverConf == null || userDriverConf.indexOf("ldap") < 0) && LOG.isErrorEnabled()) {
            LOG.error((Object)Messages.get().getBundle().key("ERR_LDAP_USER_DRIVER_MISSING_0"));
        }
        if (this.provider == null && LOG.isErrorEnabled()) {
            LOG.error((Object)Messages.get().getBundle().key("ERR_LDAP_NO_DEFAULT_PROVIDER_0"));
        }
        boolean bl = this.dn = userDriverConf != null && userDriverConf.indexOf("ldap") > 0 && this.provider != null;
        if (this.dn) {
            CmsDbContext dbc = CmsCoreProvider.getInstance().getNewDbContext(null);
            this.hasConnection(dbc);
            dbc.clear();
        }
    }

    public boolean isGroupEditable(CmsDbContext dbc, CmsGroup group) {
        String ouName = group.getOuFqn();
        for (CmsLdapGroupDefinition groupConf : (List<CmsLdapGroupDefinition>)this.getConfiguration().getGroupDefinitions()) {
            if (!groupConf.getOuName().equals(ouName)) continue;
            String[] searchRoots = (String[])groupConf.getAccessContexts().toArray(new String[0]);
            String groupFilter = groupConf.getFilterAll();
            String filter = "(&" + groupFilter + "(" + (String)groupConf.getAttributeMappings().get("groupid") + "=" + group.getSimpleName() + "))";
            try {
                if (this.lookupObjects(dbc, searchRoots, 2, filter, groupConf.getSearchAttributes()).isEmpty() || groupConf.isEditable()) continue;
                return false;
            }
            catch (CmsLdapAccessException e) {
            }
        }
        return true;
    }

    public boolean isInitialized() {
        return this.dn;
    }

    public boolean isLdapOnly() {
        return !this.ldapConfig.isEnabledCmsUsers();
    }

    public boolean isUserEditable(CmsDbContext dbc, CmsUser user) {
        String ouName = user.getOuFqn();
        if (!ouName.endsWith("/")) {
            ouName = ouName + "/";
        }
        if (ouName.startsWith("/")) {
            ouName = ouName.substring("/".length());
        }
        for (CmsLdapUserDefinition userConf : (List<CmsLdapUserDefinition>)this.getConfiguration().getUserDefinitions()) {
            if (!userConf.getOuName().equals(ouName)) continue;
            String[] searchRoots = (String[])userConf.getAccessContexts().toArray(new String[0]);
            String userFilter = userConf.getFilterAll();
            String filter = "(&" + userFilter + "(" + userConf.getMappingId() + "=" + user.getSimpleName() + "))";
            try {
                if (this.lookupObjects(dbc, searchRoots, 2, filter, userConf.getSearchAttributes()).isEmpty() || userConf.isEditable()) continue;
                return false;
            }
            catch (CmsLdapAccessException e) {
            }
        }
        return true;
    }

    public CmsGroup lookupGroup(CmsDbContext dbc, String name) throws CmsDataAccessException {
        return this.lookupGroup(dbc, name, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public CmsGroup lookupGroup(CmsDbContext dbc, String name, Map<String, List<String>> ldapResultOut) throws CmsDataAccessException {
        String ouName = CmsOrganizationalUnit.getParentFqn((String)name);
        if (!ouName.endsWith("/")) {
            ouName = ouName + "/";
        }
        if (ouName.startsWith("/")) {
            ouName = ouName.substring("/".length());
        }
        DirContext ctx = null;
        try {
            ctx = this.getDirContext(dbc);
            for (CmsLdapGroupDefinition groupConf : (List<CmsLdapGroupDefinition>)this.getConfiguration().getGroupDefinitions()) {
                if (!groupConf.getOuName().equals(ouName)) continue;
                String[] searchRoots = (String[])groupConf.getAccessContexts().toArray(new String[0]);
                try {
                    Map res = this.getDirContext(ctx, searchRoots, groupConf.getFilterByName(name), groupConf.getSearchAttributes());
                    if (res == null || res.isEmpty()) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn((Object)org.opencms.db.Messages.get().getBundle().key("ERR_UNKNOWN_GROUP_1", new Object[]{name}));
                        }
                        continue;
                    }
                    if (ldapResultOut != null) {
                        ldapResultOut.clear();
                        ldapResultOut.putAll(this.normalizeLdapResult(res));
                    }
                    CmsGroup cmsGroup = groupConf.createGroupFromSearchResult(res);
                    return cmsGroup;
                }
                catch (NamingException exc) {
                    if (!LOG.isWarnEnabled()) continue;
                    LOG.warn((Object)Messages.get().getBundle().key("LOG_WARN_LOOKING_UP_GROUP_1", new Object[]{name}), (Throwable)exc);
                }
            }
        }
        catch (NamingException exc) {
            if (LOG.isWarnEnabled()) {
                LOG.warn((Object)Messages.get().getBundle().key("LOG_WARN_LOOKING_UP_GROUP_1", new Object[]{name}), (Throwable)exc);
            }
        }
        finally {
            if (ctx != null) {
                try {
                    ctx.close();
                }
                catch (NamingException exc) {}
            }
        }
        CmsMessageContainer message = org.opencms.db.Messages.get().container("ERR_UNKNOWN_GROUP_1", (Object)name);
        if (LOG.isErrorEnabled()) {
            LOG.error((Object)message.key());
        }
        throw new CmsDbEntryNotFoundException(message);
    }

    

    public List lookupGroupNames(CmsDbContext dbc, CmsUser user)
      throws CmsDataAccessException
    {
      CmsTimedCache.CmsTimedCacheKey key = CmsTimedCache.CmsTimedCacheKey.groupsOfUser(user.getName());
      List groupNames = getCache().get(key);
      if (groupNames != null) {
        return groupNames;
      }
      groupNames = new ArrayList();
      String ouName = user.getOuFqn();
      if (!ouName.endsWith("/")) {
        ouName = ouName + "/";
      }
      if (ouName.startsWith("/")) {
        ouName = ouName.substring("/".length());
      }
      DirContext ctx = null;
      try {
        ctx = this.getDirContext(dbc);
        List groupDefinitionsForOu = filterByOrgUnit(getConfiguration().getGroupDefinitions(), ouName);
        


        for (Iterator itGroupConfs = getConfiguration().getGroupDefinitions().iterator(); itGroupConfs.hasNext();) {
          CmsLdapGroupDefinition groupConf = (CmsLdapGroupDefinition)itGroupConfs.next();
          if (groupConf.getOuName().equals(ouName))
          {


            String[] searchRoots = (String[])groupConf.getAccessContexts().toArray(new String[0]);
            String[] groupidAttribute = { groupConf.getMappingId() };
            if (groupConf.getMembersFormat().equals("nested-fulldn")) {
              GroupLookup lookup = new GroupLookup(ctx, user, groupDefinitionsForOu);
              Map groupBeans = lookup.getAllGroups();
              for (CmsLdapGroupBean groupBean : (Collection<CmsLdapGroupBean>)groupBeans.values()) {
                String cmsGroupName = groupBean.getCmsName();
                if (!groupNames.contains(cmsGroupName)) {
                  groupNames.add(cmsGroupName);
                }
              }
            } else if (!groupConf.getMembersFormat().equals("mburl")) {
              List res = null;
              try
              {
                res = searchAttributes(ctx, searchRoots, groupConf.getFilterByMember(user), groupidAttribute);
              } catch (NamingException exc) {
                if (LOG.isErrorEnabled()) {
                  LOG.error(org.opencms.db.Messages.get().getBundle().key("ERR_GET_GROUPS_OF_USER_2", new Object[] { user.getName() }), exc);
                }
              }
              



              if (res != null)
              {


                for (Iterator i = res.iterator(); i.hasNext();) {
                  Map data = (Map)i.next();
                  
                  Object obj = data.get(groupidAttribute[0]);
                  String groupName = this.getDirContext(obj, true);
                  if (!groupName.startsWith(ouName)) {
                    groupName = ouName + groupName;
                  }
                  

                  if (!groupNames.contains(groupName)) {
                    groupNames.add(groupName);
                  }
                }
              }
            }
            else {
              Map groups = this.getDirContext(dbc, groupConf);
              Iterator it = groups.keySet().iterator();
              while (it.hasNext())
              {
                String gid = this.getDirContext(it.next(), true);
                String ouGid = gid;
                if (!ouGid.startsWith(ouName)) {
                  ouGid = ouName + ouGid;
                }
                

                if (!groupNames.contains(ouGid))
                {
                  List memberUrls = new ArrayList();
                  if ((groups.get(gid) instanceof List)) {
                    memberUrls = (List)groups.get(gid);
                  } else {
                    memberUrls.add(groups.get(gid));
                  }
                  
                  if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key("LOG_DEBUG_LOOKUP_GROUP_MEMBERURL_SIZE_2", new Integer(memberUrls.size()), gid));
                  }
                  



                  Iterator iter = memberUrls.iterator();
                  CmsLdapGroupMemberUrl memberUrl; Iterator itUserConfs; while (iter.hasNext()) {
                    String urlData = (String)iter.next();
                    if (urlData != null)
                    {

                      memberUrl = new CmsLdapGroupMemberUrl(urlData);
                      
                      if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key("LOG_DEBUG_LOOKUP_GROUP_CHECK_1", memberUrl));
                      }
                      


                      for (itUserConfs = getConfiguration().getUserDefinitions().iterator(); itUserConfs.hasNext();) {
                        CmsLdapUserDefinition userConf = (CmsLdapUserDefinition)itUserConfs.next();
                        if (userConf.getOuName().equals(ouName))
                        {


                          String filter = memberUrl.getUserFilter(userConf.getMappingId(), user.getSimpleName());
                          


                          if (!this.getDirContext(dbc, new String[] { memberUrl.getRoot() }, memberUrl.getScope(), filter).isEmpty())
                          {




                            if (LOG.isDebugEnabled()) {
                              LOG.debug(Messages.get().getBundle().key("LOG_DEBUG_LOOKUP_GROUP_ADD_1", gid));
                            }
                            


                            groupNames.add(ouGid);
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
        





        if (ctx != null) {
          try {
            ctx.close();
          }
          catch (NamingException exc) {}
        }
        

        getCache().put(key, groupNames);
      }
      catch (NamingException exc)
      {
        if (LOG.isErrorEnabled()) {
          LOG.error(org.opencms.db.Messages.get().getBundle().key("ERR_GET_GROUPS_OF_USER_2", new Object[] { user.getName() }), exc);
        }
        
      }
      finally
      {
        if (ctx != null) {
          try {
            ctx.close();
          }
          catch (NamingException exc) {}
        }
      }
      

      return groupNames;
    }
    

    public List lookupGroupNames(CmsDbContext dbc, String ouFqn) throws CmsLdapAccessException {
        String ouName = ouFqn;
        if (!ouName.endsWith("/")) {
            ouName = ouName + "/";
        }
        if (ouName.startsWith("/")) {
            ouName = ouName.substring("/".length());
        }
        CmsTimedCache.CmsTimedCacheKey key = CmsTimedCache.CmsTimedCacheKey.groupsOfOu(ouName);
        List groupNames = this.getCache().get(key);
        if (groupNames != null) {
            return groupNames;
        }
        groupNames = new ArrayList();
        for (CmsLdapGroupDefinition groupConf : (List<CmsLdapGroupDefinition>)this.getConfiguration().getGroupDefinitions()) {
            if (!groupConf.getOuName().equals(ouName)) continue;
            for (Object obj : this.getDirContext(dbc, groupConf).keySet()) {
                if (obj == null) {
                    LOG.debug((Object)"NullPointerException");
                    continue;
                }
                String gid = this.getDirContext(obj, true);
                if (!gid.startsWith(groupConf.getOuName())) {
                    gid = groupConf.getOuName() + gid;
                }
                if (groupNames.contains(gid)) continue;
                groupNames.add((String)gid);
            }
        }
        this.name.put(key, groupNames);
        return groupNames;
    }

    public List lookupGroupNamesForSearch(CmsDbContext dbc, String ouFqn, String filter) throws CmsLdapAccessException {
        String ouName = ouFqn;
        if (!ouName.endsWith("/")) {
            ouName = ouName + "/";
        }
        if (ouName.startsWith("/")) {
            ouName = ouName.substring("/".length());
        }
        ArrayList<String> groupNames = new ArrayList<String>();
        for (CmsLdapGroupDefinition groupConf : (List<CmsLdapGroupDefinition>)this.getConfiguration().getGroupDefinitions()) {
            String searchFilter;
            if (!groupConf.getOuName().equals(ouName) || (searchFilter = groupConf.getFilterSearch(filter)) == null) continue;
            String[] searchRoots = (String[])groupConf.getAccessContexts().toArray(new String[0]);
            String[] groupAttributes = new String[]{groupConf.getMappingId(), groupConf.getMappingMember()};
            List res = this.lookupObjects(dbc, searchRoots, 2, searchFilter, groupAttributes);
            HashMap groupData = new HashMap(res.size());
            for (Map data : (List<Map>)res) {
                groupData.put(data.get(groupAttributes[0]), data.get(groupAttributes[1]));
            }
            for (Object obj : groupData.keySet()) {
                String gid = this.getDirContext(obj, true);
                if (!gid.startsWith(groupConf.getOuName())) {
                    gid = groupConf.getOuName() + gid;
                }
                if (groupNames.contains(gid)) continue;
                groupNames.add(gid);
            }
        }
        return groupNames;
    }

    public /* varargs */ List lookupObjects(CmsDbContext dbc, String[] searchRoots, int scope, String filter, String ... attributes) throws CmsLdapAccessException {
        List res;
        DirContext ctx = null;
        try {
            ctx = this.getDirContext(dbc);
            res = this.getDirContext(ctx, scope, searchRoots, filter, attributes);
        }
        catch (NamingException exc) {
            CmsMessageContainer message = Messages.get().container("ERR_LOOKUP_LDAP_OBJS_0");
            if (LOG.isErrorEnabled()) {
                LOG.error((Object)message.key(), (Throwable)exc);
            }
            throw new CmsLdapAccessException(message, exc);
        }
        finally {
            if (ctx != null) {
                try {
                    ctx.close();
                }
                catch (NamingException exc) {}
            }
        }
        return res;
    }

    public CmsUser lookupUser(CmsDbContext dbc, CmsUser user, String password) throws CmsDataAccessException {
        for (CmsLdapUserDefinition userConf : (List<CmsLdapUserDefinition>)this.getConfiguration().getUserDefinitions()) {
            Map userData;
            if (!userConf.getOuName().equals(user.getOuFqn()) || (userData = this.getDirContext(dbc, user.getName(), password, userConf)) == null || userData.isEmpty()) continue;
            userConf.updateUserFromSearchResult(user, userData);
            return user;
        }
        if (LOG.isWarnEnabled()) {
            LOG.warn((Object)Messages.get().getBundle().key("LOG_WARN_LOOKING_UP_USER_1", new Object[]{user.getName()}));
        }
        throw new CmsDbEntryNotFoundException(org.opencms.db.Messages.get().container("ERR_UNKNOWN_USER_1", (Object)user.getName()));
    }

    public CmsUser lookupUser(CmsDbContext dbc, String name, String password) throws CmsDataAccessException {
        String ouName = CmsOrganizationalUnit.getParentFqn((String)name);
        if (!ouName.endsWith("/")) {
            ouName = ouName + "/";
        }
        if (ouName.startsWith("/")) {
            ouName = ouName.substring("/".length());
        }
        for (CmsLdapUserDefinition userConf : (List<CmsLdapUserDefinition>)this.getConfiguration().getUserDefinitions()) {
            Map userData;
            if (!userConf.getOuName().equals(ouName) || (userData = this.getDirContext(dbc, name, password, userConf)) == null || userData.isEmpty()) continue;
            return userConf.createUserFromSearchResult(userData);
        }
        if (LOG.isWarnEnabled()) {
            LOG.warn((Object)Messages.get().getBundle().key("LOG_WARN_LOOKING_UP_USER_1", new Object[]{name}));
        }
        throw new CmsDbEntryNotFoundException(org.opencms.db.Messages.get().container("ERR_UNKNOWN_USER_1", (Object)name));
    }

    public CmsLdapUserDefinition lookupUserDefinition(CmsDbContext dbc, CmsUser user) throws CmsDataAccessException {
        List<CmsLdapUserDefinition> userDefs = this.getConfiguration().getUserDefinitions();
        for (CmsLdapUserDefinition userDef : userDefs) {
            Map userData;
            if (!userDef.getOuName().equals(user.getOuFqn()) || (userData = this.getDirContext(dbc, user.getName(), null, userDef)) == null || userData.isEmpty()) continue;
            return userDef;
        }
        if (LOG.isWarnEnabled()) {
            LOG.warn((Object)Messages.get().getBundle().key("LOG_WARN_LOOKING_UP_USER_1", new Object[]{user.getName()}));
        }
        throw new CmsDbEntryNotFoundException(org.opencms.db.Messages.get().container("ERR_UNKNOWN_USER_1", (Object)user.getName()));
    }

    public List lookupUserNames(CmsDbContext dbc) throws CmsLdapAccessException {
        List userNames = this.getCache().get(CmsTimedCache.CmsTimedCacheKey.KEY_ALL_USERS);
        if (userNames != null) {
            return userNames;
        }
        userNames = new ArrayList();
        for (CmsLdapUserDefinition userConf : (List<CmsLdapUserDefinition>)this.getConfiguration().getUserDefinitions()) {
            String uidAttr = userConf.getMappingId();
            String[] searchRoots = (String[])userConf.getAccessContexts().toArray(new String[0]);
            String userFilter = userConf.getFilterAll();
            for (Map data : (List<Map>)this.lookupObjects(dbc, searchRoots, 2, userFilter, uidAttr)) {
                Object obj = data.get(uidAttr);
                String username = this.getDirContext(obj, false);
                if (!username.startsWith(userConf.getOuName())) {
                    username = userConf.getOuName() + username;
                }
                if (userNames.contains(username)) continue;
                userNames.add((String)username);
            }
        }
        this.name.put(CmsTimedCache.CmsTimedCacheKey.KEY_ALL_USERS, userNames);
        return userNames;
    }

    public List lookupUserNames(CmsDbContext dbc, CmsGroup group) throws CmsDataAccessException {
        CmsTimedCache.CmsTimedCacheKey key = CmsTimedCache.CmsTimedCacheKey.usersOfGroup(group.getName());
        List userNames = this.getCache().get(key);
        if (userNames != null) {
            return userNames;
        }
        userNames = new ArrayList();
        String ouName = group.getOuFqn();
        if (!ouName.endsWith("/")) {
            ouName = ouName + "/";
        }
        if (ouName.startsWith("/")) {
            ouName = ouName.substring("/".length());
        }
        for (CmsLdapGroupDefinition groupConf : (List<CmsLdapGroupDefinition>)this.getConfiguration().getGroupDefinitions()) {
            if (!groupConf.getOuName().equals(ouName)) continue;
            String[] searchRoots = (String[])groupConf.getAccessContexts().toArray(new String[0]);
            String[] groupMemberAttribute = new String[]{groupConf.getMappingMember()};
            List res = this.lookupObjects(dbc, searchRoots, 2, groupConf.getFilterByName(group.getSimpleName()), groupMemberAttribute);
            if (res == null) {
                if (!LOG.isDebugEnabled()) continue;
                LOG.debug((Object)Messages.get().getBundle().key("ERR_LDAP_RETRIEVING_GROUPUSERS_1", (Object)group.getName()));
                continue;
            }
            if (groupConf.getMembersFormat().equals("nested-fulldn")) {
                userNames.addAll(new ArrayList<String>(this.readNestedUsers(dbc, groupConf, group.getName(), ouName)));
                continue;
            }
            if (!groupConf.getMembersFormat().equals("mburl")) {
                for (Map result : (List<Map>)res) {
                    try {
                        Iterator itMembers;
                        Object groupMember = result.get(groupMemberAttribute[0]);
                        if (groupMember == null) continue;
                        if (!(groupMember instanceof List)) {
                            ArrayList list = new ArrayList();
                            list.add(groupMember);
                            itMembers = list.iterator();
                        } else {
                            itMembers = ((ArrayList)groupMember).iterator();
                        }
                        while (itMembers.hasNext()) {
                            String entry = (String)itMembers.next();
                            LdapName name = new LdapName(entry);
                            String userName = ouName + name.getRdn(name.size() - 1).getValue();
                            try {
                                this.lookupUser(dbc, userName, null);
                                userNames.add((String)userName);
                            }
                            catch (CmsDbEntryNotFoundException e) {
                                if (!LOG.isDebugEnabled()) continue;
                                LOG.debug((Object)e.getLocalizedMessage(), (Throwable)e);
                            }
                        }
                    }
                    catch (Throwable e) {
                        if (!LOG.isErrorEnabled()) continue;
                        LOG.error((Object)Messages.get().getBundle().key("ERR_LDAP_LOOKUP_GROUP_USERS_2", new Object[]{group.getName(), result}), e);
                    }
                }
                continue;
            }
            for (Map result : (List<Map>)res) {
                try {
                    List memberUrlList = new ArrayList();
                    Object obj = result.get(groupMemberAttribute[0]);
                    if (obj instanceof List) {
                        memberUrlList = (List)obj;
                    } else {
                        memberUrlList.add(obj);
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug((Object)Messages.get().getBundle().key("LOG_DEBUG_LOOKUP_GROUP_MEMBERURL_SIZE_2", (Object)new Integer(memberUrlList.size()), (Object)group.getName()));
                    }
                    Iterator iter = memberUrlList.iterator();
                    while (iter.hasNext()) {
                        CmsLdapGroupMemberUrl memberUrl = new CmsLdapGroupMemberUrl((String)iter.next());
                        if (LOG.isDebugEnabled()) {
                            LOG.debug((Object)Messages.get().getBundle().key("LOG_DEBUG_LOOKUP_GROUP_CHECK_1", (Object)memberUrl));
                        }
                        userNames.addAll(this.getDirContext(dbc, new String[]{memberUrl.getRoot()}, memberUrl.getScope(), memberUrl.getFilter()));
                    }
                }
                catch (Throwable e) {
                    if (!LOG.isErrorEnabled()) continue;
                    LOG.error((Object)Messages.get().getBundle().key("ERR_LDAP_LOOKUP_GROUP_USERS_2", new Object[]{group.getName(), result}), e);
                }
            }
        }
        this.name.put(key, userNames);
        return userNames;
    }

    public List lookupUsers(CmsDbContext dbc, String ouFqn) throws CmsLdapAccessException {
        String ouName = ouFqn;
        if (!ouName.endsWith("/")) {
            ouName = ouName + "/";
        }
        if (ouName.startsWith("/")) {
            ouName = ouName.substring("/".length());
        }
        CmsTimedCache.CmsTimedCacheKey key = CmsTimedCache.CmsTimedCacheKey.usersOfOu(ouName);
        List users = this.getCache().get(key);
        if (users != null) {
            return users;
        }
        users = new ArrayList();
        HashSet<String> test = new HashSet<String>();
        for (CmsLdapUserDefinition userConf : (List<CmsLdapUserDefinition>)this.getConfiguration().getUserDefinitions()) {
            if (!userConf.getOuName().equals(ouName)) continue;
            String[] searchRoots = (String[])userConf.getAccessContexts().toArray(new String[0]);
            String userFilter = userConf.getFilterAll();
            for (Map data : (List<Map>)this.lookupObjects(dbc, searchRoots, 2, userFilter, userConf.getSearchAttributes())) {
                CmsUser user = userConf.createUserFromSearchResult(data);
                if (test.contains(user.getName())) continue;
                users.add((CmsUser)user);
                test.add(user.getName());
            }
        }
        this.name.put(key, users);
        return users;
    }

    public List lookupUsersForSearch(CmsDbContext dbc, String ouFqn, String filter) throws CmsLdapAccessException {
        String ouName = ouFqn;
        if (!ouName.endsWith("/")) {
            ouName = ouName + "/";
        }
        if (ouName.startsWith("/")) {
            ouName = ouName.substring("/".length());
        }
        ArrayList<CmsUser> users = new ArrayList<CmsUser>();
        HashSet<String> test = new HashSet<String>();
        for (CmsLdapUserDefinition userConf : (List<CmsLdapUserDefinition>)this.getConfiguration().getUserDefinitions()) {
            String userFilter;
            if (!userConf.getOuName().equals(ouName) || (userFilter = userConf.getFilterSearch(filter)) == null) continue;
            String[] searchRoots = (String[])userConf.getAccessContexts().toArray(new String[0]);
            for (Map data : (List<Map>)this.lookupObjects(dbc, searchRoots, 2, userFilter, userConf.getSearchAttributes())) {
                CmsUser user = userConf.createUserFromSearchResult(data);
                if (test.contains(user.getName())) continue;
                users.add(user);
                test.add(user.getName());
            }
        }
        return users;
    }

    public CmsUser synchronizeUser(CmsDbContext dbc, CmsUser dbu, String pwd) throws CmsDataAccessException {
        CmsUser user = null;
        if (dbu != null) {
            if (!CmsLdapManager.getInstance().getConfiguration().isLookupDefaultUsers() && OpenCms.getDefaultUsers().isDefaultUser(dbu.getName())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug((Object)("Skip synchronization of default user " + dbu.getName()));
                }
                return dbu;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug((Object)("Synchronize user " + dbu.getName()));
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug((Object)"dbu should not be null");
            }
            return null;
        }
        boolean isLdap = CmsStringUtil.isNotEmptyOrWhitespaceOnly((String)((String)dbu.getAdditionalInfo("dn")));
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly((String)pwd) || isLdap) {
            if (isLdap && LOG.isDebugEnabled()) {
                LOG.debug((Object)("dbu user already marked as ldap user: " + dbu.getName()));
            }
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly((String)pwd) && LOG.isDebugEnabled()) {
                LOG.debug((Object)("dbu user: " + dbu.getName() + " with pwd"));
            }
            if (this.isLdapOnly()) {
                user = this.lookupUser(dbc, dbu, pwd);
                user = this.getDirContext(dbu, user);
            } else {
                try {
                    user = this.lookupUser(dbc, dbu, pwd);
                    user = this.getDirContext(dbu, user);
                }
                catch (CmsDbEntryNotFoundException e) {}
            }
        } else if (LOG.isDebugEnabled()) {
            LOG.debug((Object)("dbu user not marked as ldap user: " + dbu.getName() + " and no pwd, do not try to sync"));
        }
        if (user == null && CmsStringUtil.isEmptyOrWhitespaceOnly((String)pwd)) {
            user = dbu;
        }
        return user;
    }

    public void updateGroupsForUser(CmsDbContext dbc, CmsObject cms, CmsUser user) throws CmsException {
        String groupName;
        if (!CmsLdapManager.hasLdapFlag(user.getFlags())) {
            return;
        }
        if (this.getConfiguration().getGroupConsistency().equals("none")) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug((Object)("Updating groups for ldap user \"" + user.getName() + "\"."));
        }
        ArrayList<String> ldapGroupNames = new ArrayList<String>(this.lookupGroupNames(dbc, user));
        Iterator itGroups = ldapGroupNames.iterator();
        while (itGroups.hasNext()) {
            groupName = (String)itGroups.next();
            if (CmsLdapManager.getInstance().getLdapGroup().getName().equals(groupName)) continue;
            try {
                cms.readGroup(groupName);
            }
            catch (CmsException exc) {
                if (this.getConfiguration().getGroupConsistency().equals("all")) {
                    this.addGroup(cms, groupName);
                }
                itGroups.remove();
            }
        }
        ldapGroupNames.add(this.getLdapGroup().getName());
        itGroups = ldapGroupNames.iterator();
        while (itGroups.hasNext()) {
            groupName = itGroups.next().toString();
            try {
                cms.addUserToGroup(user.getName(), groupName);
            }
            catch (CmsException e) {
                LOG.error((Object)e.getMessage(), (Throwable)e);
            }
        }
        List groupsOfUser = cms.getGroupsOfUser(user.getName(), true, true);
        for (CmsGroup group : (List<CmsGroup>)groupsOfUser) {
            if (!CmsLdapManager.hasLdapFlag(group.getFlags()) || ldapGroupNames.contains(group.getName())) continue;
            if (LOG.isDebugEnabled()) {
                LOG.debug((Object)("delete user membership for group " + group.getName()));
            }
            cms.removeUserFromGroup(user.getName(), group.getName());
        }
    }

    public void updateObject(CmsDbContext dbc, String dn, Map attributes) throws CmsDataAccessException {
        DirContext ctx = null;
        if (LOG.isDebugEnabled()) {
            LOG.debug((Object)("updateObject: " + dn));
        }
        ModificationItem[] modifications = new ModificationItem[attributes.size()];
        int c = 0;
        for (String key : (Set<String>)attributes.keySet()) {
            modifications[c++] = new ModificationItem(2, new BasicAttribute(key, attributes.get(key)));
        }
        try {
            ctx = this.getDirContext(dbc, dn);
            if (LOG.isDebugEnabled()) {
                LOG.debug((Object)Messages.get().getBundle().key("LOG_DEBUG_MODIFIED_ATTRS_2", new Object[]{this.getRelativeDN(dn), Arrays.asList(modifications).toString()}));
            }
            ctx.modifyAttributes(this.getRelativeDN(dn), modifications);
        }
        catch (NamingException exc) {
            CmsMessageContainer message = Messages.get().container("ERR_UPDATING_OBJECT_1", (Object)exc.getExplanation());
            if (LOG.isErrorEnabled()) {
                LOG.error((Object)message, (Throwable)exc);
            }
            throw new CmsLdapAccessException(message, exc);
        }
        finally {
            if (ctx != null) {
                try {
                    ctx.close();
                }
                catch (NamingException exc) {}
            }
        }
    }

    public void updateUsersForGroup(CmsDbContext dbc, CmsObject cms, CmsGroup group) throws CmsException {
        String userName;
        Iterator itUsers;
        HashSet<String> usedUsers = new HashSet<String>();
        if (CmsLdapManager.hasLdapFlag(group.getFlags())) {
            itUsers = this.lookupUserNames(dbc, group).iterator();
            while (itUsers.hasNext()) {
                userName = itUsers.next().toString();
                usedUsers.add(userName);
            }
        }
        itUsers = usedUsers.iterator();
        while (itUsers.hasNext()) {
            userName = (String)itUsers.next();
            try {
                cms.readUser(userName);
            }
            catch (CmsException exc) {
                itUsers.remove();
            }
        }
        for (CmsUser user : (List<CmsUser>)cms.getUsersOfGroup(group.getName())) {
            if (usedUsers.contains(user.getName())) continue;
            cms.removeUserFromGroup(user.getName(), group.getName());
        }
        itUsers = usedUsers.iterator();
        while (itUsers.hasNext()) {
            String userName2 = itUsers.next().toString();
            cms.addUserToGroup(userName2, group.getName());
        }
    }

    protected void authenticate(String userDN, String credentials) throws CmsLdapAccessException, CmsDbEntryNotFoundException {
        if (LOG.isDebugEnabled()) {
            LOG.debug((Object)("authenticate user " + userDN));
        }
        String providerURL = this.getDirContext(userDN);
        String principal = this.getRelativeDN(userDN);
        CmsLdapProvider ldapProvider = providerURL != null ? (CmsLdapProvider)this.ldapConfig.getLdapProviders().get(providerURL) : this.provider;
        if (ldapProvider == null) {
            throw new CmsLdapAccessException(Messages.get().container("ERR_NO_LDAP_PROVIDER_1", (Object)providerURL));
        }
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put("java.naming.provider.url", ldapProvider.getProviderUrl());
        if (LOG.isDebugEnabled()) {
            LOG.debug((Object)Messages.get().getBundle().key("LOG_DEBUG_LDAP_AUTHENTICATION_AT_1", new Object[]{providerURL}));
        }
        for (String key : (Set<String>)ldapProvider.getConfiguration().keySet()) {
            String value = (String)ldapProvider.getConfiguration().get(key);
            if ("java.naming.security.principal".equals(key) || "java.naming.security.credentials".equals(key)) continue;
            if ("com.sun.jndi.ldap.connect.pool".equals(key)) {
                value = Boolean.toString(false);
            }
            env.put(key, value);
            if (!LOG.isDebugEnabled()) continue;
            LOG.debug((Object)("\t" + key + " = " + value));
        }
        env.put("java.naming.security.principal", principal);
        env.put("java.naming.security.credentials", credentials);
        if (LOG.isDebugEnabled()) {
            LOG.debug((Object)("\tjava.naming.security.principal = " + principal));
            LOG.debug((Object)("\tjava.naming.security.credentials = " + credentials));
        }
        InitialDirContext ctx = null;
        try {
            ctx = new InitialDirContext(env);
        }
        catch (NamingException exc) {
            if (LOG.isDebugEnabled()) {
                LOG.debug((Object)("authentication failed for user " + userDN));
            }
            throw new CmsDbEntryNotFoundException(Messages.get().container("ERR_AUTHENTICATION_FAILED_0"), (Throwable)exc);
        }
        finally {
            if (ctx != null) {
                try {
                    ctx.close();
                }
                catch (NamingException exc) {}
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug((Object)("authentication successfull for user " + userDN));
        }
    }

    protected DirContext bind(CmsDbContext dbc, CmsLdapProvider ldapProvider) throws NamingException {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put("java.naming.provider.url", ldapProvider.getProviderUrl());
        if (LOG.isDebugEnabled()) {
            LOG.debug((Object)Messages.get().getBundle().key("LOG_DEBUG_LDAP_BIND_TO_1", new Object[]{ldapProvider.getProviderUrl()}));
        }
        for (String key : (Set<String>)ldapProvider.getConfiguration().keySet()) {
            String value = (String)ldapProvider.getConfiguration().get(key);
            env.put(key, value);
            if (!LOG.isDebugEnabled() || key.equals("java.naming.security.credentials")) continue;
            LOG.debug((Object)("\t" + key + " = " + value));
        }
        if (this.getConfiguration().isUsePaging()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug((Object)Messages.get().getBundle().key("LOG_DEBUG_LDAP_BIND_CONTEXT_1", (Object)InitialLdapContext.class.getName()));
            }
            return new InitialLdapContext(env, null);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug((Object)Messages.get().getBundle().key("LOG_DEBUG_LDAP_BIND_CONTEXT_1", (Object)InitialDirContext.class.getName()));
        }
        return new InitialDirContext(env);
    }

    protected Map<String, List<String>> normalizeLdapResult(Map ldapResult) {
        HashMap<String, List<String>> result = new HashMap<String, List<String>>();
        Iterator<Map.Entry> i$ = ldapResult.entrySet().iterator();
        while (i$.hasNext()) {
            Map.Entry entryObj;
            Map.Entry entry = entryObj = i$.next();
            String key = (String)entry.getKey();
            Object value = entry.getValue();
            if (value instanceof List) {
                result.put(key, (List)value);
                continue;
            }
            String valueStr = (String)entry.getValue();
            ArrayList<String> valueList = new ArrayList<String>();
            valueList.add(valueStr);
            result.put(key, valueList);
        }
        return result;
    }

    protected List<Map<String, List<String>>> normalizeLdapResults(List results) {
        ArrayList<Map<String, List<String>>> normalizedResults = new ArrayList<Map<String, List<String>>>();
        for (Map singleResult : (List<Map>)results) {
            normalizedResults.add(this.normalizeLdapResult(singleResult));
        }
        return normalizedResults;
    }

    protected Collection<String> readNestedUsers(CmsDbContext dbc, CmsLdapGroupDefinition groupDef, String groupName, String ou) throws CmsDataAccessException {
        HashMap<String, List<String>> ldapResult = new HashMap<String, List<String>>();
        HashSet<String> userNames = new HashSet<String>();
        this.lookupGroup(dbc, groupName, ldapResult);
        String rootGroupDn = this.getRelativeDN(ldapResult.get("dn").get(0));
        HashSet<String> unprocessedDns = new HashSet<String>();
        unprocessedDns.add(rootGroupDn);
        HashSet<String> processedDns = new HashSet<String>();
        while (!unprocessedDns.isEmpty()) {
            Iterator iter = unprocessedDns.iterator();
            String dn = (String)iter.next();
            iter.remove();
            processedDns.add(dn);
            LdapName ldapName = null;
            try {
                ldapName = new LdapName(dn);
            }
            catch (InvalidNameException e) {
                LOG.error((Object)Messages.get().getBundle().key("ERR_LDAP_LOOKUP_GROUP_USERS_2", new Object[]{groupName, null}), (Throwable)e);
                continue;
            }
            String name = "" + ldapName.getRdn(ldapName.size() - 1).getValue();
            String nameWithOu = ou + name;
            CmsUser user = null;
            CmsGroup group = null;
            try {
                user = this.lookupUser(dbc, nameWithOu, null);
            }
            catch (CmsDbEntryNotFoundException e) {
                LOG.info((Object)e.getLocalizedMessage(), (Throwable)e);
            }
            if (user == null) {
                try {
                    group = this.lookupGroup(dbc, nameWithOu);
                }
                catch (CmsDbEntryNotFoundException e) {
                    LOG.info((Object)e.getLocalizedMessage(), (Throwable)e);
                }
            }
            if (user != null) {
                userNames.add(nameWithOu);
                continue;
            }
            if (group == null) continue;
            List<Map<String, List<String>>> results = this.normalizeLdapResults(this.lookupObjects(dbc, (String[])groupDef.getAccessContexts().toArray(new String[0]), 2, groupDef.getFilterByName(name), groupDef.getMappingMember()));
            for (Map<String, List<String>> result : results) {
                List<String> members = result.get(groupDef.getMappingMember());
                if (members == null) continue;
                for (String member : members) {
                    if (processedDns.contains(member)) continue;
                    unprocessedDns.add(member);
                }
            }
        }
        return userNames;
    }

    
    protected List searchAttributes(DirContext ctx, int searchScope, String[] searchRoots, String filter, String[] attributes, boolean onlyFirst)
    	    throws NamingException
    	  {
    	    SearchControls ctls = new SearchControls();
    	    ctls.setSearchScope(searchScope);
    	    ctls.setReturningAttributes(attributes);
    	    
    	    List res = new ArrayList();
    	    for (int i = 0; i < searchRoots.length; i++) {
    	      NamingEnumeration answer = null;
    	      
    	      try
    	      {
    	        byte[] cookie = null;
    	        int page = 0;
    	        if (getConfiguration().isUsePaging())
    	        {
    	          if (LOG.isDebugEnabled()) {
    	            LOG.debug(Messages.get().getBundle().key("LOG_DEBUG_SEARCH_USE_PAGING_1", new Integer(getConfiguration().getPageSize())));
    	          }
    	          


    	          ((LdapContext)ctx).setRequestControls(new Control[] { new PagedResultsControl(getConfiguration().getPageSize(), false) });
    	        }
    	        


    	        if (LOG.isDebugEnabled()) {
    	          LOG.debug(Messages.get().getBundle().key("LOG_DEBUG_SEARCH_3", new Object[] { searchRoots[i], filter, Arrays.asList(attributes).toString() }));
    	        }
    	        

    	        do
    	        {
    	          answer = ctx.search(searchRoots[i], filter, ctls);
    	          
    	          if (getConfiguration().isUsePaging()) {
    	            page++;
    	            
    	            if (LOG.isDebugEnabled()) {
    	              LOG.debug(Messages.get().getBundle().key("LOG_DEBUG_SEARCH_FETCH_PAGE_1", new Integer(page)));
    	            }
    	          }
    	          


    	          while ((answer != null) && (answer.hasMore())) {
    	            SearchResult sr = (SearchResult)answer.next();
    	            Attributes attrs = sr.getAttributes();
    	            Map obj = new HashMap(attributes.length + 1);
    	            if (sr.isRelative()) {
    	              String name = sr.getName();
    	              String fullDN = ctx.getEnvironment().get("java.naming.provider.url") + "/" + name + (name.equals("") ? "" : ",") + searchRoots[i];
    	              



    	              obj.put("dn", fullDN.trim());
    	            } else {
    	              obj.put("dn", sr.getName().trim());
    	            }
    	            
    	            if (LOG.isDebugEnabled()) {
    	              LOG.debug(Messages.get().getBundle().key("LOG_DEBUG_SEARCH_ENTRY_NAME_1", obj.get("dn")));
    	            }
    	            


    	            for (int j = 0; j < attributes.length; j++) {
    	              Attribute attr = attrs.get(attributes[j]);
    	              if (attr != null) {
    	                if (attr.size() > 1) {
    	                  if (LOG.isDebugEnabled()) {
    	                    LOG.debug(Messages.get().getBundle().key("LOG_DEBUG_SEARCH_ATTRS_LIST_2", attributes[j], new Integer(attr.size())));
    	                  }
    	                  



    	                  List list = new ArrayList();
    	                  Enumeration attVals = attr.getAll();
    	                  while (attVals.hasMoreElements()) {
    	                    String value = (String)attVals.nextElement();
    	                    if (LOG.isDebugEnabled()) {
    	                      LOG.debug(Messages.get().getBundle().key("LOG_DEBUG_SEARCH_ATTRS_LIST_2", attributes[j], value));
    	                    }
    	                    


    	                    list.add(value);
    	                  }
    	                  
    	                  obj.put(attributes[j], list);
    	                } else if (attr.size() == 1) {
    	                  String value = attr.toString();
    	                  if (LOG.isDebugEnabled()) {
    	                    LOG.debug(Messages.get().getBundle().key("LOG_DEBUG_SEARCH_ATTRS_LIST_2", attributes[j], value));
    	                  }
    	                  


    	                  obj.put(attributes[j], value.substring(value.indexOf(":") + 1).trim());
    	                }
    	                else if (LOG.isDebugEnabled()) {
    	                  LOG.debug(Messages.get().getBundle().key("LOG_DEBUG_SEARCH_ATTRS_LIST_2", attributes[j], new Integer(attr.size())));
    	                }
    	              }
    	            }
    	            



    	            res.add(obj);
    	            if (onlyFirst) {
    	              return res;
    	            }
    	          }
    	          
    	          if (getConfiguration().isUsePaging())
    	          {

    	            Control[] controls = ((LdapContext)ctx).getResponseControls();
    	            if (controls != null) {
    	              for (int k = 0; k < controls.length; k++) {
    	                if ((controls[k] instanceof PagedResultsResponseControl)) {
    	                  PagedResultsResponseControl prrc = (PagedResultsResponseControl)controls[k];
    	                  cookie = prrc.getCookie();
    	                }
    	              }
    	            }
    	            

    	            ((LdapContext)ctx).setRequestControls(new Control[] { new PagedResultsControl(getConfiguration().getPageSize(), cookie, false) });

    	          }
    	          
    	        }
    	        while (cookie != null);
    	      }
    	      catch (Throwable exc)
    	      {
    	        LOG.error(exc.getLocalizedMessage(), exc);
    	      } finally {
    	        if (answer != null) {
    	          answer.close();
    	        }
    	      }
    	    }
    	    
    	    return res;
    	  }

    
    
    protected List searchAttributes(DirContext ctx, String[] searchRoots, String filter, String[] attributes) throws NamingException {
        return this.getDirContext(ctx, 2, searchRoots, filter, attributes);
    }

    private DirContext getDirContext(CmsDbContext dbc) throws NamingException {
        return this.bind(dbc, this.provider);
    }

    private DirContext getDirContext(CmsDbContext dbc, String fullDN) throws NamingException {
        CmsLdapProvider ldapProvider = (CmsLdapProvider)this.ldapConfig.getLdapProviders().get(this.getDirContext(fullDN));
        return this.bind(dbc, ldapProvider);
    }

    private String name(String string) {
        return (String)Rdn.unescapeValue(string);
    }

    private String getDirContext(String fullDN) {
        if (fullDN != null && fullDN.startsWith("ldap")) {
            return fullDN.substring(0, fullDN.lastIndexOf("/"));
        }
        return null;
    }

    private String getDirContext(Object obj, boolean unescape) {
        if (obj instanceof List) {
            if (LOG.isWarnEnabled()) {
                LOG.warn((Object)Messages.get().getBundle().key("LOG_WARN_OBJECT_VALUE_LIST_1", obj));
            }
            obj = ((List)obj).get(0);
        }
        if (unescape) {
            return (String)Rdn.unescapeValue((String)obj);
        }
        return (String)obj;
    }

    private Map getDirContext(CmsDbContext dbc, String name, String password, CmsLdapUserDefinition userConf) throws CmsDataAccessException {
        if (LOG.isDebugEnabled()) {
            LOG.debug((Object)("looking up for user " + name + " " + (CmsStringUtil.isNotEmptyOrWhitespaceOnly((String)password) ? "with pwd" : "without pwd")));
        }
        Map res = null;
        String[] searchRoots = (String[])userConf.getAccessContexts().toArray(new String[0]);
        DirContext ctx = null;
        try {
            ctx = this.getDirContext(dbc);
            res = this.getDirContext(ctx, searchRoots, userConf.getFilterByName(name), userConf.getSearchAttributes());
            if (res != null && !res.isEmpty() && password != null) {
                this.authenticate((String)res.get("dn"), password);
            }
            Map map = res;
            return map;
        }
        catch (AuthenticationException ae) {
            if (LOG.isInfoEnabled()) {
                LOG.info((Object)Messages.get().getBundle().key("LOG_INFO_USER_LOGIN_FAILED_1", new Object[]{name}), (Throwable)ae);
            }
            throw new CmsDbEntryNotFoundException(org.opencms.db.Messages.get().container("ERR_UNKNOWN_USER_1", (Object)name));
        }
        catch (NamingException exc) {
            if (LOG.isErrorEnabled()) {
                LOG.error((Object)Messages.get().getBundle().key("LOG_WARN_LOOKING_UP_USER_1", new Object[]{name}), (Throwable)exc);
            }
            throw new CmsDbEntryNotFoundException(org.opencms.db.Messages.get().container("ERR_UNKNOWN_USER_1", (Object)name));
        }
        finally {
            if (ctx != null) {
                try {
                    ctx.close();
                }
                catch (NamingException exc) {}
            }
        }
    }

    private Map getDirContext(CmsDbContext dbc, CmsLdapGroupDefinition groupConf) throws CmsLdapAccessException {
        String[] searchRoots = (String[])groupConf.getAccessContexts().toArray(new String[0]);
        String[] groupAttributes = new String[]{groupConf.getMappingId(), groupConf.getMappingMember()};
        List<Map> res = this.lookupObjects(dbc, searchRoots, 2, groupConf.getFilterAll(), groupAttributes);
        HashMap groupData = new HashMap(res.size());
        for (Map data : res) {
            groupData.put(data.get(groupAttributes[0]), data.get(groupAttributes[1]));
        }
        return groupData;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private List getDirContext(CmsDbContext dbc, String[] searchRoots, int scope, String filter) {
        ArrayList<String> users;
        users = new ArrayList<String>();
        DirContext ctx = null;
        try {
            ctx = this.getDirContext(dbc);
            for (CmsLdapUserDefinition userConf : (List<CmsLdapUserDefinition>)this.getConfiguration().getUserDefinitions()) {
                List res;
                try {
                    res = this.getDirContext(ctx, scope, searchRoots, filter, userConf.getSearchAttributes());
                }
                catch (Throwable e) {
                    if (!LOG.isErrorEnabled()) continue;
                    LOG.error((Object)Messages.get().getBundle().key("ERR_LOOKING_UP_USERS_1", (Object)e.getMessage()), e);
                    continue;
                }
                if (res == null) {
                    if (!LOG.isErrorEnabled()) continue;
                    LOG.error((Object)Messages.get().getBundle().key("ERR_LOOKING_UP_USERS_1"));
                    continue;
                }
                Map userAttributes = userConf.getAttributeMappings();
                for (Map data : (List<Map>)res) {
                    String uid = userConf.getValue(data, "userid", "", userAttributes);
                    if (!uid.startsWith(userConf.getOuName())) {
                        uid = userConf.getOuName() + uid;
                    }
                    if (users.contains(uid)) continue;
                    users.add(uid);
                }
            }
        }
        catch (Throwable e) {
            if (LOG.isErrorEnabled()) {
                LOG.error((Object)Messages.get().getBundle().key("ERR_LOOKING_UP_USERS_1", (Object)e.getMessage()), e);
            }
        }
        finally {
            if (ctx != null) {
                try {
                    ctx.close();
                }
                catch (NamingException exc) {}
            }
        }
        return users;
    }

    private Map getDirContext(DirContext ctx, String[] searchRoots, String filter, String[] attributes) throws NamingException {
        List res = this.searchAttributes(ctx, 2, searchRoots, filter, attributes, true);
        if (res == null || res.isEmpty()) {
            return null;
        }
        return (Map)res.get(0);
    }

    private List getDirContext(DirContext ctx, int searchScope, String[] searchRoots, String filter, String[] attributes) throws NamingException {
        return this.searchAttributes(ctx, searchScope, searchRoots, filter, attributes, false);
    }

    private CmsUser getDirContext(CmsUser user, CmsUser ldapUser) {
        if (LOG.isDebugEnabled()) {
            LOG.debug((Object)("Updating dbu: " + user.getName() + ", lu: " + ldapUser.getName()));
        }
        user.setName(ldapUser.getName());
        user.setDescription(ldapUser.getDescription());
        user.setFirstname(ldapUser.getFirstname());
        user.setLastname(ldapUser.getLastname());
        user.setEmail(ldapUser.getEmail());
        user.setAdditionalInfo("dn", ldapUser.getAdditionalInfo("dn"));
        user.setAddress(ldapUser.getAddress());
        if (!CmsLdapManager.hasLdapFlag(user.getFlags())) {
            user.setFlags(user.getFlags() ^ 131072);
        }
        return user;
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public class GroupLookup {
        private DirContext dn;
        private List<CmsLdapGroupDefinition> name;
        private Map<String, CmsLdapGroupBean> group;
        private Map<String, CmsLdapGroupBean> provider;
        private CmsUser groupDef;

        public GroupLookup(DirContext ctx, CmsUser user, Collection<CmsLdapGroupDefinition> groupDefinitions) {
            this.group = new HashMap<String, CmsLdapGroupBean>();
            this.provider = new HashMap<String, CmsLdapGroupBean>();
            this.dn = ctx;
            this.groupDef = user;
            this.name = new ArrayList<CmsLdapGroupDefinition>(groupDefinitions);
        }

        public Map<String, CmsLdapGroupBean> getAllGroups() {
            this.computeAllGroups();
            return Collections.unmodifiableMap(this.group);
        }

        protected void computeAllGroups() {
            Map<String, CmsLdapGroupBean> userGroups = this.keysToLower(this.getUserGroups(this.groupDef));
            this.provider.putAll(userGroups);
            while (!this.provider.isEmpty()) {
                Iterator<Map.Entry<String, CmsLdapGroupBean>> iter = this.provider.entrySet().iterator();
                Map.Entry<String, CmsLdapGroupBean> entry = iter.next();
                iter.remove();
                String groupDn = entry.getKey();
                CmsLdapGroupBean groupBean = entry.getValue();
                this.group.put(groupDn, groupBean);
                Map<String, CmsLdapGroupBean> parentGroups = this.keysToLower(this.getParentGroups(groupBean.getDn()));
                parentGroups.keySet().removeAll(this.group.keySet());
                this.provider.putAll(parentGroups);
            }
        }

        protected Map<String, CmsLdapGroupBean> getParentGroups(String fullDn) {
            HashMap<String, CmsLdapGroupBean> result = new HashMap<String, CmsLdapGroupBean>();
            for (CmsLdapGroupDefinition groupDef : this.name) {
                Map<String, CmsLdapGroupBean> groupsForDef = this.getParentGroups(fullDn, groupDef);
                result.putAll(groupsForDef);
            }
            return result;
        }

        protected Map<String, CmsLdapManager.CmsLdapGroupBean> getParentGroups(String fullDn, CmsLdapGroupDefinition groupDef)
        {
          CmsLdapManager.LOG.info("Getting parent groups of " + fullDn);
          String[] searchRoots = (String[])groupDef.getAccessContexts().toArray(new String[0]);
          String[] groupidAttribute = { groupDef.getMappingId() };
          Map result = new HashMap();
          List ldapResults = null;
          try
          {
            String filter = groupDef.getFilterByMemberValue(fullDn, true);
            ldapResults = normalizeLdapResults(searchAttributes(this.dn, searchRoots, filter, groupidAttribute));
          } catch (NamingException exc) {
            if (CmsLdapManager.LOG.isErrorEnabled()) {
              CmsLdapManager.LOG.error(org.opencms.db.Messages.get().getBundle().key("ERR_GET_GROUPS_OF_USER_2", new Object[] { fullDn }), exc);
            }
          }
          if (ldapResults == null) {
              return Collections.emptyMap();
            }
            for (Map singleResult : (List<Map>)ldapResults) {
              String dn = getRelativeDN((String)((List)singleResult.get("dn")).get(0));
              String name = (String)((List)singleResult.get(groupDef.getMappingId())).get(0);
              LOG.info("Found parent group " + name + " " + dn);
              result.put(dn, new CmsLdapGroupBean(name, dn, groupDef));
            }
            return result;
          }
        
        protected Map<String, CmsLdapGroupBean> getUserGroups(CmsUser user) {
            HashMap<String, CmsLdapGroupBean> result = new HashMap<String, CmsLdapGroupBean>();
            for (CmsLdapGroupDefinition groupDef : this.name) {
                Map<String, CmsLdapGroupBean> groupsForDef = this.getUserGroups(user, groupDef);
                result.putAll(groupsForDef);
            }
            return result;
        }

        protected Map<String, CmsLdapManager.CmsLdapGroupBean> getUserGroups(CmsUser user, CmsLdapGroupDefinition groupDef)
        {
          LOG.info("Getting direct groups for user " + user.getName());
          String[] searchRoots = (String[])groupDef.getAccessContexts().toArray(new String[0]);
          String[] groupidAttribute = { groupDef.getMappingId() };
          Map result = new HashMap();
          List ldapResults = null;
          try
          {
            ldapResults = normalizeLdapResults(searchAttributes(this.dn, searchRoots, groupDef.getFilterByMember(user), groupidAttribute));

          }
          catch (NamingException exc)
          {

            if (LOG.isErrorEnabled()) {
              LOG.error(org.opencms.db.Messages.get().getBundle().key("ERR_GET_GROUPS_OF_USER_2", new Object[] { user.getName() }), exc);
            }
          }

          if (ldapResults == null) {
            return Collections.emptyMap();
          }
          for (Map singleResult : (List<Map>)ldapResults) {
            String dn = (String)((List)singleResult.get("dn")).get(0);
            dn = getRelativeDN(dn);
            String name = (String)((List)singleResult.get(groupDef.getMappingId())).get(0);
            LOG.info("Found direct group: " + name + " " + dn);
            result.put(dn, new CmsLdapGroupBean(name, dn, groupDef));
          }
          return result;
        }
        protected Map<String, CmsLdapGroupBean> keysToLower(Map<String, CmsLdapGroupBean> map) {
            HashMap<String, CmsLdapGroupBean> result = new HashMap<String, CmsLdapGroupBean>();
            for (Map.Entry<String, CmsLdapGroupBean> entry : map.entrySet()) {
                String key = entry.getKey().toLowerCase();
                result.put(key, entry.getValue());
            }
            return result;
        }
    }

    public class CmsLdapGroupBean {
        private String dn;
        private CmsLdapGroupDefinition groupDef;
        private String name;

        public CmsLdapGroupBean(String name, String dn, CmsLdapGroupDefinition groupDef) {
            this.name = name;
            this.dn = dn;
            this.groupDef = groupDef;
        }

        public String getCmsName() {
            String ou = this.groupDef.getOuName();
            return (ou + "/" + this.name).replaceAll("/+", "/").replaceAll("^/", "");
        }

        public String getDn() {
            return this.dn;
        }

        public CmsLdapGroupDefinition getGroupDef() {
            return this.groupDef;
        }

        public String getName() {
            return this.name;
        }
    }

}
