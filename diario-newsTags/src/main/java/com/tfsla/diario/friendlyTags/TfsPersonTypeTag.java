package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.terminos.model.Persons;

public class TfsPersonTypeTag extends A_TfsPersonValueTag {
	private static final long serialVersionUID = 6926981345339356913L;

	@Override
	    public int doStartTag() throws JspException {
		
			try {
	    		Persons person = getCurrentPerson().getPerson();
	    		if (person!=null && person.getType() != null){
	    			if(!person.getType().isEmpty()){
		    			String parentPath = "/system/categories/";
		    			pageContext.getOut().print(parentPath + person.getType());
	    			}else{
	    				pageContext.getOut().print(person.getType());
	    			}
	    		}

			} catch (IOException e) {
				throw new JspException(e);
			}
			
			return SKIP_BODY;
	    }

}
