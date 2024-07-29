package com.tfsla.genericImport.transformation;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.genericImport.exception.DataTransformartionException;

public class StringToDateDataTransformation extends A_dataTransformation implements I_dataTransformation {

    protected static final Log LOG = CmsLog.getLog(StringToDateDataTransformation.class);

	public StringToDateDataTransformation() {
		name = "StringToDate";
		
		parameters.add("format");
		parameters.add("campos a aplicar separado por comas (vacio aplica a todos)");

	}
	
		

	@Override
	public String getNiceDescription() {
		return "texto a fecha";
	}

	@Override
	public String getTransformationDescription(String transformation) {
		String descrip =  "formatear texto a fecha con " + getParameter(transformation,1);
		
		String campos = getParameter(transformation,2);
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
		
		if (value==null)
			return null;
		
		String format = getParameter(transformation,1);
		String campos = getParameter(transformation,2);		
		
		setCamposToProcess(campos);

		SimpleDateFormat sdf = new SimpleDateFormat(format);
		
		
		Object[] results = new Object[value.length];
		int campIdx = 1;
		
		for (Object val : value)
		{
			if (processCampo(campIdx)) {
				if (!(val instanceof String))
					throw new DataTransformartionException("El elemento a transformar no es de tipo texto");
				try {
					String valStr = (String)val;
					valStr = valStr.trim();
					results[campIdx-1] = sdf.parse((String)valStr);
				} catch (ParseException e) {
					throw new DataTransformartionException("Error al convertir a fecha ("+ format + ") el valor " + value,e);
				}

			}
			else
				results[campIdx-1] = val;
			campIdx++;
		}

		return results;
		
	}


}
