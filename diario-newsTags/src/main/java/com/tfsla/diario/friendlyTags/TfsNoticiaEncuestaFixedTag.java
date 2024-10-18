package com.tfsla.diario.friendlyTags;

import jakarta.servlet.jsp.JspException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsNoticiaEncuestaFixedTag extends A_TfsEncuestaNoticia {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3734424822669547466L;
	private String url="";
	String encustaURL = "";
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	protected void mostrarEncuesta() throws JspException {
		// TODO Auto-generated method stub

		I_TfsNoticia noticia = getCurrentNews();
        
		if(url != ""){
			encustaURL = url;
		}else{
			encustaURL = getIndexElementValue(noticia,getTagNamePoll());  //encuesta
		}
	    String position = getIndexElementValue(noticia,getTagNamePollsPosition()); //posicion

	    if (position.toLowerCase().equals("fixed"))
	    	includeBoxEncuesta(encustaURL);
	}

	@Override
	protected void initPosition() throws JspException {
		setPosition("fixed");
		
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
