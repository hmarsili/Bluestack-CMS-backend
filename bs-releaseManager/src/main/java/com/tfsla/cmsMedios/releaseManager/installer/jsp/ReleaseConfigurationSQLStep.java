package com.tfsla.cmsMedios.releaseManager.installer.jsp;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import com.tfsla.diario.admin.jsp.TfsMessages;

public class ReleaseConfigurationSQLStep extends ReleaseConfigurationStep {

	public ReleaseConfigurationSQLStep(JSONObject manifest, TfsMessages messages) {
		super(manifest, messages);
	}

	@Override
	public int getOrder() {
		return 2;
	}
	
	@Override
	public void setDescription(String description) {
		if(manifest.containsKey("scripts")) {
			JSONArray scripts = manifest.getJSONArray("scripts");
			description = String.format(messages.key("RM_SQL_STEP_DESCRIPTION"), scripts.size());
		}
		this.description = description;
	}
	
	@Override
	public String getHtmlAction() {

		htmlAction = "<div class='release-config-step sql-step'>";
		
		if(!manifest.containsKey("scripts")) {
			htmlAction += messages.key("RM_EMPTY_STEP");
		} else {
			JSONObject parameters = manifest.getJSONObject("scripts-parameters");
			JSONArray scripts = manifest.getJSONArray("scripts");
			
			htmlAction = "<div class='release-config-step sql-step'>";
			htmlAction += "<table>";
			for(Object script : scripts) {
				
				htmlAction += "<tr>";
				htmlAction += "<td valign='top'><span class='script-name'>" + script.toString() + "</span>";
				if(parameters.containsKey(script.toString()) && parameters.getJSONObject(script.toString()).containsKey("description")) {
					htmlAction += "<span class='script-description'>" + parameters.getJSONObject(script.toString()).getString("description") + "</span>";
				}
				htmlAction += "</td>";
				htmlAction += "<td>";
				if(!parameters.containsKey(script) || !parameters.getJSONObject(script.toString()).containsKey("parameters")) {
					htmlAction += messages.key("No hay parámetros para este script");
				} else {
					htmlAction += getParamsHtml(script.toString(), parameters.getJSONObject(script.toString()).getJSONArray("parameters"));
				}
				htmlAction += "</td>";
				htmlAction += "</tr>";
			
			}
			htmlAction += "</table>";
		}
		
		String paramNamesJoined = StringUtils.join(this.paramNames.toArray(), ",");
		htmlAction += String.format("<input type='hidden' name='sql-param-names' value='%s'/>", paramNamesJoined);
		
		htmlAction += "</div>";
		
		return htmlAction;
	}

	private String getParamsHtml(String scriptName, JSONArray params) {
		String elementName = scriptName.replace(".sql", "");
		String paramsHtml = "<ul class='sql-params'>";
		for(Object i : params.toArray()) {
			JSONObject param = (JSONObject)i;
			paramsHtml += "<li class='param-index-" + param.getInt("index") + "'>";
			paramsHtml += "<div class='param-name'>";
			paramsHtml += param.getString("name");
			paramsHtml += "</div>";
			paramsHtml += "<div class='param-description'>";
			if(param.containsKey("description")) {
				paramsHtml += messages.key(param.getString("description"));
			}
			paramsHtml += "<span class='param-messages' id='message-" + elementName + param.getInt("index") + "'></span>";
			paramsHtml += "</div>";
			paramsHtml += "<div class='param-value-input'>";
			paramsHtml += "<input class='param-value' type='text' name='" + elementName + param.getInt("index") + "' id='" + elementName + param.getInt("index") + "' value='" + param.getString("default-value") + "' placeholder='Default: " + param.getString("default-value") + "' " + (param.containsKey("maxlength") ? "maxlength='" + param.getString("maxlength") + "'" : "") + "/>";
			paramsHtml += "<input class='param-regex' type='hidden' name='validator-" + elementName + param.getInt("index") + "' id='validator-" + elementName + param.getInt("index") + "' value='" + (param.containsKey("validator") ? param.getString("validator") : "") + "' data-message='" + param.getString("message") + "' />";
			paramsHtml += "</div>";
			paramsHtml += "</li>";
			
			paramNames.add(elementName + param.getInt("index"));
		}
		paramsHtml += "</ul>";
		return paramsHtml;
	}
	
	private ArrayList<String> paramNames = new ArrayList<String>();
}
