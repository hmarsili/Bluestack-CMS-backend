package com.tfsla.genericImport.transformation;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.genericImport.exception.DataTransformartionException;

public class RegexDataExtractor extends A_dataTransformation implements I_dataTransformation {

    protected static final Log LOG = CmsLog.getLog(ConditionalDataTransformation.class);

	public RegexDataExtractor() {
		name = "regExExtractor";
		
		parameters.add("patron");
		parameters.add("campos a aplicar separado por comas (vacio aplica a todos)");

	} 

	@Override
	public String getNiceDescription() {
		return "Extraccion por expresion regular";

	}

	@Override
	public String getTransformationDescription(String transformation) {
		String descrip = "Extraer el valor por medio de la expresion regular " + getParameter(transformation,1);
		
		String campos = getParameter(transformation,2);
		if (campos.equals(""))
			descrip += " (todos los campos)"; 
		else
			descrip += " ( campos " + campos + ")";
		
		return descrip;

	}

	@Override
	public String getHelpText() {
		return "Extrae el valor de acuerdo a la expresion regular (utilizando grupo) ";

	}

	@Override
	public Object[] execute(String transformation, Object[] value)
			throws DataTransformartionException {

		if (value==null)
			return null;
				

		String expresion = getParameter(transformation,1);
		String campos = getParameter(transformation,2);
		
		setCamposToProcess(campos);
		
		Object[] results = new Object[value.length];
		int campIdx = 1;
		for (Object val : value)
		{
			if (processCampo(campIdx)) {
				LOG.debug("Por aplicar a campo " + campIdx + " > " + (val!=null ? val.toString() : "NULL"));
				
				if (val==null) {
					results[campIdx-1] = null;
					campIdx++;
					continue;
				}
				if (!(val instanceof String))
					throw new DataTransformartionException("El elemento a transformar no es de string (" + value.getClass().getName() + ")");
				
				results[campIdx-1] = transform((String)val, expresion);
								
			}
			else
				results[campIdx-1] = val;
			campIdx++;
		}
		
		LOG.debug("Transformation '" + getName() + "' (value=" + value + "): " + expresion );

		return results;
	}

	protected String transform(String value, String expresion) {
		
		Pattern pattern = Pattern.compile(expresion);
		Matcher matcher = pattern.matcher(value);
		
		LOG.debug("Transformation '" + getName() + " > transform: value=" + value + " expresion: " + expresion);
		if (matcher.find()) {
			LOG.debug("Transformation '" + getName() + " > find:" + matcher.group(1));
			return matcher.group(1) ;
		}
		return null;
	}

}
