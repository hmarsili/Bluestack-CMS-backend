package com.tfsla.rankUsers.jsp;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.opencms.flex.CmsFlexController;

import org.opencms.file.CmsObject;
import org.opencms.jsp.AbstractOpenCmsTag;
import org.opencms.main.CmsException;
import org.opencms.file.CmsUser;
import com.tfsla.rankUsers.service.*;

public class GenericUserHitTag extends AbstractOpenCmsTag {

	private String userName;
	private String counter;
	private String value="1";
	
	private CmsObject cms;
	
	private PageContext pageContext;

	@Override
	public int doStartTag() throws JspException {
		
    	cms = CmsFlexController.getCmsObject(pageContext.getRequest());
    	
    	CmsUser cmsUser;
		try {
			cmsUser = cms.readUser(userName);
			RankService rService = new RankService();
		
			if (cmsUser!=null)
				rService.addUserHit(cmsUser, cms, Integer.parseInt(counter), Integer.parseInt(value));
    	
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	return SKIP_BODY;
	}
	
	public void setPageContext(PageContext arg0) {
		super.setPageContext(arg0);
		this.pageContext = arg0;

	}

	public String getCounter() {
		return counter;
	}

	public void setCounter(String counter) {
		this.counter = counter;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
