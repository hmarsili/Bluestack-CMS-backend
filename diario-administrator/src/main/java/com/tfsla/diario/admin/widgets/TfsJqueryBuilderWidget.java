package com.tfsla.diario.admin.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import com.tfsla.diario.admin.TfsXmlContentEditor;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TfsJqueryBuilderWidget extends A_TfsWidget implements I_TfsWidget {
	
	Map<String,String> configParams = null;
	String baseurl = "<cms:link>/system/modules/com.tfsla.diario.admin/schemas/noticiasConfigurationFile.json</cms:link>";
	
	@Override
	public String getWidgetHtml(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {
		parseParams();
		
		List<Map<String, String>> listConf = new ArrayList();
		
        String[] filtros = getConfiguration().split("\\|");
        for (String item:filtros){
        	String[] values = item.split("\\,");
        	Map<String, String> configurations = new HashMap<String, String>();
        	for (String value:values){        		
        		configurations.put(value.substring(0,value.indexOf(":")), value.substring(value.indexOf(":")+1,value.length()));
        	}
        	listConf.add(configurations);        	
        }              
		
		String elemetValues = getWidgetStringValue(cms,widgetDialog,param);
        String id = param.getId();
        StringBuffer result = new StringBuffer();
        StringBuffer valuesSelect = new StringBuffer();
        
        //boolean plugin = false;
        
    	String[] selectors = id.split("_");
    	String selector = selectors[1];
    	int index = 0;
    	
    	String element = id.replace("OpenCmsString", "").replaceAll("\\.", "");
    	
    	result.append("<div id=\""+element+"\" class=\"\" ></div>");
    	result.append("<div class=\"btn-group\">");
    	result.append("<input type=\"hidden\" id=\""+widgetDialog.getIdElement(id)+"\" content-type=\"" + getTypeName(param) + "\" content-definition=\""+element+"\" content-id=\""+id+"\" class=\"item-value\" value=\"\" name=\""+widgetDialog.getIdElement(id)+"\" style=\"visibility:hidden;display:none;\" />");
    	result.append("</div>");
    	result.append("<script type=\"text/javascript\">\n");
    	
    	result.append("var inticialCharge=false\n;");
    	result.append("$('#"+element+"').queryBuilder({\n");
    	result.append("sortable: true,\n");
    	result.append("filters: [\n");
    	
    	for(Map<String, String> configurations : listConf){
    		try {
    			index++;
    			if(configurations.get("to")!=null){
    				String[] maps = configurations.get("to").split("\\;");
    				int a = 0;
    				for (String item : maps) {
    					a++;
    					
    					String[] valoresConfig = item.split("#");
    					
    					String elementValue = "";
    					String input = "";
    					String type="";
    					String operator="";
    					
    					if (valoresConfig.length == 1) {
    						 elementValue = item.substring(item.indexOf("=")+1,item.length());
    						 type="string";
    						 input = "select";
        				} else {
    						input = valoresConfig[1].substring(valoresConfig[1].indexOf("=")+1);
    						elementValue = valoresConfig[0].substring(item.indexOf("=")+1);
    						if (valoresConfig.length==2) {
    							type="string";
    						} else if (valoresConfig.length == 3) {
    							type = valoresConfig[2].substring(valoresConfig[2].indexOf("=")+1);
    						} else if (valoresConfig.length == 4) {
    							type = valoresConfig[2].substring(valoresConfig[2].indexOf("=")+1);
        						operator = valoresConfig[3].substring(valoresConfig[3].indexOf("=")+1);
    						} 
        				}
    					result.append("{\n");
    			    	result.append("id: '" + elementValue + "',\n");
    	    	    	result.append("label: '" + elementValue + "',\n");
    					
    	    	    	result.append("type: '"+type+"',\n");
    	    	    	if (operator.equals("date")) {
    	    	    		result.append("operators: ['greater', 'less']\n");
    	    	    	} else {
    	    	    		result.append("operators: ['equal', 'not_equal']\n");
    	    	    	}
    	    	    	if (!input.equals("select")) 
        	        		result.append(",input: '"+ input + "'");
    	    	    	else {
    	    	    		
    	    	    		if (valuesSelect.length() == 0) {
	    	    				String[] mapsValues = configurations.get("to").split("\\;");
		    	    			for(String map : mapsValues) {
		    	    				
		    	    				String toVal = map.substring(0,map.indexOf("="));
		    	    				String labelVal = map.substring(map.indexOf("=")+1,map.length()).split("#")[0];
		    	    				valuesSelect.append("that.addOption({id:'"+ toVal +"',name:'"+ labelVal +"'});\n");
			    	    		}
    	    	    		}
    	    	    		
    	    	    		result.append(",plugin: 'selectize',\n" + 
    	        	    			"    plugin_config: {\n" + 
    	        	    			"      valueField: 'id',\n" + 
    	        	    			"      labelField: 'name',\n" + 
    	        	    			"      searchField: 'name',\n" + 
    	        	    			"      sortField: 'name',\n" + 
    	        	    			"      create: true,\n" + 
    	        	    			"      maxItems: 1,\n" +
    	        	    			"		openOnFocus: false," +
    	        	    			"		createOnBlur:true," +	
    	        	    			"      onInitialize: function() { ");
    	        	    	result.append ("getOptions(this)");
    			    	    
    	        	    	result.append("},\n" + 
	    	    					" onOptionAdd: function (value,data) {\n" + 
	    	    					"      	if (!inticialCharge){\n" + 
	    	    					"      	 	this.updateOption(value,{id:'manual-'+value,name:data.name});\n" + 
	    	    					"           this.refreshOptions();\n" + 
	    	    					"     }   "
	    	    					+ "}"
	    	    					+ "},\n" + 
	    	    					"    valueSetter: function(rule, value) {\n" + 
	    	    					"      if (rule.value.indexOf(\"manual-\")==0){\n"+
	    	    					"			rule.$el.find('.rule-value-container input')[0].selectize.addOption ({id:rule.value.replace(\"manual-\",\"\"),name:rule.value.replace(\"manual-\",\"\")});\n"+
	    	    					"			rule.$el.find('.rule-value-container input')[0].selectize.refreshOptions();\n"+
	    	    					"		}	"+
	    	    					"      rule.$el.find('.rule-value-container input')[0].selectize.setValue(value);\n" + 
	    	    					"    }\n");	
	    	    			}       	
	    	    	    	if(hasMoreItems(a, maps.length)){
	    	    	    		result.append("},                     \n");
	    	    	    	} 
    					}
    			} else {
    				result.append("id: '" + configurations.get("id") + "',         \n");
    				result.append("label: '" + configurations.get("label") + "',      \n");
    				result.append("type: '" + (configurations.get("type")==null?"string":configurations.get("type")) + "'");
    				result.append("\n");
    			}
    	    	
    	    	if(hasMoreItems(index, listConf.size())){
    	    		result.append("},                     \n");
    	    		result.append("{                     \n");
    	    	}
    		}catch (Exception e) {
    			//ignoreR
    		}
    	}

    	result.append("}]\n");
    	result.append("});\n");
    	
    	if(!elemetValues.isEmpty()){
	    	result.append("$(window).load(function() { \n");
	    	result.append("$('#"+element+"').queryBuilder('setRules', "+ elemetValues +");\n");
	    	result.append("$('[data-toggle=\"tooltip\"]').tooltip();\n");
	    	result.append("});\n");
	    	
	    	
    	}
    	
    	result.append("$(window).load(function() { \n");
    	result.append("// Fix for Selectize\n" + 
    			"$('#"+element+"').on('afterCreateRuleInput.queryBuilder', function(e, rule) {\n" + 
    			"  if (rule.filter.plugin == 'selectize') {\n" + 
    			"    rule.$el.find('.rule-value-container').css('min-width', '200px')\n" + 
    			"      .find('.selectize-control').removeClass('form-control');\n" + 
    			"  }\n" + 
    			"});");
    	
    	result.append("});\n");
    	result.append("function getOptions(that) {\n"
    			+ "inticialCharge=true;\n");
    	result.append(valuesSelect);
    	result.append(" that.refreshOptions();\n"
    			+ "inticialCharge=false;\n");
    	result.append ("}\n");
    	
    	result.append("</script>\n");        

        return result.toString();
	}

	@Override
	public List<String> getOpenCmsWidgetsClassName() {
		List<String> widgets = new ArrayList<String>();
		widgets.add(com.tfsla.diario.admin.widgets.opencms.TfsJqueryBuilderWidget.class.getName());//CmsInputWidget.class.getName());
		return widgets;
	}
	
	private boolean hasMoreItems(int index, int size) {
		if(index != size){
			return true;
		}
		return false;
	}
	
	public String getWidgetStringValue(CmsObject cms, TfsXmlContentEditor widgetDialog, I_CmsWidgetParameter param) {

        String result = param.getStringValue(cms);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result) ) {
        	return result;
        } else {
        	return "";
        }
        
    }
	
	public void getElementItems(CmsObject cms, String ctype,String path, String description, boolean onlycomposite, JSONArray jsonItems) {
		
		try {
			List<I_CmsXmlSchemaType> xmlSchemas = getSubContentDetail(cms, ctype, path);
			
			for (I_CmsXmlSchemaType xmlSchema : xmlSchemas ) {
				String newPath = ""; 
				if (!path.equals("")) 
					newPath = path + "/" + xmlSchema.getName();
				else
					newPath = xmlSchema.getName();
					
				String newDescription = ""; 
				if (!description.equals("")) 
					newDescription = description + " - " + xmlSchema.getName();
				else
					newDescription = xmlSchema.getName();
					
				if (xmlSchema.isSimpleType() ) {
				
					if (!onlycomposite || xmlSchema.getMinOccurs()==0 || xmlSchema.getMaxOccurs()>1) {
						JSONObject jsonitem = new JSONObject();
			
						jsonitem.put("key", newPath);
						jsonitem.put("value",  newDescription);
						jsonItems.add(jsonitem);
					}
				
				}
				else {
					if (onlycomposite) {
						JSONObject jsonitem = new JSONObject();
			
						jsonitem.put("key", newPath);
						jsonitem.put("value",  newDescription);
						jsonItems.add(jsonitem);
					}

					getElementItems(cms, ctype,newPath, newDescription, onlycomposite, jsonItems);
				}
			}
		}
		catch (CmsLoaderException ex) {}
		catch (CmsXmlException ex) {}
	}
	
	public List<I_CmsXmlSchemaType> getSubContentDetail(CmsObject cms, String contentTypeName, String elementName) throws CmsLoaderException, CmsXmlException {
    	
		I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(contentTypeName);
        // get the schema for the resource type to create
        String schema = (String)resType.getConfiguration().get(CmsResourceTypeXmlContent.CONFIGURATION_SCHEMA);
        CmsXmlContentDefinition contentDefinition = CmsXmlContentDefinition.unmarshal(cms, schema);	
		
        if (!elementName.equals("")) {
	        String[] nameParts = elementName.split("/");
	        for (String part : nameParts) {
	        	for (I_CmsXmlSchemaType type : contentDefinition.getTypeSequence())
	        	{
	        		if (type.getName().equals(part)) {
	        			if (!type.isSimpleType()) {
	                        // get nested content definition for nested types
	                        CmsXmlNestedContentDefinition nestedSchema = (CmsXmlNestedContentDefinition)type;
	                        contentDefinition = nestedSchema.getNestedContentDefinition();
	                    }
	        			break;
	        		}
	        	}
	        }
        }
        return contentDefinition.getTypeSequence();
    }
	
	private void parseParams(){
		
		configParams = new HashMap<String,String>();
		
		String conf = getConfiguration();
		if (conf!=null) {
			String params[] = conf.split(",");
			for (int j=0; j< params.length; j++)
	    	{
				String param[] = params[j].split(":");
				if (param.length==2)	
					configParams.put(param[0].trim(), param[1].trim());
	    		
	    	}
		}
	}

}
