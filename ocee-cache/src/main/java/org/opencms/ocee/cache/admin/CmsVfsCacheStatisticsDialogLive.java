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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
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

