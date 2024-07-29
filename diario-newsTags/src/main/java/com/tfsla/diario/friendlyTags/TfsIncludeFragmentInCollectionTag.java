package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;

public class TfsIncludeFragmentInCollectionTag extends BodyTagSupport {
	private String onposition = "";

	private boolean oncondition = true;

	public String getOnposition() {
		return onposition;
	}

	public TfsIncludeFragmentInCollectionTag()
	{
		oncondition = true;
	}
	
	public void setOnposition(String onposition) {
		this.onposition = onposition;
	}
	
	public int doStartTag() throws JspException {

		if (oncondition==false)
			return SKIP_BODY;
			
		if (onposition.equals("") && oncondition==true)
			return EVAL_BODY_INCLUDE;
		
		String[] positions = onposition.split(",");

		
		Tag ancestor = findAncestorWithClass(this, I_TfsCollectionListTag.class);
	    if (ancestor == null) {
	        throw new JspTagException("context is not in collection");
	    }
		
	    I_TfsCollectionListTag collection = (I_TfsCollectionListTag) ancestor;
	    
	    for (String position : positions) {
		    //ultimo
		    if (position.trim().toLowerCase().equals("last") && collection.isLast())
		    	return EVAL_BODY_INCLUDE;
		    
		    //primero
		    if (position.trim().toLowerCase().equals("first") && collection.getIndex()==0)
		    	return EVAL_BODY_INCLUDE;
	
		    //impar
		    if (position.trim().toLowerCase().equals("odd") && (collection.getIndex()+1) % 2 != 0)
		    	return EVAL_BODY_INCLUDE;
	
		    //par
		    if (position.trim().toLowerCase().equals("even") && (collection.getIndex()+1) % 2 == 0)
		    	return EVAL_BODY_INCLUDE;
	
		    //posicion seleccionada
		    
		    try {
			    int pos = Integer.parseInt(position);
			    if (pos-1==collection.getIndex())
			    	return EVAL_BODY_INCLUDE;
		    }
		    catch (java.lang.NumberFormatException ex)
		    {
		    	return SKIP_BODY;		    	
		    }
	    }
		//no coincide
		return SKIP_BODY;
			
	}

	public boolean isOncondition() {
		return oncondition;
	}

	public void setOncondition(boolean oncondition) {
		this.oncondition = oncondition;
	}
	
	//TODO: <nt:includehtml onposition="first|last|odd|even|1...N"></nt:includehtml>
}
