package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;

import com.tfsla.diario.utils.TfsVideoHelper;

public class TfsVideoEmbedCodeTag extends A_TfsNoticiaCollectionValue{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5547444449853232772L;

	private String publication = "";
	
	public String getPublication() {
		return publication;
	}

	public void setPublication(String publication) {
		this.publication = publication;
	}
	
	@Override
	 public int doStartTag() throws JspException {

		 I_TfsCollectionListTag collection = getCurrentCollection();
		 
		 String videoPath = "";
		if ( collection instanceof  TfsVideoFSTag){
			videoPath = ((TfsVideoFSTag)collection).getPath();
		} else if ( collection instanceof  TfsVideosListTag){
			videoPath = ((TfsVideosListTag)collection).getVideo().getVfspath();
		} 
		CmsObject cmsObject = CmsFlexController.getCmsObject(pageContext.getRequest());
			
		String content = TfsVideoHelper.getVideoEmbedCode( videoPath, cmsObject, publication);
	    printContent(content);

		return SKIP_BODY;
	 }
	
}	