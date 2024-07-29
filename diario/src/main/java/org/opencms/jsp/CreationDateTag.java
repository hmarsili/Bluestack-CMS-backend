package org.opencms.jsp;

import java.util.Date;

public class CreationDateTag extends DateTag {
    
    protected Date getDate() {
        return new Date(this.getAncestor().getXmlDocument().getFile().getDateCreated());
    }

}
