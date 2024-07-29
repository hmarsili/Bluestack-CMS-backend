/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.PageContext
 *  org.opencms.jsp.CmsJspActionElement
 *  org.opencms.main.OpenCms
 *  org.opencms.widgets.CmsInputWidget
 *  org.opencms.widgets.I_CmsWidget
 *  org.opencms.workplace.CmsWidgetDialog
 *  org.opencms.workplace.CmsWidgetDialogParameter
 *  org.opencms.workplace.CmsWorkplaceSettings
 *  org.opencms.workplace.tools.CmsIdentifiableObjectContainer
 */
package org.opencms.ocee.cache.admin;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.ocee.cache.CmsCacheConfiguration;
import org.opencms.ocee.cache.I_CmsCacheInstanceType;
import org.opencms.ocee.cache.admin.A_CmsCacheCapacitiesBean;
import org.opencms.ocee.cache.admin.Messages;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.tools.CmsIdentifiableObjectContainer;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public abstract class A_CmsCacheCapacitiesDialog
extends CmsWidgetDialog {
    public static final String KEY_PREFIX = "cache";
    public static final String[] PAGES = new String[]{"page1"};
    private A_CmsCacheCapacitiesBean o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
    private List<I_CmsCacheInstanceType> \u00d2000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;

    public A_CmsCacheCapacitiesDialog(CmsJspActionElement jsp) {
        super(jsp);
    }

    public A_CmsCacheCapacitiesDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    public void actionCommit() {
        ArrayList<Throwable> errors = new ArrayList<Throwable>();
        try {
            this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.commit();
            OpenCms.writeConfiguration(CmsCacheConfiguration.class);
        }
        catch (Throwable t) {
            errors.add(t);
        }
        this.setCommitErrors(errors);
    }

    protected abstract A_CmsCacheCapacitiesBean createCapacitiesBean();

    protected String createDialogHtml(String dialog) {
        StringBuffer result = new StringBuffer(1024);
        result.append(this.createWidgetTableStart());
        result.append(this.createWidgetErrorHeader());
        if (dialog.equals(PAGES[0])) {
            int i = 0;
            for (I_CmsCacheInstanceType type : this.getListOfTypes()) {
                String blockKey = "GUI_CACHE_EDITOR_LABEL_" + type.getType().toUpperCase() + "_BLOCK_0";
                result.append(this.dialogBlockStart(this.key(blockKey)));
                result.append(this.createWidgetTableStart());
                result.append(this.createDialogRowsHtml(i, i + (type.isProjectAware() ? 1 : 0)));
                result.append(this.createWidgetTableEnd());
                result.append(this.dialogBlockEnd());
                i += type.isProjectAware() ? 2 : 1;
            }
        }
        result.append(this.createWidgetTableEnd());
        return result.toString();
    }

    protected void defineWidgets() {
        this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = this.createCapacitiesBean();
        this.setKeyPrefix("cache");
        for (I_CmsCacheInstanceType type : this.getListOfTypes()) {
            if (type.isProjectAware()) {
                this.addWidget(new CmsWidgetDialogParameter((Object)this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super, type.getType() + "Cache.capacityOffline", PAGES[0], (I_CmsWidget)new CmsInputWidget()));
            }
            this.addWidget(new CmsWidgetDialogParameter((Object)this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super, type.getType() + "Cache.capacityOnline", PAGES[0], (I_CmsWidget)new CmsInputWidget()));
        }
    }

    protected List<I_CmsCacheInstanceType> getListOfTypes() {
        if (this.\u00d2000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new == null) {
            CmsIdentifiableObjectContainer container = new CmsIdentifiableObjectContainer(true, true);
            for (I_CmsCacheInstanceType type : this.getTypes()) {
                container.addIdentifiableObject(type.getType(), (Object)type, (float)type.getOrder());
            }
            this.\u00d2000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new = container.elementList();
        }
        return this.\u00d2000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;
    }

    protected String[] getPageArray() {
        return PAGES;
    }

    protected abstract List<? extends I_CmsCacheInstanceType> getTypes();

    protected void initMessages() {
        this.addMessages(Messages.get().getBundleName());
        super.initMessages();
    }

    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        super.initWorkplaceRequestValues(settings, request);
        this.setDialogObject((Object)this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super);
    }
}

