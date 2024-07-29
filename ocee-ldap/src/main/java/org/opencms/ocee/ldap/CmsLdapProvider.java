package org.opencms.ocee.ldap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.main.CmsLog;

public class CmsLdapProvider implements I_CmsConfigurationParameterHandler {
    private boolean f124x226a583a = false;
    private Map f125xbb78c9c2;
    private String f126x350cdc9c;

    public void addConfigurationParameter(String paramName, String paramValue) {
        if (this.f125xbb78c9c2 == null) {
            this.f125xbb78c9c2 = new HashMap();
        }
        this.f125xbb78c9c2.put(paramName, paramValue);
    }

    public Map getConfiguration() {
        return Collections.unmodifiableMap(this.f125xbb78c9c2);
    }

    public boolean isDefault() {
        return this.f124x226a583a;
    }

    public String getProviderUrl() {
        return this.f126x350cdc9c;
    }

    public void initConfiguration() {
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_LDAP_PROVIDER_1, new Object[]{this.f126x350cdc9c}));
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_LDAP_PROVIDER_PARAMETERS_1, new Object[]{this.f125xbb78c9c2}));
        }
    }

    public void setDefault(boolean isDefault) {
        this.f124x226a583a = isDefault;
    }

    public void setProviderUrl(String providerUrl) {
        this.f126x350cdc9c = providerUrl;
    }
}
