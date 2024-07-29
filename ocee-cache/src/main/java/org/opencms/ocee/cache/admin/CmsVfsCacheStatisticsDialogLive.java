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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.ocee.cache.admin.CmsVfsCacheStatisticsDialog;

public class CmsVfsCacheStatisticsDialogLive
extends CmsVfsCacheStatisticsDialog {
    public CmsVfsCacheStatisticsDialogLive(CmsJspActionElement jsp) {
        super(jsp);
    }

    public CmsVfsCacheStatisticsDialogLive(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    protected void checkRole() {
    }
}

