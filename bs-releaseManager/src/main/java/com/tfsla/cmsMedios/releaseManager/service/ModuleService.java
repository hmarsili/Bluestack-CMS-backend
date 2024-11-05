package com.tfsla.cmsMedios.releaseManager.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.digester3.Digester;
import org.xml.sax.SAXException;

import com.tfsla.cmsMedios.releaseManager.model.Module;
import com.tfsla.cmsMedios.releaseManager.model.ModuleParamGroup;
import com.tfsla.cmsMedios.releaseManager.model.ModuleParameters;

public class ModuleService {

	public static int PARAM_UNCHANGED = 0;
	public static int PARAM_NEW = 1;
	public static int PARAM_DEFAULT_CHANGED = 2;
	
	public int compareParamModule(Module oldModule, Module newModule, String paranName, String currentValue) {
		ModuleParameters newParam = newModule.getParameter(paranName);
		ModuleParameters oldParam = oldModule.getParameter(paranName);
			
		if (oldParam==null) {
			return PARAM_NEW;
		}
		
		if (currentValue.equals(oldParam.getDefaultValue()) && !newParam.getDefaultValue().equals(oldParam.getDefaultValue())) {
			return PARAM_DEFAULT_CHANGED;
		}
				
		return PARAM_UNCHANGED;
	}

	
	public int compareParamGroupModule(Module oldModule, Module newModule, String paranGroupName, String currentValue) {
		ModuleParamGroup newParamGroup = newModule.getParamGroup(paranGroupName);
		ModuleParamGroup oldParamGroup = oldModule.getParamGroup(paranGroupName);
			
		if (oldParamGroup==null) {
			return PARAM_NEW;
		}
		
		if (currentValue.equals(oldParamGroup.getParamList().getDefaultValue()) && !newParamGroup.getParamList().getDefaultValue().equals(oldParamGroup.getParamList().getDefaultValue())) {
			return PARAM_DEFAULT_CHANGED;
		}
				
		return PARAM_UNCHANGED;
	}

	public int compareParamGroupItemModule(Module oldModule, Module newModule, String paranGroupName, String paranGroupItemName, String currentValue) {
		ModuleParamGroup newParamGroup = newModule.getParamGroup(paranGroupName);
		ModuleParamGroup oldParamGroup = oldModule.getParamGroup(paranGroupName);
			
		if (oldParamGroup==null) {
			return PARAM_NEW;
		}
		
		ModuleParameters newParamItem = newParamGroup.getParameter(paranGroupItemName);
		ModuleParameters oldParamItem = oldParamGroup.getParameter(paranGroupItemName);
		
		if (oldParamItem==null) {
			return PARAM_NEW;
		}
		
		if (currentValue.equals(oldParamItem.getDefaultValue()) && !newParamItem.getDefaultValue().equals(oldParamItem.getDefaultValue())) {
			return PARAM_DEFAULT_CHANGED;
		}
				
		return PARAM_UNCHANGED;
	}
	
