package com.tfsla.genericImport.transformation;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.genericImport.exception.DataTransformartionException;

public class NullExtractorTransformation   extends A_dataTransformation implements I_dataTransformation {

    protected static final Log LOG = CmsLog.getLog(StringToDateDataTransformation.class);

	public NullExtractorTransformation() {
		name = "NullExtractor";
		
		parameters.add("campos a aplicar separado por comas (vacio aplica a todos)");

	}
			

	@Override
	public String getNiceDescription() {
		return "Quitar valores nulos";
	}

	@Override
	public String getTransformationDescription(String transformation) {
		String descrip = "Quitar los valores nulos";
		
		String campos = getParameter(transformation,1);
		if (campos.equals(""))
			descrip += " (todos los campos)"; 
		else
			descrip += " ( campos " + campos + ")";
		
		return descrip;

	}

	@Override
	public String getHelpText() {
		return "Quita los valores nulos de los campos obtenidos";
	}

	@Override
	public Object[] execute(String transformation, Object[] value) throws DataTransformartionException {
		
		if (value==null)
			return null;
		
		String campos = getParameter(transformation,1);
				
		setCamposToProcess(campos);
		
		//LOG.debug("Transformation '" + getName() + "' (value=" + value + ") ");
		
		int valuesNotNull = 0;
		for (Object val : value)
			if (val!=null)
				valuesNotNull++;

		if (valuesNotNull == 0)
			return null;
		
		
		Object[] results = new Object[valuesNotNull];
		int campIdx = 0;
		for (Object val : value)
			if (val!=null) {
					results[campIdx] = val;
					campIdx++;
			}

		return results;
		
	}


}
