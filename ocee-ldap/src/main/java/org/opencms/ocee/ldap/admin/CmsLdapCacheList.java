package org.opencms.ocee.ldap.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsRuntimeException;
import org.opencms.ocee.ldap.CmsLdapManager;
import org.opencms.ocee.ldap.CmsTimedCache.CmsTimedCacheEntry;
import org.opencms.ocee.ldap.CmsTimedCache.CmsTimedCacheKey;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDateFormatter;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListIndependentAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListOrderEnum;

public class CmsLdapCacheList extends A_CmsListDialog {
    public static final String LIST_ACTION_ICON = "ai";
    public static final String LIST_COLUMN_ICON = "ci";
    public static final String LIST_COLUMN_NAME = "ck";
    public static final String LIST_COLUMN_TIME = "ct";
    public static final String LIST_COLUMN_TYPE = "cp";
    public static final String LIST_DETAIL_CONTENT = "dc";
    public static final String LIST_IACTION_FLUSH = "if";
    public static final String LIST_ID = "lc";
    public static final String LIST_MACTION_REMOVE = "mc";

    public CmsLdapCacheList(CmsJspActionElement jsp) {
        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_LDAPCACHE_LIST_NAME_0), LIST_COLUMN_TIME, CmsListOrderEnum.ORDER_DESCENDING, LIST_COLUMN_NAME);
    }

    public CmsLdapCacheList(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    public void executeListMultiActions() throws CmsRuntimeException {
        if (!getParamListAction().equals(LIST_MACTION_REMOVE)) {
            throwListUnsupportedActionException();
        }
        for (CmsListItem item : getSelectedItems()) {
            CmsLdapManager.getInstance().getCache().remove(CmsTimedCacheKey.valueOf(item.getId()));
        }
        listSave();
    }

    public void executeListSingleActions() {
        throwListUnsupportedActionException();
    }

    protected void fillDetails(String detailId) {
        Map cache = CmsLdapManager.getInstance().getCache().getData();
        for (CmsListItem item : (List<CmsListItem>)getList().getAllContent()) {
            List<Object> data = (List)((CmsTimedCacheEntry) cache.get(CmsTimedCacheKey.valueOf(item.getId()))).getCacheData();
            StringBuffer html = new StringBuffer(512);
            try {
                for (Object dataItem : data) {
                    html.append(dataItem.toString());
                    html.append("<br>\n");
                }
            } catch (Exception e) {
            }
            item.set(LIST_DETAIL_CONTENT, html.toString());
        }
    }

    protected List<CmsListItem> getListItems() {
        List ret = new ArrayList();
        for (Entry entry : CmsLdapManager.getInstance().getCache().getData().entrySet()) {
            CmsListItem item = getList().newItem(((CmsTimedCacheKey) entry.getKey()).toString());
            item.set(LIST_COLUMN_TIME, new Date(((CmsTimedCacheEntry) entry.getValue()).getTime()));
            item.set(LIST_COLUMN_TYPE, ((CmsTimedCacheKey) entry.getKey()).getType().name());
            item.set(LIST_COLUMN_NAME, ((CmsTimedCacheKey) entry.getKey()).getName());
            ret.add(item);
        }
        return ret;
    }

    protected void initMessages() {
        addMessages(Messages.get().getBundleName());
        super.initMessages();
    }

    protected void setColumns(CmsListMetadata metadata) {
        CmsListColumnDefinition iconCol = new CmsListColumnDefinition("ci");
        iconCol.setName(Messages.get().container(Messages.GUI_LDAPCACHE_LIST_COLS_ICON_0));
        iconCol.setWidth("20");
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        iconCol.setSorteable(false);
        CmsListDirectAction iconAction = new CmsListDirectAction("ai");
        iconAction.setName(Messages.get().container(Messages.GUI_LDAPCACHE_LIST_ACTION_ICON_NAME_0));
        iconAction.setIconPath("tools/ocee-ldap/buttons/cacheentry.png");
        iconAction.setEnabled(false);
        iconCol.addDirectAction(iconAction);
        metadata.addColumn(iconCol);
        CmsListColumnDefinition typeCol = new CmsListColumnDefinition(LIST_COLUMN_TYPE);
        typeCol.setName(Messages.get().container(Messages.GUI_LDAPCACHE_LIST_COLS_TYPE_0));
        typeCol.setWidth("20%");
        metadata.addColumn(typeCol);
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_LDAPCACHE_LIST_COLS_NAME_0));
        nameCol.setWidth("50%");
        metadata.addColumn(nameCol);
        CmsListColumnDefinition timeCol = new CmsListColumnDefinition(LIST_COLUMN_TIME);
        timeCol.setName(Messages.get().container(Messages.GUI_LDAPCACHE_LIST_COLS_TIME_0));
        timeCol.setWidth("30%");
        timeCol.setFormatter(new CmsListDateFormatter());
        metadata.addColumn(timeCol);
    }

    protected void setIndependentActions(CmsListMetadata metadata) {
        CmsListItemDetails variationsDetails = new CmsListItemDetails(LIST_DETAIL_CONTENT);
        variationsDetails.setAtColumn(LIST_COLUMN_TYPE);
        variationsDetails.setVisible(false);
        variationsDetails.setShowActionName(Messages.get().container(Messages.GUI_LDAPCACHE_DETAIL_SHOW_CONTENT_NAME_0));
        variationsDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_LDAPCACHE_DETAIL_SHOW_CONTENT_HELP_0));
        variationsDetails.setHideActionName(Messages.get().container(Messages.GUI_LDAPCACHE_DETAIL_HIDE_CONTENT_NAME_0));
        variationsDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_LDAPCACHE_DETAIL_HIDE_CONTENT_HELP_0));
        variationsDetails.setName(Messages.get().container(Messages.GUI_LDAPCACHE_DETAIL_CONTENT_NAME_0));
        variationsDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_LDAPCACHE_DETAIL_CONTENT_NAME_0)));
        metadata.addItemDetails(variationsDetails);
        CmsListIndependentAction clearAction = new CmsListIndependentAction(LIST_IACTION_FLUSH);
        clearAction.setName(Messages.get().container(Messages.GUI_LDAPCACHE_IACTION_CLEAR_NAME_0));
        clearAction.setHelpText(Messages.get().container(Messages.GUI_LDAPCACHE_IACTION_CLEAR_HELP_0));
        clearAction.setConfirmationMessage(Messages.get().container(Messages.GUI_LDAPCACHE_IACTION_CLEAR_CONF_0));
        clearAction.setIconPath("tools/ocee-ldap/buttons/clear.png");
        clearAction.setEnabled(true);
        clearAction.setVisible(true);
        metadata.addIndependentAction(clearAction);
    }

    public void executeListIndepActions() {
        if (getParamListAction().equals(LIST_IACTION_FLUSH)) {
            CmsLdapManager.getInstance().getCache().flush();
        } else {
            super.executeListIndepActions();
        }
    }

    protected void setMultiActions(CmsListMetadata metadata) {
        CmsListMultiAction cleanEntry = new CmsListMultiAction(LIST_MACTION_REMOVE);
        cleanEntry.setName(Messages.get().container(Messages.GUI_LDAPCACHE_LIST_ACTION_MREMOVE_NAME_0));
        cleanEntry.setIconPath("tools/ocee-ldap/buttons/remove.png");
        cleanEntry.setEnabled(true);
        cleanEntry.setHelpText(Messages.get().container(Messages.GUI_LDAPCACHE_LIST_ACTION_MREMOVE_HELP_0));
        metadata.addMultiAction(cleanEntry);
    }
}
