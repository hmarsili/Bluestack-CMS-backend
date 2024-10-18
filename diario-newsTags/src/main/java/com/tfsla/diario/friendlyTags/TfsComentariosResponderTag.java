package com.tfsla.diario.friendlyTags;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

import com.tfsla.diario.utils.TfsHashGenerator;


public class TfsComentariosResponderTag  extends TagSupport {

	private String url = null;
	private String parentid = "0";

	/**
	 * 
	 */
	private static final long serialVersionUID = -7920103061508683666L;

    @Override
    public int doStartTag() throws JspException {
    	
		String commentUrl = url;

		String formDivId = TfsHashGenerator.getHash(commentUrl + parentid);

		formDivId = "cmt_form_" + formDivId;

    	try {
			pageContext.getOut().print("<a href=\"javascript:showCommentForm('" + formDivId + "')\"> Responder</a>");

		} catch (IOException e) {
			throw new JspException(e);
		}
		
		return SKIP_BODY;
    }

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getParentid() {
		return parentid;
	}

	public void setParentid(String parentid) {
		this.parentid = parentid;
	}

}
