package com.tfsla.diario.friendlyTags;

import java.io.IOException;
import java.util.Date;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.OpenCms;

import com.tfsla.diario.utils.TfsIncludeContentUtil;
import com.tfsla.diario.utils.TfsXmlContentNameProvider;
import com.tfsla.opencmsdev.encuestas.Encuesta;
import com.tfsla.opencmsdev.encuestas.ModuloEncuestas;

public abstract class A_TfsEncuestaNoticia extends
		A_TfsNoticiaCollectionWithBlanks {

	CmsObject m_cms = null;
	String style = "default";
	int size = 0;
	String pos = "";
	
	private String position= "";
	
	private boolean hasBodyContent = false;
	
	protected int realIndex = 0;
	
	public A_TfsEncuestaNoticia() {
		super();
	}
	
	protected String getTagNamePolls(){
		String tagNamePolls = TfsXmlContentNameProvider.getInstance().getTagName("news.polls");
		if(tagNamePolls==null) tagNamePolls = TfsXmlContentNameProvider.getInstance().getTagName("news.pools");
		
		return tagNamePolls;
	}
	
	protected String getTagNameKeyControlName(){
	  String keyControlNameTag = TfsXmlContentNameProvider.getInstance().getTagName("news.polls.poll");
      if(keyControlNameTag==null)keyControlNameTag = TfsXmlContentNameProvider.getInstance().getTagName("news.pools.pool");
	
      return keyControlNameTag;
	}
	
	protected String getTagNamePollsPosition(){
		String TagPollsPosition = TfsXmlContentNameProvider.getInstance().getTagName("news.polls.position");
		if(TagPollsPosition==null) TagPollsPosition = TfsXmlContentNameProvider.getInstance().getTagName("news.pools.position");
	   
		return TagPollsPosition;
	}

	@Override
	protected boolean hasMoreContent() {
		index++;

		boolean withElement=false;
		
		while (index<=lastElement && !withElement) {
			I_TfsNoticia noticia;
			try {
				noticia = getCurrentNews();
			} catch (JspTagException e) {
				return false;
			}
			String controlValue = getIndexElementValue(noticia,keyControlName);
			if (!controlValue.trim().equals(""))
			{
				String pos = getIndexElementValue(noticia,"posicion");
				
				if(position.equals("")){
					withElement=true;
				}else{
					if (pos.equals(position))
						withElement=true;
					else if (pos.equals("first") && position.equals("1"))
						withElement=true;
					else if (pos.equals("last") && isInLastPosition())
						withElement=true;
					else
						index++;
				}
			}
			else
				index++;
		}
		
		if (withElement)
			realIndex++;
		
		return (index<=lastElement);
	}

	@Override
	public int getIndex() {
		return realIndex-1;
	}
	
	@Override
	public boolean isLast() {
		boolean withElement=false;

		int indexAux=index;
		indexAux++;
		while (indexAux<=lastElement && !withElement) {
			I_TfsNoticia noticia;
			try {
				noticia = getCurrentNews();
			} catch (JspTagException e) {
				return false;
			}
			String controlValue = getIndexElementValue(noticia,keyControlName,indexAux);
			if (!controlValue.trim().equals(""))
			{
				
				String pos = getIndexElementValue(noticia,getTagNamePollsPosition()); //posicion
				if (pos.equals(position))
					withElement=true;
				else if (pos.equals("first") && position.equals("1"))
					withElement=true;
				else if (pos.equals("last") && isInLastPosition())
					withElement=true;
				else
					index++;
			}
			else
				indexAux++;
		}
		return (indexAux>lastElement);
	}
	
	@Override
	public int doStartTag() throws JspException {
	
		hasBodyContent = false;
		
	    m_cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	
		keyControlName = getTagNameKeyControlName(); //encuesta
	
		init(getTagNamePolls()); //encuestasRelacionadas
		initPosition();
		
		boolean hasContent = hasMoreContent();
		
		
		if (hasContent) {
			return EVAL_BODY_INCLUDE;
		}
		return SKIP_BODY;
	}

	protected void showEncuesta(String urlResource) throws JspException {
	
		TfsIncludeContentUtil includeContent = new TfsIncludeContentUtil(pageContext);
	
		String boxDivId = "poolBox_" + new Date().getTime();
	
		try {
			pageContext.getOut().print("<div  id=\"" + boxDivId + "\" path=\"" + urlResource + "\">");
	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		includeContent.setParameterToRequest("path",urlResource);
		includeContent.setParameterToRequest("id",boxDivId);
		includeContent.setParameterToRequest("style",style);
		
		
		CmsUser user = m_cms.getRequestContext().currentUser();
		String username = null;
		
		try {
			Encuesta encuesta = Encuesta.getEncuestaFromURL(m_cms, urlResource); //ModuloEncuestas.getCompletePath(...)
			if (Encuesta.ACTIVA.equals(encuesta.getEstado()))
			{
				
				//Solo validamos x cookie para saber si mostrar la encuesta abierta o cerrada para no llenar de consultas la BD
				boolean yaVoto = false;
				
				HttpServletRequest req = (HttpServletRequest)pageContext.getRequest();
				Cookie[] cookies = req.getCookies();
				if (cookies != null) {
					for (int i = 0; i < cookies.length; i++) {
						Cookie cookie = cookies[i];
						if (cookie.getName().equals(urlResource) && ModuloEncuestas.COOKIE_NAME.equals(cookie.getValue())) {
							yaVoto = true;
						}
					}
				}
				
				if (yaVoto || (encuesta.isUsuariosRegistrados() && user.getName().equals("Guest")))
					includeContent.includeWithCache("/system/modules/com.tfsla.diario.newsTags/elements/polls/" + style + "/pollClose.jsp");
				else
					includeContent.includeWithCache("/system/modules/com.tfsla.diario.newsTags/elements/polls/" + style + "/pollOpen.jsp");
	
			}
			else
				includeContent.includeWithCache("/system/modules/com.tfsla.diario.newsTags/elements/polls/" + style + "/pollClose.jsp");
	
			 
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		//includeContent.includeWithCache("/system/modules/com.tfsla.diario.newsTags/elements/pools/" + style + "/pool.jsp");
	
		try {
			pageContext.getOut().print("</div>");
	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	protected void includeBoxEncuesta(String urlResource) throws JspException {
		
		TfsIncludeContentUtil includeContent = new TfsIncludeContentUtil(pageContext);
	
		includeContent.setParameterToRequest("path",urlResource);
		includeContent.setParameterToRequest("style",style);
		
		includeContent.includeWithCache("/system/modules/com.tfsla.diario.newsTags/elements/polls/poll.jsp");
	}


	protected abstract void initPosition() throws JspException;

	protected abstract void mostrarEncuesta() throws JspException;
	
	protected abstract boolean isInLastPosition();
	
	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}
	
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}
	
	@Override
	public int doAfterBody() throws JspException {
		
		hasBodyContent = true;
		
		mostrarEncuesta();
		
		if (hasMoreContent()) {
			return EVAL_BODY_AGAIN;
		}
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		try {
			if (!hasBodyContent) while ( hasMoreContent()) mostrarEncuesta();
				
		} catch (JspException e) {
			LOG.error("Error al mostrar encuestas",e);
		}
		
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		
		return EVAL_PAGE;
	}

	protected String getPosition() {
		return position;
	}

	protected void setPosition(String position) {
		this.position = position;
	}

}