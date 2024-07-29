package org.opencms.ocee.ldap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.naming.ldap.Rdn;
import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

public class CmsLdapObject {
    private static final Log f78x5ae05e43 = CmsLog.getLog(CmsLdapObject.class);
    private String f79x226a583a;
    private boolean f80xbb78c9c2;
    private String f81x350cdc9c;
    private String f82x59c4920d = "";
    protected Map f83xe3a92f9d;
    private List f84xc79e744a;
    private String f85x94ebafec;
    private String f86xdc6db74;
    protected String[] f87x27afe517;

    public void addAccessContext(String context) {
        if (this.f84xc79e744a == null) {
            this.f84xc79e744a = new ArrayList();
        }
        this.f84xc79e744a.add(context);
    }

    public List getAccessContexts() {
        return Collections.unmodifiableList(this.f84xc79e744a);
    }

    public String getFilterAll() {
        return this.f81x350cdc9c;
    }

    public String getFilterByName() {
        return this.f86xdc6db74;
    }

    public String getFilterByName(String name) {
        if (name.startsWith(getOuName())) {
            name = name.substring(getOuName().length());
        }
        return getFilterByName().replace("?", Rdn.escapeValue(name));
    }

    public String getFilterSearch() {
        return this.f85x94ebafec;
    }

    public String getFilterSearch(String filter) {
        if (getFilterSearch() == null) {
            return null;
        }
        return getFilterSearch().replace("?", Rdn.escapeValue(filter));
    }

    public String getMappingId() {
        return this.f79x226a583a;
    }

    public String getOuName() {
        return this.f82x59c4920d;
    }

    public String getValue(Map attributes, String key, String defValue, Map keyMap) {
        String aKey;
        if (keyMap != null) {
            aKey = (String) keyMap.get(key);
        } else {
            aKey = key;
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(aKey)) {
            Object obj = attributes.get(aKey);
            if (obj instanceof List) {
                if (f78x5ae05e43.isWarnEnabled()) {
                    f78x5ae05e43.warn(Messages.get().getBundle().key(Messages.LOG_WARN_VALUE_LIST_1, aKey));
                }
                obj = ((List) obj).get(0);
            }
            String value = (String)obj;
            if (CmsStringUtil.isNotEmpty(value)) {
                return value;
            }
        }
        return defValue;
    }

    public String getValueUnescaped(Map attributes, String key, String defValue, Map keyMap) {
        String value = getValue(attributes, key, defValue, keyMap);
        try {
            return (String) Rdn.unescapeValue(value);
        } catch (Throwable t) {
            if (f78x5ae05e43.isWarnEnabled()) {
                f78x5ae05e43.warn(t.getLocalizedMessage() + " - attributes: " + attributes.toString());
            }
            return value;
        }
    }

    public boolean isEditable() {
        return this.f80xbb78c9c2;
    }

    public void setEditable(String editable) {
        this.f80xbb78c9c2 = Boolean.valueOf(editable).booleanValue();
    }

    public void setFilterAll(String filterAll) {
        this.f81x350cdc9c = filterAll;
    }

    public void setFilterByName(String filterByName) {
        this.f86xdc6db74 = filterByName;
    }

    public void setFilterSearch(String filterSearch) {
        this.f85x94ebafec = filterSearch;
    }

    public void setMappingId(String mappingId) {
        this.f79x226a583a = mappingId;
    }

    public void setOuName(String ouName) {
        this.f82x59c4920d = ouName;
    }
}
