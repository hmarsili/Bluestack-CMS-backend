package com.tfsla.templateManager.service.itemsProcessors;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.jsp.JspException;

import org.opencms.flex.CmsFlexController;
import org.opencms.staticexport.CmsLinkManager;

import com.tfsla.templateManager.service.A_ItemProcessor;
import com.tfsla.templateManager.service.templateProcessor;

public class IncludePageProcessor extends A_ItemProcessor {

	public IncludePageProcessor()
	{
		itemType = "includePage";
	}
	
	@Override
	public A_ItemProcessor clone() {
		return new IncludePageProcessor();
	}

	@Override
	public void printHTML() {
		try {
			if (project.equals("Offline")) {
				page.getOut().print("<div class=\"dojoDndItem\" dndType=\"" + itemType + "\">");

				page.getOut().print("	<div id=\"tipo\" class=\"" + itemType + "\">");
				
				for (Iterator it = this.parameters.keySet().iterator();it.hasNext();)
				{
					String param = (String) it.next();
					page.getOut().print("		<input type=\"hidden\" id=\"param\" name=\"" + param + "\" value=\"" + ((String[])parameters.get(param))[0] + "\" />");
					
				}
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
