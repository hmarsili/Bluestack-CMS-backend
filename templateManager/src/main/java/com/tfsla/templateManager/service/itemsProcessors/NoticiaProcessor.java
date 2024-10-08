package com.tfsla.templateManager.service.itemsProcessors;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import org.opencms.flex.CmsFlexController;
import org.opencms.staticexport.CmsLinkManager;

import com.tfsla.templateManager.service.A_ItemProcessor;
import com.tfsla.templateManager.service.templateProcessor;

public class NoticiaProcessor extends A_ItemProcessor {

	public NoticiaProcessor()
	{
		itemType = "noticia";
	}
	
	@Override
	public A_ItemProcessor clone() {
		return new NoticiaProcessor();
	}

	@Override
	public void printHTML() {
		try {
			if (project.equals("Offline")) {
				page.getOut().print("<div class=\"dojoDndItem\" dndType=\"" + itemType + "\">");

				page.getOut().print("	<div id=\"tipo\" class=\"" + itemType + "\">");
				page.getOut().print("		<input type=\"hidden\" id=\"param\" name=\"url\" value=\"" + ((String[])parameters.get("url"))[0] + "\" />");
			}
			
        } catch (IOException e) {
        	e.printStackTrace();
        }
        
		try {
			includeItem();
		} catch (JspException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			if (project.equals("Offline")) {
				page.getOut().print("	</div>");
				page.getOut().print("</div>");
			}
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	}

	@Override
	public void printDOJOGlobalConf() {
		if (project.equals("Offline")) {
	        CmsFlexController controller = CmsFlexController.getController(req);
	
	        String target =  templateProcessor.baseURL + templateProcessor.jsPath + "/" + itemType + ".js";
	        target = CmsLinkManager.getAbsoluteUri(target, controller.getCurrentRequest().getElementUri());
	
			try {
				includeNoCache(controller, target, "");
			} catch (JspException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void printDOJOHTML() {
		if (project.equals("Offline")) {
	        CmsFlexController controller = CmsFlexController.getController(req);
	
	        String target =  templateProcessor.baseURL + templateProcessor.includesPath + "/" + itemType + "/" + itemType + ".jsp";
	        target = CmsLinkManager.getAbsoluteUri(target, controller.getCurrentRequest().getElementUri());
	
			try {
				includeNoCache(controller, target, "");
			} catch (JspException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}

}
