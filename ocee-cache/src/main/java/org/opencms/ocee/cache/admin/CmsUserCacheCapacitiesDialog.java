/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  jakarta.servlet.http.HttpServletRequest
 *  jakarta.servlet.http.HttpServletResponse
 *  jakarta.servlet.jsp.PageContext
 *  org.opencms.jsp.CmsJspActionElement
 */
package org.opencms.ocee.cache.admin;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.ocee.cache.I_CmsCacheInstanceType;
import org.opencms.ocee.cache.admin.A_CmsCacheCapacitiesBean;
import org.opencms.ocee.cache.admin.A_CmsCacheCapacitiesDialog;
import org.opencms.ocee.cache.admin.CmsUserCacheCapacitiesBean;
import org.opencms.ocee.cache.user.CmsUserCacheInstanceType;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class CmsUserCacheCapacitiesDialog
extends A_CmsCacheCapacitiesDialog {
    public CmsUserCacheCapacitiesDialog(CmsJspActionElement jsp) {
        super(jsp);
    }

    public CmsUserCacheCapacitiesDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    @Override
    protected A_CmsCacheCapacitiesBean createCapacitiesBean() {
        return new CmsUserCacheCapacitiesBean();
    }

    @Override
    protected List<? extends I_CmsCacheInstanceType> getTypes() {
        return CmsUserCacheInstanceType.VALUES;
    }
}

