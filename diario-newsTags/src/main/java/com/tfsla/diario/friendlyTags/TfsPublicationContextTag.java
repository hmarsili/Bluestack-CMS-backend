package com.tfsla.diario.friendlyTags;

import java.util.Scanner;

import jakarta.servlet.jsp.JspException;

import com.tfsla.diario.model.TfsEdicion;
import com.tfsla.diario.model.TfsPublicacion;

public class TfsPublicationContextTag extends BaseTag {

	private String edition=null;
	private String publication=null;
	
	private TfsEdicion edicion = null;
	private TfsPublicacion publicacion = null;
	
	@Override
	public int doStartTag() throws JspException {
		
		init();
		
		if (publication!=null)
			publicacion = new TfsPublicacion(Integer.parseInt(publication));
		else
			publicacion = new TfsPublicacion(m_cms,m_cms.getRequestContext().getUri());

		if (!publicacion.isIsonline()) {
			if (edition==null || edition.toLowerCase().trim().equals("current"))
				edicion = new TfsEdicion(m_cms,m_cms.getRequestContext().getUri());
			else if (edition.toLowerCase().trim().equals("active"))
				edicion = new TfsEdicion(publicacion.getId(), publicacion.getActiveedition());
			else {
				Scanner scanner = new Scanner(publication);   
				if (scanner.hasNextInt())   
					edicion = new TfsEdicion(publicacion.getId(), scanner.nextInt());
				else
					edicion = new TfsEdicion(m_cms,m_cms.getRequestContext().getUri());
	
			}
		}
		else
			edicion = TfsEdicion.EMPTY;
			
		
		exposeContextInformation();
		
		return EVAL_BODY_INCLUDE;
	}
   
	protected void exposeContextInformation()
	{
		pageContext.getRequest().setAttribute("edition", edicion );
		pageContext.getRequest().setAttribute("publication", publicacion);
	}

	public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

	public String getPublication() {
		return publication;
	}

	public void setPublication(String publication) {
		this.publication = publication;
	}
}
