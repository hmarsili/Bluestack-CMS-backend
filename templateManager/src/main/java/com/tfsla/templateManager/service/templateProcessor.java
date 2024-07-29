package com.tfsla.templateManager.service;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.ediciones.model.Zona;
import com.tfsla.diario.ediciones.services.ZonasService;

public class templateProcessor {

	public static final String baseURL = "/system/modules/com.tfsla.templateManager";
	public static final String cssPath = "/resources/css";
	public static final String jsPath = "/resources/js";
	public static final String imgPath = "/resources/img";	
	public static final String builderPath = "/elements/PageBuilder";
	public static final String includesPath = "/elements/IncludeTypes";
	public static final String genericConfiguration = "/generic";

	private String currentPage;
	private String projectName;

	protected HttpServletRequest req;
	protected HttpServletResponse response;
	protected PageContext page;
	
	protected boolean isTemplateManagerUser = false;
	protected boolean isTemplateManagerAdmin = false;

	private List<ContainerProcessor> containers = new ArrayList<ContainerProcessor>();
	private List<ConfigurationTemplateProcessor> configurationTemplates = new ArrayList<ConfigurationTemplateProcessor>();
	private List<String> cssIncludePaths;
	private List<String> typesIncluded = new ArrayList<String>();
	
	public templateProcessor(PageContext page, HttpServletRequest request, HttpServletResponse response, String templatePage, String project, boolean isTemplateManagerUser, boolean isTemplateManagerAdmin)
	{
		this.req = request;
		this.response = response;
		this.page = page;
		
		this.isTemplateManagerUser = isTemplateManagerUser;
		this.isTemplateManagerAdmin = isTemplateManagerAdmin;

		currentPage = templatePage;
		projectName = project;
	}
	
