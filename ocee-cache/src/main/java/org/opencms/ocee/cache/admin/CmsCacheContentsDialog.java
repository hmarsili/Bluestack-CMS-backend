/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  jakarta.servlet.http.HttpServletRequest
 *  jakarta.servlet.http.HttpServletResponse
 *  jakarta.servlet.jsp.JspException
 *  jakarta.servlet.jsp.JspWriter
 *  jakarta.servlet.jsp.PageContext
 *  org.opencms.i18n.CmsEncoder
 *  org.opencms.i18n.CmsMessages
 *  org.opencms.jsp.CmsJspActionElement
 *  org.opencms.main.CmsIllegalArgumentException
 *  org.opencms.util.CmsStringUtil
 *  org.opencms.workplace.CmsDialog
 *  org.opencms.workplace.tools.CmsTool
 *  org.opencms.workplace.tools.I_CmsToolHandler
 */
package org.opencms.ocee.cache.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.ocee.cache.A_CmsCacheInstanceType;
import org.opencms.ocee.cache.CmsCacheManager;
import org.opencms.ocee.cache.I_CmsCacheInstance;
import org.opencms.ocee.cache.I_CmsCacheInstanceType;
import org.opencms.ocee.cache.admin.Messages;
import org.opencms.ocee.cache.vfs.CmsCacheFiles;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.tools.CmsTool;
import org.opencms.workplace.tools.I_CmsToolHandler;

public class CmsCacheContentsDialog
extends CmsDialog {
    public static final String[] PAGES = new String[]{"page1"};
    public static final String PARAM_CACHE = "cache";
    public static final String PARAM_ONLINE = "online";
    private String \u00d2000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;
    private String o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;

    public CmsCacheContentsDialog(CmsJspActionElement jsp) {
        super(jsp);
    }

    public CmsCacheContentsDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    public String dialogTitle() {
        String title = super.dialogTitle();
        String newTitle = Messages.get().getBundle(this.getLocale()).key("GUI_CACHE_CONTENTS_2", (Object)(this.isOnline() ? "Online" : "Offline"), (Object)this.key(new StringBuilder().append("counter.group.").append(this.getParamCache()).toString())) + "\n";
        return CmsStringUtil.substitute((String)title, (String)(this.resolveMacros(this.getAdminTool().getHandler().getName()) + "\n"), (String)newTitle);
    }

    public void displayDialog() throws JspException, IOException {
        I_CmsCacheInstanceType type;
        if (this.isForwarded()) {
            return;
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly((String)this.getParamCache()) || this.getAction() == 4) {
            this.actionCloseDialog();
            return;
        }
        StringBuffer result = new StringBuffer(10240);
        result.append(this.htmlStart(null));
        result.append(this.bodyStart("dialog", null));
        result.append(this.dialogStart());
        result.append(this.dialogContentStart(this.getParamTitle()));
        try {
            type = A_CmsCacheInstanceType.valueOf(this.getParamCache());
        }
        catch (CmsIllegalArgumentException e) {
            this.actionCloseDialog();
            return;
        }
        I_CmsCacheInstance cache = CmsCacheManager.getInstance().getCacheInstance(type);
        Map<String, Object> data = null;
        data = this.isOnline() ? cache.getCacheOnline() : cache.getCacheOffline();
        boolean displayContent = cache instanceof CmsCacheFiles;
        for (Map.Entry<String, Object> entry : new HashSet<Map.Entry<String, Object>>(data.entrySet())) {
            result.append("<p><b>" + entry.getKey() + "</b><br>\n");
            result.append("<pre>");
            if (displayContent) {
                byte[] value = (byte[])entry.getValue();
                int length = ((CmsCacheFiles)cache).getShowLength();
                String outVal = CmsEncoder.escapeXml((String)new String(value, 0, Math.min(length, value.length), "UTF-8"));
                result.append(outVal);
                if (outVal.length() < value.length) {
                    result.append("\n...");
                }
            } else {
                List values = new ArrayList<Object>();
                if (entry.getValue() instanceof List) {
                    values = (List)entry.getValue();
                } else {
                    values.add((Object)entry.getValue());
                }
                Iterator itValues = values.iterator();
                while (itValues.hasNext()) {
                    result.append(itValues.next()).append("\n");
                }
            }
            result.append("</pre></p>\n");
        }
        result.append(this.dialogContentEnd());
        result.append(this.dialogEnd());
        result.append(this.bodyEnd());
        result.append(this.htmlEnd());
        JspWriter out = this.getJsp().getJspContext().getOut();
        out.print(result.toString());
    }

    public String getParamCache() {
        return this.\u00d2000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;
    }

    public String getParamOnline() {
        return this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
    }

    public boolean isOnline() {
        return Boolean.valueOf(this.getParamOnline());
    }

    public void setParamCache(String paramCache) {
        this.\u00d2000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new = paramCache;
    }

    public void setParamOnline(String paramOnline) {
        this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = paramOnline;
    }

    protected void initMessages() {
        this.addMessages(Messages.get().getBundleName());
        super.initMessages();
    }
}

