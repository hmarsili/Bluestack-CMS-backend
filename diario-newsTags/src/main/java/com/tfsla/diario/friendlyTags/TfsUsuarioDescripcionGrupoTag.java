package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

public class TfsUsuarioDescripcionGrupoTag extends TagSupport {

	 /**
	 * 
	 */
	private static final long serialVersionUID = -8396496481029634700L;
	private static final Log LOG = CmsLog.getLog(TfsUsuarioDescripcionGrupoTag.class);
	@Override
	 public int doStartTag() throws JspException {

		
		 
		 String namegroup =  getCurrentGrupoUsuario();
		 //collection.getIndexElementValue(keyName, collection.); //.getCollectionValue();
		 if (namegroup!=null){
		//printContent(namegroup);	
			 try {
				pageContext.getOut().print(namegroup);
			} catch (IOException e) {
				
				LOG.debug(e.getStackTrace());
				//e.printStackTrace();
			}
		 }
		
		 return SKIP_BODY;
	 }
	protected String getCurrentGrupoUsuario() throws JspTagException {
		// get a reference to the parent "content container" class
	    Tag ancestor = findAncestorWithClass(this, TfsUsuarioGroupsTag.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag TfsUsuarioGroupsTag not accesible");
	    }
	    
	    TfsUsuarioGroupsTag usuarioGrupos = (TfsUsuarioGroupsTag) ancestor;
	    
	    return usuarioGrupos.getValue();
	}


}
