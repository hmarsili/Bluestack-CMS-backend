package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.BodyTagSupport;
import jakarta.servlet.jsp.tagext.Tag;

public class TfsExcludeFragmentInCollectionTag  extends BodyTagSupport {
	
	private String onposition = "";
	private boolean oncondition = true;
	
	public TfsExcludeFragmentInCollectionTag()
	{
		oncondition = true;
	}
	
	public String getOnposition() {
		return onposition;
	}

	public void setOnposition(String onposition) {
		this.onposition = onposition;
	}
	
	public boolean isOncondition() {
		return oncondition;
	}

	public void setOncondition(boolean oncondition) {
		this.oncondition = oncondition;
	}

	public int doStartTag() throws JspException {
		
		if (oncondition==false)
			return EVAL_BODY_INCLUDE;
			
		if (onposition.equals("") && oncondition==true)
			return SKIP_BODY;
		
		String[] positions = onposition.split(",");

		Tag ancestor = findAncestorWithClass(this, I_TfsCollectionListTag.class);
	    if (ancestor == null) {
	        throw new JspTagException("context is not in collection");
	    }
		
	    I_TfsCollectionListTag collection = (I_TfsCollectionListTag) ancestor;
	    
	    for (String position : positions) {
		    //ultimo
		    if (position.trim().toLowerCase().equals("last") && collection.isLast())
		    	return SKIP_BODY;
		    
		    //primero
		    if (position.trim().toLowerCase().equals("first") && collection.getIndex()==0)
		    	return SKIP_BODY;
	
		    //impar
		    if (position.trim().toLowerCase().equals("odd") && (collection.getIndex()+1) % 2 != 0)
		    	return SKIP_BODY;
	
		    //par
		    if (position.trim().toLowerCase().equals("even") && (collection.getIndex()+1) % 2 == 0)
		    	return SKIP_BODY;
	
		    try {
			    int pos = Integer.parseInt(position);
			    if (pos-1==collection.getIndex())
			    	return SKIP_BODY;
		    }
		    catch (java.lang.NumberFormatException ex)
		    {
		    	return EVAL_BODY_INCLUDE;		    	
		    }
		 
	    }
		//no coincide
		return EVAL_BODY_INCLUDE;
			
	}
	
	//TODO: <nt:excludehtml onposition="first|last|odd|even|1...N"></nt:excludehtml>

}
