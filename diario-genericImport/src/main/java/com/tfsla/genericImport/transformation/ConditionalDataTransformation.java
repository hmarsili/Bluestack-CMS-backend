package com.tfsla.genericImport.transformation;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.genericImport.exception.DataTransformartionException;


public class ConditionalDataTransformation extends A_dataTransformation implements I_dataTransformation {

    protected static final Log LOG = CmsLog.getLog(ConditionalDataTransformation.class);

	public ConditionalDataTransformation() {
		name = "conditional";
		
		parameters.add("parametro");
		parameters.add("reemplazo");
		parameters.add("campos a aplicar separado por comas (vacio aplica a todos)");

	} 
	

	@Override
	public String getNiceDescription() {
		return "Remplazo condicional";
	}

	@Override
	public String getTransformationDescription(String transformation) {
		String descrip = "reemplazar si el valor es " + getParameter(transformation,1) + " por " + getParameter(transformation,2);
		
		String campos = getParameter(transformation,3);
		if (campos.equals(""))
			descrip += " (todos los campos)"; 
		else
			descrip += " ( campos " + campos + ")";
		
			return descrip;
	}

	@Override
	public String getHelpText() {
		return "Reemplaza el valor si coincide con el ingresado por el otro";
	}

	@Override
	public Object[] execute(String transformation, Object[] value) throws DataTransformartionException {


		if (value==null)
			return null;
		
		
		String origen = getParameter(transformation,1);
		String destino = getParameter(transformation,2);
		String campos = getParameter(transformation,3);
		
		setCamposToProcess(campos);

		
		//para permitir definir un valor como nulo.
		if (destino.equals("<NULL>"))
			destino = null;

		//para permitir comparar con nulo
		if (origen.equals("<NULL>"))
			origen = null;


		Object[] results = new Object[value.length];
		int campIdx = 1;
		for (Object val : value)
		{
			if (processCampo(campIdx))
				results[campIdx-1] = transform(val, origen, destino);
			else
				results[campIdx-1] = val;
			campIdx++;
		}

		
		//LOG.debug("Transformation '" + getName() + "' (value=" + value + "): " + origen + " -> " + destino);
		
		return results;
	}
	

	protected Object transform(Object value, String origen, String destino) {

		// si el valor es nulo y la comparacion es por nulo retornar el valor destino.
		if (value == null && origen == null)
			return destino;

		if (value instanceof String)
			if (((String)value).equals(origen))
				return new Object[] {destino};
		
		if (value instanceof Integer)
			if (((Integer)value).equals(Integer.parseInt(origen)))
				return new Object[] {destino};
		
		
		return null;

	}

}
