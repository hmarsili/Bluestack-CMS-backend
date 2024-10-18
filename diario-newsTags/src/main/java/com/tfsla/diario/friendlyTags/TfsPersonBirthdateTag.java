package com.tfsla.diario.friendlyTags;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.servlet.jsp.JspException;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.diario.terminos.model.Persons;


public class TfsPersonBirthdateTag extends A_TfsPersonValueTag {
	private static final long serialVersionUID = 8555870817163605611L;
	
	/** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(A_TfsNoticiaValue.class);
	
	private final String defaultFormat = " dd/MM/yyyy hh:mm";
	private String format=null;
	
	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	@Override
	    public int doStartTag() throws JspException {
	    	try {		        
	    		Persons person = getCurrentPerson().getPerson();
	    		if (person!=null){
	    			//pageContext.getOut().print( person.getBirthdate());
	    		
	    		Date uModif = person.getBirthdate();
		        SimpleDateFormat sdf = null;
		        String content = null;
	    		
	    		if (format!=null && !format.equals(""))
		        	sdf = new SimpleDateFormat(format);
		        else
		        	sdf = new SimpleDateFormat(defaultFormat);
		        
		        content = sdf.format(uModif);
		        
		        printContent(content);
	    		}

			} catch (Exception e) {
				printContent("");
			}
			
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
