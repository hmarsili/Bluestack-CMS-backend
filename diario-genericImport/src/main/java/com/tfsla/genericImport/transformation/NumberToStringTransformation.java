package com.tfsla.genericImport.transformation;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.genericImport.exception.DataTransformartionException;

public class NumberToStringTransformation  extends A_dataTransformation implements I_dataTransformation {

    protected static final Log LOG = CmsLog.getLog(StringToDateDataTransformation.class);

	public NumberToStringTransformation() {
		name = "NumberToString";
		
		parameters.add("campos a aplicar separado por comas (vacio aplica a todos)");

	}
	
		

	@Override
	public String getNiceDescription() {
		return "numero a texto";
	}

	@Override
	public String getTransformationDescription(String transformation) {
		String descrip =  "formatear un numero como texto";
		
		String campos = getParameter(transformation,1);
		if (campos.equals(""))
			descrip += " (todos los campos)"; 
		else
			descrip += " ( campos " + campos + ")";
		
		return descrip;

	}

	@Override
	public String getHelpText() {
		return "Transforma un numero en un texto";
	}

	@Override
	public Object[] execute(String transformation, Object[] value) throws DataTransformartionException {
		
		if (value==null)
			return null;
		
		String campos = getParameter(transformation,1);
		
		
		setCamposToProcess(campos);

		
		
		//LOG.debug("Transformation '" + getName() + "' (value=" + value + ") ");
		
		
		Object[] results = new Object[value.length];
		int campIdx = 1;
		for (Object val : value)
		{
			if (processCampo(campIdx))
					results[campIdx-1] = "" + val;
			else
				results[campIdx-1] = val;
			campIdx++;
		}

		return results;
		
	}


}
