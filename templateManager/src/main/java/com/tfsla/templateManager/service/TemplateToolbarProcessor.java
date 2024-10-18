package com.tfsla.templateManager.service;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkManager;

import com.tfsla.diario.toolbar.AbstractButton;
import com.tfsla.diario.toolbar.Menu;
import com.tfsla.diario.toolbar.PrincipalButton;

public class TemplateToolbarProcessor {

	public static final String baseURL = "/system/modules/com.tfsla.diario.newsTags";
	public static final String cssPath = "/resources/css";
	public static final String jsPath = "/resources/js";
	public static final String imgPath = "/resources/img";	
	private static final Log LOG = CmsLog.getLog(TemplateToolbarProcessor.class);

	
	private String projectName;
	
	protected HttpServletRequest req;
	protected HttpServletResponse response;
	protected PageContext page;

	protected Menu menu;
	protected String html = "";
	
	public TemplateToolbarProcessor(PageContext page, HttpServletRequest request, HttpServletResponse response,  
				String project) {
	
		this.req = request;
		this.response = response;
		this.page = page;
		
		projectName = project;

	}

	private void printBeginHTML() {
			html +="<div id=\"ToolbarFooter\">" +
						"<div id=\"ToolbarFooterTop\">" +
							"<a style=\"opacity: 1;\" href=\"#MinimizeFooter\" class=\"expand\"><i class=\"toolbar-icon-arrow\" ></i></a>"+
							//"<a style=\"opacity: 1;\" href=\"#ShowFooter\" class=\"toggle down open\"><span></span></a>"+
						"</div>" +
						"<div id=\"ToolbarFooterBot\">" ;
						
	}

	private void printContainerHTML() {
		for (PrincipalButton button : menu.getButtons()) {
			html += "<div class=\"toolbar-btn-group\">" +
					"<a id=\"toolbar-button\" onclick=\"openSubMenu( toolbarOpen"+button.getName()+")\">" +
					"<i class=\"toolbar-icon-" + button.getName() +"\" ></i></a>"+
					"<ul id=\"toolbar-dropDown\" style=\"display:none\">";
	
				if (button.getSubButtons().size() != 0) {
					for (AbstractButton subButton: button.getSubButtons()) {
						
						html +="<a  style=\"cursor:pointer\" onclick=\"executeMenuOption( toolbar" + button.getName() +",'"+ subButton.getValues() +"');\">"+
								"<li>"+
									"<i class=\"toolbar-icon \"></i> "+ subButton.getName() +
								"</li>" +
								"</a>";
					}
				}
				html +="</ul> " +
				"</div>";
		}
	}
	
	private void printEndHTML() {
		if (projectName.equals("Offline")) 	{
			html += "</div></div>";
		}
	}
	
	public void printHTML(Menu menu) {
		this.menu =  menu;
		if (projectName.equals("Offline")) {			
			CmsFlexController controller = CmsFlexController.getController(req);
			
			try {
				page.getOut().print(
					"<link rel=\"stylesheet\" type=\"text/css\" href=\""
						+ OpenCms.getLinkManager().substituteLinkForUnknownTarget(
								controller.getCmsObject(),
								CmsLinkManager.getAbsoluteUri(baseURL + cssPath + "/toolbar.css", controller.getCurrentRequest().getElementUri()))
						+ "\" />\n"
				);
			
				String jsInclude = OpenCms.getLinkManager().substituteLinkForUnknownTarget(
	    				controller.getCmsObject(),
	    				CmsLinkManager.getAbsoluteUri(baseURL + jsPath + "/toolbar.js", controller.getCurrentRequest().getElementUri()));
				
				page.getOut().print("<script src=\"" + jsInclude + "\" type=\"text/javascript\"></script>");

				printBeginHTML();
				printContainerHTML();
				printEndHTML();
				
				page.getOut().print(html);
			} catch (IOException e) {
				LOG.error(e);
			}
		}
	}
}