	public void preProcessPage()
	{

        CmsFlexController controller = CmsFlexController.getController(req);
        CmsObject cms = controller.getCmsObject();

        CmsFile templateFile;
		try {
			templateFile = cms.readFile(currentPage);
			CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, templateFile);
	
			content.setAutoCorrectionEnabled(true); 
			content.correctXmlStructure(cms);

			int nroCont=1;
			String xmlName ="contenedor[" + nroCont + "]";
			I_CmsXmlContentValue value = content.getValue(xmlName, Locale.ENGLISH);
			while (value!=null)
			{
				ContainerProcessor cont = new ContainerProcessor();
				cont.setPage(page);
				cont.setReq(req);
				cont.setResponse(response);
				cont.setProject(projectName);
				cont.preProcess(cms,content, nroCont);
				
				containers.add(cont);
				
				for (String itemType : cont.getAllowedTypes())
					if (!typesIncluded.contains(itemType))
						typesIncluded.add(itemType);
				
				nroCont++;
				xmlName ="contenedor[" + nroCont + "]";
				value = content.getValue(xmlName, Locale.ENGLISH);
			}
			
			if (projectName.equals("Offline")){
				nroCont=1;
				xmlName ="configurationTemplate[" + nroCont + "]";
				value = content.getValue(xmlName, Locale.ENGLISH);
				while (value!=null)
				{
					ConfigurationTemplateProcessor containerTemplate = new ConfigurationTemplateProcessor();
					containerTemplate.preProcess(cms,content,nroCont);
					
					configurationTemplates.add(containerTemplate);
	
					nroCont++;
					xmlName ="configurationTemplate[" + nroCont + "]";
					value = content.getValue(xmlName, Locale.ENGLISH);
				}
			}
			
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void printCssStyles()
	{
        CmsFlexController controller = CmsFlexController.getController(req);
		
		/*cssIncludePaths = new ArrayList<String>();
		
 		if (projectName.equals("Offline"))
		{
			for (ContainerProcessor cp : containers)
			{
				for (String conf : cp.getAllowedConfigurations())
				{
					String css = baseURL + cssPath + "/" + conf + "/" + conf + ".css";
					if (!cssIncludePaths.contains(css))
						cssIncludePaths.add(css);
					
					for (String itemType : cp.getAllowedTypes())
					{
						css = baseURL + cssPath + "/" + conf + "/" + itemType + ".css";
						if (!cssIncludePaths.contains(css))
							cssIncludePaths.add(css);
						
						css = baseURL + cssPath + "/boxes/" + itemType + ".css";
						if (!cssIncludePaths.contains(css))
							cssIncludePaths.add(css);						
					}
					
				}
			}
		}
 		else
		{	
			for (ContainerProcessor cp : containers)
			{
				String conf = cp.getConfiguration();

				String css = baseURL + cssPath + "/" + conf + "/" + conf + ".css";
				if (!cssIncludePaths.contains(css))
					cssIncludePaths.add(css);

				for (String itemType : cp.getSelectedItemsTypes())
				{
					css = baseURL + cssPath + "/" + conf + "/" + itemType + ".css";
					if (!cssIncludePaths.contains(css))
						cssIncludePaths.add(css);
				}
			}
		}
 		*/
 		for (ContainerProcessor cp : containers)
 		{
 			if(cp.isHide() || projectName.equals("Offline")){
	 			try{
	 				page.getOut().print(
	 						"	<style type=\"text/css\">"
	 						+ "	.dndHidden {"
	 						+ "		visibility: hidden;"
	 						+ "		overflow:hidden;"
	 						+ "		display:none;"
	 						+ "  	}"
	 						+ "	.dndHidden * {"
	 						+ "	visibility: hidden;"
	 						+ "	overflow:hidden;"
	 						+ "	display:none;"
	 						+ "	}"			
	 						+ "	</style>"
	 				);
	 				
	 				break;
	 				
	 			} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
 			}
 		}	
 		String paths = "";
		/*
		if (projectName.equals("Offline")) {
			
			for (String path : cssIncludePaths)
			{
				if (controller.getCmsObject().existsResource(path))
			        paths += 
			        	"<link rel=\"stylesheet\" type=\"text/css\" href=\""
			        		+ OpenCms.getLinkManager().substituteLinkForUnknownTarget(
			        				controller.getCmsObject(),
			        				CmsLinkManager.getAbsoluteUri(path, controller.getCurrentRequest().getElementUri()))
			        		+ "\" />\n";
	
			}
		}*/
		if (projectName.equals("Offline"))
		{	
			try {
				//page.getOut().print("<link rel=\"stylesheet\" type=\"text/css\" href=\"http://ajax.googleapis.com/ajax/libs/dojo/1.5/dijit/themes/claro/claro.css\"/>\n");				
				
				page.getOut().print(
		        	"<link rel=\"stylesheet\" type=\"text/css\" href=\""
		        		+ OpenCms.getLinkManager().substituteLinkForUnknownTarget(
		        				controller.getCmsObject(),
		        				CmsLinkManager.getAbsoluteUri(baseURL + cssPath + "/design.css", controller.getCurrentRequest().getElementUri()))
		        		+ "\" />\n"
				);
				
				page.getOut().print(
				   "<link rel=\"stylesheet\" type=\"text/css\" href=\""
				    	+ OpenCms.getLinkManager().substituteLinkForUnknownTarget(
				   			controller.getCmsObject(),
				    			CmsLinkManager.getAbsoluteUri(baseURL + cssPath + "/claro.css", controller.getCurrentRequest().getElementUri()))
				    	+ "\" />\n"
				);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			page.getOut().print(paths);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void printBeginHTML()
	{
		try {

			if (projectName.equals("Offline"))
			page.getOut().print("<div id=\"dragLists\">");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public void PrintContainerHTML(String name)
	{
		ContainerProcessor cp = new ContainerProcessor(name);
		
		int idx = containers.indexOf(cp);
		
		if (idx!=-1)
			containers.get(idx).printHTML();
	}
	
	public void printEndHTML()
	{
		try {

			if (projectName.equals("Offline"))
			{
				page.getOut().print("<div class=\"clear\"></div>");
				page.getOut().print("</div>");
							
				for (String itemType : typesIncluded)
				{
					A_ItemProcessor itemP = ItemProcessorProvider.getItemProcessor(itemType);
					itemP.setPage(page);
					itemP.setReq(req);
					itemP.setResponse(response);
					itemP.setProject(projectName);
					itemP.printDOJOHTML();
				}
				
				ConfigurationTemplateProcessor configurationTemplateProcessor = new ConfigurationTemplateProcessor();
				configurationTemplateProcessor.setPage(page);
				configurationTemplateProcessor.setResponse(response);
				configurationTemplateProcessor.setReq(req);
				configurationTemplateProcessor.setProject(projectName);
				configurationTemplateProcessor.printDOJOHTML();

				page.getOut().print(getDOJOHTML());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public void printHTML()
	{	 
		printBeginHTML();
		
		for (ContainerProcessor cp : containers)
			cp.printHTML();
		
		printEndHTML();
	}
	
	public void printJS()
	{
		if (projectName.equals("Offline"))
		{	
			try {
				page.getOut().print("<script src=\"//ajax.googleapis.com/ajax/libs/dojo/1.5/dojo/dojo.xd.js\" type=\"text/javascript\" djConfig=\"isDebug:false, parseOnLoad: true\"></script>");
								
		        CmsFlexController controller = CmsFlexController.getController(req);

		        //Add DNDConfig.js
				String jsInclude = OpenCms.getLinkManager().substituteLinkForUnknownTarget(
        				controller.getCmsObject(),
        				CmsLinkManager.getAbsoluteUri(baseURL + jsPath + "/dndConfig.js", controller.getCurrentRequest().getElementUri()));
				
				page.getOut().print("<script src=\"" + jsInclude + "\" type=\"text/javascript\"></script>");
				
				page.getOut().print("<script type=\"text/javascript\">");
								
				//GLOBAL_PATH
				String jsGlobalPath = OpenCms.getLinkManager().substituteLinkForUnknownTarget(
        				controller.getCmsObject(),
        				CmsLinkManager.getAbsoluteUri("/", controller.getCurrentRequest().getElementUri()));

				page.getOut().print("\nvar GLOBAL_PATH = \"" + jsGlobalPath + "\";");
								
				//GLOBAL_CURRENT_PAGE
				page.getOut().print("\nvar GLOBAL_CURRENT_PAGE = \"" + currentPage + "\";");
								
				page.getOut().print(getDOJORequire());
				page.getOut().print(getDOJOGlobalConf());
				
				for (ContainerProcessor cp : containers)
					cp.printJS();
				
				for (String itemType : typesIncluded)
				{
					A_ItemProcessor itemP = ItemProcessorProvider.getItemProcessor(itemType);
					itemP.setPage(page);
					itemP.setReq(req);
					itemP.setResponse(response);
					itemP.setProject(projectName);
					itemP.printDOJOGlobalConf();
				}
				
				ConfigurationTemplateProcessor configurationTemplateProcessor = new ConfigurationTemplateProcessor();
				configurationTemplateProcessor.setPage(page);
				configurationTemplateProcessor.setResponse(response);
				configurationTemplateProcessor.setReq(req);
				configurationTemplateProcessor.setProject(projectName);
				configurationTemplateProcessor.printDOJOGlobalConf();				

				page.getOut().print("</script>");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}

	}
	
	private String getDOJORequire() {
		String include =
			"\ndojo.registerModulePath(\"dojo\", \"//ajax.googleapis.com/ajax/libs/dojo/1.5.2/dojo\");\n"+
			"dojo.require(\"dojo.dnd.Container\");\n" +
			"dojo.require(\"dojo.dnd.Manager\");\n" +
			"dojo.require(\"dojo.dnd.Source\");\n" +
			"dojo.require(\"dojo.parser\");\n"+
			"dojo.registerModulePath(\"dijit\", \"//ajax.googleapis.com/ajax/libs/dojo/1.5.2/dijit\");\n"+
			"dojo.require(\"dijit.MenuSeparator\");\n" +
			"dojo.require(\"dijit.MenuItem\");\n" +
			"dojo.require(\"dijit.PopupMenuItem\");\n" +
			"dojo.require(\"dijit.form.Button\");\n" +
			"dojo.require(\"dijit.Dialog\");\n" +
			"dojo.require(\"dijit.form.TextBox\");\n" +
			"dojo.require(\"dijit.form.FilteringSelect\");\n" +
			"dojo.require(\"dijit.form.ComboBox\");\n" +
			"dojo.require(\"dijit.layout.ContentPane\");\n" +
			"dojo.require(\"dijit.form.CheckBox\");\n" +
			"dojo.require(\"dijit.Menu\");\n" +
			"dojo.registerModulePath(\"dojox\", \"//ajax.googleapis.com/ajax/libs/dojo/1.5.2/dojox\");\n"+
			"dojo.require(\"dojox.data.XmlStore\");\n" +			
			"dojo.require(\"dojox.xml.parser\");\n" +
			"dojo.require(\"dojox.layout.FloatingPane\");\n";
		return include;
	}

	private String getDOJOGlobalConf()
	{
		String include = 
			"	var currentcointeiner;" +

			"  dojo.addOnLoad(function() {" +
			"		dijit.byId('dDropBox').show();" + 
			"		dijit.byId('dTrashBox').show();" +
			"	  });";

		return include;
	}
	
	private String getDOJOHTML() {
		
		CmsFlexController controller = CmsFlexController.getController(req);
		
		String include = "";
		
		if(isTemplateManagerAdmin || isTemplateManagerUser){
		
			if(isTemplateManagerAdmin){
				include +=
					"<div dojoType=\"dojox.layout.FloatingPane\" id=\"dDropBox\" closable=\"false\" title=\"Drop Box\" resizable=\"true\" dockable=\"true\" style=\"position:absolute;top:0;left:200;width:200px;height:200px;visibility:hidden;\">" +
					"	<div id=\"box\" class=\"boxes\" style=\"float: left; \">" +
					"		<div dojoType=\"dojo.dnd.Source\" autoSync=\"true\" id=\"dropBox\" jsId=\"dropBox\" class=\"dndContainer, boxes\">" +
					"		</div>" +
					"	</div>" +
					"</div>";
			}
			
			if(isTemplateManagerAdmin){
			
				include += 
					"<div dojoType=\"dojox.layout.FloatingPane\" id=\"dZoneBox\" closable=\"false\" title=\"Zone Box\" resizable=\"true\" dockable=\"true\" style=\"position:absolute;top:0;left:200;width:200px;height:200px;visibility:hidden;\">" +
					"	<div id=\"box\" class=\"boxes\" maxItems=\"100\" style=\"float: left; \">" +
					" 		<div dojoType=\"dojo.dnd.Source\" autoSync=\"true\" accept=\"noticia\" id=\"zoneBox\" jsId=\"zoneBox\" class=\"dndContainer, boxes\"> " +
					
					"		<br /><label for=\"texto\">&nbsp;Zone:</label>" +
					
		        	"		<select dojoType=\"dijit.form.FilteringSelect\" autocomplete=\"true\" id=\"zoneBoxSelect\" name=\"zoneBoxSelect\">";
				
							ZonasService zoneService = new ZonasService();
							
							for (java.util.Iterator iter = zoneService.obtenerZonas(1,1).iterator(); iter.hasNext();) {
								Zona zone = (Zona)iter.next();						
								include += "<option value=\"" + zone.getName() + "\">" + zone.getDescription() + "</option>";
							}
			
			include +=
	        	"		</select>" +
	        	
	        	"		<br /><br /><img alt=\"\" src=\"" +
	        			OpenCms.getLinkManager().substituteLinkForUnknownTarget(
	        			controller.getCmsObject(),
	        			CmsLinkManager.getAbsoluteUri(baseURL + imgPath + "/drag.png", controller.getCurrentRequest().getElementUri())) + "\" />" +        	
				
				"			<script type=\"dojo/method\" event=\"onDropExternal\" args=\"_1a,_1b,_1c\">" +
				"				dropExternal(this,_1a,_1b,_1c);" +			
				"			</script>" +			
				"		</div>" +
				"	</div>" +
				"</div>";
			
			}
			
			if(isTemplateManagerAdmin){
			
				include +=
					"<div dojoType=\"dojox.layout.FloatingPane\" id=\"dTrashBox\" closable=\"false\" title=\"Trash Box\" resizable=\"true\" dockable=\"true\" style=\"position:absolute;top:0;left:200;width:200px;height:200px;visibility:hidden;\">" +
					"	<div id=\"box\" class=\"boxes\" maxItems=\"100\" style=\"float: left; \">" +
					" 		<div dojoType=\"dojo.dnd.Source\" autoSync=\"true\" accept=\"" + StringUtils.join(typesIncluded, ",") + "\" id=\"trashBox\" jsId=\"trashBox\" class=\"dndContainer, boxes\"> " +
					
		        	"		<br /><img alt=\"\" src=\"" +
		    					OpenCms.getLinkManager().substituteLinkForUnknownTarget(
		    					controller.getCmsObject(),
		    					CmsLinkManager.getAbsoluteUri(baseURL + imgPath + "/trash.png", controller.getCurrentRequest().getElementUri()))
		    		+ 		"\" />" +
					
					"			<script type=\"dojo/method\" event=\"onDropExternal\" args=\"_1a,_1b,_1c\">" +
					"				dropExternal(this,_1a,_1b,_1c);" +
					"			</script>" +			
					"		</div>" +
					"	</div>" +
					"</div>";
			}
	
			include +=
				"<div dojoType=\"dojox.layout.FloatingPane\" id=\"dTools\" closable=\"false\" title=\"Tools\" resizable=\"true\" dockable=\"true\" style=\"position:absolute;top:0;left:0;width:200px;height:150px;visibility:hidden;\">" +
				"	<div dojoType=\"dijit.Menu\" id=\"navMenu\" style=\"width:100%;\">" +
				
				//"		<div " + (isTemplateManagerUser ? "disabled" : "") + " dojoType=\"dijit.MenuItem\" iconClass=\"dijitEditorIcon dijitEditorIconSave\"" +
				// Template Manager User ahora tambien podra guardar cambios || Esteban 3/12/2015
				"		<div dojoType=\"dijit.MenuItem\" iconClass=\"dijitEditorIcon dijitEditorIconSave\"" +
				"    		onClick=\"saveConfiguration(false);\">"+
				"        	Save"+
				"    	</div>"+
				//"		<div " + (isTemplateManagerUser ? "disabled" : "") + " dojoType=\"dijit.MenuItem\" iconClass=\"dijitEditorIcon dijitEditorIconIndent\"" +
				// Template Manager User ahora tambien podra guardar y publicar cambios || Esteban 3/12/2015
				"		<div dojoType=\"dijit.MenuItem\" iconClass=\"dijitEditorIcon dijitEditorIconIndent\"" +
				"    		onClick=\"saveConfiguration(true);\">"+
				"        	Save & Publish"+
				"    	</div>"+
				
				"		<div dojoType=\"dijit.MenuSeparator\"></div>"+			
				
				"		<div " + (isTemplateManagerUser ? "disabled" : "") + " dojoType=\"dijit.PopupMenuItem\">"+
				"    		<span>"+
				"        		Add Item As"+
				"    		</span>"+
				"    		<div dojoType=\"dijit.Menu\" id=\"smItemsAdd\">"+
				"    		</div>"+
				"		</div>"+
				
				"		<div dojoType=\"dijit.MenuSeparator\"></div>"+		
				
				"		<div " + (isTemplateManagerUser ? "disabled" : "") + " dojoType=\"dijit.PopupMenuItem\">"+
				"    		<span>"+
				"        		Template Generator"+
				"    		</span>"+
				"    		<div dojoType=\"dijit.Menu\" id=\"smItemTemplates\">"+
				"				<div dojoType=\"dijit.MenuItem\" id=\"smMenuItemTemplateSave\" disabled onClick=\"saveConfigurationTemplateName();\">Save</div>" +
				"				<div dojoType=\"dijit.MenuItem\" id=\"smMenuItemTemplateSaveAs\" onClick=\"saveAsConfigurationTemplateName();\">Save As</div>" +			
				"			</div>" +
				"		</div>" +
				
				"		<div dojoType=\"dijit.PopupMenuItem\">"+
				"    		<span>"+
				"        		Templates"+
				"    		</span>"+
				"    		<div dojoType=\"dijit.Menu\" id=\"smItemSavedViewsTemplates\">";		
			
							int nroConfigTemplates = 1;
							for (ConfigurationTemplateProcessor configurationTemplate : configurationTemplates){
								include += "	<div dojoType=\"dijit.MenuItem\" id=\"smMenuItemTemplateItem_" + configurationTemplate.getName() + "\" onClick=\"applyConfigurationTemplate(" + nroConfigTemplates + ");\" \">" + configurationTemplate.getName() + "</div>";
								nroConfigTemplates++;
							}
				include +=
				"    		</div>"+			
				"		</div>"+			
				
				"		<div dojoType=\"dijit.MenuSeparator\"></div>"+			
				
				"    	<div " + (isTemplateManagerUser ? "disabled" : "") + " dojoType=\"dijit.PopupMenuItem\" iconClass=\"dijitIconUndo\">"+
				"        	<span>"+
				"            	Restore hidden containers"+
				"         	</span>"+
				"        	<div dojoType=\"dijit.Menu\" id=\"smRestore\">";
				
							for (ContainerProcessor container : containers)
								include += "	<div dojoType=\"dijit.MenuItem\" id=\"" + container.getName() + "\" onClick=\"disabledMenuClick(this);\" " + (!container.isHide() ? " disabled " : "") + ">" + container.getName() + "</div>";
			
				include += 
				"			</div>"+
				"    	</div>"+
				"	</div>"+
				"</div>";			
		}
		
		return include;
	}
}
