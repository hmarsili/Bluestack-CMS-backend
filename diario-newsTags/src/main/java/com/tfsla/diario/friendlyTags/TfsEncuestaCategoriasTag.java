package com.tfsla.diario.friendlyTags;

import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;

import org.opencms.main.OpenCms;

public class TfsEncuestaCategoriasTag  extends BodyTagSupport implements I_TfsCollectionListTag {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2023576617079313660L;
	
	private int idx = -1;
	private List<String> categorias = null;
	
	@Override
	public int doStartTag() throws JspException {

		 idx = -1;
		
		 I_TfsEncuesta encuesta = getCurrentEncuesta();
		 
		 if (encuesta!=null & encuesta.getEncuesta()!=null)
			 categorias = encuesta.getEncuesta().getcategorias();
		 
		 return (hasMoreContent() ? EVAL_BODY_INCLUDE : SKIP_BODY );
	}

	protected I_TfsEncuesta getCurrentEncuesta() throws JspTagException {
		// get a reference to the parent "content container" class
	    Tag ancestor = findAncestorWithClass(this, I_TfsEncuesta.class);
	    if (ancestor == null) {
	        throw new JspTagException("Tag Poll not accesible");
	    }
	    
	    I_TfsEncuesta encuesta = (I_TfsEncuesta) ancestor;
		return encuesta;
	}
	
	private boolean hasMoreContent()
	{
		
		if (categorias==null)
			return false;
		
		idx++;
		
		if (idx==categorias.size())
			return false;
		
		return true;
	}
	
	@Override
	public int doAfterBody() throws JspException {

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

		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return EVAL_PAGE;
	}

	public int getIndex() {
		return idx;
	}

	public boolean isLast() {
		return (idx==categorias.size());
	}

	public String getValue()
	{
		return categorias.get(idx);
	}
	
	public int getSize()
	{
		return categorias.size();
	}
	
	public String getCollectionValue(String name) throws JspTagException {
		return categorias.get(idx);
	}
	
	public String getCollectionIndexValue(String name, int index)
			throws JspTagException {
		return null;
	}

	public int getCollectionIndexSize(String name, boolean isCollectionPart) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getCollectionPathName() throws JspTagException {
		// TODO Auto-generated method stub
		return "";
	}

}
