package com.tfsla.genericImport.transformation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.genericImport.exception.DataTransformartionException;

public class RegexRewriteTransformation extends A_dataTransformation implements I_dataTransformation {

    protected static final Log LOG = CmsLog.getLog(ConditionalDataTransformation.class);

	public RegexRewriteTransformation() {
		name = "regexRewrite";
		
		parameters.add("expresion");
		parameters.add("reescritura");
		parameters.add("campos a aplicar separado por comas (vacio aplica a todos)");
		parameters.add("Permitir valor nulo al no matchear la expresión (SI - NO)");
		
	} 

	@Override
	public String getNiceDescription() {
		return "Reecribir texto mediante expresion";
	}

	@Override
	public String getTransformationDescription(String transformation) {
		String descrip = "Reescribir la siguiente expresion "  + getParameter(transformation,1) + " mediante el patron " + getParameter(transformation,2);
		
		String campos = getParameter(transformation,3);
		if (campos.equals(""))
			descrip += " (todos los campos)"; 
		else
			descrip += " ( campos " + campos + ")";
		
		return descrip;

	}

	@Override
	public String getHelpText() {
		return "Reescribir la expresion de acuerdo a una expresion regular";
	}

	@Override
	public Object[] execute(String transformation, Object[] value)
			throws DataTransformartionException {

		if (value==null)
			return null;
		

		String expresion = getParameter(transformation,1);
		String campos = getParameter(transformation,3);
		String allowNull = getParameter(transformation,4);
		boolean allowNullValues = allowNull.trim().toLowerCase() == "yes" || allowNull.trim().toLowerCase() == "si";
		setCamposToProcess(campos);

				
		//LOG.debug("Transformation '" + getName() + "' (value=" + value + "): " + expresion + " -> " + getParameter(transformation,2) );

		Pattern pattern = Pattern.compile(expresion);
		
		Object[] results = new Object[value.length];
		int campIdx = 1;
		for (Object val : value)
		{
			if (processCampo(campIdx)) {
				
				String reescritura = getParameter(transformation,2);

				if (!(val instanceof String))
					throw new DataTransformartionException("El elemento a transformar no es de tipo texto (" + value.getClass().getName() + ")");
				
				String storedValue = (String)val;
				
				Matcher matcher = pattern.matcher(storedValue);
				
				if (matcher.find()) {
					for (int j=1;j<=matcher.groupCount();j++)
					{
						reescritura = reescritura.replaceAll(Pattern.quote("$") + j, matcher.group(j));
					}
					results[campIdx-1] = reescritura;
				}
				else
					if(allowNullValues){ 
						results[campIdx-1] = null;
					}else{
						results[campIdx-1] = storedValue;
					}
								
			}
			else
				results[campIdx-1] = val;
			campIdx++;
		}


		return results;
		
		
	}



}
