package com.tfsla.genericImport.transformation;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.genericImport.exception.DataTransformartionException;

public class DateToLongTransformation extends A_dataTransformation implements I_dataTransformation {

    protected static final Log LOG = CmsLog.getLog(DateToStringDataTransformation.class);

	public DateToLongTransformation() {
		name = "DateToLong";
		
		parameters.add("campos a aplicar separado por comas (vacio aplica a todos)");
	}
	
	@Override
	public String getNiceDescription() {
		return "fecha a numero";
	}
	
	@Override
	public String getTransformationDescription(String transformation) {
		String descrip = "formatear fecha a numero";
		
		String campos = getParameter(transformation,2);
		if (campos.equals(""))
			descrip += " (todos los campos)"; 
		else
			descrip += " ( campos " + campos + ")";
		
		return descrip;

	}

	@Override
	public String getHelpText() {
		return "Transforma una fecha en la cantidad de milisegundos";
	}

	@Override
	public Object[] execute(String transformation, Object[] value) throws DataTransformartionException {
		
		if (value==null)
			return null;
		
		String campos = getParameter(transformation,1);
		
		setCamposToProcess(campos);


		//LOG.debug("Transformation '" + getName() + "' (value=" + value + "): ");

		Object[] results = new Object[value.length];
		int campIdx = 1;
		for (Object val : value)
		{
			if (processCampo(campIdx)) {
				
				if (!(val instanceof Date))
					throw new DataTransformartionException("El elemento a transformar no es de tipo fecha (" + value.getClass().getName() + ")");

				results[campIdx-1] = ((Date)val).getTime();
			}
			else
				results[campIdx-1] = val;
			campIdx++;
		}

		return results;
	}

	

}
