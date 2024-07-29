package com.tfsla.diario.friendlyTags;


import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsNoticiaEncuestaFreeTag extends A_TfsEncuestaNoticia  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8687704179001473367L;
	private int cant = 1;

	@Override
	protected void mostrarEncuesta() throws JspException {

		I_TfsNoticia noticia = getCurrentNews();
        
		String encustaURL = getIndexElementValue(noticia,getTagNamePoll()); //encuesta
	    String position = getIndexElementValue(noticia,getTagNamePollsPosition()); //posicion configurada en la encuesta relacionada

	    if(cant<=size || size==0){
		    if(!pos.equals("")){
				    if (position.equals(pos))
				    	includeBoxEncuesta(encustaURL);
			 }else{
				 includeBoxEncuesta(encustaURL);
			 }
	    }
	    cant++;
	}

	@Override
	protected void initPosition() throws JspException {
		// TODO Auto-generated method stub
		setPosition(pos);
		cant = 1;
	}

	@Override
	protected boolean isInLastPosition() {
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