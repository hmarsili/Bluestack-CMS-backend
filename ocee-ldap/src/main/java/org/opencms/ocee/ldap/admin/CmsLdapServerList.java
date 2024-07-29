package org.opencms.ocee.ldap.admin;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.opencms.db.CmsDbContext;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsCoreProvider;
import org.opencms.main.CmsException;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.ldap.CmsLdapAccessException;
import org.opencms.ocee.ldap.CmsLdapManager;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.list.CmsHtmlList;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListOrderEnum;

public class CmsLdapServerList extends CmsHtmlList {
    private String f162x226a583a;
    private CmsObject f163xbb78c9c2;

    public CmsLdapServerList(CmsHtmlList list, CmsObject cms, String ouFqn) {
        super(list.getId(), list.getName(), list.getMetadata());
        setMaxItemsPerPage(list.getMaxItemsPerPage());
        if (list.getSortedColumn() != null) {
            setSortedColumn(list.getSortedColumn());
        }
        this.f163xbb78c9c2 = cms;
        this.f162x226a583a = ouFqn;
    }

    public void setSearchFilter(String searchFilter) {
        if (this.m_metadata.isSearchable()) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(searchFilter)) {
                if (!this.m_metadata.isSelfManaged()) {
                    this.m_filteredItems = null;
                }
                if (CmsOceeManager.getInstance().checkCoreVersion("7.5.2")) {
                    this.m_searchFilter = "";
                    getMetadata().getSearchAction().getShowAllAction().setVisible(false);
                } else if (CmsOceeManager.getInstance().checkCoreVersion("7.5.1")) {
                    getMetadata().getSearchAction().setSearchFilter("");
                } else {
                    this.m_searchFilter = "";
                    getMetadata().getSearchAction().getShowAllAction().setVisible(false);
                }
            } else {
                if (!this.m_metadata.isSelfManaged()) {
                    this.m_filteredItems = getMetadata().getSearchAction().filter(getAllContent(), searchFilter);
                }
                m17x226a583a(searchFilter);
                if (CmsOceeManager.getInstance().checkCoreVersion("7.5.2")) {
                    this.m_searchFilter = searchFilter;
                    getMetadata().getSearchAction().getShowAllAction().setVisible(true);
                } else if (CmsOceeManager.getInstance().checkCoreVersion("7.5.1")) {
                    getMetadata().getSearchAction().setSearchFilter(searchFilter);
                } else {
                    this.m_searchFilter = searchFilter;
                    getMetadata().getSearchAction().getShowAllAction().setVisible(true);
                }
            }
            String sCol = this.m_sortedColumn;
            this.m_sortedColumn = "";
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(sCol)) {
                CmsListOrderEnum order = getCurrentSortOrder();
                setSortedColumn(sCol);
                if (order == CmsListOrderEnum.ORDER_DESCENDING) {
                    setSortedColumn(sCol);
                }
            }
            setCurrentPage(1);
        }
    }

    private void m18x226a583a(CmsDbContext dbc, CmsListItem item) {
        String name = item.get("cn").toString();
        CmsGroup group = null;
        for (CmsListItemDetails detail : (List<CmsListItemDetails>)getMetadata().getItemDetailDefinitions()) {
            if (detail.isVisible()) {
                if (group == null) {
                    try {
                        group = CmsLdapManager.getInstance().lookupGroup(dbc, name);
                    } catch (CmsDataAccessException e) {
                        return;
                    }
                }
                String detailId = detail.getId();
                StringBuffer html = new StringBuffer(512);
                if (detailId.equals("du")) {
                    try {
                        Iterator itUsers = CmsLdapManager.getInstance().lookupUserNames(dbc, group).iterator();
                        while (itUsers.hasNext()) {
                            html.append(itUsers.next());
                            if (itUsers.hasNext()) {
                                html.append("<br>");
                            }
                            html.append("\n");
                        }
                    } catch (CmsException e2) {
                    }
                } else if (detailId.equals("dl")) {
                    try {
                        html.append(CmsLdapManager.getInstance().getDNforGroup(dbc, group.getName()));
                    } catch (CmsDataAccessException e3) {
                    }
                } else if (detailId.equals("dd")) {
                    html.append(group.getDescription());
                    html.append("<br>\n");
                }
                item.set(detailId, html.toString());
            }
        }
    }

    private void m19xbb78c9c2(CmsDbContext dbc, CmsListItem item) {
        String name = item.get("cl").toString();
        CmsUser user = null;
        for (CmsListItemDetails detail :  (List<CmsListItemDetails>)getMetadata().getItemDetailDefinitions()) {
            if (detail.isVisible()) {
                if (user == null) {
                    try {
                        user = CmsLdapManager.getInstance().lookupUser(dbc, name, null);
                    } catch (CmsDataAccessException e) {
                        return;
                    }
                }
                String detailId = detail.getId();
                StringBuffer html = new StringBuffer(512);
                if (detailId.equals("da")) {
                    html.append(user.getAddress());
                    if (user.getCity() != null) {
                        html.append("<br>");
                        if (user.getAdditionalInfo("USER_ZIPCODE") != null) {
                            html.append(user.getAdditionalInfo("USER_ZIPCODE"));
                            html.append(" ");
                        }
                        html.append(user.getCity());
                    }
                    if (user.getAdditionalInfo("USER_COUNTRY") != null) {
                        html.append("<br>");
                        html.append(user.getAdditionalInfo("USER_COUNTRY"));
                    }
                } else if (detailId.equals("dl")) {
                    html.append(user.getAdditionalInfo("dn"));
                } else if (detailId.equals("dg")) {
                    try {
                        List groups = CmsLdapManager.getInstance().lookupGroupNames(dbc, user);
                        Iterator itGroups = groups.iterator();
                        while (itGroups.hasNext()) {
                            try {
                                this.f163xbb78c9c2.readGroup((String) itGroups.next());
                            } catch (CmsException e2) {
                                itGroups.remove();
                            }
                        }
                        itGroups = groups.iterator();
                        while (itGroups.hasNext()) {
                            html.append(itGroups.next());
                            if (itGroups.hasNext()) {
                                html.append("<br>");
                            }
                            html.append("\n");
                        }
                    } catch (CmsDataAccessException e3) {
                    }
                } else if (detailId.equals("dn")) {
                    html.append(user.getFullName());
                    html.append("<br>\n");
                }
                item.set(detailId, html.toString());
            }
        }
    }

    private void m17x226a583a(String searchFilter) {
        Set systemPrincipals;
        boolean isUserList = getId().equals(CmsLdapNotSyncUsersList.LIST_ID);
        if (isUserList) {
            systemPrincipals = CmsLdapNotSyncUsersList.getSystemUsers(this.f163xbb78c9c2, this.f162x226a583a);
        } else {
            systemPrincipals = CmsLdapNotSyncGroupsList.getSystemGroups(this.f163xbb78c9c2, this.f162x226a583a);
        }
        CmsDbContext dbc = CmsCoreProvider.getInstance().getNewDbContext(null);
        if (isUserList) {
            try {
                Iterator itLdapPrincipals = CmsLdapManager.getInstance().lookupUsersForSearch(dbc, this.f162x226a583a, searchFilter).iterator();
            } catch (CmsLdapAccessException e) {
                dbc.clear();
                return;
            }
        }
        Iterator<Object> itLdapPrincipals;
		try {
			itLdapPrincipals = CmsLdapManager.getInstance().lookupGroupNamesForSearch(dbc, this.f162x226a583a, searchFilter).iterator();
		
        while (itLdapPrincipals.hasNext()) {
            String name;
            String email = null;
            if (isUserList) {
                CmsUser user = (CmsUser) itLdapPrincipals.next();
                name = user.getName();
                email = user.getEmail();
            } else {
                try {
                    name = (String) itLdapPrincipals.next();
                } finally {
                    dbc.clear();
                }
            }
            if (!systemPrincipals.contains(name)) {
                CmsListItem item;
                boolean found = false;
                for (CmsListItem item2 : (List<CmsListItem>)this.m_filteredItems) {
                    if (item2.getId().equals(name)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                	CmsListItem item2 = newItem(name);
                    if (isUserList) {
                        item2.set("cl", name);
                        item2.set("ce", email);
                        m19xbb78c9c2(dbc, item2);
                    } else {
                        item2.set("cn", name);
                        m18x226a583a(dbc, item2);
                    }
                    this.m_filteredItems.add(item2);
                    this.m_originalItems.add(item2);
                }
            }
        }
		} catch (CmsLdapAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
}
