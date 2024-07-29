package com.tfsla.diario.friendlyTags;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;
import org.opencms.xml.CmsXmlException;

import com.tfsla.diario.terminos.data.PersonsDAO;
import com.tfsla.diario.terminos.model.Persons;

public class TfsPeopleItemIdTag extends TagSupport{

    /**
	 * 
	 */
	private static final long serialVersionUID = -3612858321413275905L;
	protected static final Log LOG = CmsLog.getLog(TfsPeopleItemTag.class);

	@Override
    public int doStartTag() throws JspException {
		 Tag ancestor = findAncestorWithClass(this, TfsPeopleListTag.class);
		    if (ancestor == null) {
		        throw new JspTagException("Tag peopleList not accesible");
		    }
		
		    TfsPeopleListTag tagList = (TfsPeopleListTag) ancestor;
		    pageContext.getRequest().setAttribute("peopleitem", tagList.getCurrentItem()!=null?tagList.getCurrentItem():""); 
		    Persons people = new Persons();
			PersonsDAO peopledao = new PersonsDAO();
		    
		    try {
		    	List<Persons> persons = peopledao.getPersonByWord(tagList.getCurrentItem());
		    	if(!persons.isEmpty()){
		    		people = persons.get(0);
		    	}				
			} catch (Exception e) {
				e.printStackTrace();
			}	
		    pageContext.getRequest().setAttribute("peopleid", Long.toString(people.getId_person()));
		    printContent(Long.toString(people.getId_person()));
		    return SKIP_BODY;
	}	
	
	protected void printContent(String content) throws JspException {
		try {
	            pageContext.getOut().print(content);
	    } catch (IOException e) {
	        if (LOG.isErrorEnabled()) {
	            LOG.error("Error trying to retrieve Title", e);
	        }
	        throw new JspException(e);
	    }
	}

	
}
