package com.tfsla.genericImport.transformation;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.genericImport.exception.DataTransformartionException;

public class CaseConditionalTransformation  extends A_dataTransformation implements I_dataTransformation {

    protected static final Log LOG = CmsLog.getLog(ConditionalDataTransformation.class);

	public CaseConditionalTransformation() {
		name = "switch";
		
		parameters.add("valores (separados por coma)");
		parameters.add("reemplazo (separados por coma)");
		parameters.add("valor de lo contrario");		
		parameters.add("campos a aplicar separado por comas (vacio aplica a todos)");

	} 
	

	@Override
	public String getNiceDescription() {
		return "Condicional multiple";
	}

	@Override
	public String getTransformationDescription(String transformation) {
		String[] valores = getParameter(transformation,1).split(",");
		String[] reemplazos = getParameter(transformation,2).split(",");
		String descrip = "reemplazar ";
		int idx = 0;
		for (String valor : valores) {
			descrip += "( " + valor + "-> " + reemplazos[idx] + " ) ";
			idx++;
		}
		descrip += ". Si no encuentra poner " + getParameter(transformation,3);
	
		String campos = getParameter(transformation,4);
		if (campos.equals(""))
			descrip += " (todos los campos)"; 
		else
			descrip += " ( campos " + campos + ")";
		
			return descrip;
	}

	@Override
	public String getHelpText() {
		return "Reemplaza valores por otros si lo encuentra y si no lo encuentra pone un valor final";
	}

	@Override
	public Object[] execute(String transformation, Object[] value) throws DataTransformartionException {


		if (value==null)
			return null;
		
		String[] valores = getParameter(transformation,1).split(",");
		String[] reemplazos = getParameter(transformation,2).split(",");
		String defaultValue = getParameter(transformation,3);
		String campos = getParameter(transformation,4);
		
		setCamposToProcess(campos);

		

		Object[] results = new Object[value.length];
		int campIdx = 1;
		for (Object val : value)
		{
			if (processCampo(campIdx)) {
				int idx = 0;
				boolean encontrado = false;
				for (String compValor : valores) {
					//LOG.debug("Transformation '" + getName() + " / compValor: " + compValor + " - reemplazo:" + reemplazos[idx]);
					//if (val!=null)
					//	LOG.debug("Transformation '" + getName() + " / valor: " + val.toString());
					if  ((val==null && compValor.equals("<NULL>")) || val!=null && val.toString().equals(compValor)) {
						//LOG.debug("Transformation '" + getName() + " / valor: " + val.toString() + " y pasa a ser reemplazado");
						encontrado=true;
						results[campIdx-1] = (reemplazos[idx].equals("<NULL>") ? null : reemplazos[idx]);
						break;
					}
					idx++;	
				}
				if (!encontrado)
					results[campIdx-1] = (defaultValue.equals("<NULL>") ? null : defaultValue);
				
				
			}
			else
				results[campIdx-1] = val;
			campIdx++;
		}

		
		//LOG.debug("Transformation '" + getName() + "' (value=" + value + "): " + getParameter(transformation,1) + " -> " + getParameter(transformation,2) + " ELSE " + getParameter(transformation,3));
		//if (results!=null) {
		//	for (Object res : results) {
		//		LOG.debug("Transformation '" + getName() + " res: " + (res!=null ? res.toString() : "null"));
		//	}
		//}
		
		return results;
	}
	


}
