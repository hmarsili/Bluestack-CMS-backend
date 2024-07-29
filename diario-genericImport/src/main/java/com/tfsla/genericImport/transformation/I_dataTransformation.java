package com.tfsla.genericImport.transformation;

import java.util.List;

import org.opencms.file.CmsObject;

import com.tfsla.genericImport.exception.DataTransformartionException;


public interface I_dataTransformation {
	public String getName();
	public String getNiceDescription();
	public String getTransformationDescription(String transformation);
	public String getHelpText();

	public Object[] execute(String transformation, Object[] value) throws DataTransformartionException;
	
	public int parametersCount();
	public List<String> getParamsNames();
	public void setCms(CmsObject cms);
	
}
