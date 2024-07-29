package com.tfsla.diario.friendlyTags;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspException;

import com.tfsla.diario.terminos.data.PersonsDAO;
import com.tfsla.diario.terminos.model.Persons;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsPersonIdTag extends A_TfsPersonValueTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = 15456337789867231L;
	
	@Override
	 public int doStartTag() throws JspException {
		try{
		 Persons person = getCurrentPerson().getPerson();
		 
		 if (person!=null)
 			pageContext.getOut().print(Long.toString( person.getId_person()));		 
	     
		}catch(IOException e){
			throw new JspException(e);
		}
		 return SKIP_BODY;
	 }

}

/*extends A_TfsPersonValueTag {

	
	private static final long serialVersionUID = -8085623127673841427L;

	@Override
	    public int doStartTag() throws JspException {
	    	try {
	    		Persons person = getCurrentPerson().getPerson();
	    		if (person!=null)
	    			pageContext.getOut().print(Long.toString( person.getId_person()));

			} catch (IOException e) {
				throw new JspException(e);
			}
			
			return SKIP_BODY;
	    }

}*/
