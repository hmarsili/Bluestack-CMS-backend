package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

public class TfsInternationalizationMessage extends  A_TfsMessagesValueTag {
	
	private static final long serialVersionUID = -4423049087057047554L;
	
	private String key = null;
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	@Override
	 public int doStartTag() throws JspException {

		I_TfsMessages messages = getMessages();
		
		String content = messages.getMessages().keyDefault(key, key);
		printContent(content);

		return SKIP_BODY;
	 }

}
