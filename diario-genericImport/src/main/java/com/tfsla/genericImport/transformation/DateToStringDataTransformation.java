package com.tfsla.genericImport.transformation;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.genericImport.exception.DataTransformartionException;

public class DateToStringDataTransformation extends A_dataTransformation implements I_dataTransformation {

    protected static final Log LOG = CmsLog.getLog(DateToStringDataTransformation.class);

	public DateToStringDataTransformation() {
		name = "DateToString";
		
		parameters.add("format");
		parameters.add("campos a aplicar separado por comas (vacio aplica a todos)");
	}
	
	@Override
	public String getNiceDescription() {
		return "fecha a texto";
	}
	
	@Override
	public String getTransformationDescription(String transformation) {
		String descrip = "formatear fecha a " + getParameter(transformation,1);
		
		String campos = getParameter(transformation,2);
		if (campos.equals(""))
			descrip += " (todos los campos)"; 
		else
			descrip += " ( campos " + campos + ")";
		
		return descrip;

	}

	@Override
	public String getHelpText() {
		return "Transforma una fecha en un texto con el formato de entrada";
	}

	@Override
	public Object[] execute(String transformation, Object[] value) throws DataTransformartionException {
		
		if (value==null)
			return null;
		
		String format = getParameter(transformation,1);
		String campos = getParameter(transformation,2);
		
		setCamposToProcess(campos);

		SimpleDateFormat sdf = new SimpleDateFormat(format);


		//LOG.debug("Transformation '" + getName() + "' (value=" + value + "): " + format );

		Object[] results = new Object[value.length];
		int campIdx = 1;
		for (Object val : value)
		{
			if (processCampo(campIdx)) {
				
				if (!(val instanceof Date))
					throw new DataTransformartionException("El elemento a transformar no es de tipo fecha (" + value.getClass().getName() + ")");
				try{
					results[campIdx-1] = sdf.format((Date)val);
				}catch(Exception ex){
					ex.getMessage();
				}
			}
			else
				results[campIdx-1] = val;
			campIdx++;
		}

		return results;
	}

	


}
