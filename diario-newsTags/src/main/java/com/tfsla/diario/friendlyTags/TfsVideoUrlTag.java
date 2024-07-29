package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;

import com.tfsla.diario.utils.TfsVideoHelper;

public class TfsVideoUrlTag extends A_TfsNoticiaCollectionValue{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5547444449853232772L;

	private String publicUrl = "false";
	private String publication = "";
	
	public String getPublicUrl() {
		return publicUrl;
	}

	public void setPublicUrl(String publicUrl) {
		this.publicUrl = publicUrl;
	}
	
	
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
			
		boolean urlP = publicUrl.equals("false")?false:true;
		String content = TfsVideoHelper.getVideoUrlHelper( videoPath, cmsObject, urlP, publication);
	    printContent(content);

		return SKIP_BODY;
	 }
}