	public List<Module> loadModules(String path, String listFile) {
		List<Module> modules = new ArrayList<Module>();
		
		List<String> moduleList = getAvailableModules(path + listFile);
		for (String moduleFileName : moduleList) {
			try {
				modules.add(loadModule(path + moduleFileName));

			} catch (SAXException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		
		return modules;
	}
	
	public List<String> getAvailableModules(String location) {
		List<String> modules = new ArrayList<String>();
		
		
		try(BufferedReader br = new BufferedReader(new InputStreamReader(getInputStream(location)))) {
		    for(String line; (line = br.readLine()) != null; ) {
		        modules.add(line);
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return modules;
	}
	
	public Module loadModule(String fileName) throws SAXException, IOException {
		Digester digester = new Digester();

        // Use the test schema
        digester.setNamespaceAware(true);
			Schema schema = SchemaFactory.
			    newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).
			    newSchema(this.getClass().getClassLoader().
			        getResource("com/tfsla/cmsMedios/releaseManager/module.xsd"));
				
		digester.setXMLSchema(schema);
		
		// Configure the digester as required
        digester.addObjectCreate("module", Module.class);
        digester.addSetProperties("module");

        digester.addCallMethod("module/description", "setDescription", 0);
        digester.addCallMethod("module/level", "addLevel", 0);
        digester.addCallMethod("module/optional", "setOptional", 0);
        
        digester.addObjectCreate("module/parameters/parameter", ModuleParameters.class);
        digester.addSetProperties("module/parameters/parameter");
       	        
        digester.addCallMethod("module/parameters/parameter/description", "setDescription", 0);
        digester.addCallMethod("module/parameters/parameter/longDescription", "setLongDescription", 0);
        digester.addCallMethod("module/parameters/parameter/defaultValue", "setDefaultValue", 0);
        digester.addCallMethod("module/parameters/parameter/type", "setType", 0);
        digester.addCallMethod("module/parameters/parameter/regEx", "setRegEx", 0);
        digester.addCallMethod("module/parameters/parameter/optional", "setOptional", 0);
        digester.addCallMethod("module/parameters/parameter/validationMsg", "setValidationMsg", 0);
    	
        digester.addCallMethod("module/parameters/parameter/values/value", "addValue", 0);
        
        digester.addSetNext("module/parameters/parameter", "addParameter");

        
        
        digester.addObjectCreate("module/paramGroups/paramGroup", ModuleParamGroup.class);
        digester.addSetProperties("module/paramGroups/paramGroup");
        
        
        
        digester.addObjectCreate("module/paramGroups/paramGroup/parameter", ModuleParameters.class);
        digester.addSetProperties("module/paramGroups/paramGroup/parameter");
       	
        digester.addCallMethod("module/paramGroups/paramGroup/description", "setDescription", 0);
        
        digester.addCallMethod("module/paramGroups/paramGroup/parameter/description", "setDescription", 0);
        digester.addCallMethod("module/paramGroups/paramGroup/parameter/longDescription", "setLongDescription", 0);
        digester.addCallMethod("module/paramGroups/paramGroup/parameter/defaultValue", "setDefaultValue", 0);
        digester.addCallMethod("module/paramGroups/paramGroup/parameter/type", "setType", 0);
        digester.addCallMethod("module/paramGroups/paramGroup/parameter/regEx", "setRegEx", 0);
        digester.addCallMethod("module/paramGroups/paramGroup/parameter/optional", "setOptional", 0);
        digester.addCallMethod("module/paramGroups/paramGroup/parameter/validationMsg", "setValidationMsg", 0);
    	
        digester.addCallMethod("module/paramGroups/paramGroup/parameter/values/value", "addValue", 0);
        
        digester.addSetNext("module/paramGroups/paramGroup/parameter", "setParamList");
        
        
        digester.addObjectCreate("module/paramGroups/paramGroup/parameters/parameter", ModuleParameters.class);
        digester.addSetProperties("module/paramGroups/paramGroup/parameters/parameter");
       	        
        digester.addCallMethod("module/paramGroups/paramGroup/parameters/parameter/description", "setDescription", 0);
        digester.addCallMethod("module/paramGroups/paramGroup/parameters/parameter/longDescription", "setLongDescription", 0);
        digester.addCallMethod("module/paramGroups/paramGroup/parameters/parameter/defaultValue", "setDefaultValue", 0);
        digester.addCallMethod("module/paramGroups/paramGroup/parameters/parameter/type", "setType", 0);
        digester.addCallMethod("module/paramGroups/paramGroup/parameters/parameter/regEx", "setRegEx", 0);
        digester.addCallMethod("module/paramGroups/paramGroup/parameters/parameter/optional", "setOptional", 0);
        digester.addCallMethod("module/paramGroups/paramGroup/parameters/parameter/validationMsg", "setValidationMsg", 0);
    	
        digester.addCallMethod("module/paramGroups/paramGroup/parameters/parameter/values/value", "addValue", 0);
        
        digester.addSetNext("module/paramGroups/paramGroup/parameters/parameter", "addParameter");

        digester.addSetNext("module/paramGroups/paramGroup", "addParamGroup");
        
        
        Module module =(Module)digester.parse(getInputStream(fileName));   
        
        digester = null;
        
        return module;
	}
        
    protected InputStream getInputStream(String fileName) throws IOException {
    	return new FileInputStream(fileName);
        //return (this.getClass().getResourceAsStream(name));
    }
}
