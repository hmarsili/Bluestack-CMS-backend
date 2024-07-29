package com.tfsla.genericImport.transformation;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.genericImport.exception.DataTransformartionException;

public class StringFormatTransformation extends A_dataTransformation
	implements I_dataTransformation {

    protected static final Log LOG = CmsLog.getLog(FindReplaceDataTransformation.class);

	public StringFormatTransformation() {
		name = "StringFormat";
		
		parameters.add("format");
		parameters.add("campos a aplicar separado por comas (vacio aplica a todos)");

	}
	
	@Override
	public String getNiceDescription() {
		return "Formatear el texto";
	}

	@Override
	public String getTransformationDescription(String transformation) {
		String descrip =  "formatear el texto con " + getParameter(transformation,1);
		
		String campos = getParameter(transformation,2);
		if (campos.equals(""))
			descrip += " (todos los campos)"; 
		else
			descrip += " ( campos " + campos + ")";
		
		return descrip;

	}

	@Override
	public String getHelpText() {
		return "Formatea un texto con el formato de entrada";
	}

	@Override
	public Object[] execute(String transformation, Object[] value) throws DataTransformartionException {

		String format = getParameter(transformation,1);
		String campos = getParameter(transformation,2);
		
		setCamposToProcess(campos);

		if (value==null)
			return null;
		
		
		
		Object[] results = new Object[value.length];
		int campIdx = 1;
		for (Object val : value)
		{
			if (processCampo(campIdx)) {
				
				results[campIdx-1] =  String.format(format, val) ;
								
			}
			else
				results[campIdx-1] = val;
			campIdx++;
		}


		//LOG.debug("Transformation '" + getName() + "' (value=" + value + "): " + getParameter(transformation,1));
		
		return results;

	}

	


}
