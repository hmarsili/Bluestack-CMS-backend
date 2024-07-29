package com.tfsla.diario.friendlyTags;


import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsNoticiaEncuestaTag extends A_TfsEncuestaNoticia  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8687704179001473387L;
	
	protected void mostrarEncuesta() throws JspException {
		
		I_TfsNoticia noticia = getCurrentNews();
        
		String encustaURL = getIndexElementValue(noticia,getTagNamePoll()); //encuesta
	    String position = getIndexElementValue(noticia,getTagNamePollsPosition()); //posicion
	    
	    TfsCuerpoSeparadoTag cuerpo = getCuerpoSeparado();
	    
	    if (position.equals("first") && cuerpo.getIndex()==0)
	    	includeBoxEncuesta(encustaURL);
	    
	    if (position.equals(""+(cuerpo.getIndex()+1)))
	    	includeBoxEncuesta(encustaURL);
	    
	    if (position.equals("last") && cuerpo.isLast())
	    	includeBoxEncuesta(encustaURL);
	    	
	    
	}
	
	private TfsCuerpoSeparadoTag getCuerpoSeparado() throws JspTagException
	{
		Tag ancestor = findAncestorWithClass(this, TfsCuerpoSeparadoTag.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag News not accesible");
	    }
	
	    TfsCuerpoSeparadoTag tagList = (TfsCuerpoSeparadoTag) ancestor;

	    return tagList;
	}

	@Override
	protected void initPosition() throws JspException {
		// TODO Auto-generated method stub
	    TfsCuerpoSeparadoTag cuerpo = getCuerpoSeparado();
	    setPosition(""+(cuerpo.getIndex()+1));
	    realIndex = 0;
	}

	@Override
	protected boolean isInLastPosition() {
	    TfsCuerpoSeparadoTag cuerpo;
		try {
			cuerpo = getCuerpoSeparado();
			return cuerpo.isLast();
		} catch (JspTagException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
		
	}
	
	protected String getTagNamePoll(){
		String tagNamePolls = TfsXmlContentNameProvider.getInstance().getTagName("news.polls.poll");
		if(tagNamePolls==null) tagNamePolls = TfsXmlContentNameProvider.getInstance().getTagName("news.pools.pool");
		
		return tagNamePolls;
	}
	
	protected String getTagNamePollsPosition(){
		String TagPollsPosition = TfsXmlContentNameProvider.getInstance().getTagName("news.polls.position");
		if(TagPollsPosition==null) TagPollsPosition = TfsXmlContentNameProvider.getInstance().getTagName("news.pools.position");
	   
		return TagPollsPosition;
	}


}
