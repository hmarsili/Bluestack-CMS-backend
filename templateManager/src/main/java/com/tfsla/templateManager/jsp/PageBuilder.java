package com.tfsla.templateManager.jsp;	

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.opencms.file.CmsRequestContext;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.jsp.CmsJspLoginBean;
import org.opencms.main.CmsException;


import com.tfsla.templateManager.service.templateProcessor;

public class PageBuilder extends CmsJspActionElement {

	private String currentPage;
	private String projectName;
	boolean isTemplateManagerAdmin = false;
	boolean isTemplateManagerUser = false;	
	
	templateProcessor tp;
	
	public PageBuilder(PageContext page, HttpServletRequest request, HttpServletResponse response) {
		
		super(page, request, response);
		
		try{
			CmsRequestContext reqContext = getCmsObject().getRequestContext();
			currentPage = reqContext.getUri();		
			projectName = reqContext.currentProject().getName();
			
			if (projectName.equals("Offline")){
				CmsFlexController controller = CmsFlexController.getController(request);
				CmsJspLoginBean loginBean = new CmsJspLoginBean(page, request, response);
				String userName = loginBean.getUserName();				
	        
				isTemplateManagerAdmin = controller.getCmsObject().userInGroup(userName, "/TemplateManagerAdmin");
				isTemplateManagerUser = controller.getCmsObject().userInGroup(userName, "/TemplateManagerUser");
			}
		
			tp = new templateProcessor(page,request,response,currentPage,projectName,isTemplateManagerUser,isTemplateManagerAdmin);
			tp.preProcessPage();
		
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
		
	public void printCssStyles(){
		tp.printCssStyles();
	}
	
	public void printHTML()
	{
		tp.printHTML();
	}
	
	public void printContainerHTML(String name)
	{
		tp.PrintContainerHTML(name);
	}
	
	public void printHeaderHTML()
	{
		tp.printBeginHTML();
	}

	public void printFooterHTML()
	{
		tp.printEndHTML();
	}

	public void printJS()
	{
		tp.printJS();
	}
}
