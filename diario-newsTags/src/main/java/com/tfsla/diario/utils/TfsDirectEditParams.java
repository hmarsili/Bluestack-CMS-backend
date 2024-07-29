package com.tfsla.diario.utils;

import org.opencms.configuration.CmsMedios;
import org.opencms.workplace.editors.directedit.CmsDirectEditButtonSelection;
import org.opencms.workplace.editors.directedit.CmsDirectEditMode;
import org.opencms.workplace.editors.directedit.CmsDirectEditParams;
import org.opencms.configuration.CPMConfig;

public class TfsDirectEditParams extends CmsDirectEditParams {
	
	String LinkEditor = "/system/modules/com.tfsla.diario.admin/templates/editNews.jsp";
	String LinkForNew = "javascript:newArticle();";
	private CPMConfig config;
	
	public TfsDirectEditParams(String resourceName,
			CmsDirectEditButtonSelection options, CmsDirectEditMode mode,
			String linkForNew, String backlink, String domain) {
		
		super(resourceName, options, mode, linkForNew);
		
		config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		String protocolNavigation = config.getParam("", "", "newsPublisher", "protocolNavigation");
		
		if (protocolNavigation == null || protocolNavigation.equals("")) {
			protocolNavigation = "//"; 
		} else {
			protocolNavigation = protocolNavigation + "://";
		}
		
		setLinkForEdit(protocolNavigation + domain + this.LinkEditor+"?url="+resourceName+"&backlink="+backlink);
	}

	@Override
	public String getLinkForEdit() {

        return this.LinkEditor;
    }

	public void setLinkForEdit(String linkEditor) {

        this.LinkEditor = linkEditor;
    }
	
	@Override
	public String getLinkForNew() {
		 return LinkForNew;
	}


}