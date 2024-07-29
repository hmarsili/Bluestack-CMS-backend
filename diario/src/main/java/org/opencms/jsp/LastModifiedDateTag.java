package org.opencms.jsp;

import java.util.Date;

public class LastModifiedDateTag extends DateTag {

    protected Date getDate() {
    	
        return new Date(this.getAncestor().getXmlDocument().getFile().getDateLastModified());
    }
    
}
