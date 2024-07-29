/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.PageContext
 *  org.opencms.jsp.CmsJspActionElement
 */
package org.opencms.ocee.cache.admin;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.ocee.cache.I_CmsCacheInstanceType;
import org.opencms.ocee.cache.admin.A_CmsCacheCapacitiesBean;
import org.opencms.ocee.cache.admin.A_CmsCacheCapacitiesDialog;
import org.opencms.ocee.cache.admin.CmsProjectCacheCapacitiesBean;
import org.opencms.ocee.cache.project.CmsProjectCacheInstanceType;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class CmsProjectCacheCapacitiesDialog
extends A_CmsCacheCapacitiesDialog {
    public CmsProjectCacheCapacitiesDialog(CmsJspActionElement jsp) {
        super(jsp);
    }

    public CmsProjectCacheCapacitiesDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    @Override
    protected A_CmsCacheCapacitiesBean createCapacitiesBean() {
        return new CmsProjectCacheCapacitiesBean();
    }

    @Override
    protected List<? extends I_CmsCacheInstanceType> getTypes() {
        return CmsProjectCacheInstanceType.VALUES;
    }
}

