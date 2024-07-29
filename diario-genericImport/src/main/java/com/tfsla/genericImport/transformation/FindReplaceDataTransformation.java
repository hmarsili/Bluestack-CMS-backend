package com.tfsla.genericImport.transformation;


import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.genericImport.exception.DataTransformartionException;

public class FindReplaceDataTransformation extends A_dataTransformation
		implements I_dataTransformation {

    protected static final Log LOG = CmsLog.getLog(FindReplaceDataTransformation.class);

	public FindReplaceDataTransformation() {
		name = "FindAndReplace";
		
		parameters.add("old value");
		parameters.add("new value");
		parameters.add("campos a aplicar separado por comas (vacio aplica a todos)");

	}
	
	@Override
	public String getNiceDescription() {
		return "buscar y reemplazar";
	}

	@Override
	public String getTransformationDescription(String transformation) {
		String descrip = "reemplazar el valor " + getParameter(transformation,1) + " por el valor " + getParameter(transformation,2);
	
		String campos = getParameter(transformation,3);
		if (campos.equals(""))
			descrip += " (todos los campos)"; 
		else
			descrip += " ( campos " + campos + ")";
		
		return descrip;

	}

	@Override
	public String getHelpText() {
		return "Transforma un texto en una fecha con el formato de entrada";
	}

	@Override
	public Object[] execute(String transformation, Object[] value) throws DataTransformartionException {

		String oldValue = getParameter(transformation,1);
		String newValue = getParameter(transformation,2);
		String campos = getParameter(transformation,3);
		
		setCamposToProcess(campos);

		if (value==null)
			return null;
		
		
		
		Object[] results = new Object[value.length];
		int campIdx = 1;
		for (Object val : value)
		{
			LOG.debug("FindReplace: element " + campIdx + " > " + (val!=null ? val.toString() : "NULL"));
			if (processCampo(campIdx)) {
				
				LOG.debug("FindReplace: reemplazando " + oldValue + " po r" + newValue );
				if (val==null) {
					results[campIdx-1] = null;
					campIdx++;
					continue;
				}
				
					
				if (!(val instanceof String))
					throw new DataTransformartionException("El elemento a transformar no es de tipo texto (" + value.getClass().getName() + ")");
				
				String storedValue = (String)val;
				
				results[campIdx-1] = storedValue.replaceAll(oldValue, newValue);
								
			}
			else
				results[campIdx-1] = val;
			campIdx++;
		}


		//LOG.debug("Transformation '" + getName() + "' (value=" + value + "): " + getParameter(transformation,1) + " -> " + getParameter(transformation,2) );
		
		return results;

	}

	

}
