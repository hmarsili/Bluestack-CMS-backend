package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsVodPathTag extends A_TfsNoticiaCollectionValue {

	
	
    @Override
    public int doStartTag() throws JspException {

    	setKeyName("");
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 String content = collection.getCollectionValue(getCollectionPathName());  

        printContent(content);

        return SKIP_BODY;
    }
    
	

}


