package com.tfsla.templateManager.service;

import java.io.IOException;
import java.util.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;

import org.opencms.file.CmsObject;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.types.I_CmsXmlContentValue;

public class ContainerProcessor {

	protected HttpServletRequest req;
	protected HttpServletResponse response;
	protected PageContext page;
	protected String project;
	
	private String name;
	private Boolean hide; 
	private String configuration;
	private int maxItems;
	
	private List<String> allowedTypes = new ArrayList<String>();
	private List<String> allowedConfigurations = new ArrayList<String>();
	private List<A_ItemProcessor> items = new ArrayList<A_ItemProcessor>();

	public ContainerProcessor()
	{
		
	}

	public ContainerProcessor(String name)
	{
		this.name = name;
	}

	public void preProcess(CmsObject cms, CmsXmlContent content, int nroCont) {

		String xmlName ="contenedor[" + nroCont + "]/name[1]";
		I_CmsXmlContentValue value = content.getValue(xmlName, Locale.ENGLISH);
		name = value.getStringValue(cms);
		
		xmlName ="contenedor[" + nroCont + "]/hide[1]";
		value = content.getValue(xmlName, Locale.ENGLISH);
		hide = Boolean.valueOf(value.getStringValue(cms));		

		xmlName ="contenedor[" + nroCont + "]/configuration[1]";
		value = content.getValue(xmlName, Locale.ENGLISH);
		configuration = value.getStringValue(cms);
		
		xmlName ="contenedor[" + nroCont + "]/maxItems[1]";
		value = content.getValue(xmlName, Locale.ENGLISH);
		String sMaxItems = value.getStringValue(cms);
		
		try {
			maxItems = Integer.parseInt(sMaxItems);
		}
		catch (NumberFormatException ex)
		{
			maxItems = Integer.MAX_VALUE;
		}

		int nro=1;
		xmlName ="contenedor[" + nroCont + "]/tipo[" + nro + "]";
		value = content.getValue(xmlName, Locale.ENGLISH);
		while (value!=null)
		{
			String tipo = value.getStringValue(cms);
			allowedTypes.add(tipo);
			
			nro++;
			xmlName ="contenedor[" + nroCont + "]/tipo[" + nro + "]";
			value = content.getValue(xmlName, Locale.ENGLISH);
		}

		nro=1;
		xmlName ="contenedor[" + nroCont + "]/allowedConfiguration[" + nro + "]";
		value = content.getValue(xmlName, Locale.ENGLISH);
		while (value!=null)
		{

			String allowedConfiguration = value.getStringValue(cms);
			allowedConfigurations.add(allowedConfiguration);
			
			nro++;
			xmlName ="contenedor[" + nroCont + "]/allowedConfiguration[" + nro + "]";
			value = content.getValue(xmlName, Locale.ENGLISH);
		}
		
		nro=1;
		xmlName ="contenedor[" + nroCont + "]/items[" + nro + "]";
		value = content.getValue(xmlName, Locale.ENGLISH);
		while (value!=null)
		{
			xmlName ="contenedor[" + nroCont + "]/items[" + nro + "]/tipo[1]";
			value = content.getValue(xmlName, Locale.ENGLISH);
			String tipo = value.getStringValue(cms);
			
			A_ItemProcessor item = ItemProcessorProvider.getItemProcessor(tipo);
			item.setPage(page);
			item.setReq(req);
			item.setResponse(response);
			item.setProject(project);
			item.setRootPath(templateProcessor.baseURL + templateProcessor.builderPath + "/" + configuration);
			
			int nroParam = 1;

			Map<String,String[]> parameters = new HashMap<String,String[]>();
			xmlName = "contenedor[" + nroCont + "]/items[" + nro + "]/parametros[" + nroParam + "]/nombre[1]";
			value = content.getValue(xmlName, Locale.ENGLISH);
			
			while (value!=null)
			{
				String nombre = value.getStringValue(cms);

				xmlName ="contenedor[" + nroCont + "]/items[" + nro + "]/parametros[" + nroParam + "]/valor[1]";
				value = content.getValue(xmlName, Locale.ENGLISH);
				String valor = value.getStringValue(cms);

				String valores[] = new String[1];
				valores[0] = valor;
				
				parameters.put(nombre, valores);
				
				nroParam++;
				xmlName ="contenedor[" + nroCont + "]/items[" + nro + "]/parametros[" + nroParam + "]/nombre[1]";
				value = content.getValue(xmlName, Locale.ENGLISH);
			}			
			
			item.setParameters(parameters);
			
			items.add(item);
			
			nro++;
			xmlName ="contenedor[" + nroCont + "]/items[" + nro + "]";
			value = content.getValue(xmlName, Locale.ENGLISH);
		}
		
	}

	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}
	
	public List<String> getSelectedItemsTypes()
	{
		List<String> selTypes = new ArrayList<String>();
		
		for (A_ItemProcessor item : items)
			if (!selTypes.contains(item.itemType))
				selTypes.add(item.itemType);

		return selTypes;
	}

	public void printHTML() {
		try {

			if (project.equals("Offline") || (!project.equals("Offline") && !hide)) {
				//if (items.size()>0){
					String className = "";
					
					if(hide)
						className = "dndHidden";
					else if (project.equals("Offline"))
						className = "zona " + configuration;
					else
						className = configuration;
				
					page.getOut().print("<div id=\"" + name + "\"" + (project.equals("Offline") ? " typeNode=\"container\" lastClass=\"" + configuration + "\"" : "") + " class=\"" + className + "\"" + (project.equals("Offline") ? " allowedConfigurations=\"" + StringUtils.join(allowedConfigurations, ",") + "\"" : "") + " maxItems=\"" + maxItems + "\">");
				//}
			
				if (project.equals("Offline")) {	
									
					page.getOut().print("<div dojoType=\"dojo.dnd.Source\" accept=\"" + StringUtils.join(allowedTypes, ",") + "\" jsId=\"c" + name + "\" class=\"" + (items.size() > 0 ? "dndContainer" : "dndContainerEmpty") + "\">");
	
					page.getOut().print(
							"<script type=\"dojo/method\" event=\"onDropExternal\" args=\"_1a,_1b,_1c\">" +
							"dropExternal(this,_1a,_1b,_1c);" +
							"</script>");
				}
			
				for (A_ItemProcessor item : items)
					item.printHTML();

				if (project.equals("Offline"))
					page.getOut().print("</div>");

				//if (items.size()>0)
					page.getOut().print("</div>");

				if (project.equals("Offline")) {
	
					String menu = 
						" <div dojoType=\"dijit.Menu\" id=\"wcm_" + name + "\" targetNodeIds=\"" + name + "\" style=\"display: none;\">" + 				
						" 	<div dojoType=\"dijit.PopupMenuItem\" iconClass=\"dijitIconConfigure\">" +
						"		<span>Apply Configuration</span>" +
						"		<div dojoType=\"dijit.Menu\">";
	
					for (String conf : allowedConfigurations) {
						menu +=
							"<div dojoType=\"dijit.MenuItem\" id=\"mnuAllowedConfigurations_" + name + "_" + conf + "\"" + (conf.equals(configuration) ? " disabled=\"true;\"" : "") + " onClick=\"containerChangeConfiguration(currentcointeiner,'" + conf + "'); changeStatusConfigurationMenu(this);\">" +
							conf +
						    "</div>";
					}
					
					menu += "   </div>" +
							"</div>";
					
					menu += "<div dojoType=\"dijit.MenuSeparator\"></div> ";
	
					menu +=
							"	<div dojoType=\"dijit.MenuItem\" onClick=\"changeStyle(currentcointeiner,'dndHidden');dijit.byId(currentcointeiner).set('disabled', false);\">" +
							"   	Hide" +
							"   </div>" +
							"</div>";				
	
					page.getOut().print(menu);
	
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void printJS() {
		try {

			String js =
				"dojo.addOnLoad(function() {\n" +
				"var bloque = dojo.byId(\"" + name + "\");\n" +
				"dojo.connect(bloque, \"onmouseover\", function(event){\n" +
				"				currentcointeiner='"+ name + "';\n" +
				"				dojo.stopEvent(event);\n" +

				"			})});\n";

			
			
			page.getOut().print(js);
		
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public PageContext getPage() {
		return page;
	}

	public void setPage(PageContext page) {
		this.page = page;
	}

	public HttpServletRequest getReq() {
		return req;
	}

	public void setReq(HttpServletRequest req) {
		this.req = req;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public List<String> getAllowedConfigurations() {
		return allowedConfigurations;
	}

	public List<String> getAllowedTypes() {
		return allowedTypes;
	}
	
	public Boolean isHide() {
		return hide;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContainerProcessor other = (ContainerProcessor) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}	
}
