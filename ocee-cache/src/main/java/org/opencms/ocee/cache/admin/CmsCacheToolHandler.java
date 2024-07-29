/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  org.opencms.file.CmsObject
 *  org.opencms.ocee.base.CmsOceeManager
 *  org.opencms.workplace.tools.CmsOnlyAdminToolHandler
 */
package org.opencms.ocee.cache.admin;

import org.opencms.file.CmsObject;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.cache.CmsCacheManager;
import org.opencms.workplace.tools.CmsOnlyAdminToolHandler;

public class CmsCacheToolHandler
extends CmsOnlyAdminToolHandler {
    public String getDisabledHelpText() {
        if (CmsCacheManager.getInstance() == null) {
            return CmsOceeManager.getInstance().getDefaultHelpText();
        }
        return "${key.GUI_CACHE_DISABLED_NO_CONFIG_0}";
    }

    public boolean isEnabled(CmsObject cms) {
        return CmsCacheManager.getInstance() != null && CmsCacheManager.getInstance().isInitialized();
    }

    public boolean isVisible(CmsObject cms) {
        CmsCacheManager manager = CmsCacheManager.getInstance();
        if (manager == null) {
            return true;
        }
        if ((this.getPath().startsWith("/ocee-cache/user-stats") || this.getPath().startsWith("/ocee-cache/user-caps")) && !CmsOceeManager.getInstance().checkCoreVersion("7.5.2")) {
            return false;
        }
        return super.isVisible(cms);
    }
}

