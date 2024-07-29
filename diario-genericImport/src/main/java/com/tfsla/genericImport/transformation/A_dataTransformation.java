package com.tfsla.genericImport.transformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencms.file.CmsObject;

public abstract class A_dataTransformation implements I_dataTransformation {

	protected CmsObject cms;
	public void setCms(CmsObject cms) {
		this.cms = cms;
	}

	protected List<String> parameters = new ArrayList<String>();
	protected String name;

	public A_dataTransformation() {
		super();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int parametersCount() {
		// TODO Auto-generated method stub
		return parameters.size();
	}

	@Override
	public List<String> getParamsNames() {
		// TODO Auto-generated method stub
		return parameters;
	}

	protected String getParameter(String transformation, int paramNumber) {
		String[] parameters = transformation.split("\\|\\|");
		
		if (parameters.length<=paramNumber)
			return "";
		return parameters[paramNumber];
	}

	protected boolean processAll;
	protected Map<Integer,Boolean> camposToProcess;
	protected int camposToProcessCount;
	
	protected boolean isProcessAll() {
		return processAll;
	}
	
	protected boolean processCampo(int campo) {
		if (processAll)
			return true;
		return (camposToProcess.get(campo)!=null);
				
	}
	
	protected int getCamposToProcessCount() {
		return camposToProcessCount;
	}
	
	protected void setCamposToProcess(String campos) {
		camposToProcess = new HashMap<Integer,Boolean>();

		if (campos.trim().equals(""))
			processAll=true;
		else {
			processAll=false;
			
			
			String[] camposArray = campos.split(",");
			
			camposToProcessCount = camposArray.length;
			
			for (String campo: camposArray) {
				camposToProcess.put(Integer.parseInt(campo),true); 
			}
		}

	}

}